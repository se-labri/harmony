package fr.labri.harmony.analysis.cloc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import fr.labri.harmony.core.analysis.AbstractAnalysis;
import fr.labri.harmony.core.config.model.AnalysisConfiguration;
import fr.labri.harmony.core.dao.Dao;
import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.Item;
import fr.labri.harmony.core.model.Source;

/**
 * Compute the number of lines of code per Item (i.e. per file) at a given commit (by default the last commit available)
 */
public class ClocItemAnalysis extends AbstractAnalysis {

	private static final String OPT_CLOC_COMMIT_ID = "cloc-commit-id";

	public ClocItemAnalysis() {
	}

	public ClocItemAnalysis(AnalysisConfiguration config, Dao dao, Properties properties) {
		super(config, dao, properties);
	}

	@Override
	public void runOn(Source src) throws Exception {

		Event selectedEvent = null;

		Object opt = src.getConfig().getOption(OPT_CLOC_COMMIT_ID);
		if (opt != null) {
			String commitId = opt.toString();
			selectedEvent = dao.getEvent(src, commitId);
		} else {
			List<Event> commits = dao.getEvents(src);
			selectedEvent = commits.get(commits.size() - 1);
		}

		src.getWorkspace().update(selectedEvent);

		for (Item item : src.getItems()) {
			Path itemPath = Paths.get(src.getWorkspace().getPath(), item.getNativeId());
			if (Files.exists(itemPath)) {
				ClocEntries clocEntries = ClocRunner.runCloc(itemPath.toString());
				dao.saveData(getPersitenceUnitName(), clocEntries, item);
			}
		}
	}

}