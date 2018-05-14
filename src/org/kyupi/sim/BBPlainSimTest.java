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

import org.apache.log4j.Logger;
import org.junit.Test;
import org.kyupi.circuit.CircuitTools;
import org.kyupi.circuit.LevelizedCircuit;
import org.kyupi.circuit.Library;
import org.kyupi.circuit.LibraryNangate;
import org.kyupi.circuit.LibrarySAED;
import org.kyupi.circuit.MutableCircuit;
import org.kyupi.data.item.BBlock;
import org.kyupi.data.item.BVector;
import org.kyupi.data.source.BBSource;
import org.kyupi.data.source.BVSource;
import org.kyupi.misc.RuntimeTools;

import junit.framework.TestCase;

public class BBPlainSimTest extends TestCase {

	protected static Logger log = Logger.getLogger(BBPlainSimTest.class);

	@Test
	public void testNorInv() {
		LevelizedCircuit g = CircuitTools.parseBench("input(a) input(b) output(nor) output(inv) nor=NOR(a,b) inv=NOT(a)").levelized();
		int length = g.width();

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
			assertEqualsReport(ref.next(), sim.next(), i, g);
		}
	}

	@Test
	public void testC17Nangate() throws Exception {
		LevelizedCircuit g_ref = CircuitTools.loadCircuit(new File(RuntimeTools.KYUPI_HOME, "testdata/c17.isc"), new Library()).levelized();
		LevelizedCircuit g_test = CircuitTools.loadCircuit(new File(RuntimeTools.KYUPI_HOME, "testdata/Nangate/c17.v"), new LibraryNangate()).levelized();
		assertEqualsByRandomSimulation(g_ref, g_test);
	}

	@Test
	public void testC17Saed90() throws Exception {
		LevelizedCircuit g_ref = CircuitTools.loadCircuit(new File(RuntimeTools.KYUPI_HOME, "testdata/c17.isc"), new Library()).levelized();
		LevelizedCircuit g_test = CircuitTools.loadCircuit(new File(RuntimeTools.KYUPI_HOME, "testdata/SAED90/c17.v"), new LibrarySAED()).levelized();
		assertEqualsByRandomSimulation(g_ref, g_test);
	}

	@Test
	public void testSAED90cells() throws Exception {
		LevelizedCircuit g_ref = CircuitTools.loadCircuit(new File(RuntimeTools.KYUPI_HOME, "testdata/SAED90/SAED90norinv.v"), new LibrarySAED()).levelized();
		MutableCircuit mg_test = CircuitTools.loadCircuit(new File(RuntimeTools.KYUPI_HOME, "testdata/SAED90/SAED90cells.v"), new LibrarySAED());
		CircuitTools.splitMultiOutputCells(mg_test);
		LevelizedCircuit g_test = mg_test.levelized();
		assertEqualsByRandomSimulation(g_ref, g_test);
	}

	private void assertEqualsByRandomSimulation(LevelizedCircuit g_ref, LevelizedCircuit g_test) {
		int length = g_ref.width();
		assertEquals(length, g_test.width());

		BVSource ref = BVSource.from(new BBPlainSim(g_ref, BBSource.random(length, 42)));
		BVSource test = BVSource.from(new BBPlainSim(g_test, BBSource.random(length, 42)));

		// simulate 128 random patterns and compare the responses.
		for (int i = 0; i < 128; i++) {
			assertEqualsReport(ref.next(), test.next(), i, g_ref);
		}
	}

	private void assertEqualsReport(BVector expected, BVector actual, int pindex, LevelizedCircuit lc) {
		if (!expected.equals(actual)) {
			int l = expected.length();
			StringBuffer buf = new StringBuffer();
			for (int i = 0; i < l; i++) {
				char e = expected.getValue(i);
				char a = actual.getValue(i);
				if (e != a) {
					buf.append(" " + lc.intf(i).name() + "=" + a + "(exp:" + e + ")");
				}
			}
			fail("Mismatched pattern " + pindex + ": " + actual + "(exp:" + expected + ")" + buf.toString());
		}
		expected.free();
		actual.free();
	}
}
