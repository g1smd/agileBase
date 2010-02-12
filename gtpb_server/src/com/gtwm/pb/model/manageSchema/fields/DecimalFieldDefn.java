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
package com.gtwm.pb.model.manageSchema.fields;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.sql.DataSource;
import org.grlea.log.SimpleLogger;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.fields.DecimalField;
import com.gtwm.pb.model.interfaces.fields.DecimalValue;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.FieldTypeDescriptorInfo;
import com.gtwm.pb.model.interfaces.ReportDataInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.manageData.ReportData;
import com.gtwm.pb.model.manageSchema.FieldTypeDescriptor;
import com.gtwm.pb.model.manageSchema.BooleanFieldDescriptorOption.PossibleBooleanOptions;
import com.gtwm.pb.model.manageSchema.FieldTypeDescriptor.FieldCategory;
import com.gtwm.pb.model.manageSchema.ListFieldDescriptorOption.PossibleListOptions;
import com.gtwm.pb.model.manageSchema.TextFieldDescriptorOption.PossibleTextOptions;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.Helpers;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.RandomString;
import com.gtwm.pb.util.Enumerations.DatabaseFieldType;

/**
 * A simple decimal number field
 */
@Entity
public class DecimalFieldDefn extends AbstractField implements DecimalField {

    protected DecimalFieldDefn() {
    }

    public DecimalFieldDefn(DataSource dataSource, TableInfo tableContainingField, String internalFieldName, String fieldName, String fieldDesc, boolean unique, boolean notNull,
            Double defaultValue, int precision, boolean notApplicable, String notApplicableDescription, double notApplicableValue, boolean usesLookup) throws CantDoThatException {
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
        this.setPrecision(precision);
        this.setNotApplicable(notApplicable);
        if (notApplicable) {
            this.setNotApplicableDescriptionDirect(notApplicableDescription);
            this.setNotApplicableValueDirect(notApplicableValue);
        }
        this.setUsesLookup(usesLookup);
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
            throw new CantDoThatException("The not applicable property is not active for field " + this.getFieldName());
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
    public double getNotApplicableValue() throws CantDoThatException {
        if (!this.getNotApplicable()) {
            throw new CantDoThatException("The not applicable property is not active for field " + this.getFieldName());
        }
        return this.getNotApplicableValueDirect();
    }
    
    private void setNotApplicableValueDirect(Double notApplicableValue) {
        this.notApplicableValue = notApplicableValue;
    }
    
    private Double getNotApplicableValueDirect() {
        return this.notApplicableValue;
    }

    /**
     * @param precision
     *            the number of decimal places to display on screen The stored value is as accurate as the
     *            float type storing it allows
     */
    public synchronized void setPrecision(Integer precision) {
        this.precision = precision;
    }

    public synchronized Integer getPrecision() {
        return this.precision;
    }

    @Transient
    public DatabaseFieldType getDbType() {
        return DatabaseFieldType.FLOAT;
    }

    @Transient
    public FieldCategory getFieldCategory() {
        return FieldCategory.NUMBER;
    }

    @Transient
    public FieldTypeDescriptorInfo getFieldDescriptor() throws CantDoThatException {
        FieldTypeDescriptorInfo fieldDescriptor = new FieldTypeDescriptor(FieldCategory.NUMBER);
        try {
            fieldDescriptor.setBooleanOptionState(PossibleBooleanOptions.UNIQUE, super.getUnique());
            fieldDescriptor.setListOptionSelectedItem(PossibleListOptions.NUMBERPRECISION, String.valueOf(this.getPrecision()));
            if (this.hasDefault()) {
                fieldDescriptor.setTextOptionValue(PossibleTextOptions.DEFAULTVALUE, String.valueOf(this.getDefault().toString()));
            }
        } catch (ObjectNotFoundException onfex) {
            throw new CantDoThatException("Internal error setting up " + this.getClass() + " field descriptor", onfex);
        }
        return fieldDescriptor;
    }

    public synchronized void setDefault(Double defaultValue) throws CantDoThatException {
        if (super.getNotNull() && (defaultValue == null)) {
            throw new CantDoThatException("A field that cannot be null must have a default value");
        }
        super.setDefaultDefined((defaultValue != null));
        this.setDefaultDirect(defaultValue);
    }

    @Transient
    public synchronized Double getDefault() {
        // Note by Oliver: See issue 318 in Mantis
        // String formatString = "%1$." + precision + "f"; // convert '1st arg' to 'x decimal point precision'
        // 'float'
        // String formattedDefault = String.format(formatString, defaultValue);
        // return Float.parseFloat(formattedDefault);
        return this.getDefaultDirect();
    }
    
    private void setDefaultDirect(Double defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    // @Column(name="dec_defaultvalue")
    private Double getDefaultDirect() {
        return this.defaultValue;
    }

    public synchronized void clearDefault() {
        super.setDefaultDefined(false);
        this.setDefaultDirect(null);

    }

    public String formatDecimalValue(DecimalValue decimalValue) {
    	if (decimalValue == null) {
    		return "";
    	}
    	if (decimalValue.isNull()) {
    		return "";
    	}
        return this.formatFloat(decimalValue.getValueFloat());
    }

    public synchronized String formatFloat(double decimalValue) {
        String floatFormat = Helpers.generateJavaDecimalFormat(this.getPrecision());
        return String.format(floatFormat, decimalValue);
    }

    private boolean getUsesLookup() {
    	return this.usesLookup;
    }

    private void setUsesLookup(boolean usesLookup) {
    	this.usesLookup = usesLookup;
    }
    
    @Transient
    public boolean usesLookup() {
    	return this.getUsesLookup();
    }
    
    @Transient
    public SortedSet<Double> getItems() throws SQLException, CantDoThatException {
		String SQLCode = "SELECT DISTINCT " + this.getInternalFieldName() + " FROM " + this.getTableContainingField().getInternalTableName();
		Connection conn = null;
		SortedSet<Double> items = new TreeSet<Double>();
		try {
			conn = this.dataSource.getConnection();
			conn.setAutoCommit(false);
			PreparedStatement statement = conn.prepareStatement(SQLCode);
			ResultSet results = statement.executeQuery();
			while (results.next()) {
				items.add(results.getDouble(1));
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

	public SortedSet<Double> getItems(BaseReportInfo report, Map<BaseField, String> filterValues) throws SQLException, CantDoThatException {
		Connection conn = null;
		SortedSet<Double> items = new TreeSet<Double>();
		try {
			conn = this.dataSource.getConnection();
			conn.setAutoCommit(false);
			ReportDataInfo reportData = new ReportData(conn, report, false, false);
			// Generates a SELECT DISTINCT on this field including filterValues in the WHERE clause
			Map<BaseField,Boolean> emptySorts = new HashMap<BaseField,Boolean>();
			PreparedStatement statement = reportData.getReportSqlPreparedStatement(conn, filterValues, false, emptySorts, -1, this);
			ResultSet results = statement.executeQuery();
			while (results.next()) {
				items.add(results.getDouble(1));
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
     * Don't use in code. Only to be used from the DatabaseDefn constructor, hence not in interface
     */
    public void setDataSource(DataSource dataSource) throws CantDoThatException {
        if (dataSource == null) {
            throw new CantDoThatException("Can't set the data source to null, that's not very useful");
        }
        this.dataSource = dataSource;
    }

    private Double defaultValue = null;

    private Integer precision = 2;

    private Boolean notApplicable = false;

    private String notApplicableDescription = "Not applicable";

    private Double notApplicableValue = 0.0d;
    
    private Boolean usesLookup = false;
    
    private transient DataSource dataSource = null;
    
	private static final SimpleLogger logger = new SimpleLogger(DecimalFieldDefn.class);
}