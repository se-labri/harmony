package fr.labri.harmony.core.analysis;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import fr.labri.harmony.core.AbstractHarmonyService;
import fr.labri.harmony.core.config.model.AnalysisConfiguration;
import fr.labri.harmony.core.dao.Dao;
import fr.labri.harmony.core.log.HarmonyLogger;

public class AnalysisFactory {

	static final String PROPERTY_DEPENDENCIES = "depends";
	static final String PROPERTY_PERSISTENCE_UNIT = "persistence-unit";

	private Dao dao;

	public AnalysisFactory(Dao dao) {
		this.dao = dao;
	}

	public Analysis createAnalysis(AnalysisConfiguration analysisConfig) {
		return createAnalysis(analysisConfig, Analysis.class);
	}
	
	public PostProcessingAnalysis createPostProcessingAnalysis(AnalysisConfiguration analysisConfig) {
		return createAnalysis(analysisConfig, PostProcessingAnalysis.class);
	} 
	
	private <T extends HasAnalysisConfiguration> T createAnalysis(AnalysisConfiguration analysisConfig, Class<T> clazz) {
		Collection<ServiceReference<T>> serviceReferences;
		try {
			BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
			serviceReferences = context.getServiceReferences(clazz, AbstractHarmonyService.getFilter(analysisConfig.getAnalysisName()));
			if (serviceReferences != null && !serviceReferences.isEmpty()) {

				ServiceReference<T> analysisReference = serviceReferences.iterator().next();

				if (serviceReferences.size() > 1) {
					HarmonyLogger.info("Multiple implementations of the analysis" + analysisConfig.getAnalysisName() + "have been found. The first one has been selected");
				}

				String dependencies = (String) analysisReference.getProperty(PROPERTY_DEPENDENCIES);
				if (dependencies != null) analysisConfig.setDependencies(Arrays.asList(dependencies.split(":")));

				analysisConfig.setPersistenceUnit((String) analysisReference.getProperty(PROPERTY_PERSISTENCE_UNIT));

				@SuppressWarnings("unchecked")
				T analysis = (T) context.getService(analysisReference).getClass().getConstructor(AnalysisConfiguration.class, Dao.class, Properties.class)
						.newInstance(analysisConfig, dao, extractProperties(analysisReference));

				return analysis;
			}

		} catch (NoSuchMethodException | InvalidSyntaxException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
			e.printStackTrace();
		}
		HarmonyLogger.error("The analysis '" + analysisConfig.getAnalysisName()
				+ "' could not be found. \n Either the associated bundle is not loaded or there is a typo in your configuration");
		return null;
	} 

	private Properties extractProperties(ServiceReference<?> ref) {
		Properties properties = new Properties();
		for (String key : ref.getPropertyKeys())
			properties.put(key, ref.getProperty(key));
		return properties;
	}
}
