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

	private class ScanCell {
		public ScanCell(Node n) {
			node = n;
		}

		Node node;
		int chain;
		int pos;
		ScanCell next;
		ScanCell prev;
	}

	private Library lib;

	private ArrayList<ScanCell> chain_heads = new ArrayList<>();

	private ScanCell chains[][];

	public ScanChains(Graph scanned_netlist) {
		lib = scanned_netlist.library();

		// collect all scan cells into a HashMap.
		HashMap<Node, ScanCell> all_cells = new HashMap<>();
		Node[] intf = scanned_netlist.accessInterface();
		for (Node n : intf) {
			if (lib.isScanCell(n.type())) {
				ScanCell cell = new ScanCell(n);
				all_cells.put(n, cell);
			}
		}
		
		// connect each scan cell to its neighbors.
		for (Node n : all_cells.keySet()) {
			ScanCell cell = all_cells.get(n);
			Node prev = backtraceToIntf(n.in(lib.getScanInPin(n.type())));
			if (prev.isInput()) {
				//log.info("Scan-in port: " + prev.queryName());
				chain_heads.add(cell);
			} else {
				ScanCell prev_cell = all_cells.get(prev);
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

		chains = new ScanCell[chain_heads.size()][];
		log.info("ScanChainCount " + chains.length);

		// assign chain number and chain position to each scan cell
		int chainNr = 0;
		for (ScanCell head : chain_heads) {
			int length = 0;
			do {
				head.chain = chainNr;
				head.pos = length;
				length++;
				all_cells.remove(head.node);
				head = head.next;
			} while (head != null);
			log.info("ScanChain " + chainNr + " Length " + length);
			chains[chainNr] = new ScanCell[length];
			chainNr++;
		}
		
		// store scan cells into array
		for (ScanCell head : chain_heads) {
			do {
				chains[head.chain][head.pos] = head;
				head = head.next;
			} while (head != null);
		}
		for (Node n : all_cells.keySet()) {
			log.warn("Unconnected ScanCell: " + n.queryName());
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

}
