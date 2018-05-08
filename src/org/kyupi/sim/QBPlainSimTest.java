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

import org.apache.log4j.Logger;
import org.junit.Test;
import org.kyupi.circuit.MutableCircuit;
import org.kyupi.circuit.CircuitTools;
import org.kyupi.circuit.Library;
import org.kyupi.circuit.LibraryNangate;
import org.kyupi.circuit.LibrarySAED;
import org.kyupi.circuit.MutableCircuit.MutableCell;
import org.kyupi.data.FormatStil;
import org.kyupi.data.item.QBlock;
import org.kyupi.data.item.QVector;
import org.kyupi.data.source.QBSource;
import org.kyupi.data.source.QVSource;
import org.kyupi.misc.RuntimeTools;

import junit.framework.TestCase;

public class QBPlainSimTest extends TestCase {

	protected static Logger log = Logger.getLogger(QBPlainSimTest.class);

	@Test
	public void testIntf() {
		MutableCircuit g = new MutableCircuit(new Library());
		MutableCell pos0out = g.new MutableCell("p0out", Library.TYPE_BUF | Library.FLAG_OUTPUT);
		pos0out.setIntfPosition(0);
		MutableCell pos1out = g.new MutableCell("p1out", Library.TYPE_BUF | Library.FLAG_OUTPUT);
		pos1out.setIntfPosition(1);
		MutableCell pos2in = g.new MutableCell("p2in", Library.TYPE_BUF | Library.FLAG_INPUT);
		pos2in.setIntfPosition(2);

		MutableCell buf = g.new MutableCell("buf", Library.TYPE_BUF);

		MutableCell pos3out = g.new MutableCell("p3out", Library.TYPE_BUF | Library.FLAG_OUTPUT);
		pos3out.setIntfPosition(3);
		MutableCell pos4out = g.new MutableCell("p4out", Library.TYPE_BUF | Library.FLAG_OUTPUT);
		pos4out.setIntfPosition(4);
		g.connect(pos2in, -1, buf, 0);
		g.connect(buf, -1, pos1out, 0);
		g.connect(pos1out, -1, pos0out, 0);
		g.connect(buf, -1, pos3out, 0);
		g.connect(pos3out, -1, pos4out, 0);
		// log.info("graph " + g);
		QVector v = new QVector("--1--");
		ArrayList<QVector> va = new ArrayList<>();
		va.add(v);
		QVSource pat = QVSource.from(5, va);
		QVSource sim = QVSource.from(new QBPlainSim(g, QBSource.from(pat)));
		assertEquals("11111", sim.next().toString());
	}

	public void testOAI21() {
		MutableCircuit g = new MutableCircuit(new LibrarySAED());
		MutableCell i0 = g.new MutableCell("i0", Library.TYPE_BUF | Library.FLAG_INPUT);
		MutableCell i1 = g.new MutableCell("i1", Library.TYPE_BUF | Library.FLAG_INPUT);
		MutableCell i2 = g.new MutableCell("i2", Library.TYPE_BUF | Library.FLAG_INPUT);
		MutableCell o0 = g.new MutableCell("o0", Library.TYPE_BUF | Library.FLAG_OUTPUT);
		i0.setIntfPosition(0);
		i1.setIntfPosition(1);
		i2.setIntfPosition(2);
		o0.setIntfPosition(3);
		MutableCell n = g.new MutableCell("n", LibrarySAED.TYPE_OAI21);
		g.connect(i0, -1, n, 0);
		g.connect(i1, -1, n, 1);
		g.connect(i2, -1, n, 2);
		g.connect(n, -1, o0, 0);
		MutableCircuit g2 = CircuitTools.parseBench("input(i0) input(i1) input(i2) output(o0) s0=OR(i0,i1) o0=NAND(i2,s0)");

		QVSource pat = QVSource.from(QBSource.random(4, 42));
		QVSource tst = QVSource.from(new QBPlainSim(g, QBSource.random(4, 42)));
		QVSource ref = QVSource.from(new QBPlainSim(g2, QBSource.random(4, 42)));

		// simulate 128 random patterns and compare the responses.
		for (int i = 0; i < 128; i++) {
			assertEqualsReport(ref.next(), tst.next(), pat.next(), i, g.accessInterface());
		}
	}

	@Test
	public void testNorInv() {
		MutableCircuit g = CircuitTools.parseBench("input(a) input(b) output(nor) output(inv) nor=NOR(a,b) inv=NOT(a)");
		int length = g.accessInterface().length;

		QVSource pat = QVSource.from(QBSource.random(length, 42));
		QVSource sim = QVSource.from(new QBPlainSim(g, QBSource.random(length, 42)));
		QVSource ref = QVSource.from(new QBSource(length) {
			private QBSource rand = QBSource.random(length(), 42);

			public void reset() {
				rand.reset();
			}

			protected QBlock compute() {
				long cv[] = new long[2];
				long l = 0L;
				long k = 0L;
				long j = 0L;
				QBlock output = rand.next();
				for (int i = 0; i < 2; i++) {
					// log.debug("v " + i + " " +
					// StringTools.longToReadableBinaryString(output.getV(i)));
					// log.debug("c " + i + " " +
					// StringTools.longToReadableBinaryString(output.getC(i)));
					l |= ~output.getC(i) & output.getV(i);
					k |= output.getC(i) & output.getV(i);
					j |= ~output.getC(i) & ~output.getV(i);
				}
				cv[0] = -1L;
				cv[1] = -1L;
				cv[0] &= ~j;
				cv[1] &= ~j;
				cv[0] |= k;
				cv[1] &= ~k;
				cv[0] &= ~l;
				cv[1] |= l;

				output.set(2, cv[1], cv[0]);
				output.set(3, (output.getV(0) ^ output.getC(0)), output.getC(0));
				// output.set(0, 0, 0);
				// output.set(1, 0, 0);
				// log.debug("out3 set V: " +
				// StringTools.longToReadableBinaryString(output.getV(3)));
				// log.debug("out3 set C: " +
				// StringTools.longToReadableBinaryString(output.getC(3)));
				return output;
			}
		});

		// simulate 128 random patterns and compare the responses.
		for (int i = 0; i < 128; i++) {
			assertEqualsReport(ref.next(), sim.next(), pat.next(), i, g.accessInterface());
		}
	}

	@Test
	public void testS27() throws Exception {
		Library l = new LibrarySAED();
		MutableCircuit g = CircuitTools.loadCircuit(RuntimeTools.KYUPI_HOME + "/testdata/SAED90/s27.v", l);
		FormatStil p = new FormatStil(RuntimeTools.KYUPI_HOME + "/testdata/s27.stil", g);
		QVSource tests = p.getStimuliSource();
		QVSource resp = p.getResponsesSource();

		for (MutableCell n : g.accessInterface()) {
			if (n == null)
				continue;
			StringBuffer buf = new StringBuffer();
			if (n.isOutput())
				buf.append("o");
			if (n.isInput())
				buf.append("i");
			if (n.isSequential())
				buf.append("s");

			// log.debug("intf " + buf.toString() + " " + n.queryName());
		}

		ArrayList<QVector> ta = tests.toArrayList();
		ArrayList<QVector> ra = resp.toArrayList();

		QVSource sim = new QVPlainSim(g, tests);

		ArrayList<QVector> sa = sim.toArrayList();

		assertEquals(ta.size(), ra.size());
		assertEquals(64, sa.size());

		// for (int idx = 0; idx < ta.size(); idx++) {
		// log.debug("--------------------");
		// log.debug("test " + ta.get(idx));
		// log.debug("exp " + ra.get(idx));
		// log.debug("sim " + sa.get(idx));
		// }
	}

	@Test
	public void testC17Nangate() throws Exception {
		MutableCircuit g_ref = CircuitTools.loadCircuit(new File(RuntimeTools.KYUPI_HOME, "testdata/c17.isc"), new Library());
		MutableCircuit g_test = CircuitTools.loadCircuit(new File(RuntimeTools.KYUPI_HOME, "testdata/Nangate/c17.v"),
				new LibraryNangate());
		assertEqualsByRandomSimulation(g_ref, g_test);
	}

	@Test
	public void testC17Saed90() throws Exception {
		MutableCircuit g_ref = CircuitTools.loadCircuit(new File(RuntimeTools.KYUPI_HOME, "testdata/c17.isc"), new Library());
		MutableCircuit g_test = CircuitTools.loadCircuit(new File(RuntimeTools.KYUPI_HOME, "testdata/SAED90/c17.v"),
				new LibrarySAED());
		assertEqualsByRandomSimulation(g_ref, g_test);
	}

	@Test
	public void testAllSaed90() throws Exception {
		MutableCircuit g_ref = CircuitTools.loadCircuit(new File(RuntimeTools.KYUPI_HOME, "testdata/SAED90/SAED90norinv.v"),
				new LibrarySAED());
		MutableCircuit g_test = CircuitTools.loadCircuit(new File(RuntimeTools.KYUPI_HOME, "testdata/SAED90/SAED90cells.v"),
				new LibrarySAED());
		CircuitTools.splitMultiOutputCells(g_test);
		assertEqualsByRandomSimulation(g_ref, g_test);
	}

	@Test
	public void testB13Transition() throws Exception {
		MutableCircuit graph = CircuitTools.loadCircuit(new File(RuntimeTools.KYUPI_HOME, "testdata/SAED90/b13.v"),
				new LibrarySAED());
		FormatStil stil = new FormatStil(new File(RuntimeTools.KYUPI_HOME, "testdata/SAED90/b13_trans.stil"), graph);
		ArrayList<QVector> stimuli = stil.getStimuliArray();
		ArrayList<QVector> responses = stil.getResponsesArray();

		MutableCell[] intf = graph.accessInterface();

		// transition patterns: simulate each scan load for two clock cycles.
		QBSource launch = new QBPlainSim(graph, QBSource.from(intf.length, stimuli));
		QVSource capture = QVSource.from(new QBPlainSim(graph, launch));

		// this causes simulation of all vectors:
		ArrayList<QVector> simresult = capture.toArrayList();

		// only compare scan state as stil files don't contain expects for POs.
		QVector mask = new QVector(intf.length);
		for (int i = 0; i < intf.length; i++) {
			if (intf[i] != null && (intf[i].isSequential()))
				mask.setValue(i, '1');
		}

		int errors = 0;
		for (int i = 0; i < stimuli.size(); i++) {
			QVector exp = new QVector(responses.get(i)).and(mask);
			QVector sim = new QVector(simresult.get(i)).and(mask);

			if (!exp.equals(sim)) {
				log.error("---Validation error---------");
				log.error("stimuli  : " + stimuli.get(i));
				log.error("expected : " + responses.get(i));
				log.error("capture  : " + simresult.get(i));
				errors++;
			}
		}
		assertEquals(0, errors);
	}

	private void assertEqualsByRandomSimulation(MutableCircuit g_ref, MutableCircuit g_test) {
		int length = g_ref.accessInterface().length;
		assertEquals(length, g_test.accessInterface().length);

		QVSource pat = QVSource.from(QBSource.random(length, 42));
		QVSource ref = QVSource.from(new QBPlainSim(g_ref, QBSource.random(length, 42)));
		QVSource test = QVSource.from(new QBPlainSim(g_test, QBSource.random(length, 42)));

		// simulate 128 random patterns and compare the responses.
		for (int i = 0; i < 128; i++) {
			assertEqualsReport(ref.next(), test.next(), pat.next(), i, g_ref.accessInterface());
		}
	}

	private void assertEqualsReport(QVector expected, QVector actual, QVector inp, int pindex, MutableCell[] intf) {
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
			fail("Mismatched pattern " + pindex + "(" + inp + "): " + actual + "(exp:" + expected + ")"
					+ buf.toString());
		}
		expected.free();
		actual.free();
	}
}
