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

import org.kyupi.graph.Graph;
import org.kyupi.misc.RuntimeTools;

public class DesignStatistics extends App {

	public static void main(String[] args) throws Exception {
		new DesignStatistics().setArgs(args).call();
	}

	@Override
	public Void call() throws Exception {
		
		printWelcome();

		long free = RuntimeTools.garbageCollect();

		Graph g = loadCircuitFromArgs();
		
		long memory = (free - RuntimeTools.garbageCollect());
		
		int gates = g.countNodes();

		log.info("MemoryRequirement " + (memory / 1024) + " kB");
		log.info("MemoryPerNode " + (memory / gates) + " B");
		log.info("Nodes " + gates);
		log.info("Levels " + g.levels());
		
		log.info("Inputs " + g.countInputs());
		log.info("Outputs " + g.countOutputs());
		
		printGoodbye();
		
		return null;
	}
	
	public void testC17() throws Exception {
		setArgs("-d", "testdata/c17.isc");
		Graph g = loadCircuitFromArgs();
		assertEquals(13, g.countNodes());
		assertEquals(5, g.countInputs());
		assertEquals(2, g.countOutputs());
	}
}
