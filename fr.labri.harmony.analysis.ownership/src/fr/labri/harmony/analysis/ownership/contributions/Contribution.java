package fr.labri.harmony.analysis.ownership.contributions;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Contribution {

	@Id
	@GeneratedValue
	private int id;

	private String authorId;

	private int churn, touches;

	@ManyToOne
	@JoinColumn(name = "moduleContributionsId")
	private ModuleContributions moduleContributions;

	public Contribution() {

	}

	public Contribution(String authorId, int churn, int touches) {
		super();
		this.authorId = authorId;
		this.churn = churn;
		this.touches = touches;
	}

	public int getChurn() {
		return churn;
	}

	public void setChurn(int churn) {
		this.churn = churn;
	}

	public int getTouches() {
		return touches;
	}

	public void setTouches(int touches) {
		this.touches = touches;
	}

	public String getAuthorId() {
		return authorId;
	}

	public void setAuthorId(String authorId) {
		this.authorId = authorId;
	}

	public String getItemId() {
		return moduleContributions.getModuleName();
	}

	public ModuleContributions getItemContributions() {
		return moduleContributions;
	}

	public void setItemContributions(ModuleContributions itemContributions) {
		this.moduleContributions = itemContributions;
	}

}
