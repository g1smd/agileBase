/*
 *  Copyright 2009 GT webMarque Ltd
 * 
 *  This file is part of GT portalBase.
 *
 *  GT portalBase is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  GT portalBase is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with GT portalBase.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gtwm.pb.model.interfaces;

/**
 * Stores a single row of data in the report summary table. A report summary row is made up of two parts -
 * grouping field values and aggregate function values
 */
public interface ReportSummaryDataRowInfo {
    
    public void addGroupingValue(ReportFieldInfo groupingReportField, String value);
    
    public void addAggregateValue(ReportSummaryAggregateInfo aggregateFunction, Number value);

    public String getGroupingValue(ReportFieldInfo groupingReportField);

    public Number getAggregateValue(ReportSummaryAggregateInfo aggregateFunction);
}
