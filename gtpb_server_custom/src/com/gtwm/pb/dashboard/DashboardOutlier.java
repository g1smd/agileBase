package com.gtwm.pb.dashboard;

import java.util.Calendar;

import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.DashboardOutlierInfo;
import com.gtwm.pb.model.interfaces.DataRowFieldInfo;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.fields.DateValue;

public class DashboardOutlier implements DashboardOutlierInfo, Comparable<DashboardOutlierInfo> {

	private DashboardOutlier() {
	}

	public DashboardOutlier(BaseReportInfo report, int rowID, BaseField field,
			DataRowFieldInfo dataRowField, DateValue lastModified) {
		this.report = report;
		this.rowID = rowID;
		this.field = field;
		this.dataRowField = dataRowField;
		this.lastModified = lastModified;
	}

	public DateValue getModificationDate() {
		return this.lastModified;
	}

	public BaseReportInfo getReport() {
		return this.report;
	}

	public BaseField getField() {
		return this.field;
	}

	public int getRowID() {
		return this.rowID;
	}

	public DataRowFieldInfo getDataRowField() {
		return this.dataRowField;
	}

	public String toString() {
		return "Outlier from " + this.report + ", row ID " + this.rowID + ", " + this.field + " = "
				+ this.dataRowField + ", last modified " + this.lastModified;
	}

	/**
	 * Compare by modification date first, then in a manner compatible with
	 * equals and hashcode
	 */
	public int compareTo(DashboardOutlierInfo otherOutlier) {
		Calendar thisDate = this.getModificationDate().getValueDate();
		Calendar otherDate = otherOutlier.getModificationDate().getValueDate();
		// In portalBase if a modification date is null then it is old
		// - before modification dates were introduced
		if (otherDate == null && thisDate != null) {
			return 1;
		}
		int comparison = 0;
		if (otherDate != null && thisDate != null) {
			comparison = otherDate.compareTo(thisDate);
		}
		if (comparison == 0) {
			comparison = this.report.compareTo(otherOutlier.getReport());
			if (comparison == 0) {
				comparison = this.rowID - otherOutlier.getRowID();
				if (comparison == 0) {
					comparison = this.field.compareTo(otherOutlier.getField());
				}
			}
		}
		return comparison;
	}

	/**
	 * equals is based on report, row ID and field
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		DashboardOutlier otherOutlier = (DashboardOutlier) obj;
		if (!this.report.equals(otherOutlier.getReport())) {
			return false;
		}
		if (this.rowID != otherOutlier.getRowID()) {
			return false;
		}
		if (this.field.equals(otherOutlier.getField())) {
			return false;
		}
		return true;
	}

	/**
	 * Hash code consistent with our equals
	 */
	public int hashCode() {
		if (this.hashCode == 0) {
			int result = 17;
			result = 37 * result + this.report.hashCode();
			result = 37 * result + this.rowID;
			result = 37 * result + this.field.hashCode();
			this.hashCode = result;
		}
		return this.hashCode;
	}

	private int rowID = -1;

	private DateValue lastModified = null;

	private BaseReportInfo report = null;

	private BaseField field = null;

	private DataRowFieldInfo dataRowField = null;

	private volatile int hashCode = 0;
}
