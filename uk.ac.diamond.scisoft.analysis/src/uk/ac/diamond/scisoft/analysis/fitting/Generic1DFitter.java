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

package uk.ac.diamond.scisoft.analysis.fitting;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.IAnalysisMonitor;
import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Maths;
import uk.ac.diamond.scisoft.analysis.fitting.functions.APeak;
import uk.ac.diamond.scisoft.analysis.fitting.functions.CompositeFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IdentifiedPeak;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Offset;
import uk.ac.diamond.scisoft.analysis.optimize.GeneticAlg;
import uk.ac.diamond.scisoft.analysis.optimize.IOptimizer;

public class Generic1DFitter implements Serializable {

	public static int defaultSmoothing = 3;
	public static double defaultAccuracy = 0.0001;
	public static IOptimizer defaultOptimiser = new GeneticAlg(defaultAccuracy);
	private static double epslion = 1E-5;
	transient protected static final Logger logger = LoggerFactory.getLogger(Generic1DFitter.class);

	/**
	 * This method takes a dataset of Y values and fits the specified peaks to the data. The x values of the Y axis are
	 * assumed to be linear with unit increment. The APeak function specified will be returned in the list of
	 * FittedPeaks. numPeaks is the maximum number of peaks that will be fitted.
	 * 
	 * @param currentDataset
	 *            - the y values of a 1D data set that is to be fitted. Assumes that the y points are evenly spaced and
	 *            1 unit apart
	 * @param function
	 *            - A function that obeys the APeak interface
	 * @param numPeaks
	 *            - The maximum number of peaks that are fitted.
	 * @return list of FittedPeaks - an object that contain the fitted APeak objects and the corresponding objects that
	 *         describe the region of the data where the peak was found
	 */
	public static List<CompositeFunction> fitPeaks(AbstractDataset currentDataset, APeak function, int numPeaks) {
		DoubleDataset xData = DoubleDataset.arange(currentDataset.getSize());
		return fitPeaks(xData, currentDataset, function, numPeaks);
	}

	/**
	 * This method fits peaks to a dataset describing the y values at specified x values. The APeak function specified
	 * will be returned in the list of FittedPeaks. numPeaks is the maximum number of peaks that will be fitted.
	 * 
	 * @param xdata
	 *            - the x values that of the measurements given in ydata
	 * @param ydata
	 *            - the y values corresponding to the x values given
	 * @param function
	 *            - A function that obeys the APeak interface
	 * @param numPeaks
	 *            - The maximum number of peaks that are fitted.
	 * @return list of FittedPeaks - an object that contain the fitted APeak objects and the corresponding objects that
	 *         describe the region of the data where the peak was found
	 */
	public static List<CompositeFunction> fitPeaks(AbstractDataset xdata, AbstractDataset ydata, APeak function, int numPeaks) {
		int tempSmooting = (int) (xdata.getSize() * 0.01);
		int smoothing;
		if (tempSmooting > defaultSmoothing) {
			smoothing = tempSmooting;
		} else {
			smoothing = defaultSmoothing;
		}
		return fitPeaks(xdata, ydata, function, defaultOptimiser, smoothing, numPeaks);

	}

	/**
	 * This method fits peaks to a dataset describing the y values at specified x values. The APeak function specified
	 * will be returned in the list of FittedPeaks. numPeaks is the maximum number of peaks that will be fitted. The
	 * method to fit the peaks can be specified provided that the method obeys the IOptomiser interface. The smoothing
	 * is the value given when calculating the differential of the data being fitted.
	 * 
	 * @param xdata
	 *            - the x values of the measurements given in ydata
	 * @param ydata
	 *            - the y values corresponding to the x values given
	 * @param function
	 *            - A function that obeys the APeak interface
	 * @param optimiser
	 *            - An optimisation function that obeys the IOptimizer interface
	 * @param smoothing
	 *            - The value applied to the differential of the dataset before searching for peaks
	 * @param numPeaks
	 *            - The maximum number of peaks that are fitted.
	 * @return list of FittedPeaks - an object that contain the fitted APeak objects and the corresponding objects that
	 *         describe the region of the data where the peak was found
	 */
	public static List<CompositeFunction> fitPeaks(AbstractDataset xdata, AbstractDataset ydata, APeak function,
			IOptimizer optimiser, int smoothing, int numPeaks) {
		return fitPeaks(xdata, ydata, function, optimiser, smoothing, numPeaks, 0.0, false, false);
	}
	
	/**
	 * This method fits peaks to a dataset describing the y values at specified x values. The APeak function specified
	 * will be returned in the list of FittedPeaks. numPeaks is the maximum number of peaks that will be fitted. The
	 * method to fit the peaks can be specified provided that the method obeys the IOptomiser interface. The smoothing
	 * is the value given when calculating the differential of the data being fitted. Stopping. Autostopping will stop
	 * the fitting when the threshold is met of the indicated measure. For example, if the threshold is set to 0.10 and
	 * the largest peak is has an area of 100 then the routing will continue to run until the next peak being fitted as
	 * an area of less that 10. At this point the routine will stop and the results from the peaks found will be
	 * displayed.
	 * 
	 * @param xdata
	 *            - the x values that of the measurements given in ydata
	 * @param ydata
	 *            - the y values corresponding to the x values given
	 * @param function
	 *            - A function that obeys the APeak interface
	 * @param optimiser
	 *            - An optimisation function that obeys the IOptimizer interface
	 * @param smoothing
	 *            - The value applied to the differential of the dataset before searching for peaks
	 * @param numPeaks
	 *            - The maximum number of peaks that are fitted.
	 * @param threshold
	 *            - The stop point for auto stopping
	 * @param autoStopping
	 *            - Boolean to select auto stopping
	 * @param heightMeasure
	 *            - Boolean - true if height is the stopping measure and false if it is area.
	 * @return list of FittedPeaks
	 */
	public static List<CompositeFunction> fitPeaks(AbstractDataset xdata, AbstractDataset ydata, APeak function,
			IOptimizer optimiser, int smoothing, int numPeaks, double threshold, boolean autoStopping,
			boolean heightMeasure){
		return fitPeaks(xdata, ydata, function, optimiser, smoothing, numPeaks, threshold, autoStopping, heightMeasure, new IAnalysisMonitor() {
			
			@Override
			public boolean hasBeenCancelled() {
				return false;
			}
		});
		
	}
	
	/**
	 * This method fits peaks to a dataset describing the y values at specified x values. The APeak function specified
	 * will be returned in the list of FittedPeaks. numPeaks is the maximum number of peaks that will be fitted. The
	 * method to fit the peaks can be specified provided that the method obeys the IOptomiser interface. The smoothing
	 * is the value given when calculating the differential of the data being fitted. Stopping. Autostopping will stop
	 * the fitting when the threshold is met of the indicated measure. For example, if the threshold is set to 0.10 and
	 * the largest peak is has an area of 100 then the routing will continue to run until the next peak being fitted as
	 * an area of less that 10. At this point the routine will stop and the results from the peaks found will be
	 * displayed.
	 * 
	 * @param xdata
	 *            - the x values that of the measurements given in ydata
	 * @param ydata
	 *            - the y values corresponding to the x values given
	 * @param function
	 *            - A function that obeys the APeak interface
	 * @param optimiser
	 *            - An optimisation function that obeys the IOptimizer interface
	 * @param smoothing
	 *            - The value applied to the differential of the dataset before searching for peaks
	 * @param numPeaks
	 *            - The maximum number of peaks that are fitted.
	 * @param threshold
	 *            - The stop point for auto stopping
	 * @param autoStopping
	 *            - Boolean to select auto stopping
	 * @param heightMeasure
	 *            - Boolean - true if height is the stopping measure and false if it is area.
	 * @param monitor 
	 * 			  - IAnalysisMonitor - instance of IAnalysisMonitor class allowing jobs to be stopped
	 * @return list of FittedPeaks or null if job is stopped
	 */
	public static List<CompositeFunction> fitPeaks(AbstractDataset xdata, AbstractDataset ydata, APeak function,
			IOptimizer optimiser, int smoothing, int numPeaks, double threshold, boolean autoStopping,
			boolean heightMeasure, IAnalysisMonitor monitor) {

		List<IdentifiedPeak> peaks = parseDataDerivative(xdata, ydata, smoothing);
		if (peaks == null || peaks.size() <= 0) {
			logger.error("No peaks found");
			return null;
		}

		List<CompositeFunction> fittedPeaks = fitFunction(peaks, function, xdata, ydata, optimiser, numPeaks, threshold,
				autoStopping, heightMeasure, monitor);

		return fittedPeaks;
	}

	public static List<IdentifiedPeak> findPeaks(AbstractDataset xdata, AbstractDataset ydata, int smoothing) {
		return parseDataDerivative(xdata, ydata, smoothing);
	}

	private static List<CompositeFunction> fitFunction(List<IdentifiedPeak> initialPeaks, APeak function, AbstractDataset xData,
			AbstractDataset ydata, IOptimizer optimiser, int numPeaks, double threshold, boolean autoStopping,
			boolean heightMeasure, IAnalysisMonitor monitor) {

		ArrayList<CompositeFunction> peaks = new ArrayList<CompositeFunction>();
		if (numPeaks == 0) {
			numPeaks = initialPeaks.size();
		}
		if (numPeaks <= -1) {
			numPeaks = xData.getSize();
		}
		int fittedPeaks = 0;

		for (IdentifiedPeak iniPeak : initialPeaks) {
			if (fittedPeaks++ >= numPeaks) {
				break;
			}
			if(monitor.hasBeenCancelled()){
				return peaks;
			}

			int[] start = { iniPeak.getIndexOfDatasetAtMinPos() };
			int[] stop = { iniPeak.getIndexOfDatasetAtMaxPos() + 1 };
			int[] step = { 1 };
			AbstractDataset y = ydata.getSlice(start, stop, step);
			AbstractDataset x = xData.getSlice(start, stop, step);

			double lowOffset = y.min().doubleValue();
			double highOffset = (Double) y.mean();
			Offset offset = new Offset(lowOffset, highOffset);

			try {
				Constructor<? extends APeak> ctor = function.getClass().getConstructor(IdentifiedPeak.class);
				APeak localPeak = ctor.newInstance(iniPeak);
				CompositeFunction comp = new CompositeFunction();
				comp.addFunction(localPeak);
				comp.addFunction(offset);
				optimiser.optimize(new AbstractDataset[] { x }, y, comp);

				peaks.add(comp);
			} catch (IllegalArgumentException e1) {
				logger.error("There was a problem optimising the peak", e1);
			} catch (InstantiationException e1) {
				logger.error("The function could not be created for fitting", e1);
			} catch (NoSuchMethodException e1) {
				logger.error("The peak function could not be created.", e1);
			} catch (IllegalAccessException e1) {
				logger.error("The function could not be created for fitting", e1);
			} catch (InvocationTargetException e1) {
				logger.error("The function could not be created for fitting", e1);
			} catch (Exception e) {
				logger.error("There was a problem creating the optimizer.", e);
			}
		}

		if(autoStopping) {
			if(heightMeasure) {

				Collections.sort(peaks, new Comparator<CompositeFunction>() {

					@Override
					public int compare(CompositeFunction o1, CompositeFunction o2) {
						return (int) Math.signum(((APeak)o1.getFunction(0)).getHeight() - ((APeak)o2.getFunction(0)).getHeight());
					}
				});

				for (int i = 1; i < peaks.size(); i++) {
					if(((APeak)peaks.get(i).getFunction(0)).getHeight() < (((APeak)peaks.get(0).getFunction(0)).getHeight()*threshold)) return peaks.subList(0, i);
				}

			} else {

				Collections.sort(peaks, new Comparator<CompositeFunction>() {

					@Override
					public int compare(CompositeFunction o1, CompositeFunction o2) {
						return (int) Math.signum(((APeak)o1.getFunction(0)).getArea() - ((APeak)o2.getFunction(0)).getArea());
					}
				});

				for (int i = 1; i < peaks.size(); i++) {
					if(((APeak)peaks.get(i).getFunction(0)).getArea() < (((APeak)peaks.get(0).getFunction(0)).getArea()*threshold)) return peaks.subList(0, i);
				}

			}
		}

		return peaks;
	}

	private static List<IdentifiedPeak> parseDataDerivative(AbstractDataset xdata, AbstractDataset ydata, int smooth) {
		boolean verbose = false;
		ArrayList<IdentifiedPeak> peaks = new ArrayList<IdentifiedPeak>();

		AbstractDataset data = Maths.derivative(xdata, ydata, smooth+1);
		int backPos, forwardPos;
		double backTotal, forwardTotal;
		for (int i = 0; i < data.getSize() - 1; i++) {
			if (!(data.getElementDoubleAbs(i) >= 0 && data.getElementDoubleAbs(i + 1) < 0)) {
				continue;
			}

			backPos = i;
			backTotal = 0;
			// get the backwards points
			while (backPos > 0) {
				if (data.getElementDoubleAbs(backPos) >= 0) {
					backTotal += data.getElementDoubleAbs(backPos);
					backPos -= 1;

				} else {
					break;
				}
			}

			// get the forward points
			forwardPos = i + 1;
			forwardTotal = 0;
			while (forwardPos < data.getSize() - 1) {
				if (data.getElementDoubleAbs(forwardPos) <= 0) {
					forwardTotal -= data.getElementDoubleAbs(forwardPos);
					forwardPos += 1;

				} else {
					break;
				}
			}
			// this means a peak has been found
			if (Math.min(backTotal, forwardTotal) > epslion) {
				int[] start = { backPos };
				int[] stop = { forwardPos };
				int[] step = { 1 };
				AbstractDataset slicedXData = xdata.getSlice(start, stop, step);
				AbstractDataset slicedYData = ydata.getSlice(start, stop, step);
				List<Double> crossings = DatasetUtils.crossings(slicedXData, slicedYData, slicedYData.max()
						.doubleValue() / 2);
				if (crossings.size() <= 1) {
					crossings.clear();
					crossings.add((double) backPos);
					crossings.add((double) forwardPos);
				}

				IdentifiedPeak newPeak = new IdentifiedPeak(xdata.getElementDoubleAbs(i), xdata
						.getElementDoubleAbs(backPos), xdata.getElementDoubleAbs(forwardPos), Math.min(backTotal,
								forwardTotal), slicedYData.max().doubleValue()-slicedYData.min().doubleValue(), backPos, forwardPos, crossings);
				if (verbose) {
					System.out.println("Back Position = " + xdata.getElementDoubleAbs(backPos) + " Peak Pos = "
							+ xdata.getElementDoubleAbs(i) + " Forward Position = "
							+ xdata.getElementDoubleAbs(forwardPos) + ". Y value at back pos = "
							+ ydata.getElementDoubleAbs(backPos) + " Y value at forward pos = "
							+ ydata.getElementDoubleAbs(forwardPos) + " height " + slicedYData.max() + " Area "
							+ Math.min(backTotal, forwardTotal) + " sum of area variables "
							+ (backTotal + forwardTotal));
				}
				peaks.add(newPeak);
			}
		}
		Collections.sort(peaks, new Compare());

		if (verbose && peaks.size() <= 0) {
			System.err.println("No Peaks Found!!");
		}
		return peaks;
	}

	private static class Compare implements Comparator<IdentifiedPeak> {

		@Override
		public int compare(IdentifiedPeak o1, IdentifiedPeak o2) {
			if (o1.getArea() < o2.getArea()) {
				return 1;
			}
			if (o1.getArea() > o2.getArea()) {
				return -1;
			}
			return 0;
		}

	}

}
