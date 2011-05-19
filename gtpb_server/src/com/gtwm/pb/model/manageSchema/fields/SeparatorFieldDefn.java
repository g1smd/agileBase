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
import com.gtwm.pb.model.interfaces.fields.SeparatorField;
import com.gtwm.pb.model.manageSchema.FieldTypeDescriptor;
import com.gtwm.pb.model.manageSchema.FieldTypeDescriptor.FieldCategory;
import com.gtwm.pb.model.manageSchema.ListFieldDescriptorOption.FieldPrintoutSetting;
import com.gtwm.pb.model.manageSchema.fields.AbstractField;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.RandomString;
import com.gtwm.pb.util.Enumerations.DatabaseFieldType;

@Entity
public class SeparatorFieldDefn extends AbstractField implements SeparatorField {

	private SeparatorFieldDefn() {
	}
	
	public SeparatorFieldDefn(TableInfo tableContainingField, String internalFieldName,
			String fieldName, String fieldDesc) throws CodingErrorException {
		super.setTableContainingField(tableContainingField);
		if (internalFieldName == null) {
			super.setInternalFieldName((new RandomString()).toString());
		} else {
			super.setInternalFieldName(internalFieldName);
		}
		super.setFieldName(fieldName);
		super.setFieldDescription(fieldDesc);
		super.setPrintoutSetting(FieldPrintoutSetting.NAME_AND_VALUE);
		try {
			super.setUnique(false);
			super.setNotNull(false);
		} catch (CantDoThatException cdtex) {
			throw new CodingErrorException("Error setting separator field unique or not null property", cdtex);
		}
	}

	@Transient
	public FieldCategory getFieldCategory() {
		return FieldCategory.SEPARATOR;
	}

	@Transient
	public FieldTypeDescriptorInfo getFieldDescriptor() throws CantDoThatException {
		try {
			return new FieldTypeDescriptor(FieldCategory.SEPARATOR);
		} catch (ObjectNotFoundException onfex) {
			throw new CantDoThatException("Internal error setting up " + this.getClass()
					+ " field descriptor", onfex);

		}
	}

	@Transient
	/**
	 * No database type - data isn't stored in the database for separator fields
	 */
	public DatabaseFieldType getDbType() {
		return null;
	}

}
