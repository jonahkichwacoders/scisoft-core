/*-
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

// This is generated from DoubleDataset.java by fromdouble.py

package uk.ac.diamond.scisoft.analysis.dataset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.complex.Complex;


/**
 * Extend dataset for short values // PRIM_TYPE
 */
public class ShortDataset extends AbstractDataset {
	// pin UID to base class
	private static final long serialVersionUID = AbstractDataset.serialVersionUID;

	protected short[] data; // subclass alias // PRIM_TYPE

	@Override
	protected void setData() {
		data = (short[]) odata; // PRIM_TYPE
	}

	protected static short[] createArray(final int size) { // PRIM_TYPE
		short[] array = null; // PRIM_TYPE

		try {
			array = new short[size]; // PRIM_TYPE
		} catch (OutOfMemoryError e) {
			logger.error("The size of the dataset ({}) that is being created is too large "
					+ "and there is not enough memory to hold it.", size);
			throw new OutOfMemoryError("The dimensions given are too large, and there is "
					+ "not enough memory available in the Java Virtual Machine");
		}
		return array;
	}

	@Override
	public int getDtype() {
		return INT16; // DATA_TYPE
	}

	public ShortDataset() {
	}

	/**
	 * Create a zero-filled dataset of given shape
	 * @param shape
	 */
	public ShortDataset(final int... shape) {
		if (shape.length == 1) {
			size = shape[0];
			if (size < 0) {
				throw new IllegalArgumentException("Negative component in shape is not allowed");
			}
		} else {
			size = calcSize(shape);
		}
		this.shape = shape.clone();

		odata = data = createArray(size);
	}

	/**
	 * Create a dataset using given data
	 * @param data
	 * @param shape
	 *            (can be null to create 1D dataset)
	 */
	public ShortDataset(final short[] data, int... shape) { // PRIM_TYPE
		if (shape == null || shape.length == 0) {
			shape = new int[] { data.length };
		}
		size = calcSize(shape);
		if (size != data.length) {
			throw new IllegalArgumentException(String.format("Shape %s is not compatible with size of data array, %d",
					Arrays.toString(shape), data.length));
		}
		this.shape = shape.clone();

		odata = this.data = data;
	}

	/**
	 * Copy a dataset
	 * @param dataset
	 */
	public ShortDataset(final ShortDataset dataset) {
		copyToView(dataset, this, true, true);
		if (dataset.stride == null || dataset.size == dataset.base.size) {
			odata = data = dataset.data.clone();
		} else {
			offset = 0;
			stride = null;
			base = null;
			odata = data = createArray(size);
			IndexIterator iter = dataset.getIterator();
			for (int i = 0; iter.hasNext(); i++) {
				data[i] = dataset.data[iter.index];
			}
		}
	}

	/**
	 * Cast a dataset to this class type
	 * @param dataset
	 */
	public ShortDataset(final Dataset dataset) {
		copyToView(dataset, this, true, false);
		offset = 0;
		stride = null;
		base = null;
		odata = data = createArray(size);
		IndexIterator iter = dataset.getIterator();
		for (int i = 0; iter.hasNext(); i++) {
			data[i] = (short) dataset.getElementLongAbs(iter.index); // GET_ELEMENT_WITH_CAST
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			return false;
		}

		if (getRank() == 0) // already true for zero-rank dataset
			return true;

		ShortDataset other = (ShortDataset) obj;
		IndexIterator iter = getIterator();
		IndexIterator oiter = other.getIterator();
		while (iter.hasNext() && oiter.hasNext()) {
			if (data[iter.index] != other.data[oiter.index]) // OBJECT_UNEQUAL
				return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public ShortDataset clone() {
		return new ShortDataset(this);
	}

	/**
	 * Create a dataset from an object which could be a Java list, array (of arrays...) or Number. Ragged
	 * sequences or arrays are padded with zeros.
	 *
	 * @param obj
	 * @return dataset with contents given by input
	 */
	public static ShortDataset createFromObject(final Object obj) {
		ShortDataset result = new ShortDataset();

		result.shape = getShapeFromObject(obj);
		result.size = calcSize(result.shape);

		result.odata = result.data = createArray(result.size);

		int[] pos = new int[result.shape.length];
		result.fillData(obj, 0, pos);
		return result;
	}
	
	/**
	 * @param stop
	 * @return a new 1D dataset, filled with values determined by parameters
	 * @deprecated Use {@link #createRange(double)}
	 */
	@Deprecated
	public static ShortDataset arange(final double stop) {
		return createRange(0, stop, 1);
	}
	
	/**
	 * @param start
	 * @param stop
	 * @param step
	 * @return a new 1D dataset, filled with values determined by parameters
	 * @deprecated Use {@link #createRange(double, double, double)}
	 */
	@Deprecated
	public static ShortDataset arange(final double start, final double stop, final double step) {
		return createRange(start, stop, step);
	}
	
	/**
	 *
	 * @param stop
	 * @return a new 1D dataset, filled with values determined by parameters
	 */
	public static ShortDataset createRange(final double stop) {
		return createRange(0, stop, 1);
	}
	
	/**
	 *
	 * @param start
	 * @param stop
	 * @param step
	 * @return a new 1D dataset, filled with values determined by parameters
	 */
	public static ShortDataset createRange(final double start, final double stop, final double step) {
		int size = calcSteps(start, stop, step);
		ShortDataset result = new ShortDataset(size);
		for (int i = 0; i < size; i++) {
			result.data[i] = (short) (start + i * step); // PRIM_TYPE // ADD_CAST
		}
		return result;
	}

	/**
	 * @param shape
	 * @return a dataset filled with ones
	 */
	public static ShortDataset ones(final int... shape) {
		return new ShortDataset(shape).fill(1);
	}

	/**
	 * @param obj
	 * @return dataset filled with given object
	 */
	@Override
	public ShortDataset fill(final Object obj) {
		if (obj instanceof IDataset) {
			IDataset ds = (IDataset) obj;
			if (!isCompatibleWith(ds)) {
				logger.error("Tried to fill with dataset of incompatible shape");
				throw new IllegalArgumentException("Tried to fill with dataset of incompatible shape");
			}
			if (ds instanceof Dataset) {
				Dataset ads = (Dataset) ds;
				IndexIterator itd = ads.getIterator();
				IndexIterator iter = getIterator();
				while (iter.hasNext() && itd.hasNext()) {
					data[iter.index] = (short) ads.getElementLongAbs(itd.index); // GET_ELEMENT_WITH_CAST
				}
			} else {
				IndexIterator itd = new PositionIterator(ds.getShape());
				int[] pos = itd.getPos();
				IndexIterator iter = getIterator();
				while (iter.hasNext() && itd.hasNext()) {
					data[iter.index] = ds.getShort(pos); // PRIM_TYPE
				}
			}

			setDirty();
			return this;
		}

		short dv = (short) toLong(obj); // PRIM_TYPE // FROM_OBJECT
		IndexIterator iter = getIterator();
		while (iter.hasNext()) {
			data[iter.index] = dv;
		}

		setDirty();
		return this;
	}

	/**
	 * This is a typed version of {@link #getBuffer()}
	 * @return data buffer as linear array
	 */
	public short[] getData() { // PRIM_TYPE
		return data;
	}

	@Override
	protected int getBufferLength() {
		if (data == null)
			return 0;
		return data.length;
	}

	@Override
	public ShortDataset getView() {
		ShortDataset view = new ShortDataset();
		copyToView(this, view, true, true);
		view.data = data;
		return view;
	}

	/**
	 * Get a value from an absolute index of the internal array. This is an internal method with no checks so can be
	 * dangerous. Use with care or ideally with an iterator.
	 *
	 * @param index
	 *            absolute index
	 * @return value
	 */
	public short getAbs(final int index) { // PRIM_TYPE
		return data[index];
	}

	@Override
	public boolean getElementBooleanAbs(final int index) {
		return data[index] != 0; // BOOLEAN_FALSE
	}

	@Override
	public double getElementDoubleAbs(final int index) {
		return data[index]; // BOOLEAN_ZERO
	}

	@Override
	public long getElementLongAbs(final int index) {
		return data[index]; // BOOLEAN_ZERO // OMIT_CAST_INT
	}

	@Override
	public Object getObjectAbs(final int index) {
		return data[index];
	}

	@Override
	public String getStringAbs(final int index) {
		return String.format("%d", data[index]); // FORMAT_STRING
	}

	/**
	 * Set a value at absolute index in the internal array. This is an internal method with no checks so can be
	 * dangerous. Use with care or ideally with an iterator.
	 *
	 * @param index
	 *            absolute index
	 * @param val
	 *            new value
	 */
	public void setAbs(final int index, final short val) { // PRIM_TYPE
		data[index] = val;
		setDirty();
	}

	@Override
	protected void setItemDirect(final int dindex, final int sindex, final Object src) {
		short[] dsrc = (short[]) src; // PRIM_TYPE
		data[dindex] = dsrc[sindex];
	}

	@Override
	public void setObjectAbs(final int index, final Object obj) {
		if (index < 0 || index > data.length) {
			throw new IndexOutOfBoundsException("Index given is outside dataset");
		}

		setAbs(index, (short) toLong(obj)); // FROM_OBJECT
	}

	/**
	 * @param i
	 * @return item in given position
	 */
	public short get(final int i) { // PRIM_TYPE
		return data[get1DIndex(i)];
	}

	/**
	 * @param i
	 * @param j
	 * @return item in given position
	 */
	public short get(final int i, final int j) { // PRIM_TYPE
		return data[get1DIndex(i, j)];
	}

	/**
	 * @param pos
	 * @return item in given position
	 */
	public short get(final int... pos) { // PRIM_TYPE
		return data[get1DIndex(pos)];
	}

	@Override
	public Object getObject(final int i) {
		return Short.valueOf(get(i)); // CLASS_TYPE
	}

	@Override
	public Object getObject(final int i, final int j) {
		return Short.valueOf(get(i, j)); // CLASS_TYPE
	}

	@Override
	public Object getObject(final int... pos) {
		return Short.valueOf(get(pos)); // CLASS_TYPE
	}

	@Override
	public String getString(final int i) {
		return getStringAbs(get1DIndex(i));
	}

	@Override
	public String getString(final int i, final int j) {
		return getStringAbs(get1DIndex(i, j));
	}

	@Override
	public String getString(final int... pos) {
		return getStringAbs(get1DIndex(pos));
	}

	@Override
	public double getDouble(final int i) {
		return get(i); // BOOLEAN_ZERO // OMIT_SAME_CAST // ADD_CAST
	}

	@Override
	public double getDouble(final int i, final int j) {
		return get(i, j); // BOOLEAN_ZERO // OMIT_SAME_CAST // ADD_CAST
	}

	@Override
	public double getDouble(final int... pos) {
		return get(pos); // BOOLEAN_ZERO // OMIT_SAME_CAST // ADD_CAST
	}

	@Override
	public float getFloat(final int i) {
		return get(i); // BOOLEAN_ZERO // OMIT_REAL_CAST
	}

	@Override
	public float getFloat(final int i, final int j) {
		return get(i, j); // BOOLEAN_ZERO // OMIT_REAL_CAST
	}

	@Override
	public float getFloat(final int... pos) {
		return get(pos); // BOOLEAN_ZERO // OMIT_REAL_CAST
	}

	@Override
	public long getLong(final int i) {
		return get(i); // BOOLEAN_ZERO // OMIT_UPCAST
	}

	@Override
	public long getLong(final int i, final int j) {
		return get(i, j); // BOOLEAN_ZERO // OMIT_UPCAST
	}

	@Override
	public long getLong(final int... pos) {
		return get(pos); // BOOLEAN_ZERO // OMIT_UPCAST
	}

	@Override
	public int getInt(final int i) {
		return get(i); // BOOLEAN_ZERO // OMIT_UPCAST
	}

	@Override
	public int getInt(final int i, final int j) {
		return get(i, j); // BOOLEAN_ZERO // OMIT_UPCAST
	}

	@Override
	public int getInt(final int... pos) {
		return get(pos); // BOOLEAN_ZERO // OMIT_UPCAST
	}

	@Override
	public short getShort(final int i) {
		return get(i); // BOOLEAN_ZERO // OMIT_UPCAST
	}

	@Override
	public short getShort(final int i, final int j) {
		return get(i, j); // BOOLEAN_ZERO // OMIT_UPCAST
	}

	@Override
	public short getShort(final int... pos) {
		return get(pos); // BOOLEAN_ZERO // OMIT_UPCAST
	}

	@Override
	public byte getByte(final int i) {
		return (byte) get(i); // BOOLEAN_ZERO // OMIT_UPCAST
	}

	@Override
	public byte getByte(final int i, final int j) {
		return (byte) get(i, j); // BOOLEAN_ZERO // OMIT_UPCAST
	}

	@Override
	public byte getByte(final int... pos) {
		return (byte) get(pos); // BOOLEAN_ZERO // OMIT_UPCAST
	}

	@Override
	public boolean getBoolean(final int i) {
		return get(i) != 0; // BOOLEAN_FALSE
	}

	@Override
	public boolean getBoolean(final int i, final int j) {
		return get(i, j) != 0; // BOOLEAN_FALSE
	}

	@Override
	public boolean getBoolean(final int... pos) {
		return get(pos) != 0; // BOOLEAN_FALSE
	}

	/**
	 * Sets the value at a particular point to the passed value. The dataset must be 1D
	 *
	 * @param value
	 * @param i
	 */
	public void setItem(final short value, final int i) { // PRIM_TYPE
		setAbs(get1DIndex(i), value);
	}

	/**
	 * Sets the value at a particular point to the passed value. The dataset must be 2D
	 *
	 * @param value
	 * @param i
	 * @param j
	 */
	public void setItem(final short value, final int i, final int j) { // PRIM_TYPE
		setAbs(get1DIndex(i, j), value);
	}

	/**
	 * Sets the value at a particular point to the passed value
	 *
	 * @param value
	 * @param pos
	 */
	public void setItem(final short value, final int... pos) { // PRIM_TYPE
		setAbs(get1DIndex(pos), value);
	}

	@Override
	public void set(final Object obj, final int i) {
		setItem((short) toLong(obj), i); // FROM_OBJECT
	}

	@Override
	public void set(final Object obj, final int i, final int j) {
		setItem((short) toLong(obj), i, j); // FROM_OBJECT
	}

	@Override
	public void set(final Object obj, int... pos) {
		if (pos == null || (pos.length == 0 && shape.length > 0)) {
			pos = new int[shape.length];
		}

		setItem((short) toLong(obj), pos); // FROM_OBJECT
	}


	@Override
	public void resize(int... newShape) {
		final IndexIterator iter = getIterator();
		final int nsize = calcSize(newShape);
		final short[] ndata = createArray(nsize); // PRIM_TYPE
		for (int i = 0; iter.hasNext() && i < nsize; i++) {
			ndata[i] = data[iter.index];
		}

		odata = data = ndata;
		size = nsize;
		shape = newShape;
		stride = null;
		offset = 0;
		base = null;
	}

	/**
	 * In-place sort of dataset
	 *
	 * @param axis
	 *            to sort along
	 * @return sorted dataset
	 */
	@Override
	public ShortDataset sort(Integer axis) {
		if (axis == null) {
			Arrays.sort(data);
		} else {
			axis = checkAxis(axis);
			
			ShortDataset ads = new ShortDataset(shape[axis]);
			PositionIterator pi = getPositionIterator(axis);
			int[] pos = pi.getPos();
			boolean[] hit = pi.getOmit();
			while (pi.hasNext()) {
				copyItemsFromAxes(pos, hit, ads);
				Arrays.sort(ads.data);
				setItemsOnAxes(pos, hit, ads.data);
			}
		}
		
		setDirty();
		return this;
		// throw new UnsupportedOperationException("Cannot sort dataset"); // BOOLEAN_USE
	}

	@Override
	public ShortDataset getSlice(final SliceIterator siter) {
		ShortDataset result = new ShortDataset(siter.getShape());
		short[] rdata = result.data; // PRIM_TYPE

		for (int i = 0; siter.hasNext(); i++)
			rdata[i] = data[siter.index];

		result.setName(name + BLOCK_OPEN + Slice.createString(siter.shape, siter.start, siter.stop, siter.step) + BLOCK_CLOSE);
		return result;
	}

	@Override
	public void fillDataset(Dataset result, IndexIterator iter) {
		IndexIterator riter = result.getIterator();

		short[] rdata = ((ShortDataset) result).data; // PRIM_TYPE

		while (riter.hasNext() && iter.hasNext())
			rdata[riter.index] = data[iter.index];
	}

	@Override
	public ShortDataset setByBoolean(final Object obj, Dataset selection) {
		if (obj instanceof Dataset) {
			final Dataset ds = (Dataset) obj;
			final int length = ((Number) selection.sum()).intValue();
			if (length != ds.getSize()) {
				throw new IllegalArgumentException(
						"Number of true items in selection does not match number of items in dataset");
			}

			final IndexIterator oiter = ds.getIterator();
			final BooleanIterator biter = getBooleanIterator(selection);

			while (biter.hasNext() && oiter.hasNext()) {
				data[biter.index] = (short) ds.getElementLongAbs(oiter.index); // GET_ELEMENT_WITH_CAST
			}
		} else {
			final short dv = (short) toLong(obj); // PRIM_TYPE // FROM_OBJECT
			final BooleanIterator biter = getBooleanIterator(selection);

			while (biter.hasNext()) {
				data[biter.index] = dv;
			}
		}
		setDirty();
		return this;
	}

	@Override
	public ShortDataset setBy1DIndex(final Object obj, final Dataset index) {
		if (obj instanceof Dataset) {
			final Dataset ds = (Dataset) obj;
			if (index.getSize() != ds.getSize()) {
				throw new IllegalArgumentException(
						"Number of items in index dataset does not match number of items in dataset");
			}

			final IndexIterator oiter = ds.getIterator();
			final IntegerIterator iter = new IntegerIterator(index, size);

			while (iter.hasNext() && oiter.hasNext()) {
				data[iter.index] = (short) ds.getElementLongAbs(oiter.index); // GET_ELEMENT_WITH_CAST
			}
		} else {
			final short dv = (short) toLong(obj); // PRIM_TYPE // FROM_OBJECT
			IntegerIterator iter = new IntegerIterator(index, size);

			while (iter.hasNext()) {
				data[iter.index] = dv;
			}
		}
		setDirty();
		return this;
	}

	@Override
	public ShortDataset setByIndexes(final Object obj, final Object... indexes) {
		final IntegersIterator iter = new IntegersIterator(shape, indexes);
		final int[] pos = iter.getPos();

		if (obj instanceof Dataset) {
			final Dataset ds = (Dataset) obj;
			if (calcSize(iter.getShape()) != ds.getSize()) {
				throw new IllegalArgumentException(
						"Number of items in index datasets does not match number of items in dataset");
			}

			final IndexIterator oiter = ds.getIterator();

			while (iter.hasNext() && oiter.hasNext()) {
				setItem((short) ds.getElementLongAbs(oiter.index), pos); // GET_ELEMENT_WITH_CAST
			}
		} else {
			final short dv = (short) toLong(obj); // PRIM_TYPE // FROM_OBJECT

			while (iter.hasNext()) {
				setItem(dv, pos);
			}
		}
		setDirty();
		return this;
	}

	@Override
	ShortDataset setSlicedView(Dataset view, Dataset d) {
		BroadcastIterator biter = new BroadcastIterator(view, d);

		while (biter.hasNext()) {
			data[biter.aIndex] = (short) d.getElementLongAbs(biter.bIndex); // GET_ELEMENT_WITH_CAST
		}
		return this;
	}

	@Override
	public ShortDataset setSlice(final Object obj, final IndexIterator siter) {

		if (obj instanceof IDataset) {
			final IDataset ds = (IDataset) obj;
			final int[] oshape = ds.getShape();

			if (!areShapesCompatible(siter.getShape(), oshape)) {
				throw new IllegalArgumentException(String.format(
						"Input dataset is not compatible with slice: %s cf %s", Arrays.toString(oshape),
						Arrays.toString(siter.getShape())));
			}

			if (ds instanceof Dataset) {
				final Dataset ads = (Dataset) ds;
				final IndexIterator oiter = ads.getIterator();

				while (siter.hasNext() && oiter.hasNext())
					data[siter.index] = (short) ads.getElementLongAbs(oiter.index); // GET_ELEMENT_WITH_CAST
			} else {
				final IndexIterator oiter = new PositionIterator(oshape);
				final int[] pos = oiter.getPos();

				while (siter.hasNext() && oiter.hasNext())
					data[siter.index] = ds.getShort(pos); // PRIM_TYPE
			}
		} else {
			try {
				short v = (short) toLong(obj); // PRIM_TYPE // FROM_OBJECT

				while (siter.hasNext())
					data[siter.index] = v;
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("Object for setting slice is not a dataset or number");
			}
		}
		setDirty();
		return this;
	}

	@Override
	public void copyItemsFromAxes(final int[] pos, final boolean[] axes, final Dataset dest) {
		short[] ddata = (short[]) dest.getBuffer(); // PRIM_TYPE

		SliceIterator siter = getSliceIteratorFromAxes(pos, axes);
		int[] sshape = squeezeShape(siter.getShape(), false);

		IndexIterator diter = dest.getSliceIterator(null, sshape, null);

		if (ddata.length < calcSize(sshape)) {
			throw new IllegalArgumentException("destination array is not large enough");
		}

		while (siter.hasNext() && diter.hasNext())
			ddata[diter.index] = data[siter.index];
	}

	@Override
	public void setItemsOnAxes(final int[] pos, final boolean[] axes, final Object src) {
		short[] sdata = (short[]) src; // PRIM_TYPE

		SliceIterator siter = getSliceIteratorFromAxes(pos, axes);

		if (sdata.length < calcSize(siter.getShape())) {
			throw new IllegalArgumentException("destination array is not large enough");
		}

		for (int i = 0; siter.hasNext(); i++) {
			data[siter.index] = sdata[i];
		}
		setDirty();
	}

	@Override
	protected Number fromDoubleToNumber(double x) {
		short r = (short) (long) x; // ADD_CAST // PRIM_TYPE_LONG
		return Short.valueOf(r); // CLASS_TYPE
		// return Integer.valueOf((int) (long) x); // BOOLEAN_USE
		// return null; // OBJECT_USE
	}

	private List<int[]> findPositions(final short value) { // PRIM_TYPE
		IndexIterator iter = getIterator(true);
		List<int[]> posns = new ArrayList<int[]>();
		int[] pos = iter.getPos();

		{
			while (iter.hasNext()) {
				if (data[iter.index] == value) {
					posns.add(pos.clone());
				}
			}
		}
		return posns;
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public int[] maxPos(boolean ignoreInvalids) {
		if (storedValues == null) {
			calculateMaxMin(ignoreInvalids, ignoreInvalids);
		}
		String n = storeName(ignoreInvalids, ignoreInvalids, STORE_MAX_POS);
		Object o = storedValues.get(n);

		List<int[]> max = null;
		if (o == null) {
			max = findPositions(max(ignoreInvalids).shortValue()); // PRIM_TYPE
			// max = findPositions(max(false).intValue() != 0); // BOOLEAN_USE
			// max = findPositions(null); // OBJECT_USE
			storedValues.put(n, max);
		} else if (o instanceof List<?>) {
			max = (List<int[]>) o;
		} else {
			throw new InternalError("Inconsistent internal state of stored values for statistics calculation");
		}

		return max.get(0); // first maximum
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public int[] minPos(boolean ignoreInvalids) {
		if (storedValues == null) {
			calculateMaxMin(ignoreInvalids, ignoreInvalids);
		}
		String n = storeName(ignoreInvalids, ignoreInvalids, STORE_MIN_POS);
		Object o = storedValues.get(n);
		List<int[]> min = null;
		if (o == null) {
			min = findPositions(min(ignoreInvalids).shortValue()); // PRIM_TYPE
			// min = findPositions(min(false).intValue() != 0); // BOOLEAN_USE
			// min = findPositions(null); // OBJECT_USE
			storedValues.put(n, min);
		} else if (o instanceof List<?>) {
			min = (List<int[]>) o;
		} else {
			throw new InternalError("Inconsistent internal state of stored values for statistics calculation");
		}

		return min.get(0); // first minimum
	}

	@Override
	public boolean containsNans() {
		return false;
	}

	@Override
	public boolean containsInfs() {
		return false;
	}

	@Override
	public boolean containsInvalidNumbers() {
		return false;
	}

	@Override
	public ShortDataset iadd(final Object b) {
		if (b instanceof Dataset) {
			Dataset bds = (Dataset) b;
			checkCompatibility(bds);
			
			IndexIterator it1 = getIterator();
			IndexIterator it2 = bds.getIterator();
			
			while (it1.hasNext() && it2.hasNext()) {
				data[it1.index] += bds.getElementLongAbs(it2.index); // GET_ELEMENT
			}
		} else {
			final double v = toReal(b);
			IndexIterator it1 = getIterator();
			
			while (it1.hasNext()) {
				data[it1.index] += v;
			}
		}
		setDirty();
		return this;
	}

	@Override
	public ShortDataset isubtract(final Object b) {
		if (b instanceof Dataset) {
			Dataset bds = (Dataset) b;
			checkCompatibility(bds);
			
			IndexIterator it1 = getIterator();
			IndexIterator it2 = bds.getIterator();
			
			while (it1.hasNext() && it2.hasNext()) {
				data[it1.index] -= bds.getElementLongAbs(it2.index); // GET_ELEMENT
			}
		} else {
			final double v = toReal(b);
			IndexIterator it1 = getIterator();
			
			while (it1.hasNext()) {
				data[it1.index] -= v;
			}
		}
		setDirty();
		return this;
	}

	@Override
	public ShortDataset imultiply(final Object b) {
		if (b instanceof Dataset) {
			Dataset bds = (Dataset) b;
			checkCompatibility(bds);
			
			IndexIterator it1 = getIterator();
			IndexIterator it2 = bds.getIterator();
			
			while (it1.hasNext() && it2.hasNext()) {
				data[it1.index] *= bds.getElementLongAbs(it2.index); // GET_ELEMENT
			}
		} else {
			final double v = toReal(b);
			IndexIterator it1 = getIterator();
			// NAN_OMIT
			while (it1.hasNext()) {
				data[it1.index] *= v;
			}
		}
		setDirty();
		return this;
	}

	@Override
	public ShortDataset idivide(final Object b) {
		if (b instanceof Dataset) {
			Dataset bds = (Dataset) b;
			checkCompatibility(bds);
			
			IndexIterator it1 = getIterator();
			IndexIterator it2 = bds.getIterator();
			
			while (it1.hasNext() && it2.hasNext()) {
				try {
					data[it1.index] /= bds.getElementLongAbs(it2.index); // GET_ELEMENT // INT_EXCEPTION
				} catch (ArithmeticException e) {
					data[it1.index] = 0;
				}
			}
		} else {
			final double v = toReal(b);
			if (v == 0) { // INT_ZEROTEST
				fill(0); // INT_ZEROTEST
			} else { // INT_ZEROTEST
			IndexIterator it1 = getIterator();
			
			while (it1.hasNext()) {
				data[it1.index] /= v;
			}
			} // INT_ZEROTEST
		}
		setDirty();
		return this;
	}

	@Override
	public ShortDataset ifloor() {
		return this;
	}

	@Override
	public ShortDataset iremainder(final Object b) {
		if (b instanceof Dataset) {
			Dataset bds = (Dataset) b;
			checkCompatibility(bds);
			
			IndexIterator it1 = getIterator();
			IndexIterator it2 = bds.getIterator();
			
			while (it1.hasNext() && it2.hasNext()) {
				try {
					data[it1.index] %= bds.getElementLongAbs(it2.index); // GET_ELEMENT // INT_EXCEPTION
				} catch (ArithmeticException e) {
					data[it1.index] = 0;
				}
			}
		} else {
			final double v = toReal(b);
			if (v == 0) { // INT_ZEROTEST
				fill(0); // INT_ZEROTEST
			} else { // INT_ZEROTEST
			IndexIterator it1 = getIterator();
			
			while (it1.hasNext()) {
				data[it1.index] %= v;
			}
			} // INT_ZEROTEST
		}
		setDirty();
		return this;
	}

	@Override
	public ShortDataset ipower(final Object b) {
		if (b instanceof Dataset) {
			Dataset bds = (Dataset) b;
			checkCompatibility(bds);
			
			IndexIterator it1 = getIterator();
			IndexIterator it2 = bds.getIterator();
			
			while (it1.hasNext() && it2.hasNext()) {
				final double v = Math.pow(data[it1.index], bds.getElementDoubleAbs(it2.index));
				if (Double.isInfinite(v) || Double.isNaN(v)) { // INT_ZEROTEST
					data[it1.index] = 0; // INT_ZEROTEST
				} else { // INT_ZEROTEST
				data[it1.index] = (short) (long) v; // PRIM_TYPE_LONG // ADD_CAST
				} // INT_ZEROTEST
			}
		} else {
			double vr = toReal(b);
			double vi = toImag(b);
			IndexIterator it1 = getIterator();
			
			if (vi == 0.) {
				while (it1.hasNext()) {
					final double v = Math.pow(data[it1.index], vr);
					if (Double.isInfinite(v) || Double.isNaN(v)) { // INT_ZEROTEST
						data[it1.index] = 0; // INT_ZEROTEST
					} else { // INT_ZEROTEST
					data[it1.index] = (short) (long) v; // PRIM_TYPE_LONG // ADD_CAST
					} // INT_ZEROTEST
				}
			} else {
				Complex zv = new Complex(vr, vi);
				while (it1.hasNext()) {
					Complex zd = new Complex(data[it1.index], 0.);
					final double v = zd.pow(zv).getReal();
					if (Double.isInfinite(v) || Double.isNaN(v)) { // INT_ZEROTEST
						data[it1.index] = 0; // INT_ZEROTEST
					} else { // INT_ZEROTEST
					data[it1.index] = (short) (long) v; // PRIM_TYPE_LONG // ADD_CAST
					} // INT_ZEROTEST
				}
			}
		}
		setDirty();
		return this;
	}

	@Override
	public double residual(final Object b, final Dataset w, boolean ignoreNaNs) {
		double sum = 0;
		if (b instanceof Dataset) {
			Dataset bds = (Dataset) b;
			checkCompatibility(bds);
			
			IndexIterator it1 = getIterator();
			IndexIterator it2 = bds.getIterator();
			
			double comp = 0;
			{
				if (w == null) {
					while (it1.hasNext() && it2.hasNext()) {
						final double diff = data[it1.index] - bds.getElementDoubleAbs(it2.index);
						final double err = diff * diff - comp;
						final double temp = sum + err;
						comp = (temp - sum) - err;
						sum = temp;
					}
				} else {
					IndexIterator it3 = w.getIterator();
					while (it1.hasNext() && it2.hasNext() && it3.hasNext()) {
						final double diff = data[it1.index] - bds.getElementDoubleAbs(it2.index);
						final double err = diff * diff * w.getElementDoubleAbs(it3.index) - comp;
						final double temp = sum + err;
						comp = (temp - sum) - err;
						sum = temp;
					}
				}
			}
		} else {
			final double v = toReal(b);
			IndexIterator it1 = getIterator();

			double comp = 0;
			if (w == null) {
				while (it1.hasNext()) {
					final double diff = data[it1.index] - v;
					final double err = diff * diff - comp;
					final double temp = sum + err;
					comp = (temp - sum) - err;
					sum = temp;
				}
			} else {
				IndexIterator it3 = w.getIterator();
				while (it1.hasNext() && it3.hasNext()) {
					final double diff = data[it1.index] - v;
					final double err = diff * diff * w.getElementDoubleAbs(it3.index) - comp;
					final double temp = sum + err;
					comp = (temp - sum) - err;
					sum = temp;
				}
			}
		}
		return sum;
	}
}
