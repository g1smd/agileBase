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
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import com.gtwm.pb.auth.PrivilegeType;
import com.gtwm.pb.auth.DisallowedException;
import com.gtwm.pb.model.interfaces.CompanyInfo;
import com.gtwm.pb.model.interfaces.AppUserInfo;
import com.gtwm.pb.model.interfaces.AppRoleInfo;
import com.gtwm.pb.model.interfaces.JoinClauseInfo;
import com.gtwm.pb.model.interfaces.ReportDataInfo;
import com.gtwm.pb.model.interfaces.ReportFieldInfo;
import com.gtwm.pb.model.interfaces.ReportSummaryDataInfo;
import com.gtwm.pb.model.interfaces.ReportSummaryInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.ModuleActionInfo;
import com.gtwm.pb.model.interfaces.TagInfo;
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
import com.gtwm.pb.model.interfaces.WikiRecordDataRowInfo;
import com.gtwm.pb.model.interfaces.ModuleInfo;
import com.gtwm.pb.model.manageSchema.JoinType;
import com.gtwm.pb.model.manageSchema.JoinClause;
import com.gtwm.pb.model.manageSchema.ReportSummaryAggregateDefn;
import com.gtwm.pb.model.manageSchema.ReportSummaryDefn;
import com.gtwm.pb.model.manageSchema.FieldTypeDescriptor.FieldCategory;
import com.gtwm.pb.model.manageUsage.UsageStats;
import com.gtwm.pb.model.manageUsage.UsageLogger;
import com.gtwm.pb.model.manageData.ModuleAction;
import com.gtwm.pb.util.AppProperties;
import com.gtwm.pb.util.Enumerations.AggregateFunction;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.Enumerations.DatabaseFieldType;
import com.gtwm.pb.util.Enumerations.ExtraAction;
import com.gtwm.pb.util.Helpers;
import javax.servlet.http.HttpServletRequest;
import org.grlea.log.SimpleLogger;
import java.util.TreeSet;

public final class ViewMethods implements ViewMethodsInfo {

	private ViewMethods() {
		this.request = null;
		this.databaseDefn = null;
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
			if (sessionData == null) {
				throw new ObjectNotFoundException("Session data not retrieved");
			}
		}
		this.toolbarPluginName = request.getParameter(ExtraAction.INCLUDE_TOOLBAR_PLUGIN.toString()
				.toLowerCase(Locale.UK));
		this.databaseDefn = databaseDefn;
	}

	public String getModuleGraphCode(ModuleInfo module) throws CodingErrorException, IOException,
			ObjectNotFoundException {
		Set<String> graphCodeLines = new LinkedHashSet<String>();
		SortedSet<BaseReportInfo> reportsInModule = this.getReportsInModule(module);
		for (BaseReportInfo report : reportsInModule) {
			SimpleReportInfo simpleReport = (SimpleReportInfo) report;
			Set<SimpleReportInfo> alreadyProcessedReports = new HashSet<SimpleReportInfo>();
			alreadyProcessedReports.add(simpleReport);
			graphCodeLines.addAll(this.getReportGraphCode(simpleReport, alreadyProcessedReports));
		}
		String graphCode = "";
		for (String graphCodeLine : graphCodeLines) {
			graphCode += graphCodeLine + "\n";
		}
		graphCode = "digraph module_code {\n" + graphCode + "}\n";
		String internalModuleName = module.getInternalModuleName();
		String webAppRoot = this.databaseDefn.getDataManagement().getWebAppRoot();
		String graphCodeFilename = webAppRoot + "module_graphs/" + internalModuleName + ".txt";
		String graphFilename = webAppRoot + "module_graphs/" + internalModuleName + ".png";
		String graphCodeFileContents = "";
		// If newly generated code different from old, overwrite old
		if ((new File(graphCodeFilename)).exists()) {
			graphCodeFileContents = Helpers.readFile(graphCodeFilename);
		}
		if (!graphCode.equals(graphCodeFileContents)) {
			Helpers.writeFile(graphCodeFilename, graphCode);
			Runtime runtime = Runtime.getRuntime();
			try {
				Process dotProcess = runtime.exec("dot -Tpng -o " + graphFilename + " "
						+ graphCodeFilename);
				dotProcess.waitFor();
			} catch (InterruptedException iex) {
				logger.warn("Error creating graph for module " + module + ": " + iex);
			} catch (IOException ioex) {
				logger.warn("Unable to create graph for module " + module + ": " + ioex);
			}
		}
		return graphCode;
	}

	private Set<String> getReportGraphCode(SimpleReportInfo report,
			Set<SimpleReportInfo> alreadyProcessedReports) throws CodingErrorException {
		Set<String> reportGraphCode = new LinkedHashSet<String>();
		for (BaseReportInfo joinedReport : report.getJoinedReports()) {
			SimpleReportInfo simpleJoinedReport = (SimpleReportInfo) joinedReport;
			if (!simpleJoinedReport.equals(report)) {
				String reportGraphCodeLine = "\"" + report.getModule() + " : " + report + "\" ->"
						+ "\"" + simpleJoinedReport.getModule() + " : " + simpleJoinedReport
						+ "\" [shape=box];";
				reportGraphCode.add(reportGraphCodeLine);
				if (!alreadyProcessedReports.contains(simpleJoinedReport)) {
					alreadyProcessedReports.add(simpleJoinedReport);
					Set<String> reportGraphCodeLines = this.getReportGraphCode(simpleJoinedReport,
							alreadyProcessedReports);
					reportGraphCode.addAll(reportGraphCodeLines);
				}
			}
		}
		for (TableInfo joinedTable : report.getJoinedTables()) {
			String reportGraphCodeLine = "\"" + report.getModule() + " : " + report
					+ "\" -> \"[table] " + joinedTable + "\" [shape=box];";
			reportGraphCode.add(reportGraphCodeLine);
		}
		return reportGraphCode;
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

	public synchronized void addModuleAction(String internaModuleName, String actionName, String description,
			String attributes, String actionTemplate, String buttons, String callbackFunction) {
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

	public boolean isWikiIntegrated() throws ObjectNotFoundException, DisallowedException {
		AppUserInfo loggedInUser = this.getLoggedInUser();
		CompanyInfo company = loggedInUser.getCompany();
		return this.databaseDefn.getWikiManagement(company).isWikiIntegrated();
	}

	public String getWikiPageSnippet(String wikiPageName, int numChars) throws CantDoThatException,
			SQLException, DisallowedException, ObjectNotFoundException {
		AppUserInfo loggedInUser = this.getLoggedInUser();
		CompanyInfo company = loggedInUser.getCompany();
		return this.databaseDefn.getWikiManagement(company).getWikiPageSnippet(wikiPageName,
				numChars);
	}

	public String getWikiPageUrl(String wikiPageName, boolean edit) throws ObjectNotFoundException,
			DisallowedException {
		AppUserInfo loggedInUser = this.getLoggedInUser();
		CompanyInfo company = loggedInUser.getCompany();
		return this.databaseDefn.getWikiManagement(company).getWikiUrl(wikiPageName, edit);
	}

	public boolean usesToolbarPlugin() {
		return (this.toolbarPluginName != null);
	}

	public String getToolbarPluginName() {
		return this.toolbarPluginName;
	}

	public SortedSet<CompanyInfo> getCompanies() throws DisallowedException {
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
	 * Returns all the reports from a table that the specified user is able to view
	 */
	private SortedSet<BaseReportInfo> getViewableReports(TableInfo table, AppUserInfo user) throws DisallowedException, CodingErrorException {
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

	public SortedSet<BaseReportInfo> adminGetAllViewableReports(AppUserInfo user) throws ObjectNotFoundException, DisallowedException, CodingErrorException {
		SortedSet<BaseReportInfo> reports = new TreeSet<BaseReportInfo>();
		Set<TableInfo> tables = this.getAuthManager().getCompanyForLoggedInUser(this.request).getTables();
		for (TableInfo table : tables) {
			Set<BaseReportInfo> tableReports = this.getViewableReports(table, user);
			reports.addAll(tableReports);
		}
		return reports;
	}
	
	public TableInfo getTable(String tableID) throws ObjectNotFoundException, DisallowedException {
		return this.databaseDefn.getTable(this.request, tableID);
	}

	public TableInfo findTableContainingReport(String reportInternalName)
			throws ObjectNotFoundException, DisallowedException {
		return this.databaseDefn.findTableContainingReport(this.request, reportInternalName);
	}

	public Set<TableInfo> getDependentTables(TableInfo baseTable) throws ObjectNotFoundException {
		Set<TableInfo> dependentTables = new LinkedHashSet<TableInfo>();
		this.databaseDefn.getDependentTables(baseTable, dependentTables, this.request);
		return dependentTables;
	}

	public Set<TableInfo> getDependentTables() throws ObjectNotFoundException {
		TableInfo baseTable = this.sessionData.getTable();
		if (baseTable == null) {
			throw new ObjectNotFoundException(
					"Can't select dependent tables prior to table selection");
		}
		Set<TableInfo> dependentTables = new LinkedHashSet<TableInfo>();
		this.databaseDefn.getDependentTables(baseTable, dependentTables, this.request);
		return dependentTables;
	}

	public String getApplicationName() {
		return AppProperties.applicationName;
	}

	public String getApplicationVersion() {
		return AppProperties.applicationVersion;
	}

	public List<RelationField> getUnchosenRelationFields() throws DisallowedException {
		return this.getUnchosenRelationFields(this.sessionData.getTable());
	}

	public List<RelationField> getUnchosenRelationFields(TableInfo table)
			throws DisallowedException {
		if (!this.getAuthenticator().loggedInUserAllowedTo(this.request,
				PrivilegeType.VIEW_TABLE_DATA, table)) {
			throw new DisallowedException(PrivilegeType.VIEW_TABLE_DATA, table);
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

	public Map<BaseField, BaseValue> getTableDataRow() throws DisallowedException,
			ObjectNotFoundException, SQLException, CantDoThatException, CodingErrorException {
		return getTableDataRow(this.sessionData.getTable());
	}

	public Map<BaseField, BaseValue> getTableDataRow(TableInfo table) throws DisallowedException,
			ObjectNotFoundException, SQLException, CantDoThatException, CodingErrorException {
		if (!this.getAuthenticator().loggedInUserAllowedTo(this.request,
				PrivilegeType.VIEW_TABLE_DATA, table)) {
			throw new DisallowedException(PrivilegeType.VIEW_TABLE_DATA, table);
		}
		int rowId = this.sessionData.getRowId(table);
		return this.getTableDataRow(table, rowId);
	}

	public Map<BaseField, BaseValue> getTableDataRow(TableInfo table, int rowId)
			throws DisallowedException, ObjectNotFoundException, SQLException, CantDoThatException,
			CodingErrorException {
		if (!this.getAuthenticator().loggedInUserAllowedTo(this.request,
				PrivilegeType.VIEW_TABLE_DATA, table)) {
			throw new DisallowedException(PrivilegeType.VIEW_TABLE_DATA, table);
		}
		// If there was no error submitting the data
		if (this.ex == null) {
			// then we can get the values from the database
			return this.databaseDefn.getDataManagement().getTableDataRow(table, rowId);
		} else {
			// otherwise return the last values passed
			return this.sessionData.getFieldInputValues();
		}
	}

	public Set<Integer> getRelatedRowIds(int masterRowId, TableInfo relatedTable)
			throws DisallowedException, CantDoThatException, SQLException, CodingErrorException {
		BaseReportInfo sessionReport = this.sessionData.getReport();
		if (!this.getAuthenticator().loggedInUserAllowedToViewReport(this.request, sessionReport)) {
			throw new DisallowedException(PrivilegeType.VIEW_TABLE_DATA,
					sessionReport.getParentTable());
		}
		return this.databaseDefn.getDataManagement().getRelatedRowIds(sessionReport, masterRowId,
				relatedTable);
	}

	public Set<Integer> getRelatedRowIds(BaseReportInfo masterReport, int masterRowId,
			TableInfo relatedTable) throws DisallowedException, CantDoThatException, SQLException,
			CodingErrorException {
		if (!this.getAuthenticator().loggedInUserAllowedToViewReport(this.request, masterReport)) {
			throw new DisallowedException(PrivilegeType.VIEW_TABLE_DATA,
					masterReport.getParentTable());
		}
		return this.databaseDefn.getDataManagement().getRelatedRowIds(masterReport, masterRowId,
				relatedTable);
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
		Map<BaseField, String> reportFilterValues = this.sessionData.getReportFilterValues();
		return this.getReportDataRows(report, rowLimit, reportFilterValues, false);
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
		CompanyInfo company = this.databaseDefn.getAuthManager().getCompanyForLoggedInUser(
				this.request);
		List<DataRowInfo> reportDataRows = this.databaseDefn.getDataManagement().getReportDataRows(
				company, report, reportFilterValues, exactFilters, sessionReportSorts, rowLimit);
		UsageLogger usageLogger = new UsageLogger(this.databaseDefn.getDataSource());
		AppUserInfo user = this.databaseDefn.getAuthManager().getUserByUserName(this.request,
				this.request.getRemoteUser());
		usageLogger.logReportView(user, report, reportFilterValues, rowLimit);
		UsageLogger.startLoggingThread(usageLogger);
		return reportDataRows;
	}

	/**
	 * @see WikiManagementInfo#getWikiRecordDataRows(CompanyInfo, String,
	 *      String)
	 */
	public List<WikiRecordDataRowInfo> getWikiRecordDataRows(String pageNameFilter,
			String pageContentFilter) throws CantDoThatException, SQLException,
			DisallowedException, ObjectNotFoundException {
		AppUserInfo user = this.databaseDefn.getAuthManager().getUserByUserName(this.request,
				this.request.getRemoteUser());
		CompanyInfo company = user.getCompany();
		return this.databaseDefn.getWikiManagement(company).getWikiRecordDataRows(pageNameFilter,
				pageContentFilter);
	}

	/**
	 * Given a report, throws a DisallowedException if the user isn't allowed to
	 * view that report
	 */
	private void checkReportViewPrivileges(BaseReportInfo report) throws DisallowedException,
			CodingErrorException {
		if (!this.getAuthenticator().loggedInUserAllowedToViewReport(this.request, report)) {
			logger.warn("Report " + report + " is not viewable by user "
					+ this.request.getRemoteUser());
			throw new DisallowedException(PrivilegeType.VIEW_TABLE_DATA, report.getParentTable());
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

	public ReportSummaryDataInfo getReportSummaryData() throws DisallowedException, SQLException,
			ObjectNotFoundException, CodingErrorException, CantDoThatException {
		BaseReportInfo report = this.sessionData.getReport();
		return this.getReportSummaryData(report.getReportSummary());
	}

	public ReportSummaryDataInfo getReportSummaryData(ReportSummaryInfo reportSummary)
			throws DisallowedException, SQLException, ObjectNotFoundException,
			CodingErrorException, CantDoThatException {
		return this.getReportSummaryData(reportSummary, false);
	}

	public ReportSummaryDataInfo getCachedReportSummaryData(ReportSummaryInfo reportSummary)
			throws DisallowedException, ObjectNotFoundException, CodingErrorException,
			CantDoThatException, SQLException {
		return this.getReportSummaryData(reportSummary, true);
	}

	private ReportSummaryDataInfo getReportSummaryData(ReportSummaryInfo reportSummary,
			boolean useCache) throws DisallowedException, SQLException, ObjectNotFoundException,
			CodingErrorException, CantDoThatException {
		// Check privileges for all tables from which data in the report is
		// displayed from, throw
		// DisallowedException if privileges not sufficient
		this.checkReportViewPrivileges(reportSummary.getReport());
		ReportSummaryDataInfo reportSummaryData;
		CompanyInfo company = this.databaseDefn.getAuthManager().getCompanyForLoggedInUser(
				this.request);
		Map<BaseField, String> reportFilterValues = this.sessionData.getReportFilterValues();
		reportSummaryData = this.databaseDefn.getDataManagement().getReportSummaryData(company,
				reportSummary, reportFilterValues, useCache);
		return reportSummaryData;
	}

	public ReportSummaryDataInfo getFieldSummaryData(ReportFieldInfo reportField)
			throws DisallowedException, SQLException, CodingErrorException,
			ObjectNotFoundException, CantDoThatException {
		BaseReportInfo report = reportField.getParentReport();
		this.checkReportViewPrivileges(report);
		ReportSummaryInfo reportSummary = new ReportSummaryDefn(report, reportField.getFieldName(),
				false);
		BaseField field = reportField.getBaseField();
		FieldCategory fieldCategory = field.getFieldCategory();
		if (fieldCategory.equals(FieldCategory.NUMBER)) {
			reportSummary.addFunction(new ReportSummaryAggregateDefn(AggregateFunction.SUM,
					reportField));
			reportSummary.addFunction(new ReportSummaryAggregateDefn(AggregateFunction.AVG,
					reportField));
		} else if (fieldCategory.equals(FieldCategory.TEXT)) {
			if (((TextField) field).usesLookup()) {
				reportSummary.addGrouping(reportField, null);
				reportSummary.addFunction(new ReportSummaryAggregateDefn(AggregateFunction.COUNT,
						report.getReportField(report.getParentTable().getPrimaryKey()
								.getInternalFieldName())));
			} else if (!field.getTableContainingField().equals(report.getParentTable())) {
				reportSummary.addGrouping(reportField, null);
				reportSummary.addFunction(new ReportSummaryAggregateDefn(AggregateFunction.COUNT,
						report.getReportField(report.getParentTable().getPrimaryKey()
								.getInternalFieldName())));
			}
		}
		Map<BaseField, String> filters = this.sessionData.getReportFilterValues();
		CompanyInfo company = this.databaseDefn.getAuthManager().getCompanyForLoggedInUser(
				this.request);
		return this.databaseDefn.getDataManagement().getReportSummaryData(company, reportSummary,
				filters, false);
	}

	public SortedSet<TagInfo> getReportTagCloud(int minWeight, int maxWeight, int maxTags)
			throws ObjectNotFoundException, DisallowedException, CodingErrorException,
			CantDoThatException, SQLException {
		BaseReportInfo report = this.sessionData.getReport();
		if (report == null) {
			throw new ObjectNotFoundException("There's no report in the session to view");
		}
		Set<BaseField> reportBaseFields = report.getReportBaseFields();
		Set<String> stopWords = new HashSet<String>();
		return this.getReportTagCloud(report, reportBaseFields, stopWords, minWeight, maxWeight,
				maxTags);
	}

	public SortedSet<TagInfo> getReportTagCloud(BaseReportInfo report, ReportFieldInfo reportField,
			Set<String> stopWords, int minWeight, int maxWeight, int maxTags)
			throws ObjectNotFoundException, DisallowedException, CodingErrorException,
			CantDoThatException, SQLException {
		Set<BaseField> fields = new HashSet<BaseField>();
		fields.add(reportField.getBaseField());
		return this.getReportTagCloud(report, fields, stopWords, minWeight, maxWeight, maxTags);
	}

	private SortedSet<TagInfo> getReportTagCloud(BaseReportInfo report,
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
			return new TreeSet<TagInfo>();
		}
		String conglomoratedText = this.databaseDefn.getDataManagement().getReportDataText(report,
				textFields, 1000000);
		TagCloud cloud = new TagCloud(conglomoratedText, minWeight, maxWeight, maxTags, stopWords);
		return cloud.getTags();
	}

	public SortedMap<TableInfo, SortedSet<BaseField>> adminGetRelationCandidates()
			throws DisallowedException, ObjectNotFoundException {
		TableInfo table = this.sessionData.getTable();
		if (!(getAuthenticator().loggedInUserAllowedTo(this.request, PrivilegeType.MANAGE_TABLE,
				table))) {
			throw new DisallowedException(PrivilegeType.MANAGE_TABLE, table);
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
		return getAuthManager().getUsers(this.request);
	}

	public SortedSet<AppRoleInfo> adminGetRoles() throws DisallowedException,
			ObjectNotFoundException {
		return getAuthManager().getRoles(this.request);
	}

	public SortedSet<AppRoleInfo> adminGetRolesForUser(AppUserInfo user) throws DisallowedException {
		return getAuthManager().getRolesForUser(this.request, user);
	}

	public EnumSet<PrivilegeType> adminGetPrivilegeTypes() throws DisallowedException {
		return getAuthManager().getPrivilegeTypes(this.request);
	}

	public boolean loggedInUserAllowedTo(String privilegeTypeToCheck)
			throws IllegalArgumentException {
		PrivilegeType privilegeType = PrivilegeType.valueOf(privilegeTypeToCheck.toUpperCase());
		return getAuthenticator().loggedInUserAllowedTo(this.request, privilegeType);
	}

	public boolean loggedInUserAllowedTo(String privilegeTypeToCheck, TableInfo table)
			throws IllegalArgumentException {
		PrivilegeType privilegeType = PrivilegeType.valueOf(privilegeTypeToCheck.toUpperCase());
		return getAuthenticator().loggedInUserAllowedTo(this.request, privilegeType, table);
	}

	public boolean loggedInUserAllowedToViewReport(BaseReportInfo report)
			throws CodingErrorException {
		return getAuthenticator().loggedInUserAllowedToViewReport(this.request, report);
	}

	public boolean userHasPrivilege(String privilegeTypeToCheck) throws DisallowedException,
			IllegalArgumentException, ObjectNotFoundException {
		PrivilegeType privilegeType = PrivilegeType.valueOf(privilegeTypeToCheck.toUpperCase());
		AppUserInfo sessionUser = this.sessionData.getUser();
		return getAuthManager().specifiedUserHasPrivilege(this.request, privilegeType, sessionUser);
	}

	public boolean userHasPrivilege(String privilegeTypeToCheck, TableInfo table)
			throws IllegalArgumentException, ObjectNotFoundException, DisallowedException {
		PrivilegeType privilegeType = PrivilegeType.valueOf(privilegeTypeToCheck.toUpperCase());
		AppUserInfo sessionUser = this.sessionData.getUser();
		return getAuthManager().specifiedUserHasPrivilege(this.request, privilegeType, sessionUser,
				table);
	}

	public boolean userHasPrivilege(AppUserInfo user, String privilegeTypeToCheck)
			throws DisallowedException {
		PrivilegeType privilegeType = PrivilegeType.valueOf(privilegeTypeToCheck.toUpperCase());
		return getAuthManager().specifiedUserHasPrivilege(this.request, privilegeType, user);
	}

	public boolean userHasPrivilege(AppUserInfo user, String privilegeTypeToCheck, TableInfo table)
			throws DisallowedException {
		PrivilegeType privilegeType = PrivilegeType.valueOf(privilegeTypeToCheck.toUpperCase());
		boolean hasPrivilege = getAuthManager().specifiedUserHasPrivilege(this.request,
				privilegeType, user, table);
		return hasPrivilege;
	}

	public boolean roleHasPrivilege(String privilegeTypeToCheck) throws IllegalArgumentException,
			DisallowedException {
		PrivilegeType privilegeType = PrivilegeType.valueOf(privilegeTypeToCheck.toUpperCase());
		AppRoleInfo sessionRole = this.sessionData.getRole();
		return getAuthManager().specifiedRoleHasPrivilege(this.request, privilegeType, sessionRole);
	}

	public boolean roleHasPrivilege(String privilegeTypeToCheck, TableInfo table)
			throws IllegalArgumentException, DisallowedException {
		PrivilegeType privilegeType = PrivilegeType.valueOf(privilegeTypeToCheck.toUpperCase());
		AppRoleInfo sessionRole = this.sessionData.getRole();
		return getAuthManager().specifiedRoleHasPrivilege(this.request, privilegeType, sessionRole,
				table);
	}

	public boolean roleHasPrivilege(AppRoleInfo role, String privilegeTypeToCheck)
			throws IllegalArgumentException, DisallowedException {
		PrivilegeType privilegeType = PrivilegeType.valueOf(privilegeTypeToCheck.toUpperCase());
		return getAuthManager().specifiedRoleHasPrivilege(this.request, privilegeType, role);
	}

	public boolean roleHasPrivilege(AppRoleInfo role, String privilegeTypeToCheck, TableInfo table)
			throws IllegalArgumentException, DisallowedException {
		PrivilegeType privilegeType = PrivilegeType.valueOf(privilegeTypeToCheck.toUpperCase());
		return getAuthManager().specifiedRoleHasPrivilege(this.request, privilegeType, role, table);
	}

	public List<JoinClauseInfo> getCandidateJoins(SimpleReportInfo report)
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
		String username = this.request.getRemoteUser();
		return getAuthManager().getUserByUserName(this.request, username);
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
			sourceText = sourceText.replaceAll(table.getInternalTableName(), table.getTableName());
			for (BaseReportInfo report : table.getReports()) {
				sourceText = sourceText.replaceAll(report.getInternalReportName(),
						report.getReportName());
				for (BaseField field : report.getReportBaseFields()) {
					sourceText = sourceText.replaceAll(field.getInternalFieldName(),
							field.getFieldName());
				}
			}
		}
		return sourceText;
	}

	public int getUploadSpeed() {
		return this.databaseDefn.getDataManagement().getUploadSpeed();
	}

	private String toolbarPluginName = null;

	private final HttpServletRequest request;

	private SessionDataInfo sessionData; // Should be final if possible too

	private final DatabaseInfo databaseDefn;

	private Map<String, List<ModuleActionInfo>> moduleActions = new HashMap<String, List<ModuleActionInfo>>();

	private boolean whetherExceptionOccurred = false;

	private Exception ex = null;

	private static final SimpleLogger logger = new SimpleLogger(DataManagement.class);

}