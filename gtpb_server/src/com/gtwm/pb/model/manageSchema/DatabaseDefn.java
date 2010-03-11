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
package com.gtwm.pb.model.manageSchema;

import javax.servlet.http.HttpServletRequest;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import com.gtwm.pb.auth.AuthManager;
import com.gtwm.pb.model.interfaces.AuthenticatorInfo;
import com.gtwm.pb.model.interfaces.CompanyInfo;
import com.gtwm.pb.model.interfaces.ModuleInfo;
import com.gtwm.pb.model.interfaces.AuthManagerInfo;
import com.gtwm.pb.model.interfaces.ReportSortInfo;
import com.gtwm.pb.model.interfaces.BaseFieldDescriptorOptionInfo;
import com.gtwm.pb.model.interfaces.TextFieldDescriptorOptionInfo;
import com.gtwm.pb.model.interfaces.BooleanFieldDescriptorOptionInfo;
import com.gtwm.pb.model.interfaces.ListFieldDescriptorOptionInfo;
import com.gtwm.pb.model.interfaces.ReportSummaryAggregateInfo;
import com.gtwm.pb.model.interfaces.ReportSummaryDataInfo;
import com.gtwm.pb.model.interfaces.SessionDataInfo;
import com.gtwm.pb.model.interfaces.DatabaseInfo;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.SimpleReportInfo;
import com.gtwm.pb.model.interfaces.ReportFieldInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.ReportFilterInfo;
import com.gtwm.pb.model.interfaces.ReportCalcFieldInfo;
import com.gtwm.pb.model.interfaces.ReportSummaryInfo;
import com.gtwm.pb.model.interfaces.ReportSummaryGroupingInfo;
import com.gtwm.pb.model.interfaces.DataManagementInfo;
import com.gtwm.pb.model.interfaces.JoinClauseInfo;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.fields.DateField;
import com.gtwm.pb.model.interfaces.fields.CheckboxField;
import com.gtwm.pb.model.interfaces.fields.RelationField;
import com.gtwm.pb.model.interfaces.fields.SequenceField;
import com.gtwm.pb.model.interfaces.fields.TextField;
import com.gtwm.pb.model.interfaces.fields.DecimalField;
import com.gtwm.pb.model.interfaces.fields.IntegerField;
import com.gtwm.pb.model.interfaces.fields.FileField;
import com.gtwm.pb.model.interfaces.fields.SeparatorField;
import com.gtwm.pb.model.interfaces.FieldTypeDescriptorInfo;
import com.gtwm.pb.model.interfaces.AppUserInfo;
import com.gtwm.pb.model.manageData.fields.DurationValueDefn;
import com.gtwm.pb.auth.DashboardPopulator;
import com.gtwm.pb.auth.DisallowedException;
import com.gtwm.pb.auth.PrivilegeType;
import com.gtwm.pb.model.manageData.DataManagement;
import com.gtwm.pb.servlets.ServletSchemaMethods;
import com.gtwm.pb.util.Enumerations.AppAction;
import com.gtwm.pb.util.Enumerations.HiddenFields;
import com.gtwm.pb.util.Enumerations.SummaryGroupingModifier;
import com.gtwm.pb.model.manageSchema.FieldTypeDescriptor.FieldCategory;
import com.gtwm.pb.model.manageSchema.BooleanFieldDescriptorOption.PossibleBooleanOptions;
import com.gtwm.pb.model.manageSchema.ListFieldDescriptorOption.PossibleListOptions;
import com.gtwm.pb.model.manageSchema.ListFieldDescriptorOption.TextContentSizes;
import com.gtwm.pb.model.manageSchema.TextFieldDescriptorOption.PossibleTextOptions;
import com.gtwm.pb.model.manageSchema.fields.CheckboxFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.DateFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.DecimalFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.DurationFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.IntegerFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.RelationFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.TextFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.SequenceFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.FileFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.SeparatorFieldDefn;
import com.gtwm.pb.model.manageUsage.UsageLogger;
import com.gtwm.pb.util.AppProperties;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.CodingErrorException;
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
import com.gtwm.pb.model.interfaces.WikiManagementInfo;
import org.grlea.log.SimpleLogger;
import org.hibernate.HibernateException;
import org.hibernate.Session;

public class DatabaseDefn implements DatabaseInfo {

	/**
	 * There should be one DatabaseInfo object per agileBase application
	 * instance. This constructor generates it. It bootstraps the application.
	 * All schema objects are loaded into memory from the pervasive store.
	 * 
	 * The authentication manager (AuthManagerInfo), store of all users, roles
	 * and permissions is loaded too.
	 * 
	 * Finally, the data manager (a DataManagementInfo object) is created and
	 * initialised
	 * 
	 * @throws CantDoThatException
	 *             If more than one Authenticator was found in the database
	 */
	public DatabaseDefn(DataSource relationalDataSource, String webAppRoot) throws SQLException,
			ObjectNotFoundException, CantDoThatException, MissingParametersException {
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
		final ScheduledExecutorService dashboardScheduler = Executors
				.newSingleThreadScheduledExecutor();
		this.scheduledDashboardPopulate = dashboardScheduler.scheduleAtFixedRate(
				dashboardPopulator, initialDelay, 24, TimeUnit.HOURS);
		// one-off boot actions
		// this.addLockableFields();
	}

	public void cancelScheduledEvents() {
		if (this.initialDashboardPopulatorThread != null) {
			this.initialDashboardPopulatorThread.interrupt();
		}
		if (this.scheduledDashboardPopulate != null) {
			this.scheduledDashboardPopulate.cancel(true);
		}
	}

	/**
	 * Was only used once but is an example of how to add a new type of hidden
	 * field so worth keeping around
	 */
	private void addLockableFields(Set<TableInfo> allTables) throws SQLException {
		for (TableInfo table : allTables) {
			String lockableFieldName = HiddenFields.LOCKED.getFieldName();
			try {
				BaseField lockingField = table.getField(lockableFieldName);
			} catch (ObjectNotFoundException onex) {
				logger.warn("Locking field doesn't exist for table " + table + ", adding it");
				Connection conn = null;
				try {
					HibernateUtil.startHibernateTransaction();
					conn = this.relationalDataSource.getConnection();
					conn.setAutoCommit(false);
					HibernateUtil.activateObject(table);
					this.addRecordLockedFieldToTable(conn, table);
					conn.commit();
					HibernateUtil.currentSession().getTransaction().commit();
				} catch (SQLException sqlex) {
					logger.error("SQL error adding locking field: " + sqlex);
					rollbackConnections(conn);
				} catch (HibernateException hex) {
					logger.error("Hibernate error adding locking field: " + hex);
					rollbackConnections(conn);
				} catch (AgileBaseException pbex) {
					logger.error("PB error adding locking field: " + pbex);
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

	private void addWikiFieldToTable(Connection conn, TableInfo table) throws CantDoThatException,
			SQLException, ObjectNotFoundException {
		TextField wikiPageField = new TextFieldDefn(this.relationalDataSource, table, null,
				HiddenFields.WIKI_PAGE.getFieldName(),
				HiddenFields.WIKI_PAGE.getFieldDescription(), TextFieldDefn.UNIQUE_FALSE,
				TextFieldDefn.NOT_NULL_FALSE, null, TextFieldDefn.NOT_APPLICABLE_FALSE, null, null,
				TextFieldDefn.HIDDEN_FALSE, true);
		HibernateUtil.currentSession().save(wikiPageField);
		table.addField(wikiPageField);
		this.addFieldToRelationalDb(conn, table, wikiPageField);
	}

	private void addDateCreatedFieldToTable(Connection conn, TableInfo table)
			throws CantDoThatException, SQLException, ObjectNotFoundException {
		DateField dateCreatedField = new DateFieldDefn(table, null, HiddenFields.DATE_CREATED
				.getFieldName(), HiddenFields.DATE_CREATED.getFieldDescription(),
				DateFieldDefn.UNIQUE_FALSE, DateFieldDefn.NOT_NULL_FALSE,
				DateFieldDefn.DEFAULT_TO_NOW_TRUE, Calendar.SECOND);
		dateCreatedField.setHidden(DateFieldDefn.HIDDEN_TRUE);
		HibernateUtil.currentSession().save(dateCreatedField);
		table.addField(dateCreatedField);
		this.addFieldToRelationalDb(conn, table, dateCreatedField);
		// Don't add the date created field to the default report
	}

	private void addCreatedByFieldToTable(Connection conn, TableInfo table)
			throws CantDoThatException, SQLException, ObjectNotFoundException {
		TextField createdByField = new TextFieldDefn(this.relationalDataSource, table, null,
				HiddenFields.CREATED_BY.getFieldName(), HiddenFields.CREATED_BY
						.getFieldDescription(), TextFieldDefn.UNIQUE_FALSE,
				TextFieldDefn.NOT_NULL_FALSE, null, TextFieldDefn.NOT_APPLICABLE_FALSE, null, null,
				TextFieldDefn.HIDDEN_TRUE, true);
		HibernateUtil.currentSession().save(createdByField);
		table.addField(createdByField);
		this.addFieldToRelationalDb(conn, table, createdByField);
		// Don't add the created by field to the default report
	}

	private void addLastModifiedFieldToTable(Connection conn, TableInfo table)
			throws CantDoThatException, SQLException, ObjectNotFoundException {
		DateField lastModifiedField = new DateFieldDefn(table, null, HiddenFields.LAST_MODIFIED
				.getFieldName(), HiddenFields.LAST_MODIFIED.getFieldDescription(),
				DateFieldDefn.UNIQUE_FALSE, DateFieldDefn.NOT_NULL_FALSE,
				DateFieldDefn.DEFAULT_TO_NOW_TRUE, Calendar.SECOND);
		lastModifiedField.setHidden(DateFieldDefn.HIDDEN_TRUE);
		HibernateUtil.currentSession().save(lastModifiedField);
		table.addField(lastModifiedField);
		this.addFieldToRelationalDb(conn, table, lastModifiedField);
		// Don't add the last modified field to the default report
	}

	private void addModifiedByFieldToTable(Connection conn, TableInfo table)
			throws CantDoThatException, SQLException, ObjectNotFoundException {
		TextField modifiedByField = new TextFieldDefn(this.relationalDataSource, table, null,
				HiddenFields.MODIFIED_BY.getFieldName(), HiddenFields.MODIFIED_BY
						.getFieldDescription(), TextFieldDefn.UNIQUE_FALSE,
				TextFieldDefn.NOT_NULL_FALSE, null, TextFieldDefn.NOT_APPLICABLE_FALSE, null, null,
				TextFieldDefn.HIDDEN_TRUE, true);
		HibernateUtil.currentSession().save(modifiedByField);
		table.addField(modifiedByField);
		this.addFieldToRelationalDb(conn, table, modifiedByField);
		// Don't add the modified by field to the default report
	}

	private void addRecordLockedFieldToTable(Connection conn, TableInfo table)
			throws CantDoThatException, SQLException, ObjectNotFoundException {
		CheckboxField recordLockedField = new CheckboxFieldDefn(table, null, HiddenFields.LOCKED
				.getFieldName(), HiddenFields.LOCKED.getFieldDescription(), false, true);
		HibernateUtil.currentSession().save(recordLockedField);
		table.addField(recordLockedField);
		this.addFieldToRelationalDb(conn, table, recordLockedField);
	}

	public synchronized TableInfo addTable(SessionDataInfo sessionData, HttpServletRequest request,
			Connection conn, String internalTableName, String internalDefaultReportName,
			String tableName, String internalPrimaryKeyName, String tableDesc) throws SQLException,
			DisallowedException, CantDoThatException, ObjectNotFoundException, CodingErrorException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.ADMINISTRATE))) {
			throw new DisallowedException(PrivilegeType.ADMINISTRATE);
		}
		TableInfo newTable = new TableDefn(internalTableName, tableName, tableDesc);
		HibernateUtil.currentSession().save(newTable);
		try {
			String SQLCode = "CREATE TABLE " + newTable.getInternalTableName() + " ()";
			PreparedStatement statement = conn.prepareStatement(SQLCode);
			statement.execute();
			statement.close();
			// Create an initial report for the table
			SimpleReportInfo defaultReport = new SimpleReportDefn(newTable,
					internalDefaultReportName, "Default report",
					"A simple report of all items in the '" + tableName + "' data store", null);
			// The true passed means it is the default report
			newTable.addReport(defaultReport, true);
			// Add an auto-generated primary key to act as a row identifier
			SequenceField primaryKeyField = new SequenceFieldDefn(newTable, internalPrimaryKeyName,
					"ID:" + tableName, PRIMARY_KEY_DESCRIPTION);
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
			this.addWikiFieldToTable(conn, newTable);
			this.addDateCreatedFieldToTable(conn, newTable);
			this.addCreatedByFieldToTable(conn, newTable);
			this.addLastModifiedFieldToTable(conn, newTable);
			this.addModifiedByFieldToTable(conn, newTable);
			this.addRecordLockedFieldToTable(conn, newTable);
		} catch (SQLException sqlex) {
			// Reformat the error message to be more user friendly.
			// Use SQLState as an error identifier because it is standard across
			// databases
			String errorCode = sqlex.getSQLState();
			if (errorCode.equals("42P07")) {
				// A table with that name already exists
				throw new SQLException("The internal table name '"
						+ newTable.getInternalTableName() + "' already exists", errorCode);
			} else if (errorCode.equals("42601")) {
				throw new SQLException("Table couldn't be created");
			} else {
				throw new SQLException(sqlex + ": error code " + errorCode);
			}
		}
		// Cache the table in the company object
		this.authManager.getCompanyForLoggedInUser(request).addTable(newTable);
		return newTable;
		// this.dataManagement.logLastSchemaChangeTime(request);
	}

	public synchronized void setDefaultTablePrivileges(HttpServletRequest request,
			TableInfo newTable) throws DisallowedException, CantDoThatException {
		// Set table privileges
		HibernateUtil.activateObject(this.authManager.getAuthenticator());
		// ...give the user who created the table all privileges on it
		try {
			AppUserInfo loggedInUser = this.authManager.getUserByUserName(request, request
					.getRemoteUser());
			this.authManager.addUserPrivilege(request, loggedInUser, PrivilegeType.MANAGE_TABLE,
					newTable);
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
			HttpServletRequest request) throws ObjectNotFoundException {
		CompanyInfo company = this.authManager.getCompanyForLoggedInUser(request);
		Set<TableInfo> tables = company.getTables();
		for (TableInfo table : tables) {
			// check relation fields in table itself:
			if (table.equals(baseTable)) {
				continue;
			}
			// only check for table dependency if table in
			// question is not the one to be deleted:
			if (table.isDependentOn(baseTable)) {
				if (!dependentTables.contains(table)) {
					dependentTables.add(table);
					getDependentTables(table, dependentTables, request);
				}
			}
		}
	}

	public void getDirectlyDependentTables(TableInfo baseTable, Set<TableInfo> dependentTables,
			HttpServletRequest request) throws ObjectNotFoundException {
		CompanyInfo company = this.authManager.getCompanyForLoggedInUser(request);
		Set<TableInfo> tables = company.getTables();
		for (TableInfo table : tables) {
			// check relation fields in table itself:
			if (table.equals(baseTable))
				continue;
			// only check for table dependency if table in
			// question is not the one to be deleted:
			if (table.isDependentOn(baseTable)) {
				if (!dependentTables.contains(table)) {
					dependentTables.add(table);
				}
			}
		}
	}

	public synchronized void updateTable(Connection conn, HttpServletRequest request,
			TableInfo table, String newTableName, String newTableDesc, Boolean lockable)
			throws DisallowedException, CantDoThatException, ObjectNotFoundException, SQLException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, table))) {
			throw new DisallowedException(PrivilegeType.MANAGE_TABLE, table);
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
					throw new CantDoThatException("A table called '" + newTableName
							+ "' already exists");
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
						+ table.getField(HiddenFields.LOCKED.getFieldName()).getInternalFieldName()
						+ " = true";
				PreparedStatement statement = conn.prepareStatement(SQLCode);
				statement.executeUpdate();
				statement.close();
			}
		}
	}

	public synchronized void removeTable(SessionDataInfo sessionData, HttpServletRequest request,
			TableInfo tableToRemove, Connection conn) throws SQLException, DisallowedException,
			CantDoThatException, TableDependencyException, CodingErrorException,
			ObjectNotFoundException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.ADMINISTRATE))) {
			throw new DisallowedException(PrivilegeType.ADMINISTRATE);
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
		this.getDependentTables(tableToRemove, dependentTables, request);
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
				throw new SQLException(sqlex + ": error code " + errorCode);
			}
		}
		this.authManager.getCompanyForLoggedInUser(request).removeTable(tableToRemove);
		UsageLogger usageLogger = new UsageLogger(this.relationalDataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logTableSchemaChange(user, tableToRemove, AppAction.REMOVE_TABLE, "");
		UsageLogger.startLoggingThread(usageLogger);
	}

	public synchronized BaseReportInfo addReport(SessionDataInfo sessionData,
			HttpServletRequest request, Connection conn, TableInfo table,
			String internalReportName, String reportName, String reportDesc, boolean populateReport)
			throws SQLException, DisallowedException, CantDoThatException, CodingErrorException,
			ObjectNotFoundException, ObjectNotFoundException, ObjectNotFoundException,
			MissingParametersException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, table))) {
			throw new DisallowedException(PrivilegeType.MANAGE_TABLE, table);
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
				if (!(field instanceof SeparatorField)) {
					if (field instanceof RelationField) {
						// add a join to allow related field to be added to the
						// report
						RelationField relationField = ((RelationField) field);
						TableInfo relatedTable = relationField.getRelatedTable();
						if (!relatedTables.contains(relatedTable)) {
							relatedTables.add(relatedTable);
							JoinClauseInfo join = ServletSchemaMethods.generateJoinObject(request,
									table.getInternalTableName(), "", field.getInternalFieldName(),
									JoinType.LEFT_OUTER, relatedTable.getInternalTableName(), "",
									relatedTable.getPrimaryKey().getInternalFieldName(), this);
							report.addJoin(join);
						}
						report.addTableField(relationField.getDisplayField());
					} else if (!(field.equals(table.getPrimaryKey()) || field.getHidden())) {
						report.addTableField(field);
					}
				}
			}
			if (report.getReportFields().size() > 1) {
				// the second field in the list should have a sort - first is
				// the primary key
				List<ReportFieldInfo> reportFieldList = new ArrayList<ReportFieldInfo>(report
						.getReportFields());
				report.addSort(reportFieldList.get(1), true);
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
	 * 
	 * NOTE: Uses some postgres specific code, not database independent
	 */
	private boolean viewExistsInPostgres(Connection conn, BaseReportInfo report)
			throws SQLException {
		PreparedStatement viewExistsStatement = conn
				.prepareStatement("SELECT count(*) FROM (SELECT relname FROM pg_catalog.pg_class WHERE relname=?) AS relname");
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
		try {
			savepoint = conn.setSavepoint("createOrReplaceSavepoint");
			PreparedStatement statement = conn.prepareStatement(SQLCode);
			statement.execute();
			statement.close();
		} catch (SQLException sqlex) {
			if (viewExists) {
				createOrReplaceWorked = false;
				conn.rollback(savepoint);
			} else {
				// if view didn't exist already, the error must be more serious
				// than just the CREATE OR REPLACE not working
				logger.error("Requested change to report " + report.getReportName()
						+ " would break view. Error = " + sqlex);
				logger.error("SQL = " + report.getSQLForDetail());
				throw new SQLException("The requested change would cause an error in the report: "
						+ sqlex.getMessage(), sqlex.getSQLState());
			}
		}
		return createOrReplaceWorked;
	}

	/**
	 * Updates the definition of a view within the DB. This method should only
	 * be used if updateViewDbActionWithCreateOrReplace fails. Drops the view
	 * and recreates it.
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
	 * Returns a Map whose keySet contains the names of the set of views
	 * dependent upon the report identified by internalReportName. The keySet
	 * also contains internalReportName. Each key maps to a list of view names
	 * identifying views directly dependent upon the view identified by the key.
	 * 
	 * NOTE: Uses some postgres specific code, not database independent
	 */
	private void fillReportDependencyMap(Connection conn, String internalReportName,
			Map<String, List<String>> reportDependencyMap) throws SQLException {
		String SQLCode = "SELECT viewname FROM pg_views WHERE schemaname='public' AND definition ILIKE '%"
				+ internalReportName + "%'";
		PreparedStatement statement = conn.prepareStatement(SQLCode);
		ResultSet results = statement.executeQuery();
		List<String> dependentReportInternalNames = new ArrayList<String>();
		// add the empty list for now so the key is present to prevent infinite
		// recursion
		reportDependencyMap.put(internalReportName, dependentReportInternalNames);
		while (results.next()) {
			String dependentReportInternalName = results.getString("viewname");
			dependentReportInternalNames.add(dependentReportInternalName);
			if (!reportDependencyMap.keySet().contains(dependentReportInternalName)) {
				fillReportDependencyMap(conn, dependentReportInternalName, reportDependencyMap);
			}
		}
		results.close();
		statement.close();
	}

	/**
	 * Updates the definition of a view within the DB. This method should only
	 * be used if updateViewDbActionWithDropAndCreate and
	 * updateViewDbActionWithCreateOrReplace fails. Drops any dependent views so
	 * that 'report' can be updated. Once report has been updated, all dependent
	 * views are recreated.
	 */
	private void updateViewDbActionWithDropAndCreateDependencies(Connection conn,
			BaseReportInfo report, HttpServletRequest request) throws SQLException,
			ObjectNotFoundException, CodingErrorException, CantDoThatException {
		Savepoint savepoint = null;
		try {
			savepoint = conn.setSavepoint("dropAndCreateDependenciesSavepoint");
			Map<String, List<String>> reportDependencyMap = new HashMap<String, List<String>>();
			this.fillReportDependencyMap(conn, report.getInternalReportName(), reportDependencyMap);
			// Remove reports...
			List<String> deletedReports = new ArrayList<String>();
			while (deletedReports.size() < reportDependencyMap.size()) {
				for (String reportInternalName : reportDependencyMap.keySet()) {
					if (!deletedReports.contains(reportInternalName)) {
						boolean cannotDelete = false;
						for (String dependentReportInternalName : reportDependencyMap
								.get(reportInternalName)) {
							if (!deletedReports.contains(dependentReportInternalName)) {
								cannotDelete = true;
							}
						}
						if (!cannotDelete) {
							PreparedStatement dropViewStatement = conn
									.prepareStatement("DROP VIEW " + reportInternalName);
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
				TableInfo table = this.findTableContainingReportWithoutChecks(reportInternalName,
						request);
				HibernateUtil.activateObject(table);
				BaseReportInfo reportToRecreate = table.getReport(reportInternalName);
				String CreateViewSQL = "CREATE VIEW " + reportInternalName + " AS ("
						+ reportToRecreate.getSQLForDetail() + ")";
				PreparedStatement statement = conn.prepareStatement(CreateViewSQL);
				statement.execute();
				statement.close();
			}
		} catch (SQLException sqlex) {
			conn.rollback(savepoint);
			throw new SQLException("The requested change would cause an error in the report: "
					+ sqlex.getMessage(), sqlex.getSQLState());
		}
	}

	/**
	 * Attempts to read the first 10 records from a given report, to check there
	 * isn't an error in the SQL
	 */
	private void throwExceptionIfDbViewIsBroken(Connection conn, BaseReportInfo report)
			throws SQLException, ObjectNotFoundException, CodingErrorException, CantDoThatException {
		try {
			String SQLCode = "SELECT * FROM " + report.getInternalReportName() + " LIMIT 10";
			PreparedStatement statement = conn.prepareStatement(SQLCode);
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
		} catch (SQLException sqlex) {
			// log the cause but return a more user friendly message
			logger.error("Requested change to report " + report.getReportName()
					+ " would break view. Error = " + sqlex);
			logger.error("SQL = " + report.getSQLForDetail());
			throw new SQLException("The requested change would cause an error in the report: "
					+ sqlex.getMessage(), sqlex.getSQLState());
		}
	}

	/**
	 * Create the database VIEW for the report
	 */
	private void updateViewDbAction(Connection conn, BaseReportInfo report,
			HttpServletRequest request) throws SQLException, CantDoThatException,
			CodingErrorException, ObjectNotFoundException {
		boolean viewExists = viewExistsInPostgres(conn, report);
		boolean createOrReplaceWorked = updateViewDbActionWithCreateOrReplace(conn, report,
				viewExists);
		if (viewExists && !createOrReplaceWorked) {
			boolean dropAndCreateWorked = updateViewDbActionWithDropAndCreate(conn, report);
			if (!dropAndCreateWorked) {
				updateViewDbActionWithDropAndCreateDependencies(conn, report, request);
			}
		}
		throwExceptionIfDbViewIsBroken(conn, report);
	}

	public void removeReport(SessionDataInfo sessionData, HttpServletRequest request,
			Connection conn, BaseReportInfo reportToRemove) throws SQLException,
			DisallowedException, CantDoThatException, CodingErrorException, ObjectNotFoundException {
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
		parentTable.removeReport(reportToRemove);
		this.removeReportWithoutChecks(sessionData, request, reportToRemove, conn);
		UsageLogger usageLogger = new UsageLogger(this.relationalDataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logReportSchemaChange(user, reportToRemove, AppAction.REMOVE_REPORT, "");
		UsageLogger.startLoggingThread(usageLogger);
	}

	/**
	 * By calling removeReport above, you can't remove the last or default
	 * report. However, sometimes we may need to internally remove the last
	 * report, for example if we're removing a table all reports need to be
	 * removed. This method is there for private use in these situations
	 */
	private synchronized void removeReportWithoutChecks(SessionDataInfo sessionData,
			HttpServletRequest request, BaseReportInfo reportToRemove, Connection conn)
			throws DisallowedException, SQLException, CodingErrorException, CantDoThatException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, reportToRemove.getParentTable()))) {
			throw new DisallowedException(PrivilegeType.MANAGE_TABLE, reportToRemove
					.getParentTable());
		}
		TableInfo parentTable = reportToRemove.getParentTable();
		HibernateUtil.activateObject(parentTable);
		String internalReportName = reportToRemove.getInternalReportName();
		try {
			// Drop database view
			// debug: IF EXISTS added temporarily(?) to help when dropping
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
						+ " because it doesn't exist in the data store");
			} else {
				throw new SQLException(sqlex + ": error code " + errorCode);
			}
		}
	}

	public void updateReport(Connection conn, HttpServletRequest request, BaseReportInfo report,
			String newReportName, String newReportDesc, ModuleInfo newModule)
			throws DisallowedException, CantDoThatException, SQLException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, report.getParentTable()))) {
			throw new DisallowedException(PrivilegeType.MANAGE_TABLE, report.getParentTable());
		}
		HibernateUtil.activateObject(report);
		if (newReportName != null) {
			if (!report.getReportName().equals(newReportName)) {
				for (BaseReportInfo testReport : report.getParentTable().getReports()) {
					if (testReport.getReportName().equals(newReportName)
							&& (!testReport.equals(report))) {
						throw new CantDoThatException("A report called " + newReportName
								+ " already exists in the "
								+ report.getParentTable().getTableName() + " table");
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
	}

	private BaseField generateFieldObject(HttpServletRequest request, TableInfo table,
			String fieldType, String internalFieldName, String fieldName, String fieldDesc,
			boolean unique, boolean hidden) throws CodingErrorException, CantDoThatException,
			ObjectNotFoundException {
		BaseField field = null;
		// No fields in agileBase are mandatory
		boolean notNull = false;
		// The 'not applicable' property is also not used currently
		boolean notApplicable = false;
		String notApplicableDescription = null;
		String notApplicableValue = null;
		FieldCategory fieldCategoryRequested = FieldCategory.valueOf(fieldType
				.toUpperCase(Locale.UK));
		fieldName = fieldName.trim();
		switch (fieldCategoryRequested) {
		case DATE:
			int dateResolution = Integer.valueOf(request
					.getParameter(PossibleListOptions.DATERESOLUTION.getFormInputName()));
			boolean defaultToNow = HttpRequestUtil.getBooleanValue(request,
					PossibleBooleanOptions.DEFAULTTONOW.getFormInputName());
			field = new DateFieldDefn(table, internalFieldName, fieldName, fieldDesc, unique,
					notNull, defaultToNow, dateResolution);
			break;
		case TEXT:
			int textContentSize = Integer.valueOf(request
					.getParameter(PossibleListOptions.TEXTCONTENTSIZE.getFormInputName()));
			String defaultValue = HttpRequestUtil.getStringValue(request,
					PossibleTextOptions.DEFAULTVALUE.getFormInputName());
			boolean usesLookup = HttpRequestUtil.getBooleanValue(request,
					PossibleBooleanOptions.USELOOKUP.getFormInputName());
			field = new TextFieldDefn(this.relationalDataSource, table, internalFieldName,
					fieldName, fieldDesc, unique, notNull, defaultValue, notApplicable,
					notApplicableDescription, notApplicableValue, usesLookup, false);
			((TextField) field).setContentSize(textContentSize);
			break;
		case NUMBER:
			int precision = HttpRequestUtil.getIntegerValue(request,
					PossibleListOptions.NUMBERPRECISION.getFormInputName(), 0);
			usesLookup = HttpRequestUtil.getBooleanValue(request, PossibleBooleanOptions.USELOOKUP
					.getFormInputName());
			if (precision > 0) {
				Double defaultNumber = HttpRequestUtil.getDoubleValueStrict(request,
						PossibleTextOptions.DEFAULTVALUE.getFormInputName(), null,
						"Default value must be a number");
				Double notApplicableNumber = 0.0d;
				field = new DecimalFieldDefn(this.relationalDataSource, table, internalFieldName,
						fieldName, fieldDesc, unique, notNull, defaultNumber, precision,
						notApplicable, notApplicableDescription, notApplicableNumber, usesLookup);
			} else {
				Integer defaultNumber = HttpRequestUtil.getIntegerValueStrict(request,
						PossibleTextOptions.DEFAULTVALUE.getFormInputName(), null,
						"Default value must be an integer");
				int notApplicableNumber = -1;
				field = new IntegerFieldDefn(this.relationalDataSource, table, internalFieldName,
						fieldName, fieldDesc, unique, defaultNumber, notNull, notApplicable,
						notApplicableDescription, notApplicableNumber, usesLookup);
			}
			break;
		case DURATION:
			Integer durationResolution = HttpRequestUtil.getIntegerValue(request,
					PossibleListOptions.DURATIONRESOLUTION.getFormInputName(), Calendar.MINUTE);
			Integer durationScale = HttpRequestUtil.getIntegerValue(request,
					PossibleListOptions.DURATIONSCALE.getFormInputName(), Calendar.HOUR_OF_DAY);
			field = new DurationFieldDefn(table, internalFieldName, fieldName, fieldDesc, notNull,
					durationResolution, new DurationValueDefn(0, 0, 0, 0, 0, 0), durationScale); // default
			// default hardcoded as 0 interval -- amend this
			break;
		case SEQUENCE:
			field = new SequenceFieldDefn(table, internalFieldName, fieldName, fieldDesc);
			break;
		case CHECKBOX:
			Boolean checkboxDefaultValue = HttpRequestUtil.getBooleanValue(request,
					PossibleListOptions.CHECKBOXDEFAULT.getFormInputName());
			field = new CheckboxFieldDefn(table, internalFieldName, fieldName, fieldDesc,
					checkboxDefaultValue, false);
			break;
		case FILE:
			field = new FileFieldDefn(table, internalFieldName, fieldName, fieldDesc);
			break;
		case SEPARATOR:
			field = new SeparatorFieldDefn(table, internalFieldName, fieldName, fieldDesc);
			break;
		default:
			throw new CantDoThatException("Adding unrecognised field type '" + fieldType + "'");
		}
		return field;
	}

	/*
	 * Create a field object of the correct type chosen by the user and add it
	 * to the table.<br> This is the top level function which will be called to
	 * add a field. The other addField function is called by this and handles
	 * the specifics of database and in-memory addition as well as adding the
	 * field to the table's default report.<br>
	 * 
	 * @see #addField(TableInfo, BaseField) addField(TableInfo, BaseField) does
	 * the specifics
	 */
	public BaseField addField(HttpServletRequest request, Connection conn, TableInfo table,
			String fieldType, String internalFieldName, String fieldName, String fieldDesc,
			boolean unique, boolean hidden) throws SQLException, ObjectNotFoundException,
			DisallowedException, CantDoThatException, CodingErrorException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, table))) {
			throw new DisallowedException(PrivilegeType.MANAGE_TABLE, table);
		}
		BaseField field = null;
		field = this.generateFieldObject(request, table, fieldType, internalFieldName, fieldName,
				fieldDesc, unique, hidden);
		this.addField(conn, table, field, request);
		field.setHidden(hidden);
		// schema change time not recorded in memory because it doesn't affect
		// summary reports
		// this.dataManagement.logLastSchemaChangeTime(request);
		UsageLogger usageLogger = new UsageLogger(this.relationalDataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logTableSchemaChange(user, table, AppAction.ADD_FIELD, "field name: "
				+ fieldName);
		UsageLogger.startLoggingThread(usageLogger);
		return field;
	}

	public void updateFieldOption(HttpServletRequest request, BaseField field)
			throws DisallowedException, CantDoThatException, CodingErrorException, SQLException {
		TableInfo table = field.getTableContainingField();
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, table))) {
			throw new DisallowedException(PrivilegeType.MANAGE_TABLE, table);
		}
		HibernateUtil.activateObject(field);
		if (field instanceof TextField) {
			TextField textField = (TextField) field;
			FieldTypeDescriptorInfo fieldDescriptor = textField.getFieldDescriptor();
			List<BaseFieldDescriptorOptionInfo> fieldOptions = fieldDescriptor.getOptions();
			for (BaseFieldDescriptorOptionInfo fieldOption : fieldOptions) {
				String formInputName = "updateoption" + field.getInternalFieldName()
						+ fieldOption.getFormInputName();
				String formInputValue = request.getParameter(formInputName);
				if (formInputValue != null) {
					if (fieldOption instanceof BooleanFieldDescriptorOptionInfo) {
						if (formInputName.equals("updateoption" + field.getInternalFieldName()
								+ PossibleBooleanOptions.USELOOKUP.getFormInputName())) {
							Boolean useLookup = Helpers.valueRepresentsBooleanTrue(formInputValue);
							textField.setUsesLookup(useLookup);
						} else if (formInputName.equals("updateoption"
								+ field.getInternalFieldName()
								+ PossibleBooleanOptions.UNIQUE.getFormInputName())) {
							Boolean unique = Helpers.valueRepresentsBooleanTrue(formInputValue);
							textField.setUnique(unique);
						}
					} // end of BooleanFieldDescriptorOptionInfo
					else if (fieldOption instanceof ListFieldDescriptorOptionInfo) {
						if (formInputName.equals("updateoption" + field.getInternalFieldName()
								+ PossibleListOptions.TEXTCONTENTSIZE.getFormInputName())) {
							int textContentSize = Integer.valueOf(formInputValue);
							textField.setContentSize(textContentSize);
						}
					} else if (fieldOption instanceof TextFieldDescriptorOptionInfo) {
						if (formInputName.equals("updateoption" + field.getInternalFieldName()
								+ PossibleTextOptions.DEFAULTVALUE.getFormInputName())) {
							if (formInputValue.equals("")) {
								textField.setDefault(null);
							} else {
								textField.setDefault(formInputValue);
							}
						}
					}
				}
			}
			Connection conn = null;
			try {
				conn = this.relationalDataSource.getConnection();
				conn.setAutoCommit(false);
				this.addRemoveRelevantTextIndexes(conn, textField);
				conn.commit();
			} catch (SQLException sqlex) {
				logger.error("Error setting relevant text indexes: " + sqlex);
			} finally {
				if (conn != null) {
					conn.close();
				}
			}
		} // end of TextField
		else if (field instanceof DateField) {
			DateField dateField = (DateField) field;
			FieldTypeDescriptorInfo fieldDescriptor = dateField.getFieldDescriptor();
			List<BaseFieldDescriptorOptionInfo> fieldOptions = fieldDescriptor.getOptions();
			for (BaseFieldDescriptorOptionInfo fieldOption : fieldOptions) {
				String formInputName = "updateoption" + field.getInternalFieldName()
						+ fieldOption.getFormInputName();
				String formInputValue = request.getParameter(formInputName);
				if (formInputValue != null) {
					if (fieldOption instanceof BooleanFieldDescriptorOptionInfo) {
						if (formInputName.equals("updateoption" + field.getInternalFieldName()
								+ PossibleBooleanOptions.DEFAULTTONOW.getFormInputName())) {
							Boolean defaultToNow = Helpers
									.valueRepresentsBooleanTrue(formInputValue);
							dateField.setDefaultToNow(defaultToNow);
						}
					} else if (fieldOption instanceof ListFieldDescriptorOptionInfo) {
						if (formInputName.equals("updateoption" + field.getInternalFieldName()
								+ PossibleListOptions.DATERESOLUTION.getFormInputName())) {
							int dateResolution = Integer.valueOf(formInputValue);
							dateField.setDateResolution(dateResolution);
						}
					}
				}
			}
		} else if (field instanceof DecimalField) {
			DecimalField decimalField = (DecimalField) field;
			FieldTypeDescriptorInfo fieldDescriptor = decimalField.getFieldDescriptor();
			List<BaseFieldDescriptorOptionInfo> fieldOptions = fieldDescriptor.getOptions();
			for (BaseFieldDescriptorOptionInfo fieldOption : fieldOptions) {
				String formInputName = "updateoption" + field.getInternalFieldName()
						+ fieldOption.getFormInputName();
				String formInputValue = request.getParameter(formInputName);
				if (formInputValue != null) {
					if (fieldOption instanceof ListFieldDescriptorOptionInfo) {
						if (formInputName.equals("updateoption" + field.getInternalFieldName()
								+ PossibleListOptions.NUMBERPRECISION.getFormInputName())) {
							int precision = Integer.valueOf(formInputValue);
							decimalField.setPrecision(precision);
						}
					} else if (fieldOption instanceof TextFieldDescriptorOptionInfo) {
						if (formInputName.equals("updateoption" + field.getInternalFieldName()
								+ PossibleTextOptions.DEFAULTVALUE.getFormInputName())) {
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
		} else if (field instanceof IntegerFieldDefn) {
			IntegerFieldDefn integerField = (IntegerFieldDefn) field;
			FieldTypeDescriptorInfo fieldDescriptor = integerField.getFieldDescriptor();
			List<BaseFieldDescriptorOptionInfo> fieldOptions = fieldDescriptor.getOptions();
			for (BaseFieldDescriptorOptionInfo fieldOption : fieldOptions) {
				String formInputName = "updateoption" + field.getInternalFieldName()
						+ fieldOption.getFormInputName();
				String formInputValue = request.getParameter(formInputName);
				if (formInputValue != null) {
					if (fieldOption instanceof BooleanFieldDescriptorOptionInfo) {
						if (formInputName.equals("updateoption" + field.getInternalFieldName()
								+ PossibleBooleanOptions.UNIQUE.getFormInputName())) {
							Boolean unique = Helpers.valueRepresentsBooleanTrue(formInputValue);
							integerField.setUnique(unique);
							if (unique) {
								// remove old index (if there is one) before
								// adding the unique index
								this.removeIndexWrapper(table.getInternalTableName(), integerField
										.getInternalFieldName(), false);
								this.addUniqueWrapper(table.getInternalTableName(), integerField
										.getInternalFieldName());
							} else {
								// remove the unique index
								this.removeUniqueWrapper(table.getInternalTableName(), integerField
										.getInternalFieldName());
							}
						}
					} else if (fieldOption instanceof TextFieldDescriptorOptionInfo) {
						if (formInputName.equals("updateoption" + field.getInternalFieldName()
								+ PossibleTextOptions.DEFAULTVALUE.getFormInputName())) {
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
	}

	public void updateField(HttpServletRequest request, BaseField field, String fieldName,
			String fieldDesc) throws DisallowedException {
		TableInfo table = field.getTableContainingField();
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, table))) {
			throw new DisallowedException(PrivilegeType.MANAGE_TABLE, table);
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

	public synchronized void setFieldIndex(TableInfo table, BaseField field, int newindex)
			throws ObjectNotFoundException, CantDoThatException {
		HibernateUtil.activateObject(table);
		table.setFieldIndex(newindex, field);
	}

	/**
	 * Adds field to relational database, object database and memory.
	 */
	private synchronized void addField(Connection conn, TableInfo tableToAddTo,
			BaseField fieldToAdd, HttpServletRequest request) throws SQLException,
			CantDoThatException, ObjectNotFoundException, CodingErrorException {
		HibernateUtil.activateObject(tableToAddTo);
		HibernateUtil.currentSession().save(fieldToAdd);
		tableToAddTo.addField(fieldToAdd);
		if (!(fieldToAdd instanceof SeparatorField)) {
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
	 *             If an internal coding bug exists caused, specifically a field
	 *             type passed is a VARCHAR field but the type is not recognised
	 *             as a text field
	 * 
	 * @see #addFieldDbAction(Connection, String, String, String, boolean)
	 * @see #addForeignKeyDbAction(Connection, String, RelationField)
	 * @see #addIndexDbAction(Connection, String, String)
	 */
	private void addFieldToRelationalDb(Connection conn, TableInfo tableToAddTo,
			BaseField fieldToAdd) throws CantDoThatException, SQLException, ObjectNotFoundException {
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
			this.addFieldDbAction(conn, internalTableName, internalFieldName, fieldToAdd
					.getFieldName(), dbType, setUnique);
			if (fieldToAdd.hasDefault()) {
				this.setFieldDefaultDbAction(conn, fieldToAdd);
			}
			if (setNotNull) {
				this.setFieldNotNullDbAction(conn, internalTableName, internalFieldName);
			}
			// Add index only if not unique (will already have index) and for
			// field types where indexing makes sense.
			// Currently only small text fields and relations
			if (!setUnique) {
				if (fieldToAdd instanceof TextField) {
					this.addRemoveRelevantTextIndexes(conn, (TextField) fieldToAdd);
				} else if (fieldToAdd instanceof RelationField) {
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
	 * Update all the existing field values in the database with the default
	 * value for that field
	 */
	private void setFieldDefaultDbAction(Connection conn, BaseField field) throws SQLException,
			CantDoThatException, ObjectNotFoundException {
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

	private void setFieldNotNullDbAction(Connection conn, String internalTableName,
			String internalFieldName) throws SQLException {
		String SQLCode = "ALTER TABLE " + internalTableName;
		SQLCode += " ALTER COLUMN " + internalFieldName + " SET NOT NULL";
		PreparedStatement statement = conn.prepareStatement(SQLCode);
		statement.execute();
		statement.close();
	}

	/*
	 * The top level function to call to add a database foreign key relation
	 * 
	 * @see addField(String, String, String, String, Map<String, String>)
	 * Equivalent to addField but for relation fields, not normal fields
	 */
	public synchronized RelationField addRelation(HttpServletRequest request, Connection conn,
			TableInfo tableToAddTo, String internalFieldName, String fieldName, String fieldDesc,
			TableInfo relatedTable, BaseField relatedField) throws SQLException,
			DisallowedException, CantDoThatException, ObjectNotFoundException, CodingErrorException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, tableToAddTo))) {
			throw new DisallowedException(PrivilegeType.MANAGE_TABLE, tableToAddTo);
		}
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.VIEW_TABLE_DATA, relatedTable))) {
			throw new DisallowedException(PrivilegeType.VIEW_TABLE_DATA, relatedTable);
		}
		// No fields are mandatory in agileBase
		boolean notNull = false;
		String listValueFieldInternalName = request.getParameter(PossibleListOptions.LISTVALUEFIELD
				.getFormInputName());
		// Create the relation object
		RelationField relationToAdd = new RelationFieldDefn(this.relationalDataSource,
				tableToAddTo, internalFieldName, relatedTable, relatedField, notNull);
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
	 *             Thrown if the field shouldn't be removed from it's parent
	 *             table, with a message explaining why not
	 */
	private synchronized void removeFieldChecks(BaseField field, HttpServletRequest request)
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
						String calcSQL = ((ReportCalcFieldInfo) reportField)
								.getCalculationSQL(true);
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
				ReportSummaryInfo reportSummary = testReport.getReportSummary();
				for (ReportFieldInfo summaryGroupingReportField : reportSummary
						.getGroupingReportFields()) {
					BaseField groupingBaseField = summaryGroupingReportField.getBaseField();
					if (groupingBaseField.equals(field)) {
						reportsUsedIn.add(testReport);
					}
				}
				for (ReportSummaryAggregateInfo summaryAggregate : reportSummary
						.getAggregateFunctions()) {
					BaseField aggregateBaseField = summaryAggregate.getReportField().getBaseField();
					if (aggregateBaseField.equals(field)) {
						reportsUsedIn.add(testReport);
					}
					ReportFieldInfo secondaryAggregateField = summaryAggregate
							.getSecondaryReportField();
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

	public synchronized void removeField(HttpServletRequest request, Connection conn,
			BaseField field) throws SQLException, DisallowedException, CantDoThatException,
			CodingErrorException, ObjectNotFoundException {
		TableInfo table = field.getTableContainingField();
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, table))) {
			throw new DisallowedException(PrivilegeType.MANAGE_TABLE, table);
		}
		this.removeFieldChecks(field, request);
		// Don't allow deletion of the primary key
		if (field.equals(field.getTableContainingField().getPrimaryKey())) {
			throw new CantDoThatException("Can't delete the primary key field");
		}
		ReportFieldInfo removedReportField = null;
		HibernateUtil.activateObject(table);
		// remove from default report
		SimpleReportInfo defaultReport = table.getDefaultReport();
		Set<ReportFieldInfo> reportFields = defaultReport.getReportFields();
		for (ReportFieldInfo reportField : reportFields) {
			if (!reportField.isFieldFromReport()) {
				if (reportField.getBaseField().equals(field)) {
					logger.info("Removing " + reportField + " from report " + defaultReport);
					defaultReport.removeField(reportField);
					removedReportField = reportField;
					updateViewDbAction(conn, defaultReport, request);
				}
			}
		}
		table.removeField(field);
		if (!(field instanceof SeparatorField)) {
			// Now try to remove the field from the table:
			PreparedStatement statement = conn
					.prepareStatement("ALTER TABLE " + table.getInternalTableName()
							+ " DROP COLUMN " + field.getInternalFieldName());
			statement.execute();
			statement.close();
		}
		// Persist change
		if (removedReportField != null) {
			HibernateUtil.currentSession().delete(removedReportField);
		}
		HibernateUtil.currentSession().delete(field);
		UsageLogger usageLogger = new UsageLogger(this.relationalDataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logTableSchemaChange(user, table, AppAction.REMOVE_FIELD, "field: " + field);
		UsageLogger.startLoggingThread(usageLogger);
	}

	public synchronized ReportFieldInfo addFieldToReport(HttpServletRequest request,
			Connection conn, SimpleReportInfo report, SimpleReportInfo sourceReport, BaseField field)
			throws SQLException, DisallowedException, InconsistentStateException,
			CantDoThatException, CodingErrorException, ObjectNotFoundException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, report.getParentTable()))) {
			throw new DisallowedException(PrivilegeType.MANAGE_TABLE, report.getParentTable());
		}
		ReportFieldInfo newReportField = null;
		HibernateUtil.activateObject(report);
		if (sourceReport == null) {
			newReportField = report.addTableField(field);
		} else {
			ReportFieldInfo reportField = sourceReport.getReportField(field.getInternalFieldName());
			newReportField = report.addReportField(reportField);
		}
		updateViewDbAction(conn, report, request);
		// this.dataManagement.logLastSchemaChangeTime(request);
		UsageLogger usageLogger = new UsageLogger(this.relationalDataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logReportSchemaChange(user, report, AppAction.ADD_FIELD_TO_REPORT, "field: "
				+ field);
		UsageLogger.startLoggingThread(usageLogger);
		return newReportField;
	}

	public synchronized void setReportFieldIndex(Connection conn, SimpleReportInfo report,
			ReportFieldInfo field, int newindex, HttpServletRequest request) throws SQLException,
			CodingErrorException, ObjectNotFoundException, CantDoThatException {
		HibernateUtil.activateObject(report);
		report.setFieldIndex(newindex, field);
		this.updateViewDbAction(conn, report, request);
	}

	public synchronized void addJoinToReport(HttpServletRequest request, Connection conn,
			SimpleReportInfo report, JoinClauseInfo join) throws DisallowedException, SQLException,
			CantDoThatException, CodingErrorException, ObjectNotFoundException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, report.getParentTable()))) {
			throw new DisallowedException(PrivilegeType.MANAGE_TABLE, report.getParentTable());
		}
		HibernateUtil.activateObject(report);
		report.addJoin(join);
		this.updateViewDbAction(conn, report, request);
		this.dataManagement.logLastSchemaChangeTime(request);
		UsageLogger usageLogger = new UsageLogger(this.relationalDataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logReportSchemaChange(user, report, AppAction.ADD_JOIN_TO_REPORT, "join: "
				+ join);
		UsageLogger.startLoggingThread(usageLogger);
	}

	public synchronized void removeJoinFromReport(HttpServletRequest request, Connection conn,
			SimpleReportInfo report, JoinClauseInfo join) throws DisallowedException, SQLException,
			CantDoThatException, CodingErrorException, ObjectNotFoundException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, report.getParentTable()))) {
			throw new DisallowedException(PrivilegeType.MANAGE_TABLE, report.getParentTable());
		}
		HibernateUtil.activateObject(report);
		report.removeJoin(join);
		updateViewDbAction(conn, report, request);
		HibernateUtil.currentSession().delete(join);
		this.dataManagement.logLastSchemaChangeTime(request);
		UsageLogger usageLogger = new UsageLogger(this.relationalDataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logReportSchemaChange(user, report, AppAction.REMOVE_JOIN_FROM_REPORT, "join: "
				+ join);
		UsageLogger.startLoggingThread(usageLogger);
	}

	public synchronized void addSortToReport(HttpServletRequest request, Connection conn,
			SimpleReportInfo report, ReportFieldInfo reportField, boolean ascending)
			throws DisallowedException, CantDoThatException, SQLException, CodingErrorException,
			ObjectNotFoundException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, report.getParentTable()))) {
			throw new DisallowedException(PrivilegeType.MANAGE_TABLE, report.getParentTable());
		}
		HibernateUtil.activateObject(report);
		report.addSort(reportField, ascending);
		updateViewDbAction(conn, report, request);
		// this.dataManagement.logLastSchemaChangeTime(request);
	}

	public synchronized void updateSortFromReport(HttpServletRequest request, Connection conn,
			SimpleReportInfo report, ReportFieldInfo reportField, boolean ascending)
			throws DisallowedException, CantDoThatException, SQLException, CodingErrorException,
			ObjectNotFoundException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, report.getParentTable()))) {
			throw new DisallowedException(PrivilegeType.MANAGE_TABLE, report.getParentTable());
		}
		HibernateUtil.activateObject(report);
		report.updateSort(reportField, ascending);
		updateViewDbAction(conn, report, request);
		// this.dataManagement.logLastSchemaChangeTime(request);
	}

	public synchronized void removeSortFromReport(HttpServletRequest request, Connection conn,
			SimpleReportInfo report, ReportFieldInfo reportField) throws DisallowedException,
			CantDoThatException, SQLException, CodingErrorException, ObjectNotFoundException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, report.getParentTable()))) {
			throw new DisallowedException(PrivilegeType.MANAGE_TABLE, report.getParentTable());
		}
		HibernateUtil.activateObject(report);
		ReportSortInfo removedSort = report.removeSort(reportField);
		updateViewDbAction(conn, report, request);
		if (removedSort != null) {
			HibernateUtil.currentSession().delete(removedSort);
		}
		// this.dataManagement.logLastSchemaChangeTime(request);
	}

	public synchronized void addFilterToReport(HttpServletRequest request, Connection conn,
			SimpleReportInfo report, ReportFilterInfo filter) throws SQLException,
			DisallowedException, CantDoThatException, CodingErrorException, ObjectNotFoundException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, report.getParentTable()))) {
			throw new DisallowedException(PrivilegeType.MANAGE_TABLE, report.getParentTable());
		}
		HibernateUtil.activateObject(report);
		report.addFilter(filter);
		updateViewDbAction(conn, report, request);
		this.dataManagement.logLastSchemaChangeTime(request);
		UsageLogger usageLogger = new UsageLogger(this.relationalDataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logReportSchemaChange(user, report, AppAction.ADD_FILTER_TO_REPORT, "filter: "
				+ filter);
		UsageLogger.startLoggingThread(usageLogger);
	}
	
	public synchronized ModuleInfo addModule(HttpServletRequest request) throws ObjectNotFoundException, DisallowedException {
		CompanyInfo company = this.authManager.getCompanyForLoggedInUser(request);
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
		ModuleInfo newModule = new Module(moduleName, "", indexNumber);
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
			if (authenticator.loggedInUserAllowedTo(request, PrivilegeType.VIEW_TABLE_DATA,
					testTable)) {
				SortedSet<BaseReportInfo> allTableReports = testTable.getReports();
				// Strip down to the set of reports the user has privileges to
				// view
				SortedSet<BaseReportInfo> viewableReports = new TreeSet<BaseReportInfo>();
				for (BaseReportInfo report : allTableReports) {
					if (authenticator.loggedInUserAllowedToViewReport(request, report)) {
						viewableReports.add(report);
					}
				}
				availableDataStores.put(testTable, Collections
						.unmodifiableSortedSet(viewableReports));
			}
		}
		return Collections.unmodifiableMap(availableDataStores);
	}

	public synchronized void removeFilterFromReport(HttpServletRequest request, Connection conn,
			SimpleReportInfo report, ReportFilterInfo filter) throws DisallowedException,
			ObjectNotFoundException, CantDoThatException, SQLException, CodingErrorException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, report.getParentTable()))) {
			throw new DisallowedException(PrivilegeType.MANAGE_TABLE, report.getParentTable());
		}
		HibernateUtil.activateObject(report);
		report.removeFilter(filter);
		updateViewDbAction(conn, report, request);
		HibernateUtil.currentSession().delete(filter);
		this.dataManagement.logLastSchemaChangeTime(request);
		UsageLogger usageLogger = new UsageLogger(this.relationalDataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logReportSchemaChange(user, report, AppAction.REMOVE_FILTER_FROM_REPORT,
				"filter: " + filter);
		UsageLogger.startLoggingThread(usageLogger);
	}

	/**
	 * Return a set of all reports that join to this one, i.e. those that would
	 * have to be modified before dropping the given report
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
							BaseReportInfo joinedReport = join.getLeftReportField()
									.getParentReport();
							if (joinedReport.equals(report)) {
								reportsUsedIn.add(testReport);
							}
						}
						if (!join.isRightPartTable()) {
							BaseReportInfo joinedReport = join.getRightReportField()
									.getParentReport();
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

	public synchronized void addCalculationToReport(HttpServletRequest request, Connection conn,
			SimpleReportInfo report, ReportCalcFieldInfo calculationField) throws SQLException,
			DisallowedException, InconsistentStateException, CantDoThatException,
			CodingErrorException, ObjectNotFoundException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, report.getParentTable()))) {
			throw new DisallowedException(PrivilegeType.MANAGE_TABLE, report.getParentTable());
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
				updateViewDbAction(conn, report, request);
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

	public synchronized void updateCalculationInReport(HttpServletRequest request, Connection conn,
			SimpleReportInfo report, ReportCalcFieldInfo calculationField, String calculationName,
			String calculationDefn, DatabaseFieldType dbFieldType) throws DisallowedException,
			SQLException, ObjectNotFoundException, CantDoThatException, CodingErrorException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, report.getParentTable()))) {
			throw new DisallowedException(PrivilegeType.MANAGE_TABLE, report.getParentTable());
		}
		HibernateUtil.activateObject(report);
		Savepoint savepoint = null;
		if (!(calculationDefn.toLowerCase().equals(calculationField.getCalculationDefinition()))
				|| !(dbFieldType.equals(calculationField.getDbType()))) {
			Map<TableInfo, Set<BaseReportInfo>> availableDataStores = this
					.getViewableDataStores(request);
			((ReportCalcFieldDefn) calculationField).updateCalculationDefinition(calculationDefn,
					dbFieldType, availableDataStores);
		} else {
			((ReportCalcFieldDefn) calculationField).setBaseFieldName(calculationName);
		}
		savepoint = conn.setSavepoint("updateCalculationSavepoint");
		try {
			this.updateViewDbAction(conn, report, request);
		} catch (SQLException sqlex) {
			// detect aggregate functions
			if (sqlex.getMessage().contains("must appear in the GROUP BY clause")
					|| sqlex.getMessage().contains("aggregates not allowed in GROUP BY clause")) {
				conn.rollback(savepoint);
				calculationField.setAggregateFunction(true);
				updateViewDbAction(conn, report, request);
			} else {
				throw sqlex;
			}
		}
		UsageLogger usageLogger = new UsageLogger(this.relationalDataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logReportSchemaChange(user, report, AppAction.UPDATE_CALCULATION_IN_REPORT,
				"calculation name: " + calculationField.getFieldName());
		UsageLogger.startLoggingThread(usageLogger);
	}

	public synchronized void returnCalculationInReportToMemory(HttpServletRequest request,
			Connection conn, SimpleReportInfo report, ReportCalcFieldInfo calculationField,
			String oldCalculationName, String oldCalculationDefn, DatabaseFieldType oldDbFieldType)
			throws DisallowedException, CodingErrorException, CantDoThatException,
			ObjectNotFoundException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, report.getParentTable()))) {
			throw new DisallowedException(PrivilegeType.MANAGE_TABLE, report.getParentTable());
		}
		Map<TableInfo, Set<BaseReportInfo>> availableDataStores = this
				.getViewableDataStores(request);
		((ReportCalcFieldDefn) calculationField).updateCalculationDefinition(oldCalculationDefn,
				oldDbFieldType, availableDataStores);
		((ReportCalcFieldDefn) calculationField).setBaseFieldName(oldCalculationName);
	}

	private synchronized void removeFieldFromReportChecks(ReportFieldInfo reportField,
			HttpServletRequest request) throws CantDoThatException, CodingErrorException,
			ObjectNotFoundException {
		// check the field isn't used in its parent report's own summary
		ReportSummaryInfo reportSummary = reportField.getParentReport().getReportSummary();
		Set<ReportSummaryAggregateInfo> aggFns = reportSummary.getAggregateFunctions();
		for (ReportSummaryAggregateInfo aggFn : aggFns) {
			ReportFieldInfo aggReportField = aggFn.getReportField();
			if (aggReportField.equals(reportField)) {
				throw new CantDoThatException("Please remove the report summary calculation "
						+ aggFn + " before removing the report field");
			}
			ReportFieldInfo secondaryAggReportField = aggFn.getSecondaryReportField();
			if (secondaryAggReportField != null) {
				if (secondaryAggReportField.equals(reportField)) {
					throw new CantDoThatException("Please remove the report summary calculation "
							+ aggFn + " before removing the report field");
				}
			}
		}
		for (ReportFieldInfo groupingReportField : reportSummary.getGroupingReportFields()) {
			if (groupingReportField.equals(reportField)) {
				throw new CantDoThatException("Please remove the report summary grouping on "
						+ reportField + " before removing the report field");
			}
		}
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
								&& testReportField.getBaseField()
										.equals(reportField.getBaseField())) {
							if (!((reportField instanceof ReportCalcFieldInfo) && (reportField
									.equals(testReportField)))) {
								reportsUsedIn.add(testReportField.getParentReport());
							}
						}
					}
					if (testReportField instanceof ReportCalcFieldInfo) {
						String calcSQL = ((ReportCalcFieldInfo) testReportField)
								.getCalculationSQL(true);
						if (calcSQL.contains(reportField.getParentReport().getInternalReportName()
								+ "." + reportField.getInternalFieldName())) {
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

	public synchronized void removeFieldFromReport(HttpServletRequest request, Connection conn,
			ReportFieldInfo reportField) throws SQLException, DisallowedException,
			CantDoThatException, CodingErrorException, ObjectNotFoundException {
		SimpleReportInfo report = (SimpleReportInfo) reportField.getParentReport();
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, report.getParentTable()))) {
			throw new DisallowedException(PrivilegeType.MANAGE_TABLE, report.getParentTable());
		}
		HibernateUtil.activateObject(report);
		this.removeFieldFromReportChecks(reportField, request);
		report.removeField(reportField);
		updateViewDbAction(conn, report, request);
		HibernateUtil.currentSession().delete(reportField);
		UsageLogger usageLogger = new UsageLogger(this.relationalDataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logReportSchemaChange(user, report, AppAction.REMOVE_FIELD_FROM_REPORT,
				"field: " + reportField);
		UsageLogger.startLoggingThread(usageLogger);
	}

	public synchronized void addGroupingToSummaryReport(HttpServletRequest request,
			ReportFieldInfo groupingReportField, SummaryGroupingModifier groupingModifer)
			throws DisallowedException, CantDoThatException, ObjectNotFoundException, SQLException {
		HibernateUtil.activateObject(groupingReportField);
		BaseReportInfo report = groupingReportField.getParentReport();
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, report.getParentTable()))) {
			throw new DisallowedException(PrivilegeType.MANAGE_TABLE, report.getParentTable());
		}
		ReportSummaryInfo reportSummary = report.getReportSummary();
		HibernateUtil.activateObject(reportSummary);
		reportSummary.addGrouping(groupingReportField, groupingModifer);
		this.dataManagement.logLastSchemaChangeTime(request);
		// Test change by selecting rows from the database
		CompanyInfo company = this.getAuthManager().getCompanyForLoggedInUser(request);
		Map<BaseField, String> blankFilterValues = new HashMap<BaseField, String>();
		ReportSummaryDataInfo reportSummaryData = this.getDataManagement().getReportSummaryData(
				company, report.getReportSummary(), blankFilterValues, false);
		UsageLogger usageLogger = new UsageLogger(this.relationalDataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logReportSchemaChange(user, report, AppAction.ADD_GROUPING_TO_SUMMARY_REPORT,
				"field: " + groupingReportField);
		UsageLogger.startLoggingThread(usageLogger);
	}

	public synchronized void removeGroupingFromSummaryReport(HttpServletRequest request,
			ReportFieldInfo groupingReportField) throws DisallowedException,
			ObjectNotFoundException, SQLException, CantDoThatException {
		BaseReportInfo report = groupingReportField.getParentReport();
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, report.getParentTable()))) {
			throw new DisallowedException(PrivilegeType.MANAGE_TABLE, report.getParentTable());
		}
		ReportSummaryInfo reportSummary = report.getReportSummary();
		HibernateUtil.activateObject(reportSummary);
		ReportSummaryGroupingInfo removedGrouping = reportSummary
				.removeGrouping(groupingReportField);
		this.dataManagement.logLastSchemaChangeTime(request);
		// Test change by selecting rows from the database
		CompanyInfo company = this.getAuthManager().getCompanyForLoggedInUser(request);
		Map<BaseField, String> blankFilterValues = new HashMap<BaseField, String>();
		ReportSummaryDataInfo reportSummaryData = this.getDataManagement().getReportSummaryData(
				company, report.getReportSummary(), blankFilterValues, false);
		UsageLogger usageLogger = new UsageLogger(this.relationalDataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logReportSchemaChange(user, report,
				AppAction.REMOVE_GROUPING_FROM_SUMMARY_REPORT, "field: " + groupingReportField);
		UsageLogger.startLoggingThread(usageLogger);
	}

	public synchronized void addFunctionToSummaryReport(HttpServletRequest request,
			ReportSummaryAggregateInfo addedAggFn) throws DisallowedException, CantDoThatException,
			ObjectNotFoundException, SQLException {
		BaseReportInfo report = addedAggFn.getReportField().getParentReport();
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, report.getParentTable()))) {
			throw new DisallowedException(PrivilegeType.MANAGE_TABLE, report.getParentTable());
		}
		ReportSummaryInfo reportSummary = report.getReportSummary();
		HibernateUtil.activateObject(reportSummary);
		reportSummary.addFunction(addedAggFn);
		this.dataManagement.logLastSchemaChangeTime(request);
		UsageLogger usageLogger = new UsageLogger(this.relationalDataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logReportSchemaChange(user, report, AppAction.ADD_FUNCTION_TO_SUMMARY_REPORT,
				"function: " + addedAggFn);
		UsageLogger.startLoggingThread(usageLogger);
		// Test change by selecting rows from the database
		CompanyInfo company = this.getAuthManager().getCompanyForLoggedInUser(request);
		Map<BaseField, String> blankFilterValues = new HashMap<BaseField, String>();
		ReportSummaryDataInfo reportSummaryData = this.getDataManagement().getReportSummaryData(
				company, report.getReportSummary(), blankFilterValues, false);
	}

	public synchronized void removeFunctionFromSummaryReport(HttpServletRequest request,
			BaseReportInfo report, String internalAggregateName) throws DisallowedException,
			CantDoThatException, ObjectNotFoundException, SQLException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, report.getParentTable()))) {
			throw new DisallowedException(PrivilegeType.MANAGE_TABLE, report.getParentTable());
		}
		ReportSummaryInfo reportSummary = report.getReportSummary();
		HibernateUtil.activateObject(reportSummary);
		ReportSummaryAggregateInfo removedFunction = reportSummary
				.removeFunction(internalAggregateName);
		this.dataManagement.logLastSchemaChangeTime(request);
		// Test change by selecting rows from the database
		CompanyInfo company = this.getAuthManager().getCompanyForLoggedInUser(request);
		Map<BaseField, String> blankFilterValues = new HashMap<BaseField, String>();
		ReportSummaryDataInfo reportSummaryData = this.getDataManagement().getReportSummaryData(
				company, report.getReportSummary(), blankFilterValues, false);
		HibernateUtil.currentSession().delete(removedFunction);
		UsageLogger usageLogger = new UsageLogger(this.relationalDataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logReportSchemaChange(user, report,
				AppAction.REMOVE_FUNCTION_FROM_SUMMARY_REPORT, "function: " + removedFunction);
		UsageLogger.startLoggingThread(usageLogger);
	}

	public synchronized void saveSummaryReport(HttpServletRequest request, BaseReportInfo report,
			String summaryTitle) throws DisallowedException, CantDoThatException,
			ObjectNotFoundException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, report.getParentTable()))) {
			throw new DisallowedException(PrivilegeType.MANAGE_TABLE, report.getParentTable());
		}
		ReportSummaryInfo templateSummary = report.getReportSummary();
		Set<ReportSummaryAggregateInfo> aggregates = templateSummary.getAggregateFunctions();
		Set<ReportSummaryGroupingInfo> groupings = templateSummary.getGroupings();
		if (aggregates.size() == 0) {
			throw new CantDoThatException(
					"To save a report summary, it must contain one or more functions");
		}
		HibernateUtil.activateObject(templateSummary);
		ReportSummaryInfo savedSummary = new ReportSummaryDefn(report, summaryTitle);
		HibernateUtil.currentSession().save(savedSummary);
		// Move aggregates from template summary to new summary
		for (ReportSummaryAggregateInfo aggregate : aggregates) {
			savedSummary.addFunction(aggregate);
			ReportSummaryAggregateInfo removedFunction = templateSummary.removeFunction(aggregate
					.getInternalAggregateName());
		}
		// Move groupings from template summary to new summary
		for (ReportSummaryGroupingInfo grouping : groupings) {
			savedSummary.addGrouping(grouping.getGroupingReportField(), grouping
					.getGroupingModifier());
			ReportSummaryGroupingInfo removedGrouping = templateSummary.removeGrouping(grouping
					.getGroupingReportField());
		}
		report.saveReportSummary(savedSummary);
		// Reset template summary title
		templateSummary.setTitle("");
		this.dataManagement.logLastSchemaChangeTime(request);
		UsageLogger usageLogger = new UsageLogger(this.relationalDataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logReportSchemaChange(user, report, AppAction.SAVE_REPORT_SUMMARY, "title: "
				+ summaryTitle);
		UsageLogger.startLoggingThread(usageLogger);
	}

	public synchronized void removeSummaryReport(HttpServletRequest request,
			ReportSummaryInfo reportSummary) throws DisallowedException, CantDoThatException,
			ObjectNotFoundException {
		if (!(this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, reportSummary.getReport().getParentTable()))) {
			throw new DisallowedException(PrivilegeType.MANAGE_TABLE, reportSummary.getReport()
					.getParentTable());
		}
		BaseReportInfo report = reportSummary.getReport();
		if (reportSummary.equals(report.getReportSummary())) {
			throw new CantDoThatException("The default report summary can't be removed");
		}
		// Move the saved summary definition back to the default summary
		// First clear the default summary
		ReportSummaryInfo defaultSummary = report.getReportSummary();
		HibernateUtil.activateObject(defaultSummary);
		for (ReportSummaryAggregateInfo aggregate : defaultSummary.getAggregateFunctions()) {
			defaultSummary.removeFunction(aggregate.getInternalAggregateName());
		}
		for (ReportSummaryGroupingInfo grouping : defaultSummary.getGroupings()) {
			defaultSummary.removeGrouping(grouping.getGroupingReportField());
		}
		// Then move groupings and functions from saved summary to default summary
		for (ReportSummaryAggregateInfo aggregate : reportSummary.getAggregateFunctions()) {
			defaultSummary.addFunction(aggregate);
		}
		for (ReportSummaryGroupingInfo grouping : reportSummary.getGroupings()) {
			defaultSummary.addGrouping(grouping.getGroupingReportField(), grouping.getGroupingModifier());
		}
		// Finally set the default chart title
		defaultSummary.setTitle(reportSummary.getTitle());
		HibernateUtil.activateObject(report);
		report.removeSavedReportSummary(reportSummary);
		HibernateUtil.currentSession().delete(reportSummary);
		this.dataManagement.logLastSchemaChangeTime(request);
		UsageLogger usageLogger = new UsageLogger(this.relationalDataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logReportSchemaChange(user, report, AppAction.REMOVE_REPORT_SUMMARY, "title: "
				+ reportSummary.getTitle());
		UsageLogger.startLoggingThread(usageLogger);
	}

	public synchronized TableInfo getTableByName(HttpServletRequest request, String tableName)
			throws ObjectNotFoundException {
		Set<TableInfo> companyTables = this.getAuthManager().getCompanyForLoggedInUser(request)
				.getTables();
		for (TableInfo table : companyTables) {
			if (table.getTableName().equals(tableName)) {
				if (this.userAllowedToAccessTable(request, table)) {
					return table;
				}
			}
		}
		// if we've got to here the table hasn't been found
		throw new ObjectNotFoundException("The table '" + tableName
				+ "' doesn't exist or you do not have privileges to access it");
	}

	/**
	 * To access a table, the logged in user either has to have view privileges
	 * on that table, or be an administrator of the company the table is in
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

	public synchronized TableInfo getTableByInternalName(HttpServletRequest request,
			String internalTableName) throws ObjectNotFoundException, DisallowedException {
		TableInfo cachedTable = this.tableCache.get(internalTableName);
		if (cachedTable != null) {
			return cachedTable;
		}
		Set<TableInfo> companyTables = this.getAuthManager().getCompanyForLoggedInUser(request)
				.getTables();
		for (TableInfo table : companyTables) {
			if (table.getInternalTableName().equals(internalTableName)) {
				// to retrieve a table, user either has to have view privileges
				// on that table,
				// or be an administrator of the company the table is in
				if (!this.userAllowedToAccessTable(request, table)) {
					throw new DisallowedException(PrivilegeType.VIEW_TABLE_DATA, table);
				}
				this.tableCache.put(internalTableName, table);
				return table;
			}
		}
		// if we've got to here the table hasn't been found
		throw new ObjectNotFoundException("The table with id '" + internalTableName
				+ "' doesn't exist");
	}

	public synchronized TableInfo findTableContainingReport(HttpServletRequest request,
			String reportInternalName) throws ObjectNotFoundException, DisallowedException {
		AuthenticatorInfo authenticator = this.getAuthManager().getAuthenticator();
		Set<TableInfo> companyTables = this.getAuthManager().getCompanyForLoggedInUser(request)
				.getTables();
		for (TableInfo table : companyTables) {
			for (BaseReportInfo report : table.getReports()) {
				if (report.getInternalReportName().equals(reportInternalName)) {
					if (authenticator.loggedInUserAllowedTo(request, PrivilegeType.VIEW_TABLE_DATA,
							table)) {
						return table;
					} else {
						throw new DisallowedException(PrivilegeType.VIEW_TABLE_DATA, table);
					}
				}
			}
		}
		throw new ObjectNotFoundException("Report '" + reportInternalName + "' is not in any table");
	}

	private synchronized TableInfo findTableContainingReportWithoutChecks(
			String reportInternalName, HttpServletRequest request) throws ObjectNotFoundException {
		Set<TableInfo> companyTables = this.getAuthManager().getCompanyForLoggedInUser(request)
				.getTables();
		for (TableInfo table : companyTables) {
			for (BaseReportInfo report : table.getReports()) {
				if (report.getInternalReportName().equals(reportInternalName)) {
					return table;
				}
			}
		}
		throw new ObjectNotFoundException("Report '" + reportInternalName + "' is not in any table");
	}

	public synchronized TableInfo findTableContainingField(HttpServletRequest request,
			String internalFieldName) throws ObjectNotFoundException, DisallowedException {
		CompanyInfo company = this.getAuthManager().getCompanyForLoggedInUser(request);
		Set<TableInfo> tables = company.getTables();
		for (TableInfo table : tables) {
			for (BaseField field : table.getFields()) {
				if (field.getInternalFieldName().equals(internalFieldName)) {
					if (this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
							PrivilegeType.VIEW_TABLE_DATA, table)) {
						return table;
					} else {
						throw new DisallowedException(PrivilegeType.VIEW_TABLE_DATA, table);
					}
				}
			}
		}
		throw new ObjectNotFoundException("Field '" + internalFieldName + "' is not in any table");
	}

	public synchronized ReportFieldInfo findReportFieldByInternalName(HttpServletRequest request,
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
							logger.error("Found report field " + reportField + "in report "
									+ report + " but user not allowed to view that report");
							throw new DisallowedException(PrivilegeType.VIEW_TABLE_DATA, report
									.getParentTable());
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

	public synchronized WikiManagementInfo getWikiManagement(CompanyInfo company) {
		return this.wikiManagements.get(company);
	}

	public synchronized void addWikiManagement(CompanyInfo company,
			WikiManagementInfo wikiManagement) {
		this.wikiManagements.put(company, wikiManagement);
	}

	public String toString() {
		return "DatabaseDefn: Core agileBase methods";
	}

	/**
	 * @param fieldToAdd
	 *            the field object representing the relation
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
	 *            Database connection to work with
	 * @param internalTableName
	 *            Table of index
	 * @param internalFieldName
	 * @param caseInsensitiveIndex
	 *            If true will make an index using the function lower(), so it
	 *            can be used when quick filtering which filters on the
	 *            lowercase input value
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
	 * Generates and executes SQL to create a unique constraint on a field,
	 * using an index
	 */
	private void addUniqueDbAction(Connection conn, String internalTableName,
			String internalFieldName) throws SQLException, CantDoThatException {
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
				throw new CantDoThatException("The table contains duplicate values");
			} else {
				throw sqlex;
			}
		} finally {
			statement.close();
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
				this.removeIndexDbAction(conn, internalTableName, internalFieldName,
						caseInsensitiveIndex);
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
	private void addRemoveRelevantTextIndexes(Connection conn, TextField field) throws SQLException, CantDoThatException {
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
					this.addIndexDbAction(conn, internalTableName, internalFieldName, true);
				}
			}
		}
	}

	/**
	 * runs addUniqueDbAction, providing a database connection
	 */
	private void addUniqueWrapper(String internalTableName, String internalFieldName) throws CantDoThatException {
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
			logger.error("Error getting SQL connection: " + sqlex);
		}
	}

	/**
	 * Lookup of internal table name to table
	 */
	private Map<String, TableInfo> tableCache = new HashMap<String, TableInfo>();

	/**
	 * Keep a cache of the datasource so it's available quickly whenever needed
	 */
	private DataSource relationalDataSource = null;

	private DataManagementInfo dataManagement = null;

	private Map<CompanyInfo, WikiManagementInfo> wikiManagements = new HashMap<CompanyInfo, WikiManagementInfo>();

	private AuthManagerInfo authManager = null;

	public static final String PRIMARY_KEY_DESCRIPTION = "Unique record identifier";

	private ScheduledFuture<?> scheduledDashboardPopulate = null;

	private Thread initialDashboardPopulatorThread = null;

	private static final SimpleLogger logger = new SimpleLogger(DatabaseDefn.class);
}