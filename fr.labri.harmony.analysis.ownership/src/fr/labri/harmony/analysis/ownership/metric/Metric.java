package fr.labri.harmony.analysis.ownership.metric;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Metric {

	public Metric() {
	}
	
	@Id
	@GeneratedValue
	private int id;
	
	private String name;
	private String value;
	
	public Metric(String name, Object value) {
		super();
		this.name = name;
		this.value = value.toString();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	
	
	

}
