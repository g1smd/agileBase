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
package com.gtwm.pb.model.interfaces.fields;

/**
 * A field value. Subclasses will implement numeric, string, date etc. values.
 * 
 * All values should override toString in a meaningful way. toString should
 * never return null, as the Velocity templating language doesn't deal well with
 * nulls. Instead, an empty string should be returned
 * 
 * Subclasses should however allow null values to be set in the constructor,
 * i.e. take an object which could be null rather than a primitive type
 */
public interface BaseValue {

	/**
	 * Returns whether or not the value is null. Templates can use this method
	 * when displaying values, to stop it trying to display nulls. Of course if
	 * you use toString() to display the value, you don't have to worry,
	 * isNull() is only useful if you want the value in it's native object form
	 * (e.g. a date, integer etc.)
	 */
	public boolean isNull();
}
