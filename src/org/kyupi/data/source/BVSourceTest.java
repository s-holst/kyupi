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
import org.kyupi.data.item.BVector;

public class BVSourceTest extends TestCase {

	@Test
	public void test_BVSource_from_int_Iterable_1() throws Exception {
		ArrayList<BBlock> arr = new ArrayList<>();
		BBlock bb1 = new BBlock(0b001L, 0b011L);
		arr.add(bb1);

		BVSource s = BVSource.from(2, arr);

		assertEquals(2, s.length());

		assertTrue(s.hasNext());
		assertEquals("11", s.next().toString());
		assertEquals("01", s.next().toString());
		
		s.reset();
		assertTrue(s.hasNext());
		assertEquals("11", s.next().toString());
		assertEquals("01", s.next().toString());

		int count = 0;
		for (BVector v : s) {
			if (count == 0)
				assertEquals("11", v.toString());
			else if (count == 1)
				assertEquals("01", v.toString());
			else
				assertEquals("00", v.toString());
			v.free();
			count++;
		}
		assertFalse(s.hasNext());
		assertEquals(64, count);
	}

	public void test_BVSource_from_int_Iterable_2() throws Exception {
		ArrayList<BVector> arr = new ArrayList<>();
		BVector bv1 = new BVector("010");
		BVector bv2 = new BVector("001");
		arr.add(bv1);
		arr.add(bv2);

		BVSource s = BVSource.from(3, arr);
		
		assertEquals(3, s.length());

		assertTrue(s.hasNext());
		assertEquals("010", s.next().toString());
		assertEquals("001", s.next().toString());
		assertFalse(s.hasNext());
		
		s.reset();
		assertTrue(s.hasNext());
		assertEquals("010", s.next().toString());
		assertEquals("001", s.next().toString());
		assertFalse(s.hasNext());
	}
}
