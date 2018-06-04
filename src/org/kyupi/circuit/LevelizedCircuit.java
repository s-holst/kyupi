package org.kyupi.circuit;

import java.util.Arrays;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.kyupi.circuit.MutableCircuit.MutableCell;
import org.kyupi.misc.ArrayTools;
import org.kyupi.misc.Namespace;

/**
 * a circuit structure in which all cells are topologically ordered.
 * 
 * The circuit represented by this class cannot be changed. It is constructed
 * from a given MutableCircuit. The given circuit must not contain any combinational
 * loops. Every cycle in the graph has to contain either a sequential cell
 * (cell.isSequential()==true) or a primary input cell (cell.isInput()==true).
 * 
 * The first level (level 0) contains all nodes
 * that have either no predecessors (e.g. primary inputs or constants), or are
 * sequential elements (n.isSequential()==true). All remaining nodes are
 * assigned to the lowest possible level for which all of its predecessors are
 * on even lower levels. Outgoing edges always point either to a cell on a higher
 * level or a primary input / sequential cell on level 0. Each cell has a unique
 * position() on a level().
 * 
 * @author Stefan
 *
 */
public class LevelizedCircuit extends Circuit {

	public class LevelizedCell extends Cell {
		private final int l;
		private final int m;
		private final LevelizedCell[] inputCells;
		private final LevelizedCell[] outputCells;

		private LevelizedCell(MutableCell cell, int l, int m) {
			super(namespace.idFor(cell.name()), cell.type());
			//this.cid = ;
			this.l = l;
			this.m = m;
			this.intfPosition = cell.intfPosition();
			this.inputCells = new LevelizedCell[cell.maxIn() + 1];
			this.outputCells = new LevelizedCell[cell.maxOut() + 1];
		}

		public LevelizedCell outputCellAt(int outputIdx) {
			return (LevelizedCell) ArrayTools.safeGet(outputCells, outputIdx);
		}

		public LevelizedCell inputCellAt(int inputIdx) {
			return (LevelizedCell) ArrayTools.safeGet(inputCells, inputIdx);
		}

		public Iterable<LevelizedCell> inputCells() {
			return Arrays.asList(inputCells);
		}

		public Iterable<LevelizedCell> outputCells() {
			return Arrays.asList(outputCells);
		}

		public int inputCount() {
			return inputCells.length;
		}

		public int outputCount() {
			return outputCells.length;
		}

		public int inputSignalAt(int inputIdx) {
			return input_map[input_map_offset[id()] + inputIdx];
		}
		
		public int outputSignalAt(int outputIdx) {
			return output_map_offset[id()] + outputIdx;
		}

		public int level() {
			return l;
		}

		public int position() {
			return m;
		}

		public int searchOutIdx(LevelizedCell succ) {
			int i = ArrayTools.linearSearchReference(outputCells, succ);
			if (i < 0)
				throw new IllegalArgumentException("given node is not a successor");
			return i;
		}

		public int searchInIdx(LevelizedCell pred) {
			int i = ArrayTools.linearSearchReference(inputCells, pred);
			if (i < 0)
				throw new IllegalArgumentException(
						"given node " + pred.id() + " is not a predecessor of " + id());
			return i;
		}

		public String name() {
			return namespace.nameFor(id());
		}

		public String typeName() {
			return library.typeName(type);
		}

		public boolean isType(int type) {
			return library.isType(this.type, type);
		}

		public String inName(int input_pin) {
			return library.inputPinName(type, input_pin);
		}

		public String outName(int output_pin) {
			return library.outputPinName(type, output_pin);
		}

		public String toString() {
			String iosuffix = "";
			if (isInput())
				iosuffix = "I" + intfPosition();
			if (isOutput())
				iosuffix = "O" + intfPosition();
			StringBuilder b = new StringBuilder(
					"" + l + "_" + m + ":" + typeName() + "\"" + name() + "\"" + iosuffix);
			int m_in = inputCount();
			int m_out = outputCount();
			for (int i = 0; i < m_in; i++) {
				if (inputCells[i] == null) {
					b.append("<null");
				} else {
					b.append(" <" + inputCells[i].l + "_" + inputCells[i].m);
				}
			}
			for (int o = 0; o < m_out; o++) {
				if (outputCells[o] == null) {
					b.append(">null");
				} else {
					b.append(" >" + outputCells[o].l + "_" + outputCells[o].m);
				}
			}
			return b.toString();
		}
	}

	protected static Logger log = Logger.getLogger(MutableCircuit.class);

	private Namespace namespace = new Namespace();

	private final Library library;

	private LevelizedCell levels[][];

	private LevelizedCell intf[];

	private LevelizedCell cells[];

	public final String name;


	private int input_map_offset[];
	private int input_map[];
	private int output_map_offset[];
	private int edge_count;
	private LevelizedCell drivers[];
	private LevelizedCell receivers[];


	public LevelizedCircuit(MutableCircuit circuit) {
		library = circuit.library();
		name = circuit.name();		

		LinkedList<MutableCell> queue = new LinkedList<MutableCell>();
		int[] l = new int[circuit.size()];
		int[] m = new int[circuit.size()];
		int level_fills[] = new int[1];
		int maxlevel = 0;

		Arrays.fill(l, -1);
		Arrays.fill(m, 0);

		// Collect node ids for interface array.
		// Add all appropriate nodes to queue for level 0.
		int[] intfIDs = new int[circuit.width()];
		int intfMax = -1;
		Arrays.fill(intfIDs, -1);
		for (MutableCell cell : circuit.cells()) {
			if (cell == null)
				continue;
			// intf array contains:
			// - primary input ports
			// - primary output ports
			// - sequential nodes (flip-flops)
			if (cell.isPort() || cell.isSequential()) {
				if (intfIDs[cell.intfPosition()] != -1) {
					log.error("Nodes must not share same intfPosition: " + cell.name() + ", "
							+ circuit.cell(intfIDs[cell.intfPosition()]).name() + " (intfPosition: " + cell.intfPosition() + ")");
				}
				intfIDs[cell.intfPosition()] = cell.id();
				intfMax = Math.max(intfMax, cell.intfPosition());
			}
			// first level (level[0]) contains:
			// - primary input ports
			// - other nodes without inputs (such as constants)
			// - sequential nodes (flip-flops)
			if (cell.maxIn() < 0 || cell.isSequential() || cell.isInput()) {
				queue.add(cell);
			}
		}

		level_fills[0] = 0;

		// set levels and levelPositions of all nodes.
		while (!queue.isEmpty()) {
			MutableCell cell = queue.poll();
			if (l[cell.id()] != -1) {
				throw new RuntimeException("Detected combinational loop at gate: " + cell.name());
			}
			l[cell.id()] = 0;
			if (!cell.isSequential() && !cell.isInput()) {
				for (int i = cell.maxIn(); i >= 0; i--) {
					MutableCell d = cell.inputCellAt(i);
					if (d != null)
						l[cell.id()] = Math.max(l[cell.id()], l[d.id()] + 1);
				}
			}
			level_fills = ArrayTools.grow(level_fills, l[cell.id()] + 1, 1000, 0);
			m[cell.id()] = level_fills[l[cell.id()]]++;
			maxlevel = Math.max(maxlevel, l[cell.id()]);
			// log.debug("node " + g + " is level " + g.level);
			if (cell.countOuts() == 0)
				continue;
			for (MutableCell succ : cell.outputCells()) {
				if (succ != null && !succ.isSequential() && !succ.isInput()) {
					m[succ.id()]++; // re-use levelPosition to count
					// number of predecessors placed.
					if (m[succ.id()] == succ.countIns())
						queue.add(succ);
				}
			}
		}

		// allocate levels and cells.
		levels = new LevelizedCell[maxlevel + 1][];
		int cellCount = 0;
		for (int i = 0; i <= maxlevel; i++) {
			cellCount += level_fills[i];
			levels[i] = new LevelizedCell[level_fills[i]];
		}
		cells = new LevelizedCell[cellCount];
		for (MutableCell cell : circuit.cells()) {
			if (cell == null)
				continue;
			if (l[cell.id()] == -1)
				log.warn("Unconnected cell not levelized: " + cell.name());
			else {
				LevelizedCell c = new LevelizedCell(cell, l[cell.id()], m[cell.id()]);
				levels[l[cell.id()]][m[cell.id()]] = c;
				cells[c.id()] = c;
			}
		}

		// allocate and fill intf array
		intf = new LevelizedCell[intfMax + 1];
		for (int i = 0; i <= intfMax; i++) {
			int cid = intfIDs[i];
			if (cid >= 0) {
				intf[i] = levels[l[cid]][m[cid]];
			}
		}


		// connect new cells to each other
		for (MutableCell cell : circuit.cells()) {
			if (cell == null)
				continue;
			if (l[cell.id()] != -1) {
				LevelizedCell lc = levels[l[cell.id()]][m[cell.id()]];
				for (int i = 0; i <= cell.maxIn(); i++) {
					MutableCell driver = cell.inputCellAt(i);
					if (driver != null)
						lc.inputCells[i] = levels[l[driver.id()]][m[driver.id()]];
				}
				for (int i = 0; i <= cell.maxOut(); i++) {
					MutableCell reader = cell.outputCellAt(i);
					if (reader != null)
						lc.outputCells[i] = levels[l[reader.id()]][m[reader.id()]];
				}
			}
		}

		// log.debug("got levels: " + levels.length);
		//signalMap = new SignalMap(this);

		output_map_offset = new int[cells.length];
		input_map_offset = new int[cells.length];
		Arrays.fill(output_map_offset, -1);
		Arrays.fill(input_map_offset, -1);

		int output_map_idx = 0;
		int next_input_map_offset = 0;

		for (LevelizedCell n: cells) {
			int out_count = n.outputCount();
			if (out_count == 0)
				continue;

			// Node n has at least one output edge.

			output_map_offset[n.id()] = output_map_idx;

			for (int succ_idx = 0; succ_idx < out_count; succ_idx++) {
				LevelizedCell succ = n.outputCellAt(succ_idx);
				// skip unconnected outputs
				if (succ == null) {
					output_map_idx++;
					continue;
				}

				if (input_map_offset[succ.id()] == -1) {
					// allocate input_map for previously unseen successor Node.
					input_map_offset[succ.id()] = next_input_map_offset;
					next_input_map_offset += succ.inputCount();
					input_map = ArrayTools.grow(input_map, next_input_map_offset, 4096, -1);
				}
				//int input_idx = 0;
				int input_map_idx = input_map_offset[succ.id()];
				for (LevelizedCell c: succ.inputCells()) {
					if (c != null && c.id() == n.id()) {
						if (input_map[input_map_idx] < 0) {
							input_map[input_map_idx] = output_map_idx;
							break;
						}
					}
					input_map_idx++;
				}
				output_map_idx++;
			}
		}
		edge_count = output_map_idx;

		drivers = new LevelizedCell[edge_count];
		receivers = new LevelizedCell[edge_count];
		for (LevelizedCell n: cells) {
			if (n == null)
				continue;
			int out_count = n.outputCount();
			for (int i = 0; i < out_count; i++) {
				LevelizedCell succ = n.outputCellAt(i);
				if (succ != null) {
					drivers[n.outputSignalAt(i)] = n;
				}
			}
			int in_count = n.inputCount();
			for (int i = 0; i < in_count; i++) {
				LevelizedCell pred = n.inputCellAt(i);
				if (pred != null) {
					receivers[n.inputSignalAt(i)] = n;
				}
			}
		}
	}

	public int size() {
		return cells.length;
	}

	public int width() {
		return intf.length;
	}

	public int depth() {
		return levels.length;
	}

	public Iterable<LevelizedCell> cells() {
		return Arrays.asList(cells);
	}
	
	public LevelizedCell cell(int id) {
		return cells[id];
	}

	public Library library() {
		return library;
	}

	public Iterable<LevelizedCell> level(int levelIdx) {
		return Arrays.asList(levels[levelIdx]);
	}

	public Iterable<LevelizedCell> intf() {
		return Arrays.asList(intf);
	}

	public LevelizedCell intf(int intfPosition) {
		return intf[intfPosition];
	}

	public LevelizedCell driverOf(int signalID) {
		return drivers[signalID];
	}

	public LevelizedCell readerOf(int signalID) {
		return receivers[signalID];
	}

	public int signalCount() {
		return edge_count;
	}

	public LevelizedCell searchCellByName(String name) {
		if (!namespace.contains(name)) {
			return null;
		}
		return cells[namespace.idFor(name)];
	}

	public String name() {
		return name;
	}

	public int countInputs() {
		int count = 0;
		for (Cell g : intf()) {
			if (g != null && g.isInput())
				count++;
		}
		return count;
	}

	public int countOutputs() {
		int count = 0;
		for (Cell g : intf()) {
			if (g != null && g.isOutput())
				count++;
		}
		return count;
	}

	public String toString() {
		StringBuilder b = new StringBuilder();
		int level_idx = 0;
		for (LevelizedCell level[] : levels) {
			int node_idx = 0;
			b.append("" + level_idx + "[ ");
			for (LevelizedCell node : level) {
				b.append("" + node_idx + "(");
				b.append(node);
				b.append(") ");
				node_idx++;
			}
			level_idx++;
			b.append("]\n");
		}
		return b.toString();
	}
}
