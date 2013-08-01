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

public class BVectorTest extends TestCase {

	@Test
	public void test_int_length() {
		BVector vector = new BVector(10);
		assertEquals(10, vector.length());
		vector = new BVector("000");
		assertEquals(3, vector.length());
	}
	
	@Test
	public void test_int_slots() {
		BVector vector = new BVector(10);
		assertEquals(1, vector.slots());
		vector = new BVector("000");
		assertEquals(1, vector.slots());
	}

	@Test
	public void test_void_setValue_int_char() {
		BVector vector = new BVector("011100");
		vector.setValue(0, '1');
		vector.setValue(1, '0');
		vector.setValue(2, 'X');
		vector.setValue(3, '-');
		vector.setValue(4, 'X');
		vector.setValue(5, '-');
		assertEquals("101100", vector.toString());
	}

	@Test
	public void test_char_getValue_int() {
		BVector vector = new BVector("01");
		assertEquals('0', vector.getValue(0));
		assertEquals('1', vector.getValue(1));
	}

	@Test
	public void test_void_setString_String() {
		BVector vector = new BVector(2);
		assertEquals("00", vector.toString());
		vector.setString("01");
		assertEquals("01", vector.toString());
	}

	@Test
	public void test_boolean_equals_Object() {
		BVector vector1 = new BVector("01");
		BVector vector2 = new BVector("01");
		BVector vector3 = new BVector("10");
		assertTrue(vector1.equals(vector2));
		assertTrue(vector2.equals(vector1));
		assertFalse(vector1.equals(vector3));
		assertFalse(vector2.equals(vector3));
		assertFalse(vector3.equals(vector1));
		assertFalse(vector3.equals(vector2));
	}

	@Test
	public void test_void_copyTo_int_BVector() {
		BVector vector1 = new BVector("01");
		BVector vector2 = new BVector(2);
		vector1.copyTo(0, vector2);
		assertEquals("01", vector2.toString());
	}

	@Test
	public void test_void_copyTo_int_QVector() {
		BVector vector1 = new BVector("01");
		QVector vector2 = new QVector(2);
		vector1.copyTo(0, vector2);
		assertEquals("01", vector2.toString());
	}

	@Test
	public void test_void_copyTo_long_BBlock() {
		BVector vector = new BVector("01");
		BBlock block = new BBlock(2);
		vector.copyTo(0xffL, block);
		assertEquals("00", block.toString(8));
		assertEquals("01", block.toString(7));
	}

	@Test
	public void test_void_copyTo_long_QBlock() {
		BVector vector = new BVector("01");
		QBlock block = new QBlock(2);
		vector.copyTo(0xffL, block);
		assertEquals("--", block.toString(8));
		assertEquals("01", block.toString(7));
	}
}
