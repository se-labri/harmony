package fr.labri.harmony.core.source;

import java.util.Collection;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import fr.labri.harmony.core.config.model.SourceConfiguration;
import fr.labri.harmony.core.dao.DaoFactory;
import fr.labri.harmony.core.dao.ModelPersister;
import fr.labri.harmony.core.log.HarmonyLogger;
import fr.labri.harmony.core.util.DeclarativeServicesUtils;

public class SourceExtractorFactory {

	private DaoFactory daoFactory;

	public SourceExtractorFactory(DaoFactory daoFactory) {
		this.daoFactory = daoFactory;
	}

	@SuppressWarnings("rawtypes")
	public SourceExtractor<?> createSourceExtractor(SourceConfiguration config) {
		BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
		try {
			Collection<ServiceReference<SourceExtractor>> refs = context.getServiceReferences(SourceExtractor.class, DeclarativeServicesUtils.getFilter(config.getSourceExtractorName()));
			if (refs != null && !refs.isEmpty()) {
				ServiceReference<SourceExtractor> ref = refs.iterator().next();
				
				if(refs.size()>1){
					HarmonyLogger.info("Multiple implementations of the source extractor +config.getSourceExtractorName()+ have been found. The first one found has been selected");
				}
				
				SourceExtractor<?> serviceDef = context.getService(ref);
				SourceExtractor<?> service = serviceDef.getClass().getConstructor(SourceConfiguration.class, ModelPersister.class).newInstance(config, daoFactory.createModelPersister());

				return service;
			}else {
				HarmonyLogger.error("The source extractor: "+config.getSourceExtractorName()+" required for: "+config.getRepositoryURL()+" could not be found");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
}
