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
package com.gtwm.pb.model.manageSchema;

import com.gtwm.pb.model.interfaces.ReportFilterInfo;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.SimpleReportInfo;
import com.gtwm.pb.model.interfaces.ReportFieldInfo;
import com.gtwm.pb.model.interfaces.ReportCalcFieldInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.fields.RelationField;
import com.gtwm.pb.model.interfaces.fields.CalculationField;
import com.gtwm.pb.model.manageSchema.fields.AbstractField;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.Helpers;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.RandomString;
import com.gtwm.pb.util.Enumerations.FilterType;
import java.util.Locale;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import org.grlea.log.SimpleLogger;
import org.hibernate.annotations.CollectionOfElements;
import com.gtwm.pb.util.Enumerations.DatabaseFieldType;

@Entity
public class ReportFilterDefn implements ReportFilterInfo {

	protected ReportFilterDefn() {
	}

	/**
	 * Constructs a filter on a set of values, e.g. for a dropdown field. If the
	 * set of values contains only one value, then a single value filter will be
	 * constructed.
	 * 
	 * This is the method to use if you want to construct a simple single-value
	 * filter
	 */
	public ReportFilterDefn(SimpleReportInfo parentReport,
			BaseReportInfo reportContainingFilterField, BaseField filterField, String filterType,
			Set<String> filterValues) throws ObjectNotFoundException {
		this.setParentReport(parentReport);
		if (reportContainingFilterField != null) {
			this.setFilterReportFieldDirect(reportContainingFilterField
					.getReportField(filterField.getInternalFieldName()));
		} else {
			this.setFilterBaseField(filterField);
		}
		this.setFilterTypeDirect(FilterType.valueOf(filterType.toUpperCase()));
		for (String filterValue : filterValues) {
			this.getFilterValuesDirect().add(filterValue);
		}
		this.setInternalName((new RandomString()).toString());
	}

	/**
	 * Constructs a filter which can use fields or values in the filter value,
	 * e.g. a subselect filter given a user-input WHERE clause (custom
	 * subselect)
	 * 
	 * @param reportContainingFilterField
	 *            The report the field you're filtering on is from. e.g. in
	 *            SELECT myreport.field from myreport join another,
	 *            reportContainingFilterField would be myreport. If null, field
	 *            is from a table
	 * @param parentReport
	 *            Report the filter is in, e.g. in CREATE REPORT a AS SELECT
	 *            myreport.field from myreport, parentReport would be a.
	 * @param availableDataStores
	 *            A set of tables and reports which are available to use in the
	 *            filter value. Only tables and reports which the current user
	 *            has view privileges on should be passed in
	 */
	public ReportFilterDefn(SimpleReportInfo parentReport,
			BaseReportInfo reportContainingFilterField, BaseField filterField, String filterType,
			String filterValue, Map<TableInfo, Set<BaseReportInfo>> availableDataStores)
			throws CantDoThatException, ObjectNotFoundException {
		this.setParentReport(parentReport);
		if (reportContainingFilterField != null) {
			this.setFilterReportFieldDirect(reportContainingFilterField.getReportField(filterField.getInternalFieldName()));
		} else {
			this.setFilterBaseField(filterField);
		}
		this.setFilterTypeDirect(FilterType.valueOf(filterType.toUpperCase(Locale.UK)));
		this.setInternalName((new RandomString()).toString());
		if (filterValue != null) {
			Helpers.checkForSQLInjection(filterValue);
			this.setFilterValueDirect(filterValue.toLowerCase());
			// replace any tables, reports and fields in the string with their
			// internal identifiers.
			// This is similar to the way replacements work in calculations
			if (this.getFilterValueDirect().contains("{")) {
				this.setReferencesFields(true);
				String identifierToReplace = null;
				String replacement = null;
				// replace {table.field} with
				// internaltablename.internalfieldname
				for (TableInfo table : availableDataStores.keySet()) {
					for (BaseField field : table.getFields()) {
						String tableName = table.getTableName().replaceAll("(\\W)", "\\\\$1");
						String fieldName = field.getFieldName().replaceAll("(\\W)", "\\\\$1");
						identifierToReplace = "\\{" + tableName + "." + fieldName + "\\}";
						replacement = table.getInternalTableName() + "."
								+ field.getInternalFieldName();
						this.setFilterValueDirect(this.getFilterValueDirect().replaceAll(
								identifierToReplace.toLowerCase(Locale.UK), replacement));
					}
				}
				// replace {report.field} with
				// internalreportname.internalfieldname
				for (Map.Entry<TableInfo, Set<BaseReportInfo>> availableDataStoreEntry : availableDataStores
						.entrySet()) {
					for (BaseReportInfo report : availableDataStoreEntry.getValue()) {
						for (BaseField field : report.getReportBaseFields()) {
							String reportName = report.getReportName()
									.replaceAll("(\\W)", "\\\\$1");
							String fieldName = field.getFieldName().replaceAll("(\\W)", "\\\\$1");
							identifierToReplace = "\\{" + reportName + "." + fieldName + "\\}";
							replacement = report.getInternalReportName() + "."
									+ field.getInternalFieldName();
							this.setFilterValueDirect(this.getFilterValueDirect().replaceAll(
									identifierToReplace.toLowerCase(Locale.UK), replacement));
						}
					}
				}
				// replace {table} with internaltablename
				for (TableInfo table : availableDataStores.keySet()) {
					String tableName = table.getTableName().replaceAll("(\\W)", "\\\\$1");
					identifierToReplace = "\\{" + tableName + "\\}";
					replacement = table.getInternalTableName();
					this.setFilterValueDirect(this.getFilterValueDirect().replaceAll(
							identifierToReplace.toLowerCase(Locale.UK), replacement));
				}
				// replace {report} with internalreportname
				for (Map.Entry<TableInfo, Set<BaseReportInfo>> availableDataStoreEntry : availableDataStores
						.entrySet()) {
					for (BaseReportInfo report : availableDataStoreEntry.getValue()) {
						String reportName = report.getReportName().replaceAll("(\\W)", "\\\\$1");
						identifierToReplace = "\\{" + reportName + "\\}";
						replacement = report.getInternalReportName();
						this.setFilterValueDirect(this.getFilterValueDirect().replaceAll(
								identifierToReplace.toLowerCase(Locale.UK), replacement));
					}
				}
			}
			// if there are still un-recognised items, throw exception
			if (this.getFilterValueDirect().contains("{")) {
				throw new CantDoThatException("Unable to parse filter value " + filterValue);
			}
		}
	}

	@ManyToOne(targetEntity = SimpleReportDefn.class)
	// Other side of report.getReportFields()
	public SimpleReportInfo getParentReport() {
		return this.parentReport;
	}

	private void setParentReport(SimpleReportInfo parentReport) {
		this.parentReport = parentReport;
	}

	@Transient
	public boolean isFilterFieldFromReport() {
		return (this.getFilterReportFieldDirect() != null);
	}

	public String toString() {
		String filterFieldRepresentation;
		if (this.isFilterFieldFromReport()) {
			filterFieldRepresentation = this.getFilterReportFieldDirect().getParentReport() + "."
					+ this.getFilterReportFieldDirect();
		} else {
			filterFieldRepresentation = this.getFilterBaseField().getTableContainingField() + "."
					+ this.getFilterBaseField();
		}
		return filterFieldRepresentation + " " + this.getFilterType() + " "
				+ this.getFilterValueDirect() + this.getFilterValuesDirect();
	}

	private static String makeSafeSQL(String sqlString) {
		// Replace any single quotes with two single quotes
		// However, this may not be database independent
		// Also, do we need to do anything more to make it SQL injection safe?
		return sqlString.replaceAll("\\'", "''");
	}

	@Transient
	// TODO: Some more checks for invalid filterValue input
	public String getFilterSQL() throws CantDoThatException, CodingErrorException,
			ObjectNotFoundException {
		FilterType filterType = this.getFilterTypeDirect();
		BaseField filterField = null;
		ReportFieldInfo filterReportField = null;
		if (this.isFilterFieldFromReport()) {
			filterReportField = this.getFilterReportField();
			filterField = filterReportField.getBaseField();
		} else {
			filterField = this.getFilterBaseField();
		}
		// generate the string identifier for the field to perform a comparison
		// on:
		BaseField comparisonField;
		String comparisonFieldString;
		// TODO: This logic together with code later in the method is quite
		// complex and needs looking at
		if (filterType.equals(FilterType.IS_NULL) || (filterType.equals(FilterType.IS_NOT_NULL))) {
			comparisonField = filterField;
		} else if (filterField instanceof RelationField) {
			comparisonField = ((RelationField) filterField).getDisplayField();
		} else {
			comparisonField = filterField;
		}
		// In SQL, can't reference calculations in the current report by name,
		// if this is the case we have to
		// redo the calculation
		if (comparisonField instanceof CalculationField) {
			ReportCalcFieldInfo reportCalcField = (ReportCalcFieldInfo) filterReportField;
			if (reportCalcField.getParentReport().equals(this.getParentReport())) {
				comparisonFieldString = "(" + reportCalcField.getCalculationSQL(false) + ")";
			} else {
				// comparisonFieldString =
				// calculationField.getReportContainingCalculation().getInternalReportName()
				// + "."
				// + calculationField.getInternalFieldName();
				comparisonFieldString = filterReportField.getParentReport().getInternalReportName()
						+ "." + filterField.getInternalFieldName();
			}
		} else {
			if (!this.isFilterFieldFromReport()) {
				// source field from table:
				comparisonFieldString = comparisonField.getTableContainingField()
						.getInternalTableName();
			} else {
				// if field from default report, source from table instead
				// unless field is a dropdown or relation in which case display
				// value will be different to key
				// stored in db
				TableInfo reportParentTable = filterReportField.getParentReport().getParentTable();
				if (reportParentTable.getDefaultReport()
						.equals(filterReportField.getParentReport())
						&& (comparisonField.equals(filterField))) {
					comparisonFieldString = comparisonField.getTableContainingField()
							.getInternalTableName();
				} else {
					// source field from report:
					comparisonFieldString = filterReportField.getParentReport()
							.getInternalReportName();
				}
			}
			comparisonFieldString += "." + comparisonField.getInternalFieldName();
		}
		// make any text comparison case insensitive:
		if (comparisonField.getDbType().equals(DatabaseFieldType.VARCHAR)) {
			if (!filterType.equals(FilterType.IS_NOT_NULL)
					&& !filterType.equals(FilterType.IS_NULL)) {
				comparisonFieldString = "lower(" + comparisonFieldString + ")";
			}
		}
		// ready the filterValue (not strictly necessary for all filter types):
		String filterValue = "";
		if (this.getFilterValueDirect() != null) {
			// replace ' with '' only if filter doesn't reference fields. This
			// is so values like "{field1} +
			// '1 day'::interval" will work
			// TODO: SECURITY RISK. SQL injection attack possible without
			// replacing single quotes
			if ((!filterType.isDateComparison()) && (!this.getReferencesFields())) {
				filterValue = makeSafeSQL(this.getFilterValueDirect());
			} else {
				filterValue = this.getFilterValueDirect();
			}
			if (comparisonField.getDbType().equals(DatabaseFieldType.VARCHAR)) {
				if (filterType.equals(FilterType.STARTS_WITH) || filterType.equals(FilterType.DOES_NOT_START_WITH)) {
					filterValue = "'" + filterValue.toLowerCase(Locale.UK) + "%'";
				} else if (!this.getReferencesFields()) {
					filterValue = "'" + filterValue.toLowerCase(Locale.UK) + "'";
				} else {
					filterValue = "lower(" + filterValue.toLowerCase(Locale.UK) + ")";
				}
			} else if (comparisonField.getDbType().equals(DatabaseFieldType.TIMESTAMP)) {
				if ((!filterType.isDateComparison()) && (!this.getReferencesFields())) {
					filterValue = "'" + filterValue + "'::timestamp";
				}
			}
		}
		// build the sql comparison (e.g. "< 5", " like 'smith%'", etc)
		String comparisonString = null;
		switch (filterType) {
		case EQUAL:
			comparisonString = " = " + filterValue;
			break;
		case NOT_EQUAL_TO:
			comparisonString = " != " + filterValue;
			break;
		case GREATER_THAN_OR_EQUAL_TO:
			comparisonString = " >= " + filterValue;
			break;
//		case IS_IN_SUBSELECT:
//			comparisonString = " IN ";
//			comparisonString += this.getFilterSubSelect().getSQLForDetail();
//			comparisonString += ")";
//			break;
//		case IS_NOT_IN_SUBSELECT:
//			comparisonString = " NOT IN ";
//			comparisonString += this.getFilterSubSelect().getSQLForDetail();
//			comparisonString += ")";
//			break;
		case IS_NULL:
			comparisonString = " IS NULL";
			break;
		case IS_ONE_OF:
			comparisonString = " IN (";
			for (String afilterValue : this.getFilterValuesDirect()) {
				afilterValue = makeSafeSQL(afilterValue);
				afilterValue = afilterValue.toLowerCase(Locale.UK);
				comparisonString += "'" + afilterValue + "', ";
			}
			if (comparisonString
					.substring(comparisonString.length() - 2, comparisonString.length()).equals(
							", ")) {
				comparisonString = comparisonString.substring(0, comparisonString.length() - 2);
			}
			comparisonString += ")";
			break;
		case LESS_THAN:
			comparisonString = " < " + filterValue;
			break;
		case NEWER_THAN_IN_DAYS:
			comparisonString = " >= (date_trunc('day',now()) - '@" + Integer.valueOf(filterValue)
					+ " days'::interval)";
			break;
		case NEWER_THAN_IN_WEEKS:
			comparisonString = " >= (date_trunc('week',now()) - '@" + Integer.valueOf(filterValue)
					+ " weeks'::interval)";
			break;
		case NEWER_THAN_IN_MONTHS:
			comparisonString = " >= (date_trunc('month',now()) - '@" + Integer.valueOf(filterValue)
					+ " months'::interval)";
			break;
		case NEWER_THAN_IN_YEARS:
			comparisonString = " >= (date_trunc('year',now()) - '@" + Integer.valueOf(filterValue)
					+ " years'::interval)";
			break;
		case IS_NOT_NULL:
			comparisonString = " IS NOT NULL";
			break;
		case OLDER_THAN_IN_DAYS:
			comparisonString = " < (date_trunc('day',now()) - '@" + Integer.valueOf(filterValue)
					+ " days'::interval)";
			break;
		case OLDER_THAN_IN_WEEKS:
			comparisonString = " < (date_trunc('week',now()) - '@" + Integer.valueOf(filterValue)
					+ " weeks'::interval)";
			break;
		case OLDER_THAN_IN_MONTHS:
			comparisonString = " < (date_trunc('month',now()) - '@" + Integer.valueOf(filterValue)
					+ " months'::interval)";
			break;
		case OLDER_THAN_IN_YEARS:
			comparisonString = " < (date_trunc('year',now()) - '@" + Integer.valueOf(filterValue)
					+ " years'::interval)";
			break;
		case STARTS_WITH:
			comparisonString = " LIKE " + filterValue;
			break;
		case DOES_NOT_START_WITH:
			comparisonString = " NOT LIKE " + filterValue;
			break;
		default:
			throw new CodingErrorException("unrecognised filter type '"
					+ filterType.getDescription() + "'");
		}
		// now generate the full filter sql
		// (made a little more complicated as relations / generatedDropdowns
		// should be
		// filtered on their displayed value rather than the internally stored
		// value)
		String filterSQL = "";
		if (filterType.equals(FilterType.IS_NULL) || filterType.equals(FilterType.IS_NOT_NULL)) {
			filterSQL = comparisonFieldString + comparisonString;
		} else if (filterField instanceof RelationField) {
			RelationField relationField = (RelationField) filterField;
			String filterFieldIdentifier;
			if (this.isFilterFieldFromReport()) {
				filterFieldIdentifier = this.getFilterReportField().getParentReport()
						.getInternalReportName();
			} else {
				filterFieldIdentifier = relationField.getTableContainingField()
						.getInternalTableName();
			}
			filterFieldIdentifier += "." + relationField.getInternalFieldName();
			String relatedTableInternalName = relationField.getRelatedTable()
					.getInternalTableName();
			String relatedFieldInternalName = relationField.getRelatedField()
					.getInternalFieldName();
			filterSQL = filterFieldIdentifier + " IN (SELECT " + relatedFieldInternalName;
			filterSQL += " FROM " + relatedTableInternalName + " WHERE ";
			filterSQL += comparisonFieldString + comparisonString + ")";
		} else {
			filterSQL = comparisonFieldString + comparisonString;
		}
		return filterSQL;
	}

	@Transient
	public String getFilterDescription() throws CodingErrorException {
		// Create a plain English description rather than just returning the SQL
		// Start with the field name
		String filterValue = this.getFilterValueDirect();
		BaseField filterField = this.getFilterBaseField();
		String filterDescription = filterField.getFieldName() + " from the '"
				+ filterField.getTableContainingField().getTableName() + "' data store";
		switch (this.getFilterTypeDirect()) {
		case LESS_THAN:
			filterDescription += " less than " + filterValue;
			break;
		case GREATER_THAN_OR_EQUAL_TO:
			filterDescription += " greater than or equal to" + filterValue;
			break;
		case EQUAL:
			filterDescription += " = " + filterValue;
			break;
		case STARTS_WITH:
			filterDescription += " starts with '" + filterValue + "'";
			break;
		case DOES_NOT_START_WITH:
			filterDescription += " doesn't start with '" + filterValue + "'";
			break;
		case IS_ONE_OF:
			filterDescription += " is one of";
			for (String value : this.getFilterValuesDirect()) {
				// findbugs complains that 'Method concatenates strings using +
				// in a loop' but since there
				// are only going to be one or two filter values in
				// this.filtervalues this really isn't a
				// problem
				filterDescription += " '" + value + "'";
			}
			break;
		case OLDER_THAN_IN_DAYS:
			filterDescription += " is over " + filterValue + " days old";
			break;
		case NEWER_THAN_IN_DAYS:
			filterDescription += " is under " + filterValue + " days old";
			break;
		case OLDER_THAN_IN_WEEKS:
			filterDescription += " is over " + filterValue + " weeks old";
			break;
		case NEWER_THAN_IN_WEEKS:
			filterDescription += " is under " + filterValue + " weeks old";
			break;
		case OLDER_THAN_IN_MONTHS:
			filterDescription += " is over " + filterValue + " months old";
			break;
		case NEWER_THAN_IN_MONTHS:
			filterDescription += " is under " + filterValue + " months old";
			break;
		case OLDER_THAN_IN_YEARS:
			filterDescription += " is over " + filterValue + " years old";
			break;
		case NEWER_THAN_IN_YEARS:
			filterDescription += " is under " + filterValue + " years old";
			break;
		case IS_NULL:
			filterDescription += " has no value";
			break;
		case IS_NOT_NULL:
			filterDescription += " has a value";
			break;
		default:
			throw new CodingErrorException("Unrecognised filter type " + this.getFilterType());
		}
		return filterDescription;
	}

	@ManyToOne(targetEntity = AbstractField.class)
	// Uni-directional many to one
	public BaseField getFilterBaseField() {
		return this.filterField;
	}

	private void setFilterBaseField(BaseField filterField) {
		this.filterField = filterField;
	}

	@Transient
	public ReportFieldInfo getFilterReportField() throws CantDoThatException {
		if (!this.isFilterFieldFromReport()) {
			throw new CantDoThatException("Can't get filter field " + this.getFilterBaseField()
					+ " as a report field because it's from a table not a report");
		}
		return this.getFilterReportFieldDirect();
	}

	@ManyToOne(targetEntity = AbstractReportField.class)
	// Uni-directional many to one
	private ReportFieldInfo getFilterReportFieldDirect() {
		return this.filterReportField;
	}

	private void setFilterReportFieldDirect(ReportFieldInfo filterReportField) {
		this.filterReportField = filterReportField;
	}

	@Transient
	public FilterType getFilterType() {
		return this.getFilterTypeDirect();
	}

	@Enumerated(EnumType.STRING)
	private FilterType getFilterTypeDirect() {
		return this.filterType;
	}

	private void setFilterTypeDirect(FilterType filterType) {
		this.filterType = filterType;
	}

	@Transient
	public Set<String> getFilterValues() {
		if (this.getFilterValuesDirect().size() > 0) {
			return this.getFilterValuesDirect();
		} else {
			Set<String> oneFilterValue = new HashSet<String>(1);
			oneFilterValue.add(this.getFilterValueDirect());
			return oneFilterValue;
		}
	}

	// @CollectionOfElements must be used instead of @OneToMany for a collection
	// of Java core types
	@CollectionOfElements(fetch = FetchType.EAGER)
	private Set<String> getFilterValuesDirect() {
		return this.filterValues;
	}

	private void setFilterValuesDirect(Set<String> filterValues) {
		this.filterValues = filterValues;
	}

	private String getFilterValueDirect() {
		return this.filterValue;
	}

	private void setFilterValueDirect(String filterValue) {
		this.filterValue = filterValue;
	}

	@Id
	public String getInternalName() {
		return this.internalName;
	}

	private void setInternalName(String internalName) {
		this.internalName = internalName;
	}

//	private void setFilterSubSelect(BaseReportInfo filterSubSelect) {
//		this.filterSubSelect = filterSubSelect;
//	}
//
//	@ManyToOne(targetEntity = BaseReportDefn.class)
//	private BaseReportInfo getFilterSubSelect() {
//		return this.filterSubSelect;
//	}

	private void setReferencesFields(Boolean referencesFields) {
		this.referencesFields = referencesFields;
	}

	private Boolean getReferencesFields() {
		return this.referencesFields;
	}

	/**
	 * equals() and hashCode() are based on the parent report and the filter SQL
	 * returned by getFilterSQL()
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		ReportFilterInfo otherReportFilter = (ReportFilterInfo) obj;
		boolean parentReportEquality = this.getParentReport().equals(
				otherReportFilter.getParentReport());
		if (!parentReportEquality) {
			return false;
		}
		try {
			return getFilterSQL().equals(otherReportFilter.getFilterSQL());
		} catch (CantDoThatException cdtex) {
			logger.error("Error comparing filter objects: " + cdtex.toString());
			return false;
		} catch (CodingErrorException ceex) {
			logger.error("Error comparing filter objects: " + ceex.toString());
			return false;
		} catch (ObjectNotFoundException onfex) {
			logger.error("Error comparing filter objects: " + onfex.toString());
			return false;
		}
	}

	public int hashCode() {
		int result = 17;
		result = 37 * result + this.getParentReport().hashCode();
		try {
			result = 37 * result + this.getFilterSQL().hashCode();
			return result;
		} catch (CantDoThatException cdtex) {
			logger.error("Error generating hashCode from getFilterSQL(): " + cdtex.toString());
			return 0;
		} catch (CodingErrorException ceex) {
			logger.error("Error generating hashCode from getFilterSQL(): " + ceex.toString());
			return 0;
		} catch (ObjectNotFoundException onfex) {
			logger.error("Error generating hashCode from getFilterSQL(): " + onfex.toString());
			return 0;
		}
	}

	/**
	 * The field being filtered
	 */
	private BaseField filterField;

	/**
	 * The field being filtered, in ReportFieldInfo format. Only used if the
	 * field is from a report, not a table
	 */
	private ReportFieldInfo filterReportField;

	/**
	 * Report the filter is in, e.g. in CREATE REPORT a AS SELECT myreport.field
	 * from myreport, parentReport would be a.
	 */
	private SimpleReportInfo parentReport = null;

	private FilterType filterType;

	private String filterValue = null;

	private Set<String> filterValues = new HashSet<String>();

	private String internalName = null;

	/**
	 * Whether the filter is on a constant user input value, such as 'Hello' or
	 * uses references to fields, e.g. {a1.lsurname}
	 */
	private Boolean referencesFields = false;

	private static final SimpleLogger logger = new SimpleLogger(ReportFilterDefn.class);
}
