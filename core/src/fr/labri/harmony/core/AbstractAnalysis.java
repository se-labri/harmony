package fr.labri.harmony.core;

import java.util.Properties;

import fr.labri.harmony.core.config.model.AnalysisConfiguration;
import fr.labri.harmony.core.dao.Dao;

public abstract class AbstractAnalysis extends AbstractHarmonyService implements Analysis {

	protected AnalysisConfiguration config;
	
	public AbstractAnalysis() {
	}
	
	public AbstractAnalysis(AnalysisConfiguration config, Dao dao, Properties properties) {
		super(dao, properties);
		this.config = config;
	}

	@Override
	public String getPersistenceUnit() {
		return properties.getProperty(Analysis.PROPERTY_PERSISTENCE_UNIT);
	}
	
	@Override
	public String getDepends() {
		return properties.getProperty(Analysis.PROPERTY_DEPENDS);
	}
	
	@Override
	public AnalysisConfiguration getConfig() {
		return config;
	}

}
