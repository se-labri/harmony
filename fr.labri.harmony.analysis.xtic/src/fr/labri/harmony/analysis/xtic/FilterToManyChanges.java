package fr.labri.harmony.analysis.xtic;

import java.util.List;

import fr.labri.harmony.analysis.xtic.XticAnalysis.AnalyseSource;
import fr.labri.harmony.core.log.HarmonyLogger;
import fr.labri.harmony.core.model.Action;
import fr.labri.harmony.core.model.Event;

public class FilterToManyChanges implements FilterXTic {
	static final double LIMIT_ACTIONS_PER_EVENT = 300;

	// Un commit avec + de 300 actions ne fait pas de sens : soit un gros
	// merge, ou whatever, mais pas de la contrib purement technique

	@Override
	public Object filter(AnalyseSource analyse, Event event, Event parent, List<Action> actions) {
		if (actions.size() > LIMIT_ACTIONS_PER_EVENT) {
			HarmonyLogger.info("Skip due to too many actions " + actions.size() + "\t" + actions.get(0).getEvent());
			actions.clear();
		}
		return null;
	}

}
