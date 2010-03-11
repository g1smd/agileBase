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

import java.util.Map;
import java.util.Set;
import java.sql.Connection;
import java.sql.SQLException;
import com.gtwm.pb.util.MissingParametersException;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.InconsistentStateException;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.TableDependencyException;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.fields.RelationField;
import com.gtwm.pb.auth.DisallowedException;
import com.gtwm.pb.util.Enumerations.DatabaseFieldType;
import com.gtwm.pb.util.Enumerations.SummaryGroupingModifier;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

/**
 * Contains a collection of tables and reports (database views). It is the main
 * repository of schema functionality. Tables in turn contain fields, and
 * reports reference the same fields. Fields and reports contain a reference
 * back to their parent table.
 * 
 * @see com.gtwm.pb.model.interfaces.TableInfo The TableInfo class that defines
 *      a stored table
 */
public interface DatabaseInfo {

	/**
	 * Create a new table and add to the database
	 * 
	 * @param request
	 *            Allows the method to get the current user to test privileges
	 * @param sessionData
	 *            The method needs access to sessionData in order to set the
	 *            current table to the one added
	 * @throws DisallowedException
	 *             If the current user doesn't have ADMINISTRATE privileges
	 * @throws CantDoThatException
	 *             If an internal error occurred while constructing the table,
	 *             or the logged in user can't be found in the app's list of
	 *             users
	 */
	public TableInfo addTable(SessionDataInfo sessionData, HttpServletRequest request,
			Connection conn, String internalTableName, String internalDefaultReportName,
			String tableName, String internalPrimaryKeyName, String tableDesc) throws SQLException,
			DisallowedException, CantDoThatException, ObjectNotFoundException, CodingErrorException;

	public void setDefaultTablePrivileges(HttpServletRequest request, TableInfo newTable)
			throws DisallowedException, CantDoThatException;

	/**
	 * Change the name, description or options of an existing table
	 * 
	 * @throws CantDoThatException
	 *             If there is already a table with the new name
	 * @throws DisallowedException
	 *             If the logged in user doesn't have MANAGE_TABLE privileges
	 *             for the table
	 * @throws ObjectNotFoundException
	 *             If the logged in user isn't found in the application's list
	 *             of users
	 */
	public void updateTable(Connection conn, HttpServletRequest request, TableInfo table,
			String newTableName, String newTableDesc, Boolean lockable) throws DisallowedException,
			CantDoThatException, ObjectNotFoundException, SQLException;

	/**
	 * Remove table tableToRemove provided no dependencies exist; otherwise
	 * throw a TableDependencyException
	 * 
	 * @param sessionData
	 * @param request
	 * @param tableToRemove
	 * @throws SQLException
	 * @throws DisallowedException
	 * @throws CantDoThatException
	 * @throws TableDependencyException
	 * @throws CodingErrorException
	 *             If the tableToRemove is an audit table but no parent table
	 *             can be found
	 */
	public void removeTable(SessionDataInfo sessionData, HttpServletRequest request,
			TableInfo tableToRemove, Connection conn) throws SQLException, DisallowedException,
			CantDoThatException, TableDependencyException, CodingErrorException,
			ObjectNotFoundException;

	/**
	 * Adds TableInfo objects to dependentTables where all tables added are
	 * dependent (through RelationField) on baseTable or any table directly or
	 * indirectly dependent upon baseTable. Access dependentTables argument as
	 * return value.
	 * 
	 * @param baseTable
	 * @param dependentTables
	 */
	public void getDependentTables(TableInfo baseTable, Set<TableInfo> dependentTables,
			HttpServletRequest request) throws ObjectNotFoundException;

	/**
	 * Adds TableInfo objects to dependentTables where all tables added are
	 * directly dependent (through RelationField) on baseTable. Access
	 * dependentTables argument as return value.
	 * 
	 * @param baseTable
	 * @param dependentTables
	 */
	public void getDirectlyDependentTables(TableInfo baseTable, Set<TableInfo> dependentTables,
			HttpServletRequest request) throws ObjectNotFoundException;

	/**
	 * Create a report and add it to the database
	 * 
	 * @param request
	 *            Allows the method to get the current user to test privileges
	 * @param sessionData
	 *            The method needs access to sessionData in order to set the
	 *            current report to the one added
	 * @param populateReport
	 *            Whether to add all current table fields into the new report or
	 *            not
	 * @throws DisallowedException
	 *             If the current user doesn't have MANAGE_TABLE privilege for
	 *             the report's parent table
	 */
	public BaseReportInfo addReport(SessionDataInfo sessionData, HttpServletRequest request,
			Connection conn, TableInfo table, String internalReportName, String reportName,
			String reportDesc, boolean populateReport) throws SQLException, DisallowedException,
			CantDoThatException, CodingErrorException, ObjectNotFoundException,
			MissingParametersException;

	/**
	 * Change the name and/or description of a report
	 * 
	 * @throws DisallowedException
	 *             If the logged in user doesn't have MANAGE_TABLE privileges on
	 *             the report's parent table
	 * @throws CantDoThatException
	 *             If a report with the specified name already exists
	 */
	public void updateReport(Connection conn, HttpServletRequest request, BaseReportInfo report,
			String newReportName, String newReportDesc, ModuleInfo newModule)
			throws DisallowedException, CantDoThatException, SQLException;

	/**
	 * 
	 * @param sessionData
	 *            If removing the current session report, set the session report
	 *            to be another one
	 * @param request
	 *            Allows the method to get the current user to test privileges
	 * @param report
	 *            Report to remove
	 * @throws SQLException
	 * @throws DisallowedException
	 *             If the current user doesn't have MANAGE_TABLE privileges for
	 *             the report
	 * @throws CantDoThatException
	 *             If the report is the last one in the table or the default
	 *             report
	 * @throws CodingErrorException
	 *             If the report's parent table is an audit table but no
	 *             associated 'normal' table can be found for it
	 */
	public void removeReport(SessionDataInfo sessionData, HttpServletRequest request,
			Connection conn, BaseReportInfo report) throws SQLException, DisallowedException,
			CantDoThatException, CodingErrorException, ObjectNotFoundException;

	/**
	 * Add a field to a table
	 * 
	 * @param request
	 *            Allows the method to get the current user to test privileges
	 * @param table
	 *            Table to add the field to
	 * @param fieldType
	 *            The generic 'user interface' field type as read from the input
	 *            form. It doesn't differentiate between int or float numbers,
	 *            normal or multi-line text for example
	 * @throws ObjectNotFoundException
	 *             If the table with identifier internalTableName isn't found
	 * @throws CantDoThatException
	 *             Due to an internal coding error: see
	 *             DatabaseDefn.addField(Connection, TableInfo, BaseField) for
	 *             details. OR an un-recognised field type
	 * @throws DisallowedException
	 *             If the current user doesn't have MANAGE_TABLE privileges for
	 *             table
	 */
	public BaseField addField(HttpServletRequest request, Connection conn, TableInfo table,
			String internalFieldName, String fieldType, String fieldName, String fieldDesc,
			boolean unique, boolean hidden) throws SQLException, ObjectNotFoundException,
			DisallowedException, CantDoThatException, CodingErrorException;

	/**
	 * Update basic field details - name and description
	 */
	public void updateField(HttpServletRequest request, BaseField field, String fieldName,
			String fieldDesc) throws DisallowedException;

	/**
	 * Update simple options for a table field, such as whether to use a lookup
	 * for content, date precision etc.
	 */
	public void updateFieldOption(HttpServletRequest request, BaseField field)
			throws DisallowedException, CantDoThatException, CodingErrorException, SQLException;

	public void setFieldIndex(TableInfo table, BaseField field, int newindex)
			throws ObjectNotFoundException, CantDoThatException;

	public void setReportFieldIndex(Connection conn, SimpleReportInfo report,
			ReportFieldInfo field, int newindex, HttpServletRequest request) throws SQLException,
			CodingErrorException, ObjectNotFoundException, CantDoThatException;

	/**
	 * Add a foreign key database relation between tables
	 * 
	 * @param tableToAddTo
	 * @param relatedTable
	 *            The table containing the related field
	 * @param relatedField
	 * @param notNull
	 *            Whether the relation field should have a not null constraint
	 *            on it
	 * @throws SQLException
	 * @throws DisallowedException
	 *             If the current user doesn't have MANAGE_TABLE privileges for
	 *             tableToAddTo or VIEW_TABLE_DATA privileges for relatedTable
	 * @throws CantDoThatException
	 *             Due to an internal coding bug - see the private
	 *             DatabaseDefn.addField for details
	 */
	public RelationField addRelation(HttpServletRequest request, Connection conn,
			TableInfo tableToAddTo, String internalFieldName, String fieldName, String fieldDesc,
			TableInfo relatedTable, BaseField relatedField) throws SQLException,
			DisallowedException, CantDoThatException, ObjectNotFoundException, CodingErrorException;

	// public void removeTable(String InternalTableName);

	/**
	 * Remove field from memory and database
	 * 
	 * @throws DisallowedException
	 *             If the current user doesn't have MANAGE_TABLE privileges for
	 *             table
	 * @throws CantDoThatException
	 *             If you try to delete the primary key field of a table
	 */
	public void removeField(HttpServletRequest request, Connection conn, BaseField field)
			throws SQLException, DisallowedException, CantDoThatException, CodingErrorException,
			ObjectNotFoundException;

	/**
	 * @param report
	 *            The report to add the field to
	 * @param field
	 *            Identifier of the field to add
	 * @throws SQLException
	 * @throws InconsistentStateException
	 *             Shouldn't happen, see method code if it does
	 * @return The added field
	 */
	public ReportFieldInfo addFieldToReport(HttpServletRequest request, Connection conn,
			SimpleReportInfo report, SimpleReportInfo sourceReport, BaseField field)
			throws SQLException, DisallowedException, InconsistentStateException,
			CantDoThatException, CodingErrorException, ObjectNotFoundException;

	public void addSortToReport(HttpServletRequest request, Connection conn,
			SimpleReportInfo report, ReportFieldInfo reportField, boolean ascending)
			throws DisallowedException, CantDoThatException, SQLException, CodingErrorException,
			ObjectNotFoundException;

	public void updateSortFromReport(HttpServletRequest request, Connection conn,
			SimpleReportInfo report, ReportFieldInfo reportField, boolean ascending)
			throws DisallowedException, CantDoThatException, SQLException, CodingErrorException,
			ObjectNotFoundException;

	public void removeSortFromReport(HttpServletRequest request, Connection conn,
			SimpleReportInfo report, ReportFieldInfo reportField) throws DisallowedException,
			CantDoThatException, SQLException, CodingErrorException, ObjectNotFoundException;

	/**
	 * @throws CantDoThatException
	 *             If the report only has one field which we are trying to
	 *             remove. A report must contain at least one field
	 * @throws DisallowedException
	 *             If the current user doesn't have MANAGE_TABLE privileges for
	 *             the table containing report
	 */
	public void removeFieldFromReport(HttpServletRequest request, Connection conn,
			ReportFieldInfo reportField) throws SQLException, DisallowedException,
			CantDoThatException, CodingErrorException, ObjectNotFoundException;

	/**
	 * @param calculationDefn
	 *            SQL definition of the calculation, e.g 'table1.field1 * 0.175'
	 * @throws DisallowedException
	 *             If current user doesn't have MANAGE_TABLE privileges for the
	 *             report's parent table
	 * @throws InconsistentStateException
	 *             Don't worry about this - see DatabaseDefn method code if you
	 *             want
	 */
	public void addCalculationToReport(HttpServletRequest request, Connection conn,
			SimpleReportInfo report, ReportCalcFieldInfo calculationField) throws SQLException,
			DisallowedException, InconsistentStateException, CantDoThatException,
			CodingErrorException, ObjectNotFoundException;

	public void updateCalculationInReport(HttpServletRequest request, Connection conn,
			SimpleReportInfo report, ReportCalcFieldInfo calculationField, String calculationName,
			String calculationDefn, DatabaseFieldType dbFieldType) throws DisallowedException,
			SQLException, ObjectNotFoundException, CantDoThatException, CodingErrorException;

	public void returnCalculationInReportToMemory(HttpServletRequest request, Connection conn,
			SimpleReportInfo report, ReportCalcFieldInfo calculationField,
			String oldCalculationName, String oldCalculationDefn, DatabaseFieldType oldDbFieldType)
			throws DisallowedException, CodingErrorException, CantDoThatException,
			ObjectNotFoundException;

	/**
	 * Create a filter object, add it to the report, update the database view
	 * representing the report
	 * 
	 * @param report
	 *            Report to add filter to
	 * @param filterField
	 *            Field being filtered on
	 * @param filterType
	 *            GREATER_THAN, EQUAL etc.
	 * @param filterValues
	 *            A list of values being filtered on. Will be just one value for
	 *            all filter field types apart from dropdowns
	 * @throws DisallowedException
	 *             If current user doesn't have MANAGE_TABLE privileges for the
	 *             report's parent table
	 * @throws CantDoThatException
	 *             If the field is not able to be used as a filter for this
	 *             report, because it isn't in one of the tables used in it
	 * @see com.gtwm.pb.util.Enumerations.FilterType - a list of recognised
	 *      filter type values
	 */
	public void addFilterToReport(HttpServletRequest request, Connection conn,
			SimpleReportInfo report, ReportFilterInfo filter) throws SQLException,
			DisallowedException, CantDoThatException, CodingErrorException, ObjectNotFoundException;

	public Map<TableInfo, Set<BaseReportInfo>> getViewableDataStores(HttpServletRequest request)
			throws CodingErrorException, ObjectNotFoundException;

	/**
	 * @param internalFilterName
	 *            Identifier of the filter as retrieved by
	 *            filter.getInternalName()
	 * @throws DisallowedException
	 *             If the logged in user doesn't have MANAGE_TABLE privileges
	 * @throws ObjectNotFoundException
	 *             If a filter with the given internalFilterName isn't found in
	 *             the report
	 */
	public void removeFilterFromReport(HttpServletRequest request, Connection conn,
			SimpleReportInfo report, ReportFilterInfo filter) throws DisallowedException,
			ObjectNotFoundException, CantDoThatException, SQLException, CodingErrorException;

	public void addJoinToReport(HttpServletRequest request, Connection conn,
			SimpleReportInfo report, JoinClauseInfo join) throws DisallowedException, SQLException,
			CantDoThatException, CodingErrorException, ObjectNotFoundException;

	public void removeJoinFromReport(HttpServletRequest request, Connection conn,
			SimpleReportInfo report, JoinClauseInfo join) throws DisallowedException, SQLException,
			CantDoThatException, CodingErrorException, ObjectNotFoundException;

	/**
	 * For a report's summary, add a grouping into the 'GROUP BY' SQL satement
	 * 
	 * @param report
	 *            The report that contains the summary report to use
	 * 
	 * @throws DisallowedException
	 *             If the logged in user doesn't have MANAGE_TABLE privileges on
	 *             the report's parent table
	 */
	public void addGroupingToSummaryReport(HttpServletRequest request,
			ReportFieldInfo groupingReportField, SummaryGroupingModifier groupingModifer)
			throws DisallowedException, CantDoThatException, ObjectNotFoundException, SQLException;

	/**
	 * Remove groupings on groupingReportField from the summary of its parent
	 * report
	 */
	public void removeGroupingFromSummaryReport(HttpServletRequest request,
			ReportFieldInfo groupingReportField) throws DisallowedException,
			ObjectNotFoundException, SQLException, CantDoThatException;

	/**
	 * Add an aggregate function so a summary report
	 */
	public void addFunctionToSummaryReport(HttpServletRequest request,
			ReportSummaryAggregateInfo addedAggFn) throws DisallowedException, CantDoThatException,
			ObjectNotFoundException, SQLException;

	/**
	 * Remove all aggregate functions based on the field supplied. Acts on the
	 * parent report of the supplied functionReportField
	 */
	public void removeFunctionFromSummaryReport(HttpServletRequest request, BaseReportInfo report,
			String internalAggregateName) throws DisallowedException, CantDoThatException,
			ObjectNotFoundException, SQLException;

	/**
	 * Makes a copy of the current summary report and saves it in the collection
	 * of named summary reports for the report. The current summary report will
	 * then be reset - all groupings and calculations removed.
	 */
	public void saveSummaryReport(HttpServletRequest request, BaseReportInfo report,
			String summaryTitle) throws DisallowedException, CantDoThatException,
			ObjectNotFoundException;

	public void removeSummaryReport(HttpServletRequest request, ReportSummaryInfo reportSummary)
			throws DisallowedException, CantDoThatException, ObjectNotFoundException;

	/**
	 * Returns the TableInfo object that has the required internal name
	 * identifier
	 * 
	 * @param internalTableName
	 *            the identifier of the table we're looking for
	 * @return the found table
	 * @throws ObjectNotFoundException
	 *             if we can't find the table we're looking for
	 * @throws DisallowedException
	 *             To retrieve a table, user either has to have view privileges
	 *             on that table, or be an administrator of the company the
	 *             table is in
	 */
	public TableInfo getTableByInternalName(HttpServletRequest request, String internalTableName)
			throws ObjectNotFoundException, DisallowedException;

	/**
	 * @throws DisallowedException
	 *             To retrieve a table, user either has to have view privileges
	 *             on that table, or be an administrator of the company the
	 *             table is in
	 */
	public TableInfo getTableByName(HttpServletRequest request, String tableName)
			throws ObjectNotFoundException;

	/**
	 * @param reportInternalName
	 *            Identifier of the report
	 * @return The table containing the given report. To obtain a report object
	 *         we need to first know the table containing it so this method
	 *         helps us if we don't. NB in normal situations we *should* know
	 *         the table containing it already. Only in the case of a session
	 *         timeout or something will we not.
	 * @throws ObjectNotFoundException
	 *             If the report doesn't exist in *any* table
	 * @see com.gtwm.pb.model.interfaces.TableInfo#getReportByInternalName(String)
	 *      Use TableInfo.getReportByInternalName(...) directly instead if
	 *      possible, it is more efficient
	 */
	public TableInfo findTableContainingReport(HttpServletRequest request, String reportInternalName)
			throws ObjectNotFoundException, DisallowedException;

	/**
	 * @throws DisallowedException
	 *             If the user doesn't have VIEW privileges on the table found
	 */
	public TableInfo findTableContainingField(HttpServletRequest request, String internalFieldName)
			throws ObjectNotFoundException, DisallowedException;

	/**
	 * Finds a report field object if you don't know its parent table or report.
	 * An inefficient method, knowing the parent is preferable
	 * 
	 * @throws DisallowedException
	 *             If the user doesn't have privileges to view the report to be
	 *             returned
	 */
	public ReportFieldInfo findReportFieldByInternalName(HttpServletRequest request,
			String internalFieldName) throws ObjectNotFoundException, DisallowedException,
			CodingErrorException;

	/**
	 * Creates a new module and adds it to the company of the logged in user
	 */
	public ModuleInfo addModule(HttpServletRequest request) throws ObjectNotFoundException,
			DisallowedException;

	/**
	 * Return a reference to the object that manages and caches database data.
	 * databaseDefn keeps one instance of this object
	 */
	public DataManagementInfo getDataManagement();

	public DataSource getDataSource();

	/**
	 * Return a reference to the object that allows integration with the wiki
	 */
	public WikiManagementInfo getWikiManagement(CompanyInfo company);

	public void addWikiManagement(CompanyInfo company, WikiManagementInfo wikiManagement);

	/**
	 * @return A reference to the object that manages access control and
	 *         privileges for the application
	 */
	public AuthManagerInfo getAuthManager();

	/**
	 * Should be run at application shutdown to cancel any background tasks
	 */
	public void cancelScheduledEvents();

}