package fr.labri.harmony.analysis.xtic.report.json;


import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;

import fr.labri.harmony.analysis.xtic.Developer;
import fr.labri.harmony.analysis.xtic.TimedScore;
import fr.labri.harmony.analysis.xtic.aptitude.Aptitude;
import fr.labri.harmony.analysis.xtic.aptitude.PatternAptitude;
import fr.labri.harmony.analysis.xtic.report.Report;
import fr.labri.harmony.analysis.xtic.report.json.pojo.*;
import fr.labri.harmony.core.dao.Dao;
import fr.labri.harmony.core.model.Source;

public class ReportJSONMongo extends Report {

	String database="";

	public ReportJSONMongo(String path) {
		super(path);
	}

	public ReportJSONMongo(String path, String database) {
		super(path);
		this.database = database;
	}

	@Override
	public void printReport(List<Aptitude> aptitudes, List<Developer> developers) {
	}

	@Override
	public void printReport(List<Source> sources, List<Aptitude> aptitudes, List<Developer> developers) {
	}

	public void printReport(List<Source> sources, Dao dao) {


		//unique id for aptitudes => all the ids of aptitudes
		Map<Integer,List<Aptitude>> merged_aptitudes_index = new HashMap<Integer, List<Aptitude>>();
		//unique id for aptitudes => all the ids of aptitudes
		Map<Integer,List<PatternAptitude>> merged_Paptitudes_index = new HashMap<Integer, List<PatternAptitude>>();

		//build indexes 
		for(Source src : sources) {
			//get data
			List<Developer> devs = dao.getData("xtic", Developer.class, src);
			for(Developer dev : devs) {
				if(dev!=null && dev.getScore()!=null)
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


		//Pojo Object
		Map<Integer,PojoRepository> repositories = new HashMap<Integer, PojoRepository>();
		Map<Integer,PojoDeveloper> developers = new HashMap<Integer, PojoDeveloper>();
		Map<Integer,PojoPattern> patterns = new HashMap<Integer, PojoPattern>();

		Mongo mongo;
		try {
			mongo = new MongoClient("localhost", 27017);

			//Remove old database
			DB db = mongo.getDB(this.database);
			db.dropDatabase();

			Morphia morphia = new Morphia();
			Datastore ds = morphia.createDatastore(mongo, this.database);
			morphia.map(PojoDeveloper.class);
			morphia.map(PojoRepository.class);
			morphia.map(PojoActivity.class);
			morphia.map(PojoPattern.class);
			morphia.map(PojoSkill.class);
			morphia.map(PojoObservation.class);

			Map<String, PojoDeveloper> index = new HashMap<String, PojoDeveloper>();
			for(Source src : sources) {
				List<Developer> devs = dao.getData("xtic", Developer.class, src);
				for(Developer dev : devs) {
					if(dev!=null && dev.getScore()!=null)
						if(index.containsKey(dev.getEmail()) == false) {
							index.put(dev.getEmail(), new PojoDeveloper());
						}
				}
			}


			for(Source src : sources) {
				PojoRepository pr = new PojoRepository();
				pr.setUrl(src.getUrl());
				ds.save(pr);
				List<Developer> devs = dao.getData("xtic", Developer.class, src);
				for(Developer dev : devs) {
					if(dev!=null && dev.getScore()!=null) {
						PojoDeveloper d = index.get(dev.getEmail());
						developers.put(dev.hashCode(), d);
						if(d.getRepositories().contains((ObjectId)ds.getKey(pr).getId()))
							continue;
						d.setLogin(dev.getName());
						d.getRepositories().add((ObjectId)ds.getKey(pr).getId());
						ds.save(d);
						ds.update(pr, ds.createUpdateOperations(PojoRepository.class).add("developers",d));

						//activity
						PojoActivity activity = new PojoActivity();
						activity.setCommit(dev.getNbCommit());
						activity.setDeveloper((ObjectId)ds.getKey(d).getId());
						activity.setRepository((ObjectId)ds.getKey(pr).getId());
						ds.save(activity);
					}
				}
				repositories.put(src.hashCode(), pr);
			}

			//competences
			for(int apt : merged_aptitudes_index.keySet()) {
				Aptitude tmp = merged_aptitudes_index.get(apt).get(0);
				PojoSkill skill = new PojoSkill();
				skill.setName(tmp.getIdName());
				skill.setDescription(tmp.getDescription());
				ds.save(skill);

				for(int apt_P : merged_Paptitudes_index.keySet()) {
					for(PatternAptitude _pattern : merged_Paptitudes_index.get(apt_P)) {
						if(_pattern.getAptitude().hashCode() == apt) {
							PojoPattern pattern = new PojoPattern();
							pattern.setName(_pattern.getIdName());
							pattern.setDescription(_pattern.getDescription());
							pattern.setSkill((ObjectId)ds.getKey(skill).getId());
							ds.save(pattern);
							patterns.put(_pattern.hashCode(), pattern);
							ds.update(skill, ds.createUpdateOperations(PojoSkill.class).add("patterns",pattern));
							break;
						}
					}
				}
			}

			//expressions
			for(int apt : merged_aptitudes_index.keySet()) {
				for(int apt_P : merged_Paptitudes_index.keySet()) {
					for(PatternAptitude pattern : merged_Paptitudes_index.get(apt_P)) {
						if(pattern.getAptitude().hashCode() == apt) {
							for(Source src : sources) {
								//get data
								List<Developer> devs = dao.getData("xtic", Developer.class, src);
								for(Developer dev : devs) {
									if(dev!=null && dev.getScore()!=null) {
										for(PatternAptitude dev_apt : dev.getScore().keySet()) {
											if(dev_apt.hashCode() == apt_P) {
												if(!dev.getScore().get(dev_apt).getList().isEmpty()) {
													for(TimedScore ts : dev.getScore().get(dev_apt).getList()) {
														PojoObservation observation = new PojoObservation();
														observation.setWho(((ObjectId)ds.getKey(developers.get(dev.hashCode())).getId()));
														observation.setWhere(((ObjectId)ds.getKey(repositories.get(src.hashCode())).getId()));
														observation.setWhat(((ObjectId)ds.getKey(patterns.get(dev_apt.hashCode())).getId()));
														observation.setScore(Integer.valueOf(Long.toString(ts.getValue())));
														observation.setWhen(ts.getTimestamp()/1000);
														ds.save(observation);
													}
												}
												break;
											}
										}
									}
								}
							}
							break;
						}
					}
				}
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}