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
package com.gtwm.pb.model.manageSchema.fields;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.fields.IntegerField;
import com.gtwm.pb.model.interfaces.fields.IntegerValue;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.FieldTypeDescriptorInfo;
import com.gtwm.pb.model.interfaces.ReportDataInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.manageData.ReportData;
import com.gtwm.pb.model.manageSchema.FieldTypeDescriptor;
import com.gtwm.pb.model.manageSchema.BooleanFieldDescriptorOption.PossibleBooleanOptions;
import com.gtwm.pb.model.manageSchema.FieldTypeDescriptor.FieldCategory;
import com.gtwm.pb.model.manageSchema.ListFieldDescriptorOption.FieldPrintoutSetting;
import com.gtwm.pb.model.manageSchema.ListFieldDescriptorOption.PossibleListOptions;
import com.gtwm.pb.model.manageSchema.TextFieldDescriptorOption.PossibleTextOptions;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.Helpers;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.RandomString;
import com.gtwm.pb.util.Enumerations.DatabaseFieldType;
import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.sql.DataSource;

/**
 * A simple whole number field
 */
@Entity
public class IntegerFieldDefn extends AbstractField implements IntegerField {

	protected IntegerFieldDefn() {
	}

	public IntegerFieldDefn(DataSource dataSource, TableInfo tableContainingField,
			String internalFieldName, String fieldName, String fieldDesc, boolean unique,
			Integer defaultValue, boolean notNull, boolean notApplicable,
			String notApplicableDescription, int notApplicableValue, boolean usesLookup, boolean storesCurrency)
			throws CantDoThatException {
		this.setDataSource(dataSource);
		super.setTableContainingField(tableContainingField);
		if (internalFieldName == null) {
			super.setInternalFieldName((new RandomString()).toString());
		} else {
			super.setInternalFieldName(internalFieldName);
		}
		super.setFieldName(fieldName);
		super.setFieldDescription(fieldDesc);
		super.setUnique(unique);
		this.setDefault(defaultValue);
		super.setNotNull(notNull);
		this.setNotApplicable(notApplicable);
		if (notApplicable) {
			this.setNotApplicableDescriptionDirect(notApplicableDescription);
			this.setNotApplicableValueDirect(notApplicableValue);
		}
		this.setUsesLookup(usesLookup);
		this.setStoresCurrency(storesCurrency);
		super.setPrintoutSetting(FieldPrintoutSetting.NAME_AND_VALUE);
	}

	public String formatIntegerValue(IntegerValue integerValue) {
		return formatInteger(integerValue.getValueInteger());
	}

	public synchronized String formatInteger(int integerValue) {
		String integerFormat = Helpers.generateJavaDecimalFormat(0);
		return String.format(integerFormat, integerValue);
	}

	public boolean allowNotApplicable() {
		return this.getNotApplicable();
	}

	private Boolean getNotApplicable() {
		return this.notApplicable;
	}

	private void setNotApplicable(Boolean notApplicable) {
		this.notApplicable = notApplicable;
	}

	@Transient
	public String getNotApplicableDescription() throws CantDoThatException {
		if (!this.getNotApplicable()) {
			throw new CantDoThatException("The not applicable property is not active for field "
					+ this.getFieldName());
		}
		return this.getNotApplicableDescriptionDirect();
	}

	private void setNotApplicableDescriptionDirect(String notApplicableDescription) {
		this.notApplicableDescription = notApplicableDescription;
	}

	private String getNotApplicableDescriptionDirect() {
		return this.notApplicableDescription;
	}

	@Transient
	public int getNotApplicableValue() throws CantDoThatException {
		if (!this.getNotApplicable()) {
			throw new CantDoThatException("The not applicable property is not active for field "
					+ this.getFieldName());
		}
		return this.getNotApplicableValueDirect();
	}

	private Integer getNotApplicableValueDirect() {
		return this.notApplicableValue;
	}

	private void setNotApplicableValueDirect(Integer notApplicableValue) {
		this.notApplicableValue = notApplicableValue;
	}

	@Transient
	public DatabaseFieldType getDbType() {
		return DatabaseFieldType.INTEGER;
	}

	@Transient
	public FieldTypeDescriptorInfo getFieldDescriptor() throws CantDoThatException {
		FieldTypeDescriptorInfo fieldDescriptor = new FieldTypeDescriptor(FieldCategory.NUMBER);
		try {
			fieldDescriptor.setBooleanOptionState(PossibleBooleanOptions.UNIQUE, super.getUnique());
			fieldDescriptor.setBooleanOptionState(PossibleBooleanOptions.MANDATORY, super
					.getNotNull());
			fieldDescriptor.setBooleanOptionState(PossibleBooleanOptions.STORECURRENCY,
					this.getStoresCurrency());
			fieldDescriptor.setListOptionSelectedItem(PossibleListOptions.NUMBERPRECISION, "0");
			if (this.hasDefault()) {
				fieldDescriptor.setTextOptionValue(PossibleTextOptions.DEFAULTVALUE, String
						.valueOf(this.getDefault().toString()));
			}
			FieldPrintoutSetting printoutSetting = this.getPrintoutSetting();
			fieldDescriptor.setListOptionSelectedItem(PossibleListOptions.PRINTFORMAT, printoutSetting.name());
		} catch (ObjectNotFoundException onfex) {
			throw new CantDoThatException("Internal error setting up " + this.getClass()
					+ " field descriptor", onfex);
		}
		return fieldDescriptor;
	}

	@Transient
	public FieldCategory getFieldCategory() {
		return FieldCategory.NUMBER;
	}

	public synchronized void setDefault(Integer defaultValue) throws CantDoThatException {
		super.setDefaultDefined((defaultValue != null));
		this.setDefaultDirect(defaultValue);
	}

	@Transient
	public synchronized Integer getDefault() {
		return this.getDefaultDirect();
	}

	public synchronized void clearDefault() {
		super.setDefaultDefined(false);
		this.setDefaultDirect(null);
	}

	private void setDefaultDirect(Integer defaultValue) {
		this.defaultValue = defaultValue;
	}

	private Integer getDefaultDirect() {
		return this.defaultValue;
	}

	private Boolean getUsesLookup() {
		return this.usesLookup;
	}

	private void setUsesLookup(Boolean usesLookup) {
		this.usesLookup = usesLookup;
	}

	@Transient
	public boolean usesLookup() {
		return this.getUsesLookup();
	}

	private boolean getStoresCurrency() {
		return this.storesCurrency;
	}
	
	public void setStoresCurrency(boolean storesCurrency) {
		this.storesCurrency = storesCurrency;
	}
	
	@Transient
	public boolean storesCurrency() {
		return this.getStoresCurrency();
	}

	@Transient
	public SortedSet<Integer> getItems() throws SQLException, CantDoThatException {
		String SQLCode = "SELECT DISTINCT " + this.getInternalFieldName() + " FROM "
				+ this.getTableContainingField().getInternalTableName();
		Connection conn = null;
		SortedSet<Integer> items = new TreeSet<Integer>();
		try {
			conn = this.dataSource.getConnection();
			conn.setAutoCommit(false);
			PreparedStatement statement = conn.prepareStatement(SQLCode);
			ResultSet results = statement.executeQuery();
			while (results.next()) {
				items.add(results.getInt(1));
			}
			results.close();
			statement.close();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		return items;
	}

	public SortedSet<Integer> getItems(BaseReportInfo report, Map<BaseField, String> filterValues)
			throws SQLException, CantDoThatException {
		Connection conn = null;
		SortedSet<Integer> items = new TreeSet<Integer>();
		try {
			conn = this.dataSource.getConnection();
			conn.setAutoCommit(false);
			ReportDataInfo reportData = new ReportData(conn, report, false, false);
			// Generates a SELECT DISTINCT on this field including filterValues
			// in the WHERE clause
			Map<BaseField, Boolean> emptySorts = new HashMap<BaseField, Boolean>();
			PreparedStatement statement = reportData.getReportSqlPreparedStatement(conn,
					filterValues, false, emptySorts, -1, this);
			ResultSet results = statement.executeQuery();
			while (results.next()) {
				items.add(results.getInt(1));
			}
		} catch (SQLException sqlex) {
			// catch exception where field is not included
			// within report and simply return an empty tree
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		return items;
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

	private Integer defaultValue = null;

	private Boolean notApplicable = false;

	private String notApplicableDescription = "Not applicable";

	private Integer notApplicableValue = -1;

	private boolean usesLookup = false;
	
	private boolean storesCurrency = false;

	private transient DataSource dataSource = null;
}