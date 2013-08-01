/*
 * Copyright 2013 The KyuPI project contributors. See the COPYRIGHT.md file
 * at the top-level directory of this distribution.
 * This file is part of the KyuPI project. It is subject to the license terms
 * in the LICENSE.md file found in the top-level directory of this distribution.
 * No part of the KyuPI project, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.md file.
 */
package org.kyupi.apps;

import java.util.ArrayList;
import java.util.HashMap;

import org.kyupi.misc.RuntimeTools;

/**
 * prints some JVM statistics like memory footprints of various objects.
 */
public class JvmStats extends App {

	public static void main(String[] args) throws Exception {
		new JvmStats().call();
	}
	
	private static final int COUNT = 10000;

	private static final int TYPE_OBJECT = 1;
	private static final int TYPE_INTEGER = 2;
	private static final int TYPE_LONG = 3;
	private static final int TYPE_STRING5 = 4;
	private static final int TYPE_STRING10 = 5;
	private static final int TYPE_STRING15 = 6;
	private static final int TYPE_INTARRAY1000 = 7;
	private static final int TYPE_HASHMAP100 = 8;
	private static final int TYPE_OBJARRAY1000 = 9;
	private static final int TYPE_LONGARRAY1000 = 10;
	private static final int TYPE_OBJARRAYLIST1000 = 11;

	/**
	 * @exclude
	 */
	@Override
	public Void call() throws Exception {
		printWelcome();
		long int1000, long1000, ref1000;
		int1000 = measure(TYPE_INTARRAY1000);
		log.info("sizeof(int) = " + format((int1000 / 1000000) * 1000));
		long1000 = measure(TYPE_LONGARRAY1000);
		log.info("sizeof(long) = " + format((long1000 / 1000000) * 1000));
		ref1000 = measure(TYPE_OBJARRAY1000);
		log.info("sizeof(reference) = " + format((ref1000 / 1000000) * 1000));
		log.info("sizeof(Object) = " + format(measure(TYPE_OBJECT)));
		log.info("sizeof(Integer) = " + format(measure(TYPE_INTEGER)));
		log.info("sizeof(Long) = " + format(measure(TYPE_LONG)));
		log.info("sizeof(String(5)) = " + format(measure(TYPE_STRING5)));
		log.info("sizeof(String(10)) = " + format(measure(TYPE_STRING10)));
		log.info("sizeof(String(15)) = " + format(measure(TYPE_STRING15)));
		log.info("sizeof(int[1000]) = " + format(int1000));
		log.info("sizeof(long[1000]) = " + format(long1000));
		log.info("sizeof(Object[1000]) = " + format(ref1000));
		log.info("sizeof(HashMap<Int,Int>(100)) = " + format(measure(TYPE_HASHMAP100)));
		log.info("sizeof(ArrayList<Object>(1000)) = " + format(measure(TYPE_OBJARRAYLIST1000)));
		printGoodbye();
		return null;
	}

	private String format(long size) {
		return String.format("%.3f bytes", 1.0 * size / COUNT);
	}

	private Object[] objects = new Object[COUNT];

	private long measure(int type) {
		long free1 = 0;

		for (int i = 0; i < COUNT; ++i)
			objects[i] = null;

		for (int i = -1; i < COUNT; ++i) {
			Object object = null;
			switch (type) {
			case TYPE_INTARRAY1000:
				object = new int[1000];
				break;
			case TYPE_LONGARRAY1000:
				object = new long[1000];
				break;
			case TYPE_OBJARRAY1000:
				object = new Object[1000];
				break;
			case TYPE_OBJARRAYLIST1000:
				object = new ArrayList<Object>(1000);
				break;
			case TYPE_OBJECT:
				object = new Object();
				break;
			case TYPE_INTEGER:
				object = new Integer(i);
				break;
			case TYPE_LONG:
				object = new Long(i);
				break;
			case TYPE_STRING5:
				object = new String("" + (COUNT + i));
				break;
			case TYPE_STRING10:
				object = new String("xxxxx" + (COUNT + i));
				break;
			case TYPE_STRING15:
				object = new String("yyyyyxxxxx" + (COUNT + i));
				break;
			case TYPE_HASHMAP100:
				HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>();
				for (int idx = 0; idx < 100; idx++) {
					hm.put(idx, idx);
				}
				object = hm;
				break;
			}

			if (i >= 0) {
				objects[i] = object;
			} else {
				object = null;
				free1 = RuntimeTools.garbageCollect();
			}
		}
		return free1 - RuntimeTools.garbageCollect();
	}

}
