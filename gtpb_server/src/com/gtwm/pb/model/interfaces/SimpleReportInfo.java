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
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.CodingErrorException;

/**
 * A representation of an SQL VIEW, with a few bells and whistles
 * 
 * A note on compareTo, equals and hashCode, which should be implemented by
 * concrete classes for TableInfo, ReportInfo and all field types: All of these
 * should compare on object name(s) case insensitively because this is how they
 * will often be displayed to the user. Internal names such as returned by
 * getInternalTableName are used by the database and sometimes code as
 * identifiers but are not the primary object identifiers.
 */
public interface SimpleReportInfo extends BaseReportInfo {
	/**
	 * Adds a simple table field to the report
	 * 
	 * @return The created report field
	 */
	public ReportFieldInfo addTableField(BaseField fieldToAdd) throws CantDoThatException,
			CodingErrorException;

	/**
	 * Add a field from another report into this report
	 * 
	 * @return The report field added, which may be the same as
	 *         reportFieldToAdd, or may be a new wrapper around it (if it was a
	 *         calculation for example)
	 */
	public ReportFieldInfo addReportField(ReportFieldInfo reportFieldToAdd)
			throws CantDoThatException, CodingErrorException;

	/**
	 * Add a pre-built ReportFieldInfo object directly to the set of report fields,
     * This method is for memory rollback purposes when calling servletSchemaMethods.removeReportField()
	 */
	public void addField(ReportFieldInfo reportField)
		throws CantDoThatException;
	
	/**
	 * Adds a new calculation to the report
	 */
	public void addCalculation(ReportCalcFieldInfo reportCalcField);

	/**
	 * @throws CantDoThatException
	 *             If you try to remove the only field remaining in a report
	 */
	public void removeField(ReportFieldInfo reportField) throws CantDoThatException,
			ObjectNotFoundException;

	public void setFieldIndex(int index, ReportFieldInfo fieldToOrder);

	public JoinClauseInfo getJoinByInternalName(String internalJoinName) throws ObjectNotFoundException;
	
	/**
	 * @return A read-only copy set of the joins in the report, sorted by join
	 *         creation time
	 */
	public SortedSet<JoinClauseInfo> getJoins();

	/**
	 * @throws ObjectNotFoundException
	 *             If join with the given internal name is not found in the
	 *             report
	 * @throws CantDoThatException
	 *             If the join can't be removed because report fields still use
	 *             it
	 */
	public void removeJoin(JoinClauseInfo join) throws ObjectNotFoundException,
			CantDoThatException, CodingErrorException;

	public void addJoin(JoinClauseInfo join);

	/**
	 * Add a filter. A report can contain multiple filters - each filter is for
	 * one field
	 * 
	 * @throws CantDoThatException
	 *             If the filter field filterToAdd.getFilterField() is not in
	 *             any of the tables contained in the report
	 */
	public void addFilter(ReportFilterInfo filterToAdd) throws CantDoThatException;

	public void removeFilter(ReportFilterInfo filter);

	/**
	 * @throws CantDoThatException
	 *             If the sort field isn't in the joined reports
	 */
	public void addSort(ReportFieldInfo sortReportField, boolean ascendingSort)
			throws CantDoThatException, ObjectNotFoundException;

	/**
	 * Change the direction of an existing sort within a report
	 */
	public void updateSort(ReportFieldInfo sortReportField, boolean ascending)
		throws CantDoThatException, ObjectNotFoundException;
	
	/**
	 * Remove sort if one exists for sortReportField
	 * @return The removed sort, or null if none was removed
	 */
	public ReportSortInfo removeSort(ReportFieldInfo sortReportField)
		throws CantDoThatException, ObjectNotFoundException;
	
	public ReportFilterInfo getFilterByInternalName(String internalFilterName) throws ObjectNotFoundException;
	
	/**
	 * @return A read-only copy set of all the filters in the report, specifying
	 *         which fields are used as filters and what the filter values are
	 */
	public Set<ReportFilterInfo> getFilters();

	/**
	 * Return a read-only copy collection of all the fields the report is sorted
	 * by, i.e. all which are used in the SQL ORDER BY clause in the report's
	 * view.
	 */
	public SortedSet<ReportSortInfo> getSorts();

	/**
	 * Specify one of the fields in the report as being part of the set of
	 * fields defining a distinct record. i.e. if there are fields within this
	 * set, the report should return only the first record with a given set of
	 * values for these fields
	 * 
	 * @param field -
	 *            a field of the report
	 */
	public void addDistinctField(BaseField field) throws ObjectNotFoundException;

	/**
	 * Remove a field from the set of fields defining a distinct record
	 * 
	 * @param field
	 */
	public void removeDistinctField(BaseField field);

	/**
	 * @return set of fields defining a distinct record for the report's
	 *         resultset
	 */
	public Set<BaseField> getDistinctFields();

	/**
	 * Return a set of all tables used in the report. Contains at least the
	 * report's parent table.
	 * 
	 * @throws CodingErrorException
	 *             If a join object in the report is inconsistent
	 */
	public SortedSet<TableInfo> getJoinedTables() throws CodingErrorException;

	/**
	 * Return a set of reports used in this report. Contains at least the
	 * current report itself.
	 * 
	 * @throws CodingErrorException
	 *             If a join object in the report is inconsistent
	 */
	public SortedSet<BaseReportInfo> getJoinedReports() throws CodingErrorException;

	/**
	 * Utility method to return all joined tables in this report as found by getJoinedTables() <i>plus</i>
	 * all the parent tables of joined reports.
	 */
	public SortedSet<TableInfo> getJoinReferencedTables() throws CodingErrorException;
}