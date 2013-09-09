package fr.labri.harmony.analysis.report.charts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.util.ShapeUtilities;

import fr.labri.harmony.analysis.cloc.ClocEntries;
import fr.labri.harmony.analysis.cloc.ClocEntry;
import fr.labri.harmony.analysis.report.EventComparator;
import fr.labri.harmony.analysis.report.ChartDrawer;
import fr.labri.harmony.core.dao.Dao;
import fr.labri.harmony.core.model.Data;
import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.Source;

public class ClocChart extends ChartDrawer {

	public ClocChart(Dao dao) {
		super(dao);
	}

	@SuppressWarnings("deprecation")
	@Override
	public JFreeChart createChart(Source src) {
		XYSeriesCollection clocSeries = new XYSeriesCollection();
		XYSeries java = new XYSeries("Java CLOC");
		int nb = 0;
		List<Event> events = new ArrayList<>(src.getEvents());
		Collections.sort(events, new EventComparator());
		for(Event event: events) {
			ClocEntries entries = dao.getData("cloc", ClocEntries.class, Data.EVENT, event.getId());
			ClocEntry entry = entries.getEntry("Java");
			java.add(nb, entry.getCode());
		}
		clocSeries.addSeries(java);
		JFreeChart chart = ChartFactory.createXYLineChart("Evolution of CLOC","Date","Items",clocSeries,PlotOrientation.HORIZONTAL,true,true,false);
		XYSplineRenderer rend = new XYSplineRenderer();
		rend.setShape(ShapeUtilities.createDiamond(1F));
		chart.getXYPlot().setRenderer(rend);
		return chart;
	}

	@Override
	public String getChartName() {
		return "cloc_chart";
	}

}
