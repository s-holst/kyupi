package org.kyupi.circuit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.Test;
import org.kyupi.circuit.ScanChains.ScanChain;
import org.kyupi.misc.RuntimeTools;


public class ScanChainsTest {

	@Test
	public void testScanOutMapping() throws Exception {
		LevelizedCircuit graph = CircuitTools.loadCircuit(new File(RuntimeTools.KYUPI_HOME, "testdata/SAED90/s27.v"), new LibrarySAED90()).levelized();
		ScanChains chains = new ScanChains(graph);
		assertEquals(1, chains.size());
		ScanChain chain = chains.get(0);
		assertNotNull(chain);
		assertEquals(3, chain.cells.size());
		assertEquals("Scan_In", chain.in.node.name());
		int[][] map = chains.scanOutMapping();
		assertEquals(4, map.length);
		assertEquals(graph.width(), map[0].length);
		
		Cell sc0 = chain.cells.get(0).node;
		Cell sc1 = chain.cells.get(1).node;
		Cell sc2 = chain.cells.get(2).node;
		
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
		LevelizedCircuit graph = CircuitTools.loadCircuit(new File(RuntimeTools.KYUPI_HOME, "testdata/SAED90/s27.v"), new LibrarySAED90()).levelized();
		ScanChains chains = new ScanChains(graph);
		assertEquals(1, chains.size());
		ScanChain chain = chains.get(0);
		assertNotNull(chain);
		assertEquals(3, chain.cells.size());
		assertEquals("Scan_In", chain.in.node.name());
		int[][] map = chains.scanInMapping();
		assertEquals(4, map.length);
		assertEquals(graph.width(), map[0].length);
		
		Cell sc0 = chain.cells.get(0).node;
		Cell sc1 = chain.cells.get(1).node;
		Cell sc2 = chain.cells.get(2).node;
		
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
	
	@Test
	public void testMultiChain() throws Exception {
		LevelizedCircuit graph = CircuitTools.loadCircuit(new File(RuntimeTools.KYUPI_HOME, "testdata/SAED90/multichain.v"), new LibrarySAED90()).levelized();
		ScanChains chains = new ScanChains(graph);
		assertEquals(2, chains.size());
		
		ScanChain chain0 = chains.get(0);
		assertNotNull(chain0);
		assertEquals(3, chain0.cells.size());
		assertEquals("test_si000", chain0.in.node.name());
		
		ScanChain chain1 = chains.get(1);
		assertNotNull(chain1);
		assertEquals(2, chain1.cells.size());
		assertEquals("test_si001", chain1.in.node.name());
		
		int[][] map = chains.scanInMapping();
		//System.out.println(mapToString(map));
		
		Cell cell000_00 = chain0.cells.get(0).node;
		Cell cell000_01 = chain0.cells.get(1).node;
		Cell cell000_02 = chain0.cells.get(2).node;
		Cell cell001_00 = chain1.cells.get(0).node;
		Cell cell001_01 = chain1.cells.get(1).node;

		Cell test_si000 = chain0.in.node;
		Cell test_si001 = chain1.in.node;
		
		assertEquals(cell000_02.intfPosition(), map[0][test_si000.intfPosition()]);
		assertEquals(-1, map[0][test_si001.intfPosition()]);
		assertEquals(cell000_01.intfPosition(), map[1][test_si000.intfPosition()]);
		assertEquals(cell001_01.intfPosition(), map[1][test_si001.intfPosition()]);
		
		map = chains.scanOutMapping();
		
		//System.out.println(mapToString(map));

		assertEquals(cell000_00.intfPosition(), map[2][cell000_02.intfPosition()]);
		assertEquals(-1, map[2][cell001_01.intfPosition()]);
		assertEquals(cell001_00.intfPosition(), map[1][cell001_01.intfPosition()]);

		
	}
	
	@Test
	public void testMultiChainMultiClock() throws Exception {
		LevelizedCircuit graph = CircuitTools.loadCircuit(new File(RuntimeTools.KYUPI_HOME, "testdata/SAED90/multichain.v"), new LibrarySAED90()).levelized();
		ScanChains chains = new ScanChains(graph);
		assertEquals(2, chains.size());
		
		ScanChain chain0 = chains.get(0);
		assertNotNull(chain0);
		assertEquals(3, chain0.cells.size());
		assertEquals("test_si000", chain0.in.node.name());
		
		ScanChain chain1 = chains.get(1);
		assertNotNull(chain1);
		assertEquals(2, chain1.cells.size());
		assertEquals("test_si001", chain1.in.node.name());
		
		int[] clocking = new int[2];
		clocking[0] = 0;
		clocking[1] = 1;
		
		int[][] map = chains.scanInMapping(clocking);
		System.out.println(mapToString(map));
		
		Cell cell000_00 = chain0.cells.get(0).node;
		Cell cell000_01 = chain0.cells.get(1).node;
		Cell cell000_02 = chain0.cells.get(2).node;
		Cell cell001_00 = chain1.cells.get(0).node;
		Cell cell001_01 = chain1.cells.get(1).node;

		Cell test_si000 = chain0.in.node;
		Cell test_si001 = chain1.in.node;
		
		assertEquals(cell000_02.intfPosition(), map[0][test_si000.intfPosition()]);
		assertEquals(-1, map[0][test_si001.intfPosition()]);
		assertEquals(cell000_01.intfPosition(), map[1][test_si000.intfPosition()]);
		assertEquals(cell001_01.intfPosition(), map[2][test_si001.intfPosition()]);
		
		map = chains.scanOutMapping(clocking);
		
		System.out.println(mapToString(map));

		assertEquals(cell000_00.intfPosition(), map[3][cell000_02.intfPosition()]);
		assertEquals(cell000_00.intfPosition(), map[4][cell000_02.intfPosition()]);
		assertEquals(-1, map[4][cell001_01.intfPosition()]);
		assertEquals(cell001_00.intfPosition(), map[3][cell001_01.intfPosition()]);

		
	}
}
