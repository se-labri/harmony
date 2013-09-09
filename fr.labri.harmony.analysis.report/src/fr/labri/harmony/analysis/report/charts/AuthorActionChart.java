package fr.labri.harmony.analysis.report.charts;


import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import fr.labri.harmony.analysis.report.ChartDrawer;
import fr.labri.harmony.core.dao.Dao;
import fr.labri.harmony.core.model.Author;
import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.Source;

public class AuthorActionChart extends ChartDrawer {

	public AuthorActionChart(Dao dao) {
		super(dao);
	}

	@Override
	public JFreeChart createChart(Source src) {
		DefaultCategoryDataset dset = new DefaultCategoryDataset();
		for (Author author: src.getAuthors()) {
			int actions = 0;
			for (Event e: author.getEvents()) actions += e.getActions().size();
			dset.setValue(actions,"Actions",author.getName());
		}
		return ChartFactory.createBarChart("Number of actions", "Authors", "Actions", dset, PlotOrientation.HORIZONTAL, false, true, false);
	}

	@Override
	public String getChartName() {
		return "author_action_chart";
	}

}
