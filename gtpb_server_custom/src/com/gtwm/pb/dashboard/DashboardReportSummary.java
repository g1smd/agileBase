package com.gtwm.pb.dashboard;

import com.gtwm.pb.model.interfaces.DashboardReportSummaryInfo;
import com.gtwm.pb.model.interfaces.ReportSummaryDataInfo;
import com.gtwm.pb.model.interfaces.ReportSummaryInfo;

public class DashboardReportSummary implements DashboardReportSummaryInfo,
		Comparable<DashboardReportSummary> {

	private DashboardReportSummary() {
	}

	public DashboardReportSummary(ReportSummaryInfo summary, ReportSummaryDataInfo summaryData,
			int score) {
		this.score = score;
		this.summary = summary;
		this.summaryData = summaryData;
	}

	public ReportSummaryInfo getReportSummary() {
		return this.summary;
	}

	public ReportSummaryDataInfo getReportSummaryData() {
		return this.summaryData;
	}

	public int getScore() {
		return this.score;
	}

	/**
	 * equals is based on report
	 */
	public boolean equals(Object obj) {
		return this.summary.getReport().equals(
				((DashboardReportSummaryInfo) obj).getReportSummary().getReport());
	}
	
	/**
	 * Hash code consistent with our equals
	 */
	public int hashCode() {
		return this.summary.getReport().hashCode();
	}
	
	public int compareTo(DashboardReportSummary otherDashboardSummary) {
		int scoreCompare = otherDashboardSummary.getScore() - this.getScore();
		if (scoreCompare != 0) {
			return scoreCompare;
		}
		// for consistency with equals
		return this.summary.getReport().compareTo(otherDashboardSummary.getReportSummary().getReport());
	}

	private int score = 0;

	private ReportSummaryDataInfo summaryData = null;

	private ReportSummaryInfo summary = null;

}
