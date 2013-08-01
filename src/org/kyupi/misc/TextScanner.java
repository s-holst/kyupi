/*
 * Copyright 2013 The KyuPI project contributors. See the COPYRIGHT.md file
 * at the top-level directory of this distribution.
 * This file is part of the KyuPI project. It is subject to the license terms
 * in the LICENSE.md file found in the top-level directory of this distribution.
 * No part of the KyuPI project, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.md file.
 */
package org.kyupi.misc;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;

/**
 * is a simple base class for implementing text file tokenizers.
 * 
 * @author stefan
 *
 */
public abstract class TextScanner implements Iterable<Integer>, Iterator<Integer> {

	protected static Logger log = Logger.getLogger(TextScanner.class);

	public static final int SYM_NONE = 0;
	public static final int SYM_EOF = -1;
	public static final int SYM_IDENT = -2;
	public static final int SYM_STRING = -3;
	public static final int SYM_INTEGER = -4;

	protected char current_char;

	protected int current_int;

	protected int current_sym;

	private StringBuffer current_string = new StringBuffer(1024);

	private Reader src;

	private int line = 1;

	private int col = 1;

	private HashMap<String, Integer> str2sym = new HashMap<String, Integer>();

	private HashMap<Integer, String> sym2str = new HashMap<Integer, String>();

	public TextScanner(Reader src_) {
		src = src_;

		put("<NONE>", SYM_NONE);
		put("<EOF>", SYM_EOF);
		put("<IDENT>", SYM_IDENT);
		put("<STRING>", SYM_STRING);
		put("<INTEGER>", SYM_INTEGER);

		advanceChar();
	}

	protected void put(String str, int sym) {
		str2sym.put(str, sym);
		sym2str.put(sym, str);
	}

	public String get(int sym) {
		return sym2str.get(sym);
	}

	public boolean contains(String string) {
		return str2sym.containsKey(string);
	}

	public int get(String symbol) {
		return str2sym.get(symbol);
	}

	public int getLine() {
		return line;
	}

	public int getCol() {
		return col;
	}

	public String getString() {
		if (current_string.length() == 0)
			return get(current_sym);
		return current_string.toString();
	}

	public void appendToString(char c) {
		current_string.append(c);
	}

	public void clearString() {
		current_string.setLength(0);
	}

	protected void advanceChar() {
		try {
			current_int = src.read();
		} catch (IOException e) {
			e.printStackTrace();
			current_int = -1;
		}
		col++;
		if (current_int != -1)
			current_char = (char) current_int;
		if (current_char == '\n') {
			line++;
			col = 1;
		}
	}

	protected void advanceLine() {
		while ((current_int != -1) && (current_char != '\n') && (current_char != '\r')) {
			current_string.append(current_char);
			advanceChar();
		}
	}

	protected void advanceString() {
		boolean skip_ws = false;
		char delimiter = current_char;
		advanceChar();
		while ((current_int != -1) && (current_char != delimiter)) {
			if (current_char == '\\') {
				skip_ws = true;
			} else {
				if (!(Character.isWhitespace(current_char) && skip_ws)) {
					current_string.append(current_char);
					skip_ws = false;
				}
			}
			advanceChar();
		}
		if (current_char == delimiter)
			advanceChar();
	}
	
	public String pos() {
		return "" + getLine() + ":" + getCol();
	}

	public void assertNext(int sym) throws IOException {
		int nsym = next();
		if (nsym != sym)
			throw new IOException("Expected '" + get(sym) + "', but found '" + get(nsym) + "' at " + pos());
	}

	public boolean hasNext() {
		return current_int != -1;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

	public Iterator<Integer> iterator() {
		return this;
	}
}
