/*-
 * Copyright © 2010 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.diamond.scisoft.analysis.io;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IMetadataProvider;

/**
 * This class is to marshal all the data for the purpose of loading from or saving to a file directly or via a ScanFileHolder.
 * <p>
 * This is designed to take in any dataset obeying the IDataset interface but output an
 * object that is a subclass of AbstractDataset - the dataset will be converted if necessary.
 */
public class DataHolder implements IMetadataProvider {
	protected static final Logger logger = LoggerFactory.getLogger(DataHolder.class);

	/**
	 * List containing all the data to be loaded
	 */
	private List<ILazyDataset> data;

	/**
	 * List containing the names of all the data
	 */
	private List<String> names;

	/**
	 * List containing metadata
	 */
	private IMetaData metadata;

	/**
	 * This must create the three objects which will be put into the ScanFileHolder
	 */
	public DataHolder() {
		data = new Vector<ILazyDataset>();
		names = new Vector<String>();
		metadata = new MetaDataAdapter();
	}

	/**
	 * Adds a dataset and its name into the two vectors of the Object.
	 * 
	 * @param name
	 *            the name of the dataset which is to be added
	 * @param dataset
	 *            the actual data of the dataset
	 */
	public void addDataset(String name, ILazyDataset dataset) {
		names.add(name);
		data.add(dataset);
	}

	/**
	 * Adds a dataset, metadata and its name. This is for Diffraction data
	 * 
	 * @param name
	 *            the name of the dataset which is to be added
	 * @param dataset
	 *            the actual data of the dataset.
	 * @param metadata
	 *            the metadata that is associated with the dataset
	 */
	public void addDataset(String name, ILazyDataset dataset, IMetaData metadata) {
		names.add(name);
		data.add(dataset);
		this.metadata = metadata;
	}

	/**
	 * Add a ImetaData object
	 * @param metadata which is an object implementing IMetaData
	 */

	public void setMetadata(IMetaData metadata) {
		this.metadata = metadata;
	}

	/**
	 * @return an object implementing IMetaData
	 */
	@Override
	public IMetaData getMetadata() {
		return metadata;
	}

	/**
	 * This is not guaranteed to work as duplicate names will overwrite in the map.
	 * @return Read-Only Map of datasets with keys from their corresponding names
	 */
	public Map<String, ILazyDataset> getMap() {
		Map<String, ILazyDataset> hm = new LinkedHashMap<String, ILazyDataset>();
		int imax = data.size();
		for (int i = 0; i < imax; i++) {
			hm.put(names.get(i), data.get(i));
		}
		return hm;
	}

	/**
	 * @return List of datasets
	 */
	public List<ILazyDataset> getList() {
		int imax = data.size();
		List<ILazyDataset> al = new ArrayList<ILazyDataset>(imax);
		for (int i = 0; i < imax; i++) {
			al.add(data.get(i));
		}
		return data;
	}

	/**
	 * Set a generic dataset at given index. Ensure the index is in range otherwise an exception
	 * will occur
	 * @param index
	 * @param dataset
	 */
	public void setDataset(int index, ILazyDataset dataset) {
		data.set(index, dataset);
	}
	
	/**
	 * Set a generic dataset with given name
	 * @param name
	 * @param dataset
	 */
	public void setDataset(String name, ILazyDataset dataset) {
		if( names.contains(name)){
			data.set(names.indexOf(name), dataset);
		} else {
			addDataset(name, dataset);
		}
	}

	/**
	 * This does not retrieve lazy datasets.
	 * @param index
	 * @return Generic dataset with given index in holder
	 */
	public AbstractDataset getDataset(int index) {
		return DatasetUtils.convertToAbstractDataset(data.get(index));
	}

	/**
	 * This does not retrieve lazy datasets.
	 * @param name
	 * @return Generic dataset with given name (first one if name not unique)
	 */
	public AbstractDataset getDataset(String name) {
		if (names.contains(name))
			return DatasetUtils.convertToAbstractDataset(data.get(names.indexOf(name)));
		return null;
	}
	
	
	/**
	 * This pulls out the dataset which could be lazy, maintaining its laziness.
	 * @param index
	 * @return Generic dataset with given index in holder
	 */
	public ILazyDataset getLazyDataset(int index) {
		return data.get(index);
	}

	/**
	 * This pulls out the dataset which could be lazy, maintaining its laziness.
	 * @param name
	 * @return Generic dataset with given name (first one if name not unique)
	 */
	public ILazyDataset getLazyDataset(String name) {
		if (names.contains(name))
			return data.get(names.indexOf(name));
		return null;
	}

	/**
	 * @param name
	 * @return true if data holder contains name 
	 * @see java.util.List#contains(Object)
	 */
	public boolean contains(String name) {
		return names.contains(name);
	}

	/**
	 * @param name
	 * @return index of first dataset with given name
	 * @see java.util.List#indexOf(Object)
	 */
	public int indexOf(String name) {
		return names.indexOf(name);
	}

	/**
	 * @return Array of dataset names
	 */
	public String[] getNames() {
		return names.toArray(new String[names.size()]);
	}

	/**
	 * @param index
	 * @return Dataset name at given index
	 */
	public String getName(final int index) {
		if (index >= 0 && index < names.size())
			return names.get(index);
		return null;
	}

	/**
	 * @return Number of datasets
	 */
	public int size() {
		return data.size();
	}

	/**
	 * @return Number of unique dataset names
	 */
	public int namesSize() {
		return names.size();
	}

	/**
	 * Clear list of names and datasets
	 * @see java.util.List#clear()
	 */
	public void clear() {
		data.clear();
		names.clear();
		metadata = null;
	}

	/**
	 * Remove name and dataset at index
	 * @param index
	 * @see java.util.List#remove(int)
	 */
	public void remove(int index) {
		data.remove(index);
		names.remove(index);
	}

}
