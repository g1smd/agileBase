/*
 *  Copyright 2011 GT webMarque Ltd
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

import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.util.CantDoThatException;

/**
 * Like SeparatorField, not a data input field. When this field is included in a
 * table, data from the report specified will be pulled in and displayed in the
 * space (read only), where the current row ID is found in the referenced
 * report. Therefore the referenced report must include this field's parent
 * table's ID field
 */
public interface ReferencedReportDataField extends BaseField {

	/**
	 * Return the report that contains the data to display. The report must
	 * contain this field's parent table's primary key (ID) field. Data from all
	 * rows where the IDs match will be coalesced and returned
	 * 
	 * @throws CantDoThatException
	 *             if the list of candidate reports hasn't been set yet
	 */
	public BaseReportInfo getReferencedReport() throws CantDoThatException;
	
}