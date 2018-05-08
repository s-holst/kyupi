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
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.kyupi.misc.ArrayTools;
import org.kyupi.misc.Namespace;

/**
 * is a directed, cycle-free, node-annotated graph.
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
 * Nodes are topologically ordered. The first level (level 0) contains all nodes
 * that have either no predecessors (e.g. primary inputs or constants), or are
 * sequential elements (n.isSequential()==true). All remaining nodes are
 * assigned to the lowest possible level for which all of its predecessors are
 * on even lower levels. Outgoing edges always point to a node on a higher
 * level. Each node has a position on a level which is automatically assigned.
 * Each node in the graph can be uniquely addressed using its level and position
 * index.
 * 
 * Performance note: Topological ordering of the graph is done on demand after a
 * structural change in the graph. When making numerous structural changes, be
 * sure not to unnecessarily access level and position information of the nodes,
 * because topological ordering is quite costly.
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

		private int levelPosition;

		private int intfPosition;

		private Node inputs[] = new Node[0];

		private Node outputs[] = new Node[0];

		public Node(String name, int type) {
			this.id = namespace.idFor(name);
			this.type = type;
			register(this);
		}
		
		public Node(Node n) {
			this.id = namespace.idFor(n.queryName());
			this.type = n.type;
			this.intfPosition = n.intfPosition;
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

		public int levelPosition() {
			ensureLevels();
			return levelPosition;
		}

		public int intfPosition() {
			return intfPosition;
		}

		public void setIntfPosition(int pos) {
			if (this.intfPosition != pos) {
				invalidateLevels();
				this.intfPosition = pos;
			}
		}

		public int id() {
			return id;
		}

		public boolean equals(Object other) {
			if (other instanceof Node) {
				Node n = (Node) other;
				if (n.type != type)
					return false;
				if (!n.queryName().equals(queryName()))
					return false;
				return true;
			}
			return false;
		}
		
		/*
		 * type queries (convenience accessors to library)
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

		/**
		 * @return the maximum index of an input, or -1 if there are no inputs.
		 */
		public int maxIn() {
			return ArrayTools.maxIndex(inputs);
		}

		/**
		 * @return the maximum index of an output, or -1 if there are no
		 *         outputs.
		 */
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
				throw new IllegalArgumentException(
						"given node " + pred.queryName() + " is not a predecessor of " + queryName());
			return i;
		}

		public Node[] accessInputs() {
			return inputs;
		}

		public Node[] accessOutputs() {
			return outputs;
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
			String iosuffix = "";
			if (isInput())
				iosuffix = "I" + intfPosition();
			if (isOutput())
				iosuffix = "O" + intfPosition();
			StringBuilder b = new StringBuilder(
					"" + level + "_" + levelPosition + ":" + typeName() + "\"" + queryName() + "\"" + iosuffix);
			int m_in = maxIn();
			int m_out = maxOut();
			for (int i = 0; i <= m_in; i++) {
				if (inputs[i] == null) {
					b.append("<null");
				} else {
					b.append(" <" + inputs[i].level + "_" + inputs[i].levelPosition);
				}
			}
			for (int o = 0; o <= m_out; o++) {
				if (outputs[o] == null) {
					b.append(">null");
				} else {
					b.append(" >" + outputs[o].level + "_" + outputs[o].levelPosition);
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

	private Node intf[];

	private String name;

	private SignalMap signalMap;

	public Graph(Library lib) {
		library = lib;
	}
	
	public Graph(Graph g) {
		library = g.library;
		name = g.name;
		for (int idx = 0; idx < g.nodes.length; idx++) {
			Node n = g.nodes[idx];
			if (n != null)
				new Node(n);
		}
		for (int idx = 0; idx < g.nodes.length; idx++) {
			Node n = g.nodes[idx];
			if (n == null)
				continue;
			int outCount = n.maxOut() + 1;
			for (int i = 0; i < outCount; i++) {
				Node succ = n.out(i);
				if (succ != null)
					nodes[idx].setOut(i, nodes[succ.id]);
			}
			int inCount = n.maxIn() + 1;
			for (int i = 0; i < inCount; i++) {
				Node pred = n.in(i);
				if (pred != null)
					nodes[idx].setIn(i, nodes[pred.id]);
			}
		}
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
		if (intf != null)
			n = intf;
		int count = 0;
		for (Node g : n) {
			if (g != null && g.isInput())
				count++;
		}
		return count;
	}

	public int countOutputs() {
		Node n[] = nodes;
		if (intf != null)
			n = intf;
		int count = 0;
		for (Node g : n) {
			if (g != null && g.isOutput())
				count++;
		}
		return count;
	}

	public int countSequentials() {
		Node n[] = nodes;
		if (levels != null)
			n = levels[0];
		int count = 0;
		for (Node g : n) {
			if (g != null && g.isSequential())
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
	
	public int countNonPseudoNodes() {
		int count = 0;
		for (Node g : nodes) {
			if (g != null && !g.isPseudo())
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

	/**
	 * returns an array with all nodes on the topological level l of the graph.
	 * 
	 * The returned array may contain null elements. It is a reference to an
	 * internal Graph data structure. It must not be changed by the caller.
	 * 
	 * @param l
	 * @return
	 */
	public Node[] accessLevel(int l) {
		ensureLevels();
		return levels[l];
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
	public Node[] accessInterface() {
		ensureLevels();
		return intf;
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
	public Node[] accessNodes() {
		return nodes;
	}
	
	public SignalMap accessSignalMap() {
		ensureLevels();
		return signalMap;
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
			throw new IllegalArgumentException("Gate does not exist: " + namespace.nameFor(g.id));
	}

	private void invalidateLevels() {
		levels = null;
		intf = null;
	}

	private void ensureLevels() {
		if (levels != null)
			return;
		// log.debug("levelizing\n\t" +
		// StringTools.join(Thread.currentThread().getStackTrace(), "\n\t"));

		LinkedList<Node> queue = new LinkedList<Node>();
		int level_fills[] = new int[1];
		int maxlevel = 0;

		// Reset level and position of all nodes. Construct interface array.
		// Add all appropriate nodes to queue for level 0.
		intf = null;
		for (Node g : nodes) {
			if (g == null)
				continue;
			g.level = -1;
			g.levelPosition = 0;
			// intf array contains:
			// - primary input ports
			// - primary output ports
			// - sequential nodes (flip-flops)
			if (g.isPort() || g.isSequential()) {
				intf = (Node[]) ArrayTools.grow(intf, Node.class, g.intfPosition + 1, 0.5f);
				if (intf[g.intfPosition] != null) {
					log.error("Nodes must not share same intfPosition: " + g.queryName() + ", "
							+ intf[g.intfPosition].queryName() + " (intfPosition: " + g.intfPosition + ")");
				}
				intf[g.intfPosition] = g;
			}
			// first level (level[0]) contains:
			// - primary input ports
			// - other nodes without inputs (such as constants)
			// - sequential nodes (flip-flops)
			if (g.maxIn() < 0 || g.isSequential() || g.isInput()) {
				queue.add(g);
			}
		}
		intf = (Node[]) ArrayTools.strip(intf);

		level_fills[0] = 0;

		// set levels and levelPositions of all nodes.
		while (!queue.isEmpty()) {
			Node g = queue.poll();
			if (g.level != -1) {
				throw new RuntimeException("Detected combinational loop at gate: " + g.queryName());
			}
			g.level = 0;
			if (!g.isSequential() && !g.isInput()) {
				for (int i = g.maxIn(); i >= 0; i--) {
					Node d = g.in(i);
					if (d != null)
						g.level = Math.max(g.level, d.level + 1);
				}
			}
			level_fills = ArrayTools.grow(level_fills, g.level + 1, 1000, 0);
			g.levelPosition = level_fills[g.level]++;
			maxlevel = Math.max(maxlevel, g.level);
			// log.debug("node " + g + " is level " + g.level);
			if (g.countOuts() == 0)
				continue;
			for (Node succ : g.accessOutputs()) {
				if (succ != null && !succ.isSequential() && !succ.isInput()) {
					succ.levelPosition++; // re-use levelPosition to count
											// number of predecessors placed.
					if (succ.levelPosition == succ.countIns())
						queue.add(succ);
				}
			}
		}

		// allocate levels and add nodes.
		levels = new Node[maxlevel + 1][];
		for (int i = 0; i <= maxlevel; i++)
			levels[i] = new Node[level_fills[i]];
		for (Node g : nodes) {
			if (g == null)
				continue;
			if (g.level == -1)
				log.warn("Unconnected gate not levelized: " + g.queryName());
			else
				levels[g.level][g.levelPosition] = g;
		}

		// log.debug("got levels: " + levels.length);
		signalMap = new SignalMap(this);
	}

	public void printStats() {
		HashMap<String, Integer> pseudo = new HashMap<>();
		HashMap<String, Integer> combinational = new HashMap<>();
		HashMap<String, Integer> inputst = new HashMap<>();
		HashMap<String, Integer> outputst = new HashMap<>();
		HashMap<String, Integer> sequential = new HashMap<>();
		int inputs = 0;
		int outputs = 0;
		int gates = 0;
		int nodes = 0;
		int signals = 0;
		int seq = 0;
		for (Node n : accessNodes()) {
			if (n == null)
				continue;
			String type = n.typeName();
			nodes++;
			if (n.isPseudo()) {
				signals++;
				pseudo.put(type, pseudo.getOrDefault(type, 0) + 1);
				continue;
			}
			if (n.isSequential()) {
				seq++;
				sequential.put(type, sequential.getOrDefault(type, 0) + 1);
				continue;
			}
			if (n.isInput()) {
				inputs++;
				inputst.put(type, inputst.getOrDefault(type, 0) + 1);
			}
			if (n.isOutput()) {
				outputs++;
				outputst.put(type, outputst.getOrDefault(type, 0) + 1);
			}
			if (n.isInput() || n.isOutput())
				continue;
			gates++;
			combinational.put(type, combinational.getOrDefault(type, 0) + 1);
		}
		log.info("CircuitName " + getName());
		log.info("Levels " + levels());
		log.info("NodeCount " + nodes);
		log.info("  PseudoNodeCount " + signals);
		printGateCounts(pseudo);
		log.info("  CombinationalCellCount " + gates);
		printGateCounts(combinational);
		log.info("  SequentialCellCount " + seq);
		printGateCounts(sequential);
		log.info("  PrimaryInputCount " + inputs);
		printGateCounts(inputst);
		log.info("  PrimaryOutputCount " + outputs);
		printGateCounts(outputst);
	}

	private void printGateCounts(HashMap<String, Integer> map) {
		ArrayList<String> keys = new ArrayList<>(map.keySet());
		keys.sort(new Comparator<String>() {
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
		for (String key : keys) {
			log.info("    " + key + " " + map.get(key));
		}

	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}
	
	public boolean equals(Object other) {
		if (other instanceof Graph) {
			Graph g = (Graph) other;
			if (g.countNodes() != countNodes())
				return false;
			for (Node n : accessNodes()) {
				if (n == null)
					continue;
				Node other_n = g.searchNode(n.queryName());
				if (other_n == null)
					return false;
				if (!n.equals(other_n))
					return false;
				if (n.maxIn() != other_n.maxIn())
					return false;
				for (int i = 0; i <= n.maxIn(); i++) {
					Node neighbor = n.in(i);
					Node other_neighbor = other_n.in(i);
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
					Node neighbor = n.out(i);
					Node other_neighbor = other_n.out(i);
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

}
