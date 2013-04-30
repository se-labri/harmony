package fr.labri.harmony.core.analysis;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import fr.labri.harmony.core.AbstractHarmonyService;
import fr.labri.harmony.core.config.model.AnalysisConfiguration;
import fr.labri.harmony.core.dao.Dao;

public class AnalysisFactory {

	static final String PROPERTY_DEPENDENCIES = "depends";
	static final String PROPERTY_PERSISTENCE_UNIT = "persistence-unit";
	
	private Dao dao;

	public AnalysisFactory(Dao dao) {
		this.dao = dao;
	}

	public Analysis createAnalysis(AnalysisConfiguration analysisConfig) {
		Collection<ServiceReference<Analysis>> serviceReferences;
		try {
			BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
			serviceReferences = context.getServiceReferences(Analysis.class, AbstractHarmonyService.getFilter(analysisConfig.getAnalysisName()));
			if (serviceReferences == null || serviceReferences.isEmpty()) return null;

			ServiceReference<Analysis> analysisReference = serviceReferences.iterator().next();
			
			if(serviceReferences.size()>1){
				// TODO LOG : Multiple implementations of the analysis +analysisConfig.getAnalysisName()+ have been found. The first found has been selected
			}
			
			String dependencies = (String) analysisReference.getProperty(PROPERTY_DEPENDENCIES);
			if (dependencies != null) analysisConfig.setDependencies(Arrays.asList(dependencies.split(":")));
			
			analysisConfig.setPersistenceUnit((String) analysisReference.getProperty(PROPERTY_PERSISTENCE_UNIT));


			Analysis analysis = context.getService(analysisReference).getClass().getConstructor(AnalysisConfiguration.class, Dao.class, Properties.class)
					.newInstance(analysisConfig, dao, extractProperties(analysisReference));
			
			return analysis;

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
