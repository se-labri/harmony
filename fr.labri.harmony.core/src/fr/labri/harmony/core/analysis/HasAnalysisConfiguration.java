package fr.labri.harmony.core.analysis;

import fr.labri.harmony.core.config.model.AnalysisConfiguration;

public interface HasAnalysisConfiguration {
	
	/**
	 * Implemented by {@link AbstractAnalysis#getConfig()}
	 * 
	 */
	AnalysisConfiguration getConfig();

}
