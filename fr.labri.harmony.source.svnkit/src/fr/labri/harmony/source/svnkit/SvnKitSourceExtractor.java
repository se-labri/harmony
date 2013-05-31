package fr.labri.harmony.source.svnkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.wc.SVNRevision;

import fr.labri.harmony.core.log.HarmonyLogger;
import fr.labri.harmony.core.model.Action;
import fr.labri.harmony.core.model.ActionKind;
import fr.labri.harmony.core.model.Author;
import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.Item;
import fr.labri.harmony.core.model.Source;
import fr.labri.harmony.core.source.AbstractSourceExtractor;


public class SvnKitSourceExtractor extends AbstractSourceExtractor<SvnKitWorkspace> implements ISVNLogEntryHandler {
	
	private Event parent;
	private boolean extractActions;
	
	@Override
	public void initializeWorkspace() {
		workspace = new SvnKitWorkspace(this);
		workspace.init();
		
	}
	
	@Override
	public void initializeSource(boolean extractActions) {
		HarmonyLogger.info("Initializing Workspace for source " + getUrl());
		initializeWorkspace();

		source = new Source();
		source.setUrl(getUrl());
		source.setWorkspace(workspace);
		dao.saveSource(source);
		
		
		HarmonyLogger.info("Extracting Events for source " + getUrl());
		parent = null;
		this.extractActions=extractActions;
		

		extractEvents();

		// Save the remaining events
		saveAuthorsAndEvents();
		saveItemsAndActions();

		source = dao.refreshSource(source);

		onExtractionFinished();
	}
	
	@Override
	public void handleLogEntry(SVNLogEntry logEntry) throws SVNException {
		
		
		List<Event> parents = new ArrayList<>();
		if(parent != null){parents.add(parent); }
		
		
		String user = logEntry.getAuthor();
		Author author = getAuthor(user);
		if (author == null) {
			author = new Author(source, user, user);
			saveAuthor(author);
		}
		List<Author> authors = new ArrayList<>(Arrays.asList(new Author[] { author }));

		Event e = new Event(source, String.valueOf( logEntry.getRevision()),logEntry.getDate().getTime(), parents, authors);
		saveEvent(e);
		
		if (extractActions) {
			for(SVNLogEntryPath entry: logEntry.getChangedPaths().values()){
				ActionKind kind = null;
				switch (entry.getType()) {
				case 'M':
					kind = ActionKind.Edit;
					break;
				case 'A':
					kind = ActionKind.Create;
					break;
				case 'D':
					kind = ActionKind.Delete;
					break;
				case 'R':
					kind = ActionKind.Delete;
				}
				
				Item i = getItem(entry.getPath());
				if (i == null) {
					i = new Item(source, entry.getPath());
					saveItem(i);
				}
				Action a = new Action(i, kind, e, parent, source);
				saveAction(a);
			}
		}
		
		this.parent =e;
		
	}
	
	@Override
	public void extractEvents(){
		try {
			getWorkspace().getSvnClientManager().getLogClient().doLog(workspace.getSurl(), new String[] {}, SVNRevision.HEAD, SVNRevision.create(0), SVNRevision.HEAD, true, true, false, -1L, new String[]{}, this);
		} catch (SVNException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void extractActions(Event e)  {
		// TODO 
	}

}
