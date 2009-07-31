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
package com.gtwm.pb.util;

import java.util.Locale;

/**
 * Enumerations used throughout the application. These are mainly lists of
 * actions that can be carried out, plus one list of reserved words
 */
public class Enumerations {

	/**
	 * Contains list of all actions that the user interface can call via a HTTP
	 * request to set session variables.
	 * 
	 * The POSTSET_... methods act the same as the SET_... methods except the
	 * action is delayed
	 * 
	 * There is one PRESET_... method, PRESET_ROW_ID. Use this if you want to
	 * set the row ID of a table then change to a different table with
	 * SET_TABLE. Using SET_ROW_ID and POSTSET_TABLE would have the same effect
	 * but in some situations POSTSET_TABLE isn't desirable
	 * 
	 * @see com.gtwm.pb.model.interfaces.SessionDataInfo See the documentation
	 *      in SessionDataInfo for how to call these in a HTTP request
	 */
	public enum SessionAction {
		SET_MODULE, PRESET_ROW_ID, SET_REPORT, SET_TABLE, SET_ROW_ID, SET_REPORT_ROW_LIMIT, SET_REPORT_FILTER_VALUE, CLEAR_REPORT_FILTER_VALUE, CLEAR_ALL_REPORT_FILTER_VALUES, CLEAR_ALL_REPORT_SORTS, CLEAR_REPORT_SORT, SET_REPORT_SORT, SET_USER, SET_ROLE, SET_CONTEXT, SET_CUSTOM_VARIABLE, SET_CUSTOM_STRING, SET_CUSTOM_INTEGER, SET_CUSTOM_BOOLEAN, SET_CUSTOM_TABLE, SET_CUSTOM_REPORT, SET_CUSTOM_FIELD, CLEAR_CUSTOM_VARIABLE, POSTSET_REPORT, POSTSET_TABLE, POSTSET_CUSTOM_TABLE, POSTSET_CUSTOM_REPORT, SET_LOCK_OVERRIDE, LOGOUT
	}

	/**
	 * Contains a list of contexts that the UI should be placed in.<br/>
	 * BUSINESS (default) - Cut-down, cleaner interface for day-to-day use<br/>
	 * SYSADMIN - More informative interface which would be too much clutter for
	 * day-to-day use
	 */
	public enum SessionContext {
		BUSINESS, SYSADMIN
	}

	/**
	 * All application actions that the user interface can call via a HTTP
	 * request to other than session actions
	 */
	public enum AppAction {
		ADD_USER, REMOVE_USER, UPDATE_USER, ADD_ROLE, UPDATE_ROLE, REMOVE_ROLE, ASSIGN_USER_TO_ROLE, REMOVE_USER_FROM_ROLE, ADD_PRIVILEGE, REMOVE_PRIVILEGE, SET_MAX_TABLE_PRIVILEGE, CLEAR_ALL_TABLE_PRIVILEGES, ADD_TABLE, UPDATE_TABLE, REMOVE_TABLE, ADD_FIELD, REMOVE_FIELD, UPDATE_FIELD, UPDATE_FIELD_OPTION, SET_FIELD_INDEX, ADD_REPORT, UPDATE_REPORT, REMOVE_REPORT, ADD_FIELD_TO_REPORT, REMOVE_FIELD_FROM_REPORT, SET_REPORT_FIELD_INDEX, SET_REPORT_FIELD_SORTING, ADD_CALCULATION_TO_REPORT, UPDATE_CALCULATION_IN_REPORT, ADD_FILTER_TO_REPORT, REMOVE_FILTER_FROM_REPORT, ADD_JOIN_TO_REPORT, REMOVE_JOIN_FROM_REPORT, SAVE_NEW_RECORD, UPDATE_RECORD, REMOVE_RECORD, CSV_IMPORT, CSV_UPLOAD, ADD_GROUPING_TO_SUMMARY_REPORT, REMOVE_GROUPING_FROM_SUMMARY_REPORT, ADD_FUNCTION_TO_SUMMARY_REPORT, REMOVE_FUNCTION_FROM_SUMMARY_REPORT, GLOBAL_EDIT, ADD_COMPANY, REMOVE_COMPANY, ADD_TAB_ADDRESS, REMOVE_TAB_ADDRESS, CLONE_RECORD, ANONYMISE, ADD_MODULE, UPDATE_MODULE, REMOVE_MODULE, LOCK_RECORDS, LOCK_RECORD
	}

	/**
	 * Any additional actions that don't affect the server state
	 * 
	 * INCLUDE_TOOLBAR_PLUGIN=pluginname:<br>
	 * Include template gui/plugins/[pluginname]/toolbar.vm in the toolbar
	 */
	public enum ExtraAction {
		INCLUDE_TOOLBAR_PLUGIN
	}

	/**
	 * Postgresql 7.4 specific reserved names TODO: upgrade to latest postgres
	 * version
	 * 
	 * @see http
	 *      ://www.postgresql.org/docs/7.4/interactive/sql-keywords-appendix.
	 *      html#KEYWORDS-TABLE
	 */
	public enum DatabaseReservedNames {
		ALL, ANALYSE, ANALYZE, AND, ANY, ARRAY, AS, ASC, AUTHORIZATION, BETWEEN, BINARY, BOTH, CASE, CAST, CHECK, COLLATE, COLUMN, CONSTRAINT, CREATE, CROSS, DEFAULT, DEFERRABLE, DESC, DISTINCT, DO, ELSE, END, EXCEPT, FALSE, FOR, FOREIGN, FREEZE, FROM, FULL, GRANT, GROUP, HAVING, ILIKE, IN, INITIALLLY, INNER, INTERSECT, INTO, IS, ISNULL, JOIN, LEADING, LEFT, LIMIT, LOCALTIME, LOCALTIMESTAMP, NATURAL, NEW, NOT, NOTHING, NOTIFY, NOTNULL, OFF, OFFSET, OLD, ON, ONLY, OR, ORDER, OUTER, OVERLAPS, PLACING, PRIMARY, REFERENCES, RIGHT, SELECT, SIMILAR, SUM, TABLE, TO, THEN, TRAILING, TRUE, UNION, UNIQUE, USER, USING, WHEN, WHERE
	}

	/**
	 * A list of all field types used in the database
	 */
	public enum DatabaseFieldType {
		VARCHAR("text"), TIMESTAMP("date/time"), FLOAT("decimal number"), INTERVAL("time interval"), INTEGER(
				"whole number"), SERIAL("Autogenerated ID"), BOOLEAN("True/false value");

		DatabaseFieldType(String description) {
			this.description = description;
		}

		public String getDescription() {
			return this.description;
		}

		private String description = null;
	}

	/**
	 * A list of options for 'ON UPDATE' and 'ON DELETE' actions of foreign key
	 * constraints
	 */
	public enum ForeignKeyConstraint {
		RESTRICT, CASCADE, NO_ACTION, SET_NULL, SET_DEFAULT;

		public String toString() {
			return super.toString().replaceAll("_", " ");
		}
	}

	/**
	 * OBSOLETE?
	 * 
	 * A list of any reserved words used by the application in a HTTP request,
	 * in addition to those in AppAction and SessionAction. We don't want to
	 * name our fields etc. the same as one of these otherwise the application
	 * could get confused
	 */
	public enum RequestReservedNames {
		INTERNALTABLENAME, INTERNALREPORTNAME, INTERNALFIELDNAME, FIELDVALUE, RETURNTYPE, RETURN
	}

	/**
	 * Filter types which can be used in a report
	 */
	public enum FilterType {
		EQUAL(false), NOT_EQUAL_TO(false), GREATER_THAN_OR_EQUAL_TO(false), LESS_THAN(false), STARTS_WITH(
				false), DOES_NOT_START_WITH(false), NEWER_THAN_IN_DAYS(true), NEWER_THAN_IN_WEEKS(
				true), NEWER_THAN_IN_MONTHS(true), NEWER_THAN_IN_YEARS(true), OLDER_THAN_IN_DAYS(
				true), OLDER_THAN_IN_WEEKS(true), OLDER_THAN_IN_MONTHS(true), OLDER_THAN_IN_YEARS(
				true), IS_NULL(false), IS_NOT_NULL(false), IS_ONE_OF(false); // ,
																				// IS_IN_SUBSELECT(false),
		// IS_NOT_IN_SUBSELECT(false);

		private boolean isDateComparison = false;

		private FilterType(boolean isDateComparison) {
			this.isDateComparison = isDateComparison;
		}

		/**
		 * Tells you whether the filter is a date-only comparison such as
		 * NEWER_THAN_IN_MONTHS or a normal comparison such as LESS_THAN
		 */
		public boolean isDateComparison() {
			return this.isDateComparison;
		}

		/**
		 * Returns a plain English description of the filter type for display to
		 * the user
		 */
		public String getDescription() {
			return this.toString().replace("_", " ").toLowerCase(Locale.UK);
		}

		/**
		 * Returns the value of the 'filtertype' parameter that must be
		 * submitted to the server to create a filter of this type
		 */
		public String getFilterTypeParameter() {
			return this.toString().toLowerCase(Locale.UK);
		}
	}

	public enum QuickFilterType {
		LIKE("LIKE", ""), NOT_LIKE("NOT LIKE", "!"), GREATER_THAN(">=", ">"), LESS_THAN("<", "<"), EMPTY(
				"IS NULL OR gtpb_field_placeholder = ", "?"), OR(" OR ", " OR "), AND(" AND ",
				" AND ");

		private String sqlRepresentation;

		private String userRepresentation;

		QuickFilterType(String sqlRepresentation, String userRepresentation) {
			this.sqlRepresentation = sqlRepresentation;
			this.userRepresentation = userRepresentation;
		}

		public String getSqlRepresentation() {
			return this.sqlRepresentation;
		}

		public String getUserRepresentation() {
			return this.userRepresentation;
		}
	}

	/**
	 * A list of aggregate functions that can be used in a report summary. Not
	 * all database-provided functions are listed, only those we'll use. Each
	 * function has an associated plain English description for use e.g when
	 * charting
	 */
	public enum AggregateFunction {
		COUNT("Count"), SUM("Sum"), MIN("Minimum"), MAX("Maximum"), AVG("Average"), WTDAVG(
				"Weighted Avg.");

		private String label;

		AggregateFunction(String label) {
			this.label = label;
		}

		public String getLabel() {
			return this.label;
		}
	}

	/**
	 * The HTML response return type so the browser knows whether to treat the
	 * server response as a web page, XML file or download for example
	 */
	public enum ResponseReturnType {
		HTML("text/html"), XML("text/xml"), DOWNLOAD("application/octet-stream");

		private String responseType;

		ResponseReturnType(String responseType) {
			this.responseType = responseType;
		}

		/**
		 * Return the actual response type that needs to be set in the header
		 * with HttpServletResponse.setContentType(...)
		 * 
		 * @see HttpServletResponse#setContentType(String)
		 */
		public String getResponseType() {
			return this.responseType;
		}
	}

	/**
	 * For used when randomising data in a table, to tell which type of content
	 * is in each field
	 */
	public enum FieldContentType {
		FULL_NAME, PHONE_NUMBER, EMAIL_ADDRESS, NI_NUMBER, OTHER
	}

	public enum HiddenFields {
		WIKI_PAGE("Wiki page [Auto]", "Related wiki page"), DATE_CREATED("Creation time [Auto]",
				"Date & time of record creation"), CREATED_BY("Created by [Auto]",
				"User who created the record"), LAST_MODIFIED("Last modified [Auto]",
				"Date & time of last change to record"), MODIFIED_BY("Modified by [Auto]",
				"User who made the last change"), LOCKED("Locked [Auto]",
				"Whether record is locked for editing");

		private String fieldName;

		private String fieldDescription;

		HiddenFields(String _fieldName, String _fieldDescription) {
			this.fieldName = _fieldName;
			this.fieldDescription = _fieldDescription;
		}

		public String getFieldName() {
			return this.fieldName;
		}

		public String getFieldDescription() {
			return this.fieldDescription;
		}
	}

	public enum Browsers {
		MSIE("Internet Explorer", "msie"), FIREFOX("Firefox", "firefox"), MINEFIELD(
				"Firefox development version", "minefield"), CAMINO("Camino", "camino"), SYMBIAN_MOBILE(
				"Safari on Symbian mobile", "symbian"), SAFARI("Safari", "applewebkit"), OPERA(
				"Opera", "opera"), IPOD("iPod", "ipod"), IPHONE("iPhone", "iphone"),
		// So we can treat the ipod and iphone as one. They have similar or the
		// same rendering engines
		APPLE_MOBILE("iPod/iPhone", "gtpb_ipod_or_iphone"), UNKNOWN("unknown",
				"gtpb_unknown_browser");

		/**
		 * Return the human digestible form of the browser name
		 */
		public String getBrowserName() {
			return this.browserName;
		}

		/**
		 * Return a string that identifies this browser by it's presence in the
		 * user-agent string. Lowercase
		 */
		public String getUserAgentString() {
			return this.userAgentString;
		}

		public String toString() {
			return this.browserName;
		}

		private String browserName = "";

		private String userAgentString = "";

		Browsers(String browserName, String userAgentString) {
			this.browserName = browserName;
			this.userAgentString = userAgentString;
		}
	}
	
	/**
	 * In a report summary you can group by any field in the report, but the addition of field modifiers allows you to do things like group by year, quarter or month of a date field
	 */
	public enum SummaryGroupingModifier {
		DATE_YEAR("year"), DATE_QUARTER("quarter"), DATE_MONTH("month"), DATE_DAY("day");
		
		public String getDescription() {
			return this.description;
		}
		
		private String description = "";
		
		SummaryGroupingModifier(String description) {
			this.description = description;
		}
	}

}