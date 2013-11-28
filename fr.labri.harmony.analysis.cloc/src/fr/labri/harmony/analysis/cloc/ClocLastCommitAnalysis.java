package fr.labri.harmony.analysis.cloc;

import java.util.List;
import java.util.Properties;

import fr.labri.harmony.core.analysis.AbstractAnalysis;
import fr.labri.harmony.core.config.model.AnalysisConfiguration;
import fr.labri.harmony.core.dao.Dao;
import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.Source;

/**
 * Counts the lines of code at the last commit of the source repository. <br>
 * Requires that the cloc program is installed on your machine (and added to the path). <br>
 * cloc is available at http://cloc.sourceforge.net/. <br>
 */
public class ClocLastCommitAnalysis extends AbstractAnalysis {

	public ClocLastCommitAnalysis() {
	}

	public ClocLastCommitAnalysis(AnalysisConfiguration config, Dao dao, Properties properties) {
		super(config, dao, properties);
	}

	@Override
	public void runOn(Source src) throws Exception {
		List<Event> commits = dao.getEvents(src);
		Event lastCommit = commits.get(commits.size() - 1);

		src.getWorkspace().update(lastCommit);
		ClocEntries entries = ClocRunner.runCloc(src.getWorkspace().getPath());
		if (entries != null) dao.saveData(this.getPersitenceUnitName(), entries, lastCommit);
	}

}
