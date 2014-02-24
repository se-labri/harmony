package fr.labri.harmony.analysis.report.charts;

import java.util.HashSet;
import java.util.Set;

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

public class AuthorItemNonSelfDeleteChart extends ChartDrawer {

	public AuthorItemNonSelfDeleteChart(AbstractDao dao) {
		super(dao);
	}

	public JFreeChart createChart(Source src) {
		DefaultCategoryDataset dset = new DefaultCategoryDataset();
		for (Author author: src.getAuthors()) {
			int create = 0;
			for (Event e: author.getEvents()) {
				for (Action ac : e.getActions()) {
					if (ac.getKind() == ActionKind.Delete) {
						Set<Author> creators = new HashSet<>();
						for (Action aci: ac.getItem().getActions()) {
							if (aci.getKind() == ActionKind.Create) {
								creators.addAll(aci.getEvent().getAuthors());
								break;
							}
						}
						if (!creators.contains(author)) create++;
					}
				}
			}
			
			dset.addValue(create,"DeletedItems",author.getName());
		}
		return ChartFactory.createBarChart("Number of non-self created deleted items", "Authors", "Deletions", dset, PlotOrientation.HORIZONTAL, false, true, false);
	}

	@Override
	public String getChartName() {
		return "author_nonself_delete_chart";
	}

}
