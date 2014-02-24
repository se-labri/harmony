package fr.labri.harmony.analysis.report.charts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import fr.labri.harmony.analysis.report.ChartDrawer;
import fr.labri.harmony.core.dao.AbstractDao;
import fr.labri.harmony.core.model.Item;
import fr.labri.harmony.core.model.Source;

public class ItemEditionChart extends ChartDrawer {

	public ItemEditionChart(AbstractDao dao) {
		super(dao);
	}

	@Override
	public JFreeChart createChart(Source src) {
		DefaultCategoryDataset bset1 = new DefaultCategoryDataset();
		List<Item> items1 = new ArrayList<>(src.getItems());
		Collections.sort(items1,Collections.reverseOrder(new ItemComparatorByEdits()));
		List<Item> maxItems1 = items1.subList(0, Math.min(50, items1.size()));
		for(Item item: maxItems1) bset1.addValue(item.getActions().size(), "Edits", item.getNativeId());
		return ChartFactory.createBarChart("Number of editions","Items", "Edits", bset1, PlotOrientation.HORIZONTAL, false,true, false);
	}

	@Override
	public String getChartName() {
		return "item_edition_chart";
	}
	
	private final class ItemComparatorByEdits implements Comparator<Item> {
		@Override
		public int compare(Item i1, Item i2) {
			return Integer.compare(i1.getActions().size(),i2.getActions().size());
		}
	}

}
