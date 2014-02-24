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

import fr.labri.harmony.core.analysis.ISingleSourceAnalysis;
import fr.labri.harmony.core.analysis.AnalysisFactory;
import fr.labri.harmony.core.analysis.IMultipleSourceAnalysis;
import fr.labri.harmony.core.config.GlobalConfigReader;
import fr.labri.harmony.core.config.SourceConfigReader;
import fr.labri.harmony.core.config.model.AnalysisConfiguration;
import fr.labri.harmony.core.config.model.SchedulerConfiguration;
import fr.labri.harmony.core.config.model.SourceConfiguration;
import fr.labri.harmony.core.dao.Dao;
import fr.labri.harmony.core.dao.DaoFactory;
import fr.labri.harmony.core.log.HarmonyLogger;
import fr.labri.harmony.core.model.Source;
import fr.labri.harmony.core.source.SourceExtractor;
import fr.labri.harmony.core.source.SourceExtractorFactory;

public class StudyScheduler {

	private static final int NUMBER_OF_EXECUTION_UNIT_AVAILABLE = Runtime.getRuntime().availableProcessors();

	private ExecutorService threadsPool;
	private SchedulerConfiguration schedulerConfiguration;
	private DaoFactory daoFactory;
	private int executionReportId;
	private ExecutionMonitor mainMonitor;

	public StudyScheduler(SchedulerConfiguration schedulerConfiguration) {
		this.schedulerConfiguration = schedulerConfiguration;
	}

	/**
	 * Main method of the core. Runs all analyses on all sources according to the configuration
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
		
		daoFactory = new DaoFactory(global.getDatabaseConfiguration());

		// Initialization of the ExecutorService in order to manage the concurrent execution of the analyses
		if (this.schedulerConfiguration.getNumberOfThreads() > NUMBER_OF_EXECUTION_UNIT_AVAILABLE) {
			// TODO Use an OSGI compliant logging service / Some profiling to determine best ratio number of threads per core
			HarmonyLogger
					.info("You requested more threads than the number of execution unit (core) available, this choice might lead to lower execution performance");
		}
		this.threadsPool = Executors.newFixedThreadPool(this.schedulerConfiguration.getNumberOfThreads());

		SourceExtractorFactory sourceExtractorFactory = new SourceExtractorFactory(daoFactory);

		// Create the ExecutionReport
		mainMonitor = new ExecutionMonitor(daoFactory.createDao());
		executionReportId = mainMonitor.initMonitoring();

		List<AnalysisConfiguration> analysisConfigurations = global.getAnalysisConfigurations();
		// We iterate on each sources and for each one we run the set of analysis
		for (SourceConfiguration sourceConfiguration : sourceConfigurations) {
			launchSortedAnalysisOnSource(sourceExtractorFactory, sourceConfiguration, analysisConfigurations);
		}

		// We wait for the threads to finish to the extent that the timeout limit is not reached
		shutdownThreadsPool();

		mainMonitor.stopMonitoring(executionReportId);
		mainMonitor.printExecutionReport(executionReportId);

		// We run the post-processing analyses
		Collection<Source> sources = getSources(sourceConfigurations, daoFactory.createDao());

		List<AnalysisConfiguration> postProcessingAnalysisConfigurations = global.getPostProcessingAnalysisConfigurations();
		AnalysisFactory analysisFactory = new AnalysisFactory(daoFactory.createDao());
		for (AnalysisConfiguration analysisConfiguration : postProcessingAnalysisConfigurations) {
			IMultipleSourceAnalysis postProcessingAnalysis = analysisFactory.createPostProcessingAnalysis(analysisConfiguration);
			postProcessingAnalysis.runOn(sources);
		}

	}

	private Collection<Source> getSources(List<SourceConfiguration> sourceConfigurations, Dao dao) {
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

	// TODO Initialization of sources should be done concurrently to the launches of analyses, a thread must be
	// dedicated to this task.
	private void launchSortedAnalysisOnSource(final SourceExtractorFactory sourceExtractorFactory, final SourceConfiguration sourceConfiguration,
			final Collection<AnalysisConfiguration> analysesConfigurations) {

		// We create a thread dedicated to this source. It will be in charge of extracting it and launching the set of analyses on it
		threadsPool.execute(new Thread() {

			@Override
			public void run() {
				SourceExtractor<?> sourceExtractor = sourceExtractorFactory.createSourceExtractor(sourceConfiguration);
				if (sourceExtractor == null) {
					HarmonyLogger.error("Could not load the source:" + sourceConfiguration.getRepositoryURL());
					return;
				}

				String url = sourceExtractor.getConfig().getRepositoryURL();
				SourceExecutionReport executionReport = new SourceExecutionReport();
				executionReport.setSourceUrl(url);

				try {
					long startTime = System.currentTimeMillis();
					Dao dao = daoFactory.createDao();
					// Before launching any analysis on the source we must extract it (clone repository, build and store the Harmony model)
					// If a source exists in the DB before the extraction, we reuse it and do not extract the model again.
					Source src = dao.getSourceByUrl(url);
					if (src != null) {
						HarmonyLogger.info("Initializing existing source, extraction will not be performed again.");
						sourceExtractor.initializeExistingSource(src);
					} else {

						if (analysesConfigurations.isEmpty()) {
							// if there is no analysis, we simply extract the model
							sourceExtractor.initializeSource(true, true);
						} else {
							// If at least one analysis requires the actions or the harmony model, we have to extract them
							// these values are at true by default, so unless specified explicitly in the configuration, they will be extracted
							boolean extractActions = false;
							boolean extractHarmonyModel = false;
							for (AnalysisConfiguration a : analysesConfigurations) {
								if (a != null) {
									extractActions = (a.requireActions()) || extractActions;
									extractHarmonyModel = (a.requireHarmonyModel()) || extractHarmonyModel;
								}
							}
							sourceExtractor.initializeSource(extractHarmonyModel, extractActions);
						}
					}

					AnalysisFactory analysisFactory = new AnalysisFactory(dao);
					// We perform the analysis one after the other and between each of them we check
					// that an interruption of the thread wasn't requested due to the timeout limit.
					// TODO catch exception in the loop.
					for (Iterator<AnalysisConfiguration> it = analysesConfigurations.iterator(); it.hasNext() && !this.isInterrupted();) {
						ISingleSourceAnalysis currentAnalysis = analysisFactory.createAnalysis(it.next());
						HarmonyLogger.info("Running analysis " + currentAnalysis.getConfig().getAnalysisName() + " on source "
								+ sourceExtractor.getSource().getUrl());
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
					ExecutionMonitor monitor = new ExecutionMonitor(daoFactory.createDao());
					monitor.addSourceExecutionReport(executionReportId, executionReport);
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

				HarmonyLogger
						.error("Execution timeout, the pool of analysis threads will be shutdown (You may check your configuration file for running longer analysis)");

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
