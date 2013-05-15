package fr.labri.harmony.analysis.report;

import org.jfree.chart.JFreeChart;

import fr.labri.harmony.core.dao.Dao;
import fr.labri.harmony.core.model.Source;

public abstract class ChartDrawer {
	
	protected Dao dao;
	
	public ChartDrawer(Dao dao) {
		this.dao = dao;
	}
	
	public abstract JFreeChart createChart(Source src);
	
	public abstract String getChartName();

}
