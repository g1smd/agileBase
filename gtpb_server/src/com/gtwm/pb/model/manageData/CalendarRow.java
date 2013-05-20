package com.gtwm.pb.model.manageData;

import java.util.Calendar;
import org.grlea.log.SimpleLogger;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.CalendarRowInfo;
import com.gtwm.pb.model.interfaces.DataRowFieldInfo;
import com.gtwm.pb.model.interfaces.DataRowInfo;
import com.gtwm.pb.model.interfaces.ReportFieldInfo;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.Helpers;

public class CalendarRow implements CalendarRowInfo {

	public CalendarRow(BaseReportInfo report, DataRowInfo reportDataRow) throws CodingErrorException, CantDoThatException {
		this.report = report;
		this.reportDataRow = reportDataRow;
		ReportFieldInfo dateField = report.getCalendarStartField();
		DataRowFieldInfo dateValue = reportDataRow.getValue(dateField);
		String epochString = dateValue.getKeyValue();
		if (epochString.equals("")) {
			throw new CantDoThatException("Date is empty for " + report + " calendar row " + reportDataRow);
		}
		Long epoch = Long.valueOf(epochString);
		this.date = Calendar.getInstance();
		this.date.setTimeInMillis(epoch);
	}

	public BaseReportInfo getReport() {
		return this.report;
	}

	public Calendar getDate() {
		return this.date;
	}

	public DataRowInfo getDataRow() {
		return this.reportDataRow;
	}

	public String getTitle() {
		return Helpers.buildEventTitle(this.getReport(), this.getDataRow(), true, false);
	}

	public String toString() {
			String dateFormat;
			try {
				dateFormat = Helpers.generateJavaDateFormat(Calendar.DAY_OF_MONTH);
			} catch (CantDoThatException cdtex) {
				logger.debug("Error generating date format: " + cdtex);
				return null;
			}
			logger.debug("Date format for " + Calendar.DAY_OF_MONTH + " is " + dateFormat);
			return String.format(dateFormat, this.getDate());
			//SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
			//return dateFormatter.format(this.date);
	}

	/**
	 * Newest events first, then match equals and hashCode
	 */
	public int compareTo(CalendarRowInfo otherCalendarRow) {
		int dateCompare = this.getDate().compareTo(otherCalendarRow.getDate());
		if (dateCompare != 0) {
			return dateCompare;
		}
		int reportCompare = this.getReport().compareTo(otherCalendarRow.getReport());
		if (reportCompare != 0) {
			return reportCompare;
		}
		return this.getDataRow().getRowId() - otherCalendarRow.getDataRow().getRowId();
	}

	/**
	 * Equality based on report and row ID
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		CalendarRowInfo otherCalendarRow = (CalendarRowInfo) obj;
		return (this.getReport().equals(otherCalendarRow.getReport()) && (this.getDataRow().getRowId() == otherCalendarRow.getDataRow().getRowId()));
	}

	public int hashCode() {
		if (this.hashCode == 0) {
			int hashCode = 17;
			hashCode = 37 * hashCode + this.getReport().hashCode();
			hashCode = 37 * hashCode + Integer.valueOf(this.getDataRow().getRowId()).hashCode();
			this.hashCode = hashCode;
		}
		return this.hashCode;
	}

	private volatile int hashCode = 0;

	private final BaseReportInfo report;

	private final DataRowInfo reportDataRow;

	private final Calendar date;

	private static final SimpleLogger logger = new SimpleLogger(CalendarRow.class);

}
