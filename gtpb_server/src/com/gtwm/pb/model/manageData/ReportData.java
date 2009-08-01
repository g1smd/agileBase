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
package com.gtwm.pb.model.manageData;

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
import com.gtwm.pb.util.Helpers;
import com.gtwm.pb.util.Enumerations.DatabaseFieldType;
import com.gtwm.pb.util.Enumerations.QuickFilterType;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.AppProperties;
import java.sql.*;
import java.util.Date;
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
	 *            colouring, to save potentially slow SQL queries
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
				// query. NB Don't use random() as very slow for large no. rows
				// SQLCode += " WHERE random() > 0.9 ORDER BY random()";
				String pKeyInternalName = report.getParentTable().getPrimaryKey()
						.getInternalFieldName();
				int randomNumber = (int) (Math.random() * 10);
				if (randomNumber == 10) {
					randomNumber = 9;
				}
				SQLCode += " WHERE " + pKeyInternalName + " % 10 = " + randomNumber;
			}
			try {
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
						ReportDataFieldStatsInfo fieldStats = new ReportDataFieldStats(results
								.getDouble(fieldNum * 2), results.getDouble((fieldNum * 2) + 1));
						this.cachedFieldStats.put(reportField, fieldStats);
					}
				}
				results.close();
				statement.close();
			} catch (SQLException sqlex) {
				throw new SQLException("Error calculating field statistics: " + sqlex);
			}
			this.millisecsTakenToGenerateStats = System.currentTimeMillis() - startTime;
			float durationSecs = this.millisecsTakenToGenerateStats / ((float) 1000);
			if (durationSecs > (AppProperties.longSqlTime * 2)) {
				logger.warn("Long SELECT SQL execution time of " + durationSecs + " seconds for "
						+ this.report + " statistics, statement = " + SQLCode);
			}
		}
	}

	private QuickFilterType getFilterTypeFromFilterValueString(String filterValue) {
		// perform filter type specific filter value cleansing:
		QuickFilterType filterType = QuickFilterType.LIKE; // LIKE is default
		// case
		if (filterValue.startsWith(QuickFilterType.NOT_LIKE.getUserRepresentation())) {
			filterType = QuickFilterType.NOT_LIKE;
		} else if (filterValue.startsWith(QuickFilterType.GREATER_THAN.getUserRepresentation())) {
			filterType = QuickFilterType.GREATER_THAN;
		} else if (filterValue.startsWith(QuickFilterType.LESS_THAN.getUserRepresentation())) {
			filterType = QuickFilterType.LESS_THAN;
		} else if (filterValue.equals(QuickFilterType.EMPTY.getUserRepresentation())) {
			filterType = QuickFilterType.EMPTY;
		}
		return filterType;
	}

	private String getCleansedFilterValueString(BaseField filterField, String filterValue,
			QuickFilterType filterType, boolean exactFilters) {
		DatabaseFieldType dbType = filterField.getDbType();
		// clean-up filter value:
		filterValue = filterValue.replaceAll("\\*", "%");
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

				if (filterType.equals(QuickFilterType.LIKE)
						|| filterType.equals(QuickFilterType.NOT_LIKE)
						|| filterField.getDbType().equals(DatabaseFieldType.VARCHAR)
						|| filterField.getDbType().equals(DatabaseFieldType.TIMESTAMP)) {
					filterValue = filterValue + "%";
				}
			}
		}
		return filterValue;
	}

	private String getFilterTypeSqlRepresentation(BaseField filterField, QuickFilterType filterType) {
		String filterTypeSqlRepresentation = filterType.getSqlRepresentation();
		if (filterType.equals(QuickFilterType.EMPTY)) {
			filterTypeSqlRepresentation = filterTypeSqlRepresentation.replaceAll(
					"gtpb_field_placeholder", filterField.getInternalFieldName() + "::text");
		}
		return filterTypeSqlRepresentation;
	}

	/**
	 * Filter on the display value
	 */
	private String generateSqlFilterStringForRelation(RelationField relationField,
			QuickFilterType filterType) {
		StringBuilder sqlFilterString = new StringBuilder();
		String filterFieldInternalName = relationField.getInternalFieldName();
		String relatedTableInternalName = relationField.getRelatedTable().getInternalTableName();
		String relatedFieldInternalName = relationField.getRelatedField().getInternalFieldName();
		String displayFieldInternalName = relationField.getDisplayField().getInternalFieldName();
		// I'm not sure if the following is correct. Should a where clause be
		// added to only return when
		// relatedTableInternalName.relatedFieldInternalName=thisReportInternalName.relatedFieldInternalName
		sqlFilterString.append(filterFieldInternalName).append(" IN (SELECT ");
		sqlFilterString.append(relatedFieldInternalName).append(" FROM ");
		sqlFilterString.append(relatedTableInternalName).append(" WHERE ");
		String filterTypeSqlRepresentation = filterType.getSqlRepresentation();
		filterTypeSqlRepresentation = filterTypeSqlRepresentation.replaceAll(
				"gtpb_field_placeholder", displayFieldInternalName + "::text");
		// TODO: We should really check for > or < filtering on numbers here, as
		// in generateFilterStringForField(BaseField, String,
		// List<ReportQuickFilterInfo>, boolean)
		// but relations to number fields are very uncommon, perhaps adding the
		// code wouldn't be worth it?
		if (relationField.getDisplayField().getDbType().equals(DatabaseFieldType.VARCHAR)) {
			sqlFilterString.append("lower(" + displayFieldInternalName + ")" + " "
					+ filterTypeSqlRepresentation + " ?) ");
		} else {
			sqlFilterString.append(displayFieldInternalName + "::text "
					+ filterTypeSqlRepresentation + " ?) ");
		}
		return sqlFilterString.toString();
	}

	/**
	 * Filter for date/time values that can't be parsed as actual date/times but
	 * should be filtered as text, e.g *Jan
	 */
	private String generateSqlFilterStringForDateAsText(BaseField filterField,
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
		String filterTypeSqlRepresentation = this.getFilterTypeSqlRepresentation(filterField,
				filterType);
		sqlFilterString = "lower(to_char(" + filterFieldInternalName + ", '" + dateFormat + "'))";
		sqlFilterString += " " + filterTypeSqlRepresentation + " ? ";
		return sqlFilterString;
	}

	private String generateSqlFilterStringForBoolean(BaseField filterField) {
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
	private Span parseTimestamp(String valueToParse) {
		// Check any patterns that we know won't be interpreted as timestamps
		// properly
		if (valueToParse.contains("%") || valueToParse.contains("*")
				|| valueToParse.matches("^\\d{1,2}\\s*$")) {
			return null;
		} else {
			try {
				Options chronicOptions = new Options();
				chronicOptions.setGuess(false);
				chronicOptions.setContext(Pointer.PointerType.NONE);
				Span timespan = Chronic.parse(valueToParse, chronicOptions);
				if (timespan == null) {
					return null;
				}
				return timespan;
			} catch (IllegalStateException isex) {
				return null;
			}
		}
	}

	private String generateFilterStringForField(BaseField filterField, String filterValue,
			List<ReportQuickFilterInfo> filtersUsed, boolean exactFilters)
			throws CantDoThatException {
		StringBuilder filterStringForField = new StringBuilder();
		QuickFilterType filterType = this.getFilterTypeFromFilterValueString(filterValue);
		filterValue = this.getCleansedFilterValueString(filterField, filterValue, filterType,
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
			if (this.parseTimestamp(valueToParse) == null) {
				timespanCanBeParsed = false;
			}
		}
		if (filterField instanceof RelationField) {
			sqlFilterString = this.generateSqlFilterStringForRelation((RelationField) filterField,
					filterType);
			filterStringForField.append(sqlFilterString);
		} else if (dbType.equals(DatabaseFieldType.TIMESTAMP) && !timespanCanBeParsed) {
			sqlFilterString = this.generateSqlFilterStringForDateAsText(filterField, filterType);
			filterStringForField.append(sqlFilterString);
		} else if (dbType.equals(DatabaseFieldType.BOOLEAN)) {
			sqlFilterString = this.generateSqlFilterStringForBoolean(filterField);
			filterStringForField.append(sqlFilterString);
		} else {
			// filter on the stored value:
			String filterFieldInternalName = filterField.getInternalFieldName();
			if (dbType.equals(DatabaseFieldType.VARCHAR)) {
				filterStringForField.append("lower(" + filterFieldInternalName + ")");
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
				filterStringForField.append(filterFieldInternalName + "::text");
			}
			String filterTypeSqlRepresentation = this.getFilterTypeSqlRepresentation(filterField,
					filterType);
			filterStringForField.append(" " + filterTypeSqlRepresentation + " ? ");
		}
		// save the filter for use when running SQL
		ReportQuickFilterInfo filter = new ReportQuickFilter(filterField, filterValue, filterType);
		filtersUsed.add(filter);
		return filterStringForField.toString();
	}

	public PreparedStatement getReportSqlPreparedStatement(Connection conn,
			Map<BaseField, String> filterValues, boolean exactFilters,
			Map<BaseField, Boolean> reportSorts, int rowLimit, BaseField selectField)
			throws SQLException, CantDoThatException {
		StringBuilder SQLCode;
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
					String relatedTableString = relationField.getRelatedTable()
							.getInternalTableName();
					String relatedValueFieldString = relationField.getRelatedField()
							.getInternalFieldName();
					String relatedDisplayFieldString = relationField.getDisplayField()
							.getInternalFieldName();
					SQLCode.append("(SELECT ").append(relatedTableString).append(".").append(
							relatedDisplayFieldString);
					SQLCode.append(" FROM ").append(relatedTableString);
					SQLCode.append(" WHERE ").append(relatedValueFieldString).append(" = ").append(
							this.report.getInternalReportName()).append(".").append(
							relationField.getInternalFieldName());
					SQLCode.append(")AS ").append(relationField.getInternalFieldName()).append(
							"_display, ");
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
		// Apply filters if there are any
		Map<String, List<ReportQuickFilterInfo>> whereClauseMap = this.getWhereClause(filterValues,
				exactFilters);
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
			SQLCode.append(" ORDER BY ");
			SQLCode.append(sortArgs);
		}
		if (rowLimit > 0) {
			SQLCode.append(" LIMIT ");
			SQLCode.append(rowLimit);
		}
		PreparedStatement statement = conn.prepareStatement(SQLCode.toString());
		this.fillInFilterValues(filtersUsed, statement);
		return statement;
	}

	public void fillInFilterValues(List<ReportQuickFilterInfo> filtersUsed,
			PreparedStatement statement) throws SQLException {
		int i = 0;
		// Fill in filters
		for (ReportQuickFilterInfo filter : filtersUsed) {
			i++;
			// filter numbers as text, unless we're doing a > or < comparison
			// This is so part filters will match as you type
			QuickFilterType filterType = filter.getFilterType();
			DatabaseFieldType dbFieldType = filter.getFilterField().getDbType();
			if (filterType.equals(QuickFilterType.GREATER_THAN)
					|| filterType.equals(QuickFilterType.LESS_THAN)) {
				if (dbFieldType.equals(DatabaseFieldType.FLOAT)) {
					String value = filter.getFilterValue();
					statement.setDouble(i, Double.valueOf(value));
				} else if (dbFieldType.equals(DatabaseFieldType.INTEGER)
						|| dbFieldType.equals(DatabaseFieldType.SERIAL)) {
					String value = filter.getFilterValue();
					statement.setInt(i, Integer.valueOf(value));
				} else if (dbFieldType.equals(DatabaseFieldType.TIMESTAMP)) {
					// By this time, a date/time filter such as 'today' will
					// have been turned into two filters using > and <, see
					// preprocessDateFilter, so will always be processed here
					String filterValue = filter.getFilterValue();
					if (filterValue.endsWith("%")) {
						filterValue = filterValue.substring(0, filterValue.length() - 1);
					}
					Span timespan = this.parseTimestamp(filterValue);
					if (timespan == null) {
						statement.setString(i, filter.getFilterValue());
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
					statement.setString(i, filter.getFilterValue());
				}
			} else {
				statement.setString(i, filter.getFilterValue());
			}
		}
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
	private String preprocessDateFilter(String filterValue) throws CantDoThatException {
		String processedFilterValue = filterValue;
		String[] tokens = filterValue.split("\\sand\\s|\\sor\\s");
		Options options = new Options(false);
		for (String token : tokens) {
			if (!(token.startsWith(">") || token.startsWith("<"))) {
				Span timespan = this.parseTimestamp(token);
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
			Map<BaseField, String> filterValues, boolean exactFilters) throws CantDoThatException {
		StringBuilder filterArguments = new StringBuilder();
		Set<BaseField> reportBaseFields = this.report.getReportBaseFields();
		List<ReportQuickFilterInfo> filtersUsed = new LinkedList<ReportQuickFilterInfo>();
		for (Map.Entry<BaseField, String> filterValueEntry : filterValues.entrySet()) {
			// generate the filter for a field
			BaseField filterField = filterValueEntry.getKey();
			// Only apply filter if the field is actually in the report
			if (!reportBaseFields.contains(filterField)) {
				continue;
			}
			String filterValue = filterValueEntry.getValue();
			if (filterValue == null) {
				throw new CantDoThatException("Filter value for " + filterField + " is null");
			}
			filterValue = filterValue.toLowerCase();
			DatabaseFieldType filterFieldDbType = filterField.getDbType();
			// Don't bother filtering if the user's started typing a
			// filter type but hasn't typed any of the actual filter value
			if ((filterValue.equals(QuickFilterType.NOT_LIKE.getUserRepresentation()))
					|| (filterValue.equals(QuickFilterType.GREATER_THAN.getUserRepresentation()))
					|| (filterValue.equals(QuickFilterType.LESS_THAN.getUserRepresentation()))) {
				continue;
			}
			// remove commas when filtering a number field
			if (filterFieldDbType.equals(DatabaseFieldType.INTEGER)
					|| filterFieldDbType.equals(DatabaseFieldType.FLOAT)
					|| filterFieldDbType.equals(DatabaseFieldType.SERIAL)) {
				filterValue = filterValue.replaceAll(",", "");
			} else if (filterFieldDbType.equals(DatabaseFieldType.TIMESTAMP)) {
				filterValue = this.preprocessDateFilter(filterValue);
			}
			// OR/AND filtering of values (restricted to a given field)
			String[] orFilterParts = filterValue.split(QuickFilterType.OR.getUserRepresentation()
					.toLowerCase());
			String filterStringForField = "";
			for (String orFilterPartValue : orFilterParts) {
				String[] andFilterParts = orFilterPartValue.split(QuickFilterType.AND
						.getUserRepresentation().toLowerCase());
				if (andFilterParts.length > 1) {
					for (String andFilterPartValue : andFilterParts) {
						// Generate sub-part filter string for field:
						filterStringForField += "("
								+ this.generateFilterStringForField(filterField,
										andFilterPartValue, filtersUsed, exactFilters) + ")";
						filterStringForField += QuickFilterType.AND.getSqlRepresentation();
					}
					// remove trailing AND and spaces
					int charsToRemove = QuickFilterType.AND.getSqlRepresentation().length();
					filterStringForField = filterStringForField.substring(0, filterStringForField
							.length()
							- charsToRemove);
					filterStringForField = "(" + filterStringForField + ")";
				} else {
					// Generate sub-part filter string for field:
					filterStringForField += "("
							+ this.generateFilterStringForField(filterField, orFilterPartValue,
									filtersUsed, exactFilters) + ")";
				}
				filterStringForField += QuickFilterType.OR.getSqlRepresentation();
			}
			if (filterStringForField.length() > 0) {
				// remove trailing OR and spaces
				int charsToRemove = QuickFilterType.OR.getSqlRepresentation().length();
				filterStringForField = filterStringForField.substring(0, filterStringForField
						.length()
						- charsToRemove);
				filterStringForField = "(" + filterStringForField + ")";
				filterArguments.append(filterStringForField + " AND ");
			}
		}
		// check that filter arguments were supplied
		String filterArgs = "";
		if (filterArguments.length() > 0) {
			// remove trailing AND
			if (filterArguments.toString().endsWith(" AND ")) {
				filterArgs = filterArguments.substring(0, filterArguments.length() - 5).trim();
			}
		}
		Map<String, List<ReportQuickFilterInfo>> whereClause = new HashMap<String, List<ReportQuickFilterInfo>>();
		whereClause.put(filterArgs, filtersUsed);
		return whereClause;
	}

	private Map<String, String> getKeyToDisplayMapping(Connection conn, String internalSourceName,
			String internalKeyFieldName, String internalDisplayFieldName) throws SQLException {
		// Buffer the set of display values for this field:
		// Note: don't need to cache these as relation fields shouldn't be used
		// in general reports, just in table default reports
		String SQLCode = "SELECT " + internalKeyFieldName + ", " + internalDisplayFieldName;
		SQLCode += " FROM " + internalSourceName;
		PreparedStatement statement = conn.prepareStatement(SQLCode);
		ResultSet results = statement.executeQuery();
		Map<String, String> displayLookup = new HashMap<String, String>();
		while (results.next()) {
			displayLookup.put(results.getString(internalKeyFieldName), results
					.getString(internalDisplayFieldName));
		}
		return displayLookup;
	}

	public List<DataRowInfo> getReportDataRows(Connection conn,
			Map<BaseField, String> filterValues, boolean exactFilters,
			Map<BaseField, Boolean> reportSorts, int rowLimit) throws SQLException,
			CodingErrorException, CantDoThatException {
		List<DataRowInfo> reportData = null;
		// 0) Obtain all display values taken from other sources:
		Map<BaseField, Map<String, String>> displayLookups = new HashMap<BaseField, Map<String, String>>();
		for (ReportFieldInfo reportField : this.report.getReportFields()) {
			BaseField fieldSchema = reportField.getBaseField();
			if (fieldSchema instanceof RelationField) {
				// Buffer the set of display values for this field:
				RelationField relationField = (RelationField) fieldSchema;
				String relatedKey = relationField.getRelatedField().getInternalFieldName();
				String relatedDisplay = relationField.getDisplayField().getInternalFieldName();
				String relatedSource = relationField.getRelatedTable().getInternalTableName();
				Map<String, String> displayLookup = this.getKeyToDisplayMapping(conn,
						relatedSource, relatedKey, relatedDisplay);
				displayLookups.put(relationField.getRelatedField(), displayLookup);
			}
		}
		PreparedStatement statement = this.getReportSqlPreparedStatement(conn, filterValues,
				exactFilters, reportSorts, rowLimit, null);
		long executionStartTime = System.currentTimeMillis();
		ResultSet results = statement.executeQuery();
		float durationSecs = (System.currentTimeMillis() - executionStartTime) / ((float) 1000);
		if (durationSecs > AppProperties.longSqlTime) {
			logger.warn("Long SELECT SQL execution time of " + durationSecs
					+ " seconds for report " + this.report + ". Filters = " + filterValues
					+ ", sorts = " + reportSorts + ", exact filters = " + exactFilters
					+ ", statement = " + statement);
		}
		// 2) parse the SQL resultset to generate a return value:
		int initialCapacity = rowLimit;
		if (initialCapacity > 10000 || initialCapacity < 0) {
			initialCapacity = 10000;
		}
		reportData = new ArrayList<DataRowInfo>(initialCapacity);
		DataRow reportDataRow;
		DataRowField reportDataRowField;
		TableInfo parentTable = this.report.getParentTable();
		BaseField primaryKeyField = parentTable.getPrimaryKey();
		while (results.next()) {
			int rowid = results.getInt(primaryKeyField.getInternalFieldName());
			reportDataRow = new DataRow(parentTable, rowid);
			// add all columns to the row:
			for (ReportFieldInfo reportField : this.report.getReportFields()) {
				BaseField fieldSchema = reportField.getBaseField();
				String colourRepresentation = null;
				String keyValue = "";
				String displayValue = "";
				// If cell should be coloured, calculate colour hex string
				boolean fieldShouldBeColoured = this.cachedFieldStats.containsKey(reportField);
				String colourableFieldInternalName = reportField.getInternalFieldName();
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
					double numberOfStdDevsFromMean = 0d;
					double stdDev = fieldStats.getStdDev();
					if (stdDev != 0d) {
						numberOfStdDevsFromMean = (fieldValue - fieldStats.getMean()) / stdDev;
					}
					int colourVal = (int) (numberOfStdDevsFromMean * colourScalingFactor);
					int absColourVal = Math.abs(colourVal);
					if (absColourVal > 255) {
						absColourVal = 255;
					}
					colourRepresentation = Integer.toHexString(255 - absColourVal);
					if (colourRepresentation.length() == 1) {
						colourRepresentation = "0" + colourRepresentation;
					}
					// +ve = green shade, -ve = red
					if (colourVal > 0) {
						colourRepresentation = "#" + colourRepresentation + "ff"
								+ colourRepresentation;
					} else {
						colourRepresentation = "#ff" + colourRepresentation + colourRepresentation;
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
								// See DateFieldDefn constructor for format
								// explanation
								keyValue = ((ReportCalcFieldInfo) reportField).formatDate(dbValue);
							} else {
								keyValue = ((DateField) fieldSchema).formatDate(dbValue);
							}
						}
						displayValue = keyValue;
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
							displayValue = displayLookups.get(relationField.getRelatedField()).get(
									keyValue);
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
									// See DateFieldDefn constructor for format
									// explanation
									keyValue = ((ReportCalcFieldInfo) reportField)
											.formatDate(dbValue);
								} else {
									keyValue = ((DateField) fieldSchema).formatDate(dbValue);
								}
							}
							displayValue = keyValue;
						} else if (fieldSchema.getDbType().equals(DatabaseFieldType.FLOAT)) {
							double dbValue = results.getDouble(fieldSchema.getInternalFieldName());
							if (results.wasNull()) {
								keyValue = "";
							} else if (reportField instanceof ReportCalcFieldInfo) {
								keyValue = ((ReportCalcFieldInfo) reportField).formatFloat(dbValue);
							} else {
								keyValue = ((DecimalField) fieldSchema).formatFloat(dbValue);
							}
							displayValue = keyValue;
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
							displayValue = keyValue;
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
							displayValue = keyValue;
						} else {
							keyValue = results.getString(fieldSchema.getInternalFieldName());
							if (keyValue == null) {
								keyValue = "";
							}
							displayValue = keyValue;
							if (fieldSchema instanceof TextField) {
								TextField fieldSchemaText = (TextField) fieldSchema;
								// Override with 'not applicable' string if
								// necessary
								if (fieldSchemaText.allowNotApplicable()) {
									if (fieldSchemaText.getNotApplicableValue().equals(keyValue)) {
										displayValue = fieldSchemaText
												.getNotApplicableDescription();
									}
								} else {
									int textFieldSize = fieldSchemaText.getContentSize();
									if (displayValue.length() > (textFieldSize + 1)) {
										displayValue = displayValue.substring(0, textFieldSize)
												+ "&hellip;";
									}
								}
							}
						}
					}
				}
				// keyValue, displayValue and colourRepresentation (if
				// appropriate) should all be calculated now
				if (colourRepresentation == null) {
					reportDataRowField = new DataRowField(keyValue, displayValue);
				} else {
					reportDataRowField = new DataRowField(keyValue, keyValue, colourRepresentation);
				}
				// store in ReportDataRow object:
				reportDataRow.addDataRowField(fieldSchema, reportDataRowField);
			}
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
		// because
		// the data is less likely to change
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

	/**
	 * Definition of the report which we are using to return data
	 */
	private BaseReportInfo report;

	/**
	 * Set cache creation time to the creation time of the object
	 */
	private final long cacheCreationTime = (new Date()).getTime();

	/**
	 * Constructor records how long it takes to generate mean and std. dev. for
	 * number fields
	 */
	private long millisecsTakenToGenerateStats = 0;

	/**
	 * Avg. / std. dev. calcs used for field colouring
	 */
	private Map<ReportFieldInfo, ReportDataFieldStatsInfo> cachedFieldStats = new HashMap<ReportFieldInfo, ReportDataFieldStatsInfo>();

	/**
	 * Increase this number to make field colours brighter, reduce to make more
	 * pastel. A value of 25 means the brightest green will be about 10 standard
	 * deviations away from the mean and the brightest red about 10 the other
	 * way
	 */
	private static final int colourScalingFactor = 25;

	private static final SimpleLogger logger = new SimpleLogger(ReportData.class);
}