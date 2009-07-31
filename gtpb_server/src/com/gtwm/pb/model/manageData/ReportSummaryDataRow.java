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
package com.gtwm.pb.model.manageData;

import com.gtwm.pb.model.interfaces.ReportSummaryDataRowInfo;
import com.gtwm.pb.model.interfaces.ReportSummaryAggregateInfo;
import com.gtwm.pb.model.interfaces.ReportSummaryGroupingInfo;

import java.util.Map;
import java.util.LinkedHashMap;

public class ReportSummaryDataRow implements ReportSummaryDataRowInfo {

    public ReportSummaryDataRow() {
    }

    public synchronized void addGroupingValue(ReportSummaryGroupingInfo grouping, String value) {
        this.groupingFieldValues.put(grouping, value);
    }

    public synchronized void addAggregateValue(ReportSummaryAggregateInfo aggregateFunction, Number value) {
        this.aggregateValues.put(aggregateFunction, value);
    }

    public synchronized String getGroupingValue(ReportSummaryGroupingInfo grouping) {
        String groupingFieldValue = this.groupingFieldValues.get(grouping);
        if (groupingFieldValue == null) {
        	return "";
        } else {
        	return groupingFieldValue;
        }
    }

    public synchronized Number getAggregateValue(ReportSummaryAggregateInfo aggregateFunction) {
        return this.aggregateValues.get(aggregateFunction);
    }
    
    public String toString() {
        String returnValue = "<tr>";
        for (String groupingValue : this.groupingFieldValues.values()) {
            returnValue += "<td>" + groupingValue + "</td>";
        }
        for (Number aggregateValue : this.aggregateValues.values()) {
            returnValue += "<td>" + aggregateValue.toString() + "</td>";
        }
        returnValue += "</tr>";
        return returnValue;
    }

    private Map<ReportSummaryGroupingInfo, String> groupingFieldValues = new LinkedHashMap<ReportSummaryGroupingInfo, String>();

    private Map<ReportSummaryAggregateInfo, Number> aggregateValues = new LinkedHashMap<ReportSummaryAggregateInfo, Number>();
}
