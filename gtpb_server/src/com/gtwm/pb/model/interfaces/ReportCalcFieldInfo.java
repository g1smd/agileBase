/*
 *  Copyright 2009 GT webMarque Ltd
 * 
 *  This file is part of GT portalBase.
 *
 *  GT portalBase is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  GT portalBase is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with GT portalBase.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gtwm.pb.model.interfaces;

import java.util.Calendar;
import java.util.Date;
import com.gtwm.pb.model.interfaces.ReportFieldInfo;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.Enumerations.DatabaseFieldType;

/**
 * Model a calculation in a report - slightly different to other report fields
 */
public interface ReportCalcFieldInfo extends ReportFieldInfo {

    /**
     * Return the calculation as input by the user, e.g. {table1}.{field1} * {table2}.{field2}
     * 
     * Fields can be identified in one of the following manners:
     * 
     * 1) {field name}, if the field is in the parent report
     * 
     * 2) {table name}.{field name}, to pick a field from a joined table
     * 
     * 3) {table name}.{report name}.{field name} or {report name}.{field name}, to pick a field from a joined
     * report
     * 
     * The actual SQL for the calculation is returned by getCalculationSQL()
     */
    public String getCalculationDefinition();

    /**
     * Translate the user-input calculation into an SQL snippet and return it. Basically the user-facing names
     * for tables, reports and fields are replaced with the actual names used in the relational database
     * 
     * e.g. internaltablename1.internalfieldname1 * internaltablename2.internalfieldname2
     * 
     * @param includeAlisas Include an 'AS internalfieldname' after the calculation SQL
     * 
     * @throws CodingErrorException
     *             Passed up from SimpleReportInfo.getTables() or getReports()
     */
    public String getCalculationSQL(boolean includeAlias) throws CodingErrorException, CantDoThatException;
    
    /**
     * Get the underlying database type that should be used to store calculated values
     */
    public DatabaseFieldType getDbType();

    /**
     * Format a float string passed in for display, in accordance with the properties of this calculation. Use
     * the maximum display precision of all decimal fields used in the calculation
     * @throws CantDoThatException if the calculation return type isn't floating point
     */
    public String formatFloat(double floatValue) throws CantDoThatException;
    
    /**
     * Format an integer for display, e.g. turn 23523 into 23,523
     * @throws CantDoThatException	if the calculation return type isn't integer
     */
    public String formatInteger(int intValue) throws CantDoThatException;
    
    /**
     * Format a date string passed in for display, in accordance with the properties of this calculation. Use
     * the maximum display resolution of all date fields used in the calculation
     * 
     * @throws CantDoThatException
     *             If the calculation doesn't return a date/time type
     */
    public String formatDate(Date dateValue) throws CantDoThatException;

    /**
     * @see #formatDate(Date)
     */
    public String formatCalendar(Calendar dateValue) throws CantDoThatException;

    /**
     * @see com.gtwm.pb.model.interfaces.fields.DateField#getDateResolution()
     * @throws CantDoThatException
     *             If the calculation doesn't return a date/time value
     */
    public int getDateResolution() throws CantDoThatException;

    /**
     * @see com.gtwm.pb.model.interfaces.fields.DecimalField#getPrecision()
     * @throws CantDoThatException
     *             If the calculation doesn't return a decimal value
     */
    public int getDecimalPrecision() throws CantDoThatException;
    
    /**
     * Returns true if this calculation is an aggregate function such as a sum or average
     */
    public boolean isAggregateFunction();
    
    public void setAggregateFunction(boolean isAggregateFunction);
    
    /**
	 * Returns true if this field is a reference to a calc from another report
	 * rather than a direct calc definition
	 */
	public boolean referencesCalcFromOtherReport();
    
    /**
     * <b>Only to be used internally by CalculationFielDefn</b>
     */
    public String getBaseFieldName();
    
    /**
     * <b>Only to be used internally by CalculationFielDefn</b>
     */
    public String getBaseFieldInternalFieldName();
}
