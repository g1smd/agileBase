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

import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.ReportSummaryAggregateInfo;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.ObjectNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.List;

/**
 * Stores information on how to summarise a report - allows you to generate a
 * report table and report graphs. In SQL terms, a report summary is a report
 * using GROUP BY and aggregate functions such as count(*), sum(). You can only
 * group by certain types of field, as you can see from the methods. Similary,
 * you can only add functions on certain types of field.
 */
public interface ReportSummaryInfo {

	/**
	 * Group by a field filled from a report
	 */
	public void addGrouping(ReportFieldInfo groupByReportField);

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
	 */
	public List<ReportFieldInfo> getGroupingReportFields();

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
	 * @return True if the summary contains any numeric i.e non-COUNT aggregate
	 *         functions, e.g SUM, AVERAGE etc.
	 * @see com.gtwm.pb.model.manageSchema.SimpleReportDefn#addField(BaseField)
	 *      See SimpleReportDefn.addField for a use of this function
	 */
	public boolean containsNumericAggFns();

	/**
	 * @return The report that is being summarized. Each report maps one to one
	 *         to a summary
	 */
	public BaseReportInfo getReport();
}
