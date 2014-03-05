package fr.labri.harmony.analysis.xtic.aptitude;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import fr.labri.Counters;
import fr.labri.harmony.core.model.Action;

@Entity
public class Aptitude {
	
	public static final boolean CONTENT_PRESENCE_DEFAULT = true;
	public static final String CONTENT_DIRECTION_DEFAULT = "target";

	
	@Id
	@GeneratedValue
	private int id;
	private String idName;
	private String description = "";
	@OneToMany(cascade=CascadeType.ALL)
	private List<PatternAptitude> patterns = new ArrayList<PatternAptitude>();

	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	public String getIdName() {
		return idName;
	}

	public void setIdName(String idName) {
		this.idName = idName;
	}



	public Aptitude() {
	}


	public void setId(int id) {
		this.id = id;
	}
	
	public Aptitude(String idName, String description) {
		this.idName = idName;
		this.description = description;
	}
	
	public Aptitude(String idName, String description, List<PatternAptitude> patterns) {
		this.idName = idName;
		this.patterns = patterns;
		this.description = description;
		for(PatternAptitude ap : this.patterns)
			ap.setAptitude(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((idName == null) ? 0 : idName.hashCode());
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
		Aptitude other = (Aptitude) obj;
		if (idName == null) {
			if (other.idName != null)
				return false;
		} else if (!idName.equals(other.idName))
			return false;
		return true;
	}

	public int getId() {
		return this.id;
	}

	public boolean patternsAcceptFile(Action src, Action tgt) {
		for (PatternAptitude p : this.patterns)
			if (p.acceptFile(src, tgt)) {
				return true;
			}
		return false;
	}

	public void addPatterns(Collection<PatternAptitude> col) {
		col.addAll(patterns);
	}

	public Long computeScore(Counters<Aptitude> map) {
//		return formula.apply(data);	//FIXME
		return map.total();
	}


	public double computeNewScore(Map<Aptitude, Long> map) {
//		return formula.apply(data);	//FIXME
		long res = 0;
		for(long l: map.values())
			res += l;
		return res;
	}

	
	@Override
	public String toString() {
		return idName;
	}

	public void setFormula(Formula formula) {
		// this.formula = formula;
	}
	
	public List<PatternAptitude> getPatterns(){
		return patterns;
	}

}