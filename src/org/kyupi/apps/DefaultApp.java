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

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.kyupi.data.item.BBlockTest;
import org.kyupi.data.item.BVectorTest;
import org.kyupi.data.item.QBlockTest;
import org.kyupi.data.item.QVectorTest;
import org.kyupi.data.source.BBSourceTest;
import org.kyupi.data.source.BVSourceTest;
import org.kyupi.data.source.QBSourceTest;
import org.kyupi.data.source.QVSourceTest;
import org.kyupi.faults.StuckAtCollectionTest;
import org.kyupi.graph.GraphTest;
import org.kyupi.ipc.ProcessManagerTest;
import org.kyupi.sim.BBPlainSimTest;
import org.kyupi.sim.FaultSimSimpleTest;

public class DefaultApp extends App {

	private static Class<?> ALL_TEST_CLASSES[] = { BBlockTest.class, BVectorTest.class, QBlockTest.class,
			QVectorTest.class, BBSourceTest.class, BVSourceTest.class, QBSourceTest.class, QVSourceTest.class, StuckAtCollectionTest.class,
			GraphTest.class, ProcessManagerTest.class, BBPlainSimTest.class, FaultSimSimpleTest.class };

	public static void main(String[] args) throws Exception {
		new DefaultApp().setArgs(args).call();
	}

	@Override
	public Void call() throws Exception {
		printWelcome();
		log.info("This is the Kyutech Parallel Processing Platform for Integrated circuits (KyuPI).");
		log.info("KyuPI is distributed under a BSD-license. See LICENSE.md for details.");
		log.info("For getting started with the platform, see README.md.");
		log.info("Usage of the kyupi start script shipped with this distribution:");
		log.info("  kyupi <app> <options...>");
		log.info("For a list of shipped apps, see the kyupi start script.");
		log.info("Common options used by many apps:");
		printOptionHelp();
		log.info("Running test suite...");
		JUnitCore ju = new JUnitCore();
		Result res = ju.run(ALL_TEST_CLASSES);
		log.info("Tests " + res.getRunCount());
		log.info("Failures " + res.getFailureCount());
		for (Failure f : res.getFailures()) {
			log.error("FailedTest " + f.getTestHeader());
			log.error("   " + f.getMessage());
		}
		if (res.getFailureCount() > 0) {
			log.error("Some tests failed. Please ensure the following:");
			log.error(" * Execute with Java 7 or newer, preferably using the JDK from Oracle.");
			log.error(" * The kernel subdirectory contains the necessary binaries (compile them with make, if necessary).");
			log.error("If problems persist, try to troubleshoot from within Eclipse and get in touch with us. See README.md for details.");
		} else {
			log.info("Everything looks fine.");
		}
		printGoodbye();
		return null;
	}

}
