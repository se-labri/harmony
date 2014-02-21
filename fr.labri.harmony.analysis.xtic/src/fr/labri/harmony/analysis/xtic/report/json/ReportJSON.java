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


			//unique id for aptitudes => all the ids of aptitudes
			Map<Integer,List<Aptitude>> merged_aptitudes_index = new HashMap<Integer, List<Aptitude>>();
			//unique id for aptitudes => all the ids of aptitudes
			Map<Integer,List<PatternAptitude>> merged_Paptitudes_index = new HashMap<Integer, List<PatternAptitude>>();

			//we do not merge developers here
			Map<Developer,Source> index_dev_source = new HashMap<Developer, Source>();
			for(Source src : sources) {
				//get data
				List<Developer> devs = dao.getData("xtic", Developer.class, src);
				for(Developer dev : devs) {
					index_dev_source.put(dev, src);
				}
			}
			// Node : developers


			jGenerator.writeStartObject(); 
			jGenerator.writeFieldName("repositories"); 
			jGenerator.writeStartArray(); 
			for(Source src : sources) {
				jGenerator.writeStartObject();
				jGenerator.writeNumberField("id", src.getId()); 
				jGenerator.writeStringField("url", src.getUrl()); 
				jGenerator.writeEndObject(); 
			}
			jGenerator.writeEndArray(); 


			// Node : developers

			jGenerator.writeFieldName("developers"); 
			jGenerator.writeStartArray(); 
			for(Source src : sources) {
				//get data
				List<Developer> devs = dao.getData("xtic", Developer.class, src);
				for(Developer dev : devs) {
					jGenerator.writeStartObject();
					jGenerator.writeNumberField("id", dev.getId()); 
					jGenerator.writeStringField("name", dev.getName()); 
					jGenerator.writeStringField("email", dev.getEmail()); 

					//array of working repo

					jGenerator.writeFieldName("repositories"); 
					jGenerator.writeStartArray(); 
					jGenerator.writeStartObject();
					jGenerator.writeNumberField("repository", index_dev_source.get(dev).getId()); 
					jGenerator.writeNumberField("commits", dev.getNbCommit());
					jGenerator.writeEndObject(); 
					jGenerator.writeEndArray(); 

					jGenerator.writeEndObject(); 

				}
			}
			jGenerator.writeEndArray(); 

			//Merge Skill & Patterns
			for(Source src : sources) {
				//get data
				List<Developer> devs = dao.getData("xtic", Developer.class, src);
				for(Developer dev : devs) {
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
			}

			int cpt = 1 ;
			Map<Integer,Integer> ids_aptitude_patterns = new HashMap<Integer, Integer>();
			for(int apt : merged_aptitudes_index.keySet()) {
				ids_aptitude_patterns.put(apt, cpt);
				cpt++;
			}
			for(int apt_P : merged_Paptitudes_index.keySet()) {
				ids_aptitude_patterns.put(apt_P, cpt);
				cpt++;
			}

			//
			//			//competences
			jGenerator.writeFieldName("skill"); // "messages" :
			jGenerator.writeStartArray();
			for(int apt : merged_aptitudes_index.keySet()) {
				Aptitude tmp = merged_aptitudes_index.get(apt).get(0);
				jGenerator.writeStartObject(); // {
				jGenerator.writeNumberField("id", ids_aptitude_patterns.get(apt));
				jGenerator.writeStringField("name", tmp.getIdName());
				jGenerator.writeStringField("desc", tmp.getDescription());
				jGenerator.writeEndObject(); // {
			}
			jGenerator.writeEndArray();

			jGenerator.writeFieldName("pattern"); // "messages" :
			jGenerator.writeStartArray(); // [
			for(int apt_P : merged_Paptitudes_index.keySet()) {
				for(PatternAptitude pattern : merged_Paptitudes_index.get(apt_P)) {
					jGenerator.writeStartObject();
					jGenerator.writeNumberField("id", ids_aptitude_patterns.get(apt_P)); 
					jGenerator.writeNumberField("skill", ids_aptitude_patterns.get(pattern.getAptitude().hashCode())); 
					jGenerator.writeStringField("name", pattern.getIdName());
					jGenerator.writeStringField("desc", pattern.getDescription());
					jGenerator.writeEndObject();
					break;
				}
			}
			jGenerator.writeEndArray();


			//expressions
			jGenerator.writeFieldName("observations"); // "messages" :
			jGenerator.writeStartArray();
			int limit = 0;
			for(int apt : merged_aptitudes_index.keySet()) {
				for(int apt_P : merged_Paptitudes_index.keySet()) {
					for(PatternAptitude pattern : merged_Paptitudes_index.get(apt_P)) {
						if(pattern.getAptitude().hashCode() == apt) {
							for(Source src : sources) {
								//get data
								List<Developer> devs = dao.getData("xtic", Developer.class, src);
								for(Developer dev : devs) {
									for(PatternAptitude dev_apt : dev.getScore().keySet()) {
										if(dev_apt.hashCode() == apt_P) {
											if(!dev.getScore().get(dev_apt).getList().isEmpty()) {
												for(TimedScore ts : dev.getScore().get(dev_apt).getList()) {
													if(limit >= 10)
														break;
													jGenerator.writeStartObject();
													jGenerator.writeNumberField("what", ids_aptitude_patterns.get(apt));
													jGenerator.writeNumberField("where", src.getId()); 
													jGenerator.writeNumberField("who", dev.getId());
													jGenerator.writeNumberField("when", ts.getTimestamp()/1000);
													jGenerator.writeNumberField("amount", ts.getValue());
													jGenerator.writeEndObject();
													limit++;
												}
											}
											break;
										}
									}

								}
							}
							break;
						}
					}
				}
			}
			jGenerator.writeEndArray();

			jGenerator.writeEndObject();
			jGenerator.close();

		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

