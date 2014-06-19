package fr.labri.harmony.source.git;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.labri.harmony.core.config.model.SourceConfiguration;
import fr.labri.harmony.core.dao.ModelPersister;
import fr.labri.harmony.core.log.HarmonyLogger;
import fr.labri.harmony.core.model.Action;
import fr.labri.harmony.core.model.ActionKind;
import fr.labri.harmony.core.model.Author;
import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.Item;
import fr.labri.harmony.core.source.AbstractSourceExtractor;
import fr.labri.harmony.core.source.SourceExtractorException;
import fr.labri.harmony.core.util.ProcessExecutor;

public class GitSourceExtractor extends AbstractSourceExtractor<GitWorkspace> {

	public GitSourceExtractor() {
		super();
	}

	public GitSourceExtractor(SourceConfiguration config, ModelPersister modelPersister) {
		super(config, modelPersister);
	}

	// FIXME: the format should be with %B, but then a commit could use several lines
	private static final String FORMAT = "@hash@ %H @parentHash@ %P @time@ %at @authorName@ %an @authorEmail@ %ae @message@ %s";
	private static final String REGEX = "^@hash@ (.*) @parentHash@ (.*) @time@ (.*) @authorName@ (.*) @authorEmail@ (.*) @message@ (.*)$";

	private static final String GIT_EMPTY_TREE = "4b825dc642cb6eb9a060e54bf8d69288fbee4904";

	private ActionKind extractKind(String s) {
		switch (s) {
		case "A":
			return ActionKind.Create;
		case "M":
			return ActionKind.Edit;
		case "D":
			return ActionKind.Delete;
		default:
			HarmonyLogger.error("Unknown action kind: " + s);
			return null;
		}
	}

	@Override
	public void extractEvents() {
		try {
			Pattern pattern = Pattern.compile(REGEX);
			HarmonyLogger.info("Starting event extraction for source : " + source + ".");
			ProcessExecutor gitLog = new ProcessExecutor("git", "log", "--all", "--topo-order", "--reverse", "--format=" + FORMAT).setDirectory(workspace.getPath());
			gitLog.run();
			for (String line : gitLog.getOutput()) {
				Matcher matcher = pattern.matcher(line);
				if (matcher.matches()) {
					String hash = matcher.group(1);
					String[] parentHashes = matcher.group(2).split("\\s");
					long time = Long.parseLong(matcher.group(3)) * 1000L;
					String authorName = matcher.group(4);
					String authorMail = matcher.group(5);
					String message = matcher.group(6);
					extractEvent(hash, parentHashes, time, authorName, authorMail, message);
				}
			}
		} catch (IOException | InterruptedException e) {
			throw new SourceExtractorException(e);
		}

	}

	private void extractEvent(String hash, String[] parentHashes, long time, String authorName, String authorMail, String message) {
		HashSet<Event> parents = new HashSet<>();
		for (String parentHash : parentHashes) {
			if (!"".equals(parentHash)) {
				Event parent = modelPersister.getEvent(source, parentHash);
				if (parent != null) parents.add(parent);
			}
		}

		Author a = modelPersister.getAuthor(source, authorName);
		if (a == null) {
			a = new Author(source, authorName, authorName);
			a.setEmail(authorMail);
			modelPersister.saveAuthor(a);
		}

		Event e = new Event(source, hash, time, parents, Arrays.asList(new Author[] { a }));
		e.getMetadata().put(COMMIT_MESSAGE, message);

		modelPersister.saveEvent(e);
	}

	@Override
	public void extractActions(Event e) {
		if (e.getParents().size() == 0) gitDiff(e, null);
		else for (Event parent : e.getParents())
			gitDiff(e, parent);
	}

	private void gitDiff(Event event, Event parent) {
		try {
			ProcessExecutor executor = new ProcessExecutor("git", "diff", "-z", "--name-status", "--no-renames", event.getNativeId(),
					parent == null ? GIT_EMPTY_TREE : parent.getNativeId());
			// Run diff --numstat -M to get the churn and the renames
			executor.setDirectory(workspace.getPath());
			executor.run();
			ArrayList<Action> actions = new ArrayList<>();
			for (String line : executor.getOutput()) {
				String[] tokens = line.split("\u0000");
				for (int i = 0; i < tokens.length; i += 2) {
					String statusLetter = tokens[i];
					String path = tokens[i + 1];
					Action a = extractAction(event, parent, statusLetter, path);
					if (a != null) actions.add(a);
					
				}
			}
			extractActionsMetadata(event, parent, actions);
			for (Action a : actions)
				modelPersister.saveAction(a);
		} catch (IOException | InterruptedException ex) {
			throw new SourceExtractorException(ex);
		}
	}

	private Action extractAction(Event event, Event parent, String statusLetter, String path) {
		if (extractItemWithPath(path)) {
			Item i = modelPersister.getItem(source, path);
			if (i == null) {
				i = new Item();
				i.setSource(source);
				i.setNativeId(path);
				modelPersister.saveItem(i);
			}
			ActionKind kind = extractKind(statusLetter);
			Action a = new Action();
			a.setSource(source);
			a.setEvent(event);
			a.setParentEvent(parent);
			a.setKind(kind);
			a.setItem(i);
			return a;
		}

		return null;
	}

	private void extractActionsMetadata(Event event, Event parent, ArrayList<Action> actions) {
		try {
			ProcessExecutor executor = new ProcessExecutor("git", "diff", "--numstat", "-M", event.getNativeId(), parent == null ? GIT_EMPTY_TREE
					: parent.getNativeId());
			// Run diff --numstat -M to get the churn and the renames
			executor.setDirectory(workspace.getPath());
			executor.run();
			for (String numStatLine : executor.getOutput()) {
				/*
				 * git diff --numstat returns:
				 * 
				 * 1. the number of added lines;
				 * 2. a tab;
				 * 3. the number of deleted lines;
				 * 4. a tab;
				 * 5. pathname (possibly with rename/copy information);
				 * 6. a newline.
				 */
				String[] numStatTokens = numStatLine.split("\\t");
				if (numStatTokens.length != 3) {
					HarmonyLogger.error("Uh oh....numstatline=" + numStatLine);
					continue;
				}
				/*
				 * In case of a rename the path will be one of the following:
				 * 
				 * {oldPath => newPath}/to/file
				 * oldFile => newfile
				 */
				Matcher renameMatcher = Pattern.compile(".*(\\{(.*) => (.*)\\}).*").matcher(numStatTokens[2]);
				Matcher fullRenameMatcher = Pattern.compile("(.*) => (.*)").matcher(numStatTokens[2]);
				String newName;
				String oldName = null;
				if (renameMatcher.matches()) {
					newName = numStatTokens[2].replace(renameMatcher.group(1), renameMatcher.group(3)).replaceAll("/+", "/");
					oldName = numStatTokens[2].replace(renameMatcher.group(1), renameMatcher.group(2));
				} else if (fullRenameMatcher.matches()) {
					newName = fullRenameMatcher.group(2).replaceAll("/+", "/");
					oldName = fullRenameMatcher.group(1);
				} else newName = numStatTokens[2];

				// find the corresponding action in the list
				Action action = null;
				for (Action a : actions) {
					if (a.getItem().getNativeId().equalsIgnoreCase(newName) && (parent == null || a.getParentEvent().equals(parent))) {
						action = a;
						break;
					}
				}
				if (action == null) {
					HarmonyLogger.info("null action " + newName);
					return;
				}
				if (oldName != null) { // if there is a rename
					// HarmonyLogger.info(numStatTokens[2]);
					action.getMetadata().put("renamed", oldName);
				}
				if (!numStatTokens[0].contains("-")) {
					int churn = Integer.parseInt(numStatTokens[0]) + Integer.parseInt(numStatTokens[1]);
					action.getMetadata().put("churn", Integer.toString(churn));
				}
			}
		} catch (IOException | InterruptedException ex) {
			throw new SourceExtractorException(ex);
		}
	}

	@Override
	public void initializeWorkspace() {
		workspace = new GitWorkspace(this);
		workspace.init();
	}

}
