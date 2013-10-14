package fr.labri.harmony.analysis.report.analyzer;

import fr.labri.harmony.core.analysis.AbstractAnalysis;
import fr.labri.harmony.core.dao.Dao;
import fr.labri.harmony.core.model.Source;

public abstract class RepositoryAnalyzer {
	
	
	protected Dao dao;
	protected AbstractAnalysis rootAnalysis;
	
	public RepositoryAnalyzer(Dao dao, AbstractAnalysis rootAnalysis) {
		this.dao = dao;
		this.rootAnalysis= rootAnalysis;
	}
	
	public abstract void extractData(Source src);

}
