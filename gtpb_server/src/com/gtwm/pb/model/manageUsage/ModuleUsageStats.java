/*
 *  Copyright 2009 GT webMarque Ltd
 * 
 *  This file is part of GT portalBase.
 *
 *  GT portalBase is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  GT portalBase is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with GT portalBase.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gtwm.pb.model.manageUsage;

import java.util.Map;
import java.util.TreeMap;
import java.util.SortedSet;
import java.util.SortedMap;
import java.util.TreeSet;
import com.gtwm.pb.model.interfaces.AppUserInfo;
import com.gtwm.pb.model.interfaces.ModuleUsageStatsInfo;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.UserReportViewStatsInfo;
import com.gtwm.pb.model.interfaces.ModuleInfo;
import com.gtwm.pb.model.manageSchema.Module;

public class ModuleUsageStats implements ModuleUsageStatsInfo, Comparable<ModuleUsageStatsInfo> {

	private ModuleUsageStats() {
	}

	public ModuleUsageStats(ModuleInfo module) {
		this.module = module;
	}

	public ModuleInfo getModule() {
		return this.module;
	}

	public void addReportViewStats(BaseReportInfo report, UserReportViewStatsInfo stats) {
		BaseReportInfo existingReport = null;
		SortedSet<UserReportViewStatsInfo> statsSet = this.reportStats.get(report);
		if (statsSet == null) {
			statsSet = new TreeSet<UserReportViewStatsInfo>();
		}
		statsSet.add(stats);
		this.reportStats.put(report, statsSet);
	}

	public int getTotalReportViews() {
		if (this.totalReportViews == 0) {
			for (SortedSet<UserReportViewStatsInfo> statsSet : this.reportStats.values()) {
				for (UserReportViewStatsInfo stats : statsSet) {
					this.totalReportViews += stats.getReportViews();
				}
			}
		}
		return this.totalReportViews;
	}
	
	public int getUserReportViewsPercentage(BaseReportInfo report, AppUserInfo user) {
		SortedSet<UserReportViewStatsInfo> statsSet = this.reportStats.get(report);
		if (statsSet == null) {
			return 0;
		}
		int specifiedUserViews = 0;
		int allUsersViews = 0;
		for (UserReportViewStatsInfo stats : statsSet) {
			allUsersViews += stats.getReportViews();
			if (stats.getUser().equals(user)) {
				specifiedUserViews = stats.getReportViews();
			}
		}
		if (allUsersViews == 0) {
			return 0;
		}
		float userReportViewsPercentage = (((float) specifiedUserViews) / ((float) allUsersViews)) * 100;
		return Math.round(userReportViewsPercentage);
	}

	public Map<BaseReportInfo, SortedSet<UserReportViewStatsInfo>> getUserReportViewStats() {
		return this.reportStats;
	}

	/**
	 * Based on module only
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		return this.getModule().equals(((ModuleUsageStats) obj).getModule());
	}

	public int hashCode() {
		return this.module.hashCode();
	}

	/**
	 * Produce a descending sort based on the total number of report views for
	 * all reports in each module
	 */
	public int compareTo(ModuleUsageStatsInfo otherStats) {
		int totalReportViews = this.getTotalReportViews();
		int otherTotalReportViews = otherStats.getTotalReportViews();
		if (totalReportViews != otherTotalReportViews) {
			// descending sort
			return Integer.valueOf(otherTotalReportViews).compareTo(totalReportViews);
		}
		return ((Module) otherStats.getModule()).compareTo(this.module);
	}

	public String toString() {
		return "" + this.module + " -> " + this.reportStats.toString();
	}

	private SortedMap<BaseReportInfo, SortedSet<UserReportViewStatsInfo>> reportStats = new TreeMap<BaseReportInfo, SortedSet<UserReportViewStatsInfo>>();

	private ModuleInfo module = null;
	
	private int totalReportViews = 0;
}
