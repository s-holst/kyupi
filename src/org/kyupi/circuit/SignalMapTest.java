package org.kyupi.circuit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.kyupi.circuit.MutableCircuit.MutableCell;

public class SignalMapTest {

	protected static Logger log = Logger.getLogger(SignalMapTest.class);

	@Test
	public void test() {
		MutableCircuit g = CircuitTools.parseBench("INPUT(a) INPUT(b) OUTPUT(c) OUTPUT(d) c=AND(a,b) d=BUF(c)");
		assertEquals(6, g.countNodes());
		assertEquals(2, g.countInputs());
		assertEquals(2, g.countOutputs());
		
		//log.info("Graph:\n" + g.toString());
		
		SignalMap em = new SignalMap(g);
		assertEquals(5, em.length());
		MutableCell a = g.searchNode("a");
		MutableCell c_ = g.searchNode("c_");
		MutableCell c = g.searchNode("c");
		MutableCell d_ = g.searchNode("d_");
		
		
		assertEquals(em.idxForOutput(a, 0), em.idxForInput(c_, 0));
		assertEquals(em.idxForOutput(c_, 0), em.idxForInput(c, 0));
		assertEquals(em.idxForOutput(c_, 1), em.idxForInput(d_, 0));
		
		assertNotEquals(em.idxForOutput(c_, 0), em.idxForInput(d_, 0));
		assertNotEquals(em.idxForOutput(c_, 0), em.idxForInput(c_, 0));
	}

}
