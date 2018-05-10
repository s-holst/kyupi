/*
 * Copyright 2017 The KyuPI project contributors. See the COPYRIGHT.md file
 * at the top-level directory of this distribution.
 * This file is part of the KyuPI project. It is subject to the license terms
 * in the LICENSE.md file found in the top-level directory of this distribution.
 * No part of the KyuPI project, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.md file.
 */
package org.kyupi.sim;

import org.kyupi.circuit.LevelizedCircuit;
import org.kyupi.data.item.BBlock;
import org.kyupi.data.source.BBSource;
import org.kyupi.data.source.QBSource;

public class BBPlainSim extends BBSource {

	private BBSource s;
	
	public BBPlainSim(LevelizedCircuit netlist, BBSource inputData) {
		super(inputData.length());
		s = BBSource.from(new QBPlainSim(netlist, QBSource.from(inputData)));
	}
	
	@Override
	public void reset() {
		s.reset();
	}

	@Override
	protected BBlock compute() {
		if (s.hasNext())
			return s.next();
		else
			return null;
	}

}
