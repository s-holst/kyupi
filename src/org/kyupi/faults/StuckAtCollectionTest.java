/*
 * Copyright 2013 The KyuPI project contributors. See the COPYRIGHT.md file
 * at the top-level directory of this distribution.
 * This file is part of the KyuPI project. It is subject to the license terms
 * in the LICENSE.md file found in the top-level directory of this distribution.
 * No part of the KyuPI project, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.md file.
 */
package org.kyupi.faults;

import static org.junit.Assert.*;


import org.apache.log4j.Logger;
import org.junit.Test;
import org.kyupi.faults.StuckAtCollection.StuckAtFault;
import org.kyupi.graph.Graph;
import org.kyupi.graph.Graph.Node;
import org.kyupi.graph.GraphTools;
import org.kyupi.graph.Library;

public class StuckAtCollectionTest {

	protected static Logger log = Logger.getLogger(StuckAtCollection.class);

	@Test
	public void test() {
		StuckAtCollection f;
		Graph g;

		g = GraphTools.benchToGraph("INPUT(a) OUTPUT(z) z=DFF(a)");
		//log.debug("Graph=\n" + g);
		f = new StuckAtCollection(g);
		assertEquals(8, f.size());

		g = GraphTools.benchToGraph("INPUT(a) OUTPUT(z) z=NOT(a)");
		f = new StuckAtCollection(g);
		assertEquals(8, f.size());
	}

	@Test
	public void testHashAbility() {
		Graph circuit = new Graph(new Library());
		Node n1 = circuit.new Node("n1", Library.TYPE_BUF);
		Node n2 = circuit.new Node("n2", Library.TYPE_BUF);
		Node n3 = circuit.new Node("n3", Library.TYPE_BUF);
		circuit.connect(n1, -1, n2, 0);
		circuit.connect(n2, -1, n3, 0);
		StuckAtFault n1o0sa0 = StuckAtFault.newOutputSA0(n1, 0);
		StuckAtFault n2i0sa0 = StuckAtFault.newInputSA0(n2, 0);

		assertFalse(n1o0sa0.equals(n2i0sa0));
		assertNotEquals("faults on different nodes have same hash code", n1o0sa0.hashCode(), n2i0sa0.hashCode());

		StuckAtFault n1o0sa1 = StuckAtFault.newOutputSA1(n1, 0);

		assertFalse(n1o0sa0.equals(n1o0sa1));
		assertNotEquals("sa0 and sa1 on same pin have same hash code", n1o0sa0.hashCode(), n1o0sa1.hashCode());

		StuckAtFault n2o0sa0 = StuckAtFault.newOutputSA0(n2, 0);

		assertFalse(n1o0sa0.equals(n2o0sa0));
		assertNotEquals("sa0 on different nodes have same hash code", n1o0sa0.hashCode(), n2o0sa0.hashCode());


		StuckAtFault n2i0sa0b = StuckAtFault.newInputSA0(n2, 0);

		assertTrue(n2i0sa0.equals(n2i0sa0b));
		assertEquals(n2i0sa0.hashCode(), n2i0sa0b.hashCode());
	}

}
