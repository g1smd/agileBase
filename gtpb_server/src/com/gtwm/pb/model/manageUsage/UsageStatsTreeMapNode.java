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
package com.gtwm.pb.model.manageUsage;

import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.UsageStatsTreeMapNodeInfo;

public class UsageStatsTreeMapNode implements UsageStatsTreeMapNodeInfo {

	public UsageStatsTreeMapNode(BaseReportInfo report, int area, int colour) {
		this.report = report;
		this.area = area;
		this.colour = colour;
	}
	
	public int getArea() {
		return this.area;
	}

	public int getColour() {
		return this.colour;
	}

	public BaseReportInfo getReport() {
		return this.report;
	}

	/**
	 * equals based on report
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		return this.getReport().equals(((UsageStatsTreeMapNode) obj).getReport());
	}

	public int hashCode() {
		return this.getReport().hashCode();
	}
	
	private BaseReportInfo report;
	
	private int colour = 0;
	
	private int area = 0;
}
