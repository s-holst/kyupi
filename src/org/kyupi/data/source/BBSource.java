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

import org.kyupi.data.item.BBlock;
import org.kyupi.data.item.DataItem;

/**
 * reformats the data from any iterable DataItem source to binary vector blocks
 * of specified vector length.
 */
public abstract class BBSource extends DataSource<BBlock> {

	protected BBSource(int length) {
		super(length);
	}

	@Override
	protected final BBlock newDataItem(int length) {
		return new BBlock(length);
	}

	public static BBSource from(DataSource<? extends DataItem<?>> source) {
		return from(source.length(), source);
	}

	public static BBSource from(int length, Iterable<? extends DataItem<?>> source) {
		final Iterable<? extends DataItem<?>> source_ = source;
		return new BBSource(length) {

			private Iterable<? extends DataItem<?>> source = source_;
			private Iterator<? extends DataItem<?>> iterator = source.iterator();

			@Override
			public void reset() {
				iterator = source.iterator();
			}

			@Override
			protected BBlock compute() {
				if (!iterator.hasNext())
					return null;
				BBlock b = pool.alloc();
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
	
	public static BBSource random(int length, int seed) {
		final int seed_ = seed;
		return new BBSource(length) {
			private Random rand = new Random(seed_);
			
			@Override
			public void reset() {
				rand = new Random(seed_);
			}
			
			@Override
			protected BBlock compute() {
				BBlock b = pool.alloc();
				for (int i = 0; i < b.length(); i++) {
					b.set(i, rand.nextLong());
				}
				return b;
			}
		};
	}

}
