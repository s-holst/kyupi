package org.kyupi.sim;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Random;

import org.junit.Test;
import org.kyupi.graph.Graph;
import org.kyupi.graph.Graph.Node;
import org.kyupi.graph.GraphTools;
import org.kyupi.graph.Library;
import org.kyupi.misc.RuntimeTools;
import org.kyupi.sim.CombLogicSim.State;

public class CombLogicSimTest {

	private void stateSet(State state, Graph circuit, String node, long value, long care) {
		Node n = circuit.searchNode(node);
		int signalIdx = circuit.accessSignalMap().idxForOutput(n, 0);
		state.set(signalIdx, value, care);
	}
	
	private long stateGetV(State state, Graph circuit, String node) {
		Node n = circuit.searchNode(node);
		int signalIdx = circuit.accessSignalMap().idxForOutput(n, 0);
		return state.getV(signalIdx);
	}

	private long stateGetC(State state, Graph circuit, String node) {
		Node n = circuit.searchNode(node);
		int signalIdx = circuit.accessSignalMap().idxForOutput(n, 0);
		return state.getC(signalIdx);
	}
	
	@Test
	public void testC17() throws Exception {
		Graph circuit = GraphTools.loadGraph(new File(RuntimeTools.KYUPI_HOME, "testdata/c17.isc"), new Library());
		CombLogicSim sim = new CombLogicSim(circuit);
		State state = sim.new State();
		
		System.out.println(circuit.toString());
		
		// assign all inputs
		state.setStimulus(0, 1L, -1L); // 1gat
		state.setStimulus(1, 0L, -1L); // 2gat
		state.setStimulus(2, 1L, -1L); // 3gat
		state.setStimulus(3, 0L, -1L); // 6gat
		state.setStimulus(4, 0L, -1L); // 7gat

		stateSet(state, circuit, "1gat", 1, 1);
		stateSet(state, circuit, "2gat", 0, 1);
		stateSet(state, circuit, "3gat", 1, 1);
		stateSet(state, circuit, "6gat", 0, 1);
		stateSet(state, circuit, "7gat", 0, 1);

		state.propagate();
		
		
		assertEquals(1, stateGetV(state, circuit, "22gat")); // output 1
		assertEquals(1, stateGetC(state, circuit, "22gat")); // output 1
		assertEquals(0, stateGetV(state, circuit, "23gat")); // output 2
		assertEquals(1, stateGetC(state, circuit, "23gat")); // output 2
		
		state.clear();
		
		assertEquals(0, stateGetV(state, circuit, "1gat")); // input 1
		assertEquals(0, stateGetC(state, circuit, "1gat")); // input 1
		assertEquals(0, stateGetV(state, circuit, "22gat")); // output 1
		assertEquals(0, stateGetC(state, circuit, "22gat")); // output 1
		
		Random r = new Random();
		for (int i = 0; i < 10; i++) {
			long s1 = r.nextLong();
			long s2 = r.nextLong();
			long s3 = r.nextLong();
			long s6 = r.nextLong();
			long s7 = r.nextLong();
			
			//stateSet(state, circuit, "1gat", i1, -1L);
			//stateSet(state, circuit, "2gat", i2, -1L);
			//stateSet(state, circuit, "3gat", i3, -1L);
			//stateSet(state, circuit, "6gat", i4, -1L);
			//stateSet(state, circuit, "7gat", i5, -1L);
			
			state.setStimulus(0, s1, -1L); // 1gat
			state.setStimulus(1, s2, -1L); // 2gat
			state.setStimulus(2, s3, -1L); // 3gat
			state.setStimulus(3, s6, -1L); // 6gat
			state.setStimulus(4, s7, -1L); // 7gat

			state.propagate();
			
			long s10 = ~(s1 & s3);
			
			assertEquals(s10, stateGetV(state, circuit, "10gat"));

			long s11 = ~(s3 & s6);
			long s16 = ~(s2 & s11);
			long s19 = ~(s11 & s7);
			long s22 = ~(s10 & s16);
			long s23 = ~(s16 & s19);
			
			assertEquals(s22, state.getResponseV(5));
			assertEquals(-1L, state.getResponseC(5));
			assertEquals(s23, state.getResponseV(6));
			assertEquals(-1L, state.getResponseC(6));
			
			state.clear();
		}
	}
	
	@Test
	public void testSequential() {
		Graph circuit = GraphTools.benchToGraph("INPUT(a) OUTPUT(z1) OUTPUT(z2) z1=DFF(a) z2=DFF(z1)");
		CombLogicSim sim = new CombLogicSim(circuit);
		State state = sim.new State();

		state.setStimulus(0, 1L, -1L); // INPUT(a) = 1
		state.setStimulus(3, 2L, -1L); // z1=DFF(a) = 2
		state.setStimulus(4, 4L, -1L); // z2=DFF(z1) = 4
		
		state.propagate();
		
		assertEquals(1L, state.getStimulusV(0)); // INPUT(a) = 1
		assertEquals(2L, stateGetV(state, circuit, "z1_")); // output of DFF
		
		assertEquals(2L, state.getResponseV(1)); // OUTPUT(z1) = 2
		assertEquals(4L, state.getResponseV(2)); // OUTPUT(z2) = 4
		
		assertEquals(1L, state.getResponseV(3)); // z1=DFF(a), new state of DFF = 1
		assertEquals(2L, state.getResponseV(4)); // z2=DFF(z1), new state of DFF = 2
		
		assertEquals(2L, state.getStimulusV(3)); // z1=DFF(a), old state of DFF = 1
		assertEquals(4L, state.getStimulusV(4)); // z2=DFF(z1), old state of DFF = 2

		state.reapply(); // simulate another clock cycle
		
		assertEquals(1L, state.getStimulusV(3)); // z1=DFF(a), new state of DFF = 1
		assertEquals(2L, state.getStimulusV(4)); // z2=DFF(z1), new state of DFF = 2

		state.propagate(); 

		assertEquals(1L, state.getResponseV(1)); // OUTPUT(z1) = 1
		assertEquals(2L, state.getResponseV(2)); // OUTPUT(z2) = 2
		
		assertEquals(1L, state.getResponseV(3)); // z1=DFF(a), new state of DFF = 1
		assertEquals(1L, state.getResponseV(4)); // z2=DFF(z1), new state of DFF = 1

		
	}
	
	@Test
	public void testGates() {
		Graph circuit = GraphTools.benchToGraph("INPUT(a) INPUT(b) OUTPUT(nand) OUTPUT(and) nand=NAND(a,b) and=NOT(nand)");
		CombLogicSim sim = new CombLogicSim(circuit);
		State state = sim.new State();
		
		assertEquals(0L, state.getStimulusV(0));
		assertEquals(0L, state.getStimulusC(0));
		assertEquals(0L, state.getStimulusV(1));
		assertEquals(0L, state.getStimulusC(1));
		
		state.setStimulus(0, 0b0101L, -1L); // a
		state.setStimulus(1, 0b1100L, -1L); // b

		assertEquals(0b0101L, state.getStimulusV(0));
		assertEquals(-1L, state.getStimulusC(0));
		assertEquals(0b1100L, state.getStimulusV(1));
		assertEquals(-1L, state.getStimulusC(1));

		state.propagate();
		
		Node n = circuit.searchNode("nand_");
		
		assertNotNull(n);
		
		int sigA = circuit.accessSignalMap().idxForInput(n, 0);
		int sigB = circuit.accessSignalMap().idxForInput(n, 1);
		
		assertEquals(0b0101L, state.getV(sigA));
		assertEquals(-1L, state.getC(sigA));

		assertEquals(0b1100L, state.getV(sigB));
		assertEquals(-1L, state.getC(sigB));

		int sigZ = circuit.accessSignalMap().idxForOutput(n, 0);

		assertEquals(~(0b0101L & 0b1100L), state.getV(sigZ));
		assertEquals(-1L, state.getC(sigZ));
		
		assertEquals(~(0b0101L & 0b1100L), state.getResponseV(2)); // nand out
		assertEquals(-1L, state.getResponseC(2));

		assertEquals((0b0101L & 0b1100L), state.getResponseV(3)); // and out
		assertEquals(-1L, state.getResponseC(3));
}

}
