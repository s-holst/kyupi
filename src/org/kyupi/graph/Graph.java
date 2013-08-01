/*
 * Copyright 2013 The KyuPI project contributors. See the COPYRIGHT.md file
 * at the top-level directory of this distribution.
 * This file is part of the KyuPI project. It is subject to the license terms
 * in the LICENSE.md file found in the top-level directory of this distribution.
 * No part of the KyuPI project, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.md file.
 */
package org.kyupi.graph;

import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.kyupi.misc.ArrayTools;
import org.kyupi.misc.Namespace;

/**
 * is a directed, node-annotated graph.
 * 
 * Use this data structure to represent logic circuits or other graph-based
 * information.
 * <p/>
 * 
 * Edges in this graph are represented by references of a node to its neighbors.
 * Usually, a pair of references defines an edge (e.g. a signal) between two
 * nodes (e.g. logic cells). Each reference has two annotations: The type of
 * connection ('In', 'Out') of the node it originates from (e.g. input pin or
 * output pin of a cell), and a pin index.
 * <p/>
 * 
 * Example: This is a signal between output 0 of cell A and input 2 of cell B:
 * 
 * <pre>
 *       in,2     out,0
 *        ____    ____  
 * node  v    \  /    v
 *   +-----+   \/   +-----+
 *   |  A  |   /\___|2 B  |
 *   |    0|__/     |1    |
 *   |     |        |0    |
 *   +-----+        +-----+
 *       outp      inp
 * </pre>
 * 
 * The annotation for all references originating at a specific node is unique.
 * E.g. there cannot be two references marked 'out,0' at a single node.
 * <p/>
 * 
 * Performance note: Reference annotations are considered to be dense and stored
 * in arrays. Always use the lowest possible index for maximum memory
 * efficiency.
 * <p/>
 * 
 * To add additional annotations to an edge, create nodes the edges can pass
 * through at add annotation to these nodes.
 * <p/>
 * 
 * A graph has a Library of possible node types associated with it.
 * 
 * Nodes are topologically ordered. Interface nodes go to level 0, all remaining
 * nodes are sorted into the succeeding levels depending on their neighbors.
 * Each node has a position on a level. For level 0, this position must be
 * assigned explicitly. The positions of the remaining levels are determined
 * automatically.
 * 
 * Level 0 is also called the 'interface'.
 * 
 * @author stefan
 * 
 */
public class Graph {

	/**
	 * is a vertex in the graph containing references to its neighbors and
	 * payload data.
	 */
	public class Node {

		private final int id;

		private int type;

		private int level;

		private int position;

		private Node inputs[], outputs[];

		public Node(String name, int type) {
			this.id = namespace.idFor(name);
			this.type = type;
			register(this);
		}

		/*
		 * accessors
		 */

		public String queryName() {
			return namespace.nameFor(id);
		}

		public int type() {
			return type;
		}

		public int level() {
			ensureLevels();
			return level;
		}

		public int position() {
			if (!isInterface())
				ensureLevels();
			return position;
		}

		public void setPosition(int pos) {
			if (this.position != pos) {
				invalidateLevels();
				this.position = pos;
			}
		}

		public int id() {
			return id;
		}

		/*
		 * type queries
		 */

		public boolean isOutput() {
			return library.isOutput(type);
		}

		public boolean isInput() {
			return library.isInput(type);
		}

		public boolean isSequential() {
			return library.isSequential(type);
		}

		public boolean isPort() {
			return library.isPort(type);
		}

		public boolean isInterface() {
			return library.isInterface(type);
		}

		public boolean isMultiOutput() {
			return library.isMultiOutput(type);
		}

		public boolean isPseudo() {
			return library.isPseudo(type);
		}

		public boolean isPrimary() {
			return library.isPrimary(type);
		}

		public boolean isType(int other) {
			return library.isType(type, other);
		}
		
		public String typeName() {
			return library.typeName(type);
		}
		
		public String inName(int input_pin) {
			return library.inputPinName(type, input_pin);
		}

		public String outName(int output_pin) {
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

		public int maxIn() {
			return ArrayTools.maxIndex(inputs);
		}

		public int maxOut() {
			return ArrayTools.maxIndex(outputs);
		}

		public Node in(int idx) {
			return (Node) ArrayTools.safeGet(inputs, idx);
		}

		public Node out(int idx) {
			return (Node) ArrayTools.safeGet(outputs, idx);
		}

		public int searchOutIdx(Node succ) {
			int i = ArrayTools.linearSearchReference(outputs, succ);
			if (i < 0)
				throw new IllegalArgumentException("given node is not a successor");
			return i;
		}

		public int searchInIdx(Node pred) {
			int i = ArrayTools.linearSearchReference(inputs, pred);
			if (i < 0)
				throw new IllegalArgumentException("given node is not a predecessor");
			return i;
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
		public Node setIn(int idx, Node pred) {
			invalidateLevels();
			if (idx < 0) {
				idx = maxIn() + 1;
			}
			inputs = (Node[]) ArrayTools.grow(inputs, Node.class, idx + 1);
			inputs[idx] = pred;
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
		public Node setOut(int idx, Node succ) {
			invalidateLevels();
			if (idx < 0) {
				idx = maxOut() + 1;
			}
			outputs = (Node[]) ArrayTools.grow(outputs, Node.class, idx + 1);
			outputs[idx] = succ;
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
		public Node replaceIns(Node current, Node replacement) {
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
		public Node replaceOuts(Node current, Node replacement) {
			ArrayTools.replaceAll(outputs, current, replacement);
			return this;
		}

		/**
		 * change the idx of all in-references to the lowest possible value
		 * while preserving their order.
		 * 
		 * @return the node itself.
		 */
		public Node compressIns() {
			ArrayTools.moveToFront(inputs);
			return this;
		}

		/**
		 * change the idx of all out-references to the lowest possible value
		 * while preserving their order.
		 * 
		 * @return the node itself.
		 */
		public Node compressOuts() {
			ArrayTools.moveToFront(outputs);
			return this;
		}

		/**
		 * disconnect node from all its neighbors and remove from graph.
		 */
		public void remove() {
			for (int i = maxIn(); i >= 0; i--) {
				Node pred = in(i);
				if (pred == null)
					continue;
				setIn(i, null);
				pred.replaceOuts(this, null);
				if (!pred.isMultiOutput()) {
					pred.compressOuts();
				}
			}

			for (int i = maxOut(); i >= 0; i--) {
				Node succ = out(i);
				if (succ == null)
					continue;
				succ.replaceIns(this, null);
				setOut(i, null);
			}
			unregister(this);
		}

		/*
		 * misc
		 */

		public void strip() {
			if (outputs != null)
				outputs = (Node[]) ArrayTools.strip(outputs);
			if (inputs != null)
				inputs = (Node[]) ArrayTools.strip(inputs);
		}

		public String toString() {
			StringBuilder b = new StringBuilder("" + level + "_" + position + ":" + typeName() + "\"" + queryName() + "\"");
			int m_in = maxIn();
			int m_out = maxOut();
			for (int i = 0; i <= m_in; i++) {
				if (inputs[i] == null) {
					b.append("<null");
				} else {
					b.append(" <" + inputs[i].level + "_" + inputs[i].position);
				}
			}
			for (int o = 0; o <= m_out; o++) {
				if (outputs[o] == null) {
					b.append(">null");
				} else {
					b.append(" >" + outputs[o].level + "_" + outputs[o].position);
				}
			}
			return b.toString();
		}

	}

	protected static Logger log = Logger.getLogger(Graph.class);

	private Namespace namespace = new Namespace();

	private final Library library;

	private Node[] nodes = new Node[0];

	private Node levels[][];

	public Graph(Library lib) {
		library = lib;
	}

	public Library library() {
		return library;
	}

	public Node searchNode(String name) {
		if (!namespace.contains(name)) {
			return null;
		}
		return nodes[namespace.idFor(name)];
	}

	public int countInputs() {
		Node n[] = nodes;
		if (levels != null)
			n = levels[0];
		int count = 0;
		for (Node g : n) {
			if (g != null && g.isInput())
				count++;
		}
		return count;
	}

	public int countOutputs() {
		Node n[] = nodes;
		if (levels != null)
			n = levels[0];
		int count = 0;
		for (Node g : n) {
			if (g != null && g.isOutput())
				count++;
		}
		return count;
	}

	public int countNodes() {
		int count = 0;
		for (Node g : nodes) {
			if (g != null)
				count++;
		}
		return count;
	}

	/**
	 * returns the number of levels in the topologically ordered Graph.
	 * 
	 * @return
	 */
	public int levels() {
		ensureLevels();
		return levels.length;
	}
	
	public Node[] accessLevel(int l) {
		ensureLevels();
		return levels[l];
	}

	public Node[] accessInterface() {
		return accessLevel(0);
	}

	/**
	 * returns an array with all nodes contained in the graph.
	 * 
	 * If nodes have been removed from the graph previously, the array may
	 * contain null elements. The returned array is a reference to an internal
	 * Graph data structure. It should not be changed my the caller.
	 * 
	 * @return
	 */
	public Node[] accessNodes() {
		return nodes;
	}

	public void connect(Node driver, int out_idx, Node receiver, int in_idx) {
		driver.setOut(out_idx, receiver);
		receiver.setIn(in_idx, driver);
	}

	public void disconnect(Node driver, int out_idx, Node receiver, int in_idx) {
		if (driver.out(out_idx) != receiver)
			throw new IllegalArgumentException("specified driver output does not point to receiver.");
		if (receiver.in(in_idx) != driver)
			throw new IllegalArgumentException("specified receiver input does not point to driver.");
		driver.setOut(out_idx, null);
		receiver.setIn(in_idx, null);
	}

	public void strip() {
		nodes = (Node[]) ArrayTools.strip(nodes);
		for (Node g : nodes) {
			if (g != null)
				g.strip();
		}
	}

	public String toString() {
		ensureLevels();
		StringBuilder b = new StringBuilder();
		int level_idx = 0;
		for (Node level[] : levels) {
			int node_idx = 0;
			b.append("" + level_idx + "[ ");
			for (Node node : level) {
				b.append("" + node_idx + "(");
				b.append(node);
				b.append(") ");
				node_idx++;
			}
			level_idx++;
			b.append("]\n");
		}
		return b.toString();
	}

	private void register(Node g) {
		invalidateLevels();
		if (nodes.length > g.id && nodes[g.id] != null) {
			throw new IllegalArgumentException("Gate already exists: " + namespace.nameFor(g.id));
		}
		nodes = (Node[]) ArrayTools.grow(nodes, Node.class, g.id + 1);
		nodes[g.id] = g;
	}

	private void unregister(Node g) {
		invalidateLevels();
		if (nodes.length > g.id && nodes[g.id] == g) {
			nodes[g.id] = null;
		} else
			throw new IllegalArgumentException("Gate already exists: " + namespace.nameFor(g.id));
	}

	private void invalidateLevels() {
		levels = null;
	}

	private void ensureLevels() {
		if (levels != null)
			return;
		//log.debug("levelizing\n\t" + StringTools.join(Thread.currentThread().getStackTrace(), "\n\t"));
		
		Node level0[] = null;
		LinkedList<Node> queue = new LinkedList<Node>();
		ArrayList<Node> non_intf_nodes = new ArrayList<Node>();
		int level_fills[] = new int[1];
		int maxlevel = 0;

		// interface nodes (level[0]), reset positions of all non-interface
		// nodes.
		for (Node g : nodes) {
			if (g == null)
				continue;
			g.level = -1;
			if (library.isInterface(g.type)) {
				level0 = (Node[]) ArrayTools.grow(level0, Node.class, g.position + 1, 0.5f);
				if (level0[g.position] != null) {
					log.error("Conflicting positions in interface nodes: " + g.queryName() + ", " + level0[g.position].queryName());
				}
				level0[g.position] = g;
				queue.add(g);
			} else if (g.maxIn() < 0) {
				queue.add(g);
				g.position = 0;
				non_intf_nodes.add(g);
			} else {
				g.position = 0;
				non_intf_nodes.add(g);
			}
		}
		level0 = (Node[]) ArrayTools.strip(level0);
		level_fills[0] = level0.length;

		// set levels and positions of all nodes.
		while (!queue.isEmpty()) {
			Node g = queue.poll();
			if (g.level != -1) {
				throw new RuntimeException("Detected combinational loop at gate: " + g.queryName());
			}
			g.level = 0;
			if (!library.isInterface(g.type)) {
				for (int i = g.maxIn(); i >= 0; i--) {
					Node d = g.in(i);
					if (d != null)
						g.level = Math.max(g.level, d.level + 1);
				}
				if (g.level == 0) {
					// node with no inputs, place at level 1
					g.level = 1;
				}
			}
			level_fills = ArrayTools.grow(level_fills, g.level + 1, 1000, 0);
			if (g.level > 0)
				g.position = level_fills[g.level]++;
			maxlevel = Math.max(maxlevel, g.level);
			// log.debug("gate " + g.getName() + " is level " + g.level);
			for (int i = g.countOuts() - 1; i >= 0; i--) {
				Node succ = g.out(i);
				if (succ != null && !library.isInterface(succ.type)) {
					succ.position++;
					if (succ.position == succ.countIns())
						queue.add(succ);
				}
			}
		}

		// allocate levels and add nodes.
		levels = new Node[maxlevel + 1][];
		levels[0] = level0;
		for (int i = 1; i <= maxlevel; i++)
			levels[i] = new Node[level_fills[i]];
		for (Node g : non_intf_nodes) {
			if (g.level == -1)
				log.trace("Found unconnected gate: " + g.queryName());
			// throw new RuntimeException("Found unconnected gate: " +
			// g.getName());
			else
				levels[g.level][g.position] = g;
		}

		// log.debug("got levels: " + levels.length);
	}

}
