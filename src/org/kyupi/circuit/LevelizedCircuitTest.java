package org.kyupi.circuit;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.kyupi.misc.RuntimeTools;

public class LevelizedCircuitTest {

	@Test
	public void test() throws IOException {

		Library lib = new Library();

		MutableCircuit c = CircuitTools.loadCircuit(new File(RuntimeTools.KYUPI_HOME, "testdata/c17.isc"), lib);
		LevelizedCircuit lc = new LevelizedCircuit(c);
		assertEquals(13, lc.size());
		assertEquals(7, lc.width());
	}

}
