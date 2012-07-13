package com.gtwm.pb.model.manageSchema.fields;

import javax.persistence.Entity;
import javax.persistence.Transient;

import com.gtwm.pb.model.interfaces.FieldTypeDescriptorInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.fields.CommentFeedField;
import com.gtwm.pb.model.manageSchema.FieldTypeDescriptor;
import com.gtwm.pb.model.manageSchema.FieldTypeDescriptor.FieldCategory;
import com.gtwm.pb.model.manageSchema.ListFieldDescriptorOption.FieldPrintoutSetting;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.RandomString;
import com.gtwm.pb.util.Enumerations.DatabaseFieldType;

@Entity
public class CommentFeedFieldDefn extends AbstractField implements CommentFeedField {

	private CommentFeedFieldDefn() {
	}
	
	public CommentFeedFieldDefn(TableInfo tableContainingField, String internalFieldName, String fieldName,
			String fieldDesc) throws CodingErrorException {
		super.setTableContainingField(tableContainingField);
		if (internalFieldName == null) {
			super.setInternalFieldName(RandomString.generate());
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
	/**
	 * No database type - data isn't stored in the database for separator fields
	 */
	public DatabaseFieldType getDbType() {
		return null;
	}

	@Transient
	public FieldCategory getFieldCategory() throws CodingErrorException {
		return FieldCategory.COMMENT_FEED;
	}

	@Transient
	public FieldTypeDescriptorInfo getFieldDescriptor() throws CantDoThatException,
			CodingErrorException {
		try {
			return new FieldTypeDescriptor(FieldCategory.COMMENT_FEED);
		} catch (ObjectNotFoundException onfex) {
			throw new CantDoThatException("Internal error setting up " + this.getClass()
					+ " field descriptor", onfex);
		}
	}

}
