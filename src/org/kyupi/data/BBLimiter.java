package org.kyupi.data;

import org.kyupi.data.item.BBlock;
import org.kyupi.data.source.BBSource;

public class BBLimiter extends BBSource {

	private int blockCount;
	private int blockIndex;
	private BBSource source;
	
	public BBLimiter(BBSource source, int blockCount) {
		super(source.length());
		this.blockCount = blockCount;
		this.source = source;
		this.blockIndex = 0;
	}
	
	@Override
	public void reset() {
		source.reset();
		blockIndex = 0;
	}

	@Override
	protected BBlock compute() {
		if (blockIndex < blockCount) {
			blockIndex++;
			return source.hasNext() ? source.next() : null;
		}
		return null;
	}

}
