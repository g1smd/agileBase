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
package com.gtwm.pb.model.manageData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.SortedSet;
import java.util.List;
import java.util.ArrayList;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.grlea.log.SimpleLogger;
import com.gtwm.pb.model.interfaces.DatabaseInfo;
import com.gtwm.pb.model.interfaces.DataRowFieldInfo;
import com.gtwm.pb.model.interfaces.DataRowInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.fields.RelationField;
import com.gtwm.pb.model.interfaces.fields.DateField;
import com.gtwm.pb.model.interfaces.fields.DateValue;
import com.gtwm.pb.model.interfaces.fields.SeparatorField;
import com.gtwm.pb.model.manageData.fields.DateValueDefn;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.AppProperties;

public class DataRow implements DataRowInfo {

	/**
	 * Private no-arg constructor just to stop public use of no-arg constructor
	 */
	private DataRow() {
		this.row = null;
		this.rowid = -1;
		this.table = null;
	}

	public DataRow(TableInfo table, int rowid, Map<BaseField, DataRowFieldInfo> row) {
		this.row = row;
		this.rowid = rowid;
		this.table = table;
	}

	public DataRow(TableInfo table, int rowid, Connection conn) throws SQLException,
			ObjectNotFoundException, CodingErrorException {
		this.rowid = rowid;
		this.table = table;
		this.row = new LinkedHashMap<BaseField, DataRowFieldInfo>();
		// load raw data from database:
		PreparedStatement statement = conn.prepareStatement("SELECT * FROM "
				+ table.getInternalTableName() + " WHERE "
				+ table.getPrimaryKey().getInternalFieldName() + " = ?");
		this.loadDataRow(conn, statement);
	}

	public DataRow(TableInfo table, BaseField whereRowField, int rowid, Connection conn)
			throws SQLException, ObjectNotFoundException, CodingErrorException {
		this.rowid = rowid;
		this.table = table;
		this.row = new LinkedHashMap<BaseField, DataRowFieldInfo>();
		// load raw data from database:
		PreparedStatement statement = conn.prepareStatement("SELECT * FROM "
				+ table.getInternalTableName() + " WHERE " + whereRowField.getInternalFieldName()
				+ " = ?");
		this.loadDataRow(conn, statement);
	}

	private static Map<String, String> getKeyToDisplayMapping(Connection conn, String internalSourceName,
			String internalKeyFieldName, String internalDisplayFieldName) throws SQLException {
		// Buffer the set of display values for this field:
		String SQLCode = "SELECT " + internalKeyFieldName + ", " + internalDisplayFieldName;
		SQLCode += " FROM " + internalSourceName;
		PreparedStatement statement = conn.prepareStatement(SQLCode);
		ResultSet results = statement.executeQuery();
		Map<String, String> displayLookup = new LinkedHashMap<String, String>();
		while (results.next()) {
			displayLookup.put(results.getString(internalKeyFieldName), results
					.getString(internalDisplayFieldName));
		}
		return displayLookup;
	}

	private void loadDataRow(Connection conn, PreparedStatement statement) throws SQLException,
			ObjectNotFoundException, CodingErrorException {
		// 0) Obtain all display values taken from other sources:
		Map<BaseField, Map<String, String>> displayLookups = new HashMap<BaseField, Map<String, String>>();
		for (BaseField field : this.table.getFields()) {
			if (field instanceof RelationField) {
				// Buffer the set of display values for this field:
				RelationField relationField = (RelationField) field;
				String relatedKey = relationField.getRelatedField().getInternalFieldName();
				String relatedDisplay = relationField.getDisplayField().getInternalFieldName();
				String relatedSource = relationField.getRelatedTable().getInternalTableName();
				Map<String, String> displayLookup = getKeyToDisplayMapping(conn, relatedSource,
						relatedKey, relatedDisplay);
				displayLookups.put(relationField, displayLookup);
			}
		}
		statement.setInt(1, this.rowid);
		ResultSet results = statement.executeQuery();
		if (results.next()) {
			for (BaseField field : this.table.getFields()) {
				String keyValue = "";
				String displayValue = "";
				if (field instanceof RelationField) {
					RelationField relationField = (RelationField) field;
					keyValue = results.getString(relationField.getInternalFieldName());
					displayValue = displayLookups.get(relationField).get(keyValue);
				} else if (field instanceof DateField) {
					// need a lot of converting between different types
					Timestamp keyValueDate = results.getTimestamp(field.getInternalFieldName());
					if (keyValueDate != null) {
						DateValue keyValueDateValue = new DateValueDefn(keyValueDate.getTime());
						try {
							keyValueDateValue.setDateResolution(((DateField) field)
									.getDateResolution());
						} catch (CantDoThatException cdtex) {
							throw new CodingErrorException(
									"Date resolution value for field " + field.getFieldName()
											+ " not recognised by date value object", cdtex);
						}
						keyValue = keyValueDateValue.toString();
						displayValue = keyValue;
					}
				} else if (field instanceof SeparatorField) {
					// no data for separator fields
				} else {
					keyValue = results.getString(field.getInternalFieldName());
					displayValue = keyValue;
				}
				DataRowField dataRowField = new DataRowField(keyValue, displayValue);
				this.row.put(field, dataRowField);
			}
		} else {
			throw new ObjectNotFoundException("Record with identifier " + rowid + " not found");
		}
		results.close();
		statement.close();
	}

	public int getRowId() {
		return this.rowid;
	}

	public Map<BaseField, DataRowFieldInfo> getDataRowFields() {
		if (AppProperties.optimiseForPerformance) {
			return this.row;
		}
		return Collections
				.unmodifiableMap(new LinkedHashMap<BaseField, DataRowFieldInfo>(this.row));
	}
	
	public DataRowFieldInfo getValue(BaseField field) {
		return this.row.get(field);
	}
	
	public DataRowFieldInfo getValue(String fieldID) throws ObjectNotFoundException {
		for (Map.Entry<BaseField, DataRowFieldInfo> entry : this.row.entrySet()) {
			if (entry.getKey().getInternalFieldName().equals(fieldID)) {
				return entry.getValue();
			}
		}
		// fieldID is not an internal ID, try the field name
		for (Map.Entry<BaseField, DataRowFieldInfo> entry : this.row.entrySet()) {
			if (entry.getKey().getFieldName().equalsIgnoreCase(fieldID)) {
				return entry.getValue();
			}
		}
		throw new ObjectNotFoundException("Field with ID or name '" + fieldID + "' not found in data row " + this.row);
	}

	public Map<RelationField, List<DataRow>> getChildDataRows(DatabaseInfo databaseDefn,
			Connection conn, HttpServletRequest request) throws SQLException, ObjectNotFoundException, CodingErrorException {
		// declare the return value:
		Map<RelationField, List<DataRow>> childDataRows = new HashMap<RelationField, List<DataRow>>();
		// obtain a set of all tables containing any field from this table as a
		// RelationField
		SortedSet<TableInfo> relationTables = new TreeSet<TableInfo>();
		databaseDefn.getDirectlyDependentTables(this.table, relationTables, request);
		String localTableInternalName = this.table.getInternalTableName();
		String localTablePrimaryKeyName = this.table.getPrimaryKey().getInternalFieldName();
		// obtain the relation field(s) for each table & generate sql to get
		// rows where related:
		for (TableInfo relatedTable : relationTables) {
			String relatedTableInternalName = relatedTable.getInternalTableName();
			String relatedTablePrimaryKeyName = relatedTable.getPrimaryKey().getInternalFieldName();
			for (BaseField baseField : relatedTable.getFields()) {
				if (baseField instanceof RelationField) {
					RelationField relationField = (RelationField) baseField;
					StringBuilder sql = new StringBuilder();

					// generate sql:
					// "SELECT relatedTable.[row id field]
					// FROM localTable, relatedTable
					// WHERE localTable.field = relatedTable.field
					// AND localTable.primaryKeyField = rowid"

					// select b1.b1rowid from a1, b1
					// where b1.a1rowid = a1.a1rowid
					// and a1.a1rowid = 5

					sql.append("SELECT "); // change
					sql.append(relatedTableInternalName);
					sql.append(".");
					sql.append(relatedTablePrimaryKeyName);
					sql.append(" FROM ");
					sql.append(localTableInternalName);
					sql.append(", ");
					sql.append(relatedTableInternalName);
					sql.append(" WHERE (");
					sql.append(relatedTableInternalName);
					sql.append(".");
					sql.append(relationField.getInternalFieldName());
					sql.append("=");
					sql.append(localTableInternalName);
					sql.append(".");
					sql.append(relationField.getRelatedField().getInternalFieldName());
					sql.append(") AND (");
					sql.append(localTableInternalName);
					sql.append(".");
					sql.append(localTablePrimaryKeyName);
					sql.append("=");
					sql.append(this.rowid);
					sql.append(")");

					PreparedStatement statement = conn.prepareStatement(sql.toString());
					ResultSet results = statement.executeQuery();
					List<DataRow> relationDataRows = new ArrayList<DataRow>();
					while (results.next()) {
						int foreign_rowid = results.getInt(relatedTablePrimaryKeyName);
						DataRow dataRow = new DataRow(relationField.getTableContainingField(),
								relationField.getTableContainingField().getPrimaryKey(),
								foreign_rowid, conn);
						relationDataRows.add(dataRow);
					}
					if (relationDataRows.size() > 0) {
						childDataRows.put(relationField, relationDataRows);
					}
				}
			}
		}
		return childDataRows;
	}

	public String toString() {
		return this.row.toString();
	}

	private final Map<BaseField, DataRowFieldInfo> row;

	private final int rowid;

	private final TableInfo table;

	private static final SimpleLogger logger = new SimpleLogger(DataRow.class);
}
