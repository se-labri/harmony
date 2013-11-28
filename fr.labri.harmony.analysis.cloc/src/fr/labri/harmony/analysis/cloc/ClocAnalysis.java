package fr.labri.harmony.analysis.cloc;

import java.util.Properties;

import fr.labri.harmony.core.analysis.AbstractAnalysis;
import fr.labri.harmony.core.config.model.AnalysisConfiguration;
import fr.labri.harmony.core.dao.Dao;
import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.Source;
import fr.labri.harmony.core.source.WorkspaceException;


/**
 * Counts the lines of code at the <strong>each</strong> commit of the source repository. <br>
 * Requires that the cloc program is installed on your machine (and added to the path). <br>
 * cloc is available at http://cloc.sourceforge.net/
 */
public class ClocAnalysis extends AbstractAnalysis {

	public ClocAnalysis() {
		super();
	}

	public ClocAnalysis(AnalysisConfiguration config, Dao dao, Properties properties) {
		super(config, dao, properties);
	}

	@Override
	public void runOn(Source src) throws WorkspaceException {
		String workspacePath = src.getWorkspace().getPath();
		for (Event ev : src.getEvents()) {
			src.getWorkspace().update(ev);
			ClocEntries entries = ClocRunner.runCloc(workspacePath);
			if (entries != null) dao.saveData(this.getPersitenceUnitName(), entries, ev);
		}
	}

}
