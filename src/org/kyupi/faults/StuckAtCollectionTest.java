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

import static org.junit.Assert.assertEquals;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.kyupi.graph.Graph;
import org.kyupi.graph.GraphTools;

public class StuckAtCollectionTest {

	protected static Logger log = Logger.getLogger(StuckAtCollection.class);
	
	@Test
	public void test() {
		Graph g = GraphTools.benchToGraph("INPUT(a) OUTPUT(z) z=DFF(a)");
		log.debug("Graph=\n" + g);
		StuckAtCollection f = new StuckAtCollection(g);
		assertEquals(4, f.numCollapsedFaults());
		
		g = GraphTools.benchToGraph("INPUT(a) OUTPUT(z) z=NOT(a)");
		f = new StuckAtCollection(g);
		assertEquals(2, f.numCollapsedFaults());
	}

}
