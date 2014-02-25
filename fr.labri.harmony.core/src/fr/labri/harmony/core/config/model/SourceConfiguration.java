package fr.labri.harmony.core.config.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import fr.labri.harmony.core.source.SourceExtractor;

/**
 * Model class for the configuration of a source. It is mapped to a JSON object thanks to the annotations on the
 * getters/setters
 * 
 */
public class SourceConfiguration {
	private String repositoryURL;
	private String sourceExtractorName;
	private String username;
	private String password;
	private String pathOnServer;
	private String itemFilter;
	private FoldersConfiguration foldersConfiguration;
	private String configurationFileName;
	private Boolean extractAllBranches;

	private HashMap<String, Object> options;

	public SourceConfiguration() {
		repositoryURL = null;
		sourceExtractorName = null;
		username = null;
		password = null;
		pathOnServer = null;
		itemFilter = null;
		foldersConfiguration = null;
		configurationFileName = null;
		extractAllBranches = null;
		options = new HashMap<>();
	}

	public SourceConfiguration(String repositoryURL, SourceExtractor<?> srcConnector) {
		this();
		this.repositoryURL = repositoryURL;
	}
	
	public void addDefaultValues(SourceConfiguration defaultConfig) {
		if (repositoryURL == null) repositoryURL = defaultConfig.getRepositoryURL();
		if (sourceExtractorName == null) sourceExtractorName = defaultConfig.getSourceExtractorName();
		if (username == null) username = defaultConfig.getUsername();
		if (password == null) password = defaultConfig.getPassword();
		if (pathOnServer == null) pathOnServer = defaultConfig.getPathOnServer();
		if (itemFilter == null) itemFilter = defaultConfig.getItemFilter();
		if (foldersConfiguration == null) foldersConfiguration = defaultConfig.getFoldersConfiguration();
		if (configurationFileName == null) configurationFileName = defaultConfig.getConfigurationFileName();
		if (extractAllBranches == null) extractAllBranches = defaultConfig.extractAllBranches();
		for (Map.Entry<String, Object> entry : defaultConfig.getOptions().entrySet()) {
			if (!options.containsKey(entry.getKey())) options.put(entry.getKey(), entry.getValue());
		}
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
	
	@JsonProperty("item-filter")
	public String getItemFilter() {
		return 	itemFilter;
	}
	
	public void setItemFilter(String itemFilter) {
		this.itemFilter = itemFilter;
	}

	public void setSourceExtractorName(String sourceExtractorName) {
		this.sourceExtractorName = sourceExtractorName;
	}

	@JsonIgnore
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

	@JsonProperty("user")
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@JsonProperty("password")
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@JsonIgnore
	public String getConfigurationFileName() {
		return configurationFileName;
	}

	public void setConfigurationFileName(String configurationFileName) {
		this.configurationFileName = configurationFileName;
	}

	@JsonProperty("path")
	public String getPathOnServer() {
		return pathOnServer;
	}

	public void setPathOnServer(String pathOnServer) {
		this.pathOnServer = pathOnServer;
	}

	public Object getOption(String key) {
		return options.get(key);
	}

	public boolean extractAllBranches() {
		return (extractAllBranches == null) ? false : extractAllBranches;
	}

	@JsonProperty("extract-all-branches")
	public void setExtractAllBranches(boolean extractAllBranches) {
		this.extractAllBranches = extractAllBranches;
	}

	public boolean hasOption(String optItemFilter) {
		return options.containsKey(optItemFilter);
	}

}
