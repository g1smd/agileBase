package com.gtwm.pb.model.interfaces;

import java.util.SortedSet;

/**
 * Usage statistics about a particular report
 */
public interface ReportViewStatsInfo {

	/**
	 * Return information about users who've been accessing the report
	 */
	SortedSet<UserReportViewStatsInfo> getUserStats();

	/**
	 * Return the average number of report views per month
	 */
	public int getAverageViews();

	/**
	 * Return the percentage increase in views of this report for this month
	 * compared to all views. A negative value is returned if there's a decrease
	 */
	public int getPercentageIncrease();
}
