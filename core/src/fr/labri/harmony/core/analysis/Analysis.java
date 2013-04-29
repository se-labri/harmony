package fr.labri.harmony.core.analysis;

import fr.labri.harmony.core.config.model.AnalysisConfiguration;
import fr.labri.harmony.core.model.Source;

public interface Analysis {

	static final String PROPERTY_DEPENDENCIES = "depends";

	static final String PROPERTY_PERSISTENCE_UNIT = "persistence-unit";

	/**
	 * Main method of an analysis. Called when the source has been initialized
	 * 
	 * @param src
	 */
	void runOn(Source src);

	String getPersistenceUnit();

	String getDependencies();

	AnalysisConfiguration getConfig();
}
