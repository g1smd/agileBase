/*  Copyright 2011 GT webMarque Ltd
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
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.FieldTypeDescriptorInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.fields.ReferencedReportDataField;
import com.gtwm.pb.model.manageSchema.BaseReportDefn;
import com.gtwm.pb.model.manageSchema.FieldTypeDescriptor;
import com.gtwm.pb.model.manageSchema.FieldTypeDescriptor.FieldCategory;
import com.gtwm.pb.model.manageSchema.ListFieldDescriptorOption.FieldPrintoutSetting;
import com.gtwm.pb.model.manageSchema.ListFieldDescriptorOption.PossibleListOptions;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.RandomString;
import com.gtwm.pb.util.Enumerations.DatabaseFieldType;

@Entity
public class ReferencedReportDataFieldDefn extends AbstractField implements
		ReferencedReportDataField {

	private ReferencedReportDataFieldDefn() {
	}

	public ReferencedReportDataFieldDefn(TableInfo tableContainingField, String internalFieldName,
			String fieldName, String fieldDesc, BaseReportInfo referencedReport)
			throws CodingErrorException {
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
			throw new CodingErrorException(
					"Error setting separator field unique or not null property", cdtex);
		}
		this.setReferencedReport(referencedReport);
		super.setPrintoutSetting(FieldPrintoutSetting.NAME_AND_VALUE);
	}

	@Transient
	public FieldCategory getFieldCategory() {
		return FieldCategory.REFERENCED_REPORT_DATA;
	}

	@Transient
	public FieldTypeDescriptorInfo getFieldDescriptor() throws CantDoThatException {
		FieldTypeDescriptorInfo fieldDescriptor = new FieldTypeDescriptor(
				FieldCategory.REFERENCED_REPORT_DATA);
		try {
			fieldDescriptor.setListOptionSelectedItem(PossibleListOptions.LISTREPORT, this
					.getReferencedReport().getInternalReportName(), this.getReferencedReport()
					.getReportName());
			FieldPrintoutSetting printoutSetting = this.getPrintoutSetting();
			fieldDescriptor.setListOptionSelectedItem(PossibleListOptions.PRINTFORMAT, printoutSetting.name());
		} catch (ObjectNotFoundException onfex) {
			throw new CantDoThatException("Internal error setting up " + this.getClass()
					+ " field descriptor", onfex);
		}
		return fieldDescriptor;
	}

	@Transient
	/**
	 * No database type - data isn't directly stored in the database for referenced report data fields
	 */
	public DatabaseFieldType getDbType() {
		return null;
	}

	@ManyToOne(targetEntity = BaseReportDefn.class)
	public BaseReportInfo getReferencedReport() throws CantDoThatException {
		return this.referencedReport;
	}

	private void setReferencedReport(BaseReportInfo referencedReport) {
		this.referencedReport = referencedReport;
	}

	private BaseReportInfo referencedReport = null;
}
