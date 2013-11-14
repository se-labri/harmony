package fr.labri.harmony.analysis.xtic;

import java.util.ArrayList;
import java.util.List;

import fr.labri.harmony.analysis.xtic.XticAnalysis.AnalyseSource;
import fr.labri.harmony.core.model.Action;
import fr.labri.harmony.core.model.ActionKind;
import fr.labri.harmony.core.model.Event;

public class FilterDeleteAction implements FilterXTic {

	@Override
	public Object filter(AnalyseSource analyse, Event event, Event parent, List<Action> actions) {
		List<Action> toDelete = new ArrayList<Action>();
		for(Action a : actions )
			if(a.getKind().equals(ActionKind.Delete) )
				toDelete.add(a);
		actions.removeAll(toDelete);
		return null;
	}

}
