package org.kyupi.graph;

import java.io.File;

import org.junit.Test;
import org.kyupi.graph.Graph.Node;
import org.kyupi.graph.ScanChains.ScanChain;
import org.kyupi.misc.RuntimeTools;

import junit.framework.TestCase;

public class ScanChainsTest extends TestCase {

	@Test
	public void testScanOutMapping() throws Exception {
		Graph graph = GraphTools.loadGraph(new File(RuntimeTools.KYUPI_HOME, "testdata/SAED90/s27.v"), new LibrarySAED());
		ScanChains chains = new ScanChains(graph);
		assertEquals(1, chains.size());
		ScanChain chain = chains.get(0);
		assertNotNull(chain);
		assertEquals(3, chain.cells.size());
		assertEquals("Scan_In", chain.in.node.queryName());
		int[][] map = chains.scanOutMapping();
		assertEquals(4, map.length);
		assertEquals(graph.accessInterface().length, map[0].length);
		
		Node sc0 = chain.cells.get(0).node;
		Node sc1 = chain.cells.get(1).node;
		Node sc2 = chain.cells.get(2).node;
		
		// fully loaded in the first vector
		assertEquals(sc0.position(), map[0][sc0.position()]);
		assertEquals(sc1.position(), map[0][sc1.position()]);
		assertEquals(sc2.position(), map[0][sc2.position()]);

		// completely empty in the last vector
		assertEquals(-1, map[3][sc0.position()]);
		assertEquals(-1, map[3][sc1.position()]);
		assertEquals(-1, map[3][sc2.position()]);

		// ScanIn port is unassigned
		assertEquals(-1, map[2][chain.in.node.position()]);
		assertEquals(-1, map[1][chain.in.node.position()]);
		assertEquals(-1, map[0][chain.in.node.position()]);
		
		// shifted correctly
		assertEquals(map[1][sc0.position()], map[2][sc1.position()]);
		assertEquals(map[0][sc0.position()], map[1][sc1.position()]);
		assertEquals(map[1][sc1.position()], map[2][sc2.position()]);
		assertEquals(map[0][sc1.position()], map[1][sc2.position()]);

		System.out.println(mapToString(map));

	}
	
	@Test
	public void testScanInMapping() throws Exception {
		Graph graph = GraphTools.loadGraph(new File(RuntimeTools.KYUPI_HOME, "testdata/SAED90/s27.v"), new LibrarySAED());
		ScanChains chains = new ScanChains(graph);
		assertEquals(1, chains.size());
		ScanChain chain = chains.get(0);
		assertNotNull(chain);
		assertEquals(3, chain.cells.size());
		assertEquals("Scan_In", chain.in.node.queryName());
		int[][] map = chains.scanInMapping();
		assertEquals(4, map.length);
		assertEquals(graph.accessInterface().length, map[0].length);
		
		Node sc0 = chain.cells.get(0).node;
		Node sc1 = chain.cells.get(1).node;
		Node sc2 = chain.cells.get(2).node;
		
		// fully loaded in the last vector
		assertEquals(sc0.position(), map[3][sc0.position()]);
		assertEquals(sc1.position(), map[3][sc1.position()]);
		assertEquals(sc2.position(), map[3][sc2.position()]);

		// completely empty in the first vector
		assertEquals(-1, map[0][sc0.position()]);
		assertEquals(-1, map[0][sc1.position()]);
		assertEquals(-1, map[0][sc2.position()]);

		// scan-in data appears on ScanIn port
		assertEquals(sc0.position(), map[2][chain.in.node.position()]);
		assertEquals(sc1.position(), map[1][chain.in.node.position()]);
		assertEquals(sc2.position(), map[0][chain.in.node.position()]);
		
		// shifted correctly
		assertEquals(map[2][sc0.position()], map[3][sc1.position()]);
		assertEquals(map[1][sc0.position()], map[2][sc1.position()]);
		assertEquals(map[2][sc1.position()], map[3][sc2.position()]);
		assertEquals(map[1][sc1.position()], map[2][sc2.position()]);

		//System.out.println(mapToString(map));

	}
	public String mapToString(int[][] map) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[i].length; j++) {
				buf.append("\t" + map[i][j]);
			}
			buf.append("\n");
		}
		return buf.toString();
	}

}
