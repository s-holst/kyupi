package org.kyupi.graph;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;

import org.junit.Test;

public class FormatVerilogTest {

	@Test
	public void test() throws Exception {
		Graph c = FormatVerilog.load(new FileInputStream("testdata/SAED90/b13.v"), new LibrarySAED());
		assertNotNull(c);
		c.printStats();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		FormatVerilog.save(os, c);
		//FormatVerilog.save(new FileOutputStream("foo.v"), c);
		InputStream is = new ByteArrayInputStream(os.toByteArray());
		Graph c2 = FormatVerilog.load(is, new LibrarySAED());
		c2.printStats();
		assertTrue(c.equals(c2));
	}

}
