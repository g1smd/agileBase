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
package com.gtwm.pb.model.manageSchema.fields;

import javax.persistence.Entity;
import javax.persistence.Transient;
import com.gtwm.pb.model.interfaces.FieldTypeDescriptorInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.fields.FileField;
import com.gtwm.pb.model.manageSchema.FieldTypeDescriptor;
import com.gtwm.pb.model.manageSchema.FieldTypeDescriptor.FieldCategory;
import com.gtwm.pb.util.Enumerations.DatabaseFieldType;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.RandomString;

@Entity
public class FileFieldDefn extends AbstractField implements FileField {

	protected FileFieldDefn() {
	}

	public FileFieldDefn(TableInfo tableContainingField, String internalFieldName,
			String fieldName, String fieldDesc) throws CodingErrorException {
		super.setTableContainingField(tableContainingField);
		if (internalFieldName == null) {
			super.setInternalFieldName((new RandomString()).toString());
		} else {
			super.setInternalFieldName(internalFieldName);
		}
		super.setFieldName(fieldName);
		super.setFieldDescription(fieldDesc);
		try {
			super.setUnique(false);
			super.setNotNull(false);
		} catch (CantDoThatException cdtex) {
			throw new CodingErrorException("Error setting file field to unique or not null", cdtex);
		}
	}

	@Transient
	public FieldCategory getFieldCategory() {
		return FieldCategory.FILE;
	}

	/**
	 * File fields will return VARCHAR, as files aren't currently stored
	 * directly in the database, rather a filename is stored as a link to each
	 * actual file
	 */
	@Transient
	public DatabaseFieldType getDbType() {
		return DatabaseFieldType.VARCHAR;
	}

	@Transient
	public FieldTypeDescriptorInfo getFieldDescriptor() throws CantDoThatException {
		FieldTypeDescriptorInfo fieldDescriptor = new FieldTypeDescriptor(FieldCategory.FILE);
//		try {
//			fieldDescriptor.setListOptionSelectedItem(PossibleListOptions.TEXTCONTENTSIZE, String
//					.valueOf(TextContentSizes.FEW_WORDS.getNumChars()));
//		} catch (ObjectNotFoundException onfex) {
//			throw new CantDoThatException("Internal error setting up " + this.getClass()
//					+ " field descriptor", onfex);
//		}
		return fieldDescriptor;
	}

}
