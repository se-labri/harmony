package fr.labri.harmony.analysis.xtic.report.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import fr.labri.harmony.analysis.xtic.Developer;
import fr.labri.harmony.analysis.xtic.TimedScore;
import fr.labri.harmony.analysis.xtic.aptitude.Aptitude;
import fr.labri.harmony.analysis.xtic.aptitude.PatternAptitude;
import fr.labri.harmony.analysis.xtic.report.Report;
import fr.labri.harmony.analysis.xtic.report.UtilsDeveloper;
import fr.labri.harmony.core.dao.Dao;
import fr.labri.harmony.core.model.Source;

public class ReportJSON extends Report {

	public ReportJSON(String path) {
		super(path);
	}

	@Override
	public void printReport(List<Aptitude> aptitudes, List<Developer> developers) {
	}

	@Override
	public void printReport(List<Source> sources, List<Aptitude> aptitudes, List<Developer> developers) {
	}

	public void printReport(List<Source> sources, Dao dao) {
		try {

			JsonFactory jfactory = new JsonFactory();

			/*** write to file ***/
			JsonGenerator jGenerator = jfactory.createGenerator(ps);
			jGenerator.writeStartObject(); // {
			//Dépôts
			jGenerator.writeFieldName("repositories"); // "messages" :
			jGenerator.writeStartArray(); // [

			//unique id for aptitudes => all the ids of aptitudes
			Map<Integer,List<Aptitude>> merged_aptitudes_index = new HashMap<Integer, List<Aptitude>>();
			//unique id for aptitudes => all the ids of aptitudes
			Map<Integer,List<PatternAptitude>> merged_Paptitudes_index = new HashMap<Integer, List<PatternAptitude>>();


			for(Source src : sources) {
				//get data
				List<Developer> devs = dao.getData("xtic", Developer.class, src);
				System.out.println(src.getUrl()+" "+devs.size());
	

				for(Developer dev : devs) {
					System.out.println(dev.getId());
					for(PatternAptitude ap : dev.getScore().keySet()) {
						//Aptitude
						if(!merged_aptitudes_index.containsKey(ap.getAptitude().hashCode()))
							merged_aptitudes_index.put(ap.getAptitude().hashCode(), new ArrayList<Aptitude>());
						merged_aptitudes_index.get(ap.getAptitude().hashCode()).add(ap.getAptitude());
						//PatternAptitude
						if(!merged_Paptitudes_index.containsKey(ap.hashCode()))
							merged_Paptitudes_index.put(ap.hashCode(), new ArrayList<PatternAptitude>());
						merged_Paptitudes_index.get(ap.hashCode()).add(ap);
					}
				}	
				jGenerator.writeStartObject(); // {
				jGenerator.writeNumberField("id", src.getId()); 
				jGenerator.writeStringField("name", src.getUrl()); 

				jGenerator.writeFieldName("developers"); // "messages" :
				jGenerator.writeStartArray(); // [
				for(Developer dev : devs) {
					jGenerator.writeStartObject();
					jGenerator.writeNumberField("id", dev.getId()); 
					jGenerator.writeStringField("name", dev.getName()); 
					jGenerator.writeStringField("email", dev.getEmail()); 
					jGenerator.writeNumberField("commits", dev.getNbCommit());
					jGenerator.writeEndObject();
				}
				jGenerator.writeEndArray(); // ]
				jGenerator.writeEndObject(); // }
			}


			jGenerator.writeEndArray(); // ]

			//competences
			jGenerator.writeFieldName("domain_aptitudes"); // "messages" :
			jGenerator.writeStartArray();
			for(int apt : merged_aptitudes_index.keySet()) {
				Aptitude tmp = merged_aptitudes_index.get(apt).get(0);
				jGenerator.writeStartObject(); // {
				jGenerator.writeNumberField("id", apt);
				jGenerator.writeStringField("name", tmp.getIdName());
				jGenerator.writeStringField("desc", tmp.getDescription());
				jGenerator.writeFieldName("concrete_aptitudes"); // "messages" :
				jGenerator.writeStartArray(); // [
				for(int apt_P : merged_Paptitudes_index.keySet()) {
					for(PatternAptitude pattern : merged_Paptitudes_index.get(apt_P)) {
						if(pattern.getAptitude().hashCode() == apt) {
							jGenerator.writeStartObject();
							jGenerator.writeNumberField("id", apt_P); 
							jGenerator.writeStringField("name", pattern.getIdName());
							jGenerator.writeStringField("desc", pattern.getDescription());
							jGenerator.writeEndObject();
							break;
						}
					}
				}
				jGenerator.writeEndArray();
				jGenerator.writeEndObject();
			}
			jGenerator.writeEndArray();

			//expressions
			jGenerator.writeFieldName("aptitude_expressions"); // "messages" :
			jGenerator.writeStartArray();
			Set<Long> tss = new HashSet<>();
			int i = 0;
			for(int apt : merged_aptitudes_index.keySet()) {
				jGenerator.writeStartObject(); // {
				jGenerator.writeNumberField("id_domain_aptitude", apt);
				jGenerator.writeFieldName("expressions");
				jGenerator.writeStartArray(); // [
				System.out.println("domain aptitude "+merged_aptitudes_index.get(apt).get(0).getIdName());
				
				for(int apt_P : merged_Paptitudes_index.keySet()) {
					for(PatternAptitude pattern : merged_Paptitudes_index.get(apt_P)) {
						if(pattern.getAptitude().hashCode() == apt) {
							jGenerator.writeStartObject();
							jGenerator.writeNumberField("id_concrete_aptitude", apt_P); 
							jGenerator.writeFieldName("concrete_expressions"); // "messages" :
							jGenerator.writeStartArray();
							System.out.println("\t aptitude "+merged_Paptitudes_index.get(apt_P).get(0).getIdName());
							for(Source src : sources) {
								//get data
								List<Developer> devs = dao.getData("xtic", Developer.class, src);
								for(Developer dev : devs) {
									System.out.println(dev.getId());
									for(PatternAptitude dev_apt : dev.getScore().keySet()) {
										if(dev_apt.hashCode() == apt_P) {
											if(!dev.getScore().get(dev_apt).getList().isEmpty()) {
												jGenerator.writeStartObject();
												jGenerator.writeNumberField("repo", src.getId()); 
												jGenerator.writeNumberField("dev", dev.getId());
												jGenerator.writeFieldName("scores_times");
												jGenerator.writeStartArray();
												for(TimedScore ts : dev.getScore().get(dev_apt).getList()) {
													jGenerator.writeString(ts.getValue()+"_"+(ts.getTimestamp()/1000));
													tss.add(ts.getTimestamp());
													i++;
												}
												jGenerator.writeEndArray();
												jGenerator.writeEndObject();
											}
											break;
										}
									}

								}
							}
							jGenerator.writeEndArray();
							//Now we write the expressions
							jGenerator.writeEndObject();
							break;
						}
					}
				}
				jGenerator.writeEndArray();
				jGenerator.writeEndObject();
			}
			jGenerator.writeEndArray();

			jGenerator.writeEndObject(); // }
			jGenerator.close();


			System.out.println("Il y a "+i+" expressions");
			System.out.println(tss.size()+" timestamps");
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

