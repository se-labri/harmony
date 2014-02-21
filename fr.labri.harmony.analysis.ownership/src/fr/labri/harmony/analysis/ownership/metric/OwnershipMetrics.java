package fr.labri.harmony.analysis.ownership.metric;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import fr.labri.harmony.analysis.ownership.contributions.Contribution;

public class OwnershipMetrics {

	protected Collection<Contribution> contributions;
	private static final double MAJOR_CONTRIBUTOR_THRESHOLD = 0.05;

	private Float ownership = 0f;
	private Integer major = 0;
	private Integer minor = 0;
	private Integer total = 0;
	
	public OwnershipMetrics(Collection<Contribution> contributions) {
		this.contributions = contributions;
		computeMetrics();
	}

	private void computeMetrics() {
		for (Contribution c : contributions)
			total += c.getTouches();
		

		for (Contribution c : contributions) {

			float own = c.getTouches() / (float) total;

			if (own > MAJOR_CONTRIBUTOR_THRESHOLD) major++;
			else minor++;

			if (own > ownership) ownership = own;
		}
	}
	
	public List<Metric> getMetrics() {
		List<Metric> metrics = new ArrayList<Metric>();
		Collections.addAll(metrics, new Metric("Ownership", ownership), new Metric("Major", major), new Metric("Minor", minor), new Metric("Total", major + minor));
		return metrics;
	}	

}
