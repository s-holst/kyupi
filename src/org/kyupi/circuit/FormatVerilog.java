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
import org.kyupi.circuit.MutableCircuit.MutableCell;
import org.kyupi.circuit.parser.Verilog;
import org.kyupi.circuit.parser.VerilogParseTree;

public class FormatVerilog {

	protected static Logger log = Logger.getLogger(FormatVerilog.class);

	static MutableCircuit load(InputStream is, Library library) throws IOException {
		VerilogParseTree tree = Verilog.parse(is);
		ArrayList<MutableCircuit> units = tree.elaborateAll(library);
		if (units.size() == 0)
			return new MutableCircuit(library);
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
	public static void save(OutputStream os, MutableCircuit graph) {
		PrintWriter op = new PrintWriter(os);
		op.println("module " + graph.name() + " ( ");
		boolean comma_needed = false;
		for (MutableCell intf_node : graph.intf()) {
			if (intf_node == null)
				continue;
			if (intf_node.isInput() || intf_node.isOutput()) {
				if (comma_needed)
					op.print(",\n");
				op.print(s(intf_node.name()));
				comma_needed = true;
			}
		}
		op.println("  );");
		
		op.print("input ");
		comma_needed = false;
		for (MutableCell intf_node : graph.intf()) {
			if (intf_node == null)
				continue;
			if (intf_node.isInput()) {
				if (comma_needed)
					op.print(",\n");
				op.print(s(intf_node.name()));
				comma_needed = true;
			}
		}
		op.println(";");

		op.print("output ");
		comma_needed = false;
		for (MutableCell intf_node : graph.intf()) {
			if (intf_node == null)
				continue;
			if (intf_node.isOutput()) {
				if (comma_needed)
					op.print(",\n");
				op.print(s(intf_node.name()));
				comma_needed = true;
			}
		}
		op.println(";");

		op.print("wire ");
		comma_needed = false;
		for (MutableCell node : graph.cells()) {
			if (node == null) {
				continue;
			}
			if (node.isPseudo()) {
				if (comma_needed)
					op.print(",\n");
				op.print(s(node.name()));
				comma_needed = true;
			}
		}
		op.println(";");

		for (MutableCell node : graph.cells()) {
			if (node == null || node.isPseudo() || node.isInput() || node.isOutput()) {
				continue;
			}
			op.print("  " + node.typeName() + " " + s(node.name()) + " ( ");
			int i = -1;
			comma_needed = false;
			for (MutableCell n : node.inputCells()) {
				i++;
				if (n == null)
					continue;
				if (comma_needed)
					op.print(", ");
				op.print(" ." + node.inputName(i) + "(" + s(n.name()) + ") ");
				comma_needed = true;
			}
			i = -1;
			for (MutableCell n : node.outputCells()) {
				i++;
				if (n == null)
					continue;
				if (comma_needed)
					op.print(", ");
				op.print(" ." + node.outputName(i) + "(" + s(n.name()) + ") ");
				comma_needed = true;
			}
			op.println(");");
		}
		op.println("endmodule");

		op.close();
	}

}
