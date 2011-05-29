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

/**
 * A field whose contents are an auto-generated sequence of numbers
 */
public interface SequenceField extends BaseField {

	/**
	 * Sequence fields are often used for primary keys, in which case their name
	 * will be complicated, like
	 * 
	 * ID:a1) organisations
	 * 
	 * Allow a simple version to be returned, e.g. 'organisations'
	 */
	public String getSimpleName();
}
