package fr.labri.harmony.analysis.ownership.metric;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class MetricSet {

	@Id
	@GeneratedValue
	private int id;

	@OneToMany(cascade=CascadeType.ALL)
	private List<Metric> metrics;
	
	private String elementName;
	private Integer itemId;

	@Temporal(TemporalType.TIMESTAMP)
	private Date snapshotDate;

	@Temporal(TemporalType.TIMESTAMP)
	private Date otherDate;

	public MetricSet() {
	}

	public MetricSet(String elementName, int itemId, Date snapshotDate, Date otherDate, List<Metric> metrics) {
		this.setElementName(elementName);
		this.itemId = itemId;
		this.snapshotDate = snapshotDate;
		this.otherDate = otherDate;
		this.metrics = metrics;
	}
	
	public MetricSet(String elementName, Date snapshotDate, Date otherDate, List<Metric> metrics) {
		this.setElementName(elementName);
		this.snapshotDate = snapshotDate;
		this.otherDate = otherDate;
		this.metrics = metrics;
	}


	/*
	 * Getters and Setters
	 */

	public Integer getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public String getElementName() {
		return elementName;
	}

	public void setElementName(String elementName) {
		this.elementName = elementName;
	}

	public List<Metric> getMetrics() {
		return metrics;
	}

	public void setMetrics(List<Metric> metrics) {
		this.metrics = metrics;
	}

	public Date getSnapshotDate() {
		return snapshotDate;
	}

	public void setSnapshotDate(Date snapshotDate) {
		this.snapshotDate = snapshotDate;
	}

	public Date getOtherDate() {
		return otherDate;
	}

	public void setOtherDate(Date otherDate) {
		this.otherDate = otherDate;
	}

	public void setItemId(Integer itemId) {
		this.itemId = itemId;
	}
	

}
