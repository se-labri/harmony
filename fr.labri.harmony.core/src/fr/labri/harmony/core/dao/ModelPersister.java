package fr.labri.harmony.core.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;

import fr.labri.harmony.core.model.Action;
import fr.labri.harmony.core.model.Author;
import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.Item;
import fr.labri.harmony.core.model.Source;
import fr.labri.harmony.core.source.Workspace;

public class ModelPersister extends AbstractDao {

	private final static int EVENT_CACHE_SIZE = 1000;
	private final static int ACTION_CACHE_SIZE = 1000;
	
	private HashMap<String, Event> eventsCache;
	private HashMap<String, Author> authorsCache;

	private HashMap<String, Item> itemsCache;
	private List<Action> actionsCache;
	
	ModelPersister(HarmonyEntityManagerFactory harmonyModelEMF) {
		super(harmonyModelEMF);
		
		eventsCache = new HashMap<>();
		authorsCache = new HashMap<>();
		itemsCache = new HashMap<>();
		actionsCache = new ArrayList<>();
	}
	
	
	@Override
	public Event getEvent(Source source, String nativeId) {
		Event e = eventsCache.get(nativeId);
		if (e == null) e = super.getEvent(source, nativeId);
		return e;
	}

	
	public void saveEvent(Event e) {
		eventsCache.put(e.getNativeId(), e);

		if (eventsCache.size() >= EVENT_CACHE_SIZE) {
			flushEvents();
		}
	}

	@Override
	public Author getAuthor(Source source, String name) {
		Author a = authorsCache.get(name);
		if (a == null) a = super.getAuthor(source, name);
		return a;
	}

	
	public void saveAuthor(Author a) {
		authorsCache.put(a.getName(), a);
	}
	
	@Override
	public Item getItem(Source source, String path) {		
		Item i = itemsCache.get(path);
		if (i == null) i = super.getItem(source, path);
		
		return i;
	}

	
	public void saveItem(Item i) {
		itemsCache.put(i.getNativeId(), i);
	}

	
	public void saveAction(Action a) {
		actionsCache.add(a);

		if (actionsCache.size() >= ACTION_CACHE_SIZE) {
			flushActions();
		}
	}

	public void flushItems() {
		saveItems(itemsCache.values());
		itemsCache.clear();
	}

	public void flushAuthors() {
		saveAuthors(authorsCache.values());
		authorsCache.clear();		
	}

	public void flushEvents() {
		flushAuthors();
		saveEvents(eventsCache.values());
		eventsCache.clear();
	}
	
	public void flushActions() {
		flushItems();
		saveActions(actionsCache);
		actionsCache.clear();
	}	
	
	/****************************************
	 * Save methods (for Source extractors) *
	 ****************************************/

	public void saveSource(Source s) {
		save(s);
	}

	public void saveEvents(Collection<Event> events) {
		save(events);
	}

	public void saveAuthors(Collection<Author> authors) {
		save(authors);
	}

	public void saveActions(Collection<Action> actions) {
		save(actions);
	}

	public void saveItems(Collection<Item> items) {
		save(items);
	}

	public void updateAction(Action a) {
		update(a);
	}
	
	public Source reloadSource(Source source) {
		Workspace ws = source.getWorkspace();
	
		EntityManager m = getEntityManager();
		m.getTransaction().begin();
		source = m.find(Source.class, source.getId());
		m.getTransaction().commit();
	
		// The workspace is transient, so we have to reset it when reloading
		// the source;
		source.setWorkspace(ws);
	
		return source;
	}


	public void flushAll() {
		flushEvents();
		flushActions();
	}


}
