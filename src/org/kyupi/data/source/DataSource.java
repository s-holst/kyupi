/*
 * Copyright 2013 The KyuPI project contributors. See the COPYRIGHT.md file
 * at the top-level directory of this distribution.
 * This file is part of the KyuPI project. It is subject to the license terms
 * in the LICENSE.md file found in the top-level directory of this distribution.
 * No part of the KyuPI project, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.md file.
 */
package org.kyupi.data.source;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.kyupi.data.item.DataItem;
import org.kyupi.misc.Pool;

public abstract class DataSource<T extends DataItem<T>> implements Iterable<T>, Iterator<T> {

	protected static Logger log = Logger.getLogger(DataSource.class);

	private final int length;
	
	protected DataSource(int length) {
		if (length <= 0)
			throw new IllegalArgumentException("length must be > 0.");
		this.length = length;
	}
	
	public int length() {
		return length;
	}
	
	public abstract void reset();

	protected abstract T compute();

	/*
	 * Iterable interface implementation
	 */

	public final Iterator<T> iterator() {
		reset();
		return this;
	}

	/*
	 * Iterator interface implementation
	 */

	private T next_item;

	@Override
	public final boolean hasNext() {
		if (next_item == null)
			next_item = compute();
		return next_item != null;
	}

	@Override
	public final T next() {
		if (!hasNext())
			throw new NoSuchElementException();
		T item = next_item;
		next_item = null;
		return item;
	}

	@Override
	public final void remove() {
		throw new UnsupportedOperationException();
	}

	/*
	 * T pool
	 */

	protected T newDataItem(int length) {
		throw new RuntimeException("can not use pool without implementing newDataItem(int)");
	}
	

	protected Pool<T> pool = new Pool<T>() {
		public T produce() {
			T v = newDataItem(length);
			v.setPool(pool);
			return v;
		}
	};
}
