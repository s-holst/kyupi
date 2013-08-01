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

import java.util.LinkedList;

/**
 * provides a way to re-use objects to reduce object creation and GC overheads.
 * 
 * This class is used in the following way. Let Producer be a class, which wants
 * to give give out re-usable objects of the class Value.
 * 
 * The class Producer constructs a pool:
 * 
 * <pre>
 * Pool&lt;Value&gt; pool = new Pool&lt;Value&gt;() {
 * 	public Value produce() {
 * 		Value v = new Value(...);
 * 		v.setPool(pool);
 * 		return v;
 * 	}
 * };
 * </pre>
 * 
 * It then allocates objects by calling pool.alloc(). The pool will either
 * return a previously freed object or calls produce implemented above.
 * 
 * The class Value implements two methods:
 * 
 * <pre>
 * private Pool&lt;Value&gt; pool;
 * 
 * public void setPool(Pool&lt;Value&gt; pool) {
 * 	this.pool = pool;
 * }
 * 
 * public void free() {
 * 	if (pool != null)
 * 		pool.free(this);
 * }
 * </pre>
 * 
 * The receiver of a Value instance returned by a method of Producer can call
 * free as soon as the return value is not used anymore. This causes the object
 * to be added to a pool, if one is available. If no pool is defined, free
 * behaves transparently and normal garbage collection will kick in if
 * necessary.
 * 
 * @param <P>
 */
public abstract class Pool<P> {

	LinkedList<P> pool = new LinkedList<P>();

	public P alloc() {
		if (pool.isEmpty())
			return produce();
		return pool.pop();
	}

	public void free(P pooled_object) {
		pool.push(pooled_object);
	}

	public abstract P produce();
}
