package fr.labri.harmony.analysis.ownership;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import fr.labri.harmony.core.analysis.AbstractAnalysis;
import fr.labri.harmony.core.config.model.AnalysisConfiguration;
import fr.labri.harmony.core.dao.Dao;
import fr.labri.harmony.core.log.HarmonyLogger;
import fr.labri.harmony.core.model.Action;
import fr.labri.harmony.core.model.Author;
import fr.labri.harmony.core.model.Item;
import fr.labri.harmony.core.model.Source;
import fr.labri.harmony.core.output.OutputUtils;

/**
 * This analysis compute for each item of a source the number of actions that an author performed. 
 * 
 * In the article 'Don’t Touch My Code! Examining the Effects of Ownership on Software Quality' from Bird et al. they state
 * that an author is a major contributor of an item if he performed at least 5% of the actions on the files. Otherwise he is a
 * minor contributor. In their case study they found that high levels of ownership, specifically operationalized as high values
 * of Ownership and Major, and low values of Minor, are associated with less defects.
 * 
 * The results are saved as a CSV file
 * 
 * @author SE@LaBRI
 *
 */
public class OwnershipAnalysis extends AbstractAnalysis {
	
	public OwnershipAnalysis() {
		super();

	}

	public OwnershipAnalysis(AnalysisConfiguration config, Dao dao, Properties properties) {
		super(config, dao, properties);
	}
	

	@Override
	public void runOn(Source src) {
		HarmonyLogger.info("Starting ownership analysis. " + src.getItems().size() + " items to analyze.");
		
		// Ownership computation
		HashMap<Item , HashMap<Author, Integer>> ownership = new HashMap<Item , HashMap<Author, Integer>>();
		
		for (Item it : src.getItems()) {			
			HashMap<Author, Integer> authors = new HashMap<Author, Integer>();
			ownership.put(it, authors);
			for (Action a : it.getActions()){
				for (Author at : a.getEvent().getAuthors()) {
					Integer own = new Integer(1);
					if (authors.containsKey(at)) own = authors.get(at)+1;
					//+1
					authors.remove(at);
					authors.put(at, own);				
				}			
			}		
		}
		
		// Output of the results	
		FileWriter writer;
		try {
			writer = new FileWriter(new File(OutputUtils.buildOutputPath(src, this, "OwnershipResults.csv").toString()));
		
			for (Map.Entry<Item , HashMap<Author, Integer>> e : ownership.entrySet()) {
				for (Map.Entry<Author, Integer> a : e.getValue().entrySet()) {
					writer.write(e.getKey().getNativeId()+";");
					writer.write(a.getKey().getNativeId()+";"+a.getValue().intValue()+"\n");
				}
			}
			writer.close();
			
		} catch (IOException e) {
			HarmonyLogger.error("Something went wrong when saving the results from the ownership analysis :(");
			e.printStackTrace();
		}		
		HarmonyLogger.info("Finished ownership analysis.");
	}

}
