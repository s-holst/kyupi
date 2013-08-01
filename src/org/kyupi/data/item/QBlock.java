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
 * A set of 64 vectors of 4-valued elements stored in a bit-parallel fashion.
 * 
 * @author stefan
 * 
 */
public class QBlock extends DataItem<QBlock> {

	private long value[];

	private long care[];

	public QBlock(int length) {
		this.length = length;
		this.slots = 64;
		value = new long[length];
		care = new long[length];
	}

	public QBlock(BBlock value_data, BBlock care_data) {
		this(Math.max(value_data.length(), care_data.length()));
		for (int i = value_data.length() - 1; i >= 0; i--) {
			value[i] = value_data.get(i);
		}
		for (int i = care_data.length() - 1; i >= 0; i--) {
			care[i] = care_data.get(i);
		}
	}

	public void setValue(int slot, int position, char vc) {
		long mask = 1L << slot;
		switch (vc) {
		case QVector.CHAR_0:
			value[position] &= ~mask;
			care[position] |= mask;
			break;
		case QVector.CHAR_1:
			value[position] |= mask;
			care[position] |= mask;
			break;
		case QVector.CHAR_X:
			value[position] |= mask;
			care[position] &= ~mask;
			break;
		case QVector.CHAR_DC:
			value[position] &= ~mask;
			care[position] &= ~mask;
			break;
		}
	}

	public char getValue(int slot, int position) {
		long mask = 1L << slot;
		boolean val_is_one = (value[position] & mask) != 0L;
		if ((care[position] & mask) != 0L) {
			if (val_is_one)
				return QVector.CHAR_1;
			else
				return QVector.CHAR_0;
		} else {
			if (val_is_one)
				return QVector.CHAR_X;
			else
				return QVector.CHAR_DC;
		}
	}

	public void set(int position, long v, long c) {
		setV(position, v);
		setC(position, c);
	}

	public void setV(int position, long v) {
		value[position] = v;
	}

	public void setC(int position, long c) {
		care[position] = c;
	}

	public long getV(int position) {
		return value[position];
	}

	public long getC(int position) {
		return care[position];
	}

	public boolean equals(Object o) {
		if (!(o instanceof QBlock))
			return false;
		QBlock b = (QBlock) o;
		if (b.length != length)
			return false;
		for (int i = 0; i < length; i++) {
			if (b.value[i] != value[i])
				return false;
			if (b.care[i] != care[i])
				return false;
		}
		return true;
	}

	@Override
	public void copyTo(int slot, BVector dest) {
		int l = Math.min(dest.length(), length);
		long setmask = 1L << slot;
		BitSet bs = dest.access();
		for (int i = 0; i < l; i++) {
			if ((care[i] & setmask) != 0L)
				bs.set(i, (value[i] & setmask) != 0L);
		}
	}

	@Override
	public void copyTo(int slot, QVector dest) {
		int l = Math.min(dest.length(), length);
		long setmask = 1L << slot;
		for (int i = 0; i < l; i++) {
			dest.accessV().set(i, (value[i] & setmask) != 0L);
			dest.accessC().set(i, (care[i] & setmask) != 0L);
		}
	}

	@Override
	public void copyTo(long mask, BBlock dest) {
		int l = Math.min(length, dest.length);
		for (int i = 0; i < l; i++) {
			dest.set(i, (dest.get(i) & ~(care[i] & mask)) | (value[i] & care[i] & mask));
		}
	}

	@Override
	public void copyTo(long mask, QBlock dest) {
		int l = Math.min(length, dest.length);
		for (int i = 0; i < l; i++) {
			dest.setV(i, (dest.getV(i) & ~mask) | (value[i] & mask));
			dest.setC(i, (dest.getC(i) & ~mask) | (care[i] & mask));
		}
	}

	public String toString(int slot) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < value.length; i++) {
			buf.append(getValue(slot, i));
		}
		return buf.toString();
	}

	@Override
	protected void pool_free_this() {
		pool.free(this);
	}
}
