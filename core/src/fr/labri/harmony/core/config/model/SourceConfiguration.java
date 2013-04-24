package fr.labri.harmony.core.config.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import fr.labri.harmony.core.source.SourceExtractor;

public class SourceConfiguration {
	private String repositoryURL;
	private String sourceExtractorClass;

	public SourceConfiguration(String repositoryURL, SourceExtractor<?> srcConnector) {
		super();
		this.repositoryURL = repositoryURL;
	}

	public String getRepositoryURL() {
		return repositoryURL;
	}

	@JsonProperty("url")
	public void setRepositoryURL(String repositoryURL) {
		this.repositoryURL = repositoryURL;
	}
	
	public String getSourceExtractorName() {
		return sourceExtractorClass;
	}

	public String getTmpFolder() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getOutFolder() {
		// TODO Auto-generated method stub
		return null;
	}

}
