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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import com.gtwm.pb.auth.DisallowedException;
import com.gtwm.pb.auth.PrivilegeType;
import com.gtwm.pb.model.interfaces.ModuleInfo;
import com.gtwm.pb.model.interfaces.CompanyInfo;
import com.gtwm.pb.model.interfaces.AppUserInfo;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.SessionDataInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.TileInfo;
import com.gtwm.pb.model.manageData.InputRecordException;
import com.gtwm.pb.model.manageData.fields.CheckboxValueDefn;
import com.gtwm.pb.model.manageData.fields.DateValueDefn;
import com.gtwm.pb.model.manageData.fields.DecimalValueDefn;
import com.gtwm.pb.model.manageData.fields.IntegerValueDefn;
import com.gtwm.pb.model.manageData.fields.TextValueDefn;
import com.gtwm.pb.model.manageData.fields.FileValueDefn;
import com.gtwm.pb.model.interfaces.DatabaseInfo;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.fields.BaseValue;
import com.gtwm.pb.model.interfaces.fields.CheckboxField;
import com.gtwm.pb.model.interfaces.fields.DateField;
import com.gtwm.pb.model.interfaces.fields.DateValue;
import com.gtwm.pb.model.interfaces.fields.DecimalField;
import com.gtwm.pb.model.interfaces.fields.DurationField;
import com.gtwm.pb.model.interfaces.fields.IntegerField;
import com.gtwm.pb.model.interfaces.fields.RelationField;
import com.gtwm.pb.model.interfaces.fields.TextField;
import com.gtwm.pb.model.interfaces.fields.FileField;
import com.gtwm.pb.model.interfaces.fields.TextValue;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.Enumerations.SessionAction;
import com.gtwm.pb.util.Enumerations.TextCase;
import com.gtwm.pb.util.Helpers;
import com.gtwm.pb.util.MissingParametersException;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.AgileBaseException;
import com.gtwm.pb.util.Enumerations.DatabaseFieldType;
import com.gtwm.pb.model.manageData.SessionData;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.FileItem;
import org.grlea.log.SimpleLogger;

public final class ServletSessionMethods {

	private ServletSessionMethods() {
	}

	/**
	 * Set the data record ID to identify a particular table record.
	 * 
	 * Http request usage examples:
	 * 
	 * 1) &set_row_id=50 - set the row id for the current session table
	 * 
	 * 2) &set_row_id=next - set the row id to that of the next record in the
	 * current filtered session report
	 * 
	 * 3) &set_row_id=previous - set the row id to that of the previous record
	 * in the current filtered session report
	 * 
	 * 4) &set_row_id=50&rowidinternaltablename=a2a5e30cb86a5513f - set the row
	 * id for a table identified by internal table name (constant throughout
	 * life of table)
	 * 
	 * 5) &set_row_id=50&rowidinternaltablename=Contacts - set the row id for a
	 * table identified by name (name may change)
	 * 
	 * @throws ObjectNotFoundException
	 *             If a record with the specified row ID isn't found in the
	 *             table
	 */
	public static void setRowId(SessionDataInfo sessionData, HttpServletRequest request,
			String rowIdString, DatabaseInfo databaseDefn, SessionAction sessionAction)
			throws ObjectNotFoundException, DisallowedException, CantDoThatException, SQLException {
		String internalTableName = null;
		switch (sessionAction) {
		case SET_ROW_ID:
			internalTableName = request.getParameter("rowidinternaltablename");
			break;
		case PRESET_ROW_ID:
			internalTableName = request.getParameter("preset_rowidinternaltablename");
			break;
		default:
			internalTableName = request.getParameter("rowidinternaltablename");
			logger.error("Unknown session action when setting row ID: " + sessionAction);
		}
		if (internalTableName == null) {
			int rowId = -1;
			if (rowIdString.toLowerCase().equals("next")) {
				rowId = databaseDefn.getDataManagement().getNextRowId(sessionData,
						sessionData.getReport(), true);
			} else if (rowIdString.toLowerCase().equals("previous")) {
				rowId = databaseDefn.getDataManagement().getNextRowId(sessionData,
						sessionData.getReport(), false);
			} else {
				rowId = Integer.valueOf(rowIdString);
			}
			sessionData.setRowId(rowId);
		} else {
			TableInfo table = databaseDefn.getTable(request, internalTableName);
			BaseReportInfo report = table.getDefaultReport();
			String internalReportName = request.getParameter("rowidinternalreportname");
			if (internalReportName != null) {
				report = table.getReport(internalReportName);
			}
			int rowId = -1;
			if (rowIdString.toLowerCase().equals("next")) {
				rowId = databaseDefn.getDataManagement().getNextRowId(sessionData, report, true);
			} else if (rowIdString.toLowerCase().equals("previous")) {
				rowId = databaseDefn.getDataManagement().getNextRowId(sessionData, report, false);
			} else {
				rowId = Integer.valueOf(rowIdString);
			}
			sessionData.setRowId(table, rowId);
		}
	}

	/**
	 * Sets an override so the user will be able to edit a locked record. The
	 * lock on the record specified by the current session table and ID is
	 * overridden
	 * 
	 * @throws DisallowedException
	 *             If the user doesn't have manage privileges on the session
	 *             table
	 */
	public static void setLockOverride(SessionDataInfo sessionData, HttpServletRequest request,
			DatabaseInfo databaseDefn) throws ObjectNotFoundException, DisallowedException {
		TableInfo table = sessionData.getTable();
		if (table == null) {
			throw new ObjectNotFoundException("No table found in the session");
		}
		if (!(databaseDefn.getAuthManager().getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.EDIT_TABLE_DATA, table))) {
			throw new DisallowedException(databaseDefn.getAuthManager().getLoggedInUser(request),
					PrivilegeType.EDIT_TABLE_DATA, table);
		}
		((SessionData) sessionData).setRecordLockOverride();
	}

	/**
	 * Set the report the client is currently using. Also sets the current table
	 * to the report's parent table.
	 * 
	 * Http request usage:
	 * 
	 * &set_report=a2a5e30cb86a5513f (internal report name)
	 */
	public static void setReport(HttpServletRequest request, SessionDataInfo sessionData,
			String internalReportName, DatabaseInfo databaseDefn) throws ObjectNotFoundException,
			SQLException, DisallowedException {
		// The report's parent table should be known, either
		// by it being set in the same request or previously - retrieve it so we
		// can use it to obtain
		// the report object
		TableInfo table = sessionData.getTable();
		BaseReportInfo report = null;
		if (table == null) {
			// If no table - could be possible if there was a session timeout or
			// setSessionReport is
			// being called without first having called setSessionTable sometime
			// in the same session - then
			// we have to search for it
			table = databaseDefn.findTableContainingReport(request, internalReportName);
		}
		try {
			report = table.getReport(internalReportName);
		} catch (ObjectNotFoundException onfex) {
			// Still possible that table in session is a different one to that
			// containing the report we're
			// trying to set - fall back to looking up the correct table
			table = databaseDefn.findTableContainingReport(request, internalReportName);
			report = table.getReport(internalReportName);
		}
		sessionData.setReport(report);
	}

	/**
	 * Set the table the client is currently using. If this is the first time
	 * that this table has been set as the current one, then also sets the
	 * current report to the default report of that table.
	 * 
	 * Http request usage examples:
	 * 
	 * 1) &set_table=a2a5e30cb86a5513f - set the table by internal name
	 * 
	 * 2) &set_table=Contacts - set the table by name (name may change)
	 * 
	 * 3) &postset_table=a2a5e30cb86a5513f - set the table <b>after</b> doing
	 * all other session and application actions. This can be useful if running
	 * through a wizard for example, when you want to perform actions on the
	 * current table and then set the table to a different one ready for the
	 * next wizard page
	 */
	public static void setTable(SessionDataInfo sessionData, HttpServletRequest request,
			String tableInternalName, DatabaseInfo databaseDefn) throws ObjectNotFoundException,
			SQLException, DisallowedException {
		TableInfo table = databaseDefn.getTable(request, tableInternalName);
		sessionData.setTable(table);
	}

	/**
	 * Set a custom string variable in the session, identified by a supplied key
	 * 
	 * Http request usage example:
	 * 
	 * &set_custom_string=true&stringkey=selecteditem&customstringvalue=
	 * phonenumber - set a 'selecteditem' value to 'phonenumber'
	 * 
	 * @see SessionDataInfo#getCustomString(String) See
	 *      SessionDataInfo.getCustomString(stringkey) to retrieve the value
	 */
	public static void setCustomString(SessionDataInfo sessionData, HttpServletRequest request)
			throws MissingParametersException {
		String key = request.getParameter("stringkey");
		// backwards compatibility
		if (key == null) {
			key = request.getParameter("key");
		}
		String value = request.getParameter("customstringvalue");
		if (value == null) {
			value = request.getParameter("value");
		}
		if (key == null || value == null) {
			throw new MissingParametersException(
					"stringkey and customstringvalue must be supplied to set a custom session string");
		}
		sessionData.setCustomString(key, value);
	}

	/**
	 * Set a custom integer variable in the session, identified by a supplied
	 * key
	 * 
	 * Http request usage example:
	 * 
	 * &set_custom_integer=true&integerkey=chosennumber&customintegervalue=5 -
	 * set a 'chosennumber' value to 5
	 * 
	 * @see SessionDataInfo#getCustomInteger(String) See
	 *      SessionDataInfo.getCustomInteger(stringkey) to retrieve the value
	 */
	public static void setCustomInteger(SessionDataInfo sessionData, HttpServletRequest request)
			throws MissingParametersException {
		String key = request.getParameter("integerkey");
		String valueString = request.getParameter("customintegervalue");
		if (key == null || valueString == null) {
			throw new MissingParametersException(
					"integerkey and customintegervalue must be supplied to set a custom session integer");
		}
		sessionData.setCustomInteger(key, Integer.valueOf(valueString));
	}

	/**
	 * Set a custom integer variable in the session, identified by a supplied
	 * key
	 * 
	 * Http request usage example:
	 * 
	 * &set_custom_long=true&longkey=chosennumber&customlongvalue=5 - set a
	 * 'chosennumber' value to 5
	 * 
	 * @see SessionDataInfo#getCustomLong(String) See
	 *      SessionDataInfo.getCustomLong(stringkey) to retrieve the value
	 */
	public static void setCustomLong(SessionDataInfo sessionData, HttpServletRequest request)
			throws MissingParametersException {
		String key = request.getParameter("longkey");
		String valueString = request.getParameter("customlongvalue");
		if (key == null || valueString == null) {
			throw new MissingParametersException(
					"longkey and customlongvalue must be supplied to set a custom session long");
		}
		sessionData.setCustomLong(key, Long.valueOf(valueString));
	}

	public static void setCustomBoolean(SessionDataInfo sessionData, HttpServletRequest request)
			throws MissingParametersException {
		String key = request.getParameter("booleankey");
		String valueString = request.getParameter("custombooleanvalue");
		if (key == null || valueString == null) {
			throw new MissingParametersException(
					"booleankey and customboolean value must be supplied to set a custom session boolean");
		}
		sessionData.setCustomBoolean(key, Helpers.valueRepresentsBooleanTrue(valueString));
	}

	public static void setCustomTable(SessionDataInfo sessionData, HttpServletRequest request,
			boolean beforeAppActions, DatabaseInfo databaseDefn) throws MissingParametersException,
			ObjectNotFoundException, DisallowedException {
		String postActionPrefix = "";
		if (!beforeAppActions) {
			postActionPrefix = "post";
		}
		String key = request.getParameter(postActionPrefix + "tablekey");
		String internalTableName = request.getParameter(postActionPrefix
				+ "custominternaltablename");
		String tableName = request.getParameter(postActionPrefix + "customtablename");
		if (key == null || (internalTableName == null && tableName == null)) {
			throw new MissingParametersException(
					"tablekey and custominternaltablename/customtablename must be supplied to set a custom session table");
		}
		TableInfo table;
		if (internalTableName != null) {
			table = databaseDefn.getTable(request, internalTableName);
		} else {
			table = databaseDefn.getTable(request, tableName);
		}
		sessionData.setCustomTable(key, table);
	}

	public static void setCustomReport(SessionDataInfo sessionData, HttpServletRequest request,
			boolean beforeAppActions, DatabaseInfo databaseDefn) throws MissingParametersException,
			ObjectNotFoundException, DisallowedException {
		String postActionPrefix = "";
		if (!beforeAppActions) {
			postActionPrefix = "post";
		}
		String key = request.getParameter(postActionPrefix + "reportkey");
		// identify the parent table
		String internalTableName = request.getParameter(postActionPrefix
				+ "custominternaltablename");
		String tableName = request.getParameter(postActionPrefix + "customtablename");
		// identify the report
		String internalReportName = request.getParameter(postActionPrefix
				+ "custominternalreportname");
		String reportName = request.getParameter(postActionPrefix + "customreportname");
		if (key == null || (internalReportName == null && reportName == null)
				|| (internalTableName == null && tableName == null && internalReportName == null)) {
			throw new MissingParametersException(
					"reportkey, custominternalreportname/(customreportname plus custominternaltablename/customtablename) must be supplied to set a custom session report");
		}
		TableInfo parentTable = null;
		BaseReportInfo report;
		if (internalReportName != null) {
			parentTable = databaseDefn.findTableContainingReport(request, internalReportName);
		} else {
			if (internalTableName != null) {
				parentTable = databaseDefn.getTable(request, internalTableName);
			} else {
				parentTable = databaseDefn.getTable(request, tableName);
			}
		}
		if (internalReportName != null) {
			report = parentTable.getReport(internalReportName);
		} else {
			report = parentTable.getReport(reportName);
		}
		sessionData.setCustomReport(key, report);
	}

	public static void setCustomField(SessionDataInfo sessionData, HttpServletRequest request,
			DatabaseInfo databaseDefn) throws MissingParametersException, ObjectNotFoundException,
			DisallowedException {
		String key = request.getParameter("fieldkey");
		// identify the parent table
		String internalTableName = request.getParameter("custominternaltablename");
		String tableName = request.getParameter("customtablename");
		// identify the field
		String internalFieldName = request.getParameter("custominternalfieldname");
		String fieldName = request.getParameter("customfieldname");
		if (key == null || (internalFieldName == null && fieldName == null)
				|| (internalTableName == null && tableName == null && internalFieldName == null)) {
			throw new MissingParametersException(
					"fieldkey, custominternalfieldname/(customfieldname plus custominternaltablename/customtablename) must be supplied to set a custom session field");
		}
		TableInfo parentTable = null;
		BaseField field;
		if (internalFieldName != null) {
			parentTable = databaseDefn.findTableContainingField(request, internalFieldName);
		} else {
			if (internalTableName != null) {
				parentTable = databaseDefn.getTable(request, internalTableName);
			} else {
				parentTable = databaseDefn.getTable(request, tableName);
			}
		}
		if (internalFieldName != null) {
			field = parentTable.getField(internalFieldName);
		} else {
			field = parentTable.getField(fieldName);
		}
		sessionData.setCustomField(key, field);
	}

	/**
	 * When a user posts an input form, store the fields & values posted in the
	 * session for later retrieval. Do a certain amount of input processing,
	 * e.g. '.4' for a number would become '0.4' so that it could be interpreted
	 * properly
	 * 
	 * TODO: Http usage example
	 * 
	 * @param sessionData
	 *            Current table is obtained from the session
	 * @param request
	 *            Contains request parameters for field name -> value
	 * @throws ObjectNotFoundException
	 *             If current table can't be found (probably because of session
	 *             timeout), or getFieldValue fails
	 * @throws CantDoThatException
	 *             If getFieldValue fails
	 */
	public static void setFieldInputValues(SessionDataInfo sessionData, HttpServletRequest request,
			boolean newRecord, DatabaseInfo databaseDefn, TableInfo table,
			List<FileItem> multipartItems) throws ObjectNotFoundException, DisallowedException,
			SQLException, CantDoThatException, CodingErrorException, FileUploadException,
			InputRecordException {
		Map<BaseField, BaseValue> fieldInputValues = new HashMap<BaseField, BaseValue>();
		// get the list of fields in the current session table
		Set<BaseField> fields = table.getFields();
		// If we get an exception with a particular field, remember it but carry
		// on saving the other field
		// values to the session
		Exception caughtException = null;
		BaseField fieldWithException = null;
		FIELDSLOOP: for (BaseField field : fields) {
			if (!field.getFieldCategory().savesData()) {
				continue FIELDSLOOP;
			}
			try {
				BaseValue fieldValue = getFieldValue(request, field, newRecord, databaseDefn,
						multipartItems);
				// The following logic is:
				// If we have a new record, and the field wasn't submitted, or
				// it was submitted as empty, look up the default value.
				// If the field was submitted as empty and there's no default,
				// the fieldValue will represent null, store it.
				// If no field was submitted at all and it's an existing record,
				// skip it.
				if (newRecord) {
					if (fieldValue == null) {
						fieldValue = getDefaultFieldValue(sessionData, request, field, databaseDefn);
					} else if (fieldValue.isNull()) {
						BaseValue defaultValue = getDefaultFieldValue(sessionData, request, field,
								databaseDefn);
						if (defaultValue != null) {
							fieldValue = defaultValue;
						}
					}
				}
				if (fieldValue != null) {
					fieldInputValues.put(field, fieldValue);
				}
			} catch (Exception ex) {
				caughtException = ex;
				fieldWithException = field;
			}
		}
		sessionData.setFieldInputValues(fieldInputValues);
		// Delayed error handling
		if (caughtException != null) {
			if (caughtException instanceof InputRecordException) {
				// If already an input record exception, just rethrow
				throw (InputRecordException) caughtException;
			} else {
				throw new InputRecordException(caughtException.getMessage(), fieldWithException,
						caughtException);
			}
		}
	}

	/**
	 * Get user input value for a field being editing or inserted as part of a
	 * record
	 * 
	 * @return An object containing the value of the specified field, as
	 *         provided in HTTP request data, or null if there was no value for
	 *         the field
	 * @throws SQLException
	 *             If date value can't be read from the database
	 * @throws ObjectNotFoundException
	 *             if getTableDataRow() fails when reading date from relational
	 *             db
	 * @throws CantDoThatException
	 *             Ditto
	 * @throws InputRecordException
	 *             If there was invalid user input for a field, e.g. a letter
	 *             for a number field
	 * @see #setSessionFieldInputValues(SessionDataInfo, HttpServletRequest,
	 *      boolean, DatabaseInfo, TableInfo, List) Called by
	 *      setSessionFieldInputValues
	 */
	private static BaseValue getFieldValue(HttpServletRequest request, BaseField field,
			boolean newRecord, DatabaseInfo databaseDefn, List<FileItem> multipartItems)
			throws SQLException, ObjectNotFoundException, CantDoThatException,
			CodingErrorException, FileUploadException, InputRecordException {
		BaseValue fieldValue = null;
		String internalFieldName = field.getInternalFieldName();
		String fieldValueString = ServletUtilMethods.getParameter(request, internalFieldName,
				multipartItems);
		DatabaseFieldType databaseFieldType = field.getDbType();
		// reduce common errors for numbers (commas, spaces at end, - signs on
		// their own)
		if (databaseFieldType.equals(DatabaseFieldType.INTEGER)
				|| databaseFieldType.equals(DatabaseFieldType.SERIAL)
				|| databaseFieldType.equals(DatabaseFieldType.FLOAT)) {
			if (fieldValueString != null) {
				fieldValueString = fieldValueString.trim().replace(",", "").replace("\u00A3", "")
						.replace("$", ""); // 00A3 = pound sign
				if (fieldValueString.endsWith("%")) {
					fieldValueString = fieldValueString.substring(0, fieldValueString.length() - 1);
				}
				if (fieldValueString.equals("-") || (fieldValueString.equals("-."))) {
					fieldValueString = "0";
				}
			}
		}
		switch (databaseFieldType) {
		case INTEGER:
		case SERIAL:
			fieldValue = getIntegerValue(field, fieldValueString);
			break;
		case FLOAT:
			fieldValue = getDecimalValue(field, fieldValueString);
			break;
		case TIMESTAMP:
			fieldValue = getTimestampValue(request, field, internalFieldName);
			break;
		case VARCHAR:
			if (fieldValueString != null) {
				if (field instanceof FileField && !fieldValueString.equals("")) {
					// file values are the only ones that can't represent null
					// just skip it if no filename specified
					fieldValue = new FileValueDefn(request, (FileField) field, multipartItems);
				} else {
					fieldValue = getTextValue(field, fieldValueString);
				}
			}
			break;
		case BOOLEAN:
			if (fieldValueString != null) {
				if (fieldValueString.equals("")) {
					fieldValue = new CheckboxValueDefn(null);
				} else {
					// fieldValueString should be 'true' to set a true value,
					// anything else means false
					fieldValue = new CheckboxValueDefn(Boolean.valueOf(fieldValueString));
				}
			}
			break;
		default:
			throw new CodingErrorException("Unrecognised field type " + databaseFieldType);
		}
		return fieldValue;
	}

	private static BaseValue getTextValue(BaseField field, String fieldValueString) {
		BaseValue fieldValue = null;
		if (fieldValueString.equals("")) {
			fieldValue = new TextValueDefn(null);
		} else {
			TextCase textCase = ((TextField) field).getTextCase();
			if (textCase != null) {
				fieldValueString = textCase.transform(fieldValueString);
			}
			TextValue textValue = new TextValueDefn(fieldValueString);
			if (field instanceof TextField) {
				if (((TextField) field).usesTags()) {
					String tagsCsv = Helpers.joinWith(textValue.toTags(), ", ");
					return new TextValueDefn(tagsCsv);
				}
			}
			if (textValue.isPhoneNumber()) {
				// GB phone numbers
				if (textValue.isPhoneNumberGB()) {
					// Extract and store optional country prefix and
					// optional extension.
					// Grab only the NSN part for formatting.
					// NSN part might include spaces or ')' and will
					// need to be removed.
					Matcher numberPartsGB = Pattern
							.compile(
									"^\\(?(?:(?:0(?:0|11)\\)?\\s?\\(?|\\+)(4[\\s\\(\\)]*4)\\)?\\s?\\(?(?:0\\)?\\s?\\(?)?|0)([1-9]\\d{1,4}\\)?[\\s\\d]+)((?:[x\\#])\\d{3,4})?$")
							.matcher(fieldValueString);
					if (numberPartsGB.matches()) {
						// Extract NSN part of GB number, trim it
						// and remove ')' if present
						if (numberPartsGB.group(2) != null) {
							String phoneNSNString = numberPartsGB.group(2).trim()
									.replaceAll("[\\)\\s]", "");
							// Format NSN part of GB number
							String phoneNSNFormattedString = formatPhoneNumberGB(phoneNSNString);
							// Extract +44 prefix if present
							String phonePrefixString = numberPartsGB.group(1);
							// Set prefix as 0 or as +44 and space
							if (phonePrefixString != null) {
								if (phonePrefixString.equals("44") || phonePrefixString.equals("4 4")) {
									phonePrefixString = "+44 ";
								}
							} else {
								phonePrefixString = "0";
							}
							// Extract extension
							boolean phoneHasExtension = false;
							String phoneExtensionString = null;
							if (numberPartsGB.group(3) != null) {
								phoneHasExtension = true;
								phoneExtensionString = " #" + numberPartsGB.group(3);
							}
							// Add prefix back on to NSN
							fieldValueString = phonePrefixString
									+ phoneNSNFormattedString;
							// Add extension back on to number
							if (phoneHasExtension) {
								fieldValueString += phoneExtensionString;
							}
						}
					}
				}
				// International phone numbers
				else if (textValue.isPhoneNumberInternational()) {
					// Extract extension
					boolean phoneHasExtension = false;
					String phoneExtensionString = null;
					if (fieldValueString.matches("[^x\\#]+([x\\#]\\d{3,4})")) {
						phoneHasExtension = true;
						phoneExtensionString = " " + fieldValueString.replaceAll("[^x\\#]+([x\\#]\\d{3,4})", "$1");
					}
					// Extract country code and number
					fieldValueString = fieldValueString.replaceAll(
							"\\(?(?:00\\)?\\s?\\(?|\\+)\\)?\\s?\\(?([1-9][\\d\\(\\)\\s]+).*", "$1");
					// Format international number
					fieldValueString = formatPhoneNumberInternational(fieldValueString);
					// Add + to country code and number
					fieldValueString = "+" + fieldValueString;
					// Add extension back on to number
					if (phoneHasExtension) {
						fieldValueString += phoneExtensionString;
					}
				} else {
					logger.warn("Number " + textValue + " recognised as a phone number but unable to detect GB or International");
				}
			} else {
				// Replace smart quotes with normal quotes and em
				// dashes with normal dashes
				fieldValueString = Helpers.smartCharsReplace(fieldValueString);
			}
			fieldValue = new TextValueDefn(fieldValueString);
		}
		return fieldValue;
	}

	private static BaseValue getTimestampValue(HttpServletRequest request, BaseField field,
			String internalFieldName) throws CantDoThatException,
			InputRecordException {
		BaseValue fieldValue = null;
		Set<String> httpParameters = request.getParameterMap().keySet();
		// Every date will include at least a year, use this to check if
		// the date value been specifically sent by the user
		if (httpParameters.contains(internalFieldName + "_years")) {
			DateValue dateFieldValue = new DateValueDefn(null, null, null, null, null, null);
			dateFieldValue.setDateResolution(((DateField) field).getDateResolution());
			// obtain values passed within the request, (if any)
			String partGotTo = "year";
			Integer years = null;
			Integer months = null;
			Integer days = null;
			Integer hours = null;
			Integer minutes = null;
			Integer seconds = null;
			try {
				years = ServletDataMethods.getIntegerParameterValue(request, internalFieldName
						+ "_years");
				partGotTo = "month";
				months = ServletDataMethods.getIntegerParameterValue(request, internalFieldName
						+ "_months");
				partGotTo = "day";
				days = ServletDataMethods.getIntegerParameterValue(request, internalFieldName
						+ "_days");
				partGotTo = "hour";
				hours = ServletDataMethods.getIntegerParameterValue(request, internalFieldName
						+ "_hours");
				partGotTo = "minute";
				minutes = ServletDataMethods.getIntegerParameterValue(request,
						internalFieldName + "_minutes");
				partGotTo = "second";
				seconds = ServletDataMethods.getIntegerParameterValue(request,
						internalFieldName + "_seconds");
				if (years != null) {
					dateFieldValue.set(Calendar.YEAR, years);
				}
				if (months != null) {
					dateFieldValue.set(Calendar.MONTH, months);
				}
				if (days != null) {
					dateFieldValue.set(Calendar.DAY_OF_MONTH, days);
				}
				if (hours != null) {
					dateFieldValue.set(Calendar.HOUR_OF_DAY, hours);
				}
				if (minutes != null) {
					dateFieldValue.set(Calendar.MINUTE, minutes);
				}
				if (seconds != null) {
					dateFieldValue.set(Calendar.SECOND, seconds);
				}
				// Additionally, allow delta values to be submitted (add
				// more on an as-needed basis)
				Integer days_delta = ServletDataMethods.getIntegerParameterValue(request,
						internalFieldName + "_days_delta");
				if (days_delta != null) {
					dateFieldValue.add(Calendar.DAY_OF_MONTH, days_delta);
				}
				Integer minutes_delta = ServletDataMethods.getIntegerParameterValue(request,
						internalFieldName + "_minutes_delta");
				if (minutes_delta != null) {
					dateFieldValue.add(Calendar.MINUTE, minutes_delta);
				}
			} catch (NumberFormatException nfex) {
				throw new InputRecordException("The " + partGotTo
						+ " is invalid because it needs to be a whole number", field, nfex);
			}
			// If date value is null, leave fieldValue as a null object as
			// well
			// or the dateField in the database will be set to null
			if (dateFieldValue.getValueDate() == null) {
				// However if all fields have specifically been set as null
				// by
				// the user then do set the fieldValue to the dateFieldValue
				// object representing null
				switch (((DateField) field).getDateResolution()) {
				case Calendar.YEAR:
					if (years == null) {
						fieldValue = dateFieldValue;
					}
					break;
				case Calendar.MONTH:
					if (years == null && months == null) {
						fieldValue = dateFieldValue;
					}
					break;
				case Calendar.DAY_OF_MONTH:
					if (years == null && months == null && days == null) {
						fieldValue = dateFieldValue;
					}
					break;
				case Calendar.HOUR_OF_DAY:
					if (years == null && months == null && days == null && hours == null) {
						fieldValue = dateFieldValue;
					}
					break;
				case Calendar.MINUTE:
					if (years == null && months == null && days == null && hours == null
							&& minutes == null) {
						fieldValue = dateFieldValue;
					}
					break;
				}
			} else {
				fieldValue = dateFieldValue;
			}
		}
		return fieldValue;
	}

	private static BaseValue getDecimalValue(BaseField field, String fieldValueString)
			throws InputRecordException {
		BaseValue fieldValue = null;
		if (fieldValueString != null) {
			if (fieldValueString.equals("")) {
				// empty string translated to null
				fieldValue = new DecimalValueDefn(null);
			} else {
				// reformat to ensure number can be recognised by Java
				// .4 -> 0.4
				// 4. -> 4.0
				// . -> 0.0
				// (pound sign)46.50 -> 46.50 - from the error logs, users
				// commonly input pound signs
				fieldValueString = fieldValueString.replace("\u00A3", "");
				fieldValueString = fieldValueString.replace("$", "");
				if (fieldValueString.startsWith(".")) {
					fieldValueString = "0" + fieldValueString;
				}
				if (fieldValueString.endsWith(".")) {
					fieldValueString = fieldValueString + "0";
				}
				try {
					fieldValue = new DecimalValueDefn(Double.valueOf(fieldValueString));
				} catch (NumberFormatException nfex) {
					throw new InputRecordException("Value " + fieldValueString
							+ " not allowed because a number needs to be entered", field, nfex);
				}
			}
		}
		return fieldValue;
	}

	private static BaseValue getIntegerValue(BaseField field, String fieldValueString)
			throws InputRecordException {
		BaseValue fieldValue = null;
		if (fieldValueString != null) {
			if (fieldValueString.equals("")) {
				fieldValue = new IntegerValueDefn(null);
			} else {
				try {
					fieldValue = new IntegerValueDefn(Integer.valueOf(fieldValueString));
				} catch (NumberFormatException nfex) {
					throw new InputRecordException("Value " + fieldValueString
							+ " not allowed because a whole number needs to be entered", field,
							nfex);
				}
			}
		}
		return fieldValue;
	}

	/**
	 * Format GB phone numbers to include a space so that when they're exported
	 * to CSV, spreadsheets recognise them as text rather than numbers. For
	 * numbers entered with incorrect spacing, correct the spacing.
	 * 
	 * Format phone number by type, based on
	 * http://www.aa-asterisk.org.uk/index.php/Number_format and
	 * http://www.aa-asterisk.org.uk/index.php/Regular_Expressions_for_Validating_and_Formatting_GB_Telephone_Numbers
	 * edited by Ian Galpin; @g1smd
	 */
	private static String formatPhoneNumberGB(String fieldValueString) {
		fieldValueString = fieldValueString.trim();
		// Find string length
		int fieldValueLength = fieldValueString.length();
		// [2+8] 2d, 55, 56, 70, 76 (not 7624)
		String pattern28 = "(?:2|5[56]|7(?:0|6(?:[013-9]|2[0-35-9]))).*";
		// [3+7] 11d, 1d1, 3dd, 80d, 84d, 87d, 9dd
		String pattern37 = "(?:1(?:1|\\d1)|3|8(?:0[08]|4[2-5]|7[0-3])|9[018]).*";
		// [5+5] 1dddd (12 areas)
		String pattern55 = "(?:1(?:3873|5(?:242|39[456])|697[347]|768[347]|9467)).*";
		// [5+4] 1dddd (1 area)
		String pattern54 = "(?:16977[23]).*";
		// [4+6] 1ddd, 7ddd (inc 7624) (not 70, 76)
		String pattern46 = "(?:1|7(?:[1-5789]|624)).*";
		// [4+5] 1ddd (40 areas)
		String pattern45 = "(?:1(?:2(?:0[48]|54|76|9[78])|3(?:6[34]|8[46])|4(?:04|20|6[01]|8[08])|5(?:2[47]|6[26])|6(?:06|29|35|47|59|95)|7(?:26|44|50|68)|8(?:27|37|84)|9(?:0[05]|35|4[69]|63|95))).*";
		// [3+6] 500, 800
		String pattern36 = "[58]00.*";
		// Format numbers by leading digits and length
		if (fieldValueLength == 10 && fieldValueString.matches(pattern28)) {
			Matcher m28 = Pattern.compile("^(\\d{2})(\\d{4})(\\d{4})$").matcher(fieldValueString);
			if (m28.matches()) {
				fieldValueString = m28.group(1) + " " + m28.group(2) + " " + m28.group(3);
			}
		} else if (fieldValueLength == 10 && fieldValueString.matches(pattern37)) {
			Matcher m37 = Pattern.compile("^(\\d{3})(\\d{3})(\\d{4})$").matcher(fieldValueString);
			if (m37.matches()) {
				fieldValueString = m37.group(1) + " " + m37.group(2) + " " + m37.group(3);
			}
		} else if (fieldValueLength == 10 && fieldValueString.matches(pattern55)) {
			Matcher m55 = Pattern.compile("^(\\d{5})(\\d{5})$").matcher(fieldValueString);
			if (m55.matches()) {
				fieldValueString = m55.group(1) + " " + m55.group(2);
			}
		} else if (fieldValueLength == 9 && fieldValueString.matches(pattern54)) {
			Matcher m54 = Pattern.compile("^(\\d{5})(\\d{4})$").matcher(fieldValueString);
			if (m54.matches()) {
				fieldValueString = m54.group(1) + " " + m54.group(2);
			}
		} else if (fieldValueLength == 10 && fieldValueString.matches(pattern46)) {
			Matcher m46 = Pattern.compile("^(\\d{4})(\\d{6})$").matcher(fieldValueString);
			if (m46.matches()) {
				fieldValueString = m46.group(1) + " " + m46.group(2);
			}
		} else if (fieldValueLength == 9 && fieldValueString.matches(pattern45)) {
			Matcher m45 = Pattern.compile("^(\\d{4})(\\d{5})$").matcher(fieldValueString);
			if (m45.matches()) {
				fieldValueString = m45.group(1) + " " + m45.group(2);
			}
		} else if (fieldValueLength == 9 && fieldValueString.matches(pattern36)) {
			Matcher m36 = Pattern.compile("^(\\d{3})(\\d{6})$").matcher(fieldValueString);
			if (m36.matches()) {
				fieldValueString = m36.group(1) + " " + m36.group(2);
			}
		} else if (fieldValueLength > 1) {
			fieldValueString = fieldValueString.charAt(0) + " " + fieldValueString.substring(1, 5)
					+ " " + fieldValueString.substring(5);
		}
		return fieldValueString;
	}

	/**
	 * Format international phone numbers to include a space so that when
	 * they're exported to CSV, spreadsheets recognise them as text rather than
	 * numbers. Edited by Ian Galpin @g1smd
	 */
	private static String formatPhoneNumberInternational(String fieldValueString) {
		fieldValueString = fieldValueString.trim();
		String fieldValueDigitsOnlyString = fieldValueString.replaceAll("[\\s\\(\\)]", "");
		// Single digit country codes
		String pattern1 = "(?:[17]).*";
		// Double digit country codes, but not 44
		String pattern2 = "(?:(2[07]|3[0123469]|4[01356789]|5[12345678]|6[0123456]|8[12469]|9[0123458])).*";
		// Triple digit country codes
		String pattern3 = "(?:(2[12345689]|3[578]|42|5[09]|6[789]|8[03578]|9[679])\\d).*";
		// Format international numbers by leading digits
		// Use country code from digits-only data
		// and remainer of number from original data (preserves spaces)
		if (fieldValueDigitsOnlyString.matches(pattern1)) {
			Matcher c1 = Pattern.compile("^(\\d{1})(.*)$").matcher(fieldValueDigitsOnlyString);
			Matcher m1 = Pattern.compile("^(?:(?:\\d\\)?\\s?){1})(.*)$").matcher(fieldValueString);
			if (c1.matches() && m1.matches()) {
				fieldValueString = c1.group(1) + " " + m1.group(1);
			}
		} else if (fieldValueDigitsOnlyString.matches(pattern2)) {
			Matcher c2 = Pattern.compile("^(\\d{2})(.*)$").matcher(fieldValueDigitsOnlyString);
			Matcher m2 = Pattern.compile("^(?:(?:\\d\\)?\\s?){2})(.*)$").matcher(fieldValueString);
			if (c2.matches() && m2.matches()) {
				fieldValueString = c2.group(1) + " " + m2.group(1);
			}
		} else if (fieldValueDigitsOnlyString.matches(pattern3)) {
			Matcher c3 = Pattern.compile("^(\\d{3})(.*)$").matcher(fieldValueDigitsOnlyString);
			Matcher m3 = Pattern.compile("^(?:(?:\\d\\)?\\s?){3})(.*)$").matcher(fieldValueString);
			if (c3.matches() && m3.matches()) {
				fieldValueString = c3.group(1) + " " + m3.group(1);
			}
		}
		return fieldValueString;
	}

	/**
	 * @return A default value for the specified table field
	 */
	private static BaseValue getDefaultFieldValue(SessionDataInfo sessionData,
			HttpServletRequest request, BaseField field, DatabaseInfo databaseDefn)
			throws ObjectNotFoundException, DisallowedException, SQLException, CantDoThatException,
			CodingErrorException {
		BaseValue fieldValue = null;
		if (field.hasDefault()) {
			if (field instanceof TextField) {
				TextField textField = (TextField) field;
				fieldValue = new TextValueDefn(textField.getDefault());
			} else if (field instanceof DateField) {
				DateField dateField = (DateField) field;
				Calendar defaultDate = dateField.getDefault();
				fieldValue = new DateValueDefn(defaultDate);
				((DateValue) fieldValue).setDateResolution(((DateField) field).getDateResolution());
			} else if (field instanceof DecimalField) {
				DecimalField decimalField = (DecimalField) field;
				fieldValue = new DecimalValueDefn(decimalField.getDefault());
			} else if (field instanceof DurationField) {
				fieldValue = ((DurationField) field).getDefault();
			} else if (field instanceof IntegerField) {
				IntegerField integerField = (IntegerField) field;
				fieldValue = new IntegerValueDefn(integerField.getDefault());
			} else if (field instanceof CheckboxField) {
				CheckboxField checkboxField = (CheckboxField) field;
				fieldValue = new CheckboxValueDefn(checkboxField.getDefault());
			}
		}
		// now deal with mandatory fields not having default values:
		if (field instanceof RelationField) {
			RelationField relationField = (RelationField) field;
			Boolean overrideDefaultToNull = Helpers.valueRepresentsBooleanTrue(request
					.getParameter("gtpb_override_relation_default_to_null"));
			// Only look up a value if the field's not set to default to null
			if ((!relationField.getDefaultToNull()) || overrideDefaultToNull) {
				// obtain a relevant primary key value from the related table's
				// default report
				// where a session rowid has been set for the related table, use
				// this value
				BaseField relatedField = relationField.getRelatedField();
				if (!relatedField.getTableContainingField().getPrimaryKey().equals(relatedField)) {
					throw new CantDoThatException(
							"Unable to generate default for related field; expecting relation on primary key");
				}
				Integer relatedRowId = sessionData.getRowId(relatedField.getTableContainingField());
				if (relatedRowId != null) {
					fieldValue = new IntegerValueDefn(Integer.valueOf(relatedRowId));
				} else {
					TableInfo table = relatedField.getTableContainingField();
					Map<BaseField, BaseValue> tableRow = databaseDefn.getDataManagement()
							.getTableDataRow(sessionData, table, -1, false);
					for (Map.Entry<BaseField, BaseValue> fieldValueEntry : tableRow.entrySet()) {
						if (fieldValueEntry.getKey().equals(relatedField)) {
							fieldValue = fieldValueEntry.getValue();
							break;
						}
					}
				}
			}
		}
		return fieldValue;
	}

	/**
	 * Set the user currently being edited/viewed by an administrator
	 * 
	 * TODO: Http usage example
	 * 
	 * @throws ObjectNotFoundException
	 *             If userName doesn't map to an existing user object
	 * @throws DisallowedException
	 *             If the currently logged in person isn't an administrator
	 */
	public static void setUser(SessionDataInfo sessionData, HttpServletRequest request,
			String internalUserName, DatabaseInfo databaseDefn) throws ObjectNotFoundException,
			DisallowedException {
		AppUserInfo appUser = databaseDefn.getAuthManager().getUserByInternalName(request,
				internalUserName, true);
		sessionData.setUser(appUser);
	}

	public static void setModule(SessionDataInfo sessionData, HttpServletRequest request,
			String internalModuleName, DatabaseInfo databaseDefn) throws AgileBaseException {
		CompanyInfo company = databaseDefn.getAuthManager().getCompanyForLoggedInUser(request);
		ModuleInfo module = company.getModuleByInternalName(internalModuleName);
		sessionData.setModule(module);
	}

	public static void setReportGlobalFilterString(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn)
			throws MissingParametersException, ObjectNotFoundException, DisallowedException {
		BaseReportInfo report = ServletUtilMethods.getReportForRequest(sessionData, request,
				databaseDefn, true);
		String filterString = request.getParameter("filterstring");
		if (filterString == null) {
			throw new MissingParametersException("'filterstring' parameter needed");
		}
		sessionData.setGlobalFilterString(report, filterString);
	}

	/**
	 * Set a 'quick filter' value
	 * 
	 * TODO: Http usage example
	 */
	public static void setReportFilterValue(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn)
			throws MissingParametersException, ObjectNotFoundException, CodingErrorException,
			DisallowedException {
		String internalFieldName = request.getParameter("internalfieldname");
		if (internalFieldName == null) {
			throw new MissingParametersException("'internalfieldname' parameter needed in request");
		}
		String fieldValue = request.getParameter("fieldvalue");
		if (fieldValue == null) {
			throw new MissingParametersException("'fieldvalue' parameter needed in request");
		}
		try {
			fieldValue = URLDecoder.decode(fieldValue, "UTF-8");
		} catch (UnsupportedEncodingException ueex) {
			throw new CodingErrorException("Unable to decode string: " + ueex);
		}
		String filterSet = request.getParameter("customfilterset");
		// get the actual field object from the field name
		BaseField filterField;
		try {
			filterField = sessionData.getReport().getReportField(internalFieldName).getBaseField();
		} catch (ObjectNotFoundException onfex) {
			// If not in session report, fall back to looking in entire
			// database.
			// Method will throw its own ObjectNotFoundException if still not
			// found.
			filterField = databaseDefn.findReportFieldByInternalName(request, internalFieldName)
					.getBaseField();
		}
		if (filterField.getDbType().equals(DatabaseFieldType.FLOAT)
				&& fieldValue.replaceAll("0", "").equals(".")) {
			fieldValue = "0";
		}
		if (filterSet == null) {
			sessionData.setReportFilterValue(filterField, fieldValue);
		} else {
			sessionData.setCustomReportFilterValue(filterSet, filterField, fieldValue);
		}
	}

	/**
	 * Clear all report quick filtering
	 */
	public static void clearAllReportFilterValues(SessionDataInfo sessionData) {
		sessionData.clearAllReportFilterValues();
	}

	public static void setReportSort(SessionDataInfo sessionData, HttpServletRequest request,
			DatabaseInfo databaseDefn) throws MissingParametersException, ObjectNotFoundException,
			CantDoThatException {
		String internalFieldName = request.getParameter("internalfieldname");
		String fieldName = request.getParameter("fieldname");
		internalFieldName = internalFieldName != null ? internalFieldName : "";
		fieldName = fieldName != null ? fieldName : "";
		if ((internalFieldName.equals("")) && (fieldName.equals(""))) {
			throw new MissingParametersException(
					"'internalfieldname' or 'fieldname' parameter needed in request");
		}
		String sortDirectionString = request.getParameter("sortdirection");
		if (sortDirectionString == null) {
			throw new MissingParametersException("'sortdirection' parameter needed in request");
		}
		// get the actual field object from the field name
		BaseField sortField;
		if (!internalFieldName.equals("")) {
			sortField = sessionData.getReport().getReportField(internalFieldName).getBaseField();
		} else {
			sortField = sessionData.getReport().getReportField(fieldName).getBaseField();
		}
		// throw an exception if we're trying to sort on a field type which
		// hasn't been implemented/tested yet
		if (sortField instanceof DurationField) {
			throw new CantDoThatException("Sorting on this type of field is not yet implemented");
		}
		Boolean sortAscending = Boolean.valueOf(sortDirectionString);

		// clear any previously set sorts:
		clearAllReportSorts(sessionData);
		// set the requested sort:
		sessionData.setReportSort(sortField, sortAscending);
	}

	public static void clearReportSort(SessionDataInfo sessionData, HttpServletRequest request,
			DatabaseInfo databaseDefn) throws MissingParametersException, ObjectNotFoundException {
		String internalFieldName = request.getParameter("internalfieldname");
		String fieldName = request.getParameter("fieldname");
		internalFieldName = internalFieldName != null ? internalFieldName : "";
		fieldName = fieldName != null ? fieldName : "";
		if ((internalFieldName.equals("")) && (fieldName.equals(""))) {
			throw new MissingParametersException(
					"'internalfieldname' or 'fieldname' parameter needed in request");
		}
		// get the actual field object from the field name
		BaseField sortField;
		if (!internalFieldName.equals("")) {
			sortField = sessionData.getReport().getReportField(internalFieldName).getBaseField();
		} else {
			sortField = sessionData.getReport().getReportField(fieldName).getBaseField();
		}
		sessionData.clearReportSort(sortField);
	}

	public static void clearAllReportSorts(SessionDataInfo sessionData) {
		sessionData.clearAllReportSorts();
	}
	
	public static void setAppId(SessionDataInfo sessionData, HttpServletRequest request, String appId) {
		sessionData.setAppId(appId);
	}
	
	public static void clearAppId(SessionDataInfo sessionData) {
		sessionData.setAppId(null);
	}
	
	public static void setTile(SessionDataInfo sessionData, DatabaseInfo databaseDefn, HttpServletRequest request, String internalTileName) throws DisallowedException, ObjectNotFoundException {
		AppUserInfo user = databaseDefn.getAuthManager().getLoggedInUser(request);
		if (internalTileName == null) {
			sessionData.setTile(null);
			return;
		}
		for (TileInfo tile : user.getTiles()) {
			if (tile.getInternalTileName().equals(internalTileName)) {
				sessionData.setTile(tile);
				return;
			}
		}
		throw new ObjectNotFoundException("Tile " + internalTileName + " not found for user " + user);
	}

	private static final SimpleLogger logger = new SimpleLogger(ServletSessionMethods.class);
}