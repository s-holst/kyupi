/*
 * Copyright 2013 The KyuPI project contributors. See the COPYRIGHT.md file
 * at the top-level directory of this distribution.
 * This file is part of the KyuPI project. It is subject to the license terms
 * in the LICENSE.md file found in the top-level directory of this distribution.
 * No part of the KyuPI project, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.md file.
 */
package org.kyupi.circuit;

import java.util.HashMap;

public class LibraryNangate extends Library {

	// re-use Library.TYPE_ where possible.
	public static final int TYPE_AOI22 = 0x17;
	public static final int TYPE_OAI21 = 0x24;

	String[][] inputPortNameSets = { { "A" }, { "A1", "A2", "A3", "A4" }, { "A1", "A2", "B1", "B2" }, { "A", "B1", "B2" } };
	String[][] outputPortNameSets = { { "Z" }, { "ZN" } };

	private static final int MASK_INTFSPEC = 0xffff;

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
		InterfaceSpec is = interfaceSpec.get(type & MASK_INTFSPEC);
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

	private long tmpInC[] = new long[2];
	private long tmpInV[] = new long[2];

	public void propagate(int type, long[] inV, long[] inC, int inOffset, int inCount, long[] outV, long[] outC,
			int outOffset, int outCount) {
		long j = 0L;
		long k = 0L;	
		long l = 0L;
		
		switch (type & MASK_INTFSPEC) {
		case TYPE_AOI22:
			for (int i = 0; i < 2; i++){
				l |= ~inC[inOffset+i] & inV[inOffset+i];
				k |= inC[inOffset+i] & ~inV[inOffset+i];
				j |= ~inC[inOffset+i] & ~inV[inOffset+i];
			}
			tmpInC[0] = -1L;
			tmpInV[0] = -1L;
			tmpInC[0] &= ~j;
			tmpInV[0] &= ~j;			
			tmpInC[0] |= k;
			tmpInV[0] &= ~k;			
			tmpInC[0] &= ~l;
			tmpInV[0] |= l;
			
			l = 0L;
			k = 0L;
			j = 0L;
			for (int i = 2; i < 4; i++){
				l |= ~inC[inOffset+i] & inV[inOffset+i];
				k |= inC[inOffset+i] & ~inV[inOffset+i];
				j |= ~inC[inOffset+i] & ~inV[inOffset+i];
			}
			tmpInC[1] = -1L;
			tmpInV[1] = -1L;
			tmpInC[1] &= ~j;
			tmpInV[1] &= ~j;			
			tmpInC[1] |= k;
			tmpInV[1] &= ~k;			
			tmpInC[1] &= ~l;
			tmpInV[1] |= l;
			
			l = 0L;
			k = 0L;
			j = 0L;	
			for (int i = 0; i < 2; i++){
				l |= ~tmpInC[i] & tmpInV[i];
				k |= tmpInC[i] & tmpInV[i];
				j |= ~tmpInC[i] & ~tmpInV[i];
			}
			outC[outOffset] = -1L;
			outV[outOffset] =  0L;
			outC[outOffset] &= ~j;
			outV[outOffset] &= ~j;			
			outC[outOffset] |= k;
			outV[outOffset] |= k;			
			outC[outOffset] &= ~l;
			outV[outOffset] |= l;
			
			outV[outOffset] ^= outC[outOffset]; 
			return;
		case TYPE_OAI21:
			tmpInC[0] = inC[inOffset];
			tmpInV[0] = inV[inOffset];
			
			for (int i = 1; i < 3; i++){
				l |= ~inC[inOffset+i] & inV[inOffset+i];
				k |= inC[inOffset+i] & inV[inOffset+i];
				j |= ~inC[inOffset+i] & ~inV[inOffset+i];
			}
			tmpInC[1] = -1L;
			tmpInV[1] = 0L;
			tmpInC[1] &= ~j;
			tmpInV[1] &= ~j;			
			tmpInC[1] |= k;
			tmpInV[1] |= k;			
			tmpInC[1] &= ~l;
			tmpInV[1] |= l;

			l = 0L;
			k = 0L;
			j = 0L;
			for (int i = 0; i < 2; i++){
				l |= ~tmpInC[i] & tmpInV[i];
				k |= tmpInC[i] & ~tmpInV[i];
				j |= ~tmpInC[i] & ~tmpInV[i];
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
		}
		super.propagate(type, inV, inC, inOffset, inCount, outV, outC, outOffset, outCount);
	}
	
	public String toString() {
		return "Nangate";
	}
}
