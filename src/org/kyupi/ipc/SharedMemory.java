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

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

public class SharedMemory {

	protected static Logger log = Logger.getLogger(SharedMemory.class);

	// keep in sync with mem.c
	private static final int FILE_MAGIC = 0x23232323;
	private static final int ENDIAN_MAGIC = 0x76543210;

	// keep in sync with mem.h
	private static final int HEADER_SIZE = 4096;
	private static final int FILE_MAGIC_POS = 0;
	private static final int ENDIAN_MAGIC_POS = 4;
	private static final int SEQUENCE_POS = 8;
	private static final int HANDLE_POS = 24;

	private MappedByteBuffer mem;

	private byte handle;

	private ProcessManager process;

	Lock yieldStateLock = new ReentrantLock();
	Condition justReceivedYieldFromChild = yieldStateLock.newCondition();
	private volatile boolean haveYieldFromChild = false;
	private volatile byte childYieldSequence = 1;

	ReentrantLock possessionLock = new ReentrantLock();

	public SharedMemory(byte handle, long bytes, FileChannel channel, ProcessManager process) throws IOException {
		this.handle = handle;
		this.process = process;
		mem = channel.map(FileChannel.MapMode.READ_WRITE, 0, bytes + HEADER_SIZE);
		mem.order(ByteOrder.LITTLE_ENDIAN);
		mem.putInt(FILE_MAGIC_POS, FILE_MAGIC);
		mem.putInt(ENDIAN_MAGIC_POS, ENDIAN_MAGIC);
		mem.putInt(SEQUENCE_POS, childYieldSequence);
		mem.put(HANDLE_POS, handle);
	}

	public void acquire() {

		if (possessionLock.isHeldByCurrentThread()) {
			throw new IllegalMonitorStateException("Need to call yield() before acquire().");
		}

		possessionLock.lock();

		if (!process.isLaunched())
			return;
		
		// ensure that child process has yielded the memory to us.
		yieldStateLock.lock();
		while (!haveYieldFromChild) {
			try {
				if (!justReceivedYieldFromChild.await(1, TimeUnit.SECONDS)) {
					int code = process.hasDiedUnexpectedly();
					if (code != 0)
						throw new IllegalThreadStateException("child process died on me with exit code: " + code);
					log.warn("still waiting for yield of memory " + handle + " from child ...");
				}
			} catch (InterruptedException e) {
			}
		}
		byte expectedMemSeq = childYieldSequence;
		yieldStateLock.unlock();

		// busy wait on up-to-date view on the shared memory
		int polling = 0;
		for (;;) {
			if (expectedMemSeq == (byte) mem.getInt(SEQUENCE_POS))
				break;
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
			if (++polling % 1000 == 0)
				log.warn("still waiting for consistent memory " + handle + " ...");
		}

		// current thread now has possessionLock.
	}

	public void yield() {
		if (!possessionLock.isHeldByCurrentThread()) {
			throw new IllegalMonitorStateException("Only the thread that called acquire() on this memory can also yield().");
		}

		if (process.isLaunched()) {
			yieldStateLock.lock();
			haveYieldFromChild = false;
			yieldStateLock.unlock();

			// yield memory possession to child
			int seq = mem.getInt(SEQUENCE_POS) + 1;
			mem.putInt(SEQUENCE_POS, seq);
			process.yieldToChild(handle, seq);
		}

		possessionLock.unlock();
	}

	public void putInt(int index, int value) {
		if (!possessionLock.isHeldByCurrentThread()) {
			throw new IllegalMonitorStateException("Before any access, acquire() must be called.");
		}
		mem.putInt(index + HEADER_SIZE, value);
	}
	
	public void putByte(int index, byte value) {
		if (!possessionLock.isHeldByCurrentThread()) {
			throw new IllegalMonitorStateException("Before any access, acquire() must be called.");
		}
		mem.put(index + HEADER_SIZE, value);
	}
	
	public void putFloat(int index, float value) {
		if (!possessionLock.isHeldByCurrentThread()) {
			throw new IllegalMonitorStateException("Before any access, acquire() must be called.");
		}
		mem.putFloat(index + HEADER_SIZE, value);
	}
	
	public int getInt(int index) {
		if (!possessionLock.isHeldByCurrentThread()) {
			throw new IllegalMonitorStateException("Before any access, acquire() must be called.");
		}
		return mem.getInt(index + HEADER_SIZE);
	}
	
	public float getFloat(int index) {
		if (!possessionLock.isHeldByCurrentThread()) {
			throw new IllegalMonitorStateException("Before any access, acquire() must be called.");
		}
		return mem.getFloat(index + HEADER_SIZE);
	}
	
	public int getByte(int index) {
		if (!possessionLock.isHeldByCurrentThread()) {
			throw new IllegalMonitorStateException("Before any access, acquire() must be called.");
		}
		return mem.get(index + HEADER_SIZE);
	}

	// internal use

	void yieldFromChild(byte seq) {
		//log.info("yield from child on " + handle + ": seq=" + seq);
		yieldStateLock.lock();
		haveYieldFromChild = true;
		childYieldSequence = seq;
		justReceivedYieldFromChild.signal();
		yieldStateLock.unlock();
	}
}
