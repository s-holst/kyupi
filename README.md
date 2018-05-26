Kyutech Parallel Processing Platform for Integrated circuits (KyuPI)
====================================================================

KyuPI is a Java library for high-performance processing and validation of
non-hierarchical designs. Its purpose is to provide a rapid prototyping
platform to aid and accelerate research in the fields of VLSI test, diagnosis
and reliability. KyuPI is freely available under a BSD license (see
LICENSE.md).

For latest information on KyuPI, visit [kyupi.org](http://kyupi.org).


Quick Start
-----------

1. KyuPI requires Java version 8 or above. It is tested only with the
   [JDK provided by Oracle](http://www.oracle.com/technetwork/java/javase/downloads/index.html).

2. KyuPI is distributed as configured [Eclipse](http://www.eclipse.org)
   project. Import it as existing project and it should successfully build
   out-of-the-box. If you don't have Eclipse yet, you can still try the next
   step.

3. Run the start script "kyupi" for testing your environment and more
   information on command line usage (bash-script, run on Linux, Mac or Cygwin).
   By default, it runs all the unit tests and reports its results. These tests 
   only take a few seconds and should complete without failures. If there are 
   failures, follow the hints in the output to resolve them. If you have 
   imported the project into Eclipse, run all JUnit tests from there as well.

4. All public APIs include JavaDoc comments and the JUnit tests also serve as
   programmatic behavioral description of the APIs. Take a look at the "Guided
   KyuPI Library Tour" below to learn how to make your first KyuPI application. 

5. We'd love to hear your feedback: info@kyupi.org


Mission
-------

The mission of KyuPI is to aid and accelerate research in the fields of VLSI
test, diagnosis and reliability by providing a common and highly usable
rapid-prototyping software platform for high-performance processing,
simulation, and analysis of non-hierarchical designs.

On this mission, KyuPI will incorporate all basic functions for exchanging
design and simulation data between various tools and standards. It will provide
easy programmatic access to this data and include various tools for common
transformations and analyses. KyuPI implements these functions with a strong
focus on efficiency both during development and during execution.

Anybody interested can download the KyuPI project repository free of charge and
without registration. Anybody is able to benefit from well-tested and
continuously improving and extending code base for faster VLSI research without
spending energy on re-implementing basic functions over and over again. In
exchange we kindly ask you to contribute bug-fixes, improvements and new
broadly applicable features back to the KyuPI platform. This will help KyuPI
become a common platform for efficiently exchanging research code within
between individual researchers and students as well as between research groups
and projects. 


Development Principles
----------------------

We feel, that following the principles described in this section will be useful
for succeeding on the mission.

* *Design the APIs for minimal documentation.* Documentation is the first thing
  that gets neglected if time is pressing and out-dated documentation is often
  worse than no documentation at all. We consider APIs, which are full of
  surprises and require lengthy usage documentation badly designed. Instead, we
  like to have self-explaining classes and methods which are fool-proof to use
  with minimal JavaDocs explaining behavior and side-effects not obvious to the
  user.

* *Unit testing for code quality and as usage examples.* Testing is an important
  part of any design effort. We like to emphasize the double-role (software)
  testing has in KyuPI. First, it is used to validate code functionality and
  continued monitoring for any bugs introduced later on. Second, the tests also
  serve as documentation by giving examples of the intended usage of the APIs.
  This documentation stays up-to-date and is a source of inspiration and code
  snippets for implementing algorithms in KyuPI.

* *Test-driven development and maintenance.* Tests and use cases are usually
  written before the actual implementation of the desired functionality. Only
  code with existing and passing unit tests is guaranteed to be maintained.
  Other code will eventually be removed from the project.

* *Runtime efficiency.* While keeping the APIs clean and self-explaining, the
  library is designed for optimal performance and minimal memory footprint.
  Object creation overheads and garbage collection pressure are carefully
  managed by encouraging object pooling and in-place data manipulation.
  Furthermore, the library exposes as much inherent parallelism as possible to
  scale well on multi-core and many-core machines.


Current State of Development
----------------------------

After a few months of initial design and development, KyuPI has been made
available to the public on Aug. 1st 2013. Although the library already provides
some usable functionality, the project is still in its infancy. Currently, we
develop continuously and push frequently to the public repository. All APIs are
subject to change and we plan to provide backward compatibility only after the
APIs become reasonably stable.


Contributing
------------

Learning to work with a new piece of software is always an investment. We are
working hard to achieve the highest benefit/cost ratio as possible. But this
requires your help. No matter if you try KyuPI for the first time or using it
in large projects, please send us any:

* Comments on usability, documentation, API design, ...
* Reports on bugs, inconsistencies, performance issues
* Proposed or implemented use cases and applications
* Feature requests or better: Code for new features

Using the contact address info@kyupi.org. The latest information on KyuPI is
available at [kyupi.org](http://kyupi.org). The main repository is hosted on
github, which makes it easy for anyone to fork, experiment with the code,
generate patches and propose pull-requests. 



Guided KyuPI Library Tour
-------------------------

To make a KyuPI based application, subclass KyupiApp in this way:

```java
public class MyApp extends KyupiApp {
 	public static void main(String[] args) throws Exception {
 		new MyApp().setArgs(args).call();
 	}
 	
 	public MyApp() {
 		options.addOption("opt1", true, "a new command line option with argument");
 		options.addOption("opt2", false, "a new command line option without argument");
 		// TODO: add more options as needed
 	}
 
 	public Void call() throws Exception {
 		printWelcome();
 		if (argsParsed().hasOption("opt1")) {
 			log.info("Got option 'opt1' with argument: " + argsParsed().getOptionValue("opt1"));
 		} else {
 			throw new IllegalArgumentException("opt1 is mandatory.");
 		}
 		if (argsParsed().hasOption("opt2")) {
 			log.info("Got opt2");
 		}
 		
 		// TODO: Your code goes here
 		
 		printGoodbye();
 		return null;
 	}
 	
 	@Test
 	public void testSomething() {
 		// provide use-cases for MyApp.
 		setArgs("-opt1", "arg1", "-opt2");
 		try {
			call();
			// TODO: Assert some results of the call.
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
 	}
 }
```

This subclass serves both as an entry point for a Java application and a JUnit test class.
In addition, KyupiApp has several convenience methods to process command-line arguments as
demonstrated above.

The most common first step in a Kyupi application is to load circuits and test data specified
on the command line. This is done by adding the following lines to the call() method:

```java
Circuit circuit = loadNextCircuitFromArgs();
PatternList p = loadNextPatternListFromArgs(circuit);
```

Which parses the first circuit file and first pattern file specified on the command line.
For circuits, import from structural VHDL, structural verilog, ISCAS and Bench is available.
The supported technology libraries are currently the basic gate primitives used in ISCAS and Bench
benchmark sets and SAED90.

For pattern lists, the only format currently supported is STIL. The pattern parser will
cross-reference the pattern descriptions with the interface of the specified circuit.

** *org.kyupi.circuit:* Importing, Exporting and Manipulating Circuits.**

The class Circuit is the primary interface for interacting with circuit structures. A Circuit
is a directed graph in which the vertices are the (standard-)cells in the circuit and the edges are signals
between the cells. A cell can have arbitrary many incoming and outgoing signals, yet one
signal has always exactly one driving cell and one reading cell. Fan-outs (branching points) are modeled as cells
in the graph. Cells are represented by instances of the class Cell. Each cell is a member of exactly one
graph (instance of class Circuit). The number of cells in the circuit is its size (`circuit.size()`).
To access all cells in a circuit, use `cells()` to access an Iterable:

```java
for(Cell c : circuit.cells()) {
	// do something with c
}
```

The order of the cells in the circuit is undefined. The iteration may contain null elements or unconnected
cells if the circuit structure was edited previously. The interface of a circuit is a special subset of cells
that serve as primary inputs, primary outputs or sequential elements in the circuit. These cells are handled
specially during simulation. All cycles in the directed graph should contain at least one of these interface
cells. To access the cells of the interface, use:

```java
for(Cell c : circuit.intf()) {
	// do something with c
}
```

The loop iterates over all interface cells in order of their position in the interface.
Null elements are present if an interface position is not occupied by a cell. Interface cells at a specific position
is accessed with `circuit.intf(position)`. The number of interface cells and their position defines the "width" of
the circuit (`circuit.width()`). The maximum valid interface position is `circuit.width()-1`.

Each cell in a circuit is a unique ID, which is a positive and usually consecutive integer value.
The IDs are designed to be used as array indices for external payload storage.
Specific cells can be accessed by a given id (`Cell c = circuit.cell(id)`).
Furthermore, each cell has a unique name, cells can be looked up by name as well (`Cell c = circuit.searchCellByName("BUF123")`).
Cells are member of exactly one circuit, they cannot be contained in multiple circuits.
Their IDs are unique only in the context of the circuit they belong to. 

An instance of a cell provides access to its `c.id()`, `c.name()`, `c.type()`, and `c.typeName()`.
The type is an integer describing the nature of the cell (e.g. logic function).
The type name is a string description of the cell type.
If the cell is part of the interface of its circuit, `c.intfPosition()` returns its position in the interface.

A cell can have arbitrary many neighbors (other cells directly connected to the cell).
A neighbor may be connected to a cell input or a cell output.
The number of inputs (outputs) to a cell is `c.inputCount()` (`c.outputCount()`).
To access specific inputs, use `c.inputCellAt(int pinIndex)`. 
To iterate over all the input cells, use:

```java
for(Cell driver : c.inputCells()) {
	// do something with driver
}
```

Same goes for the outputs by using the respective output accessors.

Each connection (signal) between two cells in a circuit has an id as well.
Same as with the cell IDs, it is a positive integer designed to be used as an array index for external payload data storage (e.g. logic values in a simulator.
Different than cells, signals do not have a explicit representation in form of objects.
To access specific signal IDs, use `c.inputSignalAt(int pinIndex)` or `c.outputSignalAt(int pinIndex)`, each of which returns a positive integer if there is something connected to the specified pin, or -1, otherwise.
To loop over the signal IDs, use:

```java
for(int signalID : c.inputSignals()) {
	// do something with signalID
}
```

Signals at the outputs are accessed with `c.outputSignals()`.

Each signal has exactly one driving cell and one receiving cell.
To access these cells of a given signal, use the following methods provided by the circuit object: `circuit.driverOf(int signalID)`, `circuit.readerOf(int signalID)`.

Two concrete implementations of `Circuit` and `Cell` are currently available.
`MutableCircuit` allows manipulating the circuit structure by adding and removing cells and changing connections between cells.
`LevelizedCircuit` is immutable, but provides a topological sorting of the graph structure useful for simulation.
The circuit classes use instances of `MutableCell` and `LevelizedCell`, respectively.


** *org.kyupi.data:* Handling Vector Data and Data Flows**

A piece of a chunk of logic values (e.g. a test pattern, a vector) are instances of the abstract
base class `DataItem`.
There are 4 concrete implementations of `DataItem` based on the nature of the data stored.

Data can either be 2-valued or 4-valued.
2-valued data items are prefixed with B for 'binary': `BVector`, `BBlock`.
4-valued data items are prefixed with Q for 'quaternary': `QVector`, `QBlock`.

Furthermore, a data item can either hold a single vector or a set of 64 vectors for bit-parallel processing.
`BVector` and `QVector` classes represent a single vector of data stored internally with one or two BitSets.
`BBlock` and `QBlock` classes represent a set of at most 64 vectors.
These vectors are stored using one or two long for each bit position.

The width of vectors (number of bit positions) is immutable after creation, but
the bit data itself is mutable to reduce object creation overhead. 


Sets or lists of vector data is organized in streams of data items.
Each stream is created by specifying one or more source streams and some operation.
KyuPI encourages lazy evaluation by having a pull-based streaming setup.
I.e. a simulator is set up by declaring `resp = sim(tests)` and later run by pulling vectors from the resp stream `v = resp.next();`.

Data items used in streams are re-usable.
Whenever a data item (vector) is not needed anymore, `item.free()` is called so that the source stream can re-use the data item again.

There is a stream class for each of the basic vector formats `BVector`, `QVector`, `BBlock`, `QBlock` called `BVStream`, `QVStream`, `BBStream`, and `QBStream`, respectively.

Data streams can be plugged together in arbitrary complex ways for advanced data processing.
The basic source classes support easy conversion between the four vector data formats by using the static method `.from(stream)`.

Streams implement the Iterable and Iterator interfaces.
Thus they can be used easily in for loops `for(QVector v: stream) {...}`.
Be aware, that there is only one iterator state per stream.
Nested loops on a single stream will not work.
Also, a second for loop will cause the stream (and all of its sources) to reset, which repeats all the processing again.
If a single stream is used multiple times, its data should be stored first e.g. in an ArrayList by using `toArrayList()`.
Then, multiple new streams can be created from the array: `s2 = QVStream.from(width,array)`.

*org.kyupi.sim:* Plain logic simulation and fault simulation.

*org.kyupi.ipc:* Shared-memory based inter-process communication support.
