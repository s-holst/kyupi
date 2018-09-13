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
 * A vector of four-valued elements.
 * 
 * Each element is encoded in two bits. Internally, two bit vectors v (value)
 * and c (care) are stored.
 * 
 */
public class QVector extends DataItem<QVector> {

	public static final char CHAR_DC = '-'; // cv = 00
	public static final char CHAR_X = 'X'; // cv = 01
	public static final char CHAR_0 = '0'; // cv = 10
	public static final char CHAR_1 = '1'; // cv = 11

	private BitSet value;

	private BitSet care;

	// needed for copyToBVector method, avoids object creations.
	private BitSet scratch;

	public QVector(int size) {
		this.length = size;
		this.slots = 1;
		value = new BitSet(size);
		care = new BitSet(size);
		scratch = new BitSet(size);
	}

	public QVector(String s) {
		this(s.length());
		setString(s);
	}

	public QVector(QVector v) {
		this(v.length());
		v.copyTo(0, this);
	}

	public void setValue(int position, char vc) {
		switch (vc) {
		case CHAR_0:
			value.clear(position);
			care.set(position);
			break;
		case CHAR_1:
			value.set(position);
			care.set(position);
			break;
		case CHAR_X:
			value.set(position);
			care.clear(position);
			break;
		case CHAR_DC:
			value.clear(position);
			care.clear(position);
			break;
		}
	}

	public char getValue(int position) {
		if (care.get(position)) {
			if (value.get(position))
				return CHAR_1;
			else
				return CHAR_0;
		} else {
			if (value.get(position))
				return CHAR_X;
			else
				return CHAR_DC;
		}
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
		if (o instanceof QVector) {
			QVector qv = (QVector) o;
			return qv.length == length && qv.value.equals(value) && qv.care.equals(care);
		}
		return false;
	}

	/**
	 * returns true iff given QVector is compatible at every position.
	 * 
	 * A vector position is compatible if their values are either equal or one
	 * of the values is "-".
	 * 
	 * @param o
	 * @return
	 */
	public boolean compatible(Object o) {
		if (o == null)
			return false;
		if (o instanceof QVector) {
			QVector qv = (QVector) o;
			BitSet dcm1 = (BitSet) value.clone();
			dcm1.or(care);
			BitSet mask = (BitSet) qv.value.clone();
			mask.or(qv.care);
			mask.and(dcm1);
			BitSet val = (BitSet) value.clone();
			val.and(mask);
			BitSet qval = (BitSet) qv.value.clone();
			qval.and(mask);
			return qv.length == length && val.equals(qval);
		}
		return false;
	}

	/**
	 * returns true iff given QVector has the same logic values.
	 * 
	 * Only positions where both vectors are "0" or "1" are compared. If one of
	 * the vectors are "-" or "X", the position is ignored.
	 * 
	 * @param o
	 * @return
	 */
	public boolean equalLogic(Object o) {
		if (o == null)
			return false;
		if (o instanceof QVector) {
			QVector qv = (QVector) o;
			BitSet mask = (BitSet) qv.care.clone();
			mask.and(care);
			BitSet val = (BitSet) value.clone();
			val.and(mask);
			BitSet qval = (BitSet) qv.value.clone();
			qval.and(mask);
			return qv.length == length && val.equals(qval);
		}
		return false;
	}

	@Override
	public void copyTo(int slot, BVector dest) {
		dest.access().andNot(care);
		scratch.clear();
		scratch.or(value);
		scratch.and(care);
		dest.access().or(scratch);
	}

	@Override
	public void copyTo(int slot, QVector dest) {
		dest.value.clear();
		dest.value.or(value);
		dest.care.clear();
		dest.care.or(care);
	}

	@Override
	public void copyTo(long mask, BBlock dest) {
		for (int i = 0; i < length; i++) {
			if (care.get(i)) {
				long l = dest.get(i);
				if (value.get(i)) {
					l |= mask;
				} else {
					l &= ~mask;
				}
				dest.set(i, l);
			}
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
			if (care.get(i)) {
				l |= mask;
			} else {
				l &= ~mask;
			}
			dest.setC(i, l);
		}
	}

	public void shuffleTo(int[] shuffle, QVector dest) {
		int l = Math.min(shuffle.length, dest.length);
		dest.value.clear();
		dest.care.clear();
		for (int i = 0; i < l; i++) {
			if (shuffle[i] >= 0 && shuffle[i] < length) {
				dest.value.set(i, value.get(shuffle[i]));
				dest.care.set(i, care.get(shuffle[i]));
			}
		}
	}
	
	public void compactTo(int[] compactor, QVector dest) {
		int l = Math.min(compactor.length, length);
		dest.value.clear();
		dest.care.clear();
		for (int i = 0; i < l; i++) {
			if (compactor[i] >= 0 && compactor[i] < dest.length) {
				if (value.get(i))
					dest.value.flip(compactor[i]);
				if (care.get(i))
					dest.care.flip(compactor[i]);
			}
		}
	}

	public QVector and(QVector vector) {
		value.and(vector.value);
		care.and(vector.care);
		return this;
	}

	public QVector or(QVector vector) {
		value.or(vector.value);
		care.or(vector.care);
		return this;
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

	BitSet accessV() {
		return value;
	}

	BitSet accessC() {
		return care;
	}
}
