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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.kyupi.graph.Graph.Node;
import org.kyupi.misc.TextScanner;

/**
 * loads from and saves to the Bench file format.
 * 
 * Notes: The bench file format allows any internal signal to be declared as
 * primary output. In a Graph however, outputs are explicitly represented as
 * buffers. This has two consequences as demonstrated by the following example:
 * 
 * INPUT(a) OUTPUT(q) OUTPUT(qn) qn=NOT(a) q=NOT(qn)
 * 
 * 1. Both the second output-buffer and the driving NOT gate are named "qn".
 * This is not allowed in Graph, so the NOT gate will be renamed to "qn_".
 * 
 * 2. The output "qn" is used by the second NOT gate. Primary outputs in Graph
 * cannot be predecessors to any other Node in the circuit, so the input of the
 * second NOT gate will be changed to "qn_".
 * 
 * A re-export of the resulting Graph will yield:
 * 
 * INPUT(a) OUTPUT(q) OUTPUT(qn) q=BUF(q_) qn=BUF(qn_) qn_=NOT(a) q_=NOT(qn_)
 * 
 * 
 */
class FormatBench {

	private static Logger log = Logger.getLogger(FormatBench.class);

	private static class BenchScanner extends TextScanner {

		public static final int SYM_AND = Library.TYPE_AND;
		public static final int SYM_OR = Library.TYPE_OR;
		public static final int SYM_NAND = Library.TYPE_NAND;
		public static final int SYM_NOR = Library.TYPE_NOR;
		public static final int SYM_XOR = Library.TYPE_XOR;
		public static final int SYM_XNOR = Library.TYPE_XNOR;
		public static final int SYM_BUF = Library.TYPE_BUF;
		public static final int SYM_NOT = Library.TYPE_NOT;

		public static final int SYM_EQUAL = 0x10;
		public static final int SYM_COMMA = 0x11;
		public static final int SYM_PAROPEN = 0x12;
		public static final int SYM_PARCLOSE = 0x13;
		public static final int SYM_INPUT = 0x14;
		public static final int SYM_OUTPUT = 0x15;
		public static final int SYM_DFF = 0x16;

		public BenchScanner(Reader src_) throws IOException {
			super(src_);
			put("=", SYM_EQUAL);
			put(",", SYM_COMMA);
			put("(", SYM_PAROPEN);
			put(")", SYM_PARCLOSE);
			put("INPUT", SYM_INPUT);
			put("OUTPUT", SYM_OUTPUT);
			put("DFF", SYM_DFF);
			put("AND", SYM_AND);
			put("NAND", SYM_NAND);
			put("OR", SYM_OR);
			put("NOR", SYM_NOR);
			put("XOR", SYM_XOR);
			put("XNOR", SYM_XNOR);
			put("BUF", SYM_BUF);
			put("NOT", SYM_NOT);
		}

		public Integer next() {
			if (!hasNext())
				throw new NoSuchElementException();
			current_sym = SYM_NONE;

			while (current_sym == SYM_NONE) {
				clearString();
				if (current_int == -1) {
					current_sym = SYM_EOF;
					return current_sym;
				}
				switch (current_char) {
				case '=':
					current_sym = SYM_EQUAL;
					advanceChar();
					break;
				case ',':
					current_sym = SYM_COMMA;
					advanceChar();
					break;
				case '(':
					current_sym = SYM_PAROPEN;
					advanceChar();
					break;
				case ')':
					current_sym = SYM_PARCLOSE;
					advanceChar();
					break;
				case '"':
					current_sym = SYM_STRING;
					advanceString();
					break;
				case '#':
					advanceLine();
					break;
				default:
					if (Character.isDigit(current_char) || Character.isLetter(current_char)) {
						current_sym = advanceIdentifier();
					} else {
						advanceChar();
					}
				}
			}

			return current_sym;
		}

		private int advanceIdentifier() {
			while (((current_char >= 'A') && (current_char <= 'Z')) || ((current_char >= 'a') && (current_char <= 'z'))
					|| (current_char == '_') || (current_char == '.') || ((current_char >= '0') && (current_char <= '9'))) {
				appendToString(current_char);
				advanceChar();
				if (current_int == -1)
					break;
			}

			int sym = SYM_IDENT;
			if (contains(getString().toUpperCase())) {
				sym = get(getString().toUpperCase());
			}
			return sym;
		}

	}

	private static int getParams(BenchScanner scanner, ArrayList<String> params, int max) throws IOException {
		params.clear();
		scanner.assertNext(BenchScanner.SYM_PAROPEN);
		String start = scanner.pos();
		int n = scanner.next();
		while (n != BenchScanner.SYM_PARCLOSE) {
			params.add(scanner.getString());
			n = scanner.next();
			if (n == BenchScanner.SYM_COMMA) {
				n = scanner.next();
			}
			if (params.size() > max)
				throw new IOException("Too many arguments at " + start);
		}
		return params.size();
	}

	private static class GateSpec {
		int type;
		String name;
		ArrayList<String> params = new ArrayList<String>();
		Node gate;
		int pos;

		GateSpec(int type, String name) {
			this.type = type;
			this.name = name;
		}

		void realizeGate(Graph c) {
			gate = c.new Node(name, type);
			gate.setIntfPosition(pos);
		}
	}

	static GateSpec checkedPut(HashMap<String, GateSpec> map, String name, int type) throws IOException {
		GateSpec gs;
		if ((gs = map.get(name)) != null) {
			//gs.type |= type;
			throw new IOException("Gate of name " + name + " already declared.");
		} else {
			gs = new GateSpec(type, name);
			map.put(name, gs);
		}
		return gs;
	}

	static Graph load(InputStream is) throws IOException {
		Graph c = new Graph(new Library());
		BenchScanner scanner = new BenchScanner(new InputStreamReader(is));
		ArrayList<String> params = new ArrayList<String>();
		HashMap<String, GateSpec> gates = new HashMap<String, GateSpec>();
		HashMap<String, GateSpec> outputs = new HashMap<String, GateSpec>();
		HashMap<String, GateSpec> ffs = new HashMap<String, GateSpec>();
		int pos = 0;

		while (scanner.hasNext()) {
			switch (scanner.next()) {
			case BenchScanner.SYM_EOF:
				break;
			case BenchScanner.SYM_INPUT:
				getParams(scanner, params, 1);
				checkedPut(gates, params.get(0), Library.TYPE_BUF | Library.FLAG_INPUT).pos = pos++;
				break;
			case BenchScanner.SYM_OUTPUT:
				getParams(scanner, params, 1);
				GateSpec output = checkedPut(gates, params.get(0), Library.TYPE_BUF | Library.FLAG_OUTPUT);
				output.pos = pos++;
				output.params.add(params.get(0) + "_");
				outputs.put(params.get(0), output);
				break;
			default:
				String dest = scanner.getString();
				if (outputs.containsKey(dest)) {
					dest = dest + "_";
					//log.debug("dest change: " + dest);
				}
				scanner.assertNext(BenchScanner.SYM_EQUAL);
				int type = scanner.next();
				if (type == BenchScanner.SYM_DFF) {
					getParams(scanner, params, 1);
					GateSpec ff = checkedPut(gates, dest, Library.TYPE_BUF | Library.FLAG_SEQUENTIAL);
					ff.pos = pos++;
					ff.params.add(params.get(0));
					ffs.put(params.get(0), gates.get(dest));
					break;
				}
				if ((type & ~0xf) != 0)
					throw new IOException("Unknown gate type: " + scanner.getString());
				getParams(scanner, params, 1000);
				int s = params.size();
				for (int i = 0; i < s; i++) {
					if (outputs.containsKey(params.get(i))) {
						params.set(i, params.get(i) + "_");
						//log.debug("param change: " + params.get(i));
					}
				}
				checkedPut(gates, dest, type).params.addAll(params);
			}
		}
		removeUnused(gates);
		for (GateSpec gs : gates.values())
			gs.realizeGate(c);
		for (GateSpec gs : gates.values()) {
			for (String name : gs.params) {
				Node n = c.searchNode(name);
				c.connect(n, -1, gs.gate, -1);
			}
		}
		c.strip();
		return c;
	}

	private static void removeUnused(HashMap<String, GateSpec> gates) {
		boolean changed = true;
		while (changed) {
			HashSet<String> unused = new HashSet<String>();
			for (GateSpec gs : gates.values())
				if ((gs.type & (Library.FLAG_OUTPUT)) == 0)
					unused.add(gs.name);
			for (GateSpec gs : gates.values())
				unused.removeAll(gs.params);
			if (unused.isEmpty()) {
				changed = false;
			} else {
				for (String name : unused) {
					log.warn("Removing unused gate: " + name);
					gates.remove(name);
				}
			}
		}
	}

	private static String names[] = { "CONST0", "NOR", "AGTB", "NOT", "BGTA", "NOT", "XOR", "NAND", "AND", "XNOR", "BUF", "AGEB", "BUF",
			"BGEA", "OR", "CONST1" };

	public static void save(OutputStream os, Graph graph) {
		PrintWriter op = new PrintWriter(os);
		for (Node intf : graph.accessInterface()) {
			if (intf.isPrimary() && intf.isInput())
				op.println("INPUT(" + intf.queryName() + ")");
			if (intf.isPrimary() && intf.isInput())
				op.println("OUTPUT(" + intf.queryName() + ")");
		}
		for (Node inp : graph.accessInterface()) {
			if (inp.isPseudo())
				op.println(inp.queryName() + " = DFF(" + inp.queryName() + ")"); // FIXME
		}
		for (int l = 1; l < graph.levels(); l++) {
			for (Node node : graph.accessLevel(l)) {
				int ninputs = node.maxIn();
				String s = "";
				for (int i = 0; i <= ninputs; i++) {
					Node a = node.in(i);
					if (a != null)
						s += "," + a.queryName();
					else
						log.warn("ignoring null input at cell " + node.queryName());
				}
				if (s.length() > 0)
					s = s.substring(1);
				op.println(node.queryName() + " = " + names[node.type() & 0xf] + "(" + s + ")");
			}
		}
		op.close();
	}
}
