package fr.labri.harmony.analysis.cloc;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import fr.labri.harmony.core.analysis.SingleSourceAnalysis;
import fr.labri.harmony.core.config.model.AnalysisConfiguration;
import fr.labri.harmony.core.dao.Dao;
import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.Item;
import fr.labri.harmony.core.model.Source;

/**
 * Compute the number of lines of code per Item (i.e. per file) at a given commit (by default the last commit available)
 */
public class ClocItemAnalysis extends SingleSourceAnalysis {

	private static final String OPT_CLOC_COMMIT_ID = "cloc-commit-id";

	public ClocItemAnalysis() {
	}

	public ClocItemAnalysis(AnalysisConfiguration config, Dao dao) {
		super(config, dao);
	}

	@Override
	public void runOn(final Source src) throws Exception {

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

		final Path workspacePath = Paths.get(src.getWorkspace().getPath());
		Files.walkFileTree(workspacePath, new FileVisitor<Path>() {

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				String nativeId = workspacePath.relativize(file).toString().replace("\\", "/");
				Item i = dao.getItem(src, nativeId);
				if (i != null) {
					ClocEntries clocEntries = ClocRunner.runCloc(file.toAbsolutePath().toString());
					dao.saveData(getPersistenceUnitName(), clocEntries, i);
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}
		});
	}

}
