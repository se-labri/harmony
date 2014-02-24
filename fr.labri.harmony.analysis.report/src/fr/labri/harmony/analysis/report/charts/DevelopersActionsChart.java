package fr.labri.harmony.analysis.report.charts;

import java.util.HashMap;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

import fr.labri.harmony.analysis.report.ChartDrawer;
import fr.labri.harmony.core.dao.AbstractDao;
import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.Source;

public class DevelopersActionsChart extends ChartDrawer {

	public DevelopersActionsChart(AbstractDao dao) {
		super(dao);
	}

	@Override
	public JFreeChart createChart(Source src) {
		HashMap<String, Integer> actionsPerAuthor = new HashMap<>();

		for (Event e : src.getEvents()) {
			String author = e.getAuthors().get(0).getName();
			Integer count = actionsPerAuthor.get(author);
			if (count == null) count = 0;
			count += e.getActions().size();
			actionsPerAuthor.put(author, count);
		}

		DefaultPieDataset dataset = new DefaultPieDataset();
		for (String author : actionsPerAuthor.keySet()) {
			dataset.setValue(author, actionsPerAuthor.get(author));
		}

		JFreeChart chart = ChartFactory.createPieChart("Number of Actions per Developer", dataset, true, true, false);

		return chart;
	}

	@Override
	public String getChartName() {
		return "DevelopersActions";
	}

}
