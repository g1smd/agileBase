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

import java.util.List;
import java.util.Map;
import org.grlea.log.SimpleLogger;
import com.gtwm.pb.model.interfaces.ReportSummaryAggregateInfo;
import com.gtwm.pb.model.interfaces.ReportSummaryDataInfo;
import com.gtwm.pb.model.interfaces.ReportSummaryDataRowInfo;
import com.gtwm.pb.util.ObjectNotFoundException;

public class ReportSummaryData implements ReportSummaryDataInfo {

    private ReportSummaryData() {    
    }

    /**
     * @param reportSummaryDataRows	The report summary data
     * @param minAggValues	A map containing the minimum data value for each aggregate function. Useful in charting
     * @param maxAggValues	Similar to minAggValues
     */
    public ReportSummaryData(List<ReportSummaryDataRowInfo> reportSummaryDataRows, Map<ReportSummaryAggregateInfo, Number> minAggValues, Map<ReportSummaryAggregateInfo, Number> maxAggValues, Map<ReportSummaryAggregateInfo, Number> grandTotals) {
        this.reportSummaryDataRows = reportSummaryDataRows;
        this.minAggValues = minAggValues;
        this.maxAggValues = maxAggValues;
        this.grandTotals = grandTotals;
    }

    public List<ReportSummaryDataRowInfo> getReportSummaryDataRows() {
        return this.reportSummaryDataRows;
    }

    public int getValueAsPercentage(ReportSummaryAggregateInfo aggregate, Number value) {
    	if (value == null) {
    		return 0;
    	}
    	Number minNum = this.minAggValues.get(aggregate);
    	Number maxNum = this.maxAggValues.get(aggregate);
    	if (minNum == null || maxNum == null) {
    		return 0;
    	}
    	//double minValue = minNum.doubleValue();
    	double minValue = 0;
    	//if (minValue > 0) {
    	//	minValue = 0;
    	//}
    	double maxValue = maxNum.doubleValue();
    	double valueDouble = value.doubleValue();
    	double range = maxValue - minValue;
    	if (range == 0.0d) {
    		return 0;
    	}
    	Double percentage = ((valueDouble - minValue) / range) * 100;
    	return percentage.intValue();
    }
    
    public double getGrandTotal(ReportSummaryAggregateInfo aggregate) throws ObjectNotFoundException {
    	Number grandTotal = this.grandTotals.get(aggregate);
    	if (grandTotal == null) {
    		throw new ObjectNotFoundException("Grand total not found for aggregate " + aggregate);
    	}
    	return grandTotal.doubleValue();
    }
    
   public String toString() {
        return this.reportSummaryDataRows.toString();
    }

    private List<ReportSummaryDataRowInfo> reportSummaryDataRows = null;
    
    private Map<ReportSummaryAggregateInfo, Number> minAggValues = null;
    
    private Map<ReportSummaryAggregateInfo, Number> maxAggValues = null;

    private Map<ReportSummaryAggregateInfo, Number> grandTotals = null;

    private static final SimpleLogger logger = new SimpleLogger(ReportSummaryData.class);
}
