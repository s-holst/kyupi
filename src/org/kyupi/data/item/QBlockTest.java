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

public class QBlockTest extends TestCase {

	//protected static Logger log = Logger.getLogger(BBlockTest.class);

	@Test
	public void test_int_length() {
		QBlock block = new QBlock(10);
		assertEquals(10, block.length());
		block = new QBlock(new BBlock(0L, 0L, 0L), new BBlock(0L, 0L, 0L));
		assertEquals(3, block.length());
	}
	
	@Test
	public void test_int_slots() {
		QBlock block = new QBlock(10);
		assertEquals(64, block.slots());
		block = new QBlock(new BBlock(0L, 0L, 0L), new BBlock(0L, 0L, 0L));
		assertEquals(64, block.slots());
	}
	
	@Test
	public void test_void_setValue_int_int_char() {
		QBlock block = new QBlock(3);
		block.setValue(0, 0, '1');
		block.setValue(0, 1, 'X');
		block.setValue(0, 2, '0');
		block.setValue(1, 2, '1');
		assertEquals("1X0", block.toString(0));
		assertEquals("--1", block.toString(1));
		assertEquals("---", block.toString(2));
	}

	@Test
	public void test_char_getValue_int_int() {
		QBlock block = new QBlock(2);
		block.setV(0, 0b1100L);
		block.setC(0, 0b1010L);
		assertEquals('-', block.getValue(0, 0));
		assertEquals('0', block.getValue(1, 0));
		assertEquals('X', block.getValue(2, 0));
		assertEquals('1', block.getValue(3, 0));
		assertEquals('-', block.getValue(3, 1));
	}

	@Test
	public void test_void_setV_int_long() {
		QBlock block = new QBlock(2);
		block.setV(0, 0x3L);
		block.setV(1, 0x1L);
		assertEquals("XX", block.toString(0));
		assertEquals("X-", block.toString(1));
	}

	@Test
	public void test_void_setC_int_long() {
		QBlock block = new QBlock(2);
		block.setC(0, 0x3L);
		block.setC(1, 0x1L);
		assertEquals("00", block.toString(0));
		assertEquals("0-", block.toString(1));
	}

	@Test
	public void test_long_getV_int() {
		QBlock block = new QBlock(3);
		block.setValue(0, 0, '1');
		block.setValue(0, 1, 'X');
		block.setValue(1, 1, '-');
		block.setValue(1, 2, '0');
		assertEquals(1L, block.getV(0));
		assertEquals(1L, block.getV(1));
		assertEquals(0L, block.getV(2));
	}

	@Test
	public void test_long_getC_int() {
		QBlock block = new QBlock(3);
		block.setValue(0, 0, '1');
		block.setValue(0, 1, 'X');
		block.setValue(1, 1, '-');
		block.setValue(1, 2, '0');
		assertEquals(1L, block.getC(0));
		assertEquals(0L, block.getC(1));
		assertEquals(2L, block.getC(2));
	}

	@Test
	public void test_boolean_equals_Object() {
		QBlock block1 = new QBlock(new BBlock(1L, 0L), new BBlock(1L, 1L));
		QBlock block2 = new QBlock(new BBlock(1L, 0L), new BBlock(1L, 1L));
		QBlock block3 = new QBlock(new BBlock(-1L, 0L), new BBlock(1L, 1L));
		assertTrue(block1.equals(block2));
		assertTrue(block2.equals(block1));
		assertFalse(block1.equals(block3));
		assertFalse(block2.equals(block3));
		assertFalse(block3.equals(block1));
		assertFalse(block3.equals(block2));
	}

	@Test
	public void test_void_copyTo_int_BVector() {
		QBlock block = new QBlock(2);
		block.setV(0, 0b1010);
		block.setC(0, 0b1100);
		BVector vector = new BVector(2);
		block.copyTo(0, vector);
		assertEquals("00", vector.toString());
		block.copyTo(1, vector);
		assertEquals("00", vector.toString());
		block.copyTo(2, vector);
		assertEquals("00", vector.toString());
		block.copyTo(3, vector);
		assertEquals("10", vector.toString());
		vector.setString("11");
		block.copyTo(0, vector);
		assertEquals("11", vector.toString());
		block.copyTo(1, vector);
		assertEquals("11", vector.toString());
		block.copyTo(2, vector);
		assertEquals("01", vector.toString());
		block.copyTo(3, vector);
		assertEquals("11", vector.toString());
	}

	@Test
	public void test_void_copyTo_int_QVector() {
		QBlock block = new QBlock(2);
		block.setV(0, 0b1010);
		block.setC(0, 0b1100);
		QVector vector = new QVector(2);
		block.copyTo(0, vector);
		assertEquals("--", vector.toString());
		block.copyTo(1, vector);
		assertEquals("X-", vector.toString());
		block.copyTo(2, vector);
		assertEquals("0-", vector.toString());
		block.copyTo(3, vector);
		assertEquals("1-", vector.toString());
	}

	@Test
	public void test_void_copyTo_long_BBlock() {
		QBlock block1 = new QBlock(2);
		block1.setV(0, 0b11001100);
		block1.setC(0, 0b10101010);
		BBlock block2 = new BBlock(2);
		block1.copyTo(0xf0L, block2);
		assertEquals(0b10000000L, block2.get(0));
		assertEquals(0b00000000L, block2.get(1));
		block2.set(0, 0b11111111L);
		block2.set(1, 0b11111111L);
		block1.copyTo(0xf0L, block2);
		assertEquals(0b11011111L, block2.get(0));
		assertEquals(0b11111111L, block2.get(1));
	}

	@Test
	public void test_void_copyTo_long_QBlock() {
		QBlock block1 = new QBlock(2);
		block1.setV(0, 0b11001100);
		block1.setC(0, 0b10101010);
		QBlock block2 = new QBlock(2);
		block1.copyTo(0xf0L, block2);
		assertEquals(0b11000000L, block2.getV(0));
		assertEquals(0b10100000L, block2.getC(0));
		assertEquals(0b00000000L, block2.getV(1));
		assertEquals(0b00000000L, block2.getC(1));
		block2.setV(0, 0b11111111L);
		block2.setC(0, 0b11111111L);
		block2.setV(1, 0b11111111L);
		block2.setC(1, 0b11111111L);
		block1.copyTo(0xf0L, block2);
		assertEquals(0b11001111L, block2.getV(0));
		assertEquals(0b10101111L, block2.getC(0));
		assertEquals(0b00001111L, block2.getV(1));
		assertEquals(0b00001111L, block2.getC(1));
	}
}
