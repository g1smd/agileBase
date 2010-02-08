/*
 *  Copyright 2009 GT webMarque Ltd
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

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.Calendar;
import java.io.File;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Random;
import org.apache.commons.math.util.MathUtils;
import com.gtwm.pb.auth.PrivilegeType;
import com.gtwm.pb.auth.DisallowedException;
import com.gtwm.pb.model.interfaces.AuthManagerInfo;
import com.gtwm.pb.model.interfaces.AppUserInfo;
import com.gtwm.pb.model.interfaces.CompanyInfo;
import com.gtwm.pb.model.interfaces.ReportCalcFieldInfo;
import com.gtwm.pb.model.interfaces.ReportQuickFilterInfo;
import com.gtwm.pb.model.interfaces.ReportSummaryGroupingInfo;
import com.gtwm.pb.model.interfaces.SessionDataInfo;
import com.gtwm.pb.model.interfaces.DataManagementInfo;
import com.gtwm.pb.model.interfaces.DatabaseInfo;
import com.gtwm.pb.model.interfaces.ReportDataInfo;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.ReportSummaryAggregateInfo;
import com.gtwm.pb.model.interfaces.ReportSummaryDataInfo;
import com.gtwm.pb.model.interfaces.ReportSummaryDataRowInfo;
import com.gtwm.pb.model.interfaces.ReportSummaryInfo;
import com.gtwm.pb.model.interfaces.ReportFieldInfo;
import com.gtwm.pb.model.interfaces.DataRowInfo;
import com.gtwm.pb.model.interfaces.TableDataInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.fields.BaseValue;
import com.gtwm.pb.model.interfaces.fields.CheckboxValue;
import com.gtwm.pb.model.interfaces.fields.DateField;
import com.gtwm.pb.model.interfaces.fields.SeparatorField;
import com.gtwm.pb.model.interfaces.fields.TextField;
import com.gtwm.pb.model.interfaces.fields.FileField;
import com.gtwm.pb.model.interfaces.fields.RelationField;
import com.gtwm.pb.model.interfaces.fields.SequenceField;
import com.gtwm.pb.model.interfaces.fields.TextValue;
import com.gtwm.pb.model.interfaces.fields.IntegerValue;
import com.gtwm.pb.model.interfaces.fields.DurationValue;
import com.gtwm.pb.model.interfaces.fields.DecimalValue;
import com.gtwm.pb.model.interfaces.fields.DecimalField;
import com.gtwm.pb.model.interfaces.fields.DateValue;
import com.gtwm.pb.model.interfaces.fields.FileValue;
import com.gtwm.pb.model.manageData.fields.DateValueDefn;
import com.gtwm.pb.model.manageData.fields.TextValueDefn;
import com.gtwm.pb.model.manageData.fields.IntegerValueDefn;
import com.gtwm.pb.model.manageData.fields.CheckboxValueDefn;
import com.gtwm.pb.model.manageUsage.UsageLogger;
import com.gtwm.pb.util.AppProperties;
import com.gtwm.pb.util.DataDependencyException;
import com.gtwm.pb.util.Helpers;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.Enumerations.AggregateFunction;
import com.gtwm.pb.util.Enumerations.DatabaseFieldType;
import com.gtwm.pb.util.Enumerations.FieldContentType;
import com.gtwm.pb.util.Enumerations.HiddenFields;
import com.gtwm.pb.util.Enumerations.AppAction;
import com.gtwm.pb.util.Enumerations.SummaryGroupingModifier;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.grlea.log.SimpleLogger;
import org.glowacki.CalendarParser;
import org.glowacki.CalendarParserException;
import au.com.bytecode.opencsv.CSVReader;

public class DataManagement implements DataManagementInfo {

	/**
	 * @param dataSource
	 *            Provides access to the relational database
	 * @param webAppRoot
	 *            Allows access to the filesystem
	 * @param authManager
	 *            Provides access to company, role, user etc. objects
	 */
	public DataManagement(DataSource dataSource, String webAppRoot, AuthManagerInfo authManager) {
		this.dataSource = dataSource;
		this.webAppRoot = webAppRoot;
		this.authManager = authManager;
	}

	public String getWebAppRoot() {
		return this.webAppRoot;
	}

	public void saveRecord(HttpServletRequest request, TableInfo table,
			LinkedHashMap<BaseField, BaseValue> dataToSave, boolean newRecord, int rowId,
			SessionDataInfo sessionData, List<FileItem> multipartItems)
			throws InputRecordException, ObjectNotFoundException, SQLException,
			CodingErrorException, DisallowedException, CantDoThatException {
		// editing a single record, pass in one row id
		Set<Integer> rowIds = new HashSet<Integer>();
		rowIds.add(rowId);
		this.saveRecord(request, table, dataToSave, newRecord, rowIds, sessionData, multipartItems);
	}

	public int globalEdit(HttpServletRequest request, TableInfo table,
			LinkedHashMap<BaseField, BaseValue> dataToSave, SessionDataInfo sessionData,
			List<FileItem> multipartItems) throws InputRecordException, ObjectNotFoundException,
			SQLException, CodingErrorException, CantDoThatException, DisallowedException {
		int affectedFieldCount = dataToSave.size();
		for (BaseField affectedField : dataToSave.keySet()) {
			if (affectedField.getFieldName().equals(HiddenFields.LAST_MODIFIED.getFieldName())) {
				affectedFieldCount--;
			}
			if (affectedField.getFieldName().equals(HiddenFields.MODIFIED_BY.getFieldName())) {
				affectedFieldCount--;
			}
		}
		if (affectedFieldCount > 1) {
			throw new CantDoThatException(
					"Global edits can only apply changes to one field at a time. Requested field changes were "
							+ dataToSave);
		}
		Connection conn = null;
		Set<Integer> rowIds = new HashSet<Integer>();
		BaseReportInfo sessionReport = sessionData.getReport();
		BaseField primaryKey = table.getPrimaryKey();
		try {
			conn = this.dataSource.getConnection();
			conn.setAutoCommit(false);
			ReportDataInfo reportData = new ReportData(conn, sessionReport, false, false);
			// Generates a SELECT DISTINCT on the primary key including
			// filterValues & rowLimits in the WHERE clause
			Map<BaseField, Boolean> emptySorts = new HashMap<BaseField, Boolean>();
			Map<BaseField, String> filterValues = sessionData.getReportFilterValues();

			PreparedStatement statement = reportData.getReportSqlPreparedStatement(conn,
					filterValues, false, emptySorts, -1, primaryKey);
			ResultSet results = statement.executeQuery();
			while (results.next()) {
				Integer item = results.getInt(1);
				if (item != null) {
					rowIds.add(item);
				}
			}
			results.close();
			statement.close();
		} catch (SQLException sqlex) {
			// catch exception where field is not included
			// within report and simply return an empty tree
			logger.warn(sqlex.toString() + ". Probably occurred because field " + this
					+ " isn't in report " + sessionReport
					+ ", in which case it's nothing to worry about");
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		this.saveRecord(request, table, dataToSave, false, rowIds, sessionData, multipartItems);
		return rowIds.size();
	}

	public void cloneRecord(HttpServletRequest request, TableInfo table, int rowId,
			SessionDataInfo sessionData, List<FileItem> multipartItems)
			throws ObjectNotFoundException, SQLException, CantDoThatException,
			CodingErrorException, InputRecordException, DisallowedException {
		// Get values to clone.
		Map<BaseField, BaseValue> values = this.getTableDataRow(table, rowId);
		// Store in a linked hash map to maintain order as saveRecord needs
		// values in an order which isn't going to change.
		LinkedHashMap<BaseField, BaseValue> valuesToClone = new LinkedHashMap<BaseField, BaseValue>();
		// Get list of fields direct from source table
		// (getTableDataRow switches RelationField for
		// RelationField.getRelatedField)
		SortedSet<BaseField> fields = table.getFields();
		// Ignore un-clonable fields
		for (BaseField field : fields) {
			if (!(field instanceof FileField) && !(field instanceof SeparatorField)
					&& !(field.getUnique())) { // &&
				// !(field.getHidden()))
				// {
				if (field instanceof RelationField) {
					BaseValue relationValue = values.get(((RelationField) field).getRelatedField());
					valuesToClone.put(field, relationValue);
				} else {
					valuesToClone.put(field, values.get(field));
				}
			}
		}
		Set<Integer> rowIds = new HashSet<Integer>();
		rowIds.add(-1);
		this.saveRecord(request, table, valuesToClone, true, rowIds, sessionData, multipartItems);
	}

	public void lockAllTableRecords(HttpServletRequest request, SessionDataInfo sessionData)
			throws ObjectNotFoundException, CantDoThatException, SQLException {
		TableInfo table = sessionData.getTable();
		if (!table.getRecordsLockable()) {
			throw new CantDoThatException("Records in the " + table + " table can't be locked");
		}
		String lockFieldInternalName = table.getField(HiddenFields.LOCKED.getFieldName())
				.getInternalFieldName();
		String SQLCode = "UPDATE " + table.getInternalTableName() + " SET " + lockFieldInternalName
				+ " = true WHERE " + lockFieldInternalName + " = false";
		Connection conn = null;
		try {
			conn = this.dataSource.getConnection();
			conn.setAutoCommit(false);
			Statement statement = conn.createStatement();
			statement.executeUpdate(SQLCode);
			statement.close();
			conn.commit();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		// Also remove any lock override in the session
		((SessionData) sessionData).unsetRecordLockOverride();
	}

	public void lockReportRecords(HttpServletRequest request, SessionDataInfo sessionData)
			throws ObjectNotFoundException, CantDoThatException, SQLException, CodingErrorException {
		CompanyInfo company = this.authManager.getCompanyForLoggedInUser(request);
		BaseReportInfo report = sessionData.getReport();
		TableInfo table = report.getParentTable();
		if (!table.getRecordsLockable()) {
			throw new CantDoThatException("Records in the " + table + " table can't be locked");
		}
		Map<BaseField, String> filters = sessionData.getReportFilterValues();
		Map<BaseField, Boolean> sorts = new HashMap<BaseField, Boolean>();
		List<DataRowInfo> dataRows = this.getReportDataRows(company, report, filters, false, sorts,
				-1);
		String lockFieldInternalName = table.getField(HiddenFields.LOCKED.getFieldName())
				.getInternalFieldName();
		String SQLCode = "UPDATE " + table.getInternalTableName() + " SET " + lockFieldInternalName
				+ " = true WHERE " + lockFieldInternalName + " = false AND "
				+ table.getPrimaryKey().getInternalFieldName() + " = ?";
		Connection conn = null;
		try {
			conn = this.dataSource.getConnection();
			conn.setAutoCommit(false);
			PreparedStatement statement = conn.prepareStatement(SQLCode);
			for (DataRowInfo dataRow : dataRows) {
				int rowId = dataRow.getRowId();
				statement.setInt(1, rowId);
				statement.executeUpdate();
			}
			statement.close();
			conn.commit();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		// Also remove any lock override in the session
		((SessionData) sessionData).unsetRecordLockOverride();
	}

	public void lockRecord(SessionDataInfo sessionData, TableInfo table, int rowId)
			throws SQLException, CantDoThatException, ObjectNotFoundException {
		if (!table.getRecordsLockable()) {
			throw new CantDoThatException("Records in the " + table + " table can't be locked");
		}
		String SQLCode = "UPDATE " + table.getInternalTableName() + " SET "
				+ table.getField(HiddenFields.LOCKED.getFieldName()).getInternalFieldName()
				+ " = true WHERE " + table.getPrimaryKey().getInternalFieldName() + " = ?";
		Connection conn = null;
		try {
			conn = this.dataSource.getConnection();
			conn.setAutoCommit(false);
			PreparedStatement statement = conn.prepareStatement(SQLCode);
			statement.setInt(1, rowId);
			int rowsAffected = statement.executeUpdate();
			if (rowsAffected != 1) {
				logger.warn("Expected one record to be locked but " + rowsAffected
						+ " were. SQLCode = " + SQLCode + ", rowid = " + rowId);
			}
			statement.close();
			conn.commit();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		// Override lock if we just locked the same record
		TableInfo recordLockOverrideTable = ((SessionData) sessionData)
				.getRecordLockOverrideTable();
		int recordLockOverrideRowId = ((SessionData) sessionData).getRecordLockOverrideRowId();
		if (recordLockOverrideTable != null) {
			if ((recordLockOverrideTable.equals(table)) && (recordLockOverrideRowId == rowId)) {
				((SessionData) sessionData).unsetRecordLockOverride();
			}
		}
	}

	public boolean isRecordLocked(SessionDataInfo sessionData, TableInfo table, int rowId)
			throws SQLException, ObjectNotFoundException {
		TableDataInfo tableData = new TableData(table);
		Connection conn = null;
		try {
			conn = this.dataSource.getConnection();
			conn.setAutoCommit(false);
			return tableData.isRecordLocked(conn, sessionData, rowId);
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	private void setHiddenFieldValues(HttpServletRequest request, TableInfo table,
			LinkedHashMap<BaseField, BaseValue> dataToSave, boolean newRecord)
			throws CantDoThatException, ObjectNotFoundException, DisallowedException {
		BaseValue fieldValue;
		for (BaseField field : table.getFields()) {
			fieldValue = null;
			// Set values for hard-coded hidden fields:
			if (newRecord) {
				if (field.getFieldName().equals(HiddenFields.DATE_CREATED.getFieldName())) {
					fieldValue = new DateValueDefn(new Date());
					// without this line only the month & year part will be set
					((DateValue) fieldValue).setDateResolution(Calendar.SECOND);
				}
				if (field.getFieldName().equals(HiddenFields.CREATED_BY.getFieldName())) {
					String userName = request.getRemoteUser();
					AppUserInfo currentUser = this.authManager.getUserByUserName(request, userName);
					String fullname = currentUser.getForename() + " " + currentUser.getSurname();
					fieldValue = new TextValueDefn(fullname);
				}
				if (field.getFieldName().equals(HiddenFields.LOCKED.getFieldName())) {
					fieldValue = new CheckboxValueDefn(false);
				}
			}
			if (field.getFieldName().equals(HiddenFields.LAST_MODIFIED.getFieldName())) {
				fieldValue = new DateValueDefn(new Date());
				// without this line only the month & year part will be set
				((DateValue) fieldValue).setDateResolution(Calendar.SECOND);
			}
			if (field.getFieldName().equals(HiddenFields.MODIFIED_BY.getFieldName())) {
				String userName = request.getRemoteUser();
				AppUserInfo currentUser = this.authManager.getUserByUserName(request, userName);
				String fullname = currentUser.getForename() + " " + currentUser.getSurname();
				fieldValue = new TextValueDefn(fullname);
			}
			if (fieldValue != null) {
				// by design, the user should never be able to send a request
				// to update the username/date hidden fields as this would
				// put a hole in the auditing functionality
				dataToSave.put(field, fieldValue);
			}
		}
	}

	/**
	 * Used by both the public saveRecord and globalEdit methods
	 */
	private void saveRecord(HttpServletRequest request, TableInfo table,
			LinkedHashMap<BaseField, BaseValue> dataToSave, boolean newRecord, Set<Integer> rowIds,
			SessionDataInfo sessionData, List<FileItem> multipartItems)
			throws InputRecordException, ObjectNotFoundException, SQLException,
			CantDoThatException, CodingErrorException, DisallowedException {
		if ((dataToSave.size() == 0) && (!newRecord)) {
			// Note: this does actually happen quite a lot, from two particular
			// users.
			// Haven't tracked down the cause but it doesn't seem to be creating
			// a problem
			// logger.warn("Call to saveRecord with no data to save. User = "
			// + request.getRemoteUser() + ", table = " + table + ", rowIds = "
			// + rowIds);
			return;
		}
		this.setHiddenFieldValues(request, table, dataToSave, newRecord);
		boolean globalEdit = false;
		int rowId = -1;
		if (rowIds.size() > 1) {
			globalEdit = true;
		} else if (rowIds.size() == 1) {
			rowId = (new LinkedList<Integer>(rowIds)).getFirst();
		} else {
			throw new ObjectNotFoundException("Row ID list " + rowIds + " is invalid");
		}
		StringBuilder SQLCodeBuilder = new StringBuilder();
		// Generate CSV of fields and placeholders to use in update/insert SQL
		// string
		StringBuilder fieldsCsvBuilder = new StringBuilder();
		StringBuilder fieldsAndPlaceholdersCsvBuilder = new StringBuilder();
		StringBuilder valuePlaceholdersCsvBuilder = new StringBuilder();
		for (BaseField field : dataToSave.keySet()) {
			fieldsCsvBuilder.append(field.getInternalFieldName());
			fieldsCsvBuilder.append(", ");
			valuePlaceholdersCsvBuilder.append("?, ");
			fieldsAndPlaceholdersCsvBuilder.append(field.getInternalFieldName());
			fieldsAndPlaceholdersCsvBuilder.append("=?, ");
		}
		// Used if doing an INSERT
		String fieldsCsv = fieldsCsvBuilder.toString();
		String valuePlaceholdersCsv = valuePlaceholdersCsvBuilder.toString();
		// Used if doing an UPDATE
		String fieldsAndPlaceholdersCsv = fieldsAndPlaceholdersCsvBuilder.toString();
		if (!fieldsCsv.equals("")) {
			fieldsCsv = fieldsCsv.substring(0, fieldsCsv.length() - 2);
			valuePlaceholdersCsv = valuePlaceholdersCsv.substring(0,
					valuePlaceholdersCsv.length() - 2);
			fieldsAndPlaceholdersCsv = fieldsAndPlaceholdersCsv.substring(0,
					fieldsAndPlaceholdersCsv.length() - 2);
		}
		if (newRecord) {
			SQLCodeBuilder.append("INSERT INTO " + table.getInternalTableName());
			if (fieldsCsv.equals("")) {
				SQLCodeBuilder.append(" VALUES(default)");
			} else {
				SQLCodeBuilder.append("(" + fieldsCsv + ") VALUES (" + valuePlaceholdersCsv + ")");
			}
		} else {
			SQLCodeBuilder.append("UPDATE " + table.getInternalTableName() + " SET "
					+ fieldsAndPlaceholdersCsv);
			if (globalEdit) {
				// add filter for various row ids
				SQLCodeBuilder.append(" WHERE " + table.getPrimaryKey().getInternalFieldName()
						+ " in (?");
				for (int i = 1; i < rowIds.size(); i++) {
					SQLCodeBuilder.append(",?");
				}
				SQLCodeBuilder.append(")");
			} else {
				// add filter for single row id
				SQLCodeBuilder.append(" WHERE " + table.getPrimaryKey().getInternalFieldName()
						+ "=?");
			}
		}
		Connection conn = null;
		int fieldNumber = 0;
		// Will be set if we're inserting a record
		int newRowId = -1;
		TableDataInfo tableData = new TableData(table);
		try {
			conn = this.dataSource.getConnection();
			conn.setAutoCommit(false);
			PreparedStatement statement = conn.prepareStatement(SQLCodeBuilder.toString());
			for (BaseField field : dataToSave.keySet()) {
				// If an exception is raised, currentField will be the field
				// which caused it
				// currentField = field;
				fieldNumber++;
				BaseValue fieldValue = dataToSave.get(field);
				if (field instanceof FileField) {
					if (fieldValue.isNull() || fieldValue.toString().equals("")) {
						throw new InputRecordException("No file specified for the upload", field);
					}
				}
				if (fieldValue.isNull()) {
					statement.setNull(fieldNumber, Types.NULL);
				} else {
					if (fieldValue instanceof TextValue) {
						String textValue = ((TextValue) fieldValue).toXmlString();
						statement.setString(fieldNumber, textValue);
					} else if (fieldValue instanceof IntegerValue) {
						// if no related value, set relation field to null
						if (field instanceof RelationField
								&& (((IntegerValue) fieldValue).getValueInteger() == -1)) {
							statement.setNull(fieldNumber, Types.NULL);
						} else {
							statement.setInt(fieldNumber, ((IntegerValue) fieldValue)
									.getValueInteger());
						}
					} else if (fieldValue instanceof DurationValue) {
						statement.setString(fieldNumber, ((DurationValue) fieldValue)
								.getSqlFormatInterval());
					} else if (fieldValue instanceof DecimalValue) {
						statement.setDouble(fieldNumber, ((DecimalValue) fieldValue)
								.getValueFloat());
					} else if (fieldValue instanceof DateValue) {
						if (((DateValue) fieldValue).getValueDate() != null) {
							java.util.Date javaDateValue = ((DateValue) fieldValue).getValueDate()
									.getTime();
							java.sql.Timestamp sqlTimestampValue = new java.sql.Timestamp(
									javaDateValue.getTime());
							statement.setTimestamp(fieldNumber, sqlTimestampValue);
						} else {
							statement.setTimestamp(fieldNumber, null);
						}
					} else if (fieldValue instanceof CheckboxValue) {
						statement.setBoolean(fieldNumber, ((CheckboxValue) fieldValue)
								.getValueBoolean());
					} else if (fieldValue instanceof FileValue) {
						statement.setString(fieldNumber, ((FileValue) fieldValue).toString());
					} else {
						throw new CodingErrorException("Field value " + fieldValue
								+ " is of unknown type " + fieldValue.getClass().getSimpleName());
					}
				}
			}
			// We've finished setting individual fields, if an SQL error occurs
			// after here we won't know which
			// field caused it without looking for it by other means
			// currentField = null;
			if (!newRecord) {
				if (globalEdit) {
					// Fill in the 'WHERE [row id field] in (?,..,?)' for use in
					// the UPDATE statement
					for (Integer aRowId : rowIds) {
						if (tableData.isRecordLocked(conn, sessionData, aRowId)) {
							throw new CantDoThatException("Record " + aRowId + " from table "
									+ table + " is locked to prevent editing");
						}
						statement.setInt(++fieldNumber, aRowId);
					}
				} else {
					// Fill in the 'WHERE [row id field]=?' for use in the
					// UPDATE statement
					if (tableData.isRecordLocked(conn, sessionData, rowId)) {
						throw new CantDoThatException("Record " + rowId + " from table " + table
								+ " is locked to prevent editing");
					}
					statement.setInt(fieldNumber + 1, rowId);
				}
			}
			int numRowsAffected = statement.executeUpdate();
			statement.close();
			if (numRowsAffected != 1) {
				if ((numRowsAffected > 1) && (!globalEdit)) {
					conn.rollback();
					throw new ObjectNotFoundException(String.valueOf(numRowsAffected)
							+ " records altered during a single record save");
				}
			}
			if (newRecord) {
				// Find the newly inserted Row ID
				// postgres-specific code, not database independent
				String SQLCode = "SELECT currval('" + table.getInternalTableName() + "_"
						+ table.getPrimaryKey().getInternalFieldName() + "_seq')";
				statement = conn.prepareStatement(SQLCode);
				ResultSet results = statement.executeQuery();
				if (results.next()) {
					newRowId = results.getInt(1);
				} else {
					results.close();
					statement.close();
					throw new SQLException("Row ID not found for the newly inserted record. '"
							+ SQLCodeBuilder + "' didn't work");
				}
				results.close();
				statement.close();
			}
			conn.commit();
		} catch (SQLException sqlex) {
			// Find out which field caused the error by looking for internal
			// field names in the error message
			String errorMessage = sqlex.getMessage();

			for (BaseField possibleCauseField : dataToSave.keySet()) {
				if (errorMessage.contains(possibleCauseField.getInternalFieldName())) {
					if (errorMessage.contains("check constraint")) {
						errorMessage = "The value " + dataToSave.get(possibleCauseField)
								+ " falls outside the allowed range";
					} else if (errorMessage.contains("not-null constraint")) {
						errorMessage = "No value entered";
					} else if (errorMessage.contains("unique constraint")) {
						errorMessage = "Value " + dataToSave.get(possibleCauseField)
								+ " is already in the database and cannot be entered again";
					} else if (errorMessage.contains("foreign key constraint")
							&& possibleCauseField instanceof RelationField) {
						errorMessage = "Please select a valid "
								+ ((RelationField) possibleCauseField).getRelatedTable()
								+ " record first";
					} else {
						errorMessage = "Value "
								+ dataToSave.get(possibleCauseField)
								+ " not allowed ("
								+ Helpers.replaceInternalNames(errorMessage, table
										.getDefaultReport()) + ")";
					}
					throw new InputRecordException(errorMessage, possibleCauseField);
				}
			}
			// Not able to find field
			errorMessage = Helpers.replaceInternalNames(errorMessage, table.getDefaultReport());
			throw new InputRecordException(errorMessage, null);
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		// If any fields were files to upload, do the actual uploads.
		// Do this after the commit in case the uploads take a long time and
		// time out the SQL connection.
		for (BaseField field : dataToSave.keySet()) {
			if (field instanceof FileField) {
				try {
					if (newRecord) {
						this.uploadFile(request, (FileField) field, (FileValue) dataToSave
								.get(field), newRowId, multipartItems);
					} else {
						this.uploadFile(request, (FileField) field, (FileValue) dataToSave
								.get(field), rowId, multipartItems);
					}
				} catch (CantDoThatException cdtex) {
					throw new InputRecordException("Error uploading file: " + cdtex.getMessage(),
							field);
				} catch (FileUploadException fuex) {
					throw new InputRecordException("Error uploading file: " + fuex.getMessage(),
							field);
				}
			}
		}
		if (newRecord) {
			sessionData.setRowId(newRowId);
		}
		this.logLastDataChangeTime(request);
		UsageLogger usageLogger = new UsageLogger(this.dataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		// Log everything apart from hidden (auto set) fields
		Map<BaseField, BaseValue> dataToLog = new LinkedHashMap<BaseField, BaseValue>();
		for (Map.Entry<BaseField, BaseValue> entrySet : dataToSave.entrySet()) {
			BaseField field = entrySet.getKey();
			if (!field.getHidden()) {
				BaseValue value = entrySet.getValue();
				dataToLog.put(field, value);
			}
		}
		if (newRecord) {
			usageLogger.logDataChange(user, table, AppAction.SAVE_NEW_RECORD, newRowId, dataToLog
					.toString());
		} else if (globalEdit) {
			// TODO: need better logging of global edits
			usageLogger.logDataChange(user, table, AppAction.GLOBAL_EDIT, rowId, dataToLog
					.toString());
		} else {
			usageLogger.logDataChange(user, table, AppAction.UPDATE_RECORD, rowId, dataToLog
					.toString());
		}
		UsageLogger.startLoggingThread(usageLogger);
	}

	public int importCSV(HttpServletRequest request, TableInfo table,
			boolean updateExistingRecords, BaseField recordIdentifierField, boolean generateRowIds,
			char separator, char quotechar, int numHeaderLines, boolean useRelationDisplayValues,
			boolean importSequenceValues, boolean requireExactRelationMatches,
			List<FileItem> multipartItems, String csvContent) throws SQLException,
			InputRecordException, IOException, CantDoThatException, ObjectNotFoundException,
			DisallowedException, CodingErrorException {
		if (!FileUpload.isMultipartContent(new ServletRequestContext(request))) {
			if (csvContent == null) {
				throw new CantDoThatException(
						"To import CSV content, a file must be uploaded (form posted as multi-part) or csv_content specified");
			}
		}
		int numImportedRecords = 0;
		// get field set to import into. LinkedHashSet to ensure order is
		// retained so the right values are imported into the right fields
		LinkedHashSet<BaseField> fields = new LinkedHashSet<BaseField>(table.getFields());
		// if row IDs aren't included in the data to import, remove ID from the
		// field set
		BaseField primaryKey = table.getPrimaryKey();
		if (recordIdentifierField == null) {
			recordIdentifierField = primaryKey;
		}
		if (generateRowIds || (updateExistingRecords && !recordIdentifierField.equals(primaryKey))) {
			fields.remove(primaryKey);
		}
		Map<RelationField, Map<String, String>> relationLookups = new HashMap<RelationField, Map<String, String>>();
		// Remove fields which shouldn't be modified during the import
		// For serial fields, if we need to set serial values explicitly, this
		// will have to be dealt with later
		for (BaseField field : table.getFields()) {
			if (field instanceof SequenceField && (!field.equals(primaryKey))
					&& (!importSequenceValues)) {
				fields.remove(field);
			} else if (field.getHidden()) {
				if (field.getFieldName().equals(HiddenFields.WIKI_PAGE.getFieldName())) {
					fields.remove(field);
				} else if (updateExistingRecords) {
					if (field.getFieldName().equals(HiddenFields.DATE_CREATED.getFieldName())
							|| field.getFieldName().equals(HiddenFields.CREATED_BY.getFieldName())) {
						fields.remove(field);
					}
				}
			} else if (field instanceof SeparatorField) {
				fields.remove(field);
			}
			// Also, if importing relations by display value, look up
			// display/internal value mappings
			if (useRelationDisplayValues && field instanceof RelationField) {
				Map<String, String> displayToInternalValue = ((RelationField) field).getItems(true);
				relationLookups.put((RelationField) field, displayToInternalValue);
			}
		}
		// Prepare SQL
		String insertSQLCode = null;
		String updateSQLCode = null;
		// If updating, we'll need a record ID value. Depending on what the
		// identifier field is, this could be one of a couple of different types
		String recordIdentifierString = null;
		Integer recordIdentifierInteger = null;
		int recordIdentifierFieldNum = 0;
		DatabaseFieldType identifierFieldDbType = null;
		if (updateExistingRecords) {
			identifierFieldDbType = recordIdentifierField.getDbType();
			if (!identifierFieldDbType.equals(DatabaseFieldType.VARCHAR)
					&& !identifierFieldDbType.equals(DatabaseFieldType.INTEGER)
					&& !identifierFieldDbType.equals(DatabaseFieldType.SERIAL)) {
				throw new CantDoThatException(
						"The record identifier field has to be text or a whole number, "
								+ recordIdentifierField + " is a " + identifierFieldDbType);
			}
			updateSQLCode = "UPDATE " + table.getInternalTableName() + " SET ";
			int fieldNum = 0;
			for (BaseField field : fields) {
				fieldNum += 1;
				updateSQLCode += field.getInternalFieldName() + "=?, ";
				if (field.equals(recordIdentifierField)) {
					recordIdentifierFieldNum = fieldNum;
				}
			}
			if (recordIdentifierFieldNum == 0) {
				throw new CantDoThatException(
						"Can't find the field specified as record identifier ("
								+ recordIdentifierField + ") in the list of table fields " + fields
								+ " in table " + table);
			}
			updateSQLCode = updateSQLCode.substring(0, updateSQLCode.length() - 2);
			updateSQLCode += " WHERE " + recordIdentifierField.getInternalFieldName() + "=?";
		} else {
			insertSQLCode = "INSERT INTO " + table.getInternalTableName() + "(";
			String placeholders = "";
			for (BaseField field : fields) {
				insertSQLCode += field.getInternalFieldName() + ", ";
				placeholders += "?, ";
			}
			placeholders = placeholders.substring(0, placeholders.length() - 2);
			insertSQLCode = insertSQLCode.substring(0, insertSQLCode.length() - 2) + ") VALUES ("
					+ placeholders + ")";
		}
		// Find content to import
		Reader inputStreamReader = null;
		if (csvContent != null) {
			inputStreamReader = new StringReader(csvContent);
		} else {
			for (FileItem item : multipartItems) {
				// if item is a file
				if (!item.isFormField()) {
					if (item.getName().toLowerCase().endsWith(".xls")) {
						throw new CantDoThatException(
								"You need to upload as a CSV to import, Excel files can't be imported directly");
					}
					inputStreamReader = new InputStreamReader(item.getInputStream());
					break;
				}
			}
		}
		if (inputStreamReader == null) {
			throw new CantDoThatException("No file uploaded");
		}
		CSVReader csvReader = new CSVReader(inputStreamReader, separator, quotechar, numHeaderLines);
		// returns a list of String arrays
		List<String[]> csvLines = (List<String[]>) csvReader.readAll();
		// do db inserts
		Connection conn = null;
		PreparedStatement statement = null;
		// These two variables used in exception handling
		int importLine = 0;
		BaseField fieldImported = null;
		Timestamp importTime = new Timestamp(System.currentTimeMillis());
		AppUserInfo loggedInUser = authManager.getUserByUserName(request, request.getRemoteUser());
		String fullname = loggedInUser.getForename() + " " + loggedInUser.getSurname();
		try {
			conn = this.dataSource.getConnection();
			conn.setAutoCommit(false);
			if (updateExistingRecords) {
				statement = conn.prepareStatement(updateSQLCode);
			} else {
				statement = conn.prepareStatement(insertSQLCode);
			}
			CSVLINE: for (String[] csvLineArray : csvLines) {
				// convert to an object rather than a primitive array -
				// easier to work with
				List<String> lineValues = Arrays.asList(csvLineArray);
				importLine++;
				// skip blank lines
				if (lineValues.size() == 1) {
					if (lineValues.get(0).length() == 0) {
						continue CSVLINE;
					}
				}
				int fieldNum = 0;
				for (BaseField field : fields) {
					fieldImported = field;
					fieldNum++;
					if (field.getHidden()) {
						String fieldName = field.getFieldName();
						if (fieldName.equals(HiddenFields.LOCKED.getFieldName())) {
							statement.setBoolean(fieldNum, false);
						} else if (fieldName.equals(HiddenFields.DATE_CREATED.getFieldName())
								|| fieldName.equals(HiddenFields.LAST_MODIFIED.getFieldName())) {
							statement.setTimestamp(fieldNum, importTime);
						} else if (fieldName.equals(HiddenFields.CREATED_BY.getFieldName())
								|| fieldName.equals(HiddenFields.MODIFIED_BY.getFieldName())) {
							statement.setString(fieldNum, fullname);
						}
					} else if (fieldNum > lineValues.size()) {
						// booleans have a not null constraint
						if (field.getDbType().equals(Types.BOOLEAN)) {
							statement.setBoolean(fieldNum, false);
						} else {
							statement.setNull(fieldNum, Types.NULL);
						}
					} else {
						String lineValue = lineValues.get(fieldNum - 1);
						if (lineValue != null) {
							if (lineValue.equals("")) {
								// booleans have a not null constraint
								if (field.getDbType().equals(Types.BOOLEAN)) {
									statement.setBoolean(fieldNum, false);
								} else {
									statement.setNull(fieldNum, Types.NULL);
								}
							} else {
								if ((field instanceof FileField) && (generateRowIds)) {
									throw new CantDoThatException(
											"Cannot generate row ids when importing file names. See line "
													+ importLine + ", field '"
													+ field.getFieldName() + "' with value '"
													+ lineValue + "'");
								}
								switch (field.getDbType()) {
								case VARCHAR:
									statement.setString(fieldNum, lineValue);
									if (updateExistingRecords
											&& field.equals(recordIdentifierField)) {
										recordIdentifierString = lineValue;
									}
									break;
								case TIMESTAMP:
									// deal with month and year
									// resolution dates exported
									if (lineValue.matches("^[a-zA-Z]{3}\\s\\d{2,4}$")) {
										lineValue = "01 " + lineValue;
									} else if (lineValue.matches("^\\d{2,4}")) {
										lineValue = "01 Jan " + lineValue;
									}
									try {
										Calendar calValue = CalendarParser.parse(lineValue,
												CalendarParser.DD_MM_YY);
										statement.setTimestamp(fieldNum, new Timestamp(calValue
												.getTimeInMillis()));
									} catch (CalendarParserException cpex) {
										throw new InputRecordException("Error importing line "
												+ importLine + ", field " + field + ": "
												+ cpex.getMessage(), field);
									}
									break;
								case FLOAT:
									lineValue = lineValue.trim()
											.replaceAll("[^\\d\\.\\+\\-eE]", "");
									statement.setDouble(fieldNum, Double.valueOf(lineValue));
									break;
								case INTEGER:
									if ((field instanceof RelationField)
											&& (useRelationDisplayValues)) {
										// find key value for display value
										RelationField relationField = (RelationField) field;
										Map<String, String> valueKeyMap = relationLookups
												.get(relationField);
										String internalValueString = valueKeyMap.get(lineValue);
										if (internalValueString == null) {
											if (!requireExactRelationMatches) {
												// A very basic fuzzy matching
												// algorithm
												String potentialDisplayValue = null;
												String lineValueLowerCase = lineValue.toLowerCase();
												FUZZYMATCH: for (Map.Entry<String, String> entry : valueKeyMap
														.entrySet()) {
													potentialDisplayValue = entry.getKey();
													if (potentialDisplayValue.toLowerCase()
															.contains(lineValueLowerCase)) {
														internalValueString = entry.getValue();
														break FUZZYMATCH;
													}
												}
											}
											if (internalValueString == null) {
												throw new CantDoThatException(
														"Error importing line " + importLine
																+ ", field " + relationField
																+ ": Can't find a related '"
																+ relationField.getRelatedTable()
																+ "' for "
																+ relationField.getDisplayField()
																+ " '" + lineValue + "'. ");
											}
										}
										int keyValue = Integer.valueOf(internalValueString);
										statement.setInt(fieldNum, keyValue);
										if (updateExistingRecords
												&& field.equals(recordIdentifierField)) {
											recordIdentifierInteger = keyValue;
										}
									} else {
										lineValue = lineValue.trim().replaceAll(
												"[^\\d\\.\\+\\-eE]", "");
										int keyValue = Integer.valueOf(lineValue);
										statement.setInt(fieldNum, keyValue);
										if (updateExistingRecords
												&& field.equals(recordIdentifierField)) {
											recordIdentifierInteger = keyValue;
										}
									}
									break;
								case SERIAL:
									lineValue = lineValue.trim()
											.replaceAll("[^\\d\\.\\+\\-eE]", "");
									int keyValue = Integer.valueOf(lineValue);
									statement.setInt(fieldNum, keyValue);
									if (updateExistingRecords
											&& field.equals(recordIdentifierField)) {
										recordIdentifierInteger = keyValue;
									}
									break;
								case BOOLEAN:
									boolean filterValueIsTrue = Helpers
											.valueRepresentsBooleanTrue(lineValue);
									statement.setBoolean(fieldNum, filterValueIsTrue);
									break;
								}
							}
						} else {
							// booleans have a not null constraint
							if (field.getDbType().equals(Types.BOOLEAN)) {
								statement.setBoolean(fieldNum, false);
							} else {
								statement.setNull(fieldNum, Types.NULL);
							}
						}
					}
				}
				if (updateExistingRecords) {
					// for potential error messages
					String errorSnippet = null;
					if (identifierFieldDbType.equals(DatabaseFieldType.INTEGER)
							|| identifierFieldDbType.equals(DatabaseFieldType.SERIAL)) {
						if (recordIdentifierInteger == null) {
							throw new InputRecordException(
									"Can't find a record identifier value at line " + importLine,
									recordIdentifierField);
						}
						errorSnippet = recordIdentifierField.getFieldName() + " = "
								+ recordIdentifierInteger;
						// Set the 'WHERE recordIdentifier = ?' clause
						statement.setInt(fields.size() + 1, recordIdentifierInteger);
					} else {
						if (recordIdentifierString == null) {
							throw new InputRecordException(
									"Can't find a record identifier value at line " + importLine,
									recordIdentifierField);
						}
						errorSnippet = recordIdentifierField.getFieldName() + " = '"
								+ recordIdentifierString + "'";
						// Set the 'WHERE recordIdentifier = ?' clause
						statement.setString(fields.size() + 1, recordIdentifierString);
					}
					int rowsAffected = statement.executeUpdate();
					if (rowsAffected == 0) {
						throw new InputRecordException("Error importing line " + importLine
								+ ". The record identifier " + errorSnippet
								+ " can't be found in the database", recordIdentifierField);
					} else if (rowsAffected > 1) {
						throw new InputRecordException(
								"Error importing line "
										+ importLine
										+ ". The record identifier field "
										+ errorSnippet
										+ " should match one record in the database but it actually matches "
										+ rowsAffected, recordIdentifierField);
					}
					// reset to null for the next line
					recordIdentifierString = null;
					recordIdentifierInteger = null;
				} else {
					statement.executeUpdate();
				}
				numImportedRecords += 1;
			}
			statement.close();
			// reset the primary key ID sequence so new records can be added
			resetSequence((SequenceField) primaryKey, conn);
			// and any other sequence fields
			if (importSequenceValues) {
				for (BaseField field : table.getFields()) {
					if ((!field.equals(primaryKey)) && field instanceof SequenceField) {
						resetSequence((SequenceField) field, conn);
					}
				}
			}
			// ANALYZE the table after import
			if (numImportedRecords > 1000) {
				Statement analyzeStatement = conn.createStatement();
				analyzeStatement.execute("ANALYZE " + table.getInternalTableName());
				analyzeStatement.close();
			}
			conn.commit();
		} catch (SQLException sqlex) {
			String databaseErrorMessage = Helpers.replaceInternalNames(sqlex.getMessage(), table
					.getDefaultReport());
			logger.warn("Import failed, statement is " + statement);
			String errorMessage = "Error importing CSV line " + importLine;
			if (!fieldImported.getHidden()) {
				errorMessage += ", field '" + fieldImported + "'";
			}
			errorMessage += "': " + databaseErrorMessage;
			throw new InputRecordException(errorMessage, fieldImported);
		} catch (NumberFormatException nfex) {
			String causeMessage = nfex.getMessage();
			causeMessage = causeMessage.replaceAll("For input string", "value");
			String errorMessage = "Error parsing number when importing CSV line " + importLine;
			if (!fieldImported.getHidden()) {
				errorMessage += ", field '" + fieldImported + "'";
			}
			errorMessage += "': " + causeMessage;
			throw new InputRecordException(errorMessage, fieldImported);
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		this.logLastDataChangeTime(request);
		UsageLogger usageLogger = new UsageLogger(this.dataSource);
		usageLogger.logDataChange(loggedInUser, table, AppAction.CSV_IMPORT, -1, ""
				+ numImportedRecords + " records imported");
		UsageLogger.startLoggingThread(usageLogger);
		return numImportedRecords;
	}

	/**
	 * Set the sequence generator for the ID column of a table to start at the
	 * next number above the highest value in the database. This ensures that
	 * record inserts will work
	 */
	private static void resetSequence(SequenceField sequenceField, Connection conn)
			throws SQLException {
		TableInfo table = sequenceField.getTableContainingField();
		// Find the max value
		String SQLCode = "SELECT MAX(" + sequenceField.getInternalFieldName() + ") FROM "
				+ table.getInternalTableName();
		PreparedStatement statement = conn.prepareStatement(SQLCode);
		ResultSet results = statement.executeQuery();
		Integer maxValue = null;
		if (results.next()) {
			maxValue = results.getInt(1) + 1;
		} else {
			throw new SQLException("Can't get max. sequence number for field " + sequenceField);
		}
		results.close();
		statement.close();
		SQLCode = "ALTER SEQUENCE " + table.getInternalTableName() + "_"
				+ sequenceField.getInternalFieldName() + "_seq";
		SQLCode += " RESTART WITH " + maxValue;
		statement = conn.prepareStatement(SQLCode);
		statement.execute();
		statement.close();
	}

	/**
	 * Upload a file for a particular field in a particular record. If a file
	 * with the same name already exists, rename the old one to avoid
	 * overwriting. Reset the last modified time of the old one so the rename
	 * doesn't muck this up. Maintaining the file timestamp is useful for
	 * version history
	 */
	private void uploadFile(HttpServletRequest request, FileField field, FileValue fileValue,
			int rowId, List<FileItem> multipartItems) throws CantDoThatException,
			FileUploadException {
		if (rowId < 0) {
			throw new CantDoThatException("Row ID " + rowId + " invalid");
		}
		if (!FileUpload.isMultipartContent(new ServletRequestContext(request))) {
			throw new CantDoThatException(
					"To upload a file, the form must be posted as multi-part form data");
		}
		// Put the file in a unique folder per row ID.
		// This is in the format table ID / field ID / row ID
		String uploadFolderName = this.webAppRoot + "uploads/"
				+ field.getTableContainingField().getInternalTableName() + "/"
				+ field.getInternalFieldName() + "/" + rowId;
		File uploadFolder = new File(uploadFolderName);
		if (!uploadFolder.exists()) {
			if (!uploadFolder.mkdirs()) {
				throw new CantDoThatException("Error creating upload folder " + uploadFolderName);
			}
		}
		for (FileItem item : multipartItems) {
			// if item is a file
			if (!item.isFormField()) {
				if (item.getSize() == 0) {
					throw new CantDoThatException("An empty file was submitted, no upload done");
				}
				if (fileValue.toString().contains("/")) {
					throw new CantDoThatException(
							"Filename contains a slash character which is not allowed, no uploda done");
				}
				String filePath = uploadFolderName + "/" + fileValue.toString();
				File selectedFile = new File(filePath);
				if (selectedFile.exists()) {
					// rename the existing file to something else so we don't
					// overwrite it
					String basePath = filePath;
					int fileNum = 1;
					String extension = "";
					if (basePath.contains(".")) {
						extension = basePath.replaceAll("^.*\\.", "");
						basePath = basePath
								.substring(0, basePath.length() - extension.length() - 1);
					}
					String renamedFileName = basePath + "-" + fileNum;
					if (!extension.equals("")) {
						renamedFileName += "." + extension;
					}
					File renamedFile = new File(renamedFileName);
					while (renamedFile.exists()) {
						fileNum++;
						renamedFileName = basePath + "-" + fileNum;
						if (!extension.equals("")) {
							renamedFileName += "." + extension;
						}
						renamedFile = new File(renamedFileName);
					}
					// Keep the original timestamp
					long lastModified = selectedFile.lastModified();
					if (!selectedFile.renameTo(renamedFile)) {
						throw new FileUploadException("Rename of existing file from '"
								+ selectedFile + "' to '" + renamedFile + "' failed");
					}
					if (!renamedFile.setLastModified(lastModified)) {
						throw new FileUploadException("Error setting the last modified date of "
								+ renamedFile);
					}
					// I think a File object's name is inviolable but just in
					// case
					selectedFile = new File(filePath);
				}
				try {
					item.write(selectedFile);
				} catch (Exception ex) {
					throw new FileUploadException("Error writing file: " + ex.getMessage());
				}
			}
		}
	}

	/**
	 * Used when deleting a record.
	 * 
	 * Recursively get any tables which contain data that depends on the record
	 * specified, i.e. data that would be deleted in a cascade if the record
	 * were deleted
	 * 
	 * Also throw an exception immediately if any locked records are found as
	 * this means the deletion should fail
	 */
	private static Set<TableInfo> getTablesWithDependentRecords(Connection conn,
			HttpServletRequest request, SessionDataInfo sessionData, DatabaseInfo databaseDefn,
			TableInfo table, int rowId, Set<TableInfo> tablesWithDependentRecords)
			throws SQLException, ObjectNotFoundException, CantDoThatException {
		CompanyInfo company = databaseDefn.getAuthManager().getCompanyForLoggedInUser(request);
		Set<TableInfo> tables = company.getTables();
		for (TableInfo otherTable : tables) {
			for (BaseField field : otherTable.getFields()) {
				if (!(field instanceof RelationField)) {
					continue;
				}
				RelationField relationField = (RelationField) field;
				if (!(relationField.getRelatedTable().equals(table))) {
					continue;
				}
				// 'otherTable' contains a RelationField referencing 'table' we
				// are deleting a record from.
				// We should check for any records that will be lost from
				// 'otherTable' from cascade delete.
				BaseField otherPrimaryKey = otherTable.getPrimaryKey();
				String SQLCode = "SELECT " + otherPrimaryKey.getInternalFieldName() + " FROM "
						+ otherTable.getInternalTableName() + " WHERE "
						+ relationField.getInternalFieldName() + "=?";
				PreparedStatement statement = conn.prepareStatement(SQLCode);
				statement.setInt(1, rowId);
				ResultSet results = statement.executeQuery();
				while (results.next()) {
					int otherRowId = results.getInt(1);
					TableData otherTableData = new TableData(otherTable);
					if (otherTableData.isRecordLocked(conn, sessionData, otherRowId)) {
						throw new CantDoThatException("Record " + otherRowId
								+ " cannot be removed from dependent table " + otherTable
								+ " because it is locked");
					}
					// recurse
					if (!tablesWithDependentRecords.contains(otherTable)) {
						tablesWithDependentRecords.addAll(getTablesWithDependentRecords(conn,
								request, sessionData, databaseDefn, otherTable, otherRowId,
								tablesWithDependentRecords));
					}
					tablesWithDependentRecords.add(otherTable);
				}
				results.close();
				statement.close();
			}
		}
		return tablesWithDependentRecords;
	}

	public void removeRecord(HttpServletRequest request, SessionDataInfo sessionData,
			DatabaseInfo databaseDefn, TableInfo table, int rowId, boolean cascade)
			throws SQLException, ObjectNotFoundException, CodingErrorException,
			CantDoThatException, DisallowedException, DataDependencyException {
		Set<TableInfo> tablesWithDependentRecords = new LinkedHashSet<TableInfo>();
		Connection conn = null;
		try {
			conn = this.dataSource.getConnection();
			conn.setAutoCommit(false);
			tablesWithDependentRecords = getTablesWithDependentRecords(conn, request, sessionData,
					databaseDefn, table, rowId, tablesWithDependentRecords);
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		// check for cascading deletes:
		if (tablesWithDependentRecords.size() > 0) {
			// check if user has permissions on all dependent tables:
			for (TableInfo dependentTable : tablesWithDependentRecords) {
				if (dependentTable.equals(table))
					continue;
				if (this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
						PrivilegeType.EDIT_TABLE_DATA, dependentTable))
					continue;
				if (this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
						PrivilegeType.MANAGE_TABLE, dependentTable))
					continue;
				throw new CantDoThatException( // This should probably really
						// be a DisallowedException
						"Unable to delete record as you are not permitted to delete from the dependent table "
								+ dependentTable);
			}
			// user has permissions on all dependent data. however, deletion
			// should only continue if the user has opted to cascade deletion
			if (!cascade) {
				// String dependentTables = "";
				// for (TableInfo dependentTable : tablesWithDependentRecords) {
				// dependentTables += dependentTable.getTableName() + "\n";
				// }
				throw new DataDependencyException(
						"Record has dependent data stored within the following tables:\n"
								+ tablesWithDependentRecords);
			}
		}

		String SQLCode = "DELETE FROM " + table.getInternalTableName() + " WHERE "
				+ table.getPrimaryKey().getInternalFieldName() + "=?";
		try {
			conn = this.dataSource.getConnection();
			conn.setAutoCommit(false);
			PreparedStatement statement = conn.prepareStatement(SQLCode);
			TableDataInfo tableData = new TableData(table);
			if (tableData.isRecordLocked(conn, sessionData, rowId)) {
				throw new CantDoThatException("Record " + rowId + " can't be removed from " + table
						+ " because it is locked");
			}
			statement.setInt(1, rowId);
			int numRowsDeleted = statement.executeUpdate();
			statement.close();
			// Make sure we don't delete more than one row
			if (numRowsDeleted > 1) {
				conn.rollback();
				throw new ObjectNotFoundException(String.valueOf(numRowsDeleted)
						+ " records deleted for row id " + String.valueOf(rowId));
			} else {
				conn.commit();
			}
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		this.logLastDataChangeTime(request);
		UsageLogger usageLogger = new UsageLogger(dataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logDataChange(user, table, AppAction.REMOVE_RECORD, rowId, "");
		UsageLogger.startLoggingThread(usageLogger);
	}

	public List<DataRowInfo> getReportDataRows(CompanyInfo company, BaseReportInfo reportDefn,
			Map<BaseField, String> filterValues, boolean exactFilters,
			Map<BaseField, Boolean> sessionSorts, int rowLimit) throws SQLException,
			CodingErrorException, CantDoThatException {
		Connection conn = null;
		ReportDataInfo reportData = null;
		List<DataRowInfo> reportDataRows = null;
		boolean useCaching = (company != null);
		try {
			conn = this.dataSource.getConnection();
			conn.setAutoCommit(false);
			synchronized (this.cachedReportDatas) {
				if (this.cachedReportDatas.containsKey(reportDefn)) {
					reportData = this.cachedReportDatas.get(reportDefn);
					if (useCaching) {
						Long companyDataLastChangedTime = this.getLastDataChangeTime(company);
						boolean dataChangedAfterCached = (companyDataLastChangedTime > reportData
								.getCacheCreationTime());
						boolean exceededCacheTime = reportData.exceededCacheTime();
						if (dataChangedAfterCached && exceededCacheTime) {
							boolean useSample = false;
							if (reportDefn.getRowCount() > 1000) {
								useSample = true;
							}
							reportData = new ReportData(conn, reportDefn, useCaching, useSample);
							this.cachedReportDatas.put(reportDefn, reportData);
							this.reportDataCacheMisses += 1;
						} else {
							this.reportDataCacheHits += 1;
						}
					}
				} else {
					reportData = new ReportData(conn, reportDefn, useCaching, false);
					if (useCaching) {
						this.cachedReportDatas.put(reportDefn, reportData);
						this.reportDataCacheMisses += 1;
					}
				}
				if ((this.reportDataCacheHits + this.reportDataCacheMisses) > 1000) {
					logger.info("Report data cache hits = " + this.reportDataCacheHits
							+ ", misses = " + this.reportDataCacheMisses);
					this.reportDataCacheHits = 0;
					this.reportDataCacheMisses = 0;
				}
			}
			reportDataRows = reportData.getReportDataRows(conn, filterValues, exactFilters,
					sessionSorts, rowLimit);
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		return reportDataRows;
	}

	public String getReportDataText(BaseReportInfo reportDefn, Set<BaseField> textFields,
			int rowLimit) throws SQLException {
		String SQLCode = "SELECT ";
		for (BaseField textField : textFields) {
			SQLCode += "lower(" + textField.getInternalFieldName() + "), ";
		}
		SQLCode = SQLCode.substring(0, SQLCode.length() - 2);
		SQLCode += " FROM " + reportDefn.getInternalReportName();
		SQLCode += " LIMIT " + rowLimit;
		StringBuilder conglomoratedText = new StringBuilder(8192);
		Connection conn = null;
		try {
			conn = this.dataSource.getConnection();
			conn.setAutoCommit(false);
			Statement statement = conn.createStatement();
			ResultSet results = statement.executeQuery(SQLCode);
			int colNum = 0;
			int numCols = textFields.size();
			while (results.next()) {
				for (colNum = 1; colNum <= numCols; colNum++) {
					String colString = results.getString(colNum);
					if (!results.wasNull()) {
						conglomoratedText.append(results.getString(colNum)).append(" ");
					}
				}
			}
			results.close();
			statement.close();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		return conglomoratedText.toString();
	}

	public boolean isRowIdInReport(BaseReportInfo report, int rowId) throws SQLException {
		Connection conn = null;
		try {
			conn = this.dataSource.getConnection();
			ReportDataInfo reportData = new ReportData(conn, report, false, false);
			return reportData.isRowIdInReport(conn, rowId);
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	public Map<RelationField, List<DataRow>> getChildDataTableRows(DatabaseInfo databaseDefn,
			TableInfo tableDefn, int rowid, HttpServletRequest request) throws SQLException,
			ObjectNotFoundException, CodingErrorException {
		Connection conn = null;
		Map<RelationField, List<DataRow>> childDataTableRows = null;
		try {
			conn = this.dataSource.getConnection();
			conn.setAutoCommit(false);
			// retrieve the related sets of records:
			DataRow tableRow = new DataRow(tableDefn, rowid, conn);
			childDataTableRows = tableRow.getChildDataRows(databaseDefn, conn, request);
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		if (childDataTableRows == null) {
			return new HashMap<RelationField, List<DataRow>>();
		} else {
			return childDataTableRows;
		}
	}

	private static Map<String, String> getKeyToDisplayMapping(Connection conn,
			String internalSourceName, String internalKeyFieldName, String internalDisplayFieldName)
			throws SQLException {
		// Buffer the set of display values for this field:
		String SQLCode = "SELECT " + internalKeyFieldName + ", " + internalDisplayFieldName;
		SQLCode += " FROM " + internalSourceName;
		PreparedStatement statement = conn.prepareStatement(SQLCode);
		ResultSet results = statement.executeQuery();
		Map<String, String> displayLookup = new HashMap<String, String>();
		while (results.next()) {
			displayLookup.put(results.getString(internalKeyFieldName), results
					.getString(internalDisplayFieldName));
		}
		return displayLookup;
	}

	public ReportSummaryDataInfo getReportSummaryData(CompanyInfo company,
			ReportSummaryInfo reportSummary, Map<BaseField, String> reportFilterValues, boolean alwaysUseCache)
			throws SQLException, CantDoThatException {
		boolean needSummary = (reportSummary.getAggregateFunctions().size() > 0);
		if (!needSummary) {
			return null;
		}
		ReportSummaryDataInfo reportSummaryData = this.getCachedReportSummaryData(reportSummary);
		if (reportSummaryData == null) {
			reportSummaryData = this.fetchReportSummaryData(reportSummary, reportFilterValues,
					reportSummaryData);
			this.addCachedReportSummaryData(reportSummary, reportSummaryData);
		} else {
			// Work out whether any filters are active that could affect the returned data
			ReportDataInfo reportData = new ReportData(null, reportSummary.getReport(), false, false);
			Map<String, List<ReportQuickFilterInfo>> whereClauseMap = reportData.getWhereClause(
					reportFilterValues, false);
			// If asked to always use the cache if possible, use it unless there are filters active
			if (alwaysUseCache && whereClauseMap.size() == 0) {
				return reportSummaryData;
			}
			long lastDataChangeTime = this.getLastDataChangeTime(company);
			long lastSchemaChangeTime = this.getLastSchemaChangeTime(company);
			long cacheCreationTime = reportSummaryData.getCacheCreationTime();
			if ((cacheCreationTime <= lastDataChangeTime) || (cacheCreationTime <= lastSchemaChangeTime)) {
				reportSummaryData = this.fetchReportSummaryData(reportSummary, reportFilterValues,
						reportSummaryData);
				this.addCachedReportSummaryData(reportSummary, reportSummaryData);
			}
		}
		return reportSummaryData;
	}

	/**
	 * Fetch direct from the database
	 */
	private ReportSummaryDataInfo fetchReportSummaryData(ReportSummaryInfo reportSummary,
			Map<BaseField, String> reportFilterValues, ReportSummaryDataInfo reportSummaryData)
			throws CantDoThatException, SQLException {
		Set<ReportSummaryAggregateInfo> aggregateFunctions = reportSummary.getAggregateFunctions();
		Set<ReportSummaryGroupingInfo> groupings = reportSummary.getGroupings();
		List<ReportSummaryDataRowInfo> reportSummaryRows;
		reportSummaryRows = new LinkedList<ReportSummaryDataRowInfo>();
		Connection conn = null;
		try {
			conn = this.dataSource.getConnection();
			conn.setAutoCommit(false);
			// First, cache the set of display values for relation fields
			Map<ReportFieldInfo, Map<String, String>> displayLookups = new HashMap<ReportFieldInfo, Map<String, String>>();
			for (ReportSummaryGroupingInfo grouping : groupings) {
				ReportFieldInfo groupingReportField = grouping.getGroupingReportField();
				BaseField baseField = groupingReportField.getBaseField();
				if (baseField instanceof RelationField) {
					String relatedKey = ((RelationField) baseField).getRelatedField()
							.getInternalFieldName();
					String relatedDisplay = ((RelationField) baseField).getDisplayField()
							.getInternalFieldName();
					String relatedSource = ((RelationField) baseField).getRelatedTable()
							.getInternalTableName();
					Map<String, String> displayLookup = getKeyToDisplayMapping(conn, relatedSource,
							relatedKey, relatedDisplay);
					displayLookups.put(groupingReportField, displayLookup);
				}
			}
			// Create some maps to store min. and max. values of each
			// aggregate column
			// These numbers can be used e.g. to scale values when charting
			// summary data
			Map<ReportSummaryAggregateInfo, Number> maxAggValues = new HashMap<ReportSummaryAggregateInfo, Number>();
			Map<ReportSummaryAggregateInfo, Number> minAggValues = new HashMap<ReportSummaryAggregateInfo, Number>();
			Map<ReportSummaryAggregateInfo, Number> grandTotals = new HashMap<ReportSummaryAggregateInfo, Number>();
			// Also a map for working with in the loop
			Map<ReportFieldInfo, Date> previousDateValues = new HashMap<ReportFieldInfo, Date>();
			Calendar calendar = Calendar.getInstance();
			// Get database data
			PreparedStatement statement = reportSummary.getReportSummarySqlPreparedStatement(conn,
					reportFilterValues, false);
			long startTime = System.currentTimeMillis();
			ResultSet summaryResults = statement.executeQuery();
			while (summaryResults.next()) {
				ReportSummaryDataRowInfo resultRow = new ReportSummaryDataRow();
				int resultColumn = 0;
				for (ReportSummaryGroupingInfo grouping : groupings) {
					ReportFieldInfo groupingReportField = grouping.getGroupingReportField();
					SummaryGroupingModifier groupingModifier = grouping.getGroupingModifier();
					BaseField baseField = groupingReportField.getBaseField();
					resultColumn++;
					String value = "";
					DatabaseFieldType dbType = baseField.getDbType();
					if (baseField instanceof RelationField) {
						value = summaryResults.getString(resultColumn);
						Map<String, String> displayLookup = displayLookups.get(groupingReportField);
						value = displayLookup.get(value);
					} else if (dbType.equals(DatabaseFieldType.TIMESTAMP)) {
						if (groupingModifier != null) {
							value = summaryResults.getString(resultColumn);
						} else {
							Date dbValue = summaryResults.getTimestamp(resultColumn);
							if (dbValue != null) {
								if (groupingReportField instanceof ReportCalcFieldInfo) {
									// See DateFieldDefn constructor for
									// format
									// explanation
									value = ((ReportCalcFieldInfo) groupingReportField)
											.formatDate(dbValue);
								} else {
									DateField dateField = (DateField) baseField;
									value = (dateField.formatDate(dbValue));
									if (Integer.valueOf(dateField.getDateResolution()).equals(
											Calendar.DAY_OF_MONTH)) {
										Date previousDbValue = previousDateValues
												.get(groupingReportField);
										if (previousDbValue != null) {
											calendar.setTime(previousDbValue);
											int previousDayOfYear = calendar
													.get(Calendar.DAY_OF_YEAR);
											calendar.setTime(dbValue);
											int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
											int difference = Math
													.abs(dayOfYear - previousDayOfYear);
											if (difference > 1) {
												value += " (" + (difference - 1) + " day gap)";
											}
										}
										previousDateValues.put(groupingReportField, dbValue);
									}
								}
							}
						}
					} else if (dbType.equals(DatabaseFieldType.FLOAT)) {
						double floatValue = summaryResults.getDouble(resultColumn);
						if (baseField instanceof DecimalField) {
							value = ((DecimalField) baseField).formatFloat(floatValue);
						} else if (groupingReportField instanceof ReportCalcFieldInfo) {
							value = ((ReportCalcFieldInfo) groupingReportField)
									.formatFloat(floatValue);
						} else {
							value = summaryResults.getString(resultColumn);
						}
					} else if (dbType.equals(DatabaseFieldType.BOOLEAN)) {
						if (summaryResults.getBoolean(resultColumn)) {
							value = "true";
						} else {
							value = "false";
						}
					} else {
						value = summaryResults.getString(resultColumn);
					}
					resultRow.addGroupingValue(grouping, value);
				}
				for (ReportSummaryAggregateInfo aggregateFunction : aggregateFunctions) {
					resultColumn++;
					DatabaseFieldType dbType = aggregateFunction.getReportField().getBaseField()
							.getDbType();
					Double value = null;
					// deal with aggregate results which are timestamps
					// rather than doubles
					if ((!aggregateFunction.getAggregateFunction().equals(AggregateFunction.COUNT))
							&& (dbType.equals(DatabaseFieldType.TIMESTAMP))) {
						java.sql.Timestamp timestampValue = summaryResults
								.getTimestamp(resultColumn);
						if (timestampValue != null) {
							Long longValue = timestampValue.getTime();
							value = longValue.doubleValue();
						}
					} else {
						value = summaryResults.getDouble(resultColumn);
					}
					if (value != null) {
						int precision = 1;
						ReportFieldInfo aggReportField = aggregateFunction.getReportField();
						if (aggReportField instanceof ReportCalcFieldInfo) {
							DatabaseFieldType dbFieldType = ((ReportCalcFieldInfo) aggReportField)
									.getDbType();
							if (dbFieldType.equals(DatabaseFieldType.FLOAT)) {
								precision = ((ReportCalcFieldInfo) aggReportField)
										.getDecimalPrecision();
							}
						} else if (aggReportField.getBaseField() instanceof DecimalField) {
							precision = ((DecimalField) aggReportField.getBaseField())
									.getPrecision();
						}
						Number currentGrandTotal = grandTotals.get(aggregateFunction);
						if (currentGrandTotal == null) {
							currentGrandTotal = new Double(0);
						}
						double currentGrandTotalDbl = currentGrandTotal.doubleValue() + value;
						grandTotals.put(aggregateFunction, Double.valueOf(currentGrandTotalDbl));
						value = MathUtils.round(value, precision);
						resultRow.addAggregateValue(aggregateFunction, value);
						Number currentMin = minAggValues.get(aggregateFunction);
						Number currentMax = maxAggValues.get(aggregateFunction);
						if (currentMin == null) {
							minAggValues.put(aggregateFunction, value);
						} else if (value.doubleValue() < currentMin.doubleValue()) {
							minAggValues.put(aggregateFunction, value);
						}
						if (currentMax == null) {
							maxAggValues.put(aggregateFunction, value);
						} else if (value.doubleValue() > currentMax.doubleValue()) {
							maxAggValues.put(aggregateFunction, value);
						}
					}
				}
				reportSummaryRows.add(resultRow);
			}
			summaryResults.close();
			statement.close();
			float durationSecs = (System.currentTimeMillis() - startTime) / ((float) 1000);
			if (durationSecs > AppProperties.longSqlTime) {
				logger.warn("Long SELECT SQL execution time of " + durationSecs
						+ " seconds for summary '" + reportSummary + "', statement = " + statement);
			}
			reportSummaryData = new ReportSummaryData(reportSummaryRows, minAggValues,
					maxAggValues, grandTotals);
		} catch (SQLException sqlex) {
			throw new SQLException("Error getting report summary data: " + sqlex);
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		return reportSummaryData;
	}

	public Map<BaseField, BaseValue> getTableDataRow(TableInfo table, int rowId)
			throws SQLException, ObjectNotFoundException, CantDoThatException, CodingErrorException {
		Connection conn = null;
		TableDataInfo tableData = new TableData(table);
		Map<BaseField, BaseValue> tableDataRow = null;
		try {
			conn = this.dataSource.getConnection();
			conn.setAutoCommit(false);
			tableDataRow = tableData.getTableDataRow(conn, rowId);
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		if (tableDataRow == null) {
			return new HashMap<BaseField, BaseValue>();
		} else {
			return tableDataRow;
		}
	}

	public Set<Integer> getRelatedRowIds(BaseReportInfo masterReport, int masterRowId,
			TableInfo relatedTable) throws CantDoThatException, SQLException {
		if (!masterReport.getReportBaseFields().contains(relatedTable.getPrimaryKey())) {
			throw new CantDoThatException("Field " + relatedTable.getPrimaryKey()
					+ " not found in report " + masterReport);
		}
		Connection conn = null;
		Set<Integer> relatedRowIds = new LinkedHashSet<Integer>();
		try {
			conn = this.dataSource.getConnection();
			conn.setAutoCommit(false);
			String SQLCode = "SELECT " + relatedTable.getPrimaryKey().getInternalFieldName();
			SQLCode += " FROM " + masterReport.getInternalReportName();
			SQLCode += " WHERE " + masterReport.getInternalReportName() + "."
					+ masterReport.getParentTable().getPrimaryKey().getInternalFieldName() + "=?";
			PreparedStatement statement = conn.prepareStatement(SQLCode);
			statement.setInt(1, masterRowId);
			ResultSet results = statement.executeQuery();
			while (results.next()) {
				relatedRowIds.add(results.getInt(1));
			}
			results.close();
			statement.close();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		return relatedRowIds;
	}

	public void anonymiseData(TableInfo table, HttpServletRequest request,
			SessionDataInfo sessionData, Map<BaseField, FieldContentType> fieldContentTypes,
			List<FileItem> multipartItems) throws SQLException, CodingErrorException,
			CantDoThatException, InputRecordException, ObjectNotFoundException, DisallowedException {
		Random randomGenerator = new Random();
		// Get data we're going to anonymise
		List<DataRowInfo> dataRows = this.getReportDataRows(null, table.getDefaultReport(),
				new HashMap<BaseField, String>(), false, new HashMap<BaseField, Boolean>(), -1);
		// Build up list of names
		List<String> forenames = new ArrayList<String>(100);
		List<String> surnames = new ArrayList<String>(100);
		List<String> emailAddresses = new ArrayList<String>(100);
		List<String> phoneNumbers = new ArrayList<String>();
		List<String> niNumbers = new ArrayList<String>();
		for (DataRowInfo dataRow : dataRows) {
			for (BaseField field : fieldContentTypes.keySet()) {
				FieldContentType contentType = fieldContentTypes.get(field);
				if (contentType.equals(FieldContentType.FULL_NAME)) {
					String fullName = dataRow.getDataRowFields().get(field).getKeyValue();
					String surname = fullName.replaceAll("^.*\\s", "");
					String forename = fullName.substring(0, fullName.length() - surname.length());
					forenames.add(forename);
					surnames.add(surname);
				} else if (contentType.equals(FieldContentType.PHONE_NUMBER)) {
					String existingPhoneNumber = dataRow.getDataRowFields().get(field)
							.getKeyValue();
					if (existingPhoneNumber.length() > 0) {
						StringBuffer phoneNumber = new StringBuffer("01632 ");
						for (int i = 0; i < 6; i++) {
							phoneNumber.append(randomGenerator.nextInt(10));
						}
						phoneNumbers.add(phoneNumber.toString());
					} else {
						phoneNumbers.add("");
					}
				} else if (contentType.equals(FieldContentType.NI_NUMBER)) {
					String existingNINumber = dataRow.getDataRowFields().get(field).getKeyValue();
					String[] alphabet = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
							"L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y",
							"Z" };
					if (existingNINumber.length() > 0) {
						StringBuffer niNumber = new StringBuffer();
						niNumber.append(alphabet[randomGenerator.nextInt(26)]);
						niNumber.append(alphabet[randomGenerator.nextInt(26)]);
						niNumber.append(randomGenerator.nextInt(10));
						niNumber.append(randomGenerator.nextInt(10));
						niNumber.append(randomGenerator.nextInt(10));
						niNumber.append(randomGenerator.nextInt(10));
						niNumber.append(randomGenerator.nextInt(10));
						niNumber.append(randomGenerator.nextInt(10));
						niNumber.append(alphabet[randomGenerator.nextInt(26)]);
						niNumbers.add(niNumber.toString());
					} else {
						niNumbers.add("");
					}
				} else if (contentType.equals(FieldContentType.EMAIL_ADDRESS)) {
					String originalEmail = dataRow.getDataRowFields().get(field).getKeyValue();
					if (originalEmail.contains("@")) {
						emailAddresses.add("email.address@agilebase.co.uk");
					} else {
						emailAddresses.add("");
					}
				}
			}
		}
		// Anonymise
		for (DataRowInfo dataRow : dataRows) {
			int rowId = dataRow.getRowId();
			LinkedHashMap<BaseField, BaseValue> dataToSave = new LinkedHashMap<BaseField, BaseValue>();
			for (BaseField field : fieldContentTypes.keySet()) {
				FieldContentType contentType = fieldContentTypes.get(field);
				if (contentType.equals(FieldContentType.FULL_NAME)) {
					int forenameIndex = randomGenerator.nextInt(forenames.size());
					String randomForename = forenames.get(forenameIndex);
					int surnameIndex = randomGenerator.nextInt(surnames.size());
					String randomSurname = surnames.get(surnameIndex);
					TextValue fullNameValue = new TextValueDefn(randomForename + randomSurname);
					dataToSave.put(field, fullNameValue);
				} else if (contentType.equals(FieldContentType.PHONE_NUMBER)) {
					int phoneIndex = randomGenerator.nextInt(phoneNumbers.size());
					TextValue phoneNumber = new TextValueDefn(phoneNumbers.get(phoneIndex));
					dataToSave.put(field, phoneNumber);
				} else if (contentType.equals(FieldContentType.NI_NUMBER)) {
					int niIndex = randomGenerator.nextInt(niNumbers.size());
					TextValue niNumber = new TextValueDefn(niNumbers.get(niIndex));
					dataToSave.put(field, niNumber);
				} else if (contentType.equals(FieldContentType.EMAIL_ADDRESS)) {
					int emailIndex = randomGenerator.nextInt(emailAddresses.size());
					TextValue emailValue = new TextValueDefn(emailAddresses.get(emailIndex));
					dataToSave.put(field, emailValue);
				} else {
					int dataRowIndex = randomGenerator.nextInt(dataRows.size());
					DataRowInfo randomDataRow = dataRows.get(dataRowIndex);
					String randomKey = randomDataRow.getDataRowFields().get(field).getKeyValue();
					// only know how to mix up text and relation fields so far
					if (field instanceof TextField) {
						TextValue textValue = new TextValueDefn(randomKey);
						dataToSave.put(field, textValue);
					} else if (field instanceof RelationField) {
						IntegerValue intValue = new IntegerValueDefn(Integer.valueOf(randomKey));
						dataToSave.put(field, intValue);
					}
				}
			}
			this.saveRecord(request, table, dataToSave, false, rowId, sessionData, multipartItems);
		}
	}

	public String toString() {
		return "DataManagement is a class for managing data (duh!)";
	}

	public void logLastDataChangeTime(HttpServletRequest request) throws ObjectNotFoundException {
		CompanyInfo company = this.authManager.getCompanyForLoggedInUser(request);
		this.setLastDataChangeTime(company);
	}

	public void logLastSchemaChangeTime(HttpServletRequest request) throws ObjectNotFoundException {
		CompanyInfo company = this.authManager.getCompanyForLoggedInUser(request);
		this.setLastSchemaChangeTime(company);
	}

	private synchronized Long getLastDataChangeTime(CompanyInfo company) {
		if (this.lastDataChangeTimes.get(company) == null) {
			this.setLastDataChangeTime(company);
		}
		return this.lastDataChangeTimes.get(company);
	}

	private synchronized Long getLastSchemaChangeTime(CompanyInfo company) {
		if (this.lastSchemaChangeTimes.get(company) == null) {
			this.setLastSchemaChangeTime(company);
		}
		return this.lastSchemaChangeTimes.get(company);
	}

	/**
	 * Acts similarly to logLastChangeTime but taking a company object directly
	 * instead of a HTTP request
	 * 
	 * @see #logLastChangeTime(HttpServletRequest)
	 */
	private synchronized void setLastDataChangeTime(CompanyInfo company) {
		this.lastDataChangeTimes.put(company, System.currentTimeMillis());
	}

	private synchronized void setLastSchemaChangeTime(CompanyInfo company) {
		this.lastSchemaChangeTimes.put(company, System.currentTimeMillis());
	}

	private synchronized ReportSummaryDataInfo getCachedReportSummaryData(ReportSummaryInfo summary) {
		return this.cachedReportSummaryDatas.get(summary);
	}

	private synchronized void addCachedReportSummaryData(ReportSummaryInfo summary,
			ReportSummaryDataInfo summaryData) {
		this.cachedReportSummaryDatas.put(summary, summaryData);
	}

	/**
	 * Stores a cache of some info about report data: the mean and std. dev. of
	 * each numeric field in the report
	 */
	private Map<BaseReportInfo, ReportDataInfo> cachedReportDatas = new HashMap<BaseReportInfo, ReportDataInfo>();

	private Map<ReportSummaryInfo, ReportSummaryDataInfo> cachedReportSummaryDatas = new HashMap<ReportSummaryInfo, ReportSummaryDataInfo>();

	private DataSource dataSource;

	private String webAppRoot;

	private AuthManagerInfo authManager;

	/**
	 * Keep a record of the last time any schema or data change occurred for
	 * each company, to help inform caching
	 */
	private Map<CompanyInfo, Long> lastDataChangeTimes = new HashMap<CompanyInfo, Long>();

	private Map<CompanyInfo, Long> lastSchemaChangeTimes = new HashMap<CompanyInfo, Long>();

	private int reportDataCacheHits = 0;

	private int reportDataCacheMisses = 0;

	private static final SimpleLogger logger = new SimpleLogger(DataManagement.class);

}