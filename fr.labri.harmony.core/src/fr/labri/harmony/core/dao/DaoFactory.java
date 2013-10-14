package fr.labri.harmony.core.dao;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.jpa.EntityManagerFactoryBuilder;

import fr.labri.harmony.core.config.model.DatabaseConfiguration;

public class DaoFactory {
	
	/**
	 * We keep references on the EntityManagerFactories instead of the entity managers themselves
	 */
	private Map<String, HarmonyEntityManagerFactory> entityManagerFactories;

	public DaoFactory(DatabaseConfiguration config) {
		BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
		try {
			Collection<ServiceReference<EntityManagerFactoryBuilder>> refs = context.getServiceReferences(EntityManagerFactoryBuilder.class, null);
			entityManagerFactories = new HashMap<>();
			for (ServiceReference<EntityManagerFactoryBuilder> ref : refs) {
				String name = (String) ref.getProperty(EntityManagerFactoryBuilder.JPA_UNIT_NAME);
				HarmonyEntityManagerFactory factory = new HarmonyEntityManagerFactory(config, ref, context);

				entityManagerFactories.put(name, factory);

			}

		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}
	}
	
	public Dao createDao() {
		return new Dao(entityManagerFactories);
	}
	
	
	
}
