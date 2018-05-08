/*
 * Copyright 2013 The KyuPI project contributors. See the COPYRIGHT.md file
 * at the top-level directory of this distribution.
 * This file is part of the KyuPI project. It is subject to the license terms
 * in the LICENSE.md file found in the top-level directory of this distribution.
 * No part of the KyuPI project, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.md file.
 */
package org.kyupi.circuit.parser;

import java.util.ArrayList;
import java.util.HashMap;

import org.kyupi.circuit.MutableCircuit;

public class Context {

	
	private HashMap<String,MutableCircuit> entities = new HashMap<>();
	
	private ArrayList<MutableCircuit> units = new ArrayList<>();
	
	public void addAsEntity(String id, MutableCircuit g) {
		entities.put(id, g);
	}
	
	public MutableCircuit getEntity(String id) {
		return entities.get(id);
	}
	
	public void markImplemented(MutableCircuit g) {
		units.add(g);
	}
	
	public ArrayList<MutableCircuit> getUnits() {
		return units;
	}
}
