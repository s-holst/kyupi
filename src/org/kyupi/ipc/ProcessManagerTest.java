/*
 * Copyright 2013 The KyuPI project contributors. See the COPYRIGHT.md file
 * at the top-level directory of this distribution.
 * This file is part of the KyuPI project. It is subject to the license terms
 * in the LICENSE.md file found in the top-level directory of this distribution.
 * No part of the KyuPI project, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.md file.
 */
package org.kyupi.ipc;

import java.io.File;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.kyupi.misc.RuntimeTools;

public class ProcessManagerTest extends TestCase {

	protected static Logger log = Logger.getLogger(ProcessManagerTest.class);

	@Test
	public void test_usecase_childStartup() throws Exception {

		ProcessManager kernel = new ProcessManager();

		SharedMemory mem = kernel.allocate(512);

		// can access memory before starting child.
		mem.acquire();
		mem.putInt(10 * 4, -42);
		mem.yield();

		// launch child process
		kernel.launch(new File(RuntimeTools.KYUPI_HOME, "kernel/test"));

		mem.acquire();
		mem.putInt(11 * 4, -23);
		mem.yield(); // each yield syncs to a mem_acquire() in the child

		mem.acquire();
		assertEquals(-42 - 23, mem.getInt(12 * 4));
		mem.yield();

		// exits child
		kernel.join();

		// can re-launch after join
		kernel.launch(new File(RuntimeTools.KYUPI_HOME, "kernel/test"));

		// just for signalling the child.
		mem.acquire();
		mem.yield();

		// same result.
		mem.acquire();
		assertEquals(-42 - 23, mem.getInt(12 * 4));
		mem.yield();
		
		kernel.join();

	}
}
