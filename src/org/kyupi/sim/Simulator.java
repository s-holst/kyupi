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

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.kyupi.data.item.QBlock;
import org.kyupi.graph.Graph;
import org.kyupi.graph.Graph.Node;
import org.kyupi.graph.GraphTools;
import org.kyupi.misc.ArrayTools;

public class Simulator {

	protected static Logger log = Logger.getLogger(Simulator.class);

	public class State {
		private long[][] care;
		private long[][] value;

		private long[] stim_care;
		private long[] stim_value;

		private long[] resp_care;
		private long[] resp_value;

		private int rev;
		private int[][] valid_rev;
		private int[][] dirty_rev;

		private int[] stim_valid_rev;
		private int[] resp_valid_rev;

		public final State parent;
		int min_dirty_level;

		public State(State parent) {
			this.parent = parent;
			care = GraphTools.allocLong(circuit);
			value = GraphTools.allocLong(circuit);
			valid_rev = GraphTools.allocInt(circuit);
			dirty_rev = GraphTools.allocInt(circuit);
			stim_care = new long[circuit.accessInterface().length];
			stim_value = new long[circuit.accessInterface().length];
			stim_valid_rev = new int[circuit.accessInterface().length];
			Arrays.fill(stim_value, 0L);
			Arrays.fill(stim_care, 0L);
			Arrays.fill(stim_valid_rev, 0);
			resp_care = new long[circuit.accessInterface().length];
			resp_value = new long[circuit.accessInterface().length];
			resp_valid_rev = new int[circuit.accessInterface().length];
			Arrays.fill(resp_value, 0L);
			Arrays.fill(resp_care, 0L);
			Arrays.fill(resp_valid_rev, 0);
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

		public long getStimulusV(int pos) {
			if (rev == stim_valid_rev[pos])
				return stim_value[pos];
			if (parent != null)
				return parent.getStimulusV(pos);
			return 0L;
		}

		public long getResponseV(int pos) {
			if (rev == resp_valid_rev[pos])
				return resp_value[pos];
			if (parent != null)
				return parent.getResponseV(pos);
			return 0L;
		}

		public long getC(int level, int pos) {
			if (rev == valid_rev[level][pos])
				return care[level][pos];
			if (parent != null)
				return parent.getC(level, pos);
			return 0L;
		}

		public long getStimulusC(int pos) {
			if (rev == stim_valid_rev[pos])
				return stim_care[pos];
			if (parent != null)
				return parent.getStimulusC(pos);
			return 0L;
		}

		public long getResponseC(int pos) {
			if (rev == resp_valid_rev[pos])
				return resp_care[pos];
			if (parent != null)
				return parent.getResponseC(pos);
			return 0L;
		}

		public void set(int level, int pos, long value, long care) {
			this.value[level][pos] = value;
			this.care[level][pos] = care;
			setSuccessorsDirty(level, pos);
		}

		public void setSuccessorsDirty(int level, int pos) {
			valid_rev[level][pos] = rev;
			dirty_rev[level][pos] = rev - 1;
			Node[] outs = circuit.accessLevel(level)[pos].accessOutputs();
			if (outs != null)
				for (Node succ : outs) {
					if (succ == null)
						continue;
					if (valid_rev[succ.level()][succ.levelPosition()] == rev)
						continue;
					dirty_rev[succ.level()][succ.levelPosition()] = rev;
					min_dirty_level = Math.min(min_dirty_level, succ.level());
				}
		}

		public void setStimulus(int pos, long value, long care) {
			this.stim_value[pos] = value;
			this.stim_care[pos] = care;
			stim_valid_rev[pos] = rev;
			Node n = circuit.accessInterface()[pos];
			circuit.library().propagate(n.type(), stim_value, stim_care, pos, 1, this.value[n.level()],
					this.care[n.level()], n.levelPosition(), 1);
			// log.debug("intf prop " + pos + " " +
			// this.value[n.level()][n.levelPosition()]);
			setSuccessorsDirty(n.level(), n.levelPosition());
		}

		public void loadInputsFrom(QBlock b) {
			int pos = 0;
			for (Node n : circuit.accessInterface()) {
				if (n != null && n.isSequential())
					setStimulus(pos, b.getV(pos), b.getC(pos));
				pos++;
			}
			pos = 0;
			for (Node n : circuit.accessInterface()) {
				if (n != null && n.isInput())
					setStimulus(pos, b.getV(pos), b.getC(pos));
				pos++;
			}
		}

		public void storeOutputsTo(QBlock b) {
			int pos = 0;
			for (Node n : circuit.accessInterface()) {
				if (n != null && (n.isOutput() || n.isSequential()))
					b.set(pos, getResponseV(pos), getResponseC(pos));
				pos++;
			}
		}

		public boolean isDirty(int level, int pos) {
			return dirty_rev[level][pos] == rev;
		}

		public void simulate() {
			propagate_state(this);
			capture_state(this);
		}

		public void propagate() {
			propagate_state(this);
		}

		public void capture() {
			capture_state(this);
		}

	}

	private Graph circuit;

	public Simulator(Graph circuit) {
		this.circuit = circuit;
	}

	private void propagate_state(State state) {
		for (int l = state.min_dirty_level; l < state.value.length; l++) {
			int level = l;
			for (int i = 0; i < state.value[level].length; i++) {
				int pos = i;
				Node n = circuit.accessLevel(level)[pos];
				if (n == null || n.isSequential() || n.isInput())
					continue;
				if (state.isDirty(level, pos)) {
					simNode(state, n, state.value[n.level()], state.care[n.level()], n.levelPosition());
				}
			}
		}
		for (Node n : circuit.accessInterface()) {
			if (n == null || n.isInput())
				continue;
			if (n.isSequential()) { // && state.isDirty(n.level(), n.levelPosition())) {
				simNode(state, n, state.resp_value, state.resp_care, n.intfPosition());
				state.resp_valid_rev[n.intfPosition()] = state.rev;
			}
			if (n.isOutput()) {
				state.resp_value[n.intfPosition()] = state.value[n.level()][n.levelPosition()];
				state.resp_care[n.intfPosition()] = state.care[n.level()][n.levelPosition()];
				state.resp_valid_rev[n.intfPosition()] = state.valid_rev[n.level()][n.levelPosition()];
			}
		}
	}

	private void capture_state(State state) {
		for (Node n : circuit.accessInterface()) {
			if (n == null || n.isInput())
				continue;
			state.setStimulus(n.intfPosition(), state.getResponseV(n.intfPosition()),
					state.getResponseC(n.intfPosition()));
		}
	}

	private long[] dataV;
	private long[] dataC;
	private long[] cv;

	private void simNode(State s, Node n, long[] value, long[] care, int pos) {
		int input_count = n.maxIn() + 1;
		dataV = ArrayTools.grow(dataV, input_count, 4, 0L);
		dataC = ArrayTools.grow(dataC, input_count, 4, 0L);
		for (int i = 0; i < input_count; i++) {
			Node pred = n.in(i);
			if (pred == null) {
				dataV[i] = 0L;
				dataC[i] = 0L;
			} else {
				dataV[i] = s.getV(pred.level(), pred.levelPosition());
				dataC[i] = s.getC(pred.level(), pred.levelPosition());
				if (pred.isMultiOutput()) {
					cv = circuit.library().calcOutput(pred.type(), pred.searchOutIdx(n), dataV[i], dataC[i]);
					dataV[i] = cv[1];
					dataC[i] = cv[0];
				}
			}
		}
		circuit.library().propagate(n.type(), dataV, dataC, 0, input_count, value, care, pos, 1);
		//log.debug("simNode " + n.queryName() + " " + n.typeName() + " " + value[pos]);
		s.setSuccessorsDirty(n.level(), n.levelPosition());
	}

}
