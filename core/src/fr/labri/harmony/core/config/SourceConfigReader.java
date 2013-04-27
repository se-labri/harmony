package fr.labri.harmony.core.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import fr.labri.harmony.core.config.model.SourceConfiguration;

public class SourceConfigReader {

	private ArrayNode sourceConfig;
	private GlobalConfigReader global;
	private ObjectMapper mapper;

	public SourceConfigReader(String path, GlobalConfigReader global) {
		this.global = global;
		mapper = new ObjectMapper();
		try {
			sourceConfig = (ArrayNode) mapper.readTree(new File(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<SourceConfiguration> getSourcesConfigurations() {
		ArrayList<SourceConfiguration> configs = new ArrayList<>();
		for (JsonNode c : sourceConfig) {
			try {
				SourceConfiguration config = mapper.readValue(c.toString(), SourceConfiguration.class);
				config.setFoldersConfiguration(global.getFoldersConfig());
				configs.add(config);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return configs;
	}

}
