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
package com.gtwm.pb.model.manageSchema;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import org.grlea.log.SimpleLogger;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.fields.CalculationField;
import com.gtwm.pb.model.interfaces.fields.DateField;
import com.gtwm.pb.model.interfaces.fields.DecimalField;
import com.gtwm.pb.model.interfaces.fields.SeparatorField;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.ReportCalcFieldInfo;
import com.gtwm.pb.model.interfaces.ReportFieldInfo;
import com.gtwm.pb.model.interfaces.SimpleReportInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.manageSchema.fields.CalculationFieldDefn;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.Helpers;
import com.gtwm.pb.util.RandomString;
import com.gtwm.pb.util.Enumerations.DatabaseFieldType;

@Entity
public class ReportCalcFieldDefn extends AbstractReportField implements ReportCalcFieldInfo {

	protected ReportCalcFieldDefn() {
	}

	/**
	 * @param parentReport
	 *            The report the calculation should be part of
	 * @param fieldName
	 *            The user input field name of the calculation
	 * @param calculationDefn
	 *            The user input (un-parsed) calculation string, e.g.
	 *            "{table.field} * 2"
	 * @param dbFieldType
	 *            The return type
	 * @param availableDataStores
	 *            A list of tables & reports that the calculation can reference
	 */
	public ReportCalcFieldDefn(SimpleReportInfo parentReport, String internalFieldName,
			String fieldName, String calculationDefn, DatabaseFieldType dbFieldType,
			Map<TableInfo, Set<BaseReportInfo>> availableDataStores) throws CantDoThatException,
			CodingErrorException {
		super.setParentReport(parentReport);
		super.setReportFieldIsFromDirect(parentReport);
		if (internalFieldName == null) {
			this.setBaseFieldInternalFieldNameDirect((new RandomString()).toString());
		} else {
			this.setBaseFieldInternalFieldNameDirect(internalFieldName);
		}
		this.setBaseFieldNameDirect(fieldName.trim());
		this.setCalculationDefinitionDirect(calculationDefn);
		this.setDbTypeDirect(dbFieldType);
		this.setCalculationSQL(availableDataStores);
		if (dbFieldType.equals(DatabaseFieldType.TIMESTAMP)) {
			this.setDateResolution();
		} else if (dbFieldType.equals(DatabaseFieldType.FLOAT)) {
			this.setDecimalPrecision();
		}
	}

	/**
	 * This constructor takes a parameter calcToReference to allow you to
	 * construct a calc that is a reference to a calc from another report,
	 * instead of creating a new one from a calc. definition.
	 * 
	 * A calc constructed this way will return a reference to the original calc
	 * from getCalculationSQL() rather than the calc SQL itself and will return
	 * properties of the original calc when getInternalFieldName() etc. are
	 * called
	 */
	public ReportCalcFieldDefn(SimpleReportInfo parentReport, ReportCalcFieldInfo calcToReference)
			throws CodingErrorException {
		super.setParentReport(parentReport);
		super.setReportFieldIsFromDirect(calcToReference.getParentReport());
		this.setReferencedCalc(calcToReference);
		try {
			this.setCalculationSQL();
		} catch (CantDoThatException cdtex) {
			throw new CodingErrorException("Error constructing calc that references another calc",
					cdtex);
		}
	}

	@Transient
	public BaseField getBaseField() {
		// Create a new baseField referencing this report field the first time
		// the method is run
		if (this.baseField == null) {
			this.baseField = new CalculationFieldDefn(this);
		}
		return this.baseField;
	}

	@Transient
	public String getFieldName() {
		return this.getBaseField().getFieldName();
	}

	@Transient
	public String getFieldDescription() {
		return this.getBaseField().getFieldDescription();
	}

	@Transient
	public String getInternalFieldName() {
		return this.getBaseField().getInternalFieldName();
	}

	@Transient
	public DatabaseFieldType getDbType() {
		if (this.referencesCalcFromOtherReport()) {
			return this.getReferencedCalc().getDbType();
		} else {
			return this.getDbTypeDirect();
		}
	}

	@Enumerated(EnumType.STRING)
	private DatabaseFieldType getDbTypeDirect() {
		return this.dbFieldType;
	}

	private void setDbTypeDirect(DatabaseFieldType dbFieldType) {
		this.dbFieldType = dbFieldType;
	}

	public String formatFloat(double floatValue) throws CantDoThatException {
		if (this.referencesCalcFromOtherReport()) {
			return this.getReferencedCalc().formatFloat(floatValue);
		} else {
			if (!this.getDbType().equals(DatabaseFieldType.FLOAT)) {
				throw new CantDoThatException("This calculation returns a " + this.getDbType()
						+ " not a " + DatabaseFieldType.FLOAT);
			}
			return String.format(this.getJavaDecimalFormatString(), floatValue);
		}
	}

	public String formatInteger(int intValue) throws CantDoThatException {
		if (!this.getDbType().equals(DatabaseFieldType.INTEGER)) {
			throw new CantDoThatException("This calculation returns a " + this.getDbType()
					+ " not an " + DatabaseFieldType.INTEGER);
		}
		return String.format(Helpers.generateJavaDecimalFormat(0), intValue);
	}

	public String formatDate(Date dateValue) throws CantDoThatException {
		if (this.referencesCalcFromOtherReport()) {
			return this.getReferencedCalc().formatDate(dateValue);
		} else {
			if (!this.getDbType().equals(DatabaseFieldType.TIMESTAMP)) {
				throw new CantDoThatException("This calculation returns a " + this.getDbType()
						+ " not a " + DatabaseFieldType.TIMESTAMP);
			}
			String formattedDate = String.format(this.getJavaDateFormatString(), dateValue);
			return finishDateFormatting(formattedDate);
		}
	}

	public String formatCalendar(Calendar dateValue) throws CantDoThatException {
		if (this.referencesCalcFromOtherReport()) {
			return this.getReferencedCalc().formatCalendar(dateValue);
		} else {
			if (!this.getDbType().equals(DatabaseFieldType.TIMESTAMP)) {
				throw new CantDoThatException("This calculation returns a " + this.getDbType()
						+ " not a " + DatabaseFieldType.TIMESTAMP);
			}
			String formattedDate = String.format(this.getJavaDateFormatString(), dateValue);
			return finishDateFormatting(formattedDate);
		}
	}

	private static String finishDateFormatting(String dateString) {
		String formattedDate = dateString;
		// if we don't know what the date resolution should be because no dates
		// are referenced, strip off zero
		// times which are probably spurious
		if (formattedDate.endsWith("00:00:00")) {
			formattedDate = formattedDate.substring(0, formattedDate.length() - 9);
		}
		return formattedDate;
	}

	/**
	 * Sets the calculation SQL for a calc that is a reference to a calc from
	 * another report rather than a direct calculation
	 */
	private void setCalculationSQL() throws CantDoThatException {
		if (!this.referencesCalcFromOtherReport()) {
			throw new CantDoThatException(
					"This method must be used only on a calc that is a reference to a calc from another report");
		}
		ReportCalcFieldInfo referencedCalc = this.getReferencedCalc();
		String calculationSQL = referencedCalc.getParentReport().getInternalReportName() + "."
				+ referencedCalc.getInternalFieldName();
		this.setCalculationSQLDirect(calculationSQL);
	}

	/**
	 * Work out the actual SQL from the user-input calc definition and store it
	 */
	private void setCalculationSQL(Map<TableInfo, Set<BaseReportInfo>> availableDataStores)
			throws CodingErrorException, CantDoThatException {
		if (this.referencesCalcFromOtherReport()) {
			throw new CantDoThatException(
					"This method should only be used for direct calculations, not those that are references to a calc in another report");
		}
		String calculationSQL = this.getCalculationDefinition().toLowerCase(Locale.UK);
		Helpers.checkForSQLInjection(calculationSQL);
		String identifierToReplace = null;
		String identifierToMatch = null;
		String replacement = null;
		// Replace division signs with our own custom division operator, //
		// This is specially created not to throw divide by zero errors
		// but rather return null;
		calculationSQL = calculationSQL.replaceAll("\\/", "//");
		// Replace
		// {table.field} => internaltablename.internalfieldname
		for (TableInfo table : availableDataStores.keySet()) {
			FIELDSLOOP: for (BaseField field : table.getFields()) {
				if (field instanceof SeparatorField) {
					continue FIELDSLOOP;
				}
				// table name and field name may have characters in them that
				// need to be escaped before they can be used in a regex.
				// Also we're matching case insensitively so use toLowerCase()
				String tableName = table.getTableName().toLowerCase(Locale.UK);
				String fieldName = field.getFieldName().toLowerCase(Locale.UK);
				identifierToMatch = "{" + tableName + "." + fieldName + "}";
				tableName = tableName.replaceAll("(\\W)", "\\\\$1");
				fieldName = fieldName.replaceAll("(\\W)", "\\\\$1");
				identifierToReplace = "\\{" + tableName + "." + fieldName + "\\}";
				replacement = table.getInternalTableName() + "." + field.getInternalFieldName();
				// if this is a floating pt. calc., cast all numeric fields to
				// 'numeric' so that postgres calcs can work
				DatabaseFieldType fieldDbType = field.getDbType();
				if (this.getDbType().equals(DatabaseFieldType.FLOAT)
						&& (fieldDbType.equals(DatabaseFieldType.FLOAT) || fieldDbType
								.equals(DatabaseFieldType.INTEGER))) {
					replacement += "::double precision";
				}
				if (calculationSQL.contains(identifierToMatch)) {
					calculationSQL = calculationSQL.replaceAll(identifierToReplace, replacement);
					this.getFieldsUsed().add(field);
				}
			}
		}
		// If anything left to replace, try
		// {table.report.field} => internalreportname.internalfieldname
		if (calculationSQL.contains("{")) {
			for (TableInfo table : availableDataStores.keySet()) {
				for (BaseReportInfo report : table.getReports()) {
					for (BaseField field : report.getReportBaseFields()) {
						String tableName = table.getTableName().toLowerCase(Locale.UK);
						String reportName = report.getReportName().toLowerCase(Locale.UK);
						String fieldName = field.getFieldName().toLowerCase(Locale.UK);
						identifierToMatch = "{" + tableName + "." + reportName + "." + fieldName
								+ "}";
						tableName = tableName.replaceAll("(\\W)", "\\\\$1");
						reportName = reportName.replaceAll("(\\W)", "\\\\$1");
						fieldName = fieldName.replaceAll("(\\W)", "\\\\$1");
						identifierToReplace = "\\{" + tableName + "." + reportName + "."
								+ fieldName + "\\}";
						if (report.equals(this.getParentReport())
								&& (field instanceof CalculationFieldDefn)) {
							replacement = "("
									+ ((CalculationFieldDefn) field).getReportCalcField()
											.getCalculationSQL(false) + ")";
						} else {
							replacement = report.getInternalReportName() + "."
									+ field.getInternalFieldName();
							DatabaseFieldType fieldDbType = field.getDbType();
							if (this.getDbType().equals(DatabaseFieldType.FLOAT)
									&& (fieldDbType.equals(DatabaseFieldType.FLOAT) || fieldDbType
											.equals(DatabaseFieldType.INTEGER))) {
								replacement += "::double precision";
							}
						}
						if (calculationSQL.contains(identifierToMatch)) {
							calculationSQL = calculationSQL.replaceAll(identifierToReplace,
									replacement);
							this.getFieldsUsed().add(field);
						}
					}
				}
			}
		}
		// If anything left, try
		// {report.field} => internalreportname.internalfieldname
		if (calculationSQL.contains("{")) {
			for (TableInfo table : availableDataStores.keySet()) {
				for (BaseReportInfo report : table.getReports()) {
					for (BaseField field : report.getReportBaseFields()) {
						String reportName = report.getReportName().toLowerCase(Locale.UK);
						String fieldName = field.getFieldName().toLowerCase(Locale.UK);
						identifierToMatch = "{" + reportName + "." + fieldName + "}";
						reportName = reportName.replaceAll("(\\W)", "\\\\$1");
						fieldName = fieldName.replaceAll("(\\W)", "\\\\$1");
						identifierToReplace = "\\{" + reportName + "." + fieldName + "\\}";
						if (report.equals(this.getParentReport())
								&& (field instanceof CalculationFieldDefn)) {
							replacement = "("
									+ ((CalculationFieldDefn) field).getReportCalcField()
											.getCalculationSQL(false) + ")";
						} else {
							replacement = report.getInternalReportName() + "."
									+ field.getInternalFieldName();
							DatabaseFieldType fieldDbType = field.getDbType();
							if (this.getDbType().equals(DatabaseFieldType.FLOAT)
									&& (fieldDbType.equals(DatabaseFieldType.FLOAT) || fieldDbType
											.equals(DatabaseFieldType.INTEGER))) {
								replacement += "::double precision";
							}
						}
						if (calculationSQL.contains(identifierToMatch)) {
							calculationSQL = calculationSQL.replaceAll(identifierToReplace,
									replacement);
							this.getFieldsUsed().add(field);
						}
					}
				}
			}
		}
		// Just
		// {field} => internalfieldname
		// where field is in this report itself
		if (calculationSQL.contains("{")) {
			for (BaseField field : this.getParentReport().getReportBaseFields()) {
				String fieldName = field.getFieldName().toLowerCase(Locale.UK);
				identifierToMatch = "{" + fieldName + "}";
				fieldName = fieldName.replaceAll("(\\W)", "\\\\$1");
				identifierToReplace = "\\{" + fieldName + "\\}";
				if (field instanceof CalculationFieldDefn) {
					replacement = "("
							+ ((CalculationFieldDefn) field).getReportCalcField()
									.getCalculationSQL(false) + ")";
				} else {
					replacement = field.getInternalFieldName();
					DatabaseFieldType fieldDbType = field.getDbType();
					if (this.getDbType().equals(DatabaseFieldType.FLOAT)
							&& (fieldDbType.equals(DatabaseFieldType.FLOAT) || fieldDbType
									.equals(DatabaseFieldType.INTEGER))) {
						replacement += "::double precision";
					}
				}
				if (calculationSQL.contains(identifierToMatch)) {
					calculationSQL = calculationSQL.replaceAll(identifierToReplace, replacement);
					this.getFieldsUsed().add(field);
				}
			}
		}
		// Finally
		// {field} => internaltablename.internalfieldname
		// where field is in one of the report tables
		if (calculationSQL.contains("{")) {
			for (TableInfo table : ((SimpleReportInfo) this.getParentReport()).getJoinedTables()) {
				FIELDSLOOP: for (BaseField field : table.getFields()) {
					if (field instanceof SeparatorField) {
						continue FIELDSLOOP;
					}
					String fieldName = field.getFieldName().toLowerCase(Locale.UK);
					identifierToMatch = "{" + fieldName + "}";
					fieldName = fieldName.replaceAll("(\\W)", "\\\\$1");
					identifierToReplace = "\\{" + fieldName + "\\}";
					replacement = table.getInternalTableName() + "." + field.getInternalFieldName();
					DatabaseFieldType fieldDbType = field.getDbType();
					if (this.getDbType().equals(DatabaseFieldType.FLOAT)
							&& (fieldDbType.equals(DatabaseFieldType.FLOAT) || fieldDbType
									.equals(DatabaseFieldType.INTEGER))) {
						replacement += "::double precision";
					}
					if (calculationSQL.contains(identifierToMatch)) {
						calculationSQL = calculationSQL
								.replaceAll(identifierToReplace, replacement);
						this.getFieldsUsed().add(field);
					}
				}
			}
		}
		if ((calculationSQL.contains("{")) || (calculationSQL.contains("}"))) {
			int unparsableStart = calculationSQL.indexOf("{");
			if (unparsableStart < 0) {
				unparsableStart = 0;
			}
			int unparsableEnd = calculationSQL.lastIndexOf("}");
			if (unparsableEnd < 0) {
				unparsableEnd = calculationSQL.length();
			}
			String unparsablePart = calculationSQL.substring(unparsableStart, unparsableEnd + 1);
			throw new CantDoThatException("Unable to parse calculation part " + unparsablePart);
		}
		// set alias
		calculationSQL = "(" + calculationSQL + ")::" + this.getDbType().toString() + " AS "
				+ this.getInternalFieldName();
		this.setCalculationSQLDirect(calculationSQL);
	}

	@Transient
	public String getCalculationSQL(boolean includeAlias) {
		String calculationSQL = this.getCalculationSQLDirect();
		if (includeAlias) {
			return calculationSQL;
		}
		String toRemove = " AS " + this.getInternalFieldName();
		String calcSQLToReturn = calculationSQL.replaceFirst(toRemove, "");
		return calcSQLToReturn;
	}

	// Only used by setCalculationSQL and setDateResolution so doesn't need to
	// be persisted
	@Transient
	private Set<BaseField> getFieldsUsed() {
		return this.fieldsUsed;
	}

	private void setBaseFieldInternalFieldNameDirect(String internalFieldName) {
		this.baseFieldInternalFieldName = internalFieldName;
	}

	private String getBaseFieldInternalFieldNameDirect() {
		return this.baseFieldInternalFieldName;
	}

	@Transient
	public String getBaseFieldInternalFieldName() {
		if (this.referencesCalcFromOtherReport()) {
			return this.getReferencedCalc().getBaseFieldInternalFieldName();
		} else {
			return this.getBaseFieldInternalFieldNameDirect();
		}
	}

	private String getBaseFieldNameDirect() {
		return this.baseFieldName;
	}

	private void setBaseFieldNameDirect(String baseFieldName) {
		this.baseFieldName = baseFieldName;
	}

	@Transient
	public String getBaseFieldName() {
		if (this.referencesCalcFromOtherReport()) {
			return this.getReferencedCalc().getBaseFieldName();
		} else {
			return this.getBaseFieldNameDirect();
		}
	}

	protected void setBaseFieldName(String newFieldName) {
		this.setBaseFieldNameDirect(newFieldName);
	}

	@Column(length = 10000)
	private String getCalculationDefinitionDirect() {
		return this.calculationDefn;
	}

	private void setCalculationDefinitionDirect(String calculationDefn) {
		this.calculationDefn = calculationDefn;
	}

	protected void updateCalculationDefinition(String calculationDefn,
			DatabaseFieldType dbFieldType, Map<TableInfo, Set<BaseReportInfo>> availableDataStores)
			throws CodingErrorException, CantDoThatException {
		this.setCalculationDefinitionDirect(calculationDefn);
		this.setCalculationDefinitionDirect(calculationDefn);
		this.setDbTypeDirect(dbFieldType);
		this.setCalculationSQL(availableDataStores);
		if (dbFieldType.equals(DatabaseFieldType.TIMESTAMP)) {
			this.setDateResolution();
		} else if (dbFieldType.equals(DatabaseFieldType.FLOAT)) {
			this.setDecimalPrecision();
		}
	}

	@Transient
	public String getCalculationDefinition() {
		if (this.referencesCalcFromOtherReport()) {
			return this.getReferencedCalc().getCalculationDefinition();
		} else {
			return this.getCalculationDefinitionDirect();
		}
	}

	@Transient
	public int getDateResolution() throws CantDoThatException {
		if (this.referencesCalcFromOtherReport()) {
			return this.getReferencedCalc().getDateResolution();
		} else {
			if (!this.getDbType().equals(DatabaseFieldType.TIMESTAMP)) {
				throw new CantDoThatException(
						"Can't get the date resolution - this calculation returns a "
								+ this.getDbType() + " not a " + DatabaseFieldType.TIMESTAMP);
			}
			return this.getDateResolutionDirect();
		}
	}

	@Transient
	public int getDecimalPrecision() throws CantDoThatException {
		if (this.referencesCalcFromOtherReport()) {
			return this.getReferencedCalc().getDecimalPrecision();
		} else {
			if (!this.getDbType().equals(DatabaseFieldType.FLOAT)) {
				throw new CantDoThatException(
						"Can't get the decimal precision - this calculation returns a "
								+ this.getDbType() + " not a " + DatabaseFieldType.FLOAT);
			}
			return this.getDecimalPrecisionDirect();
		}
	}

	/**
	 * If this.dbFieldType and this.fieldsUsed have been set, calculate the date
	 * resolution and save it. Find the max. resolution used by any date fields
	 * used in the calc.
	 */
	private void setDateResolution() throws CodingErrorException {
		boolean datesFound = false;
		this.setDateResolutionDirect(Calendar.YEAR);
		for (BaseField field : this.getFieldsUsed()) {
			if (field instanceof DateField) {
				int resolution = ((DateField) field).getDateResolution();
				if (resolution > this.getDateResolutionDirect()) {
					this.setDateResolutionDirect(resolution);
				}
				datesFound = true;
			} else if (field instanceof CalculationField) {
				ReportCalcFieldInfo reportCalcField = ((CalculationField) field)
						.getReportCalcField();
				if (reportCalcField.getDbType().equals(DatabaseFieldType.TIMESTAMP)) {
					try {
						int resolution = reportCalcField.getDateResolution();
						if (resolution > this.getDateResolutionDirect()) {
							this.setDateResolutionDirect(resolution);
						}
					} catch (CantDoThatException cdtex) {
						throw new CodingErrorException(
								"Unable to get the date resolution for a date calculation", cdtex);
					}
					datesFound = true;
				}
			}
		}
		// use default if no date fields found
		if (!datesFound) {
			this.setDateResolutionDirect(Calendar.SECOND);
		}
		try {
			this.setJavaDateFormatString(Helpers.generateJavaDateFormat(this
					.getDateResolutionDirect()));
		} catch (CantDoThatException cdtex) {
			throw new CodingErrorException("Error setting java date format string for resolution "
					+ this.getDateResolutionDirect(), cdtex);
		}
	}

	/**
	 * If this.dbFieldType and this.fieldsUsed have been set, calculate the
	 * decimal precision and save it. Find the max. precision used by any
	 * decimal fields used in the calc.
	 */
	private void setDecimalPrecision() throws CodingErrorException {
		boolean decimalsFound = false;
		this.setDecimalPrecisionDirect(2);
		for (BaseField field : this.getFieldsUsed()) {
			if (field instanceof DecimalField) {
				int precision = ((DecimalField) field).getPrecision();
				if (precision > this.getDecimalPrecisionDirect()) {
					this.setDecimalPrecisionDirect(precision);
				}
				decimalsFound = true;
			} else if (field instanceof CalculationField) {
				ReportCalcFieldInfo reportCalcField = ((CalculationField) field)
						.getReportCalcField();
				if (reportCalcField.getDbType().equals(DatabaseFieldType.FLOAT)) {
					try {
						int precision = reportCalcField.getDecimalPrecision();
						if (precision > this.getDecimalPrecisionDirect()) {
							this.setDateResolutionDirect(precision);
						}
					} catch (CantDoThatException cdtex) {
						throw new CodingErrorException(
								"Unable to get the decimal precision for a decimal calculation",
								cdtex);
					}
					decimalsFound = true;
				}
			}
		}
		// use default if no decimal fields found
		if (!decimalsFound) {
			this.setDecimalPrecisionDirect(2);
		}
		this.setJavaDecimalFormatString(Helpers.generateJavaDecimalFormat(this
				.getDecimalPrecisionDirect()));
	}

	@Column(length = 10000)
	private String getCalculationSQLDirect() {
		return this.calculationSQL;
	}

	private void setCalculationSQLDirect(String calculationSQL) {
		this.calculationSQL = calculationSQL;
	}

	private void setDateResolutionDirect(Integer dateResolution) {
		this.dateResolution = dateResolution;
	}

	private void setDecimalPrecisionDirect(Integer decimalPrecision) {
		this.decimalPrecision = decimalPrecision;
	}

	private Integer getDateResolutionDirect() {
		return this.dateResolution;
	}

	private Integer getDecimalPrecisionDirect() {
		return this.decimalPrecision;
	}

	private void setJavaDateFormatString(String javaDataFormatString) {
		this.javaDateFormatString = javaDataFormatString;
	}

	private void setJavaDecimalFormatString(String javaDecimalFormatString) {
		this.javaDecimalFormatString = javaDecimalFormatString;
	}

	@Column(length = 1000)
	private String getJavaDateFormatString() {
		return this.javaDateFormatString;
	}

	@Column(length = 1000)
	private String getJavaDecimalFormatString() {
		return this.javaDecimalFormatString;
	}

	public boolean isAggregateFunction() {
		return this.isAggregateFunction;
	}

	public void setAggregateFunction(boolean isAggregateFunction) {
		this.isAggregateFunction = isAggregateFunction;
	}

	public boolean referencesCalcFromOtherReport() {
		return (this.getReferencedCalc() != null);
	}

	private void setReferencedCalc(ReportCalcFieldInfo referencedCalc) {
		this.referencedCalc = referencedCalc;
	}

	@ManyToOne(targetEntity = ReportCalcFieldDefn.class)
	private ReportCalcFieldInfo getReferencedCalc() {
		return this.referencedCalc;
	}

	/**
	 * A report field is defined as equal to another if the BaseField objects
	 * and parent report objects are the same
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		ReportFieldInfo otherReportField = (ReportFieldInfo) obj;
		if (otherReportField.getBaseField().equals(this.getBaseField())
				&& otherReportField.getParentReport().equals(this.getParentReport())) {
			return true;
		}
		return false;
	}

	/**
	 * hashCode() consistent with equals() above
	 */
	public int hashCode() {
		int result = 17;
		result = 37 * result + this.getBaseField().hashCode();
		result = 37 * result + this.getParentReport().hashCode();
		return result;
	}

	/**
	 * Provide a natural sort order by parent report then field index then
	 * BaseField name + internal name
	 */
	public int compareTo(ReportFieldInfo otherField) {
		if (this == otherField) {
			return 0;
		}
		BaseReportInfo otherReport = otherField.getParentReport();
		int reportCompare = this.getParentReport().compareTo(otherReport);
		if (reportCompare != 0) {
			return reportCompare;
		}
		Integer otherFieldIndex = otherField.getFieldIndex();
		int indexCompare = this.getFieldIndex().compareTo(otherFieldIndex);
		if (indexCompare != 0) {
			return indexCompare;
		}
		String otherFieldName = otherField.getBaseField().getFieldName();
		String otherInternalName = otherField.getBaseField().getInternalFieldName();
		String thisFieldName = this.getBaseField().getFieldName();
		String thisInternalName = this.getBaseField().getInternalFieldName();
		return (thisFieldName + thisInternalName).compareToIgnoreCase(otherFieldName
				+ otherInternalName);
	}

	public String toString() {
		return this.getBaseField().toString();
	}

	/**
	 * Fields used in the calculation, to be used when working out display
	 * resolution of return values
	 */
	private Set<BaseField> fieldsUsed = new HashSet<BaseField>();

	/**
	 * Used if this calculation returns a date. The value set to here doesn't
	 * matter, setDateResolution() will reset it when run
	 */
	private Integer dateResolution = Calendar.YEAR;

	/**
	 * Used if this calculation returns a decimal. The value set to here doesn't
	 * matter, setDecimalPrecision() will reset it when run
	 */
	private Integer decimalPrecision = 2;

	/**
	 * Used if this calculation returns a date
	 */
	private String javaDateFormatString = "%1$tY";

	/**
	 * Used if this calculation returns a decimal
	 */
	private String javaDecimalFormatString = "%1.2f";

	/**
	 * The calculation as input by the user, using curly bracket notation
	 */
	private String calculationDefn = "";

	private String calculationSQL = "";

	private String baseFieldInternalFieldName = null;

	private String baseFieldName = null;

	private DatabaseFieldType dbFieldType;

	private CalculationField baseField = null;

	/**
	 * Store the calc referenced, if this field is just a reference to a calc
	 * from another report
	 */
	private ReportCalcFieldInfo referencedCalc = null;

	private boolean isAggregateFunction = false;

	private static final SimpleLogger logger = new SimpleLogger(ReportCalcFieldDefn.class);
}
