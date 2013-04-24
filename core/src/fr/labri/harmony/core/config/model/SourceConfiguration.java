package fr.labri.harmony.core.config.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import fr.labri.harmony.core.source.SourceExtractor;

public class SourceConfiguration {
	private String repositoryURL;
	private String sourceExtractorName;

	public SourceConfiguration() {
	}

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

	@JsonProperty("class")
	public String getSourceExtractorName() {
		return sourceExtractorName;
	}
	
	public void setSourceExtractorName(String sourceExtractorName) {
		this.sourceExtractorName = sourceExtractorName;
	}

	@JsonIgnore
	public String getTmpFolder() {
		// TODO Auto-generated method stub
		return null;
	}

	@JsonIgnore
	public String getOutFolder() {
		// TODO Auto-generated method stub
		return null;
	}

}
