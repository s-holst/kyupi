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

import java.io.File;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.kyupi.data.item.BBlock;
import org.kyupi.data.item.BVector;
import org.kyupi.data.source.BBSource;
import org.kyupi.data.source.BVSource;
import org.kyupi.graph.Graph;
import org.kyupi.graph.GraphTools;
import org.kyupi.graph.Library;
import org.kyupi.graph.LibraryNangate;
import org.kyupi.graph.LibrarySAED;
import org.kyupi.graph.Graph.Node;
import org.kyupi.misc.RuntimeTools;

public class BBPlainSimTest extends TestCase {

	protected static Logger log = Logger.getLogger(BBPlainSimTest.class);

	@Test
	public void testNorInv() {
		Graph g = GraphTools.benchToGraph("input(a) input(b) output(nor) output(inv) nor=NOR(a,b) inv=NOT(a)");
		int length = g.accessInterface().length;

		BVSource sim = BVSource.from(new BBPlainSim(g, BBSource.random(length, 42)));
		BVSource ref = BVSource.from(new BBSource(length) {
			private BBSource rand = BBSource.random(length(), 42);

			public void reset() {
				rand.reset();
			}

			protected BBlock compute() {
				BBlock output = rand.next();
				output.set(2, ~(output.get(0) | output.get(1)));
				output.set(3, ~output.get(0));
				return output;
			}
		});

		// simulate 128 random patterns and compare the responses.
		for (int i = 0; i < 128; i++) {
			assertEqualsReport(ref.next(), sim.next(), i, g.accessInterface());
		}
	}

	public void testForced() throws Exception {
		Graph g = GraphTools.benchToGraph("input(a) input(b) output(nor) output(iinv) nor=NOR(a,b) inv=NOT(a) iinv=NOT(inv)");
		ArrayList<BVector> tests = new ArrayList<>();
		tests.add(new BVector("1100"));
		tests.add(new BVector("0100"));
		tests.add(new BVector("1000"));
		tests.add(new BVector("0000"));

		BBPlainSim sim = new BBPlainSim(g, BBSource.from(4, tests));

		BBlock good = sim.next();
		log.debug("ports:  a  b  !(a+b)  a");
		for (int i = 0; i < 4; i++) {
			log.debug("good[" + i + "] = " + good.toString(i));
		}
		Node node_a = g.searchNode("a");
		Node node_b = g.searchNode("b");
		Node node_nor_ = g.searchNode("nor_");
		Node node_inv_ = g.searchNode("inv");
		
		assertEquals(5L, sim.get(node_a));
		assertEquals(3L, sim.get(node_b));
		assertEquals(-8L, sim.get(node_nor_));
		assertEquals(-6L, sim.get(node_inv_));
		
		BBPlainDeltaSim dsim = sim.newDeltaSim();
		
		dsim.force(node_inv_, 0L);
		
		BBlock faulty = new BBlock(4);
		good.copyTo(-1L, faulty);
		dsim.sim(faulty);
		for (int i = 0; i < 4; i++) {
			log.debug("faulty1[" + i + "] = " + faulty.toString(i));
		}
		
		dsim.force(node_a, 0L);
		dsim.sim(faulty);
		for (int i = 0; i < 4; i++) {
			log.debug("faulty2[" + i + "] = " + faulty.toString(i));
		}
		dsim.free();
	}
	
	public void testFaultSimNaive() {
		Graph g = GraphTools.benchToGraph("INPUT(a) OUTPUT(z) z=DFF(a)");
		log.debug("Graph=\n" + g);
		ArrayList<BVector> patterns = new ArrayList<BVector>();
		patterns.add(new BVector("000"));
		patterns.add(new BVector("100"));
		patterns.add(new BVector("001"));
		patterns.add(new BVector("101"));
		BBPlainSim sim = new BBPlainSim(g, BBSource.from(3, patterns));
		BBlock good = sim.next();

		log.debug("good: " + good);
		BBlock faulty = new BBlock(3);
		good.copyTo(-1L, faulty);
		
		BBPlainDeltaSim dsim = sim.newDeltaSim();
		
		dsim.force(g.searchNode("z_"), -1L);
		dsim.sim(faulty);
		
		assertEquals(-1L, faulty.get(1));
		assertEquals(good.get(2), faulty.get(2)); // injection must not change ff value itself.

		log.debug("faulty: " + faulty);

		dsim.free();
	}

	@Test
	public void testC17Nangate() throws Exception {
		Graph g_ref = GraphTools.loadGraph(new File(RuntimeTools.KYUPI_HOME, "testdata/c17.isc"), new Library());
		Graph g_test = GraphTools.loadGraph(new File(RuntimeTools.KYUPI_HOME, "testdata/Nangate/c17.v"), new LibraryNangate());
		assertEqualsByRandomSimulation(g_ref, g_test);
	}

	@Test
	public void testC17Saed90() throws Exception {
		Graph g_ref = GraphTools.loadGraph(new File(RuntimeTools.KYUPI_HOME, "testdata/c17.isc"), new Library());
		Graph g_test = GraphTools.loadGraph(new File(RuntimeTools.KYUPI_HOME, "testdata/SAED90/c17.v"), new LibrarySAED());
		assertEqualsByRandomSimulation(g_ref, g_test);
	}

	@Test
	public void testSAED90cells() throws Exception {
		Graph g_ref = GraphTools.loadGraph(new File(RuntimeTools.KYUPI_HOME, "testdata/SAED90/SAED90norinv.v"), new LibrarySAED());
		Graph g_test = GraphTools.loadGraph(new File(RuntimeTools.KYUPI_HOME, "testdata/SAED90/SAED90cells.v"), new LibrarySAED());
		GraphTools.splitMultiOutputCells(g_test);
		assertEqualsByRandomSimulation(g_ref, g_test);
	}

	private void assertEqualsByRandomSimulation(Graph g_ref, Graph g_test) {
		int length = g_ref.accessInterface().length;
		assertEquals(length, g_test.accessInterface().length);

		BVSource ref = BVSource.from(new BBPlainSim(g_ref, BBSource.random(length, 42)));
		BVSource test = BVSource.from(new BBPlainSim(g_test, BBSource.random(length, 42)));

		// simulate 128 random patterns and compare the responses.
		for (int i = 0; i < 128; i++) {
			assertEqualsReport(ref.next(), test.next(), i, g_ref.accessInterface());
		}
	}

	private void assertEqualsReport(BVector expected, BVector actual, int pindex, Node[] intf) {
		if (!expected.equals(actual)) {
			int l = expected.length();
			StringBuffer buf = new StringBuffer();
			for (int i = 0; i < l; i++) {
				char e = expected.getValue(i);
				char a = actual.getValue(i);
				if (e != a) {
					buf.append(" " + intf[i].queryName() + "=" + a + "(exp:" + e + ")");
				}
			}
			fail("Mismatched pattern " + pindex + ": " + actual + "(exp:" + expected + ")" + buf.toString());
		}
		expected.free();
		actual.free();
	}
}
