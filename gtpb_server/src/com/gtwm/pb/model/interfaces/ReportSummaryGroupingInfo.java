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

import com.gtwm.pb.model.interfaces.ReportFieldInfo;
import com.gtwm.pb.util.Enumerations.SummaryGroupingModifier;

import java.util.Date;

/**
 * Represents a field to group by in a report summary
 */
public interface ReportSummaryGroupingInfo extends Comparable<ReportSummaryGroupingInfo> {

	public ReportFieldInfo getGroupingReportField();

	/**
	 * Return the modifier (if any) that should be used with this field. For
	 * example, if the field is a date, the modifier could be 'year' which would
	 * cause the grouping to be on the year only
	 */
	public SummaryGroupingModifier getGroupingModifier();

	/**
	 * Used to sort grouping fields, ensuring earliest are at the start of the
	 * report summary
	 */
	public Date getCreationTime();

}
