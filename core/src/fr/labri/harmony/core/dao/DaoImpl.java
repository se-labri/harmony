package fr.labri.harmony.core.dao;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.jpa.EntityManagerFactoryBuilder;

import fr.labri.harmony.core.analysis.Analysis;
import fr.labri.harmony.core.config.model.DatabaseConfiguration;
import fr.labri.harmony.core.model.Action;
import fr.labri.harmony.core.model.Author;
import fr.labri.harmony.core.model.Data;
import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.Item;
import fr.labri.harmony.core.model.Source;
import fr.labri.harmony.core.model.SourceElement;
import fr.labri.harmony.core.source.Workspace;

public class DaoImpl implements Dao {

	private Map<String, EntityManager> entityManagers;

	public DaoImpl(DatabaseConfiguration config) {
		BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
		try {
			Collection<ServiceReference<EntityManagerFactoryBuilder>> refs = context.getServiceReferences(EntityManagerFactoryBuilder.class, null);
			entityManagers = new HashMap<>();
			for (ServiceReference<EntityManagerFactoryBuilder> ref : refs) {
				String name = (String) ref.getProperty(EntityManagerFactoryBuilder.JPA_UNIT_NAME);
				HarmonyEntityManagerFactory factory = new HarmonyEntityManagerFactory(config, ref, context);

				entityManagers.put(name, factory.createEntityManager());

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
		return entityManagers.get(a);
	}

	private EntityManager getEntityManager() {
		return entityManagers.get(HARMONY_PERSISTENCE_UNIT);
	}

	@Override
	public void disconnect() {
		for (EntityManager f : entityManagers.values())
			f.close();
	}

	@Override
	public void saveSource(Source s) {
		save(s);
	}

	@Override
	public Source getSource(int id) {
		EntityManager m = getEntityManager();
		m.getTransaction().begin();
		Query query = m.createNamedQuery("getSourceById");
		query.setParameter("id", id);
		Source result = (Source) query.getSingleResult();
		m.getTransaction().commit();
		return result;
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
	public void saveData(Analysis a, Data d, int elementKind, int elementId) {
		d.setElementId(elementId);
		d.setElementKind(elementKind);
		EntityManager m = getEntityManager(a.getConfig().getPersistenceUnit());
		m.getTransaction().begin();
		m.persist(d);
		m.getTransaction().commit();
		if (LOGGER.isLoggable(Level.FINEST)) LOGGER.finest("Persisted data " + d + ".");
	}

	private <E> void save(E e) {
		EntityManager m = getEntityManager();
		m.getTransaction().begin();
		m.persist(e);
		m.getTransaction().commit();
		if (LOGGER.isLoggable(Level.FINEST)) LOGGER.finest("Persisted element " + e + ".");
	}

	@SuppressWarnings("unchecked")
	private <E extends SourceElement> E get(Class<E> clazz, Source s, String nativeId) {
		EntityManager m = getEntityManager();
		m.getTransaction().begin();
		String sQuery = "SELECT e FROM " + clazz.getSimpleName() + " e WHERE e.source.id = :sourceId AND e.nativeId = :nativeId";
		Query query = m.createQuery(sQuery);
		query.setParameter("sourceId", s.getId());
		query.setParameter("nativeId", nativeId);
		E result = null;
		try {
			result = (E) query.getSingleResult();
		} catch (NoResultException ex) {

		}
		m.getTransaction().commit();
		return result;
	}

	@SuppressWarnings("unchecked")
	private <E extends SourceElement> List<E> getList(Class<E> clazz, Source s) {
		EntityManager m = getEntityManager();
		m.getTransaction().begin();
		String sQuery = "SELECT e FROM " + clazz.getSimpleName() + " e";
		Query query = m.createQuery(sQuery);
		List<E> results = (List<E>) query.getResultList();
		m.getTransaction().commit();
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
	public Source refreshSource(Source source) {
		Workspace ws = source.getWorkspace();

		EntityManager m = getEntityManager();
		m.getTransaction().begin();
		source = m.merge(source);
		m.refresh(source);
		m.getTransaction().commit();

		// The workspace is transient, so we have to reset it when refreshing the source;
		source.setWorkspace(ws);

		return source;
	}

	@Override
	public void saveEvents(List<Event> events) {
		save(events);
	}

	@Override
	public void saveAuthors(List<Author> authors) {
		save(authors);
	}

	private <E> void save(List<E> elements) {
		EntityManager m = getEntityManager();
		if (!m.getTransaction().isActive())
		m.getTransaction().begin();
		for (E e : elements) {
			m.persist(e);
		}
		m.getTransaction().commit();
	}

	@Override
	public void saveItems(List<Item> items) {
		save(items);
	}

	@Override
	public void saveActions(List<Action> actions) {
		save(actions);
	}

}
