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

package uk.ac.diamond.scisoft.analysis.roi;

import java.util.ArrayList;


/**
 * Wrapper for a list of linear ROIs
 */
public class LinearROIList extends ArrayList<LinearROI> implements ROIList<LinearROI> {

	/**
	 * Add roi to list (if it is of correct class)
	 * @param roi
	 * @return true if added
	 */
	@Override
	public boolean add(IROI roi) {
		if (roi instanceof LinearROI)
			return super.add((LinearROI) roi);
		return false;
	}
}
