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

import org.kyupi.data.item.DataItem;
import org.kyupi.data.item.QVector;

public abstract class QVSource extends DataSource<QVector> {

	protected QVSource(int length) {
		super(length);
	}

	@Override
	protected QVector newDataItem(int length) {
		return new QVector(length);
	}

	public static QVSource from(DataSource<? extends DataItem<?>> source) {
		return from(source.length(), source);
	}

	public static QVSource from(int length, Iterable<? extends DataItem<?>> source) {
		final Iterable<? extends DataItem<?>> source_ = source;
		return new QVSource(length) {

			private Iterable<? extends DataItem<?>> source = source_;
			private Iterator<? extends DataItem<?>> iterator = source.iterator();
			private DataItem<?> staged;
			private int stagedIdx;

			@Override
			public void reset() {
				iterator = source.iterator();
				if (staged != null) {
					staged.free();
					staged = null;
				}
				stagedIdx = 0;
			}

			@Override
			protected QVector compute() {
				if (staged == null) {
					if (!iterator.hasNext())
						return null;
					staged = iterator.next();
					stagedIdx = 0;
				}
				QVector item = pool.alloc();
				staged.copyTo(stagedIdx++, item);
				if (stagedIdx >= staged.slots()) {
					staged.free();
					staged = null;
				}
				return item;
			}
		};
	}
}
