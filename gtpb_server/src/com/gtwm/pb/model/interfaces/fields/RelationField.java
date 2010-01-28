/*
 *  Copyright 2009 GT webMarque Ltd
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
import java.util.SortedMap;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.util.Enumerations.ForeignKeyConstraint;
import com.gtwm.pb.util.CodingErrorException;

/**
 * Not a field type exactly, represents a foreign key relation, a field that is
 * linked to a unique field from another table
 */
public interface RelationField extends BaseField {
	/**
	 * @return A reference to the field object that this one is related to by a
	 *         foreign key
	 */
	public BaseField getRelatedField();

	public TableInfo getRelatedTable();

	public void setDisplayField(BaseField displayField);

	/**
	 * @return the field used for display purposes
	 */
	public BaseField getDisplayField();

	/**
	 * Return a field name without table prepended and all the "a7) " gubbins
	 * 
	 * @return
	 */
	public String getSimplifiedFieldName();

	/**
	 * Query the database to find the display value for the given key value,
	 * represented as a String.
	 * 
	 * Useful when displaying data but don't use this in any large loop because
	 * a db query will be done on each call
	 * 
	 * @param keyValue
	 *            : internal key value, most likely a row id. Represented as a
	 *            string though because otherwise we'd have to have one method
	 *            per possible key value datatype
	 */
	public String getDisplayValue(String keyValue) throws SQLException, CodingErrorException;

	/**
	 * Use this method when you want a list of all possible internal/display
	 * values for this field, i.e. the set of distinct values in the field it
	 * points to
	 * 
	 * Values will be sorted by display value, case insensitively
	 * 
	 * @param reverseKeyValue
	 *            If false, return a map of related field values to display
	 *            values. If true return the reverse map, i.e. display values
	 *            are the key, related values the value. If true, the returned
	 *            map will be sorted by displayValue (the map key) case
	 *            insensitively)
	 * 
	 * @return a map of internal value to display value from the set of distinct
	 *         internal values in the table
	 */
	public SortedMap<String, String> getItems(boolean reverseKeyValue) throws SQLException;

	/**
	 * Get a list of all internal/display values for this field, using
	 * filterString to filter by display value.
	 * 
	 * Values will be sorted by display value, case insensitively
	 * 
	 * @param reverseKeyValue
	 *            If false, return a map of related field values to display
	 *            values. If true return the reverse map, i.e. display values
	 *            are the key, related values the value. If true, the returned
	 *            map will be sorted by displayValue (the map key) case
	 *            insensitively)
	 * 
	 * @param maxResults
	 *            Return the first maxResults only, or set to -1 for all results
	 * @param filterString
	 *            Filters the display value of the relation, case-insensitively
	 */
	public SortedMap<String, String> getItems(boolean reverseKeyValue, String filterString,
			int maxResults) throws SQLException;

	/**
	 * NOT CURRENTLY IMPLEMENTED
	 * 
	 * Specify the action to be carried taken when a related field is updated
	 * (default is cascade)
	 * 
	 * @param onUpdateAction
	 */
	public void setOnUpdateAction(ForeignKeyConstraint onUpdateAction);

	/**
	 * NOT CURRENTLY IMPLEMENTED
	 * 
	 * Get the action to be carried taken when a related field is updated
	 * (default is cascade)
	 */
	public ForeignKeyConstraint getOnUpdateAction();

	/**
	 * NOT CURRENTLY IMPLEMENTED
	 * 
	 * Specify the action to be carried taken when a related field is deleted
	 * (default is cascade)
	 * 
	 * @param onDeleteAction
	 */
	public void setOnDeleteAction(ForeignKeyConstraint onDeleteAction);

	/**
	 * NOT CURRENTLY IMPLEMENTED
	 * 
	 * Get the action to be carried taken when a related field is deleted
	 * (default is cascade)
	 */
	public ForeignKeyConstraint getOnDeleteAction();
}
