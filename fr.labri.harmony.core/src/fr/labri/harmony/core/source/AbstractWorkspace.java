package fr.labri.harmony.core.source;

import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.Item;



public abstract class AbstractWorkspace implements Workspace {
	
	protected SourceExtractor<?> sourceExtractor;
	
	public AbstractWorkspace(SourceExtractor<?> sourceExtractor) {
		this.sourceExtractor = sourceExtractor;
	}

	public String getTmpPath() {
		return sourceExtractor.getConfig().getFoldersConfiguration().getTmpFolder();
	}
	
	public String getOutPath() {
		return sourceExtractor.getConfig().getFoldersConfiguration().getOutFolder();
	}
	
	public String getUrl() {
		return sourceExtractor.getConfig().getRepositoryURL();
	}
	
	@Override
	public void update(Event e) throws WorkspaceException {
		throw new WorkspaceException("Not implemented");
	}
	
	@Override
	public void update(Event e, Item item) throws WorkspaceException {
		throw new WorkspaceException("Not implemented");
	}
	
	@Override
	public void clean() throws WorkspaceException {
		throw new WorkspaceException("Not implemented");
	}
	
}