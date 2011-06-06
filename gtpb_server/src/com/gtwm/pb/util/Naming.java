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
package com.gtwm.pb.util;

import com.gtwm.pb.model.interfaces.fields.RelationField;

/**
 * Contains functions useful when working with a database or XML. Functions will
 * generate Strings that can be used as column names, table names etc. without
 * causing a database error
 */
public final class Naming {
	/**
	 * Prevent object construction outside of this class.
	 */
	private Naming() {
	}

	/**
	 * Replace invalid characters in XML, i.e. angle brackets, ampersands and
	 * double quotes with their HTML codes
	 * 
	 * Note there is an Apache 3rd party utility to do
	 * this better but it converts all entities not just the few we need
	 */
	public static String makeValidXML(String xmlValue) {
		if (xmlValue == null) {
			return null;
		}
		String returnValue = xmlValue.replaceAll("&(?!(\\w+|#\\d+);)", "&amp;"); // &s which are not part of an entity
		returnValue = returnValue.replace("<", "&lt;");
		returnValue = returnValue.replace(">", "&gt;");
		returnValue = returnValue.replace("\"", "&quot;");
		returnValue = returnValue.replace("\u00A3", "&pound;");
		return returnValue;
	}

	/**
	 * Take a table name, e.g. 'a1) organisations' or a field name like 'ID:a1)
	 * organisations' and return the simple form - 'organisations'
	 */
	public static String getSimpleName(String complexName) {
		return complexName.replaceFirst("^.*\\)","");
	}

	/**
	 * Creates a reasonable name for a foreign key constraint. Note any given
	 * relationField must always map to the same name as we need to be able to
	 * regenerate the name at a later time in order to delete the constraint -
	 * makeCompositeId has this property. This is a thin wrapper around
	 * makeCompositeId.
	 * 
	 * @see #makeCompositeId(String, String) makeCompositeId is called by this
	 *      function with the related table name and related field name as
	 *      arguments
	 * 
	 * @param relationField
	 *            The object representing the foreign key relation
	 */
	public static String makeFKeyConstraintName(RelationField relationField) {
		String relatedTableName = relationField.getRelatedTable().getInternalTableName();
		String relatedFieldName = relationField.getInternalFieldName();
		return makeCompositeId(relatedTableName, relatedFieldName);
	}

	/**
	 * Creates a (very probably) unique composite identifier for a set of two
	 * strings that is less than 31 chars long. This is useful for creating IDs
	 * to go in the database for things like foreign keys.
	 * 
	 * Given any two strings, the method will always return the same composite
	 * value, there's no random element. This is to allow re-generation of the
	 * composite from the original inputs
	 */
	public static String makeCompositeId(String name1, String name2) {
		int compositeHashVal = name1.hashCode();
		compositeHashVal = Math.abs(37 * compositeHashVal + name2.hashCode());
		String compositeHash = Integer.toString(compositeHashVal);
		String name1Component = name1;
		String name2Component = name2;
		if (name1Component.length() > 10) {
			name1Component = name1Component.substring(0, 10);
		}
		if (name2Component.length() > 10) {
			name2Component = name2Component.substring(0, 10);
		}
		return name1Component + compositeHash + name2Component;
	}
}
