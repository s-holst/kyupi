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

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.log4j.Logger;

/**
 * provides access to global runtime information.
 * 
 * This class cannot be instantiated. It contains only static members
 * 
 */
public class RuntimeTools {

	private static Logger log = Logger.getLogger(RuntimeTools.class);
	
	public static final String NAME = "KyuPI";

	public static final long START_TIME = System.currentTimeMillis();

	public static final long MEMORY_MB = Runtime.getRuntime().maxMemory() / 1024 / 1024;

	public static final String WORKING_DIR;

	public static final String KYUPI_HOME;

	public static final String JAVA_VERSION = System.getProperty("java.version");

	public static final String JAVA_VENDOR = System.getProperty("java.vendor");

	static {
		String dir = null;
		try {
			dir = (new File(".")).getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		WORKING_DIR = dir;
		try {
			dir = (new File(System.getProperty(NAME+".home", "."))).getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		KYUPI_HOME = dir;
	}

	private RuntimeTools() {
	}

	public static void printRuntimeInfo() {
		log.info("StartTime " + new Date(START_TIME));
		log.info("Memory " + MEMORY_MB + " MB");
		log.info("JavaVersion " + JAVA_VERSION + " " + JAVA_VENDOR);
		log.info("KYUPI_HOME " + KYUPI_HOME);
		log.info("WorkingDirectory " + WORKING_DIR);
	}

	/**
	 * runs the Java garbage collector multiple times.
	 * 
	 * @return Free memory in bytes after garbage collection.
	 */
	public static long garbageCollect() {
		Runtime r = Runtime.getRuntime();
		for (int i = 0; i < 3; i++)
			runGC(r);
		return r.freeMemory();
	}

	private static void runGC(Runtime r) {
		long free = r.freeMemory();
		long free_prev = 0;
		for (int i = 0; (free > free_prev) && (i < 500); i++) {
			r.runFinalization();
			r.gc();
			Thread.yield();

			free_prev = free;
			free = r.freeMemory();
		}
	}

}
