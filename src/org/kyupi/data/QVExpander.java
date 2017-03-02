package org.kyupi.data;

import org.kyupi.data.item.QVector;
import org.kyupi.data.source.QVSource;

/**
 * generates new vectors with data from the given source arranged and expanded
 * according to a given mapping.
 * 
 * The mapping is a 2D array of integers int[v][l]. 'v' is the number of vectors
 * each source vector is expanded to. 'l' length of the expanded vectors. All
 * lengths in the map must be equal. For each source vector the expander cycles
 * through all v entries in the map and returns v new vectors of length l, each
 * containing vector data according to the corresponding line in the mapping.
 * 
 * Each entry in a line is an integer referencing a bit position in the source
 * vector. A negative or too large integer will cause a DC entry in the expanded
 * vector.
 * 
 * @author Stefan
 *
 */
public class QVExpander extends QVSource {

	private QVSource source;
	int mapidx;
	int[][] map;

	public QVExpander(QVSource source, int[][] map) {
		super(map[0].length);
		this.source = source;
		this.map = map;
	}

	@Override
	public void reset() {
		source.reset();
		current = null;
	}
	
	QVector current;

	@Override
	protected QVector compute() {
		if (current == null) {
			if (!source.hasNext())
				return null;
			current = source.next();
			mapidx = 0;
		}
		QVector ret = pool.alloc();
		current.shuffleTo(map[mapidx], ret);
		mapidx++;
		if (mapidx >= map.length)
			current = null;
		return ret;
	}

}
