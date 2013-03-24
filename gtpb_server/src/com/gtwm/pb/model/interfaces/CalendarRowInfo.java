package com.gtwm.pb.model.interfaces;

import java.util.Calendar;

/**
 * Represents an item in the calendar tile view. Items are sorted by date
 */
public interface CalendarRowInfo extends Comparable<CalendarRowInfo> {
	
	public BaseReportInfo getReport();
	
	public Calendar getDate();
	
	/**
	 * Return the data row containing the row ID and full event details
	 */
	public DataRowInfo getDataRow();
	
	/**
	 * Build and return a title for the event, taking data from the report data row
	 */
	public String getTitle();
	
}
