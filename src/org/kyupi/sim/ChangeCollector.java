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

import org.kyupi.data.item.BBlock;

/**
 * collects and accumulates the slots with bits different from the contents of
 * the BBlock.
 * 
 * This class is a drop-in replacement for a BBlock. It can be given to a
 * block-based simulator. It is used if only the slots with differences are
 * needed (e.g. in fault simulation with injected faults).
 * 
 * The class behaves exactly as BBlock until start() is called. After this call,
 * set(int,long) does not change the contents of the BBlock anymore but just
 * records the differences of the provided value with the contents set
 * previously.
 * 
 * stopAndReport() returns the comparison results and restores normal BBlock
 * operation.
 * 
 */
class ChangeCollector extends BBlock {

	boolean do_monitor = false;

	long detects = 0L;

	ChangeCollector(int length) {
		super(length);
	}

	public void start() {
		do_monitor = true;
		detects = 0L;
	}

	public void set(int position, long v) {
		if (do_monitor) {
			detects |= (v ^ get(position));
		} else {
			super.set(position, v);
		}
	}

	public long stopAndReport() {
		do_monitor = false;
		long ret = detects;
		detects = 0L;
		return ret;
	}
}
