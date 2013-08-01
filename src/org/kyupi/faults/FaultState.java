/*
 * Copyright 2013 The KyuPI project contributors. See the COPYRIGHT.md file
 * at the top-level directory of this distribution.
 * This file is part of the KyuPI project. It is subject to the license terms
 * in the LICENSE.md file found in the top-level directory of this distribution.
 * No part of the KyuPI project, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.md file.
 */
package org.kyupi.faults;

public class FaultState {

	public long obs;
	
	public int detects;
	
	public int generation;
	
	public void clear() {
		obs = 0L;
		detects = 0;
		generation = 0;
	}
	
}
