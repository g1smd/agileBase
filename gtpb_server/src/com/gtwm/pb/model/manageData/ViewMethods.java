/*
 *  Copyright 2012 GT webMarque Ltd
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

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.sql.SQLException;
import com.gtwm.pb.auth.PrivilegeType;
import com.gtwm.pb.auth.DisallowedException;
import com.gtwm.pb.model.interfaces.CommentInfo;
import com.gtwm.pb.model.interfaces.CompanyInfo;
import com.gtwm.pb.model.interfaces.AppUserInfo;
import com.gtwm.pb.model.interfaces.AppRoleInfo;
import com.gtwm.pb.model.interfaces.JoinClauseInfo;
import com.gtwm.pb.model.interfaces.ReportDataInfo;
import com.gtwm.pb.model.interfaces.ReportFieldInfo;
import com.gtwm.pb.model.interfaces.ChartDataInfo;
import com.gtwm.pb.model.interfaces.ChartInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.ModuleActionInfo;
import com.gtwm.pb.model.interfaces.WordInfo;
import com.gtwm.pb.model.interfaces.UsageStatsInfo;
import com.gtwm.pb.model.interfaces.ViewMethodsInfo;
import com.gtwm.pb.model.interfaces.AuthManagerInfo;
import com.gtwm.pb.model.interfaces.AuthenticatorInfo;
import com.gtwm.pb.model.interfaces.SessionDataInfo;
import com.gtwm.pb.model.interfaces.DatabaseInfo;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.SimpleReportInfo;
import com.gtwm.pb.model.interfaces.DataRowInfo;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.fields.BaseValue;
import com.gtwm.pb.model.interfaces.fields.RelationField;
import com.gtwm.pb.model.interfaces.fields.TextField;
import com.gtwm.pb.model.interfaces.ModuleInfo;
import com.gtwm.pb.model.manageSchema.JoinType;
import com.gtwm.pb.model.manageSchema.JoinClause;
import com.gtwm.pb.model.manageSchema.ChartAggregateDefn;
import com.gtwm.pb.model.manageSchema.ChartDefn;
import com.gtwm.pb.model.manageSchema.FieldTypeDescriptor.FieldCategory;
import com.gtwm.pb.model.manageUsage.UsageStats;
import com.gtwm.pb.model.manageUsage.UsageLogger;
import com.gtwm.pb.model.manageData.ModuleAction;
import com.gtwm.pb.servlets.ServletUtilMethods;
import com.gtwm.pb.util.AppProperties;
import com.gtwm.pb.util.Enumerations.AggregateFunction;
import com.gtwm.pb.util.MissingParametersException;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.Enumerations.DatabaseFieldType;
import com.gtwm.pb.util.Enumerations.ExtraAction;
import com.gtwm.pb.util.Enumerations.QuickFilterType;
import com.gtwm.pb.util.Helpers;
import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLStreamException;
import org.codehaus.jackson.JsonGenerationException;
import org.grlea.log.SimpleLogger;
import java.util.TreeSet;

public final class ViewMethods implements ViewMethodsInfo {

	private ViewMethods() {
		this.request = null;
		this.databaseDefn = null;
		this.sessionData = null;
	}

	/**
	 * Create and initialise the viewMethods object that'll be used by templates
	 * to display the application
	 * 
	 * @param request
	 *            Allows methods to get session data, also used when we need to
	 *            know the current user
	 * @param databaseDefn
	 *            Window on the application data
	 * @throws ObjectNotFoundException
	 *             If session data can't be retrieved from the request
	 */
	public ViewMethods(HttpServletRequest request, DatabaseInfo databaseDefn)
			throws ObjectNotFoundException {
		this.request = request;
		// Check user's logged in
		if (request.isRequestedSessionIdValid()) {
			// Separate out session data so we don't have to use a long method
			// call chain all the time
			this.sessionData = (SessionDataInfo) request.getSession().getAttribute(
					"com.gtwm.pb.servlets.sessionData");
			if (this.sessionData == null) {
				throw new ObjectNotFoundException("Session data not retrieved");
			}
		} else {
			this.sessionData = null;
		}
		this.toolbarPluginName = request.getParameter(ExtraAction.INCLUDE_TOOLBAR_PLUGIN.toString()
				.toLowerCase(Locale.UK));
		this.databaseDefn = databaseDefn;
	}

	public String getUserProfileImage(String internalUserName) throws ObjectNotFoundException {
		// Don't throw an ObjectNotFoundException if the user's not found, we don't want the template to stop rendering
		AppUserInfo user = this.getAuthManager().getUserByInternalName(this.request, internalUserName, false);
		if (user == null) {
			return null;
		}
		return user.getProfilePhoto();
	}

	/**
	 * Return all report in a module that the logged in user can see
	 */
	private SortedSet<BaseReportInfo> getReportsInModule(ModuleInfo module)
			throws CodingErrorException, ObjectNotFoundException {
		SortedSet<BaseReportInfo> reportsInModule = new TreeSet<BaseReportInfo>();
		for (BaseReportInfo report : this.getAllViewableReports()) {
			ModuleInfo reportModule = report.getModule();
			if (reportModule != null) {
				if (reportModule.equals(module)) {
					reportsInModule.add(report);
				}
			}
		}
		return reportsInModule;
	}

	public Set<ModuleInfo> getDependentModules(ModuleInfo module) throws CodingErrorException,
			ObjectNotFoundException {
		Set<ModuleInfo> dependentModules = new HashSet<ModuleInfo>();
		Set<TableInfo> dependentTables = new HashSet<TableInfo>();
		Set<BaseReportInfo> moduleReports = this.getReportsInModule(module);
		// Exclude tables that the reports in the module are from
		Set<TableInfo> moduleTables = new HashSet<TableInfo>();
		for (BaseReportInfo report : moduleReports) {
			moduleTables.add(report.getParentTable());
		}
		for (BaseReportInfo report : moduleReports) {
			TableInfo table = report.getParentTable();
			if (this.loggedInUserAllowedTo(PrivilegeType.VIEW_TABLE_DATA.name(), table)) {
				Set<TableInfo> reportDependentTables = this.getDependentTables(table, false);
				reportDependentTables.removeAll(moduleTables);
				dependentTables.addAll(reportDependentTables);
			}
		}
		for (TableInfo table : dependentTables) {
			for (BaseReportInfo report : this.getViewableReports(table)) {
				if (report.getModule() != null) {
					dependentModules.add(report.getModule());
				}
			}
		}
		return dependentModules;
	}

	public UsageStatsInfo getUsageStats() throws DisallowedException, ObjectNotFoundException,
			SQLException {
		return new UsageStats(this.request, this.databaseDefn);
	}

	public boolean isRecordLocked() throws SQLException, ObjectNotFoundException {
		TableInfo table = this.sessionData.getTable();
		int rowId = this.sessionData.getRowId();
		return this.databaseDefn.getDataManagement().isRecordLocked(this.sessionData, table, rowId);
	}

	public boolean isRecordLocked(TableInfo table, int rowId) throws SQLException,
			ObjectNotFoundException {
		return this.databaseDefn.getDataManagement().isRecordLocked(this.sessionData, table, rowId);
	}

	public boolean isRowIdInReport() throws SQLException {
		BaseReportInfo report = this.sessionData.getReport();
		int rowId = this.sessionData.getRowId();
		if (rowId < 0 || report == null) {
			return false;
		}
		return this.databaseDefn.getDataManagement().isRowIdInReport(report, rowId);
	}

	public synchronized void addModuleAction(String internaModuleName, String actionName,
			String description, String attributes, String actionTemplate, String buttons,
			String callbackFunction) {
		List<ModuleActionInfo> moduleActions = null;
		if (this.moduleActions.containsKey(internaModuleName)) {
			moduleActions = this.moduleActions.get(internaModuleName);
		} else {
			moduleActions = new LinkedList<ModuleActionInfo>();
		}
		ModuleActionInfo moduleAction = new ModuleAction(actionName, description, attributes,
				actionTemplate, buttons, callbackFunction);
		moduleActions.add(moduleAction);
		this.moduleActions.put(internaModuleName, moduleActions);
	}

	public synchronized List<ModuleActionInfo> getModuleActions(String internalModuleName) {
		List<ModuleActionInfo> moduleActions = this.moduleActions.get(internalModuleName);
		if (moduleActions == null) {
			return new LinkedList<ModuleActionInfo>();
		}
		return moduleActions;
	}

	public boolean usesToolbarPlugin() {
		return (this.toolbarPluginName != null);
	}

	public String getToolbarPluginName() {
		return this.toolbarPluginName;
	}

	public SortedSet<CompanyInfo> getCompanies() throws DisallowedException,
			ObjectNotFoundException {
		return this.databaseDefn.getAuthManager().getCompanies(this.request);
	}

	public SortedSet<TableInfo> getTablesAllowedTo(String privilegeString)
			throws ObjectNotFoundException {
		PrivilegeType privilegeType = PrivilegeType.valueOf(privilegeString.toUpperCase());
		return this.getTablesAllowedTo(privilegeType);
	}

	private SortedSet<TableInfo> getTablesAllowedTo(PrivilegeType privilegeType)
			throws ObjectNotFoundException {
		CompanyInfo company = this.databaseDefn.getAuthManager().getCompanyForLoggedInUser(
				this.request);
		SortedSet<TableInfo> companyTables = company.getTables();
		// Strip down to the set of tables the user has privileges on
		SortedSet<TableInfo> tablesAllowedTo = new TreeSet<TableInfo>();
		for (TableInfo table : companyTables) {
			if (this.getAuthenticator().loggedInUserAllowedTo(this.request, privilegeType, table)) {
				tablesAllowedTo.add(table);
			}
		}
		return tablesAllowedTo;
	}

	public SortedSet<BaseReportInfo> getViewableReports(TableInfo table)
			throws CodingErrorException {
		Set<BaseReportInfo> allTableReports = table.getReports();
		// Strip down to the set of reports the user has privileges to view
		SortedSet<BaseReportInfo> viewableReports = new TreeSet<BaseReportInfo>();
		for (BaseReportInfo report : allTableReports) {
			if (this.getAuthenticator().loggedInUserAllowedToViewReport(this.request, report)) {
				viewableReports.add(report);
			}
		}
		return viewableReports;
	}

	/**
	 * Returns all the reports from a table that the specified user is able to
	 * view
	 */
	private SortedSet<BaseReportInfo> getViewableReports(TableInfo table, AppUserInfo user)
			throws DisallowedException, CodingErrorException, ObjectNotFoundException {
		SortedSet<BaseReportInfo> allTableReports = table.getReports();
		// Strip down to the set of reports the user has privileges to view
		SortedSet<BaseReportInfo> viewableReports = new TreeSet<BaseReportInfo>();
		for (BaseReportInfo report : allTableReports) {
			if (this.getAuthManager().specifiedUserAllowedToViewReport(this.request, user, report)) {
				viewableReports.add(report);
			}
		}
		return viewableReports;
	}

	public SortedSet<BaseReportInfo> getAllViewableReports() throws CodingErrorException,
			ObjectNotFoundException {
		SortedSet<BaseReportInfo> reports = new TreeSet<BaseReportInfo>();
		Set<TableInfo> tables = this.getTablesAllowedTo(PrivilegeType.VIEW_TABLE_DATA);
		for (TableInfo table : tables) {
			Set<BaseReportInfo> tableReports = this.getViewableReports(table);
			reports.addAll(tableReports);
		}
		return reports;
	}

	public SortedSet<BaseReportInfo> adminGetAllViewableReports(AppUserInfo user)
			throws ObjectNotFoundException, DisallowedException, CodingErrorException {
		SortedSet<BaseReportInfo> reports = new TreeSet<BaseReportInfo>();
		Set<TableInfo> tables = this.getAuthManager().getCompanyForLoggedInUser(this.request)
				.getTables();
		for (TableInfo table : tables) {
			Set<BaseReportInfo> tableReports = this.getViewableReports(table, user);
			reports.addAll(tableReports);
		}
		return reports;
	}

	public Set<TableInfo> getTablesNecessaryToViewReport(BaseReportInfo report)
			throws CodingErrorException {
		return this.getAuthenticator().getTablesNecessaryToViewReport(this.request, report);
	}

	public TableInfo getTable(String tableID) throws ObjectNotFoundException, DisallowedException {
		return this.databaseDefn.getTable(this.request, tableID);
	}

	public TableInfo findTableContainingReport(String reportInternalName)
			throws ObjectNotFoundException, DisallowedException {
		return this.databaseDefn.findTableContainingReport(this.request, reportInternalName);
	}

	public Set<TableInfo> getDependentTables(TableInfo baseTable, boolean direction)
			throws ObjectNotFoundException {
		Set<TableInfo> dependentTables = new LinkedHashSet<TableInfo>();
		this.databaseDefn.getDependentTables(baseTable, dependentTables, direction, this.request);
		return dependentTables;
	}

	public Set<TableInfo> getDependentTables() throws ObjectNotFoundException {
		TableInfo baseTable = this.sessionData.getTable();
		if (baseTable == null) {
			throw new ObjectNotFoundException(
					"Can't select dependent tables prior to table selection");
		}
		Set<TableInfo> dependentTables = new LinkedHashSet<TableInfo>();
		this.databaseDefn.getDependentTables(baseTable, dependentTables, true, this.request);
		return dependentTables;
	}

	public Set<TableInfo> getDirectlyDependentTables(TableInfo baseTable)
			throws ObjectNotFoundException {
		return this.databaseDefn.getDirectlyDependentTables(baseTable, this.request);
	}

	public List<RelationField> getUnchosenRelationFields() throws DisallowedException,
			ObjectNotFoundException {
		return this.getUnchosenRelationFields(this.sessionData.getTable());
	}

	public List<RelationField> getUnchosenRelationFields(TableInfo table)
			throws DisallowedException, ObjectNotFoundException {
		if (!this.getAuthenticator().loggedInUserAllowedTo(this.request,
				PrivilegeType.VIEW_TABLE_DATA, table)) {
			throw new DisallowedException(this.getLoggedInUser(), PrivilegeType.VIEW_TABLE_DATA,
					table);
		}
		List<RelationField> unchosenRelationFields = new LinkedList<RelationField>();
		for (BaseField field : table.getFields()) {
			if (field instanceof RelationField) {
				RelationField relationField = (RelationField) field;
				if (this.sessionData.getRowId(relationField.getRelatedTable()) == -1) {
					unchosenRelationFields.add(relationField);
				}
			}
		}
		return unchosenRelationFields;
	}

	public SortedSet<CommentInfo> getComments(BaseField field, int rowId) throws SQLException,
			DisallowedException, ObjectNotFoundException, CantDoThatException {
		if (field == null) {
			logger.warn("ViewMethods.getComments called with null field");
			return new TreeSet<CommentInfo>();
		}
		TableInfo table = field.getTableContainingField();
		if (!this.getAuthenticator().loggedInUserAllowedTo(this.request,
				PrivilegeType.VIEW_TABLE_DATA, table)) {
			throw new DisallowedException(this.getLoggedInUser(), PrivilegeType.VIEW_TABLE_DATA,
					table);
		}
		CompanyInfo company = this.getLoggedInUser().getCompany();
		return this.databaseDefn.getDataManagement().getComments(company, field, rowId);
	}

	public Map<BaseField, BaseValue> getTableDataRow() throws DisallowedException,
			ObjectNotFoundException, SQLException, CantDoThatException, CodingErrorException {
		return this.getTableDataRow(this.sessionData.getTable());
	}

	public Map<BaseField, BaseValue> getTableDataRow(TableInfo table) throws DisallowedException,
			ObjectNotFoundException, SQLException, CantDoThatException, CodingErrorException {
		if (!this.getAuthenticator().loggedInUserAllowedTo(this.request,
				PrivilegeType.VIEW_TABLE_DATA, table)) {
			throw new DisallowedException(this.getLoggedInUser(), PrivilegeType.VIEW_TABLE_DATA,
					table);
		}
		int rowId = this.sessionData.getRowId(table);
		return this.getTableDataRow(table, rowId);
	}

	public Map<BaseField, BaseValue> getTableDataRow(TableInfo table, int rowId)
			throws DisallowedException, ObjectNotFoundException, SQLException, CantDoThatException,
			CodingErrorException {
		if (!this.getAuthenticator().loggedInUserAllowedTo(this.request,
				PrivilegeType.VIEW_TABLE_DATA, table)) {
			throw new DisallowedException(this.getLoggedInUser(), PrivilegeType.VIEW_TABLE_DATA,
					table);
		}
		// If there was no error submitting the data
		if (this.ex == null) {
			// then we can get the values from the database
			return this.databaseDefn.getDataManagement().getTableDataRow(this.sessionData, table,
					rowId, true);
		} else {
			// otherwise return the last values passed
			return this.sessionData.getFieldInputValues();
		}
	}

	public boolean childDataRowsExist(TableInfo parentTable, int parentRowId, TableInfo childTable)
			throws SQLException, DisallowedException, ObjectNotFoundException {
		if (!this.getAuthenticator().loggedInUserAllowedTo(request, PrivilegeType.VIEW_TABLE_DATA,
				childTable)) {
			throw new DisallowedException(this.getLoggedInUser(), PrivilegeType.VIEW_TABLE_DATA,
					childTable);
		}
		return this.databaseDefn.getDataManagement().childDataRowsExist(parentTable, parentRowId,
				childTable);
	}

	public Set<Integer> getRelatedRowIds(int masterRowId, TableInfo relatedTable)
			throws DisallowedException, CantDoThatException, SQLException, CodingErrorException,
			ObjectNotFoundException {
		BaseReportInfo sessionReport = this.sessionData.getReport();
		if (!this.getAuthenticator().loggedInUserAllowedToViewReport(this.request, sessionReport)) {
			throw new DisallowedException(this.getLoggedInUser(), PrivilegeType.VIEW_TABLE_DATA,
					sessionReport.getParentTable());
		}
		return this.databaseDefn.getDataManagement().getRelatedRowIds(sessionReport, masterRowId,
				relatedTable);
	}

	public Set<Integer> getRelatedRowIds(BaseReportInfo masterReport, int masterRowId,
			TableInfo relatedTable) throws DisallowedException, CantDoThatException, SQLException,
			CodingErrorException, ObjectNotFoundException {
		if (!this.getAuthenticator().loggedInUserAllowedToViewReport(this.request, masterReport)) {
			throw new DisallowedException(this.getLoggedInUser(), PrivilegeType.VIEW_TABLE_DATA,
					masterReport.getParentTable());
		}
		return this.databaseDefn.getDataManagement().getRelatedRowIds(masterReport, masterRowId,
				relatedTable);
	}

	public ReportDataInfo getReportData() throws SQLException, DisallowedException,
			CodingErrorException, ObjectNotFoundException {
		BaseReportInfo report = this.sessionData.getReport();
		this.checkReportViewPrivileges(report);
		CompanyInfo company = this.databaseDefn.getAuthManager().getCompanyForLoggedInUser(
				this.request);
		return this.databaseDefn.getDataManagement().getReportData(company, report, true);
	}

	public ReportDataInfo getReportData(BaseReportInfo report) throws SQLException,
			DisallowedException, CodingErrorException, ObjectNotFoundException {
		this.checkReportViewPrivileges(report);
		CompanyInfo company = this.databaseDefn.getAuthManager().getCompanyForLoggedInUser(
				this.request);
		return this.databaseDefn.getDataManagement().getReportData(company, report, true);
	}

	public ReportDataInfo getReportData(BaseReportInfo report, boolean updateCacheIfObsolete)
			throws SQLException, DisallowedException, CodingErrorException, ObjectNotFoundException {
		this.checkReportViewPrivileges(report);
		CompanyInfo company = this.databaseDefn.getAuthManager().getCompanyForLoggedInUser(
				this.request);
		return this.databaseDefn.getDataManagement().getReportData(company, report,
				updateCacheIfObsolete);
	}

	public List<DataRowInfo> getReportDataRows() throws DisallowedException, SQLException,
			ObjectNotFoundException, CodingErrorException, CantDoThatException {
		BaseReportInfo report = this.sessionData.getReport();
		if (report == null) {
			throw new ObjectNotFoundException("There's no report in the session to view");
		}
		return this.getReportDataRows(report);
	}

	public List<DataRowInfo> getReportDataRows(BaseReportInfo report) throws DisallowedException,
			SQLException, ObjectNotFoundException, CodingErrorException, CantDoThatException {
		int rowLimit = this.sessionData.getReportRowLimit();
		return this.getReportDataRows(report, rowLimit);
	}

	public List<DataRowInfo> getReportDataRows(BaseReportInfo report, int rowLimit)
			throws DisallowedException, SQLException, ObjectNotFoundException,
			CodingErrorException, CantDoThatException {
		Map<BaseField, String> reportFilterValues = this.sessionData.getReportFilterValues(report);
		return this.getReportDataRows(report, rowLimit, reportFilterValues, false);
	}

	public List<DataRowInfo> getReportDataRows(BaseReportInfo report, int rowLimit,
			Map<BaseField, String> reportFilterValues, boolean exactFilters)
			throws DisallowedException, SQLException, ObjectNotFoundException,
			CodingErrorException, CantDoThatException {
		if (report == null) {
			throw new ObjectNotFoundException("No report was provided");
		}
		// Check privileges for all tables from which data is displayed from,
		// throw DisallowedException if privileges not sufficient
		this.checkReportViewPrivileges(report);
		Map<BaseField, Boolean> sessionReportSorts = this.sessionData.getReportSorts();
		// Pass user in so that if user is an app user, the report can be filtered by Created By = that user
		AuthManagerInfo authManager = this.databaseDefn.getAuthManager();
		AppUserInfo user = authManager.getLoggedInUser(this.request);
		AppUserInfo delegateUser = user;
		if (authManager.getAuthenticator().loggedInUserAllowedTo(this.request, PrivilegeType.ADMINISTRATE) && user.getUsesCustomUI()) {
			// If the user is an administrator and a custom app user, use a delegate user = the session user
			// rather than the administrator themselves
			delegateUser = this.sessionData.getUser();
			if (delegateUser == null) {
				throw new CantDoThatException("There is no user in the session");
			}
			if (!delegateUser.getUsesCustomUI()) {
				throw new CantDoThatException("The user " + delegateUser + " must be set to use the custom " + delegateUser.getCompany() + " user interface");
			}
		}
		List<DataRowInfo> reportDataRows = this.databaseDefn.getDataManagement().getReportDataRows(
				delegateUser, report, reportFilterValues, exactFilters, sessionReportSorts, rowLimit,
				QuickFilterType.AND, false);
		if (!exactFilters) {
			// Also only log user requested (pane 2) reports
			UsageLogger usageLogger = new UsageLogger(this.databaseDefn.getDataSource());
			usageLogger.logReportView(user, report, reportFilterValues, rowLimit, null);
			UsageLogger.startLoggingThread(usageLogger);
		}
		return reportDataRows;
	}

	public List<DataRowInfo> getGloballyFilteredReportDataRows(BaseReportInfo report)
			throws DisallowedException, SQLException, ObjectNotFoundException,
			CodingErrorException, CantDoThatException {
		if (report == null) {
			throw new ObjectNotFoundException("No report was provided");
		}
		// Check privileges for all tables from which data is displayed from,
		// throw DisallowedException if privileges not sufficient
		this.checkReportViewPrivileges(report);
		String globalFilterString = this.sessionData.getGlobalFilterString(report);
		Map<BaseField, String> reportFilterValues = report
				.getGlobalFilterValues(globalFilterString);
		AppUserInfo user = this.databaseDefn.getAuthManager().getUserByUserName(this.request,
				this.request.getRemoteUser());
		CompanyInfo company = user.getCompany();
		Map<BaseField, Boolean> sessionReportSorts = new HashMap<BaseField, Boolean>(0);
		boolean exactFilters = false;
		int rowLimit = this.sessionData.getReportRowLimit();
		List<DataRowInfo> reportDataRows = this.databaseDefn.getDataManagement().getReportDataRows(
				user, report, reportFilterValues, exactFilters, sessionReportSorts, rowLimit,
				QuickFilterType.OR, false);
		UsageLogger usageLogger = new UsageLogger(this.databaseDefn.getDataSource());
		usageLogger.logReportView(user, report, reportFilterValues, rowLimit, "global search");
		UsageLogger.startLoggingThread(usageLogger);
		return reportDataRows;
	}

	public String getReportDataRowsJSON() throws DisallowedException, SQLException,
			ObjectNotFoundException, JsonGenerationException, CodingErrorException,
			CantDoThatException, XMLStreamException {
		BaseReportInfo report = this.sessionData.getReport();
		Map<BaseField, String> reportFilterValues = this.sessionData.getReportFilterValues(report);
		AppUserInfo user = this.databaseDefn.getAuthManager().getLoggedInUser(request);
		return this.databaseDefn.getDataManagement().getReportJSON(user, report,
				reportFilterValues, false, 0);
	}

	public String getReportMapJSON() throws ObjectNotFoundException, CodingErrorException,
			CantDoThatException, SQLException, DisallowedException {
		BaseReportInfo report = this.sessionData.getReport();
		Map<BaseField, String> reportFilterValues = this.sessionData.getReportFilterValues(report);
		AppUserInfo user = this.databaseDefn.getAuthManager().getLoggedInUser(request);
		CompanyInfo company = this.databaseDefn.getAuthManager().getCompanyForLoggedInUser(
				this.request);
		return this.databaseDefn.getDataManagement().getReportMapJson(user, report,
				reportFilterValues);
	}

	public String getReportTimelineJSON() throws CodingErrorException, CantDoThatException,
			MissingParametersException, DisallowedException, ObjectNotFoundException, SQLException,
			JsonGenerationException {
		AppUserInfo user = this.getLoggedInUser();
		SortedSet<BaseReportInfo> timelineReports = new TreeSet<BaseReportInfo>();
		Set<BaseReportInfo> reports = user.getOperationalDashboardReports();
		for (BaseReportInfo report : reports) {
			if (report.getCalendarStartField() != null) {
				this.checkReportViewPrivileges(report);
				timelineReports.add(report);
			}
		}
		Map<BaseField, String> filterValues = new HashMap<BaseField, String>(
				this.sessionData.getReportFilterValues());
		return this.databaseDefn.getDataManagement().getReportTimelineJSON(user, timelineReports,
				filterValues);
	}

	public String getReportCalendarJSON() throws CodingErrorException, CantDoThatException,
			MissingParametersException, DisallowedException, ObjectNotFoundException, SQLException,
			JsonGenerationException {
		BaseReportInfo report = ServletUtilMethods.getReportForRequest(this.sessionData,
				this.request, this.databaseDefn, true);
		// Check privileges for all tables from which data is displayed from,
		// throw DisallowedException if privileges not sufficient
		this.checkReportViewPrivileges(report);
		Map<BaseField, String> filterValues = new HashMap<BaseField, String>();
		// Add start and end time filters
		ReportFieldInfo eventDateReportField = report.getCalendarStartField();
		if (eventDateReportField == null) {
			throw new CantDoThatException("The report " + report + " has no applicable date fields");
		}
		BaseField eventDateField = eventDateReportField.getBaseField();
		// 'start' and 'end' are supplied by FullCalendar
		Long startEpoch = null;
		Long endEpoch = null;
		String startString = this.request.getParameter("start");
		if (startString != null) {
			startEpoch = Long.valueOf(startString);
			String endString = this.request.getParameter("end");
			if (endString == null) {
				throw new MissingParametersException(
						"calendar event start parameter provided but not end");
			}
			endEpoch = Long.valueOf(endString);
			String eventDateFilterString = ">" + startString + " AND <" + endString;
			filterValues.put(eventDateField, eventDateFilterString);
		}
		AppUserInfo user = this.getLoggedInUser();
		return this.databaseDefn.getDataManagement().getReportCalendarJSON(user, report,
				filterValues, startEpoch, endEpoch);
	}

	/**
	 * Given a report, throws a DisallowedException if the user isn't allowed to
	 * view that report
	 */
	private void checkReportViewPrivileges(BaseReportInfo report) throws DisallowedException,
			CodingErrorException, ObjectNotFoundException {
		if (!this.getAuthenticator().loggedInUserAllowedToViewReport(this.request, report)) {
			logger.warn("Report " + report + " is not viewable by user "
					+ this.request.getRemoteUser());
			throw new DisallowedException(this.getLoggedInUser(), PrivilegeType.VIEW_TABLE_DATA,
					report.getParentTable());
		}
	}

	public Map<RelationField, List<DataRow>> getChildDataTableRows() throws DisallowedException,
			SQLException, ObjectNotFoundException, CodingErrorException {
		// retrieve the previously set table & rowid:
		TableInfo table = this.sessionData.getTable(); // check for null values
		// and throw exception
		if (table == null)
			throw new ObjectNotFoundException("Can't select child data prior to table selection");
		int rowid = this.sessionData.getRowId(); // check for null values and
		// throw exception
		if (rowid == -1)
			throw new ObjectNotFoundException("Can't select child data prior to row selection");
		return this.getChildDataTableRows(table, rowid);
	}

	public Map<RelationField, List<DataRow>> getChildDataTableRows(TableInfo table, int rowid)
			throws DisallowedException, SQLException, ObjectNotFoundException, CodingErrorException {
		Map<RelationField, List<DataRow>> childDataTableRows = this.databaseDefn
				.getDataManagement().getChildDataTableRows(this.databaseDefn, table, rowid,
						this.request);
		// remove any recordsets belonging to tables the user is not authorised
		// to view:
		for (RelationField relationField : childDataTableRows.keySet()) {
			TableInfo relatedTable = relationField.getRelatedTable();
			if (!this.getAuthenticator().loggedInUserAllowedTo(this.request,
					PrivilegeType.VIEW_TABLE_DATA, relatedTable)) {
				childDataTableRows.remove(relationField);
			}
		}
		// return the permitted sets of related data:
		return childDataTableRows;
	}

	public ChartDataInfo getChartData() throws DisallowedException, SQLException,
			ObjectNotFoundException, CodingErrorException, CantDoThatException {
		BaseReportInfo report = this.sessionData.getReport();
		return this.getChartData(report.getChart());
	}

	public ChartDataInfo getChartData(ChartInfo reportSummary) throws DisallowedException,
			SQLException, ObjectNotFoundException, CodingErrorException, CantDoThatException {
		return this.getChartData(reportSummary, false);
	}

	public ChartDataInfo getCachedChartData(ChartInfo reportSummary) throws DisallowedException,
			ObjectNotFoundException, CodingErrorException, CantDoThatException, SQLException {
		return this.getChartData(reportSummary, true);
	}

	private ChartDataInfo getChartData(ChartInfo reportSummary, boolean useCache)
			throws DisallowedException, SQLException, ObjectNotFoundException,
			CodingErrorException, CantDoThatException {
		// Check privileges for all tables from which data in the report is
		// displayed from, throw
		// DisallowedException if privileges not sufficient
		this.checkReportViewPrivileges(reportSummary.getReport());
		ChartDataInfo reportSummaryData;
		CompanyInfo company = this.databaseDefn.getAuthManager().getCompanyForLoggedInUser(
				this.request);
		Map<BaseField, String> reportFilterValues = this.sessionData.getReportFilterValues();
		reportSummaryData = this.databaseDefn.getDataManagement().getChartData(company,
				reportSummary, reportFilterValues, useCache);
		return reportSummaryData;
	}

	public ChartDataInfo getFieldSummaryData(ReportFieldInfo reportField)
			throws DisallowedException, SQLException, CodingErrorException,
			ObjectNotFoundException, CantDoThatException {
		BaseReportInfo report = reportField.getParentReport();
		this.checkReportViewPrivileges(report);
		ChartInfo chart = new ChartDefn(report, reportField.getFieldName(), false);
		BaseField field = reportField.getBaseField();
		FieldCategory fieldCategory = field.getFieldCategory();
		if (fieldCategory.equals(FieldCategory.NUMBER)) {
			chart.addFunction(new ChartAggregateDefn(AggregateFunction.SUM, reportField));
			chart.addFunction(new ChartAggregateDefn(AggregateFunction.AVG, reportField));
		} else if (fieldCategory.equals(FieldCategory.TEXT)) {
			if (((TextField) field).usesLookup()) {
				chart.addGrouping(reportField, null);
				chart.addFunction(new ChartAggregateDefn(AggregateFunction.COUNT, report
						.getReportField(report.getParentTable().getPrimaryKey()
								.getInternalFieldName())));
			} else if (!field.getTableContainingField().equals(report.getParentTable())) {
				// Data from other tables is treated as look-up-able as there
				// may be more than one related record
				chart.addGrouping(reportField, null);
				chart.addFunction(new ChartAggregateDefn(AggregateFunction.COUNT, report
						.getReportField(report.getParentTable().getPrimaryKey()
								.getInternalFieldName())));
			}
		}
		Map<BaseField, String> filters = this.sessionData.getReportFilterValues();
		CompanyInfo company = this.databaseDefn.getAuthManager().getCompanyForLoggedInUser(
				this.request);
		return this.databaseDefn.getDataManagement().getChartData(company, chart, filters, false);
	}

	public SortedSet<WordInfo> getReportWordCloud(int minWeight, int maxWeight, int maxTags)
			throws ObjectNotFoundException, DisallowedException, CodingErrorException,
			CantDoThatException, SQLException {
		BaseReportInfo report = this.sessionData.getReport();
		if (report == null) {
			throw new ObjectNotFoundException("There's no report in the session to view");
		}
		Set<BaseField> reportBaseFields = report.getReportBaseFields();
		Set<String> stopWords = new HashSet<String>();
		return this.getReportWordCloud(report, reportBaseFields, stopWords, minWeight, maxWeight,
				maxTags);
	}

	public SortedSet<WordInfo> getReportWordCloud(BaseReportInfo report,
			ReportFieldInfo reportField, Set<String> stopWords, int minWeight, int maxWeight,
			int maxTags) throws ObjectNotFoundException, DisallowedException, CodingErrorException,
			CantDoThatException, SQLException {
		Set<BaseField> fields = new HashSet<BaseField>();
		if (reportField.getBaseField().equals(report.getParentTable().getPrimaryKey())) {
			// Primary key means all fields in the report
			fields.addAll(report.getReportBaseFields());
		} else {
			fields.add(reportField.getBaseField());
		}
		return this.getReportWordCloud(report, fields, stopWords, minWeight, maxWeight, maxTags);
	}

	private SortedSet<WordInfo> getReportWordCloud(BaseReportInfo report,
			Set<BaseField> reportBaseFields, Set<String> stopWords, int minWeight, int maxWeight,
			int maxTags) throws ObjectNotFoundException, DisallowedException, CodingErrorException,
			CantDoThatException, SQLException {
		Map<BaseField, String> filters = this.sessionData.getReportFilterValues();
		Set<BaseField> textFields = new HashSet<BaseField>();
		FIELDS: for (BaseField field : reportBaseFields) {
			// Get a list of suitable fields from those specified for which to
			// extract text and build tags
			if (field.getDbType().equals(DatabaseFieldType.VARCHAR)) {
				if (field instanceof TextField) {
					if (((TextField) field).usesLookup()) {
						continue FIELDS;
					}
				}
				if (!field.getHidden()) {
					textFields.add(field);
				}
			}
		}
		// Exclude words in the filters for all fields in the current report
		// from the cloud
		if (stopWords == null) {
			stopWords = new HashSet<String>();
		}
		for (BaseField field : report.getReportBaseFields()) {
			String filterValue = filters.get(field);
			if (filterValue != null) {
				filterValue = Helpers.rinseString(filterValue);
				if (!filterValue.equals("")) {
					Set<String> fieldStopWords = new HashSet<String>(Arrays.asList(filterValue
							.split("\\s")));
					stopWords.addAll(fieldStopWords);
				}
			}
		}

		if (textFields.size() == 0) {
			return new TreeSet<WordInfo>();
		}
		String conglomoratedText = this.databaseDefn.getDataManagement().getReportDataText(report,
				textFields, this.sessionData.getReportFilterValues(), 1000000);
		WordCloud cloud = new WordCloud(conglomoratedText, minWeight, maxWeight, maxTags, stopWords);
		return cloud.getWords();
	}

	public SortedMap<TableInfo, SortedSet<BaseField>> adminGetRelationCandidates()
			throws DisallowedException, ObjectNotFoundException {
		TableInfo table = this.sessionData.getTable();
		if (!(getAuthenticator().loggedInUserAllowedTo(this.request, PrivilegeType.MANAGE_TABLE,
				table))) {
			throw new DisallowedException(this.getLoggedInUser(), PrivilegeType.MANAGE_TABLE, table);
		}
		CompanyInfo company = this.databaseDefn.getAuthManager().getCompanyForLoggedInUser(
				this.request);
		SortedSet<TableInfo> companyTables = company.getTables();
		SortedMap<TableInfo, SortedSet<BaseField>> relationCandidates = new TreeMap<TableInfo, SortedSet<BaseField>>();
		// check each table for candidate fields
		for (TableInfo testTable : companyTables) {
			// related fields must be in different tables to the current one
			if (!(testTable.equals(table))) {
				// Check whether the user has privileges to view the table
				// containing potential relation
				// fields - only add fields from the related table if they do
				if (this.getAuthenticator().loggedInUserAllowedTo(this.request,
						PrivilegeType.VIEW_TABLE_DATA, table)) {
					SortedSet<BaseField> uniqueFieldsForTable = new TreeSet<BaseField>();
					// check each field to see if it's unique and can be used in
					// a
					// foreign key relation
					for (BaseField testField : testTable.getFields()) {
						if (testField.getUnique()) {
							uniqueFieldsForTable.add(testField);
						}
					}
					// if there were any candidate fields in that table, table &
					// field set to be returned
					if (uniqueFieldsForTable.size() > 0) {
						relationCandidates.put(testTable, uniqueFieldsForTable);
					}
				}
			}
		}
		return relationCandidates;
	}

	public SortedSet<AppUserInfo> adminGetUsers() throws DisallowedException,
			ObjectNotFoundException {
		return this.getAuthManager().getUsers(this.request);
	}
	
	public SortedSet<AppUserInfo> getAdministrators() throws ObjectNotFoundException, CodingErrorException {
		return this.getAuthManager().getAdministrators(this.request);
	}

	public SortedSet<AppRoleInfo> adminGetRoles() throws DisallowedException,
			ObjectNotFoundException {
		return this.getAuthManager().getRoles(this.request);
	}

	public SortedSet<AppRoleInfo> adminGetRolesForUser(AppUserInfo user)
			throws DisallowedException, ObjectNotFoundException {
		return this.getAuthManager().getRolesForUser(this.request, user);
	}

	public EnumSet<PrivilegeType> adminGetPrivilegeTypes() throws DisallowedException,
			ObjectNotFoundException {
		return EnumSet.allOf(PrivilegeType.class);
	}

	public boolean loggedInUserAllowedTo(String privilegeTypeToCheck)
			throws IllegalArgumentException, ObjectNotFoundException {
		PrivilegeType privilegeType = PrivilegeType.valueOf(privilegeTypeToCheck.toUpperCase());
		return this.getAuthenticator().loggedInUserAllowedTo(this.request, privilegeType);
	}

	public boolean loggedInUserAllowedTo(String privilegeTypeToCheck, TableInfo table)
			throws IllegalArgumentException {
		PrivilegeType privilegeType = PrivilegeType.valueOf(privilegeTypeToCheck.toUpperCase());
		return this.getAuthenticator().loggedInUserAllowedTo(this.request, privilegeType, table);
	}

	public boolean loggedInUserAllowedToViewReport(BaseReportInfo report)
			throws CodingErrorException {
		return this.getAuthenticator().loggedInUserAllowedToViewReport(this.request, report);
	}

	public boolean userHasPrivilege(String privilegeTypeToCheck) throws DisallowedException,
			IllegalArgumentException, ObjectNotFoundException {
		PrivilegeType privilegeType = PrivilegeType.valueOf(privilegeTypeToCheck.toUpperCase());
		AppUserInfo sessionUser = this.sessionData.getUser();
		return this.getAuthManager().specifiedUserHasPrivilege(this.request, privilegeType, sessionUser);
	}

	public boolean userHasPrivilege(String privilegeTypeToCheck, TableInfo table)
			throws IllegalArgumentException, ObjectNotFoundException, DisallowedException {
		PrivilegeType privilegeType = PrivilegeType.valueOf(privilegeTypeToCheck.toUpperCase());
		AppUserInfo sessionUser = this.sessionData.getUser();
		return this.getAuthManager().specifiedUserHasPrivilege(this.request, privilegeType, sessionUser,
				table);
	}

	public boolean userHasPrivilege(AppUserInfo user, String privilegeTypeToCheck)
			throws DisallowedException, ObjectNotFoundException {
		PrivilegeType privilegeType = PrivilegeType.valueOf(privilegeTypeToCheck.toUpperCase());
		return this.getAuthManager().specifiedUserHasPrivilege(this.request, privilegeType, user);
	}

	public boolean userHasPrivilege(AppUserInfo user, String privilegeTypeToCheck, TableInfo table)
			throws DisallowedException, ObjectNotFoundException {
		PrivilegeType privilegeType = PrivilegeType.valueOf(privilegeTypeToCheck.toUpperCase());
		boolean hasPrivilege = this.getAuthManager().specifiedUserHasPrivilege(this.request,
				privilegeType, user, table);
		return hasPrivilege;
	}

	public boolean roleHasPrivilege(String privilegeTypeToCheck) throws IllegalArgumentException,
			DisallowedException, ObjectNotFoundException {
		PrivilegeType privilegeType = PrivilegeType.valueOf(privilegeTypeToCheck.toUpperCase());
		AppRoleInfo sessionRole = this.sessionData.getRole();
		return this.getAuthManager().specifiedRoleHasPrivilege(this.request, privilegeType, sessionRole);
	}

	public boolean roleHasPrivilege(String privilegeTypeToCheck, TableInfo table)
			throws IllegalArgumentException, DisallowedException, ObjectNotFoundException {
		PrivilegeType privilegeType = PrivilegeType.valueOf(privilegeTypeToCheck.toUpperCase());
		AppRoleInfo sessionRole = this.sessionData.getRole();
		return this.getAuthManager().specifiedRoleHasPrivilege(this.request, privilegeType, sessionRole,
				table);
	}

	public boolean roleHasPrivilege(AppRoleInfo role, String privilegeTypeToCheck)
			throws IllegalArgumentException, DisallowedException, ObjectNotFoundException {
		PrivilegeType privilegeType = PrivilegeType.valueOf(privilegeTypeToCheck.toUpperCase());
		return this.getAuthManager().specifiedRoleHasPrivilege(this.request, privilegeType, role);
	}

	public boolean roleHasPrivilege(AppRoleInfo role, String privilegeTypeToCheck, TableInfo table)
			throws IllegalArgumentException, DisallowedException, ObjectNotFoundException {
		PrivilegeType privilegeType = PrivilegeType.valueOf(privilegeTypeToCheck.toUpperCase());
		return this.getAuthManager().specifiedRoleHasPrivilege(this.request, privilegeType, role, table);
	}

	public List<JoinClauseInfo> getCandidateJoins(SimpleReportInfo report, boolean direction)
			throws CodingErrorException, ObjectNotFoundException {
		List<JoinClauseInfo> candidateJoins = new LinkedList<JoinClauseInfo>();
		// Collect all tables which are already joined or have child reports
		// which are joined.
		// We don't want to join to these again
		Set<TableInfo> dontJoinToTables = report.getJoinReferencedTables();
		// Get the set of tables which are directly joined - these are the
		// jumping off point to look for joins to other tables
		Set<TableInfo> alreadyJoinedTables = report.getJoinedTables();
		// For permission checking
		Set<TableInfo> viewableTables = this.getTablesAllowedTo(PrivilegeType.VIEW_TABLE_DATA);
		// Look for the joins
		for (TableInfo leftTable : alreadyJoinedTables) {
			if (direction) {
				// upward joins
				for (BaseField field : leftTable.getFields()) {
					if (field instanceof RelationField) {
						RelationField relationField = ((RelationField) field);
						TableInfo rightTable = relationField.getRelatedTable();
						if (viewableTables.contains(rightTable)) {
							if (!dontJoinToTables.contains(rightTable)) {
								JoinClauseInfo candidateJoin = new JoinClause(field,
										relationField.getRelatedField(), JoinType.LEFT_OUTER);
								candidateJoins.add(candidateJoin);
							}
						}
					}
				}
			} else {
				// downward joins
				for (TableInfo dependentTable : this.getDirectlyDependentTables(leftTable)) {
					if (viewableTables.contains(dependentTable)
							&& (!dontJoinToTables.contains(dependentTable))) {
						for (BaseField field : dependentTable.getFields()) {
							if (field instanceof RelationField) {
								RelationField relationField = ((RelationField) field);
								if (relationField.getRelatedTable().equals(leftTable)) {
									JoinClauseInfo candidateJoin = new JoinClause(
											leftTable.getPrimaryKey(), relationField,
											JoinType.LEFT_OUTER);
									candidateJoins.add(candidateJoin);
								}
							}
						}
					}
				}
			}
		}
		return candidateJoins;
	}

	public boolean getWhetherExceptionOccurred() {
		return this.whetherExceptionOccurred;
	}

	public void setException(Exception ex) {
		this.whetherExceptionOccurred = true;
		this.ex = ex;
	}

	public Exception getException() {
		return this.ex;
	}

	public AppUserInfo getLoggedInUser() throws DisallowedException, ObjectNotFoundException {
		return this.getAuthManager().getLoggedInUser(this.request);
	}

	public String toString() {
		return "ViewMethods contains methods of use to Velocity template designers, for accessing schema information and database data";
	}

	private AuthManagerInfo getAuthManager() {
		return this.databaseDefn.getAuthManager();
	}

	private AuthenticatorInfo getAuthenticator() {
		return this.getAuthManager().getAuthenticator();
	}

	public String toInternalNames(String sourceText) throws ObjectNotFoundException {
		sourceText = sourceText.toLowerCase(Locale.UK);
		if (sourceText.contains("{")) {
			for (TableInfo table : this.getTablesAllowedTo(PrivilegeType.VIEW_TABLE_DATA)) {
				// generate regex for table object:
				String tableName = table.getTableName().replaceAll("(\\W)", "\\\\$1");
				// generate regex for each object combination (surrounding with
				// markers):
				String table_in = "\\{" + tableName + "\\}";
				// generate replacement text:
				String table_out = table.getInternalTableName();
				// run the replacements:
				sourceText = sourceText.replaceAll(table_in.toLowerCase(Locale.UK), table_out);
				for (BaseReportInfo report : table.getReports()) {
					String reportName = report.getReportName().replaceAll("(\\W)", "\\\\$1");
					String report_in = "\\{" + reportName + "\\}";
					String report_out = report.getInternalReportName();
					sourceText = sourceText
							.replaceAll(report_in.toLowerCase(Locale.UK), report_out);
					for (BaseField field : report.getReportBaseFields()) {
						String fieldName = field.getFieldName().replaceAll("(\\W)", "\\\\$1");

						String field_in = "\\{" + fieldName + "\\}";
						String table_field_in = "\\{" + tableName + "." + fieldName + "\\}";
						String report_field_in = "\\{" + reportName + "." + fieldName + "\\}";

						String field_out = field.getInternalFieldName();
						String table_field_out = table.getInternalTableName() + "."
								+ field.getInternalFieldName();
						String report_field_out = report.getInternalReportName() + "."
								+ field.getInternalFieldName();

						sourceText = sourceText.replaceAll(field_in.toLowerCase(Locale.UK),
								field_out);
						sourceText = sourceText.replaceAll(table_field_in.toLowerCase(Locale.UK),
								table_field_out);
						sourceText = sourceText.replaceAll(report_field_in.toLowerCase(Locale.UK),
								report_field_out);
					}
				}
			}
		}
		return sourceText;
	}

	public String toExternalNames(String sourceText) throws ObjectNotFoundException {
		for (TableInfo table : this.getTablesAllowedTo(PrivilegeType.VIEW_TABLE_DATA)) {
			sourceText = sourceText.replaceAll(table.getInternalTableName(), table.getTableName()
					.toLowerCase().replaceAll("\\W", ""));
			for (BaseReportInfo report : table.getReports()) {
				sourceText = sourceText.replaceAll(report.getInternalReportName(), report
						.getReportName().toLowerCase().replaceAll("\\W", ""));
				for (BaseField field : report.getReportBaseFields()) {
					sourceText = sourceText.replaceAll(field.getInternalFieldName(), field
							.getFieldName().toLowerCase().replaceAll("\\W", ""));
				}
			}
		}
		return sourceText;
	}

	public int getUploadSpeed() {
		return this.databaseDefn.getDataManagement().getUploadSpeed();
	}
	
	public boolean isJoinUsed(SimpleReportInfo report, JoinClauseInfo join) throws CantDoThatException, CodingErrorException {
		return this.databaseDefn.isJoinUsed(report, join);
	}

	private String toolbarPluginName = null;

	private final HttpServletRequest request;

	private final SessionDataInfo sessionData;

	private final DatabaseInfo databaseDefn;

	private Map<String, List<ModuleActionInfo>> moduleActions = new HashMap<String, List<ModuleActionInfo>>();

	private boolean whetherExceptionOccurred = false;

	private Exception ex = null;

	private static final SimpleLogger logger = new SimpleLogger(ViewMethods.class);

}