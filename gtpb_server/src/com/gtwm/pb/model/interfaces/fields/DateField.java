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

import java.util.Calendar;
import com.gtwm.pb.util.CantDoThatException;
import java.util.Date;

/**
 * Represents a date, or date + time
 */
public interface DateField extends BaseField {

	/**
	 * How accurately should we record this field?<br>
	 * Don't want to display unnecessary input boxes on the screen
	 * 
	 * @param dateResolution
	 *            Use values from the Calendar class constants
	 * @see java.util.Calendar The Calendar class for the constant values to use
	 */
	public void setDateResolution(int dateResolution) throws CantDoThatException;

	public int getDateResolution();

	/**
	 * Return a string representation of the data value passed in, formatted in
	 * accordance with this date field's properties, i.e. taking into account
	 * the resolution etc., don't show times if dates are only accurate to the
	 * day
	 */
	public String formatDate(Date dateValue);

	/**
	 * @see #format(Date) Note: format methods have different methods rather
	 *      than overloading each other: see Bloch item 26 for why
	 */
	public String formatCalendar(Calendar dateValue);

	/**
	 * Return a formatting string that can be used with postgresql's to_char
	 * function to display dates at the resolution set by setDateResolution().
	 * This is used when quick filtering. It should produce results the same as
	 * when using the format() method above
	 * 
	 * NB This is database specific.
	 */
	public String getDatabaseFormatString();

	public void setDefaultToNow(boolean defaultToNow);

	/**
	 * Return true if the field is set to use the current date/time as the
	 * default value in new records
	 */
	public boolean getDefaultToNow();

	public Calendar getDefault();

	public void clearDefault();
}
