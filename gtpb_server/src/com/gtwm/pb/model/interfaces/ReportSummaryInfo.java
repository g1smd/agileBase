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

import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.ReportSummaryAggregateInfo;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.Enumerations.SummaryFilter;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.Enumerations.SummaryGroupingModifier;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.SortedSet;

/**
 * Stores information on how to summarise a report - allows you to generate a
 * report table and report graphs. In SQL terms, a report summary is a report
 * using GROUP BY and aggregate functions such as count(*), sum(). You can only
 * group by certain types of field, as you can see from the methods. Similarly,
 * you can only add functions on certain types of field.
 */
public interface ReportSummaryInfo {

	/**
	 * Group by a field filled from a report
	 */
	public void addGrouping(ReportFieldInfo groupByReportField,
			SummaryGroupingModifier groupingModifier);

	/**
	 * Remove field from grouping. If field isn't in grouping, do nothing, don't
	 * raise an exception
	 * 
	 * @return the removed grouping, or null if no grouping on the fieldToRemove
	 *         exists
	 */
	public ReportSummaryGroupingInfo removeGrouping(ReportFieldInfo reportFieldToRemove);

	/**
	 * Add an aggregate function
	 */
	public void addFunction(ReportSummaryAggregateInfo addedAggFn) throws CantDoThatException;

	/**
	 * Remove any aggregate functions acting on the specified field - there may
	 * be 0, 1 or more. If 0, take no action and don't raise an exception.
	 * 
	 * This is used when removing a field from a report, to remove any
	 * aggregates on that field
	 * 
	 * @return the set of functions acting on fieldToRemove, which were removed
	 */
	public Set<ReportSummaryAggregateInfo> removeFunctions(ReportFieldInfo reportFieldToRemove);

	/**
	 * Remove a single aggregate function
	 */
	public ReportSummaryAggregateInfo removeFunction(String internalAggregateName)
			throws ObjectNotFoundException;

	/**
	 * Return the title given to this report summary, e.g. to be used as a chart
	 * title
	 */
	public String getTitle();

	public void setTitle(String title);

	/**
	 * Return a unique ID for the summary
	 */
	public long getId();

	/**
	 * Return a prepared statement that is for getting the summary (aggregate)
	 * data for a report
	 * 
	 * @return A string of SQL used to get the summary data from the database
	 */
	public PreparedStatement getReportSummarySqlPreparedStatement(Connection conn,
			Map<BaseField, String> filterValues, boolean exactFilters) throws SQLException,
			CantDoThatException;

	/**
	 * @return A read-only copy of the report summary's grouping field list
	 * @deprecated Replaced by getGroupings()
	 */
	public List<ReportFieldInfo> getGroupingReportFields();

	public SortedSet<ReportSummaryGroupingInfo> getGroupings();

	/**
	 * Return a single grouping field
	 */
	public ReportFieldInfo getGroupingReportField(String internalFieldName)
			throws ObjectNotFoundException;

	/**
	 * @return A read-only copy of the report summary's aggregate function list
	 */
	public Set<ReportSummaryAggregateInfo> getAggregateFunctions();

	public ReportSummaryAggregateInfo getAggregateFunctionByInternalName(
			String internalAggregateName) throws ObjectNotFoundException;

	/**
	 * Return the filter acting on this summary, or null if there is none
	 */
	public SummaryFilter getSummaryFilter();

	public void setSummaryFilter(SummaryFilter summaryFilter);

	/**
	 * Return the field that the summary filter acts on
	 */
	public ReportFieldInfo getFilterReportField();

	public void setFilterReportField(ReportFieldInfo reportField);

	/**
	 * @return True if the summary contains any numeric i.e non-COUNT aggregate
	 *         functions, e.g SUM, AVERAGE etc.
	 * @see com.gtwm.pb.model.manageSchema.SimpleReportDefn#addField(BaseField)
	 *      See SimpleReportDefn.addField for a use of this function
	 */
	public boolean containsNumericAggFns();

	/**
	 * Affects the LIMIT SQL clause, i.e. return a percentage of the total rows
	 * 
	 * @see getRangeDirection()
	 */
	public int getRangePercent();
	
	public void setRangePercent(int rangePercent);

	/**
	 * True represents the top of the range, false the bottom. so a range
	 * direction of true and a range percent of 25% would mean return the top
	 * 25% of rows, i.e. the upper quartile
	 * 
	 * @see getRangePercent()
	 */
	public boolean getRangeDirection();

	public void setRangeDirection(boolean rangeDirection);
	
	public static final boolean UPPER_RANGE = true;
	
	public static final boolean LOWER_RANGE = false;

	/**
	 * @return The report that is being summarized. Each report maps one to one
	 *         to a summary
	 */
	public BaseReportInfo getReport();
	
}
