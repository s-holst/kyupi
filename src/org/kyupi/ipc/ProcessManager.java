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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class ProcessManager {

	protected static Logger log = Logger.getLogger(ProcessManager.class);

	// keep in sync with mem.c
	static final int MSG_YIELD = 100;

	private Process child;
	private InputStream istream;
	private OutputStream ostream;
	private BufferedReader estream;

	private ArrayList<SharedMemory> memories = new ArrayList<>();
	private ArrayList<Path> paths = new ArrayList<>();

	private ErrorProcessor errorThread;
	private InputProcessor inputThread;

	private boolean isLaunched = false;

	private class ErrorProcessor extends Thread {

		public void run() {
			String s;
			try {
				while ((s = estream.readLine()) != null) {
					log.info("CHILD: " + s);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private class InputProcessor extends Thread {

		public void run() {
			int c;
			try {
				while ((c = istream.read()) >= 0) {
					if (c == MSG_YIELD) {
						int handle = istream.read();
						assert handle >= 0;
						int seq = istream.read();
						assert seq >= 0;
						memories.get(handle).yieldFromChild((byte) seq);
					} else
						log.info("CHILD sent " + c);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public SharedMemory allocate(long size) throws IOException {
		if (isLaunched()) {
			log.warn("Memory " + memories.size() + "will not be available to already launched child processes.");
		}
		Path p = Files.createTempFile("SMProcs", ".mem");
		p.toFile().deleteOnExit();
		FileChannel channel = FileChannel.open(p, StandardOpenOption.READ, StandardOpenOption.WRITE);
		SharedMemory m = new SharedMemory((byte) memories.size(), size, channel, this);
		channel.close();
		memories.add(m);
		paths.add(p);
		return m;
	}

	public void launch(File command) throws IOException {
		if (isLaunched())
			throw new IllegalStateException("Process was already launched.");
		if (!command.canExecute()) {
			String absPath = command.getAbsolutePath();
			command = new File(absPath + ".exe");
			if (!command.canExecute())			
				throw new IOException("Not Executable: " + absPath + "[.exe]");
		}
		List<String> command_line = new ArrayList<>();
		command_line.add(command.getCanonicalPath());
		for (Path m : paths) {
			command_line.add(m.toString());
		}
		ProcessBuilder pb = new ProcessBuilder(command_line);
		child = pb.start();
		istream = child.getInputStream();
		ostream = child.getOutputStream();
		estream = new BufferedReader(new InputStreamReader(child.getErrorStream()));
		isLaunched = true;
		errorThread = new ErrorProcessor();
		inputThread = new InputProcessor();
		errorThread.start();
		inputThread.start();
	}

	public void join() {
		if (!isLaunched())
			throw new IllegalStateException("Process was not launched yet.");
		try {
			ostream.close();
			child.waitFor();
			isLaunched = false;
			errorThread.join();
			inputThread.join();
			estream.close();
			istream.close();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * returns 0, if child process is running or finished cleanly. returns error
	 * code, if child process died.
	 */
	public int hasDiedUnexpectedly() {
		int code = 0;
		try {
			code = child.exitValue();
		} catch (IllegalThreadStateException e) {
		}
		return code;
	}

	public boolean isLaunched() {
		return isLaunched;
	}

	// internal use

	void yieldToChild(byte handle, int seq) {
		if (!isLaunched())
			throw new IllegalStateException("Internal Error: cannot yield to an unlaunched child.");
		try {
			//log.info("yield " + handle + " to child: seq=" + seq);
			ostream.write(ProcessManager.MSG_YIELD);
			ostream.write(handle);
			ostream.write(seq);
			ostream.flush();
		} catch (IOException e) {
			throw new IllegalThreadStateException("child communication error, possibly defunct.");
		}
	}

}
