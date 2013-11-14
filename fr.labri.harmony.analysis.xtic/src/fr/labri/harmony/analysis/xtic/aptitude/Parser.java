package fr.labri.harmony.analysis.xtic.aptitude;

import java.util.HashMap;
import java.util.Map;

public enum Parser {

	JAVA,
	XML,
	JS;
	
	Map<String, String> options = new HashMap<String, String>();
	
	public Map<String, String> getOptions() {
		return options;
	}

	public void setOptions(Map<String, String> options) {
		this.options = options;
	}

	static Parser buildParser(String value) {
		if(value.toLowerCase().equals("java")) return Parser.JAVA;
		if(value.toLowerCase().equals("js")) return Parser.JS;
		if(value.toLowerCase().equals("xml")) return Parser.XML;
		return null;
	}
	
}
