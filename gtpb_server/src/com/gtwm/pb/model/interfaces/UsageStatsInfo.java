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
 * Stores usage stats for the company and calculates the monthly charge based on
 * the number of tables
 */
public interface UsageStatsInfo {

	/**
	 * Monthly charge is calculated as £10/table for the first ten tables then
	 * £5/table for any more
	 */
	public int getMonthlyTableCost();

	/**
	 * Return the number of tables in the company
	 */
	public int getNumberOfTables();

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
	 * Return raw data from the log tables, suitable for exporting to a
	 * spreadsheet for example
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
	 */
	public List<List<String>> getRawTableStats(LogType logType, TableInfo table, int rowLimit)
			throws DisallowedException, SQLException, CantDoThatException;

	/**
	 * Get a list of reports that data from this table is included in along with
	 * some overview stats about each report
	 * 
	 * Columns for each row returned are: report, username, last access date,
	 * total access count
	 */
	public List<List<String>> getTableViewStats(TableInfo table) throws DisallowedException,
			SQLException, CodingErrorException, CantDoThatException;

	/**
	 * Get report view stats including a list of users that have been viewing a
	 * report along with some overview stats about each
	 */
	public ReportViewStatsInfo getReportViewStats(BaseReportInfo report)
			throws DisallowedException, SQLException, CodingErrorException, CantDoThatException;

	/**
	 * Return any tables that have had no report views (from any child reports)
	 */
	public SortedSet<TableInfo> getUnusedTables() throws DisallowedException, SQLException,
			ObjectNotFoundException;

}
