package org.kyupi.data;

import org.kyupi.data.item.QBlock;
import org.kyupi.data.source.QBSource;

public class QBLimiter extends QBSource {

	private int blockCount;
	private int blockIndex;
	private QBSource source;
	
	public QBLimiter(QBSource source, int blockCount) {
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
	protected QBlock compute() {
		if (blockIndex < blockCount) {
			blockIndex++;
			return source.hasNext() ? source.next() : null;
		}
		return null;
	}

}
