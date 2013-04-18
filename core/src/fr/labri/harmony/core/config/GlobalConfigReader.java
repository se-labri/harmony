package fr.labri.harmony.core.config;

import static fr.labri.harmony.core.config.ConfigProperties.*;
import static fr.labri.harmony.core.config.JsonUtils.*;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 
 * Reads a global configuration file and adds missing default values to the
 * config (e.g. number of threads, working folders)
 * 
 */
public class GlobalConfigReader {

	private JsonNode globalConfig;

	public GlobalConfigReader(String path) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			globalConfig = mapper.readTree(new File(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ObjectNode getDatabaseConfig() {
		return (ObjectNode) globalConfig.get(DATABASE);
	}

	public ArrayNode getClasspathConfig() {
		return (ArrayNode) ((globalConfig.get(CLASSPATH) != null) ? globalConfig.get(CLASSPATH) : JsonNodeFactory.instance.arrayNode());
	}

	public ObjectNode getManageCreateSourcesConfig() {
		if (!globalConfig.has(MANAGE_CREATE_SOURCES)) return ConfigBuilder.getManageCreateSourcesConfig(null, null);
		JsonNode n = globalConfig.get(MANAGE_CREATE_SOURCES);
		return ConfigBuilder.getManageCreateSourcesConfig(getInteger(n.get(NUM_THREADS)), getInteger(n.get(TIMEOUT)));
	}

	public ObjectNode getFoldersConfig() {
		if (!globalConfig.has(FOLDERS)) return ConfigBuilder.getFoldersConfig(null, null);
		JsonNode n = globalConfig.get(FOLDERS);
		return ConfigBuilder.getFoldersConfig(getString(n.get(OUT)), getString(n.get(TMP)));
	}

	public ArrayNode getAnalysesConfig() {
		ArrayNode n = (ArrayNode) globalConfig.get(ANALYSES);
		for (JsonNode c : n) ((ObjectNode) c).put(FOLDERS, getFoldersConfig());
		return n;
	}

}
