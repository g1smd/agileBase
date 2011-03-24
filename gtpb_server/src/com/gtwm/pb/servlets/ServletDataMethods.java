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
import com.gtwm.pb.model.interfaces.AppUserInfo;
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
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.DataDependencyException;
import com.gtwm.pb.util.Enumerations.TextCase;
import com.gtwm.pb.util.Helpers;
import com.gtwm.pb.util.MissingParametersException;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.Enumerations.DatabaseFieldType;
import com.gtwm.pb.util.Enumerations.FieldContentType;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.FileItem;
import org.grlea.log.SimpleLogger;

/**
 * Methods to do with managing data (creating & deleting records etc.) to be
 * used by the main agileBase servlet AppController, or any other custom servlet
 * written for a particular application based on agileBase. The JavaDoc here
 * describes the HTTP requests that must be sent to use the methods.
 * 
 * Part of a set of four interfaces, ServletSchemaMethods to manage setting up
 * the database schema, ServletDataMethods to manage data editing, ServletSessionMethods and
 * ServletAuthMethods to do with users, roles and privileges
 * 
 * @see ServletSchemaMethods
 * @see ServletAuthMethods
 */
public final class ServletDataMethods {

	private ServletDataMethods() {
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
				fieldValueString = fieldValueString.trim().replace(",", "").replace("£", "")
						.replace("$", "");
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
								+ " not allowed because a whole number needs to be entered", field,
								nfex);
					}
				}
			}
		} else if (databaseFieldType.equals(DatabaseFieldType.INTERVAL)) {
			// For error reporting
			String partGotTo = "year";
			try {
				Integer years = getIntegerParameterValue(request, internalFieldName + "_years");
				if ((years > 0) && (years < 99)) {
					years += 2000;
				}
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
						+ " is invalid because it needs to be a whole number", field, nfex);
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
					// . -> 0.0
					// £46.50 -> 46.50 - from the error logs, users commonly
					// input £ signs
					fieldValueString = fieldValueString.replace("£", "");
					fieldValueString = fieldValueString.replace("$", "");
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
								+ " not allowed because a number needs to be entered", field, nfex);
					}
				}
			}
		} else if (databaseFieldType.equals(DatabaseFieldType.TIMESTAMP)) {
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
					years = getIntegerParameterValue(request, internalFieldName + "_years");
					partGotTo = "month";
					months = getIntegerParameterValue(request, internalFieldName + "_months");
					partGotTo = "day";
					days = getIntegerParameterValue(request, internalFieldName + "_days");
					partGotTo = "hour";
					hours = getIntegerParameterValue(request, internalFieldName + "_hours");
					partGotTo = "minute";
					minutes = getIntegerParameterValue(request, internalFieldName + "_minutes");
					partGotTo = "second";
					seconds = getIntegerParameterValue(request, internalFieldName + "_seconds");
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
					Integer days_delta = getIntegerParameterValue(request, internalFieldName
							+ "_days_delta");
					if (days_delta != null) {
						dateFieldValue.add(Calendar.DAY_OF_MONTH, days_delta);
					}
					Integer minutes_delta = getIntegerParameterValue(request, internalFieldName
							+ "_minutes_delta");
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
						TextCase textCase = ((TextField) field).getTextCase();
						if (textCase != null) {
							fieldValueString = textCase.transform(fieldValueString);
						}
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
							.getTableDataRow(table, -1);
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
			InputRecordException, CodingErrorException, CantDoThatException, FileUploadException,
			MissingParametersException {
		String internalTableName = ServletUtilMethods.getParameter(request, "internaltablename",
				multipartItems);
		TableInfo table;
		if (internalTableName == null) {
			table = sessionData.getTable();
		} else {
			table = databaseDefn.getTable(request, internalTableName);
		}
		if (table == null) {
			throw new ObjectNotFoundException(
					"'internaltablename' was not provided and there is no table in the session");
		}
		if (!(databaseDefn.getAuthManager().getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.EDIT_TABLE_DATA, table))) {
			throw new DisallowedException(databaseDefn.getAuthManager().getLoggedInUser(request),
					PrivilegeType.EDIT_TABLE_DATA, table);
		}
		int rowId = -1;
		if (!newRecord) {
			String stringRowId = ServletUtilMethods.getParameter(request, "rowid", multipartItems);
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
		// if (newRecord) {
		// clear the previously cached record data:
		sessionData.setFieldInputValues(new HashMap<BaseField, BaseValue>());
		// }
		// try {
		ServletSessionMethods.setFieldInputValues(sessionData, request, newRecord, databaseDefn, table,
				multipartItems);
		// then pass to DataManagement.saveRecord
		databaseDefn.getDataManagement().saveRecord(request, table,
				new LinkedHashMap<BaseField, BaseValue>(sessionData.getFieldInputValues()),
				newRecord, rowId, sessionData, multipartItems);
		sessionData.setLastAppActionRowId(rowId);
		// } finally {
		// clear the cached record data... this might need to be handled
		// elsewhere
		// or within a try-catch block to ensure cached data is dropped
		// sessionData.setFieldInputValues(new HashMap<BaseField, BaseValue>());
		// }
	}

	/**
	 * Clone a record excepting some fields which are uncloneable, i.e. any
	 * unique fields or file upload fields
	 */
	public static void cloneRecord(SessionDataInfo sessionData, HttpServletRequest request,
			DatabaseInfo databaseDefn, List<FileItem> multipartItems)
			throws ObjectNotFoundException, DisallowedException, SQLException,
			InputRecordException, CodingErrorException, CantDoThatException,
			MissingParametersException {
		String internalTableName = ServletUtilMethods.getParameter(request, "internaltablename",
				multipartItems);
		TableInfo table;
		if (internalTableName == null) {
			table = sessionData.getTable();
		} else {
			table = databaseDefn.getTable(request, internalTableName);
		}
		if (table == null) {
			throw new ObjectNotFoundException(
					"'internaltablename' was not provided and there is no table in the session");
		}
		if (!(databaseDefn.getAuthManager().getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.EDIT_TABLE_DATA, table))) {
			throw new DisallowedException(databaseDefn.getAuthManager().getLoggedInUser(request),
					PrivilegeType.EDIT_TABLE_DATA, table);
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
			InputRecordException, FileUploadException, MissingParametersException {
		String internalTableName = request.getParameter("internaltablename");
		TableInfo table;
		if (internalTableName == null) {
			table = sessionData.getTable();
		} else {
			table = databaseDefn.getTable(request, internalTableName);
		}
		if (table == null) {
			throw new ObjectNotFoundException(
					"'internaltablename' was not provided and there is no table in the session");
		}
		if (!(databaseDefn.getAuthManager().getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.MANAGE_TABLE, table))) {
			throw new DisallowedException(databaseDefn.getAuthManager().getLoggedInUser(request),
					PrivilegeType.MANAGE_TABLE, table);
		}
		// clear the cached record data:
		sessionData.setFieldInputValues(new HashMap<BaseField, BaseValue>());
		// false means we're editing existing records, not adding a new one
		// so only values for specified fields will be set, not all table fields
		ServletSessionMethods.setFieldInputValues(sessionData, request, false, databaseDefn,
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
			throw new DisallowedException(databaseDefn.getAuthManager().getLoggedInUser(request),
					PrivilegeType.EDIT_TABLE_DATA, table);
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
			throw new DisallowedException(databaseDefn.getAuthManager().getLoggedInUser(request),
					PrivilegeType.EDIT_TABLE_DATA, table);
		}
		// get import options
		String generateRowIdsString = ServletUtilMethods.getParameter(request, "generate_row_ids",
				multipartItems);
		boolean generateRowIds = Helpers.valueRepresentsBooleanTrue(generateRowIdsString);
		String quoteCharString = ServletUtilMethods.getParameter(request, "quote_char",
				multipartItems);
		if (quoteCharString == null) {
			quoteCharString = "\"";
		} else if (quoteCharString.equals("")) {
			quoteCharString = "\"";
		}
		char quoteChar = quoteCharString.charAt(0);
		String separatorString = ServletUtilMethods.getParameter(request, "separator",
				multipartItems);
		if (separatorString == null) {
			separatorString = ",";
		} else if (separatorString.equals("")) {
			separatorString = ",";
		}
		char separator = separatorString.charAt(0);
		String numHeaderLinesString = ServletUtilMethods.getParameter(request, "num_header_lines",
				multipartItems);
		if (numHeaderLinesString == null) {
			numHeaderLinesString = "0";
		} else if (numHeaderLinesString.equals("")) {
			numHeaderLinesString = "0";
		}
		int numHeaderLines = Integer.valueOf(numHeaderLinesString);
		String csvContent = ServletUtilMethods.getParameter(request, "csv_content", multipartItems);
		String useRelationDisplayValuesString = ServletUtilMethods.getParameter(request,
				"use_relation_display_values", multipartItems);
		boolean useRelationDisplayValues = Helpers
				.valueRepresentsBooleanTrue(useRelationDisplayValuesString);
		String importSequenceValuesString = ServletUtilMethods.getParameter(request,
				"import_sequence_values", multipartItems);
		boolean importSequenceValues = Helpers
				.valueRepresentsBooleanTrue(importSequenceValuesString);
		String bestGuessRelationsString = ServletUtilMethods.getParameter(request,
				"best_guess_relations", multipartItems);
		boolean requireExactRelationValues = !(Helpers
				.valueRepresentsBooleanTrue(bestGuessRelationsString));
		String importTypeString = ServletUtilMethods.getParameter(request, "import_type",
				multipartItems);
		boolean updateExistingRecords = false;
		String trimString = ServletUtilMethods.getParameter(request, "trim", multipartItems);
		boolean trim = Helpers.valueRepresentsBooleanTrue(trimString);
		String mergeString = ServletUtilMethods.getParameter(request, "merge", multipartItems);
		boolean merge = Helpers.valueRepresentsBooleanTrue(mergeString);
		BaseField recordIdentifierField = table.getPrimaryKey();
		if (importTypeString != null) {
			if (importTypeString.toLowerCase().equals("update")) {
				updateExistingRecords = true;
				String recordIdentifierFieldInternalName = ServletUtilMethods.getParameter(request,
						"record_identifier", multipartItems);
				if (recordIdentifierFieldInternalName != null) {
					if (!recordIdentifierFieldInternalName.equals("")) {
						recordIdentifierField = table.getField(recordIdentifierFieldInternalName);
						if (!recordIdentifierField.equals(table.getPrimaryKey())
								&& !recordIdentifierField.getUnique()) {
							throw new CantDoThatException(
									"The record identifier field "
											+ recordIdentifierField
											+ " must be unique - you can turn on the unique option in the field properties");
						}
					}
				}
			}
		}
		int affectedRecords = databaseDefn.getDataManagement().importCSV(request, table,
				updateExistingRecords, recordIdentifierField, generateRowIds, separator, quoteChar,
				numHeaderLines, useRelationDisplayValues, importSequenceValues,
				requireExactRelationValues, trim, merge, multipartItems, csvContent);
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
			throw new DisallowedException(databaseDefn.getAuthManager().getLoggedInUser(request),
					PrivilegeType.EDIT_TABLE_DATA, table);
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
			throw new DisallowedException(databaseDefn.getAuthManager().getLoggedInUser(request),
					PrivilegeType.EDIT_TABLE_DATA, table);
		}
		databaseDefn.getDataManagement().lockRecord(sessionData, table, rowId);
	}

	public static void anonymiseTableData(SessionDataInfo sessionData, HttpServletRequest request,
			DatabaseInfo databaseDefn, List<FileItem> multipartItems)
			throws ObjectNotFoundException, DisallowedException, SQLException,
			CodingErrorException, CantDoThatException, InputRecordException,
			MissingParametersException {
		TableInfo table = sessionData.getTable();
		if (table == null) {
			throw new ObjectNotFoundException("There's no table in the session");
		}
		if (!(databaseDefn.getAuthManager().getAuthenticator().loggedInUserAllowedTo(request,
				PrivilegeType.EDIT_TABLE_DATA, table))) {
			throw new DisallowedException(databaseDefn.getAuthManager().getLoggedInUser(request),
					PrivilegeType.EDIT_TABLE_DATA, table);
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