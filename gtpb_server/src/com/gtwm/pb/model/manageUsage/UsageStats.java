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
package com.gtwm.pb.model.manageUsage;

import com.gtwm.pb.model.interfaces.ModuleUsageStatsInfo;
import com.gtwm.pb.model.interfaces.UsageStatsInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.CompanyInfo;
import com.gtwm.pb.model.interfaces.DatabaseInfo;
import com.gtwm.pb.model.interfaces.ModuleInfo;
import com.gtwm.pb.model.interfaces.AuthManagerInfo;
import com.gtwm.pb.model.interfaces.AppUserInfo;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.UserReportViewStatsInfo;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.util.AppProperties;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.Helpers;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.auth.PrivilegeType;
import com.gtwm.pb.auth.DisallowedException;
import com.gtwm.pb.model.manageUsage.UsageLogger.LogType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;

import org.grlea.log.SimpleLogger;

public class UsageStats implements UsageStatsInfo {

	private UsageStats() {
	}

	/**
	 * Creates a blank object with only pricing information calculated, use the
	 * factory method to get one populated with stats
	 * 
	 * @param companyTables
	 *            All the tables in the company, if you want company cost to be
	 *            calculated, otherwise an empty map can be passed in
	 */
	public UsageStats(Collection<TableInfo> companyTables, HttpServletRequest request,
			DatabaseInfo databaseDefn) {
		this.request = request;
		this.databaseDefn = databaseDefn;
		int numTables = companyTables.size();
		this.numTables = numTables;
		if (numTables > AppProperties.numFullPriceTables) {
			int numDiscountTables = this.numTables - AppProperties.numFullPriceTables;
			this.monthlyTableCost = (AppProperties.numFullPriceTables * AppProperties.tableCost)
					+ (numDiscountTables * AppProperties.discountTableCost);
		} else {
			this.monthlyTableCost = numTables * AppProperties.tableCost;
		}
	}

	/**
	 * Creates a blank UsageStats object with nothing calculated
	 */
	public UsageStats(HttpServletRequest request, DatabaseInfo databaseDefn) {
		this.request = request;
		this.databaseDefn = databaseDefn;
	}

	/**
	 * Returns an instance of this object populated with summary data for the
	 * company of the logged in administrator
	 * 
	 * @param analyzeStats
	 *            Whether to populate the instance with comparative usage data,
	 *            i.e. comparing report usage with each other etc. If not, the
	 *            object can be used to get raw stats only
	 */
	public static UsageStatsInfo getInstance(HttpServletRequest request, DatabaseInfo databaseDefn,
			boolean analyzeStats) throws DisallowedException, ObjectNotFoundException, SQLException {
		UsageStatsInfo usageStats = null;
		if (analyzeStats) {
			AuthManagerInfo authManager = databaseDefn.getAuthManager();
			Set<TableInfo> companyTables = authManager.getCompanyTables(request);
			CompanyInfo company = authManager.getCompanyForLoggedInUser(request);
			Map<ModuleInfo, ModuleUsageStatsInfo> moduleStatsMap = new HashMap<ModuleInfo, ModuleUsageStatsInfo>();
			String SQLCode = "SELECT app_user, report, count(*) FROM dbint_log_";
			SQLCode += LogType.REPORT_VIEW.name().toLowerCase();
			SQLCode += " WHERE company=?";
			SQLCode += " AND app_timestamp > (now() - '3 months'::interval)";
			SQLCode += " GROUP BY app_user, report";
			Connection conn = null;
			try {
				conn = databaseDefn.getDataSource().getConnection();
				conn.setAutoCommit(false);
				PreparedStatement statement = conn.prepareStatement(SQLCode);
				statement.setString(1, company.getCompanyName());
				ResultSet results = statement.executeQuery();
				Map<String, BaseReportInfo> reportMap = new HashMap<String, BaseReportInfo>();
				RESULTLOOP: while (results.next()) {
					String userName = results.getString(1);
					String internalReportName = results.getString(2);
					int reportViews = results.getInt(3);
					AppUserInfo appUser = null;
					try {
						appUser = authManager.getUserByUserName(request, userName);
					} catch (ObjectNotFoundException onex) {
						continue RESULTLOOP;
					}
					BaseReportInfo report = reportMap.get(internalReportName);
					if (report == null) {
						REPORTSEARCH: for (TableInfo table : companyTables) {
							for (BaseReportInfo testReport : table.getReports()) {
								if (!testReport.equals(table.getDefaultReport())) {
									if (internalReportName.equals(testReport
											.getInternalReportName())) {
										report = testReport;
										reportMap.put(testReport.getInternalReportName(), report);
										break REPORTSEARCH;
									}
								}
							}
						}
					}
					if (report == null) {
						continue RESULTLOOP;
					}
					if (report.getReportName().startsWith("dbvcalc") || report.getReportName().startsWith("dbvcrit")) {
						continue RESULTLOOP;
					}
					ModuleInfo module = report.getModule();
					if (module == null) {
						continue RESULTLOOP;
					}
					ModuleUsageStatsInfo moduleUsageStats = moduleStatsMap.get(module);
					if (moduleUsageStats == null) {
						moduleUsageStats = new ModuleUsageStats(module);
					}
					UserReportViewStatsInfo userReportViewStats = new UserReportViewStats(appUser,
							reportViews);
					moduleUsageStats.addReportViewStats(report, userReportViewStats);
					moduleStatsMap.put(module, moduleUsageStats);
				}
				results.close();
				statement.close();
			} catch (SQLException sqlex) {
				logger.error(sqlex.toString());
				throw sqlex;
			} finally {
				if (conn != null) {
					conn.close();
				}
			}
			usageStats = new UsageStats(companyTables, request, databaseDefn);
			for (ModuleUsageStatsInfo moduleUsageStats : moduleStatsMap.values()) {
				usageStats.addModuleStats(moduleUsageStats);
			}
		} else {
			usageStats = new UsageStats(request, databaseDefn);
		}
		return usageStats;
	}

	public int getMonthlyTableCost() {
		return this.monthlyTableCost;
	}

	public int getNumberOfTables() {
		return this.numTables;
	}

	public void addModuleStats(ModuleUsageStatsInfo moduleStats) {
		this.moduleStats.add(moduleStats);
	}

	public SortedSet<ModuleUsageStatsInfo> getModuleStats() {
		return this.moduleStats;
	}

	public List<List<String>> getRawStats(String logType) throws DisallowedException, SQLException,
			ObjectNotFoundException, CantDoThatException {
		return this.getRawStats(LogType.valueOf(logType.toUpperCase()));
	}

	public SortedSet<TableInfo> getUnusedTables() throws DisallowedException, SQLException,
			ObjectNotFoundException {
		AuthManagerInfo authManager = this.databaseDefn.getAuthManager();
		if (!authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.ADMINISTRATE)) {
			throw new DisallowedException(PrivilegeType.ADMINISTRATE);
		}
		Set<TableInfo> companyTables = authManager.getCompanyTables(request);
		SortedSet<TableInfo> unusedTables = new TreeSet<TableInfo>();
		Set<TableInfo> usedTables = new HashSet<TableInfo>();
		String SQLCode = "SELECT DISTINCT report FROM dbint_log_"
				+ LogType.REPORT_VIEW.name().toLowerCase() + " WHERE company=?";
		CompanyInfo company = authManager.getCompanyForLoggedInUser(this.request);
		Connection conn = null;
		try {
			conn = this.databaseDefn.getDataSource().getConnection();
			conn.setAutoCommit(false);
			PreparedStatement statement = conn.prepareStatement(SQLCode);
			statement.setString(1, company.getCompanyName());
			ResultSet results = statement.executeQuery();
			TableInfo reportTable = null;
			while (results.next()) {
				String internalReportName = results.getString(1);
				REPORT_SEARCH: for (TableInfo table : companyTables) {
					for (BaseReportInfo report : table.getReports()) {
						if (internalReportName.equals(report.getInternalReportName())) {
							usedTables.add(table);
							break REPORT_SEARCH;
						}
					}
				}
			}
			results.close();
			statement.close();
		} catch (SQLException sqlex) {
			logger.error(sqlex.toString());
			throw sqlex;
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		for (TableInfo table : companyTables) {
			if (!usedTables.contains(table)) {
				unusedTables.add(table);
			}
		}
		return unusedTables;
	}

	public List<List<String>> getReportViewStats(BaseReportInfo report) throws DisallowedException, SQLException, CodingErrorException, CantDoThatException {
		AuthManagerInfo authManager = this.databaseDefn.getAuthManager();
		if (!authManager.getAuthenticator().loggedInUserAllowedTo(request, PrivilegeType.MANAGE_TABLE, report.getParentTable())) {
			throw new DisallowedException(PrivilegeType.MANAGE_TABLE, report.getParentTable());
		}
		List<List<String>> userInfos = new LinkedList<List<String>>();
		String SQLCode = "SELECT app_user, max(app_timestamp), count(*)";
		SQLCode += " FROM dbint_log_" + LogType.REPORT_VIEW.name().toLowerCase();
		SQLCode += " WHERE report=?";
		SQLCode += " GROUP BY app_user";
		SQLCode += " ORDER BY max(app_timestamp) DESC";
		Connection conn = null;
		try {
			conn = this.databaseDefn.getDataSource().getConnection();
			conn.setAutoCommit(false);
			PreparedStatement statement = conn.prepareStatement(SQLCode);
			statement.setString(1, report.getInternalReportName());
			ResultSet results = statement.executeQuery();
			Calendar calendar = Calendar.getInstance();
			String appTimestampFormat = Helpers.generateJavaDateFormat(Calendar.DAY_OF_MONTH);
			while (results.next()) {
				List<String> row = new LinkedList<String>();
				String username = results.getString(1);
				row.add(username);
				Timestamp time = results.getTimestamp(2);
				calendar.setTimeInMillis(time.getTime());
				String lastAccessedTime = String.format(appTimestampFormat, calendar);
				row.add(lastAccessedTime);
				String viewCount = results.getString(3);
				row.add(viewCount);
				userInfos.add(row);
			}
			results.close();
			statement.close();
		} catch(SQLException sqlex) {
			// Could be because there's no log table - fail gracefully
			logger.warn("Error reading from log with SQL " + SQLCode);
			logger.warn("SQL error: " + sqlex);
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		return userInfos;
	}

	public List<List<String>> getTableViewStats(TableInfo table) throws DisallowedException, SQLException, CodingErrorException, CantDoThatException {
		AuthManagerInfo authManager = this.databaseDefn.getAuthManager();
		if (!authManager.getAuthenticator().loggedInUserAllowedTo(request, PrivilegeType.MANAGE_TABLE, table)) {
			throw new DisallowedException(PrivilegeType.MANAGE_TABLE, table);
		}
		List<List<String>> reportInfos = new LinkedList<List<String>>();
		Map<TableInfo, Set<BaseReportInfo>> viewableDataStores = this.databaseDefn.getViewableDataStores(this.request);
		Set<BaseReportInfo> reportsReferencingTable = new TreeSet<BaseReportInfo>();
		// Find which reports fields from the table are included in
		// Not 100% exhaustive because it doesn't check for field use in calculations, filters etc.
		for (Map.Entry<TableInfo, Set<BaseReportInfo>> dataStore : viewableDataStores.entrySet()) {
			TableInfo viewableTable = dataStore.getKey();
			Set<BaseReportInfo> viewableReports = dataStore.getValue();
			REPORTSLOOP: for (BaseReportInfo viewableReport : viewableReports) {
				if (reportsReferencingTable.contains(viewableReport)) {
					continue REPORTSLOOP;
				}
				Set<BaseField> reportBaseFields = viewableReport.getReportBaseFields();
				for (BaseField tableField : table.getFields()) {
					if (reportBaseFields.contains(tableField)) {
						reportsReferencingTable.add(viewableReport);
						continue REPORTSLOOP;
					}
				}
			}
		}
		// Find the last access time and access count for each user who looked at each report
		String SQLCode = "SELECT max(app_timestamp), app_user, count(*) ";
		SQLCode += "FROM dbint_log_" + LogType.REPORT_VIEW.name().toLowerCase() + " ";
		SQLCode += "WHERE report=? ";
		SQLCode += "GROUP BY app_user ";
		SQLCode += "ORDER BY max(app_timestamp) DESC";
		Connection conn = null;
		try {
			conn = this.databaseDefn.getDataSource().getConnection();
			conn.setAutoCommit(false);
			PreparedStatement statement = conn.prepareStatement(SQLCode);
			for (BaseReportInfo reportReferencingTable : reportsReferencingTable) {
				statement.setString(1, reportReferencingTable.getInternalReportName());
				ResultSet results = statement.executeQuery();
				Calendar calendar = Calendar.getInstance();
				String appTimestampFormat = Helpers.generateJavaDateFormat(Calendar.DAY_OF_MONTH);
				String reportName = "" + reportReferencingTable.getModule() + " - " + reportReferencingTable;
				TableInfo parentTable = reportReferencingTable.getParentTable();
				if (reportReferencingTable.equals(parentTable.getDefaultReport())) {
					reportName = "Direct table access";
				}
				while (results.next()) {
					List<String> row = new LinkedList<String>();
					row.add(reportName);
					Timestamp time = results.getTimestamp(1);
					calendar.setTimeInMillis(time.getTime());
					String lastAccessedTime = String.format(appTimestampFormat, calendar);
					row.add(lastAccessedTime);
					String userName = results.getString(2);
					row.add(userName);
					String viewCount = results.getString(3);
					row.add(viewCount);
					reportInfos.add(row);
				}
				results.close();
			}
			statement.close();
		} catch (SQLException sqlex) {
			logger.warn("Error reading from log with SQL " + SQLCode);
			logger.warn("SQL error: " + sqlex);
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		return reportInfos;
	}
	
	public List<List<String>> getRawTableStats(LogType logType, TableInfo table, int rowLimit)
	throws DisallowedException, SQLException, CantDoThatException {
		AuthManagerInfo authManager = this.databaseDefn.getAuthManager();
		if (!authManager.getAuthenticator().loggedInUserAllowedTo(request, PrivilegeType.MANAGE_TABLE, table)) {
			throw new DisallowedException(PrivilegeType.MANAGE_TABLE, table);
		}
		if (!(logType.equals(LogType.DATA_CHANGE) || logType.equals(LogType.TABLE_SCHEMA_CHANGE))) {
			throw new CantDoThatException("logType must by DATA_CHANGE or TABLE_SCHEMA_CHANGE when getting table stats");
		}
		List<List<String>> rawStats = new LinkedList<List<String>>();
		// First row is the column headings
		List<String> columnHeadings = new LinkedList<String>();
		columnHeadings.add("Time");
		columnHeadings.add("User");
		String SQLCode = "SELECT app_timestamp, app_user, ";
		switch (logType) {
		case DATA_CHANGE:
			SQLCode += "app_action ";
			columnHeadings.add("Action");
			break;
		case TABLE_SCHEMA_CHANGE:
			SQLCode += "app_action, details ";
			columnHeadings.add("Action");
			columnHeadings.add("Details");
			break;
		}
		rawStats.add(columnHeadings);
		SQLCode += "FROM " + "dbint_log_" + logType.name().toLowerCase();
		SQLCode += " WHERE app_table=? ORDER BY app_timestamp desc LIMIT " + rowLimit;
		Connection conn = null;
		try {
			conn = this.databaseDefn.getDataSource().getConnection();
			conn.setAutoCommit(false);
			PreparedStatement statement = conn.prepareStatement(SQLCode);
			statement.setString(1, table.getInternalTableName());
			ResultSet results = statement.executeQuery();
			Calendar calendar = Calendar.getInstance();
			String appTimestampFormat = Helpers.generateJavaDateFormat(Calendar.MINUTE);
			while (results.next()) {
				List<String> row = new LinkedList<String>();
				Timestamp time = results.getTimestamp(1);
				calendar.setTimeInMillis(time.getTime());
				row.add(String.format(appTimestampFormat, calendar));
				String userName = results.getString(2);
				row.add(userName);
				switch (logType) {
				case DATA_CHANGE:
					String action = results.getString(3);
					row.add(action);
					break;
				case TABLE_SCHEMA_CHANGE:
					action = results.getString(3);
					row.add(action);
					String details = results.getString(4);
					row.add(details);
					break;
				}
				rawStats.add(row);
			}
			results.close();
			statement.close();
		} catch (SQLException sqlex) {
			logger.error(sqlex.toString());
			throw sqlex;
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		return rawStats;
	}
	
	private List<List<String>> getRawStats(LogType logType) throws DisallowedException,
			SQLException, ObjectNotFoundException, CantDoThatException {
		AuthManagerInfo authManager = this.databaseDefn.getAuthManager();
		if (!authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.ADMINISTRATE)) {
			throw new DisallowedException(PrivilegeType.ADMINISTRATE);
		}
		Set<TableInfo> companyTables = authManager.getCompanyTables(request);
		List<List<String>> rawStats = new LinkedList<List<String>>();
		// First row is the column headings
		List<String> columnHeadings = new LinkedList<String>();
		columnHeadings.add("Time");
		columnHeadings.add("User");
		String SQLCode = "SELECT app_timestamp, app_user, ";
		switch (logType) {
		case LOGIN:
			SQLCode += "ip_address ";
			columnHeadings.add("IP Address");
			break;
		case REPORT_VIEW:
			SQLCode += "report, details ";
			columnHeadings.add("Report");
			columnHeadings.add("Details");
			break;
		case DATA_CHANGE:
			SQLCode += "app_table, app_action, saved_data, row_id ";
			columnHeadings.add("Table");
			columnHeadings.add("Action");
			columnHeadings.add("Saved data");
			columnHeadings.add("Record Identifier");
			break;
		case REPORT_SCHEMA_CHANGE:
			SQLCode += "report, app_action, details ";
			columnHeadings.add("Report");
			columnHeadings.add("Action");
			columnHeadings.add("Details");
			break;
		case TABLE_SCHEMA_CHANGE:
			SQLCode += "app_table, app_action, details ";
			columnHeadings.add("Table");
			columnHeadings.add("Action");
			columnHeadings.add("Details");
			break;
		}
		rawStats.add(columnHeadings);
		SQLCode += "FROM " + "dbint_log_" + logType.name().toLowerCase();
		SQLCode += " WHERE company=? ORDER BY app_timestamp desc";
		CompanyInfo company = authManager.getCompanyForLoggedInUser(this.request);
		Connection conn = null;
		try {
			conn = this.databaseDefn.getDataSource().getConnection();
			conn.setAutoCommit(false);
			PreparedStatement statement = conn.prepareStatement(SQLCode);
			statement.setString(1, company.getCompanyName());
			ResultSet results = statement.executeQuery();
			Calendar calendar = Calendar.getInstance();
			String appTimestampFormat = Helpers.generateJavaDateFormat(Calendar.SECOND);
			while (results.next()) {
				List<String> row = new LinkedList<String>();
				Timestamp time = results.getTimestamp(1);
				calendar.setTimeInMillis(time.getTime());
				row.add(String.format(appTimestampFormat, calendar));
				String userName = results.getString(2);
				row.add(userName);
				switch (logType) {
				case LOGIN:
					String ipAddress = results.getString(3);
					row.add(ipAddress);
					break;
				case REPORT_VIEW:
					String internalReportName = results.getString(3);
					String reportName = "removed report " + internalReportName;
					REPORT_SEARCH: for (TableInfo table : companyTables) {
						for (BaseReportInfo report : table.getReports()) {
							if (internalReportName.equals(report.getInternalReportName())) {
								if (report.equals(table.getDefaultReport())) {
									reportName = "Table " + table.getTableName();
								} else {
									reportName = report.getReportName();
									ModuleInfo module = report.getModule();
									if (module != null) {
										reportName = module.getModuleName() + ": " + reportName;
									}
								}
								break REPORT_SEARCH;
							}
						}
					}
					String details = results.getString(4);
					row.add(reportName);
					row.add(details);
					break;
				case DATA_CHANGE:
					String internalTableName = results.getString(3);
					String tableName = "";
					try {
						TableInfo table = this.databaseDefn.getTableByInternalName(request,
								internalTableName);
						tableName = table.getTableName();
					} catch (ObjectNotFoundException onex) {
						tableName = "Removed table " + internalTableName;
					}
					String action = results.getString(4);
					String saved_data = results.getString(5);
					Integer rowId = results.getInt(6);
					row.add(tableName);
					row.add(action);
					row.add(saved_data);
					row.add(rowId.toString());
					break;
				case REPORT_SCHEMA_CHANGE:
					internalReportName = results.getString(3);
					reportName = "removed report " + internalReportName;
					REPORT_SEARCH: for (TableInfo table : companyTables) {
						for (BaseReportInfo report : table.getReports()) {
							if (internalReportName.equals(report.getInternalReportName())) {
								if (!report.equals(table.getDefaultReport())) {
									reportName = report.getReportName();
									ModuleInfo module = report.getModule();
									if (module != null) {
										reportName = module.getModuleName() + ": " + reportName;
									}
								}
								break REPORT_SEARCH;
							}
						}
					}
					action = results.getString(4);
					details = results.getString(5);
					row.add(reportName);
					row.add(action);
					row.add(details);
					break;
				case TABLE_SCHEMA_CHANGE:
					internalTableName = results.getString(3);
					tableName = "";
					try {
						TableInfo table = this.databaseDefn.getTableByInternalName(request,
								internalTableName);
						tableName = table.getTableName();
					} catch (ObjectNotFoundException onex) {
						tableName = "Removed table " + internalTableName;
					}
					action = results.getString(4);
					details = results.getString(5);
					row.add(tableName);
					row.add(action);
					row.add(details);
					break;
				}
				rawStats.add(row);
			}
			results.close();
			statement.close();
		} catch (SQLException sqlex) {
			logger.error(sqlex.toString());
			throw sqlex;
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		return rawStats;
	}

	public String toString() {
		return "Monthly table cost = " + this.monthlyTableCost + ", module stats = "
				+ this.moduleStats;
	}

	private int monthlyTableCost = 0;

	private int numTables = 0;

	private SortedSet<ModuleUsageStatsInfo> moduleStats = new TreeSet<ModuleUsageStatsInfo>();

	private DatabaseInfo databaseDefn = null;

	private HttpServletRequest request = null;

	private static final SimpleLogger logger = new SimpleLogger(UsageStats.class);

}
