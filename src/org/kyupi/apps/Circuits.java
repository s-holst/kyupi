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

import java.io.File;
import java.util.ArrayList;

import org.kyupi.graph.Graph;
import org.kyupi.graph.GraphTools;

/**
 * processes and exports circuits.
 */
public class Circuits extends App {

	public static void main(String[] args) throws Exception {
		new Circuits().setArgs(args).call();
	}

	public Circuits() {
		options.addOption("p", true, "process the netlist");
	}

	public Void call() throws Exception {

		printWelcome();

		Graph graph = loadCircuitFromArgs();

		if (argsParsed().hasOption("p")) {
			switch (argsParsed().getOptionValue("p")) {
			case "fs":
				GraphTools.replaceScanCellsWithPseudoPorts(graph);
				break;
			}
		}
		ArrayList<File> files = outputFilesFromArgs();
		if (files.size() > 0) {
			GraphTools.saveGraph(graph, files.get(0), argsParsed().hasOption("f"));
			log.info("Written " + files.get(0));
		}
		
		printGoodbye();
		
		return null;
	}

}
