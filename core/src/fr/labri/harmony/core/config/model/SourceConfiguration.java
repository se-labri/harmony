package fr.labri.harmony.core.config.model;

import fr.labri.harmony.core.source.SourceExtractor;

public class SourceConfiguration {
	private String repositoryURL;
	private SourceExtractor<?> srcConnector;
	
	public SourceConfiguration(String repositoryURL, SourceExtractor<?> srcConnector) {
		super();
		this.repositoryURL = repositoryURL;
		this.srcConnector = srcConnector;
	}
	public String getRepositoryURL() {
		return repositoryURL;
	}
	public void setRepositoryURL(String repositoryURL) {
		this.repositoryURL = repositoryURL;
	}
	public SourceExtractor<?> getSrcConnector() {
		return srcConnector;
	}
	public void setSrcConnector(SourceExtractor<?> srcConnector) {
		this.srcConnector = srcConnector;
	}
	
	
	

}
