package fr.labri.harmony.core.config;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonUtils {

	public static Integer getInteger(JsonNode n) {
		return (n == null) ? null : n.asInt();
	}

	public static String getString(JsonNode n) {
		return (n == null) ? null : n.asText();
	}
	
	public static Double getDouble(JsonNode n) {
		return (n == null) ? null : n.asDouble();
	}
	
	public static Boolean getBoolean(JsonNode n) {
		return (n == null) ? null : n.asBoolean();
	}
	
}
