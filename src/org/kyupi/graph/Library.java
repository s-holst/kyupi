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
 * The type of a cell is encoded into a single 32-bit integer value. The lowest
 * 8 bits are reserved to encode the cell function. The upper 24 bits are used for
 * several flags and other parameters.
 * 
 * ffff f000 iiii ssss vvvv vvvv llll LLLL
 * 
 * The lowest 16 values of the 8-bit function code are assigned to the most basic
 * logic functions. 
 * With two inputs, 16 Boolean functions with two inputs are possible. The
 * constants <code>TYPE_*</code> define those by encoding the truth table in the
 * following way:
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
 *     [x] = number of allowed predecessors (inputs to the cell).
 *     
 *     For TYPE_BNOT and TYPE_BBUF, the first predecessor is ignored.
 * </pre>
 * 
 * 
 * 
 */
public class Library {

	protected static Logger log = Logger.getLogger(Library.class);

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

	public static final int MASK_FUNCTION = 0xffff;

	public static final int FLAG_INPUT = 0x8000_0000;
	public static final int FLAG_OUTPUT = 0x4000_0000;
	public static final int FLAG_PSEUDO = 0x2000_0000;
	public static final int FLAG_SEQUENTIAL = 0x1000_0000;
	public static final int FLAG_MULTIOUTPUT = 0x800_0000;

	protected static final int INPUTS_MASK = 0xf0_0000;
	public static final int INPUTS_UNKNOWN = 0x00_0000;
	public static final int INPUTS_1 = 0x10_0000;
	public static final int INPUTS_2 = 0x20_0000;
	public static final int INPUTS_3 = 0x30_0000;
	public static final int INPUTS_4 = 0x40_0000;
	public static final int INPUTS_5 = 0x50_0000;
	public static final int INPUTS_6 = 0x60_0000;
	public static final int INPUTS_7 = 0x70_0000;
	public static final int INPUTS_8 = 0x80_0000;

	protected static final int STRENGTH_MASK = 0xf_0000;
	public static final int STRENGTH_UNKNOWN = 0x0_0000;
	public static final int STRENGTH_0 = 0xf_0000;
	public static final int STRENGTH_1 = 0x1_0000;
	public static final int STRENGTH_2 = 0x2_0000;
	public static final int STRENGTH_4 = 0x3_0000;
	public static final int STRENGTH_8 = 0x4_0000;
	public static final int STRENGTH_16 = 0x5_0000;
	public static final int STRENGTH_32 = 0x6_0000;
	public static final int STRENGTH_64 = 0x7_0000;

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

		public InterfaceSpec(String typeName, String[] inputPinNames, int inputPinCount, String[] outputPinNames, int outputPinCount) {
			this.typeName = typeName;
			this.inputPortNames = inputPinNames;
			this.inputPorts = inputPinCount;
			this.outputPortNames = outputPinNames;
			this.outputPorts = outputPinCount;
		}
	}

	private final int INTERFACE_SPEC_MASK = 0xfff;

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
		}
	};

	public void checkValid(int type) {
		// if ((type & FLAG_INPUT) != 0 && (type & FLAG_PPINPUT) != 0)
		// throw new
		// IllegalArgumentException("Gate cannot be both primary and pseudo-primary input.");
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
		return interfaceSpec.get(type & INTERFACE_SPEC_MASK);
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

	public long evaluate(int type, long[] inputs, int numInputs) {
		long l = 0L;
		switch (type & 0xf) {
		case TYPE_CONST0:
			return 0L;
		case TYPE_NOR:
			for (int i = 0; i < numInputs; i++)
				l |= inputs[i];
			return ~l;
		case TYPE_AGTB:
			return inputs[0] & ~inputs[1];
		case TYPE_BNOT:
			return ~inputs[1];
		case TYPE_BGTA:
			return inputs[1] & ~inputs[0];
		case TYPE_NOT:
			return ~inputs[0];
		case TYPE_XOR:
			l = 0L;
			for (int i = 0; i < numInputs; i++)
				l ^= inputs[i];
			return l;
		case TYPE_NAND:
			l = -1L;
			for (int i = 0; i < numInputs; i++)
				l &= inputs[i];
			return ~l;
		case TYPE_AND:
			l = -1L;
			for (int i = 0; i < numInputs; i++)
				l &= inputs[i];
			return l;
		case TYPE_XNOR:
			l = -1L;
			for (int i = 0; i < numInputs; i++)
				l ^= inputs[i];
			return l;
		case TYPE_BUF:
			return inputs[0];
		case TYPE_AGEB:
			return inputs[0] | ~inputs[1];
		case TYPE_BBUF:
			return inputs[1];
		case TYPE_BGEA:
			return inputs[1] | ~inputs[0];
		case TYPE_OR:
			for (int i = 0; i < numInputs; i++)
				l |= inputs[i];
			return l;
		case TYPE_CONST1:
			return -1L;
		}
		throw new RuntimeException("impossible. should not reach here.");
	}

	public long[] evaluate(int type, long[] inputsV, long[] inputsC, int numInputs){
		long cv[] = new long[2];
		long j = 0L;
		long k = 0L;
		long l = 0L;
		
		switch (type & 0xf) {
		case TYPE_CONST0:
			cv[0] = -1L;
			cv[1] = 0L;
			return cv;
		case TYPE_NOR:
			for (int i = 0; i < numInputs; i++){
				l |= ~inputsC[i] & inputsV[i];
				k |= inputsC[i] & inputsV[i];
				j |= ~inputsC[i] & ~inputsV[i];
			}
			cv[0] = -1L; cv[1] = -1L;
			cv[0] &= ~j; cv[1] &= ~j;
			cv[0] |= k;  cv[1] &= ~k;
			cv[0] &= ~l; cv[1] |= l;		
			return cv;
		case TYPE_AGTB:
			for (int i = 0; i < numInputs; i++){
				l |= ~inputsC[i] & inputsV[i];
				j |= ~inputsC[i] & ~inputsV[i];
			}
			k = (inputsC[0] & ~inputsV[0]) | (inputsC[1] & inputsV[1]);
			cv[0] = -1L; cv[1] = -1L;
			cv[0] &= ~j; cv[1] &= ~j;
			cv[0] |= k;  cv[1] &= ~k;
			cv[0] &= ~l; cv[1] |= l;
			return cv;			
		case TYPE_BNOT:
			cv[0] = inputsC[1];
			cv[1] = inputsC[1] ^ inputsV[1]; 
			return cv;		
		case TYPE_BGTA:
			for (int i = 0; i < numInputs; i++){
				l |= ~inputsC[i] & inputsV[i];
				j |= ~inputsC[i] & ~inputsV[i];
			}
			k = (inputsC[0] & inputsV[0]) | (inputsC[1] & ~inputsV[1]);
			cv[0] = -1L; cv[1] = -1L;
			cv[0] &= ~j; cv[1] &= ~j;
			cv[0] |= k;  cv[1] &= ~k;
			cv[0] &= ~l; cv[1] |= l;
			return cv;					
		case TYPE_NOT:
			cv[0] = inputsC[0];
			cv[1] = inputsC[0] ^ inputsV[0]; 
			return cv;
		case TYPE_XOR:
			for (int i = 0; i < numInputs; i++){
				l |= ~inputsC[i] & inputsV[i];
				k |= ~inputsC[i] & ~inputsV[i];
				j ^= inputsV[i];
			}
			cv[0] = -1L;  cv[1] = j;
			cv[0] &= ~k; cv[1] &= ~k;
			cv[0] &= ~l; cv[1] |= l;
			return cv;	
		case TYPE_NAND:
			for (int i = 0; i < numInputs; i++){
				l |= ~inputsC[i] & inputsV[i];
				k |= inputsC[i] & ~inputsV[i];
				j |= ~inputsC[i] & ~inputsV[i];
			}
			cv[0] = -1L; cv[1] = -1L;
			cv[0] &= ~j; cv[1] &= ~j;
			cv[0] |= k;	 cv[1] &= ~k;
			cv[0] &= ~l; cv[1] |= l;
			cv[1] ^= cv[0];
			return cv;
		case TYPE_AND:
			for (int i = 0; i < numInputs; i++){
				l |= ~inputsC[i] & inputsV[i];
				k |= inputsC[i] & ~inputsV[i];
				j |= ~inputsC[i] & ~inputsV[i];
			}
			cv[0] = -1L; cv[1] = -1L;
			cv[0] &= ~j; cv[1] &= ~j;
			cv[0] |= k;	 cv[1] &= ~k;
			cv[0] &= ~l; cv[1] |= l;		
			return cv;
		case TYPE_XNOR:
			for (int i = 0; i < numInputs; i++){
				l |= ~inputsC[i] & inputsV[i];
				k |= ~inputsC[i] & ~inputsV[i];
				j ^= inputsV[i];
			}
			cv[0] = -1L;  cv[1] = j;
			cv[0] &= ~k; cv[1] &= ~k;
			cv[0] &= ~l; cv[1] |= l;
			cv[1] ^= cv[0];
			return cv;	
		case TYPE_BUF:
			cv[0] = inputsC[0];
			cv[1] = inputsV[0];
			return cv;
		case TYPE_AGEB:
			for(int i = 0; i < numInputs; i++)
				l |= ~inputsC[i] & inputsV[i];
			k = ~inputsC[0] & ~inputsV[0] & ~inputsC[1] & ~inputsV[1];
			j = inputsV[0] | ~inputsV[1];
			cv[0] = -1L; cv[1] = 0L;
			cv[0] = -1L; cv[1] = j;
			cv[0] &= ~k; cv[1] &= ~k;
			cv[0] &= ~l; cv[1] |= l;
			return cv;
		case TYPE_BBUF:
			cv[0] = inputsC[1];
			cv[1] = inputsV[1];
			return cv;	
		case TYPE_BGEA:
			for(int i = 0; i < numInputs; i++)
				l |= ~inputsC[i] & inputsV[i];
			k = ~inputsC[0] & ~inputsV[0] & ~inputsC[1] & ~inputsV[1];
			j = ~inputsV[0] | inputsV[1];
			cv[0] = -1L; cv[1] = 0L;
			cv[0] = -1L; cv[1] = j;
			cv[0] &= ~k; cv[1] &= ~k;
			cv[0] &= ~l; cv[1] |= l;
			return cv;	
		case TYPE_OR:
			for (int i = 0; i < numInputs; i++){
				l |= ~inputsC[i] & inputsV[i];
				k |= inputsC[i] & inputsV[i];
				j |= ~inputsC[i] & ~inputsV[i];
			}
			cv[0] = -1L; cv[1] = 0L;
			cv[0] &= ~j; cv[1] &= ~j;
			cv[0] |= k;	 cv[1] |= k;
			cv[0] &= ~l; cv[1] |= l;		
			return cv;		
		case TYPE_CONST1:
			cv[0] = -1L;
			cv[1] = 0L;
			return cv;	
		}
		
		throw new RuntimeException("impossible. should not reach here.");
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

	public void replaceWithPseudoPort(Node scan_cell, Graph netlist) {
		throw new UnsupportedOperationException("Scan cells are not supported by this Library");
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
