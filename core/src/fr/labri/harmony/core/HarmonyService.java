package fr.labri.harmony.core;

import java.util.Properties;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.node.ObjectNode;

import fr.labri.harmony.core.dao.Dao;


public interface HarmonyService {
	
	static final String PROPERTY_NAME = "component.name";
	
	static final Logger LOGGER = Logger.getLogger("fr.labri.harmony.core");
	
	ObjectNode getConfig();
	
	Properties getProperties();
	
	String getName();
	
	<S extends HarmonyService> S create(ObjectNode config, Dao dao, Properties properties);

}
