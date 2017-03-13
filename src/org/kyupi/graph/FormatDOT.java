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

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.kyupi.graph.Graph.Node;
import org.kyupi.misc.StringTools;

public class FormatDOT {

	protected static Logger log = Logger.getLogger(FormatDOT.class);

	private static boolean isSignalNode(Node n) {
		return n.isPseudo() && !n.isPort();
	}

	private static String drawNode(Node n) {
		if (isSignalNode(n)) {
			return "  " + n.id() + " [shape=ellipse color=\"#888888\" label=\"" + n.queryName() + "\"];";
		}
		String s = "  " + n.id() + " [label=\"{";
		int num = n.maxIn() + 1;
		ArrayList<String> inports = new ArrayList<>();
		for (int i = 0; i < num; i++) {
			inports.add("<" + i + ">" + n.inName(i));
		}
		if (inports.size() > 0) {
			s += "{" + StringTools.join(inports, "|") + "}|";
		}
		if (n.isInput())
			s += "INTF " + n.intfPosition() + " (in)|";
		s += n.queryName() + "\\n" + n.typeName();
		if (n.isOutput())
			s += "|INTF " + n.intfPosition() + " (out)";
		if (n.countOuts() > 0) {
			num = 1;
			if (n.isMultiOutput()) {
				num = n.maxOut() + 1;
			}
			ArrayList<String> outports = new ArrayList<>();
			for (int i = 0; i < num; i++) {
				outports.add("<o" + i + ">" + n.outName(i));
			}
			s += "|" + "{" + StringTools.join(outports, "|") + "}"; // n.outName(0);
		}
		s += "}\" ";
		// if (n.isInterface())
		// s += "rank=\"min\""; // pin=\"true\" pos=\"1,1\"";
		if (n.isPort()) {
			return s + "shape=Mrecord];";
		} else {
			return s + "];";
		}
	}

	public static void save(OutputStream os, Graph graph) {
		PrintWriter op = new PrintWriter(os);
		op.println("digraph circuit {");
		op.println("  ranksep=1;");
		op.println("  splines=line;");
		op.println("  node [shape=record];");
		for (Node n : graph.accessNodes()) {
			if (n == null)
				continue;
			op.println(drawNode(n));
			int num = n.maxIn() + 1;
			for (int i = 0; i < num; i++) {
				Node pred = n.in(i);
				if (pred == null)
					continue;
				// log.debug(n.queryName() + " <- " + pred.queryName());
				String predPort = ":o0:s";
				if (isSignalNode(pred))
					predPort = "";
				if (pred.isMultiOutput())
					predPort = ":o" + pred.searchOutIdx(n) + ":s";
				String port = ":" + i + ":n";
				if (isSignalNode(n))
					port = "";
				String constraint = "";
				if (n.isSequential() || (n.isOutput() && n.isInput()))
					constraint = " constraint=\"false\" style=\"dotted\"";
				op.println("  " + pred.id() + predPort + " -> " + n.id() + port + " [color=\"#888888\"" + constraint + "]");
			}
		}
		op.println("{ rank=same; ");
		for (Node n : graph.accessInterface()) {
			if (n != null && n.isInput())
				op.println(n.id() + ";");
		}
		op.println("}");
		op.println("{ rank=same; ");
		for (Node n : graph.accessInterface()) {
			if (n != null && n.isSequential())
				op.println(n.id() + ";");
		}
		op.println("}");
		// op.println("{ rank=same; ");
		// for (Node n : graph.getInterface()) {
		// if (n != null && library.isOutput(n.getType()))
		// op.println(n.getId() + ";");
		// }
		// op.println("}");
		// int nl = graph.numLevels();
		// for (int i = 1; i < nl; i++) {
		// op.println("{ rank=same; ");
		// for (Node n : graph.getLevel(i)) {
		// op.println(n.getId() + ";");
		// }
		// op.println("}");
		// }
		// op.println("  g1 [label=\"{{<0>a|<1>b|<2>c}|gate|x}\"];");
		// op.println("  g2 [label=\"{in0|x}\"];");
		// op.println("  g2 -> g1:2;");
		// op.println("  rankdir=LR;");
		op.println("}");
		op.close();
	}
}
