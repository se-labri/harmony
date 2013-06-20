package fr.labri.harmony.core.source;

import java.util.logging.Logger;

import fr.labri.harmony.core.model.Event;

public interface Workspace {
	
	static final Logger LOGGER = Logger.getLogger("fr.labri.harmony.source");	

	/**
	 * Initialize the workspace.
	 * @throws WorkspaceException
	 */
    void init() throws WorkspaceException;
    
    
    /**
     * Updates the workspace to its state after the given {@link Event}
     * @param e
     * @return The workspace path
     * @throws WorkspaceException
     */
     void update(Event e) throws WorkspaceException;
    
    /**
     * Clean the workspace
     * @throws WorkspaceException
     */
    void clean() throws WorkspaceException;
    
    /**
     * @return The local path to the workspace.
     */
    String getPath();
    
}
