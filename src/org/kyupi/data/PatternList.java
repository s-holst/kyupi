package org.kyupi.data;

import java.util.ArrayList;

import org.kyupi.data.item.QVector;
import org.kyupi.data.source.QVSource;

public interface PatternList {

	public abstract QVSource getStimuliSource();

	public abstract QVSource getResponsesSource();

	public abstract ArrayList<QVector> getStimuliArray();

	public abstract ArrayList<QVector> getResponsesArray();

	public abstract int[] getScanMap();
}
