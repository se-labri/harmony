package fr.labri.harmony.core.source;

import fr.labri.harmony.core.config.model.SourceConfiguration;
import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.Source;

public interface SourceExtractor<W extends Workspace> {

	SourceConfiguration getConfig();

	Source getSource();

	W getWorkspace();

	/**
	 * Initializes the workspace and extracts the harmony model from the source </br>
	 * 
	 * @param extractActions
	 *            Whether {@link #extractActions(Event)} will be called or not.
	 */
	void initializeSource(boolean extractHamonyModel, boolean extractActions);
	
	/**
	 * Initializes a previously extracted Source. 
	 * Calling this method will not extract the HarmonyModel
	 * @param src
	 */
	void initializeExistingSource(Source src);

	/**
	 * Initialize the workspace by performing the clone operation on the repository
	 */
	void initializeWorkspace();

	void extractEvents();

	void extractActions(Event e);

}
