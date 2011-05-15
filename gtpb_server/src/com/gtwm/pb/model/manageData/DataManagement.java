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
package com.gtwm.pb.model.manageData;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Calendar;
import java.util.TreeMap;
import java.io.File;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.math.util.MathUtils;
import com.gtwm.pb.auth.PrivilegeType;
import com.gtwm.pb.auth.DisallowedException;
import com.gtwm.pb.model.interfaces.AuthManagerInfo;
import com.gtwm.pb.model.interfaces.AppUserInfo;
import com.gtwm.pb.model.interfaces.CachedReportFeedInfo;
import com.gtwm.pb.model.interfaces.CompanyInfo;
import com.gtwm.pb.model.interfaces.DataRowFieldInfo;
import com.gtwm.pb.model.interfaces.ReportCalcFieldInfo;
import com.gtwm.pb.model.interfaces.ReportQuickFilterInfo;
import com.gtwm.pb.model.interfaces.ChartGroupingInfo;
import com.gtwm.pb.model.interfaces.SessionDataInfo;
import com.gtwm.pb.model.interfaces.DataManagementInfo;
import com.gtwm.pb.model.interfaces.DatabaseInfo;
import com.gtwm.pb.model.interfaces.ReportDataInfo;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.ChartAggregateInfo;
import com.gtwm.pb.model.interfaces.ChartDataInfo;
import com.gtwm.pb.model.interfaces.ChartDataRowInfo;
import com.gtwm.pb.model.interfaces.ChartInfo;
import com.gtwm.pb.model.interfaces.ReportFieldInfo;
import com.gtwm.pb.model.interfaces.DataRowInfo;
import com.gtwm.pb.model.interfaces.TableDataInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.fields.BaseValue;
import com.gtwm.pb.model.interfaces.fields.CheckboxValue;
import com.gtwm.pb.model.interfaces.fields.DateField;
import com.gtwm.pb.model.interfaces.fields.ReferencedReportDataField;
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
import com.gtwm.pb.servlets.ServletUtilMethods;
import com.gtwm.pb.util.AppProperties;
import com.gtwm.pb.util.DataDependencyException;
import com.gtwm.pb.util.Enumerations.DataFormat;
import com.gtwm.pb.util.Helpers;
import com.gtwm.pb.util.MissingParametersException;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.Enumerations.AggregateFunction;
import com.gtwm.pb.util.Enumerations.DatabaseFieldType;
import com.gtwm.pb.util.Enumerations.FieldContentType;
import com.gtwm.pb.util.Enumerations.HiddenFields;
import com.gtwm.pb.util.Enumerations.AppAction;
import com.gtwm.pb.util.Enumerations.SummaryGroupingModifier;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.commons.io.FileUtils;
import org.grlea.log.SimpleLogger;
import org.glowacki.CalendarParser;
import org.glowacki.CalendarParserException;
import org.json.JSONException;
import org.json.JSONStringer;
import au.com.bytecode.opencsv.CSVReader;

public final class DataManagement implements DataManagementInfo {

	private DataManagement() {
		this.webAppRoot = null;
		this.dataSource = null;
		this.authManager = null;
	}

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
			CodingErrorException, DisallowedException, CantDoThatException,
			MissingParametersException {
		// editing a single record, pass in one row id
		Set<Integer> rowIds = new HashSet<Integer>();
		rowIds.add(rowId);
		this.saveRecord(request, table, dataToSave, newRecord, rowIds, sessionData, multipartItems);
	}

	public int globalEdit(HttpServletRequest request, TableInfo table,
			LinkedHashMap<BaseField, BaseValue> dataToSave, SessionDataInfo sessionData,
			List<FileItem> multipartItems) throws InputRecordException, ObjectNotFoundException,
			SQLException, CodingErrorException, CantDoThatException, DisallowedException,
			MissingParametersException {
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
			CodingErrorException, InputRecordException, DisallowedException,
			MissingParametersException {
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
					&& !(field instanceof ReferencedReportDataField) && !(field.getUnique())) { // &&
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
			throws CantDoThatException, ObjectNotFoundException, DisallowedException,
			MissingParametersException {
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
					fieldValue = this.getCurrentUserValue(request);
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
				fieldValue = this.getCurrentUserValue(request);
			}
			if (fieldValue != null) {
				// by design, the user should never be able to send a request
				// to update the username/date hidden fields as this would
				// put a hole in the auditing functionality
				dataToSave.put(field, fieldValue);
			}
		}
	}

	private BaseValue getCurrentUserValue(HttpServletRequest request)
			throws MissingParametersException, ObjectNotFoundException, DisallowedException {
		BaseValue fieldValue;
		String userName = request.getRemoteUser();
		AppUserInfo currentUser = null;
		if (userName == null) {
			currentUser = ServletUtilMethods.getPublicUserForRequest(request,
					this.authManager.getAuthenticator());
		} else {
			currentUser = this.authManager.getUserByUserName(request, userName);
		}
		String fullname = currentUser.getForename() + " " + currentUser.getSurname();
		fullname += " (" + currentUser.getUserName() + ")";
		fieldValue = new TextValueDefn(fullname);
		return fieldValue;
	}

	/**
	 * Used by both the public saveRecord and globalEdit methods
	 */
	private void saveRecord(HttpServletRequest request, TableInfo table,
			LinkedHashMap<BaseField, BaseValue> dataToSave, boolean newRecord, Set<Integer> rowIds,
			SessionDataInfo sessionData, List<FileItem> multipartItems)
			throws InputRecordException, ObjectNotFoundException, SQLException,
			CantDoThatException, CodingErrorException, DisallowedException,
			MissingParametersException {
		if ((dataToSave.size() == 0) && (!newRecord)) {
			// Note: this does actually happen quite a lot, from two particular
			// users.
			// Haven't tracked down the cause but it doesn't seem to be creating
			// a problem.
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
								&& (((IntegerValue) fieldValue).getValueInteger() == -1)
								|| (fieldValue.isNull())) {
							statement.setNull(fieldNumber, Types.NULL);
						} else {
							statement.setInt(fieldNumber,
									((IntegerValue) fieldValue).getValueInteger());
						}
					} else if (fieldValue instanceof DurationValue) {
						statement.setString(fieldNumber,
								((DurationValue) fieldValue).getSqlFormatInterval());
					} else if (fieldValue instanceof DecimalValue) {
						statement.setDouble(fieldNumber,
								((DecimalValue) fieldValue).getValueFloat());
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
						statement.setBoolean(fieldNumber,
								((CheckboxValue) fieldValue).getValueBoolean());
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
								+ Helpers.replaceInternalNames(errorMessage,
										table.getDefaultReport()) + ")";
					}
					throw new InputRecordException(errorMessage, possibleCauseField, sqlex);
				}
			}
			// Not able to find field
			errorMessage = Helpers.replaceInternalNames(errorMessage, table.getDefaultReport());
			throw new InputRecordException(errorMessage, null, sqlex);
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
						this.uploadFile(request, (FileField) field,
								(FileValue) dataToSave.get(field), newRowId, multipartItems);
					} else {
						this.uploadFile(request, (FileField) field,
								(FileValue) dataToSave.get(field), rowId, multipartItems);
					}
				} catch (CantDoThatException cdtex) {
					throw new InputRecordException("Error uploading file: " + cdtex.getMessage(),
							field, cdtex);
				} catch (FileUploadException fuex) {
					throw new InputRecordException("Error uploading file: " + fuex.getMessage(),
							field, fuex);
				}
			}
		}
		if (newRecord) {
			sessionData.setRowId(newRowId);
		}
		this.logLastDataChangeTime(request);
		UsageLogger usageLogger = new UsageLogger(this.dataSource);
		AppUserInfo user = null;
		if (request.getRemoteUser() == null) {
			user = ServletUtilMethods.getPublicUserForRequest(request,
					this.authManager.getAuthenticator());
		} else {
			user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		}
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
			usageLogger.logDataChange(user, table, AppAction.SAVE_NEW_RECORD, newRowId,
					dataToLog.toString());
		} else if (globalEdit) {
			// TODO: need better logging of global edits
			usageLogger.logDataChange(user, table, AppAction.GLOBAL_EDIT, rowId,
					dataToLog.toString());
		} else {
			usageLogger.logDataChange(user, table, AppAction.UPDATE_RECORD, rowId,
					dataToLog.toString());
		}
		UsageLogger.startLoggingThread(usageLogger);
	}

	public int importCSV(HttpServletRequest request, TableInfo table,
			boolean updateExistingRecords, BaseField recordIdentifierField, boolean generateRowIds,
			char separator, char quotechar, int numHeaderLines, boolean useRelationDisplayValues,
			boolean importSequenceValues, boolean requireExactRelationMatches, boolean trim,
			boolean merge, List<FileItem> multipartItems, String csvContent) throws SQLException,
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
			} else if (field instanceof SeparatorField
					|| field instanceof ReferencedReportDataField) {
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
		String logCreationSQLCode = null;
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
				if (merge) {
					// Update database only if there's a non-null value from the
					// spreadsheet
					updateSQLCode += field.getInternalFieldName() + " = COALESCE(?,"
							+ field.getInternalFieldName() + "), ";
				} else {
					updateSQLCode += field.getInternalFieldName() + " = ?, ";
				}
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
			logCreationSQLCode = "UPDATE "
					+ table.getInternalTableName()
					+ " SET "
					+ table.getField(HiddenFields.DATE_CREATED.getFieldName())
							.getInternalFieldName() + "=?, "
					+ table.getField(HiddenFields.CREATED_BY.getFieldName()).getInternalFieldName()
					+ "=? WHERE " + primaryKey.getInternalFieldName() + "=?";
		}
		insertSQLCode = "INSERT INTO " + table.getInternalTableName() + "(";
		String placeholders = "";
		for (BaseField field : fields) {
			insertSQLCode += field.getInternalFieldName() + ", ";
			placeholders += "?, ";
		}
		placeholders = placeholders.substring(0, placeholders.length() - 2);
		insertSQLCode = insertSQLCode.substring(0, insertSQLCode.length() - 2) + ") VALUES ("
				+ placeholders + ")";
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
		// backupInsertStatement is for when an update returns 0 rows affected,
		// i.e. there's no matching row. In this case, do an insert
		PreparedStatement backupInsertStatement = null;
		PreparedStatement logCreationStatement = null;
		// These two variables used in exception handling
		int importLine = 0;
		BaseField fieldImported = null;
		Timestamp importTime = new Timestamp(System.currentTimeMillis());
		AppUserInfo loggedInUser = authManager.getUserByUserName(request, request.getRemoteUser());
		String fullname = loggedInUser.getForename() + " " + loggedInUser.getSurname() + " ("
				+ loggedInUser.getUserName() + ")";
		try {
			conn = this.dataSource.getConnection();
			conn.setAutoCommit(false);
			if (updateExistingRecords) {
				statement = conn.prepareStatement(updateSQLCode);
				backupInsertStatement = conn.prepareStatement(insertSQLCode);
				logCreationStatement = conn.prepareStatement(logCreationSQLCode);
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
							if (updateExistingRecords) {
								backupInsertStatement.setBoolean(fieldNum, false);
							}
						} else if (fieldName.equals(HiddenFields.DATE_CREATED.getFieldName())
								|| fieldName.equals(HiddenFields.LAST_MODIFIED.getFieldName())) {
							statement.setTimestamp(fieldNum, importTime);
							if (updateExistingRecords) {
								backupInsertStatement.setTimestamp(fieldNum, importTime);
							}
						} else if (fieldName.equals(HiddenFields.CREATED_BY.getFieldName())
								|| fieldName.equals(HiddenFields.MODIFIED_BY.getFieldName())) {
							statement.setString(fieldNum, fullname);
							if (updateExistingRecords) {
								backupInsertStatement.setString(fieldNum, fullname);
							}
						}
					} else if (fieldNum > lineValues.size()) {
						// booleans have a not null constraint
						if (field.getDbType().equals(Types.BOOLEAN)) {
							statement.setBoolean(fieldNum, false);
							if (updateExistingRecords) {
								backupInsertStatement.setBoolean(fieldNum, false);
							}
						} else {
							statement.setNull(fieldNum, Types.NULL);
							if (updateExistingRecords) {
								backupInsertStatement.setNull(fieldNum, Types.NULL);
							}
						}
					} else {
						String lineValue = lineValues.get(fieldNum - 1);
						if (lineValue != null) {
							if (trim) {
								lineValue = lineValue.trim();
							}
							if (lineValue.equals("")) {
								// booleans have a not null constraint
								if (field.getDbType().equals(Types.BOOLEAN)) {
									statement.setBoolean(fieldNum, false);
									if (updateExistingRecords) {
										backupInsertStatement.setBoolean(fieldNum, false);
									}
								} else {
									statement.setNull(fieldNum, Types.NULL);
									if (updateExistingRecords) {
										backupInsertStatement.setNull(fieldNum, Types.NULL);
									}
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
									if (updateExistingRecords) {
										backupInsertStatement.setString(fieldNum, lineValue);
										if (field.equals(recordIdentifierField)) {
											recordIdentifierString = lineValue;
										}
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
										statement.setTimestamp(fieldNum,
												new Timestamp(calValue.getTimeInMillis()));
										if (updateExistingRecords) {
											backupInsertStatement.setTimestamp(fieldNum,
													new Timestamp(calValue.getTimeInMillis()));
										}
									} catch (CalendarParserException cpex) {
										throw new InputRecordException("Error importing line "
												+ importLine + ", field " + field + ": "
												+ cpex.getMessage(), field, cpex);
									}
									break;
								case FLOAT:
									lineValue = lineValue.trim()
											.replaceAll("[^\\d\\.\\+\\-eE]", "");
									statement.setDouble(fieldNum, Double.valueOf(lineValue));
									if (updateExistingRecords) {
										backupInsertStatement.setDouble(fieldNum,
												Double.valueOf(lineValue));
									}
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
										if (updateExistingRecords) {
											backupInsertStatement.setInt(fieldNum, keyValue);
											if (field.equals(recordIdentifierField)) {
												recordIdentifierInteger = keyValue;
											}
										}
									} else {
										lineValue = lineValue.trim().replaceAll(
												"[^\\d\\.\\+\\-eE]", "");
										int keyValue = Integer.valueOf(lineValue);
										statement.setInt(fieldNum, keyValue);
										if (updateExistingRecords) {
											backupInsertStatement.setInt(fieldNum, keyValue);
											if (field.equals(recordIdentifierField)) {
												recordIdentifierInteger = keyValue;
											}
										}
									}
									break;
								case SERIAL:
									lineValue = lineValue.trim()
											.replaceAll("[^\\d\\.\\+\\-eE]", "");
									int keyValue = Integer.valueOf(lineValue);
									statement.setInt(fieldNum, keyValue);
									if (updateExistingRecords) {
										backupInsertStatement.setInt(fieldNum, keyValue);
										if (field.equals(recordIdentifierField)) {
											recordIdentifierInteger = keyValue;
										}
									}
									break;
								case BOOLEAN:
									boolean filterValueIsTrue = Helpers
											.valueRepresentsBooleanTrue(lineValue);
									statement.setBoolean(fieldNum, filterValueIsTrue);
									if (updateExistingRecords) {
										backupInsertStatement.setBoolean(fieldNum,
												filterValueIsTrue);
									}
									break;
								}
							}
						} else {
							// booleans have a not null constraint
							if (field.getDbType().equals(Types.BOOLEAN)) {
								statement.setBoolean(fieldNum, false);
								if (updateExistingRecords) {
									backupInsertStatement.setBoolean(fieldNum, false);
								}
							} else {
								statement.setNull(fieldNum, Types.NULL);
								if (updateExistingRecords) {
									backupInsertStatement.setNull(fieldNum, Types.NULL);
								}
							}
						}
					}
				}
				if (updateExistingRecords) {
					// for potential error messages
					String recordIdentifierDescription = null;
					if (identifierFieldDbType.equals(DatabaseFieldType.INTEGER)
							|| identifierFieldDbType.equals(DatabaseFieldType.SERIAL)) {
						if (recordIdentifierInteger == null) {
							throw new InputRecordException(
									"Can't find a record identifier value at line " + importLine,
									recordIdentifierField);
						}
						recordIdentifierDescription = recordIdentifierField.getFieldName() + " = "
								+ recordIdentifierInteger;
						// Set the 'WHERE recordIdentifier = ?' clause
						statement.setInt(fields.size() + 1, recordIdentifierInteger);
					} else {
						if (recordIdentifierString == null) {
							throw new InputRecordException(
									"Can't find a record identifier value at line " + importLine,
									recordIdentifierField);
						}
						recordIdentifierDescription = recordIdentifierField.getFieldName() + " = '"
								+ recordIdentifierString + "'";
						// Set the 'WHERE recordIdentifier = ?' clause
						statement.setString(fields.size() + 1, recordIdentifierString);
					}
					int rowsAffected = statement.executeUpdate();
					if (rowsAffected == 0) {
						// If can't find a match to update, insert a record
						// instead
						backupInsertStatement.executeUpdate();
						// NB Postgres specific code to find Row ID of newly
						// inserted record, not cross-db compatible
						String newRowIdSQLCode = "SELECT currval('" + table.getInternalTableName()
								+ "_" + primaryKey.getInternalFieldName() + "_seq')";
						PreparedStatement newRowIdStatement = conn
								.prepareStatement(newRowIdSQLCode);
						ResultSet newRowIdResults = newRowIdStatement.executeQuery();
						if (newRowIdResults.next()) {
							int newRowId = newRowIdResults.getInt(1);
							// Add creation metadata to the new row
							logCreationStatement.setTimestamp(1, importTime);
							logCreationStatement.setString(2, fullname);
							logCreationStatement.setInt(3, newRowId);
							int creationLogRowsAffected = logCreationStatement.executeUpdate();
							if (creationLogRowsAffected == 0) {
								throw new SQLException(
										"Unable to update creation metadata of newly inserted record, using query "
												+ logCreationStatement);
							}
						} else {
							newRowIdResults.close();
							newRowIdStatement.close();
							throw new SQLException(
									"Row ID not found for the newly inserted record. '"
											+ newRowIdStatement + "' didn't work");
						}
						newRowIdResults.close();
						newRowIdStatement.close();
					} else if (rowsAffected > 1) {
						throw new InputRecordException(
								"Error importing line "
										+ importLine
										+ ". The record identifier field "
										+ recordIdentifierDescription
										+ " should match only 1 record in the database but it actually matches "
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
			if (backupInsertStatement != null) {
				backupInsertStatement.close();
			}
			if (logCreationStatement != null) {
				logCreationStatement.close();
			}
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
			String databaseErrorMessage = Helpers.replaceInternalNames(sqlex.getMessage(),
					table.getDefaultReport());
			logger.warn("Import failed, statement is " + statement);
			logger.warn("Backup insert statement is " + backupInsertStatement);
			String errorMessage = "Error importing CSV line " + importLine;
			if (!fieldImported.getHidden()) {
				errorMessage += ", field '" + fieldImported + "'";
			}
			errorMessage += ": " + databaseErrorMessage;
			throw new InputRecordException(errorMessage, fieldImported, sqlex);
		} catch (NumberFormatException nfex) {
			String causeMessage = nfex.getMessage();
			causeMessage = causeMessage.replaceAll("For input string", "value");
			String errorMessage = "Error parsing number when importing CSV line " + importLine;
			if (!fieldImported.getHidden()) {
				errorMessage += ", field '" + fieldImported + "'";
			}
			errorMessage += ": " + causeMessage;
			throw new InputRecordException(errorMessage, fieldImported, nfex);
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		this.logLastDataChangeTime(request);
		UsageLogger usageLogger = new UsageLogger(this.dataSource);
		String logMessage = "" + numImportedRecords;
		if (updateExistingRecords) {
			logMessage += " records imported";
		} else {
			logMessage += " new records imported";
		}
		if (csvContent != null) {
			logMessage += " from file";
		}
		usageLogger.logDataChange(loggedInUser, table, AppAction.CSV_IMPORT, -1, logMessage);
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
		String uploadFolderName = this.getWebAppRoot() + "uploads/"
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
				long fileSize = item.getSize();
				if (fileSize == 0) {
					throw new CantDoThatException("An empty file was submitted, no upload done");
				}
				if (fileValue.toString().contains("/")) {
					throw new CantDoThatException(
							"Filename contains a slash character which is not allowed, no upload done");
				}
				String filePath = uploadFolderName + "/" + fileValue.toString();
				File selectedFile = new File(filePath);
				String extension = "";
				if (filePath.contains(".")) {
					extension = filePath.replaceAll("^.*\\.", "").toLowerCase();
				}
				if (selectedFile.exists()) {
					// rename the existing file to something else so we don't
					// overwrite it
					String basePath = filePath;
					int fileNum = 1;
					if (basePath.contains(".")) {
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
					// TODO: catching a general exception?!
					throw new FileUploadException("Error writing file: " + ex.getMessage());
				}
				// Record upload speed
				long requestStartTime = request.getSession().getLastAccessedTime();
				float secondsToUpload = (System.currentTimeMillis() - requestStartTime)
						/ ((float) 1000);
				if (secondsToUpload > 10) {
					// Only time reasonably long uploads otherwise we'll amplify
					// errors
					float uploadSpeed = ((float) fileSize) / secondsToUpload;
					this.updateUploadSpeed(uploadSpeed);
				}
				if (extension.equals("jpg") || extension.equals("jpeg")
						|| extension.equals("png")) {
					// image.png -> image.png.40.png
					String thumb40Path = filePath + "." + 40 + "." + extension;
					String thumb500Path = filePath + "." + 500 + "." + extension;
					File thumb40File = new File(thumb40Path);
					File thumb500File = new File(thumb500Path);
					try {
						BufferedImage originalImage = ImageIO.read(selectedFile);
						int height = originalImage.getHeight();
						int width = originalImage.getWidth();
						// Conditional resize
						if ((height > 500) || (width > 500)) {
							Thumbnails.of(selectedFile).size(500, 500).toFile(thumb500File);
						} else {
							FileUtils.copyFile(selectedFile, thumb500File);
						}
						Thumbnails.of(selectedFile).size(40, 40).toFile(thumb40File);
					} catch (IOException ioex) {
						throw new FileUploadException("Error generating thumbnail: "
								+ ioex.getMessage());
					}
				}
			}
		}
	}

	public synchronized int getUploadSpeed() {
		return (int) this.uploadSpeed;
	}

	private synchronized void updateUploadSpeed(float uploadSpeed) {
		this.uploadSpeed = (this.uploadSpeed + uploadSpeed) / 2;
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

	private String getReportDataAsFormat(DataFormat dataFormat, AppUserInfo user,
			BaseReportInfo report, int cacheMinutes) throws CodingErrorException,
			CantDoThatException, SQLException, JSONException, XMLStreamException,
			ObjectNotFoundException {
		String id = dataFormat.toString() + report.getInternalReportName();
		CachedReportFeedInfo cachedFeed = this.cachedReportFeeds.get(id);
		if (cachedFeed != null) {
			long lastDataChangeAge = System.currentTimeMillis()
					- this.getLastDataChangeTime(user.getCompany());
			long cacheAge = cachedFeed.getCacheAge();
			if ((cacheAge < lastDataChangeAge) || (cacheAge < (cacheMinutes * 60 * 1000))) {
				this.reportFeedCacheHits.incrementAndGet();
				return cachedFeed.getFeed();
			}
		}
		int numRows = 10000;
		if (dataFormat.equals(DataFormat.RSS)) {
			numRows = 100;
		}
		List<DataRowInfo> reportDataRows = this.getReportDataRows(user.getCompany(), report,
				new HashMap<BaseField, String>(0), false, new HashMap<BaseField, Boolean>(0),
				numRows);
		String dataFeedString = null;
		if (dataFormat.equals(DataFormat.JSON)) {
			dataFeedString = this.generateJSON(report, reportDataRows);
		} else if (dataFormat.equals(DataFormat.RSS)) {
			dataFeedString = this.generateRSS(user, report, reportDataRows);
		} else {
			throw new CodingErrorException("Format " + dataFormat + " has no report generator");
		}
		UsageLogger usageLogger = new UsageLogger(this.dataSource);
		usageLogger.logReportView(user, report, new HashMap<BaseField, String>(), 10000,
				dataFormat.toString());
		UsageLogger.startLoggingThread(usageLogger);
		cachedFeed = new CachedFeed(dataFeedString);
		this.cachedReportFeeds.put(id, cachedFeed);
		int cacheMisses = this.reportFeedCacheMisses.incrementAndGet();
		if (cacheMisses > 100) {
			logger.info("Public report data cache hits: " + this.reportFeedCacheHits + ", misses "
					+ cacheMisses);
			this.reportFeedCacheHits.set(0);
			this.reportFeedCacheMisses.set(0);
		}
		return dataFeedString;
	}

	public String getReportRSS(AppUserInfo user, BaseReportInfo report, int cacheMinutes)
			throws SQLException, CodingErrorException, CantDoThatException, JSONException,
			XMLStreamException, ObjectNotFoundException {
		return this.getReportDataAsFormat(DataFormat.RSS, user, report, cacheMinutes);
	}

	public String getReportJSON(AppUserInfo user, BaseReportInfo report, int cacheMinutes)
			throws JSONException, CodingErrorException, CantDoThatException, SQLException,
			XMLStreamException, ObjectNotFoundException {
		return this.getReportDataAsFormat(DataFormat.JSON, user, report, cacheMinutes);
	}

	/**
	 * Based on http://www.vogella.de/articles/RSSFeed/article.html
	 */
	private String generateRSS(AppUserInfo user, BaseReportInfo report,
			List<DataRowInfo> reportDataRows) throws XMLStreamException, ObjectNotFoundException {
		// Create a XMLOutputFactory
		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		// Create XMLEventWriter
		StringWriter stringWriter = new StringWriter();
		XMLEventWriter eventWriter = outputFactory.createXMLEventWriter(stringWriter);
		// Create a EventFactory
		XMLEventFactory eventFactory = XMLEventFactory.newInstance();
		XMLEvent end = eventFactory.createDTD("\n");
		// Create and write Start Tag
		StartDocument startDocument = eventFactory.createStartDocument();
		eventWriter.add(startDocument);
		// Create open tag
		eventWriter.add(end);
		StartElement rssStart = eventFactory.createStartElement("", "", "rss");
		eventWriter.add(rssStart);
		eventWriter.add(eventFactory.createAttribute("version", "2.0"));
		eventWriter.add(end);
		eventWriter.add(eventFactory.createStartElement("", "", "channel"));
		eventWriter.add(end);
		// Write the different nodes
		this.createNode(eventWriter, "title",
				report.getModule().getModuleName() + " - " + report.getReportName());
		// TODO: Don't hard code host part of URL
		String reportLink = "https://appserver.gtportalbase.com/agileBase/AppController.servlet?return=gui/display_application&set_table="
				+ report.getParentTable().getInternalTableName()
				+ "&set_report="
				+ report.getInternalReportName();
		this.createNode(eventWriter, "link", reportLink);
		this.createNode(eventWriter, "description", "A live data feed from www.agilebase.co.uk");
		DateFormat dateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
		Date lastDataChangeDate = new Date(this.getLastDataChangeTime(user.getCompany()));
		this.createNode(eventWriter, "pubdate", dateFormatter.format(lastDataChangeDate));
		for (DataRowInfo reportDataRow : reportDataRows) {
			eventWriter.add(eventFactory.createStartElement("", "", "item"));
			eventWriter.add(end);
			this.createNode(eventWriter, "title", buildEventTitle(report, reportDataRow, false));
			this.createNode(eventWriter, "description", reportDataRow.toString());
			String rowLink = reportLink + "&set_row_id=" + reportDataRow.getRowId();
			this.createNode(eventWriter, "link", rowLink);
			this.createNode(eventWriter, "guid", rowLink);
			eventWriter.add(end);
			eventWriter.add(eventFactory.createEndElement("", "", "item"));
			eventWriter.add(end);
		}
		eventWriter.add(eventFactory.createEndElement("","","channel"));
		eventWriter.add(end);
		eventWriter.add(eventFactory.createEndElement("","","rss"));
		eventWriter.add(end);
		return stringWriter.toString();
	}

	private void createNode(XMLEventWriter eventWriter, String name, String value)
			throws XMLStreamException {
		XMLEventFactory eventFactory = XMLEventFactory.newInstance();
		XMLEvent end = eventFactory.createDTD("\n");
		XMLEvent tab = eventFactory.createDTD("\t");
		// Create Start node
		StartElement sElement = eventFactory.createStartElement("", "", name);
		eventWriter.add(tab);
		eventWriter.add(sElement);
		// Create Content
		Characters characters = eventFactory.createCharacters(value);
		eventWriter.add(characters);
		// Create End node
		EndElement eElement = eventFactory.createEndElement("", "", name);
		eventWriter.add(eElement);
		eventWriter.add(end);
	}

	private String generateJSON(BaseReportInfo report, List<DataRowInfo> reportDataRows)
			throws JSONException {
		JSONStringer js = new JSONStringer();
		js.array();
		for (DataRowInfo reportDataRow : reportDataRows) {
			js.object();
			js.key("rowId").value(reportDataRow.getRowId());
			// TODO: could potentially do a more complex JSON object if the need
			// arises,
			// with e.g. an array of fields and additional properties such as
			// field names
			for (ReportFieldInfo reportField : report.getReportFields()) {
				DataRowFieldInfo value = reportDataRow.getValue(reportField);
				js.key(reportField.getInternalFieldName()).value(value.toString());
			}
			js.endObject();
		}
		js.endArray();
		String json = js.toString();
		return json;
	}

	public String getReportCalendarJSON(DataFormat format, AppUserInfo user, BaseReportInfo report,
			Map<BaseField, String> filterValues, Long startEpoch, Long endEpoch)
			throws CodingErrorException, CantDoThatException, SQLException, JSONException {
		// RFC 2822 date format used by Simile timeline
		DateFormat dateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
		ReportFieldInfo eventDateReportField = report.getCalendarField();
		if (eventDateReportField == null) {
			throw new CantDoThatException("The report '" + report + "' has no suitable date field");
		}
		// Try cache first
		// Make a sortedMap so toString is always consistent for the same
		// key/value pairs and we can use it as an ID
		SortedMap<BaseField, String> sortedFilterValues = new TreeMap<BaseField, String>(
				filterValues);
		String id = report.getInternalReportName() + sortedFilterValues.toString();
		CachedReportFeedInfo cachedJSON = this.cachedCalendarJSONs.get(id);
		if (cachedJSON != null) {
			this.calendarJsonCacheHits.incrementAndGet();
			// Note: if we choose not to invalidate the cache on every data
			// change, we could return only JSON that was newer than a certain
			// time here, say the last data change time plus ten secs
			return cachedJSON.getFeed();
		}
		String dateFieldInternalName = eventDateReportField.getInternalFieldName();
		int dateResolution = 0;
		if (eventDateReportField instanceof ReportCalcFieldInfo) {
			dateResolution = ((ReportCalcFieldInfo) eventDateReportField).getDateResolution();
		} else {
			BaseField eventDateBaseField = eventDateReportField.getBaseField();
			DateField eventDateField = (DateField) eventDateBaseField;
			dateResolution = eventDateField.getDateResolution();
		}
		boolean allDayValues = true;
		if (dateResolution > Calendar.DAY_OF_MONTH) {
			allDayValues = false;
		}
		List<DataRowInfo> reportDataRows = this.getReportDataRows(user.getCompany(), report,
				filterValues, false, new HashMap<BaseField, Boolean>(), 10000);
		JSONStringer js = new JSONStringer();
		if (format.equals(DataFormat.JSON_TIMELINE)) {
			js.object();
		}
		if (format.equals(DataFormat.JSON_TIMELINE)) {
			js.key("events");
		}
		js.array();
		String internalReportName = report.getInternalReportName();
		String internalTableName = report.getParentTable().getInternalTableName();
		ROWS_LOOP: for (DataRowInfo reportDataRow : reportDataRows) {
			DataRowFieldInfo eventDateValue = reportDataRow.getValue(eventDateReportField);
			if (eventDateValue.getKeyValue().equals("")) {
				continue ROWS_LOOP;
			}
			js.object();
			js.key("id").value(internalReportName + "_" + reportDataRow.getRowId());
			js.key("internalTableName").value(internalTableName);
			js.key("rowId").value(String.valueOf(reportDataRow.getRowId()));
			boolean allDayEvent = allDayValues;
			if (!allDayValues) {
				String eventDateDisplayValue = eventDateValue.getDisplayValue();
				// TODO: trim may not be necessary
				if (eventDateDisplayValue.trim().endsWith("00:00")) {
					allDayEvent = true;
				}
			}
			js.key("allDay").value(allDayEvent);
			if (format.equals(DataFormat.JSON_TIMELINE)) {
				// timeline needs formatted dates
				Long eventDateEpoch = Long.parseLong(eventDateValue.getKeyValue());
				String formattedDate = dateFormatter.format(new Date(eventDateEpoch));
				js.key("start").value(formattedDate);
			} else {
				// fullcalendar needs the number of seconds since the epoch
				Long eventDateEpoch = Long.parseLong(eventDateValue.getKeyValue()) / 1000;
				js.key("start").value(eventDateEpoch);
				if (!allDayEvent) {
					js.key("end").value(eventDateEpoch + 7200); // events last
																// 2hrs
				}
			}
			js.key("className").value("report_" + internalReportName); // fullcalendar
			js.key("classname").value("report_" + internalReportName); // timeline
			js.key("dateFieldInternalName").value(dateFieldInternalName);
			String eventTitle = buildEventTitle(report, reportDataRow, false);
			if (format.equals(DataFormat.JSON_TIMELINE)) {
				js.key("caption").value(eventTitle);
				// TODO: build short title from long title, don't rebuild from
				// scratch. Just cut off everything after the 5th comma for
				// example
				String shortTitle = buildEventTitle(report, reportDataRow, true);
				js.key("title").value(shortTitle);
			} else {
				js.key("title").value(eventTitle);
			}
			js.endObject();
		}
		js.endArray();
		if (format.equals(DataFormat.JSON_TIMELINE)) {
			js.endObject();
		}
		UsageLogger usageLogger = new UsageLogger(this.dataSource);
		usageLogger.logReportView(user, report, filterValues, 10000, "getCalendarJSON");
		UsageLogger.startLoggingThread(usageLogger);
		String json = js.toString();
		cachedJSON = new CachedFeed(json);
		this.cachedCalendarJSONs.put(id, cachedJSON);
		int cacheMisses = this.calendarJsonCacheMisses.incrementAndGet();
		if (cacheMisses > 100) {
			logger.info("JSON cache hits: " + this.calendarJsonCacheHits + ", misses "
					+ cacheMisses);
			this.calendarJsonCacheHits.set(0);
			this.calendarJsonCacheMisses.set(0);
		}
		return json;
	}

	/**
	 * TODO: perhaps move this static method to a more appropriate class
	 * 
	 * @param shortTitle
	 *            If true, return only the first part of the title
	 */
	public static String buildEventTitle(BaseReportInfo report, DataRowInfo reportDataRow,
			boolean shortTitle) {
		// ignore any date fields other than the one used for specifying
		// the event date
		// ignore any blank fields
		// for numeric and boolean fields, include the field title
		StringBuilder eventTitleBuilder = new StringBuilder();
		int fieldCount = 0;
		REPORT_FIELD_LOOP: for (ReportFieldInfo reportField : report.getReportFields()) {
			BaseField baseField = reportField.getBaseField();
			DataRowFieldInfo dataRowField = reportDataRow.getValue(baseField);
			String displayValue = dataRowField.getDisplayValue();
			if (displayValue.equals("")) {
				continue REPORT_FIELD_LOOP;
			}
			if (baseField.getDbType().equals(DatabaseFieldType.TIMESTAMP)
					|| baseField.equals(baseField.getTableContainingField().getPrimaryKey())) {
				continue REPORT_FIELD_LOOP;
			}
			fieldCount++;
			if (shortTitle && (fieldCount > 5)) {
				break REPORT_FIELD_LOOP;
			}
			switch (baseField.getDbType()) {
			case BOOLEAN:
				boolean reportFieldTrue = Helpers.valueRepresentsBooleanTrue(dataRowField
						.getKeyValue());
				if (reportFieldTrue) {
					eventTitleBuilder.append(reportField.getFieldName() + ", ");
				}
				break;
			case INTEGER:
				eventTitleBuilder.append(reportField.getFieldName()).append(" = ")
						.append(displayValue);
				break;
			case FLOAT:
				eventTitleBuilder.append(reportField.getFieldName()).append(" = ")
						.append(displayValue);
				break;
			case SERIAL:
				eventTitleBuilder.append(reportField.getFieldName()).append(" = ")
						.append(dataRowField.getKeyValue());
				break;
			default:
				eventTitleBuilder.append(displayValue);
			}
			eventTitleBuilder.append(", ");
		}
		eventTitleBuilder.delete(eventTitleBuilder.length() - 2, eventTitleBuilder.length());
		String eventTitle = eventTitleBuilder.toString();
		return eventTitle;
	}

	public List<DataRowInfo> getReportDataRows(CompanyInfo company, BaseReportInfo reportDefn,
			Map<BaseField, String> filterValues, boolean exactFilters,
			Map<BaseField, Boolean> sessionSorts, int rowLimit) throws SQLException,
			CodingErrorException, CantDoThatException {
		Connection conn = null;
		List<DataRowInfo> reportDataRows = null;
		try {
			conn = this.dataSource.getConnection();
			conn.setAutoCommit(false);
			ReportDataInfo reportData = this.getReportData(company, reportDefn, conn, true);
			reportDataRows = reportData.getReportDataRows(conn, filterValues, exactFilters,
					sessionSorts, rowLimit);
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		return reportDataRows;
	}

	public ReportDataInfo getReportData(CompanyInfo company, BaseReportInfo report,
			boolean updateCacheIfObsolete) throws SQLException {
		Connection conn = null;
		try {
			conn = this.dataSource.getConnection();
			conn.setAutoCommit(false);
			return this.getReportData(company, report, conn, updateCacheIfObsolete);
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	private ReportDataInfo getReportData(CompanyInfo company, BaseReportInfo reportDefn,
			Connection conn, boolean updateCacheIfObsolete) throws SQLException {
		// If company specified: use the cache to look up report data. If the
		// report data isn't cached, cache it now
		// Additionally, if updateCacheIfObsolete specified, update the cache if
		// it gets out of date
		ReportDataInfo reportData;
		boolean useCaching = (company != null);
		if (this.cachedReportDatas.containsKey(reportDefn)) {
			reportData = this.cachedReportDatas.get(reportDefn);
			if (useCaching && updateCacheIfObsolete) {
				Long companyDataLastChangedTime = this.getLastDataChangeTime(company);
				boolean dataChangedAfterCached = (companyDataLastChangedTime > reportData
						.getCacheCreationTime());
				if (dataChangedAfterCached && reportData.exceededCacheTime()) {
					boolean useSample = false;
					if (reportDefn.getRowCount() > 1000) {
						useSample = true;
					}
					reportData = new ReportData(conn, reportDefn, useCaching, useSample);
					this.cachedReportDatas.put(reportDefn, reportData);
					this.reportDataCacheMisses.incrementAndGet();
				} else {
					this.reportDataCacheHits.incrementAndGet();
				}
			}
		} else {
			reportData = new ReportData(conn, reportDefn, useCaching, false);
			if (useCaching) {
				this.cachedReportDatas.put(reportDefn, reportData);
				this.reportDataCacheMisses.incrementAndGet();
			}
		}
		if ((this.reportDataCacheHits.get() + this.reportDataCacheMisses.get()) > 10000) {
			logger.info("Report data cache hits = " + this.reportDataCacheHits + ", misses = "
					+ this.reportDataCacheMisses);
			this.reportDataCacheHits.set(0);
			this.reportDataCacheMisses.set(0);
		}
		return reportData;
	}

	public String getReportDataText(BaseReportInfo reportDefn, Set<BaseField> textFields,
			Map<BaseField, String> reportFilterValues, int rowLimit) throws SQLException,
			CantDoThatException {
		StringBuilder conglomoratedText = new StringBuilder(8192);
		if (textFields.size() == 0) {
			throw new CantDoThatException(
					"One or more text fields must be supplied to get report data text");
		}
		Connection conn = null;
		try {
			conn = this.dataSource.getConnection();
			conn.setAutoCommit(false);
			ReportDataInfo reportData = new ReportData(conn, reportDefn, false, false);
			Map<String, List<ReportQuickFilterInfo>> whereClauseMap = reportData.getWhereClause(
					reportFilterValues, false);
			String filterArgs = null;
			List<ReportQuickFilterInfo> filtersUsed = null;
			// TODO: there is only one WHERE clause - there should be an
			// improvement
			// over a loop
			for (Map.Entry<String, List<ReportQuickFilterInfo>> whereClause : whereClauseMap
					.entrySet()) {
				filterArgs = whereClause.getKey();
				filtersUsed = whereClause.getValue();
			}
			String SQLCode = "SELECT ";
			for (BaseField textField : textFields) {
				SQLCode += "lower(" + textField.getInternalFieldName() + "), ";
			}
			SQLCode = SQLCode.substring(0, SQLCode.length() - 2);
			SQLCode += " FROM " + reportDefn.getInternalReportName();
			if (filterArgs.length() > 0) {
				SQLCode += " WHERE " + filterArgs;
			}
			SQLCode += " LIMIT " + rowLimit;
			PreparedStatement statement = conn.prepareStatement(SQLCode);
			statement = reportData.fillInFilterValues(filtersUsed, statement);
			ResultSet results = statement.executeQuery();
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
			displayLookup.put(results.getString(internalKeyFieldName),
					results.getString(internalDisplayFieldName));
		}
		return displayLookup;
	}

	public ChartDataInfo getChartData(CompanyInfo company, ChartInfo reportSummary,
			Map<BaseField, String> reportFilterValues, boolean alwaysUseCache) throws SQLException,
			CantDoThatException {
		boolean needSummary = (reportSummary.getAggregateFunctions().size() > 0);
		if (!needSummary) {
			return null;
		}
		// Work out whether any user filters are active that could affect the
		// returned data. If there are, always look up direct from db
		ReportDataInfo reportData = new ReportData(null, reportSummary.getReport(), false, false);
		Map<String, List<ReportQuickFilterInfo>> whereClauseMap = reportData.getWhereClause(
				reportFilterValues, false);
		if (whereClauseMap.size() > 0) {
			return this.fetchChartData(reportSummary, reportFilterValues);
		}
		ChartDataInfo reportSummaryData = this.cachedChartDatas.get(reportSummary);
		if (reportSummaryData == null) {
			reportSummaryData = this.fetchChartData(reportSummary, reportFilterValues);
			this.cachedChartDatas.put(reportSummary, reportSummaryData);
			this.chartDataCacheMisses.incrementAndGet();
		} else if (alwaysUseCache) {
			this.chartDataCacheHits.incrementAndGet();
		} else {
			long lastDataChangeTime = this.getLastDataChangeTime(company);
			long lastSchemaChangeTime = this.getLastSchemaChangeTime(company);
			long cacheCreationTime = reportSummaryData.getCacheCreationTime();
			if ((cacheCreationTime <= lastDataChangeTime)
					|| (cacheCreationTime <= lastSchemaChangeTime)) {
				reportSummaryData = this.fetchChartData(reportSummary, reportFilterValues);
				this.cachedChartDatas.put(reportSummary, reportSummaryData);
				this.chartDataCacheMisses.incrementAndGet();
			} else {
				this.chartDataCacheHits.incrementAndGet();
			}
		}
		if ((this.chartDataCacheHits.get() + this.chartDataCacheMisses.get()) > 100) {
			logger.info("Summary data cache hits = " + this.chartDataCacheHits + ", misses = "
					+ this.chartDataCacheMisses);
			this.chartDataCacheHits.set(0);
			this.chartDataCacheMisses.set(0);
		}
		return reportSummaryData;
	}

	/**
	 * Fetch direct from the database
	 */
	private ChartDataInfo fetchChartData(ChartInfo reportSummary,
			Map<BaseField, String> reportFilterValues) throws CantDoThatException, SQLException {
		Set<ChartAggregateInfo> aggregateFunctions = reportSummary.getAggregateFunctions();
		Set<ChartGroupingInfo> groupings = reportSummary.getGroupings();
		List<ChartDataRowInfo> reportSummaryRows;
		reportSummaryRows = new LinkedList<ChartDataRowInfo>();
		Connection conn = null;
		try {
			conn = this.dataSource.getConnection();
			conn.setAutoCommit(false);
			// First, cache the set of display values for relation fields
			Map<ReportFieldInfo, Map<String, String>> displayLookups = new HashMap<ReportFieldInfo, Map<String, String>>();
			for (ChartGroupingInfo grouping : groupings) {
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
			Map<ChartAggregateInfo, Number> maxAggValues = new HashMap<ChartAggregateInfo, Number>();
			Map<ChartAggregateInfo, Number> minAggValues = new HashMap<ChartAggregateInfo, Number>();
			Map<ChartAggregateInfo, Number> grandTotals = new HashMap<ChartAggregateInfo, Number>();
			// Also a map for working with in the loop
			Map<ReportFieldInfo, Date> previousDateValues = new HashMap<ReportFieldInfo, Date>();
			Calendar calendar = Calendar.getInstance();
			// Get database data
			PreparedStatement statement = reportSummary.getChartSqlPreparedStatement(conn,
					reportFilterValues, false);
			long startTime = System.currentTimeMillis();
			ResultSet summaryResults = statement.executeQuery();
			while (summaryResults.next()) {
				ChartDataRowInfo resultRow = new ChartDataRow();
				int resultColumn = 0;
				for (ChartGroupingInfo grouping : groupings) {
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
				for (ChartAggregateInfo aggregateFunction : aggregateFunctions) {
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
				logger.debug("Long SELECT SQL execution time of " + durationSecs
						+ " seconds for summary '" + reportSummary + "', statement = " + statement);
			}
			return new ChartData(reportSummaryRows, minAggValues, maxAggValues, grandTotals);
		} catch (SQLException sqlex) {
			throw new SQLException("Error getting report summary data " + reportSummary + ": "
					+ sqlex);
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
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

	public int getNextRowId(SessionDataInfo sessionData, BaseReportInfo report,
			boolean forwardSearch) throws SQLException, CantDoThatException {
		Map<BaseField, String> reportFilterValues = sessionData.getReportFilterValues();
		ReportDataInfo reportData = new ReportData(null, report, false, false);
		Map<String, List<ReportQuickFilterInfo>> whereClauseMap = reportData.getWhereClause(
				reportFilterValues, false);
		String filterArgs = null;
		List<ReportQuickFilterInfo> filtersUsed = null;
		for (Map.Entry<String, List<ReportQuickFilterInfo>> whereClause : whereClauseMap.entrySet()) {
			filterArgs = whereClause.getKey();
			filtersUsed = whereClause.getValue();
		}
		int currentRowId = sessionData.getRowId(report.getParentTable());
		int nextRowId = currentRowId;
		BaseField primaryKey = report.getParentTable().getPrimaryKey();
		String SQLCode = "SELECT ";
		if (forwardSearch) {
			SQLCode += "min(" + primaryKey.getInternalFieldName() + ") FROM (";
		} else {
			SQLCode += "max(" + primaryKey.getInternalFieldName() + ") FROM (";
		}
		// subquery
		SQLCode += "SELECT " + primaryKey.getInternalFieldName();
		SQLCode += " FROM " + report.getInternalReportName();
		if (filterArgs.length() > 0) {
			SQLCode += " WHERE " + filterArgs;
		}
		// end subquery
		SQLCode += " ) as rowids_q WHERE ";
		if (forwardSearch) {
			SQLCode += primaryKey.getInternalFieldName() + " > " + currentRowId;
		} else {
			SQLCode += primaryKey.getInternalFieldName() + " < " + currentRowId;
		}
		Connection conn = null;
		try {
			conn = this.dataSource.getConnection();
			conn.setAutoCommit(false);
			PreparedStatement statement = conn.prepareStatement(SQLCode);
			statement = reportData.fillInFilterValues(filtersUsed, statement);
			ResultSet results = statement.executeQuery();
			if (results.next()) {
				nextRowId = results.getInt(1);
				if (results.wasNull()) {
					nextRowId = currentRowId;
					// TODO: cycle round if no result found rather than stopping
				}
			}
			results.close();
			statement.close();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		return nextRowId;
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
			CantDoThatException, InputRecordException, ObjectNotFoundException,
			DisallowedException, MissingParametersException {
		Random randomGenerator = new Random();
		String[] alphabet = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N",
				"O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };
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
				} else if (contentType.equals(FieldContentType.CODE)) {
					int dataRowIndex = randomGenerator.nextInt(dataRows.size());
					String currentKey = dataRow.getDataRowFields().get(field).getKeyValue();
					if (currentKey != null) {
						int length = currentKey.length();
						StringBuilder code = new StringBuilder("");
						for (int i = 0; i < length; i++) {
							if (i < 3 || (i % 10 == 0)) {
								code.append(alphabet[randomGenerator.nextInt(26)]);
							} else {
								code.append(randomGenerator.nextInt(10));
							}
						}
						TextValue codeValue = new TextValueDefn(code.toString());
						dataToSave.put(field, codeValue);
					}
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
		// Public user (not logged in) changes don't count
		// TODO: think of something better
		if (request.getRemoteUser() != null) {
			CompanyInfo company = this.authManager.getCompanyForLoggedInUser(request);
			this.setLastDataChangeTime(company);
		}
	}

	public void logLastSchemaChangeTime(HttpServletRequest request) throws ObjectNotFoundException {
		CompanyInfo company = this.authManager.getCompanyForLoggedInUser(request);
		this.setLastSchemaChangeTime(company);
	}

	private Long getLastDataChangeTime(CompanyInfo company) {
		Long lastTime = this.lastDataChangeTimes.get(company);
		if (lastTime == null) {
			this.setLastDataChangeTime(company);
			return this.lastDataChangeTimes.get(company);
		}
		return lastTime;
	}

	private Long getLastSchemaChangeTime(CompanyInfo company) {
		Long lastTime = this.lastSchemaChangeTimes.get(company);
		if (lastTime == null) {
			this.setLastSchemaChangeTime(company);
			return this.lastSchemaChangeTimes.get(company);
		}
		return lastTime;
	}

	/**
	 * Acts similarly to logLastChangeTime but taking a company object directly
	 * instead of a HTTP request
	 * 
	 * @see #logLastDataChangeTime(HttpServletRequest)
	 */
	private void setLastDataChangeTime(CompanyInfo company) {
		this.lastDataChangeTimes.put(company, System.currentTimeMillis());
		// Note: clearing optional
		this.cachedCalendarJSONs.clear();
	}

	private void setLastSchemaChangeTime(CompanyInfo company) {
		this.lastSchemaChangeTimes.put(company, System.currentTimeMillis());
	}

	/**
	 * Stores a cache of some info about report data: the mean and std. dev. of
	 * each numeric field in the report
	 */
	private Map<BaseReportInfo, ReportDataInfo> cachedReportDatas = new ConcurrentHashMap<BaseReportInfo, ReportDataInfo>();

	private Map<ChartInfo, ChartDataInfo> cachedChartDatas = new ConcurrentHashMap<ChartInfo, ChartDataInfo>();

	private Map<String, CachedReportFeedInfo> cachedCalendarJSONs = new ConcurrentHashMap<String, CachedReportFeedInfo>();

	private Map<String, CachedReportFeedInfo> cachedReportFeeds = new ConcurrentHashMap<String, CachedReportFeedInfo>();

	private final DataSource dataSource;

	private final String webAppRoot;

	private final AuthManagerInfo authManager;

	/**
	 * Keep a record of the last time any schema or data change occurred for
	 * each company, to help inform caching
	 */
	private Map<CompanyInfo, Long> lastDataChangeTimes = new ConcurrentHashMap<CompanyInfo, Long>();

	private Map<CompanyInfo, Long> lastSchemaChangeTimes = new ConcurrentHashMap<CompanyInfo, Long>();

	private AtomicInteger reportDataCacheHits = new AtomicInteger();

	private AtomicInteger reportDataCacheMisses = new AtomicInteger();

	private AtomicInteger chartDataCacheHits = new AtomicInteger();

	private AtomicInteger chartDataCacheMisses = new AtomicInteger();

	private AtomicInteger calendarJsonCacheHits = new AtomicInteger();

	private AtomicInteger calendarJsonCacheMisses = new AtomicInteger();

	private AtomicInteger reportFeedCacheHits = new AtomicInteger();

	private AtomicInteger reportFeedCacheMisses = new AtomicInteger();

	private float uploadSpeed = 50000; // Default to 50KB per second

	private static final SimpleLogger logger = new SimpleLogger(DataManagement.class);

}