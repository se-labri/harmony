package fr.labri.harmony.core;

import fr.labri.harmony.core.model.Source;
import fr.labri.harmony.core.source.WorkspaceException;

public interface Analysis extends HarmonyService {
	
	static final String PROPERTY_DEPENDS = "depends";
	
	static final String PROPERTY_PERSISTENCE_UNIT = "persistence-unit";

	void run(Source src) throws WorkspaceException;
	
	String getPersistenceUnit();
	
	String getDepends();
	
}
