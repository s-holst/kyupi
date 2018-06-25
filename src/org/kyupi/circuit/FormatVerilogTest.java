package org.kyupi.circuit;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.junit.Test;
import org.kyupi.misc.RuntimeTools;

public class FormatVerilogTest {

	@Test
	public void test() throws Exception {
		MutableCircuit c = FormatVerilog.load(new FileInputStream(new File(RuntimeTools.KYUPI_HOME, "testdata/SAED90/b13.v")), new LibrarySAED90());
		assertNotNull(c);
		c.printStats();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		FormatVerilog.save(os, c);
		//FormatVerilog.save(new FileOutputStream("foo.v"), c);
		InputStream is = new ByteArrayInputStream(os.toByteArray());
		MutableCircuit c2 = FormatVerilog.load(is, new LibrarySAED90());
		c2.printStats();
		assertTrue(c.equals(c2));
	}

}
