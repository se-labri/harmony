package fr.labri.harmony.core.analysis;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import fr.labri.harmony.core.config.model.AnalysisConfiguration;
import fr.labri.harmony.core.dao.AbstractDao;
import fr.labri.harmony.core.dao.Dao;
import fr.labri.harmony.core.log.HarmonyLogger;
import fr.labri.harmony.core.util.DeclarativeServicesUtils;

public class AnalysisFactory {

	static final String PROPERTY_DEPENDENCIES = "depends";
	static final String PROPERTY_PERSISTENCE_UNIT = "persistence-unit";

	private AbstractDao dao;

	public AnalysisFactory(AbstractDao dao) {
		this.dao = dao;
	}

	public ISingleSourceAnalysis createAnalysis(AnalysisConfiguration analysisConfig) {
		return createAnalysis(analysisConfig, ISingleSourceAnalysis.class);
	}
	
	public IMultipleSourcesAnalysis createPostProcessingAnalysis(AnalysisConfiguration analysisConfig) {
		return createAnalysis(analysisConfig, IMultipleSourcesAnalysis.class);
	} 
	
	private <T extends IAnalysis> T createAnalysis(AnalysisConfiguration analysisConfig, Class<T> clazz) {
		Collection<ServiceReference<T>> serviceReferences;
		try {
			BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
			serviceReferences = context.getServiceReferences(clazz, DeclarativeServicesUtils.getFilter(analysisConfig.getAnalysisName()));
			if (serviceReferences != null && !serviceReferences.isEmpty()) {

				ServiceReference<T> analysisReference = serviceReferences.iterator().next();

				if (serviceReferences.size() > 1) {
					HarmonyLogger.info("Multiple implementations of the analysis" + analysisConfig.getAnalysisName() + "have been found. The first one has been selected");
				}

				String dependencies = (String) analysisReference.getProperty(PROPERTY_DEPENDENCIES);
				if (dependencies != null) analysisConfig.setDependencies(Arrays.asList(dependencies.split(":")));

				analysisConfig.setPersistenceUnit((String) analysisReference.getProperty(PROPERTY_PERSISTENCE_UNIT));

				@SuppressWarnings("unchecked")
				T analysis = (T) context.getService(analysisReference).getClass().getConstructor(AnalysisConfiguration.class, Dao.class)
						.newInstance(analysisConfig, dao);

				return analysis;
			}

		} catch (NoSuchMethodException | InvalidSyntaxException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
			e.printStackTrace();
		}
		HarmonyLogger.error("The analysis '" + analysisConfig.getAnalysisName()
				+ "' could not be found. \n Either the associated bundle is not loaded or there is a typo in your configuration");
		return null;
	} 

}
