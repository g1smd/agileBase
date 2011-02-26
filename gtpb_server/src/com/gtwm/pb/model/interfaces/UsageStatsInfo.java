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

import java.util.SortedSet;
import java.util.List;
import java.sql.SQLException;
import org.json.JSONException;
import com.gtwm.pb.auth.DisallowedException;
import com.gtwm.pb.model.manageUsage.UsageLogger.LogType;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.CantDoThatException;

/**
 * Allows access to usage stats for the company and calculates the monthly
 * charge based on the number of tables
 */
public interface UsageStatsInfo {

	/**
	 * Return the hosted cost of agileBase, based on the number of tables
	 * present
	 */
	public int getMonthlyTableCost() throws ObjectNotFoundException, DisallowedException;

	/**
	 * Return the number of tables in the company
	 */
	public int getNumberOfTables() throws ObjectNotFoundException, DisallowedException;

	/**
	 * Return data that can be used to build a treemap of report view
	 * statistics. Reports are grouped by module
	 * 
	 * @return JSON formatted data suitable for use with the Infoviz toolkit, OR
	 *         a plain String error message
	 */
	public String getTreeMapJSON() throws ObjectNotFoundException, DisallowedException,
			SQLException, JSONException, CodingErrorException;

	/**
	 * Return module viewing stats, sorted by the total number of report views
	 * per module
	 * 
	 * @Deprecated No current UI templates use this. Functionality replaced by
	 *             getTreeMapJSON()
	 */
	@Deprecated
	public SortedSet<ModuleUsageStatsInfo> getModuleStats() throws DisallowedException,
			ObjectNotFoundException, SQLException;

	/**
	 * Return raw data about the current company from the log tables, suitable
	 * for exporting to a spreadsheet for example
	 * 
	 * Format is a list of rows, each row consisting of a list of columns.
	 * 
	 * Internal names such as internal report names in the log will be replaced
	 * with user friendly names.
	 * 
	 * @see com.gtwm.pb.model.manageUsage.LogType See LogType for a list of
	 *      allowed log types
	 */
	public List<List<String>> getRawStats(String logType) throws DisallowedException, SQLException,
			ObjectNotFoundException, CantDoThatException;

	/**
	 * Get raw stats about usage of a particular table - data changes or table
	 * schema changes
	 * 
	 * @param logType
	 *            Can be DATA_CHANGE or TABLE_SCHEMA_CHANGE
	 * @param rowLimit
	 *            Limit results to the top rowLimit newest log entries
	 * @deprecated Nothing seems to use this currently
	 */
	public List<List<String>> getRawTableStats(LogType logType, TableInfo table, int rowLimit)
			throws DisallowedException, SQLException, CantDoThatException, ObjectNotFoundException;

	/**
	 * Get a list of reports that data from this table is included in along with
	 * some overview stats about each report
	 * 
	 * Columns for each row returned are: report, username, last access date,
	 * total access count
	 */
	public List<List<String>> getTableViewStats(TableInfo table) throws DisallowedException,
			SQLException, CodingErrorException, CantDoThatException, ObjectNotFoundException;

	/**
	 * Get report view stats including a list of users that have been viewing a
	 * report along with some overview stats about each
	 */
	public ReportViewStatsInfo getReportViewStats(BaseReportInfo report)
			throws DisallowedException, SQLException, CodingErrorException, CantDoThatException,
			ObjectNotFoundException;

	/**
	 * Return any tables in the company that have had no report views (from any
	 * child reports)
	 */
	public SortedSet<TableInfo> getUnusedTables() throws DisallowedException, SQLException,
			ObjectNotFoundException;

	/**
	 * Return counts of the total no. log entries per week, in order from oldest
	 * to newest
	 * 
	 * @param options
	 *            For the schema change logs, the constant 1 means only return
	 *            log entries that are to do with schema building. -1 means
	 *            return those that are to do with demolishing, e.g. remove a
	 *            field, remove a table. The parameter is ignored for other log
	 *            types
	 * 
	 * @see com.gtwm.pb.model.manageUsage.LogType See LogType for a list of
	 *      allowed log types
	 */
	public List<Integer> getTimelineCounts(String logType, int options) throws DisallowedException,
			SQLException, ObjectNotFoundException;

	/**
	 * Returns a description of the last login time of the user
	 * 
	 * e.g. '3 days ago'
	 */
	public String getLastLoginAge(AppUserInfo user) throws SQLException;
}
