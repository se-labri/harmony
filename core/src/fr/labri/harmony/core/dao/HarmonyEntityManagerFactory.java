package fr.labri.harmony.core.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.jpa.EntityManagerFactoryBuilder;

import fr.labri.harmony.core.config.model.DatabaseConfiguration;

public class HarmonyEntityManagerFactory {

	private DatabaseConfiguration config;
	private String databaseName;
	private String databaseUrl;
	private EntityManagerFactory factory;

	public HarmonyEntityManagerFactory(DatabaseConfiguration configuration, ServiceReference<EntityManagerFactoryBuilder> ref, BundleContext context) {
		// We will modify the config according to the database name, so we have
		// to make a copy of it
		this.config = new DatabaseConfiguration(configuration);
		
		// We save the original Url
		databaseUrl = configuration.getUrl();
		
		
		// and modify our url according to the service name
		databaseName = (String) ref.getProperty(EntityManagerFactoryBuilder.JPA_UNIT_NAME);
		this.config.setUrl(databaseUrl + databaseName);

		EntityManagerFactoryBuilder b = context.getService(ref);
		factory = b.createEntityManagerFactory(this.config.getProperties());
	}

	public EntityManager createEntityManager() {

		EntityManager em = null;
		try {
			// We try to create the EM, an exception will be thrown and caught
			// if the database does not exist
			em = factory.createEntityManager();
		} catch (PersistenceException e) {
			createDatabase();
			em = factory.createEntityManager();
		}

		return em;

	}

	private void createDatabase() {
		try {
			Connection conn = DriverManager.getConnection(databaseUrl + "?user=" + config.getUser() + "&password=" + config.getPassword());
			Statement s = conn.createStatement();
			s.executeUpdate("CREATE DATABASE " + databaseName);
		} catch (SQLException e) {
			Dao.LOGGER.severe(e.getMessage());
			e.printStackTrace();
		}
		
	}

}
