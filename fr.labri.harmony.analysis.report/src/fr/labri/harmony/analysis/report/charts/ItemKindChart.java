package fr.labri.harmony.analysis.report.charts;

import java.util.HashMap;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import fr.labri.harmony.analysis.report.ChartDrawer;
import fr.labri.harmony.core.dao.AbstractDao;
import fr.labri.harmony.core.model.Item;
import fr.labri.harmony.core.model.Source;

public class ItemKindChart extends ChartDrawer {
	
	public ItemKindChart(AbstractDao dao) {
		super(dao);
	}

	@Override
	public JFreeChart createChart(Source src) {
		DefaultCategoryDataset dset = new DefaultCategoryDataset();
		Map<String,Integer> kinds = new HashMap<>();
		
		for (Item item: src.getItems()) {
			if (item.getNativeId().contains(".") && !item.getNativeId().startsWith(".")) {
				String kind = item.getNativeId().substring(item.getNativeId().lastIndexOf("."));
				if (!kinds.containsKey(kind)) kinds.put(kind, 1);
				else kinds.put(kind, kinds.get(kind) + 1);
			}
		}
		
		for (Map.Entry<String,Integer> e: kinds.entrySet()) dset.addValue(e.getValue(), "Number", e.getKey());
		
		return ChartFactory.createBarChart("Number of items","Kind", "Number", dset, PlotOrientation.HORIZONTAL, false,true, false);
	}

	@Override
	public String getChartName() {
		return "item_kind_chart";
	}

}
