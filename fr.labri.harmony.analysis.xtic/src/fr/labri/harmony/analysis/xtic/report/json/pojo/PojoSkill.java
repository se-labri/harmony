package fr.labri.harmony.analysis.xtic.report.json.pojo;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;


@Entity(value="Skill", noClassnameStored=true)
public class PojoSkill {
	
	@Id ObjectId id;
	//@Reference
	private List<ObjectId> patterns = new ArrayList<ObjectId>();
	private String name;
	private String description;
	
	public PojoSkill() {
	}
	
	public List<ObjectId> getPatterns() {
		return patterns;
	}

	public void setPatterns(List<ObjectId> patterns) {
		this.patterns = patterns;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String desc) {
		this.description = desc;
	}

	
	
	
}
