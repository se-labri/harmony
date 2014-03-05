package fr.labri.harmony.analysis.xtic.report.json.pojo;


import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;

@Entity(value="Pattern", noClassnameStored=true)
public class PojoPattern {
	
	@Id ObjectId id;
	
	private String name;
	private String description;
	
	private ObjectId skill;
	
	public PojoPattern() {
	}

	public ObjectId getSkill() {
		return skill;
	}

	public void setSkill(ObjectId skill) {
		this.skill = skill;
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

	public void setDescription(String description) {
		this.description = description;
	}
	
	
}
