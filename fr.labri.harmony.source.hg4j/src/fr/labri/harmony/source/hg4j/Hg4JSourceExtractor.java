package fr.labri.harmony.source.hg4j;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.tmatesoft.hg.core.HgChangeset;
import org.tmatesoft.hg.core.HgFileRevision;
import org.tmatesoft.hg.util.Path;

import fr.labri.harmony.core.config.model.SourceConfiguration;
import fr.labri.harmony.core.dao.Dao;
import fr.labri.harmony.core.log.HarmonyLogger;
import fr.labri.harmony.core.model.Action;
import fr.labri.harmony.core.model.ActionKind;
import fr.labri.harmony.core.model.Author;
import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.Item;
import fr.labri.harmony.core.source.AbstractSourceExtractor;
import fr.labri.harmony.core.source.SourceExtractorException;

public class Hg4JSourceExtractor extends AbstractSourceExtractor<Hg4JWorkspace> {

	private static final long MILLI_2_SECONDS = 1000;

	private Map<String, HgChangeset> changeSets = new HashMap<String, HgChangeset>();

	public Hg4JSourceExtractor() {
		super();
	}

	public Hg4JSourceExtractor(SourceConfiguration config, Dao dao, Properties properties) {
		super(config, dao, properties);
	}

	@Override
	public void initializeWorkspace() {
		workspace = new Hg4JWorkspace(this);
		workspace.init();
	}

	@Override
	public void extractEvents() {
		try {
			List<HgChangeset> result = workspace.getRepoFacade().createLogCommand().execute();

			for (HgChangeset chgSet : result) {
				// Name
				String revId = chgSet.getNodeid().toString();
				changeSets.put(revId, chgSet);

				// Time
				long time = chgSet.getDate().getRawTime() / MILLI_2_SECONDS;
				DateFormat formatter = new SimpleDateFormat("yyyy");

				if (formatter.format(new Date(time)).equals("1970")) {
					time = time * MILLI_2_SECONDS;
				}

				// Parent Events
				Set<Event> parents = new HashSet<>();
				if (!chgSet.getFirstParentRevision().isNull()) {
					parents.add(getEvent(chgSet.getFirstParentRevision().toString()));
				}
				if (!chgSet.getSecondParentRevision().isNull()) {
					parents.add(getEvent(chgSet.getSecondParentRevision().toString()));
				}

				// Authors
				String user = chgSet.getUser();
				String mail = "";
				if (user.contains("<") && user.contains(">")) {
					String base = user;
					user = user.substring(0, user.indexOf("<")).trim();
					mail = base.substring(base.indexOf("<") + 1, base.indexOf(">")).trim();
				}
				Author author = getAuthor(user);
				if (author == null) {
					author = new Author(source, user, user);
					author.setEmail(mail);
					saveAuthor(author);
				}
				List<Author> authors = new ArrayList<>(Arrays.asList(new Author[] { author }));

				Event e = new Event(source, revId, time, parents, authors);

				// Metadata
				Map<String, String> metadata = new HashMap<String, String>();
				metadata.put(COMMIT_MESSAGE, chgSet.getComment());
				metadata.put(BRANCH, chgSet.getBranch());
				e.setMetadata(metadata);

				saveEvent(e);
			}
		} catch (Exception e) {
			throw new SourceExtractorException(e);
		}

	}

	@Override
	public void extractActions(Event e) {
		try {
			// We take the first parent of the event as parent of the action
			Event parent = null;
			if (!e.getParents().isEmpty()) {
				parent = e.getParents().get(0);
			}

			// TODO Memory optimization possible by making a query foreach event instead of storing
			// the whole list of events.
			HgChangeset currentChgSet = changeSets.get(e.getNativeId());

			// We use the high level API provided by to hg4j to find the files that have been ...
			// ... added ...
			for (HgFileRevision fileRev : currentChgSet.getAddedFiles()) {
				if (extractItemWithPath(fileRev.getPath().toString())) {
					Item i = getItem(fileRev.getPath().toString());
					if (i == null) {
						i = new Item(source, fileRev.getPath().toString());
						saveItem(i);
					}
					Action a = new Action(i, ActionKind.Create, e, parent, source);
					saveAction(a);
				}
			}

			// ... or modified ...
			for (HgFileRevision fileRev : currentChgSet.getModifiedFiles()) {
				Item i = getItem(fileRev.getPath().toString());
				if (i == null) {
					// Should not happen
					i = new Item(source, fileRev.getPath().toString());
					saveItem(i);
				}
				Action a = new Action(i, ActionKind.Edit, e, parent, source);
				saveAction(a);
			}

			// ... or finally deleted
			for (Path path : currentChgSet.getRemovedFiles()) {
				Item i = getItem(path.toString());
				if (i == null) {
					// Should not happen
					i = new Item(source, path.toString());
					saveItem(i);
				}
				Action a = new Action(i, ActionKind.Delete, e, parent, source);
				saveAction(a);
			}

		}
		// TODO debug Hg4j to avoid the exceptions
		/*
		 * catch (HgInvalidControlFileException ex) { HarmonyLogger.error(ex.getMessage()); }
		 */
		catch (Exception ex) {
			HarmonyLogger.error(ex.getMessage());
			// throw new SourceExtractorException(ex);
		}
	}

}
