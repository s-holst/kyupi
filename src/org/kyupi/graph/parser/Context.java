/*
 * Copyright 2013 The KyuPI project contributors. See the COPYRIGHT.md file
 * at the top-level directory of this distribution.
 * This file is part of the KyuPI project. It is subject to the license terms
 * in the LICENSE.md file found in the top-level directory of this distribution.
 * No part of the KyuPI project, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.md file.
 */
package org.kyupi.graph.parser;

import java.util.ArrayList;
import java.util.HashMap;

import org.kyupi.circuit.Graph;

public class Context {

	
	private HashMap<String,Graph> entities = new HashMap<>();
	
	private ArrayList<Graph> units = new ArrayList<>();
	
	public void addAsEntity(String id, Graph g) {
		entities.put(id, g);
	}
	
	public Graph getEntity(String id) {
		return entities.get(id);
	}
	
	public void markImplemented(Graph g) {
		units.add(g);
	}
	
	public ArrayList<Graph> getUnits() {
		return units;
	}
}
