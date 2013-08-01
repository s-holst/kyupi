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
import java.util.Random;

import junit.framework.TestCase;

import org.junit.Test;
import org.kyupi.data.item.QBlock;
import org.kyupi.data.item.QVector;

public class QBSourceTest extends TestCase {

	@Test
	public void test_QBSource_random_int_int() throws Exception {
		QBSource s1 = QBSource.random(10, 42);
		QBSource s2 = QBSource.random(10, 42);

		QBlock b1 = s1.next();
		assertEquals(10, b1.length());
		
		assertEquals(new Random(42).nextLong(), b1.getV(0));

		QBlock b2 = s2.next();
		assertEquals(b1, b2);

		b2 = s2.next();
		assertNotSame(b1, b2);

		s2.reset();
		b2 = s2.next();
		assertEquals(b1, b2);
	}

	@Test
	public void test_QBSource_from_int_Iterable() throws Exception {
		ArrayList<QVector> arr = new ArrayList<>();
		QVector qv1 = new QVector("X10");
		QVector qv2 = new QVector("-01");
		arr.add(qv1);
		arr.add(qv2);

		QBSource s = QBSource.from(3, arr);

		assertEquals(3, s.length());
		assertTrue(s.hasNext());
		QBlock b = s.next();
		assertFalse(s.hasNext());

		// qv2 gets repeated to fill block

		// first values from all vectors
		assertEquals(1L, b.getV(0));
		assertEquals(0L, b.getC(0));
		// second values from all vectors
		assertEquals(1L, b.getV(1));
		assertEquals(-1L, b.getC(1));
		// third values from all vectors
		assertEquals(0xffff_ffff_ffff_fffeL, b.getV(2));
		assertEquals(-1L, b.getC(2));

		s.reset();
		assertTrue(s.hasNext());
		assertEquals(b,s.next());
		assertFalse(s.hasNext());
	}
}
