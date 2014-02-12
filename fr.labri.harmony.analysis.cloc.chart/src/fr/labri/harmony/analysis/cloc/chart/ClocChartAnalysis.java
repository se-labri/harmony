package fr.labri.harmony.analysis.cloc.chart;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import fr.labri.harmony.analysis.cloc.ClocEntries;
import fr.labri.harmony.analysis.cloc.ClocEntry;
import fr.labri.harmony.core.analysis.AbstractAnalysis;
import fr.labri.harmony.core.config.model.AnalysisConfiguration;
import fr.labri.harmony.core.dao.Dao;
import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.Source;
import fr.labri.harmony.core.output.OutputUtils;

public class ClocChartAnalysis extends AbstractAnalysis {

	public ClocChartAnalysis() {
		super();
	}

	public ClocChartAnalysis(AnalysisConfiguration config, Dao dao, Properties properties) {
		super(config, dao, properties);
	}

	@Override
	public void runOn(Source src) {

		// we use the dao.getEvents method to ensure that the events are sorted
		List<Event> events = dao.getEvents(src);
		LinkedHashSet<String> availableLanguages = new LinkedHashSet<>();
		LinkedHashMap<Long, HashMap<String, Integer>> clocData = new LinkedHashMap<>();

		for (Event event : events) {
			// get the data added by the cloc analysis
			List<ClocEntries> clocEntries = dao.getData("cloc", ClocEntries.class, event);

			HashMap<String, Integer> eventClocValues = new HashMap<>();
			for (ClocEntries c : clocEntries) {
				for (ClocEntry clocEntry : c.getEntries()) {
					availableLanguages.add(clocEntry.getLanguage());
					eventClocValues.put(clocEntry.getLanguage(), clocEntry.getCode());
				}
			}
			clocData.put(event.getTimestamp(), eventClocValues);
		}

		try {
			Path filePath = OutputUtils.buildOutputPath(src, this, "cloc.csv");
			BufferedWriter writer = Files.newBufferedWriter(filePath, Charset.forName("UTF-8"));

			// output the data as a csv file
			writer.append("Timestamp");
			for (String language : availableLanguages) {
				writer.append("," + language);
			}
			writer.append("\n");

			// for each event, we put the number of lines of code for each language in the availableLanguages set
			for (Entry<Long, HashMap<String, Integer>> clocMapEntry : clocData.entrySet()) {
				writer.append(clocMapEntry.getKey() + "");
				for (String language : availableLanguages) {

					Integer cloc = clocMapEntry.getValue().get(language);
					if (cloc == null) cloc = 0;

					writer.append("," + cloc);
				}
				writer.append("\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}