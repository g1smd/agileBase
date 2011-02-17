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
package com.gtwm.pb.model.manageData.fields;

import com.gtwm.pb.model.interfaces.fields.DateValue;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Date;
import java.util.Locale;
import java.text.DateFormat;
import java.text.ParseException;
import org.grlea.log.SimpleLogger;
import com.gtwm.pb.util.CantDoThatException;

public class DateValueDefn implements DateValue {

	private DateValueDefn() {
	}

	public DateValueDefn(Integer year, Integer month, Integer dayOfMonth, Integer hourOfDay,
			Integer minute, Integer second) {
		setDateParts(year, month, dayOfMonth, hourOfDay, minute, second);
	}

	public DateValueDefn(Date date) {
		if (date == null) {
			this.year = null;
			this.month = null;
			this.dayOfMonth = null;
			this.hourOfDay = null;
			this.minute = null;
			this.second = null;
		} else {
			GregorianCalendar dateValue = new GregorianCalendar();
			dateValue.setTime(date);
			this.year = dateValue.get(Calendar.YEAR);
			// Java Calendar dates are zero indexed i.e. June=5 not 6
			// However, it's easier if this class works as a wrapper
			// and is indexed from 1 so that June=6
			this.month = dateValue.get(Calendar.MONTH) + 1;
			this.dayOfMonth = dateValue.get(Calendar.DAY_OF_MONTH);
			this.hourOfDay = dateValue.get(Calendar.HOUR_OF_DAY);
			this.minute = dateValue.get(Calendar.MINUTE);
			this.second = dateValue.get(Calendar.SECOND);
		}
	}
	
	public DateValueDefn(Calendar calendar) {
		setDatePartsFromCalendar(calendar);
	}

	private void setDatePartsFromCalendar(Calendar calendar) {
		if (calendar == null) {
			this.year = null;
			this.month = null;
			this.dayOfMonth = null;
			this.hourOfDay = null;
			this.minute = null;
			this.second = null;			
		} else {
			Integer years = calendar.get(Calendar.YEAR);
			// Java Calendar dates are zero indexed i.e. June=5 not 6
			// However, it's easier if this class works as a wrapper
			// and is indexed from 1 so that June=6
			Integer months = calendar.get(Calendar.MONTH) + 1;
			Integer days = calendar.get(Calendar.DAY_OF_MONTH);
			Integer hours = calendar.get(Calendar.HOUR);
			Integer minutes = calendar.get(Calendar.MINUTE);
			Integer seconds = calendar.get(Calendar.SECOND);
			this.setDateParts(years, months, days, hours, minutes, seconds);
		}
	}
	
	private void setDateParts(Integer year, Integer month, Integer dayOfMonth, Integer hourOfDay,
			Integer minute, Integer second) {
		this.year = year;
		this.month = month;
		this.dayOfMonth = dayOfMonth;
		this.hourOfDay = hourOfDay;
		this.minute = minute;
		this.second = second;
	}

	public DateValueDefn(long millisecs) {
		this(new Date(millisecs));
	}

	/**
	 * @see java.text.DateFormat#parse(java.lang.String) Uses DateFormat to
	 *      translate a string into a date
	 */
	public DateValueDefn(String dateRepresentation) throws ParseException {
		this(DateFormat.getDateTimeInstance().parse(dateRepresentation));
	}

	public void set(int field, Integer value) throws CantDoThatException {
		switch (field) {
		case Calendar.SECOND:
			this.second = value;
			break;
		case Calendar.MINUTE:
			this.minute = value;
			break;
		case Calendar.HOUR_OF_DAY:
			this.hourOfDay = value;
			break;
		case Calendar.DAY_OF_MONTH:
			if ((value >= 1) && (value <= 31)) {
				this.dayOfMonth = value;
			}
			break;
		case Calendar.MONTH:
			if ((value >= 1) && (value <= 12)) {
				this.month = value;
			}
			break;
		case Calendar.YEAR:
			this.year = value;
			break;
		default:
			throw new CantDoThatException(
					"Unable to retrieve value for unrecognised date field constant: " + field);
		}
	}
	
	public void add(int field, int amount) {
		Calendar calendar = this.getValueDate();
		calendar.add(field, amount);
		this.setDatePartsFromCalendar(calendar);
	}

	public Integer get(int field) throws CantDoThatException {
		switch (field) {
		case Calendar.SECOND:
			return this.second;
		case Calendar.MINUTE:
			return this.minute;
		case Calendar.HOUR_OF_DAY:
			return this.hourOfDay;
		case Calendar.DAY_OF_MONTH:
			return this.dayOfMonth;
		case Calendar.MONTH:
			return this.month;
		case Calendar.YEAR:
			return this.year;
		default:
			throw new CantDoThatException(
					"Unable to retrieve value for unrecognised date field constant: " + field);
		}
	}

	public Calendar getValueDate() {
		switch (this.dateResolution) {
		case Calendar.YEAR:
			if (this.year != null) {
				return new GregorianCalendar(this.year, 1, 1, 0, 0, 0);
			}
			break;
		case Calendar.MONTH:
			if ((this.year != null) && (this.month != null)) {
				return new GregorianCalendar(this.year, this.month - 1, 1, 0, 0, 0);
			}
			break;
		case Calendar.DAY_OF_MONTH:
			if ((this.year != null) && (this.month != null) && (this.dayOfMonth != null)) {
				return new GregorianCalendar(this.year, this.month - 1, this.dayOfMonth, 0, 0, 0);
			}
			break;
		case Calendar.HOUR_OF_DAY:
			if ((this.year != null) && (this.month != null) && (this.dayOfMonth != null)
					&& (this.hourOfDay != null)) {
				return new GregorianCalendar(this.year, this.month - 1, this.dayOfMonth,
						this.hourOfDay, 0, 0);
			}
			break;
		case Calendar.MINUTE:
			if ((this.year != null) && (this.month != null) && (this.dayOfMonth != null)
					&& (this.hourOfDay != null) && (this.minute != null)) {
				return new GregorianCalendar(this.year, this.month - 1, this.dayOfMonth,
						this.hourOfDay, this.minute, 0);
			}
			break;
		case Calendar.SECOND:
			if ((this.year != null) && (this.month != null) && (this.dayOfMonth != null)
					&& (this.hourOfDay != null) && (this.minute != null) && (this.second != null)) {
				return new GregorianCalendar(this.year, this.month - 1, this.dayOfMonth,
						this.hourOfDay, this.minute, this.second);
			}
			break;
		}
		return null;
	}

	public void setDateResolution(int dateResolution) throws CantDoThatException {
		switch (dateResolution) {
		case Calendar.YEAR:
		case Calendar.MONTH:
		case Calendar.DAY_OF_MONTH:
		case Calendar.HOUR_OF_DAY:
		case Calendar.MINUTE:
		case Calendar.SECOND:
			this.dateResolution = dateResolution;
			break;
		default:
			throw new CantDoThatException("Unable to set unrecognised date resolution: "
					+ dateResolution);
		}
	}

	public int getDateResolution() {
		return this.dateResolution;
	}

	public String toString() {
		if (this.isNull()) {
			return "";
		} else {
			switch (this.dateResolution) {
			//TODO: replace with Helpers.getnerateJavaDataString
			// Problem is that we'd have to catch an exception then
			case Calendar.YEAR:
				return String.format(Locale.UK, "%1$tY", this.getValueDate());
			case Calendar.MONTH:
				return String.format(Locale.UK, "%1$tb-%1$tY", this.getValueDate());
			case Calendar.DAY_OF_MONTH:
				return String.format(Locale.UK, "%1$td-%1$tb-%1$tY", this.getValueDate());
			case Calendar.HOUR_OF_DAY:
				return String.format(Locale.UK, "%1$td-%1$tb-%1$tY %1$tH", this.getValueDate());
			case Calendar.MINUTE:
				return String.format(Locale.UK, "%1$td-%1$tb-%1$tY %1$tH:%1$tM", this
						.getValueDate());
			case Calendar.SECOND:
				return String.format(Locale.UK, "%1$td-%1$tb-%1$tY %1$tH:%1$tM:%1$tS", this
						.getValueDate());
			}
			return String.format(Locale.UK, "%1$td-%1$tb-%1$tY %1$tH:%1$tM:%1$tS", this
					.getValueDate());
		}
	}

	public boolean isNull() {
		return (this.getValueDate() == null);
	}

	private Integer year = null;

	private Integer month = null;

	private Integer dayOfMonth = null;

	private Integer hourOfDay = null;

	private Integer minute = null;

	private Integer second = null;

	private int dateResolution = Calendar.MONTH;

	private static final SimpleLogger logger = new SimpleLogger(DateValueDefn.class);
}
