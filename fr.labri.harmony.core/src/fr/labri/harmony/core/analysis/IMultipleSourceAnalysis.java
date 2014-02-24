package fr.labri.harmony.core.analysis;

import java.util.Collection;

import fr.labri.harmony.core.model.Source;

public interface IMultipleSourceAnalysis extends IAnalysis {

	/** 
	 * @param sources
	 */
	void runOn(Collection<Source> sources);
	
}
