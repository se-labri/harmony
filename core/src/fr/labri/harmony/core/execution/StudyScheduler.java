package fr.labri.harmony.core.execution;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import fr.labri.harmony.core.analysis.Analysis;
import fr.labri.harmony.core.analysis.AnalysisFactory;
import fr.labri.harmony.core.config.GlobalConfigReader;
import fr.labri.harmony.core.config.SourceConfigReader;
import fr.labri.harmony.core.config.model.AnalysisConfiguration;
import fr.labri.harmony.core.config.model.SchedulerConfiguration;
import fr.labri.harmony.core.config.model.SourceConfiguration;
import fr.labri.harmony.core.dao.Dao;
import fr.labri.harmony.core.dao.DaoImpl;
import fr.labri.harmony.core.log.HarmonyLogger;
import fr.labri.harmony.core.source.SourceExtractor;
import fr.labri.harmony.core.source.SourceExtractorFactory;

public class StudyScheduler {

	private static final int NUMBER_OF_EXECUTION_UNIT_AVAILABLE = Runtime.getRuntime().availableProcessors();

	private ExecutorService threadsPool;
	private SchedulerConfiguration schedulerConfiguration;
	private Dao dao;

	public StudyScheduler(SchedulerConfiguration schedulerConfiguration) {
		this.schedulerConfiguration = schedulerConfiguration;
	}

	// TODO instrumentation for performance assessment as well as report
	// creation (analyses failed/done)
	/**
	 * Main method of the core. Runs all analyses on all sources according to
	 * the configuration
	 * 
	 * @param global
	 * @param sources
	 */
	public void run(GlobalConfigReader global, SourceConfigReader sources) {

		// We create a global DAO which is in charge of building and managing
		// the EntityManagers
		// from all the bundles defining analyses
		dao = new DaoImpl(global.getDatabaseConfiguration());

		// We grab the list of analyses which have been scheduled according to
		// their dependencies
		Collection<Analysis> scheduledAnalyses = getScheduledAnalyses(global.getAnalysisConfigurations());

		// Initialization of the ExecutorService in order to manage the
		// concurrent execution of the analyses
		if (this.schedulerConfiguration.getNumberOfThreads() > NUMBER_OF_EXECUTION_UNIT_AVAILABLE) {
			// TODO Use an OSGI compliant logging service / Some profiling to
			// determine best ration number of threads per core
			HarmonyLogger.info("You requested more threads than the number of execution unit (core) available, this choice might lead to lower execution performance");
		}
		this.threadsPool = Executors.newFixedThreadPool(this.schedulerConfiguration.getNumberOfThreads());

		// We retrieve the list of source that need to be analyzed
		List<SourceConfiguration> sourceConfigurations = sources.getSourcesConfigurations();
		SourceExtractorFactory sourceExtractorFactory = new SourceExtractorFactory(dao);

		// We iterate on each sources and for each one we run the set of
		// analysis in the right order
		for (SourceConfiguration sourceConfiguration : sourceConfigurations) {
			launchSortedAnalysisOnSource(sourceExtractorFactory.createSourceExtractor(sourceConfiguration), scheduledAnalyses);
		}

		// We wait for the threads to finish to the extent that the timeout
		// limit is not reached
		shutdownThreadsPool();

	}

	/**
	 * 
	 * @param analysisConfigurations
	 * @return A list of {@link Analysis} which is order according to the
	 *         execution order
	 */
	private List<Analysis> getScheduledAnalyses(List<AnalysisConfiguration> analysisConfigurations) {

		AnalysisFactory factory = new AnalysisFactory(dao);
		Dag<Analysis> analysisDAG = new Dag<Analysis>();

		for (AnalysisConfiguration analysisConfiguration : analysisConfigurations) {

			Analysis currentAnalysis = factory.createAnalysis(analysisConfiguration);

			analysisDAG.addVertex(analysisConfiguration.getAnalysisName(), currentAnalysis);
			for (String requiredAnalysis : analysisConfiguration.getDependencies()) {
				// To have a Topological Sorting that returns the execution
				// order, the edges have to be oriented as
				// dependency -> dependent
				analysisDAG.addEdge(requiredAnalysis, analysisConfiguration.getAnalysisName());
			}

		}

		return analysisDAG.getTopoOrder();

	}

	// TODO Initialization of sources should be done concurrently to the
	// launches of analyses, a thread must me dedicated to this task.
	private void launchSortedAnalysisOnSource(final SourceExtractor<?> sourceExtractor, final Collection<Analysis> scheduledAnalyses) {

		// Before launching any analysis on the source we must extract it (clone
		// repository, build and store of the Harmony model)
		
		// If at least one analysis requires the actions, we have to extract them
		boolean extractActions = false;
		for (Analysis a : scheduledAnalyses) {
			extractActions = (a.getConfig().requireActions()) || extractActions;
		}
		
		sourceExtractor.initializeSource(extractActions);

		// We create a thread dedicated to this source. It will be in charge of
		// launching the set of analyses on it
		threadsPool.execute(new Thread() {

			@Override
			public void run() {
				try {
					// We perform the analysis one after the other and between
					// each of them we check that an interruption
					// of the thread wasn't requested due to the timeout limit.
					for (Iterator<Analysis> analyses = scheduledAnalyses.iterator(); analyses.hasNext() && !this.isInterrupted();) {
						Analysis currentAnalysis = analyses.next();
						currentAnalysis.runOn(sourceExtractor.getSource());
					}

				} catch (Exception e) {
					HarmonyLogger.error(e.getMessage());
					e.printStackTrace();
				}
			}
		});

	}

	private void shutdownThreadsPool() {

		// Disable new tasks from being submitted
		threadsPool.shutdown();

		try {
			// Wait until the end of configuration timeout for existing tasks to
			// terminate
			if (!threadsPool.awaitTermination(schedulerConfiguration.getGlobalTimeOut(), TimeUnit.MINUTES)) {

				HarmonyLogger.error("Execution timeout, the pool of analysis threads will be shutdown (You may check your configuration file for running longer analysis)");

				// Cancel currently executing tasks
				threadsPool.shutdownNow();

				// Wait a while for tasks to respond to being cancelled
				if (!threadsPool.awaitTermination(60, TimeUnit.SECONDS)) {
					HarmonyLogger.error("Harmony was not able to shutdown the pool of threads in charge of running your analyses");
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
