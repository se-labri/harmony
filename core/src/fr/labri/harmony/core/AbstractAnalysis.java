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
		return getComponentProperty(Analysis.PROPERTY_PERSISTENCE_UNIT);
	}
	
	@Override
	public String getDependencies() {
		return getComponentProperty(Analysis.PROPERTY_DEPENDENCIES);
	}
	
	@Override
	public AnalysisConfiguration getConfig() {
		return config;
	}

}
