package fr.labri.harmony.core.analysis;

import java.util.Properties;

import fr.labri.harmony.core.AbstractHarmonyService;
import fr.labri.harmony.core.config.model.AnalysisConfiguration;
import fr.labri.harmony.core.dao.Dao;

/**
 * Class to inherit to develop an analysis. <br>
 * Any subclass <strong>must</strong> implement both the default constructor
 * {@link #AbstractAnalysis()} and
 * {@link #AbstractAnalysis(AnalysisConfiguration, Dao, Properties)}
 * 
 */
public abstract class AbstractAnalysis extends AbstractHarmonyService implements Analysis {

	protected AnalysisConfiguration config;

	public AbstractAnalysis() {
		super();
	}

	public AbstractAnalysis(AnalysisConfiguration config, Dao dao, Properties properties) {
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
