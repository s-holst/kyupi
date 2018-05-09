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

4. Take a look at org/kyupi/misc/KyupiApp.java to learn how to make your first
   Kyupi application. All public APIs include JavaDoc comments and the JUnit tests
   also serve as programmatic behavioral description of the APIs.

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


Library Design Overview
-----------------------

This overview should give you some initial orientation on the design choices made
in the library and an idea of the functionality currently available.

*org.kyupi.circuit:* Importing, exporting and manipulating circuits.

The class MutableCircuit is the main data structure for holding circuit netlists in memory.
A MutableCircuit is associated with a Library that defines the available cells.
Each cell is represented in the graph as an instance of MutableCircuit.MutableCell.
The graph can be traversed in two ways. (1) follow the references in individual
nodes to their neighbors. (2) directly access arrays containing all nodes in
topological order. The graph is ordered automatically on-demand. Whenever the graph
structure is changed by adding or removing nodes, the graph is sorted into
topological order once the direct access arrays are requested.

Graphs can contain pseudo-nodes (flag FLAG_PSEUDO is set). Pseudo nodes do not
represent a physical cell in the design, but encode signal names and branch points.

By default, each node in the graph also contains a branching point. I.e. all
outgoing signal connections to the successor nodes are considered branches of the
same signal. This default behavior can be turned off by setting the flag
FLAG_MULTIOUTPUT on a node. Outputs of nodes with this flag are considered different
and explicit pseudo nodes have to be added to fan-out from each of the distinct
outputs.

Basic import/export support is available for VHDL, verilog, ISCAS, Bench, dot.

*org.kyupi.data.item:* Data structures for 2-valued and 4-valued vector data both
for individual processing and bit-parallel processing.

The 2-valued data items are prefixed with B for 'binary': BVector, BBlock.

The 4-valued data items are prefixed with Q for 'quaternary': QVector, QBlock.

BVector and QVector classes represent a single vector of data stored internally
with one or two BitSets.
BBlock and QBlock classes represent a set of at most 64 vectors. These vectors
are stored using one or two long for each bit position.

The width of vectors (number of bit positions) is immutable after creation, but
the bit data itself is mutable to reduce object creation overhead. 


*org.kyupi.data.source:* Streaming-based test data processing.

Sets of vector data is organized in streams of data items. Each stream is created
by specifying one or more source streams and some operation. KyuPI encourages
lazy evaluation by having a pull-based streaming setup. I.e. a simulator is set up
by declaring 'resp = sim(tests)' and later run by pulling vectors from the resp
stream 'v = resp.next();'.

Data items used in streams are re-usable. Whenever a data item (vector) is not needed
anymore, item.free() is called so that the source stream can re-use the data item
again.

There is a stream class for each of the basic vector formats BVector, QVector,
BBlock, QBlock called (BV|QV|BB|QB)Stream.

Data streams can be plugged together in arbitrary complex ways for advanced data
processing. The basic source classes support easy conversion between the four
vector data formats by using the static method '.from(stream)'.

Streams implement the Iterable and Iterator interfaces. Thus they can be used
easily in for loops 'for(QVector v: stream) {...}'. Be aware, that there is only one
iterator state per stream. Nested loops on a single stream will not work. Also,
a second for loop will cause the stream (and all of its sources) to reset, which
repeats all the processing again. If a single stream is used multiple times, its data
should be stored first e.g. in an ArrayList by using 'toArrayList()'. Then, multiple
new streams can be created from the array: 's2 = QVStream.from(width,array)'.

Basic import of some STIL files.

*org.kyupi.sim:* Plain logic simulation and fault simulation.

*org.kyupi.faults:* Stuck-at fault sets and fault collapsing.

*org.kyupi.ipc:* Shared-memory based inter-process communication support.

*org.kyupi.misc:* Various helpers and tools used globally.

