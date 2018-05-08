package org.kyupi.sim;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.kyupi.data.item.BBlock;
import org.kyupi.data.item.QBlock;
import org.kyupi.graph.Graph;
import org.kyupi.graph.Graph.Node;
import org.kyupi.graph.Library;
import org.kyupi.sim.CombLogicSim.State;

public class Observability {

	protected static Logger log = Logger.getLogger(Observability.class);
	
	private State base;
	private State delta;
	private Graph graph;

	private long obs[];
	private int obs_rev[];

	private BBlock obs_outputs;
	private int obs_outputs_rev;

	
	private int rev = 0;
	
	public Observability(Graph g) {
		graph = g;
		CombLogicSim sim = new CombLogicSim(graph);
		base = sim.new State();
		delta = sim.new State(base);
		obs = new long[graph.accessSignalMap().length()];
		Arrays.fill(obs, 0L);
		obs_outputs = new BBlock(graph.accessInterface().length);
		obs_rev = new int[graph.accessSignalMap().length()];
		Arrays.fill(obs_rev, 0);
		obs_outputs_rev = 0;
	}


	public void loadInputsFrom(QBlock b) {
		rev++;
		base.clear();
		base.loadInputsFrom(b);
		base.propagate();
	}
	
	public void storeOutputsTo(QBlock b) {
		base.storeOutputsTo(b);
	}

	public long getV(int sig_idx) {
		return base.getV(sig_idx);
	}

	public long getC(int sig_idx) {
		return base.getC(sig_idx);
	}
	
	public long getObservability(int sig_idx) {
		
		if (obs_outputs_rev == rev)
			obs_outputs_rev--; // invalid by default, only valid right after deltaSim
		
		if (obs_rev[sig_idx] == rev) {
			//log.info("found obs");
			return obs[sig_idx];
		}

		Node receiver = graph.accessSignalMap().receiverForIdx(sig_idx);
		
		if (receiver == null) {
			log.error("unable to find receiver for sig_idx " + sig_idx);
			Node d = graph.accessSignalMap().driverForIdx(sig_idx);
			if (d != null) {
				log.error("  driver is " + d);
			} else {
				log.error("  driver is null as well.");
				
			}
		}	
		
		if (receiver.isOutput() || receiver.isSequential()) { // we assume full observability of interface
			obs[sig_idx] = -1L;
			obs_rev[sig_idx] = rev;
			for (int pos = 0; pos < obs_outputs.length(); pos++) {
				obs_outputs.set(pos, pos == receiver.intfPosition() ? -1L : 0L);
			}
			obs_outputs_rev = rev;
			return obs[sig_idx];
		}

		if (receiver.maxOut() < 0) { // dead-end node, not observable
			obs[sig_idx] = 0L;
			obs_rev[sig_idx] = rev;
			for (int pos = 0; pos < obs_outputs.length(); pos++) {
				obs_outputs.set(pos, 0L);
			}
			obs_outputs_rev = rev;
			return obs[sig_idx];
		}
		

		if (receiver.maxOut() == 0 && canPropagate(receiver.type())) { // propagate through ffr
			Node driver = graph.accessSignalMap().driverForIdx(sig_idx);
			int in_idx = receiver.searchInIdx(driver);
			long s = sensitization(receiver, in_idx);
			int out_sig_idx = graph.accessSignalMap().idxForOutput(receiver, 0);
			obs[sig_idx] = s & getObservability(out_sig_idx);
			obs_rev[sig_idx] = rev;
			return obs[sig_idx];
		}
		
		// last resort: explicit simulation
		//log.info("explicit sim");
		deltaSim(sig_idx);
		return obs[sig_idx];
	}
	
	public BBlock accessLastObservingOutputs() {
		if (obs_outputs_rev == rev) {
			return obs_outputs;
		} else {
			log.error("accessLastObservingOutputs: Unavailable. Only available right after explicit simulation at fan-out.");
			return null;
		}
	}
	
	private boolean canPropagate(int type) {
		switch(type & 0xff) {
		case Library.TYPE_BUF:
		case Library.TYPE_NOT:
		case Library.TYPE_AND:
		case Library.TYPE_NAND:
		case Library.TYPE_OR:
		case Library.TYPE_NOR:
		case Library.TYPE_XOR:
		case Library.TYPE_XNOR:
			return true;
		}
		return false;
	}

	private long sensitization(Node n, int on_path_input_idx) {
		int in_count = n.maxIn() + 1;
		long s;
		switch (n.type() & 0xff) {
		case Library.TYPE_BUF:
		case Library.TYPE_NOT:
		case Library.TYPE_XOR:
		case Library.TYPE_XNOR:
			return -1L;
		case Library.TYPE_AND:
		case Library.TYPE_NAND:
			s = -1L;
			for (int i = 0; i < in_count; i++) {
				if (i == on_path_input_idx)
					continue;
				int sig_idx = graph.accessSignalMap().idxForInput(n, i);
				if (sig_idx < 0)
					continue;
				s = s & (getV(sig_idx) & getC(sig_idx));
			}
			return s;
		case Library.TYPE_OR:
		case Library.TYPE_NOR:
			s = -1L;
			for (int i = 0; i < in_count; i++) {
				if (i == on_path_input_idx)
					continue;
				int sig_idx = graph.accessSignalMap().idxForInput(n, i);
				if (sig_idx < 0)
					continue;
				s = s & (~getV(sig_idx) & getC(sig_idx));
			}
			return s;
		}
		return 0L;
	}
	
	private void deltaSim(int sig_idx) {
		if (sig_idx < 0)
			return;
		delta.clear();
		delta.set(sig_idx, ~base.getV(sig_idx), base.getC(sig_idx));
		delta.propagate();
		long sig_obs = 0L;
		for (Node out: graph.accessInterface()) {
			if (out == null)
				continue;
			int pos = out.intfPosition();
			obs_outputs.set(pos, 0L);
			if (out.isOutput() || out.isSequential()) {
				if (delta.isResponseUpdated(pos)) {
					long dv = delta.getResponseV(pos) ^ base.getResponseV(pos);
					long bc = delta.getResponseC(pos) & base.getResponseC(pos);
					obs_outputs.set(pos, dv & bc);
					sig_obs = sig_obs | (dv & bc);
				}
			}
		}
		obs[sig_idx] = sig_obs;
		obs_rev[sig_idx] = rev;
		obs_outputs_rev = rev;
	}

}
