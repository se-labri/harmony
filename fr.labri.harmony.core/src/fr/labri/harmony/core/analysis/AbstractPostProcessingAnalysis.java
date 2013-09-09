package fr.labri.harmony.core.analysis;

import java.util.Properties;

import fr.labri.harmony.core.AbstractHarmonyService;
import fr.labri.harmony.core.config.model.AnalysisConfiguration;
import fr.labri.harmony.core.dao.Dao;

public abstract class AbstractPostProcessingAnalysis extends AbstractHarmonyService implements PostProcessingAnalysis {

	protected AnalysisConfiguration config;

	public AbstractPostProcessingAnalysis() {
		super();
	}

	public AbstractPostProcessingAnalysis(AnalysisConfiguration config, Dao dao, Properties properties) {
		// Be careful if you modify the signature of this constructor, it is
		// called using reflexivity by AnalysisFactory
		super(dao, properties);
		this.config = config;
	}

	@Override
	public AnalysisConfiguration getConfig() {
		return config;
	}

	@Override
	public String getPersitenceUnitName() {
		return config.getPersistenceUnit();
	}

}
