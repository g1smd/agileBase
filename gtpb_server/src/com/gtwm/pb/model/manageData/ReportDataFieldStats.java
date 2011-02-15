/*
 *  Copyright 2011 GT webMarque Ltd
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
package com.gtwm.pb.model.manageData;

import com.gtwm.pb.model.interfaces.ReportDataFieldStatsInfo;

public class ReportDataFieldStats implements ReportDataFieldStatsInfo {

	private ReportDataFieldStats() {
		this.mean = 0f;
		this.stdDev = 0f;
	}

	public ReportDataFieldStats(double mean, double stdDev) {
		this.mean = mean;
		this.stdDev = stdDev;
	}

	public double getStdDev() {
		return this.stdDev;
	}

	public double getMean() {
		return this.mean;
	}

	public String toString() {
		return "Mean = " + this.mean + ", standard deviation = " + this.stdDev;
	}

	private final double stdDev;

	private final double mean;
}
