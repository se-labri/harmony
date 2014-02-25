package fr.labri.harmony.core.source;

import java.util.ArrayList;
import java.util.List;

import fr.labri.harmony.core.analysis.ISingleSourceAnalysis;
import fr.labri.harmony.core.config.model.SourceConfiguration;
import fr.labri.harmony.core.dao.AbstractDao;
import fr.labri.harmony.core.dao.ModelPersister;
import fr.labri.harmony.core.log.HarmonyLogger;
import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.Source;

public abstract class AbstractSourceExtractor<W extends Workspace> implements SourceExtractor<W> {

	// Vcs properties
	public final static String COMMIT_MESSAGE = "commit_message";
	public final static String COMMITTER = "committer";
	public final static String BRANCH = "branch";

	protected ModelPersister modelPersister;
	protected W workspace;
	protected Source source;
	protected List<ISingleSourceAnalysis> analyses;
	protected SourceConfiguration config;

	public AbstractSourceExtractor(SourceConfiguration config, ModelPersister modelPersister) {
		this.config = config;
		analyses = new ArrayList<>();	
		this.modelPersister = modelPersister;
	}

	public AbstractSourceExtractor() {
		super();
	}

	@Override
	public Source getSource() {
		return this.source;
	}

	@Override
	public W getWorkspace() {
		return this.workspace;
	}

	public String getUrl() {
		return config.getRepositoryURL();
	}

	@Override
	public SourceConfiguration getConfig() {
		return config;
	}

	public String getPersitenceUnitName() {
		return AbstractDao.HARMONY_PERSISTENCE_UNIT;
	}

	@Override
	public void initializeSource(boolean extractHarmonyModel, boolean extractActions) {
		HarmonyLogger.info("Initializing Workspace for source " + getUrl());
		initializeWorkspace();

		source = new Source();
		source.setUrl(getUrl());
		source.setWorkspace(workspace);

		modelPersister.saveSource(source);
		if (extractHarmonyModel) {
			HarmonyLogger.info("Extracting Events for source " + getUrl());
			extractEvents();
			// Save the remaining events
			modelPersister.flushEvents();
			
			if (extractActions) {
				HarmonyLogger.info("Extracting Actions for source " + getUrl());

				for (Event e : modelPersister.getEvents(source))
					extractActions(e);
				modelPersister.flushActions();
			}
			source = modelPersister.reloadSource(source);
		}
		// include the configuration in the source (may be useful to get the source's options)
		source.setConfig(getConfig());

		onExtractionFinished();
	}

	@Override
	public void initializeExistingSource(Source src) {
		this.source = src;
		try {
			initializeWorkspace();
		} catch (Exception e) {
			HarmonyLogger.info("Workspace couldn't be initialized for source: " + src.getUrl());
		}
		source.setWorkspace(workspace);
		source.setConfig(getConfig());
	}

	/**
	 * Called at the end of the {@link #initializeSource(boolean)} method, when all extraction is finished. Does nothing by default
	 */
	protected void onExtractionFinished() {
		HarmonyLogger.info("Extraction finished for source " + source.getUrl());
	}

	protected boolean extractItemWithPath(String path) {
		return config.getItemfilter() == null || path.matches(config.getItemfilter());
	}
}
