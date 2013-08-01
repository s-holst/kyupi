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

import junit.framework.TestCase;

import org.junit.Test;

public class QVectorTest extends TestCase {

	@Test
	public void test_int_length() {
		QVector vector = new QVector(10);
		assertEquals(10, vector.length());
		vector = new QVector("01X--");
		assertEquals(5, vector.length());
	}
	
	@Test
	public void test_int_slots() {
		QVector vector = new QVector(10);
		assertEquals(1, vector.slots());
		vector = new QVector("01X-");
		assertEquals(1, vector.slots());
	}

	@Test
	public void test_void_setValue_int_char() {
		QVector vector = new QVector("0000");
		vector.setValue(1, '1');
		vector.setValue(2, 'X');
		vector.setValue(3, '-');
		assertEquals("01X-", vector.toString());
	}

	@Test
	public void test_char_getValue_int() {
		QVector vector = new QVector("01X-");
		assertEquals('0', vector.getValue(0));
		assertEquals('1', vector.getValue(1));
		assertEquals('X', vector.getValue(2));
		assertEquals('-', vector.getValue(3));
	}

	@Test
	public void test_void_setString_String() {
		QVector vector = new QVector(4);
		assertEquals("----", vector.toString());
		vector.setString("01X-");
		assertEquals("01X-", vector.toString());
	}

	@Test
	public void test_boolean_equals_Object() {
		QVector vector1 = new QVector("01X-");
		QVector vector2 = new QVector("01X-");
		QVector vector3 = new QVector("01-X");
		assertTrue(vector1.equals(vector2));
		assertTrue(vector2.equals(vector1));
		assertFalse(vector1.equals(vector3));
		assertFalse(vector2.equals(vector3));
		assertFalse(vector3.equals(vector1));
		assertFalse(vector3.equals(vector2));
	}

	@Test
	public void test_void_copyTo_int_BVector() {
		QVector vector1 = new QVector("01XX--");
		BVector vector2 = new BVector("101010");
		vector1.copyTo(0, vector2);
		assertEquals("011010", vector2.toString());
	}

	@Test
	public void test_void_copyTo_int_QVector() {
		QVector vector1 = new QVector("01X-");
		QVector vector2 = new QVector(4);
		vector1.copyTo(0, vector2);
		assertEquals("01X-", vector2.toString());
	}

	@Test
	public void test_void_copyTo_long_BBlock() {
		QVector vector = new QVector("01XX--");
		BBlock block = new BBlock(0b11L, 0L, 0b11L, 0L, 0b11L, 0L);
		vector.copyTo(0b110L, block);
		assertEquals("101010", block.toString(0));
		assertEquals("011010", block.toString(1));
		assertEquals("010000", block.toString(2));
	}

	@Test
	public void test_void_copyTo_long_QBlock() {
		QVector vector = new QVector("01X-");
		QBlock block = new QBlock(4);
		vector.copyTo(0xffL, block);
		assertEquals("----", block.toString(8));
		assertEquals("01X-", block.toString(7));
	}
}
