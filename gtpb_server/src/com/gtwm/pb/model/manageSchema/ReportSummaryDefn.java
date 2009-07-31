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

import java.sql.PreparedStatement;
import java.util.Collections;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.EnumSet;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import org.grlea.log.SimpleLogger;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.ReportDataInfo;
import com.gtwm.pb.model.interfaces.ReportFieldInfo;
import com.gtwm.pb.model.interfaces.ReportQuickFilterInfo;
import com.gtwm.pb.model.interfaces.ReportSummaryInfo;
import com.gtwm.pb.model.interfaces.ReportSummaryGroupingInfo;
import com.gtwm.pb.model.interfaces.ReportSummaryAggregateInfo;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.manageData.ReportData;
import com.gtwm.pb.util.Enumerations.AggregateFunction;
import com.gtwm.pb.util.Enumerations.SummaryGroupingModifier;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.HibernateUtil;

@Entity
public class ReportSummaryDefn implements ReportSummaryInfo {

	protected ReportSummaryDefn() {
	}

	public ReportSummaryDefn(BaseReportInfo report) {
		this.setReport(report);
	}

	@Id
	@GeneratedValue
	/*
	 * Hibernate needs an ID for a persistent class - this isn't actually used
	 * by the app otherwise
	 */
	private long getId() {
		return this.id;
	}

	private void setId(long id) {
		this.id = id;
	}

	public synchronized void addGrouping(ReportFieldInfo groupByReportField,
			SummaryGroupingModifier groupingModifier) {
		ReportSummaryGroupingInfo grouping = new ReportSummaryGrouping(groupByReportField,
				groupingModifier);
		// Need a save here because no link from grouping back to report summary
		// so
		// Hibernate can't save automatically
		HibernateUtil.currentSession().save(grouping);
		this.getGroupingsDirect().add(grouping);
	}

	public synchronized ReportSummaryGroupingInfo removeGrouping(ReportFieldInfo reportFieldToRemove) {
		for (Iterator iterator = this.getGroupingsDirect().iterator(); iterator.hasNext();) {
			ReportSummaryGroupingInfo grouping = (ReportSummaryGroupingInfo) iterator.next();
			if (grouping.getGroupingReportField().equals(reportFieldToRemove)) {
				iterator.remove();
				HibernateUtil.currentSession().delete(grouping);
				return grouping;
			}
		}
		return null;
	}

	public synchronized void addFunction(ReportSummaryAggregateInfo addedAggFn)
			throws CantDoThatException {
		this.getAggregateFunctionsDirect().add(addedAggFn);
	}

	public synchronized Set<ReportSummaryAggregateInfo> removeFunctions(
			ReportFieldInfo reportFieldToRemove) {
		Set<ReportSummaryAggregateInfo> removedFunctions = new HashSet<ReportSummaryAggregateInfo>();
		for (Iterator<ReportSummaryAggregateInfo> iterator = this.getAggregateFunctionsDirect()
				.iterator(); iterator.hasNext();) {
			ReportSummaryAggregateInfo aggregateFunction = iterator.next();
			if (aggregateFunction.getReportField().equals(reportFieldToRemove)) {
				iterator.remove();
				removedFunctions.add(aggregateFunction);
				// No break - there may be more than one function acting on the
				// same field
			}
			ReportFieldInfo secondaryReportField = aggregateFunction.getSecondaryReportField();
			if (secondaryReportField != null) {
				if (secondaryReportField.equals(reportFieldToRemove)) {
					iterator.remove();
					removedFunctions.add(aggregateFunction);
				}
			}
		}
		return removedFunctions;
	}

	public ReportSummaryAggregateInfo removeFunction(String internalAggregateName)
			throws ObjectNotFoundException {
		for (Iterator<ReportSummaryAggregateInfo> iterator = this.getAggregateFunctionsDirect()
				.iterator(); iterator.hasNext();) {
			ReportSummaryAggregateInfo aggregateFunction = iterator.next();
			if (aggregateFunction.getInternalAggregateName().equals(internalAggregateName)) {
				iterator.remove();
				return aggregateFunction;
			}
		}
		throw new ObjectNotFoundException("Aggregate with internal name " + internalAggregateName
				+ " not found");
	}

	@Transient
	public synchronized PreparedStatement getReportSummarySqlPreparedStatement(Connection conn,
			Map<BaseField, String> filterValues, boolean exactFilters) throws SQLException,
			CantDoThatException {
		String groupByFieldsCsv = "";
		String aggregateFunctionsCsv = "";
		Set<ReportSummaryGroupingInfo> groupings = this.getGroupings();
		for (ReportSummaryGroupingInfo grouping : groupings) {
			String internalFieldName = grouping.getGroupingReportField().getInternalFieldName();
			SummaryGroupingModifier groupingModifier = grouping.getGroupingModifier();
			if (groupingModifier == null) {
				groupByFieldsCsv += internalFieldName;
			} else {
				switch (groupingModifier) {
				case DATE_YEAR:
					groupByFieldsCsv += "date_part('year'," + internalFieldName + ")";
					break;
				case DATE_QUARTER:
					groupByFieldsCsv += "date_part('quarter'," + internalFieldName + ")";
					break;
				case DATE_MONTH:
					groupByFieldsCsv += "date_part('month'," + internalFieldName + ")";
					break;
				case DATE_DAY:
					groupByFieldsCsv += "date_part('day'," + internalFieldName + ")";
					break;
				default:
					throw new CantDoThatException("Grouping by " + groupingModifier
							+ " not implemented");
				}
			}
			groupByFieldsCsv += ", ";
		}
		for (ReportSummaryAggregateInfo aggregateFunction : this.getAggregateFunctionsDirect()) {
			aggregateFunctionsCsv += aggregateFunction.getSQLPartForAggregate() + ", ";
		}
		// Remove trailing commas
		if (groupings.size() > 0) {
			groupByFieldsCsv = groupByFieldsCsv.substring(0, groupByFieldsCsv.length() - 2);
		}
		if (this.getAggregateFunctionsDirect().size() > 0) {
			aggregateFunctionsCsv = aggregateFunctionsCsv.substring(0, aggregateFunctionsCsv
					.length() - 2);
		}
		// Compose complete SQL
		ReportDataInfo reportData = new ReportData(conn, this.report, false, false);
		Map<String, List<ReportQuickFilterInfo>> whereClauseMap = reportData.getWhereClause(
				filterValues, exactFilters);
		String filterArgs = null;
		List<ReportQuickFilterInfo> filtersUsed = null;
		for (Map.Entry<String, List<ReportQuickFilterInfo>> whereClause : whereClauseMap.entrySet()) {
			filterArgs = whereClause.getKey();
			filtersUsed = whereClause.getValue();
		}
		String sqlForSummary = null;
		boolean validSummary = true;
		if ((groupings.size() > 0) && (this.getAggregateFunctionsDirect().size() > 0)) {
			sqlForSummary = "SELECT " + groupByFieldsCsv + ", " + aggregateFunctionsCsv;
			sqlForSummary += " FROM " + this.getReport().getInternalReportName();
			if (filterArgs.length() > 0) {
				sqlForSummary += " WHERE " + filterArgs;
			}
			sqlForSummary += " GROUP BY " + groupByFieldsCsv;
			sqlForSummary += " ORDER BY " + groupByFieldsCsv;
		} else if (this.getAggregateFunctionsDirect().size() > 0) {
			sqlForSummary = "SELECT " + aggregateFunctionsCsv + " FROM "
					+ this.getReport().getInternalReportName();
			if (filterArgs.length() > 0) {
				sqlForSummary += " WHERE " + filterArgs;
			}
		} else if (groupings.size() > 0) {
			sqlForSummary = "SELECT " + groupByFieldsCsv + " FROM "
					+ this.getReport().getInternalReportName();
			if (filterArgs.length() > 0) {
				sqlForSummary += " WHERE " + filterArgs;
			}
			sqlForSummary += " GROUP BY " + groupByFieldsCsv;
			sqlForSummary += " ORDER BY " + groupByFieldsCsv;
		} else {
			// no grouping fields and no aggregate functions means no report
			// summary
			sqlForSummary = "SELECT '' where false";
			validSummary = false;
		}
		PreparedStatement statement = conn.prepareStatement(sqlForSummary);
		if (validSummary) {
			reportData.fillInFilterValues(filtersUsed, statement);
		}
		return statement;
	}

	@Transient
	public synchronized List<ReportFieldInfo> getGroupingReportFields() {
		List<ReportFieldInfo> groupingReportFields = new LinkedList<ReportFieldInfo>();
		for (ReportSummaryGroupingInfo grouping : this.getGroupings()) {
			groupingReportFields.add(grouping.getGroupingReportField());
		}
		return Collections.unmodifiableList(groupingReportFields);
	}

	@Transient
	public SortedSet<ReportSummaryGroupingInfo> getGroupings() {
		SortedSet<ReportSummaryGroupingInfo> groupings = new TreeSet<ReportSummaryGroupingInfo>(
				this.getGroupingsDirect());
		return Collections.unmodifiableSortedSet(groupings);
	}

	@OneToMany(targetEntity = ReportSummaryGrouping.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	// Uni-directional
	private Set<ReportSummaryGroupingInfo> getGroupingsDirect() {
		return this.groupings;
	}

	private void setGroupingsDirect(Set<ReportSummaryGroupingInfo> groupingFields) {
		this.groupings = groupingFields;
	}

	public synchronized ReportFieldInfo getGroupingReportField(String internalFieldName)
			throws ObjectNotFoundException {
		for (ReportSummaryGroupingInfo grouping : this.getGroupings()) {
			ReportFieldInfo reportField = grouping.getGroupingReportField();
			if (reportField.getInternalFieldName().equals(internalFieldName)) {
				return reportField;
			}
		}
		throw new ObjectNotFoundException("Field with ID " + internalFieldName
				+ " isn't a grouping field in the " + this.getReport() + " report summary");
	}

	@Transient
	public synchronized Set<ReportSummaryAggregateInfo> getAggregateFunctions() {
		return Collections.unmodifiableSet(new LinkedHashSet<ReportSummaryAggregateInfo>(this
				.getAggregateFunctionsDirect()));
	}

	@Transient
	public synchronized ReportSummaryAggregateInfo getAggregateFunctionByInternalName(
			String internalAggregateName) throws ObjectNotFoundException {
		for (ReportSummaryAggregateInfo aggFn : this.getAggregateFunctionsDirect()) {
			if (aggFn.getInternalAggregateName().equals(internalAggregateName)) {
				return aggFn;
			}
		}
		throw new ObjectNotFoundException("Aggregate with the internal name "
				+ internalAggregateName + " not found in summary");
	}

	@OneToMany(targetEntity = ReportSummaryAggregateDefn.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	// Uni-directional one to many
	private Set<ReportSummaryAggregateInfo> getAggregateFunctionsDirect() {
		return this.aggregateFunctions;
	}

	private void setAggregateFunctionsDirect(Set<ReportSummaryAggregateInfo> aggregateFunctions) {
		this.aggregateFunctions = aggregateFunctions;
	}

	public synchronized boolean containsNumericAggFns() {
		for (ReportSummaryAggregateInfo aggregateFunction : this.getAggregateFunctionsDirect()) {
			if (!(aggregateFunction.isCountFunction())) {
				return true;
			}
		}
		return false;
	}

	@OneToOne(mappedBy = "reportSummary", targetEntity = BaseReportDefn.class)
	// @ManyToOne(targetEntity=BaseReportDefn.class)
	public BaseReportInfo getReport() {
		return this.report;
	}

	private void setReport(BaseReportInfo report) {
		this.report = report;
	}

	/**
	 * Return the list of legitimate aggregate functions
	 */
	@Transient
	public static EnumSet<AggregateFunction> getPossibleFunctionTypes() {
		return EnumSet.allOf(AggregateFunction.class);
	}

	public String toString() {
		return this.getReport().getReportName() + " summary schema - functions: "
				+ this.getAggregateFunctionsDirect() + ", groupings: " + this.getGroupings();
	}

	private Set<ReportSummaryGroupingInfo> groupings = new HashSet<ReportSummaryGroupingInfo>();

	private Set<ReportSummaryAggregateInfo> aggregateFunctions = new LinkedHashSet<ReportSummaryAggregateInfo>();

	private BaseReportInfo report;

	private long id;

	private static final SimpleLogger logger = new SimpleLogger(ReportSummaryDefn.class);
}
