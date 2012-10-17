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

import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.ChartInfo;
import com.gtwm.pb.model.interfaces.ReportMapInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.ModuleInfo;
import com.gtwm.pb.util.Enumerations.QueryPlanSelection;
import com.gtwm.pb.util.Naming;
import com.gtwm.pb.util.ObjectNotFoundException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.grlea.log.SimpleLogger;

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
	
	@Transient
	public String getColour() {
		// First check if there is a colour override in the report description
		String description = this.getReportDescription().replaceAll("\\n", " ").toLowerCase();
		// Pick 'pink' out of 'the report description. colour: pink' or similar.
		Matcher colourMatcher = Pattern.compile("^(.*colou?r\\s?\\:\\s?)(#?[\\d\\w]+)([\\s\\.].*)?$").matcher(description);
		if (colourMatcher.matches()) {
			String colour = colourMatcher.group(2);
			if (colour != null) {
				return colour;
			}
		}
		// Generate a colour from the report internalName
		int hash1 = Math.abs(this.hashCode());
		int hash2 = Math.abs((this.getInternalReportName() + this.getInternalReportName()).hashCode());
		StringBuilder temp = new StringBuilder(this.getInternalReportName());
		int hash3 = Math.abs(temp.reverse().toString().hashCode());
		int hue = hash1 % 360;
		int saturation = (hash2 % 65) + 20;
		int lightness = (hash3 % 40) + 40;
		return "hsl(" + hue + "," + saturation + "%," + lightness + "%)";
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

	@OneToOne(targetEntity = ChartDefn.class, cascade = CascadeType.ALL)
	public ChartInfo getChart() {
		return this.reportSummary;
	}

	protected void setChart(ChartInfo reportSummary) {
		this.reportSummary = reportSummary;
	}

	public ChartInfo getSavedChart(long summaryId) throws ObjectNotFoundException {
		for (ChartInfo reportSummary : this.getSavedReportSummariesDirect()) {
			if (reportSummary.getId() == summaryId) {
				return reportSummary;
			}
		}
		throw new ObjectNotFoundException("Report summary with ID " + summaryId + " not found in report " + this);
	}
	
	@Transient
	public Set<ChartInfo> getSavedCharts() {
		return Collections.unmodifiableSet(new LinkedHashSet<ChartInfo>(this
				.getSavedReportSummariesDirect()));
	}

	@OneToMany(mappedBy = "report", targetEntity = ChartDefn.class, cascade = CascadeType.ALL)
	private Set<ChartInfo> getSavedReportSummariesDirect() {
		return this.savedReportSummaries;
	}

	private void setSavedReportSummariesDirect(Set<ChartInfo> savedReportSummaries) {
		this.savedReportSummaries = savedReportSummaries;
	}

	public void removeSavedChart(ChartInfo reportSummary) {
		this.getSavedReportSummariesDirect().remove(reportSummary);
	}

	public void saveChart(ChartInfo reportSummary) {
		this.getSavedReportSummariesDirect().add(reportSummary);
	}

	@Transient
	public ReportMapInfo getMap() {
		return this.getMapDirect();
	}
	
	public void setMap(ReportMapInfo reportMap) {
		this.setMapDirect(reportMap);
	}
	
	@OneToOne(targetEntity = ReportMap.class, cascade = CascadeType.ALL)
	private ReportMapInfo getMapDirect() {
		return this.reportMap;
	}
	
	private void setMapDirect(ReportMapInfo reportMap) {
		this.reportMap = reportMap;
	}
	
	public boolean getAllowExport() {
		return this.allowExport;
	}
	
	public void setAllowExport(boolean allowExport) {
		this.allowExport = allowExport;
	}
	
	public Integer getMemoryAllocation() {
		return this.memoryAllocation;
	}
	
	public void setMemoryAllocation(Integer memoryAllocation) {
		this.memoryAllocation = memoryAllocation;
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
	
	@Transient
	public double getViewRank() {
		return this.viewRank;
	}
	
	public void setViewRank(double viewRank) {
		this.viewRank = viewRank;
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
		} else if (this.module != null && otherModule == null) {
			return 1;
		} else if (this.module == null && otherModule != null) {
			// Reports with no module go at the end
			return -1;
		}
		TableInfo otherTable = anotherReportDefn.getParentTable();
		int compare = this.getParentTable().compareTo(otherTable);
		if (compare != 0) {
			// If tables are different, sort based on table so don't need to
			// compare report properties at all
			return compare;
		}
		String otherReportName = anotherReportDefn.getReportName();
		compare = this.getReportName().toLowerCase().compareTo(otherReportName.toLowerCase());
		if (compare != 0) {
			return compare;
		}
		return this.getInternalReportName().compareTo(anotherReportDefn.getInternalReportName());
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
	
	@Transient
	public float getQuerySeconds() {
		return this.querySeconds;
	}
	
	public void setQuerySeconds(float queryTime) {
		this.querySeconds = queryTime;
	}
	
	@Transient
	public QueryPlanSelection getQueryPlanSelection() {
		return this.queryPlanSelection;
	}
	
	public void setQueryPlanSelection(QueryPlanSelection queryPlanSelection) {
		this.queryPlanSelection = queryPlanSelection;
	}

	private Set<ChartInfo> savedReportSummaries = new LinkedHashSet<ChartInfo>(1);

	private String reportName = "";

	private String reportDesc = "";

	private ModuleInfo module;

	private String internalReportName = "";

	private TableInfo parentTable;
	
	private float querySeconds = 0f;
	
	private QueryPlanSelection queryPlanSelection = QueryPlanSelection.DEFAULT;

	private ChartInfo reportSummary = null;

	private ReportMapInfo reportMap = null; 

	private int rowCount = 0;

	private boolean rowCountIsEstimate = false;
	
	private double viewRank = 0d;
	
	private boolean allowExport = false;
	
	private Integer memoryAllocation = null;

	private static final SimpleLogger logger = new SimpleLogger(BaseReportDefn.class);
}
