package org.kyupi.circuit;

public abstract class Circuit {

	public abstract Iterable<? extends Cell> intf();
	
	public abstract Cell intf(int pos);
	
	public abstract Iterable<? extends Cell> cells();
	
	public abstract Cell searchNode(String name);
	
	public abstract Library library();
	
	public abstract int width(); 
	
	public abstract String getName();
	
	public abstract int countInputs();
	
	public abstract int countOutputs();
	
	
}
