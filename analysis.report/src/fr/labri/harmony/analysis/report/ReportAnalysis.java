package fr.labri.harmony.analysis.report;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.jfree.chart.ChartUtilities;

import com.fasterxml.jackson.databind.node.ObjectNode;

import fr.labri.harmony.analysis.report.charts.ItemNumberChart;
import fr.labri.harmony.core.AbstractAnalysis;
import fr.labri.harmony.core.Dao;
import fr.labri.harmony.core.config.ConfigProperties;
import fr.labri.harmony.core.model.Source;
import fr.labri.harmony.core.source.WorkspaceException;

public class ReportAnalysis extends AbstractAnalysis {

	public ReportAnalysis() {
	}

	public ReportAnalysis(ObjectNode config, Dao dao, Properties properties) {
		super(config, dao, properties);
	}

	@Override
	public void run(Source src) throws WorkspaceException {
		LOGGER.info("Starting reporting analysis on " + src.getUrl() + ".");
		String baseFolder = config.get(ConfigProperties.FOLDERS).get(ConfigProperties.OUT).asText();
		String urlFolder = convertToFolderName(src.getUrl());
		Path outputPath = Paths.get(baseFolder,urlFolder);
		File outputFolder = outputPath.toFile();
		if (!outputFolder.exists()) outputFolder.mkdir();
		for (ProduceChart produce: getProduceCharts()) {
			try {
				ChartUtilities.saveChartAsPNG(new File(outputFolder.getAbsolutePath() + File.separator + produce.getChartName() + ".png"), produce.createChart(src), 1680, 1050);
			} catch (IOException e) {
				throw new WorkspaceException(e);
			}
		}
	}

	public List<ProduceChart> getProduceCharts() {
		List<ProduceChart> produces = new ArrayList<>();
		produces.add(new ItemNumberChart(dao));
		//produces.add(new EventAuthorChart(dao));
		//produces.add(new ActionAuthorChart(dao));
		//produces.add(new AuthorEventChart(dao));
		//produces.add(new ItemEditionChart(dao));
		//produces.add(new ItemAuthorChart(dao));
		//produces.add(new ItemKindChart(dao));
		//produces.add(new AuthorItemCreateChart(dao));
		//produces.add(new AuthorItemDeleteChart(dao));
		//produces.add(new AuthorItemNonSelfDeleteChart(dao));
		//produces.add(new AuthorDeletedRatioChart(dao));
		//produces.add(new AuthorActionChart(dao));
		return produces;
	}

	private static String convertToFolderName(String src) {
		return src.replaceAll("http://", "").replaceAll("https://", "").replaceAll("/","-").replaceAll(":", "");
	}

	@SuppressWarnings("unchecked")
	@Override
	public ReportAnalysis create(ObjectNode config, Dao dao, Properties properties) {
		return new ReportAnalysis(config, dao, properties);
	}
}
