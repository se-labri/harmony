package fr.labri.harmony.analysis.report.analyzer;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;

import au.com.bytecode.opencsv.CSVWriter;
import fr.labri.harmony.core.analysis.SingleSourceAnalysis;
import fr.labri.harmony.core.dao.AbstractDao;
import fr.labri.harmony.core.log.HarmonyLogger;
import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.Source;
import fr.labri.harmony.core.output.OutputUtils;

public class DevelopersActionsAnalyzer extends RepositoryAnalyzer {

	public DevelopersActionsAnalyzer(AbstractDao dao,SingleSourceAnalysis rootAnalysis) {
		super(dao, rootAnalysis);
	}

	@Override
	public void extractData(Source src) {
		HashMap<String, Integer> actionsPerAuthor = new HashMap<>();

		
		// Data extraction
		for (Event e : src.getEvents()) {
			String author = e.getAuthors().get(0).getName();
			Integer count = actionsPerAuthor.get(author);
			if (count == null){
				count = 0;
			}
			count += e.getActions().size();
			actionsPerAuthor.put(author, count);
		}

		
		// Data saving in CSV format
		try {
			Path csvFilePath = OutputUtils.buildOutputPath(src, rootAnalysis, "developers_actions.csv");
			CSVWriter writer = new CSVWriter(new FileWriter(csvFilePath.toString()));
			 
			//We add column headers to ease reading from d3js
			writer.writeNext(new String[] {"developer", "actionsnumber"});
			
			for (String authorName : actionsPerAuthor.keySet()) {
				writer.writeNext(new String[] {authorName, actionsPerAuthor.get(authorName).toString()});
			}	 
			writer.close();
			
			
		} catch (IOException e1) {
			HarmonyLogger.error("Could not generate data for analyzer: developers actions. Message: "+e1.getMessage());
		}


		
	}

}
