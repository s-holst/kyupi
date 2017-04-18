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
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.regex.Pattern;

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
	
	private static String s(String raw) {
		if (Pattern.matches("^[\\d_].*", raw) || raw.contains("["))
			return "\\" + raw + " ";
		return raw;
	}
	
	/**
	 * FIXME do verilog dump.
	 * 
	 * @param os
	 * @param graph
	 * @param entity_name
	 */
	public static void save(OutputStream os, Graph graph, String entity_name) {
		PrintWriter op = new PrintWriter(os);
		op.println("module " + entity_name + " ( ");
		boolean comma_needed = false;
		for (Node intf_node : graph.accessInterface()) {
			if (intf_node == null)
				continue;
			if (intf_node.isInput() || intf_node.isOutput()) {
				if (comma_needed)
					op.print(",\n");
				op.print(s(intf_node.queryName()));
				comma_needed = true;
			}
		}
		op.println("  );");
		
		op.print("input ");
		comma_needed = false;
		for (Node intf_node : graph.accessInterface()) {
			if (intf_node == null)
				continue;
			if (intf_node.isInput()) {
				if (comma_needed)
					op.print(",\n");
				op.print(s(intf_node.queryName()));
				comma_needed = true;
			}
		}
		op.println(";");

		op.print("output ");
		comma_needed = false;
		for (Node intf_node : graph.accessInterface()) {
			if (intf_node == null)
				continue;
			if (intf_node.isOutput()) {
				if (comma_needed)
					op.print(",\n");
				op.print(s(intf_node.queryName()));
				comma_needed = true;
			}
		}
		op.println(";");

		op.print("wire ");
		comma_needed = false;
		Node[] nodes = graph.accessNodes();
		for (Node node : nodes) {
			if (node == null) {
				continue;
			}
			if (node.isPseudo()) {
				if (comma_needed)
					op.print(",\n");
				op.print(s(node.queryName()));
				comma_needed = true;
			}
		}
		op.println(";");

		for (Node node : nodes) {
			if (node == null || node.isPseudo() || node.isInput() || node.isOutput()) {
				continue;
			}
			op.print("  " + node.typeName() + " " + s(node.queryName()) + " ( ");
			int i = -1;
			comma_needed = false;
			for (Node n : node.accessInputs()) {
				i++;
				if (n == null)
					continue;
				if (comma_needed)
					op.print(", ");
				op.print(" ." + node.inName(i) + "(" + s(n.queryName()) + ") ");
				comma_needed = true;
			}
			i = -1;
			for (Node n : node.accessOutputs()) {
				i++;
				if (n == null)
					continue;
				if (comma_needed)
					op.print(", ");
				op.print(" ." + node.outName(i) + "(" + s(n.queryName()) + ") ");
				comma_needed = true;
			}
			op.println(");");
		}
		op.println("endmodule");

		op.close();
	}

}
