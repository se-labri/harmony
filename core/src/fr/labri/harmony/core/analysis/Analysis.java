package fr.labri.harmony.core.analysis;

import fr.labri.harmony.core.config.model.AnalysisConfiguration;
import fr.labri.harmony.core.model.Source;

public interface Analysis {

	/**
	 * Main method of an analysis. Called when the source has been initialized
	 * 
	 * @param src
	 */
	void runOn(Source src);

	/**
	 * Implemented by {@link AbstractAnalysis#getConfig()}
	 * 
	 */
	AnalysisConfiguration getConfig();
}
