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

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import org.grlea.log.SimpleLogger;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.ReportDataInfo;
import com.gtwm.pb.model.interfaces.ReportFieldInfo;
import com.gtwm.pb.model.interfaces.ReportQuickFilterInfo;
import com.gtwm.pb.model.interfaces.ChartInfo;
import com.gtwm.pb.model.interfaces.ChartGroupingInfo;
import com.gtwm.pb.model.interfaces.ChartAggregateInfo;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.manageData.ReportData;
import com.gtwm.pb.util.Enumerations.AggregateFunction;
import com.gtwm.pb.util.Enumerations.SummaryFilter;
import com.gtwm.pb.util.Enumerations.SummaryGroupingModifier;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.HibernateUtil;

@Entity
public class ChartDefn implements ChartInfo, Comparable<ChartInfo> {

	protected ChartDefn() {
	}

	/**
	 * @param report
	 *            The report the summary is based on
	 * @param persist
	 *            Whether to persist the summary to permanent storage
	 */
	public ChartDefn(BaseReportInfo report, boolean persist) {
		this.setReport(report);
		this.persist = persist;
	}

	public ChartDefn(BaseReportInfo report, String title, boolean persist) {
		this.setReport(report);
		this.setTitle(title);
		this.persist = persist;
	}

	@Id
	@GeneratedValue
	public long getId() {
		return this.id;
	}

	private void setId(long id) {
		this.id = id;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public synchronized void addGrouping(ReportFieldInfo groupByReportField,
			SummaryGroupingModifier groupingModifier) {
		ChartGroupingInfo grouping = new ChartGrouping(groupByReportField, groupingModifier);
		if (this.persist) {
			// Need a save here because no link from grouping back to report
			// summary
			// so Hibernate can't save automatically
			HibernateUtil.currentSession().save(grouping);
		}
		this.getGroupingsDirect().add(grouping);
	}

	public synchronized ChartGroupingInfo removeGrouping(ReportFieldInfo reportFieldToRemove) {
		for (Iterator<ChartGroupingInfo> iterator = this.getGroupingsDirect().iterator(); iterator
				.hasNext();) {
			ChartGroupingInfo grouping = iterator.next();
			if (grouping.getGroupingReportField().equals(reportFieldToRemove)) {
				iterator.remove();
				if (this.persist) {
					HibernateUtil.currentSession().delete(grouping);
				}
				return grouping;
			}
		}
		return null;
	}

	public synchronized void addFunction(ChartAggregateInfo addedAggFn) throws CantDoThatException {
		this.getAggregateFunctionsDirect().add(addedAggFn);
	}

	public synchronized Set<ChartAggregateInfo> removeFunctions(ReportFieldInfo reportFieldToRemove) {
		Set<ChartAggregateInfo> removedFunctions = new HashSet<ChartAggregateInfo>();
		for (Iterator<ChartAggregateInfo> iterator = this.getAggregateFunctionsDirect().iterator(); iterator
				.hasNext();) {
			ChartAggregateInfo aggregateFunction = iterator.next();
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

	public ChartAggregateInfo removeFunction(String internalAggregateName)
			throws ObjectNotFoundException {
		for (Iterator<ChartAggregateInfo> iterator = this.getAggregateFunctionsDirect().iterator(); iterator
				.hasNext();) {
			ChartAggregateInfo aggregateFunction = iterator.next();
			if (aggregateFunction.getInternalAggregateName().equals(internalAggregateName)) {
				iterator.remove();
				return aggregateFunction;
			}
		}
		throw new ObjectNotFoundException("Aggregate with internal name " + internalAggregateName
				+ " not found");
	}

	@Transient
	public synchronized PreparedStatement getChartSqlPreparedStatement(Connection conn,
			Map<BaseField, String> filterValues, boolean exactFilters) throws SQLException,
			CantDoThatException {
		String groupByFieldsCsv = "";
		String aggregateFunctionsCsv = "";
		Set<ChartGroupingInfo> groupings = this.getGroupings();
		boolean groupingsContainDateField = false;
		for (ChartGroupingInfo grouping : groupings) {
			String internalFieldName = grouping.getGroupingReportField().getInternalFieldName();
			SummaryGroupingModifier groupingModifier = grouping.getGroupingModifier();
			if (groupingModifier == null) {
				groupByFieldsCsv += internalFieldName;
			} else {
				groupingsContainDateField = true;
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
		for (ChartAggregateInfo aggregateFunction : this.getAggregateFunctionsDirect()) {
			aggregateFunctionsCsv += aggregateFunction.getSQLPartForAggregate() + ", ";
		}
		// Remove trailing commas
		if (groupings.size() > 0) {
			groupByFieldsCsv = groupByFieldsCsv.substring(0, groupByFieldsCsv.length() - 2);
		}
		if (this.getAggregateFunctionsDirect().size() > 0) {
			aggregateFunctionsCsv = aggregateFunctionsCsv.substring(0,
					aggregateFunctionsCsv.length() - 2);
			aggregateFunctionsCsv = aggregateFunctionsCsv.replace(
					"agilebase_custom_variable_groupings", groupByFieldsCsv);
		}
		// Compose complete SQL
		ReportDataInfo reportData = new ReportData(conn, this.report, false, false);
		Map<String, List<ReportQuickFilterInfo>> whereClauseMap = reportData.getWhereClause(
				filterValues, exactFilters);
		String filterArgs = null;
		List<ReportQuickFilterInfo> filtersUsed = null;
		// TODO: there is only one WHERE clause - there should be an improvement
		// over a loop
		for (Map.Entry<String, List<ReportQuickFilterInfo>> whereClause : whereClauseMap.entrySet()) {
			filterArgs = whereClause.getKey();
			filtersUsed = whereClause.getValue();
		}
		// Add permanent filters if there are any
		SummaryFilter summaryFilter = this.getChartFilter();
		ReportFieldInfo filterReportField = this.getFilterReportField();
		if (summaryFilter != null && filterReportField != null) {
			String filterSQL = summaryFilter.getSQL();
			if (filterArgs.length() > 0) {
				filterArgs += " AND ";
			}
			filterArgs += filterSQL.replace("{fieldvalue}",
					filterReportField.getInternalFieldName());
		}
		String sqlForSummary = null;
		boolean validSummary = true;
		String sortDirection = " DESC";
		if (this.getRangeDirection()) {
			sortDirection = " ASC";
		}
		if ((groupings.size() > 0) && (this.getAggregateFunctionsDirect().size() > 0)) {
			sqlForSummary = "SELECT " + groupByFieldsCsv + ", " + aggregateFunctionsCsv;
			sqlForSummary += " FROM " + this.getReport().getInternalReportName();
			if (filterArgs.length() > 0) {
				sqlForSummary += " WHERE " + filterArgs;
			}
			sqlForSummary += " GROUP BY " + groupByFieldsCsv;
			if (groupingsContainDateField) {
				sqlForSummary += " ORDER BY " + groupByFieldsCsv;
				sqlForSummary += sortDirection;
			} else {
				String oppositeSortDirection = " ASC";
				if (this.getRangeDirection()) {
					oppositeSortDirection = " DESC";
				}
				sqlForSummary += " ORDER BY "
						+ aggregateFunctionsCsv.replace(",", oppositeSortDirection + ",");
				sqlForSummary += oppositeSortDirection;
			}
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
			sqlForSummary += " ORDER BY " + groupByFieldsCsv.replace(",", sortDirection + ",");
			sqlForSummary += sortDirection;
		} else {
			// no grouping fields and no aggregate functions means no report
			// summary
			sqlForSummary = "SELECT '' where false";
			validSummary = false;
		}
		int rangePercent = this.getRangePercent();
		if (rangePercent < 100) {
			// TODO: perhaps a window function cume_dist or percentage_rank
			// would be faster than a subselect
			double rangeFraction = ((double) rangePercent) / 100;
			String internalReportName =  this.getReport().getInternalReportName();
			sqlForSummary += " LIMIT (";
			sqlForSummary += " SELECT (count(*) * " + rangeFraction + ")::integer";
			if (groupings.size() > 0) {
				sqlForSummary += " FROM (SELECT " + groupByFieldsCsv + " FROM " + internalReportName;
				sqlForSummary += " GROUP BY " + groupByFieldsCsv + ") AS grouped";
			} else {
				sqlForSummary += " FROM " + internalReportName;
			}
			sqlForSummary += ")"; // close LIMIT
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
		for (ChartGroupingInfo grouping : this.getGroupings()) {
			groupingReportFields.add(grouping.getGroupingReportField());
		}
		return Collections.unmodifiableList(groupingReportFields);
	}

	@Transient
	public SortedSet<ChartGroupingInfo> getGroupings() {
		SortedSet<ChartGroupingInfo> groupings = new TreeSet<ChartGroupingInfo>(
				this.getGroupingsDirect());
		return Collections.unmodifiableSortedSet(groupings);
	}

	@OneToMany(targetEntity = ChartGrouping.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	// Uni-directional
	private Set<ChartGroupingInfo> getGroupingsDirect() {
		return this.groupings;
	}

	private void setGroupingsDirect(Set<ChartGroupingInfo> groupingFields) {
		this.groupings = groupingFields;
	}

	public synchronized ReportFieldInfo getGroupingReportField(String internalFieldName)
			throws ObjectNotFoundException {
		for (ChartGroupingInfo grouping : this.getGroupings()) {
			ReportFieldInfo reportField = grouping.getGroupingReportField();
			if (reportField.getInternalFieldName().equals(internalFieldName)) {
				return reportField;
			}
		}
		throw new ObjectNotFoundException("Field with ID " + internalFieldName
				+ " isn't a grouping field in the " + this.getReport() + " report summary");
	}

	@Transient
	public synchronized Set<ChartAggregateInfo> getAggregateFunctions() {
		return Collections.unmodifiableSet(new LinkedHashSet<ChartAggregateInfo>(this
				.getAggregateFunctionsDirect()));
	}

	@Transient
	public synchronized ChartAggregateInfo getAggregateFunctionByInternalName(
			String internalAggregateName) throws ObjectNotFoundException {
		for (ChartAggregateInfo aggFn : this.getAggregateFunctionsDirect()) {
			if (aggFn.getInternalAggregateName().equals(internalAggregateName)) {
				return aggFn;
			}
		}
		throw new ObjectNotFoundException("Aggregate with the internal name "
				+ internalAggregateName + " not found in summary");
	}

	@OneToMany(targetEntity = ChartAggregateDefn.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	// Uni-directional one to many
	private Set<ChartAggregateInfo> getAggregateFunctionsDirect() {
		return this.aggregateFunctions;
	}

	private void setAggregateFunctionsDirect(Set<ChartAggregateInfo> aggregateFunctions) {
		this.aggregateFunctions = aggregateFunctions;
	}

	public synchronized boolean containsNumericAggFns() {
		for (ChartAggregateInfo aggregateFunction : this.getAggregateFunctionsDirect()) {
			if (!(aggregateFunction.isCountFunction())) {
				return true;
			}
		}
		return false;
	}

	@ManyToOne(targetEntity = BaseReportDefn.class)
	// @OneToOne(mappedBy = "reportSummary", targetEntity =
	// BaseReportDefn.class)
	public BaseReportInfo getReport() {
		return this.report;
	}

	private void setReport(BaseReportInfo report) {
		this.report = report;
	}

	@Enumerated(EnumType.STRING)
	public SummaryFilter getChartFilter() {
		return this.chartFilter;
	}

	public void setChartFilter(SummaryFilter summaryFilter) {
		this.chartFilter = summaryFilter;
	}

	@ManyToOne(targetEntity = ReportFieldDefn.class)
	public ReportFieldInfo getFilterReportField() {
		return this.filterReportField;
	}

	public void setFilterReportField(ReportFieldInfo filterReportField) {
		this.filterReportField = filterReportField;
	}

	@Basic
	public int getRangePercent() {
		return this.rangePercent;
	}

	public void setRangePercent(int rangePercent) {
		this.rangePercent = rangePercent;
	}

	@Basic
	public boolean getRangeDirection() {
		return this.rangeDirection;
	}

	public void setRangeDirection(boolean rangeDirection) {
		this.rangeDirection = rangeDirection;
	}

	/**
	 * Return the list of legitimate aggregate functions
	 */
	@Transient
	public static EnumSet<AggregateFunction> getPossibleFunctionTypes() {
		return EnumSet.allOf(AggregateFunction.class);
	}

	public String toString() {
		String toString = this.getReport().toString() + " - " + this.getTitle() + "("
				+ this.getId() + ")" + " summary schema - functions: "
				+ this.getAggregateFunctions() + ", groupings: " + this.getGroupings();
		return toString;
	}

	/**
	 * equals is based on id
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		if (this.getId() == ((ChartDefn) obj).getId()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Compare by report then ID
	 */
	public int compareTo(ChartInfo otherSummary) {
		int reportCompare = this.getReport().compareTo(otherSummary.getReport());
		if (reportCompare != 0) {
			return reportCompare;
		}
		long thisId = this.getId();
		long otherId = otherSummary.getId();
		if (thisId > otherId) {
			return 1;
		} else if (thisId < otherId) {
			return -1;
		} else {
			return 0;
		}
	}

	/**
	 * hashCode consistent with equals
	 */
	public int hashCode() {
		if (this.hashCode == 0) {
			this.hashCode = (new Long(this.getId())).hashCode();
		}
		return this.hashCode;
	}

	private volatile int hashCode = 0;

	private Set<ChartGroupingInfo> groupings = new HashSet<ChartGroupingInfo>();

	private Set<ChartAggregateInfo> aggregateFunctions = new LinkedHashSet<ChartAggregateInfo>();

	private SummaryFilter chartFilter = null;

	private ReportFieldInfo filterReportField = null;

	private BaseReportInfo report;

	private String title = "";

	private int rangePercent = 100;

	private boolean rangeDirection = UPPER_RANGE;

	private boolean persist = true;

	private long id;

	private static final SimpleLogger logger = new SimpleLogger(ChartDefn.class);
}
