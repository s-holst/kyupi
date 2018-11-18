/*
 * Copyright 2013-2018 The KyuPI project contributors. See the COPYRIGHT.md file
 * at the top-level directory of this distribution.
 * This file is part of the KyuPI project. It is subject to the license terms
 * in the LICENSE.md file found in the top-level directory of this distribution.
 * No part of the KyuPI project, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.md file.
 */
package org.kyupi.circuit;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.kyupi.misc.ArrayTools;
import org.kyupi.misc.Namespace;

/**
 * represents a circuit that can be changed.
 * 
 * @author Stefan
 *
 */
public class MutableCircuit extends Circuit {

	/**
	 * is a vertex in the graph containing references to its neighbors and
	 * payload data.
	 */
	public class MutableCell extends Cell {

		private MutableCell inputs[] = new MutableCell[0];

		private MutableCell outputs[] = new MutableCell[0];
		
		private int inputSignals[] = new int[0];
		
		private int outputSignals[] = new int[0];

		public MutableCell(String name, int type) {
			super(namespace.idFor(name), type);
			register(this);
		}
		
		public MutableCell(Cell n) {
			super(namespace.idFor(n.name()), n.type());
			setIntfPosition(n.intfPosition());
			register(this);
		}

		/*
		 * accessors
		 */

		public String name() {
			return namespace.nameFor(id());
		}

		public void setIntfPosition(int pos) {
			if (this.intfPosition() != pos) {
				if (this.intfPosition() != -1) {
					intf[this.intfPosition()] = null;
				}
				this.intfPosition = pos;
				if (pos >= 0) {
					intf = (MutableCell[]) ArrayTools.grow(intf, MutableCell.class, pos+1);
					if (intf[pos] != null) {
						log.error("Nodes must not share same intfPosition: " + name() + ", "
								+ intf[pos].name() + " (intfPosition: " + pos + ")");
					}
					intf[pos] = this;
				}
			}
		}

		public boolean equals(Object other) {
			if (other instanceof MutableCell) {
				MutableCell n = (MutableCell) other;
				if (n.type != type)
					return false;
				if (!n.name().equals(name()))
					return false;
				return true;
			}
			return false;
		}
		
		/*
		 * type queries (convenience accessors to library)
		 */

		public boolean isType(int other) {
			return library.isType(type, other);
		}

		public String typeName() {
			return library.typeName(type);
		}

		public String inputName(int input_pin) {
			return library.inputPinName(type, input_pin);
		}

		public String outputName(int output_pin) {
			return library.outputPinName(type, output_pin);
		}

		/*
		 * edge queries
		 */

		public int countIns() {
			return ArrayTools.countEntries(inputs);
		}

		public int countOuts() {
			return ArrayTools.countEntries(outputs);
		}

		/**
		 * @return the maximum index of an input, or -1 if there are no inputs.
		 */
		public int maxIn() {
			return ArrayTools.maxNonNullIndex(inputs);
		}

		/**
		 * @return the maximum index of an output, or -1 if there are no
		 *         outputs.
		 */
		public int maxOut() {
			return ArrayTools.maxNonNullIndex(outputs);
		}

		public MutableCell inputCellAt(int idx) {
			return (MutableCell) ArrayTools.safeGet(inputs, idx);
		}

		public MutableCell outputCellAt(int idx) {
			return (MutableCell) ArrayTools.safeGet(outputs, idx);
		}

		public int searchOutIdx(MutableCell succ) {
			int i = ArrayTools.linearSearchReference(outputs, succ);
			if (i < 0)
				throw new IllegalArgumentException("given node is not a successor");
			return i;
		}

		public int searchInIdx(MutableCell pred) {
			int i = ArrayTools.linearSearchReference(inputs, pred);
			if (i < 0)
				throw new IllegalArgumentException(
						"given node " + pred.name() + " is not a predecessor of " + name());
			return i;
		}

		public Iterable<MutableCell> inputCells() {
			return Arrays.asList(inputs);
		}

		public Iterable<MutableCell> outputCells() {
			return Arrays.asList(outputs);
		}

		/*
		 * edge manipulations
		 */

		/**
		 * sets or adds an 'in,{idx}'-reference to given predecessor.
		 * 
		 * @param idx
		 *            desired index. If negative, append to existing
		 *            in-references (equivalent to idx=maxIn()+1).
		 * @param pred
		 *            the predecessor node to refer to. 'null' removes an
		 *            existing reference.
		 * @return the node itself.
		 */
		private MutableCell setIn(int idx, MutableCell pred, int sid) {
			if (idx < 0) {
				idx = inputCount();
			}
			inputs = (MutableCell[]) ArrayTools.grow(inputs, MutableCell.class, idx + 1);
			inputs[idx] = pred;
			inputSignals = ArrayTools.grow(inputSignals, idx+1, 2, -1);
			inputSignals[idx] = sid;
			if (sid >= 0) {
				receivers[sid] = this;
				receiverPins[sid] = idx;
			}

			return this;
		}

		/**
		 * sets or adds an 'out,{idx}'-reference to given successor.
		 * 
		 * @param idx
		 *            desired index. If negative, append to existing
		 *            out-references (equivalent to idx=maxOut()+1).
		 * @param succ
		 *            the successor node to refer to. 'null' removes an existing
		 *            reference.
		 * @return the node itself.
		 */
		private MutableCell setOut(int idx, MutableCell succ, int sid) {
			if (idx < 0) {
				idx = outputCount();
			}
			outputs = (MutableCell[]) ArrayTools.grow(outputs, MutableCell.class, idx + 1);
			outputs[idx] = succ;
			outputSignals = ArrayTools.grow(outputSignals, idx+1, 2, -1);
			outputSignals[idx] = sid;
			if (sid >= 0) {
				drivers[sid] = this;
				driverPins[sid] = idx;
			}

			return this;
		}

		/**
		 * replace all in-references to node 'current' with references to node
		 * 'replacement'. replacement may be null to delete references.
		 * 
		 * @param current
		 * @param replacement
		 * @return the node itself.
		 */
		public MutableCell replaceIns(MutableCell current, MutableCell replacement) {
			ArrayTools.replaceAll(inputs, current, replacement);
			return this;
		}

		/**
		 * replace all out-references to node 'current' with references to node
		 * 'replacement'. replacement may be null to delete references.
		 * 
		 * @param current
		 * @param replacement
		 * @return the node itself.
		 */
		public MutableCell replaceOuts(MutableCell current, MutableCell replacement) {
			ArrayTools.replaceAll(outputs, current, replacement);
			return this;
		}

		/**
		 * change the idx of all in-references to the lowest possible value
		 * while preserving their order.
		 * 
		 * @return the node itself.
		 */
		public MutableCell compressIns() {
			ArrayTools.moveToFront(inputs);
			return this;
		}

		/**
		 * change the idx of all out-references to the lowest possible value
		 * while preserving their order.
		 * 
		 * @return the node itself.
		 */
		public MutableCell compressOuts() {
			ArrayTools.moveToFront(outputs);
			return this;
		}

		/**
		 * disconnect node from all its neighbors and remove from graph.
		 */
		public void remove() {
			for (int i = maxIn(); i >= 0; i--) {
				MutableCell pred = inputCellAt(i);
				if (pred == null)
					continue;
				setIn(i, null, -1);
				pred.replaceOuts(this, null);
				if (!pred.isMultiOutput()) {
					pred.compressOuts();
				}
			}

			for (int i = maxOut(); i >= 0; i--) {
				MutableCell succ = outputCellAt(i);
				if (succ == null)
					continue;
				succ.replaceIns(this, null);
				setOut(i, null, -1);
			}
			unregister(this);
		}

		/*
		 * misc
		 */

		public void strip() {
			if (outputs != null)
				outputs = (MutableCell[]) ArrayTools.strip(outputs);
			if (inputs != null)
				inputs = (MutableCell[]) ArrayTools.strip(inputs);
		}

		public String toString() {
			String iosuffix = "";
			if (isInput())
				iosuffix = "I" + intfPosition();
			if (isOutput())
				iosuffix = "O" + intfPosition();
			StringBuilder b = new StringBuilder(
					"" + id() + ":" + typeName() + "\"" + name() + "\"" + iosuffix);
			int m_in = maxIn();
			int m_out = maxOut();
			for (int i = 0; i <= m_in; i++) {
				if (inputs[i] == null) {
					b.append("<null");
				} else {
					b.append(" <" + inputs[i].id());
				}
			}
			for (int o = 0; o <= m_out; o++) {
				if (outputs[o] == null) {
					b.append(">null");
				} else {
					b.append(" >" + outputs[o].id());
				}
			}
			return b.toString();
		}

		@Override
		public int inputCount() {
			return maxIn() + 1;
		}

		@Override
		public int outputCount() {
			return maxOut() + 1;
		}

		@Override
		public int inputSignalAt(int pinIndex) {
			return inputSignals[pinIndex];
		}

		@Override
		public int outputSignalAt(int pinIndex) {
			return outputSignals[pinIndex];
		}

	}
	
	protected static Logger log = Logger.getLogger(MutableCircuit.class);

	private Namespace namespace = new Namespace();

	private final Library library;

	private MutableCell[] nodes = new MutableCell[0];
	
	private MutableCell drivers[] = new MutableCell[0];
	private MutableCell receivers[] = new MutableCell[0];

	private int driverPins[] = new int[0];
	private int receiverPins[] = new int[0];

	private MutableCell levels[][];

	private MutableCell intf[];

	private String name;

	//private SignalMap signalMap;
	
	private int signalCount = 0;
	
	private ArrayList<Integer> signalIdRecycle = new ArrayList<>();

	public MutableCircuit(Library lib) {
		library = lib;
	}
	
	public MutableCircuit(Circuit g) {
		library = g.library();
		name = g.name();
		for (int idx = 0; idx < g.size(); idx++) {
			Cell n = g.cell(idx);
			if (n != null)
				new MutableCell(n);
		}
		
		for (int idx = 0; idx < g.size(); idx++) {
			Cell n = g.cell(idx);
			if (n == null)
				continue;
			int outCount = n.outputCount();
			for (int i = 0; i < outCount; i++) {
				Cell succ = n.outputCellAt(i);
				if (succ != null) {
					int sid = newSignalId();
					nodes[idx].setOut(i, nodes[succ.id()], sid);
				}
			}
		}
		
		for (int idx = 0; idx < g.size(); idx++) {
			Cell n = g.cell(idx);
			if (n == null)
				continue;
			int inCount = n.inputCount();
			for (int i = 0; i < inCount; i++) {
				Cell pred = n.inputCellAt(i);
				int sig = n.inputSignalAt(i);
				int predIdx = g.driverPinOf(sig);
				if (pred != null)
					nodes[idx].setIn(i, nodes[pred.id()], nodes[pred.id()].outputSignals[predIdx]);
			}
		}
	}

	public Library library() {
		return library;
	}

	public MutableCell searchCellByName(String name) {
		if (!namespace.contains(name)) {
			return null;
		}
		return nodes[namespace.idFor(name)];
	}

	public int countInputs() {
		MutableCell n[] = nodes;
		if (intf != null)
			n = intf;
		int count = 0;
		for (MutableCell g : n) {
			if (g != null && g.isInput())
				count++;
		}
		return count;
	}

	public int countOutputs() {
		MutableCell n[] = nodes;
		if (intf != null)
			n = intf;
		int count = 0;
		for (MutableCell g : n) {
			if (g != null && g.isOutput())
				count++;
		}
		return count;
	}

	public int countSequentials() {
		MutableCell n[] = nodes;
		if (levels != null)
			n = levels[0];
		int count = 0;
		for (MutableCell g : n) {
			if (g != null && g.isSequential())
				count++;
		}
		return count;
	}

	public int countNodes() {
		int count = 0;
		for (MutableCell g : nodes) {
			if (g != null)
				count++;
		}
		return count;
	}
	
	public int countNonPseudoNodes() {
		int count = 0;
		for (MutableCell g : nodes) {
			if (g != null && !g.isPseudo())
				count++;
		}
		return count;
	}

	/**
	 * returns an array containing all interface (port) nodes of the Graph in
	 * proper order.
	 * 
	 * The order of the nodes in the returned array is the same as the order of
	 * the bits in patterns etc.. The returned array may contain null elements.
	 * It is a reference to an internal Graph data structure. It must not be
	 * changed by the caller.
	 * 
	 * @return
	 */
	public Iterable<MutableCell> intf() {
		return Arrays.asList(intf);
	}
	
	public MutableCell intf(int pos) {
		return intf[pos];
	}

	public int width() {
		return ArrayTools.maxNonNullIndex(intf) + 1;
	}
	/**
	 * returns an array containing all nodes present in the graph.
	 * 
	 * The returned array can be used to loop over all the nodes in a Graph. It
	 * may contain null elements. It is a reference to an internal Graph data
	 * structure. It must not be changed by the caller.
	 * 
	 * @return
	 */
	public Iterable<MutableCell> cells() {
		return Arrays.asList(nodes);
	}
	
	public MutableCell cell(int id) {
		return nodes[id];
	}
	
	public int size() {
		return nodes.length;
	}

	public void connect(MutableCell driver, int out_idx, MutableCell receiver, int in_idx) {
		MutableCell c = driver.outputCellAt(out_idx);
		if (c != null) {
			disconnect(driver, out_idx, c, c.searchInIdx(driver));
		}
		c = receiver.inputCellAt(in_idx);
		if (c != null) {
			disconnect(c, c.searchOutIdx(receiver), receiver, in_idx);
		}
		int sid = newSignalId();
		driver.setOut(out_idx, receiver, sid);
		receiver.setIn(in_idx, driver, sid);
	}

	public void disconnect(MutableCell driver, int out_idx, MutableCell receiver, int in_idx) {
		if (driver.outputCellAt(out_idx) != receiver)
			throw new IllegalArgumentException("specified driver output does not point to receiver.");
		if (receiver.inputCellAt(in_idx) != driver)
			throw new IllegalArgumentException("specified receiver input does not point to driver.");
		freeSignalId(driver.outputSignals[out_idx]);
		driver.setOut(out_idx, null, -1);
		receiver.setIn(in_idx, null, -1);
	}

	public void strip() {
		nodes = (MutableCell[]) ArrayTools.strip(nodes);
		for (MutableCell g : nodes) {
			if (g != null)
				g.strip();
		}
	}

	public String toString() {
		StringBuilder b = new StringBuilder();
		for (MutableCell node : nodes) {
			b.append(node);
			b.append("\n");
		}
		return b.toString();
	}
	
	public LevelizedCircuit levelized() {
		return new LevelizedCircuit(this);
	}

	private void register(MutableCell g) {
		//log.info("register: " + namespace.nameFor(g.id()) + " " + g.id());
		if (nodes.length > g.id() && nodes[g.id()] != null) {
			throw new IllegalArgumentException("Gate already exists: " + namespace.nameFor(g.id()));
		}
		nodes = (MutableCell[]) ArrayTools.grow(nodes, MutableCell.class, g.id() + 1);
		nodes[g.id()] = g;
	}

	private void unregister(MutableCell g) {
		if (nodes.length > g.id() && nodes[g.id()] == g) {
			nodes[g.id()] = null;
		} else
			throw new IllegalArgumentException("Gate does not exist: " + namespace.nameFor(g.id()));
	}

	public void setName(String name) {
		this.name = name;
	}

	public String name() {
		return this.name;
	}
	
	public boolean equals(Object other) {
		if (other instanceof MutableCircuit) {
			MutableCircuit g = (MutableCircuit) other;
			if (g.countNodes() != countNodes())
				return false;
			for (MutableCell n : cells()) {
				if (n == null)
					continue;
				MutableCell other_n = g.searchCellByName(n.name());
				if (other_n == null)
					return false;
				if (!n.equals(other_n))
					return false;
				if (n.maxIn() != other_n.maxIn())
					return false;
				for (int i = 0; i <= n.maxIn(); i++) {
					MutableCell neighbor = n.inputCellAt(i);
					MutableCell other_neighbor = other_n.inputCellAt(i);
					if (neighbor == null && other_neighbor != null)
						return false;
					if (neighbor != null && other_neighbor == null)
						return false;
					if (neighbor != null) {
						if (!neighbor.equals(other_neighbor))
							return false;
					}
				}
				if (n.maxOut() != other_n.maxOut())
					return false;
				for (int i = 0; i <= n.maxOut(); i++) {
					MutableCell neighbor = n.outputCellAt(i);
					MutableCell other_neighbor = other_n.outputCellAt(i);
					if (neighbor == null && other_neighbor != null)
						return false;
					if (neighbor != null && other_neighbor == null)
						return false;
					if (neighbor != null) {
						if (!neighbor.equals(other_neighbor))
							return false;
					}
				}

			}
			return true;
		}
		return false;
	}
	
	public int signalCount() {
		return signalCount;
	}
	
	public MutableCell driverOf(int signalID) {
		return drivers[signalID];
	}

	public MutableCell readerOf(int signalID) {
		return receivers[signalID];
	}
	
	public int driverPinOf(int signalID) {
		return driverPins[signalID];
	}

	public int readerPinOf(int signalID) {
		return receiverPins[signalID];
	}
	
	private int newSignalId() {
		signalCount++;
		if (signalIdRecycle.isEmpty()) {
			drivers = (MutableCell[]) ArrayTools.grow(drivers, MutableCell.class, signalCount);
			receivers = (MutableCell[]) ArrayTools.grow(receivers, MutableCell.class, signalCount);
			driverPins = ArrayTools.grow(driverPins, signalCount, 32, -1);
			receiverPins = ArrayTools.grow(receiverPins, signalCount, 32, -1);
			return signalCount - 1;
		} else {
			return signalIdRecycle.remove(signalIdRecycle.size()-1);
		}
	}
	
	private void freeSignalId(int sid) {
		drivers[sid] = null;
		receivers[sid] = null;
		driverPins[sid] = -1;
		receiverPins[sid] = -1;
		signalCount--;
		signalIdRecycle.add(sid);
	}

}
