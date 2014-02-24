package fr.labri.harmony.core.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import fr.labri.harmony.core.model.Author;
import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.Item;
import fr.labri.harmony.core.model.Source;
import fr.labri.harmony.core.model.SourceElement;

public abstract class AbstractDao {
	
	public static final String HARMONY_PERSISTENCE_UNIT = "harmony";
	
	protected HarmonyEntityManagerFactory harmonyModelEMF;
	
	public HarmonyEntityManagerFactory getHarmonyModelEMF() {
		return harmonyModelEMF;
	}

	AbstractDao(HarmonyEntityManagerFactory harmonyModelEMF) { 
		this.harmonyModelEMF = harmonyModelEMF;
	}
		
	public EntityManager getEntityManager() {
		return harmonyModelEMF.createEntityManager();
	}
	
	/*******************
	 * Generic Methods *
	 *******************/
	
	protected <E> void save(Collection<E> elements) {
		EntityManager m = getEntityManager();
		if (!m.getTransaction().isActive()) m.getTransaction().begin();
		for (E e : elements) {
			m.persist(e);
		}
		m.getTransaction().commit();
		m.close();
	}
	
	/**
	 * 
	 * @param clazz
	 * @param s
	 * @param nativeId
	 * @return
	 */
	protected <E extends SourceElement> E get(Class<E> clazz, Source s, String nativeId) {
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

	protected <E extends SourceElement> List<E> getList(Class<E> clazz, Source s) {
		EntityManager m = getEntityManager();
		String sQuery = "SELECT e FROM " + clazz.getSimpleName() + " e WHERE e.source.id = :sourceId AND e.nativeId = :nativeId";
		TypedQuery<E> query = m.createQuery(sQuery, clazz);
		query.setParameter("sourceId", s.getId());
		try {
			return query.getResultList();
		} catch (NoResultException ex) {
			return new ArrayList<>();
		}
	}

	protected <T> T refreshElement(T element) {
		EntityManager m = getEntityManager();
		m.getTransaction().begin();
		element = m.merge(element);
		m.refresh(element);
		m.getTransaction().commit();

		return element;
	}
	
	/**
	 * Saves an entity in the core database, in a single transaction (inefficient for multiple save)
	 * @param e The entity
	 */
	protected <E> void save(E e) {
		EntityManager m = getEntityManager();
		m.getTransaction().begin();
		m.persist(e);
		m.getTransaction().commit();
		m.close();
	}
	
	/**
	 * Updates an entity in the core database, in a single transaction (inefficient for multiple update)
	 * @param e The entity
	 */
	protected <E> void update(E e) {
		EntityManager m = getEntityManager();
		m.getTransaction().begin();
		m.merge(e);
		m.getTransaction().commit();
		m.close();
	}
	
	/**
	 * 
	 * @param s
	 * @param nativeId
	 * @return The event with the given nativeId, in the given Source, or null if there is no such event
	 */
	public Event getEvent(Source s, String nativeId) {
		return get(Event.class, s, nativeId);
	}

	public Item getItem(Source s, String nativeId) {
		return get(Item.class, s, nativeId);
	}
	
	public Author getAuthor(Source s, String nativeId) {
		return get(Author.class, s, nativeId);
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

}
