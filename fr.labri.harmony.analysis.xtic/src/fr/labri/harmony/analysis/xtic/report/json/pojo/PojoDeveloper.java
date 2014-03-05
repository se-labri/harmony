package fr.labri.harmony.analysis.xtic.report.json.pojo;


import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;


@Entity(value="Developer", noClassnameStored=true)
public class PojoDeveloper  {
	
	@Id ObjectId id;
	
	String login;
	//@Reference
	List<ObjectId> repositories = new ArrayList<ObjectId>();
	
	public String getLogin() {
		return login;
	}
	
	public void setLogin(String login) {
		this.login = login;
	}

	public List<ObjectId> getRepositories() {
		return repositories;
	}

	public void setRepositories(List<ObjectId> repositories) {
		this.repositories = repositories;
	}

	public PojoDeveloper(){
	}

	
	
}
