package fr.labri.harmony.core.config.model;

import java.util.Collection;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AnalysisConfiguration {
	private String analysisName;
	private HashMap<String,String> options;
	
	// List of analyses required by this analysis and thus they need to be performed before it
	private Collection<String> dependencies;
	
	

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

	@JsonProperty("options")
	public HashMap<String, String> getOptions() {
		return options;
	}

	public void setOptions(HashMap<String, String> options) {
		this.options = options;
	}

	public String getOutFolder() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Collection<String> getDependencies() {
		return dependencies;
	}

	public void setDependencies(Collection<String> dependencies) {
		this.dependencies = dependencies;
	}
	

}
