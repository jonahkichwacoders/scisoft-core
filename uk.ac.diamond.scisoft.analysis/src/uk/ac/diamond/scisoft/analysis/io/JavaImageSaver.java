/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.io;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;

import javax.imageio.ImageIO;
import javax.media.jai.PlanarImage;
import javax.media.jai.TiledImage;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.io.IFileSaver;
import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.dawnsci.analysis.api.metadata.IMetadata;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;

/**
 * Class that saves data from DataHolder using native Java ImageIO library
 * with built-in image reader/writer
 * 
 * If the DataHolder contains more than one dataset then multiple image
 * files are saved with names labelled from 00001 to 99999.
 */
public class JavaImageSaver implements IFileSaver {

	static {
		ImageIO.scanForPlugins(); // in case the ImageIO jar has not been loaded yet 
	}

	private String fileName = "";
	private String fileType = "";
	protected double maxVal;
	protected boolean unsigned = false;
	private int numBits;

	/**
	 * @param FileName
	 *            which is the name of the file being passed to this class.
	 * @param FileType
	 *            type of file to be saved
	 * @param NumBits
	 *            number of bits in each pixel (greyscale representation); use > 32 for floats (TIFF only)
	 * @param asUnsigned
	 *            treat dataset items as unsigned (unless saving as floats)
	 */
	public JavaImageSaver(String FileName, String FileType, int NumBits, boolean asUnsigned) {
		fileName = FileName;
		fileType = FileType; // format name
		numBits = NumBits;
		if (numBits <= 32) {
			maxVal = (1L << NumBits) - 1.0; // 2^NumBits - 1 (use long in case of overflow)
			unsigned = asUnsigned;
		} else {
			maxVal = 1.0; // flag to use doubles (TIFF only)
		}
	}

	@Override
	public void saveFile(IDataHolder dh) throws ScanFileHolderException {
		File f = null;

		if (numBits <= 0) {
			throw new ScanFileHolderException(
					"Number of bits specified must be greater than 0");
		}

		for (int i = 0, imax = dh.size(); i < imax; i++) {

			try {
				String name = null;
				if (imax == 1) {
					name = fileName;
				} else {
					try {
						name = fileName.substring(0,
								(fileName.lastIndexOf(".")));
					} catch (Exception e) {
						name = fileName;
					}

					NumberFormat format = new DecimalFormat("00000");
					name = name + format.format(i + 1);
				}

				int l = new File(name).getName().lastIndexOf(".");
				if (l < 0) {
					name = name + "." + fileType;
				}

				f = new File(name);

				IDataset idata = dh.getDataset(i);
				Dataset data = DatasetUtils.convertToDataset(idata);

				if (numBits <= 16) {
					// test to see if the values of the data are within the
					// capabilities of format
					if (maxVal > 0 && data.max().doubleValue() > maxVal) {
						throw new ScanFileHolderException("The value of a pixel exceeds the maximum value that "
										+ fileType + " is capable of handling. To save a "
										+ fileType + " it is recommended to use a ScaledSaver class. File "
										+ fileName + " not written");
					}
					if (unsigned && data.min().doubleValue() < 0) {
						throw new ScanFileHolderException(
								"The value of a pixel is less than 0. Recommended using a ScaledSaver class.");
					}
					
					BufferedImage image = AWTImageUtils.makeBufferedImage(data, numBits);

					if (image == null) {
						throw new ScanFileHolderException("Unable to create a buffered image to save file type");
					}
					
					boolean w = writeImageLocked(image, fileType, f, dh);
					if (!w)
						throw new ScanFileHolderException("No writer for '" + fileName + "' of type " + fileType);
				} else {
					TiledImage image = AWTImageUtils.makeTiledImage(data, numBits);

					if (image == null) {
						throw new ScanFileHolderException("Unable to create a tiled image to save file type");
					}
					boolean w = writeImageLocked(image, fileType, f, dh);
					if (!w)
						throw new ScanFileHolderException("No writer for '" + fileName + "' of type " + fileType);
				}
			} catch (ScanFileHolderException e) {
				throw e;
			} catch (Exception e) {
				throw new ScanFileHolderException("Error saving file '" + fileName + "'", e);
			}

		}

	}

	protected boolean writeImageLocked(RenderedImage image, String fileType, File f, IDataHolder dh) throws Exception {
		
		// Write meta data to image header
		if (image instanceof PlanarImage) {
			PlanarImage pi = (PlanarImage)image;
			if (dh.getFilePath()!=null) {
				pi.setProperty("originalDataSource", dh.getFilePath());
			}

			IMetadata meta = dh.getMetadata();
			if (meta != null) {
				Collection<String> dNames = meta.getMetaNames();
				if (dNames!=null) for (String name : dNames) {
					pi.setProperty(name, meta.getMetaValue(name));
				}
			}
		}
		// Separate method added as may add file locking option.
		return ImageIO.write(image, fileType, f);
	}

}
