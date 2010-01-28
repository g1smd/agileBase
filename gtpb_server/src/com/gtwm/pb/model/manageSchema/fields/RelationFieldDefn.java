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
package com.gtwm.pb.model.manageSchema.fields;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.sql.DataSource;
import org.grlea.log.SimpleLogger;
import com.gtwm.pb.model.interfaces.fields.RelationField;
import com.gtwm.pb.model.interfaces.fields.DateField;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.FieldTypeDescriptorInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.manageSchema.FieldTypeDescriptor.FieldCategory;
import com.gtwm.pb.model.manageSchema.TableDefn;
import com.gtwm.pb.model.manageSchema.DatabaseDefn;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.RandomString;
import com.gtwm.pb.util.Enumerations.DatabaseFieldType;
import com.gtwm.pb.util.Enumerations.ForeignKeyConstraint;

@Entity
public class RelationFieldDefn extends AbstractField implements RelationField {

	protected RelationFieldDefn() {
	}

	/**
	 * Constructs an object which is related to another particular field
	 */
	public RelationFieldDefn(DataSource dataSource, TableInfo tableContainingField,
			String internalFieldName, TableInfo relatedTable, BaseField relatedField,
			boolean notNull) throws CantDoThatException {
		super.setTableContainingField(tableContainingField);
		if (internalFieldName == null) {
			super.setInternalFieldName((new RandomString()).toString());
		} else {
			super.setInternalFieldName(internalFieldName);
		}
		this.setDataSource(dataSource);
		this.setRelatedTable(relatedTable);
		this.setRelatedField(relatedField);
		this.setDisplayField(relatedField);
		super.setUnique(false);
		super.setNotNullDirect(notNull);
	}

	@ManyToOne(targetEntity = TableDefn.class)
	public TableInfo getRelatedTable() {
		return this.relatedTable;
	}

	private void setRelatedTable(TableInfo relatedTable) {
		this.relatedTable = relatedTable;
	}

	@ManyToOne(targetEntity = AbstractField.class)
	public BaseField getRelatedField() {
		return this.relatedField;
	}

	private void setRelatedField(BaseField relatedField) {
		this.relatedField = relatedField;
	}

	// public void setFieldName(String fieldName) {
	// this.getRelatedField().setFieldName(fieldName);
	// }

	@Transient
	// Override field name to make it that of the related field
	public String getFieldName() {
		if ((super.getFieldName() != null) && (!super.getFieldName().equals(""))) {
			return super.getFieldName();
		} else {
			return this.getRelatedTable().getTableName() + ": "
					+ this.getDisplayField().getFieldName();
		}
	}

	@Transient
	public String getSimplifiedFieldName() {
		if ((super.getFieldName() != null) && (!super.getFieldName().equals(""))) {
			return super.getFieldName();
		} else {
			String fieldName = this.getDisplayField().getFieldName();
			String simplifiedFieldName = fieldName;
			if (this.getDisplayField().equals(this.getRelatedTable().getPrimaryKey())) {
				simplifiedFieldName = fieldName.replaceAll("^ID\\:", "");
				int numRightBrackets = Arrays.asList(simplifiedFieldName.split("\\)")).size();
				int numLeftBrackets = Arrays.asList(simplifiedFieldName.split("\\(")).size();
				if (numRightBrackets > numLeftBrackets) {
					simplifiedFieldName = simplifiedFieldName.replaceAll("^.{1,4}\\)\\s", "");
					if (simplifiedFieldName.length() < 3) {
						simplifiedFieldName = fieldName;
					}
				}
			} else if (this.getDisplayField() instanceof RelationField) {
				String tableName = this.getRelatedTable().getTableName();
				String simplifiedTableName = tableName.replaceAll("^.{1,4}\\)\\s", "");
				fieldName = ((RelationField) this.getDisplayField()).getSimplifiedFieldName();
				simplifiedFieldName = simplifiedTableName + ": " + fieldName;
			}
			return this.pluralToSingular(simplifiedFieldName);
		}
	}

	/**
	 * Converted from http://www.devx.com/vb2themax/Tip/19612
	 */
	private String pluralToSingular(String plural) {
		String singular = plural;
		String lowerPlural = plural.toLowerCase();
		// a few exceptions
		if (lowerPlural.equals("feet")) {
			singular = "Foot";
		} else if (lowerPlural.equals("geese")) {
			singular = "Goose";
		} else if (lowerPlural.equals("men")) {
			singular = "Man";
		} else if (lowerPlural.equals("women")) {
			singular = "Woman";
		} else if (lowerPlural.equals("criteria")) {
			singular = "Criterion";

		} else if ((lowerPlural.endsWith("ies"))
				&& ("aeiou".indexOf(lowerPlural.charAt(lowerPlural.length() - 4)) == -1)) {
			// plural uses "ies" if word ends with "y" preceeded by a non-vowel
			singular = plural.substring(0, plural.length() - 3) + "y";
		} else if (lowerPlural.endsWith("s")) {
			singular = plural.substring(0, plural.length() - 1);
		}
		if (plural.equals(lowerPlural)) {
			return singular.toLowerCase();
		} else if (plural.equals(plural.toUpperCase())) {
			return singular.toUpperCase();
		} else {
			return singular;
		}
	}

	public void setDisplayField(BaseField displayField) {
		this.displayField = displayField;
	}

	@ManyToOne(targetEntity = AbstractField.class)
	public BaseField getDisplayField() {
		return this.displayField;
	}

	@Transient
	public SortedMap<String, String> getItems(boolean reverseKeyValue) throws SQLException {
		return this.getItemsWork(reverseKeyValue, null, -1);
	}

	public SortedMap<String, String> getItems(boolean reverseKeyValue, String filterString,
			int maxResults) throws SQLException {
		return this.getItemsWork(reverseKeyValue, filterString, maxResults);
	}

	@Transient
	private SortedMap<String, String> getItemsWork(boolean reverseKeyValue, String filterString,
			int maxResults) throws SQLException {
		SortedMap<String, String> items = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
		// Note: DISTINCT isn't usually necessary as the related field is
		// usually the primary key,
		// however it would theoretically be possible to create a relation to a
		// non-unique field
		String displayFieldInternalName = this.getDisplayField().getInternalFieldName();
		String SQLCode = "SELECT " + this.getRelatedField().getInternalFieldName() + ", "
				+ displayFieldInternalName;
		SQLCode += " FROM " + this.getRelatedTable().getInternalTableName();
		// Discard any items with nothing in the display field
		SQLCode += " WHERE " + displayFieldInternalName + " IS NOT NULL";
		if (filterString != null) {
			filterString = filterString.replace("*", "%").toLowerCase();
			filterString += "%";
			SQLCode += " AND lower(" + displayFieldInternalName + "::text) LIKE ?";
		}
		if (reverseKeyValue && this.getDisplayField().getDbType().equals(DatabaseFieldType.VARCHAR)) {
			SQLCode += " ORDER BY lower(" + displayFieldInternalName + ")";
		} else {
			SQLCode += " ORDER BY 1";
		}
		if (maxResults > 0) {
			SQLCode += " LIMIT " + maxResults;
		}
		Connection conn = null;
		try {
			conn = this.dataSource.getConnection();
			conn.setAutoCommit(false);
			PreparedStatement statement = conn.prepareStatement(SQLCode);
			if (filterString != null) {
				statement.setString(1, filterString);
			}
			ResultSet results = statement.executeQuery();
			while (results.next()) {
				String keyValue = results.getString(1);
				String displayValue = results.getString(2);
				if (reverseKeyValue) {
					items.put(displayValue, keyValue);
				} else {
					items.put(keyValue, displayValue);
				}
			}
			results.close();
			statement.close();
			// conn.commit(); // don't need commit, we're just querying
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		return items;
	}

	/**
	 * Where this relation links to another relation, get the primary key value
	 * of the row that that relation relates to.
	 * 
	 * For example, contacts links to sites links to organisations.
	 * 
	 * Assume contacts contains a relation contacts_site that links to
	 * site_organisation, where site_organisation is itself a relation field
	 * that links to organisations_organisationname
	 * 
	 * and this object is the contacts_site relation
	 * 
	 * then we could run getOneLevelUpPrimaryKey passing in site_organisation to
	 * return the row Id of the organisation
	 */
	private int getTierThreeRowId(RelationField tierTwoRelation, int tierTwoRowId)
			throws SQLException {
		int tierThreeRowId = -1;
		TableInfo tierThreeTable = tierTwoRelation.getRelatedTable();
		TableInfo tierTwoTable = this.getRelatedTable();
		String SQLCode = "SELECT " + tierThreeTable.getInternalTableName() + "."
				+ tierThreeTable.getPrimaryKey().getInternalFieldName();
		SQLCode += " FROM " + tierThreeTable.getInternalTableName();
		SQLCode += " INNER JOIN " + tierTwoTable.getInternalTableName();
		SQLCode += " ON " + tierTwoTable.getInternalTableName() + "."
				+ tierTwoRelation.getInternalFieldName() + " = "
				+ tierThreeTable.getPrimaryKey().getInternalFieldName();
		SQLCode += " WHERE " + tierTwoTable.getInternalTableName() + "."
				+ tierTwoTable.getPrimaryKey().getInternalFieldName() + "= ?";
		Connection conn = null;
		try {
			conn = this.dataSource.getConnection();
			conn.setAutoCommit(false);
			PreparedStatement statement = conn.prepareStatement(SQLCode);
			statement.setInt(1, tierTwoRowId);
			ResultSet results = statement.executeQuery();
			if (results.next()) {
				tierThreeRowId = results.getInt(1);
			}
			results.close();
			statement.close();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		return tierThreeRowId;
	}

	@Transient
	public String getDisplayValue(String keyValue) throws SQLException, CodingErrorException {
		if (keyValue == null) {
			return "";
		}
		if (keyValue.equals("")) {
			return "";
		}
		BaseField relatedField = this.getRelatedField();
		TableInfo relatedTable = this.getRelatedTable();
		BaseField displayField = this.getDisplayField();
		// If the display field is itself a relation, return that's display
		// value
		if (displayField instanceof RelationField) {
			RelationField relationField = (RelationField) displayField;
			int tierThreeRowId = this.getTierThreeRowId(relationField, Integer.valueOf(keyValue));
			return relationField.getDisplayValue(String.valueOf(tierThreeRowId));
		}
		// relationDisplayValues will be used if there are recursive relations
		String relationDisplayValues = "";
		String SQLCode = "SELECT ";
		// If the display field is just the primary key, take a stab at
		// selecting some other good display fields as well
		int foundFields = 0;
		if (this.getDisplayField().equals(relatedTable.getPrimaryKey())) {
			FIELDLOOP: for (BaseField field : relatedTable.getFields()) {
				if (!field.getHidden()) {
					if ((field instanceof TextFieldDefn) || (field instanceof DateField)) {
						SQLCode += field.getInternalFieldName() + ", ";
						foundFields++;
						if (foundFields > 2) {
							break FIELDLOOP;
						}
					}
					if (field instanceof RelationField) {
						RelationField relationField = (RelationField) field;
						int tierThreeRowId = this.getTierThreeRowId(relationField, Integer
								.valueOf(keyValue));
						// recurse
						relationDisplayValues += relationField.getDisplayValue(String
								.valueOf(tierThreeRowId))
								+ ", ";
					}
				}
			}
			relationDisplayValues = relationDisplayValues.replaceAll(",\\s$", "");
		} else {
			SQLCode += this.getDisplayField().getInternalFieldName();
			foundFields = 1;
		}
		String displayValue = "";
		if (foundFields > 0) {
			// strip any trailing commas
			SQLCode = SQLCode.replaceAll(",\\s$", "");
			SQLCode += " FROM " + relatedTable.getInternalTableName();
			SQLCode += " WHERE " + relatedField.getInternalFieldName() + " = ?";
			Connection conn = null;
			try {
				conn = this.dataSource.getConnection();
				conn.setAutoCommit(false);
				PreparedStatement statement = conn.prepareStatement(SQLCode);
				DatabaseFieldType keyFieldType = relatedField.getDbType();
				switch (keyFieldType) {
				case INTEGER:
					statement.setInt(1, Integer.valueOf(keyValue));
					break;
				case SERIAL:
					statement.setInt(1, Integer.valueOf(keyValue));
					break;
				default:
					throw new CodingErrorException("Unhandled type for key field " + relatedField
							+ ": " + keyFieldType);
				}
				ResultSet results = statement.executeQuery();
				if (results.next()) {
					for (int i = 0; i < foundFields; i++) {
						String resultString = results.getString(i + 1);
						if (resultString != null) {
							if (!resultString.equals("")) {
								displayValue = displayValue + results.getString(i + 1) + ", ";
							}
						}
					}
					displayValue = displayValue.replaceAll(",\\s$", "");
				}
				results.close();
				statement.close();
				conn.commit();
			} finally {
				if (conn != null) {
					conn.close();
				}
			}
		}
		if (!relationDisplayValues.equals("")) {
			if (displayValue.equals("")) {
				displayValue = relationDisplayValues;
			} else {
				displayValue = relationDisplayValues + ", " + displayValue;
			}
		}
		return displayValue;
	}

	@Transient
	public String getFieldDescription() {
		String fieldDescription = super.getFieldDescription();
		if (fieldDescription.equals(DatabaseDefn.PRIMARY_KEY_DESCRIPTION)) {
			return "";
		}
		return fieldDescription;
		/*
		 * if ((super.getFieldDescription() != null) &&
		 * (!super.getFieldDescription().equals(""))) { return
		 * super.getFieldDescription(); } else { return
		 * this.getRelatedField().getFieldDescription(); }
		 */
	}

	@Transient
	public FieldTypeDescriptorInfo getFieldDescriptor() throws CantDoThatException,
			CodingErrorException {
		return this.getRelatedField().getFieldDescriptor();
	}

	/*
	 * Override AbstractField.setUnique() to throw an exception
	 */
	// public void setUnique(boolean unique) throws CantDoThatException {
	// // The related field must always be unique and this field must always be
	// not unique
	// throw new CantDoThatException("Relation fields must always be not
	// unique");
	// }
	@Transient
	public Boolean getUnique() {
		// the related field must be unique, this field must always allow
		// duplicates
		// to allow a one-to-many relation
		return false;
	}

	@Transient
	public DatabaseFieldType getDbType() {
		DatabaseFieldType relatedFieldDbType = this.getRelatedField().getDbType();
		// A foreign key reference to a serial field is an integer
		if (relatedFieldDbType.equals(DatabaseFieldType.SERIAL)) {
			return DatabaseFieldType.INTEGER;
		} else {
			return relatedFieldDbType;
		}
	}

	@Transient
	public FieldCategory getFieldCategory() {
		return FieldCategory.RELATION;
	}

	/*
	 * Must override AbstractField's compareTo because this.getFieldName() is
	 * implemented differently. The code should be exactly the same
	 */
	public int compareTo(BaseField otherField) {
		if (this == otherField) {
			return 0;
		}
		TableInfo otherTable = otherField.getTableContainingField();
		int tableCompare = this.getTableContainingField().compareTo(otherTable);
		if (tableCompare != 0) {
			return tableCompare;
		}
		Integer otherFieldIndex = otherField.getFieldIndex();
		int indexCompare = this.getFieldIndex().compareTo(otherFieldIndex);
		if (indexCompare != 0) {
			return indexCompare;
		}
		String otherFieldName = otherField.getFieldName();
		String otherFieldInternalName = otherField.getInternalFieldName();
		return (this.getFieldName() + this.getInternalFieldName())
				.compareToIgnoreCase(otherFieldName + otherFieldInternalName);
	}

	public void setOnUpdateAction(ForeignKeyConstraint onUpdateAction) {
		this.onUpdateAction = onUpdateAction;
	}

	@Enumerated(EnumType.STRING)
	public ForeignKeyConstraint getOnUpdateAction() {
		return this.onUpdateAction;
	}

	public void setOnDeleteAction(ForeignKeyConstraint onDeleteAction) {
		this.onDeleteAction = onDeleteAction;
	}

	@Enumerated(EnumType.STRING)
	public ForeignKeyConstraint getOnDeleteAction() {
		return this.onDeleteAction;
	}

	public String toString() {
		return this.getFieldName();
	}

	/**
	 * Don't use in code. Only to be used from the DatabaseDefn constructor,
	 * hence not in interface
	 */
	public void setDataSource(DataSource dataSource) throws CantDoThatException {
		if (dataSource == null) {
			throw new CantDoThatException(
					"Can't set the data source to null, that's not very useful");
		}
		this.dataSource = dataSource;
	}

	private transient DataSource dataSource = null;

	private TableInfo relatedTable;

	private BaseField relatedField; // the field on which the relation is based

	private BaseField displayField; // the field who's value should be displayed

	private ForeignKeyConstraint onUpdateAction = ForeignKeyConstraint.CASCADE;

	private ForeignKeyConstraint onDeleteAction = ForeignKeyConstraint.CASCADE;

	private static final SimpleLogger logger = new SimpleLogger(RelationFieldDefn.class);
}