package fr.labri.harmony.core.config.model;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonProperty;

import fr.labri.harmony.core.source.SourceExtractor;

public class SourceConfiguration {
	private String repositoryURL;
	private String sourceExtractorName;
	private FoldersConfiguration foldersConfiguration;

	private HashMap<String, Object> options;

	
	public SourceConfiguration() {
		options = new HashMap<>();
	}

	public SourceConfiguration(String repositoryURL, SourceExtractor<?> srcConnector) {
		this();
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

	public void setFoldersConfiguration(FoldersConfiguration foldersConfiguration) {
		this.foldersConfiguration = foldersConfiguration;
	}
	
	public FoldersConfiguration getFoldersConfiguration() {
		return foldersConfiguration;
	}

	@JsonProperty("options")
	public HashMap<String, Object> getOptions() {
		return options;
	}

	public void setOptions(HashMap<String, Object> options) {
		this.options = options;
	}

}
