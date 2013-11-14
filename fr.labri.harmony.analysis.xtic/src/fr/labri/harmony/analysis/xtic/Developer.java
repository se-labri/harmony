package fr.labri.harmony.analysis.xtic;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MapKeyClass;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import fr.labri.CountersSkills;
import fr.labri.harmony.analysis.xtic.aptitude.PatternAptitude;


@Entity
public class Developer {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Developer other = (Developer) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Id
	@GeneratedValue
	private int id;

	@Basic
	private String name;

	@Basic
	private String email;

	@OneToMany(mappedBy="dev",cascade = CascadeType.ALL)
	@MapKeyClass(PatternAptitude.class)
	private Map<PatternAptitude, ListTimedScore> score = new HashMap<PatternAptitude, ListTimedScore>();

	public Map<PatternAptitude, ListTimedScore> getScore() {
		return score;
	}

	public void setScore(Map<PatternAptitude, ListTimedScore> score) {
		this.score = score;
	}

	@Transient
	private CountersSkills<PatternAptitude> skills = new CountersSkills<PatternAptitude>();

	private int nbCommit = 0;

	public Developer() {
	}

	public void incrCommit() {
		this.nbCommit++;
	}

	public int getNbCommit() {
		return nbCommit;
	}

	public void setNbCommit(int nbCommit) {
		this.nbCommit = nbCommit;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setSkills(CountersSkills<PatternAptitude> skills) {
		this.skills = skills;
	}

	public Developer(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void addAptitudeScore(PatternAptitude aptitude, ListTimedScore score) {
		score.setDev(this);
		this.score.put(aptitude, score);
	}

	public void addAptitudePattern(PatternAptitude ac, long timestamp, long skill_value) {
		if(skill_value==0L)
			return;
		skills.add(ac, timestamp, skill_value);
	}
	

	public void addIfAbsentAptitudePattern(PatternAptitude aptitude, long timestamp) {
		this.score.get(aptitude).addIfAbsent(timestamp);
	}


	public CountersSkills<PatternAptitude> getSkills() {
		return skills;
	}


	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getDescription() {
		return this.email + "<br/>(" + this.nbCommit + " commits)";
	}

	public void mergeDeveloper(Developer other) {
		for(PatternAptitude ap : other.getScore().keySet()) {
			if(!this.score.containsKey(ap))
				this.score.put(ap, other.getScore().get(ap));
			else {
				for(TimedScore ts : other.getScore().get(ap).getList()) {
					this.score.get(ap).addValues(ts.getTimestamp(), ts.getValue());
				}
			}
		}
		this.nbCommit += other.getNbCommit();
	}
}