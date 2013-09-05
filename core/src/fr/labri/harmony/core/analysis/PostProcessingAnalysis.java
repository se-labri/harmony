package fr.labri.harmony.core.analysis;

import java.util.Collection;

import fr.labri.harmony.core.model.Source;

/**
 * PostProcessing analyses are called when all analyses have finished
 * 
 *
 */
public interface PostProcessingAnalysis extends HasAnalysisConfiguration {

	/** 
	 * @param sources
	 */
	void runOn(Collection<Source> sources);
	
}
