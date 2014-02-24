package fr.labri.harmony.analysis.report.analyzer;

import fr.labri.harmony.core.analysis.SingleSourceAnalysis;
import fr.labri.harmony.core.dao.AbstractDao;
import fr.labri.harmony.core.model.Source;

public abstract class RepositoryAnalyzer {
	
	
	protected AbstractDao dao;
	protected SingleSourceAnalysis rootAnalysis;
	
	public RepositoryAnalyzer(AbstractDao dao, SingleSourceAnalysis rootAnalysis) {
		this.dao = dao;
		this.rootAnalysis= rootAnalysis;
	}
	
	public abstract void extractData(Source src);

}
