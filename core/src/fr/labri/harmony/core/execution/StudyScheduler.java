package fr.labri.harmony.core.execution;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import fr.labri.harmony.core.Analysis;
import fr.labri.harmony.core.config.GlobalConfigReader;
import fr.labri.harmony.core.config.SourceConfigReader;
import fr.labri.harmony.core.config.model.SchedulerConfiguration;
import fr.labri.harmony.core.config.model.SourceConfiguration;
import fr.labri.harmony.core.dao.Dao;
import fr.labri.harmony.core.dao.DaoImpl;
import fr.labri.harmony.core.source.SourceExtractor;
import fr.labri.harmony.core.source.SourceExtractorFactory;

public class StudyScheduler {

	private final static Logger LOGGER = Logger.getLogger("fr.labri.harmony.scheduler");
	private static final int NUMBER_OF_EXECUTION_UNIT_AVAILABLE = Runtime.getRuntime().availableProcessors();

	private ExecutorService threadsPool;
	private SchedulerConfiguration schedulerConfiguration;

	public StudyScheduler(SchedulerConfiguration schedulerConfiguration) {
		this.schedulerConfiguration = schedulerConfiguration;
	}

	public void run(GlobalConfigReader global, SourceConfigReader sources) {

		// We create a global DAO which is in charge of building and managing
		// the EntityManagers
		// from all the bundles defining analyses
		Dao dao = new DaoImpl(global.getDatabaseConfiguration());

		// We grab the list of analyses which have been scheduled according to
		// their dependencies
		Collection<Analysis> scheduledAnalyses = getScheduledAnalyses();

		// Initialization of the ExecutorService in order to manage the
		// concurrent execution of the analyses
		if (this.schedulerConfiguration.getNumberOfThreads() > NUMBER_OF_EXECUTION_UNIT_AVAILABLE) {
			// TODO Use an OSGI compliant logging service / Some profiling to
			// determine best ration number of threads per core
			LOGGER.info("You requested more threads than the number of execution unit (core) available, this choice might lead to lower execution performance");
		}
		this.threadsPool = Executors.newFixedThreadPool(this.schedulerConfiguration.getNumberOfThreads());

		List<SourceConfiguration> configurations = sources.getSourcesConfigurations();
		SourceExtractorFactory factory = new SourceExtractorFactory(dao);
		// We iterate on each sources and for each one we run the set of
		// analysis in the right order
		for (SourceConfiguration config : configurations) {
			launchSortedAnalysisOnSource(factory.createSourceExtractor(config), scheduledAnalyses);
		}
	}

	public Collection<Analysis> getScheduledAnalyses() {
		BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();

		try {
			Collection<ServiceReference<Analysis>> refs = context.getServiceReferences(Analysis.class, null);
			for (ServiceReference<Analysis> ref : refs) {

				String depends = (String) ref.getProperty(Analysis.PROPERTY_DEPENDS);
				
			}
		} catch (InvalidSyntaxException e) {
		}
		return null;

	}

	private void launchSortedAnalysisOnSource(final SourceExtractor<?> e, final Collection<Analysis> scheduledAnalyses) {

		// We create a thread dedicated to this source which will be in charge
		// of launching the set of analyses on it
		threadsPool.execute(new Thread() {
			@Override
			public void run() {
				try {
					for (Iterator<Analysis> analyses = scheduledAnalyses.iterator(); analyses.hasNext();) {
						Analysis currentAnalysis = analyses.next();
						currentAnalysis.run(e.getSource());
					}

				} catch (Exception e) {
					LOGGER.severe(e.getMessage());
					e.printStackTrace();
				}
			}
		});

	}

	private void shutdownThreadsPool() {
		// Disable new tasks from being submitted
		threadsPool.shutdown();

		try {
			// Wait a while for existing tasks to terminate
			if (!threadsPool.awaitTermination(schedulerConfiguration.getGlobalTimeOut(), TimeUnit.MINUTES)) {
				threadsPool.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!threadsPool.awaitTermination(schedulerConfiguration.getGlobalTimeOut(), TimeUnit.MINUTES)) System.err.println("Pool did not terminate");
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			threadsPool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}

	}

}
