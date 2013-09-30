package fr.labri.harmony.core.dao;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import fr.labri.harmony.core.AbstractHarmonyService;
import fr.labri.harmony.core.config.model.DatabaseConfiguration;
import fr.labri.harmony.core.model.Action;
import fr.labri.harmony.core.model.Author;
import fr.labri.harmony.core.model.Data;
import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.Item;
import fr.labri.harmony.core.model.Source;

public interface Dao {

	final static String HARMONY_PERSISTENCE_UNIT = "harmony";

	static final Logger LOGGER = Logger.getLogger("fr.labri.harmony.core");

	Dao create(DatabaseConfiguration config);

	void saveSource(Source s);

	Source getSource(int id);

	/**
	 * 
	 * @param url
	 * @return The source associated to the corresponding URL
	 */
	Source getSourceByUrl(String url);

	<T> T refreshElement(T element);

	Event getEvent(Source s, String nativeId);

	List<Event> getEvents(Source s);

	void saveEvent(Event e);

	void saveItem(Item i);

	Item getItem(Source s, String nativeId);

	void saveAuthor(Author a);

	Author getAuthor(Source s, String nativeId);

	void saveAction(Action a);

	List<Action> getActions(Source s);

	/**
	 * Saves in the analysis database an entity attached to an element of the harmony model
	 * 
	 * @param analysis
	 *            The analysis that saves the data
	 * @param d
	 *            the data to save
	 * @param elementKind
	 *            The kind of the element the data is attached to. See the constants in the {@link Data} interface
	 * @param elementId
	 *            The id of the element the data is attached to.
	 */
	void saveData(AbstractHarmonyService service, Data d, int elementKind, int elementId);

	<D extends Data> List<D> getDataList(String analysis, Class<D> d, int elementKind, int elementId);

	<D extends Data> D getData(String analysis, Class<D> d, int elementKind, int elementId);
	
	<D extends Data> List<D> getDataList(String database, Class<D> d);

	Source reloadSource(Source source);

	void saveEvents(Collection<Event> events);

	void saveAuthors(Collection<Author> authors);

	void saveItems(Collection<Item> items);

	void saveActions(Collection<Action> actions);

	/**
	 * @param service
	 *            The Analysis associated to the required EntityManager. If null, the core {@link HarmonyEntityManagerFactory} will be returned
	 * @return The {@link HarmonyEntityManagerFactory} associated to the given service. This is useful to run queries that are not supported by this dao
	 */
	HarmonyEntityManagerFactory getEntityManagerFactory(AbstractHarmonyService service);

	void removeAllSources();

	/**
	 * 
	 * @param item
	 * @return The ordered list of action which affected the item, from the older to the most recent one. Actions are ordered with a time sort
	 */
	// FIXME: A topo sort would be more accurate
	List<Action> getActions(Item item);

}
