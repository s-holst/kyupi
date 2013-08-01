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
import org.kyupi.data.item.BBlock;
import org.kyupi.data.item.BVector;

public class BBSourceTest extends TestCase {

	@Test
	public void test_BBSource_random_int_int() throws Exception {
		BBSource s1 = BBSource.random(10, 42);
		BBSource s2 = BBSource.random(10, 42);

		BBlock b1 = s1.next();
		assertEquals(10, b1.length());
		
		assertEquals(new Random(42).nextLong(), b1.get(0));

		BBlock b2 = s2.next();
		assertEquals(b1, b2);

		b2 = s2.next();
		assertNotSame(b1, b2);

		s2.reset();
		b2 = s2.next();
		assertEquals(b1, b2);
	}

	@Test
	public void test_BBSource_from_int_Iterable() throws Exception {
		ArrayList<BVector> arr = new ArrayList<>();
		BVector bv1 = new BVector("010");
		BVector bv2 = new BVector("001");
		arr.add(bv1);
		arr.add(bv2);

		BBSource s = BBSource.from(3, arr);

		assertEquals(3, s.length());
		assertTrue(s.hasNext());
		BBlock b = s.next();
		assertFalse(s.hasNext());

		// bv2 gets repeated to fill block

		// first values from all vectors
		assertEquals(0L, b.get(0));
		// second values from all vectors
		assertEquals(1L, b.get(1));
		// third values from all vectors
		assertEquals(0xffff_ffff_ffff_fffeL, b.get(2));

		s.reset();
		assertTrue(s.hasNext());
		assertEquals(b,s.next());
		assertFalse(s.hasNext());
	}
}
