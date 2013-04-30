package fr.labri.harmony.core.source;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import fr.labri.harmony.core.AbstractHarmonyService;
import fr.labri.harmony.core.analysis.Analysis;
import fr.labri.harmony.core.config.model.SourceConfiguration;
import fr.labri.harmony.core.dao.Dao;
import fr.labri.harmony.core.model.Source;

public abstract class AbstractSourceExtractor<W extends Workspace> extends AbstractHarmonyService implements SourceExtractor<W> {

	// Vcs properties
	public final static String COMMIT_LOG = "commit_log";
	public final static String COMMITTER = "committer";
	public final static String BRANCH = "branch";

	protected W workspace;

	protected Source source;

	protected List<Analysis> analyses;

	protected SourceConfiguration config;

	public AbstractSourceExtractor(SourceConfiguration config, Dao dao, Properties properties) {
		super(dao, properties);
		this.config = config;
		analyses = new ArrayList<>();
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

	@Override
	public void initializeSourceFully() {
		initializeWorkspace();

		source = new Source();
		source.setUrl(getUrl());
		source.setWorkspace(workspace);

		dao.saveSource(source);
		// FIXME seems really long!
		extractSource();
		dao.refreshElement(source);
	}

}
