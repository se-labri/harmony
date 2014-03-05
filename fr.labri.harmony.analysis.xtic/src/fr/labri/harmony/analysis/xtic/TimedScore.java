package fr.labri.harmony.analysis.xtic;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class TimedScore implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private int id;
	@Basic
	private Long timestamp;
	@Basic
	private Long value;
	
	@ManyToOne
	ListTimedScore list;
	
	public ListTimedScore getList() {
		return list;
	}
	public void setList(ListTimedScore list) {
		this.list = list;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
	public Long getValue() {
		return value;
	}
	public void setValue(Long value) {
		this.value = value;
	}
	public TimedScore() {
		
	}
	public TimedScore(Long timestamp, Long value) {
		this.timestamp = timestamp;
		this.value = value;
	}
	
		
}
