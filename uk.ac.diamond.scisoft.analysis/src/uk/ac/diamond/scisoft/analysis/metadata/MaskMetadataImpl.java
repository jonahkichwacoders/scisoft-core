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

package uk.ac.diamond.scisoft.analysis.metadata;

import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;

public class MaskMetadataImpl implements MaskMetadata {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Sliceable
	ILazyDataset mask;
	
	public MaskMetadataImpl(ILazyDataset mask) {
		this.mask = mask;
	}

	@Override
	public ILazyDataset getMask() {
		return mask;
	}
	
	@Override
	public MaskMetadata clone() {
		return new MaskMetadataImpl(mask);
	}

}