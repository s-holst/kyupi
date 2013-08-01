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
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;

import org.kyupi.graph.Graph.Node;
import org.kyupi.misc.TextScanner;

class FormatISCAS {

	// private static Logger log = Logger.getLogger(IscasFormat.class);

	private static class IscasScanner extends TextScanner {

		public static final int SYM_INPUT = 1;
		public static final int SYM_FROM = 2;
		public static final int SYM_SA0 = 3;
		public static final int SYM_SA1 = 4;

		public static final int SYM_AND = 20;
		public static final int SYM_OR = 21;
		public static final int SYM_NAND = 22;
		public static final int SYM_NOR = 23;
		public static final int SYM_XOR = 24;
		public static final int SYM_XNOR = 25;
		public static final int SYM_BUF = 26;
		public static final int SYM_NOT = 27;

		public IscasScanner(Reader src_) throws IOException {
			super(src_);
			put("INPT", SYM_INPUT);
			put("FROM", SYM_FROM);
			put(">SA0", SYM_SA0);
			put(">SA1", SYM_SA1);
			put("AND", SYM_AND);
			put("NAND", SYM_NAND);
			put("OR", SYM_OR);
			put("NOR", SYM_NOR);
			put("XOR", SYM_XOR);
			put("XNOR", SYM_XNOR);
			put("BUFF", SYM_BUF);
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
				case '*':
					advanceLine();
					break;
				default:
					if (!Character.isWhitespace(current_char)) {
						current_sym = advanceIdentifier();
					} else {
						advanceChar();
					}
				}
			}

			return current_sym;
		}

		private int advanceIdentifier() {
			boolean is_int = true;
			while (!Character.isWhitespace(current_char)) {
				is_int &= Character.isDigit(current_char);
				appendToString(current_char);
				advanceChar();
			}
			int sym = SYM_IDENT;
			if (is_int)
				sym = SYM_INTEGER;
			else if (contains(getString().toUpperCase())) {
				sym = get(getString().toUpperCase());
			}
			return sym;
		}

		public int getSymbol() {
			return current_sym;
		}

	}

	static Graph load(InputStream is) throws IOException {
		Graph c = new Graph(new Library());
		IscasScanner scanner = new IscasScanner(new InputStreamReader(is));

		HashMap<Integer, Node> address_gate = new HashMap<Integer, Node>();
		
		int intf_pos = 0;

		scanner.assertNext(IscasScanner.SYM_INTEGER);
		while (scanner.hasNext()) {
			int adr = Integer.parseInt(scanner.getString());
			scanner.next();
			String name = scanner.getString();
			scanner.next();
			int type;
			switch (scanner.getSymbol()) {
			case IscasScanner.SYM_INPUT:
				type = Library.TYPE_BUF | Library.FLAG_INPUT;
				break;
			case IscasScanner.SYM_NOT:
				type = Library.TYPE_NOT;
				break;
			case IscasScanner.SYM_BUF:
				type = Library.TYPE_BUF;
				break;
			case IscasScanner.SYM_AND:
				type = Library.TYPE_AND;
				break;
			case IscasScanner.SYM_OR:
				type = Library.TYPE_OR;
				break;
			case IscasScanner.SYM_NAND:
				type = Library.TYPE_NAND;
				break;
			case IscasScanner.SYM_NOR:
				type = Library.TYPE_NOR;
				break;
			case IscasScanner.SYM_XOR:
				type = Library.TYPE_XOR;
				break;
			default:
				throw new IOException("Unknown gate type: " + scanner.getString());
			}
			scanner.assertNext(IscasScanner.SYM_INTEGER);
			int fanouts = Integer.parseInt(scanner.getString());
			scanner.assertNext(IscasScanner.SYM_INTEGER);
			int fanins = Integer.parseInt(scanner.getString());
			nextAndSkipFaults(scanner);
			ArrayList<Node> driver = new ArrayList<Node>();
			for (int i = 0; i < fanins; i++) {
				if (scanner.getSymbol() != IscasScanner.SYM_INTEGER)
					throw new IOException("Expected fanin address at " + scanner.pos());
				int address = Integer.parseInt(scanner.getString());
				if (!address_gate.containsKey(address))
					throw new IOException("Unknown address at " + scanner.pos());
				driver.add(address_gate.get(address));
				scanner.next();
			}
			Node g = c.new Node(name, type);
			for (Node drv : driver) {
				c.connect(drv, -1, g, -1);
			}
			if (g.isInput())
				g.setPosition(intf_pos++);
			if (fanouts == 0) {
				Node output = c.new Node(name + "_out", Library.TYPE_BUF | Library.FLAG_OUTPUT);
				c.connect(g, -1, output, 0);
				output.setPosition(intf_pos++);				
			}
			address_gate.put(adr, g);
			if (fanouts > 1) {
				for (int i = 0; i < fanouts; i++) {
					if (scanner.getSymbol() != IscasScanner.SYM_INTEGER)
						throw new IOException("Expected integer address at " + scanner.pos());
					int address = Integer.parseInt(scanner.getString());
					address_gate.put(address, g);
					scanner.next(); // name
					scanner.assertNext(IscasScanner.SYM_FROM);
					scanner.next(); // from name
					nextAndSkipFaults(scanner);
				}
			}
		}
		c.strip();
		return c;
	}

	private static int nextAndSkipFaults(IscasScanner scanner) {
		int sym = scanner.next();
		while (sym == IscasScanner.SYM_SA0 || sym == IscasScanner.SYM_SA1) {
			if (!scanner.hasNext())
				break;
			sym = scanner.next();
		}
		return sym;
	}
}
