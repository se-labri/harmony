package fr.labri.harmony.core.source;

import fr.labri.harmony.core.model.Event;

/**
 * This is a dummy workspace, useful for bugtrackers create sources. 
 * 
 * Calling update on this workspace will throw a {@link WorkspaceException}.
 */
public class DefaultWorkspace extends AbstractWorkspace {

	public DefaultWorkspace(SourceExtractor<?> sourceExtractor) {
		super(sourceExtractor);
	}

	@Override
	public void init() throws WorkspaceException {
	}

	@Override
	public void update(Event e) throws WorkspaceException {
		throw new WorkspaceException("Workspace not updatable.");
	}

	@Override
	public void clean() throws WorkspaceException {	
	}

	/**
	 * @return null. This workspace does not have a local path
	 */
	@Override
	public String getPath() {
		return null;
	}
	
}