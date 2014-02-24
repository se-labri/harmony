package fr.labri.harmony.core.analysis;

import fr.labri.harmony.core.model.Source;

public interface ISingleSourceAnalysis extends IAnalysis{

	/**
	 * Main method of an analysis. Called when the source has been initialized
	 * 
	 * @param src
	 */
	void runOn(Source src) throws Exception;

}
