/*
 * Copyright 2013-2015 The KyuPI project contributors. See the COPYRIGHT.md file
 * at the top-level directory of this distribution.
 * This file is part of the KyuPI project. It is subject to the license terms
 * in the LICENSE.md file found in the top-level directory of this distribution.
 * No part of the KyuPI project, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.md file.
 */
package org.kyupi.misc;

import java.lang.reflect.Array;
import java.util.Arrays;

public class ArrayTools {

	private static final float GROW_FACTOR = 0.5f;

	public static Object[] grow(Object[] arr, Class<?> clazz, int min_size, float grow_factor) {
		if (arr == null || arr.length < min_size) {
			int new_size = min_size + (int) (grow_factor * min_size);
			Object[] arr2 = (Object[]) Array.newInstance(clazz, new_size);
			if (arr != null)
				System.arraycopy(arr, 0, arr2, 0, arr.length);
			arr = arr2;
		}
		return arr;
	}

	public static Object[] grow(Object[] arr, Class<?> clazz, int min_size) {
		return ArrayTools.grow(arr, clazz, min_size, GROW_FACTOR);
	}

	public static Object[] strip(Object[] arr) {
		int i;
		for (i = arr.length; i > 0 && arr[i - 1] == null; i--)
			;
		return Arrays.copyOfRange(arr, 0, i);
	}

	public static int[] grow(int[] arr, int min_size, int block_size, int init_value) {
		if (arr == null || arr.length < min_size) {
			int new_size = ((min_size - 1) / block_size + 1) * block_size;
			int[] arr2 = new int[new_size];
			Arrays.fill(arr2, init_value);
			if (arr != null)
				System.arraycopy(arr, 0, arr2, 0, arr.length);
			arr = arr2;
		}
		return arr;
	}

	public static long[] grow(long[] arr, int min_size, int block_size, long init_value) {
		if (arr == null || arr.length < min_size) {
			int new_size = ((min_size - 1) / block_size + 1) * block_size;
			long[] arr2 = new long[new_size];
			Arrays.fill(arr2, init_value);
			if (arr != null)
				System.arraycopy(arr, 0, arr2, 0, arr.length);
			arr = arr2;
		}
		return arr;
	}

	public static boolean[] grow(boolean[] arr, int min_size, int block_size, boolean init_value) {
		if (arr == null || arr.length < min_size) {
			int new_size = ((min_size - 1) / block_size + 1) * block_size;
			boolean[] arr2 = new boolean[new_size];
			Arrays.fill(arr2, init_value);
			if (arr != null)
				System.arraycopy(arr, 0, arr2, 0, arr.length);
			arr = arr2;
		}
		return arr;
	}

	public static double[] grow(double[] arr, int min_size, int block_size, double init_value) {
		if (arr == null || arr.length < min_size) {
			int new_size = ((min_size - 1) / block_size + 1) * block_size;
			double[] arr2 = new double[new_size];
			Arrays.fill(arr2, init_value);
			if (arr != null)
				System.arraycopy(arr, 0, arr2, 0, arr.length);
			arr = arr2;
		}
		return arr;
	}

	public static int countEntries(Object arr[]) {
		if (arr == null)
			return 0;
		int n = 0;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] != null)
				n++;
		}
		return n;
	}

	public static int maxNonNullIndex(Object arr[]) {
		if (arr == null)
			return -1;
		for (int i = arr.length; i >= 1; i--) {
			if (arr[i - 1] != null)
				return i - 1;
		}
		return -1;
	}

	public static Object safeGet(Object[] arr, int idx) {
		if (arr == null || idx < 0 || (idx >= arr.length))
			return null;
		return arr[idx];
	}

	public static int linearSearchReference(Object[] arr, Object node) {
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == node)
				return i;
		}
		return -1;
	}

	public static void replaceAll(Object[] arr, Object current, Object replacement) {
		if (arr == null)
			return;
		for (int i = arr.length - 1; i >= 0; i--) {
			if (arr[i] == current) {
				arr[i] = replacement;
			}
		}
	}

	public static long[] asLongArray(long... a) {
		return a;
	}

	public static void moveToFront(Object[] arr) {
		if (arr == null)
			return;
		int ptr1 = -1;
		int ptr2 = -1;
		int l = arr.length;

		while (true) {
			while (++ptr1 < l - 1 && arr[ptr1] != null)
				;
			ptr2 = Math.max(ptr1, ptr2) - 1;
			while (++ptr2 < l - 1 && arr[ptr2] == null)
				;
			if (arr[ptr1] == null && arr[ptr2] != null) {
				arr[ptr1] = arr[ptr2];
				arr[ptr2] = null;
			} else
				break;
		}
	}

	public static float min(float[] arr) {
		float retval = Float.POSITIVE_INFINITY;
		for (float v : arr) {
			if (v < retval)
				retval = v;
		}
		return retval;
	}
	
	public static int minIndex(double[] arr, int size) {
		if (size > arr.length)
			size = arr.length;
		if (size <= 0)
			return -1;
		double smallest = arr[0];
		int smallestIndex = 0;
		for (int i = 1; i < size; i++) {
			if (arr[i] < smallest) {
				smallest = arr[i];
				smallestIndex = i;
			}
		}
		return smallestIndex;
	}

	public static int search(float[] arr, float key) {
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == key)
				return i;
		}
		return -1;
	}

	public static String toString(float[] w1) {
		StringBuffer buf = new StringBuffer();
		for (float f : w1) {
			buf.append("" + f + " ");
		}
		return buf.toString();
	}

	public static String toString(double[] arr, int size) {
		if (size > arr.length)
			size = arr.length;
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < size; i++) {
			buf.append(String.format("%.3f ", arr[i]));
		}
		return "[ " + buf.toString() + "]";
	}
	
	public static String toString(boolean[] arr, int size) {
		if (size > arr.length)
			size = arr.length;
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < size; i++) {
			buf.append(arr[i] ? "H ":"L ");
		}
		return "[ " + buf.toString() + "]";
	}
	
	public static int max(int[] array) {
		int max = array[0];
		for (int x: array)
			max = Math.max(max, x);
		return max;
	}

}
