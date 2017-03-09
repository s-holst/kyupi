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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.apache.log4j.Logger;
import org.kyupi.graph.Graph;
import org.kyupi.graph.Graph.Node;
import org.kyupi.graph.Graph.Node.PinAnnotation;

public class StuckAtCollection {

	protected static Logger log = Logger.getLogger(StuckAtCollection.class);

	public static class StuckAtFault {
		public final PinAnnotation<Boolean> pin;

		private StuckAtFault(PinAnnotation<Boolean> pin) {
			this.pin = pin;
		}

		public static StuckAtFault newInputSA0(Node node, int pin_idx) {
			return new StuckAtFault(node.newInputPinAnnotation(pin_idx, false));
		}

		public static StuckAtFault newInputSA1(Node node, int pin_idx) {
			return new StuckAtFault(node.newInputPinAnnotation(pin_idx, true));
		}

		public static StuckAtFault newOutputSA0(Node node, int pin_idx) {
			return new StuckAtFault(node.newOutputPinAnnotation(pin_idx, false));
		}

		public static StuckAtFault newOutputSA1(Node node, int pin_idx) {
			return new StuckAtFault(node.newOutputPinAnnotation(pin_idx, true));
		}

		public boolean isSA0() {
			return !isSA1();
		}

		public boolean isSA1() {
			return pin.annotation();
		}

		public boolean equals(Object other) {
			if (other instanceof StuckAtFault) {
				StuckAtFault saf = (StuckAtFault) other;
				if (saf.pin.equals(pin))
					return true;
			}
			return false;
		}

		public int hashCode() {
			return pin.hashCode();
		}

		public String toString() {
			return pin.node().queryName() + "/" + pin.name() + "/S@" + (isSA1() ? "1" : "0");
		}
	}

	public final Graph netlist;

	private HashMap<StuckAtFault, StuckAtFault> fault2repfault = new HashMap<>();
	private HashMap<StuckAtFault, ArrayList<StuckAtFault>> repfault2class = new HashMap<>();

	// helper class for fault collapsing.
	private class NodeAssignment {
		Node node;
		StuckAtFault inputSA0[];
		StuckAtFault inputSA1[];
		StuckAtFault outputSA0[];
		StuckAtFault outputSA1[];
		ArrayList<StuckAtFault> faults = new ArrayList<>();

		NodeAssignment(Node n) {
			node = n;

			int n_inputs = node.maxIn() + 1;
			inputSA0 = new StuckAtFault[n_inputs];
			inputSA1 = new StuckAtFault[n_inputs];
			for (int i = 0; i < n_inputs; i++) {
				if (node.in(i) == null)
					continue;
				inputSA0[i] = StuckAtFault.newInputSA0(node, i);
				inputSA1[i] = StuckAtFault.newInputSA1(node, i);
				faults.add(inputSA0[i]);
				faults.add(inputSA1[i]);
			}

			int n_outputs = node.maxOut() + 1;
			outputSA0 = new StuckAtFault[n_outputs];
			outputSA1 = new StuckAtFault[n_outputs];
			for (int i = 0; i < n_outputs; i++) {
				if (node.out(i) == null)
					continue;
				outputSA0[i] = StuckAtFault.newOutputSA0(node, i);
				outputSA1[i] = StuckAtFault.newOutputSA1(node, i);
				faults.add(outputSA0[i]);
				faults.add(outputSA1[i]);
			}
		}
	}

	public NodeAssignment na[][];

	public StuckAtCollection(Graph g) {
		netlist = g;
		int levels = g.levels();
		na = new NodeAssignment[levels][];

		// add S@0, S@1 to each pin of each non-pseudo node in the circuit
		for (int lev = 0; lev < levels; lev++) {
			Node[] level = g.accessLevel(lev);
			NodeAssignment[] nalevel = new NodeAssignment[level.length];
			na[lev] = nalevel;
			for (int pos_idx = 0; pos_idx < level.length; pos_idx++) {
				Node n = level[pos_idx];
				if (n == null || n.isPseudo())
					continue;
				nalevel[pos_idx] = new NodeAssignment(n);
				for (StuckAtFault flt : nalevel[pos_idx].faults) {
					fault2repfault.put(flt, flt);
					ArrayList<StuckAtFault> cls = new ArrayList<>();
					cls.add(flt);
					repfault2class.put(flt, cls);
				}
			}
		}

		// TODO implement fault collapsing

	}

	public int sizeCollapsed() {
		return repfault2class.size();
	}

	public int size() {
		return fault2repfault.size();
	}

	public Set<StuckAtFault> getAllFaultsCollapsed() {
		return repfault2class.keySet();
	}

}
