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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.kyupi.graph.Graph.Node;
import org.kyupi.misc.FileTools;

public class GraphTools {

	protected static Logger log = Logger.getLogger(GraphTools.class);

	private GraphTools() {
	}

	public static Graph loadGraph(File f, Library l) throws IOException {
		switch (FileTools.fileType(f)) {
		case FileTools.FILE_TYPE_ISCAS:
			return FormatISCAS.load(FileTools.fileOpen(f));
		case FileTools.FILE_TYPE_BENCH:
			return FormatBench.load(FileTools.fileOpen(f));
		case FileTools.FILE_TYPE_VHDL:
			return FormatVHDL.load(FileTools.fileOpen(f), l);
		case FileTools.FILE_TYPE_VERILOG:
			return FormatVerilog.load(FileTools.fileOpen(f), l);
		case FileTools.FILE_TYPE_KDB:
			return FormatKDB.load(FileTools.fileOpen(f));
			
		}
		throw new IOException("unsupported import file type: " + FileTools.fileType(f));
	}

	public static Graph loadGraph(String file_name, Library l) throws IOException {
		return loadGraph(new File(file_name), l);
	}

	public static void saveGraph(Graph g, File f, boolean allowOverwrite) throws IOException {
		switch (FileTools.fileType(f)) {
		case FileTools.FILE_TYPE_BENCH:
			FormatBench.save(FileTools.fileCreate(f, allowOverwrite), g);
			break;
		case FileTools.FILE_TYPE_VHDL:
			FormatVHDL.save(FileTools.fileCreate(f, allowOverwrite), g, FileTools.fileBasename(f));
			break;
		case FileTools.FILE_TYPE_DOT:
			FormatDOT.save(FileTools.fileCreate(f, allowOverwrite), g);
			break;
		default:
			throw new IOException("unsupported export file type: " + FileTools.fileType(f));
		}
	}

	public static Graph benchToGraph(String bench) {
		InputStream is;
		try {
			is = new ByteArrayInputStream(bench.getBytes("UTF-8"));
			return FormatBench.load(is);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new Graph(new Library());
	}

	public static void removeDanglingNodes(Graph g) {
		LinkedList<Integer> ll = new LinkedList<>();
		for (Node n : g.accessNodes()) {
			if (n == null)
				continue;
			if (n.isOutput())
				continue;
			if (n.countOuts() > 0)
				continue;
			ll.add(n.id());
		}

		while (!ll.isEmpty()) {
			int node_id = ll.poll();
			Node n = g.accessNodes()[node_id];
			if (n == null)
				continue;
			if (n.countOuts() > 0)
				continue;

			for (int ii = n.maxIn(); ii >= 0; ii--) {
				Node pred = n.in(ii);
				if (pred != null) {
					boolean retain = false;
					for (Node ps: pred.accessOutputs()) {
						if (ps == null)
							continue;
						if (!ps.equals(n)) {
							retain = true;
						}
					}
					if (!retain)
						ll.add(pred.id());
				}
			}
			//log.debug("removing " + n);
			n.remove();
		}
	}
	
	public static void moveOutputOutgoingEdges(Graph g) {
		// some output ports may drive other nodes in the graph.
		// re-wire them.

		Node intf[] = g.accessInterface();

		boolean doIterate = true;

		while (doIterate) {
			doIterate = false;
			for (Node output : intf) {
				if (output == null || !output.isOutput())
					continue;
				for (int oidx = output.maxOut(); oidx >= 0; oidx--) {
					doIterate = true;
					Node successor = output.out(oidx);
					if (successor != null) {
						Node predecessor = output.in(0);
						if (predecessor == null) {
							log.error("rewire failed: output " + output.queryName() + " has no driver");
							doIterate = false;
							break;
						}
						if (predecessor.isMultiOutput()) {
							Node signal = g.new Node(output.queryName() + "_net", Library.TYPE_BUF | Library.FLAG_PSEUDO);
							int opin = predecessor.searchOutIdx(output);
							g.connect(predecessor, opin, signal, -1);
							g.connect(signal, -1, output, 0);
							predecessor = signal;
						}
						int iidx = successor.searchInIdx(output);
						g.connect(predecessor, -1, successor, iidx);
						output.setOut(oidx, null);
					}
				}
			}
		}
	}

	public static void removeSignalNodes(Graph g) {
		for (Node signal : g.accessNodes()) {
			if (signal == null)
				continue;
			if (!signal.isPseudo())
				continue;
			if (!signal.isType(Library.TYPE_BUF))
				continue;
			Node pred = signal.in(0);
			if (pred.isMultiOutput()) {
				log.info("Not removing because predecessor is MultiOutput: " + signal);
				continue;
			}
			
			LinkedList<Node> succs = new LinkedList<>();
			HashMap<Node,Integer> succport = new HashMap<>();
			//log.info("signal " + signal.queryName());
			for (Node r : signal.accessOutputs()) {
				if (r != null) {
					succs.add(r);
					succport.put(r, r.searchInIdx(signal));
				}
			}
			signal.remove();
			pred.compressOuts();
			for (Node r: succs) {
				g.connect(pred, pred.maxOut()+1, r, succport.get(r));
			}
		}
	}
	
	public static HashSet<Node> collectCombinationalOutputCone(Node head) {
		HashSet<Node> result = new HashSet<>();
		
		LinkedList<Node> frontier = new LinkedList<>();
		frontier.add(head);
		
		while (!frontier.isEmpty()) {
			Node n = frontier.removeFirst();
			for (Node successor: n.accessOutputs()) {
				if (successor == null || successor.isSequential())
					continue;
				if (!result.contains(successor)) {
					frontier.addLast(successor);
					result.add(successor);
				}
			}
		}
		
		return result;
	}

	public static void splitMultiOutputCells(Graph g) {
		Library lib = g.library();

		LinkedList<Node> todo = new LinkedList<>();

		// ensure, that all the predecessors of a multi-output node get split
		// first.
		for (int l = 1; l < g.levels(); l++) {
			for (Node cell : g.accessLevel(l)) {
				if (cell == null || !cell.isMultiOutput())
					continue;
				todo.add(cell);
			}
		}

		for (Node cell : todo) {
			for (int out_idx = cell.maxOut(); out_idx >= 0; out_idx--) {
				Node succ = cell.out(out_idx);
				if (succ == null)
					continue;
				int succ_in_idx = succ.searchInIdx(cell);
				Node subcell = g.new Node(cell.queryName() + "_" + lib.outputPinName(cell.type(), out_idx), lib.getSubCell(cell.type(),
						out_idx));
				g.disconnect(cell, out_idx, succ, succ_in_idx);
				g.connect(subcell, -1, succ, succ_in_idx);
				for (int in_idx = cell.maxIn(); in_idx >= 0; in_idx--) {
					Node pred = cell.in(in_idx);
					if (pred == null)
						continue;
					g.connect(pred, -1, subcell, in_idx);
				}
			}
			cell.remove();
		}
	}
	
	public static long[][] allocLong(Graph circuit) {
		int levels = circuit.levels();
		long value[][] = new long[levels][];
		for (int l = 0; l < levels; l++) {
			value[l] = new long[circuit.accessLevel(l).length];
			Arrays.fill(value[l], 0L);
		}
		return value;
	}

	public static int[][] allocInt(Graph circuit) {
		int levels = circuit.levels();
		int value[][] = new int[levels][];
		for (int l = 0; l < levels; l++) {
			value[l] = new int[circuit.accessLevel(l).length];
			Arrays.fill(value[l], 0);
		}
		return value;
	}
}
