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

import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.util.Enumerations.FormStyle;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.model.manageSchema.fields.IntegerFieldDefn;
import com.gtwm.pb.model.interfaces.fields.SequenceField;
import com.gtwm.pb.util.CantDoThatException;
import java.util.SortedSet;

/**
 * Stores basic information about a database table and keeps a collection of
 * fields. NOTE: To create a new table, use the method in DatabaseInfo rather
 * than just calling the constructor - DatabaseInfo keeps a collection of
 * tables.
 * 
 * @see com.gtwm.pb.model.interfaces.DatabaseInfo The DatabaseInfo interface
 */
public interface TableInfo extends Comparable<TableInfo> {

	public void setTableName(String tableName);

	public void setTableDescription(String tableDesc);

	public String getTableName();

	public String getInternalTableName();

	/**
	 * Return the table name with any prefixes removed, e.g.
	 * 
	 * "a2.1) contacts" -> "contacts"
	 * 
	 * This can be useful for displaying in the end user UI
	 */
	public String getSimpleName();

	/**
	 * First get the simple name, then return the singular version of that.
	 * 
	 * For example, given a table name 'a1) organisations', return
	 * 'organisation'
	 */
	public String getSingularName();

	public String getTableDescription();

	/**
	 * Find a field in this table
	 * 
	 * @param fieldID
	 *            Internal name of the field (preferable) or public name
	 * @throws ObjectNotFoundException
	 *             If the fieldID doesn't match any field either as an internal
	 *             or public name
	 */
	public BaseField getField(String fieldID) throws ObjectNotFoundException;

	/**
	 * @return A read-only copy of the table's field collection, sorted by field
	 *         index i.e. display position
	 */
	public SortedSet<BaseField> getFields();

	/**
	 * Add a new field to the table's set, giving it a field index greater than
	 * any other field, i.e. adding it to the end of the set of fields
	 */
	public void addField(BaseField fieldToAdd) throws CantDoThatException;

	public void setFieldIndex(int index, BaseField fieldToOrder) throws ObjectNotFoundException,
			CantDoThatException;

	public void removeField(BaseField fieldToRemove);

	/**
	 * Each table has a collection of reports which show info from the table and
	 * related tables
	 * 
	 * @param reportToAdd
	 * @param thisIsTheDefaultReport
	 *            Each table has one default report which is not editable by the
	 *            user but which the application keeps up to date as fields are
	 *            added and removed from the table
	 */
	public void addReport(BaseReportInfo reportToAdd, boolean thisIsTheDefaultReport);

	public void removeReport(BaseReportInfo reportToRemove);

	public void setDefaultReport(SimpleReportInfo report);

	/**
	 * Return a simple report containing all fields in the table and no joins,
	 * filters or calculations
	 */
	public SimpleReportInfo getDefaultReport();

	/**
	 * Note: The UI should <b>not</b> use this function to get the list of
	 * reports a user can view as this will return all tables without regard for
	 * the user's privileges. Use ViewMethodsInfo.getViewableReports(TableInfo
	 * table) instead
	 * 
	 * @return All reports belonging to this table, in a read-only collection
	 * 
	 * @see com.gtwm.pb.model.interfaces.ViewMethodsInfo#getViewableReports(TableInfo)
	 */
	public SortedSet<BaseReportInfo> getReports();

	/**
	 * Get a report from this table's collection
	 * 
	 * @param reportID
	 *            The internal name of the report (it's ID) or alternatively the
	 *            public name of the report. Note it will be interpreted as an
	 *            internal name first, so using the public name will be slower,
	 *            but possibly easier when developing a template
	 * @throws ObjectNotFoundException
	 *             If no report with the internal or public name can be found
	 *             belonging to this table
	 */
	public BaseReportInfo getReport(String reportID) throws ObjectNotFoundException;

	/**
	 * @return true if this table contains RelationFields sourced
	 *         from the parameter table
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
	 * Set the primary key to the given integer field. Note the field is
	 * specified as a concrete class, not an interface because it specifically
	 * must be an integer field, not something general like a number field
	 * 
	 * @throws CantDoThatException
	 *             If the field doesn't belong to the table
	 */
	public void setPrimaryKey(IntegerFieldDefn primaryKeyField) throws CantDoThatException;

	/**
	 * @return The primary key field of the table. This will be an integer or
	 *         sequence field
	 */
	public BaseField getPrimaryKey();

	/**
	 * Set whether records in this table can be locked for editing
	 */
	public void setRecordsLockable(Boolean lockable);

	public Boolean getRecordsLockable();

	public void setTableFormPublic(boolean tableFormPublic);

	/**
	 * Return true if new records can be created by non-logged-in users via a
	 * public form
	 */
	public boolean getTableFormPublic();

	/**
	 * Return the email address to send new records to, if there is a public
	 * form (see getTableFormPublic)
	 */
	public String getEmail();

	public void setEmail(String email);

	/**
	 * If there is a public form, optionally set a response message that will
	 * get sent to any email address found in the input data. If this is left
	 * blank, a default message containing the contents of the record will be
	 * sent
	 */
	public String getEmailResponse();

	public void setEmailResponse(String emailResponse);

	public FormStyle getFormStyle();

	public void setFormStyle(FormStyle formStyle);

	public SortedSet<FormTabInfo> getFormTabs();

	public void removeFormTab(FormTabInfo formTab);

	public void addFormTab(FormTabInfo formTab);

	/**
	 * Usually a table will use itself as the input form (naturally) however in
	 * some cases another table that has this one as a form tab may be chosen.
	 * In this case, records will open using the 'parent' table selected but
	 * with the tab for this table open.
	 */
	public void setFormTable(TableInfo table);

	public TableInfo getFormTable();

	/**
	 * Whether agileBase should be allowed to automatically delete records, if
	 * they are unedited since creation, have empty mandated fields for example
	 */
	public boolean getAllowAutoDelete();

	public void setAllowAutoDelete(boolean allowAutoDelete);

	/**
	 * Allow comments to be able to be sent to users who have privileges on the
	 * table
	 */
	public boolean getAllowNotifications();

	public void setAllowNotifications(boolean allowNotifications);
}
