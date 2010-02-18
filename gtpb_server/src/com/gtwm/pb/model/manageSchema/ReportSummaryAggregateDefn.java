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

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import org.grlea.log.SimpleLogger;
import com.gtwm.pb.model.interfaces.ReportSummaryAggregateInfo;
import com.gtwm.pb.model.interfaces.ReportFieldInfo;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.util.RandomString;
import com.gtwm.pb.util.Enumerations.AggregateFunction;
import com.gtwm.pb.util.CantDoThatException;

@Entity
public class ReportSummaryAggregateDefn implements ReportSummaryAggregateInfo {

	protected ReportSummaryAggregateDefn() {
	}

	/**
	 * @throws CantDoThatException
	 *             If the function isn't a recognised aggregate function from
	 *             the AggregateFunction enumeration
	 * @see com.gtwm.pb.util.Enumerations.AggregateFunction
	 */
	public ReportSummaryAggregateDefn(AggregateFunction function, ReportFieldInfo reportField) {
		this.setAggregateFunction(function);
		this.setReportField(reportField);
		this.setInternalAggregateName((new RandomString()).toString());
	}

	/**
	 * Create an aggregate that acts on two fields, e.g. a weighted average
	 */
	public ReportSummaryAggregateDefn(AggregateFunction function, ReportFieldInfo reportField,
			ReportFieldInfo secondaryReportField) throws CantDoThatException {
		if (secondaryReportField == null) {
			throw new CantDoThatException(
					"Secondary report field null. To create an aggregate on a single field, use the single field constructor");
		}
		this.setAggregateFunction(function);
		this.setReportField(reportField);
		this.setSecondaryReportField(secondaryReportField);
		this.setInternalAggregateName((new RandomString()).toString());
	}

	@Id
	public String getInternalAggregateName() {
		return this.internalAggregateName;
	}

	private void setInternalAggregateName(String internalAggregateName) {
		this.internalAggregateName = internalAggregateName;
	}

	@Transient
	public String getSQLPartForAggregate() throws CantDoThatException {
		ReportFieldInfo reportField = this.getReportField();
		String aggregateArgument = reportField.getInternalFieldName();
		if (this.getAggregateFunction().equals(AggregateFunction.COUNT)) {
			BaseField field = reportField.getBaseField();
			if (!field.equals(field.getTableContainingField().getPrimaryKey())) {
				aggregateArgument = "DISTINCT " + aggregateArgument;
			}
		}
		// weighted average is a special case that requires it's own definition
		// due to using two fields
		if (this.getAggregateFunction().equals(AggregateFunction.WTDAVG)) {
			if (this.getSecondaryReportField() == null) {
				throw new CantDoThatException("Must have two fields to create a weighted average");
			}
			String secondaryAggregateArgument = this.getSecondaryReportField()
					.getInternalFieldName();
			return ("(sum(" + secondaryAggregateArgument + ") / sum(" + aggregateArgument + ")) * 100");
		} else {
			String aggregateFunction = this.getAggregateFunction().toString().toLowerCase();
			return aggregateFunction + "(" + aggregateArgument + ")";
		}
	}

	@ManyToOne(targetEntity = AbstractReportField.class)
	// Uni-directional. Each field may appear in many aggregate objects
	public ReportFieldInfo getReportField() {
		return this.reportField;
	}

	private void setReportField(ReportFieldInfo reportField) {
		this.reportField = reportField;
	}

	@ManyToOne(targetEntity = AbstractReportField.class)
	// Uni-directional. Each field may appear in many aggregate objects
	public ReportFieldInfo getSecondaryReportField() {
		return this.secondaryReportField;
	}

	private void setSecondaryReportField(ReportFieldInfo reportField) {
		this.secondaryReportField = reportField;
	}

	@Transient
	public boolean isCountFunction() {
		return this.getAggregateFunction().equals(AggregateFunction.COUNT);
	}

	private String yAxisLabel() {
		return this.getAggregateFunction().getLabel();
	}

	@Enumerated(EnumType.STRING)
	public AggregateFunction getAggregateFunction() {
		return this.function;
	}

	private void setAggregateFunction(AggregateFunction function) {
		this.function = function;
	}

	/**
	 * @return A description of the aggregate function made up of the function
	 *         description and the field(s) it acts on, e.g. "Maximum(Age)"
	 */
	public String toString() {
		ReportFieldInfo reportField = this.getReportField();
		if (this.getAggregateFunction().equals(AggregateFunction.WTDAVG)) {
			return "Weighted " + this.getSecondaryReportField() + " / " + reportField
					+ " %";
		} else if (reportField.getBaseField().equals(reportField.getBaseField().getTableContainingField().getPrimaryKey())) {
			return this.yAxisLabel();
		} else if (this.isCountFunction()) {
			return this.yAxisLabel() + "(" + reportField + ")";
		} else {
			return this.yAxisLabel() + " " + reportField;
		}
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		ReportSummaryAggregateDefn otherAggregateFunction = (ReportSummaryAggregateDefn) obj;
		return this.getInternalAggregateName().equals(
				otherAggregateFunction.getInternalAggregateName());
	}

	public int hashCode() {
		return this.getInternalAggregateName().hashCode();
	}

	/**
	 * Stores the aggregate function name
	 */
	private AggregateFunction function = null;

	/**
	 * Stores the report field that the aggregate function operates on
	 */
	private ReportFieldInfo reportField = null;

	private ReportFieldInfo secondaryReportField = null;

	private String internalAggregateName = null;

	private static final SimpleLogger logger = new SimpleLogger(ReportSummaryAggregateDefn.class);
}
