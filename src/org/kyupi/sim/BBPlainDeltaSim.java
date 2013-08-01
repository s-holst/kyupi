/*
 * Copyright 2013 The KyuPI project contributors. See the COPYRIGHT.md file
 * at the top-level directory of this distribution.
 * This file is part of the KyuPI project. It is subject to the license terms
 * in the LICENSE.md file found in the top-level directory of this distribution.
 * No part of the KyuPI project, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.md file.
 */
package org.kyupi.sim;

import java.util.BitSet;
import java.util.HashSet;

import org.kyupi.data.item.BBlock;
import org.kyupi.graph.Graph.Node;
import org.kyupi.misc.ArrayTools;
import org.kyupi.misc.Pool;

public class BBPlainDeltaSim {

	private long[][] forced_value;
	private int[][] forced_generation;
	private int generation = 1;

	private BitSet[] todo_sets;
	private BBPlainSim parent;

	public BBPlainDeltaSim(BBPlainSim parent) {
		this.parent = parent;
		forced_value = parent.allocWorkingSet();
		forced_generation = new int[forced_value.length][];
		todo_sets = new BitSet[forced_value.length];
	
		for (int i = 0; i < forced_value.length; i++) {
			forced_generation[i] = new int[forced_value[i].length];
			todo_sets[i] = new BitSet(forced_value[i].length);
		}
	}
	
	HashSet<Integer> pinned = new HashSet<>();

	public long get(Node node) {
		int l = node.level();
		int p = node.position();
		if (generation == forced_generation[l][p]) {
			return forced_value[l][p];
		}
		return parent.get(node);
	}

	public void force(Node node, long value) {
		if (node.level() > 0)
			pinned.add(node.id());
		setAndSchedule(node, value);
		todo_sets[node.level()].clear(node.position());
	}

	private void setAndSchedule(Node node, long value) {
		int level = node.level();
		int pos = node.position();
		forced_generation[level][pos] = generation;
		forced_value[level][pos] = value;
		scheduleSuccessors(node);
	}

	private void scheduleSuccessors(Node node) {
		int m_out = node.maxOut();
		for (int i = 0; i <= m_out; i++) {
			Node succ = node.out(i);
			if (succ != null) {
				int sl = succ.level();
				if (pinned.contains(succ.id())) {
					todo_sets[sl].clear(succ.position());
				} else {
					todo_sets[sl].set(succ.position());
				}
			}
		}
	}

	public void clearForced() {
		generation++;
		for (BitSet bs : todo_sets) {
			bs.clear();
		}
		pinned.clear();
	}

	private long[] data;

	private long simNode(Node n) {
		int input_count = n.maxIn() + 1;
		data = ArrayTools.grow(data, input_count, 4, 0L);
		for (int i = 0; i < input_count; i++) {
			Node pred = n.in(i);
			if (pred == null)
				data[i] = 0L;
			else
				data[i] = get(pred);
		}
		return parent.graph().library().evaluate(n.type(), data, input_count);
	}

	private void deltaSimLevel(int level, BBlock partial_result) {
		BitSet bs = todo_sets[level];
		for (int pos = bs.nextSetBit(0); pos >= 0; pos = bs.nextSetBit(pos + 1)) {
			Node node = parent.graph().accessLevel(level)[pos];
			long result = simNode(node);
			if (level == 0) {
				partial_result.set(pos, result);
			} else if (result != parent.get(node)) {
				setAndSchedule(node, result);
			}
		}
	}

	public void sim(BBlock partial_result) {
		int levels = parent.graph().levels();
		for (int level = 1; level < levels; level++) {
			deltaSimLevel(level, partial_result);
		}
		deltaSimLevel(0, partial_result);
	}

	
	/*
	 * pooled object
	 */

	protected Pool<BBPlainDeltaSim> pool;

	public void setPool(Pool<BBPlainDeltaSim> pool) {
		this.pool = pool;
	}

	public void free() {
		clearForced();
		if (pool != null) {
			pool.free(this);
		}
	}

}
