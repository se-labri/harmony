package fr.labri.harmony.core;

import java.util.Properties;

import fr.labri.harmony.core.dao.Dao;

public abstract class AbstractHarmonyService implements HarmonyService {

	protected Dao dao;
		
	protected Properties properties;
	
	public AbstractHarmonyService() {
	}
	
	public AbstractHarmonyService(Dao dao, Properties properties) {
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
	
	public String getName() {
		return properties.getProperty(HarmonyService.PROPERTY_NAME);
	}
	
}
