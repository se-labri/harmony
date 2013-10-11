package fr.labri.harmony.core.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import fr.labri.harmony.core.AbstractHarmonyService;
import fr.labri.harmony.core.log.HarmonyLogger;
import fr.labri.harmony.core.model.Action;
import fr.labri.harmony.core.model.Author;
import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.HarmonyModelElement;
import fr.labri.harmony.core.model.Item;
import fr.labri.harmony.core.model.Source;
import fr.labri.harmony.core.model.SourceElement;
import fr.labri.harmony.core.source.Workspace;

public class Dao extends AbstractDao {


	Dao(Map<String, HarmonyEntityManagerFactory> entityManagerFactories) {
		super(entityManagerFactories);
	}

	public void saveSource(Source s) {
		save(s);
	}

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

	public Event getEvent(Source s, String nativeId) {
		return get(Event.class, s, nativeId);
	}

	public List<Event> getEvents(Source s) {
		return getList(Event.class, s);
	}

	public void saveEvent(Event e) {
		save(e);
	}

	public void saveItem(Item i) {
		save(i);
	}

	public Item getItem(Source s, String nativeId) {
		return get(Item.class, s, nativeId);
	}

	public void saveAuthor(Author a) {
		save(a);
	}

	public Author getAuthor(Source s, String nativeId) {
		return get(Author.class, s, nativeId);
	}

	public void saveAction(Action a) {
		save(a);
	}

	public List<Action> getActions(Source s) {
		return getList(Action.class, s);
	}

	private <E extends SourceElement> E get(Class<E> clazz, Source s, String nativeId) {
		EntityManager m = getEntityManager();
		m.getTransaction().begin();
		String sQuery = "SELECT e FROM " + clazz.getSimpleName() + " e WHERE e.source.id = :sourceId AND e.nativeId = :nativeId";
		TypedQuery<E> query = m.createQuery(sQuery, clazz);
		query.setParameter("sourceId", s.getId());
		query.setParameter("nativeId", nativeId).setMaxResults(1);
		E result = null;
		try {
			result = query.getSingleResult();
		} catch (NoResultException ex) {

		}
		m.getTransaction().commit();
		return result;
	}

	private <E extends SourceElement> List<E> getList(Class<E> clazz, Source s) {
		EntityManager m = getEntityManager();
		m.getTransaction().begin();
		CriteriaBuilder builder = m.getCriteriaBuilder();
		CriteriaQuery<E> query = builder.createQuery(clazz);
		Root<E> root = query.from(clazz);
		query.select(root);
		query.where(builder.equal(root.get("source"), s));

		List<E> results = m.createQuery(query).getResultList();
		m.getTransaction().commit();
		m.close();
		return results;
	}

	public <T> T refreshElement(T element) {
		EntityManager m = getEntityManager();
		m.getTransaction().begin();
		element = m.merge(element);
		m.refresh(element);
		m.getTransaction().commit();

		return element;
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

	public void saveEvents(Collection<Event> events) {
		save(events);
	}

	public void saveAuthors(Collection<Author> authors) {
		save(authors);
	}

	private <E> void save(Collection<E> elements) {
		EntityManager m = getEntityManager();
		if (!m.getTransaction().isActive()) m.getTransaction().begin();
		for (E e : elements) {
			m.persist(e);
		}
		m.getTransaction().commit();
		m.close();
	}

	public void saveItems(Collection<Item> items) {
		save(items);
	}

	public void saveActions(Collection<Action> actions) {
		save(actions);
	}

	public synchronized HarmonyEntityManagerFactory getEntityManagerFactory(AbstractHarmonyService harmonyService) {
		String puName;
		if (harmonyService == null)
			puName = HARMONY_PERSISTENCE_UNIT;
		else
			puName = harmonyService.getPersitenceUnitName();
		return entityManagerFactories.get(puName);
	}

	public void removeAllSources() {
		EntityManager m = getEntityManager();
		m.getTransaction().begin();
		TypedQuery<Source> query = m.createQuery("SELECT e FROM Source e", Source.class);
		for (Source s : (Collection<Source>) query.getResultList()) {
			m.remove(s);
		}
		m.getTransaction().commit();
	}

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
	
	/************************************
	 *	Access methods for data Objects *  
	 ************************************/
	
	/** 
	 * @param database
	 * @param dataClass
	 * @param harmonyModelElement
	 * @return All data objects associated to the given {@link HarmonyModelElement}, in the provided database, and of the provided type.
	 */
	public <D> List<D> getData(String database, Class<D> dataClass, HarmonyModelElement harmonyModelElement) {
		// Retrieve the data ids in the core mapping table
		EntityManager coreEntityManager = getEntityManager();

		String queryString = "SELECT dmo.dataId FROM" + DataMappingObject.class.getSimpleName() + "dmo WHERE dmo.elementId = :elementId "
				+ "AND dmo.elementKind = :elementKind AND dmo.databaseName = :databaseName AND dmo.dataClassSimpleName = :dataClass";

		TypedQuery<Integer> query = coreEntityManager.createQuery(queryString, Integer.class);
		query.setParameter("elementId", harmonyModelElement.getId());
		query.setParameter("elementKind", harmonyModelElement.getClass().getSimpleName());
		query.setParameter("databaseName", database);
		query.setParameter("dataClass", dataClass.getSimpleName());

		List<Integer> dataIds = query.getResultList();

		// Retrieve the data objects in the analysis database
		EntityManager dataEntityManager = getEntityManager(database);
		ArrayList<D> dataList = new ArrayList<>();
		for (Integer dataId : dataIds) {
			dataList.add(dataEntityManager.find(dataClass, dataId));
		}
		return dataList;
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
	 * @param database The name of the database in which the data Object will be saved (which corresponds to the persistence unit name of your analysis) 
	 * @param data The data to be saved. It has to be annotated with JPA annotations (c.f. {@link Entity})
	 * @param harmonyModelElement The element of the model the data is associated to.
	 */
	public void saveData(String database, Object data, HarmonyModelElement harmonyModelElement) {

		EntityManager dataEntityManager = getEntityManager(database);
		dataEntityManager.getTransaction().begin();
		dataEntityManager.persist(data);
		dataEntityManager.getTransaction().commit();
		
		int dataId = (int) entityManagerFactories.get(database).getPersistenceUnitUtil().getIdentifier(data);
		DataMappingObject dmo = new DataMappingObject(database, data.getClass().getSimpleName(), dataId, harmonyModelElement.getId(), harmonyModelElement.getClass().getSimpleName());
		save(dmo);
	}

}
