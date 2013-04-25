package fr.labri.harmony.core.execution;

import static org.junit.Assert.*;
import java.util.List;

import org.junit.Test;

public class DagTest {

	String v1 = "v1";
	String v2 = "v2";
	String v3 = "v3";
	
	@Test
	public void testDag() {
		Dag<String> dag = new Dag<>();
		
		dag.addVertex( v1, v1);
		dag.addVertex( v2, v2);
		dag.addVertex( v3, v3);
		
		dag.addEdge(v1, v2);
		dag.addEdge(v3, v1);
		
		List<String> order = dag.getTopoOrder();
		assertEquals(v3, order.get(0));
		assertEquals(v1, order.get(1));
		assertEquals(v2, order.get(2));
	}
	
	
}
