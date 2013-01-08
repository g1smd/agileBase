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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Arrays;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.io.File;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.math.util.MathUtils;
import com.gtwm.pb.auth.Authenticator;
import com.gtwm.pb.auth.PrivilegeType;
import com.gtwm.pb.auth.DisallowedException;
import com.gtwm.pb.model.interfaces.AuthManagerInfo;
import com.gtwm.pb.model.interfaces.AppUserInfo;
import com.gtwm.pb.model.interfaces.AuthenticatorInfo;
import com.gtwm.pb.model.interfaces.CachedReportFeedInfo;
import com.gtwm.pb.model.interfaces.CommentInfo;
import com.gtwm.pb.model.interfaces.CompanyInfo;
import com.gtwm.pb.model.interfaces.DataRowFieldInfo;
import com.gtwm.pb.model.interfaces.LocationDataRowFieldInfo;
import com.gtwm.pb.model.interfaces.ReportCalcFieldInfo;
import com.gtwm.pb.model.interfaces.ReportMapInfo;
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
import com.gtwm.pb.model.interfaces.fields.CommentFeedField;
import com.gtwm.pb.model.interfaces.fields.DateField;
import com.gtwm.pb.model.interfaces.fields.IntegerField;
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
import com.gtwm.pb.model.manageData.fields.DecimalValueDefn;
import com.gtwm.pb.model.manageData.fields.TextValueDefn;
import com.gtwm.pb.model.manageData.fields.IntegerValueDefn;
import com.gtwm.pb.model.manageData.fields.CheckboxValueDefn;
import com.gtwm.pb.model.manageSchema.FieldTypeDescriptor.FieldCategory;
import com.gtwm.pb.model.manageSchema.fields.RelationFieldDefn;
import com.gtwm.pb.model.manageUsage.UsageLogger;
import com.gtwm.pb.servlets.ServletUtilMethods;
import com.gtwm.pb.util.AppProperties;
import com.gtwm.pb.util.DataDependencyException;
import com.gtwm.pb.util.Enumerations.AttachmentType;
import com.gtwm.pb.util.Enumerations.DataFormat;
import com.gtwm.pb.util.Enumerations.QuickFilterType;
import com.gtwm.pb.util.Helpers;
import com.gtwm.pb.util.MissingParametersException;
import com.gtwm.pb.util.Naming;
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
import javax.mail.MessagingException;
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
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.grlea.log.SimpleLogger;
import org.glowacki.CalendarParser;
import org.glowacki.CalendarParserException;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;

import au.com.bytecode.opencsv.CSVReader;

// TODO: There is only one instance of DataManagement in the app
// Consider making all methods and properties static
public final class DataManagement implements DataManagementInfo {

	private DataManagement() {
		this.webAppRoot = null;
		this.dataSource = null;
		this.authManager = null;
	}

	/**
	 * @param dataSource
	 *          Provides access to the relational database
	 * @param webAppRoot
	 *          Allows access to the filesystem
	 * @param authManager
	 *          Provides access to company, role, user etc. objects
	 */
	public DataManagement(DataSource dataSource, String webAppRoot, AuthManagerInfo authManager) {
		this.dataSource = dataSource;
		this.webAppRoot = webAppRoot;
		this.authManager = authManager;
	}

	public void addComment(SessionDataInfo sessionData, BaseField field, int rowId, AppUserInfo user,
			String rawComment) throws SQLException, ObjectNotFoundException, CantDoThatException,
			CodingErrorException {
		String SQLCode = "INSERT INTO dbint_comments(created, author, internalfieldname, rowid, text) VALUES (?,?,?,?,?)";
		// Protect against cross-site scripting
		String comment = Naming.makeValidXML(Helpers.smartCharsReplace(rawComment));
		TableInfo table = field.getTableContainingField();
		Connection conn = null;
		try {
			conn = this.dataSource.getConnection();
			conn.setAutoCommit(false);
			PreparedStatement statement = conn.prepareStatement(SQLCode);
			java.sql.Timestamp timestamp = new java.sql.Timestamp(System.currentTimeMillis());
			statement.setTimestamp(1, timestamp);
			statement.setString(2, user.getForename() + " " + user.getSurname());
			statement.setString(3, field.getInternalFieldName());
			statement.setInt(4, rowId);
			statement.setString(5, comment);
			int rowsAffected = statement.executeUpdate();
			statement.close();
			if (rowsAffected != 1) {
				throw new ObjectNotFoundException("Error adding comment. " + rowsAffected
						+ " rows inserted. SQL = " + statement);
			}
			// Concatenate all comments into the hidden comments field (for
			// searching)
			BaseField concatenationField = table.getField(HiddenFields.COMMENTS_FEED.getFieldName());
			BaseField lastModifiedField = table.getField(HiddenFields.LAST_MODIFIED.getFieldName());
			SQLCode = "UPDATE " + table.getInternalTableName() + " SET "
					+ concatenationField.getInternalFieldName();
			SQLCode += " = (? || coalesce(" + concatenationField.getInternalFieldName() + ", '')), ";
			SQLCode += lastModifiedField.getInternalFieldName() + " = now()";
			SQLCode += " WHERE " + table.getPrimaryKey().getInternalFieldName() + "=?";
			statement = conn.prepareStatement(SQLCode);
			statement.setString(1, comment + "\n---\n");
			statement.setInt(2, rowId);
			rowsAffected = statement.executeUpdate();
			if (rowsAffected != 1) {
				throw new ObjectNotFoundException("Error concatenating new comment with old. "
						+ rowsAffected + " rows updated. SQL = " + statement);
			}
			statement.close();
			conn.commit();
			field.setHasComments(true);
			Set<Integer> noCommentRows = this.noComments.get(field);
			if (noCommentRows != null) {
				noCommentRows.remove(rowId);
				this.noComments.put(field, noCommentRows);
			}
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		// Email notification
		Authenticator authenticator = (Authenticator) this.authManager.getAuthenticator();
		CompanyInfo company = user.getCompany();
		Set<String> recipients = new HashSet<String>();
		if (table.getAllowNotifications()) {
			for (AppUserInfo companyUser : company.getUsers()) {
				String email = companyUser.getEmail();
				if (email != null) {
					if (email.contains("@")
							&& authenticator.userAllowedTo(PrivilegeType.VIEW_TABLE_DATA, table, companyUser)
							&& (!companyUser.getUsesCustomUI())) {
						recipients.add(email);
					}
				}
			}
			if (recipients.size() > 0) {
				this.emailComments(recipients, field, rowId, user, comment);
			}
		}
		// HTTP / websocket notification
		// UsageLogger.sendNotification(user, table, sessionData.getReport(),
		// rowId, "comment", comment);
	}

	private void emailComments(Set<String> recipients, BaseField field, int rowId, AppUserInfo user,
			String comment) throws CodingErrorException, CantDoThatException, SQLException,
			ObjectNotFoundException {
		String body = user.getForename() + " " + user.getSurname() + " added the comment\n\n";
		body += comment + "\n\n";
		body += " to " + field.getTableContainingField().getSingularName() + " ";
		TableInfo table = field.getTableContainingField();
		BaseReportInfo report = table.getDefaultReport();
		BaseField pKey = table.getPrimaryKey();
		Map<BaseField, String> filters = new HashMap<BaseField, String>(1);
		filters.put(pKey, String.valueOf(rowId));
		List<DataRowInfo> rows = this.getReportDataRows(null, report, filters, true,
				new HashMap<BaseField, Boolean>(0), 1, QuickFilterType.AND, false);
		if (rows.size() != 1) {
			logger.warn("Row can't be retrieved for comment for table " + table + ", row ID " + rowId);
			return;
		}
		DataRowInfo row = rows.get(0);
		body += "'" + buildEventTitle(report, row, true) + "'\n";
		try {
			String subject = "Comment for " + table.getSingularName();
			boolean rowIdentifierFound = false;
			BaseField firstField = null;
			DataRowFieldInfo firstValue = null;
			ENTRY_LOOP: for (Map.Entry<BaseField, DataRowFieldInfo> rowEntry : row.getDataRowFields()
					.entrySet()) {
				BaseField rowField = rowEntry.getKey();
				if (!rowField.equals(pKey)) {
					if (rowField instanceof SequenceField) {
						subject += " " + rowEntry.getValue();
						rowIdentifierFound = true;
						break ENTRY_LOOP;
					} else if (firstField == null && (!(rowField instanceof FileField))) {
						firstField = rowField;
						firstValue = rowEntry.getValue();
					}
				}
			}
			if (!rowIdentifierFound) {
				subject += " " + firstField + "=" + firstValue;
			}
			Helpers.sendEmail(recipients, body, subject);
		} catch (MessagingException mex) {
			logger.warn("Error sending comment notification email: " + mex);
		}
	}

	public SortedSet<CommentInfo> getComments(BaseField field, int rowId) throws SQLException,
			CantDoThatException {
		SortedSet<CommentInfo> comments = new TreeSet<CommentInfo>();
		// If no record for this field contains comments, return empty set
		Boolean hasComments = field.hasComments();
		if (hasComments != null) {
			if (hasComments.equals(false)) {
				return comments;
			}
		}
		// If this particular row for the field is known to contain no comments,
		// return empty set
		Set<Integer> noCommentRows = this.noComments.get(field);
		if (noCommentRows != null) {
			if (noCommentRows.contains(rowId)) {
				return comments;
			}
		}
		String sqlCode = "SELECT created, author, text FROM dbint_comments WHERE internalfieldname=? AND rowid=? ORDER BY created DESC LIMIT 10";
		Connection conn = null;
		try {
			conn = this.dataSource.getConnection();
			conn.setAutoCommit(false);
			PreparedStatement statement = conn.prepareStatement(sqlCode);
			String internalFieldName = field.getInternalFieldName();
			statement.setString(1, internalFieldName);
			statement.setInt(2, rowId);
			ResultSet results = statement.executeQuery();
			while (results.next()) {
				Timestamp createdTimestamp = results.getTimestamp(1);
				Calendar created = Calendar.getInstance();
				created.setTimeInMillis(createdTimestamp.getTime());
				String author = results.getString(2);
				String comment = results.getString(3);
				comments.add(new Comment(internalFieldName, rowId, author, created, comment));
			}
			results.close();
			statement.close();
			if (comments.size() > 0) {
				field.setHasComments(true);
			} else {
				if (noCommentRows == null) {
					// Create a concurrent set
					noCommentRows = Collections.newSetFromMap(new ConcurrentHashMap<Integer, Boolean>());
				} else if (noCommentRows.size() > 1000) {
					// Don't use an unlimited amount of memory
					noCommentRows.clear();
				}
				noCommentRows.add(rowId);
				this.noComments.put(field, noCommentRows);
				if (hasComments == null) {
					// We've seen there are no comments for this particular
					// record
					// but we don't know if there are any for the field in other
					// records. Check.
					sqlCode = "SELECT count(*) from dbint_comments WHERE internalfieldname=?";
					statement = conn.prepareStatement(sqlCode);
					statement.setString(1, internalFieldName);
					results = statement.executeQuery();
					if (results.next()) {
						int numComments = results.getInt(1);
						if (numComments > 0) {
							field.setHasComments(true);
						} else {
							// Another check in case another thread e.g. running
							// addComment has set this to true.
							// We don't want to overwrite that
							// TODO: Really, this should be atomic but it takes
							// such a small amount of time compared to the SQL it's
							// probably fine
							if (field.hasComments() == null) {
								field.setHasComments(false);
							}
						}
					} else {
						logger.error("Unable to see if comments exist with query " + statement);
					}
					results.close();
					statement.close();
				}
			}
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		return comments;
	}

	public String getWebAppRoot() {
		return this.webAppRoot;
	}

	public void saveRecord(HttpServletRequest request, TableInfo table,
			LinkedHashMap<BaseField, BaseValue> dataToSave, boolean newRecord, int rowId,
			SessionDataInfo sessionData, List<FileItem> multipartItems) throws InputRecordException,
			ObjectNotFoundException, SQLException, CodingErrorException, DisallowedException,
			CantDoThatException, MissingParametersException {
		// editing a single record, pass in one row id
		Set<Integer> rowIds = new HashSet<Integer>(1);
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
			PreparedStatement statement = reportData.getReportSqlPreparedStatement(conn, filterValues,
					false, emptySorts, -1, primaryKey, QuickFilterType.AND, false);
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
					+ " isn't in report " + sessionReport + ", in which case it's nothing to worry about");
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		this.saveRecord(request, table, dataToSave, false, rowIds, sessionData, multipartItems);
		return rowIds.size();
	}

	public void cloneRecord(HttpServletRequest request, TableInfo table, int rowId,
			SessionDataInfo sessionData, List<FileItem> multipartItems) throws ObjectNotFoundException,
			SQLException, CantDoThatException, CodingErrorException, InputRecordException,
			DisallowedException, MissingParametersException {
		// Get values to clone.
		Map<BaseField, BaseValue> values = this.getTableDataRow(sessionData, table, rowId, false);
		// Store in a linked hash map to maintain order as saveRecord needs
		// values in an order which isn't going to change.
		LinkedHashMap<BaseField, BaseValue> valuesToClone = new LinkedHashMap<BaseField, BaseValue>();
		// Get list of fields direct from source table
		// (getTableDataRow switches RelationField for
		// RelationField.getRelatedField)
		SortedSet<BaseField> fields = table.getFields();
		// Ignore un-clonable fields
		for (BaseField field : fields) {
			if ((!(field instanceof FileField)) && (!(field instanceof SeparatorField))
					&& (!(field instanceof CommentFeedField))
					&& (!(field.getFieldName().equals(HiddenFields.COMMENTS_FEED.getFieldName())))
					&& (!(field instanceof ReferencedReportDataField)) && (!(field.getUnique()))) {
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
			throws ObjectNotFoundException, CantDoThatException, SQLException, CodingErrorException,
			DisallowedException {
		CompanyInfo company = this.authManager.getCompanyForLoggedInUser(request);
		BaseReportInfo report = sessionData.getReport();
		TableInfo table = report.getParentTable();
		if (!table.getRecordsLockable()) {
			throw new CantDoThatException("Records in the " + table + " table can't be locked");
		}
		Map<BaseField, String> filters = sessionData.getReportFilterValues();
		Map<BaseField, Boolean> sorts = new HashMap<BaseField, Boolean>();
		AppUserInfo user = this.authManager.getLoggedInUser(request);
		List<DataRowInfo> dataRows = this.getReportDataRows(user, report, filters, false, sorts, -1,
				QuickFilterType.AND, false);
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
				logger.warn("Expected one record to be locked but " + rowsAffected + " were. SQLCode = "
						+ SQLCode + ", rowid = " + rowId);
			}
			statement.close();
			conn.commit();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		// Override lock if we just locked the same record
		TableInfo recordLockOverrideTable = ((SessionData) sessionData).getRecordLockOverrideTable();
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
					// 'Created by' can be overridden by an administrator on request
					String createdByOverrideUsername = request.getParameter("created_by_override_username");
					if (createdByOverrideUsername == null) {
						fieldValue = this.getUserValue(request, request.getRemoteUser());
					} else {
						fieldValue = this.getUserValue(request, createdByOverrideUsername);
					}
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
				fieldValue = this.getUserValue(request, request.getRemoteUser());
			}
			if (fieldValue != null) {
				// by design, the user should never be able to send a request
				// to update the username/date hidden fields as this would
				// put a hole in the auditing functionality
				dataToSave.put(field, fieldValue);
			}
		}
	}

	private BaseValue getUserValue(HttpServletRequest request, String userName)
			throws MissingParametersException, ObjectNotFoundException, DisallowedException {
		BaseValue fieldValue;
		AppUserInfo currentUser = null;
		if (userName == null) {
			currentUser = ServletUtilMethods.getPublicUserForRequest(request,
					this.authManager.getAuthenticator());
		} else {
			// Note: getUserByUserName will throw DisallowedException unless the userName is that of the logged in user or the logged in user is an administrator
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
			SessionDataInfo sessionData, List<FileItem> multipartItems) throws InputRecordException,
			ObjectNotFoundException, SQLException, CantDoThatException, CodingErrorException,
			DisallowedException, MissingParametersException {
		if ((dataToSave.size() == 0) && (!newRecord)) {
			// Note: this does actually happen quite a lot, from two particular
			// users, therefore I've commented out the log warning.
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
		// If any fields are files to upload, do the actual uploads.
		// Do this before opening an SQL connection in case the uploads take a long
		// time and time out the SQL connection. An upload error will then cause the
		// record save to fail. Note: new record uploads have to be left until after the
		// SQL as the new record row ID needs to be retrieved from the database
		if (!newRecord) {
			for (BaseField field : dataToSave.keySet()) {
				if (field instanceof FileField) {
					try {
						this.uploadFile(request, (FileField) field, (FileValue) dataToSave.get(field), rowId,
								multipartItems);
					} catch (CantDoThatException cdtex) {
						throw new InputRecordException("Error uploading file: " + cdtex.getMessage(), field,
								cdtex);
					} catch (FileUploadException fuex) {
						throw new InputRecordException("Error uploading file: " + fuex.getMessage(), field,
								fuex);
					}
				}
			}
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
			valuePlaceholdersCsv = valuePlaceholdersCsv.substring(0, valuePlaceholdersCsv.length() - 2);
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
				SQLCodeBuilder.append(" WHERE " + table.getPrimaryKey().getInternalFieldName() + " in (?");
				for (int i = 1; i < rowIds.size(); i++) {
					SQLCodeBuilder.append(",?");
				}
				SQLCodeBuilder.append(")");
			} else {
				// add filter for single row id
				SQLCodeBuilder.append(" WHERE " + table.getPrimaryKey().getInternalFieldName() + "=?");
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
								&& (((IntegerValue) fieldValue).getValueInteger() == -1) || (fieldValue.isNull())) {
							statement.setNull(fieldNumber, Types.NULL);
						} else {
							statement.setInt(fieldNumber, ((IntegerValue) fieldValue).getValueInteger());
						}
					} else if (fieldValue instanceof DurationValue) {
						statement.setString(fieldNumber, ((DurationValue) fieldValue).getSqlFormatInterval());
					} else if (fieldValue instanceof DecimalValue) {
						statement.setDouble(fieldNumber, ((DecimalValue) fieldValue).getValueFloat());
					} else if (fieldValue instanceof DateValue) {
						if (((DateValue) fieldValue).getValueDate() != null) {
							java.util.Date javaDateValue = ((DateValue) fieldValue).getValueDate().getTime();
							java.sql.Timestamp sqlTimestampValue = new java.sql.Timestamp(javaDateValue.getTime());
							statement.setTimestamp(fieldNumber, sqlTimestampValue);
						} else {
							statement.setTimestamp(fieldNumber, null);
						}
					} else if (fieldValue instanceof CheckboxValue) {
						statement.setBoolean(fieldNumber, ((CheckboxValue) fieldValue).getValueBoolean());
					} else if (fieldValue instanceof FileValue) {
						statement.setString(fieldNumber, ((FileValue) fieldValue).toString());
					} else {
						throw new CodingErrorException("Field value " + fieldValue + " is of unknown type "
								+ fieldValue.getClass().getSimpleName());
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
							throw new CantDoThatException("Record " + aRowId + " from table " + table
									+ " is locked to prevent editing");
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
			if ((numRowsAffected != 1) && (!globalEdit)) {
				conn.rollback();
				if (numRowsAffected > 0) {
					throw new ObjectNotFoundException(String.valueOf(numRowsAffected)
							+ " records would be altered during a single record save");
				} else {
					throw new ObjectNotFoundException(
							"The current record can't be found to edit - perhaps someone else has deleted it");
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
								+ ((RelationField) possibleCauseField).getRelatedTable() + " record first";
					} else {
						errorMessage = "Value " + dataToSave.get(possibleCauseField) + " not allowed ("
								+ Helpers.replaceInternalNames(errorMessage, table.getDefaultReport()) + ")";
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
		if (newRecord) {
			sessionData.setRowId(table, newRowId);
			// If any files to upload for new records, do the actual uploads
			for (BaseField field : dataToSave.keySet()) {
				if (field instanceof FileField) {
					try {
						this.uploadFile(request, (FileField) field, (FileValue) dataToSave.get(field),
							newRowId, multipartItems);
					} catch (CantDoThatException cdtex) {
						throw new InputRecordException("Error uploading file: " + cdtex.getMessage(), field,
								cdtex);
					} catch (FileUploadException fuex) {
						throw new InputRecordException("Error uploading file: " + fuex.getMessage(), field, fuex);
					}
				}
			}
		}
		this.logLastDataChangeTime(request);
		logLastTableDataChangeTime(table);
		UsageLogger usageLogger = new UsageLogger(this.dataSource);
		AppUserInfo user = null;
		if (request.getRemoteUser() == null) {
			user = ServletUtilMethods.getPublicUserForRequest(request,
					this.authManager.getAuthenticator());
		} else {
			user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		}
		// Send websocket notification
		// UsageLogger.sendNotification(user, table, sessionData.getReport(),
		// rowId, "edit", "Record saved: " + dataToSave);
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
			// Last session action can be new or clone
			usageLogger.logDataChange(user, table, null, sessionData.getLastAppAction(), newRowId,
					dataToLog.toString());
		} else if (globalEdit) {
			// TODO: need better logging of global edits
			usageLogger.logDataChange(user, table, null, AppAction.GLOBAL_EDIT, rowId,
					dataToLog.toString());
		} else {
			BaseField fieldUpdated = null;
			Set<BaseField> fieldSet = new TreeSet<BaseField>();
			for (BaseField field : dataToSave.keySet()) {
				if (!field.getHidden()) {
					fieldSet.add(field);
				}
			}
			if (fieldSet.size() == 1) {
				fieldUpdated = new LinkedList<BaseField>(fieldSet).getFirst();
			}
			usageLogger.logDataChange(user, table, fieldUpdated, AppAction.UPDATE_RECORD, rowId,
					dataToLog.toString());
		}
		UsageLogger.startLoggingThread(usageLogger);
	}

	public int importCSV(HttpServletRequest request, TableInfo table, boolean updateExistingRecords,
			BaseField recordIdentifierField, boolean generateRowIds, char separator, char quotechar,
			int numHeaderLines, boolean useRelationDisplayValues, boolean importSequenceValues,
			boolean requireExactRelationMatches, boolean trim, boolean merge,
			List<FileItem> multipartItems, String csvContent) throws SQLException, InputRecordException,
			IOException, CantDoThatException, ObjectNotFoundException, DisallowedException,
			CodingErrorException {
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
			if (field instanceof SequenceField && (!field.equals(primaryKey)) && (!importSequenceValues)) {
				fields.remove(field);
			} else if (field.getHidden()) {
				if (field.getFieldName().equals(HiddenFields.VIEW_COUNT.getFieldName())
						|| field.getFieldName().equals(HiddenFields.COMMENTS_FEED.getFieldName())) {
					fields.remove(field);
				} else if (updateExistingRecords) {
					if (field.getFieldName().equals(HiddenFields.DATE_CREATED.getFieldName())
							|| field.getFieldName().equals(HiddenFields.CREATED_BY.getFieldName())) {
						fields.remove(field);
					}
				}
			} else if (!field.getFieldCategory().savesData()) {
				fields.remove(field);
			}
			// Also, if importing relations by display value, look up
			// display/internal value mappings
			if (useRelationDisplayValues && field instanceof RelationField) {
				Map<String, String> displayToInternalValue = ((RelationFieldDefn) field).getItems(true,
						false);
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
				throw new CantDoThatException("Can't find the field specified as record identifier ("
						+ recordIdentifierField + ") in the list of table fields " + fields + " in table "
						+ table);
			}
			updateSQLCode = updateSQLCode.substring(0, updateSQLCode.length() - 2);
			updateSQLCode += " WHERE " + recordIdentifierField.getInternalFieldName() + "=?";
			logCreationSQLCode = "UPDATE " + table.getInternalTableName() + " SET "
					+ table.getField(HiddenFields.DATE_CREATED.getFieldName()).getInternalFieldName()
					+ "=?, " + table.getField(HiddenFields.CREATED_BY.getFieldName()).getInternalFieldName()
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
											"Cannot generate row ids when importing file names. See line " + importLine
													+ ", field '" + field.getFieldName() + "' with value '" + lineValue + "'");
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
										Calendar calValue = CalendarParser.parse(lineValue, CalendarParser.DD_MM_YY);
										statement.setTimestamp(fieldNum, new Timestamp(calValue.getTimeInMillis()));
										if (updateExistingRecords) {
											backupInsertStatement.setTimestamp(fieldNum,
													new Timestamp(calValue.getTimeInMillis()));
										}
									} catch (CalendarParserException cpex) {
										throw new InputRecordException("Error importing line " + importLine
												+ ", field " + field + ": " + cpex.getMessage(), field, cpex);
									}
									break;
								case FLOAT:
									lineValue = lineValue.trim().replaceAll("[^\\d\\.\\+\\-eE]", "");
									statement.setDouble(fieldNum, Double.valueOf(lineValue));
									if (updateExistingRecords) {
										backupInsertStatement.setDouble(fieldNum, Double.valueOf(lineValue));
									}
									break;
								case INTEGER:
									if ((field instanceof RelationField) && (useRelationDisplayValues)) {
										// find key value for display value
										RelationField relationField = (RelationField) field;
										Map<String, String> valueKeyMap = relationLookups.get(relationField);
										String internalValueString = valueKeyMap.get(lineValue);
										if (internalValueString == null) {
											if (!requireExactRelationMatches) {
												// A very basic fuzzy matching
												// algorithm
												String potentialDisplayValue = null;
												String lineValueLowerCase = lineValue.toLowerCase();
												FUZZYMATCH: for (Map.Entry<String, String> entry : valueKeyMap.entrySet()) {
													potentialDisplayValue = entry.getKey();
													if (potentialDisplayValue.toLowerCase().contains(lineValueLowerCase)) {
														internalValueString = entry.getValue();
														break FUZZYMATCH;
													}
												}
											}
											if (internalValueString == null) {
												throw new CantDoThatException("Error importing line " + importLine
														+ ", field " + relationField + ": Can't find a related '"
														+ relationField.getRelatedTable() + "' for "
														+ relationField.getDisplayField() + " '" + lineValue + "'. ");
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
										lineValue = lineValue.trim().replaceAll("[^\\d\\.\\+\\-eE]", "");
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
									lineValue = lineValue.trim().replaceAll("[^\\d\\.\\+\\-eE]", "");
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
									boolean filterValueIsTrue = Helpers.valueRepresentsBooleanTrue(lineValue);
									statement.setBoolean(fieldNum, filterValueIsTrue);
									if (updateExistingRecords) {
										backupInsertStatement.setBoolean(fieldNum, filterValueIsTrue);
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
							throw new InputRecordException("Can't find a record identifier value at line "
									+ importLine, recordIdentifierField);
						}
						recordIdentifierDescription = recordIdentifierField.getFieldName() + " = "
								+ recordIdentifierInteger;
						// Set the 'WHERE recordIdentifier = ?' clause
						statement.setInt(fields.size() + 1, recordIdentifierInteger);
					} else {
						if (recordIdentifierString == null) {
							throw new InputRecordException("Can't find a record identifier value at line "
									+ importLine, recordIdentifierField);
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
						String newRowIdSQLCode = "SELECT currval('" + table.getInternalTableName() + "_"
								+ primaryKey.getInternalFieldName() + "_seq')";
						PreparedStatement newRowIdStatement = conn.prepareStatement(newRowIdSQLCode);
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
							throw new SQLException("Row ID not found for the newly inserted record. '"
									+ newRowIdStatement + "' didn't work");
						}
						newRowIdResults.close();
						newRowIdStatement.close();
					} else if (rowsAffected > 1) {
						throw new InputRecordException("Error importing line " + importLine
								+ ". The record identifier field " + recordIdentifierDescription
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
		logLastTableDataChangeTime(table);
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
		usageLogger.logDataChange(loggedInUser, table, null, AppAction.CSV_IMPORT, -1, logMessage);
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
	 * Upload a file for a particular field in a particular record. If a file with
	 * the same name already exists, rename the old one to avoid overwriting.
	 * Reset the last modified time of the old one so the rename doesn't muck this
	 * up. Maintaining the file timestamp is useful for version history
	 */
	private void uploadFile(HttpServletRequest request, FileField field, FileValue fileValue,
			int rowId, List<FileItem> multipartItems) throws CantDoThatException, FileUploadException {
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
						basePath = basePath.substring(0, basePath.length() - extension.length() - 1);
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
						throw new FileUploadException("Rename of existing file from '" + selectedFile
								+ "' to '" + renamedFile + "' failed");
					}
					if (!renamedFile.setLastModified(lastModified)) {
						throw new FileUploadException("Error setting the last modified date of " + renamedFile);
					}
					// I think a File object's name is inviolable but just in
					// case
					selectedFile = new File(filePath);
				}
				try {
					item.write(selectedFile);
				} catch (Exception ex) {
					// Catching a general exception?! This is because the
					// library throws a raw exception. Not very good
					throw new FileUploadException("Error writing file: " + ex.getMessage());
				}
				// Record upload speed
				long requestStartTime = request.getSession().getLastAccessedTime();
				float secondsToUpload = (System.currentTimeMillis() - requestStartTime) / ((float) 1000);
				if (secondsToUpload > 10) {
					// Only time reasonably long uploads otherwise we'll amplify
					// errors
					float uploadSpeed = ((float) fileSize) / secondsToUpload;
					this.updateUploadSpeed(uploadSpeed);
				}
				if (extension.equals("jpg") || extension.equals("jpeg") || extension.equals("png")) {
					// image.png -> image.png.40.png
					String thumb40Path = filePath + "." + 40 + "." + extension;
					String thumb500Path = filePath + "." + 500 + "." + extension;
					File thumb40File = new File(thumb40Path);
					File thumb500File = new File(thumb500Path);
					int midSize = 500;
					if (field.getAttachmentType().equals(AttachmentType.PROFILE_PHOTO)) {
						midSize = 250;
					}
					boolean needResize = false;
					try {
						BufferedImage originalImage = ImageIO.read(selectedFile);
						int height = originalImage.getHeight();
						int width = originalImage.getWidth();
						if ((height > midSize) || (width > midSize)) {
							needResize = true;
						}
					} catch (IOException ioex) {
						// Certain images can sometimes fail to be read e.g. CMYK JPGs
						logger.error("Error reading image dimensions: " + ioex);
						needResize = true; // Unable to read image size, assume resize
																// needed
					}
					try {
						// Conditional resize
						if (needResize) {
							Thumbnails.of(selectedFile).size(midSize, midSize).toFile(thumb500File);
						} else {
							FileUtils.copyFile(selectedFile, thumb500File);
						}
						// Allow files that are up to 60 px tall as long as the
						// width is no > 40 px
						Thumbnails.of(selectedFile).size(40, 60).toFile(thumb40File);
					} catch (IOException ioex) {
						throw new FileUploadException("Error generating thumbnail: " + ioex.getMessage());
					}
				} else if (extension.equals("pdf")) {
					// Convert first page to PNG with imagemagick
					ConvertCmd convert = new ConvertCmd();
					IMOperation op = new IMOperation();
					op.addImage(); // Placeholder for input PDF
					op.resize(500, 500);
					op.addImage(); // Placeholder for output PNG
					try {
						// [0] means convert only first page
						convert.run(op, new Object[] { filePath + "[0]", filePath + "." + 500 + ".png" });
					} catch (IOException ioex) {
						throw new FileUploadException("IO error while converting PDF to PNG: " + ioex);
					} catch (InterruptedException iex) {
						throw new FileUploadException("Interrupted while converting PDF to PNG: " + iex);
					} catch (IM4JavaException im4jex) {
						throw new FileUploadException("Problem converting PDF to PNG: " + im4jex);
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
	 * specified, i.e. data that would be deleted in a cascade if the record were
	 * deleted
	 * 
	 * Along with each table, return a snippet of the contents of dependent
	 * record(s)
	 * 
	 * Also throw an exception immediately if any locked records are found as this
	 * means the deletion should fail
	 * 
	 * @param tables
	 *          The set of tables to check for dependencies between, e.g. all the
	 *          tables in a company
	 * @param sessionData
	 *          Used to check for locked record overrides
	 * @param recordDependencies
	 *          This method is recursive
	 */
	public static Map<TableInfo, String> getRecordsDependencies(Connection conn,
			Set<TableInfo> tables, SessionDataInfo sessionData, DatabaseInfo databaseDefn,
			TableInfo table, int rowId, Map<TableInfo, String> recordDependencies) throws SQLException,
			ObjectNotFoundException, CantDoThatException {
		for (TableInfo otherTable : tables) {
			Set<BaseField> otherTableFields = otherTable.getFields();
			for (BaseField field : otherTableFields) {
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
				BaseField primaryKey = otherTable.getPrimaryKey();
				BaseField otherUniqueKey = null;
				Set<BaseField> textFields = new HashSet<BaseField>();
				for (BaseField testField : otherTableFields) {
					// Only report text fields
					if (testField instanceof TextField) {
						if ((!testField.getHidden()) && (!((TextField) testField).usesLookup())) {
							textFields.add(testField);
						}
					}
					// If the table has a unique field, that will be more useful
					// to the user than the internal ID
					if (testField.getUnique() && (!testField.equals(primaryKey))) {
						otherUniqueKey = testField;
					}
				}
				String SQLCode = "SELECT " + primaryKey.getInternalFieldName();
				if (otherUniqueKey != null) {
					SQLCode += ", " + otherUniqueKey.getInternalFieldName();
				}
				for (BaseField textField : textFields) {
					SQLCode += ", " + textField.getInternalFieldName();
				}
				// NB Don't put a LIMIT on this statement, we want to find all
				// children in the tree
				SQLCode += " FROM " + otherTable.getInternalTableName() + " WHERE "
						+ relationField.getInternalFieldName() + "=?";
				PreparedStatement statement = conn.prepareStatement(SQLCode);
				statement.setInt(1, rowId);
				ResultSet results = statement.executeQuery();
				while (results.next()) {
					int otherRowId = results.getInt(1);
					String recordDescription = "";
					if (otherUniqueKey == null) {
						recordDescription = "internal ID " + otherRowId + ": ";
					} else {
						String otherId = results.getString(otherUniqueKey.getInternalFieldName());
						recordDescription = otherUniqueKey + " = " + otherId + ": ";
					}
					for (BaseField textField : textFields) {
						String fieldValue = results.getString(textField.getInternalFieldName());
						if (fieldValue != null) {
							if (!fieldValue.equals("")) {
								recordDescription += fieldValue.replaceAll("\\n", "... ") + ", ";
							}
						}
					}
					if (recordDescription.length() > 100) {
						recordDescription = recordDescription.substring(0, 99) + "...";
					}
					TableData otherTableData = new TableData(otherTable);
					if (otherTableData.isRecordLocked(conn, sessionData, otherRowId)) {
						throw new CantDoThatException("Record " + otherRowId
								+ " cannot be removed from dependent table " + otherTable + " because it is locked");
					}
					// recurse
					if (!recordDependencies.keySet().contains(otherTable)) {
						recordDependencies.putAll(getRecordsDependencies(conn, tables, sessionData,
								databaseDefn, otherTable, otherRowId, recordDependencies));
					}
					recordDependencies.put(otherTable, recordDescription);
				}
				results.close();
				statement.close();
			}
		}
		return recordDependencies;
	}

	private void removeUploadedFiles(TableInfo table, int rowId) {
		for (BaseField field : table.getFields()) {
			if (field instanceof FileField) {
				String folderName = this.getWebAppRoot() + "uploads/" + table.getInternalTableName() + "/"
						+ field.getInternalFieldName() + "/" + rowId;
				File folder = new File(folderName);
				if (folder.exists()) {
					try {
						FileUtils.deleteDirectory(folder);
					} catch (IOException e) {
						logger.warn("Unable to remove " + folderName + " when removing field " + table + "."
								+ field + ": " + e);
					}
				}
			}
		}
	}

	public void removeRecord(HttpServletRequest request, SessionDataInfo sessionData,
			DatabaseInfo databaseDefn, TableInfo table, int rowId, boolean cascade) throws SQLException,
			ObjectNotFoundException, CodingErrorException, CantDoThatException, DisallowedException,
			DataDependencyException {
		Map<TableInfo, String> recordDependencies = new LinkedHashMap<TableInfo, String>();
		Set<TableInfo> tables = this.authManager.getCompanyForLoggedInUser(request).getTables();
		Connection conn = null;
		try {
			conn = this.dataSource.getConnection();
			conn.setAutoCommit(false);
			recordDependencies = getRecordsDependencies(conn, tables, sessionData, databaseDefn, table,
					rowId, recordDependencies);
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		// check for cascading deletes:
		if (recordDependencies.size() > 0) {
			// check if user has permissions on all dependent tables:
			for (TableInfo dependentTable : recordDependencies.keySet()) {
				if (dependentTable.equals(table))
					continue;
				if (this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
						PrivilegeType.EDIT_TABLE_DATA, dependentTable))
					continue;
				if (this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
						PrivilegeType.MANAGE_TABLE, dependentTable))
					continue;
				throw new CantDoThatException(
				// TODO: This should probably really
				// be a DisallowedException
						"Unable to delete record as you are not permitted to delete from the dependent table "
								+ dependentTable);
			}
			// user has permissions on all dependent data. however, deletion
			// should only continue if the user has opted to cascade deletion
			if (!cascade) {
				String warning = "";
				for (Map.Entry<TableInfo, String> dependency : recordDependencies.entrySet()) {
					warning += dependency.getKey().getSimpleName() + " - " + dependency.getValue() + "\n";
				}
				throw new DataDependencyException(warning);
			}
		}
		// First get all the content of the row we're deleting, for logging
		// purposes
		Map<BaseField, BaseValue> deletedRow = this.getTableDataRow(null, table, rowId, false);
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
				// Delete comments for the record
				Set<String> internalFieldNamesSet = new HashSet<String>();
				for (BaseField field : table.getFields()) {
					if (!field.getHidden()) {
						internalFieldNamesSet.add("'" + field.getInternalFieldName() + "'");
					}
				}
				String internalFieldNamesCsv = StringUtils.join(internalFieldNamesSet, ",");
				SQLCode = "DELETE FROM dbint_comments WHERE rowid=? AND internalfieldname IN ("
						+ internalFieldNamesCsv + ")";
				statement = conn.prepareStatement(SQLCode);
				statement.setInt(1, rowId);
				int numCommentsDeleted = statement.executeUpdate();
				if (numCommentsDeleted > 0) {
					logger.info("" + numCommentsDeleted + " comments deleted when deleting " + table
							+ " record " + rowId);
				}
				statement.close();
				conn.commit();
			}
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		this.removeUploadedFiles(table, rowId);
		this.logLastDataChangeTime(request);
		logLastTableDataChangeTime(table);
		UsageLogger usageLogger = new UsageLogger(dataSource);
		AppUserInfo user = this.authManager.getUserByUserName(request, request.getRemoteUser());
		usageLogger.logDataChange(user, table, null, AppAction.REMOVE_RECORD, rowId, "Deleted data: "
				+ deletedRow);
		UsageLogger.startLoggingThread(usageLogger);
	}

	private String getReportDataAsFormat(DataFormat dataFormat, AppUserInfo user,
			BaseReportInfo report, Map<BaseField, String> filters, boolean exactFilters, long cacheSeconds)
			throws CodingErrorException, CantDoThatException, SQLException, XMLStreamException,
			ObjectNotFoundException, JsonGenerationException {
		String id = dataFormat.toString() + report.getInternalReportName();
		CachedReportFeedInfo cachedFeed = null;
		if (filters.size() == 0) {
			// For now, only cache when there are no filters. Can reconsider
			// this in future if necessary
			cachedFeed = this.cachedReportFeeds.get(id);
			if (cachedFeed != null) {
				long lastDataChangeAge = System.currentTimeMillis()
						- getLastCompanyDataChangeTime(user.getCompany());
				long cacheAge = cachedFeed.getCacheAge();
				if ((cacheAge < lastDataChangeAge) || (cacheAge < (cacheSeconds * 1000))) {
					this.reportFeedCacheHits.incrementAndGet();
					return cachedFeed.getFeed();
				}
			}
		}
		int numRows = 10000;
		if (dataFormat.equals(DataFormat.RSS)) {
			numRows = 100;
		}
		List<DataRowInfo> reportDataRows = this.getReportDataRows(user, report, filters, exactFilters,
				new HashMap<BaseField, Boolean>(0), numRows, QuickFilterType.AND, false);
		String dataFeedString = null;
		if (dataFormat.equals(DataFormat.JSON)) {
			dataFeedString = this.generateJSON(report, reportDataRows);
		} else if (dataFormat.equals(DataFormat.RSS)) {
			dataFeedString = this.generateRSS(user, report, reportDataRows);
		} else {
			throw new CodingErrorException("Format " + dataFormat + " has no report generator");
		}
		UsageLogger usageLogger = new UsageLogger(this.dataSource);
		usageLogger.logReportView(user, report, new HashMap<BaseField, String>(0), 10000,
				dataFormat.toString());
		UsageLogger.startLoggingThread(usageLogger);
		if ((filters.size() == 0) && (cacheSeconds > 0)) {
			cachedFeed = new CachedFeed(dataFeedString);
			this.cachedReportFeeds.put(id, cachedFeed);
		}
		int cacheMisses = this.reportFeedCacheMisses.incrementAndGet();
		if (cacheMisses > 100) {
			logger.info("Public report data cache hits: " + this.reportFeedCacheHits + ", misses "
					+ cacheMisses);
			this.reportFeedCacheHits.set(0);
			this.reportFeedCacheMisses.set(0);
		}
		return dataFeedString;
	}

	public String getReportRSS(AppUserInfo user, BaseReportInfo report,
			Map<BaseField, String> filters, boolean exactFilters, long cacheSeconds) throws SQLException,
			CodingErrorException, CantDoThatException, XMLStreamException, ObjectNotFoundException {
		try {
			return this.getReportDataAsFormat(DataFormat.RSS, user, report, filters, exactFilters,
					cacheSeconds);
		} catch (JsonGenerationException jgex) {
			throw new CodingErrorException("RSS generation threw a JSON exception: " + jgex);
		}
	}

	public String getReportJSON(AppUserInfo user, BaseReportInfo report,
			Map<BaseField, String> filters, boolean exactFilters, long cacheSeconds)
			throws CodingErrorException, CantDoThatException, SQLException, XMLStreamException,
			ObjectNotFoundException, JsonGenerationException {
		return this.getReportDataAsFormat(DataFormat.JSON, user, report, filters, exactFilters,
				cacheSeconds);
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
		Date lastDataChangeDate = new Date(getLastCompanyDataChangeTime(user.getCompany()));
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
		eventWriter.add(eventFactory.createEndElement("", "", "channel"));
		eventWriter.add(end);
		eventWriter.add(eventFactory.createEndElement("", "", "rss"));
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

	public String getReportMapJson(AppUserInfo user, BaseReportInfo report,
			Map<BaseField, String> filters) throws CodingErrorException, CantDoThatException,
			SQLException, ObjectNotFoundException {
		ReportMapInfo map = report.getMap();
		if (map == null) {
			throw new CantDoThatException("Report has no map configured");
		}
		ReportFieldInfo postcodeField = map.getPostcodeField();
		if (postcodeField == null) {
			throw new CantDoThatException("Report map has no postcode field identified");
		}
		ReportFieldInfo colourField = map.getColourField();
		ReportFieldInfo categoryField = map.getCategoryField();
		List<DataRowInfo> reportDataRows = this.getReportDataRows(user, report, filters, false,
				new HashMap<BaseField, Boolean>(0), 10000, QuickFilterType.AND, true);
		JsonFactory jsonFactory = new JsonFactory();
		StringWriter stringWriter = new StringWriter(1024);
		JsonGenerator jg;
		try {
			jg = jsonFactory.createJsonGenerator(stringWriter);
			jg.writeStartArray();
			for (DataRowInfo reportDataRow : reportDataRows) {
				jg.writeStartObject();
				jg.writeNumberField("rowId", reportDataRow.getRowId());
				LocationDataRowFieldInfo postcodeDataRowField = (LocationDataRowFieldInfo) reportDataRow
						.getValue(postcodeField);
				jg.writeStringField("postcode", postcodeDataRowField.getKeyValue());
				jg.writeNumberField("latitude", postcodeDataRowField.getLatitude());
				jg.writeNumberField("longitude", postcodeDataRowField.getLongitude());
				jg.writeStringField("title", buildEventTitle(report, reportDataRow, true));
				if (colourField != null) {
					String colourValue = reportDataRow.getValue(colourField).getKeyValue();
					jg.writeStringField("colourValue", colourValue);
					int upperHash = colourValue.toUpperCase().hashCode();
					int hue = 0;
					if (upperHash != Integer.MIN_VALUE) {
						// http://findbugs.blogspot.co.uk/2006/09/is-mathabs-broken.html
						hue = Math.abs(upperHash) % 360;
					}
					int saturation = 20;
					int lowerHash = colourValue.toLowerCase().hashCode();
					if (lowerHash != Integer.MIN_VALUE) {
						saturation += (Math.abs(lowerHash) % 80);
					}
					int lightness = 50;
					int capsHash = WordUtils.capitalizeFully(colourValue).hashCode();
					if (capsHash != Integer.MIN_VALUE) {
						lightness += (Math.abs(capsHash) % 40);
					}
					jg.writeNumberField("h", hue);
					jg.writeNumberField("s", saturation);
					jg.writeNumberField("l", lightness);
				}
				if (categoryField != null) {
					DataRowFieldInfo categoryValue = reportDataRow.getValue(categoryField);
					jg.writeStringField("categoryValue", categoryValue.getDisplayValue());
				}
				jg.writeEndObject();
			}
			jg.writeEndArray();
			jg.flush();
			jg.close();
		} catch (IOException ioex) {
			throw new CodingErrorException("JSON generation produced IO Exception: " + ioex, ioex);
		}
		return stringWriter.toString();
	}

	private String generateJSON(BaseReportInfo report, List<DataRowInfo> reportDataRows)
			throws CodingErrorException, JsonGenerationException {
		JsonFactory jsonFactory = new JsonFactory();
		StringWriter stringWriter = new StringWriter(1024);
		JsonGenerator jg;
		try {
			jg = jsonFactory.createJsonGenerator(stringWriter);
			jg.writeStartObject();
			jg.writeFieldName("fields");
			jg.writeStartArray();
			for (ReportFieldInfo reportField : report.getReportFields()) {
				jg.writeStartObject();
				jg.writeStringField("id", reportField.getInternalFieldName());
				jg.writeStringField("name", reportField.getFieldName());
				jg.writeStringField("desc", reportField.getFieldDescription());
				jg.writeEndObject();
			}
			jg.writeEndArray();
			jg.writeFieldName("data");
			jg.writeStartArray();
			for (DataRowInfo reportDataRow : reportDataRows) {
				jg.writeStartObject();
				jg.writeNumberField("rowId", reportDataRow.getRowId());
				String valueString = null;
				for (ReportFieldInfo reportField : report.getReportFields()) {
					BaseField field = reportField.getBaseField();
					DataRowFieldInfo value = reportDataRow.getValue(reportField);
					if (field.getFieldCategory().equals(FieldCategory.DATE)) {
						valueString = value.getDisplayValue();
					} else {
						valueString = value.getKeyValue();
					}
					jg.writeStringField(reportField.getInternalFieldName(), valueString);
				}
				jg.writeEndObject();
			}
			jg.writeEndArray();
			jg.writeEndObject();
			jg.flush();
			jg.close();
		} catch (IOException ioex) {
			throw new CodingErrorException("StringWriter produced an IO exception!", ioex);
		}
		return stringWriter.toString();
	}

	public String getReportTimelineJSON(AppUserInfo user, Set<BaseReportInfo> reports,
			Map<BaseField, String> filterValues) throws CodingErrorException, CantDoThatException,
			SQLException, JsonGenerationException, ObjectNotFoundException {
		String id = "";
		SortedMap<BaseField, String> sortedFilterValues = new TreeMap<BaseField, String>(filterValues);
		for (BaseReportInfo report : reports) {
			ReportFieldInfo eventDateReportField = report.getCalendarStartField();
			if (eventDateReportField == null) {
				throw new CantDoThatException("The report '" + report + "' has no suitable date field");
			}
			id += report.getInternalReportName();
		}
		id += sortedFilterValues.toString();
		CachedReportFeedInfo cachedJSON = cachedCalendarJSONs.get(id);
		if (cachedJSON != null) {
			this.calendarJsonCacheHits.incrementAndGet();
			// Note: if we choose not to invalidate the cache on every data
			// change, we could return only JSON that was newer than a certain
			// time here, say the last data change time plus ten secs
			return cachedJSON.getFeed();
		}
		// RFC 2822 date format used by Simile timeline
		DateFormat dateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
		UsageLogger usageLogger = new UsageLogger(this.dataSource);
		JsonFactory jsonFactory = new JsonFactory();
		StringWriter stringWriter = new StringWriter(1024);
		JsonGenerator jg;
		try {
			jg = jsonFactory.createJsonGenerator(stringWriter);
			jg.writeStartObject();
			jg.writeArrayFieldStart("events");
			for (BaseReportInfo report : reports) {
				String className = "report_" + report.getInternalReportName();
				ReportFieldInfo eventDateReportField = report.getCalendarStartField();
				List<DataRowInfo> reportDataRows = this.getReportDataRows(user, report, filterValues,
						false, new HashMap<BaseField, Boolean>(0), 10000, QuickFilterType.AND, false);
				ROWS_LOOP: for (DataRowInfo reportDataRow : reportDataRows) {
					DataRowFieldInfo eventDateValue = reportDataRow.getValue(eventDateReportField);
					if (eventDateValue.getKeyValue().equals("")) {
						continue ROWS_LOOP;
					}
					jg.writeStartObject();
					// timeline needs formatted dates
					Long eventDateEpoch = Long.parseLong(eventDateValue.getKeyValue());
					// String formattedDate = dateFormatter.format(new
					// Date(eventDateEpoch));
					jg.writeStringField("start", "new Date(" + eventDateEpoch + ")");
					String eventTitle = eventDateValue.getDisplayValue() + ": "
							+ buildEventTitle(report, reportDataRow, false);
					jg.writeStringField("caption", eventTitle);
					jg.writeStringField("description", eventTitle);
					// TODO: build short title from long title, don't rebuild
					// from
					// scratch. Just cut off everything after the 5th comma for
					// example
					String shortTitle = buildEventTitle(report, reportDataRow, true);
					jg.writeStringField("classname", className);
					jg.writeEndObject();
				}
				usageLogger.logReportView(user, report, filterValues, 10000, "getTimelineJSON");
				UsageLogger.startLoggingThread(usageLogger);
			}
			jg.writeEndArray();
			jg.writeEndObject();
			jg.flush();
			jg.close();
		} catch (IOException e) {
			throw new CodingErrorException("StringWriter produced an IO exception: " + e);
		}
		String json = stringWriter.toString();
		cachedJSON = new CachedFeed(json);
		cachedCalendarJSONs.put(id, cachedJSON);
		int cacheMisses = this.calendarJsonCacheMisses.incrementAndGet();
		if (cacheMisses > 100) {
			logger.info("Calendar JSON cache hits: " + this.calendarJsonCacheHits + ", misses "
					+ cacheMisses);
			this.calendarJsonCacheHits.set(0);
			this.calendarJsonCacheMisses.set(0);
		}
		return json;
	}

	public String getReportCalendarJSON(AppUserInfo user, BaseReportInfo report,
			Map<BaseField, String> filterValues, Long startEpoch, Long endEpoch)
			throws CodingErrorException, CantDoThatException, SQLException, JsonGenerationException,
			ObjectNotFoundException {
		ReportFieldInfo eventDateReportField = report.getCalendarStartField();
		if (eventDateReportField == null) {
			throw new CantDoThatException("The report '" + report + "' has no suitable date field");
		}
		ReportFieldInfo endDateReportField = report.getCalendarEndField();
		// Try cache first
		// Make a sortedMap so toString is always consistent for the same
		// key/value pairs and we can use it as an ID
		SortedMap<BaseField, String> sortedFilterValues = new TreeMap<BaseField, String>(filterValues);
		String id = report.getInternalReportName() + sortedFilterValues.toString();
		CachedReportFeedInfo cachedJSON = cachedCalendarJSONs.get(id);
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
		DateTimeZone zone = DateTimeZone.getDefault();
		// Workaround. At least on Ubuntu, the default timezone is Etc/GMT not
		// Europe/London, when Europe/London is set in the OS
		// Note: JodaTime used because Java's TimeZone class doesn't return
		// Europe/London when requested, on the development OS
		if (zone.getID().contains("GMT")) {
			zone = DateTimeZone.forID("Europe/London");
		}
		List<DataRowInfo> reportDataRows = this.getReportDataRows(user, report, filterValues, false,
				new HashMap<BaseField, Boolean>(0), 10000, QuickFilterType.AND, false);
		JsonFactory jsonFactory = new JsonFactory();
		StringWriter stringWriter = new StringWriter(1024);
		JsonGenerator jg;
		try {
			jg = jsonFactory.createJsonGenerator(stringWriter);
			jg.writeStartArray();
			String internalReportName = report.getInternalReportName();
			String internalTableName = report.getParentTable().getInternalTableName();
			ROWS_LOOP: for (DataRowInfo reportDataRow : reportDataRows) {
				DataRowFieldInfo eventDateValue = reportDataRow.getValue(eventDateReportField);
				if (eventDateValue.getKeyValue().equals("")) {
					continue ROWS_LOOP;
				}
				jg.writeStartObject();
				jg.writeStringField("id", internalReportName + "_" + reportDataRow.getRowId());
				jg.writeStringField("internalTableName", internalTableName);
				jg.writeNumberField("rowId", reportDataRow.getRowId());
				boolean allDayEvent = allDayValues;
				if (!allDayValues) {
					String eventDateDisplayValue = eventDateValue.getDisplayValue();
					// TODO: trim may not be necessary
					if (eventDateDisplayValue.trim().endsWith("00:00")) {
						allDayEvent = true;
					}
				}
				jg.writeBooleanField("allDay", allDayEvent);
				// fullcalendar needs the number of seconds since the epoch
				// Output in UTC rather than local time as fullcalendar adjusts
				// clientside
				// TODO: This may not work for non-GMT timezones as the offset is from
				// UTC, untested
				Long eventDateMillis = Long.parseLong(eventDateValue.getKeyValue());
				// int timezoneOffset = timeZone.getOffset(eventDateMillis);
				int timezoneOffset = 0;
				if (timezoneAdjust) {
					timezoneOffset = zone.getOffset(eventDateMillis);
				}
				Long eventDateEpoch = (eventDateMillis - timezoneOffset) / 1000;
				jg.writeNumberField("start", eventDateEpoch);
				if (!allDayEvent) {
					if (endDateReportField.equals(eventDateReportField)) {
						jg.writeNumberField("end", eventDateEpoch + 3600);
					} else {
						DataRowFieldInfo endDateValue = reportDataRow.getValue(endDateReportField);
						String endDateEpochString = endDateValue.getKeyValue();
						if (endDateEpochString.equals("")) {
							// events last 1 hr by default
							jg.writeNumberField("end", eventDateEpoch + 3600);
						} else {
							Long endDateEpoch = (Long.parseLong(endDateValue.getKeyValue()) - timezoneOffset) / 1000;
							if (endDateEpoch > eventDateEpoch) {
								jg.writeNumberField("end", endDateEpoch);
							} else {
								jg.writeNumberField("end", eventDateEpoch + 3600);
							}
						}
					}
				} else if (!endDateReportField.equals(eventDateReportField)) {
					// all day event possibly spans multiple days
					DataRowFieldInfo endDateValue = reportDataRow.getValue(endDateReportField);
					String endDateEpochString = endDateValue.getKeyValue();
					if (!endDateEpochString.equals("")) {
						Long endDateEpoch = (Long.parseLong(endDateValue.getKeyValue()) - timezoneOffset) / 1000;
						if (endDateEpoch > eventDateEpoch) {
							jg.writeNumberField("end", endDateEpoch);
						}
					}
				}
				jg.writeStringField("className", "report_" + internalReportName); // fullcalendar
				// TODO: check if classname is used in UI, remove if not
				jg.writeStringField("classname", "report_" + internalReportName);
				jg.writeStringField("dateFieldInternalName", dateFieldInternalName);
				String eventTitle = buildEventTitle(report, reportDataRow, false);
				jg.writeStringField("title", eventTitle);
				jg.writeEndObject();
			}
			jg.writeEndArray();
			jg.flush();
			jg.close();
		} catch (IOException e) {
			throw new CodingErrorException("StringWriter produced an IO exception: " + e);
		}
		UsageLogger usageLogger = new UsageLogger(this.dataSource);
		usageLogger.logReportView(user, report, filterValues, 10000, "getCalendarJSON");
		UsageLogger.startLoggingThread(usageLogger);
		String json = stringWriter.toString();
		cachedJSON = new CachedFeed(json);
		cachedCalendarJSONs.put(id, cachedJSON);
		int cacheMisses = this.calendarJsonCacheMisses.incrementAndGet();
		if (cacheMisses > 100) {
			logger.info("JSON cache hits: " + this.calendarJsonCacheHits + ", misses " + cacheMisses);
			this.calendarJsonCacheHits.set(0);
			this.calendarJsonCacheMisses.set(0);
		}
		return json;
	}

	/**
	 * TODO: perhaps move this static method to a more appropriate class
	 * 
	 * @param shortTitle
	 *          If true, return only the first part of the title
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
			switch (baseField.getDbType()) {
			case BOOLEAN:
				boolean reportFieldTrue = Helpers.valueRepresentsBooleanTrue(dataRowField.getKeyValue());
				if (reportFieldTrue) {
					eventTitleBuilder.append(reportField.getFieldName() + ", ");
					fieldCount++;
				}
				break;
			case INTEGER:
			case FLOAT:
				eventTitleBuilder.append(reportField.getFieldName()).append(" = ")
						.append(displayValue + ", ");
				fieldCount++;
				break;
			case SERIAL:
				eventTitleBuilder.append(reportField.getFieldName()).append(" = ")
						.append(dataRowField.getKeyValue() + ", ");
				fieldCount++;
				break;
			default:
				eventTitleBuilder.append(displayValue + ", ");
				fieldCount++;
			}
			if (shortTitle && (fieldCount > 3)) {
				break REPORT_FIELD_LOOP;
			}
		}
		int titleLength = eventTitleBuilder.length();
		if (titleLength > 1) {
			eventTitleBuilder.delete(eventTitleBuilder.length() - 2, eventTitleBuilder.length());
		}
		String eventTitle = eventTitleBuilder.toString();
		return eventTitle;
	}

	public List<DataRowInfo> getReportDataRows(AppUserInfo user, BaseReportInfo reportDefn,
			Map<BaseField, String> filterValues, boolean exactFilters,
			Map<BaseField, Boolean> sessionSorts, int rowLimit, QuickFilterType filterType,
			boolean lookupPostcodeLatLong) throws SQLException, CodingErrorException,
			CantDoThatException, ObjectNotFoundException {
		Connection conn = null;
		List<DataRowInfo> reportDataRows = null;
		CompanyInfo company = null;
		if (user != null) {
			company = user.getCompany();
		}
		try {
			conn = this.dataSource.getConnection();
			conn.setAutoCommit(false);
			ReportDataInfo reportData = this.getReportData(company, reportDefn, conn, true);
			reportDataRows = reportData.getReportDataRows(conn, user, filterValues, exactFilters,
					sessionSorts, rowLimit, filterType, lookupPostcodeLatLong);
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		if ((reportDataRows.size() > 0) && (filterValues.size() == 0)) {
			DataRowInfo firstRow = reportDataRows.get(0);
			this.topRecords.put(reportDefn, firstRow.getRowId());
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
		// If company specified: use the cache to look up report metadata. If
		// the
		// report data isn't cached, cache it now
		// Additionally, if updateCacheIfObsolete specified, update the cache if
		// it gets out of date
		ReportDataInfo reportData;
		boolean useCaching = (company != null);
		if (this.cachedReportDatas.containsKey(reportDefn)) {
			reportData = this.cachedReportDatas.get(reportDefn);
			if (useCaching && updateCacheIfObsolete) {
				Long companyDataLastChangedTime = getLastCompanyDataChangeTime(company);
				boolean dataChangedAfterCached = (companyDataLastChangedTime > reportData
						.getCacheCreationTime());
				if (dataChangedAfterCached && reportData.exceededCacheTime()) {
					boolean useSample = false;
					if ((reportDefn.getRowCount() > 1000) || (reportDefn.getRowCount() == 0)) {
						// If row count large or unknown
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
					reportFilterValues, false, QuickFilterType.AND);
			String filterArgs = null;
			List<ReportQuickFilterInfo> filtersUsed = null;
			// TODO: there is only one WHERE clause - there should be an
			// improvement
			// over a loop
			for (Map.Entry<String, List<ReportQuickFilterInfo>> whereClause : whereClauseMap.entrySet()) {
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
			statement = reportData.fillInFilterValues(filtersUsed, statement, false);
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
			return new HashMap<RelationField, List<DataRow>>(0);
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
		results.close();
		statement.close();
		return displayLookup;
	}

	public ChartDataInfo getChartData(CompanyInfo company, ChartInfo chart,
			Map<BaseField, String> reportFilterValues, boolean aggressiveCache) throws SQLException,
			CantDoThatException {
		boolean needSummary = (chart.getAggregateFunctions().size() > 0);
		if (!needSummary) {
			return null;
		}
		// Work out whether any user filters are active that could affect the
		// returned data. If there are, always look up direct from db
		ReportDataInfo reportData = new ReportData(null, chart.getReport(), false, false);
		Map<String, List<ReportQuickFilterInfo>> whereClauseMap = reportData.getWhereClause(
				reportFilterValues, false, QuickFilterType.AND);
		if (whereClauseMap.size() > 0) {
			return this.fetchChartData(chart, reportFilterValues);
		}
		ChartDataInfo chartData = this.cachedChartDatas.get(chart);
		if (chartData == null) {
			chartData = this.fetchChartData(chart, reportFilterValues);
			this.cachedChartDatas.put(chart, chartData);
			this.chartDataCacheMisses.incrementAndGet();
		} else {
			long cacheCreationTime = chartData.getCacheCreationTime();
			long lastDataChangeTime = getLastCompanyDataChangeTime(company);
			long lastSchemaChangeTime = this.getLastSchemaChangeTime(company);
			long interval = 0;
			if (aggressiveCache) {
				interval = 48 * 60 * 60 * 1000;
			}
			if (((cacheCreationTime - interval) <= lastDataChangeTime)
					|| ((cacheCreationTime - interval) <= lastSchemaChangeTime)) {
				chartData = this.fetchChartData(chart, reportFilterValues);
				this.cachedChartDatas.put(chart, chartData);
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
		return chartData;
	}

	/**
	 * Fetch direct from the database
	 */
	private ChartDataInfo fetchChartData(ChartInfo chart, Map<BaseField, String> reportFilterValues)
			throws CantDoThatException, SQLException {
		Set<ChartAggregateInfo> aggregateFunctions = chart.getAggregateFunctions();
		Set<ChartGroupingInfo> groupings = chart.getGroupings();
		List<ChartDataRowInfo> reportSummaryRows;
		reportSummaryRows = new LinkedList<ChartDataRowInfo>();
		Connection conn = null;
		PreparedStatement statement = null;
		try {
			conn = this.dataSource.getConnection();
			conn.setAutoCommit(false);
			// First, cache the set of display values for relation fields
			Map<ReportFieldInfo, Map<String, String>> displayLookups = new HashMap<ReportFieldInfo, Map<String, String>>();
			for (ChartGroupingInfo grouping : groupings) {
				ReportFieldInfo groupingReportField = grouping.getGroupingReportField();
				BaseField baseField = groupingReportField.getBaseField();
				if (baseField instanceof RelationField) {
					String relatedKey = ((RelationField) baseField).getRelatedField().getInternalFieldName();
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
			BaseReportInfo report = chart.getReport();
			ReportData.enableOptimisations(conn, report, true);
			statement = chart.getChartSqlPreparedStatement(conn, reportFilterValues, false);
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
									value = ((ReportCalcFieldInfo) groupingReportField).formatDate(dbValue);
								} else {
									DateField dateField = (DateField) baseField;
									value = (dateField.formatDate(dbValue));
									if (Integer.valueOf(dateField.getDateResolution()).equals(Calendar.DAY_OF_MONTH)) {
										Date previousDbValue = previousDateValues.get(groupingReportField);
										if (previousDbValue != null) {
											calendar.setTime(previousDbValue);
											int previousDayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
											calendar.setTime(dbValue);
											int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
											int difference = Math.abs(dayOfYear - previousDayOfYear);
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
							value = ((ReportCalcFieldInfo) groupingReportField).formatFloat(floatValue);
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
					DatabaseFieldType dbType = aggregateFunction.getReportField().getBaseField().getDbType();
					Double value = null;
					// deal with aggregate results which are timestamps
					// rather than doubles
					if ((!aggregateFunction.getAggregateFunction().equals(AggregateFunction.COUNT))
							&& (dbType.equals(DatabaseFieldType.TIMESTAMP))) {
						java.sql.Timestamp timestampValue = summaryResults.getTimestamp(resultColumn);
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
							DatabaseFieldType dbFieldType = ((ReportCalcFieldInfo) aggReportField).getDbType();
							if (dbFieldType.equals(DatabaseFieldType.FLOAT)) {
								precision = ((ReportCalcFieldInfo) aggReportField).getDecimalPrecision();
							}
						} else if (aggReportField.getBaseField() instanceof DecimalField) {
							precision = ((DecimalField) aggReportField.getBaseField()).getPrecision();
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
			ReportData.enableOptimisations(conn, report, false);
			float durationSecs = (System.currentTimeMillis() - startTime) / ((float) 1000);
			if (durationSecs > AppProperties.longSqlTime) {
				logger.debug("Long SELECT SQL execution time of " + durationSecs + " seconds for summary '"
						+ chart + "', statement = " + statement);
			}
			return new ChartData(reportSummaryRows, minAggValues, maxAggValues, grandTotals);
		} catch (SQLException sqlex) {
			throw new SQLException("Error getting report summary data " + chart + ": " + sqlex
					+ ". SQL = " + statement);
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	public Map<BaseField, BaseValue> getTableDataRow(SessionDataInfo sessionData, TableInfo table,
			int rowId, boolean logView) throws SQLException, ObjectNotFoundException,
			CantDoThatException, CodingErrorException {
		Connection conn = null;
		TableDataInfo tableData = new TableData(table);
		Map<BaseField, BaseValue> tableDataRow = null;
		// Only log the view if we're not looking at the top (default) record in
		// a report
		boolean logTheView = logView;
		if (logTheView) {
			BaseReportInfo sessionReport = sessionData.getReport();
			if (table.getReports().contains(sessionReport)) {
				Integer topRowId = topRecords.get(sessionReport);
				if (topRowId != null) {
					if (topRowId.equals(rowId)) {
						logTheView = false;
					}
				}
			}
		}
		try {
			conn = this.dataSource.getConnection();
			conn.setAutoCommit(false);
			tableDataRow = tableData.getTableDataRow(conn, rowId, logTheView);
			conn.commit();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		if (tableDataRow == null) {
			return new HashMap<BaseField, BaseValue>(0);
		} else {
			return tableDataRow;
		}
	}

	public String getTableDataRowJson(TableInfo table, int rowId) throws SQLException,
			ObjectNotFoundException, CantDoThatException, CodingErrorException {
		Map<BaseField, BaseValue> tableDataRow = this.getTableDataRow(null, table, rowId, false);
		JsonFactory jsonFactory = new JsonFactory();
		StringWriter stringWriter = new StringWriter(512);
		JsonGenerator jg;
		BaseField pKey = table.getPrimaryKey();
		try {
			jg = jsonFactory.createJsonGenerator(stringWriter);
			jg.writeStartObject();
			for (Map.Entry<BaseField, BaseValue> rowEntry : tableDataRow.entrySet()) {
				BaseField field = rowEntry.getKey();
				BaseValue value = rowEntry.getValue();
				String internalFieldName = field.getInternalFieldName();
				if (field.equals(pKey)) {
					jg.writeNumberField("rowId", ((IntegerValue) value).getValueInteger());
				} else if (value instanceof IntegerValue) {
					IntegerValue intVal = (IntegerValue) value;
					if (!intVal.isNull()) {
						jg.writeNumberField(internalFieldName, intVal.getValueInteger());
					}
				} else if (value instanceof CheckboxValue) {
					CheckboxValue checkVal = (CheckboxValue) value;
					if (!value.isNull()) {
						jg.writeBooleanField(internalFieldName, checkVal.getValueBoolean());
					}
				} else if (value instanceof DecimalValue) {
					DecimalValue decVal = (DecimalValue) value;
					if (!decVal.isNull()) {
						jg.writeNumberField(internalFieldName, decVal.getValueFloat());
					}
				} else {
					jg.writeStringField(internalFieldName, value.toString());
				}
			}
			jg.writeEndObject();
			jg.flush();
			jg.close();
		} catch (IOException ioex) {
			throw new CodingErrorException("JSON generation threw an IO exception: " + ioex);
		}
		return stringWriter.toString();
	}

	public int getNextRowId(SessionDataInfo sessionData, BaseReportInfo report, boolean forwardSearch)
			throws SQLException, CantDoThatException {
		Map<BaseField, String> reportFilterValues = sessionData.getReportFilterValues();
		ReportDataInfo reportData = new ReportData(null, report, false, false);
		Map<String, List<ReportQuickFilterInfo>> whereClauseMap = reportData.getWhereClause(
				reportFilterValues, false, QuickFilterType.AND);
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
		SQLCode += " ) AS rowids_q WHERE ";
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
			statement = reportData.fillInFilterValues(filtersUsed, statement, false);
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

	public boolean childDataRowsExist(TableInfo parentTable, int parentRowId, TableInfo childTable)
			throws SQLException {
		Connection conn = null;
		boolean exists = false;
		try {
			conn = this.dataSource.getConnection();
			conn.setAutoCommit(false);
			DataRowInfo dataRow = new DataRow(parentTable, parentRowId,
					new HashMap<BaseField, DataRowFieldInfo>());
			exists = dataRow.childDataRowsExist(conn, childTable);
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		return exists;
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
				// There will always be at least one row in the master report,
				// if there are no related rows the value will be null
				int rowId = results.getInt(1);
				if (!results.wasNull()) {
					relatedRowIds.add(rowId);
				}
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

	private void anonymizeComments(TableInfo table, HttpServletRequest request,
			List<String> capitalisedWords) throws CantDoThatException, SQLException,
			ObjectNotFoundException, DisallowedException {
		if (!request.getServerName().contains("backup")) {
			throw new CantDoThatException(
					"For safety, anonymisation can only run on a test/backup server");
		}
		if (!this.authManager.getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, table)) {
			AppUserInfo user = this.authManager.getLoggedInUser(request);
			throw new DisallowedException(user, PrivilegeType.MANAGE_TABLE, table);
		}
		Pattern capitalWordsPattern = Pattern.compile("[A-Z][a-z0-9]+");
		Random rand = new Random();
		Connection conn = null;
		try {
			conn = this.dataSource.getConnection();
			conn.setAutoCommit(false);
			BaseField commentsFeed = table.getField(HiddenFields.COMMENTS_FEED.getFieldName());
			BaseField pKey = table.getPrimaryKey();
			// Read existing comments and add to the capitalizedWords list
			Set<Integer> rowIds = new HashSet<Integer>();
			List<String> commentsList = new LinkedList<String>();
			String SQLCode = "SELECT " + pKey.getInternalFieldName() + ", "
					+ commentsFeed.getInternalFieldName() + " FROM " + table.getInternalTableName();
			SQLCode += " WHERE " + commentsFeed.getInternalFieldName() + " IS NOT NULL";
			PreparedStatement statement = conn.prepareStatement(SQLCode);
			ResultSet results = statement.executeQuery();
			while (results.next()) {
				rowIds.add(results.getInt(1));
				String comments = results.getString(2);
				commentsList.add(comments);
				// extract capitalised words
				Matcher matcher = capitalWordsPattern.matcher(comments);
				while (matcher.find()) {
					capitalisedWords.add(matcher.group());
				}
			}
			results.close();
			statement.close();
			SQLCode = "UPDATE " + table.getInternalTableName() + " SET "
					+ commentsFeed.getInternalFieldName() + "=?";
			SQLCode += " WHERE " + pKey.getInternalFieldName() + "=?";
			statement = conn.prepareStatement(SQLCode);
			int numComments = commentsList.size();
			for (int rowId : rowIds) {
				statement.setInt(2, rowId);
				// Choose a random comment and anonymize it
				String comment = commentsList.get(rand.nextInt(numComments));
				statement.setString(1, anonymiseNote(capitalisedWords, comment));
				int rowsAffected = statement.executeUpdate();
				if (rowsAffected != 1) {
					throw new SQLException("Update failed: returned " + rowsAffected + " rows: " + statement);
				}
			}
			statement.close();
			conn.commit();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	public void anonymiseData(TableInfo table, HttpServletRequest request,
			SessionDataInfo sessionData, Map<BaseField, FieldContentType> fieldContentTypes,
			List<FileItem> multipartItems) throws SQLException, CodingErrorException,
			CantDoThatException, InputRecordException, ObjectNotFoundException, DisallowedException,
			MissingParametersException {
		if (!request.getServerName().contains("backup")) {
			throw new CantDoThatException(
					"For safety, anonymisation can only run on a test/backup server");
		}
		Random randomGenerator = new Random();
		String[] alphabet = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N",
				"O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };
		// Get data we're going to anonymise
		List<DataRowInfo> dataRows = this.getReportDataRows(null, table.getDefaultReport(),
				new HashMap<BaseField, String>(), false, new HashMap<BaseField, Boolean>(0), -1,
				QuickFilterType.AND, false);
		// Build up list of names
		List<String> forenames = new LinkedList<String>();
		List<String> surnames = new LinkedList<String>();
		List<String> emailAddresses = new LinkedList<String>();
		List<String> companyNameParts = new LinkedList<String>();
		List<String> phoneNumbers = new LinkedList<String>();
		List<String> niNumbers = new LinkedList<String>();
		List<String> capitalisedWords = new LinkedList<String>();
		List<String> emailParts = new LinkedList<String>();
		Set<String> emailSuffixes = new LinkedHashSet<String>();
		emailSuffixes.add(".co.uk");
		emailSuffixes.add("com");
		emailSuffixes.add(".org.uk");
		emailSuffixes.add(".org");
		emailSuffixes.add(".net");
		emailSuffixes.add(".ac.uk");
		Pattern numeralPattern = Pattern.compile("[123456789]"); // no zero
		Pattern capitalWordsPattern = Pattern.compile("[A-Z][a-z0-9]+");
		int randomMultiplier = randomGenerator.nextInt(10) + 5;
		for (DataRowInfo dataRow : dataRows) {
			for (BaseField field : fieldContentTypes.keySet()) {
				FieldContentType contentType = fieldContentTypes.get(field);
				String keyValue = dataRow.getDataRowFields().get(field).getKeyValue();
				if (contentType.equals(FieldContentType.COMPANY_NAME)) {
					for (String part : keyValue.split("\\s")) {
						String cleanedPart = part.replace("(", "").replace(")", "");
						companyNameParts.add(cleanedPart);
						capitalisedWords.add(cleanedPart);
					}
				} else if (contentType.equals(FieldContentType.FULL_NAME)) {
					String fullName = keyValue;
					String surname = fullName.replaceAll("^.*\\s", "");
					String forename = fullName.substring(0, fullName.length() - surname.length());
					forenames.add(forename);
					surnames.add(surname);
					capitalisedWords.add(forename);
					capitalisedWords.add(surname);
				} else if (contentType.equals(FieldContentType.PHONE_NUMBER)) {
					if (keyValue.length() > 0) {
						StringBuffer phoneNumber = new StringBuffer("01632 ");
						for (int i = 0; i < 6; i++) {
							phoneNumber.append(randomGenerator.nextInt(10));
						}
						phoneNumbers.add(phoneNumber.toString());
					}
				} else if (contentType.equals(FieldContentType.NI_NUMBER)) {
					if (keyValue.length() > 0) {
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
					if (keyValue.contains("@")) {
						String emailSansSuffix = keyValue.trim().toLowerCase();
						for (String emailSuffix : emailSuffixes) {
							emailSansSuffix = emailSansSuffix.replaceAll(Pattern.quote(emailSuffix) + "$", "");
						}
						String[] emailComponents = emailSansSuffix.replace("@", ".").split("\\.");
						for (String emailComponent : emailComponents) {
							emailParts.add(emailComponent);
						}
					}
				} else if (contentType.equals(FieldContentType.NOTES)) {
					// extract capitalised words
					Matcher matcher = capitalWordsPattern.matcher(keyValue);
					while (matcher.find()) {
						capitalisedWords.add(matcher.group());
					}
				}
			}
		}
		// Anonymise
		Set<String> generatedCompanyNames = new HashSet<String>(1000);
		for (DataRowInfo dataRow : dataRows) {
			int rowId = dataRow.getRowId();
			LinkedHashMap<BaseField, BaseValue> dataToSave = new LinkedHashMap<BaseField, BaseValue>();
			for (BaseField field : fieldContentTypes.keySet()) {
				FieldContentType contentType = fieldContentTypes.get(field);
				if (contentType.equals(FieldContentType.COMPANY_NAME)) {
					int partIndex = randomGenerator.nextInt(companyNameParts.size());
					String companyName = "";
					int pass = 0;
					while (companyName.equals("") && (pass < 100)) {
						pass++;
						String companyNamePart = companyNameParts.get(partIndex);
						if (!companyNamePart.trim().toLowerCase().equals("ltd")) {
							companyName += companyNamePart + " ";
						}
						if (randomGenerator.nextBoolean()) {
							partIndex = randomGenerator.nextInt(companyNameParts.size());
							companyNamePart = companyNameParts.get(partIndex);
							companyName += companyNamePart + " ";
							if (!companyNamePart.trim().toLowerCase().equals("ltd")) {
								if (randomGenerator.nextBoolean()) {
									partIndex = randomGenerator.nextInt(companyNameParts.size());
									companyNamePart = companyNameParts.get(partIndex);
									companyName += companyNamePart + " ";
								}
							}
						}
						companyName = companyName.trim();
						// Company name may be unsuitable for a whole host
						// of reasons
						if (companyName.toLowerCase().equals("ltd")
								|| companyName.toLowerCase().endsWith(" the")
								|| companyName.toLowerCase().endsWith(" for")
								|| companyName.toLowerCase().endsWith(" &")
								|| companyName.toLowerCase().endsWith(" of") || companyName.matches("^\\W.*")
								|| generatedCompanyNames.contains(companyName)) {
							companyName = "";
						}
					}
					generatedCompanyNames.add(companyName);
					TextValue companyNameValue = new TextValueDefn(companyName);
					dataToSave.put(field, companyNameValue);
				} else if (contentType.equals(FieldContentType.FULL_NAME)) {
					int forenameIndex = randomGenerator.nextInt(forenames.size());
					String randomForename = forenames.get(forenameIndex);
					int surnameIndex = randomGenerator.nextInt(surnames.size());
					String randomSurname = surnames.get(surnameIndex);
					TextValue fullNameValue = new TextValueDefn(randomForename + randomSurname);
					dataToSave.put(field, fullNameValue);
				} else if (contentType.equals(FieldContentType.PHONE_NUMBER)) {
					String currentKey = dataRow.getDataRowFields().get(field).getKeyValue();
					if (!currentKey.equals("")) {
						int phoneIndex = randomGenerator.nextInt(phoneNumbers.size());
						TextValue phoneNumber = new TextValueDefn(phoneNumbers.get(phoneIndex));
						dataToSave.put(field, phoneNumber);
					}
				} else if (contentType.equals(FieldContentType.NI_NUMBER)) {
					int niIndex = randomGenerator.nextInt(niNumbers.size());
					TextValue niNumber = new TextValueDefn(niNumbers.get(niIndex));
					dataToSave.put(field, niNumber);
				} else if (contentType.equals(FieldContentType.EMAIL_ADDRESS)) {
					int dataRowIndex = randomGenerator.nextInt(dataRows.size());
					String currentKey = dataRow.getDataRowFields().get(field).getKeyValue();
					if (currentKey != null) {
						String emailAddress = "";
						if (currentKey.contains("@")) {
							int partIndex = randomGenerator.nextInt(emailParts.size());
							emailAddress = emailParts.get(partIndex);
							if (randomGenerator.nextBoolean()) {
								emailAddress += "." + emailParts.get(randomGenerator.nextInt(emailParts.size()));
							}
							emailAddress += "@";
							emailAddress += emailParts.get(randomGenerator.nextInt(emailParts.size()));
							if (randomGenerator.nextBoolean()) {
								emailAddress += ".com";
							} else if (randomGenerator.nextBoolean()) {
								emailAddress += ".co.uk";
							} else if (randomGenerator.nextBoolean()) {
								emailAddress += ".org.uk";
							} else if (randomGenerator.nextBoolean()) {
								emailAddress += ".ac.uk";
							} else if (randomGenerator.nextBoolean()) {
								emailAddress += ".net";
							} else {
								emailAddress += ".org";
							}
						}
						TextValue emailValue = new TextValueDefn(emailAddress);
						dataToSave.put(field, emailValue);
					}
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
				} else if (contentType.equals(FieldContentType.NOTES)) {
					// Replace all capitalised words with another capitalised
					// word
					int dataRowIndex = randomGenerator.nextInt(dataRows.size());
					String currentKey = dataRow.getDataRowFields().get(field).getKeyValue();
					String newKey = anonymiseNote(capitalisedWords, currentKey);
					TextValue textValue = new TextValueDefn(newKey);
					dataToSave.put(field, textValue);
				} else if (contentType.equals(FieldContentType.OTHER)) {
					int dataRowIndex = randomGenerator.nextInt(dataRows.size());
					DataRowInfo randomDataRow = dataRows.get(dataRowIndex);
					String randomKey = randomDataRow.getValue(field).getKeyValue();
					if (field instanceof TextField) {
						// Anonymise numbers within the text
						Matcher matcher = numeralPattern.matcher(randomKey);
						char[] keyChars = randomKey.toCharArray();
						while (matcher.matches()) {
							int position = matcher.start();
							keyChars[position] = alphabet[randomGenerator.nextInt(26)].charAt(0);
						}
						TextValue textValue = new TextValueDefn(String.valueOf(keyChars));
						dataToSave.put(field, textValue);
					} else if (field instanceof IntegerField) {
						String valueString = randomDataRow.getValue(field).getKeyValue();
						if (valueString != null) {
							if (!valueString.equals("")) {
								valueString = valueString.replace(",", "");
								int integer = Integer.valueOf(valueString);
								integer = integer * randomMultiplier;
								IntegerValue intValue = new IntegerValueDefn(integer);
								dataToSave.put(field, intValue);
							}
						}
					} else if (field instanceof DecimalField) {
						String valueString = randomDataRow.getValue(field).getKeyValue();
						if (valueString != null) {
							if (!valueString.equals("")) {
								valueString = valueString.replace(",", "");
								double decimal = Double.valueOf(valueString);
								decimal = decimal * randomMultiplier;
								DecimalValue decimalValue = new DecimalValueDefn(decimal);
								dataToSave.put(field, decimalValue);
							}
						}
					} else if (field instanceof RelationField) {
						IntegerValue intValue = new IntegerValueDefn(Integer.valueOf(randomKey));
						dataToSave.put(field, intValue);
					}
				} else {
					throw new CodingErrorException("Unhandled anonymisation content type " + contentType);
				}
			}
			this.saveRecord(request, table, dataToSave, false, rowId, sessionData, multipartItems);
		}
		this.anonymizeComments(table, request, capitalisedWords);
	}

	private static String anonymiseNote(List<String> capitalisedWords, String note) {
		String[] alphabet = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N",
				"O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };
		Pattern numeralPattern = Pattern.compile("[123456789]"); // no zero
		Pattern capitalWordsPattern = Pattern.compile("[A-Z][a-z0-9]+");
		String newNote = note;
		Matcher capitalWordMatcher = capitalWordsPattern.matcher(note);
		Random randomGenerator = new Random();
		while (capitalWordMatcher.find()) {
			int capitalWordIndex = randomGenerator.nextInt(capitalisedWords.size());
			String capitalWord = capitalisedWords.get(capitalWordIndex);
			newNote = newNote.replace(capitalWordMatcher.group(), capitalWord);
		}
		// Also anonymise numbers within the text
		Matcher numeralMatcher = numeralPattern.matcher(newNote);
		char[] keyChars = newNote.toCharArray();
		while (numeralMatcher.find()) {
			int position = numeralMatcher.start();
			keyChars[position] = alphabet[randomGenerator.nextInt(26)].charAt(0);
		}
		return String.valueOf(keyChars);
	}

	public BaseReportInfo getMostPopularReport(HttpServletRequest request, DatabaseInfo databaseDefn,
			AppUserInfo user) throws SQLException, CodingErrorException {
		BaseReportInfo mostPopularReport = this.userMostPopularReportCache.get(user);
		if (mostPopularReport != null) {
			return mostPopularReport;
		}
		String SQLCode = "SELECT report FROM dbint_log_report_view WHERE app_user=? AND app_timestamp > (now() - '2 months'::interval) GROUP BY report ORDER BY count(*) DESC LIMIT 50";
		Connection conn = null;
		AuthenticatorInfo authenticator = this.authManager.getAuthenticator();
		try {
			conn = this.dataSource.getConnection();
			conn.setAutoCommit(false);
			PreparedStatement statement = conn.prepareStatement(SQLCode);
			statement.setString(1, user.getUserName());
			ResultSet results = statement.executeQuery();
			while (results.next()) {
				String internalReportName = results.getString(1);
				try {
					TableInfo table = databaseDefn.findTableContainingReport(request, internalReportName);
					BaseReportInfo report = table.getReport(internalReportName);
					if (!report.equals(table.getDefaultReport())) {
						if (authenticator.loggedInUserAllowedToViewReport(request, report)) {
							results.close();
							statement.close();
							this.userMostPopularReportCache.put(user, report);
							return report;
						}
					}
				} catch (ObjectNotFoundException onfex) {
					// The report from the logs no longer exists
				} catch (DisallowedException dex) {
					// The user no longer has privileges on the most popular
					// report
				}
			}
			results.close();
			statement.close();
		} catch (SQLException sqlex) {
			logger.error("Unable to get most popular report from logs: " + sqlex);
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		return null;
	}

	public String toString() {
		return "DataManagement is a class for managing data (duh!)";
	}

	public static void logLastTableDataChangeTime(TableInfo table) {
		lastTableDataChangeTimes.put(table, System.currentTimeMillis());
	}

	public static long getLastTableDataChangeTime(TableInfo table) {
		Long lastTime = lastTableDataChangeTimes.get(table);
		if (lastTime == null) {
			logLastTableDataChangeTime(table);
			return lastTableDataChangeTimes.get(table);
		}
		return lastTime;
	}

	public void logLastDataChangeTime(HttpServletRequest request) throws ObjectNotFoundException {
		// Public user (not logged in) changes don't count
		// TODO: think of something better
		if (request.getRemoteUser() != null) {
			CompanyInfo company = this.authManager.getCompanyForLoggedInUser(request);
			setLastCompanyDataChangeTime(company);
		}
	}

	public void logLastSchemaChangeTime(HttpServletRequest request) throws ObjectNotFoundException {
		CompanyInfo company = this.authManager.getCompanyForLoggedInUser(request);
		this.setLastSchemaChangeTime(company);
	}

	private static Long getLastCompanyDataChangeTime(CompanyInfo company) {
		Long lastTime = lastCompanyDataChangeTimes.get(company);
		if (lastTime == null) {
			setLastCompanyDataChangeTime(company);
			return lastCompanyDataChangeTimes.get(company);
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
	private static void setLastCompanyDataChangeTime(CompanyInfo company) {
		lastCompanyDataChangeTimes.put(company, System.currentTimeMillis());
		// Note: clearing optional
		cachedCalendarJSONs.clear();
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

	// Static because used in static methods
	private static Map<String, CachedReportFeedInfo> cachedCalendarJSONs = new ConcurrentHashMap<String, CachedReportFeedInfo>();

	private Map<String, CachedReportFeedInfo> cachedReportFeeds = new ConcurrentHashMap<String, CachedReportFeedInfo>();

	private final DataSource dataSource;

	private final String webAppRoot;

	private final AuthManagerInfo authManager;

	/**
	 * Keep a record of the last time any schema or data change occurred for each
	 * company, to help inform caching
	 */
	private static Map<CompanyInfo, Long> lastCompanyDataChangeTimes = new ConcurrentHashMap<CompanyInfo, Long>();

	private static Map<TableInfo, Long> lastTableDataChangeTimes = new ConcurrentHashMap<TableInfo, Long>();

	private Map<CompanyInfo, Long> lastSchemaChangeTimes = new ConcurrentHashMap<CompanyInfo, Long>();

	private Map<AppUserInfo, BaseReportInfo> userMostPopularReportCache = new ConcurrentHashMap<AppUserInfo, BaseReportInfo>();

	/**
	 * In fields where some records have comments, keep track of those which
	 * definitely don't, so we don't have to look them up via SQL
	 */
	private Map<BaseField, Set<Integer>> noComments = new ConcurrentHashMap<BaseField, Set<Integer>>();

	private AtomicInteger reportDataCacheHits = new AtomicInteger();

	private AtomicInteger reportDataCacheMisses = new AtomicInteger();

	private AtomicInteger chartDataCacheHits = new AtomicInteger();

	private AtomicInteger chartDataCacheMisses = new AtomicInteger();

	private AtomicInteger calendarJsonCacheHits = new AtomicInteger();

	private AtomicInteger calendarJsonCacheMisses = new AtomicInteger();

	private AtomicInteger reportFeedCacheHits = new AtomicInteger();

	private AtomicInteger reportFeedCacheMisses = new AtomicInteger();

	/**
	 * Store the first record ID from each report, for use when logging record
	 * accesses (don't log if the current record is the top record, it's probably
	 * just been loaded as the defauls)
	 */
	private Map<BaseReportInfo, Integer> topRecords = new ConcurrentHashMap<BaseReportInfo, Integer>();

	private float uploadSpeed = 50000; // Default to 50KB per second

	private static final SimpleLogger logger = new SimpleLogger(DataManagement.class);

	private static final boolean timezoneAdjust = false;

}