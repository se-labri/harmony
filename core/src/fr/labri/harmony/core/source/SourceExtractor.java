package fr.labri.harmony.core.source;

import fr.labri.harmony.core.config.model.SourceConfiguration;
import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.Source;

public interface SourceExtractor<W extends Workspace> {

	SourceConfiguration getConfig();

	Source getSource();

	W getWorkspace();

	/**
	 * Build the complete Harmony model thanks to the local repository.
	 * This method depends on :
	 * - initializeWorkspace() for building the local repository
	 * - extractSource() for building entirely the source
	 */
	void initializeSourceFully();
	
	//TODO offer partial loading of the model in the database with another method
	
	/**
	 * Initialize the workspace by performing the clone operation on the repository 
	 */
	void initializeWorkspace();
	
	/**
	 * Build the source entirely. That means that every Harmony entity (event,action) is loaded then stored in the database
	 * This method depends on :
	 * - extractEvents()
	 * - extractActions(Event e)
	 */
	void extractSource();

	void extractEvents();

	void extractActions(Event e);

}
