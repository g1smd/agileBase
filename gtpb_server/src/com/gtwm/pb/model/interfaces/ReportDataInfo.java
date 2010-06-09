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
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.CantDoThatException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Retrieves and formats data for a particular report, for output to the user
 */
public interface ReportDataInfo {
	/**
	 * @param filterValues
	 *            The values requested by the browser, e.g. if the request
	 *            string was ?...&field1=filtervalue, there should be one map
	 *            key: the field with the internal name field1 and one value
	 *            "filtervalue"
	 * @param rowLimit
	 *            The maximum number of rows to return, -1 means no limit
	 * @return Report data as ArrayList of ReportDataRow
	 * @see http 
	 *      ://java.sun.com/j2ee/sdk_1.3/techdocs/api/javax/servlet/ServletRequest
	 *      .html#getParameterMap() HTTPServletRequest.getParameterMap generates
	 *      the filterValues map
	 */
	public List<DataRowInfo> getReportDataRows(Connection conn,
			Map<BaseField, String> filterValues, boolean exactFilters,
			Map<BaseField, Boolean> reportSorts, int rowLimit) throws SQLException,
			CodingErrorException, CantDoThatException;

	/**
	 * Return true if the record identified by the given number is present in
	 * the report
	 */
	public boolean isRowIdInReport(Connection conn, int rowId) throws SQLException;

	/**
	 * Generate an SQL PreparedStatement object for getting the report VIEW
	 * definition. Usually code would call getReportDataRows instead, this
	 * method is only useful in a few particular circumstances where more
	 * control over the returned data is required
	 * 
	 * @param selectField
	 *            If selectField is null, prepare an SQL 'SELECT * FROM'
	 *            otherwise prepare a 'SELECT DISTINCT selectField FROM'
	 * 
	 * @see #getReportDataRows(Connection, Map, int) Please consider using this
	 *      method instead as it is higher level
	 */
	public PreparedStatement getReportSqlPreparedStatement(Connection conn,
			Map<BaseField, String> filterValues, boolean exactFilters,
			Map<BaseField, Boolean> reportSorts, int rowLimit, BaseField selectField)
			throws SQLException, CantDoThatException;

	/**
	 * Generate a SQL WHERE clause that will work on a report given a map of
	 * filter values.
	 * 
	 * @return A Map containing one entry. The string key is the WHERE clause
	 *         (not containing the word 'WHERE') containing question marks for
	 *         filter values. The value is an ordered list of filters that can
	 *         be used to provide a value for each question mark in a prepared
	 *         statement
	 */
	public Map<String, List<ReportQuickFilterInfo>> getWhereClause(
			Map<BaseField, String> filterValues, boolean exactFilters) throws CantDoThatException;

	/**
	 * Given a set of filters (from getWhereClause) and the PreparedStatement
	 * for the report, match the filter values to the parameters in the
	 * statement and set the values accordingly.
	 * 
	 * This method is public so that it can be used by summary reports as well
	 */
	public void fillInFilterValues(List<ReportQuickFilterInfo> filtersUsed,
			PreparedStatement statement) throws SQLException;

	/**
	 * @return Whether the object has exceeded the time it's worth keeping in
	 *         the cache. This depends on the time it took to construct
	 */
	public boolean exceededCacheTime();

	/**
	 * Get the time that this report data object was created (and hopefully
	 * cached)
	 */
	public long getCacheCreationTime();

	/**
	 * Return the mean and standard deviation of every field in the report, for
	 * the data in it
	 */
	public Map<ReportFieldInfo, ReportDataFieldStatsInfo> getFieldStats();
}
