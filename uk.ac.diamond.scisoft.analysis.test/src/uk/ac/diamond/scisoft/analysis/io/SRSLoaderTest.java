/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;

import org.apache.commons.lang.SerializationUtils;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.dawnsci.analysis.api.metadata.IMetadata;
import org.eclipse.dawnsci.analysis.dataset.impl.AbstractDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.TestUtils;

/**
 * Test for SRS Loader
 */
public class SRSLoaderTest {

	private static String testScratchDirectoryName;

	/**
	 * Creates an empty directory for use by test code.
	 * 
	 * @throws Exception
	 *             if the directory is not created
	 */
	@BeforeClass
	static public void setUpClass() throws Exception {
		testScratchDirectoryName = TestUtils.generateDirectorynameFromClassname(SRSLoaderTest.class.getCanonicalName());
		TestUtils.makeScratchDirectory(testScratchDirectoryName);
	}

	/**
	 * Test Loader
	 */
	public SRSLoaderTest() {

	}

	/**
	 * This method tests for SciSoft trac #496
	 */
	@Test
	public void testSS49() {
		try {
			DataHolder dh;
			String testfile1 = "testfiles/gda/analysis/io/SRSLoaderTest/96356.dat";
			dh = new SRSLoader(testfile1).loadFile();

			// now the file is loaded, check to make sure that it holds the right data
			assertEquals("There is not the correct number of axis in the file", 7, dh.size());
			int dt = dh.getDataset(6).getDtype();
			if (dt == Dataset.FLOAT32)
				assertEquals("The file does not contain NANs", Float.NaN, dh.getDataset(6).getDouble(1), 10.);
			if (dt == Dataset.FLOAT64)
				assertEquals("The file does not contain NANs", Double.NaN, dh.getDataset(6).getDouble(1), 10.);
			assertEquals("The file does not contain data as well", 0.1, dh.getDataset(0).getDouble(1), 1.);
		} catch (ScanFileHolderException e) {
			fail("Couldn't load the file");
		}

	}
	
	@Test
	public void testSRSLoaderLoop()  {
		
		boolean fail = true;
		try {
			String testfile1 = "testfiles/gda/analysis/io/SRSLoaderTest/optics_april20110402.dat";
			// Dodgy old spec file, SRSLoader should reject it! Or at least not loop forever
			
			DataHolder dh = new SRSLoader(testfile1).loadFile();
			
			final int size = dh.getList().size();
			if (size!=0) fail("Test file optics_april20110402.dat should not be parsed!");
		} catch (Exception expected) {
			fail = false;
		}
		if (fail) fail("Test file optics_april20110402.dat should not be parsed!");
	}
	
	@Test
	public void testExtendedSRSLoaderLoop()  {
		
		boolean fail = true;
		try {
			String testfile1 = "testfiles/gda/analysis/io/SRSLoaderTest/optics_april20110402.dat";
			// Dodgy old spec file, SRSLoader should reject it! Or at least not loop forever
			
			DataHolder dh = new ExtendedSRSLoader(testfile1).loadFile();
			
			final int size = dh.getList().size();
			if (size!=0) fail("Test file optics_april20110402.dat should not be parsed!");
		} catch (Exception expected) {
			fail = false;
		}
		if (fail) fail("Test file optics_april20110402.dat should not be parsed!");
	}

	@Test
	public void testLoaderFactory() throws Exception {
		// clear state left over from any previous tests
		LoaderFactory.clear();
		IDataHolder dh = LoaderFactory.getData("testfiles/gda/analysis/io/SRSLoaderTest/96356.dat", null);
        if (dh==null || dh.getNames().length<1) throw new Exception();
		assertEquals("There is not the correct number of axis in the file", 7, dh.size());
		int dt = AbstractDataset.getDType(dh.getDataset(6));
		if (dt == Dataset.FLOAT32)
			assertEquals("The file does not contain NANs", Float.NaN, dh.getDataset(6).getDouble(1), 10.);
		if (dt == Dataset.FLOAT64)
			assertEquals("The file does not contain NANs", Double.NaN, dh.getDataset(6).getDouble(1), 10.);
		assertEquals("The file does not contain data as well", 0.1, dh.getDataset(0).getDouble(1), 1.);
	}
	
	private DataHolder testStoringString(String testfile, boolean storeStrings) throws ScanFileHolderException{
		DataHolder dh;
		SRSLoader srsLoader = new SRSLoader(testfile);
		srsLoader.setStoreStringValues(storeStrings);
		dh = srsLoader.loadFile();
		return dh;
	}

	private DataHolder testStoringStringNotValidImages(boolean storeStrings) throws ScanFileHolderException{
		return testStoringString("testfiles/gda/analysis/io/SRSLoaderTest/testStoringStringNotValidImages.dat", storeStrings);
	}

	private DataHolder testStoringStringValidImages(boolean storeStrings) throws ScanFileHolderException{
		return testStoringString("testfiles/gda/analysis/io/SRSLoaderTest/testStoringStringValidImages.dat", storeStrings);
	}
	
	@Test
	public void testStoringStringFalseNotValidImages() throws ScanFileHolderException {
		DataHolder dh = testStoringStringNotValidImages(false);
		ILazyDataset dataset = dh.getLazyDataset("filename");
		assertNull(dataset);
	}

	@Test
	public void testStoringStringTrueNotValidImages() throws ScanFileHolderException {
		DataHolder dh = testStoringStringNotValidImages(true);
		IDataset dataset = (IDataset)dh.getLazyDataset("filename");
		assertNotNull(dataset);
		assertEquals(3, dataset.getSize());
		assertEquals(1, (dataset.getShape()).length);
		assertEquals(3, (dataset.getShape())[0]);
		assertEquals( "testfiles/gda/analysis/io/SRSLoaderTest/NotValidImage3.tif", dataset.getObject(2));
	}
	
	@Test
	public void testStoringStringFalseValidImages() throws ScanFileHolderException {
		DataHolder dh = testStoringStringValidImages(false);
		{
			ILazyDataset dataset = dh.getLazyDataset("filename");
			assertNull(dataset);
		}
		ILazyDataset dataset_image = dh.getLazyDataset("filename_image");
		assertNotNull(dataset_image);
		assertEquals(3, (dataset_image.getShape()).length);
		assertEquals(3, (dataset_image.getShape())[0]);
		{
			//take slice from first file
			IDataset slice = dataset_image.getSlice( new int[]{0,0,0}, new int[]{1,195,1475}, new int[]{1,1,1});
			slice.squeeze();
			int[] shape = slice.getShape();
			assertNotNull(shape);
		}
		{
			//take slice from second file - which is of a different size.
			IDataset slice = dataset_image.getSlice( new int[]{1,0,0}, new int[]{2,195,1024}, new int[]{1,1,1});
			slice.squeeze();
			int[] shape = slice.getShape();
			assertNotNull(shape);
		}
	}

	@Test
	public void testStoringStringTrueValidImages() throws ScanFileHolderException {
		DataHolder dh = testStoringStringValidImages(true);
		IDataset dataset = (IDataset)dh.getLazyDataset("filename");
		assertNotNull(dataset);
		assertEquals(3, dataset.getSize());
		assertEquals(1, (dataset.getShape()).length);
		assertEquals(3, (dataset.getShape())[0]);
		assertEquals( "testfiles/gda/analysis/io/SRSLoaderTest/simple_data.dat", dataset.getObject(2));
	}

	
	@Test
	public void testWhiteSpaceBeforeNumberss() throws ScanFileHolderException {
		DataHolder dh;
		String testfile1 = "testfiles/gda/analysis/io/SRSLoaderTest/16446.dat";
		dh = new SRSLoader(testfile1).loadFile();

		assertEquals(4, dh.size());
		assertEquals("DCMFPitch", dh.getNames()[0]);
	}

	/**
	 * This method tests for B16 data's new-fangled metadata
	 */
	@Test
	public void testB16data() {
		try {
			DataHolder dh;
			String testfile1 = "testfiles/gda/analysis/io/SRSLoaderTest/34146.dat";
			dh = new SRSLoader(testfile1).loadFile();

			// now the file is loaded, check to make sure that it holds the right data
			assertEquals("There is not the correct number of axis in the file", 2, dh.size());
			assertEquals("The file does not contain correct data", 2.0, dh.getDataset(0).getDouble(1), 1e-5);
			IMetadata mdo = dh.getMetadata();
			try {
			Collection<String> mdNames = mdo.getMetaNames();
			assertEquals("Loaded incorrect number of metadata items", 11, mdNames.size());
			assertTrue("Metadata item missing", mdo.getMetaValue("cmd")!=null);
			assertTrue("Metadata item missing", mdo.getMetaValue("SRSTIM")!=null);
			assertEquals("SRS metadata item wrong", "95827", mdo.getMetaValue("SRSTIM"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Assert.fail("Could not find metadata");
				
			}
		} catch (ScanFileHolderException e) {
			fail("Couldn't load the file");
		}

	}

	@Test
	public void testSerializability() throws Exception {
		DataHolder dh = new SRSLoader("testfiles/gda/analysis/io/SRSLoaderTest/96356.dat").loadFile();
		Dataset data = dh.getDataset(0);
		SerializationUtils.serialize(data.getMetadata());
	}

	@Test
	public void testSpacesInNames() throws Exception {
		DataHolder dh = new DataHolder();
		String fileName = "quoted.dat";
		Dataset data1 = DatasetFactory.createRange(20, Dataset.INT32);
		Dataset data2 = DatasetUtils.linSpace(0, 576000, 20, Dataset.FLOAT64);
		try {
			dh.addDataset("col1", data1);
			dh.addDataset("testing data", data2);
			new SRSLoader(testScratchDirectoryName + fileName).saveFile(dh);
		} catch (Exception e) {
			throw new ScanFileHolderException("Problem testing SRSLoader class", e);
		}

		dh = new SRSLoader(testScratchDirectoryName + fileName).loadFile();

		assertEquals(2, dh.size());
		assertEquals("col1", dh.getName(0));
		assertEquals("testing data", dh.getName(1));
	}
}
