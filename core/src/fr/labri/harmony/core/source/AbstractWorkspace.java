package fr.labri.harmony.core.source;



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
	
}