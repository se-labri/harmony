package fr.labri.harmony.core.source;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import fr.labri.harmony.core.AbstractHarmonyService;
import fr.labri.harmony.core.Analysis;
import fr.labri.harmony.core.config.model.SourceConfiguration;
import fr.labri.harmony.core.dao.Dao;
import fr.labri.harmony.core.model.Source;

public abstract class AbstractSourceExtractor<W extends Workspace> extends AbstractHarmonyService implements SourceExtractor<W> {
	
	protected W workspace;
	
	protected Source source;

	protected List<Analysis> analyses;
	
	protected SourceConfiguration config;
	
	public AbstractSourceExtractor() {
	}
	
	public AbstractSourceExtractor(SourceConfiguration config, Dao dao, Properties properties) {
		super(dao, properties);
		this.config = config;
		analyses = new ArrayList<>();
	}

	public void run() throws SourceExtractorException, WorkspaceException {
		LOGGER.info("Starting extraction of source " + getUrl());
		long start = System.currentTimeMillis();
		
		workspace.setSourceExtractor(this);
		workspace.init();
		
		source = new Source();
		source.setUrl(getUrl());
		source.setWorkspace(workspace);
		dao.saveSource(source);
		
		//FIXME seems really long!
		extractSource();
		dao.refreshElement(source);
		
		for (Analysis a: analyses) a.runOn(source);
		
		workspace.clean();
		
		long stop = System.currentTimeMillis();
		long time = (stop - start) / 1000;
		LOGGER.info("Source " + getUrl() + " processed. Time: " + time + " seconds.");
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
	
}
