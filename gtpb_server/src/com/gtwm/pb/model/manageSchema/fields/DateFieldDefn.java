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

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import org.grlea.log.SimpleLogger;
import com.gtwm.pb.model.interfaces.fields.DateField;
import com.gtwm.pb.model.interfaces.FieldTypeDescriptorInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.manageSchema.FieldTypeDescriptor;
import com.gtwm.pb.model.manageSchema.BooleanFieldDescriptorOption.PossibleBooleanOptions;
import com.gtwm.pb.model.manageSchema.FieldTypeDescriptor.FieldCategory;
import com.gtwm.pb.model.manageSchema.ListFieldDescriptorOption.FieldPrintoutSetting;
import com.gtwm.pb.model.manageSchema.ListFieldDescriptorOption.PossibleListOptions;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.RandomString;
import com.gtwm.pb.util.Helpers;
import com.gtwm.pb.util.Enumerations.DatabaseFieldType;

@Entity
public class DateFieldDefn extends AbstractField implements DateField {
	/**
	 * Private constructor only used by the object database db4o Provided for
	 * performance reasons, see the 'Tuning' section 21.2 of the db4o tutorial
	 */
	protected DateFieldDefn() {
	}

	public DateFieldDefn(TableInfo tableContainingField, String internalFieldName,
			String fieldName, String fieldDesc, boolean unique, boolean notNull,
			boolean defaultToNow, int dateResolution, FieldPrintoutSetting printoutSetting) throws CantDoThatException {
		super.setTableContainingField(tableContainingField);
		if (internalFieldName == null) {
			super.setInternalFieldName((new RandomString()).toString());
		} else {
			super.setInternalFieldName(internalFieldName);
		}
		super.setFieldName(fieldName);
		super.setFieldDescription(fieldDesc);
		super.setUnique(unique);
		if (defaultToNow)
			this.setDefaultToNow(true);
		super.setNotNullDirect(notNull); // TODO: Reference super.setNotNull()
											// once a method of setting
											// defaults has been added
		this.setDateResolution(dateResolution);
		super.setPrintoutSetting(printoutSetting);
	}

	/**
	 * TODO: Need to add check for invalid value
	 */
	public void setDateResolution(int dateResolution) throws CantDoThatException {
		this.setJavaFormatString(Helpers.generateJavaDateFormat(dateResolution));
		this.setDatabaseFormatString(Helpers.generateDbDateFormat(dateResolution));
		this.setDateResolutionDirect(dateResolution);
	}

	@Transient
	public int getDateResolution() {
		return this.dateResolution;
	}

	private void setDateResolutionDirect(int dateResolution) {
		this.dateResolution = dateResolution;
	}

	private int getDateResolutionDirect() {
		return this.dateResolution;
	}

	@Transient
	public DatabaseFieldType getDbType() {
		return DatabaseFieldType.TIMESTAMP;
	}

	@Transient
	public FieldCategory getFieldCategory() {
		return FieldCategory.DATE;
	}

	@Transient
	public FieldTypeDescriptorInfo getFieldDescriptor() throws CantDoThatException {
		try {
			FieldTypeDescriptorInfo fieldDescriptor = new FieldTypeDescriptor(FieldCategory.DATE);
			fieldDescriptor.setListOptionSelectedItem(PossibleListOptions.DATERESOLUTION,
					String.valueOf(this.getDateResolutionDirect()));
			fieldDescriptor.setBooleanOptionState(PossibleBooleanOptions.MANDATORY,
					super.getNotNull());
			fieldDescriptor.setBooleanOptionState(PossibleBooleanOptions.DEFAULTTONOW,
					this.getDefaultToNow());
			FieldPrintoutSetting printoutSetting = this.getPrintoutSetting();
			fieldDescriptor.setListOptionSelectedItem(PossibleListOptions.PRINTFORMAT,
					printoutSetting.name());
			return fieldDescriptor;
		} catch (ObjectNotFoundException onfex) {
			throw new CantDoThatException("Internal error setting up " + this.getClass()
					+ " field descriptor", onfex);
		}
	}

	public synchronized void setDefaultToNow(boolean defaultToNow) {
		super.setDefaultDefined(defaultToNow);
		this.setDefaultToNowDirect(defaultToNow);
	}

	@Transient
	public synchronized boolean getDefaultToNow() {
		return (super.hasDefault() && this.getDefaultToNowDirect());
	}

	private void setDefaultToNowDirect(Boolean defaultToNow) {
		this.defaultToNow = defaultToNow;
	}

	private Boolean getDefaultToNowDirect() {
		return this.defaultToNow;
	}

	@Transient
	public Calendar getDefault() {
		if (this.getDefaultToNow()) {
			Calendar now = new GregorianCalendar();
			now.setTime(new Date());
			return now;
		} else {
			return null;
		}
	}

	public synchronized void clearDefault() {
		super.setDefaultDefined(false);
		this.setDefaultToNowDirect(false);
	}

	public String formatDate(Date dateValue) {
		return String.format(this.getJavaFormatString(), dateValue);
	}

	public String formatCalendar(Calendar dateValue) {
		return String.format(this.getJavaFormatString(), dateValue);
	}

	public String getDatabaseFormatString() {
		return this.databaseFormatString;
	}

	private void setDatabaseFormatString(String databaseFormatString) {
		this.databaseFormatString = databaseFormatString;
	}

	private void setJavaFormatString(String javaFormatString) {
		this.javaFormatString = javaFormatString;
	}

	private String getJavaFormatString() {
		return this.javaFormatString;
	}

	private Integer dateResolution = Calendar.MINUTE;

	/**
	 * Default format to correspond with default dateResolution is 01 Jan 2006
	 * 05:23
	 */
	private String javaFormatString = "%1$td %1$tb %1$tY %1$tH:%1$tM";

	private String databaseFormatString = "DD Mon YYYY HH24:MI";

	private Boolean defaultToNow = false;

	private static final SimpleLogger logger = new SimpleLogger(DateFieldDefn.class);

	public static final Boolean DEFAULT_TO_NOW_TRUE = true;

	public static final Boolean DEFAULT_TO_NOW_FALSE = false;
}
