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

import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.model.manageSchema.fields.IntegerFieldDefn;
import com.gtwm.pb.model.interfaces.fields.SequenceField;
import com.gtwm.pb.util.CantDoThatException;
import java.util.SortedSet;

/**
 * Stores basic information about a database table and keeps a collection of fields NOTE: To create a new
 * table, use the method in DatabaseInfo rather than just calling the constructor - DatabaseInfo keeps a
 * collection of tables.
 * 
 * A note on compareTo, equals and hashCode, which should be implemented by concrete classes for TableInfo,
 * ReportInfo and all field types: All of these should compare on object name(s) case insensitively because
 * this is how they will often be displayed to the user. Internal names such as returned by
 * getInternalTableName are used by the database and sometimes code as identifiers but are not the primary
 * object identifiers.
 * 
 * @see com.gtwm.pb.model.interfaces.DatabaseInfo The DatabaseInfo interface
 */
public interface TableInfo extends Comparable<TableInfo> {

    public void setTableName(String tableName);

    public void setTableDescription(String tableDesc);

    public String getTableName();

    public String getInternalTableName();

    public String getTableDescription();

    public BaseField getFieldByInternalName(String internalFieldName) throws ObjectNotFoundException;

    public BaseField getFieldByName(String fieldName) throws ObjectNotFoundException;

    /**
     * @return A read-only copy of the table's field collection, sorted by field index i.e. display position
     */
    public SortedSet<BaseField> getFields();

    /**
     * Add a new field to the table's set, giving it a field index greater than any other field, i.e. adding
     * it to the end of the set of fields
     */
    public void addField(BaseField fieldToAdd) throws CantDoThatException;

    public void setFieldIndex(int index, BaseField fieldToOrder) throws ObjectNotFoundException, CantDoThatException;

    public void removeField(BaseField fieldToRemove);

    /**
     * Each table has a collection of reports which show info from the table and related tables
     * 
     * @param reportToAdd
     * @param thisIsTheDefaultReport
     *            Each table has one default report which is not editable by the user but which the
     *            application keeps up to date as fields are added and removed from the table
     */
    public void addReport(BaseReportInfo reportToAdd, boolean thisIsTheDefaultReport);

    public void removeReport(BaseReportInfo reportToRemove);

    public void setDefaultReport(SimpleReportInfo report);

    public SimpleReportInfo getDefaultReport();

    /**
     * Note: The UI should <b>not</b> use this function to get the list of reports a user can view as this
     * will return all tables without doing any privilege checks. Use
     * ViewMethodsInfo.getViewableReports(TableInfo table) instead
     * 
     * @return All reports belonging to this table, in a read-only collection
     * 
     * @see com.gtwm.pb.model.interfaces.ViewMethodsInfo#getViewableReports(TableInfo)
     */
    public SortedSet<BaseReportInfo> getReports();

    public BaseReportInfo getReportByName(String reportName) throws ObjectNotFoundException;

    public BaseReportInfo getReportByInternalName(String internalReportName) throws ObjectNotFoundException;

    /**
     * @param table
     * @return true if the current table object contains RelationFields sourced from the parameter table
     */
    public boolean isDependentOn(TableInfo table);

    /**
     * Set the primary key to the given sequence field
     * 
     * @throws CantDoThatException
     *             If the field doesn't belong to the table
     */
    public void setPrimaryKey(SequenceField primaryKeyField) throws CantDoThatException;

    /**
     * Set the primary key to the given integer field. Note the field is specified as a concrete class, not an
     * interface because it specifically must be an integer field, not something general like a number field
     * 
     * @throws CantDoThatException
     *             If the field doesn't belong to the table
     */
    public void setPrimaryKey(IntegerFieldDefn primaryKeyField) throws CantDoThatException;

    /**
     * @return The primary key field of the table. This will be an integer or sequence field
     */
    public BaseField getPrimaryKey();
    
    /**
     * Set whether records in this table can be locked for editing
     */
    public void setRecordsLockable(Boolean lockable);
   
    public Boolean getRecordsLockable();

}
