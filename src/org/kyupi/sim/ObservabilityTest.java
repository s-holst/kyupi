package org.kyupi.sim;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.kyupi.data.item.QBlock;
import org.kyupi.data.item.QVector;
import org.kyupi.data.source.QBSource;
import org.kyupi.graph.Graph;
import org.kyupi.graph.Graph.Node;
import org.kyupi.graph.GraphTools;

public class ObservabilityTest {

	protected static Logger log = Logger.getLogger(ObservabilityTest.class);

	@Test
	public void test() {
		Graph g = GraphTools.benchToGraph("INPUT(a) OUTPUT(z) z=DFF(a)");
		log.info("Graph=\n" + g);
		Observability obs = new Observability(g);
		
		ArrayList<QVector> patterns = new ArrayList<QVector>();
		patterns.add(new QVector("000"));
		patterns.add(new QVector("100"));
		patterns.add(new QVector("001"));
		patterns.add(new QVector("001"));
		patterns.add(new QVector("101"));
		
		QBSource p = QBSource.from(3, patterns);
		QBlock blk = p.next();

		obs.loadInputsFrom(blk);
		
		Node a = g.searchNode("a");
		Node z = g.searchNode("z_");
		assertNotNull(a);
		assertNotNull(z);
		
		assertEquals(-1L, obs.getObservability(a));		
	}

}
