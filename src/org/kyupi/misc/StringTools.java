/*
 * Copyright 2013 The KyuPI project contributors. See the COPYRIGHT.md file
 * at the top-level directory of this distribution.
 * This file is part of the KyuPI project. It is subject to the license terms
 * in the LICENSE.md file found in the top-level directory of this distribution.
 * No part of the KyuPI project, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.md file.
 */
package org.kyupi.misc;

import java.util.ArrayList;

public class StringTools {

	public static String join(ArrayList<String> items, String glue) {
		StringBuilder sb = new StringBuilder();
		int n = items.size();
		for (int i = 0; i < n; i++) {
			sb.append(items.get(i).toString());
			if (i < n-1)
				sb.append(glue);
		}
		return sb.toString();
	}
	
	public static String longToReadableBinaryString(long l) {
		String s = Long.toBinaryString(l);
		s = "0000000000000000000000000000000000000000000000000000000000000000".substring(s.length()) + s;
		return s.substring(0, 4) + "." + s.substring(4, 8) + " " + s.substring(8, 12) + "." + s.substring(12, 16) + "  " +
			   s.substring(16+0, 16+4) + "." + s.substring(16+4, 16+8) + " " + s.substring(16+8, 16+12) + "." + s.substring(16+12, 16+16) + "  " +
			   s.substring(32+0, 32+4) + "." + s.substring(32+4, 32+8) + " " + s.substring(32+8, 32+12) + "." + s.substring(32+12, 32+16) + "  " +
			   s.substring(48+0, 48+4) + "." + s.substring(48+4, 48+8) + " " + s.substring(48+8, 48+12) + "." + s.substring(48+12, 48+16) + "  " 
			   
		
	;
	}

	public static String join(Object[] items, String glue) {
		StringBuilder sb = new StringBuilder();
		int n = items.length;
		for (int i = 0; i < n; i++) {
			sb.append(items[i].toString());
			if (i < n-1)
				sb.append(glue);
		}
		return sb.toString();
	}
}
