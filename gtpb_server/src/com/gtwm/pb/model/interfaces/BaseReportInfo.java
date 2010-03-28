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
package com.gtwm.pb.model.interfaces;

import com.gtwm.pb.model.interfaces.fields.BaseField;
import java.util.Set;
import java.util.SortedSet;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.CodingErrorException;
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
	public ReportSummaryInfo getReportSummary();

	/**
	 * Return a specific summary identified by ID
	 */
	public ReportSummaryInfo getSavedReportSummary(long summaryId) throws ObjectNotFoundException;
	
	/**
	 * Return any additional report summaries that have been named and saved
	 */
	public Set<ReportSummaryInfo> getSavedReportSummaries();
	
	/**
	 * Add a new saved summary to the report
	 */
	public void saveReportSummary(ReportSummaryInfo reportSummary);
	
	public void removeSavedReportSummary(ReportSummaryInfo reportSummary);
	
	public int getRowCount();

	public void setRowCount(int rowCountEstimate);

	/**
	 * Return whether the row count is definitive or an estimate based on a
	 * sample of data
	 */
	public boolean isRowCountEstimate();

	public void setRowCountEstimate(boolean rowCountIsEstimate);
}