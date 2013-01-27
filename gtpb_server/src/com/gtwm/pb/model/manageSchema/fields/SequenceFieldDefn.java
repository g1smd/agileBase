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
package com.gtwm.pb.model.manageSchema.fields;

import javax.persistence.Entity;
import javax.persistence.Transient;

import com.gtwm.pb.model.interfaces.FieldTypeDescriptorInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.fields.SequenceField;
import com.gtwm.pb.model.manageSchema.FieldTypeDescriptor;
import com.gtwm.pb.model.manageSchema.FieldTypeDescriptor.FieldCategory;
import com.gtwm.pb.model.manageSchema.ListFieldDescriptorOption.FieldPrintoutSetting;
import com.gtwm.pb.model.manageSchema.ListFieldDescriptorOption.PossibleListOptions;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.Naming;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.RandomString;
import com.gtwm.pb.util.Enumerations.DatabaseFieldType;

@Entity
public class SequenceFieldDefn extends AbstractField implements SequenceField {

	protected SequenceFieldDefn() {
	}

	public SequenceFieldDefn(TableInfo tableContainingField,
			String fieldName, String fieldDesc, FieldPrintoutSetting printoutSetting) throws CantDoThatException {
		super.setTableContainingField(tableContainingField);
		super.setInternalFieldName(RandomString.generate());
		super.setFieldName(fieldName);
		super.setFieldDescription(fieldDesc);
		super.setUnique(true);
		super.setNotNullDirect(true); // bypass hasDefault() check of AbstractField
		super.setPrintoutSetting(printoutSetting);
	}

	public void setNotNull(boolean notNull) throws CantDoThatException {
		// We will always be not null, we don't want orphan records
		throw new CantDoThatException("A sequence field is always not null");
	}

	@Transient
	public FieldTypeDescriptorInfo getFieldDescriptor() throws CantDoThatException {
		FieldPrintoutSetting printoutSetting = this.getPrintoutSetting();
		try {
			FieldTypeDescriptor fieldDescriptor = new FieldTypeDescriptor(FieldCategory.SEQUENCE);
			fieldDescriptor.setListOptionSelectedItem(PossibleListOptions.PRINTFORMAT,
					printoutSetting.name());
			return fieldDescriptor;
		} catch (ObjectNotFoundException onfex) {
			throw new CantDoThatException("Internal error setting up " + this.getClass()
					+ " field descriptor", onfex);
		}
	}

	@Transient
	public DatabaseFieldType getDbType() {
		return DatabaseFieldType.SERIAL;
	}

	@Transient
	public FieldCategory getFieldCategory() {
		return FieldCategory.SEQUENCE;
	}
	
	@Transient
	public String getSimpleName() {
		return Naming.getSimpleName(this.getFieldName());
	}
	
	@Transient
	public boolean isPrimaryKey() {
		return this.equals(this.getTableContainingField().getPrimaryKey());
	}

}
