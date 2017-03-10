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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.kyupi.graph.Graph.Node;

/**
 * is a library of the most common cell types.
 * 
 * The library includes names of cell types and pins, various functions for
 * obtaining cell parameters and simulate single cells. More comprehensive
 * libraries are implemented as sub-classes of this basic library class.
 * 
 * A cell type is encoded into a single 32-bit integer value 'type'. There are
 * various subsets of bits in this integer with specific functions as follows:
 * 
 * (highest bits:) IOP0 0000 vvvv vvvv iiii iiSM llll LLLL (:lowest bits)
 * 
 * 'l' and 'L' bits
 * 
 * The lowest 8 bits are reserved to encode the logic function of the cell. If
 * these bits are identical between two cell types, they must implement the same
 * logic function. The lowest 16 values of the 8-bit function code are assigned
 * to the most basic logic functions using the following truth table. The
 * constants <code>TYPE_*</code> are defined for each of the 16 possible
 * functions:
 * 
 * <pre>
 * bit 3 2 1 0
 * 
 * a   1 0 1 0
 * b   1 1 0 0
 * 
 *     0 0 0 0 = 0x0 = TYPE_CONST0 [0]
 *     0 0 0 1 = 0x1 = TYPE_NOR [>=2]
 *     0 0 1 0 = 0x2 = TYPE_AGTB  (a' nor b) (a and b') [2]
 *     0 0 1 1 = 0x3 = TYPE_BNOT [2]
 *     0 1 0 0 = 0x4 = TYPE_ALTB  (a nor 'b) (a' and b) [2]
 *     0 1 0 1 = 0x5 = TYPE_NOT [1]
 *     0 1 1 0 = 0x6 = TYPE_XOR [>=2]
 *     0 1 1 1 = 0x7 = TYPE_NAND [>=2]
 *     1 0 0 0 = 0x8 = TYPE_AND [>=2]
 *     1 0 0 1 = 0x9 = TYPE_XNOR [>=2]
 *     1 0 1 0 = 0xa = TYPE_BUF [1]
 *     1 0 1 1 = 0xb = TYPE_AGEB  (a or b') (a' nand b) (b -> a) [2]
 *     1 1 0 0 = 0xc = TYPE_BBUF [2]
 *     1 1 0 1 = 0xd = TYPE_ALEB  (a' or b) (a nand b') (a -> b) [2]
 *     1 1 1 0 = 0xe = TYPE_OR [>=2]
 *     1 1 1 1 = 0xf = TYPE_CONST1 [0]
 *     
 *     [x] = number of allowed inputs to the cell.
 *     
 *     For TYPE_BNOT and TYPE_BBUF, the first predecessor is ignored.
 * </pre>
 * 
 * Other libraries should use these types whenever the logic function of the
 * implemented cell is identical to one of these basic functions. When different
 * logic functions are implemented, values >=16 must be used (some l bits are
 * 1). 256 different logic functions are possible including the 16 basic ones.
 * 
 * 'M' bit
 * 
 * The M bit signifies a cell with multiple distinct outputs. Use constant
 * <code>FLAG_MULTIOUTPUT</code> to set this bit.
 * 
 * 'S' bit
 * 
 * The S bit signifies a cell with storage (sequential cell like a flip-flop).
 * Use constant <code>FLAG_SEQUENTIAL</code> to set this bit.
 * 
 * 'i' bits
 * 
 * These bits encode the number of inputs of the cell. At most 63 inputs are
 * possible.
 * 
 * 'v' bits
 * 
 * These are variants of the same cell implemented by the libraries. E.g.
 * different names or strengths.
 * 
 * 'I' bit, 'O' bit, 'P' bit
 * 
 * These bits signify primary inputs, primary outputs of circuits and pseudo
 * cells in the graph. Any cell can be flagged as input or output in principle.
 * By convention, however, only buffers should be used for that.
 * 
 * Whenever the lowest 16 bits (until 'S' flag) of the cell type are identical,
 * they must behave exactly the same in zero-delay logic simulation.
 * 
 * 
 */

public class Library {

	protected static Logger log = Logger.getLogger(Library.class);

	public static final int FLAG_INPUT = 0x8000_0000;
	public static final int FLAG_OUTPUT = 0x4000_0000;
	public static final int FLAG_PSEUDO = 0x2000_0000;

	public static final int FLAG_SEQUENTIAL = 0x0200;
	public static final int FLAG_MULTIOUTPUT = 0x0100;

	public static final int TYPE_CONST0 = 0x0;
	public static final int TYPE_NOR = 0x1;
	public static final int TYPE_AGTB = 0x2;
	public static final int TYPE_BNOT = 0x3;
	public static final int TYPE_BGTA = 0x4;
	public static final int TYPE_NOT = 0x5;
	public static final int TYPE_XOR = 0x6;
	public static final int TYPE_NAND = 0x7;
	public static final int TYPE_AND = 0x8;
	public static final int TYPE_XNOR = 0x9;
	public static final int TYPE_BUF = 0xa;
	public static final int TYPE_AGEB = 0xb;
	public static final int TYPE_BBUF = 0xc;
	public static final int TYPE_BGEA = 0xd;
	public static final int TYPE_OR = 0xe;
	public static final int TYPE_CONST1 = 0xf;

	public static final int TYPE_DFF = TYPE_BUF | FLAG_SEQUENTIAL;

	public static final int MASK_FUNCTION = 0xff;

	public static final int VARIANT_1 = 0x1_0000;
	public static final int VARIANT_2 = 0x2_0000;
	public static final int VARIANT_3 = 0x3_0000;

	protected static final int INPUTS_OFFSET = 10; // .... iiii ii-- ---- ----
	protected static final int INPUTS_MASK = 0x3f << INPUTS_OFFSET;
	public static final int INPUTS_UNKNOWN = 0 << INPUTS_OFFSET;
	public static final int INPUTS_1 = 1 << INPUTS_OFFSET;
	public static final int INPUTS_2 = 2 << INPUTS_OFFSET;
	public static final int INPUTS_3 = 3 << INPUTS_OFFSET;
	public static final int INPUTS_4 = 4 << INPUTS_OFFSET;
	public static final int INPUTS_5 = 5 << INPUTS_OFFSET;
	public static final int INPUTS_6 = 6 << INPUTS_OFFSET;
	public static final int INPUTS_7 = 7 << INPUTS_OFFSET;
	public static final int INPUTS_8 = 8 << INPUTS_OFFSET;

	protected static final int STRENGTH_MASK = 0xf0_0000;
	public static final int STRENGTH_UNKNOWN = 0x00_0000;
	public static final int STRENGTH_0 = 0xf0_0000;
	public static final int STRENGTH_1 = 0x10_0000;
	public static final int STRENGTH_2 = 0x20_0000;
	public static final int STRENGTH_4 = 0x30_0000;
	public static final int STRENGTH_8 = 0x40_0000;
	public static final int STRENGTH_16 = 0x50_0000;
	public static final int STRENGTH_32 = 0x60_0000;
	public static final int STRENGTH_64 = 0x70_0000;

	public static final int DIR_OUT = 0;
	public static final int DIR_IN = 1;

	private String[] inputPortNames = { "0", "1", "2", "3", "4", "5" };
	private String[] outputPortNames = { "0" };

	protected class InterfaceSpec {

		public String typeName;
		public String[] inputPortNames;
		public int inputPorts;
		public String[] outputPortNames;
		public int outputPorts;

		public InterfaceSpec(String typeName, String[] inputPinNames, int inputPinCount, String[] outputPinNames,
				int outputPinCount) {
			this.typeName = typeName;
			this.inputPortNames = inputPinNames;
			this.inputPorts = inputPinCount;
			this.outputPortNames = outputPinNames;
			this.outputPorts = outputPinCount;
		}
	}

	private static final int MASK_INTFSPEC = 0x3ff;

	private HashMap<Integer, InterfaceSpec> interfaceSpec = new HashMap<Integer, InterfaceSpec>() {
		private static final long serialVersionUID = 1L;
		{
			put(TYPE_CONST0, new InterfaceSpec("CONST0", inputPortNames, 0, outputPortNames, 1));
			put(TYPE_NOR, new InterfaceSpec("NOR", inputPortNames, 2, outputPortNames, 1));
			put(TYPE_AGTB, new InterfaceSpec("AGTB", inputPortNames, 2, outputPortNames, 1));
			put(TYPE_BNOT, new InterfaceSpec("BNOT", inputPortNames, 2, outputPortNames, 1));
			put(TYPE_BGTA, new InterfaceSpec("BGTA", inputPortNames, 2, outputPortNames, 1));
			put(TYPE_NOT, new InterfaceSpec("NOT", inputPortNames, 1, outputPortNames, 1));
			put(TYPE_XOR, new InterfaceSpec("XOR", inputPortNames, 2, outputPortNames, 1));
			put(TYPE_NAND, new InterfaceSpec("NAND", inputPortNames, 2, outputPortNames, 1));
			put(TYPE_AND, new InterfaceSpec("AND", inputPortNames, 2, outputPortNames, 1));
			put(TYPE_XNOR, new InterfaceSpec("XNOR", inputPortNames, 2, outputPortNames, 1));
			put(TYPE_BUF, new InterfaceSpec("BUF", inputPortNames, 1, outputPortNames, 1));
			put(TYPE_AGEB, new InterfaceSpec("AGEB", inputPortNames, 2, outputPortNames, 1));
			put(TYPE_BBUF, new InterfaceSpec("BBUF", inputPortNames, 2, outputPortNames, 1));
			put(TYPE_BGEA, new InterfaceSpec("BGEA", inputPortNames, 2, outputPortNames, 1));
			put(TYPE_OR, new InterfaceSpec("OR", inputPortNames, 2, outputPortNames, 1));
			put(TYPE_CONST1, new InterfaceSpec("CONST1", inputPortNames, 0, outputPortNames, 1));
			put(TYPE_DFF, new InterfaceSpec("DFF", inputPortNames, 1, outputPortNames, 1));
		}
	};

	private HashMap<String, Integer> typeNames = new HashMap<String, Integer>() {
		private static final long serialVersionUID = 1L;
		{
			put("CONST0", TYPE_CONST0);
			put("NOR", TYPE_NOR);
			put("AGTB", TYPE_AGTB);
			put("BNOT", TYPE_BNOT);
			put("BGTA", TYPE_BGTA);
			put("NOT", TYPE_NOT);
			put("XOR", TYPE_XOR);
			put("NAND", TYPE_NAND);
			put("AND", TYPE_AND);
			put("XNOR", TYPE_XNOR);
			put("BUF", TYPE_BUF);
			put("AGEB", TYPE_AGEB);
			put("BBUF", TYPE_BBUF);
			put("BGEA", TYPE_BGEA);
			put("OR", TYPE_OR);
			put("CONST1", TYPE_CONST1);
			put("DFF", TYPE_DFF);
		}
	};

	public void checkValid(int type) {
		// if ((type & FLAG_INPUT) != 0 && (type & FLAG_PPINPUT) != 0)
		// throw new
		// IllegalArgumentException("Gate cannot be both primary and
		// pseudo-primary input.");
	}

	public boolean isPseudo(int type) {
		return (type & (FLAG_PSEUDO)) != 0;
	}

	public boolean isPrimary(int type) {
		return (type & (FLAG_INPUT | FLAG_OUTPUT)) != 0;
	}

	public boolean isSequential(int type) {
		return (type & (FLAG_SEQUENTIAL)) != 0;
	}

	public boolean isInput(int type) {
		return (type & FLAG_INPUT) != 0;
	}

	public boolean isOutput(int type) {
		return (type & FLAG_OUTPUT) != 0;
	}

	public boolean isPort(int type) {
		return (isInput(type) || isOutput(type)) && !isPseudo(type);
	}

	public boolean isType(int type, int other) {
		return (type & MASK_FUNCTION) == (other & MASK_FUNCTION);
	}

	public boolean isMultiOutput(int type) {
		return (type & FLAG_MULTIOUTPUT) != 0;
	}

	public String typeName(int type) {
		InterfaceSpec is = getInterfaceSpec(type);
		if (is == null) {
			throw new IllegalArgumentException("Unable to resolve cell type: 0x" + Integer.toHexString(type));
		}
		return is.typeName + typeSuffix(type);
	}

	public int pinIndex(int type, String name) {
		InterfaceSpec is = getInterfaceSpec(type);
		if (is == null)
			return -1;
		int idx = 0;
		for (String s : is.inputPortNames) {
			if (s.equals(name))
				return idx;
			idx++;
		}
		idx = 0;
		for (String s : is.outputPortNames) {
			if (s.equals(name))
				return idx;
			idx++;
		}
		return -1;
	}

	public String inputPinName(int type, int idx) {
		InterfaceSpec is = getInterfaceSpec(type);
		if (idx >= is.inputPorts)
			return "?";
		return is.inputPortNames[idx];
	}

	public String outputPinName(int type, int idx) {
		InterfaceSpec is = getInterfaceSpec(type);
		if (idx >= is.outputPorts)
			return "?";
		return is.outputPortNames[idx];
	}

	protected String typeSuffix(int type) {
		return "";
	}

	protected InterfaceSpec getInterfaceSpec(int type) {
		return interfaceSpec.get(type & MASK_INTFSPEC);
	}

	public int pinDirection(int type, String key) {
		throw new UnsupportedOperationException("can not get port direction from name in basic library.");
	}

	public int resolve(String name) {
		HashMap<String, Integer> typeNames = getTypeNames();
		if (!typeNames.containsKey(name)) {
			throw new IllegalArgumentException("Unable to resolve type name: " + name);
		}
		return typeNames.get(name);
	}

	protected HashMap<String, Integer> getTypeNames() {
		return typeNames;
	}

	public Node buildPredecessorTree(Graph g, ArrayList<Node> gates, String name, int type) {

		LinkedList<Node> q = new LinkedList<Node>(gates);
		int itype = type & MASK_FUNCTION;

		if (isInput(type) || itype == Library.TYPE_CONST0 || itype == Library.TYPE_CONST1) {
			if (q.size() != 0)
				throw new IllegalArgumentException("No drivers allowed for gate of type: " + type);
			return null;
		}
		if (itype == Library.TYPE_BUF || itype == Library.TYPE_NOT) {
			if (q.size() != 1)
				throw new IllegalArgumentException("Exactly one input expected for gate of type: " + itype);
			Node n = g.new Node(name, type);
			g.connect(q.poll(), -1, n, 0);
			return n;
		}
		if (q.size() < 2) {
			throw new IllegalArgumentException("At least two parameters expected for gate: " + name);
		}
		switch (itype) {
		case Library.TYPE_NAND:
			itype = Library.TYPE_AND;
			break;
		case Library.TYPE_NOR:
			itype = Library.TYPE_OR;
			break;
		case Library.TYPE_XNOR:
			itype = Library.TYPE_XOR;
			break;
		}
		int todo = q.size();
		while (todo > 2) {
			Node n = g.new Node(name + "_" + todo, itype);
			g.connect(q.poll(), -1, n, 0);
			g.connect(q.poll(), -1, n, 1);
			q.add(n);
			todo--;
		}
		Node n = g.new Node(name, type);
		g.connect(q.poll(), -1, n, 0);
		g.connect(q.poll(), -1, n, 1);
		return n;
	}

	public void propagate(int type, long[] inV, long[] inC, int inOffset, int inCount, long[] outV, long[] outC,
			int outOffset, int outCount) {

		long j = 0L;
		long k = 0L;
		long l = 0L;

		switch (type & 0xff) {
		case TYPE_CONST0:
			outC[outOffset] = -1L;
			outV[outOffset] = 0L;
			return;
		case TYPE_NOR:
			for (int i = 0; i < inCount; i++) {
				l |= ~inC[inOffset + i] & inV[inOffset + i];
				k |= inC[inOffset + i] & inV[inOffset + i];
				j |= ~inC[inOffset + i] & ~inV[inOffset + i];
			}
			outC[outOffset] = -1L;
			outV[outOffset] = -1L;
			outC[outOffset] &= ~j;
			outV[outOffset] &= ~j;
			outC[outOffset] |= k;
			outV[outOffset] &= ~k;
			outC[outOffset] &= ~l;
			outV[outOffset] |= l;
			return;
		case TYPE_AGTB:
			for (int i = 0; i < inCount; i++) {
				l |= ~inC[inOffset + i] & inV[inOffset + i];
				j |= ~inC[inOffset + i] & ~inV[inOffset + i];
			}
			k = (inC[inOffset] & ~inV[inOffset]) | (inC[inOffset + 1] & inV[inOffset + 1]);
			outC[outOffset] = -1L;
			outV[outOffset] = -1L;
			outC[outOffset] &= ~j;
			outV[outOffset] &= ~j;
			outC[outOffset] |= k;
			outV[outOffset] &= ~k;
			outC[outOffset] &= ~l;
			outV[outOffset] |= l;
			return;
		case TYPE_BNOT:
			outC[outOffset] = inC[inOffset + 1];
			outV[outOffset] = inC[inOffset + 1] ^ inV[inOffset + 1];
			return;
		case TYPE_BGTA:
			for (int i = 0; i < inCount; i++) {
				l |= ~inC[inOffset + i] & inV[inOffset + i];
				j |= ~inC[inOffset + i] & ~inV[inOffset + i];
			}
			k = (inC[inOffset] & inV[inOffset]) | (inC[inOffset + 1] & ~inV[inOffset + 1]);
			outC[outOffset] = -1L;
			outV[outOffset] = -1L;
			outC[outOffset] &= ~j;
			outV[outOffset] &= ~j;
			outC[outOffset] |= k;
			outV[outOffset] &= ~k;
			outC[outOffset] &= ~l;
			outV[outOffset] |= l;
			return;
		case TYPE_NOT:
			outC[outOffset] = inC[inOffset];
			outV[outOffset] = inC[inOffset] ^ inV[inOffset];
			return;
		case TYPE_XOR:
			for (int i = 0; i < inCount; i++) {
				l |= ~inC[inOffset + i] & inV[inOffset + i];
				k |= ~inC[inOffset + i] & ~inV[inOffset + i];
				j ^= inV[inOffset + i];
			}
			outC[outOffset] = -1L;
			outV[outOffset] = j;
			outC[outOffset] &= ~k;
			outV[outOffset] &= ~k;
			outC[outOffset] &= ~l;
			outV[outOffset] |= l;
			return;
		case TYPE_NAND:
			for (int i = 0; i < inCount; i++) {
				l |= ~inC[inOffset + i] & inV[inOffset + i];
				k |= inC[inOffset + i] & ~inV[inOffset + i];
				j |= ~inC[inOffset + i] & ~inV[inOffset + i];
			}
			outC[outOffset] = -1L;
			outV[outOffset] = -1L;
			outC[outOffset] &= ~j;
			outV[outOffset] &= ~j;
			outC[outOffset] |= k;
			outV[outOffset] &= ~k;
			outC[outOffset] &= ~l;
			outV[outOffset] |= l;
			outV[outOffset] ^= outC[outOffset];
			return;
		case TYPE_AND:
			for (int i = 0; i < inCount; i++) {
				l |= ~inC[inOffset + i] & inV[inOffset + i];
				k |= inC[inOffset + i] & ~inV[inOffset + i];
				j |= ~inC[inOffset + i] & ~inV[inOffset + i];
			}
			outC[outOffset] = -1L;
			outV[outOffset] = -1L;
			outC[outOffset] &= ~j;
			outV[outOffset] &= ~j;
			outC[outOffset] |= k;
			outV[outOffset] &= ~k;
			outC[outOffset] &= ~l;
			outV[outOffset] |= l;
			return;
		case TYPE_XNOR:
			for (int i = 0; i < inCount; i++) {
				l |= ~inC[inOffset + i] & inV[inOffset + i];
				k |= ~inC[inOffset + i] & ~inV[inOffset + i];
				j ^= inV[inOffset + i];
			}
			outC[outOffset] = -1L;
			outV[outOffset] = j;
			outC[outOffset] &= ~k;
			outV[outOffset] &= ~k;
			outC[outOffset] &= ~l;
			outV[outOffset] |= l;
			outV[outOffset] ^= outC[outOffset];
			return;
		case TYPE_BUF:
			outC[outOffset] = inC[inOffset];
			outV[outOffset] = inV[inOffset];
			return;
		case TYPE_AGEB:
			for (int i = 0; i < inCount; i++)
				l |= ~inC[inOffset + i] & inV[inOffset + i];
			k = ~inC[inOffset] & ~inV[inOffset] & ~inC[inOffset + 1] & ~inV[inOffset + 1];
			j = inV[inOffset] | ~inV[inOffset + 1];
			outC[outOffset] = -1L;
			outV[outOffset] = 0L;
			outC[outOffset] = -1L;
			outV[outOffset] = j;
			outC[outOffset] &= ~k;
			outV[outOffset] &= ~k;
			outC[outOffset] &= ~l;
			outV[outOffset] |= l;
			return;
		case TYPE_BBUF:
			outC[outOffset] = inC[inOffset + 1];
			outV[outOffset] = inV[inOffset + 1];
			return;
		case TYPE_BGEA:
			for (int i = 0; i < inCount; i++)
				l |= ~inC[inOffset + i] & inV[inOffset + i];
			k = ~inC[inOffset] & ~inV[inOffset] & ~inC[inOffset + 1] & ~inV[inOffset + 1];
			j = ~inV[inOffset] | inV[inOffset + 1];
			outC[outOffset] = -1L;
			outV[outOffset] = 0L;
			outC[outOffset] = -1L;
			outV[outOffset] = j;
			outC[outOffset] &= ~k;
			outV[outOffset] &= ~k;
			outC[outOffset] &= ~l;
			outV[outOffset] |= l;
			return;
		case TYPE_OR:
			for (int i = 0; i < inCount; i++) {
				l |= ~inC[inOffset + i] & inV[inOffset + i];
				k |= inC[inOffset + i] & inV[inOffset + i];
				j |= ~inC[inOffset + i] & ~inV[inOffset + i];
			}
			outC[outOffset] = -1L;
			outV[outOffset] = 0L;
			outC[outOffset] &= ~j;
			outV[outOffset] &= ~j;
			outC[outOffset] |= k;
			outV[outOffset] |= k;
			outC[outOffset] &= ~l;
			outV[outOffset] |= l;
			return;
		case TYPE_CONST1:
			outC[outOffset] = -1L;
			outV[outOffset] = 0L;
			return;
		}

		throw new RuntimeException("logic function of cell type unknown: 0x" + Integer.toHexString(type));
	}

	public boolean isScanCell(int type) {
		return false;
	}

	public int getScanInPin(int type) {
		throw new UnsupportedOperationException("Scan cells are not supported by this Library");
	}

	public int getClockPin(int type) {
		throw new UnsupportedOperationException("Sequential cells are not supported by this Library");
	}

	public int getSubCell(int multi_output_type, int output_idx) {
		throw new UnsupportedOperationException("Multi-output cells are not supported by this Library");
	}

	public long[] calcOutput(int type, int output_idx, long v, long c) {
		long cv[] = new long[2];
		cv[0] = c;
		cv[1] = v;
		return cv;
	}
}
