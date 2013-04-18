package fr.labri.harmony.core.config;

import static fr.labri.harmony.core.config.ConfigProperties.*;

import java.io.File;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fr.labri.utils.file.FileUtils;

public class ConfigBuilder {

	public static ObjectNode getDatabaseConfig(String url, String user, String password, String driver) {
		ObjectNode n = JsonNodeFactory.instance.objectNode();
		n.put(DATABASE_URL, (url != null) ? url : "jdbc:h2:tmp");
		n.put(DATABASE_USER, (user != null) ? user : "SA");
		n.put(DATABASE_PASSWORD, (password != null) ? password : "");
		n.put(DATABASE_DRIVER, (driver != null) ? driver : "org.h2.Driver");
		return n;
	}

	public static ObjectNode getManageCreateSourcesConfig(Integer numThreads, Integer timeout) {
		ObjectNode n = JsonNodeFactory.instance.objectNode();
		n.put(NUM_THREADS, (numThreads != null) ? numThreads : DEFAULT_NUM_THREADS);
		n.put(TIMEOUT, (timeout != null) ? timeout : DEFAULT_TIMEOUT);
		return n;
	}

	public static ArrayNode getClasspathConfig(String[] classpath) {
		ArrayNode n = JsonNodeFactory.instance.arrayNode();
		for (String uri : classpath) n.add(uri);
		return n;
	}

	public static ObjectNode getAnalysisConfig(String clazz, ObjectNode optionsNode) {
		ObjectNode n = JsonNodeFactory.instance.objectNode();
		n.put(CLASS, clazz);
		n.put(FOLDERS, getFoldersConfig(null, null));
		if (optionsNode == null) optionsNode = JsonNodeFactory.instance.objectNode();
		n.put(OPTIONS, optionsNode);
		return n;
	}
	
	public static ObjectNode getFoldersConfig(String out, String tmp) {
		ObjectNode foldersNode = JsonNodeFactory.instance.objectNode();	
		if (out == null) {
			File f = new File(DEFAULT_OUT_FOLDER);
			if (!f.exists()) f.mkdir();
			foldersNode.put(OUT, f.getAbsolutePath());
		} else foldersNode.put(OUT, out);
		foldersNode.put(TMP, (tmp == null) ? FileUtils.createTmpFolder(DEFAULT_TMP_FOLDER, "") : tmp);	
		return foldersNode;
	}

}
