/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.dataset.function;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.complex.Complex;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.function.DatasetToNumberFunction;

/**
 * Example of finding the totals in an array of datasets
 */
public class Sum implements DatasetToNumberFunction {

	@Override
	public List<Number> value(IDataset... datasets) {
		if (datasets.length == 0)
			return null;

		List<Number> result = new ArrayList<Number>();
		for (IDataset d : datasets) {
			Object value = DatasetUtils.convertToDataset(d).sum();
			if (value instanceof Complex) {
				result.add(((Complex) value).getReal());
				result.add(((Complex) value).getImaginary());
			} else if (value instanceof Number)
				result.add((Number) value);
			else {
				throw new IllegalArgumentException("Type of return value from sum not supported");
			}
		}
		return result;
	}

}
