package org.kyupi.circuit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import org.apache.log4j.Logger;

/**
 * is the main data structure to represent and manipulate logic circuits.
 * 
 * Cells are represented by instances of class Cell. Each cell has a unique id()
 * within the graph. To access all cells, use the cells() iterable. To access a 
 * specific cell, use cell(int id) or searchCellByName(String name).
 * <p/>
 * 
 * Each cell as an arbitrary number of input pins and output pins indexed by a
 * positive integer. A connection between an output pin of a driving cell and an
 * input pin of a receiving cell is called a signal. Signals don't have an explicit
 * class. They are referenced to by unique integer ids >= 0. Each signal has exactly
 * one driver and one receiver. Fanouts have to be modeled as cells with multiple
 * outputs pins. 
 * <p/>
 * 
 * @author stefan
 * 
 */
public abstract class Circuit {

	protected static Logger log = Logger.getLogger(Circuit.class);

	public abstract String name();

	public abstract Iterable<? extends Cell> cells();
	
	public abstract Cell cell(int id);

	public abstract Cell searchCellByName(String name);

	public abstract int size();

	public abstract Iterable<? extends Cell> intf();
	
	public abstract Cell intf(int pos);
	
	public abstract int width();

	public abstract int countInputs();
	
	public abstract int countOutputs();
	
	public abstract int signalCount();

	public abstract Library library();
	
	public abstract Cell driverOf(int signalID);

	public abstract Cell readerOf(int signalID);
	
	public abstract int driverPinOf(int signalID);

	public abstract int readerPinOf(int signalID);

	public void printStats() {
		HashMap<String, Integer> pseudo = new HashMap<>();
		HashMap<String, Integer> combinational = new HashMap<>();
		HashMap<String, Integer> inputst = new HashMap<>();
		HashMap<String, Integer> outputst = new HashMap<>();
		HashMap<String, Integer> sequential = new HashMap<>();
		int inputs = 0;
		int outputs = 0;
		int gates = 0;
		int nodes = 0;
		int signals = 0;
		int seq = 0;
		for (Cell n : cells()) {
			if (n == null)
				continue;
			String type = n.typeName();
			nodes++;
			if (n.isPseudo()) {
				signals++;
				pseudo.put(type, pseudo.getOrDefault(type, 0) + 1);
				continue;
			}
			if (n.isSequential()) {
				seq++;
				sequential.put(type, sequential.getOrDefault(type, 0) + 1);
				continue;
			}
			if (n.isInput()) {
				inputs++;
				inputst.put(type, inputst.getOrDefault(type, 0) + 1);
			}
			if (n.isOutput()) {
				outputs++;
				outputst.put(type, outputst.getOrDefault(type, 0) + 1);
			}
			if (n.isInput() || n.isOutput())
				continue;
			gates++;
			combinational.put(type, combinational.getOrDefault(type, 0) + 1);
		}
		log.info("CircuitName " + name());
		//log.info("Levels " + levels());
		log.info("NodeCount " + nodes);
		log.info("  PseudoNodeCount " + signals);
		printGateCounts(pseudo);
		log.info("  CombinationalCellCount " + gates);
		printGateCounts(combinational);
		log.info("  SequentialCellCount " + seq);
		printGateCounts(sequential);
		log.info("  PrimaryInputCount " + inputs);
		printGateCounts(inputst);
		log.info("  PrimaryOutputCount " + outputs);
		printGateCounts(outputst);
	}

	private void printGateCounts(HashMap<String, Integer> map) {
		ArrayList<String> keys = new ArrayList<>(map.keySet());
		keys.sort(new Comparator<String>() {
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
		for (String key : keys) {
			log.info("    " + key + " " + map.get(key));
		}

	}
}
