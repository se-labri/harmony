package fr.labri.harmony.analysis.report;

import org.jfree.chart.JFreeChart;

import fr.labri.harmony.core.dao.AbstractDao;
import fr.labri.harmony.core.model.Source;

public abstract class ChartDrawer {
	
	protected AbstractDao dao;
	
	public ChartDrawer(AbstractDao dao) {
		this.dao = dao;
	}
	
	public abstract JFreeChart createChart(Source src);
	
	public abstract String getChartName();

}
