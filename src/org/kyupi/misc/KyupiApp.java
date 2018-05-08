/*
 * Copyright 2013-2017 The KyuPI project contributors. See the COPYRIGHT.md file
 * at the top-level directory of this distribution.
 * This file is part of the KyuPI project. It is subject to the license terms
 * in the LICENSE.md file found in the top-level directory of this distribution.
 * No part of the KyuPI project, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.md file.
 */
package org.kyupi.misc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Callable;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.kyupi.circuit.Graph;
import org.kyupi.circuit.GraphTools;
import org.kyupi.circuit.Library;
import org.kyupi.circuit.LibraryNangate;
import org.kyupi.circuit.LibrarySAED;
import org.kyupi.data.FormatStil;
import org.kyupi.data.item.QVector;

import junit.framework.TestCase;

/**
 * handles common command line options and provides utilities to build main
 * applications.
 * 
 * To build a new application, subclass KyupiApp and add a main function:
 * 
 * <pre>
 * public class MyApp extends KyupiApp {
 * 	public static void main(String[] args) throws Exception {
 * 		new MyApp().setArgs(args).call();
 * 	}
 * 
 * 	public Void call() throws Exception {
 * 		printWelcome();
 * 		// TODO: Your code goes here
 * 		printGoodbye();
 * 		return null;
 * 	}
 * }
 * </pre>
 * 
 * At the same time, any App extends TestCase and public test*() methods can be
 * added that will be run by JUnit.
 */
public abstract class KyupiApp extends TestCase implements Callable<Void> {

	protected static Logger log = Logger.getLogger(KyupiApp.class);

	protected Options options = new Options();

	private String[] args;

	private CommandLine argsParsed;

	private Library lib;

	protected static String OPT_DESIGN = "d";
	protected static String OPT_TESTS = "t";
	protected static String OPT_LIB = "l";
	protected static String OPT_INPUT = "i";
	protected static String OPT_OUTPUT = "o";
	protected static String OPT_FORCE = "f";
	protected static String OPT_JVMSTAT = "js";

	public KyupiApp() {
		options.addOption(OPT_DESIGN, true, "design file to load (d.v, d.vhdl, d.bench, d.isc, d.*.gz, d.*.bz2)");
		options.addOption(OPT_TESTS, true, "test data to read (t.stil, t.*.gz, t.*.bz2)");
		options.addOption(OPT_FORCE, false, "force overwriting any existing files");
		options.addOption(OPT_INPUT, true, "comma-seperated list of generic input files");
		options.addOption(OPT_OUTPUT, true, "comma-seperated list of generic output files");
		options.addOption(OPT_LIB, true, "technology library (*Basic, Saed90, Nangate)");
		options.addOption(OPT_JVMSTAT, false,
				"print some statictics on the memory footprints of various objects in the JVM");
	}

	public KyupiApp setArgs(String... args) {
		this.args = args;
		CommandLineParser parser = new GnuParser();
		try {
			argsParsed = parser.parse(options, args);
		} catch (ParseException e) {
			printOptionHelp();
			throw new IllegalArgumentException(e);
		}
		return this;
	}

	protected String[] args() {
		ensureArgsParsed();
		return args;
	}

	protected CommandLine argsParsed() {
		ensureArgsParsed();
		return argsParsed;
	}
	
	protected String stringFromArgsOrDefault(String opt, String dflt) {
		if (argsParsed().hasOption(opt)) {
			return argsParsed().getOptionValue(opt);
		}
		return dflt;
	}

	protected double doubleFromArgsOrDefault(String opt, double dflt) {
		if (argsParsed().hasOption(opt)) {
			return Double.parseDouble(argsParsed().getOptionValue(opt));
		}
		return dflt;
	}

	protected int intFromArgsOrDefault(String opt, int dflt) {
		if (argsParsed().hasOption(opt)) {
			return Integer.parseInt(argsParsed().getOptionValue(opt));
		}
		return dflt;
	}

	protected long longFromArgsOrDefault(String opt, long dflt) {
		if (argsParsed().hasOption(opt)) {
			return Long.parseLong(argsParsed().getOptionValue(opt));
		}
		return dflt;
	}

	protected Graph loadCircuitFromArgs() throws IOException {
		ensureArgsParsed();
		if (argsParsed.hasOption(OPT_DESIGN)) {
			String c_spec = argsParsed.getOptionValue(OPT_DESIGN);
			File f = new File(c_spec).getAbsoluteFile();
			ensureLib();
			log.info("LoadingCircuit " + f.getAbsolutePath());
			return GraphTools.loadGraph(f, lib);
		} else {
			log.error("Expected a design to load, please specify a circuit with -d ...");
			printOptionHelp();
			throw new IllegalArgumentException("Please specify a circuit with -" + OPT_DESIGN + " ...");
		}
	}

	private FormatStil patterns;

	private void ensurePatternsLoaded(Graph circuit) throws IOException {
		if (patterns == null) {
			ensureArgsParsed();
			if (argsParsed.hasOption(OPT_TESTS)) {
				String pats = argsParsed.getOptionValue(OPT_TESTS);
				patterns = new FormatStil(new File(pats), circuit);
			}
		}
	}

	protected ArrayList<QVector> loadStimuliFromArgs(Graph circuit) throws IOException {
		ensurePatternsLoaded(circuit);
		return patterns.getStimuliArray();
	}

	protected ArrayList<QVector> loadResponsesFromArgs(Graph circuit) throws IOException {
		ensurePatternsLoaded(circuit);
		return patterns.getResponsesArray();
	}

	protected ArrayList<File> outputFilesFromArgs() {
		ensureArgsParsed();
		ArrayList<File> files = new ArrayList<File>();
		if (argsParsed.hasOption(OPT_OUTPUT)) {
			String file_names[] = argsParsed.getOptionValue(OPT_OUTPUT).split(",");
			for (String n : file_names) {
				files.add(new File(n));
			}
		}
		return files;
	}

	protected void printWelcome() {
		log.info("KyupiApp " + this.getClass().getCanonicalName() + " " + String.join(" ", args()));
		RuntimeTools.printRuntimeInfo();
	}

	protected void printGoodbye() {
		log.info("EndTime " + new Date(System.currentTimeMillis()));
	}

	protected void printOptionHelp() {
		for (Object oo : options.getOptions()) {
			Option op = (Option) oo;
			log.info("  -" + op.getOpt() + "\t" + op.getDescription());
		}
	}

	protected void setLib(Library l) {
		lib = l;
	}

	protected Library getLib() {
		return lib;
	}

	private void ensureLib() {
		ensureArgsParsed();
		if (argsParsed.hasOption(OPT_LIB)) {
			String val = argsParsed.getOptionValue(OPT_LIB).toUpperCase();
			if (val.startsWith("N"))
				lib = new LibraryNangate();
			else if (val.startsWith("S"))
				lib = new LibrarySAED();
			else
				lib = new Library();
		} else {
			if (lib == null)
				lib = new Library();
		}
		log.info("Library " + lib.toString());
	}

	private void ensureArgsParsed() {
		if (argsParsed == null)
			setArgs();
	}

	public void testNothing() {
	}

}
