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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.kyupi.graph.Graph.Node;
import org.kyupi.misc.ArrayTools;

public class ScanChains {

	private static Logger log = Logger.getLogger(ScanChains.class);

	public class ScanCell {
		public ScanCell(Node n) {
			node = n;
		}

		public Node node;
		int pos = -1;
		ScanChain chain;
		ScanCell next;
		ScanCell prev;
		
		public int chainIdx() {
			return chain.chainIdx();
		}
	}

	public class ScanChain {
		public ScanCell in;
		public ArrayList<ScanCell> cells = new ArrayList<>();
		int chain_idx = -1;
		public int chainIdx() {
			return chain_idx;
		}
		// ScanCell scanout;
	}

	private Library lib;

	private Graph graph;

	private HashMap<Node, ScanCell> node_to_scancell = new HashMap<>();

	private HashMap<Node, ScanCell> node_to_scanin = new HashMap<>();

	// private HashMap<Node, ScanCell> node_to_scanout = new HashMap<>();

	private ArrayList<ScanChain> chains = new ArrayList<>();

	public ScanChains(Graph scanned_netlist) {
		graph = scanned_netlist;
		lib = graph.library();

		// collect all scan cells into a HashMap.
		Node[] intf = graph.accessInterface();
		for (Node n : intf) {
			if (n != null && lib.isScanCell(n.type())) {
				node_to_scancell.put(n, new ScanCell(n));
			}
		}

		// connect each scan cell to its neighbors.
		for (Node n : node_to_scancell.keySet()) {
			ScanCell cell = node_to_scancell.get(n);
			Node prev = backtraceToIntf(n.in(lib.getScanInPin(n.type())));
			if (prev.isInput()) {
				// log.info("Scan-in port: " + prev.queryName());
				ScanCell sc = new ScanCell(prev);
				node_to_scanin.put(prev, sc);
				sc.next = cell;
				cell.prev = sc;
			} else {
				ScanCell prev_cell = node_to_scancell.get(prev);
				if (prev_cell == null) {
					log.error("Encountered non-scan-cell on scan path: " + prev);
				} else {
					if (cell.prev != null) {
						log.warn("Scan cell already connected.");
					}
					cell.prev = prev_cell;
					prev_cell.next = cell;
				}
			}
		}

		// log.info("ScanChainCount " + node_to_scanin.size());

		// assign chain position to each scan cell
		for (ScanCell head : node_to_scanin.values()) {
			ScanChain chn = new ScanChain();
			chains.add(chn);
			chn.in = head;
			int position = -1; // -1 is scan-in port.
			do {
				head.pos = position;
				position++;
				head = head.next;
				if (head != null)
					chn.cells.add(head);
			} while (head != null);
			// log.info("ScanChainLength " + chn.cells.size());
		}

		chains.sort(new Comparator<ScanChain>() {
			@Override
			public int compare(ScanChain o1, ScanChain o2) {
				return o1.in.node.queryName().compareTo(o2.in.node.queryName());
			}
		});

		// set chain in each cell
		for (int i = 0; i < chains.size(); i++) {
			chains.get(i).in.chain = chains.get(i);
			chains.get(i).chain_idx = i;
			for (ScanCell sc : chains.get(i).cells) {
				sc.chain = chains.get(i);
			}
		}

		// FIXME find a scan out port for each chain

		// for (ScanChain chn : chains) {
		// ScanCell last = chn.scancells.get(chn.scancells.size()-1);
		// }

		// check, if all scan cells are connected into chains.
		for (Node n : node_to_scancell.keySet()) {
			if (node_to_scancell.get(n).pos < 0)
				log.warn("Unconnected ScanCell: " + n);
		}
	}

	private Node backtraceToIntf(Node node) {
		if (node.level() > 0) {
			if (node.countIns() != 1)
				log.warn("Encountered multi-input gate on scan path: " + node);
			return backtraceToIntf(node.in(0));
		}
		return node;
	}

	/**
	 * generates a mapping for QVExpander to generate separate vectors for each
	 * shift-in cycle starting with a completely empty scan chain and ending
	 * with a completely loaded scan chain.
	 * 
	 * Primary inputs are kept stable over all scan cycles. Primary outputs are
	 * not assigned in the expanded vectors.
	 * 
	 * @return
	 */
	public int[][] scanInMapping() {
		int clocking[] = new int[size()];
		Arrays.fill(clocking, 0);
		return scanInMapping(clocking);
	}

	/**
	 * generates a mapping for QVExpander to generate separate vectors for each
	 * shift-in cycle starting with a completely empty scan chain and ending
	 * with a completely loaded scan chain.
	 * 
	 * Primary inputs are kept stable over all scan cycles. Primary outputs are
	 * not assigned in the expanded vectors.
	 * 
	 * clocking is an array that contains a clock index for each scan chain.
	 * First all chains with clock index 0 are shifted, then all the chains with
	 * clock index 1, and so on. The number of clocks used for a complete shift
	 * cycle is max(clocking)+1.
	 * 
	 * @return
	 */
	public int[][] scanInMapping(int clocking[]) {
		int clock_count = ArrayTools.max(clocking) + 1;
		int port_count = graph.accessInterface().length;
		int max_chain_length = maxChainLength();
		int[][] map = new int[(max_chain_length * clock_count) + 1][port_count];
		Node[] intf = graph.accessInterface();

		// inputs and scan state of the last vector in expanded set is identical
		// to the source vector.
		for (int i = 0; i < port_count; i++) {
			if (intf[i] == null || intf[i].isOutput()) {
				map[max_chain_length * clock_count][i] = -1;
			} else {
				map[max_chain_length * clock_count][i] = i;
			}
		}

		for (int c = max_chain_length - 1; c >= 0; c--) {
			for (int clk = clock_count - 1; clk >= 0; clk--) {
				int map_idx = clk + (c * clock_count);
				for (int i = 0; i < port_count; i++) {

					// look up scan cell or scan-in port for current position i
					ScanCell sc = node_to_scancell.get(intf[i]);
					if (sc == null)
						sc = node_to_scanin.get(intf[i]);

					if (sc != null && clocking[sc.chainIdx()] == clk) {
						// found and clocked: copy index from intf pos of
						// successor scan cell
						if (sc.next != null)
							map[map_idx][i] = map[map_idx + 1][sc.next.node.intfPosition()];
						else
							map[map_idx][i] = -1;
					} else {
						// not found or not clocked: just copy from next row
						map[map_idx][i] = map[map_idx + 1][i];
					}

				}
			}
		}
		return map;
	}

	/**
	 * generates a mapping for QVExpander to generate separate vectors for each
	 * shift-out cycle starting with a completely loaded scan chain and ending
	 * with a completely empty scan chain.
	 * 
	 * Primary IO are kept stable over all scan cycles.
	 * 
	 * @return
	 */
	public int[][] scanOutMapping() {
		int clocking[] = new int[size()];
		Arrays.fill(clocking, 0);
		return scanOutMapping(clocking);
	}

	/**
	 * generates a mapping for QVExpander to generate separate vectors for each
	 * shift-out cycle starting with a completely loaded scan chain and ending
	 * with a completely empty scan chain.
	 * 
	 * Primary IO are kept stable over all scan cycles.
	 * 
	 * clocking is an array that contains a clock index for each scan chain.
	 * First all chains with clock index 0 are shifted, then all the chains with
	 * clock index 1, and so on. The number of clocks used for a complete shift
	 * cycle is max(clocking)+1.
	 * 
	 * @return
	 */
	public int[][] scanOutMapping(int clocking[]) {
		int clock_count = ArrayTools.max(clocking) + 1;
		int port_count = graph.accessInterface().length;
		int max_chain_length = maxChainLength();
		int[][] map = new int[(max_chain_length * clock_count) + 1][port_count];
		Node[] intf = graph.accessInterface();

		// outputs and scan state of the first vector in expanded set is
		// identical to the source vector.
		for (int i = 0; i < port_count; i++) {
			if (intf[i] == null || intf[i].isInput()) {
				map[0][i] = -1;
			} else {
				map[0][i] = i;
			}
		}

		for (int c = 1; c <= max_chain_length; c++) {
			for (int clk = 0; clk < clock_count; clk++) {
				int map_idx = clk + ((c - 1) * clock_count) + 1;
				for (int i = 0; i < port_count; i++) {

					// look up scan cell for current position i
					ScanCell sc = node_to_scancell.get(intf[i]);

					if (sc != null && clocking[sc.chainIdx()] == clk) {
						// found and clocked: copy index from intf pos of
						// predecessor scan cell
						map[map_idx][i] = map[map_idx - 1][sc.prev.node.intfPosition()];
					} else {
						// not found or not clocked: just copy from previous row
						map[map_idx][i] = map[map_idx - 1][i];
					}

				}
			}
		}
		return map;
	}

	public int size() {
		return chains.size();
	}
	
	public int scanCellCount() {
		return node_to_scancell.size();
	}

	public int maxChainLength() {
		int max = 0;
		for (ScanChain c : chains) {
			max = Math.max(max, c.cells.size());
		}
		return max;
	}

	public ScanChain get(int i) {
		return chains.get(i);
	}

}
