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
import java.util.Set;
import java.util.SortedSet;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.Enumerations.TextCase;

/**
 * Represents a plain field storing text
 */
public interface TextField extends BaseField {
	public void setDefault(String defaultValue) throws CantDoThatException;

	/**
	 * Return the default item. If this field is a lookup with a CSV for the
	 * default, return the first item in the list
	 */
	public String getDefault();

	/**
	 * If this field is a lookup, return the CSV of default items
	 *
	 * @throws CantDoThatException
	 *             if the field isn't a lookup
	 */
	public String getDefaultCSV() throws CantDoThatException;

	public void clearDefault() throws CantDoThatException;

	public void setContentSize(Integer contentSize) throws CantDoThatException;

	/**
	 * @see PossibleListOptions.TEXTCONTENTSIZE
	 */
	public Integer getContentSize();

	/**
	 * Return whether the 'not applicable/not required' flag has been set for
	 * this field. If it is, then the user should be able to select 'not
	 * required' as an option when entering a value
	 */
	public boolean allowNotApplicable();

	/**
	 * Return the field's own description for the 'not applicable' attribute.
	 * This could be for example "Not applicable/not required"
	 */
	public String getNotApplicableDescription() throws CantDoThatException;

	/**
	 * Return the value to be stored in the database which means 'not
	 * applicable' for this field. e.g. "NOT APPLICABLE", "-1"
	 */
	public String getNotApplicableValue() throws CantDoThatException;

	/**
	 * Returns whether the field should use a combo box entry to look up
	 * previously entered values
	 */
	public boolean usesLookup();

	public boolean getTieDownLookup();

	/**
	 * Whether to use only the values specified in the 'default' property for
	 * the lookup, or whether to allow user input values too
	 */
	public void setTieDownLookup(boolean tieDownLookup);

	/**
	 * @throws CantDoThatException
	 *             If you try to run this method on a big text field, which
	 *             can't use lookups
	 */
	public void setUsesLookup(Boolean usesLookup) throws CantDoThatException;

	public boolean usesTags();

	/**
	 * If tags are used, a CSV of items is stored in the field. Tags are used when a user wants to store more than one value in a field
	 */
	public void setUsesTags(Boolean usesTags) throws CantDoThatException;

	public void addTags(Set<String> tags) throws CantDoThatException;

	/**
	 * Returns a set of distinct values that are stored for this field in the
	 * field's parent table. Useful for displaying a lookup / combo box or set of tags for entry
	 *
	 * TODO: also include any values returned by getDefault() if that returns a
	 * comma separated list
	 *
	 * @throws CantDoThatException
	 *             if the text field type doesn't support lookups. Currently,
	 *             BigTextFieldDefn doesn't
	 */
	public SortedSet<String> getItems() throws SQLException, CantDoThatException,
			CodingErrorException;

	/**
	 * Like getItems but only use the cache, don't fall back to a database call.
	 * Will return a zero length set if getItems has never been called since
	 * application startup
	 */
	public SortedSet<String> getItemsCached() throws CantDoThatException;

	/**
	 * Similar to getItems() but instead of returning values from the field's
	 * parent table, returns them from the report passed in, filtered by the
	 * supplied filter map. An empty map can be used for no filtering.
	 *
	 * @param filterValues
	 *            A filter map in the same format as that passed to
	 *            ReportDataInfo.getReportDataRows
	 * @throws CantDoThatException
	 *             if this field doesn't occur in the report or the text field
	 *             type doesn't support lookups
	 */
	public SortedSet<String> getItems(BaseReportInfo report, Map<BaseField, String> filterValues)
			throws SQLException, CantDoThatException, CodingErrorException;

	/**
	 * Return the case that text in this field should be forced to
	 */
	public TextCase getTextCase();

	public void setTextCase(TextCase textCase);
}
