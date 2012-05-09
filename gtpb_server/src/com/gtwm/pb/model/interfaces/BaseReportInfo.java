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
package com.gtwm.pb.model.interfaces;

import com.gtwm.pb.model.interfaces.fields.BaseField;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.Enumerations.QueryPlanSelection;
import com.gtwm.pb.util.Enumerations.ReportStyle;
import com.gtwm.pb.util.ObjectNotFoundException;

/**
 * A representation of an SQL VIEW, with a few bells and whistles
 * 
 * A note on compareTo, equals and hashCode, which should be implemented by
 * concrete classes for TableInfo, ReportInfo and all field types: All of these
 * should compare on object name(s) case insensitively because this is how they
 * will often be displayed to the user. Internal names such as returned by
 * getInternalTableName are used by the database and sometimes code as
 * identifiers but are not the business object identifiers.
 */
public interface BaseReportInfo extends Comparable<BaseReportInfo> {

	/**
	 * @return The SQL code needed to generate a database view to return report
	 *         data, given the current report properties - fields, filters etc.
	 */
	public String getSQLForDetail() throws CantDoThatException, CodingErrorException,
			ObjectNotFoundException;

	public void setReportName(String reportName);

	public void setReportDescription(String reportDesc);

	public String getReportName();

	public String getReportDescription();

	public ModuleInfo getModule();

	public void setModule(ModuleInfo module);

	public String getInternalReportName();

	/**
	 * Returns a set of fields in the report, each containing a BaseField object
	 * to see the general field info plus additional report-specific field info
	 * 
	 * @return The fields as a read-only collection, in the order that they
	 *         should be displayed
	 */
	public SortedSet<ReportFieldInfo> getReportFields();

	/**
	 * Returns a set of fields in the report, without the report specific info,
	 * i.e. just as BaseField objects rather than ReportFieldInfo objects. Use
	 * this rather than getReportFields() if you're not interested in any of the
	 * report-specific field information
	 * 
	 * @return The fields as a read-only collection, in the order that they
	 *         should be displayed
	 * @see #getReportFields()
	 */
	public Set<BaseField> getReportBaseFields();

	/**
	 * Return the report field object with the specified ID, either an internal
	 * name (preferred) or a public name
	 */
	public ReportFieldInfo getReportField(String reportFieldID) throws ObjectNotFoundException;

	public TableInfo getParentTable();

	/**
	 * Return the current editable report summary
	 */
	public ChartInfo getChart();

	/**
	 * Return a specific summary identified by ID
	 */
	public ChartInfo getSavedChart(long chartId) throws ObjectNotFoundException;

	/**
	 * Return any additional report summaries that have been named and saved
	 */
	public Set<ChartInfo> getSavedCharts();

	/**
	 * Add a new saved summary to the report
	 */
	public void saveChart(ChartInfo chart);

	public void removeSavedChart(ChartInfo chart);

	public int getRowCount();

	public void setRowCount(int rowCountEstimate);

	/**
	 * Return a rolling average of the number of seconds the report SQL query
	 * takes to run
	 */
	public float getQuerySeconds();

	public void setQuerySeconds(float querySeconds);

	public QueryPlanSelection getQueryPlanSelection();

	public void setQueryPlanSelection(QueryPlanSelection queryPlanSelection);

	public void setCalendarSyncable(Boolean calendarSyncable);

	/**
	 * Return true if it's been set that this report can be synced to a calendar
	 * supporting iCal format, e.g. Google Calendar. Note there is a security
	 * consideration - the iCal export won't be password protected - anyone who
	 * knows the calendar URL will be able to view the calendar
	 */
	public Boolean getCalendarSyncable();

	/**
	 * Return the field that will be used for start dates when syncing data to a
	 * calendar, or null if there's no applicable field in the report.
	 * 
	 * The field is calculated, not selected and stored. Specifically, the last
	 * date field (normal field or calculation) is used that's not an
	 * autogenerated timestamp and that has a resolution of at least a day
	 */
	public ReportFieldInfo getCalendarStartField() throws CodingErrorException;

	public ReportFieldInfo getCalendarEndField() throws CodingErrorException;

	public void setWordCloudField(ReportFieldInfo wordCloudField);

	/**
	 * Return the field whose contents are to be used for generating a word
	 * cloud. If the field is the report table's primary key, a cloud from all
	 * relevant fields in the report joined together will be used
	 */
	public ReportFieldInfo getWordCloudField();

	/**
	 * Return an object to represent map data. If no object exists, create one
	 */
	public ReportMapInfo getMap();

	public void setMap(ReportMapInfo map);

	/**
	 * Transform a string such as 'filter' into a full-text-like filter on the
	 * whole report, or at least all relevant fields, e.g.
	 * 
	 * field1='*filter' or field2='*filter' or field3='*filter'
	 */
	public Map<BaseField, String> getGlobalFilterValues(String globalFilterString)
			throws CodingErrorException;

	public ReportStyle getReportStyle();

	public void setReportStyle(ReportStyle reportStyle);

	/**
	 * If true, report allows exporting of data even if the logged in user
	 * doesn't have MANAGE privileges on the parent table
	 */
	public boolean getAllowExport();

	public void setAllowExport(boolean allowExport);

	/**
	 * Return whether the row count is definitive or an estimate based on a
	 * sample of data
	 */
	public boolean isRowCountEstimate();

	public void setRowCountEstimate(boolean rowCountIsEstimate);

	/**
	 * The default working memory limit for SQL queries can be overriden per report.
	 * Useful if a particular report uses more memory than usual. This can be
	 * seen from an EXPLAIN ANALYZE on the SQL. The value is MB allocated.
	 * 
	 * See the work_mem parameter:
	 * 
	 * @see http
	 *      ://www.postgresql.org/docs/current/static/runtime-config-resource
	 *      .html
	 */
	public Integer getMemoryAllocation();
	
	public void setMemoryAllocation(Integer memoryAllocation);
}