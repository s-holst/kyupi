/*
 * Copyright 2013 The KyuPI project contributors. See the COPYRIGHT.md file
 * at the top-level directory of this distribution.
 * This file is part of the KyuPI project. It is subject to the license terms
 * in the LICENSE.md file found in the top-level directory of this distribution.
 * No part of the KyuPI project, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.md file.
 */
package org.kyupi.sim;

import java.util.HashMap;

import org.kyupi.data.item.QBlock;
import org.kyupi.data.source.QBSource;
import org.kyupi.faults.StuckAtCollection;
import org.kyupi.faults.StuckAtCollection.StuckAtFault;
import org.kyupi.graph.Graph;
import org.kyupi.misc.StringTools;

public class FaultSimSimple extends QBSource {

	private Graph circuit;
	private QBSource source;
	private Observability obssim;

	class Fstate {
		int ndetects;
	}

	private HashMap<StuckAtFault, Fstate> faults = new HashMap<>();

	public FaultSimSimple(StuckAtCollection faults, QBSource source) {
		super(source.length());
		this.source = source;
		this.circuit = faults.netlist;
		this.obssim = new Observability(circuit);
		for (StuckAtFault flt : faults.getAllFaultsCollapsed()) {
			this.faults.put(flt, new Fstate());
		}
	}

	@Override
	protected QBlock compute() {
		if (!source.hasNext())
			return null;
		QBlock b = source.next();
		obssim.loadInputsFrom(b);

		for (StuckAtFault fault : faults.keySet()) {
			if (fault.pin.isOutput()) {
				long obs = obssim.getObservability(fault.pin.node());
				long val = obssim.getValue(fault.pin.node());
				log.debug(fault.toString() + " obs " + StringTools.longToReadableBinaryString(obs));
				log.debug(fault.toString() + " val " + StringTools.longToReadableBinaryString(val));
				if (fault.isSA0()) {
					int cnt = Long.bitCount(obs & val);
					faults.get(fault).ndetects += cnt;
				} else {
					int cnt = Long.bitCount(obs & ~val);
					faults.get(fault).ndetects += cnt;					
				}
			} else {
				// TODO handle faults at inputs
			}
		}

		obssim.storeOutputsTo(b);
		return b;
	}

	@Override
	public void reset() {
		source.reset();
	}

	public int getDetects(StuckAtFault flt) {
		return faults.get(flt).ndetects;
	}
}
