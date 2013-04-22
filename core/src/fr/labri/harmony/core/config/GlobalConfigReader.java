package fr.labri.harmony.core.config;

import static fr.labri.harmony.core.config.ConfigProperties.ANALYSES;
import static fr.labri.harmony.core.config.ConfigProperties.CLASSPATH;
import static fr.labri.harmony.core.config.ConfigProperties.DATABASE;
import static fr.labri.harmony.core.config.ConfigProperties.FOLDERS;
import static fr.labri.harmony.core.config.ConfigProperties.MANAGE_CREATE_SOURCES;
import static fr.labri.harmony.core.config.ConfigProperties.NUM_THREADS;
import static fr.labri.harmony.core.config.ConfigProperties.OUT;
import static fr.labri.harmony.core.config.ConfigProperties.TIMEOUT;
import static fr.labri.harmony.core.config.ConfigProperties.TMP;
import static fr.labri.harmony.core.config.JsonUtils.getInteger;
import static fr.labri.harmony.core.config.JsonUtils.getString;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fr.labri.harmony.core.config.model.DatabaseConfiguration;

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

	public DatabaseConfiguration getDatabaseConfig() {
		JsonNode dbNode = globalConfig.get(DATABASE);

		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readValue(dbNode.toString(), DatabaseConfiguration.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
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
		for (JsonNode c : n)
			((ObjectNode) c).put(FOLDERS, getFoldersConfig());
		return n;
	}

}
