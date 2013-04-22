package fr.labri.harmony.core.execution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import fr.labri.harmony.core.Analysis;

/**
 * This is a Directed Acyclic Graph that represents dependencies between
 * analyses. For a given Edge, the target depends on the source, this
 * 
 */
public class DependencyGraph {

	private Map<Analysis, List<String>> dependencies;
	private Map<String, Analysis> analyses;

	public Collection<Analysis> getTopoOrder() {
		ArrayList<Analysis> result = new ArrayList<>();
		List<String> roots = getRoots();
		while (!roots.isEmpty()) {
			String a = roots.get(0);
			roots.remove(0);
			result.add(analyses.get(a));

			for (String dependant : getDependentAnalyses(a)) {
				removeEdge(a, dependant);
				if (!hasIncomingEdges(dependant)) {
					roots.add(dependant);
				}
			}
		}
		if (dependencies.isEmpty()) return result;

		throw new RuntimeException("Scheduling not possible, there are cyclic dependencies between analyses");

	}

	private boolean hasIncomingEdges(String analysis) {
		
		return (dependencies.get(analysis) != null && !dependencies.get(analysis).isEmpty());
	}

	private void removeEdge(String source, String target) {
		// TODO Auto-generated method stub

	}

	private Collection<String> getDependentAnalyses(String analysis) {
		return dependencies.get(analysis);
	}

	private List<String> getRoots() {
		// TODO Auto-generated method stub
		return null;
	}

}
