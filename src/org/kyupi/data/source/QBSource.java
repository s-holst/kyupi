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
import java.util.Random;

import org.kyupi.data.item.DataItem;
import org.kyupi.data.item.QBlock;

public abstract class QBSource extends DataSource<QBlock> {

	protected QBSource(int length) {
		super(length);
	}

	@Override
	protected QBlock newDataItem(int length) {
		return new QBlock(length);
	}

	public static QBSource from(DataSource<? extends DataItem<?>> source) {
		return from(source.length(), source);
	}

	public static QBSource from(int length, Iterable<? extends DataItem<?>> source) {
		final Iterable<? extends DataItem<?>> source_ = source;
		return new QBSource(length) {

			private Iterable<? extends DataItem<?>> source = source_;
			private Iterator<? extends DataItem<?>> iterator = source.iterator();

			@Override
			public void reset() {
				iterator = source.iterator();
			}

			@Override
			protected QBlock compute() {
				if (!iterator.hasNext())
					return null;
				QBlock b = pool.alloc();
				int fill = 0;
				while (fill < 64) {
					DataItem<?> item = iterator.next();
					if (item.slots() == 1) {
						item.copyTo(1L << fill, b);
						fill++;
					} else if (item.slots() == 64) {
						item.copyTo(-1L, b);
						fill += 64;
					} else
						throw new RuntimeException("Incompatible DataItem");
					if (!iterator.hasNext()) {
						if (fill < 64)
							item.copyTo(-1L << fill, b);
						fill = 64;
					}
					item.free();
				}
				return b;
			}
		};
	}

	public static QBSource random(int length, int seed) {
		final int seed_ = seed;
		return new QBSource(length) {
			private Random rand = new Random(seed_);
			
			@Override
			public void reset() {
				rand = new Random(seed_);
			}
			
			@Override
			protected QBlock compute() {
				QBlock b = pool.alloc();
				for (int i = 0; i < b.length(); i++) {
					b.setV(i, rand.nextLong());
					b.setC(i, rand.nextLong());
				}
				return b;
			}
		};
	}
}
