package fr.labri.harmony.core.config.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AnalysisConfiguration {
	private String analysisName;
	private String persistenceUnit;

	private HashMap<String, String> options;

	// List of analyses required by this analysis and thus they need to be performed before it
	private Collection<String> dependencies;

	private FoldersConfiguration foldersConfiguration;

	/*
	 * Whether the analysis requires the source to extract actions or not. If not set in the json configuration, the Java default will be used, which is 'false'
	 */
	private boolean requireActions;

	public AnalysisConfiguration() {
		dependencies = Collections.emptyList();
	};

	public AnalysisConfiguration(String analysisName) {
		this();
		this.analysisName = analysisName;
	}

	public AnalysisConfiguration(String analysisName, HashMap<String, String> options) {
		this(analysisName);
		this.options = options;
	}

	public String getAnalysisName() {
		return analysisName;
	}

	@JsonProperty("class")
	public void setAnalysisName(String analysisName) {
		this.analysisName = analysisName;
	}

	// TODO options have to be tested
	@JsonProperty("options")
	public HashMap<String, String> getOptions() {
		return options;
	}

	public void setOptions(HashMap<String, String> options) {
		this.options = options;
	}

	public Collection<String> getDependencies() {
		return dependencies;
	}

	@JsonIgnore
	public void setDependencies(Collection<String> dependencies) {
		this.dependencies = dependencies;
	}

	public FoldersConfiguration getFoldersConfiguration() {
		return foldersConfiguration;
	}

	@JsonIgnore
	public void setFoldersConfiguration(FoldersConfiguration foldersConfiguration) {
		this.foldersConfiguration = foldersConfiguration;
	}

	@JsonIgnore
	public String getPersistenceUnit() {
		return persistenceUnit;
	}

	public void setPersistenceUnit(String persistenceUnit) {
		this.persistenceUnit = persistenceUnit;
	}

	/**
	 * @return Whether the analysis requires the source to extract actions or not.
	 */
	public boolean requireActions() {
		return requireActions;
	}

	@JsonProperty("require-actions")
	public void setRequireActions(boolean requireActions) {
		this.requireActions = requireActions;
	}

}
