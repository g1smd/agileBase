/*
 *  Copyright 2010 GT webMarque Ltd
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
package com.gtwm.pb.model.manageUsage;

import java.util.Date;

import com.gtwm.pb.model.interfaces.UserReportViewStatsInfo;
import com.gtwm.pb.model.interfaces.AppUserInfo;
import com.gtwm.pb.auth.AppUser;

public class UserReportViewStats implements UserReportViewStatsInfo,
		Comparable<UserReportViewStatsInfo> {

	private UserReportViewStats() {
	}

	public UserReportViewStats(AppUserInfo appUser, int reportViews) {
		this.appUser = appUser;
		this.reportViews = reportViews;
	}
	
	public UserReportViewStats(AppUserInfo appUser, int reportViews, Date lastViewed) {
		this.appUser = appUser;
		this.reportViews = reportViews;
		this.lastViewed = lastViewed;
	}
	
	public AppUserInfo getUser() {
		return this.appUser;
	}

	public int getReportViews() {
		return this.reportViews;
	}
	
	public Date getLastViewed() {
		return this.lastViewed;
	}

	/**
	 * equals based on user only
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		return this.getUser().equals(((UserReportViewStats) obj).getUser());
	}

	public int hashCode() {
		return this.getUser().hashCode();
	}

	/**
	 * descending sort, comparison based on no. report views per user
	 */
	public int compareTo(UserReportViewStatsInfo otherStats) {
		int otherReportViews = otherStats.getReportViews();
		if (otherReportViews != this.reportViews) {
			// descending so compare other number to this one, not the other way round
			return Integer.valueOf(otherStats.getReportViews()).compareTo(this.getReportViews());
		}
		return ((AppUser) otherStats.getUser()).compareTo(this.appUser);
	}

	public String toString() {
		return "" + this.getUser() + " -> " + this.reportViews;
	}

	private AppUserInfo appUser;

	private int reportViews = 0;
	
	private Date lastViewed = null;
}
