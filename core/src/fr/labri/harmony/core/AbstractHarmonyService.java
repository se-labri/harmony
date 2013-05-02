package fr.labri.harmony.core;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.labri.harmony.core.dao.Dao;

public abstract class AbstractHarmonyService {

	public static final String PROPERTY_NAME = "component.name";
	protected static Logger LOGGER;

	protected Dao dao;
	
	/**
	 * Properties defined in the service's component.xml
	 */
	private Properties componentProperties;

	public AbstractHarmonyService() {
		LOGGER = LoggerFactory.getLogger(getClass());;
	}

	public AbstractHarmonyService(Dao dao, Properties properties) {
		this();
		this.dao = dao;
		this.componentProperties = properties;
	}

	public Properties getComponentProperties() {
		return componentProperties;
	}

	public void setProperties(Properties properties) {
		this.componentProperties = properties;
	}

	public Dao getDao() {
		return dao;
	}

	public void setDao(Dao dao) {
		this.dao = dao;
	}

	public String getName() {
		return componentProperties.getProperty(PROPERTY_NAME);
	}

	public String getComponentProperty(String key) {
		return componentProperties.getProperty(key);

	}
	
	public static String getFilter(String name) {
		return "(" + PROPERTY_NAME + "=" + name + ")";
	}


}
