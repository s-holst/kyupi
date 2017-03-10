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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import org.kyupi.data.item.QBlock;
import org.kyupi.graph.Graph;
import org.kyupi.graph.Graph.Node;
import org.kyupi.graph.GraphTools;
import org.kyupi.misc.ArrayTools;

public class Simulator {

	public class State {
		private long[][] care;
		private long[][] value;

		private int rev;
		private int[][] valid_rev;
		private int[][] dirty_rev;

		public final State parent;
		int min_dirty_level;

		public State(State parent) {
			this.parent = parent;
			care = GraphTools.allocLong(circuit);
			value = GraphTools.allocLong(circuit);
			valid_rev = GraphTools.allocInt(circuit);
			dirty_rev = GraphTools.allocInt(circuit);
			clear();
		}

		public State() {
			this(null);
		}

		public void clear() {
			rev++;
			min_dirty_level = value.length;
		}

		public long getV(int level, int pos) {
			if (rev == valid_rev[level][pos])
				return value[level][pos];
			if (parent != null)
				return parent.getV(level, pos);
			return 0L;
		}

		public long getC(int level, int pos) {
			if (rev == valid_rev[level][pos])
				return care[level][pos];
			if (parent != null)
				return parent.getC(level, pos);
			return 0L;
		}

		public void set(int level, int pos, long value, long care) {
			this.value[level][pos] = value;
			this.care[level][pos] = care;
			setValid(level, pos);
		}

		public void setValid(int level, int pos) {
			valid_rev[level][pos] = rev;
			dirty_rev[level][pos] = rev - 1;
			Node[] outs = circuit.accessLevel(level)[pos].accessOutputs();
			if (outs != null)
				for (Node succ : outs) {
					if (succ == null)
						continue;
					if (valid_rev[succ.level()][succ.position()] == rev && !succ.isOutput() && !succ.isSequential())
						continue;
					dirty_rev[succ.level()][succ.position()] = rev;
					min_dirty_level = Math.min(min_dirty_level, succ.level());
				}
		}

		public void loadInputsFrom(QBlock b) {
			int pos = 0;
			for (Node n : circuit.accessInterface()) {
				if (n != null && n.isSequential())
					set(0, pos, b.getV(pos), b.getC(pos));
				pos++;
			}
			pos = 0;
			for (Node n : circuit.accessInterface()) {
				if (n != null && n.isInput())
					set(0, pos, b.getV(pos), b.getC(pos));
				pos++;
			}
		}

		public void storeOutputsTo(QBlock b) {
			int pos = 0;
			for (Node n : circuit.accessInterface()) {
				if (n != null && (n.isOutput() || n.isSequential()))
					b.set(pos, getV(0, pos), getC(0, pos));
				pos++;
			}
		}

		public boolean isDirty(int level, int pos) {
			return dirty_rev[level][pos] == rev;
		}

		public void simulate() {
			propagate(this);
		}
	}

	private Graph circuit;

	// interface nodes may be driven by other interface nodes.
	// need to simulate interface nodes in proper order.
	private int[] intf_sim_order;

	public Simulator(Graph circuit) {
		this.circuit = circuit;
		intf_sim_order = calculateIntfSimOrder();
	}

	private int[] calculateIntfSimOrder() {
		int length = circuit.accessInterface().length;
		int[] order = new int[length];
		LinkedList<Node> queue = new LinkedList<Node>();
		HashSet<Node> placed = new HashSet<>();
		for (Node n : circuit.accessInterface()) {
			if (n == null)
				continue;
			if (n.countIns() == 0) {
				queue.add(n);
				continue;
			} else {
				boolean has_intf_drivers = false;
				for (Node in : n.accessInputs()) {
					if (in != null && in.level() == 0)
						has_intf_drivers = true;
				}
				if (!has_intf_drivers)
					queue.add(n);
			}
		}
		int o = 0;
		while (!queue.isEmpty()) {
			Node n = queue.removeFirst();
			placed.add(n);
			order[o++] = n.position();
			if (n.countOuts() > 0) {
				for (Node out : n.accessOutputs()) {
					if (out != null && out.level() == 0) {
						boolean all_placed = true;
						for (Node in : out.accessInputs()) {
							if (in != null && in.level() == 0 && !placed.contains(in))
								all_placed = false;
						}
						if (all_placed)
							queue.add(out);
					}
				}
			}
		}
		if (o != length) {
			throw new RuntimeException("missed some interface nodes during interface order calculation");
		}
		return order;
	}

	private void propagate(State state) {
		for (int l = state.min_dirty_level; l <= state.value.length; l++) {
			int level = l;
			if (l == state.value.length) {
				// do interface level last
				level = 0;
			}
			for (int i = 0; i < state.value[level].length; i++) {
				int pos = i;
				if (level == 0) {
					pos = intf_sim_order[i];
				}
				if (state.isDirty(level, pos)) {
					simNode(state, circuit.accessLevel(level)[pos]);
				}
			}
		}
		for (DeferredAssignment d : da) {
			state.value[d.level][d.position] = d.value[0];
			state.care[d.level][d.position] = d.care[0];
		}
		da.clear();
	}

	private long[] dataV;
	private long[] dataC;
	private long[] cv;

	private ArrayList<DeferredAssignment> da = new ArrayList<>();

	private void simNode(State s, Node n) {
		int input_count = n.maxIn() + 1;
		dataV = ArrayTools.grow(dataV, input_count, 4, 0L);
		dataC = ArrayTools.grow(dataC, input_count, 4, 0L);
		for (int i = 0; i < input_count; i++) {
			Node pred = n.in(i);
			if (pred == null) {
				dataV[i] = 0L;
				dataC[i] = 0L;
			} else {
				dataV[i] = s.getV(pred.level(), pred.position());
				dataC[i] = s.getC(pred.level(), pred.position());
				if (pred.isMultiOutput()) {
					cv = circuit.library().calcOutput(pred.type(), pred.searchOutIdx(n), dataV[i], dataC[i]);
					dataV[i] = cv[1];
					dataC[i] = cv[0];
				}
			}
		}
		if (n.isSequential()) {
			// System.out.println("new DA " + n.position() + " " + cv[1]);
			DeferredAssignment d = new DeferredAssignment(n.level(), n.position());
			da.add(d);
			circuit.library().propagate(n.type(), dataV, dataC, 0, input_count, d.value, d.care, 0, 1);			
			s.valid_rev[n.level()][n.position()] = s.rev;
			s.dirty_rev[n.level()][n.position()] = s.rev - 1;
		} else {
			circuit.library().propagate(n.type(), dataV, dataC, 0, input_count, s.value[n.level()], s.care[n.level()], n.position(), 1);
			s.setValid(n.level(), n.position());
		}
	}

	private class DeferredAssignment {
		int level;
		int position;
		long[] care = new long[1];
		long[] value = new long[1];

		public DeferredAssignment(int l, int p) {
			level = l;
			position = p;
		}
	}
}
