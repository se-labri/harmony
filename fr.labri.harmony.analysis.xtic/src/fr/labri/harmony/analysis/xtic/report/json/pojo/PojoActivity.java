package fr.labri.harmony.analysis.xtic.report.json.pojo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;

@Entity(value="Activity", noClassnameStored=true)
public class PojoActivity {
	
	@Id ObjectId id;
	//@Reference
	private ObjectId developer;
	//@Reference
	private ObjectId repository;
	
	private int commit;
	
	public PojoActivity() {
	}

	public ObjectId getDeveloper() {
		return developer;
	}

	public void setDeveloper(ObjectId developer) {
		this.developer = developer;
	}

	public ObjectId getRepository() {
		return repository;
	}

	public void setRepository(ObjectId repository) {
		this.repository = repository;
	}

	public int getCommit() {
		return commit;
	}

	public void setCommit(int commit) {
		this.commit = commit;
	}
	
	
}
