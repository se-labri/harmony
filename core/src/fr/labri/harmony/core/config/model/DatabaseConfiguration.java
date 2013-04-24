package fr.labri.harmony.core.config.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.persistence.config.PersistenceUnitProperties;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DatabaseConfiguration {
	
	private static final String DEFAULT_URL = "jdbc:h2:tmp";
	private static final String DEFAULT_USER = "SA";
	private static final String DEFAULT_PASSWORD = "";
	private static final String DEFAULT_DRIVER = "org.h2.Driver";


	private HashMap<String, String> properties;

	public DatabaseConfiguration() {
		super();
		properties = new HashMap<>();
		setUrl(DEFAULT_URL);
		setDriver(DEFAULT_DRIVER);
		setUser(DEFAULT_USER);
		setPassword(DEFAULT_PASSWORD);
		
	}

	public DatabaseConfiguration(String url, String driver, String user) {
		this();
		setUrl(url);
		setUser(user);
		setDriver(driver);
	}

	public DatabaseConfiguration(String url, String driver, String user, String password) {
		this(url, driver, user);
		setPassword(password);
	}
	
	/**
	 * Copy constructor.
	 */
	public DatabaseConfiguration(DatabaseConfiguration other) {
		this(other.getUrl(), other.getDriver(), other.getUser(), other.getPassword());
	}

	public String getUrl() {
		return properties.get(PersistenceUnitProperties.JDBC_URL);
	}

	@JsonProperty("url")
	public void setUrl(String url) {
		properties.put(PersistenceUnitProperties.JDBC_URL, url);
	}

	public String getUser() {
		return properties.get(PersistenceUnitProperties.JDBC_USER);
	}

	@JsonProperty("user")
	public void setUser(String user) {
		properties.put(PersistenceUnitProperties.JDBC_USER, user);
	}

	public String getPassword() {
		return properties.get(PersistenceUnitProperties.JDBC_PASSWORD);
	}

	@JsonProperty("password")
	public void setPassword(String password) {
		properties.put(PersistenceUnitProperties.JDBC_PASSWORD, password);
	}

	public String getDriver() {
		return properties.get(PersistenceUnitProperties.JDBC_DRIVER);
	}

	@JsonProperty("driver")
	public void setDriver(String driver) {
		properties.put(PersistenceUnitProperties.JDBC_DRIVER, driver);
	}

	@JsonIgnore
	public Map<String, String> getProperties() {
		return properties;
	}


}
