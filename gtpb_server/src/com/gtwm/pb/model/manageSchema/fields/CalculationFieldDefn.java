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

import java.util.Locale;
import org.grlea.log.SimpleLogger;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.fields.CalculationField;
import com.gtwm.pb.model.interfaces.FieldTypeDescriptorInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.ReportCalcFieldInfo;
import com.gtwm.pb.model.manageSchema.FieldTypeDescriptor;
import com.gtwm.pb.model.manageSchema.FieldTypeDescriptor.FieldCategory;
import com.gtwm.pb.model.manageSchema.ListFieldDescriptorOption.PossibleListOptions;
import com.gtwm.pb.model.manageSchema.BooleanFieldDescriptorOption.PossibleBooleanOptions;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.Enumerations.DatabaseFieldType;
import com.gtwm.pb.util.CodingErrorException;

/*
 * Doesn't extend AbstractField as implementation is completely different for calculations and calcs should not be persited to hibernate (AbstractField is a persistent class)
 */
public class CalculationFieldDefn implements CalculationField {

	protected CalculationFieldDefn() {
	}

	/**
	 * @param reportCalcField
	 *            The calculation field that contains all the calculation info
	 *            and which wraps this BaseField.
	 */
	public CalculationFieldDefn(ReportCalcFieldInfo reportCalcField) {
		this.reportCalcField = reportCalcField;
	}

	/**
	 * No action: calculation fields are never not null because they're report
	 * fields only not table fields
	 */
	public void setNotNull(boolean fieldNotNull) {
	}

	public FieldCategory getFieldCategory() throws CodingErrorException {
		switch (this.getDbType()) {
		case INTEGER:
		case FLOAT:
		case SERIAL:
			return FieldCategory.NUMBER;
		case INTERVAL:
			return FieldCategory.DURATION;
		case TIMESTAMP:
			return FieldCategory.DATE;
		case VARCHAR:
			return FieldCategory.TEXT;
		default:
			throw new CodingErrorException("Unrecognised database field type "
					+ this.getDbType().toString());
		}
	}

	public FieldTypeDescriptorInfo getFieldDescriptor() throws CantDoThatException,
			CodingErrorException {
		FieldTypeDescriptorInfo fieldDescriptor = null;
		// TODO: fill in rest of options under each case
		switch (this.getDbType()) {
		case BOOLEAN:
			fieldDescriptor = new FieldTypeDescriptor(FieldCategory.CHECKBOX);
			break;
		case INTEGER:
		case FLOAT:
			fieldDescriptor = new FieldTypeDescriptor(FieldCategory.NUMBER);
			break;
		case VARCHAR:
			fieldDescriptor = new FieldTypeDescriptor(FieldCategory.TEXT);
			break;
		case TIMESTAMP:
			fieldDescriptor = new FieldTypeDescriptor(FieldCategory.DATE);
			try {
				fieldDescriptor.setListOptionSelectedItem(PossibleListOptions.DATERESOLUTION,
						String.valueOf(this.getReportCalcField().getDateResolution()));
				fieldDescriptor.setBooleanOptionState(PossibleBooleanOptions.DEFAULTTONOW, false);
				fieldDescriptor.setBooleanOptionState(PossibleBooleanOptions.MANDATORY, false);
			} catch (ObjectNotFoundException onfex) {
				throw new CodingErrorException("Error setting date options: ", onfex);
			}
			break;
		default:
			throw new CodingErrorException("Unknown field type " + this.getDbType()
					+ " in calculation " + this.toString());
		}
		return fieldDescriptor;
	}

	public DatabaseFieldType getDbType() {
		return this.getReportCalcField().getDbType();
	}

	/**
	 * Calculations don't have descriptions, return the calc. definition instead
	 */
	public String getFieldDescription() {
		return this.getReportCalcField().getCalculationDefinition();
	}

	public Integer getFieldIndex() {
		// not relevant as calculations are never stored in tables
		// logger.warn("Call to irrelevant calculation method getFieldIndex from calculation "
		// + this);
		return null;
	}

	public String getFieldName() {
		return this.getReportCalcField().getBaseFieldName();
	}

	public Boolean getHidden() {
		// not relevant as calculations can't be hidden
		// logger.warn("Call to irrelevant calculation method getHidden from calculation "
		// + this);
		return false;
	}

	public String getInternalFieldName() {
		return this.getReportCalcField().getBaseFieldInternalFieldName();
	}

	public boolean getNotNull() {
		logger.warn("Call to irrelevant calculation method getNotNull from calculation " + this);
		return false;
	}

	public TableInfo getTableContainingField() {
		// This is used from templates in an ok way so log warning suppressed
		// logger.warn("Call to irrelevant calculation method getTableContainingField from calculation "
		// + this);
		// return best guess at a table anyway but this really has no meaning
		return this.getReportCalcField().getParentReport().getParentTable();
	}

	public Boolean getUnique() {
		logger.warn("Call to irrelevant calculation method getUnique from calculation " + this);
		return false;
	}

	public boolean hasDefault() {
		logger.warn("Call to irrelevant calculation method hasDefault from calculation " + this);
		return false;
	}

	public void setFieldDescription(String fieldDesc) {
		logger.warn("Call to irrelevant calculation method setFieldDescription from calculation "
				+ this);
	}

	public void setFieldIndex(Integer fieldIndex) {
		logger.warn("Call to irrelevant calculation method setFieldIndex from calculation " + this);
	}

	public void setFieldName(String fieldName) {
		logger.warn("Call to irrelevant calculation method setFieldName from calculation " + this);
	}

	public void setHidden(Boolean hidden) {
		logger.warn("Call to irrelevant calculation method setHidden from calculation " + this);
	}

	public void setTableContainingField(TableInfo tableContainingField) {
		logger
				.warn("Call to irrelevant calculation method setTableContainingField from calculation "
						+ this);
	}

	public void setUnique(Boolean fieldUnique) throws CantDoThatException {
		logger.warn("Call to irrelevant calculation method setUnique from calculation " + this);
	}

	public ReportCalcFieldInfo getReportCalcField() {
		return this.reportCalcField;
	}

	/**
	 * equals is based on internal field name
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		BaseField otherField = (BaseField) obj;
		return (this.getInternalFieldName()).equals(otherField.getInternalFieldName());
	}

	public int hashCode() {
		return this.getInternalFieldName().hashCode();
	}

	public int compareTo(BaseField anotherFieldDefn) {
		// This doesn't really make sense but only way I can think of to compare
		// a calc field to a field of
		// another type - calc field will always come afterwards
		if ((anotherFieldDefn == null) || (anotherFieldDefn.getClass() != this.getClass())) {
			return 1;
		}
		if (this == anotherFieldDefn) {
			return 0;
		}
		ReportCalcFieldInfo otherReportCalcField = ((CalculationField) anotherFieldDefn)
				.getReportCalcField();
		ReportCalcFieldInfo thisReportCalcField = this.getReportCalcField();
		String otherFieldName = otherReportCalcField.getBaseFieldName();
		String otherReportName = otherReportCalcField.getParentReport().getReportName();
		String otherInternalFieldName = anotherFieldDefn.getInternalFieldName();
		return (thisReportCalcField.getParentReport().getReportName().toLowerCase(Locale.UK)
				+ this.getFieldName().toLowerCase(Locale.UK) + this.getInternalFieldName())
				.compareTo(otherReportName.toLowerCase(Locale.UK)
						+ otherFieldName.toLowerCase(Locale.UK) + otherInternalFieldName);
	}

	public String toString() {
		return this.getFieldName();
	}

	private ReportCalcFieldInfo reportCalcField = null;

	private static final SimpleLogger logger = new SimpleLogger(CalculationFieldDefn.class);
}
