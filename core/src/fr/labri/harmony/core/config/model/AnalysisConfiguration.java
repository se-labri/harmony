package fr.labri.harmony.core.config.model;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AnalysisConfiguration {
	private String analysisName;
	private HashMap<String,String> options;
	
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
	
	

}
