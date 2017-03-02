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
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.kyupi.graph.Graph.Node;

public class ScanChains {

	private static Logger log = Logger.getLogger(ScanChains.class);

	public class ScanCell {
		public ScanCell(Node n) {
			node = n;
		}

		Node node;
		int pos = -1;
		ScanCell next;
		ScanCell prev;
	}

	public class ScanChain {
		public ScanCell in;
		public ArrayList<ScanCell> cells = new ArrayList<>();
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

		log.info("ScanChainCount " + node_to_scanin.size());

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
			log.info("ScanChainLength " + chn.cells.size());
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
		if (chains.size() != 1)
			throw new RuntimeException("Multiple scan chains not supported.");
		int port_count = graph.accessInterface().length;
		int chain_length = chains.get(0).cells.size();
		int[][] map = new int[chain_length + 1][port_count];
		Node[] intf = graph.accessInterface();

		// inputs and scan state of the last vector in expanded set is identical
		// to the source vector.
		for (int i = 0; i < port_count; i++) {
			if (intf[i] == null || intf[i].isOutput()) {
				map[chain_length][i] = -1;
			} else {
				map[chain_length][i] = i;
			}
		}

		for (int c = chain_length - 1; c >= 0; c--) {
			for (int i = 0; i < port_count; i++) {

				// look up scan cell or scan-in port for current position i
				ScanCell sc = node_to_scancell.get(intf[i]);
				if (sc == null)
					sc = node_to_scanin.get(intf[i]);

				if (sc != null) {
					// found: copy index from intf pos of successor scan cell
					if (sc.next != null)
						map[c][i] = map[c + 1][sc.next.node.position()];
					else
						map[c][i] = -1;
				} else {
					// not found: just copy from next row
					map[c][i] = map[c + 1][i];
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
		if (chains.size() != 1)
			throw new RuntimeException("Multiple scan chains not supported.");
		int port_count = graph.accessInterface().length;
		int chain_length = chains.get(0).cells.size();
		int[][] map = new int[chain_length + 1][port_count];
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

		for (int c = 1; c <= chain_length; c++) {
			for (int i = 0; i < port_count; i++) {

				// look up scan cell for current position i
				ScanCell sc = node_to_scancell.get(intf[i]);

				if (sc != null) {
					// found: copy index from intf pos of predecessor scan cell
					map[c][i] = map[c - 1][sc.prev.node.position()];
				} else {
					// not found: just copy from previous row
					map[c][i] = map[c - 1][i];
				}

			}
		}
		return map;
	}

	public int size() {
		return chains.size();
	}

	public ScanChain get(int i) {
		return chains.get(i);
	}

}
