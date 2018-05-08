package org.kyupi.sim;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;
import org.kyupi.circuit.Graph;
import org.kyupi.circuit.GraphTools;
import org.kyupi.data.item.QVector;
import org.kyupi.data.source.QVSource;

public class QVPlainSimTest {

	@Test
	public void test() {
		Graph g = GraphTools.benchToGraph("INPUT(a) OUTPUT(z1) OUTPUT(z2) z1=DFF(a) z2=DFF(z1)");
		//log.info("Graph=\n" + g);
		ArrayList<QVector> v = new ArrayList<>();
		v.add(new QVector("1--00"));
		QVSource pat = QVSource.from(5, v);
		QVSource sim = new QVPlainSim(g, pat);
		assertEquals("10010", sim.next().toString());
		
	}

}
