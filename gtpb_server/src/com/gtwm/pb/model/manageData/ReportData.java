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
package com.gtwm.pb.model.manageData;

import com.gtwm.pb.model.interfaces.AppUserInfo;
import com.gtwm.pb.model.interfaces.DataRowFieldInfo;
import com.gtwm.pb.model.interfaces.ReportMapInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.ReportDataInfo;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.ReportQuickFilterInfo;
import com.gtwm.pb.model.interfaces.ReportFieldInfo;
import com.gtwm.pb.model.interfaces.ReportCalcFieldInfo;
import com.gtwm.pb.model.interfaces.DataRowInfo;
import com.gtwm.pb.model.interfaces.ReportDataFieldStatsInfo;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.fields.TextField;
import com.gtwm.pb.model.interfaces.fields.RelationField;
import com.gtwm.pb.model.interfaces.fields.DecimalField;
import com.gtwm.pb.model.interfaces.fields.IntegerField;
import com.gtwm.pb.model.interfaces.fields.SequenceField;
import com.gtwm.pb.model.interfaces.fields.DateField;
import com.gtwm.pb.model.interfaces.fields.CalculationField;
import com.gtwm.pb.model.manageData.ReportDataFieldStats;
import com.gtwm.pb.model.manageData.ReportQuickFilter;
import com.gtwm.pb.util.Enumerations.HiddenFields;
import com.gtwm.pb.util.Enumerations.QueryPlanSelection;
import com.gtwm.pb.util.Helpers;
import com.gtwm.pb.util.Enumerations.DatabaseFieldType;
import com.gtwm.pb.util.Enumerations.QuickFilterType;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.AppProperties;
import com.gtwm.pb.util.ObjectNotFoundException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Statement;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import org.grlea.log.SimpleLogger;
import er.chronic.Chronic;
import er.chronic.tags.Pointer;
import er.chronic.utils.Span;
import er.chronic.Options;

public class ReportData implements ReportDataInfo {

	private ReportData() {
		this.report = null;
	}

	/**
	 * Cache information about any number fields to be used when colouring the
	 * output. Numbers are coloured according to the number of std. dev.s away
	 * from the mean, to give an idea of comparative data values at a glance.
	 * Values are cached because mean and standard deviation calculations are
	 * expensive. When the cache gets too old, the object can be deleted to a)
	 * force replenishment and b) save memory.
	 * 
	 * @param report
	 *            The report which this object produces data for
	 * @param generateFieldStats
	 *            Whether to find the std. dev and mean for colourable fields.
	 *            Use false if this object isn't going to be used for field
	 *            colouring, to save potentially slow SQL queries. If
	 *            generateFieldStats is false, conn can be null
	 * @param useSample
	 *            If generating field stats, whether to use a sample or the
	 *            whole population of rows to gather them from. A sample is a
	 *            bit faster but should only be used if you know there are
	 *            likely to be lots of rows in the report
	 */
	public ReportData(Connection conn, BaseReportInfo report, boolean generateFieldStats,
			boolean useSample) throws SQLException {
		this.report = report;
		if (generateFieldStats) {
			// Time how long it takes to generate the stats, this will tell us
			// how long it's worth caching them for
			long startTime = System.currentTimeMillis();
			StringBuilder SQLPartBuilder = new StringBuilder("count(*), ");
			// Check if any fields are colourable (numeric) and make a list of
			// them if so
			List<ReportFieldInfo> colourableFields = new LinkedList<ReportFieldInfo>();
			for (ReportFieldInfo reportField : report.getReportFields()) {
				BaseField field = reportField.getBaseField();
				// If a relation, we have to dig a little deeper to get the
				// field type
				if (field instanceof RelationField) {
					field = ((RelationField) field).getRelatedField();
				}
				if (field.getDbType().equals(DatabaseFieldType.INTEGER)
						|| field.getDbType().equals(DatabaseFieldType.FLOAT)) {
					colourableFields.add(reportField);
					String internalFieldname = reportField.getInternalFieldName();
					SQLPartBuilder.append("avg(" + internalFieldname + "), stddev("
							+ internalFieldname + "), ");
				} else if (field.getDbType().equals(DatabaseFieldType.TIMESTAMP)) {
					colourableFields.add(reportField);
					String internalFieldName = reportField.getInternalFieldName();
					SQLPartBuilder.append("avg(extract(epoch from " + internalFieldName + ")), ");
					SQLPartBuilder
							.append("stddev(extract(epoch from " + internalFieldName + ")), ");
				}
			}
			String SQLPart = SQLPartBuilder.toString();
			SQLPart = SQLPart.substring(0, SQLPart.length() - 2);
			String SQLCode = "SELECT " + SQLPart + " FROM " + report.getInternalReportName();
			if (useSample) {
				// Take a random sample of about 10% of rows to speed the
				// query. NB Don't use random() in SQL as it's very slow for a
				// large no. rows
				// SQLCode += " WHERE random() > 0.9 ORDER BY random()";
				String pKeyInternalName = report.getParentTable().getPrimaryKey()
						.getInternalFieldName();
				int randomNumber = (new Random()).nextInt(10);
				SQLCode += " WHERE " + pKeyInternalName + " % 10 = " + randomNumber;
			}
			try {
				ReportData.enableOptimisations(conn, report, true);
				PreparedStatement statement = conn.prepareStatement(SQLCode);
				ResultSet results = statement.executeQuery();
				// Save average and mean of each colourable report field to
				// cache
				if (results.next()) {
					int rowCountEstimate = results.getInt(1);
					if (useSample) {
						rowCountEstimate = rowCountEstimate * 10;
						// round it
						int roundingFactor = rowCountEstimate / 100;
						if (roundingFactor > 0) {
							rowCountEstimate = rowCountEstimate / roundingFactor;
							rowCountEstimate = rowCountEstimate * roundingFactor;
						}
					}
					this.report.setRowCount(rowCountEstimate);
					this.report.setRowCountEstimate(useSample);
					int fieldNum = 0;
					for (ReportFieldInfo reportField : colourableFields) {
						fieldNum++;
						ReportDataFieldStatsInfo fieldStats = new ReportDataFieldStats(
								results.getDouble(fieldNum * 2),
								results.getDouble((fieldNum * 2) + 1));
						this.cachedFieldStats.put(reportField, fieldStats);
					}
				}
				results.close();
				statement.close();
				ReportData.enableOptimisations(conn, report, false);
			} catch (SQLException sqlex) {
				logger.error("Error calculating field statistics for report " + report
						+ " in module " + report.getModule() + " from table "
						+ report.getParentTable() + ": " + sqlex);
			}
			this.millisecsTakenToGenerateStats = System.currentTimeMillis() - startTime;
			float durationSecs = this.millisecsTakenToGenerateStats / ((float) 1000);
			if (durationSecs > (AppProperties.longSqlTime * 2)) {
				logger.debug("Long SELECT SQL execution time of " + durationSecs + " seconds for "
						+ this.report + " statistics, statement = " + SQLCode);
			}
		}
	}

	private static QuickFilterType getFilterTypeFromFilterValueString(String filterValue) {
		// perform filter type specific filter value cleansing:
		QuickFilterType filterType = QuickFilterType.LIKE; // LIKE is default
		// case
		if (filterValue.startsWith(QuickFilterType.NOT_LIKE.getUserRepresentation())) {
			filterType = QuickFilterType.NOT_LIKE;
		} else if (filterValue.startsWith(QuickFilterType.EQUAL.getUserRepresentation())) {
			filterType = QuickFilterType.EQUAL;
		} else if (filterValue.startsWith(QuickFilterType.GREATER_THAN.getUserRepresentation())) {
			filterType = QuickFilterType.GREATER_THAN;
		} else if (filterValue.startsWith(QuickFilterType.LESS_THAN.getUserRepresentation())) {
			filterType = QuickFilterType.LESS_THAN;
		} else if (filterValue.equals(QuickFilterType.EMPTY.getUserRepresentation())) {
			filterType = QuickFilterType.EMPTY;
		}
		return filterType;
	}

	private static String getCleansedFilterValueString(BaseField filterField, String filterValue,
			QuickFilterType filterType, boolean exactFilters) {
		DatabaseFieldType dbType = filterField.getDbType();
		// clean-up filter value:
		filterValue = filterValue.replaceAll("\\*", "%");
		if (dbType.equals(DatabaseFieldType.VARCHAR) && (!exactFilters)) {
			if (filterValue.startsWith(":")) {
				filterValue = filterValue.replaceFirst(":", "");
			} else if ((!filterValue.startsWith("%")) && filterType.equals(QuickFilterType.LIKE)) {
				filterValue = "%" + filterValue;
			}
		}
		// when filtering decimal values...
		if (filterValue.endsWith(".") && dbType.equals(DatabaseFieldType.FLOAT)) {
			filterValue = filterValue.substring(0, filterValue.length() - 1);
		}
		// perform filter type specific filter value cleansing:
		if (filterType.equals(QuickFilterType.EMPTY)) {
			filterValue = "";
		} else {
			filterValue = filterValue.replaceAll("^\\Q" + filterType.getUserRepresentation()
					+ "\\E\\s*", "");
		}
		if (dbType.equals(DatabaseFieldType.BOOLEAN)) {
			boolean filterValueIsTrue = Helpers.valueRepresentsBooleanTrue(filterValue);
			if (filterValueIsTrue) {
				filterValue = "t";
			} else {
				filterValue = "f";
			}
		} else {
			if (!exactFilters) {
				// Remove letters from number field filters
				if (dbType.equals(DatabaseFieldType.FLOAT)
						|| dbType.equals(DatabaseFieldType.INTEGER)
						|| dbType.equals(DatabaseFieldType.SERIAL)) {
					filterValue = filterValue.replaceAll("[A-Za-z]", "").trim();
					if (filterValue.equals("")) {
						filterValue = "0";
					}
				} else {
					// Trim trailing spaces for text filters, SQL doesn't
					// seem to like them
					filterValue = filterValue.trim();
				}
				if (!filterType.equals(QuickFilterType.EQUAL)) {
					if (filterType.equals(QuickFilterType.LIKE)
							|| filterType.equals(QuickFilterType.NOT_LIKE)
							|| filterField.getDbType().equals(DatabaseFieldType.VARCHAR)
							|| filterField.getDbType().equals(DatabaseFieldType.TIMESTAMP)) {
						filterValue = filterValue + "%";
					}
				}
			}
		}
		return filterValue;
	}

	private static String getFilterTypeSqlRepresentation(BaseField filterField,
			QuickFilterType quickFilterType, boolean exactFilter) {
		DatabaseFieldType dbType = filterField.getDbType();
		if (exactFilter
				&& quickFilterType.equals(QuickFilterType.LIKE)
				&& ((dbType.equals(DatabaseFieldType.INTEGER) || (dbType
						.equals(DatabaseFieldType.SERIAL))))) {
			return "=";
		}
		String filterTypeSqlRepresentation = quickFilterType.getSqlRepresentation();
		if (quickFilterType.equals(QuickFilterType.EMPTY)) {
			filterTypeSqlRepresentation = filterTypeSqlRepresentation.replaceAll(
					"gtpb_field_placeholder", filterField.getInternalFieldName() + "::text");
		}
		return filterTypeSqlRepresentation;
	}

	/**
	 * Filter for date/time values that can't be parsed as actual date/times but
	 * should be filtered as text, e.g *Jan
	 */
	private static String generateSqlFilterStringForDateAsText(BaseField filterField,
			QuickFilterType filterType) throws CantDoThatException {
		String sqlFilterString = "";
		// Filter dates in the correct format
		String dateFormat = "";
		if (filterField instanceof DateField) {
			dateFormat = ((DateField) filterField).getDatabaseFormatString();
		} else if (filterField instanceof CalculationField) {
			dateFormat = Helpers.generateDbDateFormat(((CalculationField) filterField)
					.getReportCalcField().getDateResolution());
		}
		String filterFieldInternalName = filterField.getInternalFieldName();
		String filterTypeSqlRepresentation = getFilterTypeSqlRepresentation(filterField,
				filterType, false);
		sqlFilterString = "lower(to_char(" + filterFieldInternalName + ", '" + dateFormat + "'))";
		sqlFilterString += " " + filterTypeSqlRepresentation + " ? ";
		return sqlFilterString;
	}

	private static String generateSqlFilterStringForBoolean(BaseField filterField) {
		// filters on boolean have to use =, ILIKE doesn't work
		String filterFieldInternalName = filterField.getInternalFieldName();
		String sqlFilterString = "CASE WHEN " + filterFieldInternalName
				+ " THEN 't' ELSE 'f' END = ? ";
		return sqlFilterString;
	}

	/**
	 * Attempt to parse a string ('today', '23 january 2009' etc.) as a
	 * timestamp
	 * 
	 * @return A JChronic Span representing the time period, or null if
	 *         unparseable
	 */
	private static Span parseTimestamp(String valueToParse) {
		// Check any patterns that we know won't be interpreted as timestamps
		// properly
		// th = start of 'this month/year/week' etc., not yet parseable -
		// Chronic also confuses it with end of 25th etc.
		// \d\d = start of a date not yet parseable
		// * or % = text filtering, not for date parsing
		if (valueToParse.contains("%") || valueToParse.contains("*")
				|| valueToParse.trim().equalsIgnoreCase("th")
				|| valueToParse.matches("^\\d{1,2}\\s*$")) {
			return null;
		} else if ((valueToParse.matches("^\\d+$")) && (valueToParse.length() > 8)
				&& (valueToParse.length() < 11)) {
			// Note: the regex above matches a string that *only* contains
			// numbers
			// Value is a unix 'epoch' timestamp
			long epochTime = Long.valueOf(valueToParse);
			// TODO: better calculation of end time, we just add an hour at the
			// moment
			Span timespan = new Span(epochTime, epochTime + 3600);
			return timespan;
		} else {
			try {
				Options chronicOptions = new Options();
				chronicOptions.setGuess(false);
				chronicOptions.setContext(Pointer.PointerType.NONE);
				Span timespan = Chronic.parse(valueToParse, chronicOptions);
				return timespan;
			} catch (IllegalStateException isex) {
				return null;
			}
		}
	}

	private static String generateFilterStringForField(BaseField filterField, String filterValue,
			List<ReportQuickFilterInfo> filtersUsed, boolean exactFilters)
			throws CantDoThatException {
		StringBuilder filterStringForField = new StringBuilder();
		QuickFilterType filterType = getFilterTypeFromFilterValueString(filterValue);
		filterValue = getCleansedFilterValueString(filterField, filterValue, filterType,
				exactFilters);
		String sqlFilterString = "";
		DatabaseFieldType dbType = filterField.getDbType();
		boolean timespanCanBeParsed = true;
		if (dbType.equals(DatabaseFieldType.TIMESTAMP)) {
			// see if the filter string can be parsed as a valid timestamp
			String valueToParse = filterValue;
			if (valueToParse.endsWith("%")) {
				valueToParse = valueToParse.substring(0, valueToParse.length() - 1);
			}
			if (parseTimestamp(valueToParse) == null) {
				timespanCanBeParsed = false;
			}
		}
		/*
		 * if (filterField instanceof RelationField) { sqlFilterString =
		 * generateSqlFilterStringForRelation((RelationField) filterField,
		 * filterType); filterStringForField.append(sqlFilterString); } else
		 */
		if (filterField instanceof RelationField) {
			logger.warn("Relation field filter: " + filterField.getTableContainingField() + "."
					+ filterField + " = " + filterValue);
		}
		if (dbType.equals(DatabaseFieldType.TIMESTAMP) && !timespanCanBeParsed) {
			sqlFilterString = generateSqlFilterStringForDateAsText(filterField, filterType);
			filterStringForField.append(sqlFilterString);
		} else if (dbType.equals(DatabaseFieldType.BOOLEAN)) {
			sqlFilterString = generateSqlFilterStringForBoolean(filterField);
			filterStringForField.append(sqlFilterString);
		} else {
			// filter on the stored value:
			String filterFieldInternalName = filterField.getInternalFieldName();
			if (dbType.equals(DatabaseFieldType.VARCHAR)) {
				if (filterType.equals(QuickFilterType.EMPTY)) {
					filterStringForField.append(filterFieldInternalName);
				} else {
					filterStringForField.append("lower(" + filterFieldInternalName + ")");
				}
			} else if (filterType.equals(QuickFilterType.GREATER_THAN)
					|| filterType.equals(QuickFilterType.LESS_THAN)) {
				if (dbType.equals(DatabaseFieldType.FLOAT)
						|| dbType.equals(DatabaseFieldType.INTEGER)
						|| dbType.equals(DatabaseFieldType.SERIAL)
						|| (dbType.equals(DatabaseFieldType.TIMESTAMP) && timespanCanBeParsed)) {
					filterStringForField.append(filterFieldInternalName);
				} else {
					filterStringForField.append(filterFieldInternalName + "::text");
				}
			} else {
				if (exactFilters
						&& filterType.equals(QuickFilterType.LIKE)
						&& (dbType.equals(DatabaseFieldType.INTEGER) || dbType
								.equals(DatabaseFieldType.SERIAL))) {
					// When exact filtering is on, treat integers as numbers
					// rather than casting to text
					// Allows indexes etc. to work
					filterStringForField.append(filterFieldInternalName);
				} else if (dbType.equals(DatabaseFieldType.VARCHAR)) {
					filterStringForField.append(filterFieldInternalName);
				} else if (filterType.equals(QuickFilterType.EQUAL)
						&& (!dbType.equals(DatabaseFieldType.VARCHAR))) {
					filterStringForField.append(filterFieldInternalName);
				} else {
					filterStringForField.append(filterFieldInternalName + "::text");
				}
			}
			String filterTypeSqlRepresentation = getFilterTypeSqlRepresentation(filterField,
					filterType, exactFilters);
			filterStringForField.append(" " + filterTypeSqlRepresentation + " ? ");
		}
		// save the filter for use when running SQL
		ReportQuickFilterInfo filter = new ReportQuickFilter(filterField, filterValue, filterType);
		filtersUsed.add(filter);
		return filterStringForField.toString();
	}

	public PreparedStatement getReportSqlPreparedStatement(Connection conn,
			Map<BaseField, String> filterValues, boolean exactFilters,
			Map<BaseField, Boolean> reportSorts, int rowLimit, BaseField selectField,
			QuickFilterType filterType, boolean lookupPostcodeLatLong) throws SQLException,
			CantDoThatException {
		StringBuilder SQLCode;
		ReportFieldInfo postcodeField = null;
		if (lookupPostcodeLatLong) {
			ReportMapInfo map = report.getMap();
			postcodeField = map.getPostcodeField();
		}
		if (selectField == null) {
			SQLCode = new StringBuilder("SELECT ");
			Set<ReportFieldInfo> reportFields = this.report.getReportFields();
			for (ReportFieldInfo reportField : reportFields) {
				String internalFieldName = reportField.getInternalFieldName();
				SQLCode.append(internalFieldName + ", ");
				// for date fields, also include the date as a number: no. ms
				// since 1970. This can be used for std. dev/mean calculations
				// when cell colouring
				if (reportField.getBaseField().getDbType().equals(DatabaseFieldType.TIMESTAMP)) {
					SQLCode.append("(extract(EPOCH FROM " + internalFieldName + ")) AS "
							+ internalFieldName + "_ms, ");
				} else if (reportField.getBaseField() instanceof RelationField) {
					RelationField relationField = (RelationField) reportField.getBaseField();
					String relatedTableInternalName = relationField.getRelatedTable()
							.getInternalTableName();
					String relatedValueFieldString = relationField.getRelatedField()
							.getInternalFieldName();
					String relatedDisplayFieldString = relationField.getDisplayField()
							.getInternalFieldName();
					SQLCode.append("(SELECT ").append(relatedTableInternalName).append(".")
							.append(relatedDisplayFieldString);
					SQLCode.append(" FROM ").append(relatedTableInternalName);
					SQLCode.append(" WHERE ").append(relatedValueFieldString).append(" = ")
							.append(this.report.getInternalReportName()).append(".")
							.append(relationField.getInternalFieldName());
					SQLCode.append(") AS ").append(relationField.getInternalFieldName())
							.append("_display, ");
				} else if (reportField.equals(postcodeField)) {
					SQLCode.append("dbint_postcodes.latitude AS ").append(
							internalFieldName + "_latitude, ");
					SQLCode.append("dbint_postcodes.longitude AS ").append(
							internalFieldName + "_longitude, ");
				}
			}
			// remove trailing comma
			SQLCode.setLength(SQLCode.length() - 2);
			SQLCode.append(" FROM ");
		} else {
			SQLCode = new StringBuilder("SELECT DISTINCT " + selectField.getInternalFieldName()
					+ " FROM ");
		}
		SQLCode.append(this.report.getInternalReportName());
		if (postcodeField != null) {
			SQLCode.append(" LEFT OUTER JOIN dbint_postcodes ON ");
			SQLCode.append("upper(trim(" + this.report.getInternalReportName() + "."
					+ postcodeField.getInternalFieldName() + ")) = dbint_postcodes.postcode");
		}
		// Apply filters if there are any
		Map<String, List<ReportQuickFilterInfo>> whereClauseMap = this.getWhereClause(filterValues,
				exactFilters, filterType);
		String filterArgs = null;
		List<ReportQuickFilterInfo> filtersUsed = null;
		for (Map.Entry<String, List<ReportQuickFilterInfo>> whereClause : whereClauseMap.entrySet()) {
			filterArgs = whereClause.getKey();
			filtersUsed = whereClause.getValue();
		}
		// check whether filter arguments were applied
		if (filterArgs.length() > 0) {
			SQLCode.append(" WHERE ");
			SQLCode.append(filterArgs);
		}
		// apply sorts
		StringBuilder sortArguments = new StringBuilder();
		String ascString = " ASC NULLS FIRST, ";
		String descString = " DESC NULLS LAST, ";
		Set<BaseField> reportBaseFields = this.report.getReportBaseFields();
		for (Map.Entry<BaseField, Boolean> sortEntry : reportSorts.entrySet()) {
			BaseField sortField = sortEntry.getKey();
			// Only apply sort if the sorted field is actually in this report
			if (reportBaseFields.contains(sortField)) {
				Boolean sortAscending = sortEntry.getValue();
				if (sortField instanceof RelationField) {
					// Sort on the display value:
					String sortFieldDisplayName = sortField.getInternalFieldName() + "_display";
					sortArguments.append(sortFieldDisplayName);
				} else {
					// Sort on the stored value:
					String sortFieldInternalName = sortField.getInternalFieldName();
					if (sortField.getDbType().equals(DatabaseFieldType.VARCHAR)) {
						sortFieldInternalName = "lower(" + sortFieldInternalName + ")";
					}
					sortArguments.append(sortFieldInternalName);
				}
				if (sortAscending) {
					sortArguments.append(ascString);
				} else {
					sortArguments.append(descString);
				}
			}
		}
		if (sortArguments.length() > 0) {
			// remove trailing comma and spaces
			String sortArgs = sortArguments.substring(0, sortArguments.length() - 2);
			SQLCode.append(" ORDER BY ").append(sortArgs);
		}
		if (rowLimit > 0) {
			SQLCode.append(" LIMIT ").append(rowLimit);
		}
		PreparedStatement statement = conn.prepareStatement(SQLCode.toString());
		if ((rowLimit > 10000) && (this.report.getRowCount() > 10000)) {
			// Don't load large datasets fully into memory in one go
			statement.setFetchSize(100);
		}
		statement = this.fillInFilterValues(filtersUsed, statement, exactFilters);
		logger.debug("Report prepared statement: " + statement);
		return statement;
	}

	public PreparedStatement fillInFilterValues(List<ReportQuickFilterInfo> filtersUsed,
			PreparedStatement statement, boolean exactFiltering) throws SQLException {
		int i = 0;
		// Fill in filters
		for (ReportQuickFilterInfo filter : filtersUsed) {
			i++;
			// filter numbers as text, unless we're doing a > or < comparison,
			// or exact filtering is on
			// This is so part filters will match as you type
			QuickFilterType filterType = filter.getFilterType();
			DatabaseFieldType dbFieldType = filter.getFilterField().getDbType();
			String value = filter.getFilterValue();
			if (filterType.equals(QuickFilterType.GREATER_THAN)
					|| filterType.equals(QuickFilterType.LESS_THAN)) {
				if (dbFieldType.equals(DatabaseFieldType.FLOAT)) {
					statement.setDouble(i, Double.valueOf(value));
				} else if (dbFieldType.equals(DatabaseFieldType.INTEGER)
						|| dbFieldType.equals(DatabaseFieldType.SERIAL)) {
					statement.setInt(i, Integer.valueOf(value));
				} else if (dbFieldType.equals(DatabaseFieldType.TIMESTAMP)) {
					// By this time, a date/time filter such as 'today' will
					// have been turned into two filters using > and <, see
					// preprocessDateFilter, so will always be processed here
					if (value.endsWith("%")) {
						value = value.substring(0, value.length() - 1);
					}
					Span timespan = parseTimestamp(value);
					if (timespan == null) {
						statement.setString(i, value);
					} else {
						Timestamp timestamp;
						if (filterType.equals(QuickFilterType.LESS_THAN)) {
							timestamp = new Timestamp(timespan.getEnd() * 1000);
						} else {
							timestamp = new Timestamp(timespan.getBegin() * 1000);
						}
						statement.setTimestamp(i, timestamp);
					}
				} else {
					statement.setString(i, value);
				}
			} else if (filterType.equals(QuickFilterType.EQUAL)) {
				if (dbFieldType.equals(DatabaseFieldType.INTEGER)
						|| dbFieldType.equals(DatabaseFieldType.SERIAL)) {
					statement.setInt(i, Integer.valueOf(value));
				} else {
					statement.setString(i, value);
				}
			} else if (exactFiltering
					&& filterType.equals(QuickFilterType.LIKE)
					&& (dbFieldType.equals(DatabaseFieldType.INTEGER) || dbFieldType
							.equals(DatabaseFieldType.SERIAL))) {
				statement.setInt(i, Integer.valueOf(value));
			} else if (filterType.equals(QuickFilterType.EMPTY)) {
				statement.setString(i, "");
			} else {
				statement.setString(i, value);
			}
		}
		return statement;
	}

	/**
	 * Turn
	 * 
	 * 'today'
	 * 
	 * into
	 * 
	 * '>today and <today'
	 */
	private static String preprocessDateFilter(String filterValue) throws CantDoThatException {
		String processedFilterValue = filterValue;
		String[] tokens = filterValue.split("\\sand\\s|\\sor\\s");
		for (String token : tokens) {
			if (!(token.startsWith(">") || token.startsWith("<"))) {
				Span timespan = parseTimestamp(token);
				if (timespan != null) {
					String replacement = ">" + token + " and <" + token;
					processedFilterValue = processedFilterValue.replaceAll("\\Q" + token + "\\E",
							replacement);
				}
			}
		}
		return processedFilterValue;
	}

	public Map<String, List<ReportQuickFilterInfo>> getWhereClause(
			Map<BaseField, String> filterValues, boolean exactFilters, QuickFilterType filterType)
			throws CantDoThatException {
		if ((!filterType.equals(QuickFilterType.AND)) && (!filterType.equals(QuickFilterType.OR))) {
			throw new CantDoThatException("Filter type " + filterType + " should be AND or OR");
		}
		StringBuilder filterArguments = new StringBuilder();
		Set<BaseField> reportBaseFields = this.report.getReportBaseFields();
		List<ReportQuickFilterInfo> filtersUsed = new LinkedList<ReportQuickFilterInfo>();
		FILTERSLOOP: for (Map.Entry<BaseField, String> filterValueEntry : filterValues.entrySet()) {
			// Generate the filter for a field
			BaseField filterField = filterValueEntry.getKey();
			// Only apply filter if the field is actually in the report
			if (!reportBaseFields.contains(filterField)) {
				continue FILTERSLOOP;
			}
			String filterValue = filterValueEntry.getValue();
			if (filterValue == null) {
				throw new CantDoThatException("Filter value for " + filterField + " is null");
			}
			// Ignore a filter that is only made up of spaces.
			// These have caused users problems in the past as they are
			// invisible so they don't know there's a filter
			boolean filterIsOnlySpaces = true;
			SPACESCHECK: for (int i = 0; i < filterValue.length(); i++) {
				char character = filterValue.charAt(i);
				if (character != " ".charAt(0)) {
					filterIsOnlySpaces = false;
					break SPACESCHECK;
				}
			}
			if (filterIsOnlySpaces) {
				continue FILTERSLOOP;
			}
			filterValue = filterValue.toLowerCase();
			DatabaseFieldType filterFieldDbType = filterField.getDbType();
			// Don't bother filtering if the user's started typing a
			// filter type but hasn't typed any of the actual filter value
			if ((filterValue.equals(QuickFilterType.NOT_LIKE.getUserRepresentation()))
					|| (filterValue.equals(QuickFilterType.GREATER_THAN.getUserRepresentation()))
					|| (filterValue.equals(QuickFilterType.LESS_THAN.getUserRepresentation()))) {
				continue FILTERSLOOP;
			}
			// remove commas when filtering a number field
			if (filterFieldDbType.equals(DatabaseFieldType.INTEGER)
					|| filterFieldDbType.equals(DatabaseFieldType.FLOAT)
					|| filterFieldDbType.equals(DatabaseFieldType.SERIAL)) {
				filterValue = filterValue.replaceAll(",", "");
			} else if (filterFieldDbType.equals(DatabaseFieldType.TIMESTAMP)) {
				filterValue = preprocessDateFilter(filterValue);
			}
			// OR/AND filtering of values (restricted to a given field)
			String[] andFilterParts = filterValue.split(QuickFilterType.AND.getUserRepresentation()
					.toLowerCase());
			String filterStringForField = "";
			for (String andFilterPartValue : andFilterParts) {
				String[] orFilterParts = andFilterPartValue.split(QuickFilterType.OR
						.getUserRepresentation().toLowerCase());
				if (orFilterParts.length > 1) {
					for (String orFilterPartValue : orFilterParts) {
						// Generate sub-part filter string for field:
						filterStringForField += "("
								+ generateFilterStringForField(filterField, orFilterPartValue,
										filtersUsed, exactFilters) + ")";
						filterStringForField += QuickFilterType.OR.getSqlRepresentation();
					}
					// remove trailing AND and spaces
					int charsToRemove = QuickFilterType.OR.getSqlRepresentation().length();
					filterStringForField = filterStringForField.substring(0,
							filterStringForField.length() - charsToRemove);
					filterStringForField = "(" + filterStringForField + ")";
				} else {
					// Generate sub-part filter string for field:
					filterStringForField += "("
							+ generateFilterStringForField(filterField, andFilterPartValue,
									filtersUsed, exactFilters) + ")";
				}
				filterStringForField += QuickFilterType.AND.getSqlRepresentation();
			}
			if (filterStringForField.length() > 0) {
				// remove trailing AND and spaces
				int charsToRemove = QuickFilterType.AND.getSqlRepresentation().length();
				filterStringForField = filterStringForField.substring(0,
						filterStringForField.length() - charsToRemove);
				filterStringForField = "(" + filterStringForField + ")";
				// now join the whole field filter to the last one with an AND
				// or an OR
				filterArguments.append(filterStringForField + filterType.getSqlRepresentation());
			}
		}
		// check that filter arguments were supplied
		String filterArgs = "";
		if (filterArguments.length() > 0) {
			// remove trailing AND or OR
			if (filterArguments.toString().endsWith(filterType.getSqlRepresentation())) {
				filterArgs = filterArguments.substring(0,
						filterArguments.length() - filterType.getSqlRepresentation().length())
						.trim();
			}
		}
		Map<String, List<ReportQuickFilterInfo>> whereClause = new HashMap<String, List<ReportQuickFilterInfo>>();
		whereClause.put(filterArgs, filtersUsed);
		return whereClause;
	}

	public List<DataRowInfo> getReportDataRows(Connection conn, AppUserInfo user,
			Map<BaseField, String> filterValues, boolean exactFilters,
			Map<BaseField, Boolean> reportSorts, int rowLimit, QuickFilterType filterType,
			boolean lookupPostcodeLatLong) throws SQLException, CodingErrorException,
			CantDoThatException, ObjectNotFoundException {
		List<DataRowInfo> reportData = null;
		long executionStartTime = System.currentTimeMillis();
		ReportFieldInfo postcodeField = null;
		if (user != null) {
			if (user.getUsesCustomUI()) {
				// Throws ObjectNotFoundException if the report has no 'created
				// by' field
				BaseField createdByField = this.report.getReportField(
						HiddenFields.CREATED_BY.getFieldName()).getBaseField();
				String createdBy = "=" + user.getForename() + " " + user.getSurname() + " ("
						+ user.getUserName() + ")";
				filterValues.put(createdByField, createdBy);
			}
		}
		if (lookupPostcodeLatLong) {
			ReportMapInfo map = report.getMap();
			if (map != null) {
				postcodeField = map.getPostcodeField();
			}
		}
		// Query tuning settings
		QueryPlanSelection qp = report.getQueryPlanSelection();
		if (qp.equals(QueryPlanSelection.TRY_NO_NESTED_LOOPS)
				|| qp.equals(QueryPlanSelection.NO_NESTED_LOOPS)) {
			enableNestloop(conn, false);
		}
		Integer memoryAllocation = report.getMemoryAllocation();
		if (memoryAllocation != null) {
			setWorkMemOverride(conn, memoryAllocation, true);
		}
		PreparedStatement statement = this.getReportSqlPreparedStatement(conn, filterValues,
				exactFilters, reportSorts, rowLimit, null, filterType, lookupPostcodeLatLong);
		ResultSet results = statement.executeQuery();
		if (qp.equals(QueryPlanSelection.TRY_NO_NESTED_LOOPS)
				|| qp.equals(QueryPlanSelection.NO_NESTED_LOOPS)) {
			enableNestloop(conn, true);
			if (qp.equals(QueryPlanSelection.TRY_NO_NESTED_LOOPS)) {
				// See if no nested loops was any better
				float durationSecs = (System.currentTimeMillis() - executionStartTime)
						/ ((float) 1000);
				float speedup = report.getQuerySeconds() / durationSecs;
				if (speedup > 2) {
					report.setQueryPlanSelection(QueryPlanSelection.NO_NESTED_LOOPS);
					logger.info("Report " + report
							+ ": without nested loop joins, query speedup is " + speedup);
				} else {
					report.setQueryPlanSelection(QueryPlanSelection.ALTERNATIVE_NOT_FASTER);
					logger.info("Report " + report + ": removing nested loop joins is " + speedup
							+ " times faster, not going to use that technique");
				}
			}
		}
		if (memoryAllocation != null) {
			setWorkMemOverride(conn, 0, false);
		}
		float durationSecs = (System.currentTimeMillis() - executionStartTime) / ((float) 1000);
		if (durationSecs > AppProperties.longSqlTime) {
			logger.debug("Long SELECT SQL execution time of " + durationSecs
					+ " seconds for report " + this.report + ". Filters = " + filterValues
					+ ", sorts = " + reportSorts + ", exact filters = " + exactFilters
					+ ", statement = " + statement);
			if (filterValues.size() == 0) {
				switch (qp) {
				case DEFAULT:
					boolean nestedLoop = false;
					PreparedStatement explainStatement = conn
							.prepareStatement("EXPLAIN SELECT * FROM "
									+ report.getInternalReportName() + " LIMIT " + rowLimit);
					ResultSet explainResults = explainStatement.executeQuery();
					EXPLAIN_LOOP: while (explainResults.next()) {
						if (explainResults.getString(1).toLowerCase().contains("nested loop")) {
							nestedLoop = true;
							break EXPLAIN_LOOP;
						}
					}
					explainResults.close();
					explainStatement.close();
					if (nestedLoop) {
						report.setQueryPlanSelection(QueryPlanSelection.TRY_NO_NESTED_LOOPS);
					} else {
						// We don't know an alternative
						report.setQueryPlanSelection(QueryPlanSelection.ALTERNATIVE_NOT_FASTER);
					}
					break;
				case TRY_NO_NESTED_LOOPS:
				case NO_NESTED_LOOPS:
					logger.info("Report " + report
							+ ": nested loops are faster but the query is still slow");
					break;
				}
			}
		}
		// Set report query seconds as a rolling average
		report.setQuerySeconds((durationSecs + report.getQuerySeconds()) / 2);
		// 2) parse the SQL resultset to generate a return value:
		int initialCapacity = rowLimit;
		if (initialCapacity > 10000 || initialCapacity < 0) {
			initialCapacity = 10000;
		}
		reportData = new ArrayList<DataRowInfo>(initialCapacity);
		DataRow reportDataRow;
		DataRowFieldInfo reportDataRowField;
		TableInfo parentTable = this.report.getParentTable();
		BaseField primaryKeyField = parentTable.getPrimaryKey();
		Map<BaseField, DataRowFieldInfo> row;
		while (results.next()) {
			int rowid = results.getInt(primaryKeyField.getInternalFieldName());
			row = new LinkedHashMap<BaseField, DataRowFieldInfo>();
			// row.clear();
			// add all columns to the row:
			for (ReportFieldInfo reportField : this.report.getReportFields()) {
				BaseField fieldSchema = reportField.getBaseField();
				String colourRepresentation = null;
				double numberOfStdDevsFromMean = 0d;
				String keyValue = "";
				String displayValue = null;
				// If cell should be coloured, calculate colour hex string
				boolean fieldShouldBeColoured = this.cachedFieldStats.containsKey(reportField);
				String internalFieldName = reportField.getInternalFieldName();
				String colourableFieldInternalName = internalFieldName;
				if (fieldShouldBeColoured
						&& fieldSchema.getDbType().equals(DatabaseFieldType.TIMESTAMP)) {
					// _ms: see getReportSqlPreparedStatement
					colourableFieldInternalName += "_ms";
				}
				double fieldValue = 0;
				if (fieldShouldBeColoured) {
					fieldValue = results.getDouble(colourableFieldInternalName);
					// don't colour nulls
					fieldShouldBeColoured = (!results.wasNull());
				}
				if (fieldShouldBeColoured) {
					ReportDataFieldStatsInfo fieldStats = this.cachedFieldStats.get(reportField);
					double stdDev = fieldStats.getStdDev();
					if (stdDev != 0d) {
						numberOfStdDevsFromMean = (fieldValue - fieldStats.getMean()) / stdDev;
					}
					// generate new ReportDataRowField object:
					if (fieldSchema.getDbType().equals(DatabaseFieldType.INTEGER)) {
						int dbValue = results.getInt(fieldSchema.getInternalFieldName());
						if (results.wasNull()) {
							keyValue = "";
						} else if (reportField instanceof ReportCalcFieldInfo) {
							keyValue = ((ReportCalcFieldInfo) reportField).formatInteger(dbValue);
						} else if (fieldSchema instanceof SequenceField) {
							// format a sequence as 1234 not 1,234
							keyValue = String.valueOf(dbValue);
						} else {
							keyValue = ((IntegerField) fieldSchema).formatInteger(dbValue);
						}
					} else if (fieldSchema.getDbType().equals(DatabaseFieldType.FLOAT)) {
						double dbValue = results.getDouble(fieldSchema.getInternalFieldName());
						if (results.wasNull()) {
							keyValue = "";
						} else if (reportField instanceof ReportCalcFieldInfo) {
							keyValue = ((ReportCalcFieldInfo) reportField).formatFloat(dbValue);
						} else {
							keyValue = ((DecimalField) fieldSchema).formatFloat(dbValue);
						}
					} else if (fieldSchema.getDbType().equals(DatabaseFieldType.TIMESTAMP)) {
						Date dbValue = results.getTimestamp(fieldSchema.getInternalFieldName());
						if (dbValue == null) {
							keyValue = "";
						} else {
							if (reportField instanceof ReportCalcFieldInfo) {
								keyValue = "" + dbValue.getTime();
								displayValue = ((ReportCalcFieldInfo) reportField)
										.formatDate(dbValue);
							} else {
								keyValue = "" + dbValue.getTime();
								// See DateFieldDefn constructor for format
								// explanation
								displayValue = ((DateField) fieldSchema).formatDate(dbValue);
							}
						}
					} else {
						// fallback to getting as a String and let the db
						// driver work out the formatting
						keyValue = results.getString(fieldSchema.getInternalFieldName());
					}
				} else {
					// field isn't one that colouring information is cached for,
					// generate a new ReportDataRowField object
					if (fieldSchema instanceof RelationField) {
						RelationField relationField = (RelationField) fieldSchema;
						keyValue = results.getString(relationField.getInternalFieldName());
						if (keyValue != null) {
							// displayValue =
							// displayLookups.get(relationField.getRelatedField()).get(
							// keyValue);
							displayValue = results.getString(relationField.getInternalFieldName()
									+ "_display");
						} else {
							keyValue = "";
						}
					} else {
						if (fieldSchema.getDbType().equals(DatabaseFieldType.TIMESTAMP)) {
							Date dbValue = results.getTimestamp(fieldSchema.getInternalFieldName());
							if (dbValue == null) {
								keyValue = "";
							} else {
								if (reportField instanceof ReportCalcFieldInfo) {
									keyValue = "" + dbValue.getTime();
									displayValue = ((ReportCalcFieldInfo) reportField)
											.formatDate(dbValue);
								} else {
									keyValue = "" + dbValue.getTime();
									// See DateFieldDefn constructor for format
									// explanation
									displayValue = ((DateField) fieldSchema).formatDate(dbValue);
								}
							}
						} else if (fieldSchema.getDbType().equals(DatabaseFieldType.FLOAT)) {
							double dbValue = results.getDouble(fieldSchema.getInternalFieldName());
							if (results.wasNull()) {
								keyValue = "";
							} else if (reportField instanceof ReportCalcFieldInfo) {
								keyValue = ((ReportCalcFieldInfo) reportField).formatFloat(dbValue);
							} else {
								keyValue = ((DecimalField) fieldSchema).formatFloat(dbValue);
							}
							// Override with 'not applicable' string if
							// necessary
							if (fieldSchema instanceof DecimalField) {
								DecimalField fieldSchemaDecimal = (DecimalField) fieldSchema;
								if (fieldSchemaDecimal.allowNotApplicable()) {
									if (fieldSchemaDecimal.getNotApplicableValue() == dbValue) {
										displayValue = fieldSchemaDecimal
												.getNotApplicableDescription();
									}
								}
							}
						} else if (fieldSchema.getDbType().equals(DatabaseFieldType.INTEGER)) {
							int dbValue = results.getInt(fieldSchema.getInternalFieldName());
							if (results.wasNull()) {
								keyValue = "";
							} else if (reportField instanceof ReportCalcFieldInfo) {
								keyValue = ((ReportCalcFieldInfo) reportField)
										.formatInteger(dbValue);
							} else if (fieldSchema instanceof SequenceField) {
								// format a sequence as 1234 not 1,234
								keyValue = String.valueOf(dbValue);
							} else {
								keyValue = ((IntegerField) fieldSchema).formatInteger(dbValue);
							}
						} else if (fieldSchema.getDbType().equals(DatabaseFieldType.BOOLEAN)) {
							keyValue = results.getString(fieldSchema.getInternalFieldName());
							// Note: we do need a null check. Boolean values are
							// always not null, however, nulls could be produced
							// in a report by e.g. an outer join
							if (keyValue == null) {
								keyValue = "";
							} else {
								if (keyValue.equals("t")) {
									keyValue = "true";
									// colour true values green
									colourRepresentation = "#8ce18c";
								} else {
									keyValue = "false";
								}
							}
						} else {
							keyValue = results.getString(fieldSchema.getInternalFieldName());
							if (keyValue == null) {
								keyValue = "";
							}
							if (fieldSchema instanceof TextField) {
								TextField fieldSchemaText = (TextField) fieldSchema;
								// Set displayValue to 'not applicable' string
								// if necessary
								if (fieldSchemaText.allowNotApplicable()) {
									if (fieldSchemaText.getNotApplicableValue().equals(keyValue)) {
										displayValue = fieldSchemaText
												.getNotApplicableDescription();
									}
								} else {
									// int textFieldSize =
									// fieldSchemaText.getContentSize() * 2;
									// if (textFieldSize > 400) {
									// textFieldSize = 400; // max out
									// }
									if (keyValue.length() > 401) {
										displayValue = keyValue.substring(0, 400) + "...";
									}
								}
							}
						}
					}
				}
				if (displayValue == null) {
					displayValue = keyValue;
				}
				if (numberOfStdDevsFromMean != 0d) {
					// colour will be calculated from std. dev
					reportDataRowField = new DataRowField(keyValue, displayValue,
							numberOfStdDevsFromMean);
				} else if (colourRepresentation != null) {
					// colour explicitly set (e.g. a boolean field)
					reportDataRowField = new DataRowField(keyValue, displayValue,
							colourRepresentation);
				} else {
					// no colour
					if (reportField.equals(postcodeField)) {
						Double latitude = results.getDouble(internalFieldName + "_latitude");
						Double longitude = results.getDouble(internalFieldName + "_longitude");
						reportDataRowField = new LocationDataRowField(keyValue, latitude, longitude);
					} else {
						reportDataRowField = new DataRowField(keyValue, displayValue);
					}
				}
				row.put(fieldSchema, reportDataRowField);
			}
			reportDataRow = new DataRow(parentTable, rowid, row);
			reportData.add(reportDataRow);
		}
		results.close();
		statement.close();
		return reportData;
	}

	public boolean isRowIdInReport(Connection conn, int rowId) throws SQLException {
		boolean rowIdIsInReport = false;
		BaseField primaryKey = this.report.getParentTable().getPrimaryKey();
		String SQLCode = "SELECT " + primaryKey.getInternalFieldName() + " FROM "
				+ this.report.getInternalReportName();
		SQLCode += " WHERE " + primaryKey.getInternalFieldName() + " = " + rowId;
		long executionStartTime = System.currentTimeMillis();
		Statement statement = conn.createStatement();
		ResultSet results = statement.executeQuery(SQLCode);
		if (results.next()) {
			rowIdIsInReport = true;
		}
		results.close();
		statement.close();
		float durationSecs = (System.currentTimeMillis() - executionStartTime) / ((float) 1000);
		if (durationSecs > AppProperties.longSqlTime) {
			logger.warn("Long SELECT SQL execution time of " + durationSecs
					+ " seconds for isRowIdInReport on report " + this.report + ", SQLCode = "
					+ SQLCode);
		}
		return rowIdIsInReport;
	}

	public String toString() {
		return "Data for the report " + this.report;
	}

	public boolean exceededCacheTime() {
		long ageInMilliseconds = System.currentTimeMillis() - this.getCacheCreationTime();
		// If an object has existed for at least the same number of minutes as
		// the number of tenths of a
		// second it took to generate, it's worth removing it from cache.
		//
		// However, cache every object for at least 10 seconds as it may be used
		// in multiple requests in quick sequence (e.g. pane 2 and pane 3
		// loading).
		//
		// Also, if a report has a lot of rows, can cache it for longer again
		// because the overall stats are less likely to change significantly
		long cacheForMillis = this.millisecsTakenToGenerateStats * 600;
		int rowNumFactor = this.report.getRowCount() / 1000;
		if (rowNumFactor > 1) {
			cacheForMillis = cacheForMillis * rowNumFactor;
		}
		cacheForMillis += 10000;
		if (ageInMilliseconds > cacheForMillis) {
			return true;
		} else {
			return false;
		}
	}

	public long getCacheCreationTime() {
		return this.cacheCreationTime;
	}

	public Map<ReportFieldInfo, ReportDataFieldStatsInfo> getFieldStats() {
		return this.cachedFieldStats;
	}

	/**
	 * One-call method to enable all report-specific optimisations or revert to
	 * defaults
	 */
	public static void enableOptimisations(Connection conn, BaseReportInfo report, boolean enable)
			throws SQLException {
		QueryPlanSelection planSelection = report.getQueryPlanSelection();
		boolean no_nested_loops = planSelection.equals(QueryPlanSelection.NO_NESTED_LOOPS);
		Integer memoryAllocation = report.getMemoryAllocation();
		if (enable) {
			if (no_nested_loops) {
				enableNestloop(conn, false);
			}
			if (memoryAllocation != null) {
				setWorkMemOverride(conn, memoryAllocation, true);
			}
		} else {
			if (no_nested_loops) {
				enableNestloop(conn, true);
			}
			if (memoryAllocation != null) {
				setWorkMemOverride(conn, 0, false);
			}
		}
	}

	/**
	 * Calling methods should always revert back to enable=true at the end of
	 * querying
	 * 
	 * @param enableNestLoop
	 *            true = enable nested loops in queries (default), false =
	 *            disable
	 */
	public static void enableNestloop(Connection conn, boolean enableNestLoop) throws SQLException {
		Statement setNestedLoopStatement = conn.createStatement();
		if (enableNestLoop) {
			setNestedLoopStatement.execute("SET enable_nestloop=true");
		} else {
			setNestedLoopStatement.execute("SET enable_nestloop=false");
		}
		setNestedLoopStatement.close();
	}

	/**
	 * Use if a particular report requires an increased worK_mem. Be sure to
	 * revert back to default after queries
	 * 
	 * @param enableOverride
	 *            true = use the report's value for work_mem, false = revert
	 *            back to the default
	 */
	public static void setWorkMemOverride(Connection conn, int workMem, boolean enableOverride)
			throws SQLException {
		Statement setWorkMemStatement = conn.createStatement();
		if (enableOverride && (workMem > 0)) {
			setWorkMemStatement.execute("SET work_mem='" + workMem + "MB'");
		} else {
			setWorkMemStatement.execute("SET work_mem=default");
		}
		setWorkMemStatement.close();
	}

	/**
	 * Definition of the report which we are using to return data
	 */
	private final BaseReportInfo report;

	/**
	 * Set cache creation time to the creation time of the object
	 */
	private final long cacheCreationTime = (new Date()).getTime();

	/**
	 * Constructor records how long it takes to generate mean and std. dev. for
	 * number fields
	 */
	private long millisecsTakenToGenerateStats;

	/**
	 * Avg. / std. dev. calcs used for field colouring
	 */
	private Map<ReportFieldInfo, ReportDataFieldStatsInfo> cachedFieldStats = new HashMap<ReportFieldInfo, ReportDataFieldStatsInfo>();

	private static final SimpleLogger logger = new SimpleLogger(ReportData.class);
}