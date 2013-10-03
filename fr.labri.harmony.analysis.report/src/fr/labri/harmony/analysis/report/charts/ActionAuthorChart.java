package fr.labri.harmony.analysis.report.charts;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYBarRenderer;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeTableXYDataset;

import fr.labri.harmony.analysis.report.ChartDrawer;
import fr.labri.harmony.core.dao.Dao;
import fr.labri.harmony.core.model.Author;
import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.Source;

public class ActionAuthorChart extends ChartDrawer {

	public ActionAuthorChart(Dao dao) {
		super(dao);
	}

	@Override
	public JFreeChart createChart(Source src) {
		Map<Day, Map<Author, Integer>> authorEvents = new HashMap<>();
		for (Event event : src.getEvents()) {
			Date eventDate = new Date(event.getTimestamp());
			Day day = new Day(eventDate);
			if (!authorEvents.containsKey(day)) authorEvents.put(day, new HashMap<Author, Integer>());
			for (Author author : event.getAuthors()) {
				int last = 0;
				if (authorEvents.get(day).containsKey(author)) last = authorEvents.get(day).get(author);
				last += event.getActions().size();
				authorEvents.get(day).put(author, last);
			}
		}
		TimeTableXYDataset tset = new TimeTableXYDataset();
		for (Day day : authorEvents.keySet())
			for (Author author : authorEvents.get(day).keySet())
				tset.add(day, authorEvents.get(day).get(author), author.getName());
		XYBarRenderer renderer = new StackedXYBarRenderer(0.0);
		renderer.setBarPainter(new StandardXYBarPainter());
		renderer.setDrawBarOutline(false);
		renderer.setShadowVisible(false);
		XYPlot plot = new XYPlot(tset, new DateAxis("Date"), new NumberAxis("Actions"), renderer);
		plot.getDomainAxis().setLowerMargin(0.0);
		plot.getDomainAxis().setUpperMargin(0.0);
		JFreeChart tchart = new JFreeChart(plot);
		tchart.setTitle("Developer actions over time");

		return tchart;
	}

	@Override
	public String getChartName() {
		return "action_author_chart";
	}

}
