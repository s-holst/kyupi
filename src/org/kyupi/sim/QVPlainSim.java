package org.kyupi.sim;

import org.kyupi.data.item.QVector;
import org.kyupi.data.source.QBSource;
import org.kyupi.data.source.QVSource;
import org.kyupi.graph.Graph;

public class QVPlainSim extends QVSource {

	private QVSource s;
	
	public QVPlainSim(Graph netlist, QVSource inputData) {
		super(inputData.length());
		s = QVSource.from(new QBPlainSim(netlist, QBSource.from(inputData)));
	}
	
	@Override
	public void reset() {
		s.reset();
	}

	@Override
	protected QVector compute() {
		if (s.hasNext())
			return s.next();
		else
			return null;
	}

}
