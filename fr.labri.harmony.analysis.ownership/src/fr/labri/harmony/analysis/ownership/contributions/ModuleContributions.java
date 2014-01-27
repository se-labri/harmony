package fr.labri.harmony.analysis.ownership.contributions;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class ModuleContributions {

	@Id
	@GeneratedValue
	private int id;

	private String moduleName;

	@Temporal(TemporalType.TIMESTAMP)
	private Date fromDate;

	@Temporal(TemporalType.TIMESTAMP)
	private Date toDate;

	public ModuleContributions() {
		contributions = new HashMap<>();
	}

	public ModuleContributions(String moduleName, Date fromDate, Date toDate) {
		this();
		this.fromDate = fromDate;
		this.toDate = toDate;
		this.moduleName = moduleName;
	}

	@OneToMany(mappedBy = "moduleContributions", cascade = CascadeType.ALL)
	@MapKeyColumn(name = "authorNativeId")
	private Map<String, Contribution> contributions;

	public Collection<Contribution> getContributions() {
		return contributions.values();
	}

	public String getModuleName() {
		return moduleName;
	}

	public Date getFromDate() {
		return fromDate;
	}

	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

	public Date getToDate() {
		return toDate;
	}

	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}

	public void put(String author, Contribution contrib) {
		contrib.setItemContributions(this);
		contributions.put(author, contrib);
	}

	public Contribution getContribution(String author) {
		return contributions.get(author);
	}

}
