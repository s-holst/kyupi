/*
 * Copyright 2013 The KyuPI project contributors. See the COPYRIGHT.md file
 * at the top-level directory of this distribution.
 * This file is part of the KyuPI project. It is subject to the license terms
 * in the LICENSE.md file found in the top-level directory of this distribution.
 * No part of the KyuPI project, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.md file.
 */
package org.kyupi.circuit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.kyupi.circuit.MutableCircuit.MutableCell;
import org.kyupi.misc.RuntimeTools;

public class CircuitTest {

	//private static Logger log = Logger.getLogger(GraphTest.class);

	@Test
	public void testGate() {
		MutableCircuit c = new MutableCircuit(new Library());
		MutableCell input_port = c.new MutableCell("in", Library.TYPE_BUF | Library.FLAG_INPUT);
		assertEquals(0, input_port.countIns());
		assertEquals(0, input_port.countOuts());
		MutableCell and_gate = c.new MutableCell("and", Library.TYPE_AND);
		assertEquals(-1, and_gate.maxIn());
		c.connect(input_port, -1, and_gate, 0);
		assertEquals(0, input_port.countIns());
		assertEquals(1, input_port.countOuts());
		assertEquals(1, and_gate.countIns());
		assertEquals(0, and_gate.maxIn());
		c.connect(input_port, -1, and_gate, 1);
		assertEquals(2, input_port.countOuts());
		assertEquals(2, and_gate.countIns());
		assertEquals(1, and_gate.maxIn());
		c.connect(input_port, -1, and_gate, 2);
		assertEquals(3, input_port.countOuts());
		assertEquals(3, and_gate.countIns());
		assertEquals(2, and_gate.maxIn());
		c.connect(input_port, -1, and_gate, 6);
		assertEquals(4, input_port.countOuts());
		assertEquals(4, and_gate.countIns());
		assertEquals(6, and_gate.maxIn());
	}

	@Test
	public void testFlags() {
		MutableCircuit g = CircuitTools.parseBench("INPUT(a) OUTPUT(z) z=DFF(a)");
		//log.debug("Graph=\n" + g);

		assertTrue(g.intf(0).isInput());
		assertFalse(g.intf(1).isInput());
		//assertTrue(g.intf(2).isInput());

		assertFalse(g.intf(0).isOutput());
		assertTrue(g.intf(1).isOutput());
		//assertTrue(g.intf(2).isOutput());

		assertFalse(g.intf(0).isPseudo());
		assertFalse(g.intf(1).isPseudo());
		//assertTrue(g.intf(2).isPseudo());

		assertTrue(g.intf(0).isPort());
		assertTrue(g.intf(1).isPort());
		//assertFalse(g.intf(2).isPort());
	}

	@Test
	public void testLoadC17() throws Exception {
		OutputStream os = new ByteArrayOutputStream();

		Library lib = new Library();

		MutableCircuit c = CircuitTools.loadCircuit(new File(RuntimeTools.KYUPI_HOME, "testdata/c17.isc"), lib);
		assertEquals(13, c.countNodes());
		assertEquals(5, c.countInputs());
		assertEquals(2, c.countOutputs());
		FormatDOT.save(os, c.levelized());

		lib = new LibraryNangate();

		c = CircuitTools.loadCircuit(new File(RuntimeTools.KYUPI_HOME, "testdata/Nangate/c17.vhdl"), lib);
		assertEquals(15, c.countNodes());
		assertEquals(5, c.countInputs());
		assertEquals(2, c.countOutputs());
		FormatDOT.save(os, c.levelized());

		c = CircuitTools.loadCircuit(new File(RuntimeTools.KYUPI_HOME, "testdata/Nangate/c17.v"), lib);
		assertEquals(15, c.countNodes());
		assertEquals(5, c.countInputs());
		assertEquals(2, c.countOutputs());
		FormatDOT.save(os, c.levelized());

		lib = new LibrarySAED();

		c = CircuitTools.loadCircuit(new File(RuntimeTools.KYUPI_HOME, "testdata/SAED90/c17.v"), lib);
		assertEquals(11, c.countNodes());
		assertEquals(5, c.countInputs());
		assertEquals(2, c.countOutputs());
		FormatDOT.save(os, c.levelized());
	}

	@Test
	public void testLoadS27Saed90() throws Exception {
		MutableCircuit c = CircuitTools.loadCircuit(new File(RuntimeTools.KYUPI_HOME, "testdata/SAED90/s27.v"), new LibrarySAED());
		assertEquals(39, c.countNodes());
		assertEquals(8, c.countInputs());
		assertEquals(2, c.countOutputs());
	}

	@Test
	public void testLoadSAED90cells() throws Exception {
		LevelizedCircuit c = CircuitTools.loadCircuit(new File(RuntimeTools.KYUPI_HOME, "testdata/SAED90/SAED90cells.v"), new LibrarySAED()).levelized();
		assertEquals(257, c.size());
		assertEquals(15, c.countInputs());
		assertEquals(126, c.countOutputs());
		c.depth();
	}

	@Test
	public void testLoadB01Scan() throws Exception {
		LevelizedCircuit c = CircuitTools.loadCircuit(new File(RuntimeTools.KYUPI_HOME, "testdata/SAED90/b01.v"), new LibrarySAED()).levelized();
		c.depth();
	}
	
	@Test
	public void testCopy() throws Exception {
		MutableCircuit c = CircuitTools.loadCircuit(new File(RuntimeTools.KYUPI_HOME, "testdata/SAED90/b01.v"), new LibrarySAED());
		MutableCircuit c2 = new MutableCircuit(c);
		assertTrue(c.equals(c2));
	}
	

	@Test
	public void testLoadBenchmarks() throws Exception {
		Path p = Paths.get(RuntimeTools.KYUPI_HOME, "scratch/bench_route");

		if (p.toFile().exists()) {
			DirectoryStream<Path> cont = Files.newDirectoryStream(p, new Filter<Path>() {
				@Override
				public boolean accept(Path entry) throws IOException {
					String p = entry.getFileName().toString();
					return p.endsWith(".v") || p.endsWith(".vg") || p.endsWith(".bz2");
				}
			});

			LibrarySAED lib = new LibrarySAED();
			for (Path pth : cont) {
				//log.debug("load: " + pth.toString());
				CircuitTools.loadCircuit(pth.toFile(), lib);
			}
		}
	}
	
	@Test
	public void testSignals() {
		MutableCircuit g = CircuitTools.parseBench("INPUT(a) INPUT(b) OUTPUT(c) OUTPUT(d) c=AND(a,b) d=BUF(c)");
		assertEquals(6, g.countNodes());
		assertEquals(2, g.countInputs());
		assertEquals(2, g.countOutputs());
		
		//log.info("Graph:\n" + g.toString());
		
		assertEquals(5, g.signalCount());
		MutableCell a = g.searchCellByName("a");
		MutableCell c_ = g.searchCellByName("c_");
		MutableCell c = g.searchCellByName("c");
		MutableCell d_ = g.searchCellByName("d_");
		
		
		assertEquals(a.outputSignalAt(0), c_.inputSignalAt(0));
		assertEquals(c_.outputSignalAt(0), c.inputSignalAt(0));
		assertEquals(c_.outputSignalAt(1), d_.inputSignalAt(0));
		
		assertNotEquals(c_.outputSignalAt(0), d_.inputSignalAt(0));
		assertNotEquals(c_.outputSignalAt(0), c_.inputSignalAt(0));
	}
	
	@Test
	public void testSignalsCommonDriver() {
		MutableCircuit g = CircuitTools.parseBench("INPUT(a) OUTPUT(c) c=AND(a,a)");
		assertEquals(3, g.countNodes());
		assertEquals(1, g.countInputs());
		assertEquals(1, g.countOutputs());
		
		//log.info("Graph:\n" + g.toString());
		
		assertEquals(3, g.signalCount());
		MutableCell a = g.searchCellByName("a");
		MutableCell c_ = g.searchCellByName("c_");
		MutableCell c = g.searchCellByName("c");
		
		assertNotEquals(-1, c_.inputSignalAt(1));
		assertNotEquals(-1, c_.inputSignalAt(0));

		
		assertEquals(a.outputSignalAt(0), c_.inputSignalAt(0));
		assertEquals(a.outputSignalAt(1), c_.inputSignalAt(1));
		assertEquals(c_.outputSignalAt(0), c.inputSignalAt(0));
		
		assertNotEquals(a.outputSignalAt(0), a.outputSignalAt(1));
		assertNotEquals(a.outputSignalAt(0), c.inputSignalAt(0));
		
		g.levelized();
	}

}
