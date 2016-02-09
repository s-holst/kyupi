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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.kyupi.graph.Graph.Node;
import org.kyupi.graph.parser.Verilog;
import org.kyupi.graph.parser.VerilogParseTree;

public class FormatVerilog {

	protected static Logger log = Logger.getLogger(FormatVerilog.class);

	static Graph load(InputStream is, Library library) throws IOException {
		VerilogParseTree tree = Verilog.parse(is);
		ArrayList<Graph> units = tree.elaborateAll(library);
		if (units.size() == 0)
			return new Graph(library);

		Graph g = units.get(0);
		
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

		return g;
	}
}
