package fr.labri.harmony.core.config;

import static fr.labri.harmony.core.config.ConfigProperties.ANALYSES;
import static fr.labri.harmony.core.config.ConfigProperties.DATABASE;
import static fr.labri.harmony.core.config.ConfigProperties.FOLDERS;
import static fr.labri.harmony.core.config.ConfigProperties.MANAGE_CREATE_SOURCES;
import static fr.labri.harmony.core.config.ConfigProperties.OUT;
import static fr.labri.harmony.core.config.ConfigProperties.TMP;
import static fr.labri.harmony.core.config.JsonUtils.getString;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fr.labri.harmony.core.config.model.AnalysisConfiguration;
import fr.labri.harmony.core.config.model.DatabaseConfiguration;
import fr.labri.harmony.core.config.model.SchedulerConfiguration;

/**
 * 
 * Reads a global configuration file and adds missing default values to the
 * config (e.g. number of threads, working folders)
 * 
 */
public class GlobalConfigReader {

	private JsonNode globalConfig;
	private ObjectMapper mapper;


	public GlobalConfigReader(String path) {
		mapper = new ObjectMapper();
		try {
			globalConfig = mapper.readTree(new File(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public DatabaseConfiguration getDatabaseConfiguration() {
		JsonNode dbNode = globalConfig.get(DATABASE);
		if (dbNode != null) {
			try {
				return mapper.readValue(dbNode.toString(), DatabaseConfiguration.class);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return new DatabaseConfiguration();
	}

	public List<AnalysisConfiguration> getAnalysisConfigurations() {
		ArrayList<AnalysisConfiguration> configs = new ArrayList<>();
		ArrayNode n = (ArrayNode) globalConfig.get(ANALYSES);
		for (JsonNode c : n) {
			try {
				configs.add(mapper.readValue(c.toString(), AnalysisConfiguration.class));
			} catch (IOException e) {
				System.out.println("analysis config error");
			}
		}
		return configs;

	}
	
	public SchedulerConfiguration getSchedulerConfiguration() {
		JsonNode n = globalConfig.get(MANAGE_CREATE_SOURCES);
		try {
			return mapper.readValue(n.toString(), SchedulerConfiguration.class);
		} catch (Exception e) {
		}
		return new SchedulerConfiguration();
	}


	public ObjectNode getFoldersConfig() {
		if (!globalConfig.has(FOLDERS)) return ConfigBuilder.getFoldersConfig(null, null);
		JsonNode n = globalConfig.get(FOLDERS);
		return ConfigBuilder.getFoldersConfig(getString(n.get(OUT)), getString(n.get(TMP)));
	}


}
