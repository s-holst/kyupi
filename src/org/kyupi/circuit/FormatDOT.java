/*
 * Copyright 2013-2018 The KyuPI project contributors. See the COPYRIGHT.md file
 * at the top-level directory of this distribution.
 * This file is part of the KyuPI project. It is subject to the license terms
 * in the LICENSE.md file found in the top-level directory of this distribution.
 * No part of the KyuPI project, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.md file.
 */
package org.kyupi.circuit;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.kyupi.circuit.LevelizedCircuit.LevelizedCell;
import org.kyupi.circuit.MutableCircuit.MutableCell;
import org.kyupi.misc.StringTools;

public class FormatDOT {

	protected static Logger log = Logger.getLogger(FormatDOT.class);

	private static final int scaleX = 230;
	private static final int scaleY = 120;
	
		private static String drawNode(LevelizedCell n) {
		String s = "  " + n.id() + " [label=\"{";
		int num = n.inputCount();
		ArrayList<String> inports = new ArrayList<>();
		for (int i = 0; i < num; i++) {
			inports.add("<" + i + ">" + n.inName(i));
		}
		if (inports.size() > 0) {
			s += "{" + StringTools.join(inports, "|") + "}|";
		}
		String ioInfo = "";
		if (n.isPort() || n.isSequential()) {
			ioInfo += "\\n";
			if (n.isOutput() || n.isSequential())
				ioInfo += "\\> ";
			ioInfo += n.intfPosition();
			if (n.isInput() || n.isSequential())
				ioInfo += " \\>";
		}

		s += n.name() + "\\n" + n.typeName() + ioInfo;
		
		if (n.outputCount() > 0) {
			num = n.outputCount();
			ArrayList<String> outports = new ArrayList<>();
			for (int i = 0; i < num; i++) {
				outports.add("<o" + i + ">" + n.outName(i));
			}
			s += "|" + "{" + StringTools.join(outports, "|") + "}"; // n.outName(0);
		}
		s += "}\" ";
		s += "pos=\"" + (n.level()*scaleX) + "," + (n.position()*-scaleY) + "!\" ";
		if (n.isPseudo()) {
			return s + "shape=Mrecord];";
		} else {
			return s + "];";
		}
	}

	public static void save(OutputStream os, LevelizedCircuit graph) {
		PrintWriter op = new PrintWriter(os);
		op.println("#!/usr/local/bin/neato -n -Tpdf -ocircuit.pdf\n");
		op.println("# x/y arranged: neato -n -Tpdf -ocircuit.pdf circuit.dot");
		op.println("# opt. arranged: dot -Tpdf -ocircuit.pdf circuit.dot\n");
		op.println("digraph circuit {");
		op.println("  rankdir=LR;");
		op.println("  splines=false;");
		op.println("  node [shape=record];");
		for (LevelizedCell n : graph.cells()) {
			if (n == null)
				continue;
			op.println(drawNode(n));
			int num = n.inputCount();
			for (int i = 0; i < num; i++) {
				LevelizedCell pred = n.inputCellAt(i);
				if (pred == null)
					continue;
				String predPort = ":o" + pred.searchOutIdx(n) + ":e";
				String port = ":" + i + ":w";
				String constraint = "";
				if (pred.level() >= n.level())
					constraint = " constraint=\"false\" style=\"dotted\"";
				op.println("  " + pred.id() + predPort + " -> " + n.id() + port + " [color=\"#888888\"" + constraint + "]");
			}
		}
		op.println("}");
		op.close();
	}
}
