/*
 * Copyright 2013-2017 The KyuPI project contributors. See the COPYRIGHT.md file
 * at the top-level directory of this distribution.
 * This file is part of the KyuPI project. It is subject to the license terms
 * in the LICENSE.md file found in the top-level directory of this distribution.
 * No part of the KyuPI project, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.md file.
 */
package org.kyupi.sim;

import org.apache.log4j.Logger;
import org.kyupi.circuit.Graph;
import org.kyupi.data.item.QBlock;
import org.kyupi.data.source.QBSource;
import org.kyupi.sim.Simulator.State;

public class QBPlainSim extends QBSource {

	protected static Logger log = Logger.getLogger(BBPlainSim.class);

	private QBSource source;
	private State state;

	public QBPlainSim(Graph circuit, QBSource source) {
		super(source.length());
		if (circuit.accessInterface().length > source.length()) {
			throw new IllegalArgumentException("insufficient data width for the interface of the netlist.");
		}
		this.source = source;
		this.state = (new Simulator(circuit)).new State();
	}

	@Override
	protected QBlock compute() {
		if (!source.hasNext())
			return null;
		QBlock b = source.next();
		state.loadInputsFrom(b);
		state.simulate();
		state.storeOutputsTo(b);
		state.clear();
		return b;
	}

	@Override
	public void reset() {
		source.reset();
	}
}
