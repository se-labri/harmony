package fr.labri.harmony.analysis.xtic.report.json.pojo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;

@Entity(value="Observation", noClassnameStored=true)
public class PojoObservation {
	
	@Id ObjectId id;
	//@Reference
	private ObjectId who;
	//@Reference
	private ObjectId where;
	//@Reference
	private ObjectId what;
	
	private long when;
	private int score;
	
	public PojoObservation() {
	}

	public ObjectId getWho() {
		return who;
	}

	public void setWho(ObjectId who) {
		this.who = who;
	}

	public ObjectId getWhere() {
		return where;
	}

	public void setWhere(ObjectId where) {
		this.where = where;
	}

	public ObjectId getWhat() {
		return what;
	}

	public void setWhat(ObjectId what) {
		this.what = what;
	}

	public long getWhen() {
		return when;
	}

	public void setWhen(long when) {
		this.when = when;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}
	
}
