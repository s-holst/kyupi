/*
 * Copyright 2013 The KyuPI project contributors. See the COPYRIGHT.md file
 * at the top-level directory of this distribution.
 * This file is part of the KyuPI project. It is subject to the license terms
 * in the LICENSE.md file found in the top-level directory of this distribution.
 * No part of the KyuPI project, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.md file.
 */
package org.kyupi.data.item;

import java.util.BitSet;

import org.kyupi.misc.StringTools;

/**
 * A set of 64 binary vectors stored in a bit-parallel fashion.
 * 
 * @author stefan
 * 
 */
public class BBlock extends DataItem<BBlock> {

	private long value[];

	public BBlock(int length) {
		this.length = length;
		this.slots = 64;
		value = new long[length];
	}

	public BBlock(long... ls) {
		this(ls.length);
		System.arraycopy(ls, 0, value, 0, length);
	}

	public void setValue(int slot, int position, char v) {
		long mask = 1L << slot;
		if (v == BVector.CHAR_1) {
			value[position] |= mask;
		} else {
			value[position] &= ~mask;
		}
	}

	public char getValue(int slot, int position) {
		long mask = 1L << slot;
		return ((value[position] & mask) == 0L) ? BVector.CHAR_0 : BVector.CHAR_1;
	}

	public void set(int position, long v) {
		value[position] = v;
	}

	public long get(int position) {
		return value[position];
	}

	public boolean equals(Object o) {
		if (!(o instanceof BBlock))
			return false;
		BBlock b = (BBlock) o;
		if (b.value.length != value.length)
			return false;
		for (int i = 0; i < value.length; i++)
			if (b.value[i] != value[i])
				return false;
		return true;
	}

	@Override
	public void copyTo(int slot, BVector dest) {
		int l = Math.min(dest.length(), length);
		long setmask = 1L << slot;
		BitSet bs = dest.access();
		for (int i = 0; i < l; i++) {
			bs.set(i, (value[i] & setmask) != 0L);
		}
	}

	@Override
	public void copyTo(int slot, QVector dest) {
		int l = Math.min(dest.length(), length);
		long setmask = 1L << slot;
		for (int i = 0; i < l; i++) {
			dest.accessV().set(i, (value[i] & setmask) != 0L);
		}
		dest.accessC().set(0, l);
	}

	@Override
	public void copyTo(long mask, BBlock dest) {
		int l = Math.min(length, dest.length);
		for (int i = 0; i < l; i++) {
			dest.value[i] |= value[i] & mask;
			dest.value[i] &= ~(~value[i] & mask);
		}
	}

	@Override
	public void copyTo(long mask, QBlock dest) {
		int l = Math.min(length, dest.length);
		for (int i = 0; i < l; i++) {
			dest.setV(i, (dest.getV(i) & ~mask) | (value[i] & mask));
			dest.setC(i, dest.getC(i) | mask);
		}
	}

	public String toString(int slot) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < value.length; i++) {
			buf.append(getValue(slot, i));
		}
		return buf.toString();
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < value.length; i++) {
			buf.append(String.format("[%02d] ", i) + StringTools.longToReadableBinaryString(value[i]) + "\n");
		}
		return "slots 63 .... 0\n" + buf.toString();
	}

	// TODO: implement

	// public void setTransition(int slot, int position, char vv) {
	// }
	//
	// public char getTransition(int slot, int position) {
	// return '0';
	// }

	@Override
	protected void pool_free_this() {
		pool.free(this);
	}
}
