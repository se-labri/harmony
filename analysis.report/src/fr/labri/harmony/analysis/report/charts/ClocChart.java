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

import fr.labri.harmony.analysis.cloc.ClocEntries;
import fr.labri.harmony.analysis.cloc.ClocEntry;
import fr.labri.harmony.analysis.report.EventComparator;
import fr.labri.harmony.analysis.report.ProduceChart;
import fr.labri.harmony.core.Dao;
import fr.labri.harmony.core.model.Data;
import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.Source;

public class ClocChart extends ProduceChart {

	public ClocChart(Dao dao) {
		super(dao);
	}

	@SuppressWarnings("deprecation")
	@Override
	public JFreeChart createChart(Source src) {
		TimeSeriesCollection tset = new TimeSeriesCollection();
		TimeSeries sevents = new TimeSeries("Java CLOC");
		List<Event> events = new ArrayList<>(dao.getEvents(src));
		Collections.sort(events, new EventComparator());
		for(Event event: events) {
			dao.refreshElement(event);
			ClocEntries entries = dao.getData("cloc", ClocEntries.class, Data.EVENT, event.getId());
			ClocEntry entry = entries.getEntry("Java");
			Date devent = new Date(event.getTimestamp());
			sevents.addOrUpdate(new Day(devent),entry.getCode());
		}
		
		tset.addSeries(sevents);
		JFreeChart tchart = ChartFactory.createTimeSeriesChart("Number of items over time","Date","Items",tset,true,true,false);
		XYLineAndShapeRenderer trend1 = (XYLineAndShapeRenderer) tchart.getXYPlot().getRenderer();
		trend1.setShapesVisible(true);
		return tchart;
	}

	@Override
	public String getChartName() {
		return "cloc_chart";
	}

}
