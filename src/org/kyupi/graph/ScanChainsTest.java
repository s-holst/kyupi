package org.kyupi.graph;

import java.io.File;

import static org.junit.Assert.*;

import org.junit.Test;
import org.kyupi.graph.Graph.Node;
import org.kyupi.graph.ScanChains.ScanChain;
import org.kyupi.misc.RuntimeTools;


public class ScanChainsTest {

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
		assertEquals(sc0.intfPosition(), map[0][sc0.intfPosition()]);
		assertEquals(sc1.intfPosition(), map[0][sc1.intfPosition()]);
		assertEquals(sc2.intfPosition(), map[0][sc2.intfPosition()]);

		// completely empty in the last vector
		assertEquals(-1, map[3][sc0.intfPosition()]);
		assertEquals(-1, map[3][sc1.intfPosition()]);
		assertEquals(-1, map[3][sc2.intfPosition()]);

		// ScanIn port is unassigned
		assertEquals(-1, map[2][chain.in.node.intfPosition()]);
		assertEquals(-1, map[1][chain.in.node.intfPosition()]);
		assertEquals(-1, map[0][chain.in.node.intfPosition()]);
		
		// shifted correctly
		assertEquals(map[1][sc0.intfPosition()], map[2][sc1.intfPosition()]);
		assertEquals(map[0][sc0.intfPosition()], map[1][sc1.intfPosition()]);
		assertEquals(map[1][sc1.intfPosition()], map[2][sc2.intfPosition()]);
		assertEquals(map[0][sc1.intfPosition()], map[1][sc2.intfPosition()]);

		//System.out.println(mapToString(map));

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
		assertEquals(sc0.intfPosition(), map[3][sc0.intfPosition()]);
		assertEquals(sc1.intfPosition(), map[3][sc1.intfPosition()]);
		assertEquals(sc2.intfPosition(), map[3][sc2.intfPosition()]);

		// completely empty in the first vector
		assertEquals(-1, map[0][sc0.intfPosition()]);
		assertEquals(-1, map[0][sc1.intfPosition()]);
		assertEquals(-1, map[0][sc2.intfPosition()]);

		// scan-in data appears on ScanIn port
		assertEquals(sc0.intfPosition(), map[2][chain.in.node.intfPosition()]);
		assertEquals(sc1.intfPosition(), map[1][chain.in.node.intfPosition()]);
		assertEquals(sc2.intfPosition(), map[0][chain.in.node.intfPosition()]);
		
		// shifted correctly
		assertEquals(map[2][sc0.intfPosition()], map[3][sc1.intfPosition()]);
		assertEquals(map[1][sc0.intfPosition()], map[2][sc1.intfPosition()]);
		assertEquals(map[2][sc1.intfPosition()], map[3][sc2.intfPosition()]);
		assertEquals(map[1][sc1.intfPosition()], map[2][sc2.intfPosition()]);

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
