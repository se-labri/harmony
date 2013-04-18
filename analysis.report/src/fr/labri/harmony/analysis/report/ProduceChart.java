package fr.labri.harmony.analysis.report;

import org.jfree.chart.JFreeChart;

import fr.labri.harmony.core.Dao;
import fr.labri.harmony.core.model.Source;

public abstract class ProduceChart {
	
	protected Dao dao;
	
	public ProduceChart(Dao dao) {
		this.dao = dao;
	}
	
	public abstract JFreeChart createChart(Source src);
	
	public abstract String getChartName();

}
