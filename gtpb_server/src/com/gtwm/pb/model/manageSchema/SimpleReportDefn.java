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
package com.gtwm.pb.model.manageSchema;

import com.gtwm.pb.model.interfaces.JoinClauseInfo;
import com.gtwm.pb.model.interfaces.ReportFilterInfo;
import com.gtwm.pb.model.interfaces.SimpleReportInfo;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.ReportCalcFieldInfo;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.fields.RelationField;
import com.gtwm.pb.model.interfaces.fields.CalculationField;
import com.gtwm.pb.model.interfaces.fields.SeparatorField;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.ReportFieldInfo;
import com.gtwm.pb.model.interfaces.ReportSortInfo;
import com.gtwm.pb.model.interfaces.ModuleInfo;
import com.gtwm.pb.model.manageSchema.fields.AbstractField;
import com.gtwm.pb.util.RandomString;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.HibernateUtil;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.LinkedHashSet;
import java.util.HashSet;
import java.util.TreeSet;
import java.lang.StringBuffer;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import org.grlea.log.SimpleLogger;

@Entity
public class SimpleReportDefn extends BaseReportDefn implements SimpleReportInfo {

	protected SimpleReportDefn() {
	}

	/**
	 * Create a new report with one field (if possible)
	 * 
	 * @param parentTable
	 *            The table holding the report
	 */
	public SimpleReportDefn(TableInfo parentTable, String internalReportName, String reportName,
			String reportDesc, ModuleInfo module) {
		if (internalReportName == null) {
			super.setInternalReportName((new RandomString()).toString());
		} else {
			super.setInternalReportName(internalReportName);
		}
		super.setReportName(reportName);
		super.setReportDescription(reportDesc);
		super.setParentTable(parentTable);
		super.setReportSummary(new ReportSummaryDefn(this, true));
		if (module != null) {
			super.setModule(module);
		}
		// Start the report off by putting in the primary key from the parent
		// table
		if (parentTable.getFields().size() > 0) {
			BaseField firstField = parentTable.getPrimaryKey();
			ReportFieldInfo firstReportField = new ReportFieldDefn(this, firstField);
			this.getReportFieldsDirect().add(firstReportField);
			// If field is of a suitable type, add a basic summary for the new
			// report
			// Note: This can be annoying, turned off
			// if (firstField instanceof RelationField) {
			// super.getReportSummary().addGrouping((RelationField) firstField);
			// super.getReportSummary().addCountFunction(firstField);
			// } else if (firstField instanceof ManualDropdownField) {
			// super.getReportSummary().addGrouping((ManualDropdownField)
			// firstField);
			// super.getReportSummary().addCountFunction(firstField);
			// }
		}
	}

	private synchronized void validateTableVisibility(TableInfo tableFieldParentTable)
			throws CantDoThatException {
		// ensure the table owning the requested field is either the report's
		// parent table or is included in a join:
		if (!tableFieldParentTable.equals(this.getParentTable())) {
			boolean foundParentTable = false;
			for (JoinClauseInfo jc : this.getJoinsDirect()) {
				if (jc.isLeftPartTable()) {
					try {
						if (jc.getLeftTableField().getTableContainingField().equals(
								tableFieldParentTable)) {
							foundParentTable = true;
						}
					} catch (CantDoThatException cdtex) {
						logger.warn("Join clause isLeftTableField() misuse");
					}
				}
				if (jc.isRightPartTable()) {
					try {
						if (jc.getRightTableField().getTableContainingField().equals(
								tableFieldParentTable)) {
							foundParentTable = true;
						}
					} catch (CantDoThatException cdtex) {
						logger.warn("Join clause isRightTableField() misuse");
					}
				}
			}
			if (!foundParentTable) {
				throw new CantDoThatException(
						"Cannot add field until the table or report from which it comes has been joined");
			}
		}
	}

	public synchronized ReportFieldInfo addTableField(BaseField fieldToAdd)
			throws CantDoThatException, CodingErrorException {
		this.validateTableVisibility(fieldToAdd.getTableContainingField());
		// table having field to add is parent or is joined so ok to add field:
		ReportFieldInfo reportField = new ReportFieldDefn(this, fieldToAdd);
		// also add in the parent table's ID field which PB can use to find
		// related data
		BaseField primaryKeyToAdd = fieldToAdd.getTableContainingField().getPrimaryKey();
		ReportFieldInfo primaryKeyReportField = new ReportFieldDefn(this, primaryKeyToAdd);
		boolean needPrimaryKey = true;
		if (fieldToAdd.equals(primaryKeyToAdd)) {
			// don't add a pkey field twice
			needPrimaryKey = false;
		}
		for (ReportFieldInfo existingField : this.getReportFieldsDirect()) {
			BaseField existingBaseField = existingField.getBaseField();
			if (existingBaseField.equals(fieldToAdd)) {
				throw new CantDoThatException(
						"Unable to add field to report as it is already within the report");
			}
			if (needPrimaryKey && existingBaseField.equals(primaryKeyToAdd)) {
				needPrimaryKey = false;
			}
		}
		reportField.setFieldIndex(this.getReportFieldsDirect().size());
		this.getReportFieldsDirect().add(reportField);
		if (needPrimaryKey) {
			primaryKeyReportField.setFieldIndex(this.getReportFieldsDirect().size());
			this.getReportFieldsDirect().add(primaryKeyReportField);
		}
		return reportField;
	}

	private synchronized void validateReportVisibility(BaseReportInfo reportFieldParentReport)
			throws CantDoThatException {
		// ensure the report owning the requested field is included in a join:
		boolean foundParentReport = false;
		for (JoinClauseInfo jc : this.getJoinsDirect()) {
			if (!jc.isLeftPartTable()) {
				try {
					if (jc.getLeftReportField().getParentReport().equals(reportFieldParentReport)) {
						foundParentReport = true;
					}
				} catch (CantDoThatException cdtex) {
					logger.warn("Join clause isLeftTableField() misuse");
				}
			}
			if (!jc.isRightPartTable()) {
				try {
					if (jc.getRightReportField().getParentReport().equals(reportFieldParentReport)) {
						foundParentReport = true;
					}
				} catch (CantDoThatException cdtex) {
					logger.warn("Join clause isRightTableField() misuse");
				}
			}
		}
		if (!foundParentReport) {
			throw new CantDoThatException(
					"Cannot add field until the table or report from which it comes has been joined");
		}
	}

	public synchronized ReportFieldInfo addReportField(ReportFieldInfo reportField)
			throws CantDoThatException, CodingErrorException {
		this.validateReportVisibility(reportField.getParentReport());
		// ReportFieldInfo reportField;
		if (reportField instanceof ReportCalcFieldInfo) {
			reportField = new ReportCalcFieldDefn(this, (ReportCalcFieldInfo) reportField);
		} else {
			reportField = new ReportFieldDefn(this, reportField.getParentReport(), reportField
					.getBaseField());
		}
		for (ReportFieldInfo existingField : this.getReportFields()) {
			if (existingField.equals(reportField)) {
				throw new CantDoThatException("Unable to add field " + reportField
						+ " to report as it is already there");
			}
		}
		reportField.setFieldIndex(this.getReportFieldsDirect().size());
		this.getReportFieldsDirect().add(reportField);
		// updateReportSummaryWithNewField(fieldToAdd);
		return reportField;
	}

	public synchronized void addField(ReportFieldInfo reportField) throws CantDoThatException {
		if (reportField.isFieldFromReport()) {
			this.validateReportVisibility(reportField.getParentReport());
			for (ReportFieldInfo existingField : this.getReportFields()) {
				if (existingField.equals(reportField)) {
					throw new CantDoThatException("Unable to add field " + reportField
							+ " to report as it is already there");
				}
			}
		} else {
			this.validateTableVisibility(reportField.getBaseField().getTableContainingField());
		}
		reportField.setFieldIndex(this.getReportFieldsDirect().size());
		this.getReportFieldsDirect().add(reportField);
	}

	public synchronized void addCalculation(ReportCalcFieldInfo reportCalcField) {
		reportCalcField.setFieldIndex(this.getReportFieldsDirect().size());
		this.getReportFieldsDirect().add(reportCalcField);
	}

	/**
	 * @throws CantDoThatException
	 *             if the report is not the default report for a table
	 */
	protected synchronized void copyBaseFieldIndexes() throws CantDoThatException {
		if (!this.getParentTable().getDefaultReport().equals(this)) {
			throw new CantDoThatException(
					"Table field ordering can only be copied to default reports");
		}
		for (ReportFieldInfo reportField : this.getReportFields()) {
			Integer fieldIndex = reportField.getBaseField().getFieldIndex();
			reportField.setFieldIndex(fieldIndex);
		}
	}

	public synchronized void setFieldIndex(int index, ReportFieldInfo fieldToOrder) {
		// range check index
		Integer validIndex = index;
		if (validIndex < 0) {
			validIndex = 0;
		} else if (validIndex > (this.getReportFieldsDirect().size() - 1)) {
			validIndex = this.getReportFieldsDirect().size() - 1;
		}
		// Simple implementation avoids having to shuffle many fields: just swap
		// This only works if fields are moved one place at a time
		Integer originalIndex = fieldToOrder.getFieldIndex();
		Iterator<ReportFieldInfo> iterator = this.getReportFieldsDirect().iterator();
		if (validIndex > originalIndex) { // moving towards the end
			while (iterator.hasNext()) {
				ReportFieldInfo field = iterator.next();
				int fieldIndex = field.getFieldIndex();
				if ((fieldIndex > originalIndex) && (fieldIndex <= validIndex)) {
					field.setFieldIndex(fieldIndex - 1);
				}
			}
		} else { // moving towards the beginning
			while (iterator.hasNext()) {
				ReportFieldInfo field = iterator.next();
				int fieldIndex = field.getFieldIndex();
				if ((fieldIndex >= validIndex) && (fieldIndex < originalIndex)) {
					field.setFieldIndex(fieldIndex + 1);
				}
			}
		}
		fieldToOrder.setFieldIndex(validIndex);
		// Ensure indexes are consistent in case they've been messed up in the
		// past
		Map<ReportFieldInfo, Integer> correctIndexes = new HashMap<ReportFieldInfo, Integer>();
		Integer correctIndex = 0;
		// getFields is a SortedSet, by field index
		for (ReportFieldInfo field : this.getReportFields()) {
			correctIndexes.put(field, correctIndex);
			correctIndex++;
		}
		iterator = this.getReportFieldsDirect().iterator();
		while (iterator.hasNext()) {
			ReportFieldInfo field = iterator.next();
			correctIndex = correctIndexes.get(field);
			if (!field.getFieldIndex().equals(correctIndex)) {
				// logger.warn("Field index " + field.getFieldIndex() + "
				// incorrect for field " + field + ", correcting to " +
				// correctIndex);
				field.setFieldIndex(correctIndex);
			}
		}
	}

	@Transient
	public synchronized SortedSet<ReportFieldInfo> getReportFields() {
		return Collections.unmodifiableSortedSet(new TreeSet<ReportFieldInfo>(this
				.getReportFieldsDirect()));
	}

	private void setReportFieldsDirect(Set<ReportFieldInfo> reportFields) {
		this.reportFields = reportFields;
	}

	@OneToMany(mappedBy = "parentReport", targetEntity = AbstractReportField.class, cascade = CascadeType.ALL)
	private Set<ReportFieldInfo> getReportFieldsDirect() {
		return this.reportFields;
	}

	// Transient because dependent entirely on getReportFieldsDirect(), which is
	// persisted
	@Transient
	public synchronized Set<BaseField> getReportBaseFields() {
		SortedSet<ReportFieldInfo> reportFields = this.getReportFields();
		Set<BaseField> baseFields = new LinkedHashSet<BaseField>();
		for (ReportFieldInfo reportField : reportFields) {
			baseFields.add(reportField.getBaseField());
		}
		return Collections.unmodifiableSet(baseFields);
	}

	@Transient
	public synchronized ReportFieldInfo getReportField(String fieldID)
			throws ObjectNotFoundException {
		Set<ReportFieldInfo> reportFields = this.getReportFieldsDirect();
		// Search by internal name
		for (ReportFieldInfo reportField : reportFields) {
			if (fieldID.equals(reportField.getBaseField().getInternalFieldName())) {
				return reportField;
			}
		}
		// Search by public name
		for (ReportFieldInfo reportField : reportFields) {
			if (fieldID.equals(reportField.getBaseField().getFieldName())) {
				return reportField;
			}
		}
		throw new ObjectNotFoundException("No field with internal or public name " + fieldID
				+ " found in report " + super.getReportName());
	}

	public synchronized void removeField(ReportFieldInfo reportFieldToRemove)
			throws CantDoThatException, ObjectNotFoundException {
		if (this.getReportFieldsDirect().size() == 1) {
			throw new CantDoThatException("Can't remove the only field");
		}
		// remove any sorts based on field
		for (Iterator<ReportSortInfo> iterator = this.getSortsDirect().iterator(); iterator
				.hasNext();) {
			ReportSortInfo testSort = iterator.next();
			if (testSort.getSortReportField().equals(reportFieldToRemove)) {
				iterator.remove();
				HibernateUtil.currentSession().delete(testSort);
			}
		}
		// Have to use an Iterator rather than a for loop when removing an item
		// from a collection
		boolean removed = false;
		for (Iterator<ReportFieldInfo> iterator = this.getReportFieldsDirect().iterator(); iterator
				.hasNext();) {
			ReportFieldInfo testReportField = iterator.next();
			if (testReportField.equals(reportFieldToRemove)) {
				iterator.remove();
				removed = true;
				break;
			}
		}
		if (!removed) {
			throw new ObjectNotFoundException(reportFieldToRemove.toString()
					+ " not found in report " + this);
		}
		// shuffle other fields down
		for (Iterator<ReportFieldInfo> iterator = this.getReportFieldsDirect().iterator(); iterator
				.hasNext();) {
			ReportFieldInfo reportField = iterator.next();
			if (reportField.getFieldIndex() > reportFieldToRemove.getFieldIndex()) {
				reportField.setFieldIndex(reportField.getFieldIndex() - 1);
			}
		}
		// Remove from summary if field is part of the summary
		// Need to check both summary grouping fields and summary aggregate
		// functions
		super.getReportSummary().removeGrouping(reportFieldToRemove);
		super.getReportSummary().removeFunctions(reportFieldToRemove);
		// if removing the last field from a joined table, remove the joined
		// table ID as well
		if (!reportFieldToRemove.isFieldFromReport()) {
			TableInfo parentTable = reportFieldToRemove.getBaseField().getTableContainingField();
			if (!parentTable.equals(this.getParentTable())) {
				boolean needToRemovePkey = true;
				ReportFieldInfo pkeyReportFieldToRemove = null;
				// see if there are any other fields with that parent table
				for (ReportFieldInfo testReportField : this.getReportFields()) {
					if (!testReportField.isFieldFromReport()) {
						if (testReportField.getBaseField().getTableContainingField().equals(
								parentTable)) {
							needToRemovePkey = false;
						}
						if (testReportField.getBaseField().equals(parentTable.getPrimaryKey())) {
							pkeyReportFieldToRemove = testReportField;
						}
					}
				}
				if (needToRemovePkey && pkeyReportFieldToRemove != null) {
					this.removeField(pkeyReportFieldToRemove);
				}
			}
		}
	}

	@Transient
	public synchronized String getSQLForDetail() throws CantDoThatException, CodingErrorException,
			ObjectNotFoundException {
		String SQLCode = "";
		String distinctArguments = "";
		String selectArguments = "";
		String whereArguments = "";
		String groupByArguments = "";
		String havingArguments = "";
		for (BaseField baseField : this.getDistinctFieldsDirect()) {
			distinctArguments += baseField.getInternalFieldName() + ", ";
		}
		if (!distinctArguments.equals("")) {
			distinctArguments = " DISTINCT ON ("
					+ distinctArguments.substring(0, distinctArguments.length() - 2) + ") ";
		}
		boolean reportContainsAggregates = false;
		// Generate the SELECT part of the SQL statement
		for (ReportFieldInfo reportField : this.getReportFields()) {
			if (reportField instanceof ReportCalcFieldInfo) {
				ReportCalcFieldInfo reportCalcField = (ReportCalcFieldInfo) reportField;
				selectArguments += reportCalcField.getCalculationSQL(true) + ", ";
				if (reportCalcField.isAggregateFunction()) {
					reportContainsAggregates = true;
				} else {
					groupByArguments += reportCalcField.getCalculationSQL(false) + ", ";
				}
			} else { // normal field not a calculation field
				if (reportField.isFieldFromReport()) {
					// field is sourced from another report:
					BaseField baseField = reportField.getBaseField();
					BaseReportInfo sourceReport = reportField.getReportFieldIsFrom();
					selectArguments += sourceReport.getInternalReportName() + "."
							+ baseField.getInternalFieldName() + ", ";
					groupByArguments += sourceReport.getInternalReportName() + "."
							+ baseField.getInternalFieldName() + ", ";
				} else {
					// field is sourced directly from a table:
					BaseField baseField = reportField.getBaseField();
					TableInfo tableContainingField = baseField.getTableContainingField();
					// Generate a unique set of tables - no duplicates allowed
					// in the set
					selectArguments += tableContainingField.getInternalTableName() + "."
							+ baseField.getInternalFieldName() + ", ";
					groupByArguments += tableContainingField.getInternalTableName() + "."
							+ baseField.getInternalFieldName() + ", ";
				}
			}
		}
		// Add filters
		for (ReportFilterInfo filter : this.getFilters()) {
			boolean aggregateFunction = false;
			if (filter.isFilterFieldFromReport()) {
				ReportFieldInfo filterReportField = filter.getFilterReportField();
				if (filterReportField instanceof ReportCalcFieldInfo) {
					aggregateFunction = ((ReportCalcFieldInfo) filterReportField)
							.isAggregateFunction();
				}
			} else {
				BaseField filterField = filter.getFilterBaseField();
				if (filterField instanceof CalculationField) {
					ReportCalcFieldInfo filterReportCalcField = ((CalculationField) filterField)
							.getReportCalcField();
					aggregateFunction = filterReportCalcField.isAggregateFunction();
				}
			}
			if (aggregateFunction) {
				havingArguments += filter.getFilterSQL() + " AND ";
			} else {
				whereArguments += filter.getFilterSQL() + " AND ";
			}
		}
		// Generate group by clause if aggregates are used
		// Clean up generated parts
		selectArguments = selectArguments.substring(0, selectArguments.length() - 2);
		if (!(whereArguments.equals(""))) {
			whereArguments = whereArguments
					.substring(0, whereArguments.length() - " AND ".length());
		}
		if (!(havingArguments.equals(""))) {
			havingArguments = havingArguments.substring(0, havingArguments.length()
					- " AND ".length());
		}
		if (!(groupByArguments.equals(""))) {
			groupByArguments = groupByArguments.substring(0, groupByArguments.length()
					- ", ".length());
		}
		// Generate the FROM part of the SQL statement
		String fromArguments = "";
		SortedSet<JoinClauseInfo> joins = this.getJoins();
		// whether the report contains any inner or outer joins
		boolean containsProperJoin = false;
		for (JoinClauseInfo join : joins) {
			if (!(join.getJoinType().equals(JoinType.NONE))) {
				containsProperJoin = true;
				break;
			}
		}
		if (!containsProperJoin) {
			// if no joins or only NONE joins are specified just use the parent
			// table
			fromArguments = this.getParentTable().getInternalTableName();
			for (JoinClauseInfo jc : joins) {
				String rightFieldOwner;
				if (jc.isRightPartTable()) {
					rightFieldOwner = jc.getRightTableField().getTableContainingField()
							.getInternalTableName();
				} else {
					rightFieldOwner = jc.getRightReportField().getParentReport()
							.getInternalReportName();
				}
				fromArguments += ", " + rightFieldOwner;
			}
		} else {
			// if joins have been added use them to create the FROM clause
			// join clauses are stored in the order they were added, it
			// is assumed that this order will make sense when constructing the
			// FROM clause
			StringBuffer joinSQLBuffer = null;
			for (JoinClauseInfo jc : joins) {
				// Do inner and outer joins, leave the NONE type for later.
				// See JoinType JavaDoc for an explanation of what it is
				if (!jc.getJoinType().equals(JoinType.NONE)) {
					String leftFieldOwner;
					String leftField;
					String rightFieldOwner;
					String rightField;
					if (jc.isLeftPartTable()) {
						leftFieldOwner = jc.getLeftTableField().getTableContainingField()
								.getInternalTableName();
						leftField = jc.getLeftTableField().getInternalFieldName();
					} else {
						BaseReportInfo leftReport = jc.getLeftReportField().getParentReport();
						// In SQL, can't use the current report in a join,
						// replace with table
						if (leftReport.equals(this)) {
							leftFieldOwner = jc.getLeftReportField().getBaseField()
									.getTableContainingField().getInternalTableName();
						} else {
							leftFieldOwner = jc.getLeftReportField().getParentReport()
									.getInternalReportName();
						}
						leftField = jc.getLeftReportField().getBaseField().getInternalFieldName();
					}
					if (jc.isRightPartTable()) {
						rightFieldOwner = jc.getRightTableField().getTableContainingField()
								.getInternalTableName();
						rightField = jc.getRightTableField().getInternalFieldName();
					} else {
						BaseReportInfo rightReport = jc.getRightReportField().getParentReport();
						if (rightReport.equals(this)) {
							rightFieldOwner = jc.getRightReportField().getBaseField()
									.getTableContainingField().getInternalTableName();
						} else {
							rightFieldOwner = jc.getRightReportField().getParentReport()
									.getInternalReportName();
						}
						rightField = jc.getRightReportField().getBaseField().getInternalFieldName();
					}
					if (joinSQLBuffer == null) {
						// if no FROM join text yet created
						joinSQLBuffer = new StringBuffer(leftFieldOwner).append(" ").append(
								jc.getJoinType().toString()).append(" JOIN ").append(
								rightFieldOwner);
					} else {
						// if adding additional joins
						joinSQLBuffer = new StringBuffer("(").append(joinSQLBuffer).append(") ")
								.append(jc.getJoinType().toString()).append(" JOIN ").append(
										rightFieldOwner);
					}
					// Calculations in joins are a special case: if calculation
					// from current report, have to redo
					// calc. SQL rather than reference
					boolean mustRedoLeftCalcSQL = false;
					if (!jc.isLeftPartTable()) {
						ReportFieldInfo leftReportField = jc.getLeftReportField();
						BaseReportInfo leftReport = leftReportField.getParentReport();
						if (leftReportField instanceof ReportCalcFieldInfo) {
							if (leftReport.equals(this)) {
								mustRedoLeftCalcSQL = true;
							}
						}
					}
					boolean mustRedoRightCalcSQL = false;
					if (!jc.isRightPartTable()) {
						ReportFieldInfo rightReportField = jc.getRightReportField();
						BaseReportInfo rightReport = rightReportField.getParentReport();
						if (rightReportField instanceof ReportCalcFieldInfo) {
							if (rightReport.equals(this)) {
								mustRedoRightCalcSQL = true;
							}
						}
					}
					if (mustRedoLeftCalcSQL) {
						joinSQLBuffer = joinSQLBuffer.append(" ON ").append("(").append(
								((ReportCalcFieldInfo) jc.getLeftReportField())
										.getCalculationSQL(false)).append(")");
					} else {
						joinSQLBuffer = joinSQLBuffer.append(" ON ").append(leftFieldOwner).append(
								".").append(leftField);
					}
					if (mustRedoRightCalcSQL) {
						joinSQLBuffer = joinSQLBuffer.append(" = ").append("(").append(
								((ReportCalcFieldInfo) jc.getRightReportField())
										.getCalculationSQL(false)).append(")");
					} else {
						joinSQLBuffer = joinSQLBuffer.append(" = ").append(rightFieldOwner).append(
								".").append(rightField);
					}
				}
			}
			// now add the rest of the tables that aren't in a proper join
			for (JoinClauseInfo jc : joins) {
				if (jc.getJoinType().equals(JoinType.NONE)) {
					String rightFieldOwner;
					if (jc.isRightPartTable()) {
						rightFieldOwner = jc.getRightTableField().getTableContainingField()
								.getInternalTableName();
					} else {
						rightFieldOwner = jc.getRightReportField().getParentReport()
								.getInternalReportName();
					}
					joinSQLBuffer = joinSQLBuffer.append(", ").append(rightFieldOwner);
				}
			}
			fromArguments = joinSQLBuffer.toString();
		}
		String sortArguments = "";
		for (ReportSortInfo reportSort : this.getSorts()) {
			ReportFieldInfo reportField = reportSort.getSortReportField();
			sortArguments += reportField.getInternalFieldName();
			if (!reportSort.getSortDirection()) {
				sortArguments += " DESC";
			}
			sortArguments += ", ";
		}
		if (!sortArguments.equals("")) {
			sortArguments = sortArguments.substring(0, sortArguments.length() - 2);
			if (sortArguments.endsWith("DESC")) {
				sortArguments += " NULLS LAST";
			} else {
				sortArguments += " NULLS FIRST";
			}
		}
		// Put everything together
		SQLCode = "SELECT " + distinctArguments + selectArguments + " FROM " + fromArguments;
		if (!(whereArguments.equals(""))) {
			SQLCode += " WHERE " + whereArguments;
		}
		if (reportContainsAggregates) {
			SQLCode += " GROUP BY " + groupByArguments;
		}
		if (!(havingArguments.equals(""))) {
			SQLCode += " HAVING " + havingArguments;
		}
		if (!(sortArguments.equals(""))) {
			SQLCode += " ORDER BY " + sortArguments;
		}
		return SQLCode;
	}

	@Transient
	public synchronized SortedSet<JoinClauseInfo> getJoins() {
		return Collections
				.unmodifiableSortedSet(new TreeSet<JoinClauseInfo>(this.getJoinsDirect()));
	}

	@OneToMany(targetEntity = JoinClause.class, cascade = CascadeType.ALL)
	private Set<JoinClauseInfo> getJoinsDirect() {
		return this.joins;
	}

	private void setJoinsDirect(Set<JoinClauseInfo> joins) {
		this.joins = joins;
	}

	public synchronized void addJoin(JoinClauseInfo join) {
		// if a join clause already exists for the table(s)/report(s)
		// being joined then equals() in JoinClauseInfo will return true
		// and the old join will be overwritten as this.joins is a set
		this.getJoinsDirect().add(join);
	}

	@Transient
	public synchronized JoinClauseInfo getJoinByInternalName(String internalJoinName)
			throws ObjectNotFoundException {
		for (JoinClauseInfo join : this.getJoinsDirect()) {
			if (join.getInternalJoinName().equals(internalJoinName)) {
				return join;
			}
		}
		throw new ObjectNotFoundException("The join identified by " + internalJoinName
				+ " could not be located within " + this.getReportName());
	}

	public synchronized void removeJoin(JoinClauseInfo join) throws ObjectNotFoundException,
			CantDoThatException, CodingErrorException {
		// Check that join references are the last ones in the join set.
		// If so, we'll need to make sure no report fields use the join
		boolean lastLeftReference = true;
		boolean lastRightReference = true;
		TableInfo leftTable = null;
		TableInfo rightTable = null;
		BaseReportInfo leftReport = null;
		BaseReportInfo rightReport = null;
		if (join.isLeftPartTable()) {
			leftTable = join.getLeftTableField().getTableContainingField();
		} else {
			leftReport = join.getLeftReportField().getParentReport();
		}
		if (join.isRightPartTable()) {
			rightTable = join.getRightTableField().getTableContainingField();
		} else {
			rightReport = join.getRightReportField().getParentReport();
		}
		for (JoinClauseInfo testJoin : this.getJoins()) {
			if (!testJoin.equals(join)) {
				if (testJoin.isLeftPartTable()) {
					TableInfo testTable = testJoin.getLeftTableField().getTableContainingField();
					if (leftTable != null) {
						if (leftTable.equals(testTable)) {
							lastLeftReference = false;
						}
					}
					if (rightTable != null) {
						if (rightTable.equals(testTable)) {
							lastLeftReference = false;
						}
					}
				} else {
					BaseReportInfo testReport = testJoin.getLeftReportField().getParentReport();
					if (leftReport != null) {
						if (leftReport.equals(testReport)) {
							lastLeftReference = false;
						}
					}
					if (rightReport != null) {
						if (rightReport.equals(testReport)) {
							lastLeftReference = false;
						}
					}
				}
				if (testJoin.isRightPartTable()) {
					TableInfo testTable = testJoin.getRightTableField().getTableContainingField();
					if (rightTable != null) {
						if (rightTable.equals(testTable)) {
							lastRightReference = false;
						}
					}
					if (leftTable != null) {
						if (leftTable.equals(testTable)) {
							lastRightReference = false;
						}
					}
				} else {
					BaseReportInfo testReport = testJoin.getRightReportField().getParentReport();
					if (rightReport != null) {
						if (rightReport.equals(testReport)) {
							lastRightReference = false;
						}
					}
					if (leftReport != null) {
						if (leftReport.equals(testReport)) {
							lastRightReference = false;
						}
					}
				}
			}
		}
		if (lastLeftReference || lastRightReference) {
			// check join isn't used in any report fields
			for (ReportFieldInfo reportField : this.getReportFields()) {
				if (reportField instanceof ReportCalcFieldInfo) {
					String calcSQL = ((ReportCalcFieldInfo) reportField).getCalculationSQL(true);
					if (lastLeftReference) {
						if (join.isLeftPartTable()) {
							TableInfo joinTable = join.getLeftTableField()
									.getTableContainingField();
							if (!joinTable.equals(this.getParentTable())) {
								if (calcSQL.contains(joinTable.getInternalTableName())) {
									throw new CantDoThatException("Table " + joinTable
											+ " is still used in the report calculation "
											+ reportField);
								}
							}
						} else {
							BaseReportInfo joinReport = join.getLeftReportField().getParentReport();
							if (!joinReport.equals(this)) {
								if (calcSQL.contains(joinReport.getInternalReportName())) {
									throw new CantDoThatException("Report " + joinReport
											+ " is still used in the report calculation "
											+ reportField);
								}
							}
						}
					}
					if (lastRightReference) {
						if (join.isRightPartTable()) {
							TableInfo joinTable = join.getRightTableField()
									.getTableContainingField();
							if (!joinTable.equals(this.getParentTable())) {
								if (calcSQL.contains(joinTable.getInternalTableName())) {
									throw new CantDoThatException("Table " + joinTable
											+ " is still used in the report calculation "
											+ reportField);
								}
							}
						} else {
							BaseReportInfo joinReport = join.getRightReportField()
									.getParentReport();
							if (!joinReport.equals(this)) {
								if (calcSQL.contains(joinReport.getInternalReportName())) {
									throw new CantDoThatException("Report " + joinReport
											+ " is still used in the report calculation "
											+ reportField);
								}
							}
						}
					} // end of calculation checks
				} else if (reportField.isFieldFromReport()) {
					if (lastLeftReference && (!join.isLeftPartTable())) {
						if (join.getLeftReportField().getParentReport().equals(
								reportField.getReportFieldIsFrom())) {
							throw new CantDoThatException("The "
									+ reportField.getReportFieldIsFrom()
									+ " report is still used in this report, field " + reportField);
						}
					}
					if (lastRightReference && (!join.isRightPartTable())) {
						if (join.getRightReportField().getParentReport().equals(
								reportField.getReportFieldIsFrom())) {
							throw new CantDoThatException("The "
									+ reportField.getReportFieldIsFrom()
									+ " report is still used in this report, field " + reportField);
						}
					}
				} else {
					TableInfo tableContainingField = reportField.getBaseField()
							.getTableContainingField();
					if (!tableContainingField.equals(this.getParentTable())) {
						if (lastLeftReference && join.isLeftPartTable()) {
							if (join.getLeftTableField().getTableContainingField().equals(
									tableContainingField)) {
								throw new CantDoThatException("The " + tableContainingField
										+ " table is still used in this report, field "
										+ reportField);
							}
						}
						if (lastRightReference && join.isRightPartTable()) {
							if (join.getRightTableField().getTableContainingField().equals(
									tableContainingField)) {
								throw new CantDoThatException("The " + tableContainingField
										+ " table is still used in this report, field "
										+ reportField);
							}
						}
					}
				}
			}
		}
		this.getJoinsDirect().remove(join);
	}

	/*
	 * TODO: get checking code to work and put checks back in!
	 */
	public synchronized void addFilter(ReportFilterInfo filterToAdd) throws CantDoThatException {
		Set<BaseField> visibleFields = new HashSet<BaseField>();
		// allow filtering on any field in the parent table:
		for (BaseField visibleField : this.getParentTable().getFields()) {
			if (!(visibleField instanceof SeparatorField)) {
				visibleFields.add(visibleField);
			}
		}
		// allow filtering on any calculation fields in the report
		for (BaseField visibleField : this.getReportBaseFields()) {
			if (visibleField instanceof CalculationField) {
				visibleFields.add(visibleField);
			}
		}
		// allow filtering on any field from a joined table or report:
		for (JoinClauseInfo join : this.getJoinsDirect()) {
			if (join.isLeftPartTable()) {
				for (BaseField visibleField : join.getLeftTableField().getTableContainingField()
						.getFields()) {
					if (!(visibleField instanceof SeparatorField)) {
						visibleFields.add(visibleField);
					}
				}
			} else {
				for (BaseField visibleField : join.getLeftReportField().getParentReport()
						.getReportBaseFields()) {
					visibleFields.add(visibleField);
				}
			}
			if (join.isRightPartTable()) {
				for (BaseField visibleField : join.getRightTableField().getTableContainingField()
						.getFields()) {
					if (!(visibleField instanceof SeparatorField)) {
						visibleFields.add(visibleField);
					}
				}
			} else {
				for (BaseField visibleField : join.getRightReportField().getParentReport()
						.getReportBaseFields()) {
					visibleFields.add(visibleField);
				}
			}
		}
		// add the filter if allowed:
		BaseField filterBaseField = null;
		if (filterToAdd.isFilterFieldFromReport()) {
			filterBaseField = filterToAdd.getFilterReportField().getBaseField();
		} else {
			filterBaseField = filterToAdd.getFilterBaseField();
		}
		if (visibleFields.contains(filterBaseField)) {
			this.getFiltersDirect().add(filterToAdd);
		} else {
			throw new CantDoThatException("Can't add filter on field '" + filterBaseField
					+ "' until its parent table or report has been joined into the report");
		}
	}

	public synchronized void removeFilter(ReportFilterInfo filter) {
		this.getFiltersDirect().remove(filter);
	}

	@Transient
	public synchronized ReportFilterInfo getFilterByInternalName(String internalFilterName)
			throws ObjectNotFoundException {
		for (ReportFilterInfo reportFilter : this.getFilters()) {
			if (reportFilter.getInternalName().equals(internalFilterName)) {
				return reportFilter;
			}
		}
		throw new ObjectNotFoundException("The filter identified by " + internalFilterName
				+ " could not be located within " + this.getReportName());
	}

	@Transient
	public synchronized Set<ReportFilterInfo> getFilters() {
		return Collections.unmodifiableSet(new LinkedHashSet<ReportFilterInfo>(this
				.getFiltersDirect()));
	}

	@OneToMany(mappedBy = "parentReport", targetEntity = ReportFilterDefn.class, cascade = CascadeType.ALL)
	private Set<ReportFilterInfo> getFiltersDirect() {
		return this.filters;
	}

	private void setFiltersDirect(Set<ReportFilterInfo> filters) {
		this.filters = filters;
	}

	public synchronized void addSort(ReportFieldInfo sortReportField, boolean ascendingSort)
			throws CantDoThatException, ObjectNotFoundException {
		BaseField sortBaseField = sortReportField.getBaseField();
		if (sortBaseField instanceof RelationField) {
			// if a relation, sort on the display field, not the key field
			BaseField displayField = ((RelationField) sortBaseField).getDisplayField();
			ReportFieldInfo displayReportField = sortBaseField.getTableContainingField()
					.getDefaultReport().getReportField(
							displayField.getInternalFieldName());
			ReportSort sort = new ReportSort(displayReportField, ascendingSort);
			// HibernateUtil.currentSession().save(sort);
			this.getSortsDirect().add(sort);
			// also check that the display field's table is in a join
			// so that including display field in the report SQL will work
			TableInfo relationLeftTable = ((RelationField) sortBaseField).getTableContainingField();
			TableInfo relationRightTable = ((RelationField) sortBaseField).getRelatedTable();
			boolean joinExists = false;
			for (JoinClauseInfo join : this.getJoinsDirect()) {
				if (join.isLeftPartTable() && join.isRightPartTable()) {
					TableInfo joinLeftTable = join.getLeftTableField().getTableContainingField();
					TableInfo joinRightTable = join.getRightTableField().getTableContainingField();
					if ((joinLeftTable.equals(relationLeftTable) && joinRightTable
							.equals(relationRightTable))
							|| (joinLeftTable.equals(relationRightTable) && joinRightTable
									.equals(relationLeftTable))) {
						joinExists = true;
						break;
					}
				}
			}
			// if there is no join between the related table, add one
			if (!joinExists) {
				logger.info("No join found, creating one to primary key "
						+ relationRightTable.getPrimaryKey());
				JoinClauseInfo sortJoin = new JoinClause(sortBaseField, relationRightTable
						.getPrimaryKey(), JoinType.INNER);
				this.getJoinsDirect().add(sortJoin);
			}
		} else {
			// field is just a normal one, not a relation
			ReportSort sort = new ReportSort(sortReportField, ascendingSort);
			// HibernateUtil.currentSession().save(sort);
			this.getSortsDirect().add(sort);
		}
	}

	public synchronized void updateSort(ReportFieldInfo sortReportField, boolean ascending)
			throws CantDoThatException, ObjectNotFoundException {
		for (ReportSortInfo reportSort : this.getSortsDirect()) {
			if (reportSort.getSortReportField().equals(sortReportField)) {
				reportSort.setSortDirection(ascending);
				return;
			}
		}
		// if execution reaches this line then the sort wasn't in the report
		throw new ObjectNotFoundException("Sorting is not presently applied to "
				+ sortReportField.getFieldName() + " in the report " + this.getReportName());
	}

	public synchronized ReportSortInfo removeSort(ReportFieldInfo sortReportField)
			throws CantDoThatException, ObjectNotFoundException {
		ReportSortInfo reportSortToRemove = null;
		for (ReportSortInfo reportSort : this.getSortsDirect()) {
			if (reportSort.getSortReportField().equals(sortReportField)) {
				reportSortToRemove = reportSort;
				break;
			}
		}
		if (reportSortToRemove != null) {
			this.getSortsDirect().remove(reportSortToRemove);
			return reportSortToRemove;
		}
		return null;
	}

	@Transient
	public synchronized SortedSet<ReportSortInfo> getSorts() {
		return Collections
				.unmodifiableSortedSet(new TreeSet<ReportSortInfo>(this.getSortsDirect()));
	}

	// Hibernate doesn't handle Map<Interface, Class>, replace it with
	// Set<InterfaceClassPair>
	@OneToMany(targetEntity = ReportSort.class, cascade = CascadeType.ALL)
	private Set<ReportSortInfo> getSortsDirect() {
		return this.sorts;
	}

	private void setSortsDirect(Set<ReportSortInfo> sorts) {
		this.sorts = sorts;
	}

	public synchronized void addDistinctField(BaseField field) throws ObjectNotFoundException {
		if (!this.getReportFieldsDirect().contains(field)) {
			throw new ObjectNotFoundException("Unable to find field '" + field.getFieldName()
					+ "' in report " + this.getReportName()
					+ " so cannot add to distinct fields set");
		}
		this.getDistinctFieldsDirect().add(field);
	}

	public synchronized void removeDistinctField(BaseField field) {
		this.getDistinctFieldsDirect().remove(field);
	}

	@Transient
	public synchronized Set<BaseField> getDistinctFields() {
		return Collections.unmodifiableSet(new LinkedHashSet<BaseField>(this
				.getDistinctFieldsDirect()));
	}

	@OneToMany(targetEntity = AbstractField.class)
	private synchronized Set<BaseField> getDistinctFieldsDirect() {
		return this.distinctFields;
	}

	private synchronized void setDistinctFieldsDirect(Set<BaseField> distinctFields) {
		this.distinctFields = distinctFields;
	}

	@Transient
	public synchronized SortedSet<TableInfo> getJoinedTables() throws CodingErrorException {
		SortedSet<TableInfo> tables = new TreeSet<TableInfo>();
		tables.add(this.getParentTable());
		for (JoinClauseInfo join : this.getJoinsDirect()) {
			try {
				if (join.isLeftPartTable()) {
					tables.add(join.getLeftTableField().getTableContainingField());
				}
				if (join.isRightPartTable()) {
					tables.add(join.getRightTableField().getTableContainingField());
				}
			} catch (CantDoThatException cdtex) {
				throw new CodingErrorException(
						"LHS or RHS of join is apparently a table but methods to return the table don't work",
						cdtex);
			}
		}
		return tables;
	}

	@Transient
	public synchronized SortedSet<BaseReportInfo> getJoinedReports() throws CodingErrorException {
		SortedSet<BaseReportInfo> reports = new TreeSet<BaseReportInfo>();
		reports.add(this);
		for (JoinClauseInfo join : this.getJoinsDirect()) {
			try {
				if (!join.isLeftPartTable()) {
					reports.add(join.getLeftReportField().getParentReport());
				}
				if (!join.isRightPartTable()) {
					reports.add(join.getRightReportField().getParentReport());
				}
			} catch (CantDoThatException cdtex) {
				throw new CodingErrorException(
						"Join object is inconsistent with regards to which types of object (tables or reports) are stored in it",
						cdtex);
			}
		}
		return reports;
	}

	@Transient
	public SortedSet<TableInfo> getJoinReferencedTables() throws CodingErrorException {
		SortedSet<TableInfo> joinReferencedTables = this.getJoinedTables();
		for (BaseReportInfo joinedReport : this.getJoinedReports()) {
			joinReferencedTables.add(joinedReport.getParentTable());
		}
		return joinReferencedTables;
	}

	/**
	 * Fields in the report, i.e columns in the view from the DB's point of
	 * view.
	 */
	private Set<ReportFieldInfo> reportFields = new HashSet<ReportFieldInfo>();

	/**
	 * Defines data filtering for the report
	 */
	private Set<ReportFilterInfo> filters = new LinkedHashSet<ReportFilterInfo>();

	/**
	 * A set of all the joins in the report
	 */
	private Set<JoinClauseInfo> joins = new HashSet<JoinClauseInfo>();

	private Set<BaseField> distinctFields = new LinkedHashSet<BaseField>();

	private Set<ReportSortInfo> sorts = new HashSet<ReportSortInfo>();

	private static final SimpleLogger logger = new SimpleLogger(SimpleReportDefn.class);
}
