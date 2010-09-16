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

import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Map;
import java.util.EnumSet;
import com.gtwm.pb.auth.DisallowedException;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.fields.BaseValue;
import com.gtwm.pb.model.interfaces.fields.RelationField;
import com.gtwm.pb.model.manageData.DataRow;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.auth.PrivilegeType;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Methods that are used by the view component of the MVC application to pass
 * data to the user interface. The user interface's 'view on the application'.
 * 
 * An instance of this object will be created whenever control is passed to the
 * user interface. It will have access to the user's session data and the
 * application privilege checking object
 * 
 * Note: methods prefixed with 'admin' are those that are likely to be useful
 * for an administration interface rather than in screens seen by a non-admin
 * but the UI is free to use any methods from this class as it sees fit.
 * Privilege checks are carried out in all methods in which they necessary
 */
public interface ViewMethodsInfo {

	/**
	 * Return company usage statistics
	 */
	public UsageStatsInfo getUsageStats() throws DisallowedException, ObjectNotFoundException,
			SQLException;

	/**
	 * Get the list of custom actions for a particular application group. This
	 * list has to be built using the addGroupAction method
	 */
	public List<ModuleActionInfo> getModuleActions(String reportGrouping);

	/**
	 * @see #getAppGroupActions(String)
	 */
	public void addModuleAction(String reportGrouping, String actionName, String description,
			String attributes, String actionTemplate, String buttons, String callbackFunction);

	/**
	 * Returns true if a wiki is running on this computer and integrated into
	 * agileBase via the servlet container's server.xml
	 * 
	 * Returns false if no wiki functionality will be integrated into agileBase
	 */
	public boolean isWikiIntegrated() throws ObjectNotFoundException, DisallowedException;

	/**
	 * Returns true if the current session record is locked for editing, taking
	 * into account any override
	 */
	public boolean isRecordLocked() throws SQLException, ObjectNotFoundException;

	public boolean isRecordLocked(TableInfo table, int rowId) throws SQLException,
			ObjectNotFoundException;

	/**
	 * Return the first part of the text of a corresponding wiki page, formatted
	 * as HTML for display, or an empty string if no page exists with the given
	 * name
	 * 
	 * The results of this method can therefore also be used to check if a wiki
	 * page exists
	 * 
	 * @param wikiPageName
	 *            The title of the wiki page to get content out of
	 * @param numChars
	 *            The number of characters to return from the start of the
	 *            content
	 * 
	 * @throws CantDoThatException
	 *             If no wiki is integrated
	 * @throws SQLException
	 *             if there was an internal error getting the content
	 */
	public String getWikiPageSnippet(String wikiPageName, int numChars) throws CantDoThatException,
			SQLException, DisallowedException, ObjectNotFoundException;

	/**
	 * Return the URL of a wiki page, given it's name
	 * 
	 * @param edit
	 *            If true, return the URL to edit the page, if false, to view it
	 */
	public String getWikiPageUrl(String wikiPageName, boolean edit) throws ObjectNotFoundException,
			DisallowedException;

	/**
	 * Acts on the session table
	 * 
	 * @see #getUnchosenRelationFields(TableInfo)
	 */
	public List<RelationField> getUnchosenRelationFields() throws DisallowedException;

	/**
	 * Return a list of relation fields in the given table that have no rowIDs
	 * set in the session
	 * 
	 * This is to allow the interface to see if any relation fields will need
	 * values choosing before adding a new record
	 * 
	 * @throws DisallowedException
	 *             if the user can't view data in the given table
	 */
	public List<RelationField> getUnchosenRelationFields(TableInfo table)
			throws DisallowedException;

	/**
	 * Return true if the current request included a toolbar name to display in
	 * the INCLUDE_TOOLBAR_PLUGIN parameter:
	 * 
	 * INCLUDE_TOOLBAR_PLUGIN=pluginname:<br>
	 * Include template gui/plugins/[pluginname]/toolbar.vm in the toolbar
	 */
	public boolean usesToolbarPlugin();

	/**
	 * Return toolbarname - see usesToolbarPlugin()
	 * 
	 * @see #usesToolbarPlugin()
	 */
	public String getToolbarPluginName();

	public SortedSet<CompanyInfo> getCompanies() throws DisallowedException;

	/**
	 * @see com.gtwm.pb.model.interfaces.AuthManagerInfo#getPrivilegeTypes(HttpServletRequest)
	 */
	public EnumSet<PrivilegeType> adminGetPrivilegeTypes() throws DisallowedException;

	/**
	 * Provides information to let the user create a database relation. A list
	 * of fields from other tables is returned, from which one can then be
	 * picked to add to the current table to form a relation. Relations can only
	 * be created to unique fields in other tables so only these are returned.
	 * 
	 * Relations can also only be created to tables that the user has privileges
	 * to view. This is more for convenience than security (it may not be the
	 * admin who'se going to be viewing the report anyway) - we don't want the
	 * admin to see masses of unnecessary tables
	 * 
	 * The return data structure is a map of tables to the set of unique fields
	 * in each table.
	 * 
	 * The table to get relation candidates for is stored in the user session
	 * 
	 * @throws DisallowedException
	 *             If the user doesn't have MANAGE_TABLE privileges on the
	 *             current session table
	 */
	public SortedMap<TableInfo, SortedSet<BaseField>> adminGetRelationCandidates()
			throws DisallowedException, ObjectNotFoundException;

	/**
	 * @see com.gtwm.pb.model.interfaces.AuthManagerInfo#getRoles(HttpServletRequest)
	 */
	public SortedSet<AppRoleInfo> adminGetRoles() throws DisallowedException,
			ObjectNotFoundException;

	/**
	 * @see com.gtwm.pb.model.interfaces.AuthManagerInfo#getRolesForUser(HttpServletRequest,
	 *      AppUserInfo)
	 */
	public SortedSet<AppRoleInfo> adminGetRolesForUser(AppUserInfo user) throws DisallowedException;

	/**
	 * @see com.gtwm.pb.model.interfaces.AuthManagerInfo#getUsers(HttpServletRequest)
	 */
	public SortedSet<AppUserInfo> adminGetUsers() throws DisallowedException,
			ObjectNotFoundException;

	/**
	 * @return Application name as set statically in AppProperties
	 * @see com.gtwm.pb.util.AppProperties
	 */
	public String getApplicationName();

	/**
	 * @return Application version as set statically in AppProperties
	 * @see com.gtwm.pb.util.AppProperties
	 */
	public String getApplicationVersion();

	/**
	 * return any possible joins to tables from this report, not including any
	 * that are already joined
	 */
	public List<JoinClauseInfo> getCandidateJoins(SimpleReportInfo report)
			throws CodingErrorException, ObjectNotFoundException;

	/**
	 * Return a list of related records in the related table for each
	 * RelationField in the session table. The lists are stored in a Map via the
	 * RelationField object. If a user doesn't have VIEW_TABLE_DATA privileges
	 * on a child table, that table's data isn't included.
	 * 
	 * DataRowObjects have a method of the same name, so you can use this
	 * recursively to get child rows of child rows
	 * 
	 * @deprecated Functionality replaced by getRelatedRowIds
	 */
	@Deprecated
	public Map<RelationField, List<DataRow>> getChildDataTableRows() throws DisallowedException,
			SQLException, ObjectNotFoundException, CodingErrorException;

	/**
	 * @deprecated Functionality replaced by getRelatedRowIds
	 */
	@Deprecated
	public Map<RelationField, List<DataRow>> getChildDataTableRows(TableInfo table, int rowid)
			throws DisallowedException, SQLException, ObjectNotFoundException, CodingErrorException;

	/**
	 * Calls DatabaseInfo#getDependentTables to retrieve a Set tables dependent
	 * upon the Session Table
	 * 
	 * @see com.gtwm.pb.model.interfaces.DatabaseInfo#getDependentTables(TableInfo,
	 *      LinkedHashSet)
	 */
	public Set<TableInfo> getDependentTables() throws ObjectNotFoundException;

	/**
	 * Calls DatabaseInfo#getDependentTables to retrieve a Set tables dependent
	 * upon the Table passed as an argument to the method
	 * 
	 * @see com.gtwm.pb.model.interfaces.DatabaseInfo#getDependentTables(TableInfo,
	 *      LinkedHashSet)
	 */
	public Set<TableInfo> getDependentTables(TableInfo baseTable) throws ObjectNotFoundException;

	/**
	 * If an exception occurred during request processing, this method can be
	 * used to get information about what went wrong. Methods such as
	 * 'getMessage' can be used on the Exception object returned. It may also be
	 * useful to check the type of the exception. Some types of exception that
	 * can be returned are listed in the 'See Also' section below
	 * 
	 * @return The exception object describing the error, or null if there was
	 *         no exception
	 * @see #getWhetherExceptionOccurred() Check the result of
	 *      getWhetherExceptionOccured() before using this method
	 * @see com.gtwm.pb.util.ObjectNotFoundException
	 * @see java.lang.NumberFormatException
	 * @see com.gtwm.pb.util.MissingParametersException
	 * @see com.gtwm.pb.auth.DisallowedException
	 * @see com.gtwm.pb.util.AgileBaseException General AgileBaseException which
	 *      is a superclass of e.g. ObjectNotFoundException and
	 *      DisallowedException
	 * @see java.sql.SQLException
	 */
	public Exception getException();

	/**
	 * @return The list of field values for a particular record, so the record
	 *         can be displayed or edited. The session will know what table and
	 *         row id to return values for. If we are editing a record that
	 *         there was an error saving (which the session will also know),
	 *         then the redisplay values will be shown
	 * @throws DisallowedException
	 *             If the user doesn't have VIEW_TABLE_DATA privileges on the
	 *             current session table
	 * @throws ObjectNotFoundException
	 *             If no record can be found for the row id in the table in the
	 *             session
	 */
	public Map<BaseField, BaseValue> getTableDataRow() throws DisallowedException,
			ObjectNotFoundException, SQLException, CantDoThatException, CodingErrorException;

	/**
	 * Get table data for a specific table, rather than the session table. The
	 * rowid used will be the session rowid for that table
	 * 
	 * @see com.gtwm.pb.model.interfaces.SessionDataInfo#setRowId(TableInfo,
	 *      int) See sessionData.setRowId for how to set the row id for a
	 *      specific table
	 */
	public Map<BaseField, BaseValue> getTableDataRow(TableInfo table) throws DisallowedException,
			ObjectNotFoundException, SQLException, CantDoThatException, CodingErrorException;

	/**
	 * @see #getTableDataRow()
	 */
	public Map<BaseField, BaseValue> getTableDataRow(TableInfo table, int rowId)
			throws DisallowedException, ObjectNotFoundException, SQLException, CantDoThatException,
			CodingErrorException;

	/**
	 * When passed a row ID, finds rows in the session report with the parent
	 * table's row ID set to that ID. For each row found, if relatedTable's
	 * primary key is in the report, return the value of it.
	 * 
	 * For example, if the session report is orgs and masterRowId specifies a
	 * particular organisation, you can find any related contacts by passing
	 * 'contacts' as the related table.
	 * 
	 * This assumes that orgs is a report from the organisations table which has
	 * a join in it to contacts, and the contacts primary key is present in the
	 * orgs report
	 * 
	 * @param masterRowId
	 *            Row ID used as lookup: a value of the session report parent
	 *            table's primary key
	 * @param relatedTable
	 *            Table whose primary key value(s) we want from the session
	 *            report
	 * @throws CantDoThatException
	 *             if relatedTable's primary key isn't in the report
	 */
	public Set<Integer> getRelatedRowIds(int masterRowId, TableInfo relatedTable)
			throws DisallowedException, CantDoThatException, SQLException, CodingErrorException;

	/**
	 * @see #getRelatedRowIds(int, TableInfo)
	 */
	public Set<Integer> getRelatedRowIds(BaseReportInfo masterReport, int masterRowId,
			TableInfo relatedTable) throws DisallowedException, CantDoThatException, SQLException,
			CodingErrorException;

	/**
	 * Use this method to get the session report rows, using the session report,
	 * row limit and session filter values
	 * 
	 * @return An ArrayList of ReportDataRow objects reflecting the rows
	 *         (records) of the report. The session will know which report to
	 *         show, what filtering to perform etc.
	 * @throws DisallowedException
	 *             If the user doesn't have VIEW_TABLE_DATA privileges for all
	 *             tables from which report fields are taken
	 * @throws SQLException
	 *             If an exception occurs whilst communicating with the database
	 * @throws ObjectNotFoundException
	 *             If there's no report in the session
	 */
	public List<DataRowInfo> getReportDataRows() throws DisallowedException, SQLException,
			ObjectNotFoundException, CodingErrorException, CantDoThatException;

	/**
	 * Return a report data object that contains metadata about the session
	 * report data. Note, to get actual report rows which is the more usual
	 * case, use getReportDataRows instead
	 * 
	 * @see getReportDataRows
	 **/
	public ReportDataInfo getReportData() throws SQLException, DisallowedException,
			CodingErrorException, ObjectNotFoundException;

	/**
	 * Return a report data object that contains metadata about the report data.
	 * Note, to get actual report rows which is the more usual case, use
	 * getReportDataRows instead
	 * 
	 * @see getReportDataRows
	 **/
	public ReportDataInfo getReportData(BaseReportInfo report) throws SQLException,
			DisallowedException, CodingErrorException, ObjectNotFoundException;

	/**
	 * Use this method to get rows for a specified report, using the session row
	 * limit - at the time of writing, this defaults to 100 - and session filter
	 * values
	 * 
	 * @return An ArrayList of ReportDataRow objects reflecting the rows
	 *         (records) of the report.
	 * @throws DisallowedException
	 *             If the user doesn't have VIEW_TABLE_DATA privileges for all
	 *             tables from which report fields are taken
	 * @throws SQLException
	 *             If an exception occurs whilst communicating with the database
	 * @throws ObjectNotFoundException
	 *             If the passed report is null
	 */
	public List<DataRowInfo> getReportDataRows(BaseReportInfo report) throws DisallowedException,
			SQLException, ObjectNotFoundException, CodingErrorException, CantDoThatException;

	/**
	 * Use this method to get rows for a specified report, using a custom row
	 * limit (max. no. rows returned) and session filter values. -1 means no
	 * limit
	 */
	public List<DataRowInfo> getReportDataRows(BaseReportInfo report, int rowLimit)
			throws DisallowedException, SQLException, ObjectNotFoundException,
			CodingErrorException, CantDoThatException;

	/**
	 * Get report rows taking nothing from the session, i.e. specify report, row
	 * limit and filter values explicitly
	 */
	public List<DataRowInfo> getReportDataRows(BaseReportInfo report, int rowLimit,
			Map<BaseField, String> filterValues, boolean exactFilters) throws DisallowedException,
			SQLException, ObjectNotFoundException, CodingErrorException, CantDoThatException;

	/**
	 * Returns true if the record identified by the session row ID is visible in
	 * the session report
	 */
	public boolean isRowIdInReport() throws SQLException;

	/**
	 * @see WikiManagementInfo#getWikiRecordDataRows(CompanyInfo, String,
	 *      String)
	 */
	public List<WikiRecordDataRowInfo> getWikiRecordDataRows(String pageNameFilter,
			String pageContentFilter) throws CantDoThatException, SQLException,
			DisallowedException, ObjectNotFoundException;

	/**
	 * @return Report summary data for the default summary in the session report
	 *         - can be used to display a chart/table of report summary data,
	 *         i.e aggregate data
	 * @throws DisallowedException
	 *             If the user doesn't have VIEW_TABLE_DATA privileges on all
	 *             tables from which report data is taken
	 */
	public ReportSummaryDataInfo getReportSummaryData() throws DisallowedException, SQLException,
			ObjectNotFoundException, CodingErrorException, CantDoThatException;

	/**
	 * Return report summary data for a specific report
	 */
	public ReportSummaryDataInfo getReportSummaryData(ReportSummaryInfo reportSummary)
			throws DisallowedException, SQLException, ObjectNotFoundException,
			CodingErrorException, CantDoThatException;

	/**
	 * Generate a temporary report summary (not persisted) for an individual
	 * field and return the data
	 */
	public ReportSummaryDataInfo getFieldSummaryData(ReportFieldInfo reportField)
			throws DisallowedException, SQLException, CodingErrorException, ObjectNotFoundException, CantDoThatException;

	/**
	 * Return report summary data for a specific report, forcing the use of
	 * cached data if available, even if the company schema or data has been
	 * changed since the summary was saved to cache
	 */
	public ReportSummaryDataInfo getCachedReportSummaryData(ReportSummaryInfo reportSummary)
			throws DisallowedException, ObjectNotFoundException, CodingErrorException,
			CantDoThatException, SQLException;

	/**
	 * Return a tag cloud for the data in the current session report. The words
	 * come from the most relevant text fields in the report. Session filters
	 * are applied.
	 * 
	 * Each tag is a word with a weight dependent on frequency in the report
	 * 
	 * @param minWeight
	 *            A minimum size/weight, e.g. a font size
	 * @param maxWeight
	 *            A max. size/weight
	 * @param maxTags
	 *            The max. number of tags that will be returned
	 */
	public SortedSet<TagInfo> getReportTagCloud(int minWeight, int maxWeight, int maxTags)
			throws ObjectNotFoundException, DisallowedException, CodingErrorException,
			CantDoThatException, SQLException;

	/**
	 * Get a tag cloud for specific field contents only in a specific report
	 * 
	 * Also allow specific stop words to be set to blacklist unwanted terms
	 */
	public SortedSet<TagInfo> getReportTagCloud(BaseReportInfo report, ReportFieldInfo reportField,
			Set<String> stopWords, int minWeight, int maxWeight, int maxTags)
			throws ObjectNotFoundException, DisallowedException, CodingErrorException,
			CantDoThatException, SQLException;

	/**
	 * @return All reports in the table provided, that the user has privileges
	 *         to view
	 */
	public SortedSet<BaseReportInfo> getViewableReports(TableInfo table)
			throws CodingErrorException;

	/**
	 * Return all viewable reports from all viewable tables, in one list
	 */
	public SortedSet<BaseReportInfo> getAllViewableReports() throws CodingErrorException,
			ObjectNotFoundException;

	/**
	 * @param tableID
	 *            The internal table name of the table, or it's public facing
	 *            name. Using the public facing name is more expensive, so the
	 *            internal fixed name is preferred, however it may be easier to
	 *            use the public name when rapid prototyping
	 * @return Table identified
	 * @throws ObjectNotFoundException
	 */
	public TableInfo getTable(String tableID) throws ObjectNotFoundException, DisallowedException;

	/**
	 * 
	 * @param reportInternalName
	 *            - unique internal identifier for the sought report
	 * @return Parent table for sought report, (then use
	 *         table.getReportByInternalName(reportInternalName) )
	 * @throws DisallowedException
	 *             If the logged in user doesn't have privileges to view the
	 *             found table
	 */
	public TableInfo findTableContainingReport(String reportInternalName)
			throws DisallowedException, ObjectNotFoundException;

	/**
	 * @return All tables that the user has a particular privilege on, e.g.
	 *         VIEW_TABLE_DATA
	 * @see com.gtwm.pb.auth#PrivilegeType See PrivilegeType for a list of valid
	 *      privilege types
	 */
	public SortedSet<TableInfo> getTablesAllowedTo(String privilegeString)
			throws ObjectNotFoundException;

	/**
	 * Return a user object for the currently logged in user, allowing access to
	 * properties such as username, surname, password etc.
	 * 
	 * @throws DisallowedException
	 *             if there's a problem and authentication fails. This shouldn't
	 *             normally happen but could possibly if the session has expired
	 * @throws ObjectNotFoundException
	 *             , if the name of the logged in user doesn't map to a user
	 *             object which could happen if someone deletes a user who's
	 *             still logged into and using the system
	 */
	public AppUserInfo getLoggedInUser() throws DisallowedException, ObjectNotFoundException;

	/**
	 * Generated the code necessary to display a graph of report interrelations
	 * for all reports in a module, by looking at joins. If a report joins to
	 * any other reports/tables outside of this module, those are included as
	 * well.
	 * 
	 * Generated graphviz code.
	 * 
	 * Writes the generated code to a file in the 'module_graphs' folder, with
	 * the filename taken from the internal module name.
	 * 
	 * Also returns the code as a string
	 * 
	 * @see http://www.graphviz.org/
	 */
	public String getModuleGraphCode(ModuleInfo module) throws CodingErrorException, IOException,
			ObjectNotFoundException;

	/**
	 * @return Whether an exception (error) occurred when processing the request
	 * @see #getException() If an exception did occur, use getException to
	 *      retrieve it
	 */
	public boolean getWhetherExceptionOccurred();

	/**
	 * @see #userHasPrivilege(String)
	 */
	public boolean roleHasPrivilege(String privilegeTypeToCheck) throws IllegalArgumentException,
			DisallowedException;

	/**
	 * @see #userHasPrivilege(String, TableInfo)
	 */
	public boolean roleHasPrivilege(String privilegeTypeToCheck, TableInfo table)
			throws IllegalArgumentException, DisallowedException;

	/**
	 * @see #userHasPrivilege(AppUserInfo, String)
	 */
	public boolean roleHasPrivilege(AppRoleInfo role, String privilegeTypeToCheck)
			throws IllegalArgumentException, DisallowedException;

	/**
	 * @see #userHasPrivilege(AppUserInfo, String, TableInfo)
	 */
	public boolean roleHasPrivilege(AppRoleInfo role, String privilegeTypeToCheck, TableInfo table)
			throws IllegalArgumentException, DisallowedException;

	/**
	 * Checks whether the session user has a particular general privilege
	 * 
	 * @return Whether the user in the session has the general privilege
	 *         specified. Note: if the user is a member of a role that has a
	 *         privilege but doesn't him/herself have the privilege, this method
	 *         will still return false, i.e. it doesn't consider roles in the
	 *         same way that the userAllowedTo() methods do
	 * 
	 * @throws ObjectNotFoundException
	 *             If there is no user set in the session
	 * @throws IllegalArgumentException
	 *             If privilegeTypeToCheck isn't a valid privilege type
	 * @throws DisallowedException
	 *             If the logged in user doesn't have ADMINISTRATE privileges
	 */
	public boolean userHasPrivilege(String privilegeTypeToCheck) throws IllegalArgumentException,
			ObjectNotFoundException, DisallowedException;

	/**
	 * Checks whether the session user has a particular table-specific privilege
	 * 
	 * @return Whether the user in the session has the table-specific privilege
	 *         specified. Note: if the user is a member of a role that has a
	 *         privilege but doesn't him/herself have the privilege, this method
	 *         will still return false, i.e. it doesn't consider roles in the
	 *         same way that the loggedInUserAllowedTo() methods do
	 * 
	 * @throws ObjectNotFoundException
	 *             If there is no user set in the session
	 * @throws IllegalArgumentException
	 *             If privilegeTypeToCheck isn't a valid privilege type
	 * @throws DisallowedException
	 *             If the logged in user doesn't have ADMINISTRATE privileges
	 */
	public boolean userHasPrivilege(String privilegeTypeToCheck, TableInfo table)
			throws IllegalArgumentException, ObjectNotFoundException, DisallowedException;

	/**
	 * Checks whether the given user has a particular general privilege
	 */
	public boolean userHasPrivilege(AppUserInfo user, String privilegeTypeToCheck)
			throws DisallowedException;

	/**
	 * Checks whether the given user has a particular table-specific privilege
	 */
	public boolean userHasPrivilege(AppUserInfo user, String privilegeTypeToCheck, TableInfo table)
			throws DisallowedException;

	/**
	 * The only setter in this class - we use this method to save an exception
	 * for later retrieval by the UI, if one occurs. Note: The exception isn't
	 * stored in the session instead because requests can be asynchronous and
	 * simultaneous from the same client, so exceptions need to be stored per
	 * request
	 */
	public void setException(Exception ex);

	/**
	 * Allows the user interface to check whether the current user has a
	 * particular general privilege. This can be used to help choose which
	 * options to make available to the user
	 * 
	 * @param privilegeTypeToCheck
	 *            String representation of the privilege type
	 * @return True if the user has the privilege
	 * @see com.gtwm.pb.auth.PrivilegeType A list of privilege types that can be
	 *      passed as privilegeTypeToCheck
	 * @throws IllegalArgumentException
	 *             If privilegeTypeToCheck doesn't represent a valid
	 *             PrivilegeType
	 */
	public boolean loggedInUserAllowedTo(String privilegeTypeToCheck)
			throws IllegalArgumentException;

	/**
	 * Allows the user interface to check whether the current user has a
	 * particular table-specific privilege. This can be used to help choose
	 * which options to make available to the user
	 * 
	 * @param privilegeTypeToCheck
	 *            String representation of the privilege type
	 * @return True if the user has the privilege
	 * @see com.gtwm.pb.auth.PrivilegeType A list of privilege types that can be
	 *      passed as privilegeTypeToCheck
	 * @throws IllegalArgumentException
	 *             If privilegeTypeToCheck doesn't represent a valid
	 *             PrivilegeType
	 */
	public boolean loggedInUserAllowedTo(String privilegeTypeToCheck, TableInfo table)
			throws IllegalArgumentException;

	/**
	 * Checks whether a report is visible to the logged in user by checking
	 * whether that user has VIEW privileges on the parent table, all joined
	 * tables and recursively all joined reports
	 */
	public boolean loggedInUserAllowedToViewReport(BaseReportInfo report)
			throws CodingErrorException;

	/**
	 * 
	 * @param sourceText
	 * @return sourceText after agileBase object names (enclosed within curly
	 *         braces) have been replaced by internal object identifiers
	 */
	public String toInternalNames(String sourceText) throws ObjectNotFoundException;

	/**
	 * @param sourceText
	 * @return sourceText after agileBase internal object identifiers have been
	 *         replaced by their display names
	 */
	public String toExternalNames(String sourceText) throws ObjectNotFoundException;

	/**
	 * Return the average upload speed of files since agileBase started up, with
	 * newer uploads given a larger weighting
	 * 
	 * @return Bytes per second
	 */
	public int getUploadSpeed();
}
