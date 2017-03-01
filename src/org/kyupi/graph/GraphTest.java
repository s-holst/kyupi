/*
 * Copyright 2013 The KyuPI project contributors. See the COPYRIGHT.md file
 * at the top-level directory of this distribution.
 * This file is part of the KyuPI project. It is subject to the license terms
 * in the LICENSE.md file found in the top-level directory of this distribution.
 * No part of the KyuPI project, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.md file.
 */
package org.kyupi.graph;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.kyupi.graph.Graph.Node;
import org.kyupi.misc.RuntimeTools;

public class GraphTest extends TestCase {

	private static Logger log = Logger.getLogger(GraphTest.class);

	@Test
	public void testGate() {
		Graph c = new Graph(new Library());
		Node input_port = c.new Node("in", Library.TYPE_BUF | Library.FLAG_INPUT);
		assertEquals(0, input_port.countIns());
		assertEquals(0, input_port.countOuts());
		Node and_gate = c.new Node("and", Library.TYPE_AND);
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
		Graph g = GraphTools.benchToGraph("INPUT(a) OUTPUT(z) z=DFF(a)");
		log.debug("Graph=\n" + g);
		Node intf[] = g.accessInterface();

		assertTrue(intf[0].isInput());
		assertFalse(intf[1].isInput());
		//assertTrue(intf[2].isInput());

		assertFalse(intf[0].isOutput());
		assertTrue(intf[1].isOutput());
		//assertTrue(intf[2].isOutput());

		assertFalse(intf[0].isPseudo());
		assertFalse(intf[1].isPseudo());
		//assertTrue(intf[2].isPseudo());

		assertTrue(intf[0].isPort());
		assertTrue(intf[1].isPort());
		//assertFalse(intf[2].isPort());
	}

	@Test
	public void testLoadC17() throws Exception {
		OutputStream os = new ByteArrayOutputStream();

		Library lib = new Library();

		Graph c = GraphTools.loadGraph(new File(RuntimeTools.KYUPI_HOME, "testdata/c17.isc"), lib);
		assertEquals(13, c.countNodes());
		assertEquals(5, c.countInputs());
		assertEquals(2, c.countOutputs());
		FormatDOT.save(os, c);

		lib = new LibraryNangate();

		c = GraphTools.loadGraph(new File(RuntimeTools.KYUPI_HOME, "testdata/Nangate/c17.vhdl"), lib);
		assertEquals(15, c.countNodes());
		assertEquals(5, c.countInputs());
		assertEquals(2, c.countOutputs());
		FormatDOT.save(os, c);

		c = GraphTools.loadGraph(new File(RuntimeTools.KYUPI_HOME, "testdata/Nangate/c17.v"), lib);
		assertEquals(15, c.countNodes());
		assertEquals(5, c.countInputs());
		assertEquals(2, c.countOutputs());
		FormatDOT.save(os, c);

		lib = new LibrarySAED();

		c = GraphTools.loadGraph(new File(RuntimeTools.KYUPI_HOME, "testdata/SAED90/c17.v"), lib);
		assertEquals(11, c.countNodes());
		assertEquals(5, c.countInputs());
		assertEquals(2, c.countOutputs());
		FormatDOT.save(os, c);
	}

	@Test
	public void testLoadS27Saed90() throws Exception {
		Graph c = GraphTools.loadGraph(new File(RuntimeTools.KYUPI_HOME, "testdata/SAED90/s27.v"), new LibrarySAED());
		assertEquals(122, c.countNodes());
		assertEquals(8, c.countInputs());
		assertEquals(2, c.countOutputs());
		GraphTools.replaceScanCellsWithPseudoPorts(c);
		//File f = File.createTempFile("s27_scan_removed", ".dot");
		//FormatDOT.save(FileTools.fileCreate(f, true), c);
		//log.debug("wrote dot to " + f.getPath());
	}

	@Test
	public void testLoadSAED90cells() throws Exception {
		Graph c = GraphTools.loadGraph(new File(RuntimeTools.KYUPI_HOME, "testdata/SAED90/SAED90cells.v"), new LibrarySAED());
		assertEquals(257, c.countNodes());
		assertEquals(15, c.countInputs());
		assertEquals(126, c.countOutputs());
		c.levels();
	}

	@Test
	public void testLoadB01Scan() throws Exception {
		Graph c = GraphTools.loadGraph(new File(RuntimeTools.KYUPI_HOME, "testdata/SAED90/b01.v"), new LibrarySAED());
		c.levels();
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
				log.debug("load: " + pth.toString());
				GraphTools.loadGraph(pth.toFile(), lib);
			}
		}
	}

}
