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
package com.gtwm.pb.model.manageData;

import com.gtwm.pb.model.interfaces.ReportFieldInfo;
import com.gtwm.pb.model.interfaces.ChartDataRowInfo;
import com.gtwm.pb.model.interfaces.ChartAggregateInfo;
import com.gtwm.pb.model.interfaces.ChartGroupingInfo;

import java.util.Map;
import java.util.LinkedHashMap;

public class ChartDataRow implements ChartDataRowInfo {

	public ChartDataRow() {
	}

	public synchronized void addGroupingValue(ChartGroupingInfo grouping, String value) {
		this.groupingValues.put(grouping, value);
	}

	public synchronized void addAggregateValue(ChartAggregateInfo aggregateFunction,
			Number value) {
		this.aggregateValues.put(aggregateFunction, value);
	}

	public synchronized String getGroupingValue(ChartGroupingInfo grouping) {
		String groupingValue = this.groupingValues.get(grouping);
		if (groupingValue == null) {
			return "";
		} else {
			return groupingValue;
		}
	}

	public String getGroupingValue(ReportFieldInfo groupingField) {
		for (ChartGroupingInfo grouping : this.groupingValues.keySet()) {
			if (groupingField.equals(grouping.getGroupingReportField())) {
				return this.getGroupingValue(grouping);
			}
		}
		return "";
	}

	public synchronized Number getAggregateValue(ChartAggregateInfo aggregateFunction) {
		return this.aggregateValues.get(aggregateFunction);
	}
	
	public synchronized Map<ChartAggregateInfo, Number> getAggregateValues() {
		return this.aggregateValues;
	}
	
	public synchronized Map<ChartGroupingInfo, String> getGroupingValues() {
		return this.groupingValues;
	}

	public String toString() {
		String returnValue = "<tr>";
		for (String groupingValue : this.groupingValues.values()) {
			returnValue += "<td>" + groupingValue + "</td>";
		}
		for (Number aggregateValue : this.aggregateValues.values()) {
			returnValue += "<td>" + aggregateValue.toString() + "</td>";
		}
		returnValue += "</tr>";
		return returnValue;
	}

	private Map<ChartGroupingInfo, String> groupingValues = new LinkedHashMap<ChartGroupingInfo, String>();

	private Map<ChartAggregateInfo, Number> aggregateValues = new LinkedHashMap<ChartAggregateInfo, Number>();
}
