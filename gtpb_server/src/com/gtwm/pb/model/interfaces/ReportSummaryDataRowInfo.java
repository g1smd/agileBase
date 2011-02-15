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
package com.gtwm.pb.model.interfaces;

import java.util.Map;

/**
 * Stores a single row of data in the report summary table. A report summary row is made up of two parts -
 * grouping field values and aggregate function values
 */
public interface ReportSummaryDataRowInfo {
    
    public void addGroupingValue(ReportSummaryGroupingInfo grouping, String value);
    
    public void addAggregateValue(ReportSummaryAggregateInfo aggregateFunction, Number value);

    public String getGroupingValue(ReportSummaryGroupingInfo grouping);

    /**
     * @deprecated Replaced by #getGroupingValue(ReportSummaryGroupingInfo)
     */
    public String getGroupingValue(ReportFieldInfo groupingField);

    public Number getAggregateValue(ReportSummaryAggregateInfo aggregateFunction);
    
    public Map<ReportSummaryAggregateInfo, Number> getAggregateValues();
    
    public Map<ReportSummaryGroupingInfo, String> getGroupingValues();
}
