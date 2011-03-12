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

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.grlea.log.SimpleLogger;
import com.gtwm.pb.model.interfaces.ChartAggregateInfo;
import com.gtwm.pb.model.interfaces.ChartDataInfo;
import com.gtwm.pb.model.interfaces.ChartDataRowInfo;
import com.gtwm.pb.util.ObjectNotFoundException;

public class ChartData implements ChartDataInfo {

	private ChartData() {
		this.reportSummaryDataRows = null;
		this.minAggValues = null;
		this.maxAggValues = null;
		this.grandTotals = null;
	}

	/**
	 * @param reportSummaryDataRows
	 *            The report summary data
	 * @param minAggValues
	 *            A map containing the minimum data value for each aggregate
	 *            function. Useful in charting
	 * @param maxAggValues
	 *            Similar to minAggValues
	 */
	public ChartData(List<ChartDataRowInfo> reportSummaryDataRows,
			Map<ChartAggregateInfo, Number> minAggValues,
			Map<ChartAggregateInfo, Number> maxAggValues,
			Map<ChartAggregateInfo, Number> grandTotals) {
		this.reportSummaryDataRows = reportSummaryDataRows;
		this.minAggValues = minAggValues;
		this.maxAggValues = maxAggValues;
		this.grandTotals = grandTotals;
	}

	public List<ChartDataRowInfo> getChartDataRows() {
		return this.reportSummaryDataRows;
	}

	public int getValueAsPercentage(ChartAggregateInfo aggregate, Number value) {
		if (value == null) {
			return 0;
		}
		Number minNum = this.minAggValues.get(aggregate);
		Number maxNum = this.maxAggValues.get(aggregate);
		if (minNum == null || maxNum == null) {
			return 0;
		}
		double minValue = 0;
		double maxValue = maxNum.doubleValue();
		double valueDouble = value.doubleValue();
		double range = maxValue - minValue;
		if (range == 0.0d) {
			return 0;
		}
		Double percentage = ((valueDouble - minValue) / range) * 100;
		return percentage.intValue();
	}

	public double getGrandTotal(ChartAggregateInfo aggregate)
			throws ObjectNotFoundException {
		Number grandTotal = this.grandTotals.get(aggregate);
		if (grandTotal == null) {
			throw new ObjectNotFoundException("Grand total not found for aggregate " + aggregate);
		}
		return grandTotal.doubleValue();
	}

	public long getCacheCreationTime() {
		return this.cacheCreationTime;
	}

	public String toString() {
		return this.reportSummaryDataRows.toString();
	}

	private final List<ChartDataRowInfo> reportSummaryDataRows;

	private final Map<ChartAggregateInfo, Number> minAggValues;

	private final Map<ChartAggregateInfo, Number> maxAggValues;

	private final Map<ChartAggregateInfo, Number> grandTotals;

	/**
	 * Set cache creation time to the creation time of the object
	 */
	private final long cacheCreationTime = (new Date()).getTime();

	private static final SimpleLogger logger = new SimpleLogger(ChartData.class);
}
