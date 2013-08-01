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

import org.apache.log4j.Logger;
import org.kyupi.data.item.BBlock;
import org.kyupi.data.source.BBSource;
import org.kyupi.graph.Graph;
import org.kyupi.graph.Library;
import org.kyupi.graph.Graph.Node;
import org.kyupi.misc.ArrayTools;
import org.kyupi.misc.Pool;

public class BBPlainSim extends BBSource {

	protected static Logger log = Logger.getLogger(BBPlainSim.class);

	private long[][] value;

	private BBSource src;

	private Graph netlist;

	private Library library;

	public BBPlainSim(Graph netlist, BBSource inputData) {
		super(inputData.length());
		if (netlist.accessInterface().length > inputData.length()) {
			throw new IllegalArgumentException("insufficient data with for the interface of the netlist.");
		}
		this.src = inputData;
		this.netlist = netlist;
		this.library = netlist.library();

		value = allocWorkingSet();
	}
	
	long[][] allocWorkingSet() {
		int levels = netlist.levels();

		long value[][] = new long[levels][];

		for (int l = 0; l < levels; l++) {
			Node[] level = netlist.accessLevel(l);
			int num = level.length;
			for (Node n : level) {
				if (n != null && n.isMultiOutput())
					throw new IllegalArgumentException("Graphs with Multi-Output Nodes like " + n.queryName()
							+ " are not supported by simulation. Split them first.");
			}
			value[l] = new long[num];
		}

		return value;
	}
	
	Graph graph() {
		return netlist;
	}
	
	public long get(Node node) {
		int l = node.level();
		int p = node.position();
		return value[l][p];
	}

	private void setInputData(BBlock b) {
		int pos = -1;
		for (Node intf : netlist.accessInterface()) {
			pos++;
			if (intf != null && intf.isInput()) {
				value[0][pos] = b.get(pos);
			}
		}
	}

	private void getOutputData(BBlock b) {
		int pos = -1;
		for (Node intf : netlist.accessInterface()) {
			pos++;
			if (intf != null && intf.isOutput()) {
				b.set(pos, simNode(intf));
			}
		}
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
		return library.evaluate(n.type(), data, input_count);
	}

	private void simLevel(int level) {
		for (Node n : netlist.accessLevel(level)) {
			if (n != null)
				value[n.level()][n.position()] = simNode(n);
		}
	}

	@Override
	protected BBlock compute() {
		if (!src.hasNext())
			return null;
		BBlock b = src.next();
		setInputData(b);
		int levels = netlist.levels();
		for (int i = 1; i < levels; i++) {
			simLevel(i);
		}
		getOutputData(b);
		return b;
	}

	@Override
	public void reset() {
		src.reset();
	}

	
	public BBPlainDeltaSim newDeltaSim() {
		return pool.alloc();
	}
	
	/*
	 * BBPlainDeltaSim pool
	 */

	protected Pool<BBPlainDeltaSim> pool = new Pool<BBPlainDeltaSim>() {
		public BBPlainDeltaSim produce() {
			BBPlainDeltaSim v = produceDeltaSim();
			v.setPool(pool);
			return v;
		}
	};
	
	private BBPlainDeltaSim produceDeltaSim() {
		return new BBPlainDeltaSim(this);
	}
}
