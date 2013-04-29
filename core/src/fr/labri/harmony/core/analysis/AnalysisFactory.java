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

	private Dao dao;

	public AnalysisFactory(Dao dao) {
		this.dao = dao;
	}

	public Analysis createAnalysis(AnalysisConfiguration config) {
		Collection<ServiceReference<Analysis>> serviceReferences;
		try {
			BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
			serviceReferences = context.getServiceReferences(Analysis.class, AbstractHarmonyService.getFilter(config.getAnalysisName()));
			if (serviceReferences == null || serviceReferences.isEmpty()) return null;

			ServiceReference<Analysis> analysisReference = serviceReferences.iterator().next();
			String dependencies = (String) analysisReference.getProperty(Analysis.PROPERTY_DEPENDENCIES);
			if (dependencies != null) config.setDependencies(Arrays.asList(dependencies.split(":")));


			Analysis analysis = context.getService(analysisReference).getClass().getConstructor(AnalysisConfiguration.class, Dao.class, Properties.class)
					.newInstance(config, dao, extractProperties(analysisReference));
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
