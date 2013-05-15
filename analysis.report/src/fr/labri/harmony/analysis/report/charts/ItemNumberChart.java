package fr.labri.harmony.analysis.report.charts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import fr.labri.harmony.analysis.report.EventComparator;
import fr.labri.harmony.analysis.report.ChartDrawer;
import fr.labri.harmony.core.dao.Dao;
import fr.labri.harmony.core.model.Action;
import fr.labri.harmony.core.model.ActionKind;
import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.Source;

public class ItemNumberChart extends ChartDrawer {

	public ItemNumberChart(Dao dao) {
		super(dao);
	}

	@SuppressWarnings("deprecation")
	@Override
	public JFreeChart createChart(Source src) {
		TimeSeriesCollection tset = new TimeSeriesCollection();
		TimeSeries sevents = new TimeSeries("Number of items");
		List<Event> events = new ArrayList<>(dao.getEvents(src));
		Collections.sort(events, new EventComparator());
		int number = 0;
		for(Event event: events) {
			dao.refreshElement(event);
			for(Action ac: event.getActions()) {
				if (ac.getKind() == ActionKind.Create) number++;
				else if (ac.getKind() == ActionKind.Delete) number--;
			}
			Date devent = new Date(event.getTimestamp());
			sevents.addOrUpdate(new Day(devent),number);
		}
		
		tset.addSeries(sevents);
		JFreeChart tchart = ChartFactory.createTimeSeriesChart("Number of items over time","Date","Items",tset,true,true,false);
		XYLineAndShapeRenderer trend1 = (XYLineAndShapeRenderer) tchart.getXYPlot().getRenderer();
		trend1.setShapesVisible(true);
		return tchart;
	}

	@Override
	public String getChartName() {
		return "item_number_chart";
	}

}
