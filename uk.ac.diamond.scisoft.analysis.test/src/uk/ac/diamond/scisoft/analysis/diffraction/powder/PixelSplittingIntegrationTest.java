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

package uk.ac.diamond.scisoft.analysis.diffraction.powder;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
import uk.ac.diamond.scisoft.analysis.roi.ROIProfile.XAxis;

public class PixelSplittingIntegrationTest extends AbstractPixelIntegrationTestBase {

	@Test
	public void testPixelSplittingAzimuthal() {
		
		IDataset data = getData();
		if (data == null) {
			Assert.fail("Could not load test data");
			return;
		}
		
		IDiffractionMetadata meta = getDiffractionMetadata();
		
		int[] shape = new int[]{meta.getDetector2DProperties().getPy(), meta.getDetector2DProperties().getPx()};
		int binTest = AbstractPixelIntegration.calculateNumberOfBins(meta.getDetector2DProperties().getBeamCentreCoords(), shape);
		
		Assert.assertEquals(binTest,1592);
		
		PixelSplittingIntegration npsi = new PixelSplittingIntegration(meta, 1592);
		
		//first pass
		double firstTime = testWholeImageAzimuthal(data,npsi);
		//second pass
		double secondTime = testWholeImageAzimuthal(data,npsi);
		
		if (firstTime < secondTime) {
			Assert.fail("Whole image: second run should be faster due to caching, something odd is afoot");
		}
		
		npsi.setAzimuthalRange(new double[]{-180,-170});
		//first pass
		firstTime =testSectionImageAzimuthal(data,npsi);
		//first pass
		secondTime =testSectionImageAzimuthal(data,npsi);
		
		if (firstTime < secondTime) {
			Assert.fail("Sector: Second run should be faster due to caching, something odd is afoot");
		}
		
		npsi.setRadialRange(new double[]{1,5});
		//first pass
		firstTime =testSectionLimitedImageAzimuthal(data,npsi);
		//second pass
		secondTime =testSectionLimitedImageAzimuthal(data,npsi);
		
		//probably should take a similar time
		npsi.setRadialRange(null);
		npsi.setAzimuthalRange(null);
		AbstractDataset mask = getMask(data.getShape());
		npsi.setMask(mask);
		
		firstTime =testMask(data,npsi);
		secondTime =testMask(data,npsi);

	}
	
	@Test
	public void testPixelSplittingRadial() {
		
		IDataset data = getData();
		if (data == null) {
			Assert.fail("Could not load test data");
			return;
		}
		
		IDiffractionMetadata meta = getDiffractionMetadata();
		PixelSplittingIntegration npsi = new PixelSplittingIntegration(meta, 1592);
		npsi.setAzimuthalIntegration(false);
		
		//first pass
		double firstTime = testWholeImageRadial(data,npsi);
		//second pass
		double secondTime = testWholeImageRadial(data,npsi);
		
		if (firstTime < secondTime) {
			Assert.fail("Whole image: second run should be faster due to caching, something odd is afoot");
		}
		
		npsi.setRadialRange(new double[]{1,5});
		//first pass
		firstTime =testLimitedRadial(data,npsi);
		//first pass
		secondTime =testLimitedRadial(data,npsi);
		
		if (firstTime < secondTime) {
			Assert.fail("Sector: Second run should be faster due to caching, something odd is afoot");
		}
		
		npsi.setAzimuthalRange(new double[]{-180,-170});
		//first pass
		firstTime =testSectorLimitedRadial(data,npsi);
		//second pass
		secondTime =testSectorLimitedRadial(data,npsi);
		
		//probably should take a similar time
	}
	
	@Test
	public void testPixelSplittingBinSetting() {
		IDataset data = getData();
		if (data == null) {
			Assert.fail("Could not load test data");
			return;
		}
		
		IDiffractionMetadata meta = getDiffractionMetadata();
		PixelSplittingIntegration npsi = new PixelSplittingIntegration(meta);
		
		testWholeImageAzimuthal(data,npsi);
		npsi.setNumberOfBins(1000);
		testWholeImageAzimuthal1000Bins(data,npsi);
	}
	
	@Test
	public void testPixelSplittingAxis() {
		
		IDataset data = getData();
		if (data == null) {
			Assert.fail("Could not load test data");
			return;
		}
		
		IDiffractionMetadata meta = getDiffractionMetadata();
		PixelSplittingIntegration npsi = new PixelSplittingIntegration(meta, 1592);
		//2theta
		npsi.setAxisType(XAxis.ANGLE);
		List<AbstractDataset> out = npsi.integrate(data);
		npsi.setRadialRange(new double[]{10,30});
		out = npsi.integrate(data);
		Assert.assertEquals(30, out.get(0).getDouble(1591),0.00001);
		npsi.setRadialRange(null);
		
		//q
		npsi.setAxisType(XAxis.Q);
		out = npsi.integrate(data);
		Assert.assertEquals(10.401047688249356, out.get(0).getDouble(1591),0.00001);
		npsi.setRadialRange(new double[]{1,5});
		out = npsi.integrate(data);
		Assert.assertEquals(5, out.get(0).getDouble(1591),0.00001);
		npsi.setRadialRange(null);
		
		//d-spacing
		npsi.setAxisType(XAxis.RESOLUTION);
		out = npsi.integrate(data);
		Assert.assertEquals(0.6040915776473221, out.get(0).getDouble(1591),0.00001);
		npsi.setRadialRange(new double[]{0.6,4});
		out = npsi.integrate(data);
		Assert.assertEquals(0.6, out.get(0).getDouble(1591),0.00001);
		npsi.setRadialRange(null);
		//pixel
		npsi.setAxisType(XAxis.PIXEL);
		out = npsi.integrate(data);
		Assert.assertEquals(1591.8246735180485, out.get(0).getDouble(1591),0.00001);
		npsi.setRadialRange(new double[]{100,300});
		out = npsi.integrate(data);
		Assert.assertEquals(300, out.get(0).getDouble(1591),0.00001);
		
	}
	
	private double testWholeImageAzimuthal(IDataset data, AbstractPixelIntegration integrator) {
		long before = System.currentTimeMillis();
		List<AbstractDataset> out = integrator.integrate(data);
		long after = System.currentTimeMillis();
		System.out.println("pixel splitting (full) in "+(after-before));
		
		if (out.size() != 2) {
			
			Assert.fail("Incorrect number of datasets returned");
		}
		double max = out.get(1).max().doubleValue();
		double min = out.get(1).min().doubleValue();
		double maxq = out.get(0).max().doubleValue();
		double minq = out.get(0).min().doubleValue();
		Assert.assertEquals(353589.4987476404, max,0.00001);
		Assert.assertEquals(136.5790195926834, min,0.00001);
		Assert.assertEquals(10.401047688249356, maxq,0.00001);
		Assert.assertEquals(0.007368865272782814, minq,0.00001);
		
		return after-before;
	}
	
	private double testSectionImageAzimuthal(IDataset data, AbstractPixelIntegration integrator) {
		long before = System.currentTimeMillis();
		List<AbstractDataset> out = integrator.integrate(data);
		long after = System.currentTimeMillis();
		System.out.println("pixel splitting (section) in "+(after-before));
		
		if (out.size() != 2) {
			Assert.fail("Incorrect number of datasets returned");
		}
		double max = out.get(1).max().doubleValue();
		double min = out.get(1).min().doubleValue();
		double maxq = out.get(0).max().doubleValue();
		double minq = out.get(0).min().doubleValue();
		Assert.assertEquals(332059.1054805572, max,0.00001);
		Assert.assertEquals(0, min,0.00001);
		Assert.assertEquals(10.401047688249356, maxq,0.00001);
		Assert.assertEquals(0.007368865272782814, minq,0.00001);
		return after-before;
	}
	
	private double testSectionLimitedImageAzimuthal(IDataset data, AbstractPixelIntegration integrator) {
		long before = System.currentTimeMillis();
		List<AbstractDataset> out = integrator.integrate(data);
		long after = System.currentTimeMillis();
		System.out.println("pixel splitting (section, limited q) in "+(after-before));
		
		if (out.size() != 2) {
			Assert.fail("Incorrect number of datasets returned");
		}
		double max = out.get(1).max().doubleValue();
		double min = out.get(1).min().doubleValue();
		double maxq = out.get(0).max().doubleValue();
		double minq = out.get(0).min().doubleValue();
		Assert.assertEquals(372774.7288516853, max,0.00001);
		Assert.assertEquals(834.4616925286084, min,0.00001);
		Assert.assertEquals(5, maxq,0.00001);
		Assert.assertEquals(1, minq,0.00001);
		return after-before;
	}
	
	private double testWholeImageRadial(IDataset data, AbstractPixelIntegration integrator) {
		long before = System.currentTimeMillis();
		List<AbstractDataset> out = integrator.integrate(data);
		long after = System.currentTimeMillis();
		System.out.println("pixel splitting (radial full) in "+(after-before));
		
		if (out.size() != 2) {
			
			Assert.fail("Incorrect number of datasets returned");
		}
		double max = out.get(1).max().doubleValue();
		double min = out.get(1).min().doubleValue();
		double maxq = out.get(0).max().doubleValue();
		double minq = out.get(0).min().doubleValue();
		Assert.assertEquals(4003.7742024056865, max,0.00001);
		Assert.assertEquals(2004.8082292455822, min,0.00001);
		Assert.assertEquals(179.8682828227335, maxq,0.00001);
		Assert.assertEquals(-179.88694053611073, minq,0.00001);
		
		return after-before;
	}
	
	private double testLimitedRadial(IDataset data, AbstractPixelIntegration integrator) {
		long before = System.currentTimeMillis();
		List<AbstractDataset> out = integrator.integrate(data);
		long after = System.currentTimeMillis();
		System.out.println("pixel splitting (radial limited) in "+(after-before));
		
		if (out.size() != 2) {
			
			Assert.fail("Incorrect number of datasets returned");
		}
		double max = out.get(1).max().doubleValue();
		double min = out.get(1).min().doubleValue();
		double maxq = out.get(0).max().doubleValue();
		double minq = out.get(0).min().doubleValue();
		Assert.assertEquals(5346.552908675676, max,0.00001);
		Assert.assertEquals(3804.868515450916, min,0.00001);
		Assert.assertEquals(179.8682828227335, maxq,0.00001);
		Assert.assertEquals(-179.88694053611073, minq,0.00001);
		
		return after-before;
	}
	
	private double testSectorLimitedRadial(IDataset data, AbstractPixelIntegration integrator) {
		long before = System.currentTimeMillis();
		List<AbstractDataset> out = integrator.integrate(data);
		long after = System.currentTimeMillis();
		System.out.println("pixel splitting (radial limited sector) in "+(after-before));
		
		if (out.size() != 2) {
			
			Assert.fail("Incorrect number of datasets returned");
		}
		double max = out.get(1).max().doubleValue();
		double min = out.get(1).min().doubleValue();
		double maxq = out.get(0).max().doubleValue();
		double minq = out.get(0).min().doubleValue();
		Assert.assertEquals(5492.080401678215, max,0.00001);
		Assert.assertEquals(3892.45267414554, min,0.00001);
		Assert.assertEquals(-170, maxq,0.00001);
		Assert.assertEquals(-180, minq,0.00001);
		
		return after-before;
	}
	
	private double testMask(IDataset data, AbstractPixelIntegration integrator) {
		long before = System.currentTimeMillis();
		List<AbstractDataset> out = integrator.integrate(data);
		long after = System.currentTimeMillis();
		System.out.println("Non pixel splitting (radial limited sector) in "+(after-before));
		
		if (out.size() != 2) {
			
			Assert.fail("Incorrect number of datasets returned");
		}
		double max = out.get(1).max().doubleValue();
		double min = out.get(1).min().doubleValue();
		double maxq = out.get(0).max().doubleValue();
		double minq = out.get(0).min().doubleValue();
		Assert.assertEquals(359371.22672942874, max,0.00001);
		Assert.assertEquals(379.1726904312658, min,0.00001);
		Assert.assertEquals(9.45220790215385, maxq,0.00001);
		Assert.assertEquals(1.5243689810252112, minq,0.00001);
		
		return after-before;
	}
	
	private double testWholeImageAzimuthal1000Bins(IDataset data, AbstractPixelIntegration integrator) {
		long before = System.currentTimeMillis();
		List<AbstractDataset> out = integrator.integrate(data);
		long after = System.currentTimeMillis();
		System.out.println("Non pixel splitting (1000 bins) in "+(after-before));
		
		if (out.size() != 2) {
			Assert.fail("Incorrect number of datasets returned");
		}
		
		if (out.get(0).getSize() != 1000) {
			Assert.fail("Incorrect number of points");
		}
		double max = out.get(1).max().doubleValue();
		double min = out.get(1).min().doubleValue();
		double maxq = out.get(0).max().doubleValue();
		double minq = out.get(0).min().doubleValue();
		Assert.assertEquals(261085.9148959198, max,0.00001);
		Assert.assertEquals(142.85524164959446, min,0.00001);
		Assert.assertEquals(10.39911398056136, maxq,0.00001);
		Assert.assertEquals(0.009302572960778455, minq,0.00001);
		
		return after-before;
	}
}