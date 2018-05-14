package org.kyupi.circuit;

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
	

}
