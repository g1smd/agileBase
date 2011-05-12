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
package com.gtwm.pb.model.interfaces.fields;

import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.Enumerations.DatabaseFieldType;
import com.gtwm.pb.model.interfaces.FieldTypeDescriptorInfo;
import com.gtwm.pb.model.manageSchema.FieldTypeDescriptor.FieldCategory;

/**
 * Provides basic functionality common to all database fields, acts as a bases
 * for more advanced field types. Extends comparable because we want to be able
 * to sort fields. Note we also want to implement equals and hashCode for when
 * we need to search sorted collections.
 * 
 * Fields of type BaseField store info about a field, not about the values in
 * the field at any particular time. To store values, use one of the ...Input
 * classes
 * 
 * A note on compareTo, equals and hashCode, which should be implemented by
 * concrete classes for TableInfo, ReportInfo and all field types: All of these
 * should compare on object name(s) case insensitively because this is how they
 * will often be displayed to the user. Internal names such as returned by
 * getInternalTableName are used by the database and sometimes code as
 * identifiers but are not the primary object identifiers.
 */
public interface BaseField extends Comparable<BaseField> {
	/**
	 * Set the field name. The first time this is called, the internal field
	 * name (the name of the field in the relational database) is also set, to a
	 * unique and un-related value. Further calls will not alter the internal
	 * field name, it will remain constant for the life of the field
	 */
	public void setFieldName(String fieldName);

	/**
	 * The field name is made XML-valid: the four special characters (angle
	 * brackets, ampersand and double quote) are replaced with codes
	 */
	public String getFieldName();

	public String getInternalFieldName();

	public void setFieldDescription(String fieldDesc);

	public String getFieldDescription();

	/**
	 * @return the database type that stores this field
	 */
	public DatabaseFieldType getDbType();

	/**
	 * @return the 'public-facing' field type as selected by the user when
	 *         creating the field, e.g. Number or Text
	 */
	public FieldCategory getFieldCategory() throws CodingErrorException;

	/**
	 * Defines whether the field value in the database can be empty or not
	 * 
	 * @throws CantDoThatException
	 *             If you try and run this method on a field type which is
	 *             <i>always</i> null or not null
	 */
	public void setNotNull(boolean fieldNotNull) throws CantDoThatException;

	public boolean getNotNull();

	/**
	 * 
	 * @return true if a default value has been set for the field, in the case
	 *         of subclass for which a default value should not be found this
	 *         method should never return true.
	 */
	public boolean hasDefault();

	/**
	 * Defines whether the database table allows duplicate values in this field
	 * 
	 * @throws CantDoThatException
	 *             If you try to run this method on a field type which is
	 *             <i>always</i> unique of not unique
	 */
	public void setUnique(Boolean fieldUnique) throws CantDoThatException;

	public Boolean getUnique();

	public void setTableContainingField(TableInfo tableContainingField);

	public TableInfo getTableContainingField();

	/**
	 * Sets whether the field should be displayed in input forms. Note - the
	 * field may still be shown in reports even if hidden. Hiding in reports
	 * will be controlled by a separate ReportFieldInfo parameter (TODO)
	 */
	public void setHidden(Boolean hidden);

	public Boolean getHidden();

	public Integer getFieldIndex();

	/**
	 * The field's index determines it's display position in the table. When
	 * setting one, you should re-arrange others
	 */
	public void setFieldIndex(Integer fieldIndex);

	/**
	 * For use only by the user interface. The UI can use the return value to
	 * generate an input form for example to edit or display the properties
	 * 
	 * @return A representation of the field properties
	 * @throws CantDoThatException
	 *             If there was an internal error generating the descriptor
	 */
	public FieldTypeDescriptorInfo getFieldDescriptor() throws CantDoThatException,
			CodingErrorException;

	public static Boolean NOT_NULL_TRUE = true;

	public static Boolean NOT_NULL_FALSE = false;

	public static Boolean HIDDEN_TRUE = true;

	public static Boolean HIDDEN_FALSE = false;

	public static Boolean UNIQUE_TRUE = true;

	public static Boolean UNIQUE_FALSE = false;
}
