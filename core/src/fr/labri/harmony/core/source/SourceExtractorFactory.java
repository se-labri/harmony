package fr.labri.harmony.core.source;

import java.util.Collection;
import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import fr.labri.harmony.core.AbstractHarmonyService;
import fr.labri.harmony.core.config.model.SourceConfiguration;
import fr.labri.harmony.core.dao.Dao;

public class SourceExtractorFactory {

	private Dao dao;

	public SourceExtractorFactory(Dao dao) {
		this.dao = dao;
	}

	@SuppressWarnings("rawtypes")
	public SourceExtractor<?> createSourceExtractor(SourceConfiguration config) {
		BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
		try {
			Collection<ServiceReference<SourceExtractor>> refs = context.getServiceReferences(SourceExtractor.class, AbstractHarmonyService.getFilter(config.getSourceExtractorName()));
			if (refs != null && !refs.isEmpty()) {
				ServiceReference<SourceExtractor> ref = refs.iterator().next();
				
				if(refs.size()>1){
					// TODO LOG : Multiple implementations of the source extractor +config.getSourceExtractorName()+ have been found. The first one found has been selected
				}
				
				Properties properties = extractProperties(ref);
				SourceExtractor<?> serviceDef = context.getService(ref);
				SourceExtractor<?> service = serviceDef.getClass().getConstructor(SourceConfiguration.class, Dao.class, Properties.class).newInstance(config, dao, properties);

				return service;
			}
		} catch (Exception e) {
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
