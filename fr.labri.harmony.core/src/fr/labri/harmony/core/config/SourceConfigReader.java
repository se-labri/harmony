package fr.labri.harmony.core.config;

import static fr.labri.harmony.core.config.ConfigProperties.DEFAULT;
import static fr.labri.harmony.core.config.ConfigProperties.SOURCES;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.labri.harmony.core.config.model.SourceConfiguration;

public class SourceConfigReader {

	private final static String DEFAULT_CONFIG_PATH = "configuration/fr.labri.harmony/default-source-config.json";


	private JsonNode sourceConfig;
	private GlobalConfigReader global;
	private ObjectMapper mapper;
	private String configurationFileName;

	public SourceConfigReader(String path, GlobalConfigReader global) throws IOException {
		if (path == null) path = DEFAULT_CONFIG_PATH;
		this.global = global;
		mapper = new ObjectMapper();
		File configFile = new File(path);
		configurationFileName = configFile.getName();
		sourceConfig = mapper.readTree(configFile);
	}

	public List<SourceConfiguration> getSourcesConfigurations() {
		ArrayList<SourceConfiguration> configs = new ArrayList<>();
		SourceConfiguration defaultConfig = getDefaultConfiguation();
		for (JsonNode c : sourceConfig.get(SOURCES)) {
			try {
				SourceConfiguration config = mapper.readValue(c.toString(), SourceConfiguration.class);
				config.addDefaultValues(defaultConfig);
				config.setFoldersConfiguration(global.getFoldersConfig());
				config.setConfigurationFileName(configurationFileName);
				configs.add(config);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return configs;
	}

	private SourceConfiguration getDefaultConfiguation() {
		JsonNode n = sourceConfig.get(DEFAULT);
		if (n != null) {
			try {
				return mapper.readValue(n.toString(), SourceConfiguration.class);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
}

}
