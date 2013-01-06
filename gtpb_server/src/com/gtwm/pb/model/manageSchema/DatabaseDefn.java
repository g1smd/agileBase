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
package com.gtwm.pb.model.manageSchema;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import com.gtwm.pb.auth.AuthManager;
import com.gtwm.pb.model.interfaces.AuthenticatorInfo;
import com.gtwm.pb.model.interfaces.CompanyInfo;
import com.gtwm.pb.model.interfaces.FormTabInfo;
import com.gtwm.pb.model.interfaces.ModuleInfo;
import com.gtwm.pb.model.interfaces.AuthManagerInfo;
import com.gtwm.pb.model.interfaces.ReportMapInfo;
import com.gtwm.pb.model.interfaces.ReportSortInfo;
import com.gtwm.pb.model.interfaces.BaseFieldDescriptorOptionInfo;
import com.gtwm.pb.model.interfaces.TextFieldDescriptorOptionInfo;
import com.gtwm.pb.model.interfaces.BooleanFieldDescriptorOptionInfo;
import com.gtwm.pb.model.interfaces.ListFieldDescriptorOptionInfo;
import com.gtwm.pb.model.interfaces.ChartAggregateInfo;
import com.gtwm.pb.model.interfaces.ChartDataInfo;
import com.gtwm.pb.model.interfaces.SessionDataInfo;
import com.gtwm.pb.model.interfaces.DatabaseInfo;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.SimpleReportInfo;
import com.gtwm.pb.model.interfaces.ReportFieldInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.ReportFilterInfo;
import com.gtwm.pb.model.interfaces.ReportCalcFieldInfo;
import com.gtwm.pb.model.interfaces.ChartInfo;
import com.gtwm.pb.model.interfaces.ChartGroupingInfo;
import com.gtwm.pb.model.interfaces.DataManagementInfo;
import com.gtwm.pb.model.interfaces.JoinClauseInfo;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.fields.DateField;
import com.gtwm.pb.model.interfaces.fields.CheckboxField;
import com.gtwm.pb.model.interfaces.fields.ReferencedReportDataField;
import com.gtwm.pb.model.interfaces.fields.RelationField;
import com.gtwm.pb.model.interfaces.fields.SequenceField;
import com.gtwm.pb.model.interfaces.fields.TextField;
import com.gtwm.pb.model.interfaces.fields.DecimalField;
import com.gtwm.pb.model.interfaces.fields.IntegerField;
import com.gtwm.pb.model.interfaces.fields.FileField;
import com.gtwm.pb.model.interfaces.FieldTypeDescriptorInfo;
import com.gtwm.pb.model.interfaces.AppUserInfo;
import com.gtwm.pb.auth.Authenticator;
import com.gtwm.pb.auth.DashboardPopulator;
import com.gtwm.pb.auth.DisallowedException;
import com.gtwm.pb.auth.PrivilegeType;
import com.gtwm.pb.model.manageData.DataManagement;
import com.gtwm.pb.model.manageData.ReportData;
import com.gtwm.pb.servlets.ServletSchemaMethods;
import com.gtwm.pb.util.Enumerations.AppAction;
import com.gtwm.pb.util.Enumerations.AttachmentType;
import com.gtwm.pb.util.Enumerations.HiddenFields;
import com.gtwm.pb.util.Enumerations.SummaryGroupingModifier;
import com.gtwm.pb.model.manageSchema.FieldTypeDescriptor.FieldCategory;
import com.gtwm.pb.model.manageSchema.BooleanFieldDescriptorOption.PossibleBooleanOptions;
import com.gtwm.pb.model.manageSchema.ListFieldDescriptorOption.FieldPrintoutSetting;
import com.gtwm.pb.model.manageSchema.ListFieldDescriptorOption.PossibleListOptions;
import com.gtwm.pb.model.manageSchema.ListFieldDescriptorOption.TextContentSizes;
import com.gtwm.pb.model.manageSchema.TextFieldDescriptorOption.PossibleTextOptions;
import com.gtwm.pb.model.manageSchema.fields.CheckboxFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.CommentFeedFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.DateFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.DecimalFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.IntegerFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.ReferencedReportDataFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.RelationFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.TextFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.SequenceFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.FileFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.SeparatorFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.options.BasicFieldOptions;
import com.gtwm.pb.model.manageSchema.fields.options.DateFieldOptions;
import com.gtwm.pb.model.manageSchema.fields.options.DecimalFieldOptions;
import com.gtwm.pb.model.manageSchema.fields.options.IntegerFieldOptions;
import com.gtwm.pb.model.manageSchema.fields.options.RelationFieldOptions;
import com.gtwm.pb.model.manageSchema.fields.options.TextFieldOptions;
import com.gtwm.pb.model.manageUsage.UsageLogger;
import com.gtwm.pb.util.AppProperties;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.Enumerations.FormStyle;
import com.gtwm.pb.util.Enumerations.ReportStyle;
import com.gtwm.pb.util.Enumerations.SummaryFilter;
import com.gtwm.pb.util.Enumerations.TextCase;
import com.gtwm.pb.util.HttpRequestUtil;
import com.gtwm.pb.util.InconsistentStateException;
import com.gtwm.pb.util.MissingParametersException;
import com.gtwm.pb.util.Naming;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.TableDependencyException;
import com.gtwm.pb.util.AgileBaseException;
import com.gtwm.pb.util.Enumerations.DatabaseFieldType;
import com.gtwm.pb.util.HibernateUtil;
import com.gtwm.pb.util.Helpers;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.commons.io.FileUtils;
import org.grlea.log.SimpleLogger;
import org.hibernate.HibernateException;
import org.hibernate.Session;

public final class DatabaseDefn implements DatabaseInfo {

	/**
	 * There should be one DatabaseInfo object per agileBase application instance.
	 * This constructor generates it. It bootstraps the application. All schema
	 * objects are loaded into memory from the pervasive store.
	 * 
	 * The authentication manager (AuthManagerInfo), store of all users, roles and
	 * permissions is loaded too.
	 * 
	 * Finally, the data manager (a DataManagementInfo object) is created and
	 * initialised
	 * 
	 * @throws CantDoThatException
	 *           If more than one Authenticator was found in the database
	 */
	public DatabaseDefn(DataSource relationalDataSource, String webAppRoot) throws SQLException,
			ObjectNotFoundException, CantDoThatException, MissingParametersException,
			CodingErrorException {
		this.relationalDataSource = relationalDataSource;
		// Load table schema objects
		Session hibernateSession = HibernateUtil.currentSession();
		try {
			this.authManager = new AuthManager(relationalDataSource);
		} finally {
			HibernateUtil.closeSession();
		}
		// Methods and objects dealing with data as opposed to the schema are
		// kept in DataManagement
		this.dataManagement = new DataManagement(relationalDataSource, webAppRoot, this.authManager);
		DashboardPopulator dashboardPopulator = new DashboardPopulator(this);
		// Start first dashboard population immediately
		this.initialDashboardPopulatorThread = new Thread(dashboardPopulator);
		this.initialDashboardPopulatorThread.start();
		// and schedule regular dashboard population once a day at a time of low
		// activity
		int hourNow = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		int initialDelay = 24 + AppProperties.lowActivityHour - hourNow;
		this.dashboardScheduler = Executors.newSingleThreadScheduledExecutor();
		this.scheduledDashboardPopulate = dashboardScheduler.scheduleAtFixedRate(dashboardPopulator,
				initialDelay, 24, TimeUnit.HOURS);
		// one-off boot actions
		// this.addCommentsFeedFields();
	}

	public void cancelScheduledEvents() {
		// TODO: check which of these are necessary, perhaps just the last will
		// do
		if (this.initialDashboardPopulatorThread != null) {
			this.initialDashboardPopulatorThread.interrupt();
		}
		if (this.scheduledDashboardPopulate != null) {
			this.scheduledDashboardPopulate.cancel(true);
		}
		if (this.dashboardScheduler != null) {
			this.dashboardScheduler.shutdown();
		}
	}

	/**
	 * Was only used once but is an example of how to add a new type of hidden
	 * field so worth keeping around
	 */
	private void addCommentsFeedFields() throws SQLException {
		Set<TableInfo> allTables = new HashSet<TableInfo>();
		Authenticator authenticator = (Authenticator) this.authManager.getAuthenticator();
		// TODO: once this action has completed, set getCompanies back to
		// protected
		for (CompanyInfo company : authenticator.getCompanies()) {
			allTables.addAll(company.getTables());
		}
		logger.info("Adding comments feed fields");
		for (TableInfo table : allTables) {
			String commentsFeedFieldName = HiddenFields.COMMENTS_FEED.getFieldName();
			try {
				BaseField commentsFeedField = table.getField(commentsFeedFieldName);
			} catch (ObjectNotFoundException onex) {
				logger.info("Comments feed field doesn't exist for table " + table + ", adding it");
				Connection conn = null;
				try {
					HibernateUtil.startHibernateTransaction();
					conn = this.relationalDataSource.getConnection();
					conn.setAutoCommit(false);
					HibernateUtil.activateObject(table);
					this.addCommentsFeedFieldToTable(conn, table);
					// Also remove the old obsolete wiki page field
					// For some reason we need to recreate some of the default
					// reports as due to some previous bug
					// they contain the wiki page field
					this.updateViewDbActionWithDropAndCreate(conn, table.getDefaultReport());
					BaseField wikiField = table.getField(HiddenFields.WIKI_PAGE.getFieldName());
					this.removeFieldWithoutChecks(null, conn, wikiField, table);
					conn.commit();
					HibernateUtil.currentSession().getTransaction().commit();
				} catch (SQLException sqlex) {
					logger.error("SQL error adding comments feed field: " + sqlex);
					rollbackConnections(conn);
				} catch (HibernateException hex) {
					logger.error("Hibernate error adding comments feed field: " + hex);
					rollbackConnections(conn);
				} catch (AgileBaseException pbex) {
					logger.error("AB error adding comments feed field: " + pbex);
					rollbackConnections(conn);
				} finally {
					if (conn != null) {
						conn.close();
						HibernateUtil.closeSession();
					}
				}
			}
		}
	}

	// Copied from ServletSchemaMethods
	private static void rollbackConnections(Connection conn) {
		try {
			if (conn != null) {
				logger.error("rolling back sql...");
				conn.rollback();
				logger.error("sql successfully rolled back");
			}
		} catch (SQLException sqlex) {
			logger.error("oh no! another sql exception was thrown");
			sqlex.printStackTrace();
			// don't rethrow, may just be because no SQL has been sent since
			// transaction start
			// TODO: check this
		}
		logger.error("rolling back hibernate...");
		HibernateUtil.currentSession().getTransaction().rollback();
		logger.error("hibernate successfully rolled back");
	}

	private void addCommentsFeedFieldToTable(Connection conn, TableInfo table)
			throws CantDoThatException, SQLException, ObjectNotFoundException, CodingErrorException {
		TextFieldOptions fieldOptions = new TextFieldOptions();
		fieldOptions.setNotNull(false);
		fieldOptions.setUnique(false);
		fieldOptions.setNotApplicable(false);
		fieldOptions.setUsesLookup(false);
		fieldOptions.setTieDownLookup(false);
		fieldOptions.setTextCase(TextCase.ANY);
		fieldOptions.setTextContentSize(TextContentSizes.FEW_PARAS.getNumChars());
		fieldOptions.setPrintoutSetting(FieldPrintoutSetting.NAME_AND_VALUE);
		TextField commentFeedField = new TextFieldDefn(this.relationalDataSource, table, null,
				HiddenFields.COMMENTS_FEED.getFieldName(),
				HiddenFields.COMMENTS_FEED.getFieldDescription(), TextField.HIDDEN, fieldOptions);
		HibernateUtil.currentSession().save(commentFeedField);
		table.addField(commentFeedField);
		this.addFieldToRelationalDb(conn, table, commentFeedField);
	}

	// !DateField.UNIQUE, !DateField.NOT_NULL, DateField.DEFAULT_TO_NOW,
	// Calendar.SECOND, null, null, FieldPrintoutSetting.NO_PRINTOUT
	private void addDateCreatedFieldToTable(Connection conn, TableInfo table)
			throws CantDoThatException, SQLException, ObjectNotFoundException, CodingErrorException {
		DateFieldOptions fieldOptions = new DateFieldOptions();
		fieldOptions.setDateResolution(Calendar.SECOND);
		fieldOptions.setDefaultToNow(true);
		fieldOptions.setNotNull(true);
		fieldOptions.setPrintoutSetting(FieldPrintoutSetting.NO_PRINTOUT);
		fieldOptions.setUnique(false);
		DateField dateCreatedField = new DateFieldDefn(table, null,
				HiddenFields.DATE_CREATED.getFieldName(), HiddenFields.DATE_CREATED.getFieldDescription(),
				fieldOptions);
		dateCreatedField.setHidden(DateFieldDefn.HIDDEN);
		HibernateUtil.currentSession().save(dateCreatedField);
		table.addField(dateCreatedField);
		this.addFieldToRelationalDb(conn, table, dateCreatedField);
	}

	private void addCreatedByFieldToTable(Connection conn, TableInfo table)
			throws CantDoThatException, SQLException, ObjectNotFoundException, CodingErrorException {
		TextFieldOptions fieldOptions = new TextFieldOptions();
		fieldOptions.setNotApplicable(false);
		fieldOptions.setNotNull(true);
		fieldOptions.setPrintoutSetting(FieldPrintoutSetting.NAME_AND_VALUE);
		fieldOptions.setTextCase(TextCase.ANY);
		fieldOptions.setTextContentSize(TextContentSizes.FEW_WORDS.getNumChars());
		fieldOptions.setTieDownLookup(false);
		fieldOptions.setUnique(false);
		fieldOptions.setUsesLookup(false);
		TextField createdByField = new TextFieldDefn(this.relationalDataSource, table, null,
				HiddenFields.CREATED_BY.getFieldName(), HiddenFields.CREATED_BY.getFieldDescription(),
				TextField.HIDDEN, fieldOptions);
		HibernateUtil.currentSession().save(createdByField);
		table.addField(createdByField);
		this.addFieldToRelationalDb(conn, table, createdByField);
		// Don't add the created by field to the default report
	}

	private void addLastModifiedFieldToTable(Connection conn, TableInfo table)
			throws CantDoThatException, SQLException, ObjectNotFoundException, CodingErrorException {
		DateFieldOptions fieldOptions = new DateFieldOptions();
		fieldOptions.setDateResolution(Calendar.SECOND);
		fieldOptions.setDefaultToNow(true);
		fieldOptions.setNotNull(false);
		fieldOptions.setPrintoutSetting(FieldPrintoutSetting.NO_PRINTOUT);
		fieldOptions.setUnique(false);
		DateField lastModifiedField = new DateFieldDefn(table, null,
				HiddenFields.LAST_MODIFIED.getFieldName(),
				HiddenFields.LAST_MODIFIED.getFieldDescription(), fieldOptions);
		lastModifiedField.setHidden(DateFieldDefn.HIDDEN);
		HibernateUtil.currentSession().save(lastModifiedField);
		table.addField(lastModifiedField);
		this.addFieldToRelationalDb(conn, table, lastModifiedField);
		// Don't add the last modified field to the default report
	}

	private void addModifiedByFieldToTable(Connection conn, TableInfo table)
			throws CantDoThatException, SQLException, ObjectNotFoundException, CodingErrorException {
		TextFieldOptions fieldOptions = new TextFieldOptions();
		fieldOptions.setNotApplicable(false);
		fieldOptions.setNotNull(true);
		fieldOptions.setPrintoutSetting(FieldPrintoutSetting.NAME_AND_VALUE);
		fieldOptions.setTextCase(TextCase.ANY);
		fieldOptions.setTextContentSize(TextContentSizes.FEW_WORDS.getNumChars());
		fieldOptions.setTieDownLookup(false);
		fieldOptions.setUnique(false);
		fieldOptions.setUsesLookup(false);
		TextField modifiedByField = new TextFieldDefn(this.relationalDataSource, table, null,
				HiddenFields.MODIFIED_BY.getFieldName(), HiddenFields.MODIFIED_BY.getFieldDescription(),
				TextField.HIDDEN, fieldOptions);
		HibernateUtil.currentSession().save(modifiedByField);
		table.addField(modifiedByField);
		this.addFieldToRelationalDb(conn, table, modifiedByField);
	}

	private void addRecordLockedFieldToTable(Connection conn, TableInfo table)
			throws CantDoThatException, SQLException, ObjectNotFoundException, CodingErrorException {
		CheckboxField recordLockedField = new CheckboxFieldDefn(table, null,
				HiddenFields.LOCKED.getFieldName(), HiddenFields.LOCKED.getFieldDescription(), false, true,
				FieldPrintoutSetting.NO_PRINTOUT);
		HibernateUtil.currentSession().save(recordLockedField);
		table.addField(recordLockedField);
		this.addFieldToRelationalDb(conn, table, recordLockedField);
	}

	private void addViewCountFieldToTable(Connection conn, TableInfo table)
			throws CantDoThatException, SQLException, ObjectNotFoundException, CodingErrorException {
		IntegerFieldOptions fieldOptions = new IntegerFieldOptions();
		fieldOptions.setDefaultValue(0);
		fieldOptions.setNotApplicable(false);
		fieldOptions.setNotNull(false);
		fieldOptions.setPrintoutSetting(FieldPrintoutSetting.NO_PRINTOUT);
		fieldOptions.setStoresCurrency(false);
		fieldOptions.setUnique(false);
		fieldOptions.setUsesLookup(false);
		IntegerField viewCountField = new IntegerFieldDefn(this.relationalDataSource, table, null,
				HiddenFields.VIEW_COUNT.getFieldName(), HiddenFields.VIEW_COUNT.getFieldDescription(),
				fieldOptions);
		viewCountField.setHidden(true);
		HibernateUtil.currentSession().save(viewCountField);
		table.addField(viewCountField);
		this.addFieldToRelationalDb(conn, table, viewCountField);
	}

	public TableInfo addTable(SessionDataInfo sessionData, HttpServletRequest request,
			Connection conn, String internalTableName, String internalDefaultReportName,
			String tableName, String internalPrimaryKeyName, String tableDesc) throws SQLException,
			DisallowedException, CantDoThatException, ObjectNotFoundException, CodingErrorException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.ADMINISTRATE))) {
			throw new DisallowedException(this.authManager.getLoggedInUser(request),
					PrivilegeType.ADMINISTRATE);
		}
		TableInfo newTable = new TableDefn(internalTableName, tableName, tableDesc);
		HibernateUtil.currentSession().save(newTable);
		try {
			String SQLCode = "CREATE TABLE " + newTable.getInternalTableName() + " ()";
			PreparedStatement statement = conn.prepareStatement(SQLCode);
			statement.execute();
			statement.close();
			// Create an initial report for the table
			SimpleReportInfo defaultReport = new SimpleReportDefn(newTable, internalDefaultReportName,
					"Default report", "A simple report of all items in the '" + tableName + "' data store",
					null);
			// The true passed means it is the default report
			newTable.addReport(defaultReport, true);
			// Add an auto-generated primary key to act as a row identifier
			SequenceField primaryKeyField = new SequenceFieldDefn(newTable, internalPrimaryKeyName, "ID:"
					+ tableName, PRIMARY_KEY_DESCRIPTION, FieldPrintoutSetting.NO_PRINTOUT);
			HibernateUtil.currentSession().save(primaryKeyField);
			newTable.addField(primaryKeyField);
			newTable.setPrimaryKey(primaryKeyField);
			this.addFieldToRelationalDb(conn, newTable, primaryKeyField);
			setPrimaryKeyDbAction(conn, newTable);
			// Update the default report to add the primary key
			ReportFieldInfo primaryKeyReportField = defaultReport.addTableField(primaryKeyField);
			// Sort the default report by primary key descending, i.e. show new
			// records at the top
			defaultReport.addSort(primaryKeyReportField, false);
			// Save default report definition to the database
			this.updateViewDbAction(conn, defaultReport, request);
			// Add hidden table fields
			this.addDateCreatedFieldToTable(conn, newTable);
			this.addCreatedByFieldToTable(conn, newTable);
			this.addLastModifiedFieldToTable(conn, newTable);
			this.addModifiedByFieldToTable(conn, newTable);
			this.addRecordLockedFieldToTable(conn, newTable);
			this.addViewCountFieldToTable(conn, newTable);
			this.addCommentsFeedFieldToTable(conn, newTable);
		} catch (SQLException sqlex) {
			// Reformat the error message to be more user friendly.
			// Use SQLState as an error identifier because it is standard across
			// databases
			String errorCode = sqlex.getSQLState();
			if (errorCode.equals("42P07")) {
				// A table with that name already exists
				throw new SQLException("The internal table name '" + newTable.getInternalTableName()
						+ "' already exists", errorCode);
			} else if (errorCode.equals("42601")) {
				throw new SQLException("Table couldn't be created", sqlex);
			} else {
				throw new SQLException(sqlex + ": error code " + errorCode, sqlex);
			}
		}
		// Cache the table in the company object
		this.authManager.getCompanyForLoggedInUser(request).addTable(newTable);
		UsageLogger usageLogger = new UsageLogger(this.relationalDataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logTableSchemaChange(user, newTable, AppAction.ADD_TABLE, "");
		UsageLogger.startLoggingThread(usageLogger);
		return newTable;
		// this.dataManagement.logLastSchemaChangeTime(request);
	}

	public void setDefaultTablePrivileges(HttpServletRequest request, TableInfo newTable)
			throws DisallowedException, CantDoThatException {
		// Set table privileges
		HibernateUtil.activateObject(this.authManager.getAuthenticator());
		// ...give the user who created the table all privileges on it
		try {
			AppUserInfo loggedInUser = this.authManager.getUserByUserName(request,
					request.getRemoteUser());
			this.authManager
					.addUserPrivilege(request, loggedInUser, PrivilegeType.MANAGE_TABLE, newTable);
			this.authManager.addUserPrivilege(request, loggedInUser, PrivilegeType.EDIT_TABLE_DATA,
					newTable);
			this.authManager.addUserPrivilege(request, loggedInUser, PrivilegeType.VIEW_TABLE_DATA,
					newTable);
		} catch (ObjectNotFoundException onfex) {
			throw new CantDoThatException("The logged in user '" + request.getRemoteUser()
					+ "' can't be found");
		}
	}

	private void setPrimaryKeyDbAction(Connection conn, TableInfo table) throws SQLException {
		BaseField primaryKeyField = table.getPrimaryKey();
		if (primaryKeyField != null) {
			// TODO: truncate internal table name so that the addition of
			// '_pkey' doesn't make a string longer
			// than 31 chars
			// This could be a boundary case to unit test
			String SQLCode = "ALTER TABLE " + table.getInternalTableName() + " ADD CONSTRAINT "
					+ table.getInternalTableName() + "_pkey PRIMARY KEY("
					+ primaryKeyField.getInternalFieldName() + ")";
			PreparedStatement statement = conn.prepareStatement(SQLCode);
			statement.execute();
			statement.close();
		}
	}

	public void getDependentTables(TableInfo baseTable, Set<TableInfo> dependentTables,
			boolean direction, HttpServletRequest request) throws ObjectNotFoundException {
		CompanyInfo company = this.authManager.getCompanyForLoggedInUser(request);
		Set<TableInfo> tables = company.getTables();
		for (TableInfo table : tables) {
			if (table.equals(baseTable)) {
				continue;
			}
			// Find dependent parents
			TableInfo t1 = baseTable;
			TableInfo t2 = table;
			if (direction) {
				// No, find dependent children
				t1 = table;
				t2 = baseTable;
			}
			if (t1.isDependentOn(t2)) {
				if (!dependentTables.contains(table)) {
					dependentTables.add(table);
					this.getDependentTables(table, dependentTables, direction, request);
				}
			}
		}
	}

	public SortedSet<TableInfo> getDirectlyDependentTables(TableInfo baseTable,
			HttpServletRequest request) throws ObjectNotFoundException {
		CompanyInfo company = this.authManager.getCompanyForLoggedInUser(request);
		Set<TableInfo> tables = company.getTables();
		SortedSet<TableInfo> dependentTables = new TreeSet<TableInfo>();
		for (TableInfo table : tables) {
			if (table.equals(baseTable)) {
				continue;
			}
			if (table.isDependentOn(baseTable)) {
				if (!dependentTables.contains(table)) {
					dependentTables.add(table);
				}
			}
		}
		return dependentTables;
	}

	public void updateTable(Connection conn, HttpServletRequest request, TableInfo table,
			String newTableName, String newTableDesc, Boolean lockable, Boolean tableFormPublic,
			String tableEmail, String tableEmailResponse, FormStyle formStyle, boolean allowAutoDelete,
			boolean allowNotifications) throws DisallowedException, CantDoThatException,
			ObjectNotFoundException, SQLException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, table))) {
			throw new DisallowedException(this.authManager.getLoggedInUser(request),
					PrivilegeType.MANAGE_TABLE, table);
		}
		HibernateUtil.activateObject(table);
		if (newTableName != null) {
			if (table.getTableName().equals(newTableName)) {
				// if no change to table name ignore request
				return;
			}
			CompanyInfo company = this.authManager.getCompanyForLoggedInUser(request);
			for (TableInfo existingTable : company.getTables()) {
				if (existingTable.getTableName().equals(newTableName)) {
					throw new CantDoThatException("A table called '" + newTableName + "' already exists");
				}
			}
			table.setTableName(newTableName);
			// Also re-name primary key to match
			table.getPrimaryKey().setFieldName("ID:" + newTableName);
			// Set a comment on the table for easy viewing using third party
			// tools
			String SQLCode = "COMMENT ON TABLE " + table.getInternalTableName() + " IS '"
					+ Helpers.rinseString(table.getTableName()) + "'";
			PreparedStatement statement = conn.prepareStatement(SQLCode);
			statement.execute();
			statement.close();
		}
		if (newTableDesc != null) {
			table.setTableDescription(newTableDesc);
		}
		if (lockable != null) {
			table.setRecordsLockable(lockable);
			if (lockable) {
				// Lock all existing records
				String SQLCode = "UPDATE " + table.getInternalTableName() + " SET "
						+ table.getField(HiddenFields.LOCKED.getFieldName()).getInternalFieldName() + " = true";
				PreparedStatement statement = conn.prepareStatement(SQLCode);
				statement.executeUpdate();
				statement.close();
			}
		}
		if (tableFormPublic != null) {
			table.setTableFormPublic(tableFormPublic);
		}
		if (tableEmail != null) {
			table.setEmail(tableEmail);
		}
		if (tableEmailResponse != null) {
			table.setEmailResponse(tableEmailResponse);
		}
		if (formStyle != null) {
			table.setFormStyle(formStyle);
		}
		table.setAllowAutoDelete(allowAutoDelete);
		table.setAllowNotifications(allowNotifications);
	}

	public void updateMap(HttpServletRequest request, BaseReportInfo report,
			ReportFieldInfo postcodeField, ReportFieldInfo colourField, ReportFieldInfo categoryField)
			throws DisallowedException, ObjectNotFoundException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, report.getParentTable()))) {
			throw new DisallowedException(this.authManager.getLoggedInUser(request),
					PrivilegeType.MANAGE_TABLE, report.getParentTable());
		}
		HibernateUtil.activateObject(report);
		ReportMapInfo map = report.getMap();
		if (map == null) {
			map = new ReportMap();
			HibernateUtil.currentSession().save(map);
			report.setMap(map);
		}
		if ((postcodeField == null) && (colourField == null) && (categoryField == null)) {
			map.setPostcodeField(null);
		} else {
			if (postcodeField != null) {
				map.setPostcodeField(postcodeField);
			}
			if (colourField != null) {
				map.setColourField(colourField);
			}
			if (categoryField != null) {
				map.setCategoryField(categoryField);
			}
		}
	}

	public void removeTable(SessionDataInfo sessionData, HttpServletRequest request,
			TableInfo tableToRemove, Connection conn) throws SQLException, DisallowedException,
			CantDoThatException, TableDependencyException, CodingErrorException, ObjectNotFoundException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.ADMINISTRATE))) {
			throw new DisallowedException(this.authManager.getLoggedInUser(request),
					PrivilegeType.ADMINISTRATE);
		}
		// Check the table doesn't have any user-added fields
		for (BaseField field : tableToRemove.getFields()) {
			if (!(field.equals(tableToRemove.getPrimaryKey()) || field.getHidden())) {
				throw new CantDoThatException("Please remove all fields before removing the table");
			}
		}
		// Check that it doesn't have any reports
		if (tableToRemove.getReports().size() > 1) {
			throw new CantDoThatException("Please remove reports " + tableToRemove.getReports()
					+ " before removing the table");
		}
		// Get a set of dependent tables. If empty proceed with the deletion of
		// the table, otherwise, raise an exception
		LinkedHashSet<TableInfo> dependentTables = new LinkedHashSet<TableInfo>();
		this.getDependentTables(tableToRemove, dependentTables, true, request);
		if (dependentTables.size() > 0) {
			LinkedHashSet<BaseReportInfo> dependentReports = new LinkedHashSet<BaseReportInfo>();
			for (TableInfo dependentTable : dependentTables) {
				dependentReports.addAll(dependentTable.getReports());
			}
			throw new TableDependencyException(
					"Unable to remove table - other tables are linked to it, that need to be removed first",
					dependentTables, dependentReports);
		}
		// No dependencies exist so remove the table & its default report:
		BaseReportInfo defaultReport = tableToRemove.getDefaultReport();
		this.removeReportWithoutChecks(sessionData, request, defaultReport, conn);
		// Remove any privileges on the table
		this.getAuthManager().removePrivilegesOnTable(request, tableToRemove);
		this.tableCache.remove(tableToRemove.getInternalTableName());
		// Delete from persistent store
		HibernateUtil.currentSession().delete(tableToRemove);
		try {
			// Delete the table from the relational database.
			// The CASCADE is to drop the related sequence.
			// TODO: replace this with a specific sequence drop
			PreparedStatement statement = conn.prepareStatement("DROP TABLE "
					+ tableToRemove.getInternalTableName() + " CASCADE");
			statement.execute();
			statement.close();
		} catch (SQLException sqlex) {
			String errorCode = sqlex.getSQLState();
			if (errorCode.equals("42P01")) {
				logger.warn("Can't delete table " + tableToRemove
						+ " from relational database, it's not there");
				// TODO: review why we're swallowing this error
			} else {
				throw new SQLException(sqlex + ": error code " + errorCode, sqlex);
			}
		}
		this.authManager.getCompanyForLoggedInUser(request).removeTable(tableToRemove);
		UsageLogger usageLogger = new UsageLogger(this.relationalDataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logTableSchemaChange(user, tableToRemove, AppAction.REMOVE_TABLE, "");
		UsageLogger.startLoggingThread(usageLogger);
	}

	public BaseReportInfo addReport(SessionDataInfo sessionData, HttpServletRequest request,
			Connection conn, TableInfo table, String internalReportName, String reportName,
			String reportDesc, boolean populateReport) throws SQLException, DisallowedException,
			CantDoThatException, CodingErrorException, ObjectNotFoundException, ObjectNotFoundException,
			ObjectNotFoundException, MissingParametersException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, table))) {
			throw new DisallowedException(this.authManager.getLoggedInUser(request),
					PrivilegeType.MANAGE_TABLE, table);
		}
		SimpleReportInfo report = null;
		// Put the new report in the current session module
		ModuleInfo module = sessionData.getModule();
		report = new SimpleReportDefn(table, internalReportName, reportName, reportDesc, module);
		if (populateReport) {
			// Populate the report initially with all fields in the table it's
			// based on
			Set<TableInfo> relatedTables = new TreeSet<TableInfo>();
			for (BaseField field : table.getFields()) {
				if (field.getFieldCategory().savesData()) {
					if (field instanceof RelationField) {
						RelationField relationField = ((RelationField) field);
						// Workaround for bug: creating more than one relation
						// at a time fails with a Hibernate Exception
						// Also relation fields which have a display field which
						// is also a relation field are complex
						if ((relatedTables.size() == 0)
								&& !(relationField.getDisplayField() instanceof RelationField)) {
							// add a join to allow related field to be added to
							// the report
							TableInfo relatedTable = relationField.getRelatedTable();
							if (!relatedTables.contains(relatedTable)) {
								relatedTables.add(relatedTable);
								JoinClauseInfo join = ServletSchemaMethods.generateJoinObject(request, table
										.getInternalTableName(), "", field.getInternalFieldName(), JoinType.LEFT_OUTER,
										relatedTable.getInternalTableName(), "", relatedTable.getPrimaryKey()
												.getInternalFieldName(), this);
								report.addJoin(join);
							}
							report.addTableField(relationField.getDisplayField());
						}
					} else if (!(field.equals(table.getPrimaryKey()) || field.getHidden())) {
						report.addTableField(field);
					}
				}
			}
			if (report.getReportFields().size() > 1) {
				// the second field in the list should have a sort - first is
				// the primary key
				List<ReportFieldInfo> reportFieldList = new ArrayList<ReportFieldInfo>(
						report.getReportFields());
				boolean ascending = true;
				ReportFieldInfo sortField = reportFieldList.get(1);
				if (sortField.getBaseField().getDbType().equals(DatabaseFieldType.TIMESTAMP)) {
					ascending = false;
				}
				report.addSort(sortField, ascending);
			}
		}
		HibernateUtil.currentSession().save(report);
		this.updateViewDbAction(conn, report, request);
		HibernateUtil.activateObject(table);
		table.addReport(report, false);
		// this.dataManagement.logLastSchemaChangeTime(request);
		return report;
	}

	/**
	 * Find out if the view already exists
	 */
	private boolean viewExists(Connection conn, BaseReportInfo report) throws SQLException {
		PreparedStatement viewExistsStatement = conn
				.prepareStatement("select count(*) from information_schema.views where table_name=?");
		viewExistsStatement.setString(1, report.getInternalReportName());
		ResultSet viewExistsResults = viewExistsStatement.executeQuery();
		boolean viewExists = false;
		while (viewExistsResults.next()) {
			if (viewExistsResults.getInt(1) == 1) {
				viewExists = true;
			}
		}
		viewExistsResults.close();
		viewExistsStatement.close();
		return viewExists;
	}

	/**
	 * Updates the definition of a view within the DB. This method will not work
	 * if the number of columns within the view are being changed.
	 */
	private boolean updateViewDbActionWithCreateOrReplace(Connection conn, BaseReportInfo report,
			boolean viewExists) throws SQLException, CantDoThatException, CodingErrorException,
			ObjectNotFoundException {
		String SQLCode = "CREATE OR REPLACE VIEW " + report.getInternalReportName() + " AS ("
				+ report.getSQLForDetail() + ")";
		boolean createOrReplaceWorked = true;
		Savepoint savepoint = null;
		PreparedStatement statement = null;
		try {
			savepoint = conn.setSavepoint("createOrReplaceSavepoint");
			statement = conn.prepareStatement(SQLCode);
			statement.execute();
			statement.close();
		} catch (SQLException sqlex) {
			if (viewExists) {
				createOrReplaceWorked = false;
				conn.rollback(savepoint);
			} else {
				// if view didn't exist already, the error must be more serious
				// than just the CREATE OR REPLACE not working
				// logger.error("Requested change to report " +
				// report.getReportName()
				// + " would break view. Error = " + sqlex);
				// logger.error("SQL = " + report.getSQLForDetail());
				throw new SQLException("The requested change would cause an error in the report: "
						+ sqlex.getMessage() + ". SQL = " + statement, sqlex.getSQLState(), sqlex);
			}
		}
		return createOrReplaceWorked;
	}

	/**
	 * Updates the definition of a view within the DB. This method should only be
	 * used if updateViewDbActionWithCreateOrReplace fails. Drops the view and
	 * recreates it.
	 */
	private boolean updateViewDbActionWithDropAndCreate(Connection conn, BaseReportInfo report)
			throws SQLException, CantDoThatException, CodingErrorException, ObjectNotFoundException {
		String CreateViewSQL = "CREATE VIEW " + report.getInternalReportName() + " AS ("
				+ report.getSQLForDetail() + ")";
		boolean dropAndCreateWorked = true;
		Savepoint savepoint = null;
		try {
			savepoint = conn.setSavepoint("dropAndCreateSavepoint");
			PreparedStatement dropViewStatement = conn.prepareStatement("DROP VIEW "
					+ report.getInternalReportName());
			dropViewStatement.execute();
			dropViewStatement.close();
			PreparedStatement statement = conn.prepareStatement(CreateViewSQL);
			statement.execute();
			statement.close();
		} catch (SQLException sqlex) {
			conn.rollback(savepoint);
			dropAndCreateWorked = false;
		}
		return dropAndCreateWorked;
	}

	/**
	 * Returns a Map whose keySet contains the names of the set of views dependent
	 * upon the report identified by internalReportName. The keySet also contains
	 * internalReportName. Each key maps to a list of view names identifying views
	 * directly dependent upon the view identified by the key.
	 */
	private void fillViewDependencyMap(Connection conn, String internalReportName,
			Map<String, List<String>> reportDependencyMap, boolean recurse) throws SQLException {
		String SQLCode = this.viewDependencySQL();
		PreparedStatement statement = conn.prepareStatement(SQLCode);
		statement.setString(1, internalReportName);
		ResultSet results = statement.executeQuery();
		List<String> dependentReportInternalNames = new ArrayList<String>();
		// add the empty list for now so the key is present to prevent
		// infinite recursion
		reportDependencyMap.put(internalReportName, dependentReportInternalNames);
		while (results.next()) {
			String dependentReportInternalName = results.getString(1);
			dependentReportInternalNames.add(dependentReportInternalName);
			if (!reportDependencyMap.keySet().contains(dependentReportInternalName) && recurse) {
				fillViewDependencyMap(conn, dependentReportInternalName, reportDependencyMap, recurse);
			}
		}
		results.close();
		statement.close();
	}

	private String viewDependencySQL() {
		// 1) Standard compliant and simple but slow
		// String SQLCode =
		// "SELECT table_name FROM information_schema.views WHERE view_definition ILIKE ?";
		// 2) Old Postgres way, equally slow
		// String SQLCode =
		// "SELECT viewname FROM pg_views WHERE schemaname='public' AND definition ILIKE '%"
		// + internalReportName + "%'";
		// 3) Low level Postgres way, complicated but fast
		// See
		// http://forums.postgresql.com.au/viewtopic.php?f=30&t=104965&start=0
		String SQLCode = "SELECT distinct dependent.relname";
		SQLCode += " FROM pg_depend";
		SQLCode += " JOIN pg_rewrite ON pg_depend.objid = pg_rewrite.oid";
		SQLCode += " JOIN pg_class as dependent ON pg_rewrite.ev_class = dependent.oid";
		SQLCode += " JOIN pg_class as dependee ON pg_depend.refobjid = dependee.oid";
		SQLCode += " WHERE dependee.relname = ?";
		SQLCode += " AND dependent.oid != dependee.oid";
		return SQLCode;
	}

	/**
	 * Updates the definition of a view within the DB. This method should only be
	 * used if updateViewDbActionWithDropAndCreate and
	 * updateViewDbActionWithCreateOrReplace fails. Drops any dependent views so
	 * that 'report' can be updated. Once report has been updated, all dependent
	 * views are recreated.
	 */
	private void updateViewDbActionWithDropAndCreateDependencies(Connection conn,
			BaseReportInfo report, HttpServletRequest request) throws SQLException,
			ObjectNotFoundException, CodingErrorException, CantDoThatException {
		Savepoint savepoint = null;
		PreparedStatement statement = null;
		try {
			savepoint = conn.setSavepoint("dropAndCreateDependenciesSavepoint");
			Map<String, List<String>> reportDependencyMap = new HashMap<String, List<String>>();
			this.fillViewDependencyMap(conn, report.getInternalReportName(), reportDependencyMap, true);
			// Remove reports...
			List<String> deletedReports = new ArrayList<String>();
			while (deletedReports.size() < reportDependencyMap.size()) {
				for (String reportInternalName : reportDependencyMap.keySet()) {
					if (!deletedReports.contains(reportInternalName)) {
						boolean cannotDelete = false;
						for (String dependentReportInternalName : reportDependencyMap.get(reportInternalName)) {
							if (!deletedReports.contains(dependentReportInternalName)) {
								cannotDelete = true;
							}
						}
						if (!cannotDelete) {
							PreparedStatement dropViewStatement = conn.prepareStatement("DROP VIEW "
									+ reportInternalName);
							dropViewStatement.execute();
							dropViewStatement.close();
							deletedReports.add(reportInternalName);
						}
					}
				}
			}
			// Recreate reports...
			Collections.reverse(deletedReports);
			for (String reportInternalName : deletedReports) {
				TableInfo table = this.findTableContainingReportWithoutChecks(reportInternalName, request);
				HibernateUtil.activateObject(table);
				BaseReportInfo reportToRecreate = table.getReport(reportInternalName);
				String CreateViewSQL = "CREATE VIEW " + reportInternalName + " AS ("
						+ reportToRecreate.getSQLForDetail() + ")";
				statement = conn.prepareStatement(CreateViewSQL);
				statement.execute();
				statement.close();
			}
		} catch (SQLException sqlex) {
			conn.rollback(savepoint);
			throw new SQLException("The requested change would cause an error in the report: "
					+ sqlex.getMessage() + ". SQL = " + statement, sqlex.getSQLState(), sqlex);
		}
	}

	/**
	 * Attempts to read the first 10 records from a given report, to check there
	 * isn't an error in the SQL
	 */
	private void throwExceptionIfDbViewIsBroken(Connection conn, BaseReportInfo report)
			throws SQLException, ObjectNotFoundException, CodingErrorException, CantDoThatException {
		PreparedStatement statement = null;
		try {
			ReportData.enableOptimisations(conn, report, true);
			String SQLCode = "SELECT * FROM " + report.getInternalReportName() + " LIMIT 10";
			statement = conn.prepareStatement(SQLCode);
			ResultSet testResults = statement.executeQuery();
			ResultSetMetaData metaData = testResults.getMetaData();
			int numColumns = metaData.getColumnCount();
			while (testResults.next()) {
				for (int i = 1; i <= numColumns; i++) {
					String testKey = testResults.getString(i);
				}
			}
			testResults.close();
			statement.close();
			ReportData.enableOptimisations(conn, report, false);
		} catch (SQLException sqlex) {
			// log the cause but return a more user friendly message
			// logger.error("Requested change to report " +
			// report.getReportName()
			// + " would break view. Error = " + sqlex);
			// logger.error("SQL = " + report.getSQLForDetail());
			throw new SQLException("The requested change would cause an error in the report: "
					+ sqlex.getMessage() + ". SQL = " + statement, sqlex.getSQLState(), sqlex);
		}
	}

	/**
	 * Create the database VIEW for the report
	 */
	private void updateViewDbAction(Connection conn, BaseReportInfo report, HttpServletRequest request)
			throws SQLException, CantDoThatException, CodingErrorException, ObjectNotFoundException {
		boolean viewExists = viewExists(conn, report);
		boolean createOrReplaceWorked = updateViewDbActionWithCreateOrReplace(conn, report, viewExists);
		if (viewExists && !createOrReplaceWorked) {
			boolean dropAndCreateWorked = updateViewDbActionWithDropAndCreate(conn, report);
			if (!dropAndCreateWorked) {
				updateViewDbActionWithDropAndCreateDependencies(conn, report, request);
			}
		}
		this.throwExceptionIfDbViewIsBroken(conn, report);
	}

	public void removeReport(SessionDataInfo sessionData, HttpServletRequest request,
			Connection conn, BaseReportInfo reportToRemove) throws SQLException, DisallowedException,
			CantDoThatException, CodingErrorException, ObjectNotFoundException {
		TableInfo parentTable = reportToRemove.getParentTable();
		// Can't remove the last report
		if (parentTable.getReports().size() == 1) {
			throw new CantDoThatException("Can't remove the last report");
		}
		// or the default report
		if (reportToRemove.equals(parentTable.getDefaultReport())) {
			throw new CantDoThatException("Can't remove the default report");
		}
		Set<BaseReportInfo> dependentReports = this.getDependentReports(
				(SimpleReportInfo) reportToRemove, request);
		if (dependentReports.size() > 0) {
			throw new CantDoThatException("Reports " + dependentReports + " depend on this one");
		}
		Set<TableInfo> tablesReferencingReport = this.getTablesIncludingReferences(reportToRemove,
				request);
		if (tablesReferencingReport.size() > 0) {
			throw new CantDoThatException("Tables " + tablesReferencingReport
					+ " reference data in this report");
		}
		CompanyInfo company = this.authManager.getCompanyForLoggedInUser(request);
		for (TableInfo table : company.getTables()) {
			for (FormTabInfo formTab : table.getFormTabs()) {
				BaseReportInfo selectorReport = formTab.getSelectorReport();
				if (selectorReport != null) {
					if (selectorReport.equals(reportToRemove)) {
						throw new CantDoThatException("The table " + table + " has a tab that uses this report");
					}
				}
			}
		}
		parentTable.removeReport(reportToRemove);
		this.removeReportWithoutChecks(sessionData, request, reportToRemove, conn);
		UsageLogger usageLogger = new UsageLogger(this.relationalDataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logReportSchemaChange(user, reportToRemove, AppAction.REMOVE_REPORT, "");
		UsageLogger.startLoggingThread(usageLogger);
	}

	/**
	 * By calling removeReport above, you can't remove the last or default report.
	 * However, sometimes we may need to internally remove the last report, for
	 * example if we're removing a table all reports need to be removed. This
	 * method is there for private use in these situations
	 */
	private void removeReportWithoutChecks(SessionDataInfo sessionData, HttpServletRequest request,
			BaseReportInfo reportToRemove, Connection conn) throws DisallowedException, SQLException,
			CodingErrorException, CantDoThatException, ObjectNotFoundException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, reportToRemove.getParentTable()))) {
			throw new DisallowedException(this.authManager.getLoggedInUser(request),
					PrivilegeType.MANAGE_TABLE, reportToRemove.getParentTable());
		}
		// Remove the report from any 'hidden report' lists belonging to users
		// and from being the default report of any user
		CompanyInfo company = this.getAuthManager().getCompanyForLoggedInUser(request);
		for (AppUserInfo user : company.getUsers()) {
			HibernateUtil.activateObject(user);
			user.unhideReport(reportToRemove);
			user.removeOperationalDashboardReport(reportToRemove);
			if (reportToRemove.equals(user.getDefaultReport())) {
				logger.warn("Default report " + reportToRemove.getModule() + " - " + reportToRemove
						+ " removed for user " + user);
				user.setDefaultReport(null);
			}
		}
		TableInfo parentTable = reportToRemove.getParentTable();
		HibernateUtil.activateObject(parentTable);
		String internalReportName = reportToRemove.getInternalReportName();
		try {
			// Drop database view
			// Note: IF EXISTS added temporarily(?) to help when dropping
			// problem tables
			PreparedStatement statement = conn.prepareStatement("DROP VIEW IF EXISTS "
					+ internalReportName);
			statement.execute();
			statement.close();
			HibernateUtil.currentSession().delete(reportToRemove);
		} catch (SQLException sqlex) {
			String errorCode = sqlex.getSQLState();
			if (errorCode.equals("42P01")) {
				throw new SQLException("Can't delete report " + reportToRemove
						+ " because it doesn't exist in the data store", sqlex);
			} else {
				throw new SQLException(sqlex + ": error code " + errorCode, sqlex);
			}
		}
	}

	public void uploadCustomReportTemplate(HttpServletRequest request, BaseReportInfo report,
			String templateName, List<FileItem> multipartItems) throws DisallowedException,
			ObjectNotFoundException, CantDoThatException, FileUploadException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.VIEW_TABLE_DATA, report.getParentTable()))) {
			throw new DisallowedException(this.authManager.getLoggedInUser(request),
					PrivilegeType.VIEW_TABLE_DATA, report.getParentTable());
		}
		if (!FileUpload.isMultipartContent(new ServletRequestContext(request))) {
			throw new CantDoThatException(
					"To upload a template, the form must be posted as multi-part form data");
		}
		CompanyInfo company = this.getAuthManager().getCompanyForLoggedInUser(request);
		// strip extension
		String rinsedFileName = templateName.toLowerCase().replaceAll("\\..*$", "");
		rinsedFileName = Helpers.rinseString(rinsedFileName).replace(" ", "_");
		String uploadFolderName = this.getDataManagement().getWebAppRoot()
				+ "WEB-INF/templates/uploads/" + company.getInternalCompanyName() + "/"
				+ report.getInternalReportName();
		File uploadFolder = new File(uploadFolderName);
		if (!uploadFolder.exists()) {
			if (!uploadFolder.mkdirs()) {
				throw new CantDoThatException("Error creating upload folder " + uploadFolderName);
			}
		}
		for (FileItem item : multipartItems) {
			// if item is a file
			if (!item.isFormField()) {
				long fileSize = item.getSize();
				if (fileSize == 0) {
					throw new CantDoThatException("An empty file was submitted, no upload done");
				}
				String filePath = uploadFolderName + "/" + rinsedFileName + ".vm";
				File selectedFile = new File(filePath);
				try {
					item.write(selectedFile);
				} catch (Exception ex) {
					// Catching a general exception?! This is because the third
					// party
					// library throws a raw exception. Not very good
					throw new FileUploadException("Error writing file: " + ex.getMessage());
				}
			}
		}
	}

	public void removeCustomReportTemplate(HttpServletRequest request, BaseReportInfo report,
			String templateName) throws DisallowedException, ObjectNotFoundException, CantDoThatException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.VIEW_TABLE_DATA, report.getParentTable()))) {
			throw new DisallowedException(this.authManager.getLoggedInUser(request),
					PrivilegeType.VIEW_TABLE_DATA, report.getParentTable());
		}
		CompanyInfo company = this.getAuthManager().getCompanyForLoggedInUser(request);
		// strip extension
		String rinsedFileName = templateName.toLowerCase().replaceAll("\\..*$", "");
		rinsedFileName = Helpers.rinseString(rinsedFileName).replace(" ", "_");
		String uploadFolderName = this.getDataManagement().getWebAppRoot()
				+ "WEB-INF/templates/uploads/" + company.getInternalCompanyName() + "/"
				+ report.getInternalReportName();
		File uploadFolder = new File(uploadFolderName);
		if (!uploadFolder.exists()) {
			throw new ObjectNotFoundException("The template folder " + uploadFolderName
					+ " does not exist");
		}
		String filePath = uploadFolderName + "/" + rinsedFileName + ".vm";
		File selectedFile = new File(filePath);
		if (!selectedFile.delete()) {
			throw new CantDoThatException("Delete of " + filePath + " failed");
		}
	}

	public void updateReport(Connection conn, HttpServletRequest request, BaseReportInfo report,
			String newReportName, String newReportDesc, ModuleInfo newModule, ReportStyle newReportStyle,
			boolean allowExport, Integer memoryAllocation) throws DisallowedException,
			CantDoThatException, SQLException, ObjectNotFoundException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, report.getParentTable()))) {
			throw new DisallowedException(this.authManager.getLoggedInUser(request),
					PrivilegeType.MANAGE_TABLE, report.getParentTable());
		}
		HibernateUtil.activateObject(report);
		if (newReportName != null) {
			if (!report.getReportName().equals(newReportName)) {
				for (BaseReportInfo testReport : report.getParentTable().getReports()) {
					if (testReport.getReportName().equals(newReportName) && (!testReport.equals(report))) {
						throw new CantDoThatException("A report called " + newReportName
								+ " already exists in the " + report.getParentTable().getTableName() + " table");
					}
				}
				report.setReportName(newReportName);
				String SQLCode = "COMMENT ON VIEW " + report.getInternalReportName() + " IS '"
						+ Helpers.rinseString(newReportName) + "'";
				PreparedStatement statement = conn.prepareStatement(SQLCode);
				statement.execute();
				statement.close();
			}
		}
		if (newReportDesc != null) {
			report.setReportDescription(newReportDesc);
		}
		if (newModule != null) {
			report.setModule(newModule);
		}
		if (newReportStyle != null) {
			report.setReportStyle(newReportStyle);
		}
		if (memoryAllocation != null) {
			if (memoryAllocation < 1) {
				report.setMemoryAllocation(null);
			} else if (memoryAllocation > 500) {
				throw new CantDoThatException("Memory allocation max. is 500MB");
			} else {
				report.setMemoryAllocation(memoryAllocation);
			}
		}
		report.setAllowExport(allowExport);
	}

	private BaseField generateFieldObject(HttpServletRequest request, TableInfo table,
			String fieldType, String internalFieldName, String fieldName, String fieldDesc)
			throws CodingErrorException, CantDoThatException, ObjectNotFoundException,
			DisallowedException {
		BaseField field = null;
		BasicFieldOptions basicOptions = new BasicFieldOptions();
		basicOptions.setUnique(HttpRequestUtil.getBooleanValue(request,
				PossibleBooleanOptions.UNIQUE.getFormInputName()));
		basicOptions.setNotNull(HttpRequestUtil.getBooleanValue(request,
				PossibleBooleanOptions.MANDATORY.getFormInputName()));
		// The 'not applicable' property is not used currently
		// TODO: really? check
		// boolean notApplicable = false;
		// String notApplicableDescription = null;
		// String notApplicableValue = null;
		basicOptions.setPrintoutSetting(FieldPrintoutSetting.NAME_VALUE_LINEBREAK);
		String printoutSettingString = HttpRequestUtil.getStringValue(request,
				PossibleListOptions.PRINTFORMAT.getFormInputName());
		if (printoutSettingString != null) {
			if (!printoutSettingString.equals("")) {
				basicOptions.setPrintoutSetting(FieldPrintoutSetting.valueOf(printoutSettingString
						.toUpperCase()));
			}
		}
		FieldCategory fieldCategoryRequested = FieldCategory.valueOf(fieldType.toUpperCase(Locale.UK));
		fieldName = fieldName.trim();
		switch (fieldCategoryRequested) {
		case DATE:
			field = this.generateDateField(request, table, internalFieldName, fieldName, fieldDesc,
					basicOptions);
			break;
		case TEXT:
			field = generateTextField(request, table, internalFieldName, fieldName, fieldDesc,
					basicOptions);
			break;
		case NUMBER:
			field = generateNumberField(request, table, internalFieldName, fieldName, fieldDesc,
					basicOptions);
			break;
		case SEQUENCE:
			field = new SequenceFieldDefn(table, internalFieldName, fieldName, fieldDesc,
					basicOptions.getPrintoutSetting());
			break;
		case CHECKBOX:
			Boolean checkboxDefaultValue = HttpRequestUtil.getBooleanValue(request,
					PossibleListOptions.CHECKBOXDEFAULT.getFormInputName());
			field = new CheckboxFieldDefn(table, internalFieldName, fieldName, fieldDesc,
					checkboxDefaultValue, false, basicOptions.getPrintoutSetting());
			break;
		case FILE:
			field = new FileFieldDefn(table, internalFieldName, fieldName, fieldDesc,
					basicOptions.getPrintoutSetting());
			break;
		case SEPARATOR:
			field = new SeparatorFieldDefn(table, internalFieldName, fieldName, fieldDesc);
			break;
		case COMMENT_FEED:
			field = new CommentFeedFieldDefn(table, internalFieldName, fieldName, fieldDesc);
			break;
		case REFERENCED_REPORT_DATA:
			String internalTableName = HttpRequestUtil.getStringValue(request,
					PossibleListOptions.LISTTABLE.getFormInputName());
			String internalReportName = HttpRequestUtil.getStringValue(request,
					PossibleListOptions.LISTREPORT.getFormInputName());
			TableInfo referencedReportTable = this.getTable(request, internalTableName);
			BaseReportInfo referencedReport = referencedReportTable.getReport(internalReportName);
			field = new ReferencedReportDataFieldDefn(table, internalFieldName, fieldName, fieldDesc,
					referencedReport, basicOptions.getPrintoutSetting());
			break;
		default:
			throw new CantDoThatException("Adding unrecognised field type '" + fieldType + "'");
		}
		return field;
	}

	private BaseField generateNumberField(HttpServletRequest request, TableInfo table,
			String internalFieldName, String fieldName, String fieldDesc, BasicFieldOptions basicOptions)
			throws CantDoThatException {
		BaseField field;
		int precision = HttpRequestUtil.getIntegerValue(request,
				PossibleListOptions.NUMBERPRECISION.getFormInputName(), 0);
		boolean usesLookup = HttpRequestUtil.getBooleanValue(request,
				PossibleBooleanOptions.USELOOKUP.getFormInputName());
		boolean storesCurrency = HttpRequestUtil.getBooleanValue(request,
				PossibleBooleanOptions.STORECURRENCY.getFormInputName());
		if (precision > 0) {
			Double defaultNumber = HttpRequestUtil.getDoubleValueStrict(request,
					PossibleTextOptions.DEFAULTVALUE.getFormInputName(), null,
					"Default value must be a number");
			DecimalFieldOptions fieldOptions = new DecimalFieldOptions();
			fieldOptions.setUnique(basicOptions.getUnique());
			fieldOptions.setNotNull(basicOptions.getNotNull());
			fieldOptions.setDefaultValue(defaultNumber);
			fieldOptions.setNotApplicable(false);
			fieldOptions.setNotApplicableValue(0.0d);
			fieldOptions.setPrecision(precision);
			fieldOptions.setPrintoutSetting(basicOptions.getPrintoutSetting());
			fieldOptions.setStoresCurrency(storesCurrency);
			fieldOptions.setUsesLookup(usesLookup);
			field = new DecimalFieldDefn(this.relationalDataSource, table, internalFieldName, fieldName,
					fieldDesc, fieldOptions);
		} else {
			Integer defaultNumber = HttpRequestUtil.getIntegerValueStrict(request,
					PossibleTextOptions.DEFAULTVALUE.getFormInputName(), null,
					"Default value must be an integer");
			IntegerFieldOptions fieldOptions = new IntegerFieldOptions();
			fieldOptions.setUnique(basicOptions.getUnique());
			fieldOptions.setNotNull(basicOptions.getNotNull());
			fieldOptions.setPrintoutSetting(basicOptions.getPrintoutSetting());
			fieldOptions.setDefaultValue(defaultNumber);
			fieldOptions.setNotApplicable(false);
			fieldOptions.setNotApplicableValue(-1);
			fieldOptions.setStoresCurrency(storesCurrency);
			fieldOptions.setUsesLookup(usesLookup);
			field = new IntegerFieldDefn(this.relationalDataSource, table, internalFieldName, fieldName,
					fieldDesc, fieldOptions);
		}
		return field;
	}

	private BaseField generateTextField(HttpServletRequest request, TableInfo table,
			String internalFieldName, String fieldName, String fieldDesc, BasicFieldOptions basicOptions)
			throws CantDoThatException {
		BaseField field;
		String defaultValue = HttpRequestUtil.getStringValue(request,
				PossibleTextOptions.DEFAULTVALUE.getFormInputName());
		boolean usesLookup = HttpRequestUtil.getBooleanValue(request,
				PossibleBooleanOptions.USELOOKUP.getFormInputName());
		boolean tieDownLookup = HttpRequestUtil.getBooleanValue(request,
				PossibleBooleanOptions.TIEDOWNLOOKUP.getFormInputName());
		int textContentSize = Integer.valueOf(request.getParameter(PossibleListOptions.TEXTCONTENTSIZE
				.getFormInputName()));
		TextCase textCase = TextCase.valueOf(request.getParameter(
				PossibleListOptions.TEXTCASE.getFormInputName()).toUpperCase());
		TextFieldOptions textOptions = new TextFieldOptions();
		textOptions.setDefaultValue(defaultValue);
		textOptions.setNotApplicable(false);
		textOptions.setNotApplicableDescription(null);
		textOptions.setNotApplicableValue(null);
		textOptions.setNotNull(basicOptions.getNotNull());
		textOptions.setPrintoutSetting(basicOptions.getPrintoutSetting());
		textOptions.setTextCase(textCase);
		textOptions.setTextContentSize(textContentSize);
		textOptions.setTieDownLookup(tieDownLookup);
		textOptions.setUnique(basicOptions.getUnique());
		textOptions.setUsesLookup(usesLookup);
		field = new TextFieldDefn(this.relationalDataSource, table, internalFieldName, fieldName,
				fieldDesc, !TextField.HIDDEN, textOptions);
		return field;
	}

	private BaseField generateDateField(HttpServletRequest request, TableInfo table,
			String internalFieldName, String fieldName, String fieldDesc, BasicFieldOptions basicOptions)
			throws CantDoThatException {
		BaseField field;
		int dateResolution = Integer.valueOf(request.getParameter(PossibleListOptions.DATERESOLUTION
				.getFormInputName()));
		boolean defaultToNow = HttpRequestUtil.getBooleanValue(request,
				PossibleBooleanOptions.DEFAULTTONOW.getFormInputName());
		String maxAgeYearsString = request
				.getParameter(PossibleTextOptions.MAXYEARS.getFormInputName());
		Integer maxAgeYears = null;
		if (!maxAgeYearsString.equals("")) {
			maxAgeYears = Integer.valueOf(maxAgeYearsString);
		}
		String minAgeYearsString = request
				.getParameter(PossibleTextOptions.MINYEARS.getFormInputName());
		Integer minAgeYears = null;
		if (!minAgeYearsString.equals("")) {
			minAgeYears = Integer.valueOf(minAgeYearsString);
		}
		DateFieldOptions dateOptions = new DateFieldOptions();
		dateOptions.setDateResolution(dateResolution);
		dateOptions.setDefaultToNow(defaultToNow);
		dateOptions.setMaxAgeYears(maxAgeYears);
		dateOptions.setMinAgeYears(minAgeYears);
		dateOptions.setPrintoutSetting(basicOptions.getPrintoutSetting());
		dateOptions.setUnique(basicOptions.getUnique());
		dateOptions.setNotNull(basicOptions.getNotNull());
		field = new DateFieldDefn(table, internalFieldName, fieldName, fieldDesc, dateOptions);
		return field;
	}

	/*
	 * Create a field object of the correct type chosen by the user and add it to
	 * the table.<br> This is the top level function which will be called to add a
	 * field. The other addField function is called by this and handles the
	 * specifics of database and in-memory addition as well as adding the field to
	 * the table's default report.<br>
	 * 
	 * @see #addField(TableInfo, BaseField) addField(TableInfo, BaseField) does
	 * the specifics
	 */
	public BaseField addField(HttpServletRequest request, Connection conn, TableInfo table,
			String fieldType, String internalFieldName, String fieldName, String fieldDesc)
			throws SQLException, ObjectNotFoundException, DisallowedException, CantDoThatException,
			CodingErrorException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, table))) {
			throw new DisallowedException(this.authManager.getLoggedInUser(request),
					PrivilegeType.MANAGE_TABLE, table);
		}
		BaseField field = null;
		field = this.generateFieldObject(request, table, fieldType, internalFieldName, fieldName,
				fieldDesc);
		this.addField(conn, table, field, request);
		// schema change time not recorded in memory because it doesn't affect
		// summary reports
		// this.dataManagement.logLastSchemaChangeTime(request);
		UsageLogger usageLogger = new UsageLogger(this.relationalDataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logTableSchemaChange(user, table, AppAction.ADD_FIELD, "field name: " + fieldName);
		UsageLogger.startLoggingThread(usageLogger);
		return field;
	}

	// TODO: improve: takes a lot of repetitive code to do very little:
	// updateXFieldOptions are all very similar
	public void updateFieldOption(HttpServletRequest request, Connection conn, BaseField field)
			throws DisallowedException, CantDoThatException, CodingErrorException, SQLException,
			ObjectNotFoundException {
		TableInfo table = field.getTableContainingField();
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, table))) {
			throw new DisallowedException(this.authManager.getLoggedInUser(request),
					PrivilegeType.MANAGE_TABLE, table);
		}
		HibernateUtil.activateObject(field);
		String inputStart = "updateoption" + field.getInternalFieldName();
		if (field instanceof TextField) {
			this.updateTextFieldOptions(request, conn, field, inputStart);
		} else if (field instanceof DateField) {
			this.updateDateFieldOptions(request, field, inputStart);
		} else if (field instanceof DecimalField) {
			this.updateDecimalFieldOptions(request, field, inputStart);
		} else if (field instanceof IntegerFieldDefn) {
			this.updateIntegerFieldOptions(request, field, table, inputStart);
		} else if (field instanceof CheckboxField) {
			this.updateCheckboxFieldOptions(request, field, inputStart);
		} else if (field instanceof RelationField) {
			this.updateRelationFieldOptions(request, field, inputStart);
		} else if (field instanceof FileField) {
			this.updateFieldFieldOptions(request, field, inputStart);
		}
		// Simple properties common to all fields
		BaseFieldDescriptorOptionInfo printoutOption = new ListFieldDescriptorOption(
				PossibleListOptions.PRINTFORMAT);
		String formInputName = "updateoption" + field.getInternalFieldName()
				+ printoutOption.getFormInputName();
		String formInputValue = request.getParameter(formInputName);
		if (formInputValue != null) {
			FieldPrintoutSetting printoutSetting = FieldPrintoutSetting.valueOf(formInputValue);
			field.setPrintoutSetting(printoutSetting);
		}
	}

	private void updateFieldFieldOptions(HttpServletRequest request, BaseField field,
			String inputStart) throws CantDoThatException, CodingErrorException {
		FileField fileField = (FileField) field;
		FieldTypeDescriptorInfo fieldDescriptor = fileField.getFieldDescriptor();
		List<BaseFieldDescriptorOptionInfo> fieldOptions = fieldDescriptor.getOptions();
		for (BaseFieldDescriptorOptionInfo fieldOption : fieldOptions) {
			String formInputName = inputStart + fieldOption.getFormInputName();
			String formInputValue = request.getParameter(formInputName);
			if (formInputValue != null) {
				if (fieldOption instanceof ListFieldDescriptorOptionInfo) {
					if (formInputName.equals(inputStart
							+ PossibleListOptions.ATTACHMENTTYPE.getFormInputName())) {
						AttachmentType attachmentType = AttachmentType.valueOf(formInputValue);
						fileField.setAttachmentType(attachmentType);
					}
				}
			}
		}
	}

	private void updateRelationFieldOptions(HttpServletRequest request, BaseField field,
			String inputStart) throws CantDoThatException, CodingErrorException, ObjectNotFoundException {
		RelationField relationField = (RelationField) field;
		FieldTypeDescriptorInfo fieldDescriptor = relationField.getFieldDescriptor();
		List<BaseFieldDescriptorOptionInfo> fieldOptions = fieldDescriptor.getOptions();
		for (BaseFieldDescriptorOptionInfo fieldOption : fieldOptions) {
			String formInputName = inputStart + fieldOption.getFormInputName();
			String formInputValue = request.getParameter(formInputName);
			if (formInputValue != null) {
				if (fieldOption instanceof BooleanFieldDescriptorOptionInfo) {
					if (formInputName
							.equals(inputStart + PossibleBooleanOptions.MANDATORY.getFormInputName())) {
						Boolean notNull = Helpers.valueRepresentsBooleanTrue(formInputValue);
						relationField.setNotNull(notNull);
					} else if (formInputName.equals("updateoption" + field.getInternalFieldName()
							+ PossibleBooleanOptions.DEFAULTTONULL.getFormInputName())) {
						Boolean defaultToNull = Helpers.valueRepresentsBooleanTrue(formInputValue);
						relationField.setDefaultToNull(defaultToNull);
					} else if (formInputName.equals(inputStart
							+ PossibleBooleanOptions.ONETOONE.getFormInputName())) {
						Boolean oneToOne = Helpers.valueRepresentsBooleanTrue(formInputValue);
						relationField.setOneToOne(oneToOne);
					}
				} else if (fieldOption instanceof ListFieldDescriptorOptionInfo) {
					if (formInputName.equals(inputStart
							+ PossibleListOptions.LISTVALUEFIELD.getFormInputName())) {
						BaseField displayField = relationField.getRelatedTable().getField(formInputValue);
						relationField.setDisplayField(displayField);
					} else if (formInputName.equals(inputStart
							+ PossibleListOptions.LISTSECONDARYFIELD.getFormInputName())) {
						BaseField secondaryDisplayField = null;
						if (!formInputValue.equals("")) {
							secondaryDisplayField = relationField.getRelatedTable().getField(formInputValue);
						}
						relationField.setSecondaryDisplayField(secondaryDisplayField);
					}
				}
			}
		}
	}

	private void updateCheckboxFieldOptions(HttpServletRequest request, BaseField field,
			String inputStart) throws CantDoThatException, CodingErrorException {
		CheckboxField checkboxField = (CheckboxField) field;
		FieldTypeDescriptorInfo fieldDescriptor = checkboxField.getFieldDescriptor();
		List<BaseFieldDescriptorOptionInfo> fieldOptions = fieldDescriptor.getOptions();
		for (BaseFieldDescriptorOptionInfo fieldOption : fieldOptions) {
			String formInputName = inputStart + fieldOption.getFormInputName();
			String formInputValue = request.getParameter(formInputName);
			if (formInputValue != null) {
				if (fieldOption instanceof ListFieldDescriptorOptionInfo) {
					if (formInputName.equals(inputStart
							+ PossibleListOptions.CHECKBOXDEFAULT.getFormInputName())) {
						Boolean defaultValue = Helpers.valueRepresentsBooleanTrue(formInputValue);
						checkboxField.setDefault(defaultValue);
					}
				}
			}
		}
	}

	private void updateDecimalFieldOptions(HttpServletRequest request, BaseField field,
			String inputStart) throws CantDoThatException, CodingErrorException {
		DecimalField decimalField = (DecimalField) field;
		FieldTypeDescriptorInfo fieldDescriptor = decimalField.getFieldDescriptor();
		List<BaseFieldDescriptorOptionInfo> fieldOptions = fieldDescriptor.getOptions();
		for (BaseFieldDescriptorOptionInfo fieldOption : fieldOptions) {
			String formInputName = inputStart + fieldOption.getFormInputName();
			String formInputValue = request.getParameter(formInputName);
			if (formInputValue != null) {
				if (fieldOption instanceof BooleanFieldDescriptorOptionInfo) {
					if (formInputName
							.equals(inputStart + PossibleBooleanOptions.MANDATORY.getFormInputName())) {
						boolean notNull = Helpers.valueRepresentsBooleanTrue(formInputValue);
						decimalField.setNotNull(notNull);
					} else if (formInputName.equals("updateoption" + field.getInternalFieldName()
							+ PossibleBooleanOptions.STORECURRENCY.getFormInputName())) {
						boolean storesCurrency = Helpers.valueRepresentsBooleanTrue(formInputValue);
						decimalField.setStoresCurrency(storesCurrency);
					}
				} else if (fieldOption instanceof ListFieldDescriptorOptionInfo) {
					if (formInputName.equals(inputStart
							+ PossibleListOptions.NUMBERPRECISION.getFormInputName())) {
						int precision = Integer.valueOf(formInputValue);
						decimalField.setPrecision(precision);
					}
				} else if (fieldOption instanceof TextFieldDescriptorOptionInfo) {
					if (formInputName
							.equals(inputStart + PossibleTextOptions.DEFAULTVALUE.getFormInputName())) {
						if (formInputValue.equals("")) {
							decimalField.setDefault(null);
						} else {
							Double defaultValue = Double.parseDouble(formInputValue);
							decimalField.setDefault(defaultValue);
						}
					}
				}
			}
		}
	}

	private void updateDateFieldOptions(HttpServletRequest request, BaseField field, String inputStart)
			throws CantDoThatException, CodingErrorException {
		DateField dateField = (DateField) field;
		FieldTypeDescriptorInfo fieldDescriptor = dateField.getFieldDescriptor();
		List<BaseFieldDescriptorOptionInfo> fieldOptions = fieldDescriptor.getOptions();
		for (BaseFieldDescriptorOptionInfo fieldOption : fieldOptions) {
			String formInputName = inputStart + fieldOption.getFormInputName();
			String formInputValue = request.getParameter(formInputName);
			if (formInputValue != null) {
				if (fieldOption instanceof BooleanFieldDescriptorOptionInfo) {
					if (formInputName.equals(inputStart
							+ PossibleBooleanOptions.DEFAULTTONOW.getFormInputName())) {
						Boolean defaultToNow = Helpers.valueRepresentsBooleanTrue(formInputValue);
						dateField.setDefaultToNow(defaultToNow);
					} else if (formInputName.equals("updateoption" + field.getInternalFieldName()
							+ PossibleBooleanOptions.MANDATORY.getFormInputName())) {
						Boolean notNull = Helpers.valueRepresentsBooleanTrue(formInputValue);
						dateField.setNotNull(notNull);
					}
				} else if (fieldOption instanceof ListFieldDescriptorOptionInfo) {
					if (formInputName.equals(inputStart
							+ PossibleListOptions.DATERESOLUTION.getFormInputName())) {
						int dateResolution = Integer.valueOf(formInputValue);
						dateField.setDateResolution(dateResolution);
					}
				} else if (fieldOption instanceof TextFieldDescriptorOptionInfo) {
					if (formInputName.equals(inputStart + PossibleTextOptions.MAXYEARS.getFormInputName())) {
						Integer maxAgeYears = null;
						if (!formInputValue.equals("")) {
							maxAgeYears = Integer.valueOf(formInputValue);
						}
						dateField.setMaxAgeYears(maxAgeYears);
					} else if (formInputName.equals(inputStart
							+ PossibleTextOptions.MINYEARS.getFormInputName())) {
						Integer minAgeYears = null;
						if (!formInputValue.equals("")) {
							minAgeYears = Integer.valueOf(formInputValue);
						}
						dateField.setMinAgeYears(minAgeYears);
					}
				}
			}
		}
	}

	private void updateTextFieldOptions(HttpServletRequest request, Connection conn, BaseField field,
			String inputStart) throws CantDoThatException, CodingErrorException, SQLException {
		TextField textField = (TextField) field;
		FieldTypeDescriptorInfo fieldDescriptor = textField.getFieldDescriptor();
		List<BaseFieldDescriptorOptionInfo> fieldOptions = fieldDescriptor.getOptions();
		for (BaseFieldDescriptorOptionInfo fieldOption : fieldOptions) {
			String formInputName = inputStart + fieldOption.getFormInputName();
			String formInputValue = request.getParameter(formInputName);
			if (formInputValue != null) {
				if (fieldOption instanceof BooleanFieldDescriptorOptionInfo) {
					if (formInputName
							.equals(inputStart + PossibleBooleanOptions.USELOOKUP.getFormInputName())) {
						Boolean useLookup = Helpers.valueRepresentsBooleanTrue(formInputValue);
						textField.setUsesLookup(useLookup);
					} else if (formInputName.equals("updateoption" + field.getInternalFieldName()
							+ PossibleBooleanOptions.UNIQUE.getFormInputName())) {
						Boolean unique = Helpers.valueRepresentsBooleanTrue(formInputValue);
						textField.setUnique(unique);
					} else if (formInputName.equals("updateoption" + field.getInternalFieldName()
							+ PossibleBooleanOptions.TIEDOWNLOOKUP.getFormInputName())) {
						Boolean tieDownLookup = Helpers.valueRepresentsBooleanTrue(formInputValue);
						textField.setTieDownLookup(tieDownLookup);
					} else if (formInputName.equals("updateoption" + field.getInternalFieldName()
							+ PossibleBooleanOptions.MANDATORY.getFormInputName())) {
						Boolean notNull = Helpers.valueRepresentsBooleanTrue(formInputValue);
						textField.setNotNull(notNull);
					}
				} // end of BooleanFieldDescriptorOptionInfo
				else if (fieldOption instanceof ListFieldDescriptorOptionInfo) {
					if (formInputName.equals(inputStart
							+ PossibleListOptions.TEXTCONTENTSIZE.getFormInputName())) {
						int textContentSize = Integer.valueOf(formInputValue);
						textField.setContentSize(textContentSize);
					} else if (formInputName.equals("updateoption" + field.getInternalFieldName()
							+ PossibleListOptions.TEXTCASE.getFormInputName())) {
						TextCase textCase = TextCase.valueOf(formInputValue.toUpperCase());
						this.setTextCase(field, textCase);
						textField.setTextCase(textCase);
					}
				} else if (fieldOption instanceof TextFieldDescriptorOptionInfo) {
					if (formInputName
							.equals(inputStart + PossibleTextOptions.DEFAULTVALUE.getFormInputName())) {
						if (formInputValue.equals("")) {
							textField.setDefault(null);
						} else {
							textField.setDefault(formInputValue);
						}
					}
				}
			}
		}
		this.addRemoveRelevantTextIndexes(conn, textField);
	}

	private void updateIntegerFieldOptions(HttpServletRequest request, BaseField field,
			TableInfo table, String inputStart) throws CantDoThatException {
		IntegerFieldDefn integerField = (IntegerFieldDefn) field;
		FieldTypeDescriptorInfo fieldDescriptor = integerField.getFieldDescriptor();
		List<BaseFieldDescriptorOptionInfo> fieldOptions = fieldDescriptor.getOptions();
		for (BaseFieldDescriptorOptionInfo fieldOption : fieldOptions) {
			String formInputName = inputStart + fieldOption.getFormInputName();
			String formInputValue = request.getParameter(formInputName);
			if (formInputValue != null) {
				if (fieldOption instanceof BooleanFieldDescriptorOptionInfo) {
					if (formInputName
							.equals(inputStart + PossibleBooleanOptions.MANDATORY.getFormInputName())) {
						Boolean notNull = Helpers.valueRepresentsBooleanTrue(formInputValue);
						integerField.setNotNull(notNull);
					} else if (formInputName.equals("updateoption" + field.getInternalFieldName()
							+ PossibleBooleanOptions.STORECURRENCY.getFormInputName())) {
						boolean storesCurrency = Helpers.valueRepresentsBooleanTrue(formInputValue);
						integerField.setStoresCurrency(storesCurrency);
					} else if (formInputName.equals("updateoption" + field.getInternalFieldName()
							+ PossibleBooleanOptions.UNIQUE.getFormInputName())) {
						Boolean unique = Helpers.valueRepresentsBooleanTrue(formInputValue);
						integerField.setUnique(unique);
						if (unique) {
							// remove old index (if there is one) before
							// adding the unique index
							this.removeIndexWrapper(table.getInternalTableName(),
									integerField.getInternalFieldName(), false);
							this.addUniqueWrapper(table.getInternalTableName(),
									integerField.getInternalFieldName());
						} else {
							// remove the unique index
							this.removeUniqueWrapper(table.getInternalTableName(),
									integerField.getInternalFieldName());
						}
					}
				} else if (fieldOption instanceof TextFieldDescriptorOptionInfo) {
					if (formInputName
							.equals(inputStart + PossibleTextOptions.DEFAULTVALUE.getFormInputName())) {
						if (formInputValue.equals("")) {
							integerField.setDefault(null);
						} else {
							Integer defaultValue = Integer.parseInt(formInputValue);
							integerField.setDefault(defaultValue);
						}
					}
				}
			}
		}
	}

	/**
	 * Sets all values of a field in its table to a particular case. Don't do
	 * anything for large text fields
	 */
	private void setTextCase(BaseField field, TextCase textCase) throws CantDoThatException,
			SQLException {
		if (!(field instanceof TextField)) {
			throw new CantDoThatException("Can't change the case of a "
					+ field.getClass().getSimpleName() + " field");
		}
		if (textCase.equals(TextCase.ANY)) {
			return;
		}
		if (((TextField) field).getContentSize() >= TextContentSizes.FEW_PARAS.getNumChars()) {
			// Don't do anything for large text fields
			return;
		}
		String sqlCode = "UPDATE " + field.getTableContainingField().getInternalTableName();
		sqlCode += " SET " + field.getInternalFieldName() + " = " + textCase.getSqlRepresentation()
				+ "(" + field.getInternalFieldName() + ")";
		Connection conn = null;
		try {
			conn = this.relationalDataSource.getConnection();
			conn.setAutoCommit(false);
			PreparedStatement statement = conn.prepareStatement(sqlCode);
			statement.executeUpdate();
			statement.close();
			conn.commit();
		} catch (SQLException sqlex) {
			logger.error("Error setting text case: " + sqlex);
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	public void updateField(HttpServletRequest request, BaseField field, String fieldName,
			String fieldDesc) throws DisallowedException, ObjectNotFoundException {
		TableInfo table = field.getTableContainingField();
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, table))) {
			throw new DisallowedException(this.authManager.getLoggedInUser(request),
					PrivilegeType.MANAGE_TABLE, table);
		}
		HibernateUtil.activateObject(field);
		// amend field name
		// don't allow blank fieldnames
		if (fieldName != null) {
			if ((!field.getFieldName().equals(fieldName)) && (!fieldName.equals(""))) {
				field.setFieldName(fieldName);
			}
		}
		// amend field description
		// do allow blank descriptions
		if (fieldDesc != null) {
			if (!field.getFieldDescription().equals(fieldDesc)) {
				field.setFieldDescription(fieldDesc);
			}
		}
	}

	public void setFieldIndex(TableInfo table, BaseField field, int newindex)
			throws ObjectNotFoundException, CantDoThatException {
		HibernateUtil.activateObject(table);
		table.setFieldIndex(newindex, field);
	}

	/**
	 * Adds field to relational database, object database and memory.
	 */
	private void addField(Connection conn, TableInfo tableToAddTo, BaseField fieldToAdd,
			HttpServletRequest request) throws SQLException, CantDoThatException,
			ObjectNotFoundException, CodingErrorException {
		HibernateUtil.activateObject(tableToAddTo);
		HibernateUtil.currentSession().save(fieldToAdd);
		tableToAddTo.addField(fieldToAdd);
		if (fieldToAdd.getFieldCategory().savesData()) {
			SimpleReportInfo defaultReport = tableToAddTo.getDefaultReport();
			defaultReport.addTableField(fieldToAdd);
			// Do SQL
			this.addFieldToRelationalDb(conn, tableToAddTo, fieldToAdd);
			this.updateViewDbAction(conn, defaultReport, request);
		}
	}

	/**
	 * Adds field to relational database but not to object database
	 * 
	 * @throws CantDoThatException
	 *           If an internal coding bug exists caused, specifically a field
	 *           type passed is a VARCHAR field but the type is not recognised as
	 *           a text field
	 * 
	 * @see #addFieldDbAction(Connection, String, String, String, boolean)
	 * @see #addForeignKeyDbAction(Connection, String, RelationField)
	 * @see #addIndexDbAction(Connection, String, String)
	 */
	private void addFieldToRelationalDb(Connection conn, TableInfo tableToAddTo, BaseField fieldToAdd)
			throws CantDoThatException, SQLException, ObjectNotFoundException, CodingErrorException {
		String internalTableName = tableToAddTo.getInternalTableName();
		String internalFieldName = fieldToAdd.getInternalFieldName();
		String dbType = fieldToAdd.getDbType().toString().toLowerCase();
		boolean setUnique = fieldToAdd.getUnique();
		boolean setNotNull = fieldToAdd.getNotNull();
		// If field to add is the primary key, don't set SQL UNIQUE and NOT NULL
		// properties
		// These will be implied by the fact that it is a primary key - use
		// setPrimaryKeyDbAction to set it as
		// such
		if (tableToAddTo.getPrimaryKey().equals(fieldToAdd)) {
			setUnique = false;
			setNotNull = false;
		}
		// Add length of field to text field type
		if (fieldToAdd.getDbType().equals(DatabaseFieldType.VARCHAR)) {
			BaseField temp = fieldToAdd;
			// in the case of fields that look up their values
			// obtain the field that is the original source of data
			if (temp instanceof RelationField) {
				temp = ((RelationField) temp).getRelatedField();
			} else {
				// sourceFieldFound = true;
			}
			// }
			if (temp instanceof TextField) {
				dbType += "(100000)";
			} else if (temp instanceof FileField) {
				dbType += "(1000)"; // limit on filename size
			} else {
				throw new CantDoThatException(
						"Unknown text field type encountered while trying adding field to database");
			}
		}
		try {
			// Create field in DB
			this.addFieldDbAction(conn, internalTableName, internalFieldName, fieldToAdd.getFieldName(),
					dbType, setUnique);
			if (fieldToAdd.hasDefault()) {
				this.setFieldDefaultDbAction(conn, fieldToAdd);
			}
			// Add index only if not unique (will already have index) and for
			// field types where indexing makes sense.
			// Currently only small text fields and relations
			if (!setUnique) {
				// Note: don't set text field indexes, these are now set in
				// ServletSchemaMethods.addField -> updateFieldOptions
				if (fieldToAdd instanceof RelationField) {
					this.addIndexDbAction(conn, internalTableName, internalFieldName, false);
				}
			}
			if (fieldToAdd instanceof RelationField) {
				this.addForeignKeyDbAction(conn, internalTableName, (RelationField) fieldToAdd);
			}
		} catch (SQLException sqlex) {
			throw sqlex;
		}
	}

	/**
	 * Actually generates and executes the SQL for adding a field.
	 * 
	 * @see #addField(TableInfo, TextField) An example function that calls this
	 */
	private void addFieldDbAction(Connection conn, String internalTableName,
			String internalFieldName, String fieldName, String dbType, boolean isUnique)
			throws SQLException, CantDoThatException {
		// Add the field
		String SQLCode = "ALTER TABLE " + internalTableName;
		SQLCode += " ADD COLUMN " + internalFieldName + " " + dbType;
		PreparedStatement statement = conn.prepareStatement(SQLCode);
		statement.execute();
		statement.close();
		SQLCode = "COMMENT ON COLUMN " + internalTableName + "." + internalFieldName + " IS '"
				+ Helpers.rinseString(fieldName) + "'";
		statement = conn.prepareStatement(SQLCode);
		statement.execute();
		statement.close();
		if (isUnique) {
			this.addUniqueDbAction(conn, internalTableName, internalFieldName);
		}
	}

	/**
	 * Update all the existing field values in the database with the default value
	 * for that field
	 */
	private void setFieldDefaultDbAction(Connection conn, BaseField field) throws SQLException,
			CantDoThatException, ObjectNotFoundException, CodingErrorException {
		if (field.hasDefault()) {
			String internalTableName = field.getTableContainingField().getInternalTableName();
			String internalFieldName = field.getInternalFieldName();
			String SQLCode = "UPDATE " + internalTableName + " SET " + internalFieldName + "=?";
			PreparedStatement statement = conn.prepareStatement(SQLCode);
			if (field instanceof TextField) {
				String defaultValue = ((TextField) field).getDefault();
				statement.setString(1, defaultValue);
			} else if (field instanceof DecimalField) {
				Double defaultValue = ((DecimalField) field).getDefault();
				statement.setDouble(1, defaultValue);
			} else if (field instanceof IntegerField) {
				Integer defaultValue = ((IntegerField) field).getDefault();
				statement.setInt(1, defaultValue);
			} else if (field instanceof CheckboxField) {
				Boolean defaultValue = ((CheckboxField) field).getDefault();
				statement.setBoolean(1, defaultValue);
			} else if (field instanceof DateField) {
				Calendar defaultValueCalendar = ((DateField) field).getDefault();
				Timestamp defaultValue = new Timestamp(defaultValueCalendar.getTimeInMillis());
				statement.setTimestamp(1, defaultValue);
			} else {
				throw new CantDoThatException("Unable to set default value for field type "
						+ field.getFieldCategory());
			}
			statement.execute();
			statement.close();
		}
	}

	/*
	 * The top level function to call to add a database foreign key relation
	 * 
	 * @see addField(String, String, String, String, Map<String, String>)
	 * Equivalent to addField but for relation fields, not normal fields
	 */
	public RelationField addRelation(HttpServletRequest request, Connection conn,
			TableInfo tableToAddTo, String internalFieldName, String fieldName, String fieldDesc,
			TableInfo relatedTable, BaseField relatedField) throws SQLException, DisallowedException,
			CantDoThatException, ObjectNotFoundException, CodingErrorException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, tableToAddTo))) {
			throw new DisallowedException(this.authManager.getLoggedInUser(request),
					PrivilegeType.MANAGE_TABLE, tableToAddTo);
		}
		String listValueFieldInternalName = request.getParameter(PossibleListOptions.LISTVALUEFIELD
				.getFormInputName());
		String mandatoryString = request.getParameter(PossibleBooleanOptions.MANDATORY
				.getFormInputName());
		boolean notNull = Helpers.valueRepresentsBooleanTrue(mandatoryString);
		String defaultToNullString = request.getParameter(PossibleBooleanOptions.DEFAULTTONULL
				.getFormInputName());
		boolean defaultToNull = Helpers.valueRepresentsBooleanTrue(defaultToNullString);
		FieldPrintoutSetting printoutSetting = FieldPrintoutSetting.NAME_AND_VALUE;
		String oneToOneString = request
				.getParameter(PossibleBooleanOptions.ONETOONE.getFormInputName());
		boolean oneToOne = Helpers.valueRepresentsBooleanTrue(oneToOneString);
		String printoutSettingString = HttpRequestUtil.getStringValue(request,
				PossibleListOptions.PRINTFORMAT.getFormInputName());
		if (printoutSettingString != null) {
			if (!printoutSettingString.equals("")) {
				printoutSetting = FieldPrintoutSetting.valueOf(printoutSettingString.toUpperCase());
			}
		}
		// Create the relation object
		RelationFieldOptions fieldOptions = new RelationFieldOptions();
		fieldOptions.setDefaultToNull(defaultToNull);
		fieldOptions.setNotNull(notNull);
		fieldOptions.setPrintoutSetting(printoutSetting);
		fieldOptions.setOneToOne(oneToOne);
		if (oneToOne) {
			fieldOptions.setUnique(false);
		} else {
			fieldOptions.setUnique(true);
		}
		RelationField relationToAdd = new RelationFieldDefn(this.relationalDataSource, tableToAddTo,
				internalFieldName, relatedTable, relatedField, fieldOptions);
		relationToAdd.setFieldDescription(fieldDesc);
		relationToAdd.setFieldName(fieldName);
		if (listValueFieldInternalName == null) {
			// if no other field was specified for display purposes
			// use the field on which the relation is based
			relationToAdd.setDisplayField(relationToAdd.getRelatedField());
		} else {
			BaseField valueField = relatedTable.getField(listValueFieldInternalName);
			relationToAdd.setDisplayField(valueField);
		}
		// Add it to the databases and in-memory cache
		addField(conn, tableToAddTo, relationToAdd, request);
		this.dataManagement.logLastSchemaChangeTime(request);
		UsageLogger usageLogger = new UsageLogger(this.relationalDataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logTableSchemaChange(user, tableToAddTo, AppAction.ADD_FIELD, "relation: "
				+ relationToAdd);
		UsageLogger.startLoggingThread(usageLogger);
		return relationToAdd;
	}

	/**
	 * Test whether field can legally be removed from a table
	 * 
	 * @throws CantDoThatException
	 *           Thrown if the field shouldn't be removed from it's parent table,
	 *           with a message explaining why not
	 */
	private void removeFieldChecks(BaseField field, HttpServletRequest request)
			throws CantDoThatException, CodingErrorException, ObjectNotFoundException {
		// Don't allow deletion of the primary key
		if (field.equals(field.getTableContainingField().getPrimaryKey())) {
			throw new CantDoThatException("Can't delete the primary key field");
		}
		// Check the field isn't used in a relation
		SortedSet<TableInfo> relatedTables = new TreeSet<TableInfo>();
		CompanyInfo company = this.authManager.getCompanyForLoggedInUser(request);
		Set<TableInfo> allTables = company.getTables();
		for (TableInfo testTable : allTables) {
			for (BaseField testField : testTable.getFields()) {
				if (testField instanceof RelationField) {
					RelationField testRelationField = (RelationField) testField;
					if (testRelationField.getDisplayField().equals(field)
							|| testRelationField.getRelatedField().equals(field)) {
						relatedTables.add(testRelationField.getTableContainingField());
					}
				}
			}
		}
		if (relatedTables.size() > 0) {
			throw new CantDoThatException("The field " + field
					+ " is used as a relation in the following tables: " + relatedTables
					+ ". Please remove it from these tables first.");
		}
		// Check the field isn't used in any reports
		SortedSet<BaseReportInfo> reportsUsedIn = new TreeSet<BaseReportInfo>();
		for (TableInfo testTable : allTables) {
			for (BaseReportInfo testReport : testTable.getReports()) {
				// ignore default reports, those should be picked up by the
				// table checks above
				if (testReport.equals(testTable.getDefaultReport())) {
					continue;
				}
				for (ReportFieldInfo reportField : testReport.getReportFields()) {
					if (reportField.getBaseField().equals(field)) {
						reportsUsedIn.add(testReport);
					} else if (reportField instanceof ReportCalcFieldInfo) {
						String calcSQL = ((ReportCalcFieldInfo) reportField).getCalculationSQL(true);
						if (calcSQL.contains(field.getInternalFieldName())) {
							reportsUsedIn.add(testReport);
						}
					}
				}
				if (testReport instanceof SimpleReportInfo) {
					SimpleReportInfo simpleTestReport = (SimpleReportInfo) testReport;
					for (ReportFilterInfo testReportFilter : simpleTestReport.getFilters()) {
						BaseField filterField = null;
						if (testReportFilter.isFilterFieldFromReport()) {
							filterField = testReportFilter.getFilterReportField().getBaseField();
						} else {
							filterField = testReportFilter.getFilterBaseField();
						}
						if (filterField.equals(field)) {
							reportsUsedIn.add(testReport);
						}
					}
					for (JoinClauseInfo join : simpleTestReport.getJoins()) {
						if (join.isLeftPartTable()) {
							BaseField joinField = join.getLeftTableField();
							if (joinField.equals(field)) {
								reportsUsedIn.add(testReport);
							}
						}
						if (join.isRightPartTable()) {
							BaseField joinField = join.getRightTableField();
							if (joinField.equals(field)) {
								reportsUsedIn.add(testReport);
							}
						}
					}
				}
				ChartInfo reportSummary = testReport.getChart();
				for (ChartGroupingInfo grouping : reportSummary.getGroupings()) {
					BaseField groupingBaseField = grouping.getGroupingReportField().getBaseField();
					if (groupingBaseField.equals(field)) {
						reportsUsedIn.add(testReport);
					}
				}
				for (ChartAggregateInfo summaryAggregate : reportSummary.getAggregateFunctions()) {
					BaseField aggregateBaseField = summaryAggregate.getReportField().getBaseField();
					if (aggregateBaseField.equals(field)) {
						reportsUsedIn.add(testReport);
					}
					ReportFieldInfo secondaryAggregateField = summaryAggregate.getSecondaryReportField();
					if (secondaryAggregateField != null) {
						aggregateBaseField = secondaryAggregateField.getBaseField();
						if (aggregateBaseField.equals(field)) {
							reportsUsedIn.add(testReport);
						}
					}
				}
			}
		}
		if (reportsUsedIn.size() > 0) {
			String errorMessage = "The field " + field + " is used in the following reports: ";
			for (BaseReportInfo report : reportsUsedIn) {
				ModuleInfo reportModule = report.getModule();
				if (reportModule == null) {
					errorMessage += report.getParentTable() + "." + report + ", ";
				} else {
					errorMessage += reportModule + " > " + report + ", ";
				}
			}
			errorMessage = errorMessage.substring(0, errorMessage.length() - 2);
			errorMessage += ". Please remove it from fields, filters, calculations etc. in these reports before removing it from the table";
			throw new CantDoThatException(errorMessage);
		}
	}

	public void removeField(HttpServletRequest request, Connection conn, BaseField field)
			throws SQLException, DisallowedException, CantDoThatException, CodingErrorException,
			ObjectNotFoundException {
		TableInfo table = field.getTableContainingField();
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, table))) {
			throw new DisallowedException(this.authManager.getLoggedInUser(request),
					PrivilegeType.MANAGE_TABLE, table);
		}
		this.removeFieldChecks(field, request);
		this.removeFieldWithoutChecks(request, conn, field, table);
		UsageLogger usageLogger = new UsageLogger(this.relationalDataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logTableSchemaChange(user, table, AppAction.REMOVE_FIELD, "field: " + field);
		UsageLogger.startLoggingThread(usageLogger);
	}

	private void removeFieldWithoutChecks(HttpServletRequest request, Connection conn,
			BaseField field, TableInfo table) throws CantDoThatException, ObjectNotFoundException,
			SQLException, CodingErrorException {
		HibernateUtil.activateObject(table);
		ReportFieldInfo removedReportField = null;
		// remove from default report
		SimpleReportInfo defaultReport = table.getDefaultReport();
		Set<ReportFieldInfo> reportFields = defaultReport.getReportFields();
		for (ReportFieldInfo reportField : reportFields) {
			if (!reportField.isFieldFromReport()) {
				if (reportField.getBaseField().equals(field)) {
					logger.info("Removing " + reportField + " from report " + defaultReport);
					defaultReport.removeField(reportField);
					removedReportField = reportField;
					this.updateViewDbAction(conn, defaultReport, request);
				}
			}
		}
		table.removeField(field);
		if (field.getFieldCategory().savesData()) {
			// Now try to remove the field from the table:
			PreparedStatement statement = conn.prepareStatement("ALTER TABLE "
					+ table.getInternalTableName() + " DROP COLUMN " + field.getInternalFieldName());
			statement.execute();
			statement.close();
		}
		// Also delete any comments linked to the field
		PreparedStatement statement = conn
				.prepareStatement("DELETE FROM dbint_comments WHERE internalfieldname=?");
		statement.setString(1, field.getInternalFieldName());
		statement.execute();
		statement.close();
		// Persist change
		if (removedReportField != null) {
			HibernateUtil.currentSession().delete(removedReportField);
		}
		HibernateUtil.currentSession().delete(field);
		if (field instanceof FileField) {
			String fieldFolderName = this.dataManagement.getWebAppRoot() + "uploads/"
					+ field.getTableContainingField().getInternalTableName() + "/"
					+ field.getInternalFieldName();
			File directory = new File(fieldFolderName);
			if (directory.exists()) {
				try {
					FileUtils.deleteDirectory(directory);
				} catch (IOException e) {
					logger.warn("Unable to remove " + fieldFolderName + " when removing field " + table + "."
							+ field + ": " + e);
				}
			}
		}
	}

	public ReportFieldInfo addFieldToReport(HttpServletRequest request, Connection conn,
			SimpleReportInfo report, SimpleReportInfo sourceReport, BaseField field) throws SQLException,
			DisallowedException, InconsistentStateException, CantDoThatException, CodingErrorException,
			ObjectNotFoundException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, report.getParentTable()))) {
			throw new DisallowedException(this.authManager.getLoggedInUser(request),
					PrivilegeType.MANAGE_TABLE, report.getParentTable());
		}
		ReportFieldInfo newReportField = null;
		HibernateUtil.activateObject(report);
		if (sourceReport == null) {
			newReportField = report.addTableField(field);
		} else {
			ReportFieldInfo reportField = sourceReport.getReportField(field.getInternalFieldName());
			newReportField = report.addReportField(reportField);
		}
		this.updateViewDbAction(conn, report, request);
		// this.dataManagement.logLastSchemaChangeTime(request);
		UsageLogger usageLogger = new UsageLogger(this.relationalDataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logReportSchemaChange(user, report, AppAction.ADD_FIELD_TO_REPORT, "field: "
				+ field);
		UsageLogger.startLoggingThread(usageLogger);
		return newReportField;
	}

	public void addDistinctToReport(HttpServletRequest request, Connection conn,
			SimpleReportInfo report, BaseField distinctField) throws DisallowedException,
			ObjectNotFoundException, CantDoThatException, CodingErrorException, SQLException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, report.getParentTable()))) {
			throw new DisallowedException(this.authManager.getLoggedInUser(request),
					PrivilegeType.MANAGE_TABLE, report.getParentTable());
		}
		HibernateUtil.activateObject(report);
		report.addDistinctField(distinctField);
		this.updateViewDbAction(conn, report, request);
		UsageLogger usageLogger = new UsageLogger(this.relationalDataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logReportSchemaChange(user, report, AppAction.ADD_REPORT_DISTINCT, "field: "
				+ distinctField);
		UsageLogger.startLoggingThread(usageLogger);
	}

	public void removeDistinctFromReport(HttpServletRequest request, Connection conn,
			SimpleReportInfo report, BaseField distinctField) throws DisallowedException,
			ObjectNotFoundException, CantDoThatException, CodingErrorException, SQLException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, report.getParentTable()))) {
			throw new DisallowedException(this.authManager.getLoggedInUser(request),
					PrivilegeType.MANAGE_TABLE, report.getParentTable());
		}
		HibernateUtil.activateObject(report);
		report.removeDistinctField(distinctField);
		this.updateViewDbAction(conn, report, request);
		UsageLogger usageLogger = new UsageLogger(this.relationalDataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logReportSchemaChange(user, report, AppAction.REMOVE_REPORT_DISTINCT, "field: "
				+ distinctField);
		UsageLogger.startLoggingThread(usageLogger);
	}

	public void setReportFieldIndex(Connection conn, SimpleReportInfo report, ReportFieldInfo field,
			int newindex, HttpServletRequest request) throws SQLException, CodingErrorException,
			ObjectNotFoundException, CantDoThatException {
		HibernateUtil.activateObject(report);
		report.setFieldIndex(newindex, field);
		this.updateViewDbAction(conn, report, request);
	}

	public void addJoinToReport(HttpServletRequest request, Connection conn, SimpleReportInfo report,
			JoinClauseInfo join) throws DisallowedException, SQLException, CantDoThatException,
			CodingErrorException, ObjectNotFoundException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, report.getParentTable()))) {
			throw new DisallowedException(this.authManager.getLoggedInUser(request),
					PrivilegeType.MANAGE_TABLE, report.getParentTable());
		}
		HibernateUtil.activateObject(report);
		report.addJoin(join);
		this.updateViewDbAction(conn, report, request);
		this.dataManagement.logLastSchemaChangeTime(request);
		UsageLogger usageLogger = new UsageLogger(this.relationalDataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logReportSchemaChange(user, report, AppAction.ADD_JOIN_TO_REPORT, "join: " + join);
		UsageLogger.startLoggingThread(usageLogger);
	}

	public boolean isJoinUsed(SimpleReportInfo report, JoinClauseInfo join) throws CantDoThatException, CodingErrorException {
		Set<JoinClauseInfo> reportJoins = report.getJoins();
		Set<ReportFieldInfo> reportFields = report.getReportFields();
		Set<BaseField> reportBaseFields = report.getReportBaseFields();
		boolean leftUsed = false;
		boolean rightUsed = false;
		if (join.isLeftPartTable()) {
			TableInfo leftTable = join.getLeftTableField().getTableContainingField();
			if (this.tableJoinChecks(join, report, leftTable)) {
				leftUsed = true;
			}
		}
		if (join.isRightPartTable()) {
			TableInfo rightTable = join.getRightTableField().getTableContainingField();
			if (this.tableJoinChecks(join, report, rightTable)) {
				rightUsed = true;
			}
		}
		if (!(join.isLeftPartTable())) {
			BaseReportInfo leftReport = join.getLeftReportField().getParentReport();
			if (this.reportJoinChecks(join, report, leftReport)) {
				leftUsed = true;
			}
		}
		if (!(join.isRightPartTable())) {
			BaseReportInfo rightReport = join.getRightReportField().getParentReport();
			if (this.reportJoinChecks(join, report, rightReport)) {
				rightUsed = true;
			}
		}
		int usedCount = 0;
		if (leftUsed) {
			usedCount++;
		}
		if (rightUsed) {
			usedCount++;
		}
		if (usedCount < 2) {
			return false;
		}
		return true;
	}

	private boolean reportJoinChecks(JoinClauseInfo join, SimpleReportInfo report, BaseReportInfo checkReport) throws CodingErrorException, CantDoThatException {
		Set<ReportFieldInfo> reportFields = report.getReportFields();
		Set<JoinClauseInfo> reportJoins = report.getJoins();
		Set<ReportFilterInfo> filters = report.getFilters();
		// Is checkReport used directly in the report?
		for (ReportFieldInfo reportField : reportFields) {
			if (reportField.getParentReport().equals(checkReport)) {
				return true;
			}
			if (reportField instanceof ReportCalcFieldInfo) {
				String calcSQL = ((ReportCalcFieldInfo) reportField).getCalculationSQL(false);
				if (calcSQL.contains(checkReport.getInternalReportName())) {
					return true;
				}
			}
		}
		// check if it's used to join to another join later in the set
		for (JoinClauseInfo reportJoin : reportJoins) {
			if (reportJoin.getCreationTimestamp().after(join.getCreationTimestamp())) {
				if (!(reportJoin.isLeftPartTable())) {
					if (reportJoin.getLeftReportField().getParentReport().equals(checkReport)) {
						return true;
					}
				}
				if (!(reportJoin.isRightPartTable())) {
					if (reportJoin.getRightReportField().getParentReport().equals(checkReport)) {
						return true;
					}
				}
			}
		}
		for (ReportFilterInfo filter : filters) {
			if (filter.isFilterFieldFromReport()) {
				ReportFieldInfo filterField = filter.getFilterReportField();
				if (filterField.getParentReport().equals(checkReport)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean  tableJoinChecks(JoinClauseInfo join, SimpleReportInfo report, TableInfo checkTable) throws CantDoThatException, CodingErrorException {
		Set<JoinClauseInfo> reportJoins = report.getJoins();
		Set<ReportFieldInfo> reportFields = report.getReportFields();
		Set<BaseField> reportBaseFields = report.getReportBaseFields();
		Set<ReportFilterInfo> filters = report.getFilters();
		// check if checkTable is used directly in the report
		for (BaseField field : reportBaseFields) {
			if (field.getTableContainingField().equals(checkTable)) {
				return true;
			}
		}
		// Is checkTable used in any calculations?
		for(ReportFieldInfo reportField : reportFields) {
			if (reportField instanceof ReportCalcFieldInfo) {
				for (BaseField calcField : ((ReportCalcFieldInfo) reportField).getFieldsUsed()) {
					if (calcField.getTableContainingField().equals(checkTable)) {
						return true;
					}
				}
			}
		}
		// check if it's used to join to another join later in the set
		for (JoinClauseInfo reportJoin : reportJoins) {
			if (reportJoin.getCreationTimestamp().after(join.getCreationTimestamp())) {
				if (reportJoin.isLeftPartTable()) {
					if (reportJoin.getLeftTableField().getTableContainingField().equals(checkTable)) {
						return true;
					}
				}
				if (reportJoin.isRightPartTable()) {
					if (reportJoin.getRightTableField().getTableContainingField().equals(checkTable)) {
						return true;
					}
				}
			}
		}
		for (ReportFilterInfo filter : filters) {
			if (!filter.isFilterFieldFromReport()) {
				BaseField filterField = filter.getFilterBaseField();
				if (filterField.getTableContainingField().equals(checkTable)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void removeJoinFromReport(HttpServletRequest request, Connection conn,
			SimpleReportInfo report, JoinClauseInfo join) throws DisallowedException, SQLException,
			CantDoThatException, CodingErrorException, ObjectNotFoundException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, report.getParentTable()))) {
			throw new DisallowedException(this.authManager.getLoggedInUser(request),
					PrivilegeType.MANAGE_TABLE, report.getParentTable());
		}
		if (this.isJoinUsed(report, join)) {
			throw new CantDoThatException("The join " + join + " is used in the report " + report + " and can't be removed");
		}
		HibernateUtil.activateObject(report);
		report.removeJoin(join);
		this.updateViewDbAction(conn, report, request);
		HibernateUtil.currentSession().delete(join);
		this.dataManagement.logLastSchemaChangeTime(request);
		UsageLogger usageLogger = new UsageLogger(this.relationalDataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logReportSchemaChange(user, report, AppAction.REMOVE_JOIN_FROM_REPORT, "join: "
				+ join);
		UsageLogger.startLoggingThread(usageLogger);
	}

	public void addSortToReport(HttpServletRequest request, Connection conn, SimpleReportInfo report,
			ReportFieldInfo reportField, boolean ascending) throws DisallowedException,
			CantDoThatException, SQLException, CodingErrorException, ObjectNotFoundException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, report.getParentTable()))) {
			throw new DisallowedException(this.authManager.getLoggedInUser(request),
					PrivilegeType.MANAGE_TABLE, report.getParentTable());
		}
		HibernateUtil.activateObject(report);
		report.addSort(reportField, ascending);
		this.updateViewDbAction(conn, report, request);
		// this.dataManagement.logLastSchemaChangeTime(request);
	}

	public void updateSortFromReport(HttpServletRequest request, Connection conn,
			SimpleReportInfo report, ReportFieldInfo reportField, boolean ascending)
			throws DisallowedException, CantDoThatException, SQLException, CodingErrorException,
			ObjectNotFoundException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, report.getParentTable()))) {
			throw new DisallowedException(this.authManager.getLoggedInUser(request),
					PrivilegeType.MANAGE_TABLE, report.getParentTable());
		}
		HibernateUtil.activateObject(report);
		report.updateSort(reportField, ascending);
		this.updateViewDbAction(conn, report, request);
		// this.dataManagement.logLastSchemaChangeTime(request);
	}

	public void removeSortFromReport(HttpServletRequest request, Connection conn,
			SimpleReportInfo report, ReportFieldInfo reportField) throws DisallowedException,
			CantDoThatException, SQLException, CodingErrorException, ObjectNotFoundException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, report.getParentTable()))) {
			throw new DisallowedException(this.authManager.getLoggedInUser(request),
					PrivilegeType.MANAGE_TABLE, report.getParentTable());
		}
		HibernateUtil.activateObject(report);
		ReportSortInfo removedSort = report.removeSort(reportField);
		this.updateViewDbAction(conn, report, request);
		if (removedSort != null) {
			HibernateUtil.currentSession().delete(removedSort);
		}
		// this.dataManagement.logLastSchemaChangeTime(request);
	}

	public void addFilterToReport(HttpServletRequest request, Connection conn,
			SimpleReportInfo report, ReportFilterInfo filter) throws SQLException, DisallowedException,
			CantDoThatException, CodingErrorException, ObjectNotFoundException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, report.getParentTable()))) {
			throw new DisallowedException(this.authManager.getLoggedInUser(request),
					PrivilegeType.MANAGE_TABLE, report.getParentTable());
		}
		HibernateUtil.activateObject(report);
		report.addFilter(filter);
		this.updateViewDbAction(conn, report, request);
		this.dataManagement.logLastSchemaChangeTime(request);
		UsageLogger usageLogger = new UsageLogger(this.relationalDataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logReportSchemaChange(user, report, AppAction.ADD_FILTER_TO_REPORT, "filter: "
				+ filter);
		UsageLogger.startLoggingThread(usageLogger);
	}

	public ModuleInfo addModule(HttpServletRequest request, CompanyInfo company)
			throws ObjectNotFoundException, DisallowedException {
		if (company == null) {
			company = this.authManager.getCompanyForLoggedInUser(request);
		}
		// Make sure module name is unique
		String baseModuleName = "New Module";
		String moduleName = baseModuleName;
		SortedSet<ModuleInfo> modules = company.getModules();
		Set<String> existingModuleNames = new HashSet<String>();
		int indexNumber = 0;
		for (ModuleInfo existingModule : modules) {
			existingModuleNames.add(existingModule.getModuleName());
		}
		if (modules.size() > 0) {
			ModuleInfo lastModule = modules.last();
			indexNumber = lastModule.getIndexNumber() + 10;
		} else {
			indexNumber = 10;
		}
		int moduleCount = 0;
		while (existingModuleNames.contains(moduleName)) {
			moduleCount++;
			moduleName = baseModuleName + " " + String.valueOf(moduleCount);
		}
		ModuleInfo newModule = new Module(moduleName, "actions/go-home.png", indexNumber);
		newModule.setColour("blue");
		HibernateUtil.currentSession().save(newModule);
		HibernateUtil.activateObject(company);
		company.addModule(newModule);
		UsageLogger usageLogger = new UsageLogger(this.relationalDataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logReportSchemaChange(user, null, AppAction.ADD_MODULE, newModule.getModuleName());
		UsageLogger.startLoggingThread(usageLogger);
		return newModule;
	}

	public Map<TableInfo, Set<BaseReportInfo>> getViewableDataStores(HttpServletRequest request)
			throws CodingErrorException, ObjectNotFoundException {
		// Get the list of viewable tables and reports, for possible use in
		// replacing user input fields etc. with internal names
		// TODO: Shares common code with ViewMethods.getViewableReports/Tables,
		// refactor
		// Actually, if/when we add report privileges this will be obsolete
		// anyway
		Map<TableInfo, Set<BaseReportInfo>> availableDataStores = new HashMap<TableInfo, Set<BaseReportInfo>>();
		Set<TableInfo> companyTables = this.getAuthManager().getCompanyForLoggedInUser(request)
				.getTables();
		AuthenticatorInfo authenticator = this.getAuthManager().getAuthenticator();
		for (TableInfo testTable : companyTables) {
			if (authenticator.loggedInUserAllowedTo(request, PrivilegeType.VIEW_TABLE_DATA, testTable)) {
				SortedSet<BaseReportInfo> allTableReports = testTable.getReports();
				// Strip down to the set of reports the user has privileges to
				// view
				SortedSet<BaseReportInfo> viewableReports = new TreeSet<BaseReportInfo>();
				for (BaseReportInfo report : allTableReports) {
					if (authenticator.loggedInUserAllowedToViewReport(request, report)) {
						viewableReports.add(report);
					}
				}
				availableDataStores.put(testTable, Collections.unmodifiableSortedSet(viewableReports));
			}
		}
		return Collections.unmodifiableMap(availableDataStores);
	}

	public void removeFilterFromReport(HttpServletRequest request, Connection conn,
			SimpleReportInfo report, ReportFilterInfo filter) throws DisallowedException,
			ObjectNotFoundException, CantDoThatException, SQLException, CodingErrorException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, report.getParentTable()))) {
			throw new DisallowedException(this.authManager.getLoggedInUser(request),
					PrivilegeType.MANAGE_TABLE, report.getParentTable());
		}
		HibernateUtil.activateObject(report);
		report.removeFilter(filter);
		this.updateViewDbAction(conn, report, request);
		HibernateUtil.currentSession().delete(filter);
		this.dataManagement.logLastSchemaChangeTime(request);
		UsageLogger usageLogger = new UsageLogger(this.relationalDataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logReportSchemaChange(user, report, AppAction.REMOVE_FILTER_FROM_REPORT, "filter: "
				+ filter);
		UsageLogger.startLoggingThread(usageLogger);
	}

	private SortedSet<TableInfo> getTablesIncludingReferences(BaseReportInfo report,
			HttpServletRequest request) throws ObjectNotFoundException, CantDoThatException,
			CodingErrorException {
		CompanyInfo company = this.authManager.getCompanyForLoggedInUser(request);
		Set<TableInfo> allTables = company.getTables();
		SortedSet<TableInfo> tablesIncluding = new TreeSet<TableInfo>();
		TABLES_LOOP: for (TableInfo table : allTables) {
			for (BaseField field : table.getFields()) {
				if (field.getFieldCategory().equals(FieldCategory.REFERENCED_REPORT_DATA)) {
					BaseReportInfo referencedReport = ((ReferencedReportDataField) field)
							.getReferencedReport();
					if (referencedReport.equals(report)) {
						tablesIncluding.add(table);
						continue TABLES_LOOP;
					}
				}
			}
		}
		return tablesIncluding;
	}

	/**
	 * Return a set of all reports that would have to be modified before dropping
	 * the given report, i.e. those that join to this one
	 * 
	 * TODO: some overlap between this and
	 */
	private SortedSet<BaseReportInfo> getDependentReports(SimpleReportInfo report,
			HttpServletRequest request) throws CantDoThatException, ObjectNotFoundException {
		CompanyInfo company = this.authManager.getCompanyForLoggedInUser(request);
		Set<TableInfo> tables = company.getTables();
		SortedSet<BaseReportInfo> reportsUsedIn = new TreeSet<BaseReportInfo>();
		for (TableInfo table : tables) {
			for (BaseReportInfo testReport : table.getReports()) {
				// default reports won't have joins
				// and don't test for joins to self
				if (testReport.equals(testReport.getParentTable().getDefaultReport())
						|| testReport.equals(report)) {
					continue;
				}
				if (testReport instanceof SimpleReportInfo) {
					SimpleReportInfo simpleTestReport = (SimpleReportInfo) testReport;
					for (JoinClauseInfo join : simpleTestReport.getJoins()) {
						if (!join.isLeftPartTable()) {
							BaseReportInfo joinedReport = join.getLeftReportField().getParentReport();
							if (joinedReport.equals(report)) {
								reportsUsedIn.add(testReport);
							}
						}
						if (!join.isRightPartTable()) {
							BaseReportInfo joinedReport = join.getRightReportField().getParentReport();
							if (joinedReport.equals(report)) {
								reportsUsedIn.add(testReport);
							}
						}
					}
				}
			}
		}
		return reportsUsedIn;
	}

	public void addCalculationToReport(HttpServletRequest request, Connection conn,
			SimpleReportInfo report, ReportCalcFieldInfo calculationField) throws SQLException,
			DisallowedException, InconsistentStateException, CantDoThatException, CodingErrorException,
			ObjectNotFoundException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, report.getParentTable()))) {
			throw new DisallowedException(this.authManager.getLoggedInUser(request),
					PrivilegeType.MANAGE_TABLE, report.getParentTable());
		}
		HibernateUtil.activateObject(report);
		Savepoint savepoint = null;
		report.addCalculation(calculationField);
		savepoint = conn.setSavepoint("addCalculationSavepoint");
		try {
			this.updateViewDbAction(conn, report, request);
		} catch (SQLException sqlex) {
			// detect aggregate functions
			if (sqlex.getMessage().contains("must appear in the GROUP BY clause")
					|| sqlex.getMessage().contains("aggregates not allowed in GROUP BY clause")) {
				conn.rollback(savepoint);
				calculationField.setAggregateFunction(true);
				this.updateViewDbAction(conn, report, request);
			} else {
				throw sqlex;
			}
		}
		UsageLogger usageLogger = new UsageLogger(this.relationalDataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logReportSchemaChange(user, report, AppAction.ADD_CALCULATION_TO_REPORT,
				"calculation name: " + calculationField.getFieldName());
		UsageLogger.startLoggingThread(usageLogger);
	}

	public void updateCalculationInReport(HttpServletRequest request, Connection conn,
			SimpleReportInfo report, ReportCalcFieldInfo calculationField, String calculationName,
			String calculationDefn, DatabaseFieldType dbFieldType, boolean isReportHidden)
			throws DisallowedException, SQLException, ObjectNotFoundException, CantDoThatException,
			CodingErrorException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, report.getParentTable()))) {
			throw new DisallowedException(this.authManager.getLoggedInUser(request),
					PrivilegeType.MANAGE_TABLE, report.getParentTable());
		}
		HibernateUtil.activateObject(report);
		Savepoint savepoint = null;
		boolean definitionUpdate = false;
		if (!(calculationDefn.toLowerCase().equals(calculationField.getCalculationDefinition()))
				|| !(dbFieldType.equals(calculationField.getDbType()))) {
			Map<TableInfo, Set<BaseReportInfo>> availableDataStores = this.getViewableDataStores(request);
			((ReportCalcFieldDefn) calculationField).updateCalculationDefinition(calculationDefn,
					dbFieldType, availableDataStores);
			definitionUpdate = true;
		} else {
			((ReportCalcFieldDefn) calculationField).setBaseFieldName(calculationName);
			calculationField.setReportHidden(isReportHidden);
		}
		if (definitionUpdate) {
			savepoint = conn.setSavepoint("updateCalculationSavepoint");
			try {
				this.updateViewDbAction(conn, report, request);
			} catch (SQLException sqlex) {
				// detect aggregate functions
				if (sqlex.getMessage().contains("must appear in the GROUP BY clause")
						|| sqlex.getMessage().contains("aggregates not allowed in GROUP BY clause")) {
					conn.rollback(savepoint);
					calculationField.setAggregateFunction(true);
					this.updateViewDbAction(conn, report, request);
				} else {
					throw sqlex;
				}
			}
		}
		UsageLogger usageLogger = new UsageLogger(this.relationalDataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logReportSchemaChange(user, report, AppAction.UPDATE_CALCULATION_IN_REPORT,
				"calculation name: " + calculationField.getFieldName());
		UsageLogger.startLoggingThread(usageLogger);
	}

	public void returnCalculationInReportToMemory(HttpServletRequest request, Connection conn,
			SimpleReportInfo report, ReportCalcFieldInfo calculationField, String oldCalculationName,
			String oldCalculationDefn, DatabaseFieldType oldDbFieldType) throws DisallowedException,
			CodingErrorException, CantDoThatException, ObjectNotFoundException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, report.getParentTable()))) {
			throw new DisallowedException(this.authManager.getLoggedInUser(request),
					PrivilegeType.MANAGE_TABLE, report.getParentTable());
		}
		Map<TableInfo, Set<BaseReportInfo>> availableDataStores = this.getViewableDataStores(request);
		((ReportCalcFieldDefn) calculationField).updateCalculationDefinition(oldCalculationDefn,
				oldDbFieldType, availableDataStores);
		((ReportCalcFieldDefn) calculationField).setBaseFieldName(oldCalculationName);
	}

	private void removeFieldFromReportChecks(ReportFieldInfo reportField, HttpServletRequest request)
			throws CantDoThatException, CodingErrorException, ObjectNotFoundException {
		// check the field isn't used in one of the report's own charts
		for (ChartInfo chart : reportField.getParentReport().getSavedCharts()) {
			Set<ChartAggregateInfo> aggFns = chart.getAggregateFunctions();
			for (ChartAggregateInfo aggFn : aggFns) {
				ReportFieldInfo aggReportField = aggFn.getReportField();
				if (aggReportField.equals(reportField)) {
					throw new CantDoThatException("Please remove the chart calculation " + aggFn
							+ " before removing the report field");
				}
				ReportFieldInfo secondaryAggReportField = aggFn.getSecondaryReportField();
				if (secondaryAggReportField != null) {
					if (secondaryAggReportField.equals(reportField)) {
						throw new CantDoThatException("Please remove the report summary calculation " + aggFn
								+ " before removing the report field");
					}
				}
			}
			for (ChartGroupingInfo grouping : chart.getGroupings()) {
				if (grouping.getGroupingReportField().equals(reportField)) {
					throw new CantDoThatException("Please remove the chart grouping on " + reportField
							+ " before removing the report field");
				}
			}
			if (reportField.equals(chart.getFilterReportField())) {
				throw new CantDoThatException("Please remove the chart filter on " + reportField
						+ " before removing the report field");
			}
		}
		BaseReportInfo thisReport = reportField.getParentReport();
		// check the field isn't referenced from any other reports
		SortedSet<BaseReportInfo> reportsUsedIn = new TreeSet<BaseReportInfo>();
		CompanyInfo company = this.authManager.getCompanyForLoggedInUser(request);
		Set<TableInfo> allTables = company.getTables();
		for (TableInfo testTable : allTables) {
			for (BaseReportInfo testReport : testTable.getReports()) {
				if (testReport.equals(testReport.getParentTable().getDefaultReport())) {
					continue;
				}
				for (ReportFieldInfo testReportField : testReport.getReportFields()) {
					if (testReportField.isFieldFromReport()) {
						BaseReportInfo reportFieldIsFrom = testReportField.getReportFieldIsFrom();
						if (reportFieldIsFrom.equals(reportField.getParentReport())
								&& testReportField.getBaseField().equals(reportField.getBaseField())) {
							if (!((reportField instanceof ReportCalcFieldInfo) && (reportField
									.equals(testReportField)))) {
								reportsUsedIn.add(testReportField.getParentReport());
							}
						}
					}
					if (testReportField instanceof ReportCalcFieldInfo) {
						String calcSQL = ((ReportCalcFieldInfo) testReportField).getCalculationSQL(true);
						if (calcSQL.contains(reportField.getParentReport().getInternalReportName() + "."
								+ reportField.getInternalFieldName())) {
							if (!testReportField.equals(reportField)) {
								reportsUsedIn.add(testReport);
							}
						}
					}
				}
				if (testReport instanceof SimpleReportInfo) {
					SimpleReportInfo simpleTestReport = (SimpleReportInfo) testReport;
					for (JoinClauseInfo join : simpleTestReport.getJoins()) {
						if (!join.isLeftPartTable()) {
							ReportFieldInfo joinReportField = join.getLeftReportField();
							if (joinReportField.equals(reportField)) {
								reportsUsedIn.add(testReport);
							}
						}
						if (!join.isRightPartTable()) {
							ReportFieldInfo joinReportField = join.getRightReportField();
							if (joinReportField.equals(reportField)) {
								reportsUsedIn.add(testReport);
							}
						}
					}
				}
			}
		}
		// check field isn't referenced from any calcs in the same report
		for (ReportFieldInfo testReportField : reportField.getParentReport().getReportFields()) {
			if (testReportField instanceof ReportCalcFieldInfo) {
				if (reportField.isFieldFromReport()) {
					// Field references calculation in another report, that's ok
					continue;
				}
				ReportCalcFieldInfo testCalc = (ReportCalcFieldInfo) testReportField;
				String calcSQL = testCalc.getCalculationSQL(false);
				if (calcSQL.contains(reportField.getInternalFieldName())) {
					throw new CantDoThatException("The report field " + reportField
							+ " is referenced by the calculation " + testCalc
							+ ". Please remove or update this calculation before removing " + reportField);
				}
			}
		}
		if (reportsUsedIn.size() > 0) {
			String errorMessage = "The report field " + reportField
					+ " is used in the following reports: ";
			for (BaseReportInfo report : reportsUsedIn) {
				ModuleInfo module = report.getModule();
				if (module == null) {
					errorMessage = errorMessage + report.getParentTable() + "." + report + ", ";
				} else {
					errorMessage = errorMessage + module + " > " + report + ", ";
				}
			}
			errorMessage = errorMessage.substring(0, errorMessage.length() - 2);
			errorMessage += ". Please remove all references in these reports before removing it from the "
					+ reportField.getParentReport() + " report";
			throw new CantDoThatException(errorMessage);
		}
	}

	public void removeFieldFromReport(HttpServletRequest request, Connection conn,
			ReportFieldInfo reportField) throws SQLException, DisallowedException, CantDoThatException,
			CodingErrorException, ObjectNotFoundException {
		SimpleReportInfo report = (SimpleReportInfo) reportField.getParentReport();
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, report.getParentTable()))) {
			throw new DisallowedException(this.authManager.getLoggedInUser(request),
					PrivilegeType.MANAGE_TABLE, report.getParentTable());
		}
		HibernateUtil.activateObject(report);
		this.removeFieldFromReportChecks(reportField, request);
		report.removeField(reportField);
		if (reportField.equals(report.getWordCloudField())) {
			report.setWordCloudField(null);
		}
		ReportMapInfo map = report.getMap();
		if (map != null) {
			if (reportField.equals(map.getPostcodeField())) {
				map.setPostcodeField(null);
			}
			if (reportField.equals(map.getColourField())) {
				map.setColourField(null);
			}
		}
		this.updateViewDbAction(conn, report, request);
		HibernateUtil.currentSession().delete(reportField);
		UsageLogger usageLogger = new UsageLogger(this.relationalDataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logReportSchemaChange(user, report, AppAction.REMOVE_FIELD_FROM_REPORT, "field: "
				+ reportField);
		UsageLogger.startLoggingThread(usageLogger);
	}

	public void setChartFilter(HttpServletRequest request, BaseReportInfo report,
			SummaryFilter chartFilter) throws SQLException, DisallowedException, ObjectNotFoundException,
			CantDoThatException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, report.getParentTable()))) {
			throw new DisallowedException(this.authManager.getLoggedInUser(request),
					PrivilegeType.MANAGE_TABLE, report.getParentTable());
		}
		ChartInfo reportSummary = report.getChart();
		HibernateUtil.activateObject(reportSummary);
		reportSummary.setChartFilter(chartFilter);
		this.dataManagement.logLastSchemaChangeTime(request);
		// Test change by selecting rows from the database
		CompanyInfo company = this.getAuthManager().getCompanyForLoggedInUser(request);
		Map<BaseField, String> blankFilterValues = new HashMap<BaseField, String>();
		ChartDataInfo reportSummaryData = this.getDataManagement().getChartData(company,
				report.getChart(), blankFilterValues, false);
		UsageLogger usageLogger = new UsageLogger(this.relationalDataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logReportSchemaChange(user, report, AppAction.SET_CHART_FILTER, "chart filter: "
				+ chartFilter);
		UsageLogger.startLoggingThread(usageLogger);
	}

	public void setChartFilterField(HttpServletRequest request, BaseReportInfo report,
			ReportFieldInfo reportField) throws SQLException, DisallowedException,
			ObjectNotFoundException, CantDoThatException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, report.getParentTable()))) {
			throw new DisallowedException(this.authManager.getLoggedInUser(request),
					PrivilegeType.MANAGE_TABLE, report.getParentTable());
		}
		ChartInfo chart = report.getChart();
		HibernateUtil.activateObject(chart);
		chart.setFilterReportField(reportField);
		this.dataManagement.logLastSchemaChangeTime(request);
		// Test change by selecting rows from the database
		CompanyInfo company = this.getAuthManager().getCompanyForLoggedInUser(request);
		Map<BaseField, String> blankFilterValues = new HashMap<BaseField, String>();
		ChartDataInfo reportSummaryData = this.getDataManagement().getChartData(company,
				report.getChart(), blankFilterValues, false);
		UsageLogger usageLogger = new UsageLogger(this.relationalDataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logReportSchemaChange(user, report, AppAction.SET_CHART_FILTER_FIELD,
				"report field: " + reportField);
		UsageLogger.startLoggingThread(usageLogger);
	}

	public void setChartRange(HttpServletRequest request, BaseReportInfo report, int rangePercent,
			boolean rangeDirection) throws SQLException, DisallowedException, ObjectNotFoundException,
			CantDoThatException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, report.getParentTable()))) {
			throw new DisallowedException(this.authManager.getLoggedInUser(request),
					PrivilegeType.MANAGE_TABLE, report.getParentTable());
		}
		ChartInfo chart = report.getChart();
		HibernateUtil.activateObject(chart);
		chart.setRangePercent(rangePercent);
		chart.setRangeDirection(rangeDirection);
		this.dataManagement.logLastSchemaChangeTime(request);
		// Test change by selecting rows from the database
		CompanyInfo company = this.getAuthManager().getCompanyForLoggedInUser(request);
		Map<BaseField, String> blankFilterValues = new HashMap<BaseField, String>();
		ChartDataInfo reportSummaryData = this.getDataManagement().getChartData(company,
				report.getChart(), blankFilterValues, false);
		UsageLogger usageLogger = new UsageLogger(this.relationalDataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		String logString = "";
		if (rangePercent < 100) {
			if (rangeDirection) {
				logString = "Upper ";
			} else {
				logString = "Lower ";
			}
		}
		logString += rangePercent + "%";
		usageLogger.logReportSchemaChange(user, report, AppAction.SET_CHART_RANGE, logString);
		UsageLogger.startLoggingThread(usageLogger);
	}

	public void addGroupingToChart(HttpServletRequest request, ReportFieldInfo groupingReportField,
			SummaryGroupingModifier groupingModifer) throws DisallowedException, CantDoThatException,
			ObjectNotFoundException, SQLException {
		HibernateUtil.activateObject(groupingReportField);
		BaseReportInfo report = groupingReportField.getParentReport();
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, report.getParentTable()))) {
			throw new DisallowedException(this.authManager.getLoggedInUser(request),
					PrivilegeType.MANAGE_TABLE, report.getParentTable());
		}
		ChartInfo chart = report.getChart();
		HibernateUtil.activateObject(chart);
		chart.addGrouping(groupingReportField, groupingModifer);
		this.dataManagement.logLastSchemaChangeTime(request);
		// Test change by selecting rows from the database
		CompanyInfo company = this.getAuthManager().getCompanyForLoggedInUser(request);
		Map<BaseField, String> blankFilterValues = new HashMap<BaseField, String>();
		ChartDataInfo reportSummaryData = this.getDataManagement().getChartData(company,
				report.getChart(), blankFilterValues, false);
		UsageLogger usageLogger = new UsageLogger(this.relationalDataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logReportSchemaChange(user, report, AppAction.ADD_GROUPING_TO_CHART, "field: "
				+ groupingReportField);
		UsageLogger.startLoggingThread(usageLogger);
	}

	public void removeGroupingFromChart(HttpServletRequest request,
			ReportFieldInfo groupingReportField) throws DisallowedException, ObjectNotFoundException,
			SQLException, CantDoThatException {
		BaseReportInfo report = groupingReportField.getParentReport();
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, report.getParentTable()))) {
			throw new DisallowedException(this.authManager.getLoggedInUser(request),
					PrivilegeType.MANAGE_TABLE, report.getParentTable());
		}
		ChartInfo chart = report.getChart();
		HibernateUtil.activateObject(chart);
		ChartGroupingInfo removedGrouping = chart.removeGrouping(groupingReportField);
		this.dataManagement.logLastSchemaChangeTime(request);
		// Test change by selecting rows from the database
		CompanyInfo company = this.getAuthManager().getCompanyForLoggedInUser(request);
		Map<BaseField, String> blankFilterValues = new HashMap<BaseField, String>();
		ChartDataInfo reportSummaryData = this.getDataManagement().getChartData(company,
				report.getChart(), blankFilterValues, false);
		UsageLogger usageLogger = new UsageLogger(this.relationalDataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logReportSchemaChange(user, report, AppAction.REMOVE_GROUPING_FROM_CHART, "field: "
				+ groupingReportField);
		UsageLogger.startLoggingThread(usageLogger);
	}

	public void addFunctionToChart(HttpServletRequest request, ChartAggregateInfo addedAggFn)
			throws DisallowedException, CantDoThatException, ObjectNotFoundException, SQLException {
		BaseReportInfo report = addedAggFn.getReportField().getParentReport();
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, report.getParentTable()))) {
			throw new DisallowedException(this.authManager.getLoggedInUser(request),
					PrivilegeType.MANAGE_TABLE, report.getParentTable());
		}
		ChartInfo chart = report.getChart();
		HibernateUtil.activateObject(chart);
		chart.addFunction(addedAggFn);
		this.dataManagement.logLastSchemaChangeTime(request);
		UsageLogger usageLogger = new UsageLogger(this.relationalDataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logReportSchemaChange(user, report, AppAction.ADD_FUNCTION_TO_CHART, "function: "
				+ addedAggFn);
		UsageLogger.startLoggingThread(usageLogger);
		// Test change by selecting rows from the database
		CompanyInfo company = this.getAuthManager().getCompanyForLoggedInUser(request);
		Map<BaseField, String> blankFilterValues = new HashMap<BaseField, String>();
		ChartDataInfo reportSummaryData = this.getDataManagement().getChartData(company,
				report.getChart(), blankFilterValues, false);
	}

	public void removeFunctionFromChart(HttpServletRequest request, BaseReportInfo report,
			String internalAggregateName) throws DisallowedException, CantDoThatException,
			ObjectNotFoundException, SQLException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, report.getParentTable()))) {
			throw new DisallowedException(this.authManager.getLoggedInUser(request),
					PrivilegeType.MANAGE_TABLE, report.getParentTable());
		}
		ChartInfo chart = report.getChart();
		HibernateUtil.activateObject(chart);
		ChartAggregateInfo removedFunction = chart.removeFunction(internalAggregateName);
		this.dataManagement.logLastSchemaChangeTime(request);
		// Test change by selecting rows from the database
		CompanyInfo company = this.getAuthManager().getCompanyForLoggedInUser(request);
		Map<BaseField, String> blankFilterValues = new HashMap<BaseField, String>();
		ChartDataInfo reportSummaryData = this.getDataManagement().getChartData(company,
				report.getChart(), blankFilterValues, false);
		HibernateUtil.currentSession().delete(removedFunction);
		UsageLogger usageLogger = new UsageLogger(this.relationalDataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logReportSchemaChange(user, report, AppAction.REMOVE_FUNCTION_FROM_CHART,
				"function: " + removedFunction);
		UsageLogger.startLoggingThread(usageLogger);
	}

	public void saveChart(HttpServletRequest request, BaseReportInfo report, String summaryTitle)
			throws DisallowedException, CantDoThatException, ObjectNotFoundException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, report.getParentTable()))) {
			throw new DisallowedException(this.authManager.getLoggedInUser(request),
					PrivilegeType.MANAGE_TABLE, report.getParentTable());
		}
		ChartInfo templateChart = report.getChart();
		Set<ChartAggregateInfo> aggregates = templateChart.getAggregateFunctions();
		Set<ChartGroupingInfo> groupings = templateChart.getGroupings();
		if (aggregates.size() == 0) {
			throw new CantDoThatException(
					"To save a report summary, it must contain one or more functions");
		}
		HibernateUtil.activateObject(templateChart);
		ChartInfo savedChart = new ChartDefn(report, summaryTitle, true);
		HibernateUtil.currentSession().save(savedChart);
		// Move aggregates from template summary to new summary
		for (ChartAggregateInfo aggregate : aggregates) {
			savedChart.addFunction(aggregate);
			ChartAggregateInfo removedFunction = templateChart.removeFunction(aggregate
					.getInternalAggregateName());
		}
		// Move groupings from template summary to new summary
		for (ChartGroupingInfo grouping : groupings) {
			savedChart.addGrouping(grouping.getGroupingReportField(), grouping.getGroupingModifier());
			ChartGroupingInfo removedGrouping = templateChart.removeGrouping(grouping
					.getGroupingReportField());
		}
		// Any date range filter
		savedChart.setChartFilter(templateChart.getChartFilter());
		templateChart.setChartFilter(null);
		savedChart.setFilterReportField(templateChart.getFilterReportField());
		templateChart.setFilterReportField(null);
		// Range (row limit)
		savedChart.setRangeDirection(templateChart.getRangeDirection());
		templateChart.setRangeDirection(true);
		savedChart.setRangePercent(templateChart.getRangePercent());
		templateChart.setRangePercent(100);
		// Summary title
		report.saveChart(savedChart);
		templateChart.setTitle("");
		this.dataManagement.logLastSchemaChangeTime(request);
		UsageLogger usageLogger = new UsageLogger(this.relationalDataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logReportSchemaChange(user, report, AppAction.SAVE_CHART, "title: " + summaryTitle);
		UsageLogger.startLoggingThread(usageLogger);
	}

	public void removeChart(HttpServletRequest request, ChartInfo reportSummary)
			throws DisallowedException, CantDoThatException, ObjectNotFoundException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, reportSummary.getReport().getParentTable()))) {
			throw new DisallowedException(this.authManager.getLoggedInUser(request),
					PrivilegeType.MANAGE_TABLE, reportSummary.getReport().getParentTable());
		}
		BaseReportInfo report = reportSummary.getReport();
		if (reportSummary.equals(report.getChart())) {
			throw new CantDoThatException("The default report summary can't be removed");
		}
		HibernateUtil.activateObject(report);
		report.removeSavedChart(reportSummary);
		// Move the saved summary definition back to the default summary
		ChartInfo oldDefaultChart = report.getChart();
		((BaseReportDefn) report).setChart(reportSummary);
		report.removeSavedChart(oldDefaultChart);
		HibernateUtil.currentSession().delete(oldDefaultChart);
		this.dataManagement.logLastSchemaChangeTime(request);
		UsageLogger usageLogger = new UsageLogger(this.relationalDataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logReportSchemaChange(user, report, AppAction.REMOVE_CHART, "title: "
				+ reportSummary.getTitle());
		UsageLogger.startLoggingThread(usageLogger);
	}

	/**
	 * Called uniquely by getTable
	 */
	private TableInfo getTableByName(HttpServletRequest request, String tableName)
			throws ObjectNotFoundException, DisallowedException {
		CompanyInfo company = this.getAuthManager().getCompanyForLoggedInUser(request);
		Set<TableInfo> companyTables = company.getTables();
		for (TableInfo table : companyTables) {
			if (table.getTableName().equals(tableName)) {
				this.tableCache.put(company.getInternalCompanyName() + tableName, table);
				if (!this.userAllowedToAccessTable(request, table)) {
					throw new DisallowedException(this.authManager.getLoggedInUser(request),
							PrivilegeType.VIEW_TABLE_DATA, table);
				} else {
					return table;
				}
			}
		}
		// if we've got to here the table hasn't been found
		throw new ObjectNotFoundException("The table '" + tableName + "' doesn't exist");
	}

	/**
	 * To access a table, the logged in user either has to have view privileges on
	 * that table, or be an administrator of the company the table is in
	 */
	private boolean userAllowedToAccessTable(HttpServletRequest request, TableInfo table)
			throws ObjectNotFoundException {
		if (this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.VIEW_TABLE_DATA, table)) {
			return true;
		}
		CompanyInfo company = this.authManager.getCompanyForLoggedInUser(request);
		if (this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.ADMINISTRATE)
				&& company.getTables().contains(table)) {
			return true;
		}
		return false;
	}

	public TableInfo getTable(HttpServletRequest request, String internalTableName)
			throws ObjectNotFoundException, DisallowedException {
		TableInfo cachedTable = this.tableCache.get(internalTableName);
		if (cachedTable != null) {
			if (!this.userAllowedToAccessTable(request, cachedTable)) {
				throw new DisallowedException(this.authManager.getLoggedInUser(request),
						PrivilegeType.VIEW_TABLE_DATA, cachedTable);
			} else {
				return cachedTable;
			}
		}
		CompanyInfo company = this.getAuthManager().getCompanyForLoggedInUser(request);
		/*
		 * Treat internal table name as a public table name. Amalgamation of company
		 * ID + table name *should* be unique
		 */
		cachedTable = this.tableCache.get(company.getInternalCompanyName() + internalTableName);
		if (cachedTable != null) {
			if (!this.userAllowedToAccessTable(request, cachedTable)) {
				throw new DisallowedException(this.authManager.getLoggedInUser(request),
						PrivilegeType.VIEW_TABLE_DATA, cachedTable);
			} else {
				return cachedTable;
			}
		}
		Set<TableInfo> companyTables = company.getTables();
		// Not in cache, look through one by one
		TableInfo comparisonTable = new TableDefn(internalTableName, "", "");
		for (TableInfo companyTable : companyTables) {
			if (companyTable.equals(comparisonTable)) {
				this.tableCache.put(internalTableName, companyTable);
				// to retrieve a table, user either has to have view
				// privileges on that table,
				// or be an administrator of the company the table is in
				if (!this.userAllowedToAccessTable(request, companyTable)) {
					throw new DisallowedException(this.authManager.getLoggedInUser(request),
							PrivilegeType.VIEW_TABLE_DATA, companyTable);
				} else {
					return companyTable;
				}
			}
		}
		// Not found by internal name, try by name
		return this.getTableByName(request, internalTableName);
	}

	public TableInfo findTableContainingReport(HttpServletRequest request, String reportInternalName)
			throws ObjectNotFoundException, DisallowedException {
		AuthenticatorInfo authenticator = this.getAuthManager().getAuthenticator();
		TableInfo cachedTable = this.reportTableCache.get(reportInternalName);
		if (cachedTable != null) {
			if (authenticator.loggedInUserAllowedTo(request, PrivilegeType.VIEW_TABLE_DATA, cachedTable)) {
				return cachedTable;
			} else {
				throw new DisallowedException(this.authManager.getLoggedInUser(request),
						PrivilegeType.VIEW_TABLE_DATA, cachedTable);
			}
		}
		Set<TableInfo> companyTables = this.getAuthManager().getCompanyForLoggedInUser(request)
				.getTables();
		for (TableInfo table : companyTables) {
			for (BaseReportInfo report : table.getReports()) {
				if (report.getInternalReportName().equals(reportInternalName)) {
					this.reportTableCache.put(reportInternalName, table);
					if (authenticator.loggedInUserAllowedTo(request, PrivilegeType.VIEW_TABLE_DATA, table)) {
						return table;
					} else {
						throw new DisallowedException(this.authManager.getLoggedInUser(request),
								PrivilegeType.VIEW_TABLE_DATA, table);
					}
				}
			}
		}
		throw new ObjectNotFoundException("Report '" + reportInternalName + "' is not in any table");
	}

	private TableInfo findTableContainingReportWithoutChecks(String reportInternalName,
			HttpServletRequest request) throws ObjectNotFoundException {
		TableInfo cachedTable = this.reportTableCache.get(reportInternalName);
		if (cachedTable != null) {
			return cachedTable;
		}
		Set<TableInfo> companyTables = this.getAuthManager().getCompanyForLoggedInUser(request)
				.getTables();
		for (TableInfo table : companyTables) {
			for (BaseReportInfo report : table.getReports()) {
				if (report.getInternalReportName().equals(reportInternalName)) {
					this.reportTableCache.put(reportInternalName, table);
					return table;
				}
			}
		}
		throw new ObjectNotFoundException("Report '" + reportInternalName + "' is not in any table");
	}

	public TableInfo findTableContainingField(HttpServletRequest request, String internalFieldName)
			throws ObjectNotFoundException, DisallowedException {
		CompanyInfo company = this.getAuthManager().getCompanyForLoggedInUser(request);
		Set<TableInfo> tables = company.getTables();
		for (TableInfo table : tables) {
			for (BaseField field : table.getFields()) {
				if (field.getInternalFieldName().equals(internalFieldName)) {
					if (this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
							PrivilegeType.VIEW_TABLE_DATA, table)) {
						return table;
					} else {
						throw new DisallowedException(this.authManager.getLoggedInUser(request),
								PrivilegeType.VIEW_TABLE_DATA, table);
					}
				}
			}
		}
		throw new ObjectNotFoundException("Field '" + internalFieldName + "' is not in any table");
	}

	public ReportFieldInfo findReportFieldByInternalName(HttpServletRequest request,
			String internalFieldName) throws ObjectNotFoundException, DisallowedException,
			CodingErrorException {
		// look through report fields, some of which are calculations
		Set<TableInfo> companyTables = this.getAuthManager().getCompanyForLoggedInUser(request)
				.getTables();
		AuthenticatorInfo authenticator = this.authManager.getAuthenticator();
		for (TableInfo table : companyTables) {
			for (BaseReportInfo report : table.getReports()) {
				for (ReportFieldInfo reportField : report.getReportFields()) {
					if (reportField.getInternalFieldName().equals(internalFieldName)) {
						if (authenticator.loggedInUserAllowedToViewReport(request, report)) {
							return reportField;
						} else {
							// Not strictly the right exception, but the best we
							// can do
							logger.error("Found report field " + reportField + "in report " + report
									+ " but user not allowed to view that report");
							throw new DisallowedException(this.authManager.getLoggedInUser(request),
									PrivilegeType.VIEW_TABLE_DATA, report.getParentTable());
						}
					}
				}
			}
		}
		// not in any calculations either
		throw new ObjectNotFoundException("Can't find a field with the internal name "
				+ internalFieldName + "  anywhere in the organisation");
	}

	public AuthManagerInfo getAuthManager() {
		return this.authManager;
	}

	public DataManagementInfo getDataManagement() {
		return this.dataManagement;
	}

	public DataSource getDataSource() {
		return this.relationalDataSource;
	}

	public String toString() {
		return "DatabaseDefn: Core agileBase methods";
	}

	/**
	 * @param fieldToAdd
	 *          the field object representing the relation
	 */
	private void addForeignKeyDbAction(Connection conn, String internalTableName,
			RelationField fieldToAdd) throws SQLException {
		String relatedTableInternalName = fieldToAdd.getRelatedTable().getInternalTableName();
		String internalFieldName = fieldToAdd.getInternalFieldName();
		String SQLCode = "ALTER TABLE " + internalTableName;
		SQLCode += " ADD CONSTRAINT " + Naming.makeFKeyConstraintName(fieldToAdd);
		SQLCode += " FOREIGN KEY (" + internalFieldName + ")";
		SQLCode += " REFERENCES " + relatedTableInternalName + "("
				+ fieldToAdd.getRelatedField().getInternalFieldName() + ")";
		SQLCode += " ON UPDATE " + fieldToAdd.getOnUpdateAction().toString();
		SQLCode += " ON DELETE " + fieldToAdd.getOnDeleteAction().toString();
		PreparedStatement statement = conn.prepareStatement(SQLCode);
		statement.execute();
		statement.close();
	}

	/**
	 * Generates and executes the SQL to add a single-column index
	 * 
	 * @param conn
	 *          Database connection to work with
	 * @param internalTableName
	 *          Table of index
	 * @param internalFieldName
	 * @param caseInsensitiveIndex
	 *          If true will make an index using the function lower(), so it can
	 *          be used when quick filtering which filters on the lowercase input
	 *          value
	 */
	private void addIndexDbAction(Connection conn, String internalTableName,
			String internalFieldName, boolean caseInsensitiveIndex) throws SQLException {
		String indexName = Naming.makeCompositeId(internalTableName, internalFieldName);
		if (caseInsensitiveIndex) {
			indexName = "l_" + indexName;
		}
		String SQLCode = "CREATE INDEX " + indexName;
		if (caseInsensitiveIndex) {
			SQLCode += " ON " + internalTableName + "(lower(" + internalFieldName
					+ ") varchar_pattern_ops)";
		} else {
			SQLCode += " ON " + internalTableName + "(" + internalFieldName + ")";
		}
		PreparedStatement statement = conn.prepareStatement(SQLCode);
		statement.execute();
		statement.close();
	}

	/**
	 * @see #addIndexDbAction(Connection, String, String, boolean)
	 */
	private void removeIndexDbAction(Connection conn, String internalTableName,
			String internalFieldName, boolean caseInsensitiveIndex) throws SQLException {
		String indexName = Naming.makeCompositeId(internalTableName, internalFieldName);
		if (caseInsensitiveIndex) {
			indexName = "l_" + indexName;
		}
		String SQLCode = "DROP INDEX IF EXISTS " + indexName;
		PreparedStatement statement = conn.prepareStatement(SQLCode);
		statement.execute();
		statement.close();
	}

	/**
	 * Generates and executes SQL to create a unique constraint on a field, using
	 * an index. If this fails due to duplicates, report the duplicates
	 */
	private void addUniqueDbAction(Connection conn, String internalTableName, String internalFieldName)
			throws SQLException, CantDoThatException {
		// Use the same naming convention that postgresql uses
		String indexName = internalTableName + "_" + internalFieldName + "_key";
		String SQLCode = "CREATE UNIQUE INDEX " + indexName + " ON " + internalTableName + "("
				+ internalFieldName + ")";
		PreparedStatement statement = null;
		try {
			statement = conn.prepareStatement(SQLCode);
			statement.execute();
		} catch (SQLException sqlex) {
			if (sqlex.getMessage().contains("Table contains duplicated values")) {
				// Find the actual duplicates
				String errorMessage = "There are duplicate values: ";
				Connection dupConn = null;
				try {
					dupConn = this.relationalDataSource.getConnection();
					dupConn.setAutoCommit(false);
					SQLCode = "SELECT " + internalFieldName + ", count(*) FROM " + internalTableName;
					SQLCode += " GROUP BY " + internalFieldName;
					SQLCode += " HAVING count(*) > 1 ORDER BY " + internalFieldName;
					PreparedStatement dupStatement = dupConn.prepareStatement(SQLCode);
					ResultSet results = dupStatement.executeQuery();
					RESULTSLOOP: while (results.next()) {
						if (errorMessage.length() > 100) {
							errorMessage += "...";
							break RESULTSLOOP;
						} else {
							errorMessage += results.getString(1) + ", ";
						}
					}
					results.close();
					dupStatement.close();
				} catch (SQLException dsqlex) {
					logger.error("Error finding duplicate values while setting unique property: " + dsqlex);
					throw new CantDoThatException("There are duplicate values", dsqlex);
				} finally {
					if (dupConn != null) {
						dupConn.close();
					}
				}
				throw new CantDoThatException(errorMessage, sqlex);
			} else {
				throw sqlex;
			}
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
	}

	private void removeUniqueDbAction(Connection conn, String internalTableName,
			String internalFieldName) throws SQLException {
		// Use the same naming convention that postgresql uses
		String indexName = internalTableName + "_" + internalFieldName + "_key";
		String SQLCode = "DROP INDEX IF EXISTS " + indexName;
		PreparedStatement statement = conn.prepareStatement(SQLCode);
		statement.execute();
		statement.close();
	}

	/*
	 * runs removeIndexDbAction, providing a database connection and taking no
	 * action on error apart from logging
	 */
	private void removeIndexWrapper(String internalTableName, String internalFieldName,
			boolean caseInsensitiveIndex) {
		try {
			Connection conn = this.relationalDataSource.getConnection();
			try {
				conn.setAutoCommit(false);
				this.removeIndexDbAction(conn, internalTableName, internalFieldName, caseInsensitiveIndex);
				conn.commit();
			} catch (SQLException sqlex) {
				logger.warn("Index removal failed: " + sqlex);
			} finally {
				if (conn != null) {
					conn.close();
				}
			}
		} catch (SQLException sqlex) {
			logger.error("Error getting SQL connection: " + sqlex);
		}
	}

	/**
	 * Given all the properties of a text field (content size, whether it uses a
	 * lookup, if it's unique etc.), set indexes appropriately
	 */
	private void addRemoveRelevantTextIndexes(Connection conn, TextField field) throws SQLException,
			CantDoThatException {
		String internalTableName = field.getTableContainingField().getInternalTableName();
		String internalFieldName = field.getInternalFieldName();
		this.removeIndexDbAction(conn, internalTableName, internalFieldName, true);
		this.removeIndexDbAction(conn, internalTableName, internalFieldName, false);
		this.removeUniqueDbAction(conn, internalTableName, internalFieldName);
		// Large text fields can't have any indexes
		// They can't be unique or use lookups either
		if (field.getContentSize() < TextContentSizes.FEW_PARAS.getNumChars()) {
			if (field.getUnique()) {
				this.addUniqueDbAction(conn, internalTableName, internalFieldName);
			} else {
				if (field.usesLookup()) {
					this.addIndexDbAction(conn, internalTableName, internalFieldName, false);
				} else {
					// Don't add case insensitive indexes until necessary
					// this.addIndexDbAction(conn, internalTableName, internalFieldName,
					// true);
				}
			}
		}
	}

	/**
	 * runs addUniqueDbAction, providing a database connection
	 */
	private void addUniqueWrapper(String internalTableName, String internalFieldName)
			throws CantDoThatException {
		try {
			Connection conn = this.relationalDataSource.getConnection();
			try {
				conn.setAutoCommit(false);
				this.addUniqueDbAction(conn, internalTableName, internalFieldName);
				conn.commit();
			} catch (SQLException sqlex) {
				logger.warn("Error adding unique constraint: " + sqlex);
			} finally {
				if (conn != null) {
					conn.close();
				}
			}
		} catch (SQLException sqlex) {
			logger.error("Error getting SQL connection: " + sqlex);
		}
	}

	/**
	 * runs removeUniqueDbAction, providing a database connection
	 */
	private void removeUniqueWrapper(String internalTableName, String internalFieldName) {
		try {
			Connection conn = this.relationalDataSource.getConnection();
			try {
				conn.setAutoCommit(false);
				this.removeUniqueDbAction(conn, internalTableName, internalFieldName);
				conn.commit();
			} catch (SQLException sqlex) {
				logger.warn("Unique index removal failed: " + sqlex);
			} finally {
				if (conn != null) {
					conn.close();
				}
			}
		} catch (SQLException sqlex) {
			logger.error("Error getting/closing SQL connection: " + sqlex);
		}
	}

	/**
	 * Lookup of internal table name to table
	 */
	private Map<String, TableInfo> tableCache = new ConcurrentHashMap<String, TableInfo>();

	private Map<String, TableInfo> reportTableCache = new ConcurrentHashMap<String, TableInfo>();

	/**
	 * Keep a cache of the datasource so it's available quickly whenever needed
	 */
	private DataSource relationalDataSource = null;

	private DataManagementInfo dataManagement = null;

	private AuthManagerInfo authManager = null;

	public static final String PRIMARY_KEY_DESCRIPTION = "Unique record identifier";

	private ScheduledExecutorService dashboardScheduler = null;

	private ScheduledFuture<?> scheduledDashboardPopulate = null;

	private Thread initialDashboardPopulatorThread = null;

	private static final SimpleLogger logger = new SimpleLogger(DatabaseDefn.class);
}