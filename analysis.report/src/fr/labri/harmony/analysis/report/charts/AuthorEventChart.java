package fr.labri.harmony.analysis.report.charts;


import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import fr.labri.harmony.analysis.report.ChartDrawer;
import fr.labri.harmony.core.dao.Dao;
import fr.labri.harmony.core.model.Author;
import fr.labri.harmony.core.model.Source;

public class AuthorEventChart extends ChartDrawer {

	public AuthorEventChart(Dao dao) {
		super(dao);
	}

	@Override
	public JFreeChart createChart(Source src) {
		DefaultCategoryDataset dset = new DefaultCategoryDataset();
		for (Author author: src.getAuthors()) {
			dset.setValue(author.getEvents().size(), "Events", author.getName());
		}
		return ChartFactory.createBarChart("Number of events", "Authors", "Events", dset, PlotOrientation.HORIZONTAL, false, true, false);
	}

	@Override
	public String getChartName() {
		return "author_event_chart";
	}

}
