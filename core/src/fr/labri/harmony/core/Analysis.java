package fr.labri.harmony.core;

import java.util.Properties;

import fr.labri.harmony.core.config.model.AnalysisConfiguration;
import fr.labri.harmony.core.dao.Dao;
import fr.labri.harmony.core.model.Source;
import fr.labri.harmony.core.source.WorkspaceException;

public interface Analysis extends HarmonyService {
	
	static final String PROPERTY_DEPENDS = "depends";
	
	static final String PROPERTY_PERSISTENCE_UNIT = "persistence-unit";

	void run(Source src) throws WorkspaceException;
	
	String getPersistenceUnit();
	
	String getDepends();
	
	AnalysisConfiguration getConfig();

	<A extends Analysis> A create(AnalysisConfiguration config, Dao dao, Properties properties);
	
}
