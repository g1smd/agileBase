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
package com.gtwm.pb.model.manageSchema;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.grlea.log.SimpleLogger;
import com.gtwm.pb.model.interfaces.ReportFieldInfo;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.manageSchema.fields.AbstractField;

@Entity
public class ReportFieldDefn extends AbstractReportField implements ReportFieldInfo {

	protected ReportFieldDefn() {
	}

	/**
	 * Construct a report field that came from a table
	 */
	public ReportFieldDefn(BaseReportInfo parentReport, BaseField baseField) {
		this.setParentReport(parentReport);
		this.setBaseField(baseField);
	}

	/**
	 * Construct a report field that came from a different report
	 */
	public ReportFieldDefn(BaseReportInfo parentReport, BaseReportInfo reportFieldIsFrom,
			BaseField baseField) {
		this(parentReport, baseField);
		this.setReportFieldIsFromDirect(reportFieldIsFrom);
	}

	@ManyToOne(targetEntity = AbstractField.class)
	// Uni-directional many to one: a BaseField may be used in more than one
	// report
	public BaseField getBaseField() {
		return this.baseField;
	}

	@Transient
	public String getFieldName() {
		return this.getBaseField().getFieldName();
	}

	@Transient
	public String getFieldDescription() {
		return this.getBaseField().getFieldDescription();
	}

	@Transient
	public String getInternalFieldName() {
		return this.getBaseField().getInternalFieldName();
	}

	private void setBaseField(BaseField baseField) {
		this.baseField = baseField;
	}

	/**
	 * A report field is defined as equal to another if the BaseField objects
	 * and parent report objects are the same
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		ReportFieldInfo otherReportField = (ReportFieldInfo) obj;
		if (otherReportField.getBaseField().equals(this.getBaseField())
				&& otherReportField.getParentReport().equals(this.getParentReport())) {
			return true;
		}
		return false;
	}

	/**
	 * hashCode() consistent with equals() above
	 */
	public int hashCode() {
		return this.getBaseField().hashCode() + this.getParentReport().hashCode();
	}

	/**
	 * Provide a natural sort order by parent report then field index then
	 * BaseField name + internal name
	 */
	public int compareTo(ReportFieldInfo otherField) {
		if (this == otherField) {
			return 0;
		}
		BaseReportInfo otherReport = otherField.getParentReport();
		int compare = this.getParentReport().compareTo(otherReport);
		if (compare != 0) {
			return compare;
		}
		Integer otherFieldIndex = otherField.getFieldIndex();
		compare = this.getFieldIndex().compareTo(otherFieldIndex);
		if (compare != 0) {
			return compare;
		}
		String otherFieldName = otherField.getBaseField().getFieldName();
		String thisFieldName = this.getBaseField().getFieldName();
		compare = thisFieldName.compareToIgnoreCase(otherFieldName);
		if (compare != 0) {
			return compare;
		}
		String thisInternalName = this.getBaseField().getInternalFieldName();
		String otherInternalName = otherField.getBaseField().getInternalFieldName();
		return (thisInternalName).compareTo(otherInternalName);
	}

	public String toString() {
		return this.getBaseField().toString();
	}

	private BaseField baseField = null;

	private static final SimpleLogger logger = new SimpleLogger(ReportFieldDefn.class);
}
