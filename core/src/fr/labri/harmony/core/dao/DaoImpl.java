package fr.labri.harmony.core.dao;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.jpa.EntityManagerFactoryBuilder;

import fr.labri.harmony.core.AbstractHarmonyService;
import fr.labri.harmony.core.config.model.DatabaseConfiguration;
import fr.labri.harmony.core.log.HarmonyLogger;
import fr.labri.harmony.core.model.Action;
import fr.labri.harmony.core.model.Author;
import fr.labri.harmony.core.model.Data;
import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.Item;
import fr.labri.harmony.core.model.Source;
import fr.labri.harmony.core.model.SourceElement;
import fr.labri.harmony.core.source.Workspace;

public class DaoImpl implements Dao {

	/**
	 * We keep references on the EntityManagerFactories instead of the entity
	 * managers themselves
	 */
	private Map<String, HarmonyEntityManagerFactory> entityManagerFactories;

	public DaoImpl(DatabaseConfiguration config) {
		BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
		try {
			Collection<ServiceReference<EntityManagerFactoryBuilder>> refs = context.getServiceReferences(EntityManagerFactoryBuilder.class, null);
			entityManagerFactories = new HashMap<>();
			for (ServiceReference<EntityManagerFactoryBuilder> ref : refs) {
				String name = (String) ref.getProperty(EntityManagerFactoryBuilder.JPA_UNIT_NAME);
				HarmonyEntityManagerFactory factory = new HarmonyEntityManagerFactory(config, ref, context);

				entityManagerFactories.put(name, factory);

			}

			if (config.cleanDatabase()) {
				HarmonyLogger.info("Cleaning database...");
				removeAllSources();
			}

		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Dao create(DatabaseConfiguration config) {
		return new DaoImpl(config);
	}

	private EntityManager getEntityManager(String a) {
		HarmonyEntityManagerFactory f = entityManagerFactories.get(a);
		if (f == null) return null;
		return f.createEntityManager();
	}

	private EntityManager getEntityManager() {
		return getEntityManager(HARMONY_PERSISTENCE_UNIT);
	}

	@Override
	public void saveSource(Source s) {
		save(s);
	}

	@Override
	public Source getSource(int id) {
		EntityManager m = getEntityManager();
		m.getTransaction().begin();
		Source result = m.find(Source.class, id);
		m.getTransaction().commit();
		return result;
	}

	@Override
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

	@Override
	public Event getEvent(Source s, String nativeId) {
		return get(Event.class, s, nativeId);
	}

	@Override
	public List<Event> getEvents(Source s) {
		return getList(Event.class, s);
	}

	@Override
	public void saveEvent(Event e) {
		save(e);
	}

	@Override
	public void saveItem(Item i) {
		save(i);
	}

	@Override
	public Item getItem(Source s, String nativeId) {
		return get(Item.class, s, nativeId);
	}

	@Override
	public void saveAuthor(Author a) {
		save(a);
	}

	@Override
	public Author getAuthor(Source s, String nativeId) {
		return get(Author.class, s, nativeId);
	}

	@Override
	public void saveAction(Action a) {
		save(a);
	}

	@Override
	public List<Action> getActions(Source s) {
		return getList(Action.class, s);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <D extends Data> List<D> getDataList(String a, Class<D> d, int elementKind, int elementId) {
		EntityManager m = getEntityManager(a);
		m.getTransaction().begin();
		String sQuery = "SELECT d FROM " + d.getSimpleName() + " d WHERE d.elementKind = :elementKind AND d.elementId = :elementId";
		Query query = m.createQuery(sQuery);
		query.setParameter("elementKind", elementKind);
		query.setParameter("elementId", elementId);
		List<D> results = query.getResultList();
		m.getTransaction().commit();
		return results;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <D extends Data> D getData(String a, Class<D> d, int elementKind, int elementId) {
		EntityManager m = getEntityManager(a);
		m.getTransaction().begin();
		String sQuery = "SELECT d FROM " + d.getSimpleName() + " d WHERE d.elementKind = :elementKind AND d.elementId = :elementId";
		Query query = m.createQuery(sQuery);
		query.setParameter("elementKind", elementKind);
		query.setParameter("elementId", elementId);
		D result = (D) query.getSingleResult();
		m.getTransaction().commit();
		return result;
	}

	@Override
	public void saveData(AbstractHarmonyService service, Data d, int elementKind, int elementId) {
		d.setElementId(elementId);
		d.setElementKind(elementKind);
		EntityManager m = getEntityManager(service.getPersitenceUnitName());
		m.getTransaction().begin();
		m.persist(d);
		m.getTransaction().commit();
		m.close();
		if (LOGGER.isLoggable(Level.FINEST)) LOGGER.finest("Persisted data " + d + ".");
	}

	private <E> void save(E e) {
		EntityManager m = getEntityManager();
		m.getTransaction().begin();
		m.persist(e);
		m.getTransaction().commit();
		m.close();
		if (LOGGER.isLoggable(Level.FINEST)) LOGGER.finest("Persisted element " + e + ".");
	}

	@SuppressWarnings("unchecked")
	private <E extends SourceElement> E get(Class<E> clazz, Source s, String nativeId) {
		EntityManager m = getEntityManager();
		m.getTransaction().begin();
		String sQuery = "SELECT e FROM " + clazz.getSimpleName() + " e WHERE e.source.id = :sourceId AND e.nativeId = :nativeId";
		Query query = m.createQuery(sQuery);
		query.setParameter("sourceId", s.getId());
		query.setParameter("nativeId", nativeId).setMaxResults(1);
		E result = null;
		try {
			result = (E) query.getSingleResult();
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

	@Override
	public <T> T refreshElement(T element) {
		EntityManager m = getEntityManager();
		m.getTransaction().begin();
		element = m.merge(element);
		m.refresh(element);
		m.getTransaction().commit();

		return element;
	}

	@Override
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

	@Override
	public void saveEvents(Collection<Event> events) {
		save(events);
	}

	@Override
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

	@Override
	public void saveItems(Collection<Item> items) {
		save(items);
	}

	@Override
	public void saveActions(Collection<Action> actions) {
		save(actions);
	}

	@Override
	public synchronized HarmonyEntityManagerFactory getEntityManagerFactory(AbstractHarmonyService harmonyService) {
		String puName;
		if (harmonyService == null) puName = HARMONY_PERSISTENCE_UNIT;
		else puName = harmonyService.getPersitenceUnitName();
		return entityManagerFactories.get(puName);
	}

	@Override
	public void removeAllSources() {
		EntityManager m = getEntityManager();
		m.getTransaction().begin();
		TypedQuery<Source> query = m.createQuery("SELECT e FROM Source e", Source.class);
		for (Source s : (Collection<Source>) query.getResultList()) {
			m.remove(s);
		}
		m.getTransaction().commit();
	}

}
