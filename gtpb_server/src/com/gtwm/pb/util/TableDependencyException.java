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
package com.gtwm.pb.util;

import java.util.LinkedHashSet;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.BaseReportInfo;

public class TableDependencyException extends AgileBaseException {

	public TableDependencyException(String message, LinkedHashSet<TableInfo> dependentTables,
			LinkedHashSet<BaseReportInfo> dependentReports) {
		super(message);
		this.dependentTables = dependentTables;
		this.dependentReports = dependentReports;
	}

	public TableDependencyException(String message, Throwable cause,
			LinkedHashSet<TableInfo> dependentTables, LinkedHashSet<BaseReportInfo> dependentReports) {
		super(message, cause);
		this.dependentTables = dependentTables;
		this.dependentReports = dependentReports;
	}

	public LinkedHashSet<TableInfo> getDependentTables() {
		return this.dependentTables;
	}

	public LinkedHashSet<BaseReportInfo> getDependentReports() {
		return this.dependentReports;
	}

	private final LinkedHashSet<TableInfo> dependentTables;

	private final LinkedHashSet<BaseReportInfo> dependentReports;
}
