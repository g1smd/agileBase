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
package com.gtwm.pb.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;
import javax.sql.DataSource;
import org.grlea.log.SimpleLogger;
import com.gtwm.pb.auth.PublicUser;
import com.gtwm.pb.model.interfaces.AppUserInfo;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.CompanyInfo;
import com.gtwm.pb.model.interfaces.DataRowFieldInfo;
import com.gtwm.pb.model.interfaces.DataRowInfo;
import com.gtwm.pb.model.interfaces.DatabaseInfo;
import com.gtwm.pb.model.interfaces.ReportCalcFieldInfo;
import com.gtwm.pb.model.interfaces.ReportDataInfo;
import com.gtwm.pb.model.interfaces.ReportFieldInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.fields.DateField;
import com.gtwm.pb.model.manageData.DataManagement;
import com.gtwm.pb.model.manageUsage.UsageLogger;
import com.gtwm.pb.util.AgileBaseException;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.MissingParametersException;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.Enumerations.QuickFilterType;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.parameter.TzId;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.UidGenerator;

public final class CalendarPublisher extends HttpServlet {

	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		this.databaseDefn = (DatabaseInfo) servletContext
				.getAttribute("com.gtwm.pb.servlets.databaseDefn");
		if (this.databaseDefn == null) {
			throw new ServletException(
					"Error starting CalendarPublisher servlet. No databaseDefn object in the servlet context");
		}
	}

	public void destroy() {
		super.destroy();
		// release memory for good measure
		this.databaseDefn = null;
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException {
		String getType = request.getParameter("get");
		if (getType == null) {
			getType = "cal";
		}
		if (getType.equals("cal")) {
			this.doCalendarGet(request, response);
		} else {
			logger.error("Unrecognised get parameter " + getType);
			throw new ServletException("Unrecognised get parameter " + getType);
		}
	}

	private void doCalendarGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException {
		String internalCompanyName = request.getParameter("c");
		if (internalCompanyName == null) {
			throw new ServletException(new MissingParametersException(
					"c (internal company ID) parameter is necessary to export a calendar"));
		}
		String internalTableName = request.getParameter("t");
		if (internalTableName == null) {
			throw new ServletException(new MissingParametersException(
					"t (internal table ID) parameter is necessary to export a calendar"));
		}
		String internalReportName = request.getParameter("r");
		if (internalReportName == null) {
			throw new ServletException(new MissingParametersException(
					"r (internal report ID) parameter is necessary to export a calendar"));
		}
		String userName = "calendarviewer";
		String forename = "Calendar";
		String surname = "Viewer";
		try {
			AppUserInfo publicUser = new PublicUser(this.databaseDefn.getAuthManager()
					.getAuthenticator(), internalCompanyName, userName, surname, forename);
			CompanyInfo company = publicUser.getCompany();
			TableInfo table = null;
			TABLE_LOOP: for (TableInfo testTable : company.getTables()) {
				if (testTable.getInternalTableName().equals(internalTableName)) {
					table = testTable;
					break TABLE_LOOP;
				}
			}
			if (table == null) {
				throw new ObjectNotFoundException("Table with ID " + internalTableName
						+ " not found in company " + company);
			}
			BaseReportInfo report = table.getReport(internalReportName);
			net.fortuna.ical4j.model.Calendar calendar = this.getCalendar(request, publicUser,
					report);
			response.setContentType("text/calendar");
			PrintWriter out = response.getWriter();
			CalendarOutputter calendarOutputter = new CalendarOutputter();
			calendarOutputter.output(calendar, out);
		} catch (AgileBaseException abex) {
			logger.error("Error preparing calendar for export: " + abex);
			throw new ServletException("Error preparing calendar: " + abex);
		} catch (SocketException sex) {
			logger.error("Error preparing calendar for export: " + sex);
			throw new ServletException("Error preparing calendar: " + sex);
		} catch (SQLException sqlex) {
			logger.error("Error preparing calendar for export: " + sqlex);
			throw new ServletException("Error preparing calendar: " + sqlex);
		} catch (ParseException pex) {
			logger.error("Error preparing calendar for export: " + pex);
			throw new ServletException("Error preparing calendar: " + pex);
		} catch (IOException ioex) {
			logger.error("Error preparing calendar for export: " + ioex);
			throw new ServletException("Error preparing calendar: " + ioex);
		} catch (ValidationException vex) {
			logger.error("Error preparing calendar for export: " + vex);
			throw new ServletException("Error preparing calendar: " + vex);
		}
	}

	private net.fortuna.ical4j.model.Calendar getCalendar(HttpServletRequest request,
			AppUserInfo publicUser, BaseReportInfo report) throws CantDoThatException,
			CodingErrorException, SQLException, ParseException, SocketException {
		if (!report.getCalendarSyncable()) {
			throw new CantDoThatException("The report " + report
					+ " has not been set as publicly exportable");
		}
		ReportFieldInfo eventStartField = report.getCalendarStartField();
		ReportFieldInfo eventEndField = report.getCalendarEndField();
		if (eventStartField == null) {
			throw new CantDoThatException("The report " + report
					+ " contains no date fields that can be used for calendar syncing");
		}
		int dateResolution;
		if (eventStartField instanceof ReportCalcFieldInfo) {
			dateResolution = ((ReportCalcFieldInfo) eventStartField).getDateResolution();
		} else {
			dateResolution = ((DateField) eventStartField.getBaseField()).getDateResolution();
		}
		// We don't need to know the company
		ReportDataInfo reportData = this.databaseDefn.getDataManagement().getReportData(null,
				report, false);
		UidGenerator ug = new UidGenerator("1");
		DataSource dataSource = this.databaseDefn.getDataSource();
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			Map<BaseField, Boolean> sessionReportSorts = new HashMap<BaseField, Boolean>();
			Map<BaseField, String> reportFilterValues = new HashMap<BaseField, String>();
			// select only rows with an event date
			reportFilterValues.put(eventStartField.getBaseField(), "!?");
			List<DataRowInfo> reportDataRows = reportData.getReportDataRows(conn,
					reportFilterValues, false, sessionReportSorts, rowLimit, QuickFilterType.AND);
			net.fortuna.ical4j.model.Calendar calendar = new Calendar();
			calendar.getProperties().add(
					new ProdId("-//Ben Fortuna//iCal4j 1.0 generated by agilebase.co.uk//EN"));
			calendar.getProperties().add(Version.VERSION_2_0);
			calendar.getProperties().add(CalScale.GREGORIAN);
			TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
			VTimeZone tz = registry.getTimeZone("Europe/London").getVTimeZone();
			calendar.getComponents().add(tz);
			java.util.Calendar eventCalendar = java.util.Calendar.getInstance();
			TimeZone timeZone = java.util.Calendar.getInstance().getTimeZone();
			for (DataRowInfo reportDataRow : reportDataRows) {
				String eventTitle = DataManagement.buildEventTitle(report, reportDataRow, false);
				DataRowFieldInfo eventStartInfo = reportDataRow.getValue(eventStartField);
				long eventEpochTime = Long.valueOf(eventStartInfo.getKeyValue());
				eventCalendar.setTimeInMillis(eventEpochTime);
				int hours = eventCalendar.get(java.util.Calendar.HOUR_OF_DAY);
				int minutes = eventCalendar.get(java.util.Calendar.MINUTE);
				boolean wholeDay = (dateResolution < java.util.Calendar.HOUR_OF_DAY)
						|| ((hours == 0) && (minutes == 0));
				VEvent rowEvent = null;
				// Whole day events if the field has no hours/minutes, or if
				// they are both zero
				if (wholeDay) {
					// if (dateResolution >= java.util.Calendar.HOUR_OF_DAY) {
					// For some reason, whole day events need the GMT offset
					// adding but timed events don't
					//eventEpochTime += timeZone.getOffset(eventEpochTime);
					eventEpochTime += (1000 * 60 * 60);
					// }
					net.fortuna.ical4j.model.Date eventIcalDate = new net.fortuna.ical4j.model.Date(
							eventEpochTime);
					net.fortuna.ical4j.model.Date eventEndDate = new net.fortuna.ical4j.model.Date(
							eventEpochTime + (1000 * 60 * 60 * 22));
					if (eventEndField.equals(eventStartField)) {
						rowEvent = new VEvent(eventIcalDate, eventEndDate, eventTitle);
					} else {
						// Multi-day events
						DataRowFieldInfo eventEndInfo = reportDataRow.getValue(eventEndField);
						String endEpochString = eventEndInfo.getKeyValue();
						//if (endEpochString.equals("")) {
						//	rowEvent = new VEvent(eventIcalDate, eventTitle);
						//} else {
							Long endEpochTime = Long.valueOf(endEpochString);
							endEpochTime += timeZone.getOffset(endEpochTime);
							eventEndDate = new net.fortuna.ical4j.model.Date(
									endEpochTime);
							if (eventEndDate.after(eventIcalDate)) {
								rowEvent = new VEvent(eventIcalDate, eventEndDate, eventTitle);
							} else {
								rowEvent = new VEvent(eventIcalDate, eventTitle);
							}
						//}
					}
				} else { // not a whole day
					net.fortuna.ical4j.model.DateTime startTime = new net.fortuna.ical4j.model.DateTime(
							eventEpochTime);
					String endEpochString = "";
					if (!eventEndField.equals(eventStartField)) {
						DataRowFieldInfo eventEndInfo = reportDataRow.getValue(eventEndField);
						endEpochString = eventEndInfo.getKeyValue();
					}
					Long endEpochTime = null;
					if (!endEpochString.equals("")) {
						endEpochTime = Long.valueOf(endEpochString);
					} else {
						endEpochTime = eventEpochTime + (1000 * 60 * 60);
					}
					// sanity check
					if (eventEpochTime > endEpochTime) {
						endEpochTime = eventEpochTime + (1000 * 60 * 60);
					}
					net.fortuna.ical4j.model.DateTime endTime = new net.fortuna.ical4j.model.DateTime(
							endEpochTime);
					rowEvent = new VEvent(startTime, endTime, eventTitle);
				}
				// add Timezone
				TzId tzParam = new TzId(tz.getProperties().getProperty(Property.TZID).getValue());
				rowEvent.getProperties().getProperty(Property.DTSTART).getParameters().add(tzParam);
				rowEvent.getProperties().add(ug.generateUid());
				calendar.getComponents().add(rowEvent);
			}
			// Log the report access
			String remoteHost = request.getRemoteHost() + ", " + request.getHeader("User-Agent");
			UsageLogger usageLogger = new UsageLogger(this.databaseDefn.getDataSource());
			usageLogger.logReportView(publicUser, report, reportFilterValues, rowLimit, remoteHost);
			UsageLogger.startLoggingThread(usageLogger);
			return calendar;
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	private static final int rowLimit = 10000;

	private DatabaseInfo databaseDefn = null;

	private static final SimpleLogger logger = new SimpleLogger(CalendarPublisher.class);
}
