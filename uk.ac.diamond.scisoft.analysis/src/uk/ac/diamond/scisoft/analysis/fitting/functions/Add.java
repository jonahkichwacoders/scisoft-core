/*-
 * Copyright 2013 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis.fitting.functions;

/**
 * Add several functions
 */
public class Add extends ANaryOperator implements IOperator {
	private static final String cname = "Add";

	public Add() {
		super();
		name = cname;
	}

	@Override
	public double val(double... values) {
		double y = 0;
		for (int i = 0, imax = getNoOfFunctions(); i < imax; i++) {
			IFunction f = getFunction(i);
			if (f != null) {
				y += f.val(values);
			}
		}

		return y;
	}

	@Override
	protected double internalDerivative(IParameter param, double... values) {
		double d = 0;

		for (int i = 0, imax = getNoOfFunctions(); i < imax; i++) {
			IFunction f = getFunction(i);
			d += f.partialDeriv(param, values);
		}

		return d;
	}
}