package fr.labri.harmony.core.source;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.fasterxml.jackson.databind.node.ObjectNode;

import fr.labri.harmony.core.AbstractHarmonyService;
import fr.labri.harmony.core.Analysis;
import fr.labri.harmony.core.Dao;
import fr.labri.harmony.core.model.Source;

public abstract class AbstractSourceExtractor<W extends Workspace> extends AbstractHarmonyService implements SourceExtractor<W> {
	
	protected W workspace;
	
	protected Source source;

	protected List<Analysis> analyses;
	
	public AbstractSourceExtractor() {
	}
	
	public AbstractSourceExtractor(ObjectNode config, Dao dao, Properties properties) {
		super(config, dao, properties);
		analyses = new ArrayList<>();
	}

	@Override
	public void run() throws SourceExtractorException, WorkspaceException {
		LOGGER.info("Starting extraction of source " + getUrl());
		long start = System.currentTimeMillis();
		
		createWorkspace();
		workspace.setSourceExtractor(this);
		workspace.init();
		
		source = new Source();
		source.setUrl(getUrl());
		source.setWorkspace(workspace);
		dao.saveSource(source);
		
		//FIXME seems really long!
		extractSource();
		dao.refreshElement(source);
		
		for (Analysis a: analyses) a.run(source);
		
		workspace.clean();
		
		long stop = System.currentTimeMillis();
		long time = (stop - start) / 1000;
		LOGGER.info("Source " + getUrl() + " processed. Time: " + time + " seconds.");
	}
	
	//TODO these two methods should be documented
	protected abstract void createWorkspace() throws WorkspaceException;
	
	protected abstract void extractSource() throws SourceExtractorException;

	@Override
	public Source getSource() {
		return this.source;
	}

	@Override
	public W getWorkspace() {
		return this.workspace;
	}
	
	@Override
	public void setAnalyses(List<Analysis> analyses) {
		this.analyses = analyses;
	}
	
	public String getUrl() {
		return config.get("url").asText();
	}
	
}
