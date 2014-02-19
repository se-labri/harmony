package fr.labri.harmony.core.output;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import fr.labri.harmony.core.analysis.AbstractAnalysis;
import fr.labri.harmony.core.analysis.AbstractPostProcessingAnalysis;
import fr.labri.harmony.core.model.Source;

public class OutputUtils {

	/**
	 * This method returns the correct path to save the data produced by an analysis on a source in the right folder
	 * 
	 * @param src
	 *            the source on which the analysis was performed
	 * @param analysis
	 *            the analysis calling this method
	 * @param fileName
	 * @return tailored output path
	 * @throws IOException
	 * 
	 */
	public static java.nio.file.Path buildOutputPath(Source src, AbstractAnalysis analysis, String fileName) throws IOException {

		String baseUrl = analysis.getConfig().getFoldersConfiguration().getOutFolder();

		// Specific to TFS
		String pathOnServer = "";
		if (src.getConfig().getPathOnServer() != null) {
			pathOnServer = src.getConfig().getPathOnServer();
		}

		String urlFolder = convertToFolderName(src.getUrl() + pathOnServer);

		Path outputPath = Paths.get(baseUrl, urlFolder, analysis.getName());
		File outputFolder = outputPath.toFile();
		if (!outputFolder.exists()) {
			outputFolder.mkdirs();
		}

		outputPath = Paths.get(outputPath.toString(), fileName);

		return outputPath;
	}
	

	public static java.nio.file.Path buildOutputPath(Source src, AbstractPostProcessingAnalysis analysis, String fileName) throws IOException {

		String baseUrl = analysis.getConfig().getFoldersConfiguration().getOutFolder();

		// Specific to TFS
		String pathOnServer = "";
		if (src.getConfig().getPathOnServer() != null) {
			pathOnServer = src.getConfig().getPathOnServer();
		}

		String urlFolder = convertToFolderName(src.getUrl() + pathOnServer);

		Path outputPath = Paths.get(baseUrl, urlFolder, analysis.getName());
		File outputFolder = outputPath.toFile();
		if (!outputFolder.exists()) {
			outputFolder.mkdirs();
		}

		outputPath = Paths.get(outputPath.toString(), fileName);

		return outputPath;
	}

	private static String convertToFolderName(String src) {
		return src.replaceAll("http://", "").replaceAll("https://", "").replaceAll("/", "-").replaceAll(":", "").replaceAll("$", "");
	}

}
