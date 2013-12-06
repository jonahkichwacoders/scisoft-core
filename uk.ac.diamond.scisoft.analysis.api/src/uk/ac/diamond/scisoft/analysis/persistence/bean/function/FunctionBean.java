/*-
 * Copyright 2013 Diamond Light Source Ltd.
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
package uk.ac.diamond.scisoft.analysis.persistence.bean.function;

import java.util.Arrays;

import uk.ac.diamond.scisoft.analysis.fitting.functions.IParameter;

/**
 * Function bean used to marshall/unmarshall to / from JSON strings <br>
 * A converter can be used to convert this bean to IFunction<br> 
 * (See uk.ac.diamond.scisoft.analysis.persistence.bean.function.FunctionBeanConverter) 
 */
public class FunctionBean {

	public static final int TYPE_GAUSSIAN = 0;
	public static final int TYPE_CUBIC = 1;
	public static final int TYPE_FERMI = 2;
	public static final int TYPE_FERMI_GAUSS = 3;
	public static final int TYPE_LORENTZIAN = 4;
	public static final int TYPE_OFFSET = 5;
	public static final int TYPE_PEARSONVII = 6;
	public static final int TYPE_POLYNOMIAL = 7;
	public static final int TYPE_PSEUDO_VOIGT = 8;
	public static final int TYPE_QUADRATIC = 9;
	public static final int TYPE_STEP = 10;
	public static final int TYPE_STRAIGHT_LINE = 11;

	protected String name;
	protected int type;
	protected IParameter[] parameters;

	public FunctionBean() {
		
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public IParameter[] getParameters() {
		return parameters;
	}
	public void setParameters(IParameter[] parameters) {
		this.parameters = parameters;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + Arrays.hashCode(parameters);
		result = prime * result + type;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FunctionBean other = (FunctionBean) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (!Arrays.equals(parameters, other.parameters))
			return false;
		if (type != other.type)
			return false;
		return true;
	}
}
