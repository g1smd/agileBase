/*
 *  Copyright 2010 GT webMarque Ltd
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
package com.gtwm.pb.model.interfaces;

import com.gtwm.pb.model.interfaces.ReportFieldInfo;
import com.gtwm.pb.util.Enumerations.AggregateFunction;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.Enumerations.AggregateRange;

/**
 * Simple definition of a single aggregate function used in a report summary,
 * e.g sum(myTable.myNumericField). Stores a pair of items - the function and
 * the field it operates on.
 */
public interface ReportSummaryAggregateInfo {

	/**
	 * @throws CantDoThatException
	 *             If the aggregate representation doesn't contain enough
	 *             information to create SQL from it
	 * @return Some text for this function that can form part of an SQL
	 *         statement, e.g sum(myTable.myNumericField), count(*)
	 */
	public String getSQLPartForAggregate() throws CantDoThatException;

	/**
	 * @return The field the aggregate function acts on
	 */
	public ReportFieldInfo getReportField();
	
	/**
	 * @return A second field if the aggregate function acts on two fields, otherwise null
	 */
	public ReportFieldInfo getSecondaryReportField();

	/**
	 * @return True if the object represents a COUNT(*) function
	 */
	public boolean isCountFunction();

	public AggregateFunction getAggregateFunction();

	public AggregateRange getAggregateRange();
	
	/**
	 * Used when comparing objects for equality
	 */
	public String getInternalAggregateName();
}
