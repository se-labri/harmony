package fr.labri.harmony.core.execution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 *
 * @param <N> The Node Type
 */
public class Dag<N> {

	private Map<String, Set<String>> edges;
	private Map<String, N> vertices;

	public Dag() {
		edges = new HashMap<>();
		vertices = new HashMap<>();
	}
	
	public Dag(Dag<N> other) {
		edges = new HashMap<>(other.edges);
		vertices = new HashMap<>(other.vertices);
	}
	
	public void addVertex(String name, N value) {
		vertices.put(name, value);
		edges.put(name, new HashSet<String>());
	}
	
	public List<N> getTopoOrder() {
		Dag<N> dag = new Dag<>(this);
		ArrayList<N> result = new ArrayList<>();
		List<String> roots = dag.getRoots();
		while (!roots.isEmpty()) {
			String a = roots.get(0);
			roots.remove(0);
			result.add(dag.vertices.get(a));

			for (String dependant : dag.getNextVertices(a)) {
				dag.removeEdge(a, dependant);
				if (!dag.hasIncomingEdges(dependant)) {
					roots.add(dependant);
				}
			}
		}
		if (!dag.hasEdges()) return result;

		throw new RuntimeException("Scheduling not possible, there are cyclic dependencies between analyses");

	}

	private boolean hasIncomingEdges(String vertex) {
		for (Set<String> e : edges.values()) {
			if (e.contains(vertex)) return true;
		}
		return false;
	}

	private void removeEdge(String source, String target) {
		Collection<String> targetVertices = getNextVertices(source);
		targetVertices.remove(target);

	}

	public Collection<String> getNextVertices(String vertex) {
		return edges.get(vertex);
	}

	private List<String> getRoots() {
		List<String> roots = new ArrayList<>();
		for(String vertex : vertices.keySet()) {
			if (!hasIncomingEdges(vertex)) roots.add(vertex);
		}
		return roots;
	}

	public void addEdge(String source, String target) {
		Set<String> e = edges.get(source);
		if (e == null) e = new HashSet<>();
		e.add(target);
	}
	
	public boolean hasEdges() {
		for (Set<String> e : edges.values()) {
			if(!e.isEmpty()) return true;
		}
		return false;
	}

}
