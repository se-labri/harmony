package fr.labri.harmony.analysis.xtic;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import fr.labri.harmony.analysis.xtic.XticAnalysis.AnalyseSource;
import fr.labri.harmony.analysis.xtic.aptitude.PatternAptitude;
import fr.labri.harmony.core.model.Action;
import fr.labri.harmony.core.model.Event;

public class FilterMimeType {
	static public FilterXTic getFilter(List<PatternAptitude> _patterns) {
		StringBuilder str = new StringBuilder();
		boolean first = true;
		for (PatternAptitude p : _patterns) {
			for(Pattern mime :  p.getMime().keySet()) {
				if (mime != null) {
					if (!first)
						str.append("|");
					str.append('(');
					str.append(mime.pattern());
					str.append(')');
					first = false;
				} else {
					first = true;
					break;
				}
			}
		}

		if (first)
			return new FilterXTic() {
			@Override
			public Object filter(AnalyseSource analyse, Event event, Event parent, List<Action> actions) {
				return null;
			}
		};

		final Pattern _pattern = Pattern.compile(str.toString());
		return new FilterXTic() {
			@Override
			public Object filter(AnalyseSource analyse, Event event, Event parent, List<Action> actions) {
				List<Action> toRemove = new ArrayList<Action>();
				for (Action a : actions) {
					if (!_pattern.matcher(a.getItem().getNativeId()).find())
						toRemove.add(a);
				}
				actions.removeAll(toRemove);
				return null;
			}
		};
	}

}
