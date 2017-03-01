/*
 * Copyright 2013 The KyuPI project contributors. See the COPYRIGHT.md file
 * at the top-level directory of this distribution.
 * This file is part of the KyuPI project. It is subject to the license terms
 * in the LICENSE.md file found in the top-level directory of this distribution.
 * No part of the KyuPI project, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.md file.
 */
package org.kyupi.faults;

import org.apache.log4j.Logger;
import org.kyupi.graph.Graph;
import org.kyupi.graph.Graph.Node;
import org.kyupi.graph.Library;


// FIXME proper handling of signals between interface nodes. 
public class StuckAtCollection {

	protected static Logger log = Logger.getLogger(StuckAtCollection.class);

	public class NodeAssignment {
		public FaultState inputSA0[];
		public FaultState inputSA1[];

		public FaultState outputSA0;
		public FaultState outputSA1;

		Node node;

		public NodeAssignment(Node n) {
			node = n;
			int n_inputs = node.maxIn() + 1;
			inputSA0 = new FaultState[n_inputs];
			inputSA1 = new FaultState[n_inputs];
		}
	}

	public NodeAssignment na[][];
	
	public final Graph netlist;
	
	private int collapsedFaultCount;

	public StuckAtCollection(Graph g) {
		netlist = g;
		int levels = g.levels();
		na = new NodeAssignment[levels][];

		// add S@0, S@1 to the input pin of each circuit output.
		Node level[] = g.accessInterface();
		na[0] = new NodeAssignment[level.length];
		for (int pos_idx = 0; pos_idx < level.length; pos_idx++) {
			Node n = level[pos_idx];
			if (n == null || !n.isOutput())
				continue;
			NodeAssignment a = na[0][pos_idx] = new NodeAssignment(n);
			//log.debug("adding faults to inputs of: " + n);
			for (int i = 0; i < a.inputSA0.length; i++) {
				if (a.node.in(i) != null) {
					a.inputSA0[i] = new FaultState();
					a.inputSA1[i] = new FaultState();
					collapsedFaultCount += 2;
				}
			}
		}

		for (int level_idx = levels - 1; level_idx >= 0; level_idx--) {

			level = g.accessLevel(level_idx);
			if (level_idx > 0) 
				na[level_idx] = new NodeAssignment[level.length];

			for (int pos_idx = 0; pos_idx < level.length; pos_idx++) {
				Node n = level[pos_idx];
				if (n == null)
					continue;
				
				if (level_idx == 0 && !(n.isInput() || n.isSequential()))
					continue;

				NodeAssignment a = na[level_idx][pos_idx] = new NodeAssignment(n);

				//log.debug("handling " + n + " is seq " + n.isSequential());
				if (a.node.maxOut() == 0) {
					Node succ = a.node.out(0);
					NodeAssignment succ_a = na[succ.level()][succ.position()];
					a.outputSA0 = succ_a.inputSA0[succ.searchInIdx(a.node)];
					a.outputSA1 = succ_a.inputSA1[succ.searchInIdx(a.node)];
				} else {
					a.outputSA0 = new FaultState();
					a.outputSA1 = new FaultState();
					collapsedFaultCount += 2;
				}

				// faults at input pins of circuit outputs are already added.
				if (n.isOutput())
					continue;
				
				for (int ipin = 0; ipin < a.inputSA0.length; ipin++) {
					if (a.node.isType(Library.TYPE_AND)) {
						a.inputSA0[ipin] = a.outputSA0;
						a.inputSA1[ipin] = new FaultState();
					} else if (a.node.isType(Library.TYPE_NOT)) {
						a.inputSA0[ipin] = a.outputSA1;
						a.inputSA1[ipin] = a.outputSA0;
					} else if (a.node.isType(Library.TYPE_BUF) && !a.node.isSequential()) {
						a.inputSA0[ipin] = a.outputSA0;
						a.inputSA1[ipin] = a.outputSA1;
					} else {
						a.inputSA0[ipin] = new FaultState();
						a.inputSA1[ipin] = new FaultState();
						collapsedFaultCount += 2;
					}
					// TODO: more collapsing
				}

			}
		}
	}
	
	public int numCollapsedFaults() {
		return collapsedFaultCount;
	}

	public void clear() {
		for (NodeAssignment aa[] : na) {
			for (NodeAssignment a : aa) {
				if (a == null)
					continue;
				for (FaultState f : a.inputSA0)
					if (f != null)
						f.clear();
				for (FaultState f : a.inputSA1)
					if (f != null)
						f.clear();
				if (a.outputSA0 != null)
					a.outputSA0.clear();
				if (a.outputSA1 != null)
					a.outputSA1.clear();
			}
		}
	}
}
