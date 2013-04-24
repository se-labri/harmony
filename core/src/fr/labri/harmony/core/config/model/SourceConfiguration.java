package fr.labri.harmony.core.config.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import fr.labri.harmony.core.source.SourceExtractor;

public class SourceConfiguration {
	private String repositoryURL;
	private SourceExtractor<?> srcConnector;
	private String sourceExtractorClass;

	public SourceConfiguration(String repositoryURL, SourceExtractor<?> srcConnector) {
		super();
		this.repositoryURL = repositoryURL;
		this.srcConnector = srcConnector;
	}

	public String getRepositoryURL() {
		return repositoryURL;
	}

	@JsonProperty("url")
	public void setRepositoryURL(String repositoryURL) {
		this.repositoryURL = repositoryURL;
	}

	public SourceExtractor<?> getSrcConnector() {
		return srcConnector;
	}

	@JsonIgnore
	public void setSourceExtractor(SourceExtractor<?> srcConnector) {
		this.srcConnector = srcConnector;
	}
	
	public String getSourceExtractorName() {
		return sourceExtractorClass;
	}

}
