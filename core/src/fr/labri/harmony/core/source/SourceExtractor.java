package fr.labri.harmony.core.source;

import fr.labri.harmony.core.config.model.SourceConfiguration;
import fr.labri.harmony.core.model.Source;

public interface SourceExtractor<W extends Workspace> {
	
	SourceConfiguration getConfig();

	void run() throws WorkspaceException, SourceExtractorException;

	Source getSource();

	W getWorkspace();

}
