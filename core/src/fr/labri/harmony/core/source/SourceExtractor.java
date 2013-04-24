package fr.labri.harmony.core.source;

import java.util.List;
import java.util.Properties;

import fr.labri.harmony.core.Analysis;
import fr.labri.harmony.core.config.model.SourceConfiguration;
import fr.labri.harmony.core.dao.Dao;
import fr.labri.harmony.core.model.Source;

public interface SourceExtractor<W extends Workspace> {
	
	SourceConfiguration getConfig();

	void run() throws WorkspaceException, SourceExtractorException;

	Source getSource();

	W getWorkspace();

	void setAnalyses(List<Analysis> analyses);

	<S extends SourceExtractor<?>> S create(SourceConfiguration config, Dao dao, Properties properties);

}
