package fr.labri.harmony.source.git;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fr.labri.harmony.core.config.model.SourceConfiguration;
import fr.labri.harmony.core.dao.Dao;
import fr.labri.harmony.core.model.Action;
import fr.labri.harmony.core.model.ActionKind;
import fr.labri.harmony.core.model.Author;
import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.Item;
import fr.labri.harmony.core.source.AbstractSourceExtractor;
import fr.labri.harmony.core.source.SourceExtractorException;

public class GitSourceExtractor extends AbstractSourceExtractor<GitWorkspace> {

	public GitSourceExtractor(SourceConfiguration config, Dao dao, Properties properties) {
		super(config, dao, properties);
	}

	private static final String FORMAT = "{^@^hash^@^: ^@^%H^@^, " + "^@^parentHash^@^ :^@^%P^@^, " + "^@^time^@^: ^@^%at^@^, " + "^@^authorName^@^ : ^@^%an^@^, "
			+ "^@^message^@^: ^@^%B^@^},";

	private ArrayNode extractGitLog() throws IOException, InterruptedException {
		ProcessBuilder pb = new ProcessBuilder("git", "log", "--topo-order", "--reverse", "--format=" + FORMAT);
		pb.directory(new File(workspace.getPath()));
		Process p = pb.start();
		BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
		StringBuffer b = new StringBuffer();
		b.append("[");
		String line = "";
		while ((line = r.readLine()) != null)
			b.append(line);
		p.waitFor();
		r.close();
		String json = b.toString();
		json = json.substring(0, json.length() - 1).replaceAll("[\\r\\n]", "").replaceAll("\"", "\\\"").replaceAll("\\^@\\^", "\"") + "]";
		ObjectMapper m = new ObjectMapper();
		ArrayNode logs = (ArrayNode) m.readTree(json);
		return logs;
	}

	private void extractAction(String line, Event e, Event p) {
		String[] tokens = line.split("\\s+");
		Item i = dao.getItem(source, tokens[1]);
		if (i == null) {
			i = new Item();
			i.setSource(source);
			i.setNativeId(tokens[1]);
			dao.saveItem(i);
		}
		ActionKind kind = extractKind(tokens[0]);
		Action a = new Action();
		a.setSource(source);
		a.setEvent(e);
		a.setParentEvent(p);
		a.setKind(kind);
		a.setItem(i);
		dao.saveAction(a);
	}

	private ActionKind extractKind(String s) {
		switch (s) {
		case "A":
			return ActionKind.Create;
		case "M":
			return ActionKind.Edit;
		case "D":
			return ActionKind.Delete;
		default:
			LOGGER.severe("Unknown action kind: " + s);
			return null;
		}
	}



	@Override
	public void extractEvents() {
		LOGGER.info("Starting event extraction for source : " + source + ".");
		try {
			ArrayNode logs = extractGitLog();

			for (int i = 0; i < logs.size(); i++) {
				ObjectNode log = (ObjectNode) logs.get(i);
				String hash = log.get("hash").asText();
				String[] parentHashes = log.get("parentHash").asText().split("\\s");
				long time = Long.parseLong(log.get("time").asText()) * 1000L;
				String authorName = log.get("authorName").asText();

				// String message = log.get("message").asText();

				List<Event> parents = new ArrayList<>();
				for (String parentHash : parentHashes) {
					if (!"".equals(parentHash)) {
						Event parent = dao.getEvent(source, parentHash);
						if (parent != null) parents.add(parent);
					}
				}

				Author a = dao.getAuthor(source, authorName);
				if (a == null) {
					a = new Author(source, authorName, authorName);
					dao.saveAuthor(a);
				}

				Event e = new Event(source, hash, time, parents, Arrays.asList(new Author[] { a }));
				dao.saveEvent(e);
			}
		} catch (Exception e) {
			throw new SourceExtractorException(e);
		}
	}

	@Override
	public void extractActions(Event e) {
		try {
			if (e.getParents().size() == 0) {
				ProcessBuilder b = new ProcessBuilder("git", "diff", "--name-status", "--no-renames", e.getNativeId());
				b.directory(new File(workspace.getPath()));
				Process p = b.start();
				BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line;
				while ((line = r.readLine()) != null)
					extractAction(line, e, null);
				p.waitFor();
				r.close();
			} else {
				for (Event parent : e.getParents()) {
					ProcessBuilder b = new ProcessBuilder("git", "diff", "--name-status", "--no-renames", e.getNativeId(), parent.getNativeId());
					b.directory(new File(workspace.getPath()));
					Process p = b.start();
					BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
					String line;
					while ((line = r.readLine()) != null)
						extractAction(line, e, parent);
					p.waitFor();
					r.close();
				}
			}

		} catch (Exception ex) {
			throw new SourceExtractorException(ex);
		}
	}

	@Override
	public void extractSource() {
		extractEvents();
		for (Event e : source.getEvents())
			extractActions(e);
	}

	@Override
	public void initializeWorkspace() {
		workspace = new GitWorkspace(this);
		workspace.init();	
	}

}
