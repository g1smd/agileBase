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
package com.gtwm.pb.model.manageSchema;

import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.ReportSummaryInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.ModuleInfo;
import com.gtwm.pb.util.Naming;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class BaseReportDefn implements BaseReportInfo {

	public void setReportName(String reportName) {
		this.setReportNameDirect(reportName);
	}

	@Transient
	public String getReportName() {
		return Naming.makeValidXML(this.getReportNameDirect());
	}

	private void setReportNameDirect(String reportName) {
		this.reportName = reportName;
	}

	/**
	 * For Hibernate
	 */
	private String getReportNameDirect() {
		return this.reportName;
	}

	public String toString() {
		return this.getReportName();
	}

	public void setReportDescription(String reportDesc) {
		this.reportDesc = reportDesc;
	}

	public String getReportDescription() {
		return this.reportDesc;
	}

	@ManyToOne(targetEntity = Module.class)
	public ModuleInfo getModule() {
		return this.module;
	}

	public void setModule(ModuleInfo module) {
		this.module = module;
	}

	@Id
	public String getInternalReportName() {
		return this.internalReportName;
	}

	/**
	 * To be used by subclasses
	 */
	protected void setInternalReportName(String internalReportName) {
		this.internalReportName = internalReportName;
	}

	@ManyToOne(targetEntity = TableDefn.class)
	// Other side of table.getReports()
	// @OneToOne(mappedBy="defaultReportDirect", targetEntity=TableDefn.class)
	// // Other side of table.getDefaultReport()
	public TableInfo getParentTable() {
		return this.parentTable;
	}

	protected void setParentTable(TableInfo parentTable) {
		this.parentTable = parentTable;
	}

	@OneToOne(mappedBy="report", targetEntity = ReportSummaryDefn.class, cascade = CascadeType.ALL)
	public ReportSummaryInfo getReportSummary() {
		return this.reportSummary;
	}

	protected void setReportSummary(ReportSummaryInfo reportSummary) {
		this.reportSummary = reportSummary;
	}

	public Set<ReportSummaryInfo> getSavedReportSummaries() {
		return Collections.unmodifiableSet(new LinkedHashSet<ReportSummaryInfo>(this
				.getSavedReportSummariesDirect()));
	}

	@OneToMany(mappedBy = "report", targetEntity = ReportSummaryDefn.class, cascade = CascadeType.ALL)
	private Set<ReportSummaryInfo> getSavedReportSummariesDirect() {
		return this.savedReportSummaries;
	}

	private void setSavedReportSummariesDirect(Set<ReportSummaryInfo> savedReportSummaries) {
		this.savedReportSummaries = savedReportSummaries;
	}

	public void removeSavedReportSummary(ReportSummaryInfo reportSummary) {
		this.getSavedReportSummariesDirect().remove(reportSummary);
	}

	public void saveReportSummary(ReportSummaryInfo reportSummary) {
		this.getSavedReportSummariesDirect().add(reportSummary);
	}

	@Transient
	public int getRowCount() {
		return this.rowCount;
	}

	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}

	@Transient
	public boolean isRowCountEstimate() {
		return this.rowCountIsEstimate;
	}

	@Transient
	public void setRowCountEstimate(boolean rowCountIsEstimate) {
		this.rowCountIsEstimate = rowCountIsEstimate;
	}

	/**
	 * Provide a natural sort order by report name case insensitively. Use
	 * module and table in the comparison as well as we may make a collection of
	 * reports from different modules/tables
	 */
	public int compareTo(BaseReportInfo anotherReportDefn) {
		if (this == anotherReportDefn) {
			return 0;
		}
		ModuleInfo otherModule = anotherReportDefn.getModule();
		if (this.module != null && otherModule != null) {
			int moduleCompare = ((Module) this.module).compareTo(otherModule);
			if (moduleCompare != 0) {
				return moduleCompare;
			}
		}
		TableInfo otherTable = anotherReportDefn.getParentTable();
		int tableCompare = this.getParentTable().compareTo(otherTable);
		if (tableCompare != 0) {
			// If tables are different, sort based on table so don't need to
			// compare report properties at all
			return tableCompare;
		}
		String otherReportName = anotherReportDefn.getReportName();
		// Also include internalReportName as that is what equals() is based on
		String otherInternalReportName = anotherReportDefn.getInternalReportName();
		return (this.getReportName().toLowerCase(Locale.UK) + this.getInternalReportName())
				.compareTo(otherReportName.toLowerCase(Locale.UK) + otherInternalReportName);
	}

	/**
	 * equals is based in internal report name
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		BaseReportDefn otherReport = (BaseReportDefn) obj;
		return this.getInternalReportName().equals(otherReport.getInternalReportName());
	}

	public int hashCode() {
		return this.getInternalReportName().hashCode();
	}

	private Set<ReportSummaryInfo> savedReportSummaries = new LinkedHashSet<ReportSummaryInfo>(1);

	private String reportName = "";

	private String reportDesc = "";

	private ModuleInfo module;

	private String internalReportName = "";

	private TableInfo parentTable;

	private ReportSummaryInfo reportSummary = null;

	private int rowCount = 0;

	private boolean rowCountIsEstimate = false;
}
