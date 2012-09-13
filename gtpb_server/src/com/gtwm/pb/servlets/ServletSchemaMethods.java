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
package com.gtwm.pb.servlets;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.lang.StringUtils;
import org.grlea.log.SimpleLogger;
import org.hibernate.HibernateException;
import com.gtwm.pb.auth.Company;
import com.gtwm.pb.auth.DisallowedException;
import com.gtwm.pb.auth.PrivilegeType;
import com.gtwm.pb.model.interfaces.AppUserInfo;
import com.gtwm.pb.model.interfaces.FormTabInfo;
import com.gtwm.pb.model.interfaces.ModuleInfo;
import com.gtwm.pb.model.interfaces.ChartAggregateInfo;
import com.gtwm.pb.model.interfaces.AuthManagerInfo;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.CompanyInfo;
import com.gtwm.pb.model.interfaces.JoinClauseInfo;
import com.gtwm.pb.model.interfaces.ReportCalcFieldInfo;
import com.gtwm.pb.model.interfaces.ReportFieldInfo;
import com.gtwm.pb.model.interfaces.ReportFilterInfo;
import com.gtwm.pb.model.interfaces.ReportSortInfo;
import com.gtwm.pb.model.interfaces.ChartInfo;
import com.gtwm.pb.model.interfaces.SessionDataInfo;
import com.gtwm.pb.model.interfaces.SimpleReportInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.DatabaseInfo;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.fields.DateField;
import com.gtwm.pb.model.interfaces.fields.DecimalField;
import com.gtwm.pb.model.interfaces.fields.RelationField;
import com.gtwm.pb.model.interfaces.fields.TextField;
import com.gtwm.pb.model.interfaces.fields.IntegerField;
import com.gtwm.pb.model.manageSchema.FormTab;
import com.gtwm.pb.model.manageSchema.JoinClause;
import com.gtwm.pb.model.manageSchema.JoinType;
import com.gtwm.pb.model.manageSchema.ReportCalcFieldDefn;
import com.gtwm.pb.model.manageSchema.ReportFilterDefn;
import com.gtwm.pb.model.manageSchema.ReportSort;
import com.gtwm.pb.model.manageSchema.ChartAggregateDefn;
import com.gtwm.pb.model.manageSchema.BooleanFieldDescriptorOption.PossibleBooleanOptions;
import com.gtwm.pb.model.manageSchema.FieldTypeDescriptor.FieldCategory;
import com.gtwm.pb.model.manageSchema.ListFieldDescriptorOption.PossibleListOptions;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.Enumerations.FormStyle;
import com.gtwm.pb.util.Enumerations.ReportStyle;
import com.gtwm.pb.util.Enumerations.SummaryFilter;
import com.gtwm.pb.util.Enumerations.TextCase;
import com.gtwm.pb.util.Helpers;
import com.gtwm.pb.util.HibernateUtil;
import com.gtwm.pb.util.HttpRequestUtil;
import com.gtwm.pb.util.InconsistentStateException;
import com.gtwm.pb.util.MissingParametersException;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.AgileBaseException;
import com.gtwm.pb.util.TableDependencyException;
import com.gtwm.pb.util.Enumerations.DatabaseFieldType;
import com.gtwm.pb.util.Enumerations.FilterType;
import com.gtwm.pb.util.Enumerations.AggregateFunction;
import com.gtwm.pb.util.Enumerations.SummaryGroupingModifier;
import com.gtwm.pb.model.manageSchema.TextFieldDescriptorOption.PossibleTextOptions;

/**
 * Methods to do with the schema (editing companies, tables, fields etc.) to be
 * used by the main agileBase servlet AppController, or any other custom servlet
 * written for a particular application based on agileBase. The JavaDoc here
 * describes the HTTP requests that must be sent to use the methods.
 * 
 * Part of a set of three classes, ServletSchemaMethods to manage setting up the
 * database schema, ServletDataMethods to manage data editing and
 * ServletAuthMethods to do with users, roles and privileges
 * 
 * @see ServletDataMethods
 * @see ServletAuthMethods
 */
public final class ServletSchemaMethods {

	/**
	 * Add a new company which will be able to have its own private set of
	 * tables
	 * 
	 * Http usage example:
	 * 
	 * &add_company=true&companyname=GTWM
	 * 
	 * @throws DisallowedException
	 *             If the currently logged in user doesn't have MASTER
	 *             privileges
	 */
	public synchronized static void addCompany(HttpServletRequest request, DatabaseInfo databaseDefn)
			throws DisallowedException, MissingParametersException, CantDoThatException,
			CodingErrorException, SQLException {
		String companyName = request.getParameter("companyname");
		if (companyName == null) {
			throw new MissingParametersException("'companyname' is required to add a company");
		}
		try {
			HibernateUtil.startHibernateTransaction();
			CompanyInfo company = new Company(companyName);
			HibernateUtil.currentSession().save(company);
			AuthManagerInfo authManager = databaseDefn.getAuthManager();
			authManager.addCompany(request, company);
			// Create an initial module
			ModuleInfo module = databaseDefn.addModule(request, company);
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (Exception ex) {
			HibernateUtil.rollbackHibernateTransaction();
			throw new CantDoThatException("Company addition failed", ex);
		} finally {
			HibernateUtil.closeSession();
		}
	}

	public synchronized static void removeCompany(HttpServletRequest request,
			AuthManagerInfo authManager) throws DisallowedException, MissingParametersException,
			SQLException, ObjectNotFoundException, CodingErrorException, CantDoThatException {
		String internalCompanyName = request.getParameter("internalcompanyname");
		if (internalCompanyName == null) {
			throw new MissingParametersException(
					"'internalcompanyname' is required to remove a company");
		}
		CompanyInfo company = authManager.getCompanyByInternalName(request, internalCompanyName);
		HibernateUtil.startHibernateTransaction();
		try {
			authManager.removeCompany(request, company);
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (HibernateException hex) {
			HibernateUtil.rollbackHibernateTransaction();
			// TODO? rollback memory

			throw new CantDoThatException("Company removal failed", hex);
		} catch (AgileBaseException pbex) {
			HibernateUtil.rollbackHibernateTransaction();
			// TODO? rollback memory

			throw new CantDoThatException("Company removal failed", pbex);
		} finally {
			HibernateUtil.closeSession();
		}
	}

	public synchronized static void addModule(HttpServletRequest request,
			SessionDataInfo sessionData, DatabaseInfo databaseDefn) throws AgileBaseException,
			SQLException {
		AuthManagerInfo authManager = databaseDefn.getAuthManager();
		CompanyInfo company = authManager.getCompanyForLoggedInUser(request);
		ModuleInfo newModule = null;
		try {
			HibernateUtil.startHibernateTransaction();
			newModule = databaseDefn.addModule(request, company);
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (HibernateException hex) {
			HibernateUtil.currentSession().getTransaction().rollback();
			company.removeModule(newModule);
			throw new CantDoThatException("Module addition failed", hex);
		} finally {
			HibernateUtil.closeSession();
		}
		sessionData.setModule(newModule);
	}

	// TODO: move body to DatabaseDefn, like addModule
	public synchronized static void removeModule(HttpServletRequest request,
			SessionDataInfo sessionData, DatabaseInfo databaseDefn) throws AgileBaseException {
		CompanyInfo company = databaseDefn.getAuthManager().getCompanyForLoggedInUser(request);
		String internalModuleName = request.getParameter("internalmodulename");
		if (internalModuleName == null) {
			throw new MissingParametersException(
					"internalmodulename is required to remove a module");
		}
		ModuleInfo module = company.getModuleByInternalName(internalModuleName);
		// Check that the module hasn't got any reports in it
		Set<TableInfo> tables = company.getTables();
		SortedSet<BaseReportInfo> memberReports = new TreeSet<BaseReportInfo>();
		for (TableInfo table : tables) {
			for (BaseReportInfo report : table.getReports()) {
				ModuleInfo reportModule = report.getModule();
				if (reportModule != null) {
					if (reportModule.equals(module)) {
						memberReports.add(report);
					}
				}
			}
		}
		if (memberReports.size() > 0) {
			throw new CantDoThatException("The module " + module + " still contains reports "
					+ memberReports);
		}
		try {
			HibernateUtil.startHibernateTransaction();
			HibernateUtil.activateObject(company);
			company.removeModule(module);
			HibernateUtil.currentSession().delete(module);
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (HibernateException hex) {
			HibernateUtil.currentSession().getTransaction().rollback();
			company.addModule(module);
			throw new CantDoThatException("module removal failed", hex);
		} finally {
			HibernateUtil.closeSession();
		}
		sessionData.setModule(null);
	}

	// TODO: move body to DatabaseDefn, like addModule
	public synchronized static void updateModule(HttpServletRequest request,
			SessionDataInfo sessionData, AuthManagerInfo authManager) throws AgileBaseException {
		CompanyInfo company = authManager.getCompanyForLoggedInUser(request);
		String internalModuleName = request.getParameter("internalmodulename");
		String moduleName = request.getParameter("modulename");
		String iconPath = request.getParameter("iconpath");
		String indexString = request.getParameter("indexnumber");
		String colour = request.getParameter("colour");
		String section = request.getParameter("section");
		Integer indexNumber = null;
		if (indexString != null) {
			indexNumber = Integer.valueOf(indexString);
		}
		if ((moduleName == null) && (iconPath == null) && (indexString == null) && (colour == null)
				&& (section == null)) {
			throw new MissingParametersException(
					"At least one of modulename, iconpath, colour, section or indexnumber are required to update a module");
		}
		// Look up module
		ModuleInfo module = null;
		if (internalModuleName != null) {
			Set<ModuleInfo> modules = company.getModules();
			for (ModuleInfo testModule : modules) {
				if (testModule.getInternalModuleName().equals(internalModuleName)) {
					module = testModule;
					break;
				}
			}
		}
		if (module == null) {
			module = sessionData.getModule();
		}
		if (module == null) {
			throw new ObjectNotFoundException("No module found in session or with internal name "
					+ internalModuleName);
		}
		String originalModuleName = module.getModuleName();
		String originalIconPath = module.getIconPath();
		int originalIndexNumber = module.getIndexNumber();
		String originalColour = module.getColour();
		String originalSection = module.getSection();
		try {
			HibernateUtil.startHibernateTransaction();
			HibernateUtil.activateObject(module);
			if (moduleName != null) {
				module.setModuleName(moduleName);
			}
			if (iconPath != null) {
				module.setIconPath(iconPath);
			}
			if (indexNumber != null) {
				module.setIndexNumber(indexNumber);
			}
			if (colour != null) {
				module.setColour(colour);
			}
			if (section != null) {
				module.setSection(section);
			}
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (HibernateException hex) {
			HibernateUtil.currentSession().getTransaction().rollback();
			module.setModuleName(originalModuleName);
			module.setIconPath(originalIconPath);
			module.setIndexNumber(originalIndexNumber);
			module.setColour(originalColour);
			module.setSection(originalSection);
			throw new CantDoThatException("module removal failed", hex);
		} finally {
			HibernateUtil.closeSession();
		}
	}

	public synchronized static void addTable(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn) throws DisallowedException,
			SQLException, CantDoThatException, ObjectNotFoundException, CodingErrorException {
		String internalTableName = request.getParameter("internaltablename");
		String internalDefaultReportName = request.getParameter("internaldefaultreportname");
		String internalPrimaryKeyName = request.getParameter("internalprimarykeyname");
		String baseTableName = request.getParameter("tablename");
		String tableName = baseTableName;
		if (baseTableName == null) {
			baseTableName = "New Table";
			tableName = baseTableName;
			// Make sure table name is unique in the company
			CompanyInfo company = databaseDefn.getAuthManager().getCompanyForLoggedInUser(request);
			SortedSet<TableInfo> tables = company.getTables();
			Set<String> existingTableNames = new HashSet<String>();
			for (TableInfo existingTable : tables) {
				existingTableNames.add(existingTable.getTableName());
			}
			int tableCount = 0;
			while (existingTableNames.contains(tableName)) {
				tableCount++;
				tableName = baseTableName + " " + String.valueOf(tableCount);
			}
		}
		String tableDesc = request.getParameter("tabledesc");
		if (tableDesc == null) {
			tableDesc = "";
		}
		// begin updating model and persisting changes
		TableInfo newTable = null; // use this to store the new table as the
		// session table
		Connection conn = null;
		try {
			HibernateUtil.startHibernateTransaction();
			conn = databaseDefn.getDataSource().getConnection();
			conn.setAutoCommit(false);
			// create the new table
			newTable = databaseDefn.addTable(sessionData, request, conn, internalTableName,
					internalDefaultReportName, tableName, internalPrimaryKeyName, tableDesc);
			// add permissions on the new table
			databaseDefn.setDefaultTablePrivileges(request, newTable);
			conn.commit();
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (SQLException sqlex) {
			rollbackConnections(conn);
			CompanyInfo company = databaseDefn.getAuthManager().getCompanyForLoggedInUser(request);
			company.removeTable(newTable);
			databaseDefn.getAuthManager().removePrivilegesOnTable(request, newTable);
			throw new CantDoThatException("table addition failed", sqlex);
		} catch (HibernateException hex) {
			rollbackConnections(conn);
			CompanyInfo company = databaseDefn.getAuthManager().getCompanyForLoggedInUser(request);
			company.removeTable(newTable);
			databaseDefn.getAuthManager().removePrivilegesOnTable(request, newTable);
			throw new CantDoThatException("table addition failed", hex);
		} catch (AgileBaseException abex) {
			rollbackConnections(conn);
			CompanyInfo company = databaseDefn.getAuthManager().getCompanyForLoggedInUser(request);
			company.removeTable(newTable);
			databaseDefn.getAuthManager().removePrivilegesOnTable(request, newTable);
			throw new CantDoThatException("table addition failed", abex);
		} finally {
			if (conn != null) {
				conn.close();
			}
			HibernateUtil.closeSession();
		}
		if (newTable != null) {
			sessionData.setTable(newTable);
		}
	}

	public synchronized static void setTableForm(HttpServletRequest request,
			SessionDataInfo sessionData, DatabaseInfo databaseDefn) throws DisallowedException,
			MissingParametersException, ObjectNotFoundException {
		TableInfo table = ServletUtilMethods.getTableForRequest(sessionData, request, databaseDefn,
				true);
		AuthManagerInfo authManager = databaseDefn.getAuthManager();
		if (!authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, table)) {
			throw new DisallowedException(authManager.getLoggedInUser(request),
					PrivilegeType.MANAGE_TABLE, table);
		}
		String formInternalTableName = request.getParameter("forminternaltablename");
		if (formInternalTableName == null) {
			throw new MissingParametersException(
					"forminternaltablename is required to set a table form");
		}
		TableInfo formTable = null;
		if (!formInternalTableName.equals("")) {
			formTable = databaseDefn.getTable(request, formInternalTableName);
		}
		try {
			HibernateUtil.startHibernateTransaction();
			HibernateUtil.activateObject(table);
			table.setFormTable(formTable);
			HibernateUtil.currentSession().getTransaction().commit();
		} finally {
			HibernateUtil.closeSession();
		}
	}

	public synchronized static void addFormTab(HttpServletRequest request,
			SessionDataInfo sessionData, DatabaseInfo databaseDefn) throws DisallowedException,
			MissingParametersException, ObjectNotFoundException {
		TableInfo table = ServletUtilMethods.getTableForRequest(sessionData, request, databaseDefn,
				true);
		AuthManagerInfo authManager = databaseDefn.getAuthManager();
		if (!authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, table)) {
			throw new DisallowedException(authManager.getLoggedInUser(request),
					PrivilegeType.MANAGE_TABLE, table);
		}
		String tabTableId = request.getParameter("tabtable");
		if (tabTableId == null) {
			throw new MissingParametersException(
					"tabtable must be supplied to add a form tab to a table");
		}
		TableInfo tabTable = databaseDefn.getTable(request, tabTableId);
		SortedSet<FormTabInfo> formTabs = table.getFormTabs();
		int newIndex = 0;
		if (formTabs.size() > 0) {
			newIndex = formTabs.last().getIndex() + 1;
		}
		FormTabInfo formTab = new FormTab(table, tabTable, newIndex);
		try {
			HibernateUtil.startHibernateTransaction();
			HibernateUtil.currentSession().save(formTab);
			HibernateUtil.activateObject(table);
			table.addFormTab(formTab);
			HibernateUtil.currentSession().getTransaction().commit();
		} finally {
			HibernateUtil.closeSession();
		}
	}

	public synchronized static void removeFormTab(HttpServletRequest request,
			SessionDataInfo sessionData, DatabaseInfo databaseDefn) throws DisallowedException,
			MissingParametersException, ObjectNotFoundException {
		TableInfo table = ServletUtilMethods.getTableForRequest(sessionData, request, databaseDefn,
				true);
		AuthManagerInfo authManager = databaseDefn.getAuthManager();
		if (!authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, table)) {
			throw new DisallowedException(authManager.getLoggedInUser(request),
					PrivilegeType.MANAGE_TABLE, table);
		}
		String tabTableId = request.getParameter("tabtable");
		TableInfo tabTable = databaseDefn.getTable(request, tabTableId);
		FormTabInfo formTab = null;
		TABS_LOOP: for (FormTabInfo testFormTab : table.getFormTabs()) {
			if (testFormTab.getTable().equals(tabTable)) {
				formTab = testFormTab;
				break TABS_LOOP;
			}
		}
		if (formTab == null) {
			throw new ObjectNotFoundException("A tab for table " + tabTableId
					+ " was not found in table " + table);
		}
		try {
			HibernateUtil.startHibernateTransaction();
			HibernateUtil.activateObject(table);
			table.removeFormTab(formTab);
			HibernateUtil.currentSession().delete(formTab);
			HibernateUtil.currentSession().getTransaction().commit();
		} finally {
			HibernateUtil.closeSession();
		}
	}

	private static void rollbackConnections(Connection conn) {
		try {
			if (conn != null) {
				logger.error("rolling back sql...");
				conn.rollback();
				logger.error("sql successfully rolled back");
			}
		} catch (SQLException sqlex) {
			logger.error("oh no! another sql exception was thrown: " + sqlex);
			sqlex.printStackTrace();
			// don't rethrow, may just be because no SQL has been sent since
			// transaction start
		}
		logger.error("rolling back hibernate...");
		HibernateUtil.currentSession().getTransaction().rollback();
		logger.error("hibernate successfully rolled back");
	}

	public synchronized static void updateFormTab(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn)
			throws MissingParametersException, ObjectNotFoundException, DisallowedException,
			CantDoThatException {
		TableInfo table = ServletUtilMethods.getTableForRequest(sessionData, request, databaseDefn,
				ServletUtilMethods.USE_SESSION);
		AuthManagerInfo authManager = databaseDefn.getAuthManager();
		if (!authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, table)) {
			throw new DisallowedException(authManager.getLoggedInUser(request),
					PrivilegeType.MANAGE_TABLE, table);
		}
		String tabInternalTableName = request.getParameter("tabinternaltablename");
		TableInfo tabTable = databaseDefn.getTable(request, tabInternalTableName);
		FormTabInfo formTab = null;
		TABS_LOOP: for (FormTabInfo testFormTab : table.getFormTabs()) {
			if (testFormTab.getTable().equals(tabTable)) {
				formTab = testFormTab;
				break TABS_LOOP;
			}
		}
		if (formTab == null) {
			throw new ObjectNotFoundException("Table " + table + " doesn't contain a tab for "
					+ tabTable);
		}
		String tabInternalReportName = request.getParameter("tabinternalreportname");
		if (tabInternalReportName == null) {
			throw new MissingParametersException(
					"tabinternalreportname is necessary to update a form tab");
		}
		BaseReportInfo selectorReport = null;
		if (!tabInternalReportName.equals("")) {
			selectorReport = tabTable.getReport(tabInternalReportName);
		}
		try {
			HibernateUtil.startHibernateTransaction();
			HibernateUtil.activateObject(formTab);
			formTab.setSelectorReport(selectorReport);
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (HibernateException hex) {
			rollbackConnections(null);
			throw new CantDoThatException("Problem updating form tab: " + hex);
		} finally {
			HibernateUtil.closeSession();
		}
	}

	/**
	 * @throws CantDoThatException
	 *             If a table already exists with the table name you're trying
	 *             to rename to
	 * @throws MissingParametersException
	 *             If tablename and tabledesc both missing
	 * @throws ObjectNotFoundException
	 *             If there's no table in the session
	 * @throws DisallowedException
	 *             If the logged in user doesn't have MANAGE_TABLE privileges on
	 *             the table being altered
	 */
	public synchronized static void updateTable(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn) throws CantDoThatException,
			MissingParametersException, ObjectNotFoundException, DisallowedException, SQLException {
		// locate table to be amended
		TableInfo table = ServletUtilMethods.getTableForRequest(sessionData, request, databaseDefn,
				ServletUtilMethods.USE_SESSION);
		// load parameters for request
		String newTableName = request.getParameter("tablename");
		String newTableDesc = request.getParameter("tabledesc");
		String tableEmail = request.getParameter("tableemail");
		Boolean lockable = null;
		String lockableString = request.getParameter("lockable");
		if (lockableString != null) {
			lockable = Helpers.valueRepresentsBooleanTrue(lockableString);
		}
		Boolean tableFormPublic = null;
		String tableFormPublicString = request.getParameter("tableformpublic");
		if (tableFormPublicString != null) {
			tableFormPublic = Helpers.valueRepresentsBooleanTrue(tableFormPublicString);
		}
		FormStyle formStyle = null;
		String formStyleString = request.getParameter("formstyle");
		if (formStyleString != null) {
			formStyle = FormStyle.valueOf(formStyleString.toUpperCase());
		}
		boolean allowAutoDelete = table.getAllowAutoDelete();
		boolean oldAllowAutoDelete = allowAutoDelete;
		String allowAutoDeleteString = request.getParameter("allowautodelete");
		if (allowAutoDeleteString != null) {
			allowAutoDelete = Helpers.valueRepresentsBooleanTrue(allowAutoDeleteString);
		}
		boolean allowNotifications = table.getAllowNotifications();
		boolean oldAllowNotifications = allowNotifications;
		String allowNotificationsString = request.getParameter("allownotifications");
		if (allowNotificationsString != null) {
			allowNotifications = Helpers.valueRepresentsBooleanTrue(allowNotificationsString);
		}
		if (newTableName == null && newTableDesc == null && lockable == null
				&& tableFormPublic == null && tableEmail == null && formStyle == null
				&& allowAutoDeleteString == null && allowNotificationsString == null) {
			throw new MissingParametersException(
					"One or more table update parameter must be supplied to update a table");
		}
		// store current values that may be overwritten by
		// DatabaseDefn.updateTable so they can be rolled back
		String oldTableName = table.getTableName();
		String oldTableDesc = table.getTableDescription();
		boolean oldLockable = table.getRecordsLockable();
		boolean oldTableFormPublic = table.getTableFormPublic();
		String oldTableEmail = table.getEmail();
		FormStyle oldFormStyle = table.getFormStyle();
		// begin updating model and persisting changes
		Connection conn = null;
		try {
			HibernateUtil.startHibernateTransaction();
			conn = databaseDefn.getDataSource().getConnection();
			conn.setAutoCommit(false);
			// update the table:
			databaseDefn.updateTable(conn, request, table, newTableName, newTableDesc, lockable,
					tableFormPublic, tableEmail, formStyle, allowAutoDelete, allowNotifications);
			conn.commit();
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (HibernateException hex) {
			rollbackConnections(conn);
			// restore report properties
			table.setTableName(oldTableName);
			table.setTableDescription(oldTableDesc);
			table.setRecordsLockable(oldLockable);
			table.setTableFormPublic(oldTableFormPublic);
			table.setEmail(oldTableEmail);
			table.setFormStyle(oldFormStyle);
			table.setAllowAutoDelete(oldAllowAutoDelete);
			throw new CantDoThatException("Updating table failed", hex);
		} catch (SQLException sqlex) {
			rollbackConnections(conn);
			// restore report properties
			table.setTableName(oldTableName);
			table.setTableDescription(oldTableDesc);
			table.setRecordsLockable(oldLockable);
			table.setTableFormPublic(oldTableFormPublic);
			table.setEmail(oldTableEmail);
			table.setFormStyle(oldFormStyle);
			table.setAllowAutoDelete(oldAllowAutoDelete);
			throw new CantDoThatException("Updating table failed", sqlex);
		} catch (AgileBaseException pex) {
			rollbackConnections(conn);
			// restore report properties
			table.setTableName(oldTableName);
			table.setTableDescription(oldTableDesc);
			table.setRecordsLockable(oldLockable);
			table.setTableFormPublic(oldTableFormPublic);
			table.setEmail(oldTableEmail);
			table.setFormStyle(oldFormStyle);
			table.setAllowAutoDelete(oldAllowAutoDelete);
			throw new CantDoThatException("Updating table failed", pex);
		} finally {
			HibernateUtil.closeSession();
			if (conn != null) {
				conn.close();
			}
		}
	}

	public synchronized static void removeTable(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn) throws DisallowedException,
			SQLException, ObjectNotFoundException, MissingParametersException,
			TableDependencyException, CantDoThatException, CodingErrorException {
		// locate the table to be amended
		TableInfo tableToRemove = ServletUtilMethods.getTableForRequest(sessionData, request,
				databaseDefn, ServletUtilMethods.USE_SESSION);
		// begin updating model and persisting changes
		Connection conn = null;
		try {
			HibernateUtil.startHibernateTransaction();
			conn = databaseDefn.getDataSource().getConnection();
			conn.setAutoCommit(false);
			// remove the table
			databaseDefn.removeTable(sessionData, request, tableToRemove, conn);
			conn.commit();
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (SQLException sqlex) {
			CompanyInfo company = databaseDefn.getAuthManager().getCompanyForLoggedInUser(request);
			Set<TableInfo> tables = company.getTables();
			if (!tables.contains(tableToRemove)) {
				company.addTable(tableToRemove);
				databaseDefn.setDefaultTablePrivileges(request, tableToRemove);
			}
			rollbackConnections(conn);
			throw new CantDoThatException("table removal failed", sqlex);
		} catch (HibernateException hex) {
			CompanyInfo company = databaseDefn.getAuthManager().getCompanyForLoggedInUser(request);
			Set<TableInfo> tables = company.getTables();
			if (!tables.contains(tableToRemove)) {
				company.addTable(tableToRemove);
				databaseDefn.setDefaultTablePrivileges(request, tableToRemove);
			}
			rollbackConnections(conn);
			throw new CantDoThatException("table removal failed", hex);
		} catch (AgileBaseException pbex) {
			CompanyInfo company = databaseDefn.getAuthManager().getCompanyForLoggedInUser(request);
			Set<TableInfo> tables = company.getTables();
			if (!tables.contains(tableToRemove)) {
				company.addTable(tableToRemove);
				databaseDefn.setDefaultTablePrivileges(request, tableToRemove);
			}
			rollbackConnections(conn);
			throw new CantDoThatException("table removal failed. " + pbex.getMessage(), pbex);
		} finally {
			if (conn != null) {
				conn.close();
			}
			HibernateUtil.closeSession();
		}
		// If the table removed was the session table, set the session table to
		// null
		TableInfo sessionTable = sessionData.getTable();
		if (sessionTable != null) {
			if (tableToRemove.equals(sessionData.getTable())) {
				sessionData.setTable(null);
			}
		}
	}

	private synchronized static String generateNewReportName(TableInfo table) {
		String baseReportName = table.getSimpleName();
		String reportName = baseReportName;
		// Ensure report name is unique
		SortedSet<BaseReportInfo> reports = table.getReports();
		Set<String> existingReportNames = new HashSet<String>();
		for (BaseReportInfo existingReport : reports) {
			existingReportNames.add(existingReport.getReportName());
		}
		int reportCount = 0;
		while (existingReportNames.contains(reportName)) {
			reportCount++;
			reportName = baseReportName + " " + String.valueOf(reportCount);
		}
		return reportName;
	}

	public synchronized static void addReport(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn) throws DisallowedException,
			SQLException, ObjectNotFoundException, CantDoThatException, CodingErrorException,
			MissingParametersException {
		TableInfo table = ServletUtilMethods.getTableForRequest(sessionData, request, databaseDefn,
				ServletUtilMethods.USE_SESSION);
		String baseInternalReportName = request.getParameter("internalreportname");
		String reportName = request.getParameter("reportname");
		if (reportName == null) {
			reportName = generateNewReportName(table);
		}
		String reportDesc = request.getParameter("reportdesc");
		if (reportDesc == null) {
			reportDesc = "";
		}
		boolean populateReport = Helpers.valueRepresentsBooleanTrue(request
				.getParameter("populatereport"));
		// begin updating model and persisting changes
		BaseReportInfo newReport = null;
		Connection conn = null;
		try {
			HibernateUtil.startHibernateTransaction();
			conn = databaseDefn.getDataSource().getConnection();
			conn.setAutoCommit(false);
			newReport = databaseDefn.addReport(sessionData, request, conn, table,
					baseInternalReportName, reportName, reportDesc, populateReport);
			conn.commit();
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (SQLException sqlex) {
			rollbackConnections(conn);
			// remove report from memory
			if (newReport != null) {
				if (table.getReports().contains(newReport)) {
					table.removeReport(newReport);
				}
			}
			throw new CantDoThatException("report addition failed", sqlex);
		} catch (HibernateException hex) {
			rollbackConnections(conn);
			// remove report from memory
			if (newReport != null) {
				if (table.getReports().contains(newReport)) {
					table.removeReport(newReport);
				}
			}
			throw new CantDoThatException("report addition failed", hex);
		} catch (AgileBaseException pex) {
			rollbackConnections(conn);
			// remove report from memory
			if (newReport != null) {
				// TODO: Eclipse reports this as dead code, why?
				if (table.getReports().contains(newReport)) {
					table.removeReport(newReport);
				}
			}
			throw new CantDoThatException("report addition failed", pex);
		} finally {
			if (conn != null) {
				conn.close();
			}
			HibernateUtil.closeSession();
		}
		// change the session report to update pane 2 & 3
		sessionData.setReport(newReport);
	}

	// TODO: this upload could take significant time, think whether it's
	// possible to un-synchronize
	public synchronized static void uploadCustomReportTemplate(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn, List<FileItem> multipartItems)
			throws MissingParametersException, ObjectNotFoundException, DisallowedException,
			CantDoThatException {
		// TODO: use ServletUtilMethods.getReportForRequest but updating it for
		// multi-part form data
		BaseReportInfo report = sessionData.getReport();
		String templateName = ServletUtilMethods.getParameter(request, "templatename",
				multipartItems);
		try {
			databaseDefn.uploadCustomReportTemplate(request, report, templateName, multipartItems);
		} catch (FileUploadException fuex) {
			throw new CantDoThatException("template upload failed", fuex);
		}
	}

	public synchronized static void removeCustomReportTemplate(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn)
			throws MissingParametersException, DisallowedException, ObjectNotFoundException,
			CantDoThatException {
		BaseReportInfo report = sessionData.getReport();
		String templateName = request.getParameter("customtemplatename");
		if (templateName == null) {
			throw new MissingParametersException(
					"customtemplatename must be provided to remove a template");
		}
		databaseDefn.removeCustomReportTemplate(request, report, templateName);
	}

	public synchronized static void addDistinctToReport(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn) throws DisallowedException,
			MissingParametersException, ObjectNotFoundException, SQLException, CantDoThatException,
			CodingErrorException {
		SimpleReportInfo report = (SimpleReportInfo) ServletUtilMethods.getReportForRequest(
				sessionData, request, databaseDefn, true);
		String internalFieldName = request.getParameter("distinctinternalfieldname");
		if (internalFieldName == null) {
			throw new MissingParametersException(
					"distinctinternalfieldname must be provided to add a report DISTINCT clause");
		}
		BaseField distinctField = report.getReportField(internalFieldName).getBaseField();
		Connection conn = null;
		try {
			HibernateUtil.startHibernateTransaction();
			conn = databaseDefn.getDataSource().getConnection();
			conn.setAutoCommit(false);
			// update the report:
			databaseDefn.addDistinctToReport(request, conn, report, distinctField);
			HibernateUtil.currentSession().getTransaction().commit();
			conn.commit();
		} finally {
			if (conn != null) {
				conn.close();
				HibernateUtil.closeSession();
			}
		}
	}

	public synchronized static void removeDistinctFromReport(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn) throws DisallowedException,
			MissingParametersException, ObjectNotFoundException, SQLException, CantDoThatException,
			CodingErrorException {
		SimpleReportInfo report = (SimpleReportInfo) ServletUtilMethods.getReportForRequest(
				sessionData, request, databaseDefn, true);
		String internalFieldName = request.getParameter("distinctinternalfieldname");
		if (internalFieldName == null) {
			throw new MissingParametersException(
					"distinctinternalfieldname must be provided to remove a report DISTINCT clause");
		}
		BaseField distinctField = report.getReportField(internalFieldName).getBaseField();
		Connection conn = null;
		try {
			HibernateUtil.startHibernateTransaction();
			conn = databaseDefn.getDataSource().getConnection();
			conn.setAutoCommit(false);
			// update the report:
			databaseDefn.removeDistinctFromReport(request, conn, report, distinctField);
			HibernateUtil.currentSession().getTransaction().commit();
			conn.commit();
		} finally {
			if (conn != null) {
				conn.close();
				HibernateUtil.closeSession();
			}
		}
	}

	public synchronized static void updateReport(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn) throws CantDoThatException,
			MissingParametersException, ObjectNotFoundException, DisallowedException, SQLException {
		// get the report to be amended
		BaseReportInfo report = ServletUtilMethods.getReportForRequest(sessionData, request,
				databaseDefn, ServletUtilMethods.USE_SESSION);
		// load parameters from the request
		String newReportName = request.getParameter("reportname");
		String newReportDesc = request.getParameter("reportdesc");
		String internalModuleName = request.getParameter("internalmodulename");
		String reportStyleName = request.getParameter("reportstyle");
		String allowExportString = request.getParameter("allowexport");
		String memoryAllocationString = request.getParameter("memoryallocation");
		Integer memoryAllocation = null;
		if (memoryAllocationString != null) {
			memoryAllocation = Integer.valueOf(memoryAllocationString);
		}
		boolean allowExport = Helpers.valueRepresentsBooleanTrue(allowExportString);
		if (newReportName == null && newReportDesc == null && internalModuleName == null
				&& reportStyleName == null && allowExportString == null && memoryAllocation == null) {
			throw new MissingParametersException(
					"A reportname, reportdesc, reportstyle or internalmodulename parameter must be supplied to update a report with a new name or description");
		}
		CompanyInfo company = databaseDefn.getAuthManager().getCompanyForLoggedInUser(request);
		ModuleInfo module = null;
		if (internalModuleName != null) {
			if (!internalModuleName.equals("")) {
				module = company.getModuleByInternalName(internalModuleName);
			}
		}
		ReportStyle reportStyle = null;
		if (reportStyleName != null) {
			reportStyle = ReportStyle.valueOf(reportStyleName.toUpperCase());
		}
		// store current values that may be overwritten by
		// DatabaseDefn.updateReport so they can be rolled back
		String oldReportName = report.getReportName();
		String oldReportDesc = report.getReportDescription();
		ModuleInfo oldModule = report.getModule();
		ReportStyle oldReportStyle = report.getReportStyle();
		boolean oldAllowExport = report.getAllowExport();
		Integer oldMemoryAllocation = report.getMemoryAllocation();
		// begin updating model and persisting changes
		Connection conn = null;
		try {
			HibernateUtil.startHibernateTransaction();
			conn = databaseDefn.getDataSource().getConnection();
			conn.setAutoCommit(false);
			// update the report:
			databaseDefn.updateReport(conn, request, report, newReportName, newReportDesc, module,
					reportStyle, allowExport, memoryAllocation);
			HibernateUtil.currentSession().getTransaction().commit();
			conn.commit();
		} catch (SQLException sqlex) {
			rollbackConnections(conn);
			// restore report properties
			report.setReportName(oldReportName);
			report.setReportDescription(oldReportDesc);
			report.setModule(oldModule);
			report.setReportStyle(oldReportStyle);
			report.setAllowExport(oldAllowExport);
			report.setMemoryAllocation(oldMemoryAllocation);
			throw new CantDoThatException("Updating report failed", sqlex);
		} catch (HibernateException hex) {
			rollbackConnections(null);
			// restore report properties
			report.setReportName(oldReportName);
			report.setReportDescription(oldReportDesc);
			report.setModule(oldModule);
			report.setReportStyle(oldReportStyle);
			report.setAllowExport(oldAllowExport);
			report.setMemoryAllocation(oldMemoryAllocation);
			throw new CantDoThatException("Updating report failed", hex);
		} catch (AgileBaseException pex) {
			rollbackConnections(null);
			// restore report properties
			report.setReportName(oldReportName);
			report.setReportDescription(oldReportDesc);
			report.setModule(oldModule);
			report.setReportStyle(oldReportStyle);
			report.setAllowExport(oldAllowExport);
			report.setMemoryAllocation(oldMemoryAllocation);
			throw new CantDoThatException("Updating report failed", pex);
		} finally {
			if (conn != null) {
				conn.close();
			}
			HibernateUtil.closeSession();
		}
	}

	public synchronized static void removeReport(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn)
			throws MissingParametersException, DisallowedException, ObjectNotFoundException,
			SQLException, CantDoThatException {
		// get the table / report to be amended
		TableInfo table = ServletUtilMethods.getTableForRequest(sessionData, request, databaseDefn,
				ServletUtilMethods.USE_SESSION);
		BaseReportInfo report = ServletUtilMethods.getReportForRequest(sessionData, request,
				databaseDefn, ServletUtilMethods.USE_SESSION);
		// begin updating model and persisting changes
		Connection conn = null;
		try {
			HibernateUtil.startHibernateTransaction();
			conn = databaseDefn.getDataSource().getConnection();
			conn.setAutoCommit(false);
			databaseDefn.removeReport(sessionData, request, conn, report);
			HibernateUtil.currentSession().getTransaction().commit();
			// Commit SQL after Hibernate. If there's a problem with SQL, less
			// of a problem. If SQL is committed first, could be left with a
			// report object with no corresponding view.
			conn.commit();
		} catch (SQLException sqlex) {
			rollbackConnections(conn);
			// return report to memory
			if (!table.getReports().contains(report)) {
				table.addReport(report, false);
			}
			throw new CantDoThatException("report removal failed: " + sqlex.getMessage(), sqlex);
		} catch (HibernateException hex) {
			rollbackConnections(conn);
			// return report to memory
			if (!table.getReports().contains(report)) {
				table.addReport(report, false);
			}
			throw new CantDoThatException("report removal failed: " + hex.getMessage(), hex);
		} catch (AgileBaseException pex) {
			rollbackConnections(conn);
			// return report to memory
			if (!table.getReports().contains(report)) {
				table.addReport(report, false);
			}
			throw new CantDoThatException("report removal failed: " + pex.getMessage(), pex);
		} finally {
			if (conn != null) {
				conn.close();
			}
			HibernateUtil.closeSession();
		}
		// change the session report to update pane 2 & 3
		sessionData.setReport(table.getDefaultReport());
	}

	public synchronized static void addField(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn)
			throws MissingParametersException, ObjectNotFoundException, SQLException,
			DisallowedException, CantDoThatException, CodingErrorException {
		// get the table to be amended
		TableInfo table = ServletUtilMethods.getTableForRequest(sessionData, request, databaseDefn,
				ServletUtilMethods.USE_SESSION);
		// load parameters from the request
		String internalFieldName = request.getParameter("internalfieldname");
		String fieldName = request.getParameter("fieldname");
		String fieldType = request.getParameter("fieldtype");
		if (fieldName == null || fieldType == null) {
			throw new MissingParametersException(
					"'fieldname' and 'fieldtype' parameters are both required to add a field");
		}
		String fieldDesc = request.getParameter("fielddesc");
		if (fieldDesc == null) {
			fieldDesc = "";
		}
		if (fieldType.equals(FieldCategory.RELATION.getFieldTypeParameter())) {
			addRelation(request, table, databaseDefn);
		} else {
			if (fieldType.equals(FieldCategory.REFERENCED_REPORT_DATA.getFieldTypeParameter())) {
				if (fieldName.equals("")) {
					String internalTableName = HttpRequestUtil.getStringValue(request,
							PossibleListOptions.LISTTABLE.getFormInputName());
					String internalReportName = HttpRequestUtil.getStringValue(request,
							PossibleListOptions.LISTREPORT.getFormInputName());
					TableInfo referencedReportTable = databaseDefn.getTable(request,
							internalTableName);
					BaseReportInfo referencedReport = referencedReportTable
							.getReport(internalReportName);
					fieldName = referencedReport.getReportName();
					if (fieldName.startsWith("dbvcalc_")) {
						fieldName = fieldName.replace("dbvcalc_", "");
					}
				}
			}
			// for any field other than relation or referenced report data, name
			// has to be specified
			if (fieldName.equals("")) {
				throw new MissingParametersException("Field name must be specified");
			}
			// begin updating model and persisting changes
			BaseField newField = null;
			Connection conn = null;
			try {
				HibernateUtil.startHibernateTransaction();
				conn = databaseDefn.getDataSource().getConnection();
				conn.setAutoCommit(false);
				// create the new field
				newField = databaseDefn.addField(request, conn, table, fieldType,
						internalFieldName, fieldName, fieldDesc);
				conn.commit();
				HibernateUtil.currentSession().getTransaction().commit();
			} catch (SQLException sqlex) {
				rollbackConnections(conn);
				// remove field from memory (table fields and default report
				// fields)
				if (newField != null) {
					table.removeField(newField);
					if (table.getDefaultReport().getReportBaseFields().contains(newField)) {
						ReportFieldInfo reportField = table.getDefaultReport().getReportField(
								newField.getInternalFieldName());
						table.getDefaultReport().removeField(reportField);
					}
				}
				throw new CantDoThatException("field addition failed", sqlex);
			} catch (HibernateException hex) {
				rollbackConnections(conn);
				// remove field from memory (table fields and default report
				// fields)
				if (newField != null) {
					table.removeField(newField);
					if (table.getDefaultReport().getReportBaseFields().contains(newField)) {
						ReportFieldInfo reportField = table.getDefaultReport().getReportField(
								newField.getInternalFieldName());
						table.getDefaultReport().removeField(reportField);
					}
				}
				throw new CantDoThatException("field addition failed", hex);
			} catch (AgileBaseException pex) {
				rollbackConnections(conn);
				// remove field from memory (table fields and default report
				// fields)
				if (newField != null) {
					table.removeField(newField);
					if (table.getDefaultReport().getReportBaseFields().contains(newField)) {
						ReportFieldInfo reportField = table.getDefaultReport().getReportField(
								newField.getInternalFieldName());
						table.getDefaultReport().removeField(reportField);
					}
				}
				throw new CantDoThatException("field addition failed", pex);
			} finally {
				// These lines seem to be needed to 'load' data from Hibernate
				// I don't know why that should be
				// but they should be left here
				logger.info(table.getFields().toString());
				logger.info(table.getDefaultReport().getReportFields().toString());
				if (conn != null) {
					conn.close();
				}
				HibernateUtil.closeSession();
			}
		}
	}

	/**
	 * Doesn't need to be public because it's only called by addField
	 * 
	 * @param table
	 *            The table to add the relation to, normally the session table
	 */
	private static void addRelation(HttpServletRequest request, TableInfo table,
			DatabaseInfo databaseDefn) throws MissingParametersException, ObjectNotFoundException,
			SQLException, CantDoThatException, DisallowedException, CodingErrorException {
		// Get options specified by user
		String internalFieldName = request.getParameter("internalfieldname");
		String fieldName = request.getParameter("fieldname");
		if (fieldName == null) {
			fieldName = "";
		}
		String fieldDesc = request.getParameter("fielddesc");
		if (fieldDesc == null) {
			fieldDesc = "";
		}
		String listTable = request.getParameter(PossibleListOptions.LISTTABLE.getFormInputName());
		if (listTable == null) {
			throw new MissingParametersException("'"
					+ PossibleListOptions.LISTTABLE.getFormInputName()
					+ "' parameter required in the request to add a relation");
		}
		TableInfo relatedTable = databaseDefn.getTable(request, listTable);
		BaseField relatedField = relatedTable.getPrimaryKey();
		// Check there isn't already a relation to this table
		for (BaseField field : table.getFields()) {
			if (field instanceof RelationField) {
				if (((RelationField) field).getRelatedTable().equals(relatedTable)) {
					throw new CantDoThatException("Field '" + field
							+ "' already relates to the table " + relatedTable);
				}
			}
		}
		// By ensuring that the key field for a relation is always the primary
		// key
		// field, we can simplify much of the code involving relation fields.
		// So, if it's decided that it should be possible to set another field
		// as the key field then much of this surrounding code will need to be
		// re-tested.
		// Begin updating model and persisting changes
		RelationField newField = null;
		Connection conn = null;
		try {
			HibernateUtil.startHibernateTransaction();
			conn = databaseDefn.getDataSource().getConnection();
			conn.setAutoCommit(false);
			// create the new relation field
			newField = databaseDefn.addRelation(request, conn, table, internalFieldName, fieldName,
					fieldDesc, relatedTable, relatedField);
			conn.commit();
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (SQLException sqlex) {
			rollbackConnections(conn);
			// remove field from memory (table fields and default report fields)
			if (newField != null) {
				table.removeField(newField);
				if (table.getDefaultReport().getReportBaseFields().contains(newField)) {
					ReportFieldInfo reportField = table.getDefaultReport().getReportField(
							newField.getInternalFieldName());
					table.getDefaultReport().removeField(reportField);
				}
			}
			throw new CantDoThatException("field addition failed", sqlex);
		} catch (HibernateException hex) {
			rollbackConnections(conn);
			if (newField != null) {
				table.removeField(newField);
				if (table.getDefaultReport().getReportBaseFields().contains(newField)) {
					ReportFieldInfo reportField = table.getDefaultReport().getReportField(
							newField.getInternalFieldName());
					table.getDefaultReport().removeField(reportField);
				}
			}
			// remove field from memory (table fields and default report fields)
			throw new CantDoThatException("field addition failed", hex);
		} catch (AgileBaseException pex) {
			rollbackConnections(conn);
			if (newField != null) {
				table.removeField(newField);
				if (table.getDefaultReport().getReportBaseFields().contains(newField)) {
					ReportFieldInfo reportField = table.getDefaultReport().getReportField(
							newField.getInternalFieldName());
					table.getDefaultReport().removeField(reportField);
				}
			}
			// remove field from memory (table fields and default report fields)
			throw new CantDoThatException("field addition failed", pex);
		} finally {
			if (conn != null) {
				conn.close();
			}
			HibernateUtil.closeSession();
		}
	}

	public synchronized static void removeField(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn) throws DisallowedException,
			ObjectNotFoundException, MissingParametersException, SQLException, CantDoThatException,
			CodingErrorException {
		TableInfo tableToRemoveFrom = ServletUtilMethods.getTableForRequest(sessionData, request,
				databaseDefn, ServletUtilMethods.USE_SESSION);
		String internalFieldName = request.getParameter("internalfieldname");
		if (internalFieldName == null) {
			throw new MissingParametersException(
					"'internalfieldname' parameter needed in the request to remove a field");
		}
		BaseField field = tableToRemoveFrom.getField(internalFieldName);
		ReportFieldInfo reportField = null;
		FieldCategory fieldCategory = field.getFieldCategory();
		if ((!fieldCategory.equals(FieldCategory.SEPARATOR))
				&& (!fieldCategory.equals(FieldCategory.COMMENT_FEED))
				&& (!fieldCategory.equals(FieldCategory.REFERENCED_REPORT_DATA))) {
			// These types of field not included in default report
			reportField = tableToRemoveFrom.getDefaultReport().getReportField(internalFieldName);
		}
		// begin updating model and persisting changes
		Connection conn = null;
		try {
			HibernateUtil.startHibernateTransaction();
			conn = databaseDefn.getDataSource().getConnection();
			conn.setAutoCommit(false);
			// remove the field
			databaseDefn.removeField(request, conn, field);
			HibernateUtil.currentSession().getTransaction().commit();
			// SQL commit after Hibernate commit because otherwise we don't roll
			// back SQL
			// When deleting, errors likely to be Hibernate
			conn.commit();
		} catch (SQLException sqlex) {
			// add field back into table & default report
			if (!tableToRemoveFrom.getFields().contains(field)) {
				field.getTableContainingField().addField(field);
			}
			if (reportField != null) {
				if (!tableToRemoveFrom.getDefaultReport().getReportFields().contains(reportField)) {
					field.getTableContainingField().getDefaultReport().addTableField(field);
				}
			}
			rollbackConnections(conn);
			throw new CantDoThatException("Field removal failed: " + sqlex.getMessage(), sqlex);
		} catch (HibernateException hex) {
			// add field back into table & default report
			if (!tableToRemoveFrom.getFields().contains(field))
				field.getTableContainingField().addField(field);
			if (reportField != null) {
				if (!tableToRemoveFrom.getDefaultReport().getReportFields().contains(reportField)) {
					field.getTableContainingField().getDefaultReport().addTableField(field);
				}
			}
			rollbackConnections(conn);
			throw new CantDoThatException("Field removal failed: " + hex.getMessage(), hex);
		} catch (AgileBaseException pex) {
			// add field back into table & default report
			if (!tableToRemoveFrom.getFields().contains(field))
				field.getTableContainingField().addField(field);
			if (reportField != null) {
				if (!tableToRemoveFrom.getDefaultReport().getReportFields().contains(reportField)) {
					field.getTableContainingField().getDefaultReport().addTableField(field);
				}
			}
			rollbackConnections(conn);
			throw new CantDoThatException("Field removal failed: " + pex.getMessage(), pex);
		} finally {
			if (conn != null) {
				conn.close();
			}
			HibernateUtil.closeSession();
		}
	}

	public synchronized static void updateFieldOption(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn)
			throws MissingParametersException, DisallowedException, ObjectNotFoundException,
			CantDoThatException, CodingErrorException {
		TableInfo table = ServletUtilMethods.getTableForRequest(sessionData, request, databaseDefn,
				ServletUtilMethods.USE_SESSION);
		String internalFieldName = request.getParameter("internalfieldname");
		if (internalFieldName == null) {
			throw new MissingParametersException(
					"'internalfieldname' parameter needed in the request to remove a field");
		}
		BaseField field = table.getField(internalFieldName);
		// store current values that may be overwritten by
		// DatabaseDefn.updateFieldOption so they can be rolled back
		Boolean textFieldUsesLookup = null;
		Integer textFieldContentSize = null;
		Boolean dateFieldDefaultToNow = null;
		Integer dateFieldResolution = null;
		Integer decimalFieldPrecision = null;
		String textFieldDefault = null;
		Double decimalFieldDefault = null;
		Integer integerFieldDefault = null;
		Boolean unique = field.getUnique();
		Boolean notNull = field.getNotNull();
		TextCase textCase = null;
		Integer minYear = null;
		Integer maxYear = null;
		boolean tieDownLookup = false;
		if (field instanceof TextField) {
			textFieldUsesLookup = ((TextField) field).usesLookup();
			textFieldContentSize = ((TextField) field).getContentSize();
			textFieldDefault = ((TextField) field).getDefault();
			textCase = ((TextField) field).getTextCase();
			tieDownLookup = ((TextField) field).getTieDownLookup();
		} else if (field instanceof DateField) {
			dateFieldDefaultToNow = ((DateField) field).getDefaultToNow();
			dateFieldResolution = ((DateField) field).getDateResolution();
			minYear = ((DateField) field).getMinAgeYears();
			maxYear = ((DateField) field).getMaxAgeYears();
		} else if (field instanceof DecimalField) {
			decimalFieldPrecision = ((DecimalField) field).getPrecision();
			decimalFieldDefault = ((DecimalField) field).getDefault();
		} else if (field instanceof IntegerField) {
			integerFieldDefault = ((IntegerField) field).getDefault();
		}
		// begin updating model and persisting changes
		try {
			HibernateUtil.startHibernateTransaction();
			// update the field:
			databaseDefn.updateFieldOption(request, field);
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (HibernateException hex) {
			rollbackConnections(null);
			restoreFieldOptions(field, textFieldUsesLookup, tieDownLookup, textFieldContentSize,
					dateFieldDefaultToNow, dateFieldResolution, minYear, maxYear, decimalFieldPrecision,
					textFieldDefault, decimalFieldDefault, integerFieldDefault, unique, notNull,
					textCase);
			throw new CantDoThatException("Updating field failed", hex);
		} catch (AgileBaseException pex) {
			rollbackConnections(null);
			restoreFieldOptions(field, textFieldUsesLookup, tieDownLookup, textFieldContentSize,
					dateFieldDefaultToNow, dateFieldResolution, minYear, maxYear, decimalFieldPrecision,
					textFieldDefault, decimalFieldDefault, integerFieldDefault, unique, notNull,
					textCase);
			throw new CantDoThatException("Updating field failed: " + pex.getMessage(), pex);
		} catch (SQLException sqlex) {
			rollbackConnections(null);
			restoreFieldOptions(field, textFieldUsesLookup, tieDownLookup, textFieldContentSize,
					dateFieldDefaultToNow, dateFieldResolution, minYear, maxYear, decimalFieldPrecision,
					textFieldDefault, decimalFieldDefault, integerFieldDefault, unique, notNull,
					textCase);
			throw new CantDoThatException("Updating field failed", sqlex);
		} finally {
			HibernateUtil.closeSession();
		}
	}

	/**
	 * Used by updateFieldOptions when restoring a field object to it's initial
	 * state after an update error
	 * 
	 * TODO: refactor to use a properties object rather than masses of parameters
	 */
	private static void restoreFieldOptions(BaseField field, Boolean textFieldUsesLookup, boolean tieDownLookup,
			Integer textFieldContentSize, Boolean dateFieldDefaultToNow,
			Integer dateFieldResolution, Integer minYear, Integer maxYear, Integer decimalFieldPrecision, String textFieldDefault,
			Double decimalFieldDefault, Integer integerFieldDefault, Boolean unique,
			Boolean notNull, TextCase textCase) throws CantDoThatException {
		field.setUnique(unique);
		field.setNotNull(notNull);
		if (field instanceof TextField) {
			((TextField) field).setUsesLookup(textFieldUsesLookup);
			((TextField) field).setContentSize(textFieldContentSize);
			((TextField) field).setDefault(textFieldDefault);
			((TextField) field).setTextCase(textCase);
			((TextField) field).setTieDownLookup(tieDownLookup);
		} else if (field instanceof DateField) {
			((DateField) field).setDefaultToNow(dateFieldDefaultToNow);
			((DateField) field).setDateResolution(dateFieldResolution);
			((DateField) field).setMinAgeYears(minYear);
			((DateField) field).setMaxAgeYears(maxYear);
		} else if (field instanceof DecimalField) {
			((DecimalField) field).setPrecision(decimalFieldPrecision);
			((DecimalField) field).setDefault(decimalFieldDefault);
		} else if (field instanceof IntegerField) {
			((IntegerField) field).setDefault(integerFieldDefault);
		}
	}

	public synchronized static void updateField(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn) throws DisallowedException,
			ObjectNotFoundException, MissingParametersException, SQLException, CantDoThatException,
			CodingErrorException {
		TableInfo table = ServletUtilMethods.getTableForRequest(sessionData, request, databaseDefn,
				ServletUtilMethods.USE_SESSION);
		String internalFieldName = request.getParameter("internalfieldname");
		if (internalFieldName == null) {
			throw new MissingParametersException(
					"'internalfieldname' parameter needed in the request to remove a field");
		}
		BaseField field = table.getField(internalFieldName);
		// now collect new values for field properties:
		String fieldName = request.getParameter("fieldname");
		String fieldDesc = request.getParameter("fielddesc");
		if ((fieldName == null) && (fieldDesc == null)) {
			throw new MissingParametersException(
					"'fieldname' or 'fielddesc' parameter is required to update a field");
		}
		String oldFieldName = field.getFieldName();
		String oldFieldDescription = field.getFieldDescription();
		// begin updating model and persisting changes
		try {
			HibernateUtil.startHibernateTransaction();
			// update the field:
			databaseDefn.updateField(request, field, fieldName, fieldDesc);
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (HibernateException hex) {
			rollbackConnections(null);
			// restore field
			field.setFieldName(oldFieldName);
			field.setFieldDescription(oldFieldDescription);
			throw new CantDoThatException("Updating field failed", hex);
		} catch (AgileBaseException pex) {
			rollbackConnections(null);
			// restore field
			field.setFieldName(oldFieldName);
			field.setFieldDescription(oldFieldDescription);
			throw new CantDoThatException("Updating field failed", pex);
		} finally {
			HibernateUtil.closeSession();
		}
	}

	/**
	 * Used to re-order fields in a table
	 */
	public synchronized static void setFieldIndex(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn) throws ObjectNotFoundException,
			MissingParametersException, CantDoThatException, SQLException, DisallowedException {
		TableInfo table = ServletUtilMethods.getTableForRequest(sessionData, request, databaseDefn,
				ServletUtilMethods.USE_SESSION);
		String internalFieldName = request.getParameter("internalfieldname");
		if (internalFieldName == null) {
			throw new MissingParametersException(
					"'internalfieldname' parameter needed in the request to re-order a field");
		}
		BaseField field = table.getField(internalFieldName);
		Integer newFieldIndex = ServletDataMethods.getIntegerParameterValue(request,
				"newfieldindex");
		if (newFieldIndex == null) {
			throw new MissingParametersException(
					"'newfieldindex' parameter needed in the request to re-order a field");
		}
		int oldFieldIndex = field.getFieldIndex();
		// begin updating model and persisting changes
		try {
			HibernateUtil.startHibernateTransaction();
			// set the new field index
			databaseDefn.setFieldIndex(table, field, newFieldIndex);
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (HibernateException hex) {
			rollbackConnections(null);
			// restore old field index
			table.setFieldIndex(oldFieldIndex, field);
			if (table.getDefaultReport().getReportBaseFields().contains(field)) {
				ReportFieldInfo reportField = table.getDefaultReport().getReportField(
						internalFieldName);
				table.getDefaultReport().setFieldIndex(oldFieldIndex, reportField);
			}
			throw new CantDoThatException("Setting field index failed", hex);
		} catch (AgileBaseException pex) {
			rollbackConnections(null);
			// restore old field index
			table.setFieldIndex(oldFieldIndex, field);
			if (table.getDefaultReport().getReportBaseFields().contains(field)) {
				ReportFieldInfo reportField = table.getDefaultReport().getReportField(
						internalFieldName);
				table.getDefaultReport().setFieldIndex(oldFieldIndex, reportField);
			}
			throw new CantDoThatException("Setting field index failed", pex);
		} finally {
			HibernateUtil.closeSession();
		}
	}

	public synchronized static void addFieldToReport(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn) throws DisallowedException,
			MissingParametersException, ObjectNotFoundException, SQLException,
			InconsistentStateException, CantDoThatException, CodingErrorException {
		// ServletUtilMethods.getReportForRequest cannot be used here as it
		// would cause confusion
		// between the report source / destination for field addition
		// perhaps changing the name of parameter identifying the field's parent
		// report would help
		BaseReportInfo baseReport = sessionData.getReport();
		if (baseReport == null) {
			throw new ObjectNotFoundException("There is no report in the session");
		}
		if (!(baseReport instanceof SimpleReportInfo)) {
			throw new CantDoThatException(
					"You can only add fields to normal reports, not union reports");
		}
		SimpleReportInfo report = (SimpleReportInfo) baseReport;
		TableInfo sourceTable = null;
		SimpleReportInfo sourceReport = null;
		// try to obtain the name of the source table; if this is null then the
		// field
		// must be coming from another report. If the internalreportname param
		// is also
		// null then assume the field is coming from the session report (??)
		// If the other report identified is simply a table's default report
		// then revert
		// back to using the table as this allows for more flexibilty
		String internalTableName = request.getParameter("internaltablename") == null ? "" : request
				.getParameter("internaltablename");
		if (internalTableName != "") {
			// obtain table
			sourceTable = databaseDefn.getTable(request, internalTableName);
		} else {
			// obtain report
			String internalReportName = request.getParameter("internalreportname") == null ? ""
					: request.getParameter("internalreportname");
			if (internalReportName == "") {
				sourceTable = report.getParentTable();
			} else {
				sourceTable = databaseDefn.findTableContainingReport(request, internalReportName);
				if (!sourceTable.getDefaultReport().getInternalReportName()
						.equals(internalReportName)) {
					sourceReport = (SimpleReportInfo) sourceTable.getReport(internalReportName);
				}
			}
		}
		String internalFieldName = request.getParameter("internalfieldname");
		if (internalFieldName == null) {
			throw new MissingParametersException(
					"'internalfieldname' parameter needed in request to add a field to a report");
		}
		BaseField field;
		if (sourceReport != null) {
			field = sourceReport.getReportField(internalFieldName).getBaseField();
		} else {
			field = sourceTable.getField(internalFieldName);
		}
		// begin updating model and persisting changes
		Connection conn = null;
		ReportFieldInfo addedReportField = null;
		try {
			HibernateUtil.startHibernateTransaction();
			conn = databaseDefn.getDataSource().getConnection();
			conn.setAutoCommit(false);
			addedReportField = databaseDefn.addFieldToReport(request, conn, report, sourceReport,
					field);
			// add sort on this field if specified:
			String sortDirectionString = request.getParameter("sortdirection");
			if (sortDirectionString != null) {
				if (sortDirectionString.equals("ascending")) {
					databaseDefn.addSortToReport(request, conn, report, addedReportField, true);
				} else if (sortDirectionString.equals("descending")) {
					databaseDefn.addSortToReport(request, conn, report, addedReportField, false);
				}
			}
			conn.commit();
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (SQLException sqlex) {
			rollbackConnections(conn);
			// remove reportfield from memory
			if (addedReportField != null) {
				report.removeField(addedReportField);
			}
			throw new CantDoThatException("report field addition failed: "
					+ Helpers.replaceInternalNames(sqlex.getMessage(), report), sqlex);
		} catch (HibernateException hex) {
			rollbackConnections(conn);
			// remove reportfield from memory
			if (addedReportField != null) {
				report.removeField(addedReportField);
			}
			throw new CantDoThatException("report field addition failed", hex);
		} catch (AgileBaseException pex) {
			rollbackConnections(conn);
			// remove reportfield from memory
			if (addedReportField != null) {
				report.removeField(addedReportField);
			}
			throw new CantDoThatException("report field addition failed: " + pex.getMessage(), pex);
		} finally {
			if (conn != null) {
				conn.close();
			}
			HibernateUtil.closeSession();
		}
	}

	public synchronized static void removeFieldFromReport(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn) throws DisallowedException,
			MissingParametersException, ObjectNotFoundException, SQLException, CantDoThatException {
		BaseReportInfo baseReport = ServletUtilMethods.getReportForRequest(sessionData, request,
				databaseDefn, ServletUtilMethods.USE_SESSION);
		if (!(baseReport instanceof SimpleReportInfo)) {
			throw new CantDoThatException(
					"You can only remove fields from normal reports, not union reports");
		}
		SimpleReportInfo report = (SimpleReportInfo) baseReport;
		String internalFieldName = request.getParameter("internalfieldname");
		if (internalFieldName == null) {
			throw new MissingParametersException(
					"'internalfieldname' parameter needed in request to add a field to a report");
		}
		ReportFieldInfo reportField = report.getReportField(internalFieldName);
		// begin updating model and persisting changes
		Connection conn = null;
		try {
			HibernateUtil.startHibernateTransaction();
			conn = databaseDefn.getDataSource().getConnection();
			conn.setAutoCommit(false);
			// remove the report field
			databaseDefn.removeFieldFromReport(request, conn, reportField);
			HibernateUtil.currentSession().getTransaction().commit();
			// SQL commit after Hibernate commit because otherwise we don't roll
			// back SQL.
			// When deleting, errors likely to be Hibernate
			conn.commit();
		} catch (SQLException sqlex) {
			rollbackConnections(conn);
			// return reportfield to memory
			try {
				report.addField(reportField);
			} catch (AgileBaseException pex) {
				logger.warn("Error returning report field to memory");
				pex.printStackTrace();
			}
			throw new CantDoThatException("report field removal failed", sqlex);
		} catch (HibernateException hex) {
			rollbackConnections(conn);
			// return reportfield to memory
			try {
				report.addField(reportField);
			} catch (AgileBaseException pex) {
				logger.warn("Error returning report field to memory");
				pex.printStackTrace();
			}
			throw new CantDoThatException("report field removal failed", hex);
		} catch (AgileBaseException pex) {
			rollbackConnections(conn);
			// return reportfield to memory
			try {
				report.addField(reportField);
			} catch (AgileBaseException secondPex) {
				logger.warn("Error returning report field to memory");
				secondPex.printStackTrace();
			}
			throw new CantDoThatException("Report field removal failed. " + pex.getMessage(), pex);
		} finally {
			if (conn != null) {
				conn.close();
			}
			HibernateUtil.closeSession();
		}
	}

	/**
	 * Used to re-order fields in a report
	 */
	public synchronized static void setReportFieldIndex(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn) throws DisallowedException,
			MissingParametersException, ObjectNotFoundException, SQLException, CantDoThatException {
		BaseReportInfo baseReport = ServletUtilMethods.getReportForRequest(sessionData, request,
				databaseDefn, ServletUtilMethods.USE_SESSION);
		if (!(baseReport instanceof SimpleReportInfo)) {
			throw new CantDoThatException(
					"You can only reorder fields from normal reports, not union reports");
		}
		SimpleReportInfo report = (SimpleReportInfo) baseReport;
		String internalFieldName = request.getParameter("internalfieldname");
		if (internalFieldName == null) {
			throw new MissingParametersException(
					"'internalfieldname' parameter needed in request to reorder a field in a report");
		}
		ReportFieldInfo field = report.getReportField(internalFieldName);
		Integer newFieldIndex = ServletDataMethods.getIntegerParameterValue(request,
				"newfieldindex");
		if (newFieldIndex == null) {
			throw new MissingParametersException(
					"'newfieldindex' parameter needed in the request to re-order a field");
		}
		int oldFieldIndex = field.getFieldIndex();
		// begin updating model and persisting changes
		Connection conn = null;
		try {
			HibernateUtil.startHibernateTransaction();
			conn = databaseDefn.getDataSource().getConnection();
			conn.setAutoCommit(false);
			// set the report field index:
			databaseDefn.setReportFieldIndex(conn, report, field, newFieldIndex, request);
			conn.commit();
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (SQLException sqlex) {
			rollbackConnections(conn);
			// restore old field index
			report.setFieldIndex(oldFieldIndex, field);
			throw new CantDoThatException("Report field re-indexing failed", sqlex);
		} catch (HibernateException hex) {
			rollbackConnections(conn);
			// restore old field index
			report.setFieldIndex(oldFieldIndex, field);
			throw new CantDoThatException("Report field re-indexing failed", hex);
		} catch (AgileBaseException pex) {
			rollbackConnections(conn);
			// restore old field index
			report.setFieldIndex(oldFieldIndex, field);
			throw new CantDoThatException("Report field re-indexing failed", pex);
		} finally {
			if (conn != null) {
				conn.close();
			}
			HibernateUtil.closeSession();
		}
	}

	/**
	 * Used to set sorting applied to report fields
	 */
	public synchronized static void setReportFieldSorting(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn) throws DisallowedException,
			MissingParametersException, ObjectNotFoundException, SQLException, CantDoThatException,
			CodingErrorException {
		BaseReportInfo baseReport = ServletUtilMethods.getReportForRequest(sessionData, request,
				databaseDefn, ServletUtilMethods.USE_SESSION);
		if (!(baseReport instanceof SimpleReportInfo)) {
			throw new CantDoThatException(
					"You can only set field sorting for normal reports, not union reports");
		}
		SimpleReportInfo report = (SimpleReportInfo) baseReport;
		String internalFieldName = request.getParameter("internalfieldname");
		if (internalFieldName == null) {
			throw new MissingParametersException(
					"'internalfieldname' parameter needed in request to set sorting on a field in a report");
		}
		ReportFieldInfo field = report.getReportField(internalFieldName);
		String sorting = request.getParameter("sortdirection" + field.getInternalFieldName());
		if (sorting == null) {
			throw new MissingParametersException(
					"'sortdirection$internalfieldname]' parameter needed in the request to re-order a field");
		} else if (!(sorting.equals("unsorted") || sorting.equals("ascending") || sorting
				.equals("descending"))) {
			throw new CodingErrorException(
					"'sorting' must be either 'unsorted', 'ascending' or 'descending'");
		}
		boolean ascending = (sorting.equals("ascending"));
		// check existing sorts in the report to see if sorting is presently
		// applied to the field
		boolean sortExists = false;
		ReportSortInfo oldReportSort = null;
		for (ReportSortInfo reportSort : report.getSorts()) {
			if (reportSort.getSortReportField().equals(field)) {
				sortExists = true;
				oldReportSort = new ReportSort(reportSort.getSortReportField(),
						reportSort.getSortDirection());
				break;
			}
		}
		logger.info(report.getSorts().toString());
		// begin updating model and persisting changes
		Connection conn = null;
		try {
			HibernateUtil.startHibernateTransaction();
			conn = databaseDefn.getDataSource().getConnection();
			conn.setAutoCommit(false);
			if (sortExists) {
				if (sorting.equals("unsorted")) {
					databaseDefn.removeSortFromReport(request, conn, report, field);
				} else {
					databaseDefn.updateSortFromReport(request, conn, report, field, ascending);
				}
			} else {
				if (!sorting.equals("unsorted")) {
					databaseDefn.addSortToReport(request, conn, report, field, ascending);
				} // else if requesting removal of a sort that doesn't
					// presently exist, silently ignore the request
			}
			conn.commit();
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (SQLException sqlex) {
			rollbackConnections(conn);
			// rollback changes to in memory objects
			report.removeSort(field);
			if (sortExists) {
				report.addSort(oldReportSort.getSortReportField(), oldReportSort.getSortDirection());
			}
			throw new CantDoThatException("report field sorting failed", sqlex);
		} catch (HibernateException hex) {
			rollbackConnections(conn);
			// rollback changes to in memory objects
			report.removeSort(field);
			if (sortExists) {
				report.addSort(oldReportSort.getSortReportField(), oldReportSort.getSortDirection());
			}
			throw new CantDoThatException("report field sorting failed", hex);
		} catch (AgileBaseException pex) {
			rollbackConnections(conn);
			// rollback changes to in memory objects
			report.removeSort(field);
			if (sortExists) {
				report.addSort(oldReportSort.getSortReportField(), oldReportSort.getSortDirection());
			}
			throw new CantDoThatException("report field sorting failed", pex);
		} finally {
			if (conn != null) {
				conn.close();
			}
			HibernateUtil.closeSession();
			logger.info(report.getSorts().toString());
		}
	}

	public synchronized static void addCalculationToReport(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn) throws DisallowedException,
			MissingParametersException, ObjectNotFoundException, SQLException, CantDoThatException,
			CodingErrorException {
		BaseReportInfo baseReport = ServletUtilMethods.getReportForRequest(sessionData, request,
				databaseDefn, ServletUtilMethods.USE_SESSION);
		if (!(baseReport instanceof SimpleReportInfo)) {
			throw new CantDoThatException(
					"You can only add calculations to normal reports, not union reports");
		}
		SimpleReportInfo report = (SimpleReportInfo) baseReport;
		String internalCalculationName = request.getParameter("internalcalculationname");
		String calculationName = request.getParameter("calculationname");
		String calculationDefn = request.getParameter("calculationdefn");
		String dbType = request.getParameter("databasetype");
		if (calculationName == null || calculationDefn == null || dbType == null) {
			throw new MissingParametersException(
					"'calculationname', 'calculationdefn' and 'databasetype' parameters needed in request to add a calculation");
		}
		// begin updating model and persisting changes
		ReportCalcFieldInfo newCalculationField = null;
		Connection conn = null;
		try {
			HibernateUtil.startHibernateTransaction();
			conn = databaseDefn.getDataSource().getConnection();
			conn.setAutoCommit(false);
			// create the new field
			DatabaseFieldType databaseType = DatabaseFieldType.valueOf(dbType
					.toUpperCase(Locale.UK));
			Map<TableInfo, Set<BaseReportInfo>> availableDataStores = databaseDefn
					.getViewableDataStores(request);
			newCalculationField = new ReportCalcFieldDefn(report, internalCalculationName,
					calculationName, calculationDefn, databaseType, availableDataStores);
			databaseDefn.addCalculationToReport(request, conn, report, newCalculationField);
			conn.commit();
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (SQLException sqlex) {
			rollbackConnections(conn);
			// remove calculation from memory
			if (newCalculationField != null) {
				report.removeField(newCalculationField);
			}
			String message = sqlex.getMessage();
			int bracketDifference = 0;
			if (message.contains("syntax error at or near \")\"")) {
				bracketDifference = -1;
			} else {
				bracketDifference = StringUtils.countMatches(message, "(")
						- StringUtils.countMatches(message, ")");
			}
			if (bracketDifference != 0) {
				String word;
				if (Math.abs(bracketDifference) == 1) {
					word = "is ";
				} else {
					word = "are ";
				}
				if (bracketDifference > 0) {
					message = "It looks like brackets may not match - there " + word
							+ bracketDifference + " closing bracket(s) missing";
				} else {
					message = "It looks like brackets may not match - there " + word
							+ bracketDifference + " more closing bracket(s) than opening brackets";
				}
			}
			throw new CantDoThatException("Calculation addition failed. "
					+ Helpers.replaceInternalNames(message, report), sqlex);
		} catch (HibernateException hex) {
			rollbackConnections(conn);
			// remove calculation from memory
			if (newCalculationField != null) {
				report.removeField(newCalculationField);
			}
			throw new CantDoThatException("Calculation addition failed. " + hex.getMessage(), hex);
		} catch (AgileBaseException pex) {
			rollbackConnections(conn);
			// remove calculation from memory
			if (newCalculationField != null) {
				report.removeField(newCalculationField);
			}
			throw new CantDoThatException("Calculation addition failed. " + pex.getMessage(), pex);
		} finally {
			conn.close();
			HibernateUtil.closeSession();
		}
	}

	public synchronized static void updateCalculationInReport(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn) throws CantDoThatException,
			DisallowedException, ObjectNotFoundException, MissingParametersException, SQLException,
			CodingErrorException {
		BaseReportInfo baseReport = ServletUtilMethods.getReportForRequest(sessionData, request,
				databaseDefn, ServletUtilMethods.USE_SESSION);
		if (!(baseReport instanceof SimpleReportInfo)) {
			throw new CantDoThatException(
					"You can only add calculations to normal reports, not union reports");
		}
		SimpleReportInfo report = (SimpleReportInfo) baseReport;
		String internalCalculationName = request.getParameter("internalcalculationname");
		String calculationName = request.getParameter("calculationname");
		String calculationDefn = request.getParameter("calculationdefn");
		String dbType = request.getParameter("databasetype");
		String isReportHiddenString = request.getParameter("isreporthidden");
		boolean isReportHidden = Helpers.valueRepresentsBooleanTrue(isReportHiddenString);
		if (internalCalculationName == null || calculationName == null || calculationDefn == null
				|| dbType == null) {
			throw new MissingParametersException(
					"'internalcalculationname', 'calculationname', 'calculationdefn' and 'databasetype' (optionally 'isreporthidden') parameters needed in request to update a calculation");
		}
		// begin updating model and persisting changes
		ReportCalcFieldInfo calculationField = (ReportCalcFieldInfo) report
				.getReportField(internalCalculationName);
		String oldCalculationName = calculationField.getBaseFieldName();
		String oldCalculationDefn = calculationField.getCalculationDefinition();
		boolean oldIsReportHidden = calculationField.isReportHidden();
		DatabaseFieldType oldDbFieldType = calculationField.getDbType();
		Connection conn = null;
		try {
			HibernateUtil.startHibernateTransaction();
			conn = databaseDefn.getDataSource().getConnection();
			conn.setAutoCommit(false);
			// update the calculation field
			DatabaseFieldType databaseType = DatabaseFieldType.valueOf(dbType
					.toUpperCase(Locale.UK));
			databaseDefn.updateCalculationInReport(request, conn, report, calculationField,
					calculationName, calculationDefn, databaseType, isReportHidden);
			conn.commit();
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (SQLException sqlex) {
			rollbackConnections(conn);
			// undo changes to calculation in memory
			databaseDefn.returnCalculationInReportToMemory(request, conn, report, calculationField,
					oldCalculationName, oldCalculationDefn, oldDbFieldType);
			throw new CantDoThatException("Calculation addition failed. " + sqlex.getMessage(),
					sqlex);
		} catch (HibernateException hex) {
			rollbackConnections(conn);
			// undo changes to calculation in memory
			databaseDefn.returnCalculationInReportToMemory(request, conn, report, calculationField,
					oldCalculationName, oldCalculationDefn, oldDbFieldType);
			throw new CantDoThatException("Calculation addition failed. " + hex.getMessage(), hex);
		} catch (AgileBaseException pex) {
			rollbackConnections(conn);
			// undo changes to calculation in memory
			databaseDefn.returnCalculationInReportToMemory(request, conn, report, calculationField,
					oldCalculationName, oldCalculationDefn, oldDbFieldType);
			throw new CantDoThatException("Calculation addition failed. " + pex.getMessage(), pex);
		} finally {
			if (conn != null) {
				conn.close();
			}
			HibernateUtil.closeSession();
		}
	}

	public synchronized static void addFilterToReport(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn) throws ObjectNotFoundException,
			MissingParametersException, SQLException, DisallowedException, CantDoThatException,
			CodingErrorException {
		// ServletUtilMethods.getReportForRequest cannot be used here as it
		// would cause confusion
		// between the report to have the filter applied to and any report
		// used as part of the filter's definition
		// perhaps changing the name of parameter identifying the field's parent
		// report would help
		BaseReportInfo baseReport = sessionData.getReport();
		if (baseReport == null) {
			throw new ObjectNotFoundException("There is no report in the session");
		}
		if (!(baseReport instanceof SimpleReportInfo)) {
			throw new CantDoThatException(
					"You can only add filters to normal reports, not union reports");
		}
		SimpleReportInfo report = (SimpleReportInfo) baseReport;
		String internalTableName = request.getParameter("internaltablename");
		String internalReportName = request.getParameter("internalreportname");
		String internalFieldName = request.getParameter("internalfieldname");
		String filterType = request.getParameter("filtertype");
		if ((internalReportName == null && internalTableName == null) || internalFieldName == null
				|| filterType == null) {
			throw new MissingParametersException(
					"'internalreportname/internaltablename', 'internalfieldname' and 'filtertype' parameters must all be present in a request to add a field filter");
		}
		TableInfo fieldTable = null;
		BaseReportInfo reportContainingFilterField = null;
		if ((internalReportName != null) && (internalReportName != "")) {
			fieldTable = databaseDefn.findTableContainingReport(request, internalReportName);
			reportContainingFilterField = fieldTable.getReport(internalReportName);
		} else {
			fieldTable = databaseDefn.getTable(request, internalTableName);
			reportContainingFilterField = fieldTable.getDefaultReport();
		}
		BaseField field;
		if (fieldTable.getDefaultReport().equals(reportContainingFilterField)) {
			reportContainingFilterField = null;
			field = fieldTable.getField(internalFieldName);
		} else {
			ReportFieldInfo reportField = reportContainingFilterField
					.getReportField(internalFieldName);
			if (reportField == null) {
				throw new ObjectNotFoundException("Unable to locate field within report "
						+ reportContainingFilterField.getReportName());
			} else {
				field = reportField.getBaseField();
			}
		}
		Set<String> filterValues = new HashSet<String>();
		// Use if there's only one filter item
		String firstFilterValue = null;
		if (FilterType.IS_ONE_OF.getFilterTypeParameter().equals(filterType)) {
			String parameter = request.getParameter(PossibleTextOptions.DEFAULTVALUE
					.getFormInputName());
			filterValues = Helpers.stringArrayToSet(parameter.split(";"));
		} else if ((!FilterType.IS_NULL.getFilterTypeParameter().equals(filterType)) && (!FilterType.IS_NOT_NULL.getFilterTypeParameter().equals(filterType))) {
			// For IS_NULL and IS_NOT_NULL, there is no filter value
			filterValues.add(request.getParameter(PossibleTextOptions.DEFAULTVALUE
					.getFormInputName()));
			 firstFilterValue = new TreeSet<String>(filterValues).first();
		}
		// create filter object
		ReportFilterInfo filter = null;
		// begin updating model and persisting changes
		Connection conn = null;
		try {
			HibernateUtil.startHibernateTransaction();
			conn = databaseDefn.getDataSource().getConnection();
			conn.setAutoCommit(false);
			if (FilterType.IS_ONE_OF.getFilterTypeParameter().equals(filterType)) {
				filter = new ReportFilterDefn(report, reportContainingFilterField, field,
						filterType, filterValues);
			} else {
				Map<TableInfo, Set<BaseReportInfo>> availableDataStores = databaseDefn
						.getViewableDataStores(request);
				filter = new ReportFilterDefn(report, reportContainingFilterField, field,
						filterType, firstFilterValue, availableDataStores);
			}
			databaseDefn.addFilterToReport(request, conn, report, filter);
			conn.commit();
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (SQLException sqlex) {
			rollbackConnections(conn);
			// remove report filter from memory
			if (filter != null) {
				report.removeFilter(filter);
			}
			String message = "Filter addition failed";
			if (sqlex.getMessage().contains("window functions not allowed in WHERE")) {
				message += ". That calculation can't be used in a filter (it contains a window function). Try creating the calculation in a separate view and joining to it instead.";
			}
			throw new CantDoThatException(message, sqlex);
		} catch (HibernateException hex) {
			rollbackConnections(conn);
			// remove report filter from memory
			if (filter != null) {
				report.removeFilter(filter);
			}
			throw new CantDoThatException("filter addition failed", hex);
		} catch (AgileBaseException pex) {
			rollbackConnections(conn);
			// remove report filter from memory
			if (filter != null) {
				report.removeFilter(filter);
			}
			throw new CantDoThatException("filter addition failed", pex);
		} finally {
			if (conn != null) {
				conn.close();
			}
			HibernateUtil.closeSession();
		}
	}

	public synchronized static void removeFilterFromReport(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn) throws ObjectNotFoundException,
			CantDoThatException, MissingParametersException, DisallowedException, SQLException,
			CodingErrorException {
		BaseReportInfo baseReport = ServletUtilMethods.getReportForRequest(sessionData, request,
				databaseDefn, ServletUtilMethods.USE_SESSION);
		if (!(baseReport instanceof SimpleReportInfo)) {
			throw new CantDoThatException(
					"You can only remove joins from normal reports, not union reports");
		}
		SimpleReportInfo report = (SimpleReportInfo) baseReport;
		String internalFilterName = request.getParameter("internalfiltername");
		if (internalFilterName == null) {
			throw new MissingParametersException(
					"'internalfiltername' parameter is required to identify the filter for removal.");
		}
		ReportFilterInfo reportFilter = report.getFilterByInternalName(internalFilterName);
		// begin updating model and persisting changes
		Connection conn = null;
		try {
			HibernateUtil.startHibernateTransaction();
			conn = databaseDefn.getDataSource().getConnection();
			conn.setAutoCommit(false);
			// remove the report filter
			databaseDefn.removeFilterFromReport(request, conn, report, reportFilter);
			conn.commit();
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (SQLException sqlex) {
			rollbackConnections(conn);
			// return reportfilter to memory
			report.addFilter(reportFilter);
			throw new CantDoThatException("Filter removal failed", sqlex);
		} catch (HibernateException hex) {
			rollbackConnections(conn);
			// return reportfilter to memory
			report.addFilter(reportFilter);
			throw new CantDoThatException("Filter removal failed", hex);
		} catch (AgileBaseException pex) {
			rollbackConnections(conn);
			// return reportfilter to memory
			report.addFilter(reportFilter);
			throw new CantDoThatException("Filter removal failed", pex);
		} finally {
			if (conn != null) {
				conn.close();
			}
			HibernateUtil.closeSession();
		}
	}

	/**
	 * !!!! NO PARAMETER MAY BE NULL !!!
	 * 
	 * @param leftInternalTableName
	 * @param leftInternalReportName
	 * @param leftInternalFieldName
	 * @param joinType
	 * @param rightInternalTableName
	 * @param rightInternalReportName
	 * @param rightInternalFieldName
	 * @throws MissingParametersException
	 * @throws ObjectNotFoundException
	 * @throws CodingErrorException
	 */
	public static JoinClauseInfo generateJoinObject(HttpServletRequest request,
			String leftInternalTableName, String leftInternalReportName,
			String leftInternalFieldName, JoinType joinType, String rightInternalTableName,
			String rightInternalReportName, String rightInternalFieldName, DatabaseInfo databaseDefn)
			throws MissingParametersException, ObjectNotFoundException, CodingErrorException,
			DisallowedException {
		// ensure fields and join type parameters are supplied
		if ((leftInternalTableName == null && leftInternalReportName == null)
				|| (rightInternalTableName == null && rightInternalReportName == null)
				|| leftInternalFieldName == null || rightInternalFieldName == null) {
			throw new CodingErrorException("generateJoinObject cannot be passed a null string");
		}
		if ((leftInternalTableName.equals("") && leftInternalReportName.equals(""))
				|| (rightInternalTableName.equals("") && rightInternalReportName.equals(""))
				|| leftInternalFieldName.equals("") || rightInternalFieldName.equals("")) {
			throw new MissingParametersException(
					"'leftinternalfieldname', 'jointype' and 'rightinternalfieldname' parameters must all be present in a request to add a join. Also a field owner (either table or report) must be specified for the left and right fields");
		}
		// declare join field objects for join clauses
		BaseField leftTableField = null;
		ReportFieldInfo leftReportField = null;
		BaseField rightTableField = null;
		ReportFieldInfo rightReportField = null;
		// Check for a (left) report/table name, (if both were supplied make use
		// of the report)
		if (!leftInternalReportName.equals("")) {
			TableInfo leftTable = databaseDefn.findTableContainingReport(request,
					leftInternalReportName);
			BaseReportInfo leftReport = leftTable.getReport(leftInternalReportName);
			leftReportField = leftReport.getReportField(leftInternalFieldName);
		} else if (!leftInternalTableName.equals("")) {
			TableInfo leftTable = databaseDefn.getTable(request, leftInternalTableName);
			leftTableField = leftTable.getField(leftInternalFieldName);
		} else {
			throw new MissingParametersException(
					"A field owner (either table or report) must be specified for both the left and right fields");
		}
		// Check for a (right) report/table name, (if both were supplied make
		// use of the report)
		if (!rightInternalReportName.equals("")) {
			TableInfo rightTable = databaseDefn.findTableContainingReport(request,
					rightInternalReportName);
			BaseReportInfo rightReport = rightTable.getReport(rightInternalReportName);
			rightReportField = rightReport.getReportField(rightInternalFieldName);
		} else if (!rightInternalTableName.equals("")) {
			TableInfo rightTable = databaseDefn.getTable(request, rightInternalTableName);
			rightTableField = rightTable.getField(rightInternalFieldName);
		} else {
			throw new MissingParametersException(
					"A field owner (either table or report) must be specified for both the left and right fields");
		}
		JoinClauseInfo join = null;
		if (leftTableField != null && rightTableField != null) {
			join = new JoinClause(leftTableField, rightTableField, joinType);
		} else if (leftTableField != null && rightReportField != null) {
			join = new JoinClause(leftTableField, rightReportField, joinType);
		} else if (leftReportField != null && rightTableField != null) {
			join = new JoinClause(leftReportField, rightTableField, joinType);
		} else if (leftReportField != null && rightReportField != null) {
			join = new JoinClause(leftReportField, rightReportField, joinType);
		} else {
			throw new CodingErrorException("Unexpected state reached when creating JoinClause");
		}
		return join;
	}

	public synchronized static void addJoinToReport(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn) throws ObjectNotFoundException,
			CantDoThatException, MissingParametersException, DisallowedException, SQLException,
			CodingErrorException {
		BaseReportInfo baseReport = ServletUtilMethods.getReportForRequest(sessionData, request,
				databaseDefn, ServletUtilMethods.USE_SESSION);
		if (!(baseReport instanceof SimpleReportInfo)) {
			throw new CantDoThatException(
					"You can only add joins to normal reports, not union reports");
		}
		SimpleReportInfo report = (SimpleReportInfo) baseReport;
		String leftInternalTableName = "";
		String leftInternalReportName = (request.getParameter("leftinternalreportname") == null) ? ""
				: request.getParameter("leftinternalreportname");
		if (!leftInternalReportName.equals("")) {
			TableInfo table = databaseDefn.findTableContainingReport(request,
					leftInternalReportName);
			if (table.getDefaultReport().getInternalReportName().equals(leftInternalReportName)) {
				leftInternalTableName = table.getInternalTableName();
				leftInternalReportName = "";
			}
		}
		if (leftInternalTableName.equals("") && leftInternalReportName.equals("")) {
			leftInternalTableName = (request.getParameter("leftinternaltablename") == null) ? ""
					: request.getParameter("leftinternaltablename");
		}
		String leftInternalFieldName = (request.getParameter("leftinternalfieldname") == null) ? ""
				: request.getParameter("leftinternalfieldname");
		String joinTypeString = (request.getParameter("jointype") == null) ? "" : request
				.getParameter("jointype");
		JoinType joinType = JoinType.valueOf(joinTypeString.toUpperCase());
		String rightInternalTableName = "";
		String rightInternalReportName = (request.getParameter("rightinternalreportname") == null) ? ""
				: request.getParameter("rightinternalreportname");
		if (!rightInternalReportName.equals("")) {
			TableInfo table = databaseDefn.findTableContainingReport(request,
					rightInternalReportName);
			if (table.getDefaultReport().getInternalReportName().equals(rightInternalReportName)) {
				rightInternalTableName = table.getInternalTableName();
				rightInternalReportName = "";
			}
		}
		if (rightInternalTableName.equals("") && rightInternalReportName.equals("")) {
			rightInternalTableName = (request.getParameter("rightinternaltablename") == null) ? ""
					: request.getParameter("rightinternaltablename");
		}
		String rightInternalFieldName = (request.getParameter("rightinternalfieldname") == null) ? ""
				: request.getParameter("rightinternalfieldname");
		JoinClauseInfo join = generateJoinObject(request, leftInternalTableName,
				leftInternalReportName, leftInternalFieldName, joinType, rightInternalTableName,
				rightInternalReportName, rightInternalFieldName, databaseDefn);
		// begin updating model and persisting changes
		Connection conn = null;
		try {
			HibernateUtil.startHibernateTransaction();
			conn = databaseDefn.getDataSource().getConnection();
			conn.setAutoCommit(false);
			databaseDefn.addJoinToReport(request, conn, report, join);
			conn.commit();
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (SQLException sqlex) {
			rollbackConnections(conn);
			// remove join from memory
			report.removeJoin(join);
			String errorMessage = "Join addition failed";
			if (sqlex.getMessage().contains("No operator matches the given name and argument type")) {
				errorMessage += ". You're trying to join two fields of different types (e.g. a text field to a number field). They have to be the same";
			} else {
				errorMessage += ". " + Helpers.replaceInternalNames(sqlex.getMessage(), report);
				if (sqlex.getMessage().contains("column reference")
						&& sqlex.getMessage().contains(" is ambiguous")) {
					errorMessage += ". It may be that a calculation using this field needs to specifically include the field's table or report name";
				}
			}
			throw new CantDoThatException(errorMessage, sqlex);
		} catch (HibernateException hex) {
			rollbackConnections(conn);
			// remove join from memory
			report.removeJoin(join);
			throw new CantDoThatException("Join addition failed", hex);
		} catch (AgileBaseException pex) {
			rollbackConnections(conn);
			// remove join from memory
			report.removeJoin(join);
			throw new CantDoThatException("Join addition failed", pex);
		} finally {
			if (conn != null) {
				conn.close();
			}
			HibernateUtil.closeSession();
		}
	}

	public synchronized static void removeJoinFromReport(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn) throws ObjectNotFoundException,
			CantDoThatException, MissingParametersException, DisallowedException, SQLException,
			CodingErrorException {
		BaseReportInfo baseReport = ServletUtilMethods.getReportForRequest(sessionData, request,
				databaseDefn, ServletUtilMethods.USE_SESSION);
		if (!(baseReport instanceof SimpleReportInfo)) {
			throw new CantDoThatException(
					"You can only remove joins from normal reports, not union reports");
		}
		SimpleReportInfo report = (SimpleReportInfo) baseReport;
		String internalJoinName = request.getParameter("internaljoinname");
		if (internalJoinName == null) {
			throw new MissingParametersException(
					"'internaljoinname' parameter is required to identify the join for removal.");
		}
		// begin updating model and persisting changes
		JoinClauseInfo join = report.getJoinByInternalName(internalJoinName); // store
		// for
		// rollback
		Connection conn = null;
		try {
			HibernateUtil.startHibernateTransaction();
			conn = databaseDefn.getDataSource().getConnection();
			conn.setAutoCommit(false);
			// remove the report filter
			databaseDefn.removeJoinFromReport(request, conn, report, join);
			conn.commit();
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (SQLException sqlex) {
			rollbackConnections(conn);
			// return report join to memory
			report.addJoin(join);
			throw new CantDoThatException(
					"Removing this join would cause an error as the joined data is still in use in the report",
					sqlex);
		} catch (HibernateException hex) {
			rollbackConnections(conn);
			// return report join to memory
			report.addJoin(join);
			throw new CantDoThatException("Join removal failed", hex);
		} catch (AgileBaseException abex) {
			rollbackConnections(conn);
			// return report join to memory
			report.addJoin(join);
			throw new CantDoThatException("Join removal failed: " + abex.getMessage(), abex);
		} finally {
			if (conn != null) {
				conn.close();
			}
			HibernateUtil.closeSession();
		}
	}

	public synchronized static void setChartFilter(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn) throws DisallowedException,
			MissingParametersException, ObjectNotFoundException, CantDoThatException, SQLException {
		BaseReportInfo report = ServletUtilMethods.getReportForRequest(sessionData, request,
				databaseDefn, ServletUtilMethods.USE_SESSION);
		String summaryFilterString = request.getParameter("summaryfilter");
		if (summaryFilterString == null) {
			throw new MissingParametersException(
					"summaryfilter parameter is needed to set the summary filter");
		}
		SummaryFilter summaryFilter = null;
		if (!summaryFilterString.equals("")) {
			summaryFilter = SummaryFilter.valueOf(summaryFilterString.toUpperCase());
		}
		try {
			HibernateUtil.startHibernateTransaction();
			databaseDefn.setChartFilter(request, report, summaryFilter);
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (HibernateException hex) {
			rollbackConnections(null);
			databaseDefn.setChartFilter(request, report, null);
			throw new CantDoThatException("summary filter addition failed", hex);
		} catch (AgileBaseException pex) {
			rollbackConnections(null);
			databaseDefn.setChartFilter(request, report, null);
			throw new CantDoThatException("summary filter addition failed", pex);
		} catch (SQLException sqlex) {
			rollbackConnections(null);
			databaseDefn.setChartFilter(request, report, null);
			throw new CantDoThatException("summary filter addition failed", sqlex);
		} finally {
			HibernateUtil.closeSession();
		}
	}

	public synchronized static void setChartFilterField(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn) throws DisallowedException,
			MissingParametersException, ObjectNotFoundException, CantDoThatException, SQLException {
		BaseReportInfo report = ServletUtilMethods.getReportForRequest(sessionData, request,
				databaseDefn, ServletUtilMethods.USE_SESSION);
		String internalFieldName = request.getParameter("internalfieldname");
		if (internalFieldName == null) {
			throw new MissingParametersException(
					"internalfieldname parameter is needed to set the summary filter");
		}
		ReportFieldInfo reportField = null;
		if (!internalFieldName.equals("")) {
			reportField = report.getReportField(internalFieldName);
		}
		try {
			HibernateUtil.startHibernateTransaction();
			databaseDefn.setChartFilterField(request, report, reportField);
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (HibernateException hex) {
			rollbackConnections(null);
			databaseDefn.setChartFilter(request, report, null);
			throw new CantDoThatException("summary filter field addition failed", hex);
		} catch (AgileBaseException pex) {
			rollbackConnections(null);
			databaseDefn.setChartFilter(request, report, null);
			throw new CantDoThatException("summary filter field addition failed", pex);
		} catch (SQLException sqlex) {
			rollbackConnections(null);
			databaseDefn.setChartFilter(request, report, null);
			throw new CantDoThatException("summary filter field addition failed", sqlex);
		} finally {
			HibernateUtil.closeSession();
		}
	}

	public synchronized static void setChartRange(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn) throws DisallowedException,
			MissingParametersException, ObjectNotFoundException, CantDoThatException, SQLException {
		BaseReportInfo report = ServletUtilMethods.getReportForRequest(sessionData, request,
				databaseDefn, ServletUtilMethods.USE_SESSION);
		String rangeDirectionString = request.getParameter("rangedirection");
		String rangePercentString = request.getParameter("rangepercent");
		if (rangeDirectionString == null || rangePercentString == null) {
			throw new MissingParametersException(
					"rangedirection and rangepercent parameters are needed to set the summary filter");
		}
		boolean rangeDirection = Helpers.valueRepresentsBooleanTrue(rangeDirectionString);
		int rangePercent = Integer.valueOf(rangePercentString);
		try {
			HibernateUtil.startHibernateTransaction();
			databaseDefn.setChartRange(request, report, rangePercent, rangeDirection);
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (HibernateException hex) {
			rollbackConnections(null);
			databaseDefn.setChartRange(request, report, 100, true);
			throw new CantDoThatException("summary range addition failed", hex);
		} catch (AgileBaseException pex) {
			rollbackConnections(null);
			databaseDefn.setChartRange(request, report, 100, true);
			throw new CantDoThatException("summary range addition failed", pex);
		} catch (SQLException sqlex) {
			rollbackConnections(null);
			databaseDefn.setChartRange(request, report, 100, true);
			throw new CantDoThatException("summary range addition failed", sqlex);
		} finally {
			HibernateUtil.closeSession();
		}
	}

	public synchronized static void addGroupingToChart(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn) throws DisallowedException,
			CantDoThatException, MissingParametersException, ObjectNotFoundException {
		BaseReportInfo report = ServletUtilMethods.getReportForRequest(sessionData, request,
				databaseDefn, ServletUtilMethods.USE_SESSION);
		String internalFieldName = request.getParameter("internalfieldname");
		if (internalFieldName == null) {
			throw new MissingParametersException(
					"'internalfieldname' parameter is required to add a grouping field to a report summary");
		}
		SummaryGroupingModifier groupingModifer = null;
		if (internalFieldName.contains("_")) {
			String groupingModifierString = internalFieldName.replaceAll("^.*\\_", "");
			try {
				groupingModifer = SummaryGroupingModifier.valueOf("DATE_"
						+ groupingModifierString.toUpperCase());
			} catch (IllegalArgumentException iaex) {
				throw new ObjectNotFoundException("No grouping modifier found for "
						+ groupingModifierString, iaex);
			}
			internalFieldName = internalFieldName.replaceAll("\\_.*$", "");
		}
		ReportFieldInfo groupingReportField = report.getReportField(internalFieldName);
		try {
			HibernateUtil.startHibernateTransaction();
			databaseDefn.addGroupingToChart(request, groupingReportField, groupingModifer);
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (HibernateException hex) {
			rollbackConnections(null);
			groupingReportField.getParentReport().getChart().removeGrouping(groupingReportField);
			throw new CantDoThatException("summary grouping addition failed", hex);
		} catch (AgileBaseException pex) {
			rollbackConnections(null);
			groupingReportField.getParentReport().getChart().removeGrouping(groupingReportField);
			throw new CantDoThatException("summary grouping addition failed", pex);
		} catch (SQLException sqlex) {
			rollbackConnections(null);
			groupingReportField.getParentReport().getChart().removeGrouping(groupingReportField);
			throw new CantDoThatException("summary grouping addition failed", sqlex);
		} finally {
			HibernateUtil.closeSession();
		}
	}

	public synchronized static void removeGroupingFromChart(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn) throws DisallowedException,
			MissingParametersException, ObjectNotFoundException, CantDoThatException {
		BaseReportInfo report = ServletUtilMethods.getReportForRequest(sessionData, request,
				databaseDefn, ServletUtilMethods.USE_SESSION);
		String internalFieldName = request.getParameter("internalfieldname");
		if (internalFieldName == null) {
			throw new MissingParametersException(
					"'internalfieldname' parameter is required to remove a grouping field from a report summary");
		}
		ChartInfo chart = report.getChart();
		if (chart.getGroupings().size() == 1) {
			for (ChartAggregateInfo aggregateFunction : chart.getAggregateFunctions()) {
				if (aggregateFunction.getAggregateFunction().equals(
						AggregateFunction.CUMULATIVE_COUNT)
						|| aggregateFunction.getAggregateFunction().equals(
								AggregateFunction.CUMULATIVE_SUM)) {
					// Can't leave a cumulative aggregate with no groupings, it
					// will break the SQL
					throw new CantDoThatException("Please remove the cumulative function first");
				}
			}
		}
		ReportFieldInfo groupingReportField = chart.getGroupingReportField(internalFieldName);
		HibernateUtil.startHibernateTransaction();
		try {
			databaseDefn.removeGroupingFromChart(request, groupingReportField);
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (HibernateException hex) {
			rollbackConnections(null);
			// TODO: making the second arg. null will remove any previous
			// grouping modifier, but we can perhaps sort this out at a later
			// date
			chart.addGrouping(groupingReportField, null);
			throw new CantDoThatException("summary grouping removal failed", hex);
		} catch (AgileBaseException pex) {
			rollbackConnections(null);
			groupingReportField.getParentReport().getChart().addGrouping(groupingReportField, null);
			throw new CantDoThatException("summary grouping removal failed", pex);
		} catch (SQLException sqlex) {
			rollbackConnections(null);
			groupingReportField.getParentReport().getChart().addGrouping(groupingReportField, null);
			throw new CantDoThatException("summary grouping removal failed", sqlex);
		} finally {
			HibernateUtil.closeSession();
		}
	}

	public synchronized static void addFunctionToChart(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn) throws DisallowedException,
			MissingParametersException, ObjectNotFoundException, CantDoThatException {
		BaseReportInfo report = ServletUtilMethods.getReportForRequest(sessionData, request,
				databaseDefn, ServletUtilMethods.USE_SESSION);
		String internalFieldName = request.getParameter("internalfieldname");
		String secondaryInternalFieldName = request.getParameter("secondaryinternalfieldname");
		String functionName = request.getParameter("function");
		if (internalFieldName == null || functionName == null) {
			throw new MissingParametersException(
					"'internalfieldname' and 'function' parameters are required to add an aggregate function to a report summary");
		}
		ReportFieldInfo functionReportField = report.getReportField(internalFieldName);
		ReportFieldInfo secondaryFunctionReportField = null;
		if (secondaryInternalFieldName != null) {
			if (!secondaryInternalFieldName.equals("")) {
				secondaryFunctionReportField = report.getReportField(secondaryInternalFieldName);
			}
		}
		AggregateFunction function = AggregateFunction.valueOf(functionName.toUpperCase());
		ChartAggregateInfo addedAggFn = null;
		if (secondaryFunctionReportField == null) {
			addedAggFn = new ChartAggregateDefn(function, functionReportField);
		} else {
			addedAggFn = new ChartAggregateDefn(function, functionReportField,
					secondaryFunctionReportField);
		}
		try {
			HibernateUtil.startHibernateTransaction();
			databaseDefn.addFunctionToChart(request, addedAggFn);
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (HibernateException hex) {
			rollbackConnections(null);
			try {
				functionReportField.getParentReport().getChart()
						.removeFunction(addedAggFn.getInternalAggregateName());
			} catch (ObjectNotFoundException onfex) {
				logger.error("Unable to rollback function addition - maybe it didn't get added");
			}
			throw new CantDoThatException("summary function addition failed", hex);
		} catch (AgileBaseException pex) {
			rollbackConnections(null);
			try {
				functionReportField.getParentReport().getChart()
						.removeFunction(addedAggFn.getInternalAggregateName());
			} catch (ObjectNotFoundException onfex) {
				logger.error("Unable to rollback function addition - maybe it didn't get added");
			}
			throw new CantDoThatException("summary function addition failed", pex);
		} catch (SQLException sqlex) {
			rollbackConnections(null);
			try {
				functionReportField.getParentReport().getChart()
						.removeFunction(addedAggFn.getInternalAggregateName());
			} catch (ObjectNotFoundException onfex) {
				logger.error("Unable to rollback function addition - maybe it didn't get added");
			}
			throw new CantDoThatException("summary function addition failed due to: " + sqlex,
					sqlex);
		} finally {
			HibernateUtil.closeSession();
		}
	}

	public synchronized static void removeFunctionFromChart(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn) throws DisallowedException,
			MissingParametersException, ObjectNotFoundException, CantDoThatException {
		BaseReportInfo report = ServletUtilMethods.getReportForRequest(sessionData, request,
				databaseDefn, ServletUtilMethods.USE_SESSION);
		String internalAggregateName = request.getParameter("internalaggregatename");
		if (internalAggregateName == null) {
			throw new MissingParametersException(
					"'internalaggregatename' parameter is required to remove a function field from a report summary");
		}
		ChartAggregateInfo aggregateToRemove = report.getChart()
				.getAggregateFunctionByInternalName(internalAggregateName);
		AggregateFunction aggFunctionType = aggregateToRemove.getAggregateFunction();
		ReportFieldInfo functionReportField = aggregateToRemove.getReportField();
		ReportFieldInfo secondaryReportField = aggregateToRemove.getSecondaryReportField();
		try {
			HibernateUtil.startHibernateTransaction();
			databaseDefn.removeFunctionFromChart(request, report, internalAggregateName);
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (HibernateException hex) {
			rollbackConnections(null);
			functionReportField.getParentReport().getChart().addFunction(aggregateToRemove);
			throw new CantDoThatException("summary function removal failed", hex);
		} catch (AgileBaseException pex) {
			rollbackConnections(null);
			functionReportField.getParentReport().getChart().addFunction(aggregateToRemove);
			throw new CantDoThatException("summary function removal failed", pex);
		} catch (SQLException sqlex) {
			rollbackConnections(null);
			functionReportField.getParentReport().getChart().addFunction(aggregateToRemove);
			throw new CantDoThatException("summary function removal failed", sqlex);
		} finally {
			HibernateUtil.closeSession();
		}
	}

	/**
	 * Takes the existing 'scratchpad' summary report, gives it a name
	 * (specified by the user) and saves it to a permanent collection of
	 * summaries in the report. Then the scratchpad summary is blanked
	 */
	public synchronized static void saveChart(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn) throws DisallowedException,
			MissingParametersException, ObjectNotFoundException, CantDoThatException {
		BaseReportInfo report = ServletUtilMethods.getReportForRequest(sessionData, request,
				databaseDefn, ServletUtilMethods.USE_SESSION);
		String chartTitle = request.getParameter("summarytitle");
		if (chartTitle == null) {
			throw new MissingParametersException(
					"'summarytitle' parameter is required to save a report summary");
		}
		try {
			HibernateUtil.startHibernateTransaction();
			databaseDefn.saveChart(request, report, chartTitle);
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (HibernateException hex) {
			rollbackConnections(null);
			// Could have some code to roll back memory state here
			// but will only add it if it becomes necessary
			throw new CantDoThatException("chart saving failed", hex);
		} catch (AgileBaseException pex) {
			rollbackConnections(null);
			throw new CantDoThatException("chart saving failed", pex);
		} finally {
			HibernateUtil.closeSession();
		}
	}

	public synchronized static void removeChart(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn) throws DisallowedException,
			MissingParametersException, ObjectNotFoundException, CantDoThatException {
		BaseReportInfo report = ServletUtilMethods.getReportForRequest(sessionData, request,
				databaseDefn, ServletUtilMethods.USE_SESSION);
		String summaryIdString = request.getParameter("summaryid");
		if (summaryIdString == null) {
			throw new MissingParametersException(
					"'summaryid' parameter is required to remove a chart");
		}
		long summaryId = Long.valueOf(summaryIdString);
		try {
			HibernateUtil.startHibernateTransaction();
			ChartInfo summaryToRemove = null;
			SUMMARY_LOOP: for (ChartInfo reportSummary : report.getSavedCharts()) {
				if (reportSummary.getId() == summaryId) {
					summaryToRemove = reportSummary;
					break SUMMARY_LOOP;
				}
			}
			if (summaryToRemove == null) {
				throw new ObjectNotFoundException("A chart with the ID " + summaryIdString
						+ " was not found in report " + report);
			}
			databaseDefn.removeChart(request, summaryToRemove);
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (HibernateException hex) {
			rollbackConnections(null);
			// Could have some code to roll back memory state here
			// but will only add it if it becomes necessary
			throw new CantDoThatException("chart removal failed", hex);
		} catch (AgileBaseException pex) {
			rollbackConnections(null);
			throw new CantDoThatException("chart removal failed", pex);
		} finally {
			HibernateUtil.closeSession();
		}
	}

	public synchronized static void hideReportFromUser(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn)
			throws MissingParametersException, ObjectNotFoundException, DisallowedException,
			CantDoThatException {
		// Authentication is here because we don't call any databaseDefn method
		if (!databaseDefn.getAuthManager().getAuthenticator()
				.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE)) {
			throw new DisallowedException(databaseDefn.getAuthManager().getLoggedInUser(request),
					PrivilegeType.ADMINISTRATE);
		}
		BaseReportInfo report = ServletUtilMethods.getReportForRequest(sessionData, request,
				databaseDefn, ServletUtilMethods.USE_SESSION);
		String internalUserName = request.getParameter("internalusername");
		if (internalUserName == null) {
			throw new MissingParametersException(
					"internalusername is necessary to specify the user when hiding a report from them");
		}
		AppUserInfo appUser = databaseDefn.getAuthManager().getUserByInternalName(request,
				internalUserName);
		try {
			HibernateUtil.startHibernateTransaction();
			HibernateUtil.activateObject(appUser);
			appUser.hideReport(report);
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (HibernateException hex) {
			rollbackConnections(null);
			throw new CantDoThatException("report hiding failed", hex);
		} finally {
			HibernateUtil.closeSession();
		}
	}

	public synchronized static void unhideReportFromUser(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn)
			throws MissingParametersException, ObjectNotFoundException, DisallowedException,
			CantDoThatException {
		// Authentication is here because we don't call any databaseDefn method
		if (!databaseDefn.getAuthManager().getAuthenticator()
				.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE)) {
			throw new DisallowedException(databaseDefn.getAuthManager().getLoggedInUser(request),
					PrivilegeType.ADMINISTRATE);
		}
		BaseReportInfo report = ServletUtilMethods.getReportForRequest(sessionData, request,
				databaseDefn, ServletUtilMethods.USE_SESSION);
		String internalUserName = request.getParameter("internalusername");
		if (internalUserName == null) {
			throw new MissingParametersException(
					"internalusername is necessary to specify the user when hiding a report from them");
		}
		AppUserInfo appUser = databaseDefn.getAuthManager().getUserByInternalName(request,
				internalUserName);
		try {
			HibernateUtil.startHibernateTransaction();
			HibernateUtil.activateObject(appUser);
			appUser.unhideReport(report);
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (HibernateException hex) {
			rollbackConnections(null);
			throw new CantDoThatException("report un-hiding failed", hex);
		} finally {
			HibernateUtil.closeSession();
		}
	}

	public synchronized static void addFormTable(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn)
			throws MissingParametersException, ObjectNotFoundException, DisallowedException,
			CantDoThatException {
		TableInfo table = ServletUtilMethods.getTableForRequest(sessionData, request, databaseDefn,
				ServletUtilMethods.USE_SESSION);
		AppUserInfo appUser = databaseDefn.getAuthManager().getUserByUserName(request,
				request.getRemoteUser());
		try {
			HibernateUtil.startHibernateTransaction();
			HibernateUtil.activateObject(appUser);
			appUser.addFormTable(table);
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (HibernateException hex) {
			rollbackConnections(null);
			throw new CantDoThatException("adding table " + table + " to forms for user " + appUser
					+ " failed", hex);
		} finally {
			HibernateUtil.closeSession();
		}
	}

	public synchronized static void removeFormTable(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn)
			throws MissingParametersException, ObjectNotFoundException, DisallowedException,
			CantDoThatException {
		TableInfo table = ServletUtilMethods.getTableForRequest(sessionData, request, databaseDefn,
				ServletUtilMethods.USE_SESSION);
		AppUserInfo appUser = databaseDefn.getAuthManager().getUserByUserName(request,
				request.getRemoteUser());
		try {
			HibernateUtil.startHibernateTransaction();
			HibernateUtil.activateObject(appUser);
			appUser.removeFormTable(table);
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (HibernateException hex) {
			rollbackConnections(null);
			throw new CantDoThatException("removing table " + table + " from forms for user "
					+ appUser + " failed", hex);
		} finally {
			HibernateUtil.closeSession();
		}
	}

	public synchronized static void addOperationalDashboardReport(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn)
			throws MissingParametersException, ObjectNotFoundException, DisallowedException,
			CantDoThatException {
		BaseReportInfo report = ServletUtilMethods.getReportForRequest(sessionData, request,
				databaseDefn, ServletUtilMethods.USE_SESSION);
		AppUserInfo appUser = databaseDefn.getAuthManager().getUserByUserName(request,
				request.getRemoteUser());
		try {
			HibernateUtil.startHibernateTransaction();
			HibernateUtil.activateObject(appUser);
			appUser.addOperationalDashboardReport(report);
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (HibernateException hex) {
			rollbackConnections(null);
			throw new CantDoThatException("adding report " + report
					+ " to operational dashboard of user " + appUser + " failed", hex);
		} finally {
			HibernateUtil.closeSession();
		}
	}

	public synchronized static void removeOperationalDashboardReport(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn)
			throws MissingParametersException, ObjectNotFoundException, DisallowedException,
			CantDoThatException {
		BaseReportInfo report = ServletUtilMethods.getReportForRequest(sessionData, request,
				databaseDefn, ServletUtilMethods.USE_SESSION);
		AppUserInfo appUser = databaseDefn.getAuthManager().getUserByUserName(request,
				request.getRemoteUser());
		try {
			HibernateUtil.startHibernateTransaction();
			HibernateUtil.activateObject(appUser);
			appUser.removeOperationalDashboardReport(report);
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (HibernateException hex) {
			rollbackConnections(null);
			throw new CantDoThatException("report un-hiding failed", hex);
		} finally {
			HibernateUtil.closeSession();
		}
	}

	public synchronized static void setCalendarSyncable(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn, boolean calendarSyncable)
			throws MissingParametersException, ObjectNotFoundException, DisallowedException,
			CantDoThatException {
		BaseReportInfo report = ServletUtilMethods.getReportForRequest(sessionData, request,
				databaseDefn, ServletUtilMethods.USE_SESSION);
		if (!databaseDefn
				.getAuthManager()
				.getAuthenticator()
				.loggedInUserAllowedTo(request, PrivilegeType.VIEW_TABLE_DATA,
						report.getParentTable())) {
			throw new DisallowedException(databaseDefn.getAuthManager().getLoggedInUser(request),
					PrivilegeType.VIEW_TABLE_DATA);
		}
		try {
			HibernateUtil.startHibernateTransaction();
			HibernateUtil.activateObject(report);
			report.setCalendarSyncable(calendarSyncable);
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (HibernateException hex) {
			rollbackConnections(null);
			throw new CantDoThatException("setting calendar syncable failed", hex);
		} finally {
			HibernateUtil.closeSession();
		}
	}

	public synchronized static void setReportWordCloudField(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn)
			throws MissingParametersException, ObjectNotFoundException, DisallowedException,
			CantDoThatException {
		BaseReportInfo report = ServletUtilMethods.getReportForRequest(sessionData, request,
				databaseDefn, true);
		String internalFieldName = request.getParameter("internalfieldname");
		if (internalFieldName == null) {
			throw new MissingParametersException(
					"internalfieldname parameter needed to set a report word cloud field");
		}
		ReportFieldInfo wordCloudReportField = null;
		if (!internalFieldName.equals("")) {
			wordCloudReportField = report.getReportField(internalFieldName);
		}
		try {
			HibernateUtil.startHibernateTransaction();
			HibernateUtil.activateObject(report);
			report.setWordCloudField(wordCloudReportField);
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (HibernateException hex) {
			rollbackConnections(null);
			throw new CantDoThatException("Setting report word cloud failed", hex);
		} finally {
			HibernateUtil.closeSession();
		}
	}

	public synchronized static void updateMap(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn)
			throws MissingParametersException, ObjectNotFoundException, DisallowedException {
		BaseReportInfo report = ServletUtilMethods.getReportForRequest(sessionData, request,
				databaseDefn, true);
		String postcodeFieldInternalName = request.getParameter("postcodefieldinternalname");
		String colourFieldInternalName = request.getParameter("colourfieldinternalname");
		String categoryFieldInternalName = request.getParameter("categoryfieldinternalname");
		if ((postcodeFieldInternalName == null) && (colourFieldInternalName == null)
				&& (categoryFieldInternalName == null)) {
			throw new MissingParametersException(
					"postcodefieldinternalname, colourfieldinternalname or categoryfieldinternalname are needed to update a map");
		}
		ReportFieldInfo postcodeField = null;
		ReportFieldInfo colourField = null;
		ReportFieldInfo categoryField = null;
		if (postcodeFieldInternalName != null) {
			if (!postcodeFieldInternalName.equals("")) {
				postcodeField = report.getReportField(postcodeFieldInternalName);
			}
		}
		if (colourFieldInternalName != null) {
			if (!colourFieldInternalName.equals("")) {
				colourField = report.getReportField(colourFieldInternalName);
			}
		}
		if (categoryFieldInternalName != null) {
			if (!categoryFieldInternalName.equals("")) {
				categoryField = report.getReportField(categoryFieldInternalName);
			}
		}
		try {
			HibernateUtil.startHibernateTransaction();
			databaseDefn.updateMap(request, report, postcodeField, colourField, categoryField);
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (HibernateException hex) {
			rollbackConnections(null);
		} finally {
			HibernateUtil.closeSession();
		}
	}

	public synchronized static void setUserDefaultReport(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn)
			throws MissingParametersException, ObjectNotFoundException, DisallowedException,
			CantDoThatException {
		String internalUserName = request.getParameter("internalusername");
		if (internalUserName == null) {
			throw new MissingParametersException(
					"internalusername is necessary to specify the user when setting a default report for them");
		}
		// An empty internalreportname parameter will produce a null report
		BaseReportInfo report = null;
		String internalReportName = request.getParameter("internalreportname");
		if (internalReportName == null) {
			throw new MissingParametersException(
					"internalreportname is necessary when setting a default report for a user");
		}
		if (!internalReportName.equals("")) {
			report = ServletUtilMethods.getReportForRequest(sessionData, request, databaseDefn,
					ServletUtilMethods.DO_NOT_USE_SESSION);
		}
		AppUserInfo appUser = databaseDefn.getAuthManager().getUserByInternalName(request,
				internalUserName);
		try {
			HibernateUtil.startHibernateTransaction();
			HibernateUtil.activateObject(appUser);
			appUser.setDefaultReport(report);
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (HibernateException hex) {
			rollbackConnections(null);
			throw new CantDoThatException("setting default report failed", hex);
		} finally {
			HibernateUtil.closeSession();
		}
	}

	@Deprecated
	public synchronized static void contractSection(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn)
			throws MissingParametersException, ObjectNotFoundException, DisallowedException,
			CantDoThatException {
		String internalFieldName = request.getParameter("internalfieldname");
		if (internalFieldName == null) {
			throw new MissingParametersException(
					"An internalFieldName to identify the section is necessary to contract a section");
		}
		TableInfo table = sessionData.getTable();
		// Get the field just to ensure the identifier is valid
		BaseField field = table.getField(internalFieldName);
		AppUserInfo appUser = databaseDefn.getAuthManager().getLoggedInUser(request);
		try {
			HibernateUtil.startHibernateTransaction();
			HibernateUtil.activateObject(appUser);
			appUser.contractSection(field.getInternalFieldName());
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (HibernateException hex) {
			rollbackConnections(null);
			throw new CantDoThatException("contracting section " + field + " in table " + table
					+ " failed", hex);
		} finally {
			HibernateUtil.closeSession();
		}
	}

	@Deprecated
	public synchronized static void expandSection(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn)
			throws MissingParametersException, ObjectNotFoundException, DisallowedException,
			CantDoThatException {
		String internalFieldName = request.getParameter("internalfieldname");
		if (internalFieldName == null) {
			throw new MissingParametersException(
					"An internalFieldName to identify the section is necessary to expand a section");
		}
		TableInfo table = sessionData.getTable();
		// Get the field just to ensure the identifier is valid
		BaseField field = table.getField(internalFieldName);
		AppUserInfo appUser = databaseDefn.getAuthManager().getLoggedInUser(request);
		try {
			HibernateUtil.startHibernateTransaction();
			HibernateUtil.activateObject(appUser);
			appUser.expandSection(field.getInternalFieldName());
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (HibernateException hex) {
			rollbackConnections(null);
			throw new CantDoThatException("expanding section " + field + " in table " + table
					+ " failed", hex);
		} finally {
			HibernateUtil.closeSession();
		}
	}

	public synchronized static void enableDisableApp(HttpServletRequest request,
			DatabaseInfo databaseDefn) throws MissingParametersException, ObjectNotFoundException,
			DisallowedException, CantDoThatException {
		String app = request.getParameter("app");
		if (app == null) {
			throw new MissingParametersException("An app parameter is needed to add an app");
		}
		boolean enable = Helpers.valueRepresentsBooleanTrue(request.getParameter("enable"));
		AuthManagerInfo authManager = databaseDefn.getAuthManager();
		AppUserInfo user = authManager.getLoggedInUser(request);
		CompanyInfo company = user.getCompany();
		if (!authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.ADMINISTRATE)) {
			throw new DisallowedException(user, PrivilegeType.ADMINISTRATE);
		}
		try {
			HibernateUtil.startHibernateTransaction();
			HibernateUtil.activateObject(company);
			if (enable) {
				company.addApp(app);
			} else {
				company.removeApp(app);
			}
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (HibernateException hex) {
			rollbackConnections(null);
			throw new CantDoThatException("Adding/removing app " + app + " to company " + company
					+ " by user " + user + " failed", hex);
		} finally {
			HibernateUtil.closeSession();
		}
	}

	private static final SimpleLogger logger = new SimpleLogger(ServletSchemaMethods.class);
}