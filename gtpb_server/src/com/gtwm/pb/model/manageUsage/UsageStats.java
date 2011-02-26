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
package com.gtwm.pb.model.manageUsage;

import com.gtwm.pb.model.interfaces.ModuleUsageStatsInfo;
import com.gtwm.pb.model.interfaces.ReportViewStatsInfo;
import com.gtwm.pb.model.interfaces.UsageStatsInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.CompanyInfo;
import com.gtwm.pb.model.interfaces.DatabaseInfo;
import com.gtwm.pb.model.interfaces.ModuleInfo;
import com.gtwm.pb.model.interfaces.AuthManagerInfo;
import com.gtwm.pb.model.interfaces.AppUserInfo;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.UsageStatsTreeMapNodeInfo;
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
import java.util.Date;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONException;
import org.json.JSONStringer;
import org.grlea.log.SimpleLogger;

public class UsageStats implements UsageStatsInfo {

	private UsageStats() {
		this.request = null;
		this.databaseDefn = null;
	}

	public UsageStats(HttpServletRequest request, DatabaseInfo databaseDefn)
			throws DisallowedException, ObjectNotFoundException {
		this.request = request;
		this.databaseDefn = databaseDefn;
	}

	/**
	 * Set monthlyTableCost and numTables variables
	 */
	private void setCostDetails() throws ObjectNotFoundException, DisallowedException {
		AuthManagerInfo authManager = databaseDefn.getAuthManager();
		if (authManager.getAuthenticator().loggedInUserAllowedTo(this.request,
				PrivilegeType.ADMINISTRATE)) {
			CompanyInfo company = authManager.getCompanyForLoggedInUser(this.request);
			Set<TableInfo> companyTables = company.getTables();
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
	}

	public synchronized int getMonthlyTableCost() throws ObjectNotFoundException, DisallowedException {
		if (this.numTables == 0) {
			this.setCostDetails();
		}
		return this.monthlyTableCost;
	}

	public synchronized int getNumberOfTables() throws ObjectNotFoundException, DisallowedException {
		if (this.numTables == 0) {
			this.setCostDetails();
		}
		return this.numTables;
	}

	public String getTreeMapJSON() throws ObjectNotFoundException, DisallowedException,
			SQLException, JSONException, CodingErrorException {
		Map<String, Map<ModuleInfo, Set<UsageStatsTreeMapNodeInfo>>> sectionTreeMaps = new HashMap<String, Map<ModuleInfo, Set<UsageStatsTreeMapNodeInfo>>>();
		Map<ModuleInfo, Integer> moduleAreas = new HashMap<ModuleInfo, Integer>();
		Map<String, Integer> sectionAreas = new HashMap<String, Integer>();
		int totalArea = 0;
		AuthManagerInfo authManager = this.databaseDefn.getAuthManager();
		CompanyInfo company = authManager.getCompanyForLoggedInUser(this.request);
		Set<TableInfo> companyTables = company.getTables();
		String SQLCode = "SELECT report, average_count, percentage_increase FROM dbint_report_view_stats_materialized";
		SQLCode += " WHERE company=?";
		Connection conn = null;
		try {
			conn = databaseDefn.getDataSource().getConnection();
			conn.setAutoCommit(false);
			PreparedStatement statement = conn.prepareStatement(SQLCode);
			statement.setString(1, company.getCompanyName());
			ResultSet results = statement.executeQuery();
			Map<String, BaseReportInfo> reportMap = new HashMap<String, BaseReportInfo>();
			RESULTLOOP: while (results.next()) {
				String internalReportName = results.getString(1);
				BaseReportInfo report = reportMap.get(internalReportName);
				if (report == null) {
					REPORTSEARCH: for (TableInfo table : companyTables) {
						for (BaseReportInfo testReport : table.getReports()) {
							if (!testReport.equals(table.getDefaultReport())) {
								if (internalReportName.equals(testReport.getInternalReportName())) {
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
				if (report.getReportName().contains("dbvcalc_")
						|| report.getReportName().contains("dbvcrit_")) {
					continue RESULTLOOP;
				}
				ModuleInfo module = report.getModule();
				if (module == null) {
					continue RESULTLOOP;
				}
				int averageCount = results.getInt(2);
				totalArea += averageCount;
				int percentageIncrease = results.getInt(3);
				// Hard code the max and min for now.
				// TODO: These could become parameters
				if (results.wasNull()) {
					percentageIncrease = -750;
				} else if (percentageIncrease > 750) {
					percentageIncrease = 750;
				} else if (percentageIncrease < -750) {
					percentageIncrease = -750;
				}
				String section = module.getSection();
				if (section == null) {
					section = "";
				}
				Map<ModuleInfo, Set<UsageStatsTreeMapNodeInfo>> sectionTreeMap = sectionTreeMaps
						.get(section);
				if (sectionTreeMap == null) {
					sectionTreeMap = new HashMap<ModuleInfo, Set<UsageStatsTreeMapNodeInfo>>();
				}
				Set<UsageStatsTreeMapNodeInfo> treeMapNodes = sectionTreeMap.get(module);
				if (treeMapNodes == null) {
					treeMapNodes = new HashSet<UsageStatsTreeMapNodeInfo>();
				}
				UsageStatsTreeMapNodeInfo treeMapNode = new UsageStatsTreeMapNode(report,
						averageCount, percentageIncrease);
				treeMapNodes.add(treeMapNode);
				sectionTreeMap.put(module, treeMapNodes);
				sectionTreeMaps.put(section, sectionTreeMap);
				// keep total areas up to date
				Integer moduleArea = moduleAreas.get(module);
				if (moduleArea == null) {
					moduleArea = averageCount;
				} else {
					moduleArea += averageCount;
				}
				moduleAreas.put(module, moduleArea);
				Integer sectionArea = sectionAreas.get(section);
				if (sectionArea == null) {
					sectionArea = averageCount;
				} else {
					sectionArea += averageCount;
				}
				sectionAreas.put(section, sectionArea);
			}
			results.close();
			statement.close();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		if (sectionTreeMaps.size() == 0) {
			return "Not enough data has been gathered to display a map of utilisation. Please try again tomorrow";
		}
		// transfer the data into JSON
		JSONStringer js = new JSONStringer();
		BaseReportInfo report = null;
		js.object();
		js.key("id").value("root");
		js.key("name").value("Treemap breakdown of report views");
		js.key("data").object().key("$area").value(totalArea).endObject();
		js.key("children").array();
		// loop through sections
		for (Map.Entry<String, Map<ModuleInfo, Set<UsageStatsTreeMapNodeInfo>>> sectionTreeMapsEntry : sectionTreeMaps
				.entrySet()) {
			String section = sectionTreeMapsEntry.getKey();
			js.object(); // start section object
			js.key("id").value("s_" + section);
			js.key("name").value("<b>" + section.toUpperCase() + "</b>");
			js.key("data").object().key("$area").value(sectionAreas.get(section)).endObject();
			js.key("children").array();
			// loop through modules
			Map<ModuleInfo, Set<UsageStatsTreeMapNodeInfo>> sectionTreeMap = sectionTreeMapsEntry
					.getValue();
			for (Map.Entry<ModuleInfo, Set<UsageStatsTreeMapNodeInfo>> treeMapEntry : sectionTreeMap
					.entrySet()) {
				ModuleInfo module = treeMapEntry.getKey();
				Set<UsageStatsTreeMapNodeInfo> leaves = treeMapEntry.getValue();
				js.object(); // start module object
				js.key("id").value("m_" + module.getInternalModuleName());
				js.key("name").value(module.getModuleName().toLowerCase());
				js.key("data").object().key("$area").value(moduleAreas.get(module)).endObject();
				js.key("children").array();
				for (UsageStatsTreeMapNodeInfo leaf : leaves) {
					js.object(); // start report object
					report = leaf.getReport();
					if (authManager.getAuthenticator().loggedInUserAllowedToViewReport(
							this.request, report)) {
						js.key("id").value("r_" + report.getInternalReportName());
					} else {
						js.key("id").value("nopriv_r_" + report.getInternalReportName());
					}
					js.key("name").value(report.getReportName().toLowerCase());
					js.key("data");
					js.object();
					js.key("$area").value(leaf.getArea());
					js.key("$color").value(leaf.getColour());
					js.endObject(); // end data object
					js.key("children").array().endArray(); // no children, empty
					// array still
					// necessary
					js.endObject(); // end report
				}
				js.endArray(); // end children
				js.endObject(); // end module
			}
			js.endArray(); // end children of section
			js.endObject(); // end section object
		}
		js.endArray(); // end children of root
		js.endObject(); // end root
		String JSONString = js.toString();
		return JSONString;
	}

	public SortedSet<ModuleUsageStatsInfo> getModuleStats() throws DisallowedException,
			ObjectNotFoundException, SQLException {
		SortedSet<ModuleUsageStatsInfo> moduleStats = new TreeSet<ModuleUsageStatsInfo>();
		AuthManagerInfo authManager = this.databaseDefn.getAuthManager();
		CompanyInfo company = authManager.getCompanyForLoggedInUser(this.request);
		Set<TableInfo> companyTables = company.getTables();
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
					appUser = authManager.getUserByUserName(this.request, userName);
				} catch (ObjectNotFoundException onex) {
					continue RESULTLOOP;
				}
				BaseReportInfo report = reportMap.get(internalReportName);
				if (report == null) {
					REPORTSEARCH: for (TableInfo table : companyTables) {
						for (BaseReportInfo testReport : table.getReports()) {
							if (!testReport.equals(table.getDefaultReport())) {
								if (internalReportName.equals(testReport.getInternalReportName())) {
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
				if (report.getReportName().contains("dbvcalc_")
						|| report.getReportName().contains("dbvcrit_")) {
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
		for (ModuleUsageStatsInfo moduleUsageStats : moduleStatsMap.values()) {
			moduleStats.add(moduleUsageStats);
		}
		return moduleStats;
	}

	public List<List<String>> getRawStats(String logType) throws DisallowedException, SQLException,
			ObjectNotFoundException, CantDoThatException {
		return this.getRawStats(LogType.valueOf(logType.toUpperCase()));
	}

	public SortedSet<TableInfo> getUnusedTables() throws DisallowedException, SQLException,
			ObjectNotFoundException {
		AuthManagerInfo authManager = this.databaseDefn.getAuthManager();
		if (!authManager.getAuthenticator().loggedInUserAllowedTo(this.request,
				PrivilegeType.ADMINISTRATE)) {
			throw new DisallowedException(PrivilegeType.ADMINISTRATE);
		}
		CompanyInfo company = authManager.getCompanyForLoggedInUser(this.request);
		Set<TableInfo> companyTables = company.getTables();
		SortedSet<TableInfo> unusedTables = new TreeSet<TableInfo>();
		Set<TableInfo> usedTables = new HashSet<TableInfo>();
		String SQLCode = "SELECT DISTINCT report FROM dbint_log_"
				+ LogType.REPORT_VIEW.name().toLowerCase() + " WHERE company=? AND app_timestamp > now() - '6 months'::interval";
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

	public ReportViewStatsInfo getReportViewStats(BaseReportInfo report)
			throws DisallowedException, SQLException, CodingErrorException, CantDoThatException, ObjectNotFoundException {
		AuthManagerInfo authManager = this.databaseDefn.getAuthManager();
		if (!(authManager.getAuthenticator().loggedInUserAllowedTo(this.request,
				PrivilegeType.MANAGE_TABLE, report.getParentTable()) || authManager
				.getAuthenticator().loggedInUserAllowedTo(this.request, PrivilegeType.ADMINISTRATE))) {
			throw new DisallowedException(PrivilegeType.MANAGE_TABLE, report.getParentTable());
		}
		SortedSet<UserReportViewStatsInfo> userInfos = new TreeSet<UserReportViewStatsInfo>();
		int averageViews = 0;
		int percentageIncrease = 0;
		String SQLCode = "SELECT app_user, max(app_timestamp), count(*)";
		SQLCode += " FROM dbint_log_" + LogType.REPORT_VIEW.name().toLowerCase();
		SQLCode += " WHERE report=? AND app_timestamp > now() - '6 months'::interval";
		SQLCode += " GROUP BY app_user";
		SQLCode += " ORDER BY max(app_timestamp) DESC";
		Connection conn = null;
		try {
			conn = this.databaseDefn.getDataSource().getConnection();
			conn.setAutoCommit(false);
			PreparedStatement statement = conn.prepareStatement(SQLCode);
			statement.setString(1, report.getInternalReportName());
			ResultSet results = statement.executeQuery();
			AppUserInfo user = null;
			RESULTSLOOP: while (results.next()) {
				String username = results.getString(1);
				try {
					user = this.databaseDefn.getAuthManager().getUserByUserName(this.request,
							username);
				} catch (ObjectNotFoundException onex) {
					// user no longer exists
					continue RESULTSLOOP;
				}
				Date time = results.getDate(2);
				int viewCount = results.getInt(3);
				UserReportViewStatsInfo userInfo = new UserReportViewStats(user, viewCount, time);
				userInfos.add(userInfo);
			}
			results.close();
			statement.close();
			SQLCode = "SELECT average_count, percentage_increase FROM dbint_report_view_stats_materialized";
			SQLCode += " WHERE report=?";
			statement = conn.prepareStatement(SQLCode);
			statement.setString(1, report.getInternalReportName());
			results = statement.executeQuery();
			if (results.next()) {
				averageViews = results.getInt(1);
				percentageIncrease = results.getInt(2);
			}
			results.close();
			statement.close();
		} catch (SQLException sqlex) {
			// Could be because there's no log table - fail gracefully
			logger.warn("Error reading from log with SQL " + SQLCode);
			logger.warn("SQL error: " + sqlex);
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		ReportViewStatsInfo reportViewStats = new ReportViewStats(averageViews, percentageIncrease,
				userInfos);
		return reportViewStats;
	}

	public List<List<String>> getTableViewStats(TableInfo table) throws DisallowedException,
			SQLException, CodingErrorException, CantDoThatException, ObjectNotFoundException {
		AuthManagerInfo authManager = this.databaseDefn.getAuthManager();
		if (!(authManager.getAuthenticator().loggedInUserAllowedTo(this.request,
				PrivilegeType.MANAGE_TABLE, table) || authManager.getAuthenticator()
				.loggedInUserAllowedTo(this.request, PrivilegeType.ADMINISTRATE))) {
			throw new DisallowedException(PrivilegeType.MANAGE_TABLE, table);
		}
		List<List<String>> reportInfos = new LinkedList<List<String>>();
		Map<TableInfo, Set<BaseReportInfo>> viewableDataStores = this.databaseDefn
				.getViewableDataStores(this.request);
		Set<BaseReportInfo> reportsReferencingTable = new TreeSet<BaseReportInfo>();
		// Find which reports fields from the table are included in
		// Not 100% exhaustive because it doesn't check for field use in
		// calculations, filters etc.
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
		// Find the last access time and access count for each user who looked
		// at each report
		String SQLCode = "SELECT max(app_timestamp), app_user, count(*)";
		SQLCode += " FROM dbint_log_" + LogType.REPORT_VIEW.name().toLowerCase();
		SQLCode += " WHERE report=? AND app_timestamp > now() - '6 months'::interval";
		SQLCode += " GROUP BY app_user ";
		SQLCode += " ORDER BY max(app_timestamp) DESC";
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
				String reportName = "" + reportReferencingTable.getModule() + " - "
						+ reportReferencingTable;
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
			throws DisallowedException, SQLException, CantDoThatException, ObjectNotFoundException {
		AuthManagerInfo authManager = this.databaseDefn.getAuthManager();
		if (!(authManager.getAuthenticator().loggedInUserAllowedTo(this.request,
				PrivilegeType.MANAGE_TABLE, table) || authManager.getAuthenticator()
				.loggedInUserAllowedTo(this.request, PrivilegeType.ADMINISTRATE))) {
			throw new DisallowedException(PrivilegeType.MANAGE_TABLE, table);
		}
		if (!(logType.equals(LogType.DATA_CHANGE) || logType.equals(LogType.TABLE_SCHEMA_CHANGE))) {
			throw new CantDoThatException(
					"logType must by DATA_CHANGE or TABLE_SCHEMA_CHANGE when getting table stats");
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
		if (!authManager.getAuthenticator().loggedInUserAllowedTo(this.request,
				PrivilegeType.ADMINISTRATE)) {
			throw new DisallowedException(PrivilegeType.ADMINISTRATE);
		}
		CompanyInfo company = authManager.getCompanyForLoggedInUser(this.request);
		Set<TableInfo> companyTables = company.getTables();
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
		SQLCode += " WHERE company=? ORDER BY app_timestamp desc LIMIT 10000";
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
						TableInfo table = this.databaseDefn.getTable(this.request,
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
						TableInfo table = this.databaseDefn.getTable(this.request,
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

	public List<Integer> getTimelineCounts(String logType, int options) throws DisallowedException,
			SQLException, ObjectNotFoundException {
		return this.getTimelineCounts(LogType.valueOf(logType.toUpperCase()), options);
	}

	private List<Integer> getTimelineCounts(LogType logType, int options)
			throws DisallowedException, SQLException, ObjectNotFoundException {
		AuthManagerInfo authManager = this.databaseDefn.getAuthManager();
		if (!authManager.getAuthenticator().loggedInUserAllowedTo(this.request,
				PrivilegeType.ADMINISTRATE)) {
			throw new DisallowedException(PrivilegeType.ADMINISTRATE);
		}
		CompanyInfo company = authManager.getCompanyForLoggedInUser(this.request);
		// Check cache
		List<Integer> timelineCounts = new LinkedList<Integer>();
		timelineCounts = company.getCachedSparkline(logType, options);
		if (timelineCounts.size() > 0) {
			return timelineCounts;
		}
		// Count entries per week, filling in weeks where there are no entries with 0
		String SQLCode = "SELECT coalesce(log_records.count, 0) as count";
		SQLCode += " FROM";
		SQLCode += "  (SELECT date_trunc('week',app_timestamp) as week, count(*) as count";
		SQLCode += "   FROM " + "dbint_log_" + logType.name().toLowerCase();
		SQLCode += "   WHERE company=? AND app_timestamp > (now() - '52 weeks'::interval)";
		if (options == 1) {
			SQLCode += "   AND NOT (app_action like 'remove%')";
		} else if (options == -1) {
			SQLCode += "   AND app_action like 'remove%'";
		}
		SQLCode += "   GROUP BY week";
		SQLCode += "  ) AS log_records";
		SQLCode += "  RIGHT OUTER JOIN (SELECT date_trunc('week',now()) - generate_series(0,52) * '1 week'::interval) as weeks(week)";
		SQLCode += "  ON log_records.week = weeks.week";
		SQLCode += " ORDER BY weeks.week";
		Connection conn = null;
		try {
			conn = this.databaseDefn.getDataSource().getConnection();
			conn.setAutoCommit(false);
			PreparedStatement statement = conn.prepareStatement(SQLCode);
			statement.setString(1, company.getCompanyName());
			ResultSet results = statement.executeQuery();
			while(results.next()) {
				timelineCounts.add(results.getInt(1));
			}
			results.close();
			statement.close();
			// Remove the last entry, it will be only part of a week
			timelineCounts.remove(timelineCounts.size() - 1);
		} finally {
			if (conn != null) {
				conn.close();
			}			
		}
		company.setCachedSparkline(logType, options, timelineCounts);
		return timelineCounts;
	}
	
	public String getLastLoginAge(AppUserInfo user) throws SQLException {
		String lastLoginAge = null;
		String SQLCode = "SELECT age(max(app_timestamp)::date) FROM dbint_log_" + LogType.LOGIN.name().toLowerCase();
		SQLCode += " WHERE app_user = ?";
		Connection conn = null;
		try {
			conn = this.databaseDefn.getDataSource().getConnection();
			conn.setAutoCommit(false);
			PreparedStatement statement = conn.prepareStatement(SQLCode);
			statement.setString(1, user.getUserName());
			ResultSet results = statement.executeQuery();
			while(results.next()) {
				lastLoginAge = results.getString(1);
			}
			results.close();
			statement.close();
			if (lastLoginAge == null) {
				SQLCode = "select age(min(app_timestamp)::date) from dbint_log_" + LogType.LOGIN.name().toLowerCase();
				statement = conn.prepareStatement(SQLCode);
				results = statement.executeQuery();
				while(results.next()) {
					lastLoginAge = "more than " + results.getString(1);
				}
				results.close();
				statement.close();
			}
		} finally {
			if (conn != null) {
				conn.close();
			}			
		}
		if (lastLoginAge.equals("00:00:00")) {
			lastLoginAge = "today";
		} else {
			lastLoginAge += " ago";
		}
		return lastLoginAge;
	}

	public String toString() {
		return "Usage Stats object";
	}

	private int monthlyTableCost = 0;

	private int numTables = 0;

	private final DatabaseInfo databaseDefn;

	private final HttpServletRequest request;

	private static final SimpleLogger logger = new SimpleLogger(UsageStats.class);
}
