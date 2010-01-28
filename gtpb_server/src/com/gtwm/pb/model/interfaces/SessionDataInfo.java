/*
 *  Copyright 2009 GT webMarque Ltd
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
import com.gtwm.pb.model.interfaces.fields.BaseValue;
import com.gtwm.pb.util.TableDependencyException;
import com.gtwm.pb.util.Enumerations.SessionContext;
import com.gtwm.pb.util.Enumerations.AppAction;
import java.sql.SQLException;
import java.util.Map;

/**
 * Stores all session specific data when a client is using the web application.
 * When data is sent from the UI to the server, parameters passed such as table
 * identifiers, row ids etc. will be stored in the session via this object.
 * 
 * In general, any user-specific data that the user interface view methods need
 * to access should be stored here.
 */
public interface SessionDataInfo {
	
	/**
	 * Return the server-changing last action that the user took
	 */
	public AppAction getLastAppAction();
	
	public void setLastAppAction(AppAction appAction);
	
	/**
	 * Return the row id that the last action acted on (if it was relevant to an individual record)
	 */
	public int getLastAppActionRowId();
	
	public void setLastAppActionRowId(int lastAppActionRowId);

	/**
	 * Delete a custom session variable, or take no action if it doesn't exist.
	 * Variables of any type are cleared, i.e. anything that's been set with
	 * setCustomString, setCustomTable, setCustomReport etc.
	 */
	public void clearCustomVariable(String key);

	/**
	 * Un-filter a particular field. NOTE: This is not the same as setting the
	 * filter value to null or an empty string.
	 * 
	 * Use CLEAR_REPORT_FILTER_VALUE in the HTTP request to clear a field
	 * filter, supplying internalFieldName=<i>identifier of the field to
	 * un-filter</i>
	 * 
	 * @param field
	 */
	public void clearReportFilterValue(BaseField field);

	public void clearReportSort(BaseField field);

	/**
	 * Return the value associated with the given key
	 * 
	 * @deprecated Use getCustomString instead
	 */
	public String getCustomVariable(String key);

	public String getCustomString(String key);

	public Integer getCustomInteger(String key);

	public Boolean getCustomBoolean(String key);

	public TableInfo getCustomTable(String key);

	public BaseReportInfo getCustomReport(String key);

	public BaseField getCustomField(String key);
	
	public Map<BaseField, String> getCustomReportFilterValues(String filterSet);

	/**
	 * @return A read-only copy of the session's field input values
	 */
	public Map<BaseField, BaseValue> getFieldInputValues();

	public BaseReportInfo getReport();

	/**
	 * @return A read-only copy of the session's report filter values
	 */
	public Map<BaseField, String> getReportFilterValues();

	public Map<BaseField, Boolean> getReportSorts();

	public int getReportRowLimit();

	/**
	 * @return The current role being edited by an administrator
	 */
	public AppRoleInfo getRole();

	/**
	 * @return The current UI context
	 */
	public SessionContext getContext();

	public int getRowId();

	/**
	 * Return the id of the selected record for the given table, or -1 if
	 * nothing is selected
	 */
	public int getRowId(TableInfo table);

	public TableInfo getTable();

	public TableDependencyException getTableDependencyException();

	public void setTableDependencyException(TableDependencyException tdex);

	/**
	 * @return The user currently being edited by an administrator (not the
	 *         currently logged in user)
	 */
	public AppUserInfo getUser();

	public void setModule(ModuleInfo module);

	public ModuleInfo getModule();

	/**
	 * See whether or not the session contains a table. Note - if it contains a
	 * table, then it contains a report as well and vice versa
	 */
	public boolean hasTable();

	/**
	 * Allow the UI to set a custom session variable, identified by a key
	 * 
	 * @deprecated Use setCustomString instead
	 */
	public void setCustomVariable(String key, String value);

	public void setCustomString(String key, String value);

	public void setCustomInteger(String key, Integer value);

	public void setCustomBoolean(String key, Boolean value);

	public void setCustomTable(String key, TableInfo value);

	public void setCustomReport(String key, BaseReportInfo value);

	public void setCustomField(String key, BaseField value);

	/**
	 * Save the values that the user typed into an input form, in case the form
	 * needs to be re-displayed.
	 * 
	 * This method will extract the values relating to fields in the current
	 * report as set by setReport().
	 * 
	 * It's used internally by DataManagement.saveRecord, don't call from the
	 * user interface
	 * 
	 * @param parameterMap
	 *            Map of field to field value.
	 */
	public void setFieldInputValues(Map<BaseField, BaseValue> parameterMap);

	/**
	 * Set the report the client is currently using. Also set the current table
	 * to the report's parent table.
	 * 
	 * Use SET_REPORT=internalReportName in the HTTP request to set the report
	 * 
	 * @throws SQLException
	 *             If there was an error auto-setting the row ID
	 */
	public void setReport(BaseReportInfo report) throws SQLException;

	/**
	 * Set a filter on a particular field.
	 * 
	 * Use SET_REPORT_FILTER_VALUE in the HTTP request to set a filter,
	 * supplying internalFieldName=<i>identifier of the field</i>&fieldValue=<i>filter
	 * string</i>
	 * 
	 * @param field
	 * @param fieldValue
	 *            String representation of the value to set. If null or an empty
	 *            string, then clear the report filter value instead
	 * @see #clearReportFilterValue(BaseField)
	 */
	public void setReportFilterValue(BaseField field, String fieldValue);
	
	public void setCustomReportFilterValue(String filterSet, BaseField field, String fieldValue);

	public void setReportSort(BaseField reportField, Boolean sortAscending);

	/**
	 * Remove all filters set by setReportFilterValues(). Filters are per field,
	 * not per report - all filters will be removed, not just those for the
	 * current report
	 */
	public void clearAllReportFilterValues();

	public void clearAllReportSorts();

	/**
	 * Use SET_REPORT_ROW_LIMIT=<i>rowLimit</i> in the HTTP request to set the
	 * row limit.
	 * 
	 * The row limit in the session is global, not per report, so if a user
	 * requests that they see 1000 rows, they'll see 1000 rows for all reports
	 * they view.
	 * 
	 * agileBase will interpret a value of less than 1 as no limit
	 * 
	 * @param rowLimit
	 *            The maximum no. rows of a report that should be displayed
	 */
	public void setReportRowLimit(int rowLimit);

	/**
	 * Set the role currently being edited by an administrator. Use
	 * SET_ROLE=roleName in the HTTP request.
	 */
	public void setRole(AppRoleInfo role);

	/**
	 * Set the UI context
	 */
	public void setContext(SessionContext sessionContext);

	/**
	 * Set the record identifier for the current table. The record ID for each
	 * table is remembered and can be different. Set to -1 to clear the row Id
	 * 
	 * Use SET_ROW_ID=<the row ID> in the HTTP request to set the database
	 * record row
	 * 
	 * @param rowId
	 *            The database row ID of the current record
	 */
	public void setRowId(int rowId);

	/**
	 * Similar to setRowId(int rowId) but specify table rather than use the
	 * session table
	 */
	public void setRowId(TableInfo table, int rowId);

	/**
	 * Set the table the client is currently using. If this is the first time
	 * that this table has been set as the current one, then also set the
	 * current report to the default report of that table.
	 * 
	 * Use SET_TABLE=internalTableName in the HTTP request to set the table.
	 * 
	 * @throws SQLException
	 *             If there was an error finding the initial row ID to set for
	 *             the table
	 */
	public void setTable(TableInfo table) throws SQLException;

	/**
	 * Set the user currently being edited by an administrator. Use
	 * SET_USER=userName in the HTTP request.
	 */
	public void setUser(AppUserInfo appUser);

}