package fr.labri.harmony.core;

import java.util.Properties;

import com.fasterxml.jackson.databind.node.ObjectNode;

public abstract class AbstractAnalysis extends AbstractHarmonyService implements Analysis {

	public AbstractAnalysis() {
	}
	
	public AbstractAnalysis(ObjectNode config, Dao dao, Properties properties) {
		super(config, dao, properties);
	}
	
	public String getPersistenceUnit() {
		return properties.getProperty(Analysis.PROPERTY_PERSISTENCE_UNIT);
	}
	
	public String getDepends() {
		return properties.getProperty(Analysis.PROPERTY_DEPENDS);
	}

}
