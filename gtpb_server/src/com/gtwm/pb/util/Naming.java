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
	 * TODO: Sure there is a 3rd party utility e.g. an apache commons one to do
	 * this better
	 */
	public static String makeValidXML(String xmlValue) {
		if (xmlValue == null) {
			return null;
		}
		String returnValue = xmlValue.replaceAll("<", "&lt;");
		returnValue = returnValue.replaceAll(">", "&gt;");
		returnValue = returnValue.replaceAll("&", "&amp;");
		returnValue = returnValue.replaceAll("\"", "&quot;");
		return returnValue;
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
