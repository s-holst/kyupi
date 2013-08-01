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

import java.util.HashMap;

import org.kyupi.graph.Graph.Node;

public class LibrarySAED extends Library {

	// re-use Library.TYPE_ where possible.
	// new type ids: 0x10, 0x20 ... 0xff0


	public static final int TYPE_DEC24 = 0xc00 | FLAG_MULTIOUTPUT;
	public static final int TYPE_HADD = 0xd00 | FLAG_MULTIOUTPUT;
	public static final int TYPE_FADD = 0xe00 | FLAG_MULTIOUTPUT;
	public static final int TYPE_SDFFAR = 0xf00 | FLAG_MULTIOUTPUT | FLAG_SEQUENTIAL;

	public static final int TYPE_FADD_CO = 0xe10;

	private static final int[] SUBTYPES_DEC24 = { TYPE_NOR | INPUTS_2, TYPE_BGTA, TYPE_AGTB, TYPE_AND | INPUTS_2 };
	private static final int[] SUBTYPES_HADD = { TYPE_XOR | INPUTS_2, TYPE_AND | INPUTS_2 };
	private static final int[] SUBTYPES_FADD = { TYPE_XOR | INPUTS_3, TYPE_FADD_CO };

	private String[] pinNamesIN = { "IN" };
	private String[] pinNamesINx = { "IN1", "IN2", "IN3", "IN4", "IN5", "IN6" };
	private String[] pinNamesQ = { "Q" };
	private String[] pinNamesQQN = { "Q", "QN" };
	private String[] pinNamesQx = { "Q0", "Q1", "Q2", "Q3" };
	private String[] pinNamesZ = { "Z" };
	private String[] pinNamesZN = { "ZN" };
	private String[] pinNamesA0B0 = { "A0", "B0" };
	private String[] pinNamesABCI = { "A", "B", "CI" };
	private String[] pinNamesSCO = { "S", "CO" };
	private String[] pinNamesSOC1 = { "SO", "C1" };
	private String[] pinNamesDSSCR = { "D", "SE", "SI", "CLK", "RSTB" };

	private final int INTERFACE_SPEC_MASK = 0xfff | INPUTS_MASK | FLAG_MULTIOUTPUT | FLAG_SEQUENTIAL;

	private HashMap<Integer, InterfaceSpec> interfaceSpecSAED = new HashMap<Integer, InterfaceSpec>() {
		private static final long serialVersionUID = 1L;
		{
			put(TYPE_AND | INPUTS_2, new InterfaceSpec("AND2", pinNamesINx, 2, pinNamesQ, 1));
			put(TYPE_AND | INPUTS_3, new InterfaceSpec("AND3", pinNamesINx, 3, pinNamesQ, 1));
			put(TYPE_AND | INPUTS_4, new InterfaceSpec("AND4", pinNamesINx, 4, pinNamesQ, 1));
			put(TYPE_SDFFAR | INPUTS_5, new InterfaceSpec("SDFFAR", pinNamesDSSCR, 5, pinNamesQQN, 2));
			put(TYPE_FADD, new InterfaceSpec("FADD", pinNamesABCI, 3, pinNamesSCO, 2));
			put(TYPE_HADD, new InterfaceSpec("HADD", pinNamesA0B0, 2, pinNamesSOC1, 2));
			put(TYPE_CONST1, new InterfaceSpec("TIEH", pinNamesIN, 0, pinNamesZ, 1));
			put(TYPE_CONST0, new InterfaceSpec("TIEL", pinNamesIN, 0, pinNamesZN, 1));
			put(TYPE_DEC24, new InterfaceSpec("DEC24", pinNamesINx, 2, pinNamesQx, 4));
		}
	};

	private HashMap<String, Integer> typeNames = new HashMap<String, Integer>() {
		private static final long serialVersionUID = 1L;
		{
			put("AND2X1", TYPE_AND | INPUTS_2 | STRENGTH_1);
			put("AND2X2", TYPE_AND | INPUTS_2 | STRENGTH_2);
			put("AND2X4", TYPE_AND | INPUTS_2 | STRENGTH_4);
			put("AND3X1", TYPE_AND | INPUTS_3 | STRENGTH_1);
			put("SDFFARX1", TYPE_SDFFAR | INPUTS_5 | STRENGTH_1);
			put("SDFFARX2", TYPE_SDFFAR | INPUTS_5 | STRENGTH_2);
			put("TIEH", TYPE_CONST1);
			put("TIEL", TYPE_CONST0);
			put("HADDX1", TYPE_HADD | STRENGTH_1);
			put("HADDX2", TYPE_HADD | STRENGTH_2);
			put("FADDX1", TYPE_FADD | STRENGTH_1);
			put("FADDX2", TYPE_FADD | STRENGTH_1);
			put("DEC24X1", TYPE_DEC24 | STRENGTH_1);
			put("DEC24X2", TYPE_DEC24 | STRENGTH_2);
		}
	};

	public int pinDirection(int type, String name) {
		if (name.startsWith("Q"))
			return DIR_OUT;
		if (name.startsWith("Z"))
			return DIR_OUT;
		if ((type & INTERFACE_SPEC_MASK) == TYPE_FADD)
			if (name.equals("S") || name.equals("CO"))
				return DIR_OUT;
		if ((type & INTERFACE_SPEC_MASK) == TYPE_HADD)
			if (name.equals("SO") || name.equals("C1"))
				return DIR_OUT;
		return DIR_IN;
	}

	protected InterfaceSpec getInterfaceSpec(int type) {
		InterfaceSpec is = interfaceSpecSAED.get(type & INTERFACE_SPEC_MASK);
		if (is == null)
			is = super.getInterfaceSpec(type);
		return is;
	}

	protected HashMap<String, Integer> getTypeNames() {
		return typeNames;
	}

	protected String typeSuffix(int type) {
		String suffix = "";
		switch (type & STRENGTH_MASK) {
		case STRENGTH_0:
			suffix = "X0";
			break;
		case STRENGTH_1:
			suffix = "X1";
			break;
		case STRENGTH_2:
			suffix = "X2";
			break;
		case STRENGTH_4:
			suffix = "X4";
			break;
		case STRENGTH_8:
			suffix = "X8";
			break;
		case STRENGTH_16:
			suffix = "X16";
			break;
		case STRENGTH_32:
			suffix = "X32";
			break;
		}
		return suffix;
	}

	public long evaluate(int type, long[] inputs, int numInputs) {
		switch (type & INTERFACE_SPEC_MASK) {
		case TYPE_FADD_CO:
			return ((inputs[0] & inputs[1]) | (inputs[1] & inputs[2]) | (inputs[0] & inputs[2]));
		}
		return super.evaluate(type, inputs, numInputs);
	}

	public boolean isScanCell(int type) {
		if (isType(type, TYPE_SDFFAR))
			return true;
		return false;
	}

	public int getScanInPin(int type) {
		if (isType(type, TYPE_SDFFAR))
			return 2;
		throw new IllegalArgumentException("Given type is not a scan cell.");
	}

	public int pinIndex(int type, String name) {
		// Fixup for different lib versions
		if (name.equals("INP"))
			return 0;
		if (name.equals("Z"))
			return 0;
		if (name.equals("ZN"))
			return 0;
		return super.pinIndex(type, name);
	}

	public void replaceWithPseudoPort(Node scan_cell, Graph netlist) {
		if (isType(scan_cell.type(), TYPE_SDFFAR)) {
			Node d_in = scan_cell.in(0);
			Node q_out = scan_cell.out(0);
			int q_out_pin = -1;
			if (q_out != null)
				q_out_pin = q_out.searchInIdx(scan_cell);
			Node qn_out = scan_cell.out(1);
			int qn_out_pin = -1;
			if (qn_out != null)
				qn_out_pin = qn_out.searchInIdx(scan_cell);
			String name = scan_cell.queryName();
			int pos = scan_cell.position();
			scan_cell.remove();
			Node buf = netlist.new Node(name, Library.TYPE_BUF | Library.FLAG_INPUT | Library.FLAG_OUTPUT | LibraryNangate.FLAG_PSEUDO);
			buf.setPosition(pos);
			netlist.connect(d_in, -1, buf, 0);
			if (q_out != null) {
				netlist.connect(buf, 0, q_out, q_out_pin);
			}
			if (qn_out != null) {
				Node inv = netlist.new Node(name + "_genQN", Library.TYPE_NOT);
				netlist.connect(buf, -1, inv, 0);
				// buf.addSuccessor(inv, 0);
				// inv.setSuccessor(0, qn_out, qn_out_pin);
				netlist.connect(inv, -1, qn_out, qn_out_pin);
			}

			return;
		}
		throw new IllegalArgumentException("Given type is not a scan cell.");

	}

	public int getSubCell(int multi_output_type, int output_idx) {
		switch (multi_output_type & INTERFACE_SPEC_MASK) {
		case TYPE_DEC24:
			return SUBTYPES_DEC24[output_idx];
		case TYPE_HADD:
			return SUBTYPES_HADD[output_idx];
		case TYPE_FADD:
			return SUBTYPES_FADD[output_idx];
		}
		throw new IllegalArgumentException("Cannot get sub types from given type: " + multi_output_type);
	}

}
