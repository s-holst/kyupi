/*
 * Copyright 2013-2017 The KyuPI project contributors. See the COPYRIGHT.md file
 * at the top-level directory of this distribution.
 * This file is part of the KyuPI project. It is subject to the license terms
 * in the LICENSE.md file found in the top-level directory of this distribution.
 * No part of the KyuPI project, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.md file.
 */
package org.kyupi.circuit;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.kyupi.circuit.Graph.Node;
import org.kyupi.graph.parser.Verilog;
import org.kyupi.graph.parser.VerilogParseTree;

public class FormatVerilog {

	protected static Logger log = Logger.getLogger(FormatVerilog.class);

	static Graph load(InputStream is, Library library) throws IOException {
		VerilogParseTree tree = Verilog.parse(is);
		ArrayList<Graph> units = tree.elaborateAll(library);
		if (units.size() == 0)
			return new Graph(library);
		return units.get(0);
	}
	
	private static String s(String raw) {
		if (Pattern.matches("^[\\d_].*", raw) || raw.contains("["))
			return "\\" + raw + " ";
		return raw;
	}
	
	/**
	 * Saving graph in verilog format to given output stream.
	 * 
	 * @param os
	 * @param graph
	 * @param entity_name
	 */
	public static void save(OutputStream os, Graph graph) {
		PrintWriter op = new PrintWriter(os);
		op.println("module " + graph.getName() + " ( ");
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
