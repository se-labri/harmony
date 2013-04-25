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

		// We create a global DAO which is in charge of building and managing the EntityManagers
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

		// We retrieve the list of source that we will look at
		List<SourceConfiguration> sourceConfigurations = sources.getSourcesConfigurations();
		SourceExtractorFactory sourceExtractorFactory = new SourceExtractorFactory(dao);
		
		
		// We iterate on each sources and for each one we run the set of analysis in the right order
		for (SourceConfiguration sourceConfiguration : sourceConfigurations) {
			launchSortedAnalysisOnSource(sourceExtractorFactory.createSourceExtractor(sourceConfiguration), scheduledAnalyses);
		}
		
		// We wait for the threads to finish to the extent that the timeout limit is not reached
		shutdownThreadsPool();
		
	}

	public Collection<Analysis> getScheduledAnalyses() {
		BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();

		try {
			Collection<ServiceReference<Analysis>> refs = context.getServiceReferences(Analysis.class, null);
			for (ServiceReference<Analysis> ref : refs) {

				String depends = (String) ref.getProperty(Analysis.PROPERTY_DEPENDENCIES);

			}
		} catch (InvalidSyntaxException e) {
		}
		return null;

	}

	private void launchSortedAnalysisOnSource(final SourceExtractor<?> e, final Collection<Analysis> scheduledAnalyses) {
		
		// Before launching any analysis on the source we must extract it (clone, build of the Harmony model)
		//TODO

		// We create a thread dedicated to this source. It will be in charge
		// of launching the set of analyses on it
		threadsPool.execute(new Thread() {
			
			@Override
			public void run() {
				try {
					// We perform the analysis one after the other and between each of them we check that an interruption
					// of the thread wasn't requested due to the timeout limit.
					for (Iterator<Analysis> analyses = scheduledAnalyses.iterator(); analyses.hasNext()&&!this.isInterrupted();) {
						Analysis currentAnalysis = analyses.next();
						currentAnalysis.runOn(e.getSource());
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
			// Wait until the end of configuration timeout for existing tasks to terminate
			if (!threadsPool.awaitTermination(schedulerConfiguration.getGlobalTimeOut(), TimeUnit.MINUTES)) {
				
				LOGGER.severe("Execution timeout, the pool of analysis threads will be shutdown (You may check your configuration file for running longer analysis)");
				
				// Cancel currently executing tasks
				threadsPool.shutdownNow(); 
				
				// Wait a while for tasks to respond to being cancelled
				if (!threadsPool.awaitTermination(60, TimeUnit.SECONDS)){
					LOGGER.severe("Harmony was not able to shutdown the pool of threads in charge of running your analyses");
				}			
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			threadsPool.shutdownNow();
			
			// Preserve interrupt status
			// @see: http://www.ibm.com/developerworks/java/library/j-jtp05236/
			Thread.currentThread().interrupt();
		}

	}

}
