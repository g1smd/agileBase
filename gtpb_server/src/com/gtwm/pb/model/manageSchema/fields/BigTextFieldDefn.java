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

import java.sql.SQLException;
import java.util.Map;
import java.util.SortedSet;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.fields.TextField;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.FieldTypeDescriptorInfo;
import com.gtwm.pb.model.manageSchema.FieldTypeDescriptor;
import com.gtwm.pb.model.manageSchema.FieldTypeDescriptor.FieldCategory;
import com.gtwm.pb.model.manageSchema.BooleanFieldDescriptorOption.PossibleBooleanOptions;
import com.gtwm.pb.model.manageSchema.ListFieldDescriptorOption.PossibleListOptions;
import com.gtwm.pb.model.manageSchema.ListFieldDescriptorOption.TextContentSizes;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.RandomString;
import com.gtwm.pb.util.Enumerations.DatabaseFieldType;
import com.gtwm.pb.util.Enumerations.TextCase;

/**
 * A multi-line text field that could be represented by a <textarea> form element
 * 
 * Deprecated in favour of plain TextFieldDefn instances - all text fields are now represented by 
 * the same type of field to make it possible to switch between sizes without having to change type
 */
@Entity
@Deprecated
public class BigTextFieldDefn extends AbstractField implements TextField {

    protected BigTextFieldDefn() {
    }

    public BigTextFieldDefn(TableInfo tableContainingField, String internalFieldName, String fieldName, String fieldDesc, boolean notNull, String defaultValue)
            throws CantDoThatException {
        super.setTableContainingField(tableContainingField);
        if (internalFieldName == null) {
            super.setInternalFieldName((new RandomString()).toString());
        } else {
            super.setInternalFieldName(internalFieldName);
        }
        super.setFieldName(fieldName);
        super.setFieldDescription(fieldDesc);
        this.setDefault(defaultValue);
        super.setNotNull(notNull);
    }

    /**
     * Big text fields don't implement the 'not applicable' property at the moment
     */
    @Transient
    public boolean allowNotApplicable() {
        return false;
    }

    @Transient
    public String getNotApplicableDescription() throws CantDoThatException {
        throw new CantDoThatException("The not applicable property isn't used in big text fields");
    }

    @Transient
    public String getNotApplicableValue() throws CantDoThatException {
        throw new CantDoThatException("The not applicable property isn't used in big text fields");
    }

    @Transient
    public DatabaseFieldType getDbType() {
        return DatabaseFieldType.VARCHAR;
    }

    @Transient
    public FieldCategory getFieldCategory() {
        return FieldCategory.TEXT;
    }

    @Transient
    public FieldTypeDescriptorInfo getFieldDescriptor() throws CantDoThatException {
        FieldTypeDescriptorInfo fieldDescriptor = new FieldTypeDescriptor(FieldCategory.TEXT);
        try {
            fieldDescriptor.setListOptionSelectedItem(PossibleListOptions.TEXTCONTENTSIZE, String.valueOf(this.getContentSize()));
            fieldDescriptor.setBooleanOptionState(PossibleBooleanOptions.UNIQUE, super.getUnique());
            fieldDescriptor.setBooleanOptionState(PossibleBooleanOptions.MANDATORY, super.getNotNull());
        } catch (ObjectNotFoundException onfex) {
            throw new CantDoThatException("Internal error setting up " + this.getClass() + " field descriptor", onfex);
        }
        return fieldDescriptor;
    }

    public synchronized void setDefault(String defaultValue) throws CantDoThatException {
        super.setDefaultDefined((defaultValue != null));
        this.setDefaultDirect(defaultValue);
    }

    @Transient
    public synchronized String getDefaultCSV() {
    		return this.getDefaultDirect();
    }
    
    @Transient
    public synchronized String getDefault() {
        return this.getDefaultDirect();
    }

    public void clearDefault() throws CantDoThatException {
        this.setDefault(null);
        //super.setDefaultDefined(false);
        //this.defaultValue = null;
    }
    
    private void setDefaultDirect(String defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    private String getDefaultDirect() {
        return this.defaultValue;
    }

    public synchronized void setContentSize(Integer maxChars) {
        this.contentSize = maxChars;
    }

    public synchronized Integer getContentSize() {
        return this.contentSize;
    }
    
    /**
     * Always returns false - big text fields never use a combo lookup
     */
    public boolean usesLookup() {
    	return false;
    }
    
    public void setUsesLookup(Boolean usesLookup) throws CantDoThatException {
    	throw new CantDoThatException("You can't set the big text field " + this + " to use a lookup");
    }

    @Transient
	public SortedSet<String> getItems() throws SQLException, CantDoThatException {
		throw new CantDoThatException("" + this.getClass().getSimpleName() + " can't use getItems()");
	}

	public SortedSet<String> getItems(BaseReportInfo report, Map<BaseField, String> filterValues) throws SQLException, CantDoThatException {
		throw new CantDoThatException("" + this.getClass().getSimpleName() + " can't use getItems()");
	}

	@Enumerated(EnumType.STRING)
	public TextCase getTextCase() {
		return this.textCase;
	}
	
	public void setTextCase(TextCase textCase) {
		this.textCase = textCase;
	}
	
	private TextCase textCase = TextCase.ANY;
	private Integer contentSize = TextContentSizes.FEW_SENTENCES.getNumChars();

    private String defaultValue = null;
}
