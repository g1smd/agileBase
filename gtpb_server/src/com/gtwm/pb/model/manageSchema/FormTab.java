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
package com.gtwm.pb.model.manageSchema;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.FormTabInfo;
import com.gtwm.pb.model.interfaces.TableInfo;

@Entity
public class FormTab implements FormTabInfo {

	private FormTab() {
	}

	public FormTab(TableInfo parentTable, TableInfo table, int index) {
		this.setParentTable(parentTable);
		this.setTable(table);
		this.setIndex(index);
	}

	@Id
	@GeneratedValue
	/**
	 * Hibernate needs an ID for a persistent class - this isn't actually used
	 * by the app otherwise
	 */
	protected long getId() {
		return this.id;
	}

	private void setId(long id) {
		this.id = id;
	}

	@ManyToOne(targetEntity = TableDefn.class)
	private TableInfo getParentTable() {
		return this.parentTable;
	}

	private void setParentTable(TableInfo parentTable) {
		this.parentTable = parentTable;
	}

	@ManyToOne(targetEntity = TableDefn.class)
	public TableInfo getTable() {
		return this.table;
	}

	private void setTable(TableInfo table) {
		this.table = table;
	}

	@ManyToOne(targetEntity = BaseReportDefn.class)
	public BaseReportInfo getSelectorReport() {
		return this.selectorReport;
	}

	public void setSelectorReport(BaseReportInfo selectorReport) {
		this.selectorReport = selectorReport;
	}

	public int getIndex() {
		return this.index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String toString() {
		return this.table.getSimpleName();
	}

	/**
	 * Compare by index
	 */
	public int compareTo(FormTabInfo formTab) {
		int indexCompare = Integer.valueOf(index).compareTo(formTab.getIndex());
		if (indexCompare != 0) {
			return indexCompare;
		}
		// Fall back to comparing by table, as per equals
		return this.table.compareTo(formTab.getTable());
	}

	/**
	 * There will only be one tab per table in a form (parent table)
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		FormTab otherFormTab = (FormTab) obj;
		return (this.getTable().equals(otherFormTab.getTable()) && this.getParentTable().equals(
				otherFormTab.getParentTable()));
	}

	public int hashCode() {
		int hashCode = 17;
		hashCode = 37 * hashCode + this.getTable().hashCode();
		hashCode = 37 * hashCode + this.getParentTable().hashCode();
		return hashCode;
	}

	private TableInfo table;

	private TableInfo parentTable;

	private BaseReportInfo selectorReport;

	private int index = 0;

	private long id;

}
