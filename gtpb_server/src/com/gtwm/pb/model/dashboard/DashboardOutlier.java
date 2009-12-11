package com.gtwm.pb.model.dashboard;

import java.util.Calendar;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.DashboardOutlierInfo;
import com.gtwm.pb.model.interfaces.ReportFieldInfo;

public class DashboardOutlier implements DashboardOutlierInfo, Comparable<DashboardOutlierInfo> {

	private DashboardOutlier() {
	}
	
	public DashboardOutlier(BaseReportInfo report, int rowID, ReportFieldInfo reportField, double value, double mean, double stdDev, Calendar modificationDate) {
		this.report = report;
		this.rowID = rowID;
		this.reportField = reportField;
		this.value = value;
		this.mean = mean;
		this.stdDev = stdDev;
		this.modificationDate = modificationDate;
	}
	
	public double getMean() {
		return this.mean;
	}

	public Calendar getModificationDate() {
		return this.modificationDate;
	}

	public BaseReportInfo getReport() {
		return this.report;
	}

	public ReportFieldInfo getReportField() {
		return this.reportField;
	}

	public int getRowID() {
		return this.rowID;
	}

	public double getStdDev() {
		return this.stdDev;
	}

	public double getValue() {
		return this.value;
	}

	/**
	 * Compare by modification date first, then in a manner compatible with equals and hashcode
	 */
	public int compareTo(DashboardOutlierInfo otherOutlier) {
		int comparison = this.modificationDate.compareTo(otherOutlier.getModificationDate());
		if (comparison == 0) {
			comparison = this.report.compareTo(otherOutlier.getReport());
			if (comparison == 0) {
				comparison = this.rowID - otherOutlier.getRowID();
				if (comparison == 0) {
					comparison = this.reportField.compareTo(otherOutlier.getReportField());
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
		if (this.reportField.equals(otherOutlier.getReportField())) {
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
			result = 37 * result + this.reportField.hashCode();
			this.hashCode = result;
		}
		return this.hashCode;
	}
	
	private int rowID = -1;
	
	private double value = 0;
	
	private double stdDev = 0;
	
	private double mean = 0;
	
	private Calendar modificationDate = null;
	
	private BaseReportInfo report = null;
	
	private ReportFieldInfo reportField = null;
	
	private volatile int hashCode = 0;
}
