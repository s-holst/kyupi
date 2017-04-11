/*
 * Copyright 2017 The KyuPI project contributors. See the COPYRIGHT.md file
 * at the top-level directory of this distribution.
 * This file is part of the KyuPI project. It is subject to the license terms
 * in the LICENSE.md file found in the top-level directory of this distribution.
 * No part of the KyuPI project, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.md file.
 */
package org.kyupi.sim;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Random;

import org.junit.Test;
import org.kyupi.graph.Graph;
import org.kyupi.graph.GraphTools;
import org.kyupi.graph.Library;
import org.kyupi.misc.RuntimeTools;
import org.kyupi.sim.Simulator.State;

public class SimulatorTest {

	private void stateSet(State state, Graph circuit, String node, long value, long care) {
		state.set(circuit.searchNode(node).level(), circuit.searchNode(node).levelPosition(), value, care);
	}
	
	private long stateGetV(State state, Graph circuit, String node) {
		return state.getV(circuit.searchNode(node).level(), circuit.searchNode(node).levelPosition());
	}

	private long stateGetC(State state, Graph circuit, String node) {
		return state.getC(circuit.searchNode(node).level(), circuit.searchNode(node).levelPosition());
	}
	
	@Test
	public void test() throws Exception {
		Graph circuit = GraphTools.loadGraph(new File(RuntimeTools.KYUPI_HOME, "testdata/c17.isc"), new Library());
		Simulator sim = new Simulator(circuit);
		State state = sim.new State();
		
		// assign all inputs
		stateSet(state, circuit, "1gat", 1, 1);
		stateSet(state, circuit, "2gat", 0, 1);
		stateSet(state, circuit, "3gat", 1, 1);
		stateSet(state, circuit, "6gat", 0, 1);
		stateSet(state, circuit, "7gat", 0, 1);

		state.simulate();
		
		
		assertEquals(1, stateGetV(state, circuit, "22gat_out")); // output 1
		assertEquals(1, stateGetC(state, circuit, "22gat_out")); // output 1
		assertEquals(0, stateGetV(state, circuit, "23gat_out")); // output 2
		assertEquals(1, stateGetC(state, circuit, "23gat_out")); // output 2
		
		state.clear();
		
		assertEquals(0, stateGetV(state, circuit, "1gat")); // input 1
		assertEquals(0, stateGetC(state, circuit, "1gat")); // input 1
		assertEquals(0, stateGetV(state, circuit, "22gat_out")); // output 1
		assertEquals(0, stateGetC(state, circuit, "22gat_out")); // output 1
		
		Random r = new Random();
		for (int i = 0; i < 10; i++) {
			long i1 = r.nextLong();
			long i2 = r.nextLong();
			long i3 = r.nextLong();
			long i4 = r.nextLong();
			long i5 = r.nextLong();
			
			stateSet(state, circuit, "1gat", i1, -1L);
			stateSet(state, circuit, "2gat", i2, -1L);
			stateSet(state, circuit, "3gat", i3, -1L);
			stateSet(state, circuit, "6gat", i4, -1L);
			stateSet(state, circuit, "7gat", i5, -1L);
			
			state.simulate();
			
			long s1 = ~(i1 & i3);
			long s2 = ~(i3 & i4);
			long s3 = ~(i2 & s2);
			long s4 = ~(s2 & i5);
			long o1 = ~(s1 & s3);
			long o2 = ~(s3 & s4);
			
			assertEquals(o1, stateGetV(state, circuit, "22gat_out"));
			assertEquals(-1L, stateGetC(state, circuit, "22gat_out"));
			assertEquals(o2, stateGetV(state, circuit, "23gat_out"));
			assertEquals(-1L, stateGetC(state, circuit, "23gat_out"));
			
			state.clear();
		}
	}
	
	@Test
	public void testSequential() {
		Graph circuit = GraphTools.benchToGraph("INPUT(a) OUTPUT(z1) OUTPUT(z2) z1=DFF(a) z2=DFF(z1)");
		Simulator sim = new Simulator(circuit);
		State state = sim.new State();
		
		state.setStimulus(3, 2L, -1L); // z1=DFF(a)
		state.setStimulus(4, 4L, -1L); // z2=DFF(z1)
		state.setStimulus(0, 1L, -1L); // INPUT(a)
		
		state.simulate();
		
		assertEquals(1L, state.getStimulusV(0)); // INPUT(a)
		assertEquals(2L, state.getResponseV(1)); // OUTPUT(z1)
		assertEquals(4L, state.getResponseV(2)); // OUTPUT(z2)
		assertEquals(1L, state.getResponseV(3)); // z1=DFF(a)
		assertEquals(2L, state.getResponseV(4)); // z2=DFF(z1)
		
		
	}

}
