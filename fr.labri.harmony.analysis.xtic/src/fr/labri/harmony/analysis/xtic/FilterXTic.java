package fr.labri.harmony.analysis.xtic;

import java.util.List;

import fr.labri.harmony.analysis.xtic.XticAnalysis.AnalyseSource;
import fr.labri.harmony.core.model.Action;
import fr.labri.harmony.core.model.Event;

public interface FilterXTic {
	public Object filter(AnalyseSource analyse, Event event, Event parent, List<Action> actions);
}
