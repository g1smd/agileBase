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
package com.gtwm.pb.model.interfaces;

import java.util.List;

import com.gtwm.pb.util.ObjectNotFoundException;

public interface ReportSummaryDataInfo {
	/**
	 * @return The data that this summary holds
	 */
	public List<ReportSummaryDataRowInfo> getReportSummaryDataRows();

	/**
	 * This object stores information about the max. and min. values in the data
	 * rows for each aggregate, so given a value, can calculate it as a % e.g.
	 * if the min. value of a particular aggregate is 0 and the max 50, then the
	 * value 25 would be 50%
	 */
	public int getValueAsPercentage(ReportSummaryAggregateInfo aggregate, Number value);
	
	/**
	 * For sum and count aggregates, grand totals are stored
	 */
	public double getGrandTotal(ReportSummaryAggregateInfo aggregate) throws ObjectNotFoundException;
}
