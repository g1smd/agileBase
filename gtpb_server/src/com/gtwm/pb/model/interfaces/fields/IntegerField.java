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
package com.gtwm.pb.model.interfaces.fields;

import java.sql.SQLException;
import java.util.Map;
import java.util.SortedSet;

import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.util.CantDoThatException;

public interface IntegerField {
    public void setDefault(Integer defaultValue) throws CantDoThatException;

    /**
     * Format an integer for display, e.g. turn 23523 into 23,523
     */
    public String formatIntegerValue(IntegerValue integerValue);
    
    public String formatInteger(int integerValue);
    
    /**
     * @return The field's default value, or null if there is no default defined
     */
    public Integer getDefault();

    public void clearDefault();
    
    /**
     * Return whether the 'not applicable/not required' flag has been set for this field. If it is, then the
     * user should be able to select 'not required' as an option when entering a value
     */
    public boolean allowNotApplicable();

    /**
     * Return the field's own description for the 'not applicable' attribute. This could be for example "Not
     * applicable/not required"
     */
    public String getNotApplicableDescription() throws CantDoThatException;

    /**
     * Return the value to be stored in the database which means 'not applicable' for this field. e.g. "NOT
     * APPLICABLE", "-1"
     */
    public int getNotApplicableValue() throws CantDoThatException;
    
    /**
     * Returns whether the field should use a combo box entry to look up previously entered values
     */
    public boolean usesLookup();
    
	/**
	 * Returns a set of distinct values that are stored for this field in the
	 * field's parent table. Useful for displaying a lookup / combo box of
	 * values for entry
	 * 
	 * @throws CantDoThatException
	 *             if the text field type doesn't support lookups. Currently,
	 *             BigTextFieldDefn doesn't
	 */
	public SortedSet<Integer> getItems() throws SQLException, CantDoThatException;
	
	/**
	 * Similar to getItems() but instead of returning values from the field's parent table, returns them from the report passed in, filtered by the supplied filter map. An empty map can be used for no filtering.
	 * @param filterValues A filter map in the same format as that passed to ReportDataInfo.getReportDataRows
	 * @throws CantDoThatException if this field doesn't occur in the report or the text field type doesn't support lookups
	 */
	public SortedSet<Integer> getItems(BaseReportInfo report, Map<BaseField, String> filterValues) throws SQLException, CantDoThatException;
}
