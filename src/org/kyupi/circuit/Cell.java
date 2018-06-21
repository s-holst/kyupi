package org.kyupi.circuit;

import java.util.Iterator;

public abstract class Cell {

	private final int id;

	public final int id() {
		return id;
	}

	protected int type;

	public final int type() {
		return type;
	}

	protected Cell(int id, int type) {
		this.id = id;
		this.type = type;
	}

	protected int intfPosition = -1;

	public final int intfPosition() {
		return intfPosition;
	}

	public static final int FLAG_INPUT = 0x8000_0000;
	public static final int FLAG_OUTPUT = 0x4000_0000;
	public static final int FLAG_PSEUDO = 0x2000_0000;

	public static final int FLAG_SEQUENTIAL = 0x0200;
	public static final int FLAG_MULTIOUTPUT = 0x0100;

	protected static final int INPUTS_OFFSET = 10; // .... iiii ii-- ---- ----
	protected static final int INPUTS_MASK = 0x3f << INPUTS_OFFSET;
	public static final int INPUTS_UNKNOWN = 0 << INPUTS_OFFSET;
	public static final int INPUTS_1 = 1 << INPUTS_OFFSET;
	public static final int INPUTS_2 = 2 << INPUTS_OFFSET;
	public static final int INPUTS_3 = 3 << INPUTS_OFFSET;
	public static final int INPUTS_4 = 4 << INPUTS_OFFSET;
	public static final int INPUTS_5 = 5 << INPUTS_OFFSET;
	public static final int INPUTS_6 = 6 << INPUTS_OFFSET;
	public static final int INPUTS_7 = 7 << INPUTS_OFFSET;
	public static final int INPUTS_8 = 8 << INPUTS_OFFSET;

	public static final int VARIANT_1 = 0x1_0000;
	public static final int VARIANT_2 = 0x2_0000;
	public static final int VARIANT_3 = 0x3_0000;

	public static final int TYPE_MASK = 0xff;

	public static final int TYPE_CONST0 = 0x0;
	public static final int TYPE_NOR = 0x1;
	public static final int TYPE_AGTB = 0x2;
	public static final int TYPE_BNOT = 0x3;
	public static final int TYPE_BGTA = 0x4;
	public static final int TYPE_NOT = 0x5;
	public static final int TYPE_XOR = 0x6;
	public static final int TYPE_NAND = 0x7;
	public static final int TYPE_AND = 0x8;
	public static final int TYPE_XNOR = 0x9;
	public static final int TYPE_BUF = 0xa;
	public static final int TYPE_AGEB = 0xb;
	public static final int TYPE_BBUF = 0xc;
	public static final int TYPE_BGEA = 0xd;
	public static final int TYPE_OR = 0xe;
	public static final int TYPE_CONST1 = 0xf;

	public static final int TYPE_DFF = TYPE_BUF | FLAG_SEQUENTIAL;

	public static final int TYPE_AO21 = 0x10 | INPUTS_3;
	public static final int TYPE_AO221 = 0x11 | INPUTS_5;
	public static final int TYPE_AO222 = 0x12 | INPUTS_6;
	public static final int TYPE_AO22 = 0x13 | INPUTS_4;
	public static final int TYPE_AOI21 = 0x14 | INPUTS_3;
	public static final int TYPE_AOI221 = 0x15 | INPUTS_5;
	public static final int TYPE_AOI222 = 0x16 | INPUTS_6;
	public static final int TYPE_AOI22 = 0x17 | INPUTS_4;

	public static final int TYPE_OA21 = 0x20 | INPUTS_3;
	public static final int TYPE_OA221 = 0x21 | INPUTS_5;
	public static final int TYPE_OA222 = 0x22 | INPUTS_6;
	public static final int TYPE_OA22 = 0x23 | INPUTS_4;
	public static final int TYPE_OAI21 = 0x24 | INPUTS_3;
	public static final int TYPE_OAI221 = 0x25 | INPUTS_5;
	public static final int TYPE_OAI222 = 0x26 | INPUTS_6;
	public static final int TYPE_OAI22 = 0x27 | INPUTS_4;
	public static final int TYPE_OAI21_NANGATE = 0x28 | INPUTS_3;

	public static final int TYPE_MUX21 = 0x30 | INPUTS_3;
	public static final int TYPE_MUX41 = 0x31 | INPUTS_6;
	public static final int TYPE_CGLPPR = 0x32 | INPUTS_3;

	public static final int TYPE_DEC24 = 0x40 | FLAG_MULTIOUTPUT | INPUTS_2;
	public static final int TYPE_HADD = 0x41 | FLAG_MULTIOUTPUT | INPUTS_2;
	public static final int TYPE_FADD = 0x42 | FLAG_MULTIOUTPUT | INPUTS_3;

	public static final int TYPE_DFFAR = 0x50 | FLAG_MULTIOUTPUT | FLAG_SEQUENTIAL | INPUTS_3;
	public static final int TYPE_SDFFAR = 0x51 | FLAG_MULTIOUTPUT | FLAG_SEQUENTIAL | INPUTS_5;
	public static final int TYPE_SDFFASR = 0x52 | FLAG_MULTIOUTPUT | FLAG_SEQUENTIAL | INPUTS_6;

	public static final int TYPE_AOBUF = TYPE_BUF | INPUTS_1 | VARIANT_1;
	public static final int TYPE_DELLN = TYPE_BUF | INPUTS_1 | VARIANT_2;
	public static final int TYPE_NBUFF = TYPE_BUF | INPUTS_1 | VARIANT_3;
	public static final int TYPE_AOINV = TYPE_NOT | INPUTS_1 | VARIANT_1;
	public static final int TYPE_IBUFF = TYPE_NOT | INPUTS_1 | VARIANT_2;
	public static final int TYPE_INV = TYPE_NOT | INPUTS_1 | VARIANT_3;

	public final boolean isPseudo() {
		return (type & (FLAG_PSEUDO)) != 0;
	}

	public final boolean isSequential() {
		return (type & (FLAG_SEQUENTIAL)) != 0;
	}

	public final boolean isInput() {
		return (type & FLAG_INPUT) != 0;
	}

	public final boolean isOutput() {
		return (type & FLAG_OUTPUT) != 0;
	}

	public final boolean isPort() {
		return (isInput() || isOutput());
	}

	/**
	 * 
	 * By default, each cell in the graph also contains a branching point. I.e.
	 * all outgoing signal connections to the successor nodes are considered
	 * branches of the same signal. This default behavior can be turned off by
	 * setting the flag FLAG_MULTIOUTPUT on a cell. Outputs of nodes with this
	 * flag are considered different and explicit pseudo nodes have to be added
	 * to fan-out from each of the distinct outputs.
	 * 
	 * @return
	 */
	public final boolean isMultiOutput() {
		return (type & FLAG_MULTIOUTPUT) != 0;
	}

	public abstract int inputCount();

	public abstract int outputCount();

	public abstract Iterable<? extends Cell> inputCells();

	public abstract Iterable<? extends Cell> outputCells();

	public abstract Cell inputCellAt(int pinIndex);

	public abstract Cell outputCellAt(int pinIndex);

	public abstract int inputSignalAt(int pinIndex);

	public abstract int outputSignalAt(int pinIndex);

	public abstract String name();

	public abstract String typeName();
	
	public abstract String inputName(int pinIndex);

	public abstract String outputName(int pinIndex);

	public abstract boolean isType(int type);

	class SignalIterator implements Iterator<Integer> {

		private boolean forInput;
		private int index;

		public SignalIterator(boolean forInput) {
			this.forInput = forInput;
			index = 0;
		}

		@Override
		public boolean hasNext() {
			if (forInput) {
				return index < inputCount();
			} else {
				return index < outputCount();
			}
		}

		@Override
		public Integer next() {
			index++;
			if (forInput) {
				return inputSignalAt(index - 1);
			} else {
				return outputSignalAt(index - 1);
			}
		}

	}

	class SignalIterable implements Iterable<Integer> {
		private boolean forInput;

		public SignalIterable(boolean forInput) {
			this.forInput = forInput;
		}

		@Override
		public Iterator<Integer> iterator() {
			return new SignalIterator(forInput);
		}

	}

	public Iterable<Integer> inputSignals() {
		return new SignalIterable(true);
	}

	public Iterable<Integer> outputSignals() {
		return new SignalIterable(false);
	}

}
