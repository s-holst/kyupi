/*
 * Copyright 2013 The KyuPI project contributors. See the COPYRIGHT.md file
 * at the top-level directory of this distribution.
 * This file is part of the KyuPI project. It is subject to the license terms
 * in the LICENSE.md file found in the top-level directory of this distribution.
 * No part of the KyuPI project, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.md file.
 */
package org.kyupi.circuit;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.kyupi.circuit.MutableCircuit.MutableCell;
import org.kyupi.misc.FileTools;

public class CircuitTools {

	protected static Logger log = Logger.getLogger(CircuitTools.class);

	private CircuitTools() {
	}

	public static MutableCircuit loadCircuit(File f, Library l) throws IOException {
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

	public static MutableCircuit loadCircuit(String file_name, Library l) throws IOException {
		return loadCircuit(new File(file_name), l);
	}

	public static void saveGraph(MutableCircuit g, File f, boolean allowOverwrite) throws IOException {
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

	public static MutableCircuit parseBench(String bench) {
		InputStream is;
		try {
			is = new ByteArrayInputStream(bench.getBytes("UTF-8"));
			return FormatBench.load(is);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new MutableCircuit(new Library());
	}

	public static void removeDanglingNodes(MutableCircuit g) {
		LinkedList<Integer> ll = new LinkedList<>();
		for (MutableCell n : g.accessNodes()) {
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
			MutableCell n = g.accessNodes()[node_id];
			if (n == null)
				continue;
			if (n.countOuts() > 0)
				continue;

			for (int ii = n.maxIn(); ii >= 0; ii--) {
				MutableCell pred = n.in(ii);
				if (pred != null) {
					boolean retain = false;
					for (MutableCell ps: pred.accessOutputs()) {
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
	
	public static void moveOutputOutgoingEdges(MutableCircuit g) {
		// some output ports may drive other nodes in the graph.
		// re-wire them.

		MutableCell intf[] = g.accessInterface();

		boolean doIterate = true;

		while (doIterate) {
			doIterate = false;
			for (MutableCell output : intf) {
				if (output == null || !output.isOutput())
					continue;
				for (int oidx = output.maxOut(); oidx >= 0; oidx--) {
					doIterate = true;
					MutableCell successor = output.out(oidx);
					if (successor != null) {
						MutableCell predecessor = output.in(0);
						if (predecessor == null) {
							log.error("rewire failed: output " + output.queryName() + " has no driver");
							doIterate = false;
							break;
						}
						if (predecessor.isMultiOutput()) {
							MutableCell signal = g.new MutableCell(output.queryName() + "_net", Library.TYPE_BUF | Library.FLAG_PSEUDO);
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

	public static void removeSignalNodes(MutableCircuit g) {
		for (MutableCell signal : g.accessNodes()) {
			if (signal == null)
				continue;
			if (!signal.isPseudo())
				continue;
			if (!signal.isType(Library.TYPE_BUF))
				continue;
			MutableCell pred = signal.in(0);
			if (pred.isMultiOutput()) {
				log.info("Not removing because predecessor is MultiOutput: " + signal);
				continue;
			}
			
			LinkedList<MutableCell> succs = new LinkedList<>();
			HashMap<MutableCell,Integer> succport = new HashMap<>();
			//log.info("signal " + signal.queryName());
			for (MutableCell r : signal.accessOutputs()) {
				if (r != null) {
					succs.add(r);
					succport.put(r, r.searchInIdx(signal));
				}
			}
			signal.remove();
			pred.compressOuts();
			for (MutableCell r: succs) {
				g.connect(pred, pred.maxOut()+1, r, succport.get(r));
			}
		}
	}
	
	public static HashSet<MutableCell> collectCombinationalOutputCone(MutableCell head) {
		HashSet<MutableCell> result = new HashSet<>();
		
		LinkedList<MutableCell> frontier = new LinkedList<>();
		frontier.add(head);
		
		while (!frontier.isEmpty()) {
			MutableCell n = frontier.removeFirst();
			for (MutableCell successor: n.accessOutputs()) {
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

	public static void splitMultiOutputCells(MutableCircuit g) {
		Library lib = g.library();

		LinkedList<MutableCell> todo = new LinkedList<>();

		// TODO: ensure, that all the predecessors of a multi-output node get split
		// first.
		for (MutableCell cell : g.accessNodes()) {
				if (cell == null || !cell.isMultiOutput())
					continue;
				todo.add(cell);
			}

		for (MutableCell cell : todo) {
			for (int out_idx = cell.maxOut(); out_idx >= 0; out_idx--) {
				MutableCell succ = cell.out(out_idx);
				if (succ == null)
					continue;
				int succ_in_idx = succ.searchInIdx(cell);
				MutableCell subcell = g.new MutableCell(cell.queryName() + "_" + lib.outputPinName(cell.type(), out_idx), lib.getSubCell(cell.type(),
						out_idx));
				g.disconnect(cell, out_idx, succ, succ_in_idx);
				g.connect(subcell, -1, succ, succ_in_idx);
				for (int in_idx = cell.maxIn(); in_idx >= 0; in_idx--) {
					MutableCell pred = cell.in(in_idx);
					if (pred == null)
						continue;
					g.connect(pred, -1, subcell, in_idx);
				}
			}
			cell.remove();
		}
	}
	
//	public static long[][] allocLong(MutableCircuit circuit) {
//		int levels = circuit.levels();
//		long value[][] = new long[levels][];
//		for (int l = 0; l < levels; l++) {
//			value[l] = new long[circuit.accessLevel(l).length];
//			Arrays.fill(value[l], 0L);
//		}
//		return value;
//	}
//
//	public static int[][] allocInt(MutableCircuit circuit, int init_value) {
//		int levels = circuit.levels();
//		int value[][] = new int[levels][];
//		for (int l = 0; l < levels; l++) {
//			value[l] = new int[circuit.accessLevel(l).length];
//			Arrays.fill(value[l], init_value);
//		}
//		return value;
//	}
//	
//	public static int[][] allocInt(MutableCircuit circuit) {
//		return CircuitTools.allocInt(circuit, 0);
//	}

}
