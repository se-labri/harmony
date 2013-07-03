package fr.labri.harmony.core.execution;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;

import fr.labri.harmony.core.analysis.Analysis;
import fr.labri.harmony.core.analysis.AnalysisFactory;
import fr.labri.harmony.core.analysis.PostProcessingAnalysis;
import fr.labri.harmony.core.config.GlobalConfigReader;
import fr.labri.harmony.core.config.SourceConfigReader;
import fr.labri.harmony.core.config.model.AnalysisConfiguration;
import fr.labri.harmony.core.config.model.SchedulerConfiguration;
import fr.labri.harmony.core.config.model.SourceConfiguration;
import fr.labri.harmony.core.dao.Dao;
import fr.labri.harmony.core.dao.DaoImpl;
import fr.labri.harmony.core.log.HarmonyLogger;
import fr.labri.harmony.core.model.Source;
import fr.labri.harmony.core.source.SourceExtractor;
import fr.labri.harmony.core.source.SourceExtractorFactory;

public class StudyScheduler {

	private static final int NUMBER_OF_EXECUTION_UNIT_AVAILABLE = Runtime.getRuntime().availableProcessors();

	private ExecutorService threadsPool;
	private SchedulerConfiguration schedulerConfiguration;
	private Dao dao;
	private int executionReportId;
	private ExecutionMonitor mainMonitor;

	public StudyScheduler(SchedulerConfiguration schedulerConfiguration) {
		this.schedulerConfiguration = schedulerConfiguration;
	}

	/**
	 * Main method of the core. Runs all analyses on all sources according to
	 * the configuration
	 * 
	 * @param global
	 * @param sourceConfigReader
	 */
	public void run(GlobalConfigReader global, SourceConfigReader sourceConfigReader) {

		// We create the directories specified in the folder configuration
		try {
			FileUtils.forceMkdir(new File(global.getFoldersConfig().getOutFolder()));
			FileUtils.forceMkdir(new File(global.getFoldersConfig().getTmpFolder()));
		} catch (IOException e) {
			HarmonyLogger.error("An error occured while creating working folders");
			e.printStackTrace();
		}
		// We retrieve the list of source that need to be analyzed
		List<SourceConfiguration> sourceConfigurations = sourceConfigReader.getSourcesConfigurations();
		if (sourceConfigurations.isEmpty()) {
			HarmonyLogger.error("No source to analyze");
			return;
		}

		// We create a global DAO which is in charge of building and
		// managing
		// the EntityManagers from all the bundles defining analyses
		dao = new DaoImpl(global.getDatabaseConfiguration());

		cleanExistingSources();
		
		// We grab the list of analyses which have been scheduled according
		// to
		// their dependencies
		Collection<Analysis> scheduledAnalyses = getScheduledAnalyses(global.getAnalysisConfigurations());

		// We do the same thing for the post-processing analyses
		Collection<PostProcessingAnalysis> scheduledPostProcessingAnalyses = getScheduledPostProcessingAnalyses(global.getPostProcessingAnalysisConfigurations());

		// Initialization of the ExecutorService in order to manage the
		// concurrent execution of the analyses
		if (this.schedulerConfiguration.getNumberOfThreads() > NUMBER_OF_EXECUTION_UNIT_AVAILABLE) {
			// TODO Use an OSGI compliant logging service / Some profiling
			// to
			// determine best ration number of threads per core
			HarmonyLogger.info("You requested more threads than the number of execution unit (core) available, this choice might lead to lower execution performance");
		}
		this.threadsPool = Executors.newFixedThreadPool(this.schedulerConfiguration.getNumberOfThreads());

		SourceExtractorFactory sourceExtractorFactory = new SourceExtractorFactory(dao);

		// Create the ExecutionReport
		mainMonitor = new ExecutionMonitor(dao);
		executionReportId = mainMonitor.initMonitoring();

		// We iterate on each sources and for each one we run the set of
		// analysis in the right order
		for (SourceConfiguration sourceConfiguration : sourceConfigurations) {
			
			SourceExtractor<?> currentSource = sourceExtractorFactory.createSourceExtractor(sourceConfiguration);
			if(currentSource!=null){
				launchSortedAnalysisOnSource(currentSource, scheduledAnalyses);
			}
			else {
				HarmonyLogger.error("Could not load the source:"+sourceConfiguration.getRepositoryURL());
			}
			
		}

		// We wait for the threads to finish to the extent that the timeout
		// limit is not reached
		shutdownThreadsPool();

		mainMonitor.stopMonitoring(executionReportId);
		mainMonitor.printExecutionReport(executionReportId);

		// We run the post-processing analyses
		Collection<Source> sources = getSources(sourceConfigurations);

		for (PostProcessingAnalysis analysis : scheduledPostProcessingAnalyses) {
			analysis.runOn(sources);
		}

	}

	private void cleanExistingSources() {
		dao.removeAllSources();

	}

	private Collection<Source> getSources(List<SourceConfiguration> sourceConfigurations) {
		ArrayList<Source> sources = new ArrayList<>();
		for (SourceConfiguration configuration : sourceConfigurations) {
			Source source = dao.getSourceByUrl(configuration.getRepositoryURL());
			if (source != null) {
				source.setConfig(configuration);
				sources.add(source);
			}
		}
		return sources;
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
			
			// We check that requested analysis was located with success
			if (currentAnalysis==null){
				HarmonyLogger.error("Harmony was not able to locate the analysis named:"+analysisConfiguration.getAnalysisName()+", hence this analysis will be ignored.");
				HarmonyLogger.error("In order to solve this problem check that:");
				HarmonyLogger.error("- the analysis name is correctly spelled in the configuration file");
				HarmonyLogger.error("- the analysis project is loaded. Take a look at your Run Configurations. Is the analysis bundle selected ? Are the dependencies of your bundle satisfied (see validate plugins) ?");
				
			}else{
				analysisDAG.addVertex(analysisConfiguration.getAnalysisName(), currentAnalysis);
				for (String requiredAnalysis : analysisConfiguration.getDependencies()) {
					// To have a Topological Sorting that returns the execution
					// order, the edges have to be oriented as
					// dependency -> dependent
					analysisDAG.addEdge(requiredAnalysis, analysisConfiguration.getAnalysisName());
				}
			}

		}

		return analysisDAG.getTopoOrder();

	}

	private Collection<PostProcessingAnalysis> getScheduledPostProcessingAnalyses(List<AnalysisConfiguration> analysisConfigurations) {
		AnalysisFactory factory = new AnalysisFactory(dao);
		Dag<PostProcessingAnalysis> analysisDAG = new Dag<PostProcessingAnalysis>();

		for (AnalysisConfiguration analysisConfiguration : analysisConfigurations) {

			PostProcessingAnalysis currentAnalysis = factory.createPostProcessingAnalysis(analysisConfiguration);
			if (currentAnalysis != null) {
				analysisDAG.addVertex(analysisConfiguration.getAnalysisName(), currentAnalysis);
				for (String requiredAnalysis : analysisConfiguration.getDependencies()) {
					// To have a Topological Sorting that returns the execution
					// order, the edges have to be oriented as
					// dependency -> dependent
					analysisDAG.addEdge(requiredAnalysis, analysisConfiguration.getAnalysisName());
				}
			}

		}

		return analysisDAG.getTopoOrder();
	}

	// TODO Initialization of sources should be done concurrently to the
	// launches of analyses, a thread must be dedicated to this task.
	private void launchSortedAnalysisOnSource(final SourceExtractor<?> sourceExtractor, final Collection<Analysis> scheduledAnalyses) {

		// We create a thread dedicated to this source. It will be in charge of
		// extracting it and launching the set of analyses on it
		threadsPool.execute(new Thread() {

			@Override
			public void run() {
				SourceExecutionReport executionReport = new SourceExecutionReport();
				executionReport.setSourceUrl(sourceExtractor.getConfig().getRepositoryURL());

				try {
					long startTime = System.currentTimeMillis();

					// Before launching any analysis on the source we must
					// extract it (clone
					// repository, build and store of the Harmony model)

					// If at least one analysis requires the actions, we have to
					// extract them
					boolean extractActions = false;
					boolean extractHarmonyModel = false;
					for (Analysis a : scheduledAnalyses) {
						if (a != null) {
							extractActions = (a.getConfig().requireActions()) || extractActions;
							extractHarmonyModel = (a.getConfig().requireHarmonyModel()) || extractHarmonyModel;
						}
					}
					sourceExtractor.initializeSource(extractHarmonyModel, extractActions);

					// We perform the analysis one after the other and between
					// each of them we check that an interruption
					// of the thread wasn't requested due to the timeout limit.
					for (Iterator<Analysis> analyses = scheduledAnalyses.iterator(); analyses.hasNext() && !this.isInterrupted();) {
						Analysis currentAnalysis = analyses.next();
						currentAnalysis.runOn(sourceExtractor.getSource());
					}
					long endTime = System.currentTimeMillis();
					executionReport.setExecutionTimeMillis(endTime - startTime);
					executionReport.setExecutedWithoutError(true);
				} catch (Exception e) {
					executionReport.setExecutedWithoutError(false);
					executionReport.setException(e);
					e.printStackTrace();
				} finally {
					ExecutionMonitor monitor = new ExecutionMonitor(dao);
					monitor.addSourceExecutionReport(executionReportId, executionReport);
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
			} else {
				HarmonyLogger.info("Finished execution of analyses");
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
