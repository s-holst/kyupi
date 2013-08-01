/*
 * Copyright 2013 The KyuPI project contributors. See the COPYRIGHT.md file
 * at the top-level directory of this distribution.
 * This file is part of the KyuPI project. It is subject to the license terms
 * in the LICENSE.md file found in the top-level directory of this distribution.
 * No part of the KyuPI project, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.md file.
 */
package org.kyupi.data.source;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.junit.Test;
import org.kyupi.data.item.BBlock;
import org.kyupi.data.item.QBlock;
import org.kyupi.data.item.QVector;

public class QVSourceTest extends TestCase {

	@Test
	public void test_QVSource_from_int_Iterable_1() throws Exception {
		ArrayList<QBlock> arr = new ArrayList<>();
		QBlock qb1 = new QBlock(new BBlock(0b010101L, 0b110011L), new BBlock(0b000011, 0b000011));
		arr.add(qb1);

		// v0: 010101
		// c0: 000011
		// V0: -X-X01
		// v1: 110011
		// c1: 000011
		// V2: XX--11

		QVSource s = QVSource.from(2, arr);

		assertEquals(2, s.length());

		assertTrue(s.hasNext());
		assertEquals("11", s.next().toString());
		assertEquals("01", s.next().toString());
		assertEquals("X-", s.next().toString());
		assertEquals("--", s.next().toString());
		assertEquals("XX", s.next().toString());
		assertEquals("-X", s.next().toString());
		assertEquals("--", s.next().toString());

		s.reset();
		assertTrue(s.hasNext());
		assertEquals("11", s.next().toString());
		assertEquals("01", s.next().toString());
		assertEquals("X-", s.next().toString());

		int count = 0;
		for (QVector v : s) {
			if (count == 0)
				assertEquals("11", v.toString());
			else if (count == 1)
				assertEquals("01", v.toString());
			else if (count == 2)
				assertEquals("X-", v.toString());
			v.free();
			count++;
		}
		assertFalse(s.hasNext());
		assertEquals(64, count);
	}

	public void test_QVSource_from_int_Iterable_2() throws Exception {
		ArrayList<QVector> arr = new ArrayList<>();
		QVector qv1 = new QVector("X10");
		QVector qv2 = new QVector("-01");
		arr.add(qv1);
		arr.add(qv2);

		QVSource s = QVSource.from(3, arr);

		assertEquals(3, s.length());

		assertTrue(s.hasNext());
		assertEquals("X10", s.next().toString());
		assertEquals("-01", s.next().toString());
		assertFalse(s.hasNext());

		s.reset();
		assertTrue(s.hasNext());
		assertEquals("X10", s.next().toString());
		assertEquals("-01", s.next().toString());
		assertFalse(s.hasNext());
	}
}
