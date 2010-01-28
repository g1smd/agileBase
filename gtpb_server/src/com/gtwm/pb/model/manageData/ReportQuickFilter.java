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
package com.gtwm.pb.model.manageData;

import com.gtwm.pb.model.interfaces.ReportQuickFilterInfo;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.util.Enumerations.QuickFilterType;

public class ReportQuickFilter implements ReportQuickFilterInfo {

	public ReportQuickFilter(BaseField filterField, String filterValue, QuickFilterType filterType) {
		this.filterField = filterField;
		this.filterValue = filterValue;
		this.filterType = filterType;
	}
	
	public BaseField getFilterField() {
		return this.filterField;
	}

	public QuickFilterType getFilterType() {
		return this.filterType;
	}

	public String getFilterValue() {
		return this.filterValue;
	}

	public String toString() {
		return this.getFilterField().toString() + " " + this.getFilterType() + " " + this.getFilterValue();
	}
	
	private BaseField filterField = null;
	
	private String filterValue = null;
	
	private QuickFilterType filterType = null;

}
