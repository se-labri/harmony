package fr.labri.harmony.core.execution;

import java.util.Collection;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import fr.labri.harmony.core.Analysis;

public class AnalysisScheduler {
	
	public AnalysisScheduler() {
		// TODO Auto-generated constructor stub
	}
	
	public Collection<Analysis> getScheduledAnalyses() {
		BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();

		try {
			Collection<ServiceReference<Analysis>> refs = context.getServiceReferences(Analysis.class, null);
			for (ServiceReference<Analysis> ref : refs) {

				String depends = (String) ref.getProperty(Analysis.PROPERTY_DEPENDS);
			}
		} catch (InvalidSyntaxException e) {}
		return null;

	}
	
	private DependencyGraph createDependencyGraph(Collection<ServiceReference<Analysis>> refs) {
		// TODO
		return null;
		
	}

}
