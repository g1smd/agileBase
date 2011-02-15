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

/**
 * Wrapper around an actual field object. When a field is stored in a report, the report needs to know various
 * report-specific things about it sutch as the column order in the report
 */
public interface ReportFieldInfo extends Comparable<ReportFieldInfo> {

    /**
     * Return the actual field object this class wraps around - use this to get the field properties
     */
    public BaseField getBaseField();
    
    /**
     * Shortcut to getBaseField.getFieldName()
     * @see com.gtwm.pb.model.interfaces.fields.BaseField#getFieldName()
     */
    public String getFieldName();
    
    /**
     * Shortcut to getBaseField().getFieldDescription()
     * @see com.gtwm.pb.model.interfaces.fields.BaseField#getFieldDescription()
     */
    public String getFieldDescription();
    
    /**
     * Shortcut to getBaseField().getInternalFieldName()
     * @see com.gtwm.pb.model.interfaces.fields.BaseField#getInternalFieldName()
     */
    public String getInternalFieldName();

    /**
     * When a field is added to a report, it either gets added from a table or from another report. If from a
     * table, you can get the table by using getBaseField().getTableContainingField(). If from a report, you
     * can use this method to return that report
     * 
     * @return The report that the field comes from, if a report
     * @throws CantDoThatException
     *             If field is from a table not a report
     */
    public BaseReportInfo getReportFieldIsFrom() throws CantDoThatException;

    /**
     * @return Whether the field is from a report or a table
     */
    public boolean isFieldFromReport();

    /**
     * @return The report this field is in - this is different to the report the field is <i>from</i>, as
     *         returned by getReportFieldIsFrom(), if the field is from a report not a table
     */
    public BaseReportInfo getParentReport();
    
    /**
     * Report fields are sorted by field index
     */
    public void setFieldIndex(Integer fieldIndex);
    
    public Integer getFieldIndex();
}
