package fr.labri.harmony.core;

import java.util.Properties;

import com.fasterxml.jackson.databind.node.ObjectNode;

import fr.labri.harmony.core.dao.Dao;

public abstract class AbstractHarmonyService implements HarmonyService {

	protected Dao dao;
	
	protected ObjectNode config;
	
	protected Properties properties;
	
	public AbstractHarmonyService() {
	}
	
	public AbstractHarmonyService(ObjectNode config, Dao dao, Properties properties) {
		this.config = config;
		this.dao = dao;
		this.properties = properties;
	}
	
	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public Dao getDao() {
		return dao;
	}

	public void setDao(Dao dao) {
		this.dao = dao;
	}

	public ObjectNode getConfig() {
		return config;
	}

	public void setConfig(ObjectNode config) {
		this.config = config;
	}
	
	public String getName() {
		return properties.getProperty(HarmonyService.PROPERTY_NAME);
	}
	
}
