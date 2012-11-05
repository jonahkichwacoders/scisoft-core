/*-
 * Copyright 2012 Diamond Light Source Ltd.
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

package uk.ac.diamond.scisoft.analysis.roi;

import java.util.Arrays;

/**
 * A circular region of interest
 */
public class CircularROI extends ROIBase {
	private double rad;

	/**
	 * Create a unit circle centred on origin
	 */
	public CircularROI() {
		this(1, 0, 0);
	}

	/**
	 * Create circle of given radius centred on origin
	 * @param radius
	 */
	public CircularROI(double radius) {
		this(radius, 0, 0);
	}

	/**
	 * Create circle of given radius with specified centre
	 * @param radius
	 * @param ptx centre point x value
	 * @param pty centre point y value
	 */
	public CircularROI(double radius, double ptx, double pty) {
		spt = new double[] { ptx, pty };
		rad = Math.abs(radius);
	}

	@Override
	public void downsample(double subFactor) {
		super.downsample(subFactor);
		rad /= subFactor;
	}

	/**
	 * @return radius
	 */
	public double getRadius() {
		return rad;
	}

	/**
	 * Set radius
	 * @param radius
	 */
	public void setRadius(double radius) {
		rad = radius;
	}

	/**
	 * Get point on circle at given angle
	 * @param angle in radians
	 * @return point 
	 */
	public double[] getPoint(double angle) {
		return new double[] { spt[0] + rad*Math.cos(angle), 
				spt[1] + rad*Math.sin(angle)};
	}

	/**
	 * Get point on circle at given angle
	 * @param angle in degrees
	 * @return point 
	 */
	public double[] getPointDegrees(double angle) {
		return getPoint(Math.toRadians(angle));
	}

	@Override
	public String toString() {
		return String.format("Centre %s Radius %g", Arrays.toString(spt), rad);
	}
}