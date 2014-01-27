package fr.labri.harmony.core.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import fr.labri.harmony.core.log.HarmonyLogger;
import fr.labri.harmony.core.model.Action;
import fr.labri.harmony.core.model.ActionKind;
import fr.labri.harmony.core.model.Author;
import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.HarmonyModelElement;
import fr.labri.harmony.core.model.Item;
import fr.labri.harmony.core.model.Source;
import fr.labri.harmony.core.source.Workspace;

public class Dao extends AbstractDao {

	Dao(Map<String, HarmonyEntityManagerFactory> entityManagerFactories) {
		super(entityManagerFactories);
	}

	/****************************
	 * Events Retrieval Methods *
	 ****************************/

	/**
	 * 
	 * @param s
	 * @param nativeId
	 * @return The event with the given nativeId, in the given Source, or null if there is no such event
	 */
	public Event getEvent(Source s, String nativeId) {
		return get(Event.class, s, nativeId);
	}

	/**
	 * @param source
	 * @return The events of the source, ordered by their timestamp, from the first to the latest event.
	 */
	public List<Event> getEvents(Source source) {

		String queryString = "SELECT e FROM Event e WHERE e.source = :source ORDER BY e.timestamp ASC";

		EntityManager em = getEntityManager();
		em.getTransaction().begin();
		TypedQuery<Event> query = em.createQuery(queryString, Event.class);
		query.setParameter("source", source);
		List<Event> events = query.getResultList();
		em.getTransaction().commit();

		return events;
	}

	/**
	 * 
	 * @param item
	 * @param date
	 * @return The first event occurring on this item after (exclusive) the specified date and time
	 */
	public Event getNextEvent(Item item, Date date) {
		EntityManager em = getEntityManager();

		String queryString = "SELECT e FROM Event e, Action a WHERE a.item = :item AND a MEMBER OF e.actions AND e.timestamp > :date ORDER BY e.timestamp";
		TypedQuery<Event> query = em.createQuery(queryString, Event.class).setMaxResults(1);
		query.setParameter("item", item).setParameter("date", date.getTime());
		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	/**
	 * 
	 * @param item
	 * @param date
	 * @return The first event occurring on this item before of at the specified date and time
	 */
	public Event getPreviousEvent(Item item, Date date) {
		EntityManager em = getEntityManager();

		String queryString = "SELECT e FROM Event e, Action a WHERE a.item = :item AND a MEMBER OF e.actions AND e.timestamp <= :date ORDER BY e.timestamp DESC";
		TypedQuery<Event> query = em.createQuery(queryString, Event.class).setMaxResults(1);
		query.setParameter("item", item).setParameter("date", date.getTime());
		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	/***************************
	 * Items Retrieval Methods *
	 ***************************/

	public List<Item> getItems(Event event) {
		EntityManager em = getEntityManager();
		String queryString = "SELECT DISTINCT a.item FROM Event e, Action a JOIN a.item i WHERE e = :event AND a MEMBER OF e.actions";
		TypedQuery<Item> query = em.createQuery(queryString, Item.class);
		query.setParameter("event", event);

		try {
			return query.getResultList();
		} catch (NoResultException e) {
			return new ArrayList<>();
		}
	}

	/**
	 * @param src
	 * @param date
	 * @return The list of items that exists in the source at the specified date
	 */
	public List<Item> getItems(Source src, Date date) {
		EntityManager em = getEntityManager();

		// We select the items that are not created after the date or deleted before the date
		String queryString = "SELECT i FROM Item i WHERE i.source = :source AND NOT EXISTS "
		// Returns 'found' if the next event is a create
				+ " (SELECT 'found' FROM Action a JOIN a.event e WHERE a.item = i AND a.kind = :createKind AND e.timestamp = "
				// Selects the timestamp of the next event on this item
				+ "(SELECT MIN(e.timestamp) FROM Event e, Action a WHERE a.item = i AND a MEMBER OF e.actions AND e.timestamp > :date)) " + "AND NOT EXISTS "
				// Returns 'found' if the previous event is a delete
				+ " (SELECT 'found' FROM Action a JOIN a.event e WHERE a.item = i AND a.kind = :deleteKind AND e.timestamp = "
				// Selects the timestamp of the last event on this item
				+ "(SELECT MAX(e.timestamp) FROM Event e, Action a WHERE a.item = i AND a MEMBER OF e.actions AND e.timestamp < :date))";

		TypedQuery<Item> query = em.createQuery(queryString, Item.class);
		query.setParameter("source", src).setParameter("date", date.getTime()).setParameter("createKind", ActionKind.Create).setParameter("deleteKind", ActionKind.Delete);

		try {
			return query.getResultList();
		} catch (NoResultException e) {
			return new ArrayList<>();
		}
	}

	public Item getItem(Source s, String nativeId) {
		return get(Item.class, s, nativeId);
	}

	/*****************************
	 * Actions Retrieval Methods *
	 *****************************/

	public List<Action> getActions(Source s) {
		return getList(Action.class, s);
	}

	/**
	 * 
	 * @param item
	 * @return The list of actions which affected and item, ordered by timestamp;
	 */
	public List<Action> getActions(Item item) {
		List<Action> actions = new ArrayList<>();
		EntityManager m = getEntityManager();
		m.getTransaction().begin();

		try {
			String stringQuery = "SELECT a FROM Action a JOIN a.event e WHERE a.item = :item ORDER BY e.timestamp";

			TypedQuery<Action> q = m.createQuery(stringQuery, Action.class);
			q.setParameter("item", item);
			actions = q.getResultList();
		} catch (Exception e) {
			HarmonyLogger.error(e.getMessage());
		} finally {
			m.getTransaction().commit();
			m.close();
		}
		return actions;

	}

	/**
	 * 
	 * @param item
	 * @param fromDate Can be null
	 * @param toDate Can be null
	 * @return The list of actions which affected and item between fromDate and toDate, ordered by timestamp
	 */
	public List<Action> getActions(Item item, Date fromDate, Date toDate) {
		EntityManager em = getEntityManager();
		String stringQuery = "SELECT a FROM Action a JOIN a.item i JOIN a.event e WHERE i = :item ";
				
		if (fromDate != null) stringQuery += " AND e.timestamp >= :fromDate ";
		if (toDate != null) stringQuery += " AND e.timestamp <= :toDate "; 
		stringQuery += "ORDER BY e.timestamp";
		
		TypedQuery<Action> query = em.createQuery(stringQuery, Action.class);
		query.setParameter("item", item);

		if (fromDate != null) query.setParameter("fromDate", fromDate.getTime());
		if (toDate != null) query.setParameter("toDate", toDate.getTime());

		try {
			return query.getResultList();
		} catch (NoResultException e) {
			return new ArrayList<>();
		}
	}

	/**
	 * 
	 * @param item
	 * @return The action related to the creation of the Item or null if there is no such action
	 */
	public Action getCreateAction(Item item) {
		EntityManager m = getEntityManager();

		String stringQuery = "SELECT a FROM Action a JOIN a.event e WHERE a.item = :item AND a.kind = :createKind";

		TypedQuery<Action> q = m.createQuery(stringQuery, Action.class);
		q.setParameter("item", item);
		q.setParameter("createKind", ActionKind.Create);
		try {
			return q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}

	}

	/**
	 * 
	 * @param item
	 * @return The action related to the deletion of the Item or null if there is no such action
	 */
	public Action getDeleteAction(Item item) {
		EntityManager m = getEntityManager();

		String stringQuery = "SELECT a FROM Action a JOIN a.event e WHERE a.item = :item AND a.kind = :deleteKind";

		TypedQuery<Action> q = m.createQuery(stringQuery, Action.class);
		q.setParameter("item", item);
		q.setParameter("deleteKind", ActionKind.Delete);
		try {
			return q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}

	}

	/*****************************
	 * Authors Retrieval Methods *
	 *****************************/

	public Author getAuthor(Source s, String nativeId) {
		return get(Author.class, s, nativeId);
	}

	/**
	 * @param item
	 * @return The set of authors which performed an action on this item. The return type is a list but it is guaranteed that is does not contain duplicates
	 */
	public List<Author> getAuthors(Item item) {
		EntityManager m = getEntityManager();

		String queryString = "SELECT DISTINCT auth FROM Author auth, Event e, Action act WHERE act.item = :item AND act MEMBER OF e.actions AND auth MEMBER OF e.authors";
		TypedQuery<Author> query = m.createQuery(queryString, Author.class);
		query.setParameter("item", item);

		return query.getResultList();

	}

	/**
	 * @param item
	 * @param fromDate
	 * @param toDate
	 * @return The set of authors which performed an action on this item between fromDate and toDate. The return type is a list but it is guaranteed that is
	 *         does not contain duplicates
	 */
	public List<Author> getAuthors(Item item, Date fromDate, Date toDate) {
		EntityManager m = getEntityManager();

		String queryString = "SELECT DISTINCT auth FROM Author auth, Event e, Action act WHERE act.item = :item AND act MEMBER OF e.actions AND auth MEMBER OF e.authors AND e.timestamp > :fromDate AND e.timestamp < e.toDate";
		TypedQuery<Author> query = m.createQuery(queryString, Author.class);
		query.setParameter("item", item);
		query.setParameter("fromDate", fromDate.getTime());
		query.setParameter("toDate", toDate.getTime());

		return query.getResultList();

	}

	/************************************
	 * Access methods for data Objects *
	 ************************************/

	/**
	 * @param database
	 * @param dataClass
	 * @param harmonyModelElement
	 * @return All data objects associated to the given {@link HarmonyModelElement}, in the provided database, and of the provided type.
	 */
	public <D> List<D> getData(String database, Class<D> dataClass, HarmonyModelElement harmonyModelElement) {
		List<Integer> dataIds = getDataIds(database, dataClass, harmonyModelElement);

		// Retrieve the data objects in the analysis database
		EntityManager dataEntityManager = getEntityManager(database);
		ArrayList<D> dataList = new ArrayList<>();
		// TODO : do a single transaction
		for (Integer dataId : dataIds) {
			dataList.add(dataEntityManager.find(dataClass, dataId));
		}
		return dataList;
	}

	public List<Integer> getDataIds(String database, Class<?> dataClass, HarmonyModelElement harmonyModelElement) {
		EntityManager coreEntityManager = getEntityManager();

		String queryString = "SELECT dmo.dataId FROM " + DataMappingObject.class.getSimpleName() + " dmo WHERE dmo.elementId = :elementId "
				+ "AND dmo.elementType = :elementType AND dmo.databaseName = :databaseName AND dmo.dataClassSimpleName = :dataClass";

		TypedQuery<Integer> query = coreEntityManager.createQuery(queryString, Integer.class);
		query.setParameter("elementId", harmonyModelElement.getId());
		query.setParameter("elementType", harmonyModelElement.getClass().getSimpleName());
		query.setParameter("databaseName", database);
		query.setParameter("dataClass", dataClass.getSimpleName());

		try {
			return query.getResultList();
		} catch (NoResultException e) {
			return new ArrayList<Integer>();
		}
	}

	/**
	 * @param database
	 * @param dataClass
	 * @return All the data of the given class stored in the given database.
	 */
	public <D> List<D> getData(String database, Class<D> dataClass) {
		EntityManager m = getEntityManager(database);
		String sQuery = "SELECT d FROM " + dataClass.getSimpleName() + " d";
		TypedQuery<D> query = m.createQuery(sQuery, dataClass);
		List<D> results = query.getResultList();
		return results;
	}

	/**
	 * Saves a data Object associated to an harmony element.
	 * 
	 * @param database
	 *            The name of the database in which the data Object will be saved (which corresponds to the persistence unit name of your analysis)
	 * @param data
	 *            The data to be saved. It has to be annotated with JPA annotations (c.f. {@link Entity})
	 * @param harmonyModelElement
	 *            The element of the model the data is associated to.
	 */
	public void saveData(String database, Object data, HarmonyModelElement harmonyModelElement) {

		EntityManager dataEntityManager = getEntityManager(database);
		dataEntityManager.getTransaction().begin();
		dataEntityManager.persist(data);
		dataEntityManager.getTransaction().commit();

		int dataId = (int) entityManagerFactories.get(database).getPersistenceUnitUtil().getIdentifier(data);
		DataMappingObject dmo = new DataMappingObject(database, data.getClass().getSimpleName(), dataId, harmonyModelElement.getId(), harmonyModelElement.getClass()
				.getSimpleName());
		save(dmo);
	}

	/**
	 * Updates a data object in the database
	 * 
	 * @param database
	 * @param data
	 */
	public void updateData(String database, Object data) {
		EntityManager dataEntityManager = getEntityManager(database);
		dataEntityManager.getTransaction().begin();
		try {
			dataEntityManager.persist(dataEntityManager.merge(data));
			dataEntityManager.getTransaction().commit();
		} catch (Exception e) {
			dataEntityManager.getTransaction().rollback();
		}
	}

	/****************************
	 * Source Retrieval Methods *
	 ****************************/

	/**
	 * 
	 * @param id
	 * @return
	 */
	public Source getSource(int id) {
		EntityManager m = getEntityManager();
		m.getTransaction().begin();
		Source result = m.find(Source.class, id);
		m.getTransaction().commit();
		return result;
	}

	public Source getSourceByUrl(String url) {

		EntityManager m = getEntityManager();
		m.getTransaction().begin();
		try {
			CriteriaBuilder cb = m.getCriteriaBuilder();
			CriteriaQuery<Source> cq = cb.createQuery(Source.class);
			Root<Source> src = cq.from(Source.class);
			cq.select(src);
			cq.where(cb.equal(src.get("url"), url));
			TypedQuery<Source> q = m.createQuery(cq).setMaxResults(1);

			Source source = q.getSingleResult();

			m.getTransaction().commit();

			return source;
		} catch (NoResultException e) {
			m.getTransaction().rollback();
			return null;
		}
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

	/****************************************
	 * Save methods (for Source extractors) *
	 ****************************************/

	public void saveSource(Source s) {
		save(s);
	}

	public void saveEvent(Event e) {
		save(e);
	}

	public void saveItem(Item i) {
		save(i);
	}

	public void saveAuthor(Author a) {
		save(a);
	}

	public void saveAction(Action a) {
		save(a);
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

}
