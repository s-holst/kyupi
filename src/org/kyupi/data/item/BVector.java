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

/**
 * A vector of binary values.
 * 
 */
public class BVector extends DataItem<BVector> {

	public static final char CHAR_0 = '0';
	public static final char CHAR_1 = '1';

	private BitSet value;

	public BVector(int length) {
		this.length = length;
		this.slots = 1;
		value = new BitSet(length);
	}

	public BVector(String s) {
		this(s.length());
		setString(s);
	}

	public void setValue(int position, char v) {
		switch(v) {
		case CHAR_0:
			value.clear(position);
			break;
		case CHAR_1:
			value.set(position);
		}
	}

	public char getValue(int position) {
		if (value.get(position))
			return CHAR_1;
		else
			return CHAR_0;
	}

	public void setString(String s) {
		int l = Math.min(length, s.length());
		for (int i = 0; i < l; i++) {
			setValue(i, s.charAt(i));
		}
	}

	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o instanceof BVector) {
			BVector bv = (BVector) o;
			return bv.length == length && bv.value.equals(value);
		}
		return false;
	}

	@Override
	public void copyTo(int slot, BVector dest) {
		dest.value.clear();
		dest.value.or(value);
	}

	@Override
	public void copyTo(int slot, QVector dest) {
		dest.accessV().clear();
		dest.accessV().or(value);
		dest.accessC().set(0, length);
	}

	@Override
	public void copyTo(long mask, BBlock dest) {
		for (int i = 0; i < length; i++) {
			long l = dest.get(i);
			if (value.get(i)) {
				l |= mask;
			} else {
				l &= ~mask;
			}
			dest.set(i, l);
		}
	}

	@Override
	public void copyTo(long mask, QBlock dest) {
		for (int i = 0; i < length; i++) {
			long l = dest.getV(i);
			if (value.get(i)) {
				l |= mask;
			} else {
				l &= ~mask;
			}
			dest.setV(i, l);
			l = dest.getC(i);
			l |= mask;
			dest.setC(i, l);
		}
	}

	public String toString() {
		StringBuffer buf = new StringBuffer(length);
		for (int i = 0; i < length; i++) {
			buf.append(getValue(i));
		}
		return buf.toString();
	}

	@Override
	protected void pool_free_this() {
		pool.free(this);
	}

	BitSet access() {
		return value;
	}
}
