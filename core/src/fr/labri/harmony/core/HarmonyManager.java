package fr.labri.harmony.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fr.labri.harmony.core.config.ConfigProperties;
import fr.labri.harmony.core.config.GlobalConfigReader;
import fr.labri.harmony.core.config.SourceConfigReader;
import fr.labri.harmony.core.config.model.SourceConfiguration;
import fr.labri.harmony.core.dao.Dao;
import fr.labri.harmony.core.source.SourceExtractor;

public class HarmonyManager {

	public static List<SourceExtractor<?>> createSourceExtractors(SourceConfigReader r, Dao dao) {
		List<SourceExtractor<?>> extractors = new ArrayList<>();
		for (SourceConfiguration config : r.getSourcesConfigurations()) {
			config.setSourceExtractor(getSourceExtractor(config.getSourceExtractorName(), config, dao));
		}
		for (int i = 0; i < configs.size(); i++) {
			ObjectNode config = configs.get(i);
			String name = config.get(ConfigProperties.CLASS).asText();
			extractors.add(getSourceExtractor(name, config, dao));
		}
		return extractors;
	}

	public static List<Analysis> createAnalyses(GlobalConfigReader r, Dao dao) {
		List<Analysis> analyses = new ArrayList<>();
		ArrayNode configs = r.getAnalysesConfig();
		for (int i = 0; i < configs.size(); i++) {
			ObjectNode config = (ObjectNode) configs.get(i);
			String name = config.get(ConfigProperties.CLASS).asText();
			analyses.add(getAnalysis(name, config, dao));
		}
		return analyses;
	}

	@SuppressWarnings("rawtypes")
	public static SourceExtractor<?> getSourceExtractor(String name, SourceConfiguration config, Dao dao) {
		BundleContext context = FrameworkUtil.getBundle(HarmonyManager.class).getBundleContext();
		try {
			Collection<ServiceReference<SourceExtractor>> refs = context.getServiceReferences(SourceExtractor.class, getFilter(name));
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
		return null;	}

	@SuppressWarnings("rawtypes")
	public static Collection<SourceExtractor> getSourceExtractors(ObjectNode config, Dao dao) {
		return createHarmonyServices(SourceExtractor.class, config, dao);
	}

	public static Analysis getAnalysis(String name, ObjectNode config, Dao dao) {
		return createHarmonyService(Analysis.class, name, config, dao);
	}

	public static Collection<Analysis> getAnalyses(ObjectNode config, Dao dao) {
		return createHarmonyServices(Analysis.class, config, dao);
	}

	public static <S extends HarmonyService> Collection<S> createHarmonyServices(Class<S> clazz, ObjectNode config, Dao dao) {
		BundleContext context = FrameworkUtil.getBundle(HarmonyManager.class).getBundleContext();
		try {
			Collection<ServiceReference<S>> refs = context.getServiceReferences(clazz, null);
			List<S> services = new ArrayList<>();
			for (ServiceReference<S> ref : refs) {
				S service = context.getService(ref).create(config, dao, extractProperties(ref));
				services.add(service);
			}
			return services;
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}

	public static <S extends HarmonyService> S createHarmonyService(Class<S> clazz, String name, ObjectNode config, Dao dao) {
		BundleContext context = FrameworkUtil.getBundle(HarmonyManager.class).getBundleContext();
		try {
			Collection<ServiceReference<S>> refs = context.getServiceReferences(clazz, getFilter(name));
			if (refs != null && !refs.isEmpty()) {
				ServiceReference<S> ref = refs.iterator().next();
				Properties properties = extractProperties(ref);
				S serviceDef = context.getService(ref);
				S service = serviceDef.create(config, dao, properties);
				return service;
			}
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static Properties extractProperties(ServiceReference<?> ref) {
		Properties properties = new Properties();
		for (String key : ref.getPropertyKeys())
			properties.put(key, ref.getProperty(key));
		return properties;
	}

	private static String getFilter(String name) {
		return "(" + HarmonyService.PROPERTY_NAME + "=" + name + ")";
	}

}
