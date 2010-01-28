/*
 *  Copyright 2009 GT webMarque Ltd
 * 
 *  This file is part of agileBase.
 *
 *  agileBase is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  agileBase is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with agileBase.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gtwm.pb.model.interfaces.fields;

import com.gtwm.pb.model.interfaces.ReportCalcFieldInfo;

/**
 * This class is a placeholder just to allow ReportField.getBaseField() to work
 * in the case of calculation fields. Calcs aren't proper table fields and
 * should never be persisted to Hibernate. All the interesting methods are in
 * ReportCalcFieldDefn
 * 
 * @see com.gtwm.pb.model.interfaces.ReportCalcFieldInfo
 */
public interface CalculationField extends BaseField {
	/**
	 * Return the calculation report field that wraps this 'Base' field and
	 * contains the calculation methods. Note that normally you shouldn't use
	 * this method but instead call methods from the wrapping calc report field
	 * directly
	 */
	public ReportCalcFieldInfo getReportCalcField();
}
