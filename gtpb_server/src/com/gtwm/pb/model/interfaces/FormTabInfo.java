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
package com.gtwm.pb.model.interfaces;

/**
 * Represents a tab to select a data entry table in a form, when there are multiple tables in one form
 */
public interface FormTabInfo extends Comparable<FormTabInfo> {

	/**
	 * Table which this tab will contain
	 */
	public TableInfo getTable();
	
	/**
	 * If the table may have more than one record related with the parent, use this report to select one to edit.
	 * 
	 * If null, then the table should only contain one 'child' record
	 */
	public BaseReportInfo getSelectorReport();
	
	public void setSelectorReport(BaseReportInfo report);
	
	/**
	 * Order tabs by index
	 */
	public int getIndex();
	
	public void setIndex(int index);
}
