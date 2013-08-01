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

public class LibraryNangate extends Library {

	// re-use Library.TYPE_ where possible.
	// new type ids: 0x10, 0x20 ... 0xff0
	public static final int TYPE_AOI22 = 0x10;
	public static final int TYPE_OAI21 = 0x20;

	String[][] inputPortNameSets = { { "A" }, { "A1", "A2", "A3", "A4" }, { "A1", "A2", "B1", "B2" }, { "A", "B1", "B2" } };
	String[][] outputPortNameSets = { { "Z" }, { "ZN" } };

	private static final int INTERFACE_SPEC_MASK = 0xfff | INPUTS_MASK;

	private HashMap<Integer, InterfaceSpec> interfaceSpec = new HashMap<Integer, InterfaceSpec>() {
		private static final long serialVersionUID = 1L;
		{
			put(TYPE_BUF, new InterfaceSpec("BUF", inputPortNameSets[0], 1, outputPortNameSets[0], 1));
			put(TYPE_NOT, new InterfaceSpec("INV", inputPortNameSets[0], 1, outputPortNameSets[1], 1));
			put(TYPE_NAND | INPUTS_2, new InterfaceSpec("NAND2", inputPortNameSets[1], 2, outputPortNameSets[1], 1));
			put(TYPE_AOI22, new InterfaceSpec("AOI22", inputPortNameSets[2], 4, outputPortNameSets[1], 1));
			put(TYPE_OAI21, new InterfaceSpec("OAI21", inputPortNameSets[3], 3, outputPortNameSets[1], 1));
		}
	};

	private HashMap<String, Integer> typeNames = new HashMap<String, Integer>() {
		private static final long serialVersionUID = 1L;
		{
			put("NAND2_X1", TYPE_NAND | INPUTS_2 | STRENGTH_1);
			put("AOI22_X1", TYPE_AOI22 | STRENGTH_1);
			put("OAI21_X1", TYPE_OAI21 | STRENGTH_1);
			put("INV_X1", TYPE_NOT | STRENGTH_1);
		}
	};

	public int pinDirection(int type, String name) {
		if (name.startsWith("Z"))
			return DIR_OUT;
		if (name.startsWith("A"))
			return DIR_IN;
		if (name.startsWith("B"))
			return DIR_IN;
		return -1;
	}

	protected InterfaceSpec getInterfaceSpec(int type) {
		InterfaceSpec is = interfaceSpec.get(type & INTERFACE_SPEC_MASK);
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
		case STRENGTH_1:
			suffix = "_X1";
			break;
		case STRENGTH_2:
			suffix = "_X2";
			break;
		case STRENGTH_4:
			suffix = "_X4";
		}
		return suffix;
	}

	public long evaluate(int type, long[] inputs, int numInputs) {
		//long l = 0L;
		switch (type & INTERFACE_SPEC_MASK) {
		case TYPE_AOI22:
			return ~((inputs[0] & inputs[1]) | (inputs[2] & inputs[3]));
		case TYPE_OAI21:
			return ~(inputs[0] & (inputs[1] | inputs[2]));
		}
		return super.evaluate(type, inputs, numInputs);
	}
}
