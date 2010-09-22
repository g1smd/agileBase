package com.gtwm.pb.model.manageUsage;

import java.util.SortedSet;
import com.gtwm.pb.model.interfaces.ReportViewStatsInfo;
import com.gtwm.pb.model.interfaces.UserReportViewStatsInfo;

public class ReportViewStats implements ReportViewStatsInfo {

	private ReportViewStats() {
		this.averageViews = 0;
		this.percentageIncrease = 0;
		this.userStats = null;
	}
	
	public ReportViewStats(int averageViews, int percentageIncrease, SortedSet<UserReportViewStatsInfo> userStats) {
		this.averageViews = averageViews;
		this.percentageIncrease = percentageIncrease;
		this.userStats = userStats;
	}
	
	public int getAverageViews() {
		return this.averageViews;
	}

	public int getPercentageIncrease() {
		return this.percentageIncrease;
	}

	public SortedSet<UserReportViewStatsInfo> getUserStats() {
		return this.userStats;
	}

	private final int averageViews;
	
	private final int percentageIncrease;
	
	private final SortedSet<UserReportViewStatsInfo> userStats;
}
