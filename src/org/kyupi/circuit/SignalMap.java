/*
 * Copyright 2018 The KyuPI project contributors. See the COPYRIGHT.md file
 * at the top-level directory of this distribution.
 * This file is part of the KyuPI project. It is subject to the license terms
 * in the LICENSE.md file found in the top-level directory of this distribution.
 * No part of the KyuPI project, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.md file.
 */
package org.kyupi.circuit;

import org.kyupi.circuit.MutableCircuit.MutableCell;
import org.kyupi.misc.ArrayTools;

/**
 * is a mapping from all the edges in a Graph to a unique index.
 * 
 * This mapping can be used to index into arrays or memory with edge-related data.
 * Useful for signal values during simulation or observability/fault annotations.
 * 
 * @author Stefan
 *
 */

public class SignalMap {

	private int input_map_offset[][];
	private int input_map[];

	private int output_map_offset[][];
	
	private int edge_count;
	
	private MutableCell drivers[];
	private MutableCell receivers[];
	

	public SignalMap(MutableCircuit g) {
		
		output_map_offset = CircuitTools.allocInt(g, -1);
		input_map_offset = CircuitTools.allocInt(g, -1);
		int output_map_idx = 0;
		int next_input_map_offset = 0;
		
		for (MutableCell n: g.accessNodes()) {
			if (n == null || n.maxOut() < 0)
				continue;
			int out_count = n.maxOut() + 1;

			// Node n has at least one output edge.

			output_map_offset[n.level()][n.levelPosition()] = output_map_idx;
									
			for (int succ_idx = 0; succ_idx < out_count; succ_idx++) {
				MutableCell succ = n.out(succ_idx);
				// skip unconnected outputs
				if (succ == null) {
					output_map_idx++;
					continue;
				}

				if (input_map_offset[succ.level()][succ.levelPosition()] == -1) {
					// allocate input_map for previously unseen successor Node.
					input_map_offset[succ.level()][succ.levelPosition()] = next_input_map_offset;
					next_input_map_offset += succ.maxIn() + 1;
					input_map = ArrayTools.grow(input_map, next_input_map_offset, 4096, -1);
				}
				input_map[input_map_offset[succ.level()][succ.levelPosition()] + succ.searchInIdx(n)] = output_map_idx;
				output_map_idx++;
			}
		}
		edge_count = output_map_idx;
		
		drivers = new MutableCell[edge_count];
		receivers = new MutableCell[edge_count];
		for (MutableCell n: g.accessNodes()) {
			if (n == null)
				continue;
			int out_count = n.maxOut() + 1;
			for (int i = 0; i < out_count; i++) {
				MutableCell succ = n.out(i);
				if (succ != null) {
					drivers[idxForOutput(n, i)] = n;
				}
			}
			int in_count = n.maxIn() + 1;
			for (int i = 0; i < in_count; i++) {
				MutableCell pred = n.in(i);
				if (pred != null) {
					receivers[idxForInput(n, i)] = n;
				}
			}
		}
	}
	
	public int idxForInput(MutableCell n, int in_idx) {
		return idxForInput(n.level(), n.levelPosition(), in_idx);
	}

	public int idxForInput(int level, int pos, int in_idx) {
		return input_map[input_map_offset[level][pos] + in_idx];
	}

	public int idxForOutput(MutableCell n, int out_idx) {
		return idxForOutput(n.level(), n.levelPosition(), out_idx);
	}
		
	public int idxForOutput(int level, int pos, int out_idx) {
		return output_map_offset[level][pos] + out_idx;
	}
	
	public MutableCell driverForIdx(int idx) {
		return drivers[idx];
	}
	
	public MutableCell receiverForIdx(int idx) {
		return receivers[idx];
	}
	
	public int length() {
		return edge_count;
	}
}
