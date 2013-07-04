package fr.labri.harmony.analysis.ownership;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
		try {
			String baseFolder = config.getFoldersConfiguration().getOutFolder();
			
			//Specific to TFS
			String pathOnServer= "";
			if (src.getConfig().getPathOnServer()!= null){pathOnServer=src.getConfig().getPathOnServer();}
			
			String urlFolder = convertToFolderName(src.getUrl()+pathOnServer);
			Path outputPath = Paths.get(baseFolder, urlFolder);
			File outputFolder = outputPath.toFile();
			if (!outputFolder.exists()) outputFolder.mkdir();
			
			FileWriter writer = new FileWriter(new File(outputPath+"\\OwnershipResults.csv"));
			for (Map.Entry<Item , HashMap<Author, Integer>> e : ownership.entrySet()) {
				for (Map.Entry<Author, Integer> a : e.getValue().entrySet()) {
					writer.write(e.getKey().getNativeId()+";");
					writer.write(a.getKey().getNativeId()+";"+a.getValue().intValue()+"\n");
				}
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		HarmonyLogger.info("Finished ownership analysis.");
	}
	
	private static String convertToFolderName(String src) {
		return src.replaceAll("http://", "").replaceAll("https://", "").replaceAll("/", "-").replaceAll(":", "").replaceAll("$", "");
	}

}
