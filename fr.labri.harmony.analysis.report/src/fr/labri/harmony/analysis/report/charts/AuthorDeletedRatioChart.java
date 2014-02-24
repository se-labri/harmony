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

public class AuthorDeletedRatioChart extends ChartDrawer {

	public AuthorDeletedRatioChart(AbstractDao dao) {
		super(dao);
	}

	@Override
	public JFreeChart createChart(Source src) {
		DefaultCategoryDataset dset = new DefaultCategoryDataset();
		for (Author author: src.getAuthors()) {
			int total = 0;
			int create = 0;
			for (Event e: author.getEvents()) {
				for (Action ac : e.getActions()) {
					if (ac.getKind() == ActionKind.Create) {
						total++;
						Set<Author> deletors = new HashSet<>();
						for (Action aci: ac.getItem().getActions()) {
							if (aci.getKind() == ActionKind.Delete) {
								deletors.addAll(aci.getEvent().getAuthors());
								break;
							}
						}
						if (!deletors.contains(author)) create++;
					}
				}
			}
			
			dset.addValue((double)create/(double) total,"NonDeletedCreationRatio",author.getName());
		}
		return ChartFactory.createBarChart("Ratio of non self-deleted creations", "Authors", "Creations", dset, PlotOrientation.HORIZONTAL, false, true, false);
	}

	@Override
	public String getChartName() {
		return "author_nonself_deleted_ratio_chart";
	}

}
