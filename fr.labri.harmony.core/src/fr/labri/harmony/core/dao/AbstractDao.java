package fr.labri.harmony.core.dao;

import java.util.Map;

import javax.persistence.EntityManager;

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

}
