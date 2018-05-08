/*
 * Copyright 2018 The KyuPI project contributors. See the COPYRIGHT.md file
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
import org.kyupi.circuit.Graph;
import org.kyupi.circuit.GraphTools;
import org.kyupi.circuit.Library;
import org.kyupi.circuit.LibrarySAED;
import org.kyupi.circuit.Graph.Node;
import org.kyupi.data.item.QBlock;
import org.kyupi.misc.ArrayTools;

public class CombLogicSim {

	protected static Logger log = Logger.getLogger(CombLogicSim.class);

	private Graph circuit;

	public class State {

		public final State parent;

		private int rev;

		private long[] care;
		private long[] value;
		private int[] valid_rev;		

		private long[] stim_care;
		private long[] stim_value;
		private int[] stim_valid_rev;

		private long[] resp_care;
		private long[] resp_value;
		private int[] resp_valid_rev;

		private int[][] dirty_rev;
		int min_dirty_level;

		public State(State parent) {
			this.parent = parent;
			care = new long[circuit.accessSignalMap().length()];
			value = new long[circuit.accessSignalMap().length()];
			Arrays.fill(care, 0L);
			Arrays.fill(value, 0L);
			valid_rev = new int[circuit.accessSignalMap().length()];
			Arrays.fill(valid_rev, 0);

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

		public long getV(int signal_idx) {
			if (rev == valid_rev[signal_idx])
				return value[signal_idx];
			if (parent != null)
				return parent.getV(signal_idx);
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

		public long getC(int signal_idx) {
			if (rev == valid_rev[signal_idx])
				return care[signal_idx];
			if (parent != null)
				return parent.getC(signal_idx);
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
		
		public boolean isResponseUpdated(int pos) {
			return rev == resp_valid_rev[pos];
		}

		public void set(int signal_idx, long value, long care) {
			this.value[signal_idx] = value;
			this.care[signal_idx] = care;
			valid_rev[signal_idx] = rev;
			Node receiver = circuit.accessSignalMap().receiverForIdx(signal_idx);
			dirty_rev[receiver.level()][receiver.levelPosition()] = rev;
			min_dirty_level = Math.min(min_dirty_level, receiver.level());
		}

		public void setStimulus(int pos, long value, long care) {
			this.stim_value[pos] = value;
			this.stim_care[pos] = care;
			stim_valid_rev[pos] = rev;
			Node n = circuit.accessInterface()[pos];
			dirty_rev[n.level()][n.levelPosition()] = rev;
			min_dirty_level = 0;
		}

		public void loadInputsFrom(QBlock b) {
			int pos = 0;
			for (Node n : circuit.accessInterface()) {
				if (n != null && (n.isSequential() || n.isInput()))
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

		public void propagate() {
			propagate_state(this);
		}

		public void reapply() {
			clear();
			for (Node n : circuit.accessInterface()) {
				if (n == null)
					continue;
				int pos = n.intfPosition();
				long v = 0L;
				long c = 0L;

				// re-apply values previous to clear()
				if (n.isInput() && !n.isOutput()) {
					// previous stimulus
					if (stim_valid_rev[pos] == rev - 1) {
						v = stim_value[pos];
						c = stim_care[pos];
					} else if (parent != null) {
						v = parent.getStimulusV(pos);
						c = parent.getStimulusC(pos);
					}
				} else {
					// previous response
					if (resp_valid_rev[pos] == rev - 1) {
						v = resp_value[pos];
						c = resp_care[pos];
					} else if (parent != null) {
						v = parent.getResponseV(pos);
						c = parent.getResponseC(pos);
					}
				}
				setStimulus(pos, v, c);
			}
		}

		public void clear() {
			rev++;
			min_dirty_level = Integer.MAX_VALUE;
		}

	}

	public CombLogicSim(Graph circuit) {
		this.circuit = circuit;
	}

	private long[] tmpIV = new long[32];
	private long[] tmpIC = new long[32];

	private long[] tmpOV = new long[32];
	private long[] tmpOC = new long[32];

	private void propagate_state(State state) {
		int level_count = circuit.levels();
		for (int level_idx = state.min_dirty_level; level_idx < level_count; level_idx++) {
			Node[] level = circuit.accessLevel(level_idx);
			for (int pos = 0; pos < level.length; pos++) {
				Node n = level[pos];
				if (n == null || n.maxOut() < 0)
					continue;
				int output_count = n.maxOut() + 1;
				int input_count = n.maxIn() + 1;
				tmpOV = ArrayTools.grow(tmpOV, output_count, 32, 0);
				tmpOC = ArrayTools.grow(tmpOC, output_count, 32, 0);

				if (state.isDirty(level_idx, pos)) {
					if (n.isInput() || n.isSequential()) {
						tmpIV[0] = state.getStimulusV(n.intfPosition());
						tmpIC[0] = state.getStimulusC(n.intfPosition());
						//log.debug("simulate " + n + " " + tmpIV[0]);
						sim(n.type(), tmpIV, tmpIC, 1, tmpOV, tmpOC, output_count);
					} else {
						tmpIV = ArrayTools.grow(tmpIV, input_count, 32, 0);
						tmpIC = ArrayTools.grow(tmpIC, input_count, 32, 0);
						for (int i = 0; i < input_count; i++) {
							int sidx = circuit.accessSignalMap().idxForInput(n, i);
							if (sidx >= 0) {
								tmpIV[i] = state.getV(sidx);
								tmpIC[i] = state.getC(sidx);
							} else {
								tmpIV[i] = 0L;
								tmpIC[i] = 0L;
							}
						}
						sim(n.type(), tmpIV, tmpIC, input_count, tmpOV, tmpOC, output_count);
					}
					
					for (int o = 0; o < output_count; o++) {
						int sidx = circuit.accessSignalMap().idxForOutput(n, o);
						if (sidx >= 0 && state.valid_rev[sidx] != state.rev) {
							//log.debug("set signal " + n + " " + sidx + " " + tmpOV[o]);

							state.set(sidx, tmpOV[o], tmpOC[o]);
						}
					}
				}
			}
		}
		for (Node n : circuit.accessInterface()) {
			if (n == null || !state.isDirty(n.level(), n.levelPosition()))
				continue;
			if (n.isSequential() || n.isOutput()) {
				int input_count = n.maxIn() + 1;
				for (int i = 0; i < input_count; i++) {
					int sidx = circuit.accessSignalMap().idxForInput(n, i);
					if (sidx >= 0) {
						tmpIV[i] = state.getV(sidx);
						tmpIC[i] = state.getC(sidx);
					} else {
						tmpIV[i] = 0L;
						tmpIC[i] = 0L;
					}
				}
				sim(n.type(), tmpIV, tmpIC, input_count, tmpOV, tmpOC, 1);
				state.resp_value[n.intfPosition()] = tmpOV[0];
				state.resp_care[n.intfPosition()] = tmpOC[0];
				state.resp_valid_rev[n.intfPosition()] = state.rev;
			}
		}
	}

	private long tmpInC[] = new long[3];
	private long tmpInV[] = new long[3];

	private void sim(int type, long[] inV, long[] inC, int inCount, long[] outV, long[] outC, int outCount) {

		long j = 0L;
		long k = 0L;
		long l = 0L;

		long v = 0L;
		long c = 0L;

		if (outCount <= 0)
			return;

		switch (type & 0xff) {
		case Library.TYPE_CONST0:
			c = -1L;
			v = 0L;
			break;
		case Library.TYPE_NOR:
			for (int i = 0; i < inCount; i++) {
				l |= ~inC[i] & inV[i];
				k |= inC[i] & inV[i];
				j |= ~inC[i] & ~inV[i];
			}
			c = -1L;
			v = -1L;
			c &= ~j;
			v &= ~j;
			c |= k;
			v &= ~k;
			c &= ~l;
			v |= l;
			break;
		case Library.TYPE_AGTB:
			for (int i = 0; i < inCount; i++) {
				l |= ~inC[i] & inV[i];
				j |= ~inC[i] & ~inV[i];
			}
			k = (inC[0] & ~inV[0]) | (inC[1] & inV[1]);
			c = -1L;
			v = -1L;
			c &= ~j;
			v &= ~j;
			c |= k;
			v &= ~k;
			c &= ~l;
			v |= l;
			break;
		case Library.TYPE_BNOT:
			c = inC[1];
			v = inC[1] ^ inV[1];
			break;
		case Library.TYPE_BGTA:
			for (int i = 0; i < inCount; i++) {
				l |= ~inC[i] & inV[i];
				j |= ~inC[i] & ~inV[i];
			}
			k = (inC[0] & inV[0]) | (inC[1] & ~inV[1]);
			c = -1L;
			v = -1L;
			c &= ~j;
			v &= ~j;
			c |= k;
			v &= ~k;
			c &= ~l;
			v |= l;
			break;
		case Library.TYPE_NOT:
			c = inC[0];
			v = inC[0] ^ inV[0];
			break;
		case Library.TYPE_XOR:
			for (int i = 0; i < inCount; i++) {
				l |= ~inC[i] & inV[i];
				k |= ~inC[i] & ~inV[i];
				j ^= inV[i];
			}
			c = -1L;
			v = j;
			c &= ~k;
			v &= ~k;
			c &= ~l;
			v |= l;
			break;
		case Library.TYPE_NAND:
			for (int i = 0; i < inCount; i++) {
				l |= ~inC[i] & inV[i];
				k |= inC[i] & ~inV[i];
				j |= ~inC[i] & ~inV[i];
			}
			c = -1L;
			v = -1L;
			c &= ~j;
			v &= ~j;
			c |= k;
			v &= ~k;
			c &= ~l;
			v |= l;
			v ^= c;
			break;
		case Library.TYPE_AND:
			for (int i = 0; i < inCount; i++) {
				l |= ~inC[i] & inV[i];
				k |= inC[i] & ~inV[i];
				j |= ~inC[i] & ~inV[i];
			}
			c = -1L;
			v = -1L;
			c &= ~j;
			v &= ~j;
			c |= k;
			v &= ~k;
			c &= ~l;
			v |= l;
			break;
		case Library.TYPE_XNOR:
			for (int i = 0; i < inCount; i++) {
				l |= ~inC[i] & inV[i];
				k |= ~inC[i] & ~inV[i];
				j ^= inV[i];
			}
			c = -1L;
			v = j;
			c &= ~k;
			v &= ~k;
			c &= ~l;
			v |= l;
			v ^= c;
			break;
		case Library.TYPE_BUF:
			c = inC[0];
			v = inV[0];
			break;
		case Library.TYPE_AGEB:
			for (int i = 0; i < inCount; i++)
				l |= ~inC[i] & inV[i];
			k = ~inC[0] & ~inV[0] & ~inC[1] & ~inV[1];
			j = inV[0] | ~inV[1];
			c = -1L;
			v = 0L;
			c = -1L;
			v = j;
			c &= ~k;
			v &= ~k;
			c &= ~l;
			v |= l;
			break;
		case Library.TYPE_BBUF:
			c = inC[1];
			v = inC[1];
			break;
		case Library.TYPE_BGEA:
			for (int i = 0; i < inCount; i++)
				l |= ~inC[i] & inV[i];
			k = ~inC[0] & ~inV[0] & ~inC[1] & ~inV[1];
			j = ~inV[0] | inV[1];
			c = -1L;
			v = 0L;
			c = -1L;
			v = j;
			c &= ~k;
			v &= ~k;
			c &= ~l;
			v |= l;
			break;
		case Library.TYPE_OR:
			for (int i = 0; i < inCount; i++) {
				l |= ~inC[i] & inV[i];
				k |= inC[i] & inV[i];
				j |= ~inC[i] & ~inV[i];
			}
			c = -1L;
			v = 0L;
			c &= ~j;
			v &= ~j;
			c |= k;
			v |= k;
			c &= ~l;
			v |= l;
			break;
		case Library.TYPE_CONST1:
			c = -1L;
			v = 0L;
			break;
		case LibrarySAED.TYPE_AO21 & 0xff:
			for (int i = 0; i < 2; i++) {
				l |= ~inC[i] & inV[i];
				k |= inC[i] & ~inV[i];
				j |= ~inC[i] & ~inV[i];
			}
			tmpInC[0] = -1L;
			tmpInV[0] = -1L;
			tmpInC[0] &= ~j;
			tmpInV[0] &= ~j;
			tmpInC[0] |= k;
			tmpInV[0] &= ~k;
			tmpInC[0] &= ~l;
			tmpInV[0] |= l;
			l = 0L;
			k = 0L;
			j = 0L;
			tmpInC[1] = inC[2];
			tmpInV[1] = inV[2];
			for (int i = 0; i < 2; i++) {
				l |= ~tmpInC[i] & tmpInV[i];
				k |= tmpInC[i] & tmpInV[i];
				j |= ~tmpInC[i] & ~tmpInV[i];
			}
			c = -1L;
			v = 0L;
			c &= ~j;
			v &= ~j;
			c |= k;
			v |= k;
			c &= ~l;
			v |= l;
			break;
		case LibrarySAED.TYPE_AO221 & 0xff:
			for (int i = 0; i < 2; i++) {
				l |= ~inC[i] & inV[i];
				k |= inC[i] & ~inV[i];
				j |= ~inC[i] & ~inV[i];
			}
			tmpInC[0] = -1L;
			tmpInV[0] = -1L;
			tmpInC[0] &= ~j;
			tmpInV[0] &= ~j;
			tmpInC[0] |= k;
			tmpInV[0] &= ~k;
			tmpInC[0] &= ~l;
			tmpInV[0] |= l;
			l = 0L;
			k = 0L;
			j = 0L;
			for (int i = 2; i < 4; i++) {
				l |= ~inC[i] & inV[i];
				k |= inC[i] & ~inV[i];
				j |= ~inC[i] & ~inV[i];
			}
			tmpInC[1] = -1L;
			tmpInV[1] = -1L;
			tmpInC[1] &= ~j;
			tmpInV[1] &= ~j;
			tmpInC[1] |= k;
			tmpInV[1] &= ~k;
			tmpInC[1] &= ~l;
			tmpInV[1] |= l;
			l = 0L;
			k = 0L;
			j = 0L;
			tmpInC[2] = inC[4];
			tmpInV[2] = inV[4];
			for (int i = 0; i < 3; i++) {
				l |= ~tmpInC[i] & tmpInV[i];
				k |= tmpInC[i] & tmpInV[i];
				j |= ~tmpInC[i] & ~tmpInV[i];
			}
			c = -1L;
			v = 0L;
			c &= ~j;
			v &= ~j;
			c |= k;
			v |= k;
			c &= ~l;
			v |= l;
			break;
		case LibrarySAED.TYPE_AO222 & 0xff:
			for (int i = 0; i < 2; i++) {
				l |= ~inC[i] & inV[i];
				k |= inC[i] & ~inV[i];
				j |= ~inC[i] & ~inV[i];
			}
			tmpInC[0] = -1L;
			tmpInV[0] = -1L;
			tmpInC[0] &= ~j;
			tmpInV[0] &= ~j;
			tmpInC[0] |= k;
			tmpInV[0] &= ~k;
			tmpInC[0] &= ~l;
			tmpInV[0] |= l;
			l = 0L;
			k = 0L;
			j = 0L;
			for (int i = 2; i < 4; i++) {
				l |= ~inC[i] & inV[i];
				k |= inC[i] & ~inV[i];
				j |= ~inC[i] & ~inV[i];
			}
			tmpInC[1] = -1L;
			tmpInV[1] = -1L;
			tmpInC[1] &= ~j;
			tmpInV[1] &= ~j;
			tmpInC[1] |= k;
			tmpInV[1] &= ~k;
			tmpInC[1] &= ~l;
			tmpInV[1] |= l;
			l = 0L;
			k = 0L;
			j = 0L;
			for (int i = 4; i < 6; i++) {
				l |= ~inC[i] & inV[i];
				k |= inC[i] & ~inV[i];
				j |= ~inC[i] & ~inV[i];
			}
			tmpInC[2] = -1L;
			tmpInV[2] = -1L;
			tmpInC[2] &= ~j;
			tmpInV[2] &= ~j;
			tmpInC[2] |= k;
			tmpInV[2] &= ~k;
			tmpInC[2] &= ~l;
			tmpInV[2] |= l;
			l = 0L;
			k = 0L;
			j = 0L;
			for (int i = 0; i < 3; i++) {
				l |= ~tmpInC[i] & tmpInV[i];
				k |= tmpInC[i] & tmpInV[i];
				j |= ~tmpInC[i] & ~tmpInV[i];
			}
			c = -1L;
			v = 0L;
			c &= ~j;
			v &= ~j;
			c |= k;
			v |= k;
			c &= ~l;
			v |= l;
			break;
		case LibrarySAED.TYPE_AO22 & 0xff:
			for (int i = 0; i < 2; i++) {
				l |= ~inC[i] & inV[i];
				k |= inC[i] & ~inV[i];
				j |= ~inC[i] & ~inV[i];
			}
			tmpInC[0] = -1L;
			tmpInV[0] = -1L;
			tmpInC[0] &= ~j;
			tmpInV[0] &= ~j;
			tmpInC[0] |= k;
			tmpInV[0] &= ~k;
			tmpInC[0] &= ~l;
			tmpInV[0] |= l;
			l = 0L;
			k = 0L;
			j = 0L;
			for (int i = 2; i < 4; i++) {
				l |= ~inC[i] & inV[i];
				k |= inC[i] & ~inV[i];
				j |= ~inC[i] & ~inV[i];
			}
			tmpInC[1] = -1L;
			tmpInV[1] = -1L;
			tmpInC[1] &= ~j;
			tmpInV[1] &= ~j;
			tmpInC[1] |= k;
			tmpInV[1] &= ~k;
			tmpInC[1] &= ~l;
			tmpInV[1] |= l;
			l = 0L;
			k = 0L;
			j = 0L;
			for (int i = 0; i < 2; i++) {
				l |= ~tmpInC[i] & tmpInV[i];
				k |= tmpInC[i] & tmpInV[i];
				j |= ~tmpInC[i] & ~tmpInV[i];
			}
			c = -1L;
			v = 0L;
			c &= ~j;
			v &= ~j;
			c |= k;
			v |= k;
			c &= ~l;
			v |= l;
			break;
		case LibrarySAED.TYPE_AOI21 & 0xff:
			for (int i = 0; i < 2; i++) {
				l |= ~inC[i] & inV[i];
				k |= inC[i] & ~inV[i];
				j |= ~inC[i] & ~inV[i];
			}
			tmpInC[0] = -1L;
			tmpInV[0] = -1L;
			tmpInC[0] &= ~j;
			tmpInV[0] &= ~j;
			tmpInC[0] |= k;
			tmpInV[0] &= ~k;
			tmpInC[0] &= ~l;
			tmpInV[0] |= l;
			l = 0L;
			k = 0L;
			j = 0L;
			tmpInC[1] = inC[2];
			tmpInV[1] = inV[2];
			for (int i = 0; i < 2; i++) {
				l |= ~tmpInC[i] & tmpInV[i];
				k |= tmpInC[i] & tmpInV[i];
				j |= ~tmpInC[i] & ~tmpInV[i];
			}
			c = -1L;
			v = 0L;
			c &= ~j;
			v &= ~j;
			c |= k;
			v |= k;
			c &= ~l;
			v |= l;
			v ^= c;
			break;
		case LibrarySAED.TYPE_AOI221 & 0xff:
			for (int i = 0; i < 2; i++) {
				l |= ~inC[i] & inV[i];
				k |= inC[i] & ~inV[i];
				j |= ~inC[i] & ~inV[i];
			}
			tmpInC[0] = -1L;
			tmpInV[0] = -1L;
			tmpInC[0] &= ~j;
			tmpInV[0] &= ~j;
			tmpInC[0] |= k;
			tmpInV[0] &= ~k;
			tmpInC[0] &= ~l;
			tmpInV[0] |= l;
			l = 0L;
			k = 0L;
			j = 0L;
			for (int i = 2; i < 4; i++) {
				l |= ~inC[i] & inV[i];
				k |= inC[i] & ~inV[i];
				j |= ~inC[i] & ~inV[i];
			}
			tmpInC[1] = -1L;
			tmpInV[1] = -1L;
			tmpInC[1] &= ~j;
			tmpInV[1] &= ~j;
			tmpInC[1] |= k;
			tmpInV[1] &= ~k;
			tmpInC[1] &= ~l;
			tmpInV[1] |= l;
			l = 0L;
			k = 0L;
			j = 0L;
			tmpInC[2] = inC[4];
			tmpInV[2] = inV[4];
			for (int i = 0; i < 3; i++) {
				l |= ~tmpInC[i] & tmpInV[i];
				k |= tmpInC[i] & tmpInV[i];
				j |= ~tmpInC[i] & ~tmpInV[i];
			}
			c = -1L;
			v = 0L;
			c &= ~j;
			v &= ~j;
			c |= k;
			v |= k;
			c &= ~l;
			v |= l;
			v ^= c;
			break;
		case LibrarySAED.TYPE_AOI222 & 0xff:
			for (int i = 0; i < 2; i++) {
				l |= ~inC[i] & inV[i];
				k |= inC[i] & ~inV[i];
				j |= ~inC[i] & ~inV[i];
			}
			tmpInC[0] = -1L;
			tmpInV[0] = -1L;
			tmpInC[0] &= ~j;
			tmpInV[0] &= ~j;
			tmpInC[0] |= k;
			tmpInV[0] &= ~k;
			tmpInC[0] &= ~l;
			tmpInV[0] |= l;
			l = 0L;
			k = 0L;
			j = 0L;
			for (int i = 2; i < 4; i++) {
				l |= ~inC[i] & inV[i];
				k |= inC[i] & ~inV[i];
				j |= ~inC[i] & ~inV[i];
			}
			tmpInC[1] = -1L;
			tmpInV[1] = -1L;
			tmpInC[1] &= ~j;
			tmpInV[1] &= ~j;
			tmpInC[1] |= k;
			tmpInV[1] &= ~k;
			tmpInC[1] &= ~l;
			tmpInV[1] |= l;
			l = 0L;
			k = 0L;
			j = 0L;
			for (int i = 4; i < 6; i++) {
				l |= ~inC[i] & inV[i];
				k |= inC[i] & ~inV[i];
				j |= ~inC[i] & ~inV[i];
			}
			tmpInC[2] = -1L;
			tmpInV[2] = -1L;
			tmpInC[2] &= ~j;
			tmpInV[2] &= ~j;
			tmpInC[2] |= k;
			tmpInV[2] &= ~k;
			tmpInC[2] &= ~l;
			tmpInV[2] |= l;
			l = 0L;
			k = 0L;
			j = 0L;
			for (int i = 0; i < 3; i++) {
				l |= ~tmpInC[i] & tmpInV[i];
				k |= tmpInC[i] & tmpInV[i];
				j |= ~tmpInC[i] & ~tmpInV[i];
			}
			c = -1L;
			v = 0L;
			c &= ~j;
			v &= ~j;
			c |= k;
			v |= k;
			c &= ~l;
			v |= l;
			v ^= c;
			break;
		case LibrarySAED.TYPE_AOI22 & 0xff:
			for (int i = 0; i < 2; i++) {
				l |= ~inC[i] & inV[i];
				k |= inC[i] & ~inV[i];
				j |= ~inC[i] & ~inV[i];
			}
			tmpInC[0] = -1L;
			tmpInV[0] = -1L;
			tmpInC[0] &= ~j;
			tmpInV[0] &= ~j;
			tmpInC[0] |= k;
			tmpInV[0] &= ~k;
			tmpInC[0] &= ~l;
			tmpInV[0] |= l;
			l = 0L;
			k = 0L;
			j = 0L;
			for (int i = 2; i < 4; i++) {
				l |= ~inC[i] & inV[i];
				k |= inC[i] & ~inV[i];
				j |= ~inC[i] & ~inV[i];
			}
			tmpInC[1] = -1L;
			tmpInV[1] = -1L;
			tmpInC[1] &= ~j;
			tmpInV[1] &= ~j;
			tmpInC[1] |= k;
			tmpInV[1] &= ~k;
			tmpInC[1] &= ~l;
			tmpInV[1] |= l;
			l = 0L;
			k = 0L;
			j = 0L;
			for (int i = 0; i < 2; i++) {
				l |= ~tmpInC[i] & tmpInV[i];
				k |= tmpInC[i] & tmpInV[i];
				j |= ~tmpInC[i] & ~tmpInV[i];
			}
			c = -1L;
			v = 0L;
			c &= ~j;
			v &= ~j;
			c |= k;
			v |= k;
			c &= ~l;
			v |= l;
			v ^= c;
			break;
		case LibrarySAED.TYPE_OA21 & 0xff:
			for (int i = 0; i < 2; i++) {
				l |= ~inC[i] & inV[i];
				k |= inC[i] & inV[i];
				j |= ~inC[i] & ~inV[i];
			}
			tmpInC[0] = -1L;
			tmpInV[0] = 0L;
			tmpInC[0] &= ~j;
			tmpInV[0] &= ~j;
			tmpInC[0] |= k;
			tmpInV[0] |= k;
			tmpInC[0] &= ~l;
			tmpInV[0] |= l;
			l = 0L;
			k = 0L;
			j = 0L;
			tmpInC[1] = inC[2];
			tmpInV[1] = inV[2];
			for (int i = 0; i < 2; i++) {
				l |= ~tmpInC[i] & tmpInV[i];
				k |= tmpInC[i] & ~tmpInV[i];
				j |= ~tmpInC[i] & ~tmpInV[i];
			}
			c = -1L;
			v = -1L;
			c &= ~j;
			v &= ~j;
			c |= k;
			v &= ~k;
			c &= ~l;
			v |= l;
			break;
		case LibrarySAED.TYPE_OA221 & 0xff:
			for (int i = 0; i < 2; i++) {
				l |= ~inC[i] & inV[i];
				k |= inC[i] & inV[i];
				j |= ~inC[i] & ~inV[i];
			}
			tmpInC[0] = -1L;
			tmpInV[0] = 0L;
			tmpInC[0] &= ~j;
			tmpInV[0] &= ~j;
			tmpInC[0] |= k;
			tmpInV[0] |= k;
			tmpInC[0] &= ~l;
			tmpInV[0] |= l;
			l = 0L;
			k = 0L;
			j = 0L;
			for (int i = 2; i < 4; i++) {
				l |= ~inC[i] & inV[i];
				k |= inC[i] & inV[i];
				j |= ~inC[i] & ~inV[i];
			}
			tmpInC[1] = -1L;
			tmpInV[1] = 0L;
			tmpInC[1] &= ~j;
			tmpInV[1] &= ~j;
			tmpInC[1] |= k;
			tmpInV[1] |= k;
			tmpInC[1] &= ~l;
			tmpInV[1] |= l;
			l = 0L;
			k = 0L;
			j = 0L;
			tmpInC[2] = inC[4];
			tmpInV[2] = inV[4];
			for (int i = 0; i < 3; i++) {
				l |= ~tmpInC[i] & tmpInV[i];
				k |= tmpInC[i] & ~tmpInV[i];
				j |= ~tmpInC[i] & ~tmpInV[i];
			}
			c = -1L;
			v = -1L;
			c &= ~j;
			v &= ~j;
			c |= k;
			v &= ~k;
			c &= ~l;
			v |= l;
			break;
		case LibrarySAED.TYPE_OA222 & 0xff:
			for (int i = 0; i < 2; i++) {
				l |= ~inC[i] & inV[i];
				k |= inC[i] & inV[i];
				j |= ~inC[i] & ~inV[i];
			}
			tmpInC[0] = -1L;
			tmpInV[0] = 0L;
			tmpInC[0] &= ~j;
			tmpInV[0] &= ~j;
			tmpInC[0] |= k;
			tmpInV[0] |= k;
			tmpInC[0] &= ~l;
			tmpInV[0] |= l;
			l = 0L;
			k = 0L;
			j = 0L;
			for (int i = 2; i < 4; i++) {
				l |= ~inC[i] & inV[i];
				k |= inC[i] & inV[i];
				j |= ~inC[i] & ~inV[i];
			}
			tmpInC[1] = -1L;
			tmpInV[1] = 0L;
			tmpInC[1] &= ~j;
			tmpInV[1] &= ~j;
			tmpInC[1] |= k;
			tmpInV[1] |= k;
			tmpInC[1] &= ~l;
			tmpInV[1] |= l;
			l = 0L;
			k = 0L;
			j = 0L;
			for (int i = 4; i < 6; i++) {
				l |= ~inC[i] & inV[i];
				k |= inC[i] & inV[i];
				j |= ~inC[i] & ~inV[i];
			}
			tmpInC[2] = -1L;
			tmpInV[2] = 0L;
			tmpInC[2] &= ~j;
			tmpInV[2] &= ~j;
			tmpInC[2] |= k;
			tmpInV[2] |= k;
			tmpInC[2] &= ~l;
			tmpInV[2] |= l;
			l = 0L;
			k = 0L;
			j = 0L;
			for (int i = 0; i < 3; i++) {
				l |= ~tmpInC[i] & tmpInV[i];
				k |= tmpInC[i] & ~tmpInV[i];
				j |= ~tmpInC[i] & ~tmpInV[i];
			}
			c = -1L;
			v = -1L;
			c &= ~j;
			v &= ~j;
			c |= k;
			v &= ~k;
			c &= ~l;
			v |= l;
			break;
		case LibrarySAED.TYPE_OA22 & 0xff:
			for (int i = 0; i < 2; i++) {
				l |= ~inC[i] & inV[i];
				k |= inC[i] & inV[i];
				j |= ~inC[i] & ~inV[i];
			}
			tmpInC[0] = -1L;
			tmpInV[0] = 0L;
			tmpInC[0] &= ~j;
			tmpInV[0] &= ~j;
			tmpInC[0] |= k;
			tmpInV[0] |= k;
			tmpInC[0] &= ~l;
			tmpInV[0] |= l;
			l = 0L;
			k = 0L;
			j = 0L;
			for (int i = 2; i < 4; i++) {
				l |= ~inC[i] & inV[i];
				k |= inC[i] & inV[i];
				j |= ~inC[i] & ~inV[i];
			}
			tmpInC[1] = -1L;
			tmpInV[1] = 0L;
			tmpInC[1] &= ~j;
			tmpInV[1] &= ~j;
			tmpInC[1] |= k;
			tmpInV[1] |= k;
			tmpInC[1] &= ~l;
			tmpInV[1] |= l;
			l = 0L;
			k = 0L;
			j = 0L;
			for (int i = 0; i < 2; i++) {
				l |= ~tmpInC[i] & tmpInV[i];
				k |= tmpInC[i] & ~tmpInV[i];
				j |= ~tmpInC[i] & ~tmpInV[i];
			}
			c = -1L;
			v = -1L;
			c &= ~j;
			v &= ~j;
			c |= k;
			v &= ~k;
			c &= ~l;
			v |= l;
			break;
		case LibrarySAED.TYPE_OAI21 & 0xff:
			for (int i = 0; i < 2; i++) {
				l |= ~inC[i] & inV[i];
				k |= inC[i] & inV[i];
				j |= ~inC[i] & ~inV[i];
			}
			tmpInC[0] = -1L;
			tmpInV[0] = 0L;
			tmpInC[0] &= ~j;
			tmpInV[0] &= ~j;
			tmpInC[0] |= k;
			tmpInV[0] |= k;
			tmpInC[0] &= ~l;
			tmpInV[0] |= l;
			l = 0L;
			k = 0L;
			j = 0L;
			tmpInC[1] = inC[2];
			tmpInV[1] = inV[2];
			for (int i = 0; i < 2; i++) {
				l |= ~tmpInC[i] & tmpInV[i];
				k |= tmpInC[i] & ~tmpInV[i];
				j |= ~tmpInC[i] & ~tmpInV[i];
			}
			c = -1L;
			v = -1L;
			c &= ~j;
			v &= ~j;
			c |= k;
			v &= ~k;
			c &= ~l;
			v |= l;
			v ^= c;
			break;
		case LibrarySAED.TYPE_OAI221 & 0xff:
			for (int i = 0; i < 2; i++) {
				l |= ~inC[i] & inV[i];
				k |= inC[i] & inV[i];
				j |= ~inC[i] & ~inV[i];
			}
			tmpInC[0] = -1L;
			tmpInV[0] = 0L;
			tmpInC[0] &= ~j;
			tmpInV[0] &= ~j;
			tmpInC[0] |= k;
			tmpInV[0] |= k;
			tmpInC[0] &= ~l;
			tmpInV[0] |= l;
			l = 0L;
			k = 0L;
			j = 0L;
			for (int i = 2; i < 4; i++) {
				l |= ~inC[i] & inV[i];
				k |= inC[i] & inV[i];
				j |= ~inC[i] & ~inV[i];
			}
			tmpInC[1] = -1L;
			tmpInV[1] = 0L;
			tmpInC[1] &= ~j;
			tmpInV[1] &= ~j;
			tmpInC[1] |= k;
			tmpInV[1] |= k;
			tmpInC[1] &= ~l;
			tmpInV[1] |= l;
			l = 0L;
			k = 0L;
			j = 0L;
			tmpInC[2] = inC[4];
			tmpInV[2] = inV[4];
			for (int i = 0; i < 3; i++) {
				l |= ~tmpInC[i] & tmpInV[i];
				k |= tmpInC[i] & ~tmpInV[i];
				j |= ~tmpInC[i] & ~tmpInV[i];
			}
			c = -1L;
			v = -1L;
			c &= ~j;
			v &= ~j;
			c |= k;
			v &= ~k;
			c &= ~l;
			v |= l;
			v ^= c;
			break;
		case LibrarySAED.TYPE_OAI222 & 0xff:
			for (int i = 0; i < 2; i++) {
				l |= ~inC[i] & inV[i];
				k |= inC[i] & inV[i];
				j |= ~inC[i] & ~inV[i];
			}
			tmpInC[0] = -1L;
			tmpInV[0] = 0L;
			tmpInC[0] &= ~j;
			tmpInV[0] &= ~j;
			tmpInC[0] |= k;
			tmpInV[0] |= k;
			tmpInC[0] &= ~l;
			tmpInV[0] |= l;
			l = 0L;
			k = 0L;
			j = 0L;
			for (int i = 2; i < 4; i++) {
				l |= ~inC[i] & inV[i];
				k |= inC[i] & inV[i];
				j |= ~inC[i] & ~inV[i];
			}
			tmpInC[1] = -1L;
			tmpInV[1] = 0L;
			tmpInC[1] &= ~j;
			tmpInV[1] &= ~j;
			tmpInC[1] |= k;
			tmpInV[1] |= k;
			tmpInC[1] &= ~l;
			tmpInV[1] |= l;
			l = 0L;
			k = 0L;
			j = 0L;
			for (int i = 4; i < 6; i++) {
				l |= ~inC[i] & inV[i];
				k |= inC[i] & inV[i];
				j |= ~inC[i] & ~inV[i];
			}
			tmpInC[2] = -1L;
			tmpInV[2] = 0L;
			tmpInC[2] &= ~j;
			tmpInV[2] &= ~j;
			tmpInC[2] |= k;
			tmpInV[2] |= k;
			tmpInC[2] &= ~l;
			tmpInV[2] |= l;
			l = 0L;
			k = 0L;
			j = 0L;
			for (int i = 0; i < 3; i++) {
				l |= ~tmpInC[i] & tmpInV[i];
				k |= tmpInC[i] & ~tmpInV[i];
				j |= ~tmpInC[i] & ~tmpInV[i];
			}
			c = -1L;
			v = -1L;
			c &= ~j;
			v &= ~j;
			c |= k;
			v &= ~k;
			c &= ~l;
			v |= l;
			v ^= c;
			break;
		case LibrarySAED.TYPE_OAI22 & 0xff:
			for (int i = 0; i < 2; i++) {
				l |= ~inC[i] & inV[i];
				k |= inC[i] & inV[i];
				j |= ~inC[i] & ~inV[i];
			}
			tmpInC[0] = -1L;
			tmpInV[0] = 0L;
			tmpInC[0] &= ~j;
			tmpInV[0] &= ~j;
			tmpInC[0] |= k;
			tmpInV[0] |= k;
			tmpInC[0] &= ~l;
			tmpInV[0] |= l;
			l = 0L;
			k = 0L;
			j = 0L;
			for (int i = 2; i < 4; i++) {
				l |= ~inC[i] & inV[i];
				k |= inC[i] & inV[i];
				j |= ~inC[i] & ~inV[i];
			}
			tmpInC[1] = -1L;
			tmpInV[1] = 0L;
			tmpInC[1] &= ~j;
			tmpInV[1] &= ~j;
			tmpInC[1] |= k;
			tmpInV[1] |= k;
			tmpInC[1] &= ~l;
			tmpInV[1] |= l;
			l = 0L;
			k = 0L;
			j = 0L;
			for (int i = 0; i < 2; i++) {
				l |= ~tmpInC[i] & tmpInV[i];
				k |= tmpInC[i] & ~tmpInV[i];
				j |= ~tmpInC[i] & ~tmpInV[i];
			}
			c = -1L;
			v = -1L;
			c &= ~j;
			v &= ~j;
			c |= k;
			v &= ~k;
			c &= ~l;
			v |= l;
			v ^= c;
			break;
		case LibrarySAED.TYPE_MUX21 & 0xff:
			for (int i = 0; i < inCount; i++)
				l |= ~inC[i] & inV[i];
			k |= (inC[2] & ~inV[2] & inC[0] & ~inV[0])
					| (inC[2] & inV[2] & inC[1] & ~inV[1])
					| (inC[0] & ~inV[0] & inC[1] & ~inV[1]);

			j |= (inC[2] & ~inV[2] & inC[0] & inV[0])
					| (inC[2] & inV[2] & inC[1] & inV[1]);
			/*
			 * j |= (inC[0+2] & ~inV[0+2] & inC[0] &
			 * inV[0]) | (inC[0+2] & inV[0+2] &
			 * inC[0+1] & inV[0+1]) | (inC[0] &
			 * inV[0] & inC[0+1] & inV[0+1]);
			 */
			// appropriate implement

			c = 0L;
			v = 0L;
			c |= j;
			v |= j;
			c |= k;
			v &= ~k;
			c &= ~l;
			v |= l;
			break;
		case LibrarySAED.TYPE_MUX41 & 0xff:
			for (int i = 0; i < inCount; i++)
				l |= ~inC[i] & inV[i];
			k |= (inC[4] & ~inV[4] & inC[5] & ~inV[5] & inC[0]
					& ~inV[0])
					| (inC[4] & ~inV[4] & inC[5] & inV[5]
							& inC[1] & ~inV[1])
					| (inC[4] & inV[4] & inC[5] & ~inV[5]
							& inC[2] & ~inV[2])
					| (inC[4] & inV[4] & inC[5] & inV[5] & inC[3]
							& ~inV[3])
					| (inC[0] & ~inV[0] & inC[1] & ~inV[1] & inC[4]
							& ~inV[4])
					| (inC[2] & ~inV[2] & inC[3] & ~inV[3]
							& inC[4] & inV[4])
					| (inC[0] & ~inV[0] & inC[2] & ~inV[2] & inC[5]
							& ~inV[5])
					| (inC[1] & ~inV[1] & inC[3] & ~inV[3]
							& inC[5] & inV[5])
					| (inC[0] & ~inV[0] & inC[1] & ~inV[1] & inC[2]
							& ~inV[2] & inC[3] & ~inV[3]);
			j |= (inC[4] & ~inV[4] & inC[5] & ~inV[5] & inC[0]
					& inV[0])
					| (inC[4] & ~inV[4] & inC[5] & inV[5]
							& inC[1] & inV[1])
					| (inC[4] & inV[4] & inC[5] & ~inV[5]
							& inC[2] & inV[2])
					| (inC[4] & inV[4] & inC[5] & inV[5] & inC[3]
							& inV[3])
					| (inC[0] & inV[0] & inC[1] & inV[1] & inC[4]
							& ~inV[4])
					| (inC[2] & inV[2] & inC[3] & inV[3] & inC[4]
							& inV[4])
					| (inC[0] & inV[0] & inC[2] & inV[2] & inC[5]
							& ~inV[5])
					| (inC[1] & inV[1] & inC[3] & inV[3] & inC[5]
							& inV[5])
					| (inC[0] & inV[0] & inC[1] & inV[1] & inC[2]
							& inV[2] & inC[3] & inV[3]);
			c = 0L;
			v = 0L;
			c |= j;
			v |= j;
			c |= k;
			v &= ~k;
			c &= ~l;
			v |= l;
			break;
		case LibrarySAED.TYPE_FADD_CO & 0xff:
			for (int i = 0; i < inCount; i++)
				l |= ~inC[i] & inV[i];
			k |= (inC[0] & ~inV[0] & inC[1] & ~inV[1])
					| (inC[1] & ~inV[1] & inC[2] & ~inV[2])
					| (inC[0] & ~inV[0] & inC[2] & ~inV[2]);

			j |= (inC[0] & inV[0] & inC[1] & inV[1])
					| (inC[1] & inV[1] & inC[2] & inV[2] & inC[0]
							& ~inV[0])
					| (inC[0] & inV[0] & inC[2] & inV[2] & inC[1]
							& ~inV[1]);
			/*
			 * j |= (inC[0] & inV[0] & inC[0+1] &
			 * inV[0+1]) | (inC[0+1] & inV[0+1] &
			 * inC[0+2] & inV[0+2]) | (inC[0] &
			 * inV[0] & inC[0+2] & inV[0+2]);
			 */
			// appropriate implementation

			c = 0L;
			v = 0L;
			c |= j;
			v |= j;
			c |= k;
			v &= ~k;
			c &= ~l;
			v |= l;
			break;
		case LibrarySAED.TYPE_SDFFAR & 0xff:
		case LibrarySAED.TYPE_SDFFASR & 0xff:
			c = inC[0];
			v = inV[0];
			if (outCount > 0) {
				outC[0] = c;
				outV[0] = v;
			}
			if (outCount > 1) {
				outC[1] = c;
				outV[1] = ~v;
			}
			for (int i = 2; i < outCount; i++) {
				outC[i] = 0L;
				outV[i] = 0L;
			}
			return;
		case LibrarySAED.TYPE_HADD & 0xff:
			if (outCount > 0) {
				sim(Library.TYPE_XOR, inV, inC, 2, tmpInV, tmpInC, 1);
				outC[0] = tmpInC[0];
				outV[0] = tmpInV[0];
			}
			if (outCount > 1) {
				sim(Library.TYPE_AND, inV, inC, 2, tmpInV, tmpInC, 1);
				outC[1] = tmpInC[0];
				outV[1] = tmpInV[0];
			}
			for (int i = 2; i < outCount; i++) {
				outC[i] = 0L;
				outV[i] = 0L;
			}
			return;
		case LibrarySAED.TYPE_FADD & 0xff:
			if (outCount > 0) {
				sim(Library.TYPE_XOR, inV, inC, 3, tmpInV, tmpInC, 1);
				outC[0] = tmpInC[0];
				outV[0] = tmpInV[0];
			}
			if (outCount > 1) {
				sim(LibrarySAED.TYPE_FADD_CO, inV, inC, 3, tmpInV, tmpInC, 1);
				outC[1] = tmpInC[0];
				outV[1] = tmpInV[0];
			}
			for (int i = 2; i < outCount; i++) {
				outC[i] = 0L;
				outV[i] = 0L;
			}
			return;
		case LibrarySAED.TYPE_DEC24 & 0xff:
			if (outCount > 0) {
				sim(Library.TYPE_NOR, inV, inC, 2, tmpInV, tmpInC, 1);
				outC[0] = tmpInC[0];
				outV[0] = tmpInV[0];
			}
			if (outCount > 1) {
				sim(Library.TYPE_BGTA, inV, inC, 2, tmpInV, tmpInC, 1);
				outC[1] = tmpInC[0];
				outV[1] = tmpInV[0];
			}
			if (outCount > 2) {
				sim(Library.TYPE_AGTB, inV, inC, 2, tmpInV, tmpInC, 1);
				outC[2] = tmpInC[0];
				outV[2] = tmpInV[0];
			}
			if (outCount > 3) {
				sim(Library.TYPE_AND, inV, inC, 2, tmpInV, tmpInC, 1);
				outC[3] = tmpInC[0];
				outV[3] = tmpInV[0];
			}
			for (int i = 4; i < outCount; i++) {
				outC[i] = 0L;
				outV[i] = 0L;
			}
			return;
		default:
			throw new RuntimeException("logic function of cell type unknown: 0x" + Integer.toHexString(type & 0xff));
		}

		// fan-out for single-output cells
		for (int i = 0; i < outCount; i++) {
			outC[i] = c;
			outV[i] = v;
		}

	}

}
