package com.gtwm.pb.dashboard.interfaces;

import java.util.Set;
import java.util.SortedSet;

/**
 * Represents the dashboard for a company
 */
public interface DashboardInfo {
	
	/**
	 * Return list of exceptions found by looking through all reports in the company
	 */
	public SortedSet<DashboardOutlierInfo> getDashboardOutliers();
	
	public Set<DashboardTrendOutlierInfo> getDashboardTrendOutliers();
	
	/**
	 * Get report summary charts that agileBase automatically selects
	 */
	public SortedSet<DashboardChartInfo> getSuggestedReportSummaries();
	
	/**
	 * Return a set of headlines highlighting particular data from agileBase or the logs
	 */
	public Set<String> getHeadlineNotices();
	
	/**
	 * Return a grid object that can be used to lay out charts in the dashboard
	 * @param containerWidth
	 *            The pixel width of the space available for this grid
	 * @param widthUnit
	 *            The pixel width of individual blocks to go into the grid
	 * @param heightUnit
	 *            The pixel height of individual blocks to go into the grid
	 */
	public DashboardGridInfo getLayoutGrid(int containerWidth, int widthUnit, int heightUnit);
}
