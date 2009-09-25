/*
 *  Copyright 2009 GT webMarque Ltd
 * 
 *  This file is part of GT portalBase.
 *
 *  GT portalBase is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  GT portalBase is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with GT portalBase.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gtwm.pb.servlets;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import com.gtwm.pb.auth.DisallowedException;
import com.gtwm.pb.auth.PrivilegeType;
import com.gtwm.pb.model.interfaces.ModuleInfo;
import com.gtwm.pb.model.interfaces.CompanyInfo;
import com.gtwm.pb.model.interfaces.AppUserInfo;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.SessionDataInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.manageData.InputRecordException;
import com.gtwm.pb.model.manageData.fields.CheckboxValueDefn;
import com.gtwm.pb.model.manageData.fields.DateValueDefn;
import com.gtwm.pb.model.manageData.fields.DecimalValueDefn;
import com.gtwm.pb.model.manageData.fields.DurationValueDefn;
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
import com.gtwm.pb.model.interfaces.fields.SeparatorField;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.DataDependencyException;
import com.gtwm.pb.util.Helpers;
import com.gtwm.pb.util.MissingParametersException;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.PortalBaseException;
import com.gtwm.pb.util.Enumerations.DatabaseFieldType;
import com.gtwm.pb.util.Enumerations.FieldContentType;
import com.gtwm.pb.model.manageData.SessionData;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.FileItem;
import org.grlea.log.SimpleLogger;

/**
 * Methods to do with managing data (creating & deleting records, managing the
 * session etc.) to be used by the main portalBase servlet AppController, or any
 * other custom servlet written for a particular application based on
 * portalBase. The JavaDoc here describes the HTTP requests that must be sent to
 * use the methods.
 * 
 * Part of a set of three interfaces, ServletSchemaMethods to manage setting up
 * the database schema, ServletDataMethods to manage data editing and
 * ServletAuthMethods to do with users, roles and privileges
 * 
 * @see ServletSchemaMethods
 * @see ServletAuthMethods
 */
public class ServletDataMethods {

	private ServletDataMethods() {
	}

	/**
	 * Set the data record ID to identify a particular table record.
	 * 
	 * Http request usage examples:
	 * 
	 * 1) &set_row_id=50 - set the row id for the current session table
	 * 
	 * 2) &set_row_id=50&rowidinternaltablename=a2a5e30cb86a5513f - set the row
	 * id for a table identified by internal table name (constant throughout
	 * life of table)
	 * 
	 * 3) &set_row_id=50&rowidtablename=Contacts - set the row id for a table
	 * identified by name (name may change)
	 * 
	 * @throws ObjectNotFoundException
	 *             If a record with the specified row ID isn't found in the
	 *             table
	 */
	public static void setSessionRowId(SessionDataInfo sessionData, HttpServletRequest request,
			String rowIdString, DatabaseInfo databaseDefn) throws ObjectNotFoundException,
			DisallowedException {
		String internalTableName = request.getParameter("rowidinternaltablename");
		String tableName = request.getParameter("rowidtablename");
		if (internalTableName == null && tableName == null) {
			sessionData.setRowId(Integer.valueOf(rowIdString));
		} else {
			TableInfo table;
			if (internalTableName != null) {
				table = databaseDefn.getTableByInternalName(request, internalTableName);
			} else {
				table = databaseDefn.getTableByName(request, tableName);
			}
			sessionData.setRowId(table, Integer.valueOf(rowIdString));
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
	public static void setSessionLockOverride(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn) throws ObjectNotFoundException,
			DisallowedException {
		TableInfo table = sessionData.getTable();
		if (table == null) {
			throw new ObjectNotFoundException("No table found in the session");
		}
		if (!(databaseDefn.getAuthManager().getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.EDIT_TABLE_DATA, table))) {
			throw new DisallowedException(PrivilegeType.EDIT_TABLE_DATA, table);
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
	public static void setSessionReport(HttpServletRequest request, SessionDataInfo sessionData,
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
			report = table.getReportByInternalName(internalReportName);
		} catch (ObjectNotFoundException onfex) {
			// Still possible that table in session is a different one to that
			// containing the report we're
			// trying to set - fall back to looking up the correct table
			table = databaseDefn.findTableContainingReport(request, internalReportName);
			report = table.getReportByInternalName(internalReportName);
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
	 * 2) &set_table=true&settablename=Contacts - set the table by name (name
	 * may change)
	 * 
	 * 3) &postset_table=a2a5e30cb86a5513f - set the table <b>after<.b> doing
	 * all other session and application actions. This can be useful if running
	 * through a wizard for example, when you want to perform actions on the
	 * current table and then set the table to a different one ready for the
	 * next wizard page
	 * 
	 * 4) &postset_table&postsettablename=Contacts - postset the table by name
	 */
	public static void setSessionTable(SessionDataInfo sessionData, HttpServletRequest request,
			String tableInternalName, DatabaseInfo databaseDefn) throws ObjectNotFoundException,
			SQLException, DisallowedException {
		TableInfo table;
		// specifying table name optional
		String tableName = request.getParameter("settablename");
		if (tableName == null) {
			tableName = request.getParameter("postsettablename");
		}
		if (tableName != null) {
			table = databaseDefn.getTableByName(request, tableName);
		} else {
			table = databaseDefn.getTableByInternalName(request, tableInternalName);
		}
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
	public static void setSessionCustomString(SessionDataInfo sessionData,
			HttpServletRequest request) throws MissingParametersException {
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
	 * &set_custom_integer=true&stringkey=chosennumber&customstringvalue=5 - set
	 * a 'chosennumber' value to 5
	 * 
	 * @see SessionDataInfo#getCustomInteger(String) See
	 *      SessionDataInfo.getCustomInteger(stringkey) to retrieve the value
	 */
	public static void setSessionCustomInteger(SessionDataInfo sessionData,
			HttpServletRequest request) throws MissingParametersException {
		String key = request.getParameter("integerkey");
		String valueString = request.getParameter("customintegervalue");
		if (key == null || valueString == null) {
			throw new MissingParametersException(
					"integerkey and customintegervalue must be supplied to set a custom session integer");
		}
		sessionData.setCustomInteger(key, Integer.valueOf(valueString));
	}

	public static void setSessionCustomBoolean(SessionDataInfo sessionData,
			HttpServletRequest request) throws MissingParametersException {
		String key = request.getParameter("booleankey");
		String valueString = request.getParameter("custombooleanvalue");
		if (key == null || valueString == null) {
			throw new MissingParametersException(
					"booleankey and customboolean value must be supplied to set a custom session boolean");
		}
		sessionData.setCustomBoolean(key, Helpers.valueRepresentsBooleanTrue(valueString));
	}

	public static void setSessionCustomTable(SessionDataInfo sessionData,
			HttpServletRequest request, boolean beforeAppActions, DatabaseInfo databaseDefn)
			throws MissingParametersException, ObjectNotFoundException, DisallowedException {
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
			table = databaseDefn.getTableByInternalName(request, internalTableName);
		} else {
			table = databaseDefn.getTableByName(request, tableName);
		}
		sessionData.setCustomTable(key, table);
	}

	public static void setSessionCustomReport(SessionDataInfo sessionData,
			HttpServletRequest request, boolean beforeAppActions, DatabaseInfo databaseDefn)
			throws MissingParametersException, ObjectNotFoundException, DisallowedException {
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
				parentTable = databaseDefn.getTableByInternalName(request, internalTableName);
			} else {
				parentTable = databaseDefn.getTableByName(request, tableName);
			}
		}
		if (internalReportName != null) {
			report = parentTable.getReportByInternalName(internalReportName);
		} else {
			report = parentTable.getReportByName(reportName);
		}
		sessionData.setCustomReport(key, report);
	}

	public static void setSessionCustomField(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn)
			throws MissingParametersException, ObjectNotFoundException, DisallowedException {
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
					"fieldkey, reportkey, custominternalfieldname/(customfieldname plus custominternaltablename/customtablename) must be supplied to set a custom session field");
		}
		TableInfo parentTable = null;
		BaseField field;
		if (internalFieldName != null) {
			parentTable = databaseDefn.findTableContainingField(request, internalFieldName);
		} else {
			if (internalTableName != null) {
				parentTable = databaseDefn.getTableByInternalName(request, internalTableName);
			} else {
				parentTable = databaseDefn.getTableByName(request, tableName);
			}
		}
		if (internalFieldName != null) {
			field = parentTable.getFieldByInternalName(internalFieldName);
		} else {
			field = parentTable.getFieldByName(fieldName);
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
	public static void setSessionFieldInputValues(SessionDataInfo sessionData,
			HttpServletRequest request, boolean newRecord, DatabaseInfo databaseDefn,
			TableInfo table, List<FileItem> multipartItems) throws ObjectNotFoundException,
			DisallowedException, SQLException, CantDoThatException, CodingErrorException,
			FileUploadException, InputRecordException {
		Map<BaseField, BaseValue> fieldInputValues = new HashMap<BaseField, BaseValue>();
		// get the list of fields in the current session table
		Set<BaseField> fields = table.getFields();
		// If we get an exception with a particular field, remember it but carry
		// on saving the other field
		// values to the session
		Exception caughtException = null;
		BaseField fieldWithException = null;
		FIELDSLOOP: for (BaseField field : fields) {
			if (field instanceof SeparatorField) {
				continue FIELDSLOOP;
			}
			try {
				BaseValue fieldValue = getFieldValue(sessionData, request, field, newRecord,
						databaseDefn, multipartItems);
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
							fieldValue = getDefaultFieldValue(sessionData, request, field,
									databaseDefn);
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
				throw new InputRecordException(caughtException.getMessage(), fieldWithException);
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
	private static BaseValue getFieldValue(SessionDataInfo sessionData, HttpServletRequest request,
			BaseField field, boolean newRecord, DatabaseInfo databaseDefn,
			List<FileItem> multipartItems) throws SQLException, ObjectNotFoundException,
			CantDoThatException, CodingErrorException, FileUploadException, InputRecordException {
		BaseValue fieldValue = null;
		String internalFieldName = field.getInternalFieldName();
		String fieldValueString = AppController.getParameter(request, internalFieldName,
				multipartItems);
		DatabaseFieldType databaseFieldType = field.getDbType();
		// reduce common errors for numbers (commas, spaces at end, - signs on
		// their own)
		if (databaseFieldType.equals(DatabaseFieldType.INTEGER)
				|| databaseFieldType.equals(DatabaseFieldType.SERIAL)
				|| databaseFieldType.equals(DatabaseFieldType.FLOAT)) {
			if (fieldValueString != null) {
				fieldValueString = fieldValueString.trim().replaceAll(",", "");
				if (fieldValueString.endsWith("%")) {
					fieldValueString = fieldValueString.substring(0, fieldValueString.length() - 1);
				}
				if (fieldValueString.equals("-") || (fieldValueString.equals("-."))) {
					fieldValueString = "0";
				}
			}
		}
		if (databaseFieldType.equals(DatabaseFieldType.INTEGER)
				|| databaseFieldType.equals(DatabaseFieldType.SERIAL)) {
			if (fieldValueString != null) {
				if (fieldValueString.equals("")) {
					// empty string means null
					fieldValue = new IntegerValueDefn(null);
				} else {
					try {
						fieldValue = new IntegerValueDefn(Integer.valueOf(fieldValueString));
					} catch (NumberFormatException nfex) {
						throw new InputRecordException("Value " + fieldValueString
								+ " not allowed because a whole number needs to be entered", field);
					}
				}
			}
		} else if (databaseFieldType.equals(DatabaseFieldType.INTERVAL)) {
			// For error reporting
			String partGotTo = "year";
			try {
				Integer years = getIntegerParameterValue(request, internalFieldName + "_years");
				partGotTo = "month";
				Integer months = getIntegerParameterValue(request, internalFieldName + "_months");
				partGotTo = "day";
				Integer days = getIntegerParameterValue(request, internalFieldName + "_days");
				partGotTo = "hour";
				Integer hours = getIntegerParameterValue(request, internalFieldName + "_hours");
				partGotTo = "minute";
				Integer minutes = getIntegerParameterValue(request, internalFieldName + "_minutes");
				partGotTo = "second";
				Integer seconds = getIntegerParameterValue(request, internalFieldName + "_seconds");
				if (years != null && months != null && days != null && hours != null
						&& minutes != null && seconds != null) {
					fieldValue = new DurationValueDefn(years, months, days, hours, minutes, seconds);
				}
			} catch (NumberFormatException nfex) {
				throw new InputRecordException("The " + partGotTo
						+ " is invalid because it needs to be a whole number", field);
			}
		} else if (databaseFieldType.equals(DatabaseFieldType.FLOAT)) {
			if (fieldValueString != null) {
				if (fieldValueString.equals("")) {
					// empty string translated to null
					fieldValue = new DecimalValueDefn(null);
				} else {
					// reformat to ensure number can be recognised by Java
					// .4 -> 0.4
					// 4. -> 4.0
					// .  -> 0.0
					if (fieldValueString.startsWith(".")) {
						fieldValueString = "0" + fieldValueString;
					}
					if (fieldValueString.endsWith(".")) {
						fieldValueString = fieldValueString + "0";
					}
					try {
						fieldValue = new DecimalValueDefn(Double.valueOf(Double
								.valueOf(fieldValueString)));
					} catch (NumberFormatException nfex) {
						throw new InputRecordException("Value " + fieldValueString
								+ " not allowed because a number needs to be entered", field);
					}
				}
			}
		} else if (databaseFieldType.equals(DatabaseFieldType.TIMESTAMP)) {
			DateValue dateFieldValue = new DateValueDefn(null, null, null, null, null, null);
			dateFieldValue.setDateResolution(((DateField) field).getDateResolution());
			// obtain values passed within the request, (if any)
			String partGotTo = "year";
			try {
				Integer years = getIntegerParameterValue(request, internalFieldName + "_years");
				partGotTo = "month";
				Integer months = getIntegerParameterValue(request, internalFieldName + "_months");
				partGotTo = "day";
				Integer days = getIntegerParameterValue(request, internalFieldName + "_days");
				partGotTo = "hour";
				Integer hours = getIntegerParameterValue(request, internalFieldName + "_hours");
				partGotTo = "minute";
				Integer minutes = getIntegerParameterValue(request, internalFieldName + "_minutes");
				partGotTo = "second";
				Integer seconds = getIntegerParameterValue(request, internalFieldName + "_seconds");
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
			} catch (NumberFormatException nfex) {
				throw new InputRecordException("The " + partGotTo
						+ " is invalid because it needs to be a whole number", field);
			}
			// if date value is null, leave fieldValue as null as well or the
			// dateField in the database will be set to null
			if (dateFieldValue.getValueDate() != null) {
				fieldValue = dateFieldValue;
			}
		} else if (databaseFieldType.equals(DatabaseFieldType.VARCHAR)) {
			if (fieldValueString != null) {
				if (field instanceof FileField && !fieldValueString.equals("")) {
					// file values are the only ones that can't represent null
					// just skip it if no filename specified
					fieldValue = new FileValueDefn(request, (FileField) field, multipartItems);
				} else {
					if (fieldValueString.equals("")) {
						fieldValue = new TextValueDefn(null);
					} else {
						fieldValue = new TextValueDefn(fieldValueString);
					}
				}
			}
		} else if (databaseFieldType.equals(DatabaseFieldType.BOOLEAN)) {
			if (fieldValueString != null) {
				if (fieldValueString.equals("")) {
					fieldValue = new CheckboxValueDefn(null);
				} else {
					// fieldValueString should be 'true' to set a true value,
					// anything else means false
					fieldValue = new CheckboxValueDefn(Boolean.valueOf(fieldValueString));
				}
			}
		}
		return fieldValue;
	}

	/**
	 * Get the value of a single parameter in a HTTP request, or null if the
	 * parameter doesn't exist or is empty in the request. The parameter must be
	 * integer
	 * 
	 * @throws NumberFormatException
	 *             If the parameter value isn't an integer
	 */
	public static Integer getIntegerParameterValue(HttpServletRequest request, String parameterName) {
		String parameterValueString = request.getParameter(parameterName);
		if (parameterValueString != null) {
			if (!parameterValueString.equals("")) {
				return Integer.valueOf(parameterValueString);
			}
		}
		return null;
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
			// obtain a relevant primary key value from the related table's
			// default report
			// where a session rowid has been set for the related table, use
			// this value
			BaseField relatedField = ((RelationField) field).getRelatedField();
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
						.getTableDataRow(table, -1);
				for (Map.Entry<BaseField, BaseValue> fieldValueEntry : tableRow.entrySet()) {
					if (fieldValueEntry.getKey().equals(relatedField)) {
						// (field instanceof IntegerField) -- must be the case
						// if primary key field
						fieldValue = fieldValueEntry.getValue();
						break;
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
	public static void setSessionUser(SessionDataInfo sessionData, HttpServletRequest request,
			String internalUserName, DatabaseInfo databaseDefn) throws ObjectNotFoundException,
			DisallowedException {
		AppUserInfo appUser = databaseDefn.getAuthManager().getUserByInternalName(request,
				internalUserName);
		sessionData.setUser(appUser);
	}

	public static void setSessionModule(SessionDataInfo sessionData, HttpServletRequest request,
			String internalModuleName, DatabaseInfo databaseDefn) throws PortalBaseException {
		CompanyInfo company = databaseDefn.getAuthManager().getCompanyForLoggedInUser(request);
		//Findbugs found this unused variable
		//Set<ModuleInfo> modules = company.getModules();
		ModuleInfo module = company.getModuleByInternalName(internalModuleName);
		sessionData.setModule(module);
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
		String filterSet = request.getParameter("customfilterset");
		// get the actual field object from the field name
		BaseField filterField;
		try {
			filterField = sessionData.getReport().getReportFieldByInternalName(internalFieldName)
					.getBaseField();
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

	public static void setSessionReportSort(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn)
			throws MissingParametersException, ObjectNotFoundException, CantDoThatException {
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
			sortField = sessionData.getReport().getReportFieldByInternalName(internalFieldName)
					.getBaseField();
		} else {
			sortField = sessionData.getReport().getReportFieldByName(fieldName).getBaseField();
		}
		// throw an exception if we're trying to sort on a field type which
		// hasn't been implemented/tested yet
		if (sortField instanceof DurationField) {
			throw new CantDoThatException("Sorting on this type of field is not yet implemented");
		}
		Boolean sortAscending = Boolean.valueOf(sortDirectionString);

		// clear any previously set sorts:
		clearAllSessionReportSorts(sessionData);
		// set the requested sort:
		sessionData.setReportSort(sortField, sortAscending);
	}

	public static void clearSessionReportSort(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn)
			throws MissingParametersException, ObjectNotFoundException {
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
			sortField = sessionData.getReport().getReportFieldByInternalName(internalFieldName)
					.getBaseField();
		} else {
			sortField = sessionData.getReport().getReportFieldByName(fieldName).getBaseField();
		}
		sessionData.clearReportSort(sortField);
	}

	public static void clearAllSessionReportSorts(SessionDataInfo sessionData) {
		sessionData.clearAllReportSorts();
	}

	/**
	 * Insert or update a database record. Fields passed in the request are
	 * saved.
	 * 
	 * Null handling is as follows:
	 * 
	 * 1) If a field is passed to the request with an empty value, e.g. a form
	 * is submitted containing the field but no data has been entered for it,
	 * then the application will specifically insert a NULL into that field,
	 * both when creating new and editing existing records.
	 * 
	 * 2) If a field is skipped, e.g. isn't in the form in the first place or is
	 * disabled, it will be skipped in the SQL as well, i.e. it won't be
	 * included in the SQL INSERT or UPDATE statement. When creating records,
	 * this will result in a null value being inserted. When editing records,
	 * the current value will remain unchanged.
	 * 
	 * In both cases 1 and 2, if a default value property has been set for the
	 * field, that will be used instead of inserting NULL or skipping.
	 */
	public static void saveRecord(SessionDataInfo sessionData, HttpServletRequest request,
			boolean newRecord, DatabaseInfo databaseDefn, List<FileItem> multipartItems)
			throws ObjectNotFoundException, DisallowedException, SQLException,
			InputRecordException, CodingErrorException, CantDoThatException, FileUploadException {
		String internalTableName = AppController.getParameter(request, "internaltablename",
				multipartItems);
		TableInfo table;
		if (internalTableName == null) {
			table = sessionData.getTable();
		} else {
			table = databaseDefn.getTableByInternalName(request, internalTableName);
		}
		if (table == null) {
			throw new ObjectNotFoundException(
					"'internaltablename' was not provided and there is no table in the session");
		}
		if (!(databaseDefn.getAuthManager().getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.EDIT_TABLE_DATA, table))) {
			throw new DisallowedException(PrivilegeType.EDIT_TABLE_DATA, table);
		}
		int rowId = -1;
		if (!newRecord) {
			String stringRowId = AppController.getParameter(request, "rowid", multipartItems);
			if (stringRowId == null) {
				rowId = sessionData.getRowId();
				if (rowId == -1) {
					throw new ObjectNotFoundException("There's no record identifier in the session");
				}
			} else {
				// TODO: need to test sending up incorrect row id (e.g. incase
				// record being amended has been deleted by another user)
				rowId = Integer.parseInt(stringRowId);
			}
		}

		// Gather data to save, store it in the session
		if (newRecord) {
			// clear the cached record data:
			sessionData.setFieldInputValues(new HashMap<BaseField, BaseValue>());
		}
		try {
			setSessionFieldInputValues(sessionData, request, newRecord, databaseDefn, table,
					multipartItems);
			// then pass to DataManagement.saveRecord
			databaseDefn.getDataManagement().saveRecord(request, table,
					new LinkedHashMap<BaseField, BaseValue>(sessionData.getFieldInputValues()),
					newRecord, rowId, sessionData, multipartItems);
			sessionData.setLastAppActionRowId(rowId);
		} finally {
			// clear the cached record data... this might need to be handled
			// elsewhere
			// or within a try-catch block to ensure cached data is dropped
			sessionData.setFieldInputValues(new HashMap<BaseField, BaseValue>());
		}
	}

	/**
	 * Clone a record excepting some fields which are uncloneable, i.e. any
	 * unique fields or file upload fields
	 */
	public static void cloneRecord(SessionDataInfo sessionData, HttpServletRequest request,
			DatabaseInfo databaseDefn, List<FileItem> multipartItems)
			throws ObjectNotFoundException, DisallowedException, SQLException,
			InputRecordException, CodingErrorException, CantDoThatException {
		String internalTableName = AppController.getParameter(request, "internaltablename",
				multipartItems);
		TableInfo table;
		if (internalTableName == null) {
			table = sessionData.getTable();
		} else {
			table = databaseDefn.getTableByInternalName(request, internalTableName);
		}
		if (table == null) {
			throw new ObjectNotFoundException(
					"'internaltablename' was not provided and there is no table in the session");
		}
		if (!(databaseDefn.getAuthManager().getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.EDIT_TABLE_DATA, table))) {
			throw new DisallowedException(PrivilegeType.EDIT_TABLE_DATA, table);
		}
		int rowId = sessionData.getRowId();
		if (rowId == -1) {
			throw new ObjectNotFoundException("There's no record identifier in the session");
		}
		databaseDefn.getDataManagement().cloneRecord(request, table, rowId, sessionData,
				multipartItems);
		// Record the *new* row ID of the cloned record
		sessionData.setLastAppActionRowId(sessionData.getRowId());
	}

	public static void globalEdit(SessionDataInfo sessionData, HttpServletRequest request,
			DatabaseInfo databaseDefn, List<FileItem> multipartItems) throws DisallowedException,
			ObjectNotFoundException, CodingErrorException, SQLException, CantDoThatException,
			InputRecordException, FileUploadException {
		String internalTableName = request.getParameter("internaltablename");
		TableInfo table;
		if (internalTableName == null) {
			table = sessionData.getTable();
		} else {
			table = databaseDefn.getTableByInternalName(request, internalTableName);
		}
		if (table == null) {
			throw new ObjectNotFoundException(
					"'internaltablename' was not provided and there is no table in the session");
		}
		if (!(databaseDefn.getAuthManager().getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, table))) {
			throw new DisallowedException(PrivilegeType.MANAGE_TABLE, table);
		}
		// clear the cached record data:
		sessionData.setFieldInputValues(new HashMap<BaseField, BaseValue>());
		// false means we're editing existing records, not adding a new one
		// so only values for specified fields will be set, not all table fields
		ServletDataMethods.setSessionFieldInputValues(sessionData, request, false, databaseDefn,
				table, multipartItems);
		int affectedRecords = databaseDefn.getDataManagement().globalEdit(request, table,
				new LinkedHashMap<BaseField, BaseValue>(sessionData.getFieldInputValues()),
				sessionData, multipartItems);
		logDataChanges(request, databaseDefn, "globally edited " + affectedRecords + " records in "
				+ table.getTableName() + " (" + table.getInternalTableName() + ")");
		// clear the cached record data:
		sessionData.setFieldInputValues(new HashMap<BaseField, BaseValue>());
	}

	/**
	 * Delete the record of 'internaltablename' (or the session table if
	 * 'internaltablename'=="") identified by 'rowid' (or the session record if
	 * 'rowid'="")
	 * 
	 * @throws ObjectNotFoundException
	 *             If either a) table or row id object aren't in the session, b)
	 *             there is no record with the given rowId in the table, c)
	 *             there is more than one record with the given rowId in the
	 *             table
	 * @throws DisallowedException
	 *             If user doesn't have EDIT_TABLE_DATA privileges
	 */
	public static void removeRecord(SessionDataInfo sessionData, HttpServletRequest request,
			DatabaseInfo databaseDefn) throws MissingParametersException, ObjectNotFoundException,
			DisallowedException, SQLException, CodingErrorException, CantDoThatException,
			DataDependencyException {
		// obtain a reference to the table containing the record:
		TableInfo table = ServletUtilMethods.getTableForRequest(sessionData, request, databaseDefn,
				true);
		// obtain the rowid of the record to be deleted:
		String rowId = request.getParameter("rowid");
		int iRowId = -1;
		if (rowId == null) {
			iRowId = sessionData.getRowId();
		} else {
			try {
				iRowId = Integer.parseInt(rowId);
			} catch (RuntimeException rtex) {
				iRowId = -1;
			} catch (Exception e) {
				iRowId = -1;
			}
		}
		if (iRowId == -1) {
			throw new ObjectNotFoundException(
					"'rowid' was not provided and there is no record identifier in the session");
		}
		String cascadeValueString = request.getParameter("cascadedelete");
		boolean cascade = Helpers.valueRepresentsBooleanTrue(cascadeValueString);
		// having obtained valid parameters, delete the record:
		if (!(databaseDefn.getAuthManager().getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.EDIT_TABLE_DATA, table))) {
			throw new DisallowedException(PrivilegeType.EDIT_TABLE_DATA, table);
		}
		databaseDefn.getDataManagement().removeRecord(request, sessionData, databaseDefn, table,
				iRowId, cascade);
		ServletDataMethods.logDataChanges(request, databaseDefn, "deleted the record with rowid "
				+ rowId + " from " + table.getTableName() + " (" + table.getInternalTableName()
				+ ")");
		// Un-set the session record
		sessionData.setRowId(-1);
		sessionData.setLastAppActionRowId(iRowId);
	}

	public static void importRecords(SessionDataInfo sessionData, HttpServletRequest request,
			DatabaseInfo databaseDefn, List<FileItem> multipartItems) throws DisallowedException,
			ObjectNotFoundException, MissingParametersException, CantDoThatException, SQLException,
			IOException, InputRecordException, CodingErrorException {
		// obtain a reference to the table to import into:
		TableInfo table = ServletUtilMethods.getTableForRequest(sessionData, request, databaseDefn,
				true);
		if (!(databaseDefn.getAuthManager().getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.EDIT_TABLE_DATA, table))) {
			throw new DisallowedException(PrivilegeType.EDIT_TABLE_DATA, table);
		}
		// get import options
		String generateRowIdsString = AppController.getParameter(request, "generate_row_ids",
				multipartItems);
		boolean generateRowIds = Helpers.valueRepresentsBooleanTrue(generateRowIdsString);
		String quoteCharString = AppController.getParameter(request, "quote_char", multipartItems);
		if (quoteCharString == null) {
			quoteCharString = "\"";
		} else if (quoteCharString.equals("")) {
			quoteCharString = "\"";
		}
		char quoteChar = quoteCharString.charAt(0);
		String separatorString = AppController.getParameter(request, "separator", multipartItems);
		if (separatorString == null) {
			separatorString = ",";
		} else if (separatorString.equals("")) {
			separatorString = ",";
		}
		char separator = separatorString.charAt(0);
		String numHeaderLinesString = AppController.getParameter(request, "num_header_lines",
				multipartItems);
		if (numHeaderLinesString == null) {
			numHeaderLinesString = "0";
		} else if (numHeaderLinesString.equals("")) {
			numHeaderLinesString = "0";
		}
		int numHeaderLines = Integer.valueOf(numHeaderLinesString);
		String csvContent = AppController.getParameter(request, "csv_content", multipartItems);
		String useRelationDisplayValuesString = AppController.getParameter(request,
				"use_relation_display_values", multipartItems);
		boolean useRelationDisplayValues = Helpers
				.valueRepresentsBooleanTrue(useRelationDisplayValuesString);
		String importSequenceValuesString = AppController.getParameter(request,
				"import_sequence_values", multipartItems);
		boolean importSequenceValues = Helpers
				.valueRepresentsBooleanTrue(importSequenceValuesString);
		String bestGuessRelationsString = AppController.getParameter(request,
				"best_guess_relations", multipartItems);
		boolean requireExactRelationValues = !(Helpers
				.valueRepresentsBooleanTrue(bestGuessRelationsString));
		String importTypeString = AppController.getParameter(request, "import_type", multipartItems);
		boolean updateExistingRecords = false;
		BaseField recordIdentifierField = table.getPrimaryKey();
		if(importTypeString != null) {
			if(importTypeString.toLowerCase().equals("update")) {
				updateExistingRecords = true;
				String recordIdentifierFieldInternalName = AppController.getParameter(request, "record_identifier", multipartItems);
				if (recordIdentifierFieldInternalName != null) {
					if(!recordIdentifierFieldInternalName.equals("")) {
						recordIdentifierField = table.getFieldByInternalName(recordIdentifierFieldInternalName);
						if(!recordIdentifierField.equals(table.getPrimaryKey()) && !recordIdentifierField.getUnique()) {
							throw new CantDoThatException("The record identifier field " + recordIdentifierField + " must be unique - you can turn on the unique option in the field properties");
						}
					}
				}
			}
		}
		int affectedRecords = databaseDefn.getDataManagement().importCSV(request, table,
				updateExistingRecords, recordIdentifierField, generateRowIds, separator, quoteChar, numHeaderLines, useRelationDisplayValues,
				importSequenceValues, requireExactRelationValues, multipartItems, csvContent);
		logDataChanges(request, databaseDefn, "imported " + affectedRecords + " records into "
				+ table.getTableName() + " (" + table.getInternalTableName() + ")");
	}

	public static void lockRecords(SessionDataInfo sessionData, HttpServletRequest request,
			DatabaseInfo databaseDefn) throws DisallowedException, ObjectNotFoundException,
			SQLException, CodingErrorException, CantDoThatException {
		TableInfo table = sessionData.getReport().getParentTable();
		if (table == null) {
			throw new ObjectNotFoundException("There is no table in the session");
		}
		if (!(databaseDefn.getAuthManager().getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.EDIT_TABLE_DATA, table))) {
			throw new DisallowedException(PrivilegeType.EDIT_TABLE_DATA, table);
		}
		String lockType = request.getParameter("lock_type");
		if (lockType == null) {
			lockType = "filtered_report_records";
		}
		if (lockType.equals("filtered_report_records")) {
			databaseDefn.getDataManagement().lockReportRecords(request, sessionData);
		} else if (lockType.equals("all_records")) {
			databaseDefn.getDataManagement().lockAllTableRecords(request, sessionData);
		} else {
			throw new CantDoThatException(
					"lock_type must be either 'filtered_report_records' or 'all_records'");
		}
	}

	public static void lockRecord(SessionDataInfo sessionData, HttpServletRequest request,
			DatabaseInfo databaseDefn) throws DisallowedException, ObjectNotFoundException,
			MissingParametersException, SQLException, CodingErrorException, CantDoThatException {
		TableInfo table = ServletUtilMethods.getTableForRequest(sessionData, request, databaseDefn,
				ServletUtilMethods.USE_SESSION);
		int rowId = ServletUtilMethods.getRowIdForRequest(sessionData, request, databaseDefn,
				ServletUtilMethods.USE_SESSION);
		if (!(databaseDefn.getAuthManager().getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.EDIT_TABLE_DATA, table))) {
			throw new DisallowedException(PrivilegeType.EDIT_TABLE_DATA, table);
		}
		databaseDefn.getDataManagement().lockRecord(sessionData, table, rowId);
	}

	public static void anonymiseTableData(SessionDataInfo sessionData, HttpServletRequest request,
			DatabaseInfo databaseDefn, List<FileItem> multipartItems)
			throws ObjectNotFoundException, DisallowedException, SQLException,
			CodingErrorException, CantDoThatException, InputRecordException {
		TableInfo table = sessionData.getTable();
		if (table == null) {
			throw new ObjectNotFoundException("There's no table in the session");
		}
		if (!(databaseDefn.getAuthManager().getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.EDIT_TABLE_DATA, table))) {
			throw new DisallowedException(PrivilegeType.EDIT_TABLE_DATA, table);
		}
		// get field content types specified
		Map<BaseField, FieldContentType> fieldContentTypes = new HashMap<BaseField, FieldContentType>();
		for (BaseField field : table.getFields()) {
			String contentTypeString = request.getParameter(field.getInternalFieldName());
			if (contentTypeString != null) {
				if (!contentTypeString.equals("")) {
					FieldContentType fieldContentType = FieldContentType.valueOf(contentTypeString
							.toUpperCase());
					fieldContentTypes.put(field, fieldContentType);
				}
			}
		}
		databaseDefn.getDataManagement().anonymiseData(table, request, sessionData,
				fieldContentTypes, multipartItems);
	}

	private static void logDataChanges(HttpServletRequest request, DatabaseInfo databaseDefn,
			String operation) throws DisallowedException, ObjectNotFoundException {
		AppUserInfo currentUser = databaseDefn.getAuthManager().getUserByUserName(request,
				request.getRemoteUser());
		String fullname = currentUser.getForename() + " " + currentUser.getSurname();
		String timestamp = String.format(Locale.UK, "%1$td-%1$tb-%1$tY %1$tH:%1$tM:%1$tS",
				new Date());
		logger.info(fullname + " (" + currentUser + ") " + operation + " at " + timestamp);
	}

	private static final SimpleLogger logger = new SimpleLogger(ServletDataMethods.class);
}