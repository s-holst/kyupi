/*
 * Copyright 2013 The KyuPI project contributors. See the COPYRIGHT.md file
 * at the top-level directory of this distribution.
 * This file is part of the KyuPI project. It is subject to the license terms
 * in the LICENSE.md file found in the top-level directory of this distribution.
 * No part of the KyuPI project, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.md file.
 */
package org.kyupi.data.item;

import org.apache.log4j.Logger;
import org.kyupi.misc.Pool;

/**
 * is the abstract base class for pooled, two-dimensional data items.
 * 
 * DataItems are generally mutable to allow fast, in-place data manipulation.
 * Producer of DataItem instances are advised to manage a pool of items with
 * compatible dimensions. See Pool. User of DataItem instances are advised to
 * call free() right before losing the last reference to this item.
 * 
 * The ownership of the data contained in a DataItem is transferred to the user.
 * He may manipulate the data at will and/or propagate the DataItem to
 * sub-users. The ownership of the container (instance of DataItem) remains with
 * the producer, who manages the pool.
 * 
 * A DataItem has two dimensions: length and slots. These dimensions are
 * immutable. "length" is the number of elements in a vector. "slots" are the
 * number of vectors stored in a DataItem. The slots value closely relates to
 * the internal representation of a DataItem. Currently, DataItems with slots=1
 * and slots=64 are available.
 */
public abstract class DataItem<T> {

	protected static Logger log = Logger.getLogger(DataItem.class);

	protected int length;

	protected int slots;

	public int length() {
		return length;
	}

	public int slots() {
		return slots;
	}

	public abstract void copyTo(int slot, BVector dest);

	public abstract void copyTo(int slot, QVector dest);

	public abstract void copyTo(long mask, BBlock dest);

	public abstract void copyTo(long mask, QBlock dest);

	/*
	 * pooled object
	 */

	protected Pool<T> pool;

	public void setPool(Pool<T> pool) {
		this.pool = pool;
	}

	public void free() {
		if (pool != null) {
			pool_free_this();
		}
	}

	protected abstract void pool_free_this();

}
