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
import org.kyupi.data.item.QVector;
import org.kyupi.data.source.QBSource;
import org.kyupi.data.source.QVSource;

public class QVPlainSim extends QVSource {

	private QVSource s;
	
	public QVPlainSim(LevelizedCircuit netlist, QVSource inputData) {
		super(inputData.length());
		s = QVSource.from(new QBPlainSim(netlist, QBSource.from(inputData)));
	}
	
	@Override
	public void reset() {
		s.reset();
	}

	@Override
	protected QVector compute() {
		if (s.hasNext())
			return s.next();
		else
			return null;
	}

}
