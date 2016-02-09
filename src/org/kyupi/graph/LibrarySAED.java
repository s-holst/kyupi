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
	public static final int TYPE_AO21 = 0x10;
	public static final int TYPE_AO221 = 0x20;
	public static final int TYPE_AO222 = 0x30;
	public static final int TYPE_AO22 = 0x40;
	public static final int TYPE_AOBUF = 0x50 | TYPE_BUF;
	public static final int TYPE_AOI21 = 0x60;
	public static final int TYPE_AOI221 = 0x70;
	public static final int TYPE_AOI222 = 0x80;
	public static final int TYPE_AOI22 = 0x90;
	public static final int TYPE_AOINV = 0xa0 | TYPE_NOT;

	public static final int TYPE_OA21 = 0xb0;
	public static final int TYPE_OA221 = 0xc0;
	public static final int TYPE_OA222 = 0xd0;
	public static final int TYPE_OA22 = 0xe0;
	public static final int TYPE_OAI21 = 0xf0;
	public static final int TYPE_OAI221 = 0x100;
	public static final int TYPE_OAI222 = 0x110;
	public static final int TYPE_OAI22 = 0x120;

	public static final int TYPE_DELLN = 0x140 | TYPE_BUF;
	public static final int TYPE_IBUFF = 0x150 | TYPE_NOT;
	public static final int TYPE_INV = 0x160 | TYPE_NOT;
	public static final int TYPE_MUX21 = 0x170;
	public static final int TYPE_MUX41 = 0x180;
	public static final int TYPE_NBUFF = 0x190 | TYPE_BUF;

	public static final int TYPE_DEC24 = 0xc00 | FLAG_MULTIOUTPUT;
	public static final int TYPE_HADD = 0xd00 | FLAG_MULTIOUTPUT;
	public static final int TYPE_FADD = 0xe00 | FLAG_MULTIOUTPUT;
	public static final int TYPE_SDFFAR = 0xf00 | FLAG_MULTIOUTPUT | FLAG_SEQUENTIAL;
	public static final int TYPE_DFFAR = 0xf10 | FLAG_MULTIOUTPUT | FLAG_SEQUENTIAL;

	public static final int TYPE_FADD_CO = 0xe10;

	private static final int[] SUBTYPES_DEC24 = { TYPE_NOR | INPUTS_2, TYPE_BGTA, TYPE_AGTB, TYPE_AND | INPUTS_2 };
	private static final int[] SUBTYPES_HADD = { TYPE_XOR | INPUTS_2, TYPE_AND | INPUTS_2 };
	private static final int[] SUBTYPES_FADD = { TYPE_XOR | INPUTS_3, TYPE_FADD_CO };

	private String[] pinNamesIN = { "IN" };
	private String[] pinNamesINx = { "IN1", "IN2", "IN3", "IN4", "IN5", "IN6" };
	private String[] pinNamesIN2S = { "IN1", "IN2", "S" };
	private String[] pinNamesIN4S1 = { "IN1", "IN2", "IN3", "IN4", "S0", "S1" };
	private String[] pinNamesQ = { "Q" };
	private String[] pinNamesQN = { "QN" };
	private String[] pinNamesQQN = { "Q", "QN" };
	private String[] pinNamesQx = { "Q0", "Q1", "Q2", "Q3" };
	private String[] pinNamesZ = { "Z" };
	private String[] pinNamesZN = { "ZN" };
	private String[] pinNamesA0B0 = { "A0", "B0" };
	private String[] pinNamesABCI = { "A", "B", "CI" };
	private String[] pinNamesSCO = { "S", "CO" };
	private String[] pinNamesSOC1 = { "SO", "C1" };
	private String[] pinNamesDSSCR = { "D", "SE", "SI", "CLK", "RSTB" };
	private String[] pinNamesDCR = { "D", "CLK", "RSTB" };

	private final int INTERFACE_SPEC_MASK = 0xfff | INPUTS_MASK | FLAG_MULTIOUTPUT | FLAG_SEQUENTIAL;

	private HashMap<Integer, InterfaceSpec> interfaceSpecSAED = new HashMap<Integer, InterfaceSpec>() {
		private static final long serialVersionUID = 1L;
		{
			put(TYPE_AND | INPUTS_2, new InterfaceSpec("AND2", pinNamesINx, 2, pinNamesQ, 1));
			put(TYPE_AND | INPUTS_3, new InterfaceSpec("AND3", pinNamesINx, 3, pinNamesQ, 1));
			put(TYPE_AND | INPUTS_4, new InterfaceSpec("AND4", pinNamesINx, 4, pinNamesQ, 1));
			put(TYPE_AO21, new InterfaceSpec("AO21", pinNamesINx, 3, pinNamesQ, 1));
			put(TYPE_AO221, new InterfaceSpec("AO221", pinNamesINx, 5, pinNamesQ, 1));
			put(TYPE_AO222, new InterfaceSpec("AO222", pinNamesINx, 6, pinNamesQ, 1));
			put(TYPE_AO22, new InterfaceSpec("AO22", pinNamesINx, 4, pinNamesQ, 1));
			put(TYPE_AOBUF, new InterfaceSpec("AOBUF", pinNamesIN, 1, pinNamesQ, 1));
			put(TYPE_AOI21, new InterfaceSpec("AOI21", pinNamesINx, 3, pinNamesQN, 1));
			put(TYPE_AOI221, new InterfaceSpec("AOI221", pinNamesINx, 5, pinNamesQN, 1));
			put(TYPE_AOI222, new InterfaceSpec("AOI222", pinNamesINx, 6, pinNamesQN, 1));
			put(TYPE_AOI22, new InterfaceSpec("AOI22", pinNamesINx, 4, pinNamesQN, 1));
			put(TYPE_AOINV, new InterfaceSpec("AOINV", pinNamesIN, 1, pinNamesQN, 1));
			put(TYPE_DELLN, new InterfaceSpec("DELLN1", pinNamesIN, 1, pinNamesQ, 1));
			put(TYPE_DELLN, new InterfaceSpec("DELLN2", pinNamesIN, 1, pinNamesQ, 1));
			put(TYPE_DELLN, new InterfaceSpec("DELLN3", pinNamesIN, 1, pinNamesQ, 1));
			put(TYPE_IBUFF, new InterfaceSpec("IBUFF", pinNamesIN, 1, pinNamesQN, 1));
			put(TYPE_INV, new InterfaceSpec("INV", pinNamesIN, 1, pinNamesQN, 1));
			put(TYPE_MUX21, new InterfaceSpec("MUX21", pinNamesIN2S, 3, pinNamesQ, 1));
			put(TYPE_MUX41, new InterfaceSpec("MUX41", pinNamesIN4S1, 6, pinNamesQ, 1));
			put(TYPE_NAND | INPUTS_2, new InterfaceSpec("NAND2", pinNamesINx, 2, pinNamesQN, 1));
			put(TYPE_NAND | INPUTS_3, new InterfaceSpec("NAND3", pinNamesINx, 3, pinNamesQN, 1));
			put(TYPE_NAND | INPUTS_4, new InterfaceSpec("NAND4", pinNamesINx, 4, pinNamesQN, 1));
			put(TYPE_NBUFF, new InterfaceSpec("NBUFF", pinNamesIN, 1, pinNamesQ, 1));
			put(TYPE_NOR | INPUTS_2, new InterfaceSpec("NOR2", pinNamesINx, 2, pinNamesQN, 1));
			put(TYPE_NOR | INPUTS_3, new InterfaceSpec("NOR3", pinNamesINx, 3, pinNamesQN, 1));
			put(TYPE_NOR | INPUTS_4, new InterfaceSpec("NOR4", pinNamesINx, 4, pinNamesQN, 1));
			put(TYPE_OA21, new InterfaceSpec("OA21", pinNamesINx, 3, pinNamesQ, 1));
			put(TYPE_OA221, new InterfaceSpec("OA221", pinNamesINx, 5, pinNamesQ, 1));
			put(TYPE_OA222, new InterfaceSpec("OA222", pinNamesINx, 6, pinNamesQ, 1));
			put(TYPE_OA22, new InterfaceSpec("OA22", pinNamesINx, 4, pinNamesQ, 1));
			put(TYPE_OAI21, new InterfaceSpec("OAI21", pinNamesINx, 3, pinNamesQN, 1));
			put(TYPE_OAI221, new InterfaceSpec("OAI221", pinNamesINx, 5, pinNamesQN, 1));
			put(TYPE_OAI222, new InterfaceSpec("OAI222", pinNamesINx, 6, pinNamesQN, 1));
			put(TYPE_OAI22, new InterfaceSpec("OAI22", pinNamesINx, 4, pinNamesQN, 1));
			put(TYPE_OR | INPUTS_2, new InterfaceSpec("OR2", pinNamesINx, 2, pinNamesQ, 1));
			put(TYPE_OR | INPUTS_3, new InterfaceSpec("OR3", pinNamesINx, 3, pinNamesQ, 1));
			put(TYPE_OR | INPUTS_4, new InterfaceSpec("OR4", pinNamesINx, 4, pinNamesQ, 1));
			put(TYPE_XNOR | INPUTS_2, new InterfaceSpec("XNOR2", pinNamesINx, 2, pinNamesQ, 1));
			put(TYPE_XNOR | INPUTS_3, new InterfaceSpec("XNOR3", pinNamesINx, 3, pinNamesQ, 1));
			put(TYPE_XOR | INPUTS_2, new InterfaceSpec("XOR2", pinNamesINx, 2, pinNamesQ, 1));
			put(TYPE_XOR | INPUTS_3, new InterfaceSpec("XOR3", pinNamesINx, 3, pinNamesQ, 1));
			put(TYPE_SDFFAR | INPUTS_5, new InterfaceSpec("SDFFAR", pinNamesDSSCR, 5, pinNamesQQN, 2));
			put(TYPE_DFFAR | INPUTS_3, new InterfaceSpec("DFFAR", pinNamesDCR, 3, pinNamesQQN, 2));
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
			put("AND3X2", TYPE_AND | INPUTS_3 | STRENGTH_2);
			put("AND3X4", TYPE_AND | INPUTS_3 | STRENGTH_4);
			put("AND4X1", TYPE_AND | INPUTS_4 | STRENGTH_1);
			put("AND4X2", TYPE_AND | INPUTS_4 | STRENGTH_2);
			put("AND4X4", TYPE_AND | INPUTS_4 | STRENGTH_4);
			put("AO21X1", TYPE_AO21 | STRENGTH_1);
			put("AO21X2", TYPE_AO21 | STRENGTH_2);
			put("AO221X1", TYPE_AO221 | STRENGTH_1);
			put("AO221X2", TYPE_AO221 | STRENGTH_2);
			put("AO222X1", TYPE_AO222 | STRENGTH_1);
			put("AO222X2", TYPE_AO222 | STRENGTH_2);
			put("AO22X1", TYPE_AO22 | STRENGTH_1);
			put("AO22X2", TYPE_AO22 | STRENGTH_2);
			put("AOBUFX1", TYPE_AOBUF | STRENGTH_1);
			put("AOBUFX2", TYPE_AOBUF | STRENGTH_2);
			put("AOBUFX4", TYPE_AOBUF | STRENGTH_4);
			put("AOI21X1", TYPE_AOI21 | STRENGTH_1);
			put("AOI21X2", TYPE_AOI21 | STRENGTH_2);
			put("AOI221X1", TYPE_AOI221 | STRENGTH_1);
			put("AOI221X2", TYPE_AOI221 | STRENGTH_2);
			put("AOI222X1", TYPE_AOI222 | STRENGTH_1);
			put("AOI222X2", TYPE_AOI222 | STRENGTH_2);
			put("AOI22X1", TYPE_AOI22 | STRENGTH_1);
			put("AOI22X2", TYPE_AOI22 | STRENGTH_2);
			put("AOINVX1", TYPE_AOINV | STRENGTH_1);
			put("AOINVX2", TYPE_AOINV | STRENGTH_2);
			put("AOINVX4", TYPE_AOINV | STRENGTH_4);
			put("DELLN1X2", TYPE_DELLN | STRENGTH_2);
			put("DELLN2X2", TYPE_DELLN | STRENGTH_2);
			put("DELLN3X2", TYPE_DELLN | STRENGTH_2);
			put("IBUFFX16", TYPE_IBUFF | STRENGTH_16);
			put("IBUFFX2", TYPE_IBUFF | STRENGTH_2);
			put("IBUFFX32", TYPE_IBUFF | STRENGTH_32);
			put("IBUFFX4", TYPE_IBUFF | STRENGTH_4);
			put("IBUFFX8", TYPE_IBUFF | STRENGTH_8);
			put("INVX0", TYPE_INV | STRENGTH_0);
			put("INVX16", TYPE_INV | STRENGTH_16);
			put("INVX1", TYPE_INV | STRENGTH_1);
			put("INVX2", TYPE_INV | STRENGTH_2);
			put("INVX32", TYPE_INV | STRENGTH_32);
			put("INVX4", TYPE_INV | STRENGTH_4);
			put("INVX8", TYPE_INV | STRENGTH_8);
			put("MUX21X1", TYPE_MUX21 | STRENGTH_1);
			put("MUX21X2", TYPE_MUX21 | STRENGTH_2);
			put("MUX41X1", TYPE_MUX41 | STRENGTH_1);
			put("MUX41X2", TYPE_MUX41 | STRENGTH_2);
			put("NAND2X0", TYPE_NAND | INPUTS_2 | STRENGTH_0);
			put("NAND2X1", TYPE_NAND | INPUTS_2 | STRENGTH_1);
			put("NAND2X2", TYPE_NAND | INPUTS_2 | STRENGTH_2);
			put("NAND2X4", TYPE_NAND | INPUTS_2 | STRENGTH_4);
			put("NAND3X0", TYPE_NAND | INPUTS_3 | STRENGTH_0);
			put("NAND3X1", TYPE_NAND | INPUTS_3 | STRENGTH_1);
			put("NAND3X2", TYPE_NAND | INPUTS_3 | STRENGTH_2);
			put("NAND3X4", TYPE_NAND | INPUTS_3 | STRENGTH_4);
			put("NAND4X0", TYPE_NAND | INPUTS_3 | STRENGTH_0);
			put("NAND4X1", TYPE_NAND | INPUTS_3 | STRENGTH_1);
			put("NBUFFX16", TYPE_NBUFF | STRENGTH_16);
			put("NBUFFX2", TYPE_NBUFF | STRENGTH_2);
			put("NBUFFX32", TYPE_NBUFF | STRENGTH_32);
			put("NBUFFX4", TYPE_NBUFF | STRENGTH_4);
			put("NBUFFX8", TYPE_NBUFF | STRENGTH_8);
			put("NOR2X0", TYPE_NOR | INPUTS_2 | STRENGTH_0);
			put("NOR2X1", TYPE_NOR | INPUTS_2 | STRENGTH_1);
			put("NOR2X2", TYPE_NOR | INPUTS_2 | STRENGTH_2);
			put("NOR2X4", TYPE_NOR | INPUTS_2 | STRENGTH_4);
			put("NOR3X0", TYPE_NOR | INPUTS_3 | STRENGTH_0);
			put("NOR3X1", TYPE_NOR | INPUTS_3 | STRENGTH_1);
			put("NOR3X2", TYPE_NOR | INPUTS_3 | STRENGTH_2);
			put("NOR3X4", TYPE_NOR | INPUTS_3 | STRENGTH_4);
			put("NOR4X0", TYPE_NOR | INPUTS_4 | STRENGTH_0);
			put("NOR4X1", TYPE_NOR | INPUTS_4 | STRENGTH_1);
			put("OA21X1", TYPE_OA21 | STRENGTH_1);
			put("OA21X2", TYPE_OA21 | STRENGTH_2);
			put("OA221X1", TYPE_OA221 | STRENGTH_1);
			put("OA221X2", TYPE_OA221 | STRENGTH_2);
			put("OA222X1", TYPE_OA222 | STRENGTH_1);
			put("OA222X2", TYPE_OA222 | STRENGTH_2);
			put("OA22X1", TYPE_OA22 | STRENGTH_1);
			put("OA22X2", TYPE_OA22 | STRENGTH_2);
			put("OAI21X1", TYPE_OAI21 | STRENGTH_1);
			put("OAI21X2", TYPE_OAI21 | STRENGTH_2);
			put("OAI221X1", TYPE_OAI221 | STRENGTH_1);
			put("OAI221X2", TYPE_OAI221 | STRENGTH_2);
			put("OAI222X1", TYPE_OAI222 | STRENGTH_1);
			put("OAI222X2", TYPE_OAI222 | STRENGTH_2);
			put("OAI22X1", TYPE_OAI22 | STRENGTH_1);
			put("OAI22X2", TYPE_OAI22 | STRENGTH_2);
			put("OR2X1", TYPE_OR | INPUTS_2 | STRENGTH_1);
			put("OR2X2", TYPE_OR | INPUTS_2 | STRENGTH_2);
			put("OR2X4", TYPE_OR | INPUTS_2 | STRENGTH_4);
			put("OR3X1", TYPE_OR | INPUTS_3 | STRENGTH_1);
			put("OR3X2", TYPE_OR | INPUTS_3 | STRENGTH_2);
			put("OR3X4", TYPE_OR | INPUTS_3 | STRENGTH_4);
			put("OR4X1", TYPE_OR | INPUTS_4 | STRENGTH_1);
			put("OR4X2", TYPE_OR | INPUTS_4 | STRENGTH_2);
			put("OR4X4", TYPE_OR | INPUTS_4 | STRENGTH_4);
			put("XNOR2X1", TYPE_XNOR  | INPUTS_2 | STRENGTH_1);
			put("XNOR2X2", TYPE_XNOR  | INPUTS_2 | STRENGTH_2);
			put("XNOR3X1", TYPE_XNOR  | INPUTS_3 | STRENGTH_1);
			put("XNOR3X2", TYPE_XNOR  | INPUTS_3 | STRENGTH_2);
			put("XOR2X1", TYPE_XOR  | INPUTS_2 | STRENGTH_1);
			put("XOR2X2", TYPE_XOR  | INPUTS_2 | STRENGTH_2);
			put("XOR3X1", TYPE_XOR  | INPUTS_3 | STRENGTH_1);
			put("XOR3X2", TYPE_XOR  | INPUTS_3 | STRENGTH_2);
			put("SDFFARX1", TYPE_SDFFAR | INPUTS_5 | STRENGTH_1);
			put("SDFFARX2", TYPE_SDFFAR | INPUTS_5 | STRENGTH_2);
			put("DFFARX1", TYPE_DFFAR | INPUTS_3 | STRENGTH_1);
			put("DFFARX2", TYPE_DFFAR | INPUTS_3 | STRENGTH_2);
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
		case TYPE_AO21:
			return ((inputs[0] & inputs[1]) | inputs[2]);
		case TYPE_AO221:
			return ((inputs[0] & inputs[1]) | (inputs[2] & inputs[3]) | inputs[4]);
		case TYPE_AO222:
			return ((inputs[0] & inputs[1]) | (inputs[2] & inputs[3]) | (inputs[4] & inputs[5]));
		case TYPE_AO22:
			return ((inputs[0] & inputs[1]) | (inputs[2] & inputs[3]));
		case TYPE_AOI21:
			return ~((inputs[0] & inputs[1]) | inputs[2]);
		case TYPE_AOI221:
			return ~((inputs[0] & inputs[1]) | (inputs[2] & inputs[3]) | inputs[4]);
		case TYPE_AOI222:
			return ~((inputs[0] & inputs[1]) | (inputs[2] & inputs[3]) | (inputs[4] & inputs[5]));
		case TYPE_AOI22:
			return ~((inputs[0] & inputs[1]) | (inputs[2] & inputs[3]));
		case TYPE_OA21:
			return ((inputs[0] | inputs[1]) & inputs[2]);
		case TYPE_OA221:
			return ((inputs[0] | inputs[1]) & (inputs[2] | inputs[3]) & inputs[4]);
		case TYPE_OA222:
			return ((inputs[0] | inputs[1]) & (inputs[2] | inputs[3]) & (inputs[4] | inputs[5]));
		case TYPE_OA22:
			return ((inputs[0] | inputs[1]) & (inputs[2] | inputs[3]));
		case TYPE_OAI21:
			return ~((inputs[0] | inputs[1]) & inputs[2]);
		case TYPE_OAI221:
			return ~((inputs[0] | inputs[1]) & (inputs[2] | inputs[3]) & inputs[4]);
		case TYPE_OAI222:
			return ~((inputs[0] | inputs[1]) & (inputs[2] | inputs[3]) & (inputs[4] | inputs[5]));
		case TYPE_OAI22:
			return ~((inputs[0] | inputs[1]) & (inputs[2] | inputs[3]));
		case TYPE_MUX21:
			return ((inputs[0] & ~inputs[2]) | (inputs[1] & inputs[2]));
		case TYPE_MUX41:
			return ((inputs[0] & ~inputs[4] & ~inputs[5]) | (inputs[1] & ~inputs[4] & inputs[5]) | (inputs[2] & inputs[4] & ~inputs[5]) | (inputs[3]
					& inputs[4] & inputs[5]));
		case TYPE_FADD_CO:
			return ((inputs[0] & inputs[1]) | (inputs[1] & inputs[2]) | (inputs[0] & inputs[2]));
		}
		return super.evaluate(type, inputs, numInputs);
	}
	
	public long[] evaluate(int type, long[] inputsV, long[] inputsC, int numInputs) {
		long cv[] = new long[2];
		long TinputsC[] = new long[3];
		long TinputsV[] = new long[3];
		long j = 0L;
		long k = 0L;
		long l = 0L;
		
		switch (type & INTERFACE_SPEC_MASK) {
		case TYPE_AO21:
			for (int i = 0; i < 2; i++){
				l |= ~inputsC[i] & inputsV[i];
				k |= inputsC[i] & ~inputsV[i];
				j |= ~inputsC[i] & ~inputsV[i];
			}
			TinputsC[0] = -1L; TinputsV[0] = -1L;
			TinputsC[0] &= ~j; TinputsV[0] &= ~j;			
			TinputsC[0] |= k;  TinputsV[0] &= ~k;			
			TinputsC[0] &= ~l; TinputsV[0] |= l;
			l = 0L; k = 0L; j = 0L;
			TinputsC[1] = inputsC[2];
			TinputsV[1] = inputsV[2];
			for (int i = 0; i < 2; i++){
				l |= ~TinputsC[i] & TinputsV[i];
				k |= TinputsC[i] & TinputsV[i];
				j |= ~TinputsC[i] & ~TinputsV[i];
			}
			cv[0] = -1L; cv[1] =  0L;
			cv[0] &= ~j; cv[1] &= ~j;			
			cv[0] |= k;  cv[1] |= k;			
			cv[0] &= ~l; cv[1] |= l;			
			return cv;			
		case TYPE_AO221:
			for (int i = 0; i < 2; i++){
				l |= ~inputsC[i] & inputsV[i];
				k |= inputsC[i] & ~inputsV[i];
				j |= ~inputsC[i] & ~inputsV[i];
			}
			TinputsC[0] = -1L; TinputsV[0] = -1L;
			TinputsC[0] &= ~j; TinputsV[0] &= ~j;			
			TinputsC[0] |= k;  TinputsV[0] &= ~k;			
			TinputsC[0] &= ~l; TinputsV[0] |= l;
			l = 0L; k = 0L; j = 0L;
			for (int i = 2; i < 4; i++){
				l |= ~inputsC[i] & inputsV[i];
				k |= inputsC[i] & ~inputsV[i];
				j |= ~inputsC[i] & ~inputsV[i];
			}
			TinputsC[1] = -1L; TinputsV[1] = -1L;
			TinputsC[1] &= ~j; TinputsV[1] &= ~j;			
			TinputsC[1] |= k;  TinputsV[1] &= ~k;			
			TinputsC[1] &= ~l; TinputsV[1] |= l;
			l = 0L; k = 0L; j = 0L;	
			TinputsC[2] = inputsC[4];
			TinputsV[2] = inputsV[4];
			for (int i = 0; i < 3; i++){
				l |= ~TinputsC[i] & TinputsV[i];
				k |= TinputsC[i] & TinputsV[i];
				j |= ~TinputsC[i] & ~TinputsV[i];
			}
			cv[0] = -1L; cv[1] =  0L;
			cv[0] &= ~j; cv[1] &= ~j;			
			cv[0] |= k;  cv[1] |= k;			
			cv[0] &= ~l; cv[1] |= l;
			return cv;
		case TYPE_AO222:
			for (int i = 0; i < 2; i++){
				l |= ~inputsC[i] & inputsV[i];
				k |= inputsC[i] & ~inputsV[i];
				j |= ~inputsC[i] & ~inputsV[i];
			}
			TinputsC[0] = -1L; TinputsV[0] = -1L;
			TinputsC[0] &= ~j; TinputsV[0] &= ~j;			
			TinputsC[0] |= k;  TinputsV[0] &= ~k;			
			TinputsC[0] &= ~l; TinputsV[0] |= l;
			l = 0L; k = 0L; j = 0L;
			for (int i = 2; i < 4; i++){
				l |= ~inputsC[i] & inputsV[i];
				k |= inputsC[i] & ~inputsV[i];
				j |= ~inputsC[i] & ~inputsV[i];
			}
			TinputsC[1] = -1L; TinputsV[1] = -1L;
			TinputsC[1] &= ~j; TinputsV[1] &= ~j;			
			TinputsC[1] |= k;  TinputsV[1] &= ~k;			
			TinputsC[1] &= ~l; TinputsV[1] |= l;
			l = 0L; k = 0L; j = 0L;	
			for (int i = 4; i < 6; i++){
				l |= ~inputsC[i] & inputsV[i];
				k |= inputsC[i] & ~inputsV[i];
				j |= ~inputsC[i] & ~inputsV[i];
			}
			TinputsC[2] = -1L; TinputsV[2] = -1L;
			TinputsC[2] &= ~j; TinputsV[2] &= ~j;			
			TinputsC[2] |= k;  TinputsV[2] &= ~k;			
			TinputsC[2] &= ~l; TinputsV[2] |= l;
			l = 0L; k = 0L; j = 0L;	
			for (int i = 0; i < 3; i++){
				l |= ~TinputsC[i] & TinputsV[i];
				k |= TinputsC[i] & TinputsV[i];
				j |= ~TinputsC[i] & ~TinputsV[i];
			}
			cv[0] = -1L; cv[1] =  0L;
			cv[0] &= ~j; cv[1] &= ~j;			
			cv[0] |= k;  cv[1] |= k;			
			cv[0] &= ~l; cv[1] |= l;
			return cv;
		case TYPE_AO22:
			for (int i = 0; i < 2; i++){
				l |= ~inputsC[i] & inputsV[i];
				k |= inputsC[i] & ~inputsV[i];
				j |= ~inputsC[i] & ~inputsV[i];
			}
			TinputsC[0] = -1L; TinputsV[0] = -1L;
			TinputsC[0] &= ~j; TinputsV[0] &= ~j;			
			TinputsC[0] |= k;  TinputsV[0] &= ~k;			
			TinputsC[0] &= ~l; TinputsV[0] |= l;
			l = 0L; k = 0L; j = 0L;
			for (int i = 2; i < 4; i++){
				l |= ~inputsC[i] & inputsV[i];
				k |= inputsC[i] & ~inputsV[i];
				j |= ~inputsC[i] & ~inputsV[i];
			}
			TinputsC[1] = -1L; TinputsV[1] = -1L;
			TinputsC[1] &= ~j; TinputsV[1] &= ~j;			
			TinputsC[1] |= k;  TinputsV[1] &= ~k;			
			TinputsC[1] &= ~l; TinputsV[1] |= l;
			l = 0L; k = 0L; j = 0L;	
			for (int i = 0; i < 2; i++){
				l |= ~TinputsC[i] & TinputsV[i];
				k |= TinputsC[i] & TinputsV[i];
				j |= ~TinputsC[i] & ~TinputsV[i];
			}
			cv[0] = -1L; cv[1] =  0L;
			cv[0] &= ~j; cv[1] &= ~j;			
			cv[0] |= k;  cv[1] |= k;			
			cv[0] &= ~l; cv[1] |= l;
			return cv;
		case TYPE_AOI21:
			for (int i = 0; i < 2; i++){
				l |= ~inputsC[i] & inputsV[i];
				k |= inputsC[i] & ~inputsV[i];
				j |= ~inputsC[i] & ~inputsV[i];
			}
			TinputsC[0] = -1L; TinputsV[0] = -1L;
			TinputsC[0] &= ~j; TinputsV[0] &= ~j;			
			TinputsC[0] |= k;  TinputsV[0] &= ~k;			
			TinputsC[0] &= ~l; TinputsV[0] |= l;
			l = 0L; k = 0L; j = 0L;
			TinputsC[1] = inputsC[2];
			TinputsV[1] = inputsV[2];
			for (int i = 0; i < 2; i++){
				l |= ~TinputsC[i] & TinputsV[i];
				k |= TinputsC[i] & TinputsV[i];
				j |= ~TinputsC[i] & ~TinputsV[i];
			}
			cv[0] = -1L; cv[1] =  0L;
			cv[0] &= ~j; cv[1] &= ~j;			
			cv[0] |= k;  cv[1] |= k;			
			cv[0] &= ~l; cv[1] |= l;
			cv[1] ^= cv[0];
			return cv;	
		case TYPE_AOI221:
			for (int i = 0; i < 2; i++){
				l |= ~inputsC[i] & inputsV[i];
				k |= inputsC[i] & ~inputsV[i];
				j |= ~inputsC[i] & ~inputsV[i];
			}
			TinputsC[0] = -1L; TinputsV[0] = -1L;
			TinputsC[0] &= ~j; TinputsV[0] &= ~j;			
			TinputsC[0] |= k;  TinputsV[0] &= ~k;			
			TinputsC[0] &= ~l; TinputsV[0] |= l;
			l = 0L; k = 0L; j = 0L;
			for (int i = 2; i < 4; i++){
				l |= ~inputsC[i] & inputsV[i];
				k |= inputsC[i] & ~inputsV[i];
				j |= ~inputsC[i] & ~inputsV[i];
			}
			TinputsC[1] = -1L; TinputsV[1] = -1L;
			TinputsC[1] &= ~j; TinputsV[1] &= ~j;			
			TinputsC[1] |= k;  TinputsV[1] &= ~k;			
			TinputsC[1] &= ~l; TinputsV[1] |= l;
			l = 0L; k = 0L; j = 0L;	
			TinputsC[2] = inputsC[4];
			TinputsV[2] = inputsV[4];
			for (int i = 0; i < 3; i++){
				l |= ~TinputsC[i] & TinputsV[i];
				k |= TinputsC[i] & TinputsV[i];
				j |= ~TinputsC[i] & ~TinputsV[i];
			}
			cv[0] = -1L; cv[1] =  0L;
			cv[0] &= ~j; cv[1] &= ~j;			
			cv[0] |= k;  cv[1] |= k;			
			cv[0] &= ~l; cv[1] |= l;
			cv[1] ^= cv[0];
			return cv;
		case TYPE_AOI222:
			for (int i = 0; i < 2; i++){
				l |= ~inputsC[i] & inputsV[i];
				k |= inputsC[i] & ~inputsV[i];
				j |= ~inputsC[i] & ~inputsV[i];
			}
			TinputsC[0] = -1L; TinputsV[0] = -1L;
			TinputsC[0] &= ~j; TinputsV[0] &= ~j;			
			TinputsC[0] |= k;  TinputsV[0] &= ~k;			
			TinputsC[0] &= ~l; TinputsV[0] |= l;
			l = 0L; k = 0L; j = 0L;
			for (int i = 2; i < 4; i++){
				l |= ~inputsC[i] & inputsV[i];
				k |= inputsC[i] & ~inputsV[i];
				j |= ~inputsC[i] & ~inputsV[i];
			}
			TinputsC[1] = -1L; TinputsV[1] = -1L;
			TinputsC[1] &= ~j; TinputsV[1] &= ~j;			
			TinputsC[1] |= k;  TinputsV[1] &= ~k;			
			TinputsC[1] &= ~l; TinputsV[1] |= l;
			l = 0L; k = 0L; j = 0L;	
			for (int i = 4; i < 6; i++){
				l |= ~inputsC[i] & inputsV[i];
				k |= inputsC[i] & ~inputsV[i];
				j |= ~inputsC[i] & ~inputsV[i];
			}
			TinputsC[2] = -1L; TinputsV[2] = -1L;
			TinputsC[2] &= ~j; TinputsV[2] &= ~j;			
			TinputsC[2] |= k;  TinputsV[2] &= ~k;			
			TinputsC[2] &= ~l; TinputsV[2] |= l;
			l = 0L; k = 0L; j = 0L;	
			for (int i = 0; i < 3; i++){
				l |= ~TinputsC[i] & TinputsV[i];
				k |= TinputsC[i] & TinputsV[i];
				j |= ~TinputsC[i] & ~TinputsV[i];
			}
			cv[0] = -1L; cv[1] =  0L;
			cv[0] &= ~j; cv[1] &= ~j;			
			cv[0] |= k;  cv[1] |= k;			
			cv[0] &= ~l; cv[1] |= l;
			cv[1] ^= cv[0];
			return cv;
		case TYPE_AOI22:
			for (int i = 0; i < 2; i++){
				l |= ~inputsC[i] & inputsV[i];
				k |= inputsC[i] & ~inputsV[i];
				j |= ~inputsC[i] & ~inputsV[i];
			}
			TinputsC[0] = -1L; TinputsV[0] = -1L;
			TinputsC[0] &= ~j; TinputsV[0] &= ~j;			
			TinputsC[0] |= k;  TinputsV[0] &= ~k;			
			TinputsC[0] &= ~l; TinputsV[0] |= l;
			l = 0L; k = 0L; j = 0L;
			for (int i = 2; i < 4; i++){
				l |= ~inputsC[i] & inputsV[i];
				k |= inputsC[i] & ~inputsV[i];
				j |= ~inputsC[i] & ~inputsV[i];
			}
			TinputsC[1] = -1L; TinputsV[1] = -1L;
			TinputsC[1] &= ~j; TinputsV[1] &= ~j;			
			TinputsC[1] |= k;  TinputsV[1] &= ~k;			
			TinputsC[1] &= ~l; TinputsV[1] |= l;
			l = 0L; k = 0L; j = 0L;	
			for (int i = 0; i < 2; i++){
				l |= ~TinputsC[i] & TinputsV[i];
				k |= TinputsC[i] & TinputsV[i];
				j |= ~TinputsC[i] & ~TinputsV[i];
			}
			cv[0] = -1L; cv[1] =  0L;
			cv[0] &= ~j; cv[1] &= ~j;			
			cv[0] |= k;  cv[1] |= k;			
			cv[0] &= ~l; cv[1] |= l;
			cv[1] ^= cv[0]; 
			return cv;
		case TYPE_OA21:
			for (int i = 0; i < 2; i++){
				l |= ~inputsC[i] & inputsV[i];
				k |= inputsC[i] & inputsV[i];
				j |= ~inputsC[i] & ~inputsV[i];
			}
			TinputsC[0] = -1L; TinputsV[0] = 0L;
			TinputsC[0] &= ~j; TinputsV[0] &= ~j;			
			TinputsC[0] |= k;  TinputsV[0] |= k;			
			TinputsC[0] &= ~l; TinputsV[0] |= l;
			l = 0L; k = 0L; j = 0L;
			TinputsC[1] = inputsC[2];
			TinputsV[1] = inputsV[2];
			for (int i = 0; i < 2; i++){
				l |= ~TinputsC[i] & TinputsV[i];
				k |= TinputsC[i] & ~TinputsV[i];
				j |= ~TinputsC[i] & ~TinputsV[i];
			}
			cv[0] = -1L; cv[1] = -1L;
			cv[0] &= ~j; cv[1] &= ~j;			
			cv[0] |= k;  cv[1] &= ~k;			
			cv[0] &= ~l; cv[1] |= l;
			return cv;
		case TYPE_OA221:
			for (int i = 0; i < 2; i++){
				l |= ~inputsC[i] & inputsV[i];
				k |= inputsC[i] & inputsV[i];
				j |= ~inputsC[i] & ~inputsV[i];
			}
			TinputsC[0] = -1L; TinputsV[0] = 0L;
			TinputsC[0] &= ~j; TinputsV[0] &= ~j;			
			TinputsC[0] |= k;  TinputsV[0] |= k;			
			TinputsC[0] &= ~l; TinputsV[0] |= l;
			l = 0L; k = 0L; j = 0L;
			for (int i = 2; i < 4; i++){
				l |= ~inputsC[i] & inputsV[i];
				k |= inputsC[i] & inputsV[i];
				j |= ~inputsC[i] & ~inputsV[i];
			}
			TinputsC[1] = -1L; TinputsV[1] = 0L;
			TinputsC[1] &= ~j; TinputsV[1] &= ~j;			
			TinputsC[1] |= k;  TinputsV[1] |= k;			
			TinputsC[1] &= ~l; TinputsV[1] |= l;
			l = 0L; k = 0L; j = 0L;
			TinputsC[2] = inputsC[4];
			TinputsV[2] = inputsV[4];
			for (int i = 0; i < 3; i++){
				l |= ~TinputsC[i] & TinputsV[i];
				k |= TinputsC[i] & ~TinputsV[i];
				j |= ~TinputsC[i] & ~TinputsV[i];
			}
			cv[0] = -1L; cv[1] = -1L;
			cv[0] &= ~j; cv[1] &= ~j;			
			cv[0] |= k;  cv[1] &= ~k;			
			cv[0] &= ~l; cv[1] |= l;
			return cv;
		case TYPE_OA222:
			for (int i = 0; i < 2; i++){
				l |= ~inputsC[i] & inputsV[i];
				k |= inputsC[i] & inputsV[i];
				j |= ~inputsC[i] & ~inputsV[i];
			}
			TinputsC[0] = -1L; TinputsV[0] = 0L;
			TinputsC[0] &= ~j; TinputsV[0] &= ~j;			
			TinputsC[0] |= k;  TinputsV[0] |= k;			
			TinputsC[0] &= ~l; TinputsV[0] |= l;
			l = 0L; k = 0L; j = 0L;
			for (int i = 2; i < 4; i++){
				l |= ~inputsC[i] & inputsV[i];
				k |= inputsC[i] & inputsV[i];
				j |= ~inputsC[i] & ~inputsV[i];
			}
			TinputsC[1] = -1L; TinputsV[1] = 0L;
			TinputsC[1] &= ~j; TinputsV[1] &= ~j;			
			TinputsC[1] |= k;  TinputsV[1] |= k;			
			TinputsC[1] &= ~l; TinputsV[1] |= l;
			l = 0L; k = 0L; j = 0L;
			for (int i = 4; i < 6; i++){
				l |= ~inputsC[i] & inputsV[i];
				k |= inputsC[i] & inputsV[i];
				j |= ~inputsC[i] & ~inputsV[i];
			}
			TinputsC[2] = -1L; TinputsV[2] = 0L;
			TinputsC[2] &= ~j; TinputsV[2] &= ~j;			
			TinputsC[2] |= k;  TinputsV[2] |= k;			
			TinputsC[2] &= ~l; TinputsV[2] |= l;
			l = 0L; k = 0L; j = 0L;
			for (int i = 0; i < 3; i++){
				l |= ~TinputsC[i] & TinputsV[i];
				k |= TinputsC[i] & ~TinputsV[i];
				j |= ~TinputsC[i] & ~TinputsV[i];
			}
			cv[0] = -1L; cv[1] = -1L;
			cv[0] &= ~j; cv[1] &= ~j;			
			cv[0] |= k;  cv[1] &= ~k;			
			cv[0] &= ~l; cv[1] |= l;
			return cv;
		case TYPE_OA22:
			for (int i = 0; i < 2; i++){
				l |= ~inputsC[i] & inputsV[i];
				k |= inputsC[i] & inputsV[i];
				j |= ~inputsC[i] & ~inputsV[i];
			}
			TinputsC[0] = -1L; TinputsV[0] = 0L;
			TinputsC[0] &= ~j; TinputsV[0] &= ~j;			
			TinputsC[0] |= k;  TinputsV[0] |= k;			
			TinputsC[0] &= ~l; TinputsV[0] |= l;
			l = 0L; k = 0L; j = 0L;
			for (int i = 2; i < 4; i++){
				l |= ~inputsC[i] & inputsV[i];
				k |= inputsC[i] & inputsV[i];
				j |= ~inputsC[i] & ~inputsV[i];
			}
			TinputsC[1] = -1L; TinputsV[1] = 0L;
			TinputsC[1] &= ~j; TinputsV[1] &= ~j;			
			TinputsC[1] |= k;  TinputsV[1] |= k;			
			TinputsC[1] &= ~l; TinputsV[1] |= l;
			l = 0L; k = 0L; j = 0L;
			for (int i = 0; i < 2; i++){
				l |= ~TinputsC[i] & TinputsV[i];
				k |= TinputsC[i] & ~TinputsV[i];
				j |= ~TinputsC[i] & ~TinputsV[i];
			}
			cv[0] = -1L; cv[1] = -1L;
			cv[0] &= ~j; cv[1] &= ~j;			
			cv[0] |= k;  cv[1] &= ~k;			
			cv[0] &= ~l; cv[1] |= l;
			return cv;
		case TYPE_OAI21:
			for (int i = 0; i < 2; i++){
				l |= ~inputsC[i] & inputsV[i];
				k |= inputsC[i] & inputsV[i];
				j |= ~inputsC[i] & ~inputsV[i];
			}
			TinputsC[0] = -1L; TinputsV[0] = 0L;
			TinputsC[0] &= ~j; TinputsV[0] &= ~j;			
			TinputsC[0] |= k;  TinputsV[0] |= k;			
			TinputsC[0] &= ~l; TinputsV[0] |= l;
			l = 0L; k = 0L; j = 0L;
			TinputsC[1] = inputsC[2];
			TinputsV[1] = inputsV[2];
			for (int i = 0; i < 2; i++){
				l |= ~TinputsC[i] & TinputsV[i];
				k |= TinputsC[i] & ~TinputsV[i];
				j |= ~TinputsC[i] & ~TinputsV[i];
			}
			cv[0] = -1L; cv[1] = -1L;
			cv[0] &= ~j; cv[1] &= ~j;			
			cv[0] |= k;  cv[1] &= ~k;			
			cv[0] &= ~l; cv[1] |= l;
			cv[1] ^= cv[0]; 
			return cv;
		case TYPE_OAI221:
			for (int i = 0; i < 2; i++){
				l |= ~inputsC[i] & inputsV[i];
				k |= inputsC[i] & inputsV[i];
				j |= ~inputsC[i] & ~inputsV[i];
			}
			TinputsC[0] = -1L; TinputsV[0] = 0L;
			TinputsC[0] &= ~j; TinputsV[0] &= ~j;			
			TinputsC[0] |= k;  TinputsV[0] |= k;			
			TinputsC[0] &= ~l; TinputsV[0] |= l;
			l = 0L; k = 0L; j = 0L;
			for (int i = 2; i < 4; i++){
				l |= ~inputsC[i] & inputsV[i];
				k |= inputsC[i] & inputsV[i];
				j |= ~inputsC[i] & ~inputsV[i];
			}
			TinputsC[1] = -1L; TinputsV[1] = 0L;
			TinputsC[1] &= ~j; TinputsV[1] &= ~j;			
			TinputsC[1] |= k;  TinputsV[1] |= k;			
			TinputsC[1] &= ~l; TinputsV[1] |= l;
			l = 0L; k = 0L; j = 0L;
			TinputsC[2] = inputsC[4];
			TinputsV[2] = inputsV[4];
			for (int i = 0; i < 3; i++){
				l |= ~TinputsC[i] & TinputsV[i];
				k |= TinputsC[i] & ~TinputsV[i];
				j |= ~TinputsC[i] & ~TinputsV[i];
			}
			cv[0] = -1L; cv[1] = -1L;
			cv[0] &= ~j; cv[1] &= ~j;			
			cv[0] |= k;  cv[1] &= ~k;			
			cv[0] &= ~l; cv[1] |= l;
			cv[1] ^= cv[0];
			return cv;
		case TYPE_OAI222:
			for (int i = 0; i < 2; i++){
				l |= ~inputsC[i] & inputsV[i];
				k |= inputsC[i] & inputsV[i];
				j |= ~inputsC[i] & ~inputsV[i];
			}
			TinputsC[0] = -1L; TinputsV[0] = 0L;
			TinputsC[0] &= ~j; TinputsV[0] &= ~j;			
			TinputsC[0] |= k;  TinputsV[0] |= k;			
			TinputsC[0] &= ~l; TinputsV[0] |= l;
			l = 0L; k = 0L; j = 0L;
			for (int i = 2; i < 4; i++){
				l |= ~inputsC[i] & inputsV[i];
				k |= inputsC[i] & inputsV[i];
				j |= ~inputsC[i] & ~inputsV[i];
			}
			TinputsC[1] = -1L; TinputsV[1] = 0L;
			TinputsC[1] &= ~j; TinputsV[1] &= ~j;			
			TinputsC[1] |= k;  TinputsV[1] |= k;			
			TinputsC[1] &= ~l; TinputsV[1] |= l;
			l = 0L; k = 0L; j = 0L;
			for (int i = 4; i < 6; i++){
				l |= ~inputsC[i] & inputsV[i];
				k |= inputsC[i] & inputsV[i];
				j |= ~inputsC[i] & ~inputsV[i];
			}
			TinputsC[2] = -1L; TinputsV[2] = 0L;
			TinputsC[2] &= ~j; TinputsV[2] &= ~j;			
			TinputsC[2] |= k;  TinputsV[2] |= k;			
			TinputsC[2] &= ~l; TinputsV[2] |= l;
			l = 0L; k = 0L; j = 0L;
			for (int i = 0; i < 3; i++){
				l |= ~TinputsC[i] & TinputsV[i];
				k |= TinputsC[i] & ~TinputsV[i];
				j |= ~TinputsC[i] & ~TinputsV[i];
			}
			cv[0] = -1L; cv[1] = -1L;
			cv[0] &= ~j; cv[1] &= ~j;			
			cv[0] |= k;  cv[1] &= ~k;			
			cv[0] &= ~l; cv[1] |= l;
			cv[1] ^= cv[0]; 
			return cv;
		case TYPE_OAI22:
			for (int i = 0; i < 2; i++){
				l |= ~inputsC[i] & inputsV[i];
				k |= inputsC[i] & inputsV[i];
				j |= ~inputsC[i] & ~inputsV[i];
			}
			TinputsC[0] = -1L; TinputsV[0] = 0L;
			TinputsC[0] &= ~j; TinputsV[0] &= ~j;			
			TinputsC[0] |= k;  TinputsV[0] |= k;			
			TinputsC[0] &= ~l; TinputsV[0] |= l;
			l = 0L; k = 0L; j = 0L;
			for (int i = 2; i < 4; i++){
				l |= ~inputsC[i] & inputsV[i];
				k |= inputsC[i] & inputsV[i];
				j |= ~inputsC[i] & ~inputsV[i];
			}
			TinputsC[1] = -1L; TinputsV[1] = 0L;
			TinputsC[1] &= ~j; TinputsV[1] &= ~j;			
			TinputsC[1] |= k;  TinputsV[1] |= k;			
			TinputsC[1] &= ~l; TinputsV[1] |= l;
			l = 0L; k = 0L; j = 0L;
			for (int i = 0; i < 2; i++){
				l |= ~TinputsC[i] & TinputsV[i];
				k |= TinputsC[i] & ~TinputsV[i];
				j |= ~TinputsC[i] & ~TinputsV[i];
			}
			cv[0] = -1L; cv[1] = -1L;
			cv[0] &= ~j; cv[1] &= ~j;			
			cv[0] |= k;  cv[1] &= ~k;			
			cv[0] &= ~l; cv[1] |= l;
			cv[1] ^= cv[0]; 
			return cv;
		case TYPE_MUX21:
			for(int i = 0; i < numInputs; i++)
				l |= ~inputsC[i] & inputsV[i];
			k |= (inputsC[2] & ~inputsV[2] & inputsC[0] & ~inputsV[0]) |
					(inputsC[2] & inputsV[2] & inputsC[1] & ~inputsV[1]) | 
					(inputsC[0] & ~inputsV[0] & inputsC[1] & ~inputsV[1]);
			
			j |= (inputsC[2] & ~inputsV[2] & inputsC[0] & inputsV[0]) | 
					(inputsC[2] & inputsV[2] & inputsC[1] & inputsV[1]);
/*			j |= (inputsC[2] & ~inputsV[2] & inputsC[0] & inputsV[0]) | 
					(inputsC[2] & inputsV[2] & inputsC[1] & inputsV[1]) |
					(inputsC[0] & inputsV[0] & inputsC[1] & inputsV[1]);*/
			//appropriate implement
			
			cv[0] = 0L; cv[1] = 0L;
			cv[0] |= j; cv[1] |= j;
			cv[0] |= k; cv[1] &= ~k;
			cv[0] &= ~l; cv[1] |= l;
			return cv;
		case TYPE_MUX41:
			for(int i = 0; i < numInputs; i++)
				l |= ~inputsC[i] & inputsV[i];			
			k |= (inputsC[4] & ~inputsV[4] & inputsC[5] & ~inputsV[5] & inputsC[0] & ~inputsV[0]) |
					(inputsC[4] & ~inputsV[4] & inputsC[5] & inputsV[5] & inputsC[1] & ~inputsV[1]) |
					(inputsC[4] & inputsV[4] & inputsC[5] & ~inputsV[5] & inputsC[2] & ~inputsV[2]) |
					(inputsC[4] & inputsV[4] & inputsC[5] & inputsV[5] & inputsC[3] & ~inputsV[3]) |
					(inputsC[0] & ~inputsV[0] & inputsC[1] & ~inputsV[1] & inputsC[4] & ~inputsV[4]) |
					(inputsC[2] & ~inputsV[2] & inputsC[3] & ~inputsV[3] & inputsC[4] & inputsV[4]) |
					(inputsC[0] & ~inputsV[0] & inputsC[2] & ~inputsV[2] & inputsC[5] & ~inputsV[5]) |
					(inputsC[1] & ~inputsV[1] & inputsC[3] & ~inputsV[3] & inputsC[5] & inputsV[5]) |
					(inputsC[0] & ~inputsV[0] & inputsC[1] & ~inputsV[1] & inputsC[2] & ~inputsV[2] & inputsC[3] & ~inputsV[3]);
			j |= (inputsC[4] & ~inputsV[4] & inputsC[5] & ~inputsV[5] & inputsC[0] & inputsV[0]) |
					(inputsC[4] & ~inputsV[4] & inputsC[5] & inputsV[5] & inputsC[1] & inputsV[1]) |
					(inputsC[4] & inputsV[4] & inputsC[5] & ~inputsV[5] & inputsC[2] & inputsV[2]) |
					(inputsC[4] & inputsV[4] & inputsC[5] & inputsV[5] & inputsC[3] & inputsV[3]) |
					(inputsC[0] & inputsV[0] & inputsC[1] & inputsV[1] & inputsC[4] & ~inputsV[4]) |
					(inputsC[2] & inputsV[2] & inputsC[3] & inputsV[3] & inputsC[4] & inputsV[4]) |
					(inputsC[0] & inputsV[0] & inputsC[2] & inputsV[2] & inputsC[5] & ~inputsV[5]) |
					(inputsC[1] & inputsV[1] & inputsC[3] & inputsV[3] & inputsC[5] & inputsV[5]) |
					(inputsC[0] & inputsV[0] & inputsC[1] & inputsV[1] & inputsC[2] & inputsV[2] & inputsC[3] & inputsV[3]);
			cv[0] = 0L; cv[1] = 0L;
			cv[0] |= j; cv[1] |= j;
			cv[0] |= k; cv[1] &= ~k;
			cv[0] &= ~l; cv[1] |= l;
			return cv;
		case TYPE_FADD_CO:
			for(int i = 0; i < numInputs; i++)
				l |= ~inputsC[i] & inputsV[i];
			k |= (inputsC[0] & ~inputsV[0] & inputsC[1] & ~inputsV[1]) | 
					(inputsC[1] & ~inputsV[1] & inputsC[2] & ~inputsV[2]) | 
					(inputsC[0] & ~inputsV[0] & inputsC[2] & ~inputsV[2]);

			j |= (inputsC[0] & inputsV[0] & inputsC[1] & inputsV[1]) | 
					(inputsC[1] & inputsV[1] & inputsC[2] & inputsV[2] & inputsC[0] & ~inputsV[0]) | 
					(inputsC[0] & inputsV[0] & inputsC[2] & inputsV[2] & inputsC[1] & ~inputsV[1]);
/*			j |= (inputsC[0] & inputsV[0] & inputsC[1] & inputsV[1]) | 
					(inputsC[1] & inputsV[1] & inputsC[2] & inputsV[2]) | 
					(inputsC[0] & inputsV[0] & inputsC[2] & inputsV[2]);*/
			//appropriate implement

			cv[0] = 0L; cv[1] = 0L;
			cv[0] |= j; cv[1] |= j;
			cv[0] |= k; cv[1] &= ~k;
			cv[0] &= ~l; cv[1] |= l;
			return cv;
		}
		return super.evaluate(type, inputsV, inputsC, numInputs);
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
