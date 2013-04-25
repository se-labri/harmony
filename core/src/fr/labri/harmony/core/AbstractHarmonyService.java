package fr.labri.harmony.core;

import java.util.Properties;
import java.util.logging.Logger;

import fr.labri.harmony.core.dao.Dao;

public abstract class AbstractHarmonyService {

	public static final String PROPERTY_NAME = "component.name";
	protected static final Logger LOGGER = Logger.getLogger("fr.labri.harmony.core");

	protected Dao dao;
	

	/**
	 * Properties defined in the service's component.xml
	 */
	private Properties componentProperties;

	public AbstractHarmonyService() {
	}

	public AbstractHarmonyService(Dao dao, Properties properties) {
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

}
