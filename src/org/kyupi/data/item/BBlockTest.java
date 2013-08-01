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

public class BBlockTest extends TestCase {

	//protected static Logger log = Logger.getLogger(BBlockTest.class);

	@Test
	public void test_int_length() {
		BBlock block = new BBlock(10);
		assertEquals(10, block.length());
		block = new BBlock(0L, 0L, 0L);
		assertEquals(3, block.length());
	}
	
	@Test
	public void test_int_slots() {
		BBlock block = new BBlock(10);
		assertEquals(64, block.slots());
		block = new BBlock(0L, 0L, 0L);
		assertEquals(64, block.slots());
	}
	
	@Test
	public void test_void_setValue_int_int_char() {
		BBlock block = new BBlock(3);
		block.setValue(0, 0, '1');
		block.setValue(1, 2, '1');
		assertEquals("100", block.toString(0));
		assertEquals("001", block.toString(1));
	}

	@Test
	public void test_char_getValue_int_int() {
		BBlock block = new BBlock(2);
		block.set(0, 0x3L);
		assertEquals('1', block.getValue(1, 0));
		assertEquals('0', block.getValue(2, 0));
		assertEquals('0', block.getValue(1, 1));
	}

	@Test
	public void test_void_set_int_long() {
		BBlock block = new BBlock(2);
		block.set(0, 0x3L);
		block.set(1, 0x1L);
		assertEquals("11", block.toString(0));
		assertEquals("10", block.toString(1));
	}

	@Test
	public void test_long_get_int() {
		BBlock block = new BBlock(3);
		block.setValue(0, 0, '1');
		block.setValue(1, 2, '1');
		assertEquals(1L, block.get(0));
		assertEquals(0L, block.get(1));
		assertEquals(2L, block.get(2));
	}

	@Test
	public void test_boolean_equals_Object() {
		BBlock block1 = new BBlock(1L, 0L);
		BBlock block2 = new BBlock(1L, 0L);
		BBlock block3 = new BBlock(-1L, 0L);
		assertTrue(block1.equals(block2));
		assertTrue(block2.equals(block1));
		assertFalse(block1.equals(block3));
		assertFalse(block2.equals(block3));
		assertFalse(block3.equals(block1));
		assertFalse(block3.equals(block2));
	}

	@Test
	public void test_void_copyTo_int_BVector() {
		BBlock block = new BBlock(1L, 0L, 0x40000000L);
		BVector vector = new BVector(3);
		block.copyTo(0, vector);
		assertEquals("100", vector.toString());
		block.copyTo(30, vector);
		assertEquals("001", vector.toString());
		block.copyTo(60, vector);
		assertEquals("000", vector.toString());
	}

	@Test
	public void test_void_copyTo_int_QVector() {
		BBlock block = new BBlock(1L, 0L, 0x40000000L);
		QVector vector = new QVector(3);
		block.copyTo(0, vector);
		assertEquals("100", vector.toString());
		block.copyTo(30, vector);
		assertEquals("001", vector.toString());
		block.copyTo(60, vector);
		assertEquals("000", vector.toString());
	}

	@Test
	public void test_void_copyTo_long_BBlock() {
		BBlock block1 = new BBlock(0b10101010L, 0b11001100L);
		BBlock block2 = new BBlock(2);
		block1.copyTo(0xf0L, block2);
		assertEquals(0b10100000L, block2.get(0));
		assertEquals(0b11000000L, block2.get(1));
		assertEquals("11", block2.toString(7));
		assertEquals("00", block2.toString(3));
	}

	@Test
	public void test_void_copyTo_long_QBlock() {
		BBlock block1 = new BBlock(0b10101010L, 0b11001100L);
		QBlock block2 = new QBlock(2);
		block1.copyTo(0xf0L, block2);
		assertEquals(0b10100000L, block2.getV(0));
		assertEquals(0b11000000L, block2.getV(1));
		assertEquals(0b11110000L, block2.getC(0));
		assertEquals(0b11110000L, block2.getC(1));
		assertEquals("11", block2.toString(7));
		assertEquals("--", block2.toString(3));
	}
}
