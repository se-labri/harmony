package fr.labri.harmony.core;

import fr.labri.harmony.core.config.model.AnalysisConfiguration;
import fr.labri.harmony.core.model.Source;
import fr.labri.harmony.core.source.WorkspaceException;

public interface Analysis {
	
	static final String PROPERTY_DEPENDENCIES = "depends";
	
	static final String PROPERTY_PERSISTENCE_UNIT = "persistence-unit";

	void runOn(Source src) throws WorkspaceException;
	
	String getPersistenceUnit();
	
	String getDependencies();
	
	AnalysisConfiguration getConfig();	
}
