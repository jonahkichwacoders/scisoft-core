/*
 * Copyright 2011 Diamond Light Source Ltd.
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
 * Class that wrappers the Lorentzian function (aka Breit-Wigner or Cauchy distribution) <br>
 * y(x) = A x(half)^2 / ( x(half)^2 + (x-a)^2 ) <br>
 * where : <br>
 * A is the height<br>
 * a is the position of the peak.<br>
 * and <br>
 * x(half) is the half width at half maximum, known as gamma
 */
public class Lorentzian extends APeak {
	private static final String cname = "Lorentzian";
	private static final String cdescription = "y(x) = A x(half)^2 / ( x(half)^2 + (x-a)^2 )\nwhere A is the height\na is the position of the peak\nx(half) is the half width at half maximum, known as gamma";
	private static String[] paramNames = new String[]{"posn", "fwhm", "area"};
	private static final double[] params = new double[] { 0, 0, 0 };

	public Lorentzian() { 
		this(params);
	}

	/**
	 * Constructor which takes the three properties required, which are
	 * 
	 * <pre>
	 *     Parameter 1	- Position
	 *     Parameter 2 	- half width at half maximum
	 *     Parameter 3 	- Area
	 * </pre>
	 * 
	 * @param params
	 */
	public Lorentzian(double... params) {
		super(params);

		setNames();
	}

	public Lorentzian(IParameter... params) {
		super(params);

		setNames();
	}

	public Lorentzian(IdentifiedPeak peakParameters) {
		super(3);

		double range = peakParameters.getMaxXVal()-peakParameters.getMinXVal();
		getParameter(0).setValue(peakParameters.getPos());
		getParameter(0).setLowerLimit(peakParameters.getMinXVal());//-range);
		getParameter(0).setUpperLimit(peakParameters.getMaxXVal());//+range);

		// height
		getParameter(1).setLowerLimit(0);
		getParameter(1).setUpperLimit(peakParameters.getHeight()*2);
		getParameter(1).setValue(peakParameters.getHeight());

		//fwhm
		getParameter(2).setLowerLimit(0);
		getParameter(2).setUpperLimit(range*2);
		getParameter(2).setValue(peakParameters.getFWHM()/4);

		setNames();
	}

	/**
	 * Constructor which takes more sensible values for the parameters, which also incorporates the limits which they
	 * can be in, reducing the overall complexity of the problem
	 * 
	 * @param minPeakPosition
	 *            The minimum value of the peak position
	 * @param maxPeakPosition
	 *            The maximum value of the peak position
	 * @param maxHeight
	 *            The maximum height of the peak
	 * @param maxHalfWidth
	 *            The maximum half width at half maximum
	 */
	public Lorentzian(double minPeakPosition, double maxPeakPosition, double maxHeight, double maxHalfWidth) {
		super(3);

		getParameter(0).setValue((minPeakPosition + maxPeakPosition) / 2.0);
		getParameter(0).setLowerLimit(minPeakPosition);
		getParameter(0).setUpperLimit(maxPeakPosition);

		getParameter(1).setLowerLimit(0.0);
		getParameter(1).setUpperLimit(maxHeight);
		getParameter(1).setValue(maxHeight / 2.0);

		getParameter(2).setLowerLimit(0.0);
		getParameter(2).setUpperLimit(maxHalfWidth);
		// better fitting is generally found if sigma expands into the peak.
		getParameter(2).setValue(maxHalfWidth / 10.0);

		setNames();
	}

	private void setNames() {
		name = cname;
		description = cdescription;
		for (int i = 0; i < paramNames.length; i++) {
			IParameter p = getParameter(i);
			p.setName(paramNames[i]);
		}
	}

	double hwhm, hwhm_sq, mean, one_by_pi, area;

	private void calcCachedParameters() {
		mean = getParameterValue(0);
		double FWHM = getParameterValue(1);
		area = getParameterValue(2);
		
		hwhm = FWHM/2.0;
		hwhm_sq = hwhm*hwhm;
		one_by_pi = 1.0/Math.PI;

		setDirty(false);
	}

	@Override
	public double val(double... values) {
		if (isDirty())
			calcCachedParameters();

		double position = values[0];
		double dist = position - mean;
		double result = one_by_pi * (hwhm / ((dist*dist) + hwhm_sq));
		return area * result;
	}

	@Override
	public double getArea() {
		return getParameter(2).getValue(); 
	}

	@Override
	public double getFWHM() {
		return getParameter(1).getValue();
	}

	@Override
	public double getPosition() {
		return getParameter(0).getValue();
	}

}
