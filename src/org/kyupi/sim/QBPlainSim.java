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
import org.kyupi.data.item.QBlock;
import org.kyupi.data.source.QBSource;
import org.kyupi.graph.Graph;
import org.kyupi.graph.Graph.Node;
import org.kyupi.graph.Library;
import org.kyupi.misc.ArrayTools;

public class QBPlainSim extends QBSource {

	protected static Logger log = Logger.getLogger(BBPlainSim.class);

	private long[][] value;
	private long[][] care;

	private QBSource inputData;
	private Graph netlist;

	private Library library;

	public QBPlainSim(Graph netlist, QBSource inputData) {
		super(inputData.length());
		if (netlist.accessInterface().length > inputData.length()) {
			throw new IllegalArgumentException("insufficient data width for the interface of the netlist.");
		}
		this.inputData = inputData;
		this.netlist = netlist;
		int levels = netlist.levels();
		this.library = netlist.library();

		value = new long[levels][];
		care = new long[levels][];
		for (int l = 0; l < levels; l++) {
			int num = netlist.accessLevel(l).length;
			value[l] = new long[num];
			care[l] = new long[num];
		}
	}

	private void setInputData(QBlock b) {
		int pos = -1;
		for (Node intf : netlist.accessInterface()) {
			pos++;
			if (intf == null)
				continue;
			if (intf.isInput() || intf.isSequential()) {
				value[0][pos] = b.getV(pos);
				care[0][pos] = b.getC(pos);
			}
		}
	}

	private void getOutputData(QBlock b) {
		int pos = -1;
		for (Node intf : netlist.accessInterface()) {
			pos++;
			if (intf == null)
				continue;
			if (intf.isOutput() || intf.isSequential()) {
				simNode(intf);
				b.set(pos, value[0][pos], care[0][pos]);
			}
		}
	}

	private long[] dataV;
	private long[] dataC;
	private long[] cv;

	private void simNode(Node n) {
		int input_count = n.maxIn() + 1;
		dataV = ArrayTools.grow(dataV, input_count, 4, 0L);
		dataC = ArrayTools.grow(dataC, input_count, 4, 0L);
		for (int i = 0; i < input_count; i++) {
			Node pred = n.in(i);
			if (pred == null) {
				dataV[i] = 0L;
				dataC[i] = 0L;
			} else {
				dataV[i] = value[pred.level()][pred.position()];
				dataC[i] = care[pred.level()][pred.position()];
				if (pred.isMultiOutput()) {
					cv = library.calcOutput(pred.type(), pred.searchOutIdx(n), dataV[i], dataC[i]);
					dataV[i] = cv[1];
					dataC[i] = cv[0];
				}
			}
		}
		cv = library.evaluate(n.type(), dataV, dataC, input_count);

		care[n.level()][n.position()] = cv[0];
		value[n.level()][n.position()] = cv[1];
	}

	private void simLevel(int level) {
		for (Node n : netlist.accessLevel(level)) {
			simNode(n);
		}
	}

	protected QBlock compute() {
		if (!inputData.hasNext())
			return null;
		QBlock b = inputData.next();
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
		inputData.reset();
	}
}
