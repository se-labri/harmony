package fr.labri.harmony.core.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import fr.labri.harmony.core.AbstractHarmonyService;
import fr.labri.harmony.core.model.Source;
import fr.labri.harmony.core.model.SourceElement;

public abstract class AbstractDao {
	
	public static final String HARMONY_PERSISTENCE_UNIT = "harmony";
	
	/**
	 * We keep references on the EntityManagerFactories instead of the entity managers themselves
	 */
	protected Map<String, HarmonyEntityManagerFactory> entityManagerFactories;
	
	AbstractDao(Map<String, HarmonyEntityManagerFactory> entityManagerFactories) {
		this.entityManagerFactories = entityManagerFactories;
	}
		
	public Map<String, HarmonyEntityManagerFactory> getEntityManagerFactories() {
		return entityManagerFactories;
	}
	
	public EntityManager getEntityManager(String a) {
		HarmonyEntityManagerFactory f = entityManagerFactories.get(a);
		if (f == null) return null;
		return f.createEntityManager();
	}

	public EntityManager getEntityManager() {
		return getEntityManager(HARMONY_PERSISTENCE_UNIT);
	}
	
	public synchronized HarmonyEntityManagerFactory getEntityManagerFactory(AbstractHarmonyService harmonyService) {
		String puName;
		if (harmonyService == null) puName = HARMONY_PERSISTENCE_UNIT;
		else puName = harmonyService.getPersitenceUnitName();
		return entityManagerFactories.get(puName);
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

}
