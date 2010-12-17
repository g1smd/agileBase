/*
 *  Copyright 2010 GT webMarque Ltd
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
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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
import com.gtwm.pb.model.interfaces.ReportDataInfo;
import com.gtwm.pb.model.interfaces.ReportFieldInfo;
import com.gtwm.pb.model.interfaces.SessionDataInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.fields.DateValue;
import com.gtwm.pb.model.manageData.fields.DateValueDefn;
import com.gtwm.pb.util.AgileBaseException;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.Enumerations.DatabaseFieldType;
import com.gtwm.pb.util.Helpers;
import com.gtwm.pb.util.MissingParametersException;
import com.gtwm.pb.util.ObjectNotFoundException;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VEvent;
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
		String internalCompanyName = request.getParameter("internalcompanyname");
		if (internalCompanyName == null) {
			throw new ServletException(new MissingParametersException(
					"internalcompanyname parameter is necessary to export a calendar"));
		}
		String internalTableName = request.getParameter("internaltablename");
		if (internalTableName == null) {
			throw new ServletException(new MissingParametersException(
					"internaltablename parameter is necessary to export a calendar"));
		}
		String internalReportName = request.getParameter("internalreportname");
		if (internalReportName == null) {
			throw new ServletException(new MissingParametersException(
					"internalreportname parameter is necessary to export a calendar"));
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
			net.fortuna.ical4j.model.Calendar calendar = this.getCalendar(request, report);
			PrintWriter out = response.getWriter();
			CalendarOutputter calendarOutputter = new CalendarOutputter();
			calendarOutputter.output(calendar, out);
		} catch (AgileBaseException abex) {
			throw new ServletException("Error preparing calendar: " + abex);
		} catch (SocketException sex) {
			throw new ServletException("Error preparing calendar: " + sex);
		} catch (SQLException sqlex) {
			throw new ServletException("Error preparing calendar: " + sqlex);
		} catch (ParseException pex) {
			throw new ServletException("Error preparing calendar: " + pex);
		} catch (IOException ioex) {
			throw new ServletException("Error preparing calendar: " + ioex);
		} catch (ValidationException vex) {
			throw new ServletException("Error preparing calendar: " + vex);
		}
	}

	private net.fortuna.ical4j.model.Calendar getCalendar(HttpServletRequest request,
			BaseReportInfo report) throws CantDoThatException, CodingErrorException, SQLException,
			ParseException, SocketException {
		if (!report.getCalendarSyncable()) {
			throw new CantDoThatException("The report " + report
					+ " has not been set as publicly exportable");
		}
		ReportFieldInfo eventDateField = report.getCalendarField();
		if (eventDateField == null) {
			throw new CantDoThatException("The report " + report
					+ " contains no date fields that can be used for calendar syncing");
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
			reportFilterValues.put(eventDateField.getBaseField(), "!?");
			List<DataRowInfo> reportDataRows = reportData.getReportDataRows(conn,
					reportFilterValues, false, sessionReportSorts, rowLimit);
			Calendar calendar = new Calendar(); // an iCal Calendar, not a
												// java.util.Calendar object
			calendar.getProperties().add(
					new ProdId("-//Ben Fortuna//iCal4j 1.0 generated by agilebase.co.uk//EN"));
			calendar.getProperties().add(Version.VERSION_2_0);
			calendar.getProperties().add(CalScale.GREGORIAN);
			for (DataRowInfo reportDataRow : reportDataRows) {
				// ignore any date fields other than the one used for specifying
				// the event date
				// ignore any blank fields
				// for numeric and boolean fields, include the field title
				StringBuilder eventTitleBuilder = new StringBuilder();
				REPORT_FIELD_LOOP: for (ReportFieldInfo reportField : report.getReportFields()) {
					BaseField baseField = reportField.getBaseField();
					if (baseField.getDbType().equals(DatabaseFieldType.TIMESTAMP)
							|| baseField
									.equals(baseField.getTableContainingField().getPrimaryKey())) {
						continue REPORT_FIELD_LOOP;
					}
					DataRowFieldInfo dataRowField = reportDataRow.getValue(baseField);
					switch (baseField.getDbType()) {
					case BOOLEAN:
						boolean reportFieldTrue = Helpers.valueRepresentsBooleanTrue(dataRowField
								.getKeyValue());
						if (reportFieldTrue) {
							eventTitleBuilder.append(reportField.getFieldName());
						}
						break;
					case INTEGER:
						eventTitleBuilder.append(reportField.getFieldName()).append(" = ")
								.append(dataRowField.getDisplayValue());
						break;
					case FLOAT:
						eventTitleBuilder.append(reportField.getFieldName()).append(" = ")
								.append(dataRowField.getDisplayValue());
						break;
					case SERIAL:
						eventTitleBuilder.append(reportField.getFieldName()).append(" = ")
								.append(dataRowField.getKeyValue());
						break;
					default:
						eventTitleBuilder.append(dataRowField.getDisplayValue());
					}
					eventTitleBuilder.append(", ");
				}
				eventTitleBuilder
						.delete(eventTitleBuilder.length() - 2, eventTitleBuilder.length());
				DataRowFieldInfo eventDateInfo = reportDataRow.getValue(eventDateField.getBaseField());
				logger.debug("Millisecs for date " + eventDateInfo + " are " + eventDateInfo.getKeyValue());
				String eventEpochTimeString = eventDateInfo.getKeyValue();
				long eventEpochTime = Long.valueOf(eventEpochTimeString);
				net.fortuna.ical4j.model.Date eventIcalDate = new net.fortuna.ical4j.model.Date(
						eventEpochTime);
				VEvent rowEvent = new VEvent(eventIcalDate, eventTitleBuilder.toString());
				rowEvent.getProperties().add(ug.generateUid());
				calendar.getComponents().add(rowEvent);
			}
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
