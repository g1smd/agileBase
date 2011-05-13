/*
 *  Copyright 2011 GT webMarque Ltd
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

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.fileupload.FileItem;
import org.json.JSONException;
import com.gtwm.pb.auth.DisallowedException;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.fields.RelationField;
import com.gtwm.pb.model.interfaces.fields.BaseValue;
import com.gtwm.pb.model.manageData.InputRecordException;
import com.gtwm.pb.model.manageData.DataRow;
import com.gtwm.pb.util.DataDependencyException;
import com.gtwm.pb.util.Enumerations.DataFormat;
import com.gtwm.pb.util.MissingParametersException;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.Enumerations.FieldContentType;

/**
 * Main store of all data (non-schema) related methods and objects. One instance
 * of this class will be constructed to deal with everything data-related
 */
public interface DataManagementInfo {

	/**
	 * @param company
	 *            Needed for internal caching mechanism. Can be null, but if so,
	 *            no caching will be done
	 * @param reportDefn
	 *            The report definition which contains the list of fields etc.
	 * @param filterValues
	 *            The values requested by the browser, e.g. if the request
	 *            string is ?do=refreshReportData&field1=filtervalue, there will
	 *            be one map key = "field1", one value "filtervalue"
	 * @param exactFilters
	 *            True if filter values should match data exactly (with an ILIKE
	 *            'filtervalue'), false to return any rows where values start
	 *            with the filter (with an ILIKE 'filtervalue%'). Matching is
	 *            always case insensitive in any case
	 * @param sessionSorts
	 *            Use to sort the results by particular a particular field
	 * @param rowLimit
	 *            The maximum number of rows to return, -1 means no limit
	 * @return report data as a list of rows
	 */
	public List<DataRowInfo> getReportDataRows(CompanyInfo company, BaseReportInfo reportDefn,
			Map<BaseField, String> filterValues, boolean exactFilters,
			Map<BaseField, Boolean> sessionSorts, int rowLimit) throws SQLException,
			CodingErrorException, CantDoThatException;

	/**
	 * Return all the text from the specified fields in the report as one big
	 * String of words. Useful for generating tag clouds with. Text will be
	 * returned lower case
	 */
	public String getReportDataText(BaseReportInfo reportDefn, Set<BaseField> fields,
			Map<BaseField, String> reportFilterValues, int rowLimit) throws SQLException,
			CantDoThatException;

	/**
	 * Return a calendar JSON feed for the data in a report, suitable for use
	 * with
	 * 
	 * http://arshaw.com/fullcalendar/
	 * 
	 * or
	 * 
	 * http://www.simile-widgets.org/timeline/
	 * 
	 * @param format
	 *            Either "fullcalendar" or "timeline" to produce JSON compatible
	 *            with either of the two products above
	 * @param filterValues
	 *            Session filters *plus* a filter on the calendar date field so
	 *            that only dates requested by fullcalendar are returned
	 *            (controlled by start and end parameters in the HTTP request)
	 * @param startEpoch
	 *            The start unix timestamp as provided by fullcalendar
	 * @param endEpoch
	 *            The end unix timestamp as provided by fullcalendar
	 */
	public String getReportCalendarJSON(DataFormat format, AppUserInfo user,
			BaseReportInfo report, Map<BaseField, String> filterValues, Long startEpoch,
			Long endEpoch) throws CodingErrorException, CantDoThatException, SQLException,
			JSONException;

	/**
	 * @param user
	 *            Allows the method to log the user that made this request
	 * @param report
	 *            The report to format as JSON or RSS
	 * @param cacheMinutes
	 *            The number of minutes to cache a response for a particular
	 *            report before regenerating
	 */
	public String getReportJSON(AppUserInfo user, BaseReportInfo report, int cacheMinutes)
			throws JSONException, CodingErrorException, CantDoThatException, SQLException, XMLStreamException, ObjectNotFoundException;

	public String getReportRSS(AppUserInfo user, BaseReportInfo report, int cacheMinutes) throws SQLException, CodingErrorException, CantDoThatException, JSONException, XMLStreamException, ObjectNotFoundException;

	/**
	 * Return a report data object that contains metadata about the report data.
	 * Note, to get actual report rows which is the more usual case, use
	 * getReportDataRows instead
	 * 
	 * @see getReportDataRows
	 * 
	 * @param company
	 *            Needed for internal caching mechanism. Can be null, but if so,
	 *            no caching will be done at all
	 * @param updateCacheIfObsolete
	 *            If false, just read the cache, if true update it as well if
	 *            it's obsolete. When you have a low tolerance of out of date
	 *            cached statistics, use true but if you just need rough
	 *            statistics, false can be used
	 */
	public ReportDataInfo getReportData(CompanyInfo company, BaseReportInfo report,
			boolean updateCacheIfObsolete) throws SQLException;

	/**
	 * Return true if the record with the given primary key is visible in the
	 * report
	 */
	public boolean isRowIdInReport(BaseReportInfo reportDefn, int rowId) throws SQLException;

	/**
	 * Return the full path of the root of the web application on the server
	 */
	public String getWebAppRoot();

	/**
	 * A List of related records in the related table for each RelationField in
	 * tableDefn. The lists are stored in a Map via the RelationField object
	 * 
	 * @throws SQLException
	 * @throws ObjectNotFoundException
	 *             If a record with the provided rowid is not found in the table
	 */
	public Map<RelationField, List<DataRow>> getChildDataTableRows(DatabaseInfo databaseDefn,
			TableInfo tableDefn, int rowid, HttpServletRequest request) throws SQLException,
			ObjectNotFoundException, CodingErrorException;

	/**
	 * @param reportDefn
	 *            Report to show summary for
	 * @param company
	 *            Used to inform per-company caching
	 * @param alwaysUseCache
	 *            If true, always use the cached data if any's available
	 * @return The report summary data, or null if the summary isn't valid, e.g.
	 *         doesn't contain an aggregate function
	 */
	public ChartDataInfo getChartData(CompanyInfo company, ChartInfo reportSummaryDefn,
			Map<BaseField, String> reportFilterValues, boolean alwaysUseCache) throws SQLException,
			CantDoThatException;

	/**
	 * Get a single row as a map of field name => value, for editing or viewing
	 * 
	 * @see com.gtwm.pb.model.interfaces.TableDataInfo#getTableDataRow(java.sql.Connection,
	 *      int) See TableData.getTableDataRow for an explanation of the
	 *      exceptions thrown
	 */
	public Map<BaseField, BaseValue> getTableDataRow(TableInfo table, int rowId)
			throws SQLException, ObjectNotFoundException, CantDoThatException, CodingErrorException;

	/**
	 * When passed a row ID and a report, finds rows in the report with the
	 * parent table's row ID set to that ID. For each row found, if
	 * relatedTable's primary key is in the report, return the value of it.
	 * 
	 * @param masterRowId
	 *            Row ID used as lookup: a value of the report parent table's
	 *            primary key
	 * @param relatedTable
	 *            Table whose primary key value(s) we want from the report
	 * @throws CantDoThatException
	 *             if relatedTable's primary key isn't in the report
	 */
	public Set<Integer> getRelatedRowIds(BaseReportInfo masterReport, int masterRowId,
			TableInfo relatedTable) throws CantDoThatException, SQLException;

	/**
	 * Save a new record or update an existing one in a database table. Also
	 * save the field input values in the session for later retrieval
	 * 
	 * @see com.gtwm.pb.model.interfaces.SessionDataInfo#setFieldInputValues(Map)
	 * 
	 * @param table
	 *            Table to save data to
	 * @param dataToSave
	 *            User input data as a map of field => value string. Type is a
	 *            LinkedHashMap rather than a Map because we need to be sure
	 *            that data can be iterated over in the same order twice
	 * @param newRecord
	 *            Whether creating a new record or editing an existing, i.e.
	 *            INSERT or UPDATE
	 * @param rowId
	 *            Will only be used if doing an UPDATE
	 * @param sessionData
	 *            Will be used when doing an INSERT to change the session row id
	 *            to that of the newly inserted row
	 * @throws InputRecordException
	 *             if a field in the record causes an error when trying to add
	 *             to the database, be it an SQL or other error
	 * @throws ObjectNotFoundException
	 *             If the SQL didn't affect any records, or it potentially
	 *             affected more than one
	 * @throws SQLException
	 *             If there was an error closing the SQL connection. All other
	 *             SQL exceptions will be re-thrown as InputRecordExceptions
	 * @throws CodingErrorException
	 *             If there was an internal error writing to the audit table
	 */
	public void saveRecord(HttpServletRequest request, TableInfo table,
			LinkedHashMap<BaseField, BaseValue> dataToSave, boolean newRecord, int rowId,
			SessionDataInfo sessionData, List<FileItem> multipartItems)
			throws InputRecordException, ObjectNotFoundException, SQLException,
			CodingErrorException, DisallowedException, CantDoThatException,
			MissingParametersException;

	public void cloneRecord(HttpServletRequest request, TableInfo table, int rowId,
			SessionDataInfo sessionData, List<FileItem> multipartItems)
			throws ObjectNotFoundException, SQLException, CantDoThatException,
			CodingErrorException, InputRecordException, DisallowedException,
			MissingParametersException;

	/**
	 * Lock all unlocked table records in the current session table
	 */
	public void lockAllTableRecords(HttpServletRequest request, SessionDataInfo sessionData)
			throws ObjectNotFoundException, CantDoThatException, SQLException;

	/**
	 * Lock all records which are in the current report with the current filters
	 * on.
	 * 
	 * NB This can be quite slow for a large number of rows. To speed things up,
	 * pass in filters which reduce the number of rows or use
	 * lockAllTableRecords instead if appropriate which is a lot faster
	 */
	public void lockReportRecords(HttpServletRequest request, SessionDataInfo sessionData)
			throws ObjectNotFoundException, CantDoThatException, SQLException, CodingErrorException;

	/**
	 * Lock an individual record
	 */
	public void lockRecord(SessionDataInfo sessionData, TableInfo table, int rowId)
			throws SQLException, CantDoThatException, ObjectNotFoundException;

	public boolean isRecordLocked(SessionDataInfo sessionData, TableInfo table, int rowId)
			throws SQLException, ObjectNotFoundException;

	/**
	 * Import a CSV file into a table. The import is wrapped in one transaction
	 * so if one record import fails the whole import will fail. CSV fields must
	 * be ordered the same as the table fields being imported into. The first
	 * file encountered in the input form will be imported, the form name
	 * doesn't matter
	 * 
	 * @param updateExistingRecords
	 *            Whether to match the import data to existing records in the
	 *            table or create all new records
	 * @param recordIdentifierField
	 *            If updating existing records, this field is the identifying
	 *            key by which data is matched
	 * @param generateRowIds
	 *            If true, row IDs will be generated as the data's imported. If
	 *            false, they'll be read from the first field of each line in
	 *            the CSV
	 * @param separator
	 *            Field separation character, e.g. a comma
	 * @param quotechar
	 *            Character used to quote field content, e.g. a double quote
	 * @param numHeaderLines
	 *            Number of lines to skip at the start of the file
	 * @param useRelationDisplayValues
	 *            Whether the import file contains the display values of
	 *            relations or internal values
	 * @param importSequenceValues
	 *            Whether to import values into sequence fields. If true, the
	 *            values will be imported and the sequence set to restart from
	 *            the max. of all values in the table. If false, sequence fields
	 *            will be ignored and the input file should miss out the
	 *            sequence columns
	 * @param requireExactRelationValues
	 *            If true, require that values for relation fields in the import
	 *            spreadsheet exactly match display values in the database. If
	 *            false, try to find the best match
	 * @param csvContent
	 *            If uploading a file, CSV content will be taken from the file
	 *            and the csvContent string left null. If not, csv content will
	 *            be directly passed in in string form using csvContent
	 * @param trim
	 *            Trim leading and trailing whitespace from each value as it's
	 *            imported
	 * @param merge
	 *            For updating (rather than inserting). If true, merge the
	 *            spreadsheet and database data, i.e. where a spreadsheet field
	 *            is empty, leave the existing value in the database. If false,
	 *            overwrite all database data from the spreadsheet
	 * @throws CantDoThatException
	 *             if the form wasn't posted as multi-part/form data
	 * @throws InputRecordException
	 *             If there was an error with the data being imported. Details
	 *             of line number and field causing the error will be given
	 */
	public int importCSV(HttpServletRequest request, TableInfo table,
			boolean updateExistingRecords, BaseField recordIdentifierField, boolean generateRowIds,
			char separator, char quotechar, int numHeaderLines, boolean useRelationDisplayValues,
			boolean importSequenceValues, boolean requireExactRelationMatches, boolean trim,
			boolean merge, List<FileItem> multipartItems, String csvContent) throws SQLException,
			InputRecordException, IOException, CantDoThatException, ObjectNotFoundException,
			DisallowedException, CodingErrorException;

	/**
	 * @throws ObjectNotFoundException
	 *             If no rows were deleted (probably because the record
	 *             identified by rowId couldn't be found) <b>or</b> if more than
	 *             one row would be deleted
	 * @throws CodingErrorException
	 *             If there was an internal error writing to the audit table
	 */
	public void removeRecord(HttpServletRequest request, SessionDataInfo sessionData,
			DatabaseInfo datbaseDefn, TableInfo table, int rowId, boolean cascade)
			throws SQLException, ObjectNotFoundException, CodingErrorException,
			CantDoThatException, DisallowedException, DataDependencyException;

	/**
	 * A very basic global edit. Sets all field values specified in the
	 * dataToEditParameter, for all rows in the table
	 * 
	 * @param dataToEdit
	 *            A map of field to a value for each field
	 */
	public int globalEdit(HttpServletRequest request, TableInfo table,
			LinkedHashMap<BaseField, BaseValue> dataToEdit, SessionDataInfo sessionData,
			List<FileItem> multipartItems) throws InputRecordException, ObjectNotFoundException,
			SQLException, CodingErrorException, CantDoThatException, DisallowedException,
			MissingParametersException;

	/**
	 * Randomise data in a table
	 */
	public void anonymiseData(TableInfo table, HttpServletRequest request,
			SessionDataInfo sessionData, Map<BaseField, FieldContentType> fieldContentTypes,
			List<FileItem> multipartItems) throws SQLException, CodingErrorException,
			CantDoThatException, InputRecordException, ObjectNotFoundException,
			DisallowedException, MissingParametersException;

	/**
	 * Given an HTTP request, find the company owning the logged in user and
	 * update the last data change time for that company. Used as part of the
	 * caching mechanism. Should be used whenever any data change or some schema
	 * changes take place
	 */
	public void logLastDataChangeTime(HttpServletRequest request) throws ObjectNotFoundException;

	public void logLastSchemaChangeTime(HttpServletRequest request) throws ObjectNotFoundException;

	/**
	 * Return the average upload speed of files since agileBase started up, with
	 * newer uploads given a larger weighting
	 * 
	 * @return Bytes per second
	 */
	public int getUploadSpeed();

	/**
	 * Get the next or previous row ID in the given report with filters active
	 * 
	 * @param forwardSearch
	 *            Whether to get the next or previous ID
	 */
	public int getNextRowId(SessionDataInfo sessionData, BaseReportInfo report,
			boolean forwardSearch) throws SQLException, CantDoThatException;
}