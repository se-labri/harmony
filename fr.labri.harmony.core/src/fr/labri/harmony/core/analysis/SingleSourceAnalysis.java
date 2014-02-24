package fr.labri.harmony.core.analysis;

import java.util.Properties;

import fr.labri.harmony.core.config.model.AnalysisConfiguration;
import fr.labri.harmony.core.dao.Dao;

/**
 * Class to inherit to develop an analysis. <br>
 * Any subclass <strong>must</strong> implement both the default constructor
 * {@link #AbstractAnalysis()} and
 * {@link #AbstractAnalysis(AnalysisConfiguration, Dao, Properties)}
 * 
 */
public abstract class SingleSourceAnalysis extends AbstractAnalysis implements ISingleSourceAnalysis {

	public SingleSourceAnalysis(AnalysisConfiguration config, Dao dao) {
		// Be careful if you modify the signature of this constructor, it is
		// called using reflexivity by AnalysisFactory
		super(config, dao);
	}

	public SingleSourceAnalysis() {
		super();
	}

}
