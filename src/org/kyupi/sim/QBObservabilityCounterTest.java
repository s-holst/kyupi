/*
 * Copyright 2013 The KyuPI project contributors. See the COPYRIGHT.md file
 * at the top-level directory of this distribution.
 * This file is part of the KyuPI project. It is subject to the license terms
 * in the LICENSE.md file found in the top-level directory of this distribution.
 * No part of the KyuPI project, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.md file.
 */
package org.kyupi.sim;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.kyupi.data.FormatStil;
import org.kyupi.data.item.BVector;
import org.kyupi.data.source.QBSource;
import org.kyupi.data.source.QVSource;
import org.kyupi.graph.Graph;
import org.kyupi.graph.Graph.Node;
import org.kyupi.graph.GraphTools;
import org.kyupi.graph.Library;
import org.kyupi.graph.LibrarySAED;
import org.kyupi.misc.RuntimeTools;

import junit.framework.TestCase;

public class QBObservabilityCounterTest extends TestCase {

	protected static Logger log = Logger.getLogger(QBObservabilityCounterTest.class);

	@Test
	public void testS27() throws Exception {
		Library l = new LibrarySAED();
		Graph g = GraphTools.loadGraph(RuntimeTools.KYUPI_HOME + "/testdata/SAED90/s27.v", l);
		//log.info("Graph=\n" + g);
		FormatStil p = new FormatStil(RuntimeTools.KYUPI_HOME + "/testdata/s27.stil", g);
		QVSource t = p.getStimuliSource();
		QBObservabilityCounter obs = new QBObservabilityCounter(g, QBSource.from(t));
		obs.next();
		// FIXME: assert fault coverage
	}
	
	@Test
	public void test() {
		Graph g = GraphTools.benchToGraph("INPUT(a) OUTPUT(z) z=DFF(a)");
		//log.info("Graph=\n" + g);
		ArrayList<BVector> patterns = new ArrayList<BVector>();
		patterns.add(new BVector("000"));
		patterns.add(new BVector("100"));
		patterns.add(new BVector("001"));
		patterns.add(new BVector("001"));
		patterns.add(new BVector("101"));
		QBObservabilityCounter obs = new QBObservabilityCounter(g, QBSource.from(3, patterns));
		obs.next();
		Node a = g.searchNode("a");
		Node z = g.searchNode("z_");
		
		assertNotNull(a);
		assertNotNull(z);

		int a_idx = g.accessSignalMap().idxForOutput(a, 0);
		int z_idx = g.accessSignalMap().idxForOutput(z, 0);

		assertEquals(61, obs.getSA0ObsCount(a_idx));
		assertEquals(3, obs.getSA1ObsCount(a_idx));
		// FIXME: correct handling of sequential elements during fault simulation
		assertEquals(62, obs.getSA0ObsCount(z_idx));
		assertEquals(2, obs.getSA1ObsCount(z_idx));
		
		
		
	}
}
