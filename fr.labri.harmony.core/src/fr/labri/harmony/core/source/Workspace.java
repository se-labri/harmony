package fr.labri.harmony.core.source;

import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.Item;

/**
 * A workspace represents a source's working directory, e.g. the clone destination for a git repository.
 * It is initialized to the latest event in the repository, and can be updated to a particular version (corresponding to an event)
 */
public interface Workspace {
	
	/**
	 * Initialize the workspace.
	 * @throws WorkspaceException
	 */
    void init() throws WorkspaceException;
    
    
    /**
     * Updates the workspace to its state after the given {@link Event}
     * @param e
     * @throws WorkspaceException
     */
     void update(Event e) throws WorkspaceException;
     
     /**
      * Updates a single item in the workspace to the version it had after the given event
      * @param e
      * @param item
      * @throws WorkspaceException
      */
     void update(Event e, Item item) throws WorkspaceException;
    
    /**
     * Cleans the workspace, i.e. deletes the directory in which it is contained.
     * @throws WorkspaceException
     */
    void clean() throws WorkspaceException;
    
    /**
     * @return The local path to the workspace.
     */
    String getPath();
    
}
