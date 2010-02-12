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

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.ManyToOne;
import org.grlea.log.SimpleLogger;
import com.gtwm.pb.model.interfaces.JoinClauseInfo;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.ReportFieldInfo;
import com.gtwm.pb.model.manageSchema.fields.AbstractField;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.RandomString;

@Entity
public class JoinClause implements JoinClauseInfo {

	protected JoinClause() {
	}

	/**
	 * Construct a table to table join
	 */
	public JoinClause(BaseField leftField, BaseField rightField, JoinType joinType) {
		this.setInternalJoinName((new RandomString()).toString());
		this.setJoinType(joinType);
		this.setLeftTableFieldDirect(leftField);
		this.setRightTableFieldDirect(rightField);
		this.setIsLeftPartTable(true);
		this.setIsRightPartTable(true);
		this.setCreationTimestampDirect(new Date());
	}

	/**
	 * Construct a table to report join
	 */
	public JoinClause(BaseField leftField, ReportFieldInfo rightReportField, JoinType joinType) {
		this.setInternalJoinName((new RandomString()).toString());
		this.setJoinType(joinType);
		this.setLeftTableFieldDirect(leftField);
		this.setRightReportFieldDirect(rightReportField);
		this.setIsLeftPartTable(true);
		this.setIsRightPartTable(false);
		this.setCreationTimestampDirect(new Date());
	}

	/**
	 * Construct a report to table join
	 */
	public JoinClause(ReportFieldInfo leftReportField, BaseField rightField, JoinType joinType) {
		this.setInternalJoinName((new RandomString()).toString());
		this.setJoinType(joinType);
		this.setLeftReportFieldDirect(leftReportField);
		this.setRightTableFieldDirect(rightField);
		this.setIsLeftPartTable(false);
		this.setIsRightPartTable(true);
		this.setCreationTimestampDirect(new Date());
	}

	/**
	 * Construct a report to report join
	 */
	public JoinClause(ReportFieldInfo leftReportField, ReportFieldInfo rightReportField,
			JoinType joinType) {
		this.setInternalJoinName((new RandomString()).toString());
		this.setJoinType(joinType);
		this.setLeftReportFieldDirect(leftReportField);
		this.setRightReportFieldDirect(rightReportField);
		this.setIsLeftPartTable(false);
		this.setIsRightPartTable(false);
		this.setCreationTimestampDirect(new Date());
	}

	@Transient
	public boolean isLeftPartTable() {
		return this.getIsLeftPartTable();
	}

	private Boolean getIsLeftPartTable() {
		return this.isLeftPartTable;
	}

	private void setIsLeftPartTable(Boolean isLeftPartTable) {
		this.isLeftPartTable = isLeftPartTable;
	}

	@Transient
	public boolean isRightPartTable() {
		return this.getIsRightPartTable();
	}

	private boolean getIsRightPartTable() {
		return this.isRightPartTable;
	}

	private void setIsRightPartTable(boolean isRightPartTable) {
		this.isRightPartTable = isRightPartTable;
	}

	@Transient
	public BaseField getLeftTableField() throws CantDoThatException {
		if (this.isLeftPartTable() == false) {
			throw new CantDoThatException("The left part of the join is a report not a table");
		}
		return this.getLeftTableFieldDirect();
	}

	@ManyToOne(targetEntity = AbstractField.class)
	private BaseField getLeftTableFieldDirect() {
		return this.leftField;
	}

	private void setLeftTableFieldDirect(BaseField leftField) {
		this.leftField = leftField;
	}

	@Transient
	public BaseField getRightTableField() throws CantDoThatException {
		if (this.isRightPartTable() == false) {
			throw new CantDoThatException("The right part of the join is a report not a table");
		}
		return this.getRightTableFieldDirect();
	}

	@ManyToOne(targetEntity = AbstractField.class)
	private BaseField getRightTableFieldDirect() {
		return this.rightField;
	}

	private void setRightTableFieldDirect(BaseField rightField) {
		this.rightField = rightField;
	}

	@Transient
	public ReportFieldInfo getLeftReportField() throws CantDoThatException {
		if (this.isLeftPartTable()) {
			throw new CantDoThatException("The left part of the join is a table not a report");
		}
		return this.getLeftReportFieldDirect();
	}

	@ManyToOne(targetEntity = AbstractReportField.class)
	private ReportFieldInfo getLeftReportFieldDirect() {
		return this.leftReportField;
	}

	private void setLeftReportFieldDirect(ReportFieldInfo leftReportField) {
		this.leftReportField = leftReportField;
	}

	@Transient
	public ReportFieldInfo getRightReportField() throws CantDoThatException {
		if (this.isRightPartTable()) {
			throw new CantDoThatException("The right part of the join is a table not a report");
		}
		return this.getRightReportFieldDirect();
	}

	@ManyToOne(targetEntity = AbstractReportField.class)
	private ReportFieldInfo getRightReportFieldDirect() {
		return this.rightReportField;
	}

	private void setRightReportFieldDirect(ReportFieldInfo rightReportField) {
		this.rightReportField = rightReportField;
	}

	@Enumerated(EnumType.STRING)
	public JoinType getJoinType() {
		return this.joinType;
	}

	private void setJoinType(JoinType joinType) {
		this.joinType = joinType;
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		BaseField leftField = this.getLeftTableFieldDirect();
		BaseField rightField = this.getRightTableFieldDirect();
		ReportFieldInfo leftReportField = this.getLeftReportFieldDirect();
		ReportFieldInfo rightReportField = this.getRightReportFieldDirect();
		if (!(obj instanceof JoinClauseInfo)) {
			return false;
		} else {
			JoinClause jc = (JoinClause) obj;
			int L1 = (this.isLeftPartTable()) ? 1 : 0;
			int R1 = (this.isRightPartTable()) ? 1 : 0;
			int L2 = (jc.isLeftPartTable()) ? 1 : 0;
			int R2 = (jc.isRightPartTable()) ? 1 : 0;
			int LSum = L1 + R1;
			int RSum = L2 + R2;
			if (LSum != RSum) {
				return false;
			} else {
				try {
					// compare to own join fields:
					if ((L1 == 1) && (R1 == 1) && (L2 == 1) && (R2 == 1)) {
						boolean regular = ((leftField.equals(jc.getLeftTableField())) && (rightField
								.equals(jc.getRightTableField())));
						boolean goofy = ((leftField.equals(jc.getRightTableField())) && (rightField
								.equals(jc.getLeftTableField())));
						return (regular || goofy);
					} else if ((L1 == 1) && (R1 == 0) && (L2 == 0) && (R2 == 1)) {
						return ((leftField.equals(jc.getRightTableField())) && (rightReportField
								.equals(jc.getLeftReportField())));
					} else if ((L1 == 0) && (R1 == 1) && (L2 == 1) && (R2 == 0)) {
						return ((leftReportField.equals(jc.getRightReportField())) && (rightField
								.equals(jc.getLeftTableField())));
					} else if ((L1 == 1) && (R1 == 0) && (L2 == 1) && (R2 == 0)) {
						return ((leftField.equals(jc.getLeftTableField())) && (rightReportField
								.equals(jc.getRightReportField())));
					} else if ((L1 == 0) && (R1 == 1) && (L2 == 0) && (R2 == 1)) {
						return ((leftReportField.equals(jc.getLeftReportField())) && (rightField
								.equals(jc.getRightTableField())));
					} else if ((L1 == 0) && (R1 == 0) && (L2 == 0) && (R2 == 0)) {
						boolean regular = ((leftReportField.equals(jc.getLeftReportField())) && (rightReportField
								.equals(jc.getRightReportField())));
						boolean goofy = ((leftReportField.equals(jc.getRightReportField())) && (rightReportField
								.equals(jc.getLeftReportField())));
						return (regular || goofy);
					} else {
						logger.error("JoinClause.equals() unhandled state!");
						return false; // this line should never run!
					}
				} catch (CantDoThatException cdtex) {
					logger.error("Call to get an incorrect join type: " + cdtex);
					return false;
				}
			}
		}
	}

	public int hashCode() {
		boolean isLeftPartTable = this.isLeftPartTable();
		boolean isRightPartTable = this.isRightPartTable();
		if (isLeftPartTable && isRightPartTable) {
			return Integer.valueOf(this.getLeftTableFieldDirect().hashCode()
					+ this.getRightTableFieldDirect().hashCode()).hashCode();
		} else if (!isLeftPartTable && !isRightPartTable) {
			return Integer.valueOf(this.getLeftReportFieldDirect().hashCode()
					+ this.getRightReportFieldDirect().hashCode()).hashCode();
		} else if (isLeftPartTable && !isRightPartTable) {
			// concat table+field+report+field
			return Integer.valueOf(this.getLeftTableFieldDirect().hashCode()
					+ this.getRightReportFieldDirect().hashCode()).hashCode();
		} else { // if (!isLeftPartTable && isRightPartTable) {
			// concat table+field+report+field
			return Integer.valueOf(this.getRightTableFieldDirect().hashCode()
					+ this.getLeftReportFieldDirect().hashCode()).hashCode();
		}
	}

	/**
	 * Compare based on time of object creation
	 */
	public int compareTo(JoinClauseInfo otherJoin) {
		Date thisCreationTimestamp = this.getCreationTimestamp();
		return thisCreationTimestamp.compareTo(otherJoin.getCreationTimestamp());
	}

	@Id
	public String getInternalJoinName() {
		return this.internalJoinName;
	}

	private void setInternalJoinName(String internalJoinName) {
		this.internalJoinName = internalJoinName;
	}

	private void setCreationTimestampDirect(Date creationTimestamp) {
		this.creationTimestamp = creationTimestamp;
	}

	@Transient
	public Date getCreationTimestamp() {
		// Change from java.sql.Timestamp which hibernate (sometimes?) returns to a Java Data object
		return new Date(this.getCreationTimestampDirect().getTime());
	}
	
	@Temporal(value = TemporalType.TIMESTAMP)
	private Date getCreationTimestampDirect() {
		return this.creationTimestamp;
	}

	public String toString() {
		String leftSide;
		String rightSide;
		if (this.isLeftPartTable()) {
			leftSide = this.getLeftTableFieldDirect().getTableContainingField().toString() + "."
					+ this.getLeftTableFieldDirect();
		} else {
			leftSide = this.getLeftReportFieldDirect().getParentReport().toString() + "."
					+ this.getLeftReportFieldDirect();
		}
		if (this.isRightPartTable()) {
			rightSide = this.getRightTableFieldDirect().getTableContainingField().toString() + "."
					+ this.getRightTableFieldDirect();
		} else {
			rightSide = this.getRightReportFieldDirect().getParentReport().toString() + "."
					+ this.getRightReportFieldDirect();
		}
		return leftSide + " " + this.getJoinType() + " join " + rightSide;
	}

	private String internalJoinName = null;

	private BaseField leftField = null;

	private BaseField rightField = null;

	private ReportFieldInfo leftReportField = null;

	private ReportFieldInfo rightReportField = null;

	private Boolean isLeftPartTable = true;

	private Boolean isRightPartTable = true;

	private JoinType joinType = JoinType.INNER;

	private Date creationTimestamp = null;

	private static final SimpleLogger logger = new SimpleLogger(JoinClause.class);
}
