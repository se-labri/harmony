package fr.labri.harmony.core.analysis;

import fr.labri.harmony.core.config.model.AnalysisConfiguration;
import fr.labri.harmony.core.dao.Dao;

public abstract class MultipleSourceAnalysis extends AbstractAnalysis implements IMultipleSourceAnalysis {

	public MultipleSourceAnalysis(AnalysisConfiguration config, Dao dao) {
		// Be careful if you modify the signature of this constructor, it is
		// called using reflexivity by AnalysisFactory
		super(config, dao);
	}

}
