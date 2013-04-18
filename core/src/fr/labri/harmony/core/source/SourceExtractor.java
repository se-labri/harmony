package fr.labri.harmony.core.source;

import java.util.List;

import fr.labri.harmony.core.Analysis;
import fr.labri.harmony.core.HarmonyService;
import fr.labri.harmony.core.model.Source;

public interface SourceExtractor<W extends Workspace> extends HarmonyService {
	
	void run() throws WorkspaceException, SourceExtractorException;
	
	Source getSource();
	
	W getWorkspace();
	
	void setAnalyses(List<Analysis> analyses);
	
}
