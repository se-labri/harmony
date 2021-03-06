package fr.labri.harmony.source.svnkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.wc.SVNRevision;

import fr.labri.harmony.core.config.model.SourceConfiguration;
import fr.labri.harmony.core.dao.ModelPersister;
import fr.labri.harmony.core.log.HarmonyLogger;
import fr.labri.harmony.core.model.Action;
import fr.labri.harmony.core.model.ActionKind;
import fr.labri.harmony.core.model.Author;
import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.Item;
import fr.labri.harmony.core.model.Source;
import fr.labri.harmony.core.source.AbstractSourceExtractor;

/**
 * @see http://svnkit.com/javadoc/
 * @see http://wiki.svnkit.com/Managing_A_Working_Copy
 * 
 * 
 */

public class SvnKitSourceExtractor extends AbstractSourceExtractor<SvnKitWorkspace> implements ISVNLogEntryHandler {

	private Event parent;
	private boolean extractActions;

	public SvnKitSourceExtractor() {
		super();
	}

	public SvnKitSourceExtractor(SourceConfiguration config, ModelPersister modelPersister) {
		super(config, modelPersister);
	}

	@Override
	public void initializeWorkspace() {
		workspace = new SvnKitWorkspace(this);
		workspace.init();

	}

	@Override
	public void initializeSource(boolean extractHarmonyModel, boolean extractActions) {
		HarmonyLogger.info("Initializing Workspace for source " + getUrl());
		initializeWorkspace();

		source = new Source();
		source.setUrl(getUrl());
		source.setWorkspace(workspace);
		modelPersister.saveSource(source);

		if (extractHarmonyModel) {
			HarmonyLogger.info("Extracting Events for source " + getUrl());
			parent = null;
			this.extractActions = extractActions;

			extractEvents();

			// Save the remaining events
			modelPersister.flushAll();

			source = modelPersister.reloadSource(source);
		}
		source.setConfig(getConfig());

		onExtractionFinished();
	}

	@Override
	public void handleLogEntry(SVNLogEntry logEntry) throws SVNException {

		HashSet<Event> parents = new HashSet<>();
		if (parent != null) {
			parents.add(parent);
		}

		String user = logEntry.getAuthor();
		if (user == null) {
			user = "unknown";
		}

		Author author = modelPersister.getAuthor(source, user);
		if (author == null) {
			author = new Author(source, user, user);
			modelPersister.saveAuthor(author);
		}
		List<Author> authors = new ArrayList<>(Arrays.asList(new Author[] { author }));

		Event e = new Event(source, String.valueOf(logEntry.getRevision()), logEntry.getDate().getTime(), parents, authors);

		// TODO handle more metadata
		Map<String, String> metadata = new HashMap<String, String>();
		metadata.put(COMMIT_MESSAGE, logEntry.getMessage());
		e.setMetadata(metadata);

		modelPersister.saveEvent(e);

		if (extractActions) {
			/*
			 * Needed for the operation below
			 */
			String url = this.source.getUrl();
			if (url.endsWith("/")) url = url.substring(0, url.length() - 1);

			for (SVNLogEntryPath entry : logEntry.getChangedPaths().values()) {
				ActionKind kind = null;
				if (entry.getKind() == SVNNodeKind.FILE) {
					switch (entry.getType()) {
					case SVNLogEntryPath.TYPE_MODIFIED:
						kind = ActionKind.Edit;
						break;
					case SVNLogEntryPath.TYPE_ADDED:
						kind = ActionKind.Create;
						break;
					case SVNLogEntryPath.TYPE_DELETED:
						kind = ActionKind.Delete;
						break;
					case SVNLogEntryPath.TYPE_REPLACED:
						kind = ActionKind.Delete;
					}
					/*
					 * It is possible to launch Harmony on SVN urls that have the following forms :
					 * "url/trunk/" "url/trunk/src" "url/trunk/src/test" Problem : SVN will return
					 * items with a path which is context-independent, for instance :
					 * "/trunk/src/test/Test.java" whatever the url you mention. The code below will
					 * produce items with a context-dependent path. Thus, : "url/trunk/" ->
					 * "src/test/Test.java" "url/trunk/src" -> "test/Test.java" "url/trunk/src/test"
					 * -> "Test.java"
					 */
					String path = entry.getPath();
					if (path.startsWith("/")) path = path.substring(1);
					String tokens[] = path.split("\\/");
					String commonPart = "";
					for (String token : tokens) {
						commonPart += "/" + token;
						if (url.endsWith(commonPart) || url.endsWith(commonPart + "/")) {
							commonPart = commonPart.substring(1);
							if (url.equals(commonPart) == false) {
								path = path.substring(commonPart.length());
							} else path = "/";
							break;
						}
					}
					if (extractItemWithPath(path)) {
						Item i = modelPersister.getItem(source, path);
						if (i == null) {
							i = new Item(source, path);
							modelPersister.saveItem(i);
						}
						Action a = new Action(i, kind, e, parent, source);
						modelPersister.saveAction(a);
					}
				}
			}
		}

		this.parent = e;

	}

	@Override
	public void extractEvents() {
		try {
			getWorkspace().getSvnClientManager().getLogClient()
					.doLog(workspace.getSurl(), new String[] {}, SVNRevision.HEAD, SVNRevision.create(0), SVNRevision.HEAD, false, true, false, -1L, new String[] {}, this);
		} catch (SVNException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void extractActions(Event e) {
		// TODO
	}

}
