package fr.labri.harmony.core;

import java.util.Properties;
import java.util.logging.Logger;


public interface HarmonyService {
	
	static final String PROPERTY_NAME = "component.name";
	
	static final Logger LOGGER = Logger.getLogger("fr.labri.harmony.core");
		
	Properties getProperties();
	
	String getName();
	
}
