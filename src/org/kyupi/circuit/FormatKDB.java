/*
 * Copyright 2013 The KyuPI project contributors. See the COPYRIGHT.md file
 * at the top-level directory of this distribution.
 * This file is part of the KyuPI project. It is subject to the license terms
 * in the LICENSE.md file found in the top-level directory of this distribution.
 * No part of the KyuPI project, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.md file.
 */
package org.kyupi.circuit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.kyupi.circuit.MutableCircuit.MutableCell;

/**
 * loads from the Kyutech E322 file format.
 * 
 * 
 * 
 * 
 */
class FormatKDB {

	private static Logger log = Logger.getLogger(FormatKDB.class);

	private static HashMap<Integer, Integer> map = new HashMap<Integer, Integer>() {
		private static final long serialVersionUID = 1L;
		{
			// definitions from De_Design.h
			put(-4,Library.TYPE_XNOR);
			put(-3,Library.TYPE_NOT);
			put(-2,Library.TYPE_NOR);
			put(-1,Library.TYPE_NAND);
			put(0,Library.TYPE_BUF | Library.FLAG_INPUT); // primary input
			put(1,Library.TYPE_OR);
			put(2,Library.TYPE_AND);
			put(3,Library.TYPE_BUF | Library.FLAG_PSEUDO); // fanout branch line
			put(4,Library.TYPE_BUF | Library.FLAG_OUTPUT); // primary output
			put(5,Library.TYPE_XOR);
			
			put(10,Library.TYPE_BUF | Library.FLAG_INPUT | Library.FLAG_PSEUDO); // PPI
			put(11,Library.TYPE_BUF | Library.FLAG_OUTPUT | Library.FLAG_PSEUDO); // PPO
			
			put(12,Library.TYPE_BUF); // BUFFER
			put(13,Library.TYPE_BUF | Library.FLAG_PSEUDO); // hierarchy bound
			
			put(14,Library.TYPE_CONST0);
			put(15,Library.TYPE_CONST1);

			put(16,Library.TYPE_BUF | Library.FLAG_PSEUDO); // input side floating pin
			put(17,Library.TYPE_BUF | Library.FLAG_PSEUDO); // output side floating pin

			put(20,Library.TYPE_BUF | Library.FLAG_PSEUDO); // input side floating pin (no scan or latch)
			put(21,Library.TYPE_BUF | Library.FLAG_PSEUDO); // output side floating pin  (no scan or latch)

			put(23,Library.TYPE_BUF | Library.FLAG_SEQUENTIAL); // a latch
		}
	};
	
	private static int[] getLine(BufferedReader bfr) throws IOException {
		String line = bfr.readLine();
		if (line == null)
			return null;
		String[] sarr = line.split("\\h+");
		ArrayList<Integer> narr = new ArrayList<>();
		for (int i = 0; i < sarr.length; i++) {
			//log.info("s '" + sarr[i] + "'");
			if (sarr[i].length() == 0)
				continue;
			narr.add(Integer.parseInt(sarr[i]));
		}
		int[] nnarr = new int[narr.size()];
		for (int i = 0; i < narr.size(); i++) {
			nnarr[i] = narr.get(i);
		}
		return nnarr;
	}
	
	static MutableCircuit load(InputStream is) throws IOException {
		MutableCircuit c = new MutableCircuit(new Library());
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader bfr = new BufferedReader(isr);
		int[] data;
		int line = 0;
		
		data = getLine(bfr);
		line++;
		if (data==null || data.length < 4)
			throw new IOException("Line " + line + " : Premature end of file");
		int signalCount = data[0];
		int outputCount = data[1];
		int inputCount = data[2];
		int fanIOCount = data[3];
		
		MutableCell[] nodes = new MutableCell[signalCount+1];
		int[] faninCount = new int[signalCount+1];
		int[] faninPtr = new int[signalCount+1];
		int[] fanoutCount = new int[signalCount+1];
		int[] fanoutPtr = new int[signalCount+1];
		
		int intfIdx = 0;
		
		log.info("Stats Signals " + signalCount + " Outputs " + outputCount + " Inputs " + inputCount + " FanIO " + fanIOCount);
		
		for (int i = 0; i < signalCount; i++) {
			data = getLine(bfr);
			line++;
			if (data==null || data.length < 7)
				throw new IOException("Line " + line + " : Premature end of file");
			int signal = data[0];
			int type = data[1];
			faninCount[signal] = data[2];
			faninPtr[signal] = data[3];
			//int signalName = data[4];
			fanoutCount[signal] = data[5];
			fanoutPtr[signal] = data[6];
			if (!map.containsKey(type)) {
				throw new IOException("Line " + line + " : Unknown gate type " + type);
			}
			nodes[signal] = c.new MutableCell("id"+signal, map.get(type));
			if (c.library().isPrimary(nodes[signal].type())) {
				nodes[signal].setIntfPosition(intfIdx++);
			}
			if (c.library().isSequential(nodes[signal].type())) {
				nodes[signal].setIntfPosition(intfIdx++);
			}
		}

		int[] fanIOList = new int[fanIOCount+1];
		for (int i = 0; i < fanIOCount; i++) {
			data = getLine(bfr);
			line++;
			if (data==null || data.length < 2)
				throw new IOException("Line " + line + " : Premature end of file");
			int listnr = data[0];
			int signal = data[1];
			fanIOList[listnr] = signal;
		}
		
		while((data = getLine(bfr)) != null) {
			log.info("Additional data " + data);
		}
		
		for (int i = 0; i < signalCount; i++) {
			MutableCell n = nodes[i];
			if (fanoutCount[i] == 1) {
				c.connect(n, -1, nodes[fanoutPtr[i]], -1);
			} else {
				for (int j = 0; j < fanoutCount[i]; j++) {
					c.connect(n, -1, nodes[fanIOList[j+fanoutPtr[i]]], -1);
				}
			}
		}

		for (MutableCell n :  c.accessLevel(0)) {
			log.info("LEVEL0 node: " + n);
		}
		c.strip();
		return c;
	}

}
