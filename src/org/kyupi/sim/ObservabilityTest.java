package org.kyupi.sim;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.kyupi.circuit.CircuitTools;
import org.kyupi.circuit.LevelizedCircuit;
import org.kyupi.circuit.LevelizedCircuit.LevelizedCell;
import org.kyupi.circuit.MutableCircuit;
import org.kyupi.data.item.QBlock;
import org.kyupi.data.item.QVector;
import org.kyupi.data.source.QBSource;

public class ObservabilityTest {

	protected static Logger log = Logger.getLogger(ObservabilityTest.class);

	@Test
	public void test() {
		MutableCircuit g = CircuitTools.parseBench("INPUT(a) OUTPUT(z) z=DFF(a)");
		LevelizedCircuit lc = new LevelizedCircuit(g);
		//log.info("Graph=\n" + g);
		Observability obs = new Observability(lc);
		
		ArrayList<QVector> patterns = new ArrayList<QVector>();
		patterns.add(new QVector("000"));
		patterns.add(new QVector("100"));
		patterns.add(new QVector("001"));
		patterns.add(new QVector("001"));
		patterns.add(new QVector("101"));
		
		QBSource p = QBSource.from(3, patterns);
		QBlock blk = p.next();

		obs.loadInputsFrom(blk);
		
		LevelizedCell a = lc.searchCellByName("a");
		LevelizedCell z = lc.searchCellByName("z_");
		assertNotNull(a);
		assertNotNull(z);
		
		int idx = a.outputSignalAt(0);
		assertEquals(-1L, obs.getObservability(idx));		
	}

}
