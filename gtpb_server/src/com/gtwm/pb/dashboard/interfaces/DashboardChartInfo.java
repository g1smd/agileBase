package com.gtwm.pb.dashboard.interfaces;

import com.gtwm.pb.model.interfaces.ChartInfo;

/**
 * Wraps a report summary with a bit of additional info useful when displaying it in a dashboard
 * @see ChartDataInfo
 */
public interface DashboardChartInfo {
	
	public ChartInfo getChart();
	
	/**
	 * Return a score that ranks the summary in the dashboard
	 */
	public int getScore();
	
}
