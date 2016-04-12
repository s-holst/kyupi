/*
 * Copyright 2013 The KyuPI project contributors. See the COPYRIGHT.md file
 * at the top-level directory of this distribution.
 * This file is part of the KyuPI project. It is subject to the license terms
 * in the LICENSE.md file found in the top-level directory of this distribution.
 * No part of the KyuPI project, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.md file.
 */
package org.kyupi.graph.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.kyupi.graph.Graph;
import org.kyupi.graph.Library;
import org.kyupi.graph.Graph.Node;

public class VerilogParseTree {

	protected static Logger log = Logger.getLogger(VerilogParseTree.class);

	private ArrayList<Module> modules = new ArrayList<>();

	// package-private members are for verilog parser use.

	class Assignment {
		String targetName;
		String sourceName;
	}

	class PortConnection {
		String portName;
		String variableName;
	}

	class ModuleInstantiation {
		String moduleName;
		String instanceName;
		ArrayList<PortConnection> portConnections = new ArrayList<>();

		PortConnection newPortConnection() {
			PortConnection pc = new PortConnection();
			portConnections.add(pc);
			return pc;
		}
	}

	class Range {
		int start = -1;
		int end = -1;
	}

	class RangedVariableList {
		Range range = new Range();
		HashSet<String> variableNames = new HashSet<>();
	}

	class Module {
		String moduleName;
		ArrayList<String> portNames = new ArrayList<>();
		ArrayList<RangedVariableList> inputDeclarations = new ArrayList<>();
		ArrayList<RangedVariableList> outputDeclarations = new ArrayList<>();
		ArrayList<RangedVariableList> wireDeclarations = new ArrayList<>();
		ArrayList<Assignment> assignments = new ArrayList<>();
		ArrayList<ModuleInstantiation> moduleInstantiations = new ArrayList<>();

		RangedVariableList newInputDeclaration() {
			RangedVariableList rvl = new RangedVariableList();
			inputDeclarations.add(rvl);
			return rvl;
		}

		RangedVariableList newOutputDeclaration() {
			RangedVariableList rvl = new RangedVariableList();
			outputDeclarations.add(rvl);
			return rvl;
		}

		RangedVariableList newWireDeclaration() {
			RangedVariableList rvl = new RangedVariableList();
			wireDeclarations.add(rvl);
			return rvl;
		}

		Assignment newAssignment() {
			Assignment a = new Assignment();
			assignments.add(a);
			return a;
		}

		ModuleInstantiation newModuleInstantiation() {
			ModuleInstantiation i = new ModuleInstantiation();
			moduleInstantiations.add(i);
			return i;
		}
	}

	VerilogParseTree() {
	}

	Module newModule() {
		Module m = new Module();
		modules.add(m);
		return m;
	}

	// public methods for generating graphs from parse tree.

	public ArrayList<Graph> elaborateAll(Library l) throws IOException {
		ArrayList<Graph> graphs = new ArrayList<>();
		for (Module m : modules) {
			graphs.add(elaborate(m, l));
		}
		return graphs;
	}

	public Graph elaborate(Module m, Library l) throws IOException {
		Graph g = new Graph(l);
		HashMap<String, Range> inputNames = new HashMap<>();
		for (RangedVariableList rvl : m.inputDeclarations) {
			for (String rv : rvl.variableNames) {
				inputNames.put(rv, rvl.range);
			}
		}
		HashMap<String, Range> outputNames = new HashMap<>();
		for (RangedVariableList rvl : m.outputDeclarations) {
			for (String rv : rvl.variableNames) {
				outputNames.put(rv, rvl.range);
			}
		}
		HashSet<String> wireNames = new HashSet<>();
		for (RangedVariableList rvl : m.wireDeclarations) {
			wireNames.addAll(expand(rvl.variableNames,rvl.range));
		}

		int interfacePos = 0;

		for (String portName : m.portNames) {
			int type = Library.TYPE_BUF;
			Range r;
			if (inputNames.containsKey(portName)) {
				type |= Library.FLAG_INPUT;
				r = inputNames.get(portName);
			} else if (outputNames.containsKey(portName)) {
				type |= Library.FLAG_OUTPUT;
				r = outputNames.get(portName);
			} else
				throw new IOException("direction of port \"" + portName + "\" not declared.");
			ArrayList<String> pl = new ArrayList<>();
			pl.add(portName);
			List<String> expanded = expand(pl, r);
			for (String na : expanded) {
				Node n = g.new Node(na, type);
				n.setPosition(interfacePos++);
			}
		}
		for (String wireName : wireNames) {
			findOrDeclareSignal(g, wireName);
		}
		for (Assignment assignment : m.assignments) {
			Node target = findOrDeclareSignal(g, assignment.targetName);
			Node source = findOrDeclareSignal(g, assignment.sourceName);
			g.connect(source, -1, target, 0);
		}
		for (ModuleInstantiation mi : m.moduleInstantiations) {
			Node n = g.new Node(mi.instanceName, l.resolve(mi.moduleName));
			if (n.isPort() || n.isSequential())
				n.setPosition(interfacePos++);
			for (PortConnection pc : mi.portConnections) {
				int pidx = l.pinIndex(n.type(), pc.portName);
				if (pidx < 0) {
					throw new IOException("pin name \"" + pc.portName + "\" unknown for cell " + n.queryName() + " of type " + n.typeName());
				}
				int pdir = l.pinDirection(n.type(), pc.portName);
				Node other = findOrDeclareSignal(g, pc.variableName);
				if (pdir == Library.DIR_IN) {
					g.connect(other, -1, n, pidx);
				} else {
					g.connect(n, pidx, other, 0);
				}
			}
		}

		return g;
	}

	// private helper

	private Node findOrDeclareSignal(Graph g, String name) {
		Node signal = g.searchNode(name);
		if (signal != null)
			return signal;
		return g.new Node(name, Library.TYPE_BUF | Library.FLAG_PSEUDO);
	}

	private List<String> expand(Collection<String> variableNames, Range range) {
		ArrayList<String> names = new ArrayList<>();
		if (range.start < 0 || range.end < 0) {
			names.addAll(variableNames);
			return names;
		}
		if (range.start < range.end) {
			for (int i = range.start; i <= range.end; i++) {
				for (String name : variableNames) {
					names.add(name + "[" + i + "]");
				}
			}
		} else {
			for (int i = range.start; i >= range.end; i--) {
				for (String name : variableNames) {
					names.add(name + "[" + i + "]");
				}
			}
		}
		return names;
	}

}
