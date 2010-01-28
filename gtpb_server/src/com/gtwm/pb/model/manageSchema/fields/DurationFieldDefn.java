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

import java.util.Calendar;
import com.gtwm.pb.model.interfaces.fields.DurationField;
import com.gtwm.pb.model.interfaces.fields.DurationValue;
import com.gtwm.pb.model.interfaces.FieldTypeDescriptorInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.manageData.fields.DurationValueDefn;
import com.gtwm.pb.model.manageSchema.FieldTypeDescriptor;
import com.gtwm.pb.model.manageSchema.FieldTypeDescriptor.FieldCategory;
import com.gtwm.pb.model.manageSchema.ListFieldDescriptorOption.PossibleListOptions;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.RandomString;
import com.gtwm.pb.util.Enumerations.DatabaseFieldType;

/**
 * TODO: This class hasn't been made Hibernate compliant. Skipped since PB doesn't use it yet
 */
public class DurationFieldDefn extends AbstractField implements DurationField {

    protected DurationFieldDefn() {
    }

    public DurationFieldDefn(TableInfo tableContainingField, String internalFieldName, String fieldName, String fieldDesc, boolean notNull,
            int durationResolution, DurationValue defaultValue, int durationScale) throws CantDoThatException {
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
        this.setResolution(durationResolution);
        this.setScale(durationScale);
    }

    public DatabaseFieldType getDbType() {
        return DatabaseFieldType.INTERVAL;
    }

    // NOTE: Need check for invalid values
    public void setResolution(int durationResolution) {
        this.durationResolution = durationResolution;
    }

    public int getResolution() {
        return this.durationResolution;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public int getScale() {
        return this.scale;
    }

    public FieldCategory getFieldCategory() {
        return FieldCategory.DURATION;
    }

    public FieldTypeDescriptorInfo getFieldDescriptor() throws CantDoThatException {
        FieldTypeDescriptorInfo fieldDescriptor = new FieldTypeDescriptor(FieldCategory.DURATION);
        try {
            fieldDescriptor.setListOptionSelectedItem(PossibleListOptions.DURATIONRESOLUTION, String.valueOf(this.durationResolution));
            fieldDescriptor.setListOptionSelectedItem(PossibleListOptions.DURATIONSCALE, String.valueOf(this.scale));
        } catch (ObjectNotFoundException onfex) {
            throw new CantDoThatException("Internal error setting up " + this.getClass() + " field descriptor", onfex);
        }
        return fieldDescriptor;
    }

    public synchronized void setDefault(DurationValue defaultValue) throws CantDoThatException {
        if ((this.getNotNull() == true) && (defaultValue == null)) {
            throw new CantDoThatException("A field that cannot be null must have a default value");
        }
        super.setDefaultDefined((defaultValue != null));
        this.defaultValue = defaultValue;
    }

    public synchronized void clearDefault() {
        super.setDefaultDefined(false);
        this.defaultValue = null;
    }

    public synchronized DurationValue getDefault() {
        return this.defaultValue;
    }

    private DurationValue defaultValue = new DurationValueDefn();

    private int durationResolution = Calendar.MINUTE;

    private int scale = Calendar.HOUR_OF_DAY;
}
