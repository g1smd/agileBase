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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import org.grlea.log.SimpleLogger;
import com.gtwm.pb.model.interfaces.ReportSortInfo;
import com.gtwm.pb.model.interfaces.ReportFieldInfo;
import java.lang.Boolean;

@Entity
public class ReportSort implements ReportSortInfo {

	private ReportSort() {
	}

	/**
	 * @param sortDirection
	 *            true = ascending, false = descending
	 */
	public ReportSort(ReportFieldInfo sortReportField, Boolean sortDirection) {
		this.setSortReportField(sortReportField);
		this.setSortDirection(sortDirection);
	}

	@Id
	@GeneratedValue
	/**
	 * Hibernate needs an ID for a persistent class - this isn't actually used
	 * by the app otherwise
	 */
	private long getId() {
		return this.id;
	}

	private void setId(long id) {
		this.id = id;
	}

	private void setSortReportField(ReportFieldInfo sortReportField) {
		this.sortReportField = sortReportField;
	}

	@ManyToOne(targetEntity = AbstractReportField.class)
	// Uni-directional many to one
	public ReportFieldInfo getSortReportField() {
		return this.sortReportField;
	}

	public void setSortDirection(Boolean booleanVal) {
		this.sortDirection = booleanVal;
	}

	public Boolean getSortDirection() {
		return this.sortDirection;
	}

	/**
	 * Implement equals based on baseFieldKeys being equal. This will make a Set
	 * of BaseFieldBooleanPair objects functionally equivalent to a Map<BaseField,
	 * Boolean>
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		ReportSort otherSort = (ReportSort) obj;
		return (this.getSortReportField().equals(otherSort.getSortReportField()));
	}

	public int hashCode() {
		return this.getSortReportField().hashCode();
	}

	public int compareTo(ReportSortInfo otherSort) {
		return this.getSortReportField().compareTo(otherSort.getSortReportField());
	}

	public String toString() {
		String sortDir = "ascending";
		if (!this.getSortDirection()) {
			sortDir = "descending";
		}
		return this.getSortReportField().toString() + " " + sortDir;
	}

	private ReportFieldInfo sortReportField = null;

	private Boolean sortDirection;

	private long id;

	private static final SimpleLogger logger = new SimpleLogger(ReportSort.class);
}
