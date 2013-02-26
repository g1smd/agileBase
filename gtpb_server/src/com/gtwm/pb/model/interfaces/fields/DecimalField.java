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
package com.gtwm.pb.model.interfaces.fields;

import java.sql.SQLException;
import java.util.Map;
import java.util.SortedSet;

import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.util.CantDoThatException;

/**
 * A floating point number field
 */
public interface DecimalField extends BaseField {

	/**
	 * Used when converting an integer to a decimal field
	 * @param internalFieldName
	 */
		public void setInternalFieldName(String internalFieldName);	
	
    /**
     * @param precision
     *            the number of decimal places to display on screen The stored value is as accurate as the
     *            float type storing it allows
     */
    public void setPrecision(Integer precision);

    public Integer getPrecision();

    public void setDefault(Double defaultValue) throws CantDoThatException;

    /**
     * @return The field's default value, or null if there is no default defined
     */
    public Double getDefault();

    public void clearDefault();

    /**
     * Return a string representation of the number passed in, formatted according to the precision of this
     * field object
     */
    public String formatDecimalValue(DecimalValue decimalValue);
    
    /**
     * @see #formatDecimalValue(DecimalValue)
     */
    public String formatFloat(double decimalValue);
    
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
     * Return the value to be stored in the database which means 'not applicable' for this field. e.g. 0.0
     */
    public double getNotApplicableValue() throws CantDoThatException;
    
    /**
     * Returns whether the field should use a combo box entry to look up previously entered values
     */
    public boolean usesLookup();
    
    /**
     * Returns whether this field is used to store currency amounts
     */
    public boolean storesCurrency();
    
    public void setStoresCurrency(boolean storesCurrency);
    
	/**
	 * Returns a set of distinct values that are stored for this field in the
	 * field's parent table. Useful for displaying a lookup / combo box of
	 * values for entry
	 * 
	 * @throws CantDoThatException
	 *             if the text field type doesn't support lookups. Currently,
	 *             BigTextFieldDefn doesn't
	 */
	public SortedSet<Double> getItems() throws SQLException, CantDoThatException;
	
	/**
	 * Similar to getItems() but instead of returning values from the field's parent table, returns them from the report passed in, filtered by the supplied filter map. An empty map can be used for no filtering.
	 * @param filterValues A filter map in the same format as that passed to ReportDataInfo.getReportDataRows
	 * @throws CantDoThatException if this field doesn't occur in the report or the text field type doesn't support lookups
	 */
	public SortedSet<Double> getItems(BaseReportInfo report, Map<BaseField, String> filterValues) throws SQLException, CantDoThatException;
}
