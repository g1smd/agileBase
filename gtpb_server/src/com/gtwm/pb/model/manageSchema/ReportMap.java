/*
 *  Copyright 2012 GT webMarque Ltd
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
package com.gtwm.pb.model.manageSchema;

import javax.persistence.Entity;
import com.gtwm.pb.model.interfaces.ReportFieldInfo;
import com.gtwm.pb.model.interfaces.ReportMapInfo;

@Entity
public class ReportMap implements ReportMapInfo {

	public ReportFieldInfo getPostcodeField() {
		return this.postcodeField;
	}

	public void setPostcodeField(ReportFieldInfo postcodeField) {
		this.postcodeField = postcodeField;
	}

	public ReportFieldInfo getColourField() {
		return this.colourField;
	}

	public void setColourField(ReportFieldInfo colourField) {
		this.colourField = colourField;
	}

	public ReportFieldInfo getCategoryField() {
		return this.categoryField;
	}

	public void setCategoryField(ReportFieldInfo categoryField) {
		this.categoryField = categoryField;
	}
	
	private ReportFieldInfo postcodeField;
	
	private ReportFieldInfo colourField;
	
	private ReportFieldInfo categoryField;

}
