/*
 * Copyright 2013 The KyuPI project contributors. See the COPYRIGHT.md file
 * at the top-level directory of this distribution.
 * This file is part of the KyuPI project. It is subject to the license terms
 * in the LICENSE.md file found in the top-level directory of this distribution.
 * No part of the KyuPI project, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.md file.
 */
package org.kyupi.data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.kyupi.data.item.QVector;
import org.kyupi.data.parser.Stil;
import org.kyupi.data.parser.Stil.Operation;
import org.kyupi.data.source.QVSource;
import org.kyupi.graph.Graph;
import org.kyupi.graph.Graph.Node;
import org.kyupi.misc.FileTools;

public class FormatStil {

	protected static Logger log = Logger.getLogger(FormatStil.class);

	private ArrayList<QVector> stimuli = new ArrayList<>();
	private ArrayList<QVector> responses = new ArrayList<>();

	private int length;
	
	private int[][] scan2intf;

	public FormatStil(File stil_file, Graph g) throws IOException {
		InputStream is = FileTools.fileOpen(stil_file);
		Stil data = Stil.load(is, stil_file);

		length = g.accessInterface().length;

		// debug output
		//log.debug("pi: " + data.primary_inputs);
		//log.debug("po: " + data.primary_outputs);
		int nchains = data.chain_names.size();
		for (int i = 0; i < nchains; i++) {
			// reverse the order of scan cells to fit with shift-vectors.
			ArrayList<String> sc = data.chain_cells.get(i);
			ArrayList<String> sc2 = new ArrayList<>();
			for (int j = sc.size() - 1; j >= 0; j--) {
				sc2.add(sc.get(j));
			}
			data.chain_cells.set(i, sc2);
			//log.debug("chain " + data.chain_names.get(i) + ": " + data.chain_cells.get(i));
		}

		// find clock input
		int clock_port = data.primary_inputs.indexOf(data.clock);
		//log.debug("Clock is on port: " + clock_port);

		// cross-reference to circuit interface
		HashMap<String, Integer> intf = new HashMap<>();
		//ArrayList<Node> intf_names = new ArrayList<>();
		for (Node inode : g.accessInterface()) {
			//intf_names.add(inode);
			if (inode != null) {
				intf.put(inode.queryName(), inode.intfPosition());
				//log.debug("intf " + inode.queryName() + " pos " + inode.position());
			}
		}
		//log.debug("intf: " + intf_names);

		int pi2intf[] = crossRef(data.primary_inputs, intf);
		int po2intf[] = crossRef(data.primary_outputs, intf);
		scan2intf = new int[nchains][];
		for (int chain = 0; chain < nchains; chain++) {
			scan2intf[chain] = crossRef(data.chain_cells.get(chain), intf);
		}

		// assemble patterns and responses for all capture cycles. FIXME
		// multi-scan
		String scanout = null;
		String scanin = null;
		String pi = null;
		String po = null;
		int nops = data.ops.size();
		for (int i = 0; i < nops; i++) {
			Operation op = data.ops.get(i);
			if (op.scanout != null) {
				scanout = op.scanout;
				if (scanin != null && pi != null && po != null) {
					// assemble new pattern
					QVector v = new QVector(length);
					QVector r = new QVector(length);
					setValues(v, pi, pi2intf);
					setValues(v, scanin, scan2intf[0]);
					setValues(r, po, po2intf);
					setValues(r, scanout, scan2intf[0]);

					//log.debug("capture @ " + i + ":\tpi=" + pi + " po=" + po + " si=" + scanin + " so=" + scanout);
					//log.debug("  test=" + v);
					//log.debug("  resp=" + r);
					stimuli.add(v);
					responses.add(r);

					scanout = null;
					scanin = null;
					po = null;
					pi = null;
				}
			}
			if (op.scanin != null) {
				scanin = op.scanin;
			}
			if (op.pi != null && op.po != null && op.pi.charAt(clock_port) != '0') {
				po = op.po;
				pi = op.pi;
			}

		}

	}

	public FormatStil(String string, Graph g) throws IOException {
		this(new File(string), g);
	}

	private void setValues(QVector dest, String src, int map[]) {
		if (src == null) {
			log.error("No values to set! (src=null)");
			return;
		}
		int l = src.length();
		for (int i = 0; i < l; i++) {
			if (map[i] < 0)
				continue;
			char vc = src.charAt(i);
			switch (vc) {
			case 'P':
			case 'H':
				vc = '1';
				break;
			case 'L':
				vc = '0';
				break;
			}
			dest.setValue(map[i], vc);
		}
	}

	private int[] crossRef(ArrayList<String> names, HashMap<String, Integer> intf) {
		int name_count = names.size();
		int map[] = new int[name_count];
		Arrays.fill(map, -1);
		for (int i = 0; i < name_count; i++) {
			String pi_name = names.get(i);
			String pi_name_orig = pi_name;
			while (!intf.containsKey(pi_name)) {
				if (pi_name.endsWith(".SI")) {
					pi_name = pi_name.substring(0, pi_name.length() - 3);
				} else if (pi_name.contains(".")) {
					pi_name = pi_name.substring(pi_name.indexOf(".") + 1);
				} else
					break;
				// log.debug("Trying alternative: " + pi_name);
			}
			if (intf.containsKey(pi_name)) {
				map[i] = intf.get(pi_name);
			} else {
				log.warn("Not found in graph: " + pi_name_orig);
			}
		}
		return map;
	}

	public QVSource getStimuliSource() {
		return QVSource.from(length, stimuli);
	}

	public QVSource getResponsesSource() {
		return QVSource.from(length, responses);
	}

	public ArrayList<QVector> getStimuliArray() {
		return stimuli;
	}

	public ArrayList<QVector> getResponsesArray() {
		return responses;
	}

	public int[] getScanMap() {
		return scan2intf[0];
	}
}
