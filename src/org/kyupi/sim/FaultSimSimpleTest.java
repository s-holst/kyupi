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

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.kyupi.data.FormatStil;
import org.kyupi.data.item.BVector;
import org.kyupi.data.source.BBSource;
import org.kyupi.data.source.QVSource;
import org.kyupi.faults.FaultState;
import org.kyupi.faults.StuckAtCollection;
import org.kyupi.graph.Graph;
import org.kyupi.graph.GraphTools;
import org.kyupi.graph.Library;
import org.kyupi.graph.LibrarySAED;
import org.kyupi.misc.RuntimeTools;

public class FaultSimSimpleTest extends TestCase {

	protected static Logger log = Logger.getLogger(FaultSimSimpleTest.class);


	// FIXME
	@Test
	public void testFaultSimS27() throws Exception {
		Library l = new LibrarySAED();
		Graph g = GraphTools.loadGraph(RuntimeTools.KYUPI_HOME + "/testdata/SAED90/s27.v", l);
		log.info("Graph=\n" + g);
		GraphTools.replaceScanCellsWithPseudoPorts(g);
		log.info("Graph=\n" + g);
		FormatStil p = new FormatStil(RuntimeTools.KYUPI_HOME + "/testdata/s27.stil", g);
		QVSource t = p.getStimuliSource();
		//StuckAtCollection f = new StuckAtCollection(g);
		//FaultSimSimple fsim = new FaultSimSimple(f, BBSource.from(t));
		//fsim.next();
	}
	// FIXME 
	public void xtestFaultSimSimple() {
		Graph g = GraphTools.benchToGraph("INPUT(a) OUTPUT(z) z=DFF(a)");
		//log.info("Graph=\n" + g);
		StuckAtCollection f = new StuckAtCollection(g);
		ArrayList<BVector> patterns = new ArrayList<BVector>();
		patterns.add(new BVector("000"));
		patterns.add(new BVector("100"));
		patterns.add(new BVector("001"));
		patterns.add(new BVector("001"));
		patterns.add(new BVector("101"));
		FaultSimSimple fsim = new FaultSimSimple(f, BBSource.from(3, patterns));
		fsim.next();
		FaultState a_sa0 = f.na[0][0].outputSA0;
		FaultState a_sa1 = f.na[0][0].outputSA1;
		FaultState z_sa0 = f.na[0][2].outputSA0;
		FaultState z_sa1 = f.na[0][2].outputSA1;
		
		assertEquals(61, a_sa0.detects);
		assertEquals(3, a_sa1.detects);
		assertEquals(62, z_sa0.detects);
		assertEquals(2, z_sa1.detects);
		
		
		
	}
}
