/*-
 * Copyright 2014 Diamond Light Source Ltd.
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

package uk.ac.diamond.scisoft.analysis.dataset.function;

import java.util.List;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import junit.framework.Assert;

import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
import uk.ac.diamond.scisoft.analysis.diffraction.QSpace;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class PixelIntegrationTest {

	
	final static String testFileFolder = "testfiles/gda/analysis/io/Fit2dLoaderTest/";
	
	@Test
	public void testNonPixelSplitting() {
		
		AbstractDataset data = getData();
		if (data == null) {
			Assert.fail("Could not load test data");
			return;
		}
		
		QSpace qSpace = getQSpace();

		NonPixelSplittingIntegration npsi = new NonPixelSplittingIntegration(qSpace, 1592);
		
		List<AbstractDataset> out = npsi.value(data);
		
		if (out.size() != 2) {
			Assert.fail("Incorrect number of datasets returned");
		}
		
		double max = out.get(1).max().doubleValue();
		double min = out.get(1).min().doubleValue();
		double maxq = out.get(0).max().doubleValue();
		double minq = out.get(0).min().doubleValue();
		
		Assert.assertEquals(318301.5494672755, max,0.00001);
		Assert.assertEquals(132.55555555555554, min,0.00001);
		Assert.assertEquals(10.397824434316313, maxq,0.00001);
		Assert.assertEquals(0.0073678526262440005, minq,0.00001);
		
	}
	
	@Test
	public void testPixelSplitting() {
		
		AbstractDataset data = getData();
		if (data == null) {
			Assert.fail("Could not load test data");
			return;
		}
		
		QSpace qSpace = getQSpace();

		PixelSplittingIntegration npsi = new PixelSplittingIntegration(qSpace, 1592);
		
		List<AbstractDataset> out = npsi.value(data);
		
		if (out.size() != 2) {
			Assert.fail("Incorrect number of datasets returned");
		}
		
		double max = out.get(1).max().doubleValue();
		double min = out.get(1).min().doubleValue();
		double maxq = out.get(0).max().doubleValue();
		double minq = out.get(0).min().doubleValue();
		
		Assert.assertEquals(300275.76460062194, max,0.00001);
		Assert.assertEquals(136.5910102402878, min,0.00001);
		Assert.assertEquals(10.400892093105334, maxq,0.00001);
		Assert.assertEquals(0.004904701898820428, minq,0.00001);
		
	}
	
	private AbstractDataset getData() {
		final String path = testFileFolder+"/test1.f2d";
		AbstractDataset data = null;
		try {
			DataHolder dataHolder = LoaderFactory.getData(path, null);
			data = dataHolder.getDataset(0);
		} catch (Exception e) {
		}
 		
		return data;
	}
	
	private QSpace getQSpace() {
		Vector3d origin = new Vector3d(0, 0, 1);
		
		Matrix3d or = new Matrix3d(0.18890371330716021,
				0.9819916621345969, -0.0027861437324560243, -0.9818848715409118, 0.18892425862797949,
				0.014481834861492937, 0.014747411225481444, 0.0, 0.9998912510179028);
		
		Vector3d bv = new Vector3d(-149.3111967697318, 270.9243609214487, 368.7598186824519);
		
		DetectorProperties dp = new DetectorProperties(bv, origin, 2048, 2048, 0.2, 0.2, or);
		DiffractionCrystalEnvironment ce = new DiffractionCrystalEnvironment(0.42566937014852557);
		
		return new QSpace(dp, ce);
		

	}
	
}