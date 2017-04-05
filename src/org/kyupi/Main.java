/*
 * Copyright 2013 The KyuPI project contributors. See the COPYRIGHT.md file
 * at the top-level directory of this distribution.
 * This file is part of the KyuPI project. It is subject to the license terms
 * in the LICENSE.md file found in the top-level directory of this distribution.
 * No part of the KyuPI project, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.md file.
 */
package org.kyupi;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.kyupi.data.QVExpanderTest;
import org.kyupi.data.item.BBlockTest;
import org.kyupi.data.item.BVectorTest;
import org.kyupi.data.item.QBlockTest;
import org.kyupi.data.item.QVectorTest;
import org.kyupi.data.source.BBSourceTest;
import org.kyupi.data.source.BVSourceTest;
import org.kyupi.data.source.QBSourceTest;
import org.kyupi.data.source.QVSourceTest;
import org.kyupi.faults.StuckAtCollectionTest;
import org.kyupi.graph.Graph;
import org.kyupi.graph.Graph.Node;
import org.kyupi.graph.GraphTest;
import org.kyupi.graph.GraphTools;
import org.kyupi.graph.ScanChainsTest;
import org.kyupi.ipc.ProcessManagerTest;
import org.kyupi.misc.JvmStats;
import org.kyupi.misc.KyupiApp;
import org.kyupi.misc.RuntimeTools;
import org.kyupi.sim.BBPlainSimTest;
import org.kyupi.sim.FaultSimSimpleTest;
import org.kyupi.sim.ObservabilityTest;
import org.kyupi.sim.QBPlainSimTest;
import org.kyupi.sim.QVPlainSimTest;
import org.kyupi.sim.SimulatorTest;

public class Main extends KyupiApp {

	private static Class<?> ALL_TEST_CLASSES[] = { BBlockTest.class, BVectorTest.class, QBlockTest.class,
			QVectorTest.class, BBSourceTest.class, BVSourceTest.class, QBSourceTest.class, QVSourceTest.class,
			StuckAtCollectionTest.class, GraphTest.class, ScanChainsTest.class, ProcessManagerTest.class,
			BBPlainSimTest.class, QBPlainSimTest.class, QVPlainSimTest.class, FaultSimSimpleTest.class,
			ObservabilityTest.class, SimulatorTest.class, QVExpanderTest.class, Main.class };

	public static void main(String[] args) throws Exception {
		new Main().setArgs(args).call();
	}

	@Override
	public Void call() throws Exception {
		printWelcome();

		if (argsParsed().hasOption("js")) {
			new JvmStats().print_stats();
			return null;
		}

		if (argsParsed().hasOption("d")) {
			long free = RuntimeTools.garbageCollect();

			Graph graph = loadCircuitFromArgs();

			long memory = (free - RuntimeTools.garbageCollect());

			log.info("MemoryRequirement " + (memory / 1024) + " kB");
			log.info("MemoryPerNode " + (memory / graph.countNodes()) + " B");
			printGraphStats(graph);

			ArrayList<File> files = outputFilesFromArgs();
			if (files.size() > 0) {
				GraphTools.saveGraph(graph, files.get(0), argsParsed().hasOption("f"));
				log.info("Written " + files.get(0));
			}

			return null;
		}

		log.info("This is the Kyutech Parallel Processing Platform for Integrated circuits (KyuP³I).");
		log.info("KyuPI is distributed under a BSD-license. See LICENSE.md for details.");
		log.info("For getting started with this Java library, see README.md.");
		log.info("Usage of the kyupi default application:");
		log.info("  kyupi <options...>");
		log.info("Options:");
		printOptionHelp();
		log.info("Running test suite...");
		JUnitCore ju = new JUnitCore();
		Result res = ju.run(ALL_TEST_CLASSES);
		log.info("Tests " + res.getRunCount());
		log.info("Failures " + res.getFailureCount());
		for (Failure f : res.getFailures()) {
			log.error("FailedTest " + f.getTestHeader());
			log.error("   " + f.getMessage() + f.getTrace());
		}
		if (res.getFailureCount() > 0) {
			log.error("Some tests failed. Please ensure the following:");
			log.error(" * Execute with Java 8 or newer, preferably using the JDK from Oracle.");
			log.error(" * The kernel subdirectory contains the necessary binaries (compile them with make).");
			log.error("If problems persist, try to troubleshoot and get in touch with us. See README.md for details.");
		} else {
			log.info("All tests passed. Everything looks fine.");
		}
		printGoodbye();
		return null;
	}

	private void printGraphStats(Graph graph) {
		HashMap<String, Integer> pseudo = new HashMap<>();
		HashMap<String, Integer> combinational = new HashMap<>();
		HashMap<String, Integer> inputst = new HashMap<>();
		HashMap<String, Integer> outputst = new HashMap<>();
		HashMap<String, Integer> sequential = new HashMap<>();
		int inputs = 0;
		int outputs = 0;
		int gates = 0;
		int nodes = 0;
		int signals = 0;
		int seq = 0;
		for (Node n : graph.accessNodes()) {
			if (n == null)
				continue;
			String type = n.typeName();
			nodes++;
			if (n.isPseudo()) {
				signals++;
				pseudo.put(type, pseudo.getOrDefault(type, 0) + 1);
				continue;
			}
			if (n.isSequential()) {
				seq++;
				sequential.put(type, sequential.getOrDefault(type, 0) + 1);
				continue;
			}
			if (n.isInput()) {
				inputs++;
				inputst.put(type, inputst.getOrDefault(type, 0) + 1);
			}
			if (n.isOutput()) {
				outputs++;
				outputst.put(type, outputst.getOrDefault(type, 0) + 1);
			}
			if (n.isInput() || n.isOutput())
				continue;
			gates++;
			combinational.put(type, combinational.getOrDefault(type, 0) + 1);
		}
		log.info("Levels " + graph.levels());
		log.info("NodeCount " + nodes);
		log.info("  PseudoNodeCount " + signals);
		printGateCounts(pseudo);
		log.info("  CombinationalCellCount " + gates);
		printGateCounts(combinational);
		log.info("  SequentialCellCount " + seq);
		printGateCounts(sequential);
		log.info("  PrimaryInputCount " + inputs);
		printGateCounts(inputst);
		log.info("  PrimaryOutputCount " + outputs);
		printGateCounts(outputst);
	}

	private void printGateCounts(HashMap<String, Integer> map) {
		ArrayList<String> keys = new ArrayList<>(map.keySet());
		keys.sort(new Comparator<String>() {
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
		for (String key : keys) {
			log.info("    " + key + " " + map.get(key));
		}

	}

	public void testC17() throws Exception {
		setArgs("-d", RuntimeTools.KYUPI_HOME + "/testdata/c17.isc");
		Graph g = loadCircuitFromArgs();
		assertEquals(13, g.countNodes());
		assertEquals(5, g.countInputs());
		assertEquals(2, g.countOutputs());
	}

}
