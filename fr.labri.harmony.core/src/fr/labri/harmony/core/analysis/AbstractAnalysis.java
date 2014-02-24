package fr.labri.harmony.core.analysis;

import fr.labri.harmony.core.config.model.AnalysisConfiguration;
import fr.labri.harmony.core.dao.Dao;

public abstract class AbstractAnalysis implements IAnalysis {

	protected AnalysisConfiguration config;
	protected Dao dao;
	
	public AbstractAnalysis(AnalysisConfiguration config, Dao dao) {
		// Be careful if you modify the signature of this constructor, it is
		// called using reflexivity by AnalysisFactory
		this.dao = dao;
		this.config = config;
	}
	
	public AbstractAnalysis() {
	}

	@Override
	public AnalysisConfiguration getConfig() {
		return config;
	}

	@Override
	public String getPersistenceUnitName() {
		return config.getPersistenceUnit();
	}

}
