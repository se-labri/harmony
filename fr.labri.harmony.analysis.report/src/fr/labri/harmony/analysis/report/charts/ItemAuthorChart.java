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

public class ItemAuthorChart extends ChartDrawer {
	
	public ItemAuthorChart(AbstractDao dao) {
		super(dao);
	}

	@Override
	public JFreeChart createChart(Source src) {
		DefaultCategoryDataset bset2 = new DefaultCategoryDataset();
		List<Item> items2 = new ArrayList<>(src.getItems());
		Collections.sort(items2,Collections.reverseOrder(new ItemComparatorByAuthors()));
		List<Item> maxItems2 = items2.subList(0, Math.min(50, items2.size()));
		for(Item item: maxItems2) bset2.addValue(item.getAuthors().size(), "Authors", item.getNativeId());
		return ChartFactory.createBarChart("Number of authors","Items", "Authors", bset2, PlotOrientation.HORIZONTAL, false, true, false);
	}

	@Override
	public String getChartName() {
		return "item_author_chart";
	}
	
	private final class ItemComparatorByAuthors implements Comparator<Item> {
		@Override
		public int compare(Item i1, Item i2) {
			return Integer.compare(i1.getAuthors().size(),i2.getAuthors().size());
		}
	}

}
