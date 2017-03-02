package org.kyupi.data;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;
import org.kyupi.data.item.QVector;
import org.kyupi.data.source.QVSource;

public class QVExpanderTest {

	@Test
	public void test() {
		ArrayList<QVector> arr = new ArrayList<>();
		QVector qv1 = new QVector("X10");
		QVector qv2 = new QVector("-01");
		arr.add(qv1);
		arr.add(qv2);

		QVSource s = QVSource.from(3, arr);

		int[][] map = new int[][] { { 2, 1, 0, 0 }, { -1, 3, 1, 2 } };
		
		QVSource exp = new QVExpander(s, map);

		assertEquals(4, exp.length());
		assertTrue(exp.hasNext());
		assertEquals("01XX", exp.next().toString());
		assertEquals("--10", exp.next().toString());
		assertEquals("10--", exp.next().toString());
		assertEquals("--01", exp.next().toString());
		assertFalse(exp.hasNext());
	}

}
