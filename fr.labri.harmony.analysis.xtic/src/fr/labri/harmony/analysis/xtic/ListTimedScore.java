package fr.labri.harmony.analysis.xtic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;


@Entity
public class ListTimedScore implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue
	private int id;
	
	@ManyToOne
	Developer dev;
	
	@OneToMany(cascade=CascadeType.ALL)
	private List<TimedScore> list;
	
	public Developer getAptitude() {
		return dev;
	}

	public void setDev(Developer aptitude) {
		this.dev = aptitude;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public List<TimedScore> getList() {
		return list;
	}

	public void setList(List<TimedScore> list) {
		this.list = list;
	}

	

	public ListTimedScore(){
		list = new ArrayList<>();
	}

	public boolean addValues(long timestamp, long value) {
		for(TimedScore ts : list)
			if(Long.compare(timestamp,ts.getTimestamp())==0){
				ts.setValue(ts.getValue()+value);
				return true;
			}
		return list.add(new TimedScore(timestamp,value));
	}

	public List<TimedScore> scoreSortedByTime() {
		Collections.sort(list, new TimedScoreSorter());
		return getList();
	}

	public long getScore() {
		long score = 0;
		for(TimedScore ts : list)
			score += ts.getValue();
		return score;
	}
	
	private boolean hasTimestamp(long time) {
		for(TimedScore ts : list)
			if(Long.compare(time,ts.getTimestamp())==0)
				return true;
		return false;
	}
	

	public boolean addIfAbsent(Long timestamp) {
		if(!hasTimestamp(timestamp)){
			return addValues(timestamp,0L);
		}
		else
			return false;
	}
	
	public Set<Long> getTimestamps() {
		Set<Long> times = new HashSet<Long>();
		for(TimedScore ts : list)
			times.add(ts.getTimestamp());
		return times;
	}
}

class TimedScoreSorter implements Comparator<TimedScore> {

	@Override
	public int compare(TimedScore arg0, TimedScore arg1) {
		return Long.compare(arg0.getTimestamp(), arg1.getTimestamp());
	}

}
