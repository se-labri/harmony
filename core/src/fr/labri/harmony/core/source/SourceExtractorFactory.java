package fr.labri.harmony.core.source;

import java.util.Collection;
import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import fr.labri.harmony.core.HarmonyManager;
import fr.labri.harmony.core.config.model.SourceConfiguration;
import fr.labri.harmony.core.dao.Dao;

public class SourceExtractorFactory {

	private Dao dao;
	public SourceExtractorFactory(Dao dao) {
		this.dao = dao;
	}
	
	public SourceExtractor<?> createSourceExtractor(SourceConfiguration config) {
		BundleContext context = FrameworkUtil.getBundle(HarmonyManager.class).getBundleContext();
		try {
			Collection<ServiceReference<SourceExtractor>> refs = context.getServiceReferences(SourceExtractor.class, HarmonyManager.getFilter(config.getSourceExtractorName()));
			if (refs != null && !refs.isEmpty()) {
				ServiceReference<SourceExtractor> ref = refs.iterator().next();
				Properties properties = extractProperties(ref);
				SourceExtractor serviceDef = context.getService(ref);
				SourceExtractor service = serviceDef.create(config, dao, properties);
				return service;
			}
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private Properties extractProperties(ServiceReference<?> ref) {
		Properties properties = new Properties();
		for (String key : ref.getPropertyKeys())
			properties.put(key, ref.getProperty(key));
		return properties;
	}
	
}
