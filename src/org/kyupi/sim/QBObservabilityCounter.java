package org.kyupi.sim;

import java.util.Arrays;

import org.kyupi.circuit.Graph;
import org.kyupi.data.item.QBlock;
import org.kyupi.data.source.QBSource;

public class QBObservabilityCounter extends QBSource {

	private QBSource source;
	private Observability obscalc;
	private int[] obsCount0;
	private int[] obsCount1;
	private int dropThreshold = Integer.MAX_VALUE;

	public QBObservabilityCounter(Graph circuit, QBSource source) {
		super(source.length());
		this.source = source;
		this.obscalc = new Observability(circuit);
		obsCount0 = new int[circuit.accessSignalMap().length()];
		obsCount1 = new int[circuit.accessSignalMap().length()];
		Arrays.fill(obsCount0, 0);
		Arrays.fill(obsCount1, 0);
	}
	
	public void setDropThreshold(int ndetects) {
		dropThreshold = ndetects;
	}

	@Override
	public void reset() {
		source.reset();
		Arrays.fill(obsCount0, 0);
		Arrays.fill(obsCount1, 0);
	}

	@Override
	protected QBlock compute() {
		if (!source.hasNext())
			return null;
		QBlock s = source.next();
		obscalc.loadInputsFrom(s);
		
		for (int i = 0; i < obsCount0.length; i++) {
			if (obsCount0[i] >= dropThreshold && obsCount1[i] >= dropThreshold)
				continue;
			long obs = obscalc.getObservability(i);
			long val = obscalc.getV(i);
			long obs0 = obs & val;
			long obs1 = obs & ~val;
			obsCount0[i] += Long.bitCount(obs0);
			obsCount1[i] += Long.bitCount(obs1);
		}

		obscalc.storeOutputsTo(s);
		return s;

	}
	
	public int length() {
		return obsCount0.length;
	}
	
	public int getSA0ObsCount(int sig_idx) {
		return obsCount0[sig_idx];
	}

	public int getSA1ObsCount(int sig_idx) {
		return obsCount1[sig_idx];
	}

	public int countSAFaultsDetectedAtLeast(int threshold) {

		int sum = 0;
		
		for (int i = 0; i < obsCount0.length; i++) {
			if (obsCount0[i] >= threshold) {
				sum++;
			}
			if (obsCount1[i] >= threshold) {
				sum++;
			}
		}
		return sum;
	}

		
}
