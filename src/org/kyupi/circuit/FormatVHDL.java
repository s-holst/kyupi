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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.kyupi.circuit.MutableCircuit.MutableCell;
import org.kyupi.circuit.parser.VHDL93;

public class FormatVHDL {

	protected static Logger log = Logger.getLogger(FormatVHDL.class);

	static MutableCircuit load(InputStream is, Library library) throws IOException {
		ArrayList<MutableCircuit> units = VHDL93.parse(is, library);
		if (units.size() > 0)
			return units.get(0);
		else
			return null;
	}

	private static String s(String raw) {
		if (Pattern.matches("^[\\d_].*", raw))
			return "\\" + raw + "\\";
		return raw;
	}

	public static void save(OutputStream os, MutableCircuit graph, String entity_name) {
		PrintWriter op = new PrintWriter(os);
		op.println("library IEEE;");
		op.println("use IEEE.std_logic_1164.all;");
		op.println("entity " + entity_name + " is ");
		op.println("  port(");
		for (MutableCell intf_node : graph.accessInterface()) {
			if (intf_node.isPrimary() && intf_node.isInput())
				op.println("    " + s(intf_node.queryName()) + ": in std_logic;");
			if (intf_node.isPrimary() && intf_node.isOutput())
				op.println("    " + s(intf_node.queryName()) + ": out std_logic;");
		}
		op.println("  );");
		op.println("end " + entity_name + ";");
		op.println("architecture NL of " + entity_name + " is");

		MutableCell[] nodes = graph.accessNodes();
		for (MutableCell node : nodes) {
			if (node == null) {
				continue;
			}
			if (!node.isPort())
				op.println("  signal " + s(node.queryName()) + ": std_logic;");
		}
		op.println("begin");
		for (MutableCell node : nodes) {
			if (node == null) {
				continue;
			}
			if (!node.isInput()) {
				op.print("  " + s(node.queryName()) + " <= ");
				switch (node.type() & Library.MASK_FUNCTION) {
				case Library.TYPE_NAND:
					if (node.countIns() == 2) {
						op.println(s(node.in(0).queryName()) + " nand " + s(node.in(1).queryName()) + ";");
					}
				}
			}
		}
		op.println("end NL;");

		op.close();
	}
}
