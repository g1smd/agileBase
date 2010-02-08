package com.gtwm.pb.model.interfaces;

/**
 * Wraps a report summary with a bit of additional info useful when displaying it in a dashboard
 * @see ReportSummaryDataInfo
 */
public interface DashboardReportSummaryInfo {
	
	public ReportSummaryInfo getReportSummary();
	
	/**
	 * Return a score that ranks the summary in the dashboard
	 */
	public int getScore();
	
}
