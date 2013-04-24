package fr.labri.harmony.core.source;


public abstract class AbstractWorkspace implements Workspace {
	
	protected SourceExtractor<?> sourceExtractor;
	
	public AbstractWorkspace() {
	}
	
	@Override
	public void setSourceExtractor(SourceExtractor<?> sourceExtractor) {
		this.sourceExtractor = sourceExtractor;
	}
	
	public String getTmpPath() {
		return sourceExtractor.getConfig().getTmpFolder();
	}
	
	public String getOutPath() {
		return sourceExtractor.getConfig().getOutFolder();
	}
	
	public String getUrl() {
		return sourceExtractor.getConfig().getRepositoryURL();
	}
	
}