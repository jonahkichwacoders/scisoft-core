/*-
 * Copyright 2014 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.diffraction;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import org.eclipse.dawnsci.analysis.api.diffraction.DetectorProperties;

/**
 * A two-circle detector - the detector is mounted on one arm can rotated about a point on a second
 * arm which also can rotate about a fixed base.
 */
public class TwoCircleDetector implements Cloneable {

	// all dimensions in millimetres, angles and offsets in degrees

	// laboratory frame is sited on base of gamma rotation axis that is defined as (0, 0, 1)

	Vector3d beamDir;
	Vector3d beamPos; // relative to lab frame
	double gammaOff;
	double deltaOff;
	Vector3d deltaDir; // relative to gamma frame
	Vector3d deltaPos; // base of delta rotation axis relative to lab frame
	Vector3d detectorPos; // relative to delta frame
	Matrix3d detectorOri; // relative to delta frame

	Matrix3d orientation; // detector orientation relative to lab frame
	Vector3d origin; // position of detector origin relative to beam

	private static final double detectorOffset = Math.toRadians(9);
	private static final double circle = 1000;

	/**
	 * create forward transformation for a two-circle mounting of a detector
	 * 
	 */
	public TwoCircleDetector() {
		beamDir = new Vector3d(0, 0, 1);
		beamPos = new Vector3d(0, circle, 0);
		gammaOff = 0;
		deltaOff = 0;
		deltaDir = new Vector3d(-1, 0, 0);
		deltaPos = new Vector3d(-circle, circle, 0);
		detectorPos = new Vector3d(circle, 0, circle);
		detectorOri = MatrixUtils.computeOrientation(new Vector3d(0, -Math.sin(detectorOffset), -Math.cos(detectorOffset)),
				new Vector3d(1, 0, 0));
	}

	TwoCircleDetector(TwoCircleDetector other) {
		beamDir = (Vector3d) other.beamDir.clone();
		beamPos = (Vector3d) other.beamPos.clone();
		gammaOff = other.gammaOff;
		deltaOff = other.deltaOff;
		deltaDir = (Vector3d) other.deltaDir.clone();
		deltaPos = (Vector3d) other.deltaPos.clone();
		detectorPos = (Vector3d) other.detectorPos.clone();
		detectorOri = (Matrix3d) other.detectorOri.clone();
	}

	@Override
	protected TwoCircleDetector clone() {
		return new TwoCircleDetector(this);
	}

	/**
	 * Set beam with a position and direction
	 * @param position (in mm)
	 * @param direction
	 */
	public void setBeam(Vector3d position, Vector3d direction) {
		beamPos = position;
		beamDir = direction;
	}

	/**
	 * Set gamma circle with angular offset
	 * @param offset (in degrees)
	 */
	public void setGamma(double offset) {
		gammaOff = offset;
	}

	/**
	 * Set delta circle with axis position and direction, and angular offset
	 * @param position (in mm)
	 * @param direction
	 * @param offset (in degrees)
	 */
	public void setDelta(Vector3d position, Vector3d direction, double offset) {
		deltaPos = position;
		deltaDir = direction;
		deltaOff = offset;
	}

	/**
	 * Set detector face with position of its origin [(0,0) point], normal direction,
	 * and fast axis direction
	 * @param position (in mm)
	 * @param normal
	 * @param fast (or component of it if it is not perpendicular to the normal)
	 */
	public void setDetector(Vector3d position, Vector3d normal, Vector3d fast) {
		detectorPos = position;
		detectorOri = MatrixUtils.computeOrientation(normal, fast);
	}

	/**
	 * Set detector face with position of its origin [(0,0) point], normal direction,
	 * and angle of fast axis (up from horizontal)
	 * @param position (in mm)
	 * @param normal
	 * @param angle (in degrees)
	 */
	public void setDetector(Vector3d position, Vector3d normal, double angle) {
		detectorPos = position;
		detectorOri = MatrixUtils.computeOrientation(normal, MatrixUtils.computeIntersection(normal, angle));
	}

	/**
	 * Calculate detector setup
	 */
	private void calc(double gamma, double delta) {
		if (beamDir == null || beamPos == null || deltaDir == null || deltaPos == null || detectorOri== null || detectorPos == null) {
			throw new IllegalStateException("Some vectors have not been defined");
		}

		// start at detector which is defined relative to delta frame
		Matrix3d rDelta = new Matrix3d();
		rDelta.set(new AxisAngle4d(deltaDir, Math.toRadians(delta + deltaOff)));

		origin = (Vector3d) detectorPos.clone();
		rDelta.transform(origin);

		Matrix3d rGamma = new Matrix3d();
		rGamma.set(new AxisAngle4d(new double[] {0, 1, 0, Math.toRadians(gamma + gammaOff)}));
		origin.add(deltaPos);
		rGamma.transform(origin);
		origin.sub(beamPos); // as detector properties has its frame based at the sample-beam intersection

		orientation = new Matrix3d();
		orientation.mul(rDelta, detectorOri);
		orientation.mul(rGamma, orientation);
		orientation.normalize();
		MatrixUtils.santise(orientation);
		orientation.transpose(); // make inverse as that's what detector properties needs
	}

	/**
	 * @param old
	 * @param gamma (in degrees)
	 * @param delta (in degrees)
	 */
	public void updateDetectorProperties(DetectorProperties old, double gamma, double delta) {
		calc(gamma, delta);
		old.setOrigin(origin);
		old.setOrientation(orientation);
		old.setBeamVector(beamDir);
	}

	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		out.append("beam d: ");
		out.append(beamDir);
		out.append(", p: ");
		out.append(beamPos);
		out.append(",\ndelta d: ");
		out.append(deltaDir);
		out.append(", a: ");
		out.append(deltaOff);
		out.append(", p: ");
		out.append(deltaPos);
		out.append("\ndet d: ");
		out.append(detectorOri);
		out.append("p: ");
		out.append(detectorPos);
		out.append("\no: ");
		out.append(origin);
		out.append("\n");
		out.append(orientation);

		return out.toString();
	}

	public boolean isClose(TwoCircleDetector other, final double rel, final double abs) {
		return MatrixUtils.isClose(beamDir, other.beamDir, rel, abs) && MatrixUtils.isClose(beamPos, other.beamPos, rel, abs)
				&& MatrixUtils.isClose(gammaOff, other.gammaOff, rel, abs) && MatrixUtils.isClose(deltaOff, other.deltaOff, rel, abs)
				&& MatrixUtils.isClose(deltaDir, other.deltaDir, rel, abs) && MatrixUtils.isClose(deltaPos, other.deltaPos, rel, abs)
				&& MatrixUtils.isClose(detectorPos, other.detectorPos, rel, abs) && MatrixUtils.isClose(detectorOri, other.detectorOri, rel, abs);
	}

	static Vector3d createDirection(double a, double b) {
		double theta = Math.toRadians(a);
		double phi = Math.toRadians(b);
		double st = Math.sin(theta);
		return new Vector3d(st*Math.cos(phi), st*Math.sin(phi), Math.cos(theta));
	}

	/**
	 * Setup two circle detector in a variety of ways (0 <= t <= 90, -180 < p <= 180, -90 < axis angle <= 90)
	 * <dl>
	 * <dt>8-parameter</dt>
	 * <dd>beam dir (t,p),
	 *  detector pos, detector normal, detector fast axis angle from horizontal</dd>
	 * <dt>10-parameter</dt>
	 * <dd>beam dir (t,p), delta dir,
	 *  detector pos, detector normal, detector fast axis angle from horizontal</dd>
	 * <dt>18-parameter</dt>
	 * <dd>	beam pos (x,y,z), beam dir (t,p), gamma offset,
	 *  delta pos, delta dir, delta offset,
	 *  detector pos, detector normal, detector fast axis angle from horizontal</dd>
	 * </dl>
	 * @param two
	 * @param p only array lengths of 8, 10, 18 are supported
	 */
	public static void setupTwoCircle(TwoCircleDetector two, double... p) {
		int i = 0;
		switch (p.length) {
		case 8:
			two.setBeam(two.beamPos, createDirection(p[i++], p[i++]));
			two.setGamma(two.gammaOff);
			two.setDelta(two.deltaPos, two.deltaDir, two.deltaOff);
			break;
		case 10:
			two.setBeam(two.beamPos, createDirection(p[i++], p[i++]));
			two.setGamma(two.gammaOff);
			two.setDelta(two.deltaPos, createDirection(p[i++], p[i++]), two.deltaOff);
			break;
		case 18:
			two.setBeam(new Vector3d(p[i++], p[i++], p[i++]), createDirection(p[i++], p[i++]));
			two.setGamma(p[i++]);
			two.setDelta(new Vector3d(p[i++], p[i++], p[i++]), createDirection(p[i++], p[i++]), p[i++]);
			break;
		default:
			throw new IllegalArgumentException("Number of parameters not specified correctly");
		}
		two.setDetector(new Vector3d(p[i++], p[i++], p[i++]), createDirection(p[i++], p[i++]), p[i++]);
	}

	private static double[] getAngles(Vector3d v) {
		return new double[] {Math.toDegrees(Math.acos(v.z)), Math.toDegrees(Math.atan2(v.y, v.x))};
	}

	/**
	 * Get parameters from two circle detector (0 <= t <= 90, -180 < p <= 180, -90 < axis angle <= 90) 
	 * <dl>
	 * <dt>8-parameter</dt>
	 * <dd>beam dir (t,p),
	 *  detector pos, detector normal, detector fast axis angle from horizontal</dd>
	 * <dt>10-parameter</dt>
	 * <dd>beam dir (t,p), delta dir,
	 *  detector pos, detector normal, detector fast axis angle from horizontal</dd>
	 * <dt>18-parameter</dt>
	 * <dd>	beam pos (x,y,z), beam dir (t,p), gamma offset,
	 *  delta pos, delta dir, delta offset,
	 *  detector pos, detector normal, detector fast axis angle from horizontal</dd>
	 * </dl>
	 * @param two
	 * @param n only values of 8, 10, 18 are supported
	 * @return parameters
	 */
	public static double[] getTwoCircleParameters(TwoCircleDetector two, int n) {
		double[] params = new double[n];
		int i = 0;
		double[] a;
		Vector3d v;
		switch (n) {
		case 8:
			a = getAngles(two.beamDir);
			params[i++] = a[0];
			params[i++] = a[1];
			break;
		case 10:
			a = getAngles(two.beamDir);
			params[i++] = a[0];
			params[i++] = a[1];
			a = getAngles(two.deltaDir);
			params[i++] = a[0];
			params[i++] = a[1];
			break;
		case 18:
			params[i++] = two.beamPos.x;
			params[i++] = two.beamPos.y;
			params[i++] = two.beamPos.z;
			a = getAngles(two.beamDir);
			params[i++] = a[0];
			params[i++] = a[1];
			params[i++] = two.gammaOff;
			params[i++] = two.deltaPos.x;
			params[i++] = two.deltaPos.y;
			params[i++] = two.deltaPos.z;
			a = getAngles(two.deltaDir);
			params[i++] = a[0];
			params[i++] = a[1];
			params[i++] = two.deltaOff;
			break;
		default:
			throw new IllegalArgumentException("Number of parameters not specified correctly");
		}
	
		params[i++] = two.detectorPos.x;
		params[i++] = two.detectorPos.y;
		params[i++] = two.detectorPos.z;
		v = new Vector3d(0, 0, -1);
		two.detectorOri.transform(v);
		a = getAngles(v);
		params[i++] = a[0];
		params[i++] = a[1];
		v.set(-1, 0, 0);
		two.detectorOri.transform(v);
		params[i++] = Math.toDegrees(Math.asin(v.y));
//		System.err.println("Params: " + Arrays.toString(params));
		return params;
	}
}
