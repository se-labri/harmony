package fr.labri.harmony.analysis.report.charts;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import fr.labri.harmony.analysis.report.ChartDrawer;
import fr.labri.harmony.core.dao.AbstractDao;
import fr.labri.harmony.core.model.Action;
import fr.labri.harmony.core.model.ActionKind;
import fr.labri.harmony.core.model.Author;
import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.Source;

public class AuthorItemCreateChart extends ChartDrawer {

	public AuthorItemCreateChart(AbstractDao dao) {
		super(dao);
	}

	@Override
	public JFreeChart createChart(Source src) {
		DefaultCategoryDataset dset = new DefaultCategoryDataset();
		for (Author author: src.getAuthors()) {
			int create = 0;
			for (Event e: author.getEvents()) for (Action ac : e.getActions()) if (ac.getKind() == ActionKind.Create) create++;
			
			dset.addValue(create,"Actions",author.getName());
		}
		return ChartFactory.createBarChart("Number of create actions", "Authors", "Actions", dset, PlotOrientation.HORIZONTAL, false, true, false);
	}

	@Override
	public String getChartName() {
		return "author_create_chart";
	}

}
