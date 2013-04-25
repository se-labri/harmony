package fr.labri.harmony.core.source;

import fr.labri.harmony.core.config.model.SourceConfiguration;
import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.Source;

public interface SourceExtractor<W extends Workspace> {

	SourceConfiguration getConfig();

	Source getSource();

	W getWorkspace();

	/**
	 * Initializes the source, which means performing a clone, or initializing
	 * the connection to the server
	 * 
	 */
	//TODO : add a runnable, which will perform the clone operation
	void initializeSource();

	void extractSource();

	void extractEvents();

	void extractActions(Event e);

}
