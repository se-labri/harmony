package fr.labri.harmony.core.analysis;

import fr.labri.harmony.core.config.model.AnalysisConfiguration;

public interface IAnalysis {
	
	/**
	 * Implemented by {@link SingleSourceAnalysis#getConfig()}
	 * 
	 */
	AnalysisConfiguration getConfig();
	
	String getPersistenceUnitName();

}
