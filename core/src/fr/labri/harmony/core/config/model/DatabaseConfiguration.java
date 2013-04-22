package fr.labri.harmony.core.config.model;

import java.util.HashMap;

import org.eclipse.persistence.config.PersistenceUnitProperties;

public class DatabaseConfiguration extends HashMap<String, String> {

	private static final long serialVersionUID = 6532030853123974291L;

	public DatabaseConfiguration(String url, String driver, String login) {
		super();
		this.put(PersistenceUnitProperties.JDBC_URL, url);
		this.put(PersistenceUnitProperties.JDBC_USER, login);
		this.put(PersistenceUnitProperties.JDBC_DRIVER, driver);
	}

	public DatabaseConfiguration(String url, String driver, String login, String password) {
		this(url, driver, login);
		this.put(PersistenceUnitProperties.JDBC_PASSWORD, password);
	}

	public DatabaseConfiguration() {
	};

	public String getUrl() {
		return this.get(PersistenceUnitProperties.JDBC_URL);
	}

	public void setUrl(String url) {
		this.put(PersistenceUnitProperties.JDBC_URL, url);
	}

	public String getUser() {
		return this.get(PersistenceUnitProperties.JDBC_USER);
	}

	public void setUser(String user) {
		this.put(PersistenceUnitProperties.JDBC_USER, user);
	}

	public String getPassword() {
		return this.get(PersistenceUnitProperties.JDBC_PASSWORD);
	}

	public void setPassword(String password) {
		this.put(PersistenceUnitProperties.JDBC_PASSWORD, password);
	}

	public String getDriver() {
		return this.get(PersistenceUnitProperties.JDBC_DRIVER);
	}

	public void setDriver(String driver) {
		this.put(PersistenceUnitProperties.JDBC_DRIVER, driver);
	}

}
