package fr.labri.harmony.core.config.model;

import java.util.Collection;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AnalysisConfiguration {
	private String analysisName;
	private HashMap<String,String> options;
	
	// List of analyses required by this analysis and thus they need to be performed before it
	private Collection<String> dependencies;
	
	private FoldersConfiguration foldersConfiguration;

	public AnalysisConfiguration(){};
	
	public AnalysisConfiguration(String analysisName) {
		super();
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
	
	//TODO options have to be tested
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
	
	

}
