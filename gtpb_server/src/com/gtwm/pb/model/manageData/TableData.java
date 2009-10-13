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
package com.gtwm.pb.model.manageData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.grlea.log.SimpleLogger;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.fields.RelationField;
import com.gtwm.pb.model.interfaces.fields.BaseValue;
import com.gtwm.pb.model.interfaces.fields.DateValue;
import com.gtwm.pb.model.interfaces.fields.FileField;
import com.gtwm.pb.model.interfaces.fields.DateField;
import com.gtwm.pb.model.interfaces.fields.SeparatorField;
import com.gtwm.pb.model.interfaces.SessionDataInfo;
import com.gtwm.pb.model.interfaces.TableDataInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.manageData.fields.IntegerValueDefn;
import com.gtwm.pb.model.manageData.fields.CheckboxValueDefn;
import com.gtwm.pb.model.manageData.fields.DateValueDefn;
import com.gtwm.pb.model.manageData.fields.TextValueDefn;
import com.gtwm.pb.model.manageData.fields.DecimalValueDefn;
import com.gtwm.pb.model.manageData.fields.FileValueDefn;
import com.gtwm.pb.util.Enumerations.DatabaseFieldType;
import com.gtwm.pb.util.Enumerations.HiddenFields;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.CodingErrorException;

public class TableData implements TableDataInfo {

	private TableData() {
	}

	public TableData(TableInfo table) {
		this.table = table;
	}
	
	public boolean isRecordLocked(Connection conn, SessionDataInfo sessionData, int rowId)
			throws ObjectNotFoundException, SQLException {
		// Does the table support locking in the first place?
		if (! this.table.getRecordsLockable()) {
			return false;
		}
		// Check if any possible lock is overridden
		// If so, we don't have to do an SQL query
		TableInfo recordLockOverrideTable = ((SessionData) sessionData).getRecordLockOverrideTable();
		int recordLockOverrideRowId = ((SessionData) sessionData).getRecordLockOverrideRowId();
		if (recordLockOverrideTable != null) {
			if ((rowId == recordLockOverrideRowId) && (recordLockOverrideTable.equals(this.table))) {
				return false;
			}
		}
		String SQLCode = "SELECT " + this.table.getField(HiddenFields.LOCKED.getFieldName()).getInternalFieldName() + " FROM " + this.table.getInternalTableName() + " WHERE " + this.table.getPrimaryKey().getInternalFieldName() + " = " + rowId;
		Statement statement = conn.createStatement();
		ResultSet results = statement.executeQuery(SQLCode);
		if (results.next()) {
			boolean locked = results.getBoolean(1);
			results.close();
			statement.close();
			return locked;
		} else {
			results.close();
			statement.close();
			throw new ObjectNotFoundException("Can't tell if row ID " + rowId + " from table " + table + " is locked or not as the row can't be found. SQL = " + SQLCode);
		}
	}

	public Map<BaseField, BaseValue> getTableDataRow(Connection conn, int rowId)
			throws CantDoThatException, SQLException, ObjectNotFoundException, CodingErrorException {
		Map<BaseField, BaseValue> tableDataRow = new HashMap<BaseField, BaseValue>();
		String SQLCode = "SELECT * FROM " + this.table.getInternalTableName();
		if (rowId < 0) {
			SQLCode += " LIMIT 1";
		} else {
			SQLCode += " WHERE " + this.table.getPrimaryKey().getInternalFieldName() + " = ?";
		}
		PreparedStatement statement = null;
		ResultSet results = null;
		try {
			statement = conn.prepareStatement(SQLCode);
			if (rowId >= 0) {
				statement.setInt(1, rowId);
			}
			results = statement.executeQuery();
			if (results.next()) {
				FIELDSLOOP: for (BaseField tableField : this.table.getFields()) {
					if (tableField instanceof SeparatorField) {
						continue FIELDSLOOP;
					}
					String internalFieldName = tableField.getInternalFieldName();
					BaseField field = null;
					if (tableField instanceof RelationField) {
						field = ((RelationField) tableField).getRelatedField();
					} else {
						field = tableField;
					}
					BaseValue fieldValue = null;
					DatabaseFieldType dbType = field.getDbType();
					// Get a field value of the correct type
					if (dbType.equals(DatabaseFieldType.INTEGER)
							|| dbType.equals(DatabaseFieldType.SERIAL)) {
						Integer fieldValueInt = results.getInt(internalFieldName);
						if (results.wasNull()) {
							fieldValueInt = null;
						}
						fieldValue = new IntegerValueDefn(fieldValueInt);
					} else if (dbType.equals(DatabaseFieldType.TIMESTAMP)) {
						DateValue dateValue = new DateValueDefn(results
								.getTimestamp(internalFieldName));
						// Date from database may be null
						// if (dateValue.get(Calendar.MONTH) != null) {
						// dateValue.set(Calendar.MONTH,
						// dateValue.get(Calendar.MONTH) - 1);
						// }
						fieldValue = dateValue;
						try {
							((DateValueDefn) fieldValue).setDateResolution(((DateField) field)
									.getDateResolution());
						} catch (CantDoThatException cdtex) {
							throw new CodingErrorException("unexpected date resolution for "
									+ field.getFieldName() + " {" + field.getInternalFieldName()
									+ "}", cdtex);
						}
					} else if (dbType.equals(DatabaseFieldType.VARCHAR)) {
						String fieldValueString = results.getString(internalFieldName);
						if (field instanceof FileField) {
							fieldValue = new FileValueDefn(fieldValueString);
						} else {
							fieldValue = new TextValueDefn(fieldValueString);
						}
					} else if (dbType.equals(DatabaseFieldType.INTERVAL)) {
						// TODO: See below note
						fieldValue = new TextValueDefn(
								"DEBUG: Change me to parse this string into a DurationValue: "
										+ results.getString(internalFieldName));
					} else if (dbType.equals(DatabaseFieldType.FLOAT)) {
						Double fieldValueFloat = results.getDouble(internalFieldName);
						if (results.wasNull()) {
							fieldValueFloat = null;
						}
						fieldValue = new DecimalValueDefn(fieldValueFloat);
					} else if (dbType.equals(DatabaseFieldType.BOOLEAN)) {
						fieldValue = new CheckboxValueDefn(results.getBoolean(internalFieldName));
					} else {
						throw new CantDoThatException(
								"Error getting table data row from database. Field '"
										+ field.getFieldName() + "' is of an unrecognised type");
					}
					tableDataRow.put(field, fieldValue);
				}
			}
		} finally {
			if (results != null) {
				results.close();
			}
			if (statement != null) {
				statement.close();
			}
		}
		return tableDataRow;
	}

	public String toString() {
		return "TableData can't return actual "
				+ this.table.toString()
				+ " table data at this low level. Please use a higher level method such as one from ViewMethods to do so";
	}

	private TableInfo table = null;

	private static final SimpleLogger logger = new SimpleLogger(TableData.class);
}
