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
package com.gtwm.pb.model.manageData;

import com.gtwm.pb.model.interfaces.CompanyInfo;
import com.gtwm.pb.model.interfaces.ModuleInfo;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.SessionDataInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.DatabaseInfo;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.fields.BaseValue;
import com.gtwm.pb.model.interfaces.AppUserInfo;
import com.gtwm.pb.model.interfaces.AppRoleInfo;
import com.gtwm.pb.model.interfaces.AuthenticatorInfo;
import com.gtwm.pb.model.manageUsage.UsageLogger;
import com.gtwm.pb.util.TableDependencyException;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.auth.DisallowedException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.Enumerations.SessionContext;
import com.gtwm.pb.util.Enumerations.AppAction;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import org.grlea.log.SimpleLogger;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Note: the setter methods in this class don't need to be synchronized because this is session data 
// - there is one object per user so as long as one user doesn't make simultaneous requests, we're ok
// On second thoughts, synchronize them anyway just in case
public class SessionData implements SessionDataInfo {

	public SessionData() {
		logger.warn("Empty SessionData object created");
	}

	/**
	 * Construct a new session data object.
	 * 
	 * Initialisation: sets the session report to the first one that the logged
	 * in user can see
	 * 
	 * @param request
	 *            Has temporary use in the constructor for privilege checks, not
	 *            stored for the life of SessionData
	 * @param databaseDefn
	 *            Has temporary use in the constructor for privilege checks, not
	 *            stored for the life of SessionData
	 */
	public SessionData(DatabaseInfo databaseDefn, DataSource relationalDataSource,
			HttpServletRequest request) throws SQLException, DisallowedException,
			ObjectNotFoundException, CodingErrorException {
		// Sessions are created when someone logs in
		AppUserInfo user = databaseDefn.getAuthManager().getUserByUserName(request,
				request.getRemoteUser());
		UsageLogger usageLogger = new UsageLogger(relationalDataSource);
		usageLogger.logLogin(user, request.getRemoteAddr());
		UsageLogger.startLoggingThread(usageLogger);
		this.relationalDataSource = relationalDataSource;
		// Set the session report to the first that the logged in user can view
		AuthenticatorInfo authenticator = databaseDefn.getAuthManager().getAuthenticator();
		CompanyInfo company = databaseDefn.getAuthManager().getCompanyForLoggedInUser(request);
		for (ModuleInfo module : company.getModules()) {
			for (TableInfo table : company.getTables()) {
				for (BaseReportInfo report : table.getReports()) {
					ModuleInfo reportModule = report.getModule();
					if (module.equals(reportModule)) {
						if (authenticator.loggedInUserAllowedToViewReport(request, report)) {
							this.setReport(report);
							return;
						}
					}
				}
			}
		}
		logger.warn("No viewable report found to show for user " + user + " in company " + company);
	}

	public synchronized void setTable(TableInfo table) throws SQLException {
		// clear the cached record data:
		this.setFieldInputValues(new HashMap<BaseField, BaseValue>());
		this.table = table;
		if (table == null) {
			return;
		}
		// Also set the current report to the default report (if no current
		// report exists yet)
		if (this.currentTableReports.get(table) == null) {
			this.currentTableReports.put(table, table.getDefaultReport());
		}
		// If no row ID exists, set the record to that identified by the first
		// line of the current report
		this.autoSetRowId();
	}

	public synchronized void setReport(BaseReportInfo report) throws SQLException {
		// clear the cached record data:
		this.setFieldInputValues(new HashMap<BaseField, BaseValue>());
		if (!(report.equals(this.currentTableReports.get(this.table)))) {
			this.table = report.getParentTable();
			this.currentTableReports.put(report.getParentTable(), report);
			// If no row ID exists, set the record to that identified by the
			// first line of the current report
			this.autoSetRowId();
		}
	}

	/**
	 * If there is no row ID for the current table, set one based on the first
	 * record from the current report
	 * 
	 * @throws SQLException
	 */
	private void autoSetRowId() throws SQLException {
		if (this.currentTableRowIds.get(this.table) == null) {
			String SQLCode = "SELECT " + this.table.getPrimaryKey().getInternalFieldName()
					+ " FROM " + getReport().getInternalReportName() + " LIMIT 1";
			Connection conn = null;
			try {
				conn = this.relationalDataSource.getConnection();
				conn.setAutoCommit(false);
				PreparedStatement statement = conn.prepareStatement(SQLCode);
				ResultSet resultSet = statement.executeQuery();
				// If resultSet.next() is false, there is no data in the report
				if (resultSet.next()) {
					int rowId = resultSet.getInt(1);
					this.setRowId(rowId);
				}
				resultSet.close();
				statement.close();
			} catch (SQLException sqlex) {
				logger.warn("SQL error auto setting row ID. The session report may have an error. "
						+ sqlex);
			} finally {
				if (conn != null) {
					conn.close();
				}
			}
		}
	}

	public void setRowId(int rowId) {
		this.setRowId(this.table, rowId);
	}

	public synchronized void setRowId(TableInfo table, int rowId) {
		if (rowId != this.getRowId(table)) {
			// A different record is being selected.
			// Clear the cached record data
			this.setFieldInputValues(new HashMap<BaseField, BaseValue>());
		}
		if (rowId == -1) {
			this.currentTableRowIds.remove(table);
		} else {
			this.currentTableRowIds.put(table, rowId);
		}
	}

	public synchronized TableInfo getTable() {
		return this.table;
	}

	public synchronized BaseReportInfo getReport() {
		return this.currentTableReports.get(this.table);
	}

	public synchronized int getRowId() {
		return getRowId(this.table);
	}

	public synchronized int getRowId(TableInfo table) {
		Integer rowId = this.currentTableRowIds.get(table);
		if (rowId == null) {
			return -1;
		} else {
			return rowId;
		}
	}

	public int getReportRowLimit() {
		return this.reportRowLimit;
	}

	public boolean hasTable() {
		if (this.getTable() == null) {
			return false;
		} else {
			return true;
		}
	}

	public void setReportRowLimit(int reportRowLimit) {
		this.reportRowLimit = reportRowLimit;
	}

	public void setUser(AppUserInfo appUser) {
		this.appUser = appUser;
	}

	public void setRole(AppRoleInfo role) {
		this.role = role;
	}

	public void setContext(SessionContext sessionContext) {
		this.sessionContext = sessionContext;
	}

	public synchronized void setReportFilterValue(BaseField field, String fieldValue) {
		if (fieldValue != null) {
			if (!(fieldValue.equals(""))) {
				this.reportFilterValues.put(field, fieldValue);
				return;
			}
		}
		// if fieldValue empty, clear the field filter
		this.clearReportFilterValue(field);
	}

	public synchronized void setCustomReportFilterValue(String filterSet, BaseField field,
			String fieldValue) {
		Map<BaseField, String> customFilterValues = this.customReportFilterValues.get(filterSet);
		if (customFilterValues == null) {
			customFilterValues = new HashMap<BaseField, String>();
		}
		if (fieldValue != null) {
			if (!(fieldValue.equals(""))) {
				customFilterValues.put(field, fieldValue);
				this.customReportFilterValues.put(filterSet, customFilterValues);
				return;
			}
		}
		this.clearCustomReportFilterValue(filterSet, field);
	}

	public synchronized void setReportSort(BaseField reportField, Boolean sortAscending) {
		this.reportSorts.put(reportField, sortAscending);
	}

	public synchronized void clearAllReportFilterValues() {
		this.reportFilterValues.clear();
	}

	public synchronized void clearAllReportSorts() {
		this.reportSorts.clear();
	}

	public synchronized void clearReportFilterValue(BaseField field) {
		this.reportFilterValues.remove(field);
	}

	private synchronized void clearCustomReportFilterValue(String filterSet, BaseField field) {
		Map<BaseField, String> customFilterValues = this.customReportFilterValues.get(filterSet);
		if (customFilterValues != null) {
			customFilterValues.remove(field);
			this.customReportFilterValues.put(filterSet, customFilterValues);
		}
	}

	public synchronized void clearReportSort(BaseField field) {
		this.reportSorts.remove(field);
	}

	public synchronized Map<BaseField, String> getReportFilterValues() {
		return Collections.unmodifiableMap(new HashMap<BaseField, String>(this.reportFilterValues));
	}

	public synchronized Map<BaseField, String> getCustomReportFilterValues(String filterSet) {
		Map<BaseField, String> customFilterValues = this.customReportFilterValues.get(filterSet);
		if (customFilterValues == null) {
			return Collections.unmodifiableMap(new HashMap<BaseField, String>());
		} else {
			return Collections.unmodifiableMap(new HashMap<BaseField, String>(customFilterValues));
		}
	}

	public synchronized Map<BaseField, Boolean> getReportSorts() {
		return Collections.unmodifiableMap(new HashMap<BaseField, Boolean>(this.reportSorts));
	}

	public synchronized void setFieldInputValues(Map<BaseField, BaseValue> inputValues) {
		this.inputValues = inputValues;
	}

	public synchronized Map<BaseField, BaseValue> getFieldInputValues() {
		return Collections
				.unmodifiableMap(new LinkedHashMap<BaseField, BaseValue>(this.inputValues));
	}

	public AppUserInfo getUser() {
		return this.appUser;
	}

	public ModuleInfo getModule() {
		return this.module;
	}

	public void setModule(ModuleInfo module) {
		this.module = module;
	}

	public AppRoleInfo getRole() {
		return this.role;
	}

	public SessionContext getContext() {
		return this.sessionContext;
	}

	public TableDependencyException getTableDependencyException() {
		return this.tdex;
	}

	public void setTableDependencyException(TableDependencyException tdex) {
		this.tdex = tdex;
	}

	public synchronized void clearCustomVariable(String key) {
		this.customStrings.remove(key);
		this.customIntegers.remove(key);
		this.customBooleans.remove(key);
		this.customTables.remove(key);
		this.customReports.remove(key);
		this.customFields.remove(key);
	}

	public synchronized String getCustomVariable(String key) {
		return this.getCustomString(key);
	}

	public synchronized String getCustomString(String key) {
		return this.customStrings.get(key);
	}

	public synchronized Integer getCustomInteger(String key) {
		return this.customIntegers.get(key);
	}

	public synchronized Long getCustomLong(String key) {
		return this.customLongs.get(key);
	}

	public synchronized Boolean getCustomBoolean(String key) {
		return this.customBooleans.get(key);
	}

	public synchronized TableInfo getCustomTable(String key) {
		return this.customTables.get(key);
	}

	public synchronized BaseReportInfo getCustomReport(String key) {
		return this.customReports.get(key);
	}

	public synchronized BaseField getCustomField(String key) {
		return this.customFields.get(key);
	}

	public void setCustomVariable(String key, String value) {
		this.setCustomString(key, value);
	}

	public synchronized void setCustomString(String key, String value) {
		this.customStrings.put(key, value);
	}

	public synchronized void setCustomInteger(String key, Integer value) {
		this.customIntegers.put(key, value);
	}

	public synchronized void setCustomLong(String key, Long value) {
		this.customLongs.put(key, value);
	}

	public synchronized void setCustomBoolean(String key, Boolean value) {
		this.customBooleans.put(key, value);
	}

	public synchronized void setCustomTable(String key, TableInfo value) {
		this.customTables.put(key, value);
	}

	public synchronized void setCustomReport(String key, BaseReportInfo value) {
		this.customReports.put(key, value);
	}

	public synchronized void setCustomField(String key, BaseField value) {
		this.customFields.put(key, value);
	}

	/**
	 * If the current record (identified by session table and row id) is locked,
	 * override the lock. For security, must check that the logged in user has
	 * MANAGE privileges on the session table before calling this
	 */
	public void setRecordLockOverride() {
		this.lockOverrideTable = this.getTable();
		this.lockOverrideRowId = this.getRowId();
	}

	/**
	 * Remove any lock override
	 */
	protected void unsetRecordLockOverride() {
		this.lockOverrideTable = null;
		this.lockOverrideRowId = -1;
	}

	/**
	 * Should not be used directly by UI templates, rather use
	 * ViewMethods.isRecordLocked()
	 */
	protected int getRecordLockOverrideRowId() {
		return this.lockOverrideRowId;
	}

	/**
	 * Should not be used directly by UI templates, rather use
	 * ViewMethods.isRecordLocked()
	 */
	protected TableInfo getRecordLockOverrideTable() {
		return this.lockOverrideTable;
	}

	public AppAction getLastAppAction() {
		return this.lastAppAction;
	}

	public void setLastAppAction(AppAction appAction) {
		this.lastAppAction = appAction;
	}

	public int getLastAppActionRowId() {
		return this.lastAppActionRowId;
	}

	public void setLastAppActionRowId(int lastAppActionRowId) {
		this.lastAppActionRowId = lastAppActionRowId;
	}

	public String toString() {
		String sessionData = "";
		sessionData += "getUser() = " + this.getUser() + "\n";
		sessionData += "getRole() = " + this.getRole() + "\n";
		sessionData += "getTable() = " + this.getTable() + "\n";
		sessionData += "getReport() = " + this.getReport() + "\n";
		sessionData += "getRowId() = " + this.getRowId() + "\n";
		sessionData += "getReportRowLimit() = " + this.getReportRowLimit() + "\n";
		sessionData += "getReportFilterValues() = " + this.getReportFilterValues() + "\n";
		sessionData += "customTables = " + this.customTables + "\n";
		sessionData += "customReports = " + this.customReports + "\n";
		sessionData += "customFields = " + this.customFields + "\n";
		sessionData += "customStrings = " + this.customStrings + "\n";
		sessionData += "customIntegers = " + this.customIntegers + "\n";
		return sessionData;
	}

	private TableInfo table = null;

	/**
	 * Stores the last accessed report for each table so that when a table is
	 * selected, the last used report can be shown
	 */
	private Map<TableInfo, BaseReportInfo> currentTableReports = new HashMap<TableInfo, BaseReportInfo>();

	/**
	 * Stores the last accessed record identifier for each table
	 */
	private Map<TableInfo, Integer> currentTableRowIds = new HashMap<TableInfo, Integer>();

	/**
	 * Standard report filters
	 */
	private Map<BaseField, String> reportFilterValues = new HashMap<BaseField, String>();

	/**
	 * Custom report filters, the first string specifying a filter set
	 */
	private Map<String, Map<BaseField, String>> customReportFilterValues = new HashMap<String, Map<BaseField, String>>();

	private Map<BaseField, Boolean> reportSorts = new HashMap<BaseField, Boolean>();

	private Map<BaseField, BaseValue> inputValues = new HashMap<BaseField, BaseValue>();

	/**
	 * How many records will be shown in the UI when viewing a report
	 */
	private int reportRowLimit = 100;

	private AppUserInfo appUser = null;

	private AppRoleInfo role = null;

	private ModuleInfo module = null;

	/**
	 * Which (if any) record has it's lock overridden
	 */
	private int lockOverrideRowId = -1;

	private int lastAppActionRowId = -1;

	private AppAction lastAppAction = null;

	private TableInfo lockOverrideTable = null;

	private SessionContext sessionContext = SessionContext.BUSINESS;

	private Map<String, String> customStrings = new HashMap<String, String>();

	private Map<String, Integer> customIntegers = new HashMap<String, Integer>();

	private Map<String, Long> customLongs = new HashMap<String, Long>();

	private Map<String, Boolean> customBooleans = new HashMap<String, Boolean>();

	private Map<String, TableInfo> customTables = new HashMap<String, TableInfo>();

	private Map<String, BaseReportInfo> customReports = new HashMap<String, BaseReportInfo>();

	private Map<String, BaseField> customFields = new HashMap<String, BaseField>();

	private TableDependencyException tdex = null;

	private DataSource relationalDataSource = null;

	private static final SimpleLogger logger = new SimpleLogger(SessionData.class);
}
