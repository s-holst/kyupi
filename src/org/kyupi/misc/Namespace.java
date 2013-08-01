/*
 * Copyright 2013 The KyuPI project contributors. See the COPYRIGHT.md file
 * at the top-level directory of this distribution.
 * This file is part of the KyuPI project. It is subject to the license terms
 * in the LICENSE.md file found in the top-level directory of this distribution.
 * No part of the KyuPI project, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.md file.
 */
package org.kyupi.misc;

import java.util.HashMap;

/**
 * provides a mapping between strings and integer identifiers.
 *
 */
public class Namespace {

	private String[] names = new String[0];
	
	// TODO: implement memory-efficient version.
	private HashMap<String, Integer> name_id = new HashMap<String, Integer>();
	
	private int last_id;
	
	
	public int idFor(String name) {
		if(contains(name)) {
			return name_id.get(name);
		}
		name_id.put(name, last_id);
		names = (String[]) ArrayTools.grow(names, String.class, last_id+1);
		names[last_id] = name;
		last_id++;
		return last_id-1;
	}
	
	public String nameFor(int id) {
		return names[id];
	}

	public boolean contains(String name) {
		return name_id.containsKey(name);
	}
	
}
