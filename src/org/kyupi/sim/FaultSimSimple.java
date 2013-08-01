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

import org.kyupi.data.item.BBlock;
import org.kyupi.data.source.BBSource;
import org.kyupi.faults.FaultState;
import org.kyupi.faults.StuckAtCollection;
import org.kyupi.faults.StuckAtCollection.NodeAssignment;
import org.kyupi.graph.Graph;
import org.kyupi.graph.Library;
import org.kyupi.graph.Graph.Node;
import org.kyupi.misc.ArrayTools;

public class FaultSimSimple extends BBSource {

	private BBPlainSim sim;
	private StuckAtCollection faults;
	private Graph netlist;
	private Library library;
	private int generation;
	private ChangeCollector coll;

	public FaultSimSimple(StuckAtCollection faults, BBSource tests) {
		super(tests.length());
		this.faults = faults;
		this.faults.clear();
		this.netlist = faults.netlist;
		this.library = netlist.library();
		this.sim = new BBPlainSim(netlist, tests);
		coll = new ChangeCollector(tests.length());
	}

	@Override
	public void reset() {
		sim.reset();
		faults.clear();
		generation = 0;
	}

	@Override
	protected BBlock compute() {
		if (!sim.hasNext())
			return null;
		BBlock test = sim.next();
		test.copyTo(-1L, coll);
		generation++;
		for (Node n : netlist.accessNodes()) {
			if (n == null)
				continue;
			NodeAssignment a = faults.na[n.level()][n.position()];
			int iidx = 0;
			for (FaultState fs : a.inputSA0)
				ensureInputFault(fs, n, iidx++, 0L);
			iidx = 0;
			for (FaultState fs : a.inputSA1)
				ensureInputFault(fs, n, iidx++, -1L);
			ensureOutputFault(a.outputSA0, n, 0L);
			ensureOutputFault(a.outputSA1, n, -1L);
		}
		return test;
	}

	private void ensureInputFault(FaultState fs, Node n, int iidx, long value) {
		if (fs != null && fs.generation < generation) {
			//log.debug("simulate input: " + n + " " + iidx + " " + value);
			coll.start();
			if (n.isOutput()) {
				coll.set(n.position(), value);
			} else {
				BBPlainDeltaSim dsim = sim.newDeltaSim();
				injectAtInput(dsim, n, iidx, value);
				dsim.sim(coll);
				dsim.free();
			}
			fs.obs = coll.stopAndReport();
			fs.detects += Long.bitCount(fs.obs);
			fs.generation = generation;
		}
	}

	private void ensureOutputFault(FaultState fs, Node n, long value) {
		if (fs != null && fs.generation < generation) {
			//log.debug("simulate output: " + n + " " + value);
			coll.start();
			BBPlainDeltaSim dsim = sim.newDeltaSim();
			injectAtOutput(dsim, n, value);
			dsim.sim(coll);
			dsim.free();
			fs.obs = coll.stopAndReport();
			fs.detects += Long.bitCount(fs.obs);
			fs.generation = generation;
		}
	}

	private long data[];

	private void injectAtInput(BBPlainDeltaSim dsim, Node node, int input_idx, long value) {
		int input_count = node.maxIn() + 1;
		data = ArrayTools.grow(data, input_count, 4, 0L);
		for (int i = 0; i < input_count; i++) {
			if (i == input_idx) {
				data[i] = value;
			} else {
				Node pred = node.in(i);
				if (pred == null)
					data[i] = 0L;
				else
					data[i] = sim.get(pred);
			}
		}
		injectAtOutput(dsim, node, library.evaluate(node.type(), data, input_count));
	}

	private void injectAtOutput(BBPlainDeltaSim dsim, Node node, long value) {
		dsim.force(node, value);
	}

}
