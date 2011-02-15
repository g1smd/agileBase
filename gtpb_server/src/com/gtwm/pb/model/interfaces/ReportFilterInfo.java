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
package com.gtwm.pb.model.interfaces;

import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.Enumerations.FilterType;

import java.util.Set;

/**
 * An report filter, i.e. a WHERE clause in an SQL statement. Defines a filter on a single field. Multiple
 * filters may be added to a report
 * 
 * @see com.gtwm.pb.model.interfaces.BaseReportInfo ReportInfo contains 0 or more ReportFilterInfo objects
 */
public interface ReportFilterInfo {

    /**
     * If the report containing the filter field is the default report, filter on the table otherwise filter
     * on the report. See code for other conditions
     * 
     * @return The SQL fragment needed to apply this filter when creating a view, something like
     *         "table.fieldname >= 5"
     */
    public String getFilterSQL() throws CantDoThatException, CodingErrorException, ObjectNotFoundException;

    /**
     * Return a plain english description of the filter, something like "'Field' from 'Table' greater than 5"
     * 
     * @throws CodingErrorException
     *             If the filter type isn't recognised by the method, i.e. code needs to be added to the
     *             method to handle the filter type
     */
    public String getFilterDescription() throws CodingErrorException;

    /**
     * Return the field which is being filtered
     */
    public BaseField getFilterBaseField();

    /**
     * Return true if the field being filtered is from a report, false if it's from a table
     */
    public boolean isFilterFieldFromReport();

    /**
     * Return the report field object which is the field being filtered
     * 
     * @throws CantDoThatException
     *             If the field being filtered is from a table, not a report
     */
    public ReportFieldInfo getFilterReportField() throws CantDoThatException;
    
    /**
     * Return the report that this filter is part of
     */
    public SimpleReportInfo getParentReport();

    /**
     * Return the filter type, i.e. "EQUALS", "LESS_THAN" etc.
     */
    public FilterType getFilterType();

    /**
     * Return a list of the values the field is filtered on. In many cases, will just return a list of one
     * value, but dropdown field filters may return multiple values
     */
    public Set<String> getFilterValues();

    public String getInternalName();
}
