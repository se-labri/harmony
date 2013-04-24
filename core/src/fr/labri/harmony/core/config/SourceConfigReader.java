package fr.labri.harmony.core.config;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SourceConfigReader {

	private ArrayNode sourceConfig;
	
	private GlobalConfigReader global;

	public SourceConfigReader(String path, GlobalConfigReader global) {
		this.global = global;
		ObjectMapper mapper = new ObjectMapper();
		try {
			sourceConfig = (ArrayNode) mapper.readTree(new File(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public ArrayNode getSourcesConfig() {
		for (JsonNode c : sourceConfig) ((ObjectNode) c).put(ConfigProperties.FOLDERS, global.getFoldersConfig());
		return sourceConfig;
	}

}
