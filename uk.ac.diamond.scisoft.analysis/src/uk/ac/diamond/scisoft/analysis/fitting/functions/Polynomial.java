/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.fitting.functions;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.complex.Complex;
import org.ddogleg.solver.PolynomialOps;
import org.ddogleg.solver.PolynomialRoots;
import org.ddogleg.solver.RootFinderType;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IParameter;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Maths;
import org.ejml.data.Complex64F;

/**
 * Class that wrappers the equation <br>
 * y(x) = a_0 x^n + a_1 x^(n-1) + a_2 x^(n-2) + ... + a_(n-1) x + a_n
 */
public class Polynomial extends AFunction {
	private static final String NAME = "Polynomial";
	private static final String DESC = "y(x) = a_0 x^n + a_1 x^(n-1) + a_2 x^(n-2) + ... + a_(n-1) x + a_n";
	private double[] a;
	private String[] paramNames;
	private int nparams; // actually degree + 1

	/**
	 * Basic constructor, not advisable to use
	 */
	public Polynomial() {
		this(0);
	}

	/**
	 * Make a polynomial of given degree (0 - constant, 1 - linear, 2 - quadratic, etc)
	 * @param degree
	 */
	public Polynomial(final int degree) {
		super(degree + 1);
		a = new double[degree+1];
		fillParameters(a);

		setNames();
	}

	/**
	 * Make a polynomial with given parameters
	 * @param params
	 */
	public Polynomial(double[] params) {
		super(params);

		setNames();
	}

	/**
	 * Make a polynomial with given parameters
	 * @param params
	 */
	public Polynomial(IParameter... params) {
		super(params);

		setNames();
	}

	/**
	 * Constructor that allows for the positioning of all the parameter bounds
	 * 
	 * @param min
	 *            minimum boundaries
	 * @param max
	 *            maximum boundaries
	 */
	public Polynomial(double[] min, double[] max) {
		super(0);
		if (min.length != max.length) {
			throw new IllegalArgumentException("");
		}
		nparams = min.length;
		parameters = new Parameter[nparams];
		a = new double[nparams];

		for (int i = 0; i < nparams; i++) {
			a[i] = 0.5*(min[i] + max[i]);
			parameters[i] = new Parameter(a[i], min[i], max[i]);
		}

		setNames();
	}

	private void setNames() {
		name = NAME;
		description = DESC;
		for (int i = 0; i < paramNames.length; i++) {
			IParameter p = getParameter(i);
			p.setName(paramNames[i]);
		}
	}

	@Override
	protected void fillParameters(double... params) {
		super.fillParameters(params);
		nparams = getNoOfParameters();
		a = new double[nparams];
		paramNames = new String[nparams];
		for (int i = 0; i < nparams; i++) {
			a[i] = params[i];
			paramNames[i] = "Parameter" + i;
		}
	}

	@Override
	protected void fillParameters(IParameter... params) {
		super.fillParameters(params);
		nparams = getNoOfParameters();
		a = new double[nparams];
		paramNames = new String[nparams];
		for (int i = 0; i < paramNames.length; i++) {
			a[i] = params[i].getValue();
			paramNames[i] = "Parameter" + i;
		}
	}

	private void calcCachedParameters() {
		for (int i = 0; i < nparams; i++)
			a[i] = getParameterValue(i);

		setDirty(false);
	}

	@Override
	public double val(double... values) {
		if (isDirty())
			calcCachedParameters();

		final double position = values[0];

		double v = a[0];
		for (int i = 1; i < nparams; i++) {
			v = v * position + a[i];
		}
		return v;
	}

	@Override
	public void fillWithValues(DoubleDataset data, CoordinatesIterator it) {
		if (isDirty())
			calcCachedParameters();

		it.reset();
		double[] coords = it.getCoordinates();
		int i = 0;
		double[] buffer = data.getData();
		while (it.hasNext()) {
			double v = a[0];
			double p = coords[0];
			for (int j = 1; j < nparams; j++) {
				v = v * p + a[j];
			}
			buffer[i++] = v;
		}
	}

	@Override
	public double partialDeriv(IParameter parameter, double... position) {
		if (isDuplicated(parameter))
			return super.partialDeriv(parameter, position);

		int i = indexOfParameter(parameter);
		if (i < 0)
			return 0;

		final double pos = position[0];
		final int n = nparams - 1 - i;
		switch (n) {
		case 0:
			return 1.0;
		case 1:
			return pos;
		case 2:
			return pos * pos;
		default:
			return Math.pow(pos, n);
		}
	}


	@Override
	public void fillWithPartialDerivativeValues(IParameter parameter, DoubleDataset data, CoordinatesIterator it) {
		Dataset pos = DatasetUtils.convertToDataset(it.getValues()[0]);

		final int n = nparams - 1 - indexOfParameter(parameter);
		switch (n) {
		case 0:
			data.fill(1);
			break;
		case 1:
			data.setSlice(pos);
			break;
		case 2:
			Maths.square(pos, data);
			break;
		default:
			Maths.power(pos, n, data);
			break;
		}
	}

	/**
	 * Create a 2D dataset which contains in each row a coordinate raised to n-th powers.
	 * <p>
	 * This is for solving the linear least squares problem 
	 * @param coords
	 * @return matrix
	 */
	public DoubleDataset makeMatrix(Dataset coords) {
		final int rows = coords.getSize();
		DoubleDataset matrix = new DoubleDataset(rows, nparams);

		for (int i = 0; i < rows; i++) {
			final double x = coords.getDouble(i);
			double v = 1.0;
			for (int j = nparams - 1; j >= 0; j--) {
				matrix.setItem(v, i, j);
				v *= x;
			}
		}

		return matrix;
	}

	/**
	 * Set the degree after a class instantiation
	 * @param degree
	 */
	public void setDegree(int degree) {
		parameters = new Parameter[degree + 1];
		for (int i = 0; i < degree + 1; i++) {
			parameters[i] = new Parameter();
		}
		a = new double[degree + 1];
		nparams = degree + 1;
		name = NAME;
		description = DESC;
		fillParameters(a);
		if (parent != null)
			parent.updateParameters();
	}
	
	public String getStringEquation(){
		
		StringBuilder out = new StringBuilder();
		
		DecimalFormat df = new DecimalFormat("0.#####E0");
		
		for (int i = nparams-1; i >= 2; i--) {
			out.append(df.format(parameters[nparams - 1 -i].getValue()));
			out.append(String.format("x^%d + ", i));
		}
		
		if (nparams >= 2)
			out.append(df.format(parameters[nparams-2].getValue()) + "x + ");
		if (nparams >= 1)
			out.append(df.format(parameters[nparams-1].getValue()));
		
		return out.toString();
	}

	/**
	 * Find all roots
	 * @return all roots or null if there is any problem finding the roots
	 */
	public Complex[] findRoots() {
		return findRoots(a);
	}

	/**
	 * Find all roots
	 * @param coeffs
	 * @return all roots or null if there is any problem finding the roots
	 */
	public static Complex[] findRoots(double... coeffs) {
		double[] reverse = coeffs.clone();
		ArrayUtils.reverse(reverse);
		double max = Double.NEGATIVE_INFINITY;
		for (double r : reverse) {
			max = Math.max(max, Math.abs(r));
		}
		for (int i = 0; i < reverse.length; i++) {
			reverse[i] /= max;
		}

		org.ddogleg.solver.Polynomial p = org.ddogleg.solver.Polynomial.wrap(reverse);
		PolynomialRoots rf = PolynomialOps.createRootFinder(p.computeDegree(), RootFinderType.EVD);
		if (rf.process(p)) {
			// reorder to NumPy's roots output
			List<Complex64F> rts = rf.getRoots();
			Complex[] out = new Complex[rts.size()];
			int i = 0;
			for (Complex64F r : rts) {
				out[i++] = new Complex(r.getReal(), r.getImaginary());
			}
			return sort(out);
		}

		return null;
	}

	private static Complex[] sort(Complex[] values) {
		// reorder to NumPy's roots output
		List<Complex> rts = Arrays.asList(values);
		Collections.sort(rts, new Comparator<Complex>() {
			@Override
			public int compare(Complex o1, Complex o2) {
				double a = o1.getReal();
				double b = o2.getReal();
				
				double u = 10*Math.ulp(Math.max(Math.abs(a), Math.abs(b)));
				if (Math.abs(a - b) > u)
					return a < b ? -1 : 1;

				a = o1.getImaginary();
				b = o2.getImaginary();
				if (a == b)
					return 0;
				return a < b ? 1 : -1;
			}
		});

		return rts.toArray(new Complex[0]);
	}

	public double[] getA() {
		return a;
	}

	public void setA(double[] a) {
		this.a = a;
	}

	public String[] getParamNames() {
		return paramNames;
	}

	public void setParamNames(String[] paramNames) {
		this.paramNames = paramNames;
	}

	public int getNparams() {
		return nparams;
	}

	public void setNparams(int nparams) {
		this.nparams = nparams;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(a);
		result = prime * result + nparams;
		result = prime * result + Arrays.hashCode(paramNames);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Polynomial other = (Polynomial) obj;
		if (!Arrays.equals(a, other.a))
			return false;
		if (nparams != other.nparams)
			return false;
		if (!Arrays.equals(paramNames, other.paramNames))
			return false;
		return true;
	}

}
