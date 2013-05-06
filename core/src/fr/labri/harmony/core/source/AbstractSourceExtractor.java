package fr.labri.harmony.core.source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import fr.labri.harmony.core.AbstractHarmonyService;
import fr.labri.harmony.core.analysis.Analysis;
import fr.labri.harmony.core.config.model.SourceConfiguration;
import fr.labri.harmony.core.dao.Dao;
import fr.labri.harmony.core.log.HarmonyLogger;
import fr.labri.harmony.core.model.Action;
import fr.labri.harmony.core.model.Author;
import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.Item;
import fr.labri.harmony.core.model.Source;

public abstract class AbstractSourceExtractor<W extends Workspace> extends AbstractHarmonyService implements SourceExtractor<W> {

	// Vcs properties
	public final static String COMMIT_LOG = "commit_log";
	public final static String COMMITTER = "committer";
	public final static String BRANCH = "branch";

	// FIXME: there is a bug when EVENT_CACHE_SIZE > 1
	private final static int EVENT_CACHE_SIZE = 1;
	private final static int ACTION_CACHE_SIZE = 1000;
	

	protected W workspace;

	protected Source source;

	protected List<Analysis> analyses;

	private List<Event> eventsCache;
	private HashMap<String, Author> authors;
	private List<Author> authorsCache;
	
	private HashMap<String, Item> items;
	private List<Item> itemsCache;
	private List<Action> actionsCache;

	protected SourceConfiguration config;

	public AbstractSourceExtractor(SourceConfiguration config, Dao dao, Properties properties) {
		super(dao, properties);
		this.config = config;
		analyses = new ArrayList<>();
		eventsCache = new ArrayList<>();
		authors = new HashMap<>();
		authorsCache = new ArrayList<>();
		items = new HashMap<>();
		itemsCache = new ArrayList<>();
		actionsCache = new ArrayList<>();
	}

	public AbstractSourceExtractor() {
		super();
	}

	@Override
	public Source getSource() {
		return this.source;
	}

	@Override
	public W getWorkspace() {
		return this.workspace;
	}

	public String getUrl() {
		return config.getRepositoryURL();
	}

	@Override
	public SourceConfiguration getConfig() {
		return config;
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
		extractEvents();

		// Save the remaining events
		saveAuthorsAndEvents();
		
		if (extractActions) {
			HarmonyLogger.info("Extracting Actions for source " + getUrl());

			for (Event e : dao.getEvents(source))
				extractActions(e);
			
			saveItemsAndActions();
		}

		source = dao.refreshSource(source);
	}

	protected void addEvent(Event e) {
		eventsCache.add(e);

		if (eventsCache.size() >= EVENT_CACHE_SIZE) {
			saveAuthorsAndEvents();
		}
	}

	protected Author getAuthor(String name) {
		return authors.get(name);
	}

	protected void addAuthor(Author a) {
		authors.put(a.getName(), a);
		authorsCache.add(a);
	}
	
	private void saveAuthorsAndEvents() {
		dao.saveAuthors(authorsCache);
		authorsCache.clear();
		dao.saveEvents(eventsCache);
		eventsCache.clear();
	}
	
	protected Item getItem(String path) {
		return items.get(path);
	}
	
	protected void saveItem(Item i) {
		items.put(i.getNativeId(), i);
		itemsCache.add(i);
	}
	
	protected void saveAction( Action a) {
		actionsCache.add(a);
		
		if (actionsCache.size() >= ACTION_CACHE_SIZE) {
			saveItemsAndActions();
		}
	}

	private void saveItemsAndActions() {
		dao.saveItems(itemsCache);
		itemsCache.clear();
		dao.saveActions(actionsCache);
		actionsCache.clear();
	}

}
