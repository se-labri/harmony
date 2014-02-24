package fr.labri.harmony.source.git.jgit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
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
import fr.labri.harmony.core.util.MapUtils;

public class JGitSourceExtractor extends AbstractSourceExtractor<JGitWorkspace> {

	public JGitSourceExtractor() {
		super();
	}

	public JGitSourceExtractor(SourceConfiguration config, Dao dao, Properties properties) {
		super(config, dao, properties);
	}

	protected Map<String, RevCommit> revs = new HashMap<>();

	@Override
	public void extractEvents() {
		Map<String, Set<String>> commitsTags = new HashMap<>(); // Key : commit
																// Id , value :
																// tags
		try {
			Git git = workspace.getGit();

			List<Ref> tagList = git.tagList().call();
			for (Ref tag : tagList) {
				// tag.getName returns the full name (i.e. /refs/tags/the-tag),
				// we need to split it
				String[] splitted = tag.getName().split("\\/");
				String commitId = null;
				if (tag.isPeeled() && tag.getPeeledObjectId() != null) {
					commitId = tag.getPeeledObjectId().getName();
				} else {
					commitId = tag.getObjectId().getName();
				}
				MapUtils.addElementToSet(commitsTags, commitId, splitted[splitted.length - 1]);
			}

			RevWalk w = new RevWalk(git.getRepository());
			w.sort(RevSort.TOPO, true);
			w.sort(RevSort.REVERSE, true);

			if (getConfig().extractAllBranches()) {
				for (Ref ref : git.getRepository().getAllRefs().values()) {
					try {
						w.markStart(w.parseCommit(ref.getObjectId()));
					} catch (IncorrectObjectTypeException e) {
						try {
							w.markStart(w.parseCommit(ref.getTarget().getObjectId()));
						} catch (IncorrectObjectTypeException e1) {
						}
					}
				}
			} else {
				Ref ref = git.getRepository().getRef("master");
				if (ref == null) ref = git.getRepository().getRef("trunk");
				if (ref == null) ref = git.getRepository().getRef("HEAD");
				if (ref == null) return;
				w.markStart(w.parseCommit(ref.getObjectId()));
			}

			for (RevCommit commit : w) {
				revs.put(commit.getName(), commit);
				Set<Event> parents = new HashSet<>();
				for (RevCommit parent : commit.getParents())
					parents.add(getEvent(parent.getName()));

				String user = commit.getAuthorIdent().getName();
				Author author = getAuthor(user);
				if (author == null) {
					author = new Author(source, user, user);
					if (commit.getAuthorIdent().getEmailAddress() != null) author.setEmail(commit.getAuthorIdent().getEmailAddress());
					saveAuthor(author);
				}
				List<Author> authors = new ArrayList<>(Arrays.asList(new Author[] { author }));
				// Better consistency of the time data is allowed using commit
				// time on the repo instead of time of when
				// the authors commited his changed
				Event event = new Event(source, commit.getName(), commit.getCommitterIdent().getWhen().getTime(), parents, authors);

				// Adding commit tags
				if (commitsTags.get(commit.getName()) != null) {
					event.setTags(commitsTags.get(commit.getName()));
				}

				Map<String, String> metadata = new HashMap<String, String>();
				metadata.put(COMMIT_MESSAGE, commit.getFullMessage());
				event.setMetadata(metadata);

				saveEvent(event);
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
		if (extractItemWithPath(path)) {
			Item i = getItem(path);
			if (i == null) {
				i = new Item(source, path);
				saveItem(i);
			}
			Action a = new Action(i, kind, e, p, source);
			saveAction(a);
		}
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
