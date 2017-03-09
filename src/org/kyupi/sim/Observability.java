package org.kyupi.sim;

import org.kyupi.data.item.QBlock;
import org.kyupi.graph.Graph;
import org.kyupi.graph.Graph.Node;
import org.kyupi.graph.GraphTools;
import org.kyupi.sim.Simulator.State;

public class Observability {

	private long[][] obs;
	private int[][] obs_rev;
	private int rev;

	private State base;
	private State delta;
	private Graph circuit;

	public Observability(Graph circuit) {
		this.circuit = circuit;
		Simulator sim = new Simulator(circuit);
		base = sim.new State();
		delta = sim.new State(base);
		isSimulated = false;
		obs = GraphTools.allocLong(circuit);
		obs_rev = GraphTools.allocInt(circuit);
	}

	public long getObservability(Node node) {
		return getObservability(node.level(), node.position());
	}
	
	public long getObservability(int level, int pos) {
		if (rev != obs_rev[level][pos]) {
			ensureSimulated();
			delta.set(level, pos, ~base.getV(level, pos), base.getC(level, pos));
			delta.simulate();
			long o = 0L;
			for (Node n : circuit.accessInterface()) {
				if (n == null || n.isInput())
					continue;
				int p = n.position();
				long care = base.getC(0, p) & delta.getC(0, p);
				long change = base.getV(0, p) ^ delta.getV(0, p);
				o |= care & change;
			}
			delta.clear();
			obs[level][pos] = o;
			obs_rev[level][pos] = rev;
		}
		return obs[level][pos];
	}
	
	public long getValue(Node node) {
		return getValue(node.level(), node.position());
	}
	
	public long getValue(int level, int pos) {
		ensureSimulated();
		return base.getV(level, pos);
	}
	

	public void loadInputsFrom(QBlock b) {
		isSimulated = false;
		rev++;
		base.clear();
		base.loadInputsFrom(b);
	}

	public void storeOutputsTo(QBlock b) {
		ensureSimulated();
		base.storeOutputsTo(b);
	}

	private boolean isSimulated;

	private void ensureSimulated() {
		if (isSimulated)
			return;
		base.simulate();
		isSimulated = true;
	}


}
