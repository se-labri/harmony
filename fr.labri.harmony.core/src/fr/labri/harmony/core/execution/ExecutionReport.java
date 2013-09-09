package fr.labri.harmony.core.execution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class ExecutionReport {

	public ExecutionReport() {
		sourceExecutionReports = new ArrayList<>();
	}

	@Id
	@GeneratedValue
	private int id;

	@Temporal(value = TemporalType.DATE)
	private Date executionDate;

	@OneToMany
	private Collection<SourceExecutionReport> sourceExecutionReports;

	private long totalSystemTimeMillis;
	private long totalUserTimeMillis;
	
	public boolean add(SourceExecutionReport e) {
		return sourceExecutionReports.add(e);
	}

	public int getId() {
		return id;
	}

	public Date getExecutionDate() {
		return executionDate;
	}

	public void setExecutionDate(Date executionDate) {
		this.executionDate = executionDate;
	}

	public Collection<SourceExecutionReport> getSourceExecutionReports() {
		return sourceExecutionReports;
	}

	public void setSourceExecutionReports(Collection<SourceExecutionReport> sourceExecutionReports) {
		this.sourceExecutionReports = sourceExecutionReports;
	}

	public long getTotalSystemTimeMillis() {
		return totalSystemTimeMillis;
	}

	public void setTotalSystemTimeMillis(long totalSystemTimeMillis) {
		this.totalSystemTimeMillis = totalSystemTimeMillis;
	}

	public long getTotalUserTimeMillis() {
		return totalUserTimeMillis;
	}

	public void setTotalUserTimeMillis(long totalUserTimeMillis) {
		this.totalUserTimeMillis = totalUserTimeMillis;
	}

}
