package fr.labri.harmony.source.git.jgit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

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

public class JGitSourceExtractor extends AbstractSourceExtractor<JGitWorkspace> {

	public JGitSourceExtractor() {
		super();
	}

	public JGitSourceExtractor(SourceConfiguration config, Dao dao, Properties properties) {
		super(config, dao, properties);
	}

	protected Map<String, RevCommit> revs = new HashMap<String, RevCommit>();

	@Override
	public void extractEvents() {
		try {
			Git git = workspace.getGit();
			RevWalk w = new RevWalk(git.getRepository());
			w.sort(RevSort.TOPO, true);
			w.sort(RevSort.REVERSE, true);
			for (Ref ref : git.getRepository().getAllRefs().values())
				w.markStart(w.parseCommit(ref.getObjectId()));

			for (RevCommit c : w) {
				revs.put(c.getName(), c);

				List<Event> parents = new ArrayList<>();
				for (RevCommit parent : c.getParents())
					parents.add(dao.getEvent(source, parent.getName()));

				String user = c.getAuthorIdent().getName();
				Author author = getAuthor(user);
				if (author == null) {
					author = new Author(source, user, user);
					addAuthor(author);
				}
				List<Author> authors = new ArrayList<>(Arrays.asList(new Author[] { author }));

				Event e = new Event(source, c.getName(), c.getAuthorIdent().getWhen().getTime(), parents, authors);
				addEvent(e); 
				// TODO : add commit log
				/*
				 * Metadata metadata = new Metadata(); metadata.getMetadata().put(VcsProperties.COMMIT_LOG, c.getFullMessage());
				 * metadata.getMetadata().put(VcsProperties.COMMITTER, c.getCommitterIdent().getName()); dao.addData(e, metadata);
				 */
			}
		} catch (Exception e) {
			throw new SourceExtractorException(e);
		}
	}

	private void extractAction(DiffEntry d, Event e, Event p) {
		String path = d.getNewPath();
		ActionKind kind = null;
		switch (d.getChangeType()) {
		case ADD:
			kind = ActionKind.Create;
			break;
		case DELETE:
			kind = ActionKind.Delete;
			path = d.getOldPath();
			break;
		case MODIFY:
			kind = ActionKind.Edit;
			break;
		case COPY:
			kind = ActionKind.Create;
			break;
		case RENAME:
			kind = ActionKind.Create;
			break;
		default:
			HarmonyLogger.error("Unknown action kind: " + d.getChangeType());
			break;
		}
		Item i = getItem(path);
		if (i == null) {
			i = new Item(source, path);
			saveItem(i);
		}
		Action a = new Action(i, kind, e, p, source);
		saveAction(a);
	}

	@Override
	public void initializeWorkspace() {
		workspace = new JGitWorkspace(this);
		workspace.init();
	}

	@Override
	public void extractActions(Event e) {
		try {
			Git git = workspace.getGit();
			DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
			df.setRepository(git.getRepository());
			df.setDiffComparator(RawTextComparator.DEFAULT);
			df.setDetectRenames(false);

			if (e.getParents().size() == 0) {
				TreeWalk w = new TreeWalk(git.getRepository());
				w.addTree(revs.get(e.getNativeId()).getTree());
				List<DiffEntry> entries = df.scan(new EmptyTreeIterator(), w.getTree(0, AbstractTreeIterator.class));
				for (DiffEntry d : entries)
					extractAction(d, e, null);
			} else {
				for (Event p : e.getParents()) {
					List<DiffEntry> entries = df.scan(revs.get(p.getNativeId()).getTree(), revs.get(e.getNativeId()).getTree());
					for (DiffEntry d : entries)
						extractAction(d, e, p);
				}
			}

		} catch (IOException ex) {
			throw new SourceExtractorException(ex);
		}

	}

}
