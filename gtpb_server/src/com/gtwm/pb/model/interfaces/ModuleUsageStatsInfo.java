/*
 *  Copyright 2009 GT webMarque Ltd
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
package com.gtwm.pb.model.interfaces;

import java.util.SortedSet;
import java.util.Map;

/**
 * Returns usage stats for all the reports in a module
 */
public interface ModuleUsageStatsInfo {

	public ModuleInfo getModule();
	
	public void addReportViewStats(BaseReportInfo report, UserReportViewStatsInfo stats);
	
	/**
	 * For each report, return how many times each user viewed it, highest users first
	 */
	public Map<BaseReportInfo, SortedSet<UserReportViewStatsInfo>> getUserReportViewStats();
	
	/**
	 * Return the total number of report views for all reports in this module
	 */
	public int getTotalReportViews();
	
	/**
	 * Return the percentage of report views carried out by a certain user
	 */
	public int getUserReportViewsPercentage(BaseReportInfo report, AppUserInfo user);
	
}
