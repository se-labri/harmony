package fr.labri.harmony.ownership.output;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;
import fr.labri.harmony.analysis.ownership.metric.Metric;
import fr.labri.harmony.analysis.ownership.metric.MetricSet;
import fr.labri.harmony.core.analysis.SingleSourceAnalysis;
import fr.labri.harmony.core.config.model.AnalysisConfiguration;
import fr.labri.harmony.core.dao.Dao;
import fr.labri.harmony.core.model.Source;
import fr.labri.harmony.core.output.OutputUtils;

public class OwnershipMetricsToCsv extends SingleSourceAnalysis {

	public OwnershipMetricsToCsv() {
		super();
	}

	public OwnershipMetricsToCsv(AnalysisConfiguration config, Dao dao) {
		super(config, dao);
	}

	@Override
	public void runOn(Source src) throws Exception {
		List<String[]> lines = new ArrayList<>();
		ArrayList<String> headers = null;

		List<MetricSet> metricSets = dao.getData(getPersistenceUnitName(), MetricSet.class, src);
		
		if (metricSets == null || metricSets.size() == 0) return; 
				
		headers = getHeaders(metricSets);
		for (MetricSet metricSet : metricSets) {
			List<String> nextLine = new ArrayList<>();
			nextLine.add(metricSet.getElementName());
			for (Metric m : metricSet.getMetrics()) {
				nextLine.add(m.getValue());
			}
			lines.add(nextLine.toArray(new String[nextLine.size()]));
		}

		CSVWriter writer = new CSVWriter(Files.newBufferedWriter(OutputUtils.buildOutputPath(src, this, "ownership.csv"), Charset.defaultCharset()));
		writer.writeNext(headers.toArray(new String[headers.size()]));
		writer.writeAll(lines);
		writer.close();

	}

	private ArrayList<String> getHeaders(List<MetricSet> metrics) {
		ArrayList<String> headers;
		headers = new ArrayList<>();
		headers.add("name");
		for (Metric m : metrics.get(0).getMetrics()) {
			headers.add(m.getName());
		}
		return headers;
	}

}
