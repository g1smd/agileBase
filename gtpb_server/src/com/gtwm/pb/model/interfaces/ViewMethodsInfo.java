/*
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
import com.gtwm.pb.util.MissingParametersException;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.auth.PrivilegeType;
import java.sql.SQLException;
import javax.xml.stream.XMLStreamException;
import org.codehaus.jackson.JsonGenerationException;

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
	 * For a given module, find any modules containing ancestor data. That means
	 * 
	 * * for each of the reports in the module:
	 * 
	 * * find any tables the reports' parent table is depentent on e.g. for contacts, find organisations
	 * 
	 * * for all reports based on that ancestor table, add the modules to the list of modules that's returned
	 * 
	 * Note: use only tables and reports the logged in user's allowed to view
	 */
	public Set<ModuleInfo> getDependentModules(ModuleInfo module) throws CodingErrorException, ObjectNotFoundException;
	
	/**
	 * Returns true if the current session record is locked for editing, taking
	 * into account any override
	 */
	public boolean isRecordLocked() throws SQLException, ObjectNotFoundException;

	public boolean isRecordLocked(TableInfo table, int rowId) throws SQLException,
			ObjectNotFoundException;

	/**
	 * Acts on the session table
	 * 
	 * @see #getUnchosenRelationFields(TableInfo)
	 */
	public List<RelationField> getUnchosenRelationFields() throws DisallowedException,
			ObjectNotFoundException;

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
			throws DisallowedException, ObjectNotFoundException;

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

	public SortedSet<CompanyInfo> getCompanies() throws DisallowedException,
			ObjectNotFoundException;

	/**
	 * Return the list of possible privilege types (public information in the
	 * API)
	 */
	public EnumSet<PrivilegeType> adminGetPrivilegeTypes() throws DisallowedException,
			ObjectNotFoundException;

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
	public SortedSet<AppRoleInfo> adminGetRolesForUser(AppUserInfo user)
			throws DisallowedException, ObjectNotFoundException;

	/**
	 * @see com.gtwm.pb.model.interfaces.AuthManagerInfo#getUsers(HttpServletRequest)
	 */
	public SortedSet<AppUserInfo> adminGetUsers() throws DisallowedException,
			ObjectNotFoundException;

	public SortedSet<AppUserInfo> getAdministrators() throws ObjectNotFoundException, CodingErrorException;
	
	/**
	 * return any possible joins to tables from this report, not including any
	 * that are already joined
	 * 
	 * @param direction
	 *            True = upward joins - child to parent. False = downward joins
	 *            - parent to child
	 */
	public List<JoinClauseInfo> getCandidateJoins(SimpleReportInfo report, boolean direction)
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
	 * Return the location of the user's profile image, or null if they have none
	 */
	public String getUserProfileImage(String internalUserName) throws ObjectNotFoundException;
	
	/**
	 * Calls DatabaseInfo#getDependentTables to retrieve a Set tables dependent
	 * upon the Session Table
	 * 
	 * @see com.gtwm.pb.model.interfaces.DatabaseInfo#getDependentTables(TableInfo,
	 *      LinkedHashSet)
	 */
	public Set<TableInfo> getDependentTables() throws ObjectNotFoundException;

	public Set<TableInfo> getDirectlyDependentTables(TableInfo baseTable)
			throws ObjectNotFoundException;

	/**
	 * Calls DatabaseInfo#getDependentTables to retrieve a Set tables dependent
	 * upon the Table passed as an argument to the method
	 * 
	 * @param direction: true find child tables, false finds parents and ancestors
	 * 
	 * @see com.gtwm.pb.model.interfaces.DatabaseInfo#getDependentTables(TableInfo,
	 *      LinkedHashSet)
	 */
	public Set<TableInfo> getDependentTables(TableInfo baseTable, boolean direction) throws ObjectNotFoundException;

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
	 * Get a set of comments that have been added to a particular field in the
	 * record specified
	 */
	public SortedSet<CommentInfo> getComments(BaseField field, int rowId) throws SQLException,
			DisallowedException, ObjectNotFoundException, CantDoThatException;

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
	 * @see DataRowInfo#childDataRowsExist()
	 */
	public boolean childDataRowsExist(TableInfo parentTable, int parentRowId, TableInfo childTable) throws SQLException,
			DisallowedException, ObjectNotFoundException;

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
			throws DisallowedException, CantDoThatException, SQLException, CodingErrorException,
			ObjectNotFoundException;

	/**
	 * @see #getRelatedRowIds(int, TableInfo)
	 */
	public Set<Integer> getRelatedRowIds(BaseReportInfo masterReport, int masterRowId,
			TableInfo relatedTable) throws DisallowedException, CantDoThatException, SQLException,
			CodingErrorException, ObjectNotFoundException;

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
	 * Allow overriding the stats cache update, in cases where we just want the
	 * data as quickly as possible and are not bothered about the stats
	 * 
	 * @see GetReportData
	 */
	public ReportDataInfo getReportData(BaseReportInfo report, boolean updateCacheIfObsolete)
			throws SQLException, DisallowedException, CodingErrorException, ObjectNotFoundException;

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
	 * Specify a global filter that will be used on all relevant fields in a
	 * report, rather than separate filters for each field. This is somewhat
	 * similar to a full text search
	 */
	public List<DataRowInfo> getGloballyFilteredReportDataRows(BaseReportInfo report)
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
	 * Return report data as JSON
	 */
	public String getReportDataRowsJSON() throws DisallowedException,
			SQLException, ObjectNotFoundException, JsonGenerationException, CodingErrorException, CantDoThatException, XMLStreamException;
	
	/**
	 * Get a calendar feed for a report suitable for use with
	 * 
	 * http://arshaw.com/fullcalendar/
	 * 
	 * The parameters 'start' and 'end' that fullcalendar provides in the HTTP
	 * request are added to the filters taken from the session and applied to
	 * the date field
	 * 
	 * Parameters 'internaltablename' and 'internalreportname' specify the
	 * report. If none are given, the session report is used
	 * 
	 */
	public String getReportCalendarJSON() throws CodingErrorException, CantDoThatException,
			MissingParametersException, DisallowedException, ObjectNotFoundException, SQLException,
			JsonGenerationException;

	/**
	 * Get a timeline feed suitable for use with Simile Timeplot
	 */
	public String getReportTimelineJSON() throws CodingErrorException, CantDoThatException,
			MissingParametersException, DisallowedException, ObjectNotFoundException, SQLException,
			JsonGenerationException;

	public String getReportMapJSON() throws ObjectNotFoundException, CodingErrorException,
			CantDoThatException, SQLException, DisallowedException;

	/**
	 * Returns true if the record identified by the session row ID is visible in
	 * the session report
	 */
	public boolean isRowIdInReport() throws SQLException;

	/**
	 * @return Report summary data for the default summary in the session report
	 *         - can be used to display a chart/table of report summary data,
	 *         i.e aggregate data
	 * @throws DisallowedException
	 *             If the user doesn't have VIEW_TABLE_DATA privileges on all
	 *             tables from which report data is taken
	 */
	public ChartDataInfo getChartData() throws DisallowedException, SQLException,
			ObjectNotFoundException, CodingErrorException, CantDoThatException;

	/**
	 * Return report summary data for a specific report
	 */
	public ChartDataInfo getChartData(ChartInfo reportSummary) throws DisallowedException,
			SQLException, ObjectNotFoundException, CodingErrorException, CantDoThatException;

	/**
	 * Generate a temporary report summary (not persisted) for an individual
	 * field and return the data
	 */
	public ChartDataInfo getFieldSummaryData(ReportFieldInfo reportField)
			throws DisallowedException, SQLException, CodingErrorException,
			ObjectNotFoundException, CantDoThatException;

	/**
	 * Return report summary data for a specific report, forcing the use of
	 * cached data if available, even if the company schema or data has been
	 * changed since the summary was saved to cache
	 */
	public ChartDataInfo getCachedChartData(ChartInfo reportSummary) throws DisallowedException,
			ObjectNotFoundException, CodingErrorException, CantDoThatException, SQLException;

	/**
	 * Return a word cloud for the data in the current session report. The words
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
	public SortedSet<WordInfo> getReportWordCloud(int minWeight, int maxWeight, int maxTags)
			throws ObjectNotFoundException, DisallowedException, CodingErrorException,
			CantDoThatException, SQLException;

	/**
	 * Get a word cloud for specific field contents only in a specific report
	 * 
	 * Also allow specific stop words to be set to blacklist unwanted terms
	 */
	public SortedSet<WordInfo> getReportWordCloud(BaseReportInfo report,
			ReportFieldInfo reportField, Set<String> stopWords, int minWeight, int maxWeight,
			int maxTags) throws ObjectNotFoundException, DisallowedException, CodingErrorException,
			CantDoThatException, SQLException;

	/**
	 * Return all reports in the table provided, that the logged in user has
	 * privileges to view
	 */
	public SortedSet<BaseReportInfo> getViewableReports(TableInfo table)
			throws CodingErrorException;

	/**
	 * Return all viewable reports for the logged in user from all tables, in
	 * one list
	 */
	public SortedSet<BaseReportInfo> getAllViewableReports() throws CodingErrorException,
			ObjectNotFoundException;

	/**
	 * Return all viewable reports for the specified user
	 */
	public SortedSet<BaseReportInfo> adminGetAllViewableReports(AppUserInfo user)
			throws ObjectNotFoundException, DisallowedException, CodingErrorException;

	/**
	 * Returns all tables the user will need view privileges on in order to see
	 * the specified report
	 */
	public Set<TableInfo> getTablesNecessaryToViewReport(BaseReportInfo report)
			throws CodingErrorException;

	/**
	 * @param tableID
	 *            The internal table name of the table, or it's public facing
	 *            name. Using the public facing name is more expensive, so the
	 *            internal fixed name is preferred, however it may be easier to
	 *            use the public name when rapid prototyping
	 * @return Table identified
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
	 * Return whether a join is actively used in the report or is unecessary and can be removed
	 */
	public boolean isJoinUsed(SimpleReportInfo report, JoinClauseInfo join) throws CantDoThatException, CodingErrorException;

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
			DisallowedException, ObjectNotFoundException;

	/**
	 * @see #userHasPrivilege(String, TableInfo)
	 */
	public boolean roleHasPrivilege(String privilegeTypeToCheck, TableInfo table)
			throws IllegalArgumentException, DisallowedException, ObjectNotFoundException;

	/**
	 * @see #userHasPrivilege(AppUserInfo, String)
	 */
	public boolean roleHasPrivilege(AppRoleInfo role, String privilegeTypeToCheck)
			throws IllegalArgumentException, DisallowedException, ObjectNotFoundException;

	/**
	 * @see #userHasPrivilege(AppUserInfo, String, TableInfo)
	 */
	public boolean roleHasPrivilege(AppRoleInfo role, String privilegeTypeToCheck, TableInfo table)
			throws IllegalArgumentException, DisallowedException, ObjectNotFoundException;

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
			throws DisallowedException, ObjectNotFoundException;

	/**
	 * Checks whether the given user has a particular table-specific privilege
	 */
	public boolean userHasPrivilege(AppUserInfo user, String privilegeTypeToCheck, TableInfo table)
			throws DisallowedException, ObjectNotFoundException;

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
			throws IllegalArgumentException, ObjectNotFoundException;

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
