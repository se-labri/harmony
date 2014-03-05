package fr.labri.harmony.analysis.xtic.report.json.pojo;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;

import fr.labri.harmony.analysis.xtic.Developer;

@Entity(value="Repository", noClassnameStored=true)
public class PojoRepository {
	
	@Id ObjectId id;
	//@Reference(ignoreMissing=true)
	List<ObjectId> developers = new ArrayList<ObjectId>();
	
	String url;
	public List<ObjectId> getDevs() {
		return developers;
	}
	public void setDevs(List<ObjectId> devs) {
		this.developers = devs;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}

}
