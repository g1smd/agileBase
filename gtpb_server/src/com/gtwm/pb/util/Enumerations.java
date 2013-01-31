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
package com.gtwm.pb.util;

import java.util.Locale;

import org.apache.commons.lang.WordUtils;

/**
 * Enumerations used throughout the application. These are mainly lists of
 * actions that can be carried out, plus one list of reserved words
 */
public class Enumerations {

	public enum AppType {
		COMMENT_STREAM("Recent comments", true), DATA_STREAM("Data stream", true), FOCUS("Item focus", false), DATA_LINK(
				"Data link", false), FILES("Files", false), CALENDAR("Today", false), VISUALISATION("Visualisation", false), CHAT("Chat", false);

		private String appName;

		private boolean large;

		AppType(String appName, boolean large) {
			this.appName = appName;
			this.large = large;
		}

		public String getAppName() {
			return this.appName;
		}

		public boolean isLarge() {
			return this.large;
		}
	}

	/**
	 * Contains list of all actions that the user interface can call via a HTTP
	 * request to set session variables.
	 * 
	 * The POSTSET_... methods act the same as the SET_... methods except the
	 * action is delayed until others have completed
	 * 
	 * There is one PRESET_... method, PRESET_ROW_ID. Use this if you want to set
	 * the row ID of a table then change to a different table with SET_TABLE.
	 * Using SET_ROW_ID and POSTSET_TABLE would have the same effect but in some
	 * situations POSTSET_TABLE isn't desirable
	 * 
	 * @see com.gtwm.pb.model.interfaces.SessionDataInfo See the documentation in
	 *      SessionDataInfo for how to call these in a HTTP request
	 */
	public enum SessionAction {
		SET_MODULE, PRESET_ROW_ID, SET_TABLE, SET_REPORT, SET_ROW_ID, NEXT_ROW_ID, PREVIOUS_ROW_ID, SET_REPORT_ROW_LIMIT, SET_REPORT_FILTER_VALUE, CLEAR_REPORT_FILTER_VALUE, CLEAR_ALL_REPORT_FILTER_VALUES, SET_GLOBAL_REPORT_FILTER_STRING, CLEAR_ALL_REPORT_SORTS, CLEAR_REPORT_SORT, SET_REPORT_SORT, SET_USER, SET_ROLE, SET_CONTEXT, SET_CUSTOM_VARIABLE, SET_CUSTOM_STRING, SET_CUSTOM_INTEGER, SET_CUSTOM_LONG, SET_CUSTOM_BOOLEAN, SET_CUSTOM_TABLE, SET_CUSTOM_REPORT, SET_CUSTOM_FIELD, CLEAR_CUSTOM_VARIABLE, POSTSET_TABLE, POSTSET_REPORT, POSTSET_CUSTOM_TABLE, POSTSET_CUSTOM_REPORT, SET_LOCK_OVERRIDE, SET_APP_ID, CLEAR_APP_ID, LOGOUT
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
	 * All application actions that the user interface can call via a HTTP request
	 * to other than session actions
	 */
	public enum AppAction {
		ADD_USER, REMOVE_USER, UPDATE_USER, ADD_ROLE, UPDATE_ROLE, REMOVE_ROLE, ASSIGN_USER_TO_ROLE, REMOVE_USER_FROM_ROLE, ADD_PRIVILEGE, REMOVE_PRIVILEGE, SET_MAX_TABLE_PRIVILEGE, CLEAR_ALL_TABLE_PRIVILEGES, ADD_TABLE, UPDATE_TABLE, REMOVE_TABLE, ADD_FIELD, REMOVE_FIELD, UPDATE_FIELD, UPDATE_FIELD_OPTION, SET_FIELD_INDEX, ADD_REPORT, UPDATE_REPORT, REMOVE_REPORT, ADD_FIELD_TO_REPORT, REMOVE_FIELD_FROM_REPORT, SET_REPORT_FIELD_INDEX, SET_REPORT_FIELD_SORTING, ADD_CALCULATION_TO_REPORT, UPDATE_CALCULATION_IN_REPORT, ADD_FILTER_TO_REPORT, REMOVE_FILTER_FROM_REPORT, ADD_JOIN_TO_REPORT, REMOVE_JOIN_FROM_REPORT, SAVE_NEW_RECORD, UPDATE_RECORD, REMOVE_RECORD, CSV_IMPORT, CSV_UPLOAD, ADD_GROUPING_TO_CHART, REMOVE_GROUPING_FROM_CHART, ADD_FUNCTION_TO_CHART, REMOVE_FUNCTION_FROM_CHART, SET_CHART_FILTER_FIELD, SET_CHART_FILTER, SET_CHART_RANGE, SAVE_CHART, REMOVE_CHART, GLOBAL_EDIT, ADD_COMPANY, REMOVE_COMPANY, CLONE_RECORD, ANONYMISE, ADD_MODULE, UPDATE_MODULE, REMOVE_MODULE, LOCK_RECORDS, LOCK_RECORD, SET_DASHBOARD_CHART_STATE, HIDE_REPORT, UNHIDE_REPORT, SET_USER_DEFAULT_REPORT, SET_CALENDAR_SYNCABLE, ADD_OPERATIONAL_DASHBOARD_REPORT, REMOVE_OPERATIONAL_DASHBOARD_REPORT, ADD_FORM_TABLE, REMOVE_FORM_TABLE, SET_TABLE_FORM, SET_WORD_CLOUD_FIELD, ENABLE_DISABLE_APP, UPLOAD_CUSTOM_TEMPLATE, REMOVE_CUSTOM_TEMPLATE, ADD_COMMENT, UPDATE_MAP, ADD_FORM_TAB, REMOVE_FORM_TAB, UPDATE_FORM_TAB, ADD_REPORT_DISTINCT, REMOVE_REPORT_DISTINCT, SEND_PASSWORD_RESET, UPLOAD_PROFILE_PICTURE, ADD_APP, REMOVE_APP;
	}

	/**
	 * Actions potentially available to un-authenticated users
	 */
	public enum PublicAction {
		SHOW_FORM, SAVE_NEW_RECORD, UPDATE_RECORD, GET_REPORT_JSON, GET_REPORT_RSS, SEND_PASSWORD_RESET;
	}

	/*
	 * Any additional actions that don't affect the server state
	 * 
	 * INCLUDE_TOOLBAR_PLUGIN=pluginname:<br> Include template
	 * gui/plugins/[pluginname]/toolbar.vm in the toolbar
	 */
	public enum ExtraAction {
		INCLUDE_TOOLBAR_PLUGIN
	}

	/**
	 * Postgresql specific reserved names
	 * 
	 * TODO: upgrade to latest postgres version
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
	 * A list of any reserved words used by the application in a HTTP request, in
	 * addition to those in AppAction and SessionAction. We don't want to name our
	 * fields etc. the same as one of these otherwise the application could get
	 * confused
	 */
	public enum RequestReservedNames {
		INTERNALTABLENAME, INTERNALREPORTNAME, INTERNALFIELDNAME, FIELDVALUE, RETURNTYPE, RETURN
	}

	/**
	 * Filter types which can be used in a report
	 */
	public enum FilterType {
		EQUAL(false), NOT_EQUAL_TO(false), GREATER_THAN_OR_EQUAL_TO(false), LESS_THAN(false), STARTS_WITH(
				false), DOES_NOT_START_WITH(false), NEWER_THAN_IN_DAYS(true), NEWER_THAN_IN_WEEKS(true), NEWER_THAN_IN_MONTHS(
				true), NEWER_THAN_IN_YEARS(true), OLDER_THAN_IN_DAYS(true), OLDER_THAN_IN_WEEKS(true), OLDER_THAN_IN_MONTHS(
				true), OLDER_THAN_IN_YEARS(true), IS_NULL(false), IS_NOT_NULL(false), IS_ONE_OF(false); // ,
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
		 * Returns a plain English description of the filter type for display to the
		 * user
		 */
		public String getDescription() {
			return this.toString().replace("_", " ").toLowerCase(Locale.UK);
		}

		/**
		 * Returns the value of the 'filtertype' parameter that must be submitted to
		 * the server to create a filter of this type
		 */
		public String getFilterTypeParameter() {
			return this.toString().toLowerCase(Locale.UK);
		}
	}

	public enum QuickFilterType {
		LIKE("LIKE", ""), NOT_LIKE("NOT LIKE", "!"), EQUAL("=", "="), GREATER_THAN(">=", ">"), LESS_THAN(
				"<", "<"), EMPTY("IS NULL OR gtpb_field_placeholder = ", "?"), OR(" OR ", " OR "), AND(
				" AND ", " AND ");

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
	 * A list of aggregate functions that can be used in a chart. Not all
	 * database-provided functions are listed, only those we'll use. Each function
	 * has an associated plain English description for use e.g when charting
	 */
	public enum AggregateFunction {
		COUNT("count"), SUM("sum"), MIN("minimum"), MAX("maximum"), AVG("average"), WTDAVG(
				"weighted avg."), CUMULATIVE_COUNT("cumulative count"), CUMULATIVE_SUM("cumulative sum");

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
		HTML("text/html"), JSON("application/javascript"), XML("application/xml"), DOWNLOAD(
				"application/octet-stream");

		private String responseType;

		ResponseReturnType(String responseType) {
			this.responseType = responseType;
		}

		/**
		 * Return the actual response type that needs to be set in the header with
		 * HttpServletResponse.setContentType(...)
		 * 
		 * @see HttpServletResponse#setContentType(String)
		 */
		public String getResponseType() {
			return this.responseType;
		}
	}

	/**
	 * For used when randomising data in a table, to tell which type of content is
	 * in each field
	 */
	public enum FieldContentType {
		COMPANY_NAME, FULL_NAME, PHONE_NUMBER, EMAIL_ADDRESS, NI_NUMBER, CODE, NOTES, OTHER
	}

	public enum HiddenFields {
		WIKI_PAGE("Wiki page [Auto]", "Related wiki page"), DATE_CREATED("Creation time [Auto]",
				"Date & time of record creation"), CREATED_BY("Created by [Auto]",
				"User who created the record"), LAST_MODIFIED("Last modified [Auto]",
				"Date & time of last change to record"), MODIFIED_BY("Modified by [Auto]",
				"User who made the last change"), LOCKED("Locked [Auto]",
				"Whether record is locked for editing"), VIEW_COUNT("View count [Auto]",
				"Number of times the record has been viewed"), COMMENTS_FEED("Comments feed [Auto]",
				"Conglomoration of all comments on the record");

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
		MSIE("Internet Explorer", "msie", false), FIREFOX("Firefox", "firefox", false), MINEFIELD(
				"Firefox development version", "minefield", false), CAMINO("Camino", "camino", false), SYMBIAN_MOBILE(
				"Safari on Symbian mobile", "symbian", true), CHROME("Google Chrome", "chrome", false), SAFARI(
				"Safari", "applewebkit", false), OPERA("Opera", "opera", false), IPOD("iPod", "ipod", true), IPHONE(
				"iPhone", "iphone", true), IPAD("iPad", "ipad", true), KONQUEROR("Konqueror", "konqueror",
				false), UNKNOWN("unknown", "gtpb_unknown_browser", false);

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

		public boolean isMobile() {
			return this.mobile;
		}

		public String toString() {
			return this.browserName;
		}

		private String browserName = "";

		private String userAgentString = "";

		private boolean mobile = false;

		/**
		 * @param browserName
		 *          User friendly browser name
		 * @param userAgentString
		 *          Lowercase version of user agent component that identifies the
		 *          browser
		 */
		Browsers(String browserName, String userAgentString, boolean mobile) {
			this.browserName = browserName;
			this.userAgentString = userAgentString;
			this.mobile = mobile;
		}
	}

	/**
	 * In a chart you can group by any field in the report, but the addition of
	 * field modifiers allows you to do things like group by year, quarter or
	 * month of a date field
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

	public enum Period {
		DAY, WEEK, MONTH, YEAR;
	}

	/**
	 * Initial display a user sees
	 */
	public enum InitialView {
		LIMITED("for external users outside the company"), REPORT("full height report view"), FULL(
				"full three pane interface"), EXECUTIVE_DASHBOARD("executive dashboard");

		public String getDescription() {
			return this.description;
		}

		private String description = "";

		InitialView(String description) {
			this.description = description;
		}
	}

	public enum SummaryFilter {
		LAST_30_DAYS("Last 30 days", "{fieldvalue} > (now() - '30 days'::interval)"), THIS_YEAR(
				"This calendar year",
				"{fieldvalue} >= (date_trunc('year',now()) - '@0 months'::interval) AND {fieldvalue} < (date_trunc('year',now()) - '@0 months'::interval)"), YEAR_ON_YEAR(
				"Last month year on year",
				"date_part('month', {fieldvalue}) = date_part('month', now() - '1 month'::interval)"), LAST_90_DAYS(
				"Last 90 days", "{fieldvalue} >= (now() - '90 days'::interval) AND {fieldvalue} < now()");

		public String getDescription() {
			return this.description;
		}

		public String getSQL() {
			return this.sql;
		}

		private String sql = "";

		private String description = "";

		SummaryFilter(String description, String sql) {
			this.description = description;
			this.sql = sql;
		}
	}

	public enum TextCase {
		ANY(""), LOWER("lower"), UPPER("upper"), TITLE("initcap");

		public String getSqlRepresentation() {
			return this.sqlRepresentation;
		}

		/**
		 * Transform the given text into the case that this TextCase represents
		 */
		public String transform(String text) {
			switch (this) {
			case ANY:
				return text;
			case LOWER:
				return text.toLowerCase();
			case UPPER:
				return text.toUpperCase();
			case TITLE:
				return WordUtils.capitalizeFully(text);
			default:
				return text;
			}
		}

		TextCase(String sqlRepresentation) {
			this.sqlRepresentation = sqlRepresentation;
		}

		private String sqlRepresentation = "";
	}

	/**
	 * Used to flag whether we've tried overriding the default plan for a database
	 * query
	 */
	public enum QueryPlanSelection {
		DEFAULT, TRY_NO_NESTED_LOOPS, NO_NESTED_LOOPS, ALTERNATIVE_NOT_FASTER;
	}

	/**
	 * Formats for exporting public data
	 */
	public enum DataFormat {
		RSS, JSON, JSON_FULLCALENDAR, JSON_TIMELINE;
	}

	public enum AttachmentType {
		DOCUMENT("Document"), IMAGE("Image/photo"), PROFILE_PHOTO("Photo of a person");

		public String getDescription() {
			return this.description;
		}

		AttachmentType(String description) {
			this.description = description;
		}

		private String description = "";
	}

	public enum ReportStyle {
		SPREADSHEET("Standard spreadsheet-like rows"), SECTIONED(
				"Data broken up into sections with headings"), ONE_SECTION("One heading then detail");

		public String getDescription() {
			return this.description;
		}

		ReportStyle(String description) {
			this.description = description;
		}

		private String description = "";
	}

	public enum FormStyle {
		SINGLE_COLUMN, TWO_COLUMNS, TWO_COLUMNS_WITHIN_SECTION;
	}
}