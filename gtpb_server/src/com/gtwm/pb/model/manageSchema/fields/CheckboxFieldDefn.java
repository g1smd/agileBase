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

import javax.persistence.Entity;
import javax.persistence.Transient;
import com.gtwm.pb.model.interfaces.FieldTypeDescriptorInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.fields.CheckboxField;
import com.gtwm.pb.model.manageSchema.FieldTypeDescriptor;
import com.gtwm.pb.model.manageSchema.FieldTypeDescriptor.FieldCategory;
import com.gtwm.pb.model.manageSchema.ListFieldDescriptorOption.FieldPrintoutSetting;
import com.gtwm.pb.model.manageSchema.ListFieldDescriptorOption.PossibleListOptions;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.RandomString;
import com.gtwm.pb.util.Enumerations.DatabaseFieldType;

@Entity
public class CheckboxFieldDefn extends AbstractField implements CheckboxField {

	protected CheckboxFieldDefn() {
	}

	public CheckboxFieldDefn(TableInfo tableContainingField, String internalFieldName,
			String fieldName, String fieldDesc, Boolean defaultValue, boolean hidden) throws CantDoThatException {
		super.setTableContainingField(tableContainingField);
		if (internalFieldName == null) {
			super.setInternalFieldName((new RandomString()).toString());
		} else {
			super.setInternalFieldName(internalFieldName);
		}
		super.setFieldName(fieldName);
		super.setFieldDescription(fieldDesc);
		this.setDefault(defaultValue);
		super.setNotNull(true);
		super.setHidden(hidden);
		super.setPrintoutSetting(FieldPrintoutSetting.NAME_AND_VALUE);
	}

	@Transient
	public DatabaseFieldType getDbType() {
		return DatabaseFieldType.BOOLEAN;
	}

	@Transient
	public FieldTypeDescriptorInfo getFieldDescriptor() throws CantDoThatException {
		FieldTypeDescriptorInfo fieldDescriptor = new FieldTypeDescriptor(FieldCategory.CHECKBOX);
		try {
			String defaultValue = this.getDefault().toString();
			fieldDescriptor.setListOptionSelectedItem(PossibleListOptions.CHECKBOXDEFAULT,
					defaultValue);
			FieldPrintoutSetting printoutSetting = this.getPrintoutSetting();
			fieldDescriptor.setListOptionSelectedItem(PossibleListOptions.PRINTFORMAT, printoutSetting.name());
			// if (this.hasDefault()) {
			// fieldDescriptor.setTextOptionValue(PossibleTextOptions.DEFAULTVALUE,
			// String.valueOf(this.defaultValue.toString()));
			// }
		} catch (ObjectNotFoundException onfex) {
			throw new CantDoThatException("Internal error setting up " + this.getClass()
					+ " field descriptor", onfex);
		}
		return fieldDescriptor;
	}

	@Transient
	public FieldCategory getFieldCategory() {
		return FieldCategory.CHECKBOX;
	}

	public synchronized void setDefault(Boolean defaultValue) throws CantDoThatException {
		super.setDefaultDefined((defaultValue != null));
		this.setDefaultDirect(defaultValue);
	}

	@Transient
	public synchronized Boolean getDefault() {
		return this.getDefaultDirect();
	}

	// @Column(name="bool_defaultvalue")
	private Boolean getDefaultDirect() {
		return this.defaultValue;
	}

	private void setDefaultDirect(Boolean defaultValue) {
		this.defaultValue = defaultValue;
	}

	public synchronized void clearDefault() {
		super.setDefaultDefined(false);
		this.setDefaultDirect(null);
	}

	private Boolean defaultValue = null;
}
