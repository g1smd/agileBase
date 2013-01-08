/*
 *  Copyright 2012 GT webMarque Ltd
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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import javax.mail.MessagingException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.view.VelocityViewServlet;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;
import com.gtwm.pb.model.interfaces.AppRoleInfo;
import com.gtwm.pb.model.interfaces.AppUserInfo;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.CommentInfo;
import com.gtwm.pb.model.interfaces.DataRowFieldInfo;
import com.gtwm.pb.model.interfaces.DataRowInfo;
import com.gtwm.pb.model.interfaces.DatabaseInfo;
import com.gtwm.pb.model.interfaces.FormTabInfo;
import com.gtwm.pb.model.interfaces.SessionDataInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.ViewMethodsInfo;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.fields.BaseValue;
import com.gtwm.pb.model.interfaces.fields.TextField;
import com.gtwm.pb.model.manageData.SessionData;
import com.gtwm.pb.model.manageData.ViewMethods;
import com.gtwm.pb.model.manageData.ViewTools;
import com.gtwm.pb.model.manageSchema.DatabaseDefn;
import com.gtwm.pb.model.manageUsage.UsageLogger;
import com.gtwm.pb.util.AgileBaseException;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.auth.DisallowedException;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.Enumerations.SessionAction;
import com.gtwm.pb.util.Enumerations.AppAction;
import com.gtwm.pb.util.Enumerations.ResponseReturnType;
import com.gtwm.pb.util.Enumerations.SessionContext;
import com.gtwm.pb.util.Helpers;
import com.gtwm.pb.util.MissingParametersException;
import com.gtwm.pb.util.AppProperties;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.grlea.log.SimpleLogger;

/*
 * A note about templates:
 * Apache Velocity is the templating language, similar to Smarty for PHP.
 * Velocity templates are located in the folder specified by the
 * file.resource.loader.path
 * key in /velocity.properties
 */

/**
 * The controller in the MCV application. This is the 'main' class.
 */
public final class AppController extends VelocityViewServlet {

	/**
	 * init() is called once automatically by the servlet container (e.g. Tomcat)
	 * at servlet startup. We use it to initialise various things, namely:
	 * 
	 * a) create the DatabaseDefn object which is the top level application
	 * object. The DatabaseDefn object will load the list of tables & reports into
	 * memory when it is constructed. It will also configure and load the object
	 * database
	 * 
	 * b) create a DataSource object here to pass to the DatabaseDefn. This data
	 * source then acts as a pool of connections from which a connection to the
	 * relational database can be called up whenever needed.
	 */
	public void init() throws ServletException {

		logger.info("Initialising " + AppProperties.applicationName);
		ServletContext servletContext = getServletContext();
		this.webAppRoot = servletContext.getRealPath("/");

		// Create and cache a DatabaseDefn object which is an entry point to all
		// application logic
		// and schema information. The relational database is also gotten
		DataSource relationalDataSource = null;
		InitialContext initialContext = null;
		try {
			// Get a data source for the relational database to pass to the
			// DatabaseDefn object
			initialContext = new InitialContext();
			relationalDataSource = (DataSource) initialContext.lookup("java:comp/env/jdbc/agileBaseData");
			if (relationalDataSource == null) {
				throw new ServletException("Can't get data source");
			}
			this.relationalDataSource = relationalDataSource;
			// Store 'global objects' data sources and webAppRoot in
			// databaseDefn
			this.databaseDefn = new DatabaseDefn(relationalDataSource, this.webAppRoot);
			// Store top level stuff in the context so that other servlets can
			// access it
			servletContext.setAttribute("com.gtwm.pb.servlets.databaseDefn", this.databaseDefn);
			servletContext.setAttribute("com.gtwm.pb.servlets.relationalDataSource",
					this.relationalDataSource);
		} catch (NullPointerException npex) {
			ServletUtilMethods.logException(npex, "Error initialising controller servlet");
			throw new ServletException("Error initialising controller servlet", npex);
		} catch (SQLException sqlex) {
			ServletUtilMethods.logException(sqlex, "Database error loading schema");
			throw new ServletException("Database error loading schema", sqlex);
		} catch (NamingException neex) {
			ServletUtilMethods.logException(neex, "Can't get initial context");
			throw new ServletException("Can't get initial context");
		} catch (RuntimeException rtex) {
			ServletUtilMethods.logException(rtex, "Runtime initialisation error");
			throw new ServletException("Runtime initialisation error", rtex);
		} catch (Exception ex) {
			ServletUtilMethods.logException(ex, "General initialisation error");
			throw new ServletException("General initialisation error", ex);
		}
		logger.info("Application fully loaded");
	}

	public void destroy() {
		super.destroy();
		this.databaseDefn.cancelScheduledEvents();
		Connection conn = null;
		try {
			conn = this.relationalDataSource.getConnection();
			UsageLogger.logDataOlderThan(conn, 0);
			conn.commit();
		} catch (SQLException sqlex) {
			logger.error("SQL exception while performing final logging during shutdown: " + sqlex);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException sqlex) {
					logger.error("Error closing SQL connection at app close: " + sqlex);
				}
			}
		}
		// Release memory - still not sure if this is actually necessary
		this.databaseDefn = null;
		this.relationalDataSource = null;
		logger.info("agileBase shut down");
	}

	/**
	 * Optionally return application/xml or other content rather than the default
	 * text/html
	 * 
	 * If there is no returntype specified, don't set a header, but return
	 * ResponseReturnType.HTML
	 * 
	 * This can be useful when using AJAX interfaces with XMLHttpRequest in the
	 * browser
	 */
	public static ResponseReturnType setReturnType(HttpServletRequest request,
			HttpServletResponse response, List<FileItem> multipartItems) {
		String returnType = ServletUtilMethods.getParameter(request, "returntype", multipartItems);
		ResponseReturnType responseReturnType = ResponseReturnType.HTML;
		if (returnType != null) {
			try {
				responseReturnType = ResponseReturnType.valueOf(returnType.toUpperCase());
				response.setContentType(responseReturnType.getResponseType());
				if (responseReturnType.equals(ResponseReturnType.DOWNLOAD)) {
					String filename = ServletUtilMethods.getParameter(request, "returnfilename",
							multipartItems);
					response.setHeader("Content-Disposition", "attachment;filename=\"" + filename + "\"");
				}
			} catch (IllegalArgumentException iaex) {
				EnumSet<ResponseReturnType> allReturnTypes = EnumSet.allOf(ResponseReturnType.class);
				ServletUtilMethods.logException(iaex, request, "Unknown returntype specified: "
						+ returnType + " - must be one of " + allReturnTypes);
			}
		}
		return responseReturnType;
	}

	public static void carryOutSessionActions(HttpServletRequest request,
			SessionDataInfo sessionData, DatabaseInfo databaseDefn, Context context, HttpSession session,
			List<FileItem> multipartItems) throws SQLException, AgileBaseException {
		// Set any new values for the session variables before carrying out main
		// app actions
		EnumSet<SessionAction> sessionActions = EnumSet.allOf(SessionAction.class);
		for (SessionAction sessionAction : sessionActions) {
			String sessionActionParam = request.getParameter(sessionAction.toString().toLowerCase());
			if (sessionActionParam != null) {
				switch (sessionAction) {
				case PRESET_ROW_ID:
					ServletSessionMethods.setRowId(sessionData, request, sessionActionParam, databaseDefn,
							sessionAction);
					break;
				case SET_TABLE:
					ServletSessionMethods.setTable(sessionData, request, sessionActionParam, databaseDefn);
					break;
				case SET_REPORT:
					ServletSessionMethods.setReport(request, sessionData, sessionActionParam, databaseDefn);
					break;
				case SET_ROW_ID:
					ServletSessionMethods.setRowId(sessionData, request, sessionActionParam, databaseDefn,
							sessionAction);
					break;
				case SET_REPORT_ROW_LIMIT:
					sessionData.setReportRowLimit(Integer.parseInt(sessionActionParam));
					break;
				case SET_REPORT_FILTER_VALUE:
					ServletSessionMethods.setReportFilterValue(sessionData, request, databaseDefn);
					break;
				case CLEAR_ALL_REPORT_FILTER_VALUES:
					ServletSessionMethods.clearAllReportFilterValues(sessionData);
					break;
				case SET_GLOBAL_REPORT_FILTER_STRING:
					ServletSessionMethods.setReportGlobalFilterString(sessionData, request, databaseDefn);
					break;
				case SET_REPORT_SORT:
					ServletSessionMethods.setReportSort(sessionData, request, databaseDefn);
					break;
				case CLEAR_REPORT_SORT:
					ServletSessionMethods.clearReportSort(sessionData, request, databaseDefn);
					break;
				case CLEAR_ALL_REPORT_SORTS:
					ServletSessionMethods.clearAllReportSorts(sessionData);
					break;
				case SET_USER:
					ServletSessionMethods.setUser(sessionData, request, sessionActionParam, databaseDefn);
					break;
				case SET_ROLE:
					AppRoleInfo role = databaseDefn.getAuthManager()
							.getRoleByInternalName(sessionActionParam);
					sessionData.setRole(role);
					break;
				case SET_CONTEXT:
					SessionContext sessionContext = SessionContext.valueOf(sessionActionParam.toUpperCase());
					sessionData.setContext(sessionContext);
					break;
				case SET_CUSTOM_VARIABLE:
				case SET_CUSTOM_STRING:
					// SET_CUSTOM_VARIABLE is deprecated in favour of
					// SET_CUSTOM_STRING
					ServletSessionMethods.setCustomString(sessionData, request);
					break;
				case SET_CUSTOM_INTEGER:
					ServletSessionMethods.setCustomInteger(sessionData, request);
					break;
				case SET_CUSTOM_LONG:
					ServletSessionMethods.setCustomLong(sessionData, request);
					break;
				case SET_CUSTOM_BOOLEAN:
					ServletSessionMethods.setCustomBoolean(sessionData, request);
					break;
				case SET_CUSTOM_TABLE:
					ServletSessionMethods.setCustomTable(sessionData, request, true, databaseDefn);
					break;
				case SET_CUSTOM_REPORT:
					ServletSessionMethods.setCustomReport(sessionData, request, true, databaseDefn);
					break;
				case SET_CUSTOM_FIELD:
					ServletSessionMethods.setCustomField(sessionData, request, databaseDefn);
					break;
				case CLEAR_CUSTOM_VARIABLE:
					sessionData.clearCustomVariable(sessionActionParam);
					break;
				case SET_MODULE:
					ServletSessionMethods.setModule(sessionData, request, sessionActionParam, databaseDefn);
					break;
				case SET_LOCK_OVERRIDE:
					ServletSessionMethods.setLockOverride(sessionData, request, databaseDefn);
					break;
				case SET_APP_ID:
					ServletSessionMethods.setAppId(sessionData, request, sessionActionParam);
					break;
				case CLEAR_APP_ID:
					ServletSessionMethods.clearAppId(sessionData);
					break;
				case LOGOUT:
					logout(request);
					break;
				}
			}
		}
	}

	public static void carryOutAppActions(HttpServletRequest request, SessionDataInfo sessionData,
			DatabaseInfo databaseDefn, List<FileItem> multipartItems, StringBuffer appActionName)
			throws AgileBaseException, SQLException, FileUploadException, IOException, MessagingException {
		// perform any actions
		EnumSet<AppAction> appActions = EnumSet.allOf(AppAction.class);
		for (AppAction appAction : appActions) {
			// Store so exception handling has access to the action carried out
			appActionName.setLength(0);
			appActionName.append(appAction.toString());
			String appActionValue = ServletUtilMethods.getParameter(request, appAction.toString()
					.toLowerCase(Locale.UK), multipartItems);
			if (appActionValue != null) {
				sessionData.setLastAppAction(appAction);
				switch (appAction) {
				// Most commonly used actions at the start
				case UPDATE_RECORD:
					ServletDataMethods.saveRecord(sessionData, request, false, databaseDefn, multipartItems);
					break;
				case ADD_USER:
					// ADD_USER before SAVE_NEW_RECORD so that user record can be created
					// and record belonging to them in the same request (Edudo)
					ServletAuthMethods.addUser(sessionData, request, databaseDefn.getAuthManager());
					break;
				case SAVE_NEW_RECORD:
					ServletDataMethods.saveRecord(sessionData, request, true, databaseDefn, multipartItems);
					break;
				case CLONE_RECORD:
					ServletDataMethods.cloneRecord(sessionData, request, databaseDefn, multipartItems);
					break;
				case REMOVE_RECORD:
					ServletDataMethods.removeRecord(sessionData, request, databaseDefn);
					break;
				case GLOBAL_EDIT:
					ServletDataMethods.globalEdit(sessionData, request, databaseDefn, multipartItems);
					break;
				case CONTRACT_SECTION:
					ServletSchemaMethods.contractSection(sessionData, request, databaseDefn);
					break;
				case EXPAND_SECTION:
					ServletSchemaMethods.expandSection(sessionData, request, databaseDefn);
					break;
				case ADD_COMMENT:
					ServletDataMethods.addComment(sessionData, request, databaseDefn);
					break;
				case REMOVE_USER:
					ServletAuthMethods.removeUser(sessionData, request, databaseDefn);
					break;
				case UPDATE_USER:
					ServletAuthMethods.updateUser(sessionData, request, databaseDefn.getAuthManager());
					break;
				case SEND_PASSWORD_RESET:
					ServletAuthMethods.sendPasswordReset(request, databaseDefn.getAuthManager());
					break;
				case ADD_ROLE:
					ServletAuthMethods.addRole(sessionData, request, databaseDefn.getAuthManager());
					break;
				case UPDATE_ROLE:
					ServletAuthMethods.updateRole(sessionData, request, databaseDefn.getAuthManager());
					break;
				case REMOVE_ROLE:
					ServletAuthMethods.removeRole(sessionData, request, databaseDefn);
					break;
				case ASSIGN_USER_TO_ROLE:
					ServletAuthMethods.assignUserToRole(sessionData, request, databaseDefn.getAuthManager());
					break;
				case REMOVE_USER_FROM_ROLE:
					ServletAuthMethods.removeUserFromRole(request, databaseDefn);
					break;
				case ADD_PRIVILEGE:
					ServletAuthMethods.addPrivilege(request, databaseDefn);
					break;
				case REMOVE_PRIVILEGE:
					ServletAuthMethods.removePrivilege(request, databaseDefn);
					break;
				case SET_MAX_TABLE_PRIVILEGE:
					ServletAuthMethods.setMaxTablePrivilege(sessionData, request, databaseDefn);
					break;
				case CLEAR_ALL_TABLE_PRIVILEGES:
					ServletAuthMethods.clearAllTablePrivileges(request, databaseDefn);
					break;
				case ADD_TABLE:
					ServletSchemaMethods.addTable(sessionData, request, databaseDefn);
					break;
				case UPDATE_TABLE:
					ServletSchemaMethods.updateTable(sessionData, request, databaseDefn);
					break;
				case REMOVE_TABLE:
					ServletSchemaMethods.removeTable(sessionData, request, databaseDefn);
					break;
				case ADD_FIELD:
					ServletSchemaMethods.addField(sessionData, request, databaseDefn);
					break;
				case REMOVE_FIELD:
					ServletSchemaMethods.removeField(sessionData, request, databaseDefn);
					break;
				case UPDATE_FIELD:
					ServletSchemaMethods.updateField(sessionData, request, databaseDefn);
					break;
				case UPDATE_FIELD_OPTION:
					ServletSchemaMethods.updateFieldOption(sessionData, request, databaseDefn);
					break;
				case SET_FIELD_INDEX:
					ServletSchemaMethods.setFieldIndex(sessionData, request, databaseDefn);
					break;
				case ADD_REPORT:
					ServletSchemaMethods.addReport(sessionData, request, databaseDefn);
					break;
				case UPDATE_REPORT:
					ServletSchemaMethods.updateReport(sessionData, request, databaseDefn);
					break;
				case UPLOAD_CUSTOM_TEMPLATE:
					ServletSchemaMethods.uploadCustomReportTemplate(sessionData, request, databaseDefn,
							multipartItems);
					break;
				case REMOVE_CUSTOM_TEMPLATE:
					ServletSchemaMethods.removeCustomReportTemplate(sessionData, request, databaseDefn);
					break;
				case REMOVE_REPORT:
					ServletSchemaMethods.removeReport(sessionData, request, databaseDefn);
					break;
				case ADD_FIELD_TO_REPORT:
					ServletSchemaMethods.addFieldToReport(sessionData, request, databaseDefn);
					break;
				case REMOVE_FIELD_FROM_REPORT:
					ServletSchemaMethods.removeFieldFromReport(sessionData, request, databaseDefn);
					break;
				case SET_REPORT_FIELD_INDEX:
					ServletSchemaMethods.setReportFieldIndex(sessionData, request, databaseDefn);
					break;
				case SET_REPORT_FIELD_SORTING:
					ServletSchemaMethods.setReportFieldSorting(sessionData, request, databaseDefn);
					break;
				case ADD_CALCULATION_TO_REPORT:
					ServletSchemaMethods.addCalculationToReport(sessionData, request, databaseDefn);
					break;
				case UPDATE_CALCULATION_IN_REPORT:
					ServletSchemaMethods.updateCalculationInReport(sessionData, request, databaseDefn);
					break;
				case ADD_FILTER_TO_REPORT:
					ServletSchemaMethods.addFilterToReport(sessionData, request, databaseDefn);
					break;
				case REMOVE_FILTER_FROM_REPORT:
					ServletSchemaMethods.removeFilterFromReport(sessionData, request, databaseDefn);
					break;
				case ADD_JOIN_TO_REPORT:
					ServletSchemaMethods.addJoinToReport(sessionData, request, databaseDefn);
					break;
				case REMOVE_JOIN_FROM_REPORT:
					ServletSchemaMethods.removeJoinFromReport(sessionData, request, databaseDefn);
					break;
				case ADD_GROUPING_TO_CHART:
					ServletSchemaMethods.addGroupingToChart(sessionData, request, databaseDefn);
					break;
				case REMOVE_GROUPING_FROM_CHART:
					ServletSchemaMethods.removeGroupingFromChart(sessionData, request, databaseDefn);
					break;
				case ADD_FUNCTION_TO_CHART:
					ServletSchemaMethods.addFunctionToChart(sessionData, request, databaseDefn);
					break;
				case REMOVE_FUNCTION_FROM_CHART:
					ServletSchemaMethods.removeFunctionFromChart(sessionData, request, databaseDefn);
					break;
				case SET_CHART_FILTER:
					ServletSchemaMethods.setChartFilter(sessionData, request, databaseDefn);
					break;
				case SET_CHART_FILTER_FIELD:
					ServletSchemaMethods.setChartFilterField(sessionData, request, databaseDefn);
					break;
				case SET_CHART_RANGE:
					ServletSchemaMethods.setChartRange(sessionData, request, databaseDefn);
					break;
				case SAVE_CHART:
					ServletSchemaMethods.saveChart(sessionData, request, databaseDefn);
					break;
				case REMOVE_CHART:
					ServletSchemaMethods.removeChart(sessionData, request, databaseDefn);
					break;
				case SET_WORD_CLOUD_FIELD:
					ServletSchemaMethods.setReportWordCloudField(sessionData, request, databaseDefn);
					break;
				case UPDATE_MAP:
					ServletSchemaMethods.updateMap(sessionData, request, databaseDefn);
					break;
				case SET_DASHBOARD_CHART_STATE:
					ServletDashboardMethods.setDashboardSummaryState(sessionData, request, databaseDefn);
					break;
				case CSV_IMPORT:
					ServletDataMethods.importRecords(sessionData, request, databaseDefn, multipartItems);
					break;
				case LOCK_RECORDS:
					ServletDataMethods.lockRecords(sessionData, request, databaseDefn);
					break;
				case ADD_COMPANY:
					ServletSchemaMethods.addCompany(request, databaseDefn);
					break;
				case REMOVE_COMPANY:
					ServletSchemaMethods.removeCompany(request, databaseDefn.getAuthManager());
					break;
				case ANONYMISE:
					ServletDataMethods.anonymiseTableData(sessionData, request, databaseDefn, multipartItems);
					break;
				case ADD_MODULE:
					ServletSchemaMethods.addModule(request, sessionData, databaseDefn);
					break;
				case REMOVE_MODULE:
					ServletSchemaMethods.removeModule(request, sessionData, databaseDefn);
					break;
				case UPDATE_MODULE:
					ServletSchemaMethods.updateModule(request, sessionData, databaseDefn.getAuthManager());
					break;
				case HIDE_REPORT:
					ServletSchemaMethods.hideReportFromUser(sessionData, request, databaseDefn);
					break;
				case UNHIDE_REPORT:
					ServletSchemaMethods.unhideReportFromUser(sessionData, request, databaseDefn);
					break;
				case ADD_OPERATIONAL_DASHBOARD_REPORT:
					ServletSchemaMethods.addOperationalDashboardReport(sessionData, request, databaseDefn);
					break;
				case REMOVE_OPERATIONAL_DASHBOARD_REPORT:
					ServletSchemaMethods.removeOperationalDashboardReport(sessionData, request, databaseDefn);
					break;
				case ADD_FORM_TABLE:
					ServletSchemaMethods.addFormTable(sessionData, request, databaseDefn);
					break;
				case REMOVE_FORM_TABLE:
					ServletSchemaMethods.removeFormTable(sessionData, request, databaseDefn);
					break;
				case ADD_FORM_TAB:
					ServletSchemaMethods.addFormTab(request, sessionData, databaseDefn);
					break;
				case REMOVE_FORM_TAB:
					ServletSchemaMethods.removeFormTab(request, sessionData, databaseDefn);
					break;
				case UPDATE_FORM_TAB:
					ServletSchemaMethods.updateFormTab(sessionData, request, databaseDefn);
					break;
				case SET_TABLE_FORM:
					ServletSchemaMethods.setTableForm(request, sessionData, databaseDefn);
					break;
				case SET_USER_DEFAULT_REPORT:
					ServletSchemaMethods.setUserDefaultReport(sessionData, request, databaseDefn);
					break;
				case SET_CALENDAR_SYNCABLE:
					boolean calendarSyncable = Helpers.valueRepresentsBooleanTrue(appActionValue);
					ServletSchemaMethods.setCalendarSyncable(sessionData, request, databaseDefn,
							calendarSyncable);
					break;
				case ENABLE_DISABLE_APP:
					ServletSchemaMethods.enableDisableApp(request, databaseDefn);
					break;
				case ADD_REPORT_DISTINCT:
					ServletSchemaMethods.addDistinctToReport(sessionData, request, databaseDefn);
					break;
				case REMOVE_REPORT_DISTINCT:
					ServletSchemaMethods.removeDistinctFromReport(sessionData, request, databaseDefn);
					break;
				}
			}
		}
	}

	public static void carryOutPostSessionActions(HttpServletRequest request,
			SessionDataInfo sessionData, DatabaseInfo databaseDefn, EnumSet<SessionAction> sessionActions)
			throws SQLException, ObjectNotFoundException, MissingParametersException, DisallowedException {
		for (SessionAction sessionAction : sessionActions) {
			String sessionActionParam = request.getParameter(sessionAction.toString().toLowerCase(
					Locale.UK));
			if (sessionActionParam != null) {
				switch (sessionAction) {
				case POSTSET_TABLE:
					ServletSessionMethods.setTable(sessionData, request, sessionActionParam, databaseDefn);
					break;
				case POSTSET_REPORT:
					ServletSessionMethods.setReport(request, sessionData, sessionActionParam, databaseDefn);
					break;
				case POSTSET_CUSTOM_TABLE:
					ServletSessionMethods.setCustomTable(sessionData, request, false, databaseDefn);
					break;
				case POSTSET_CUSTOM_REPORT:
					ServletSessionMethods.setCustomReport(sessionData, request, false, databaseDefn);
					break;
				}
			}
		}
	}

	/**
	 * The main control function, called for every HTTP GET or POST request.
	 * Basically performs three functions:
	 * 
	 * 1) Sets any session variables posted from the user interface, if any
	 * 
	 * 2) Performs any actions requested by the UI, if any
	 * 
	 * 3) Parses and returns the template requested by the UI
	 */
	public Template handleRequest(HttpServletRequest request, HttpServletResponse response,
			Context context) {
		// Start timing request
		long handleRequestStartTime = System.currentTimeMillis();
		// Cache multipart form data so don't have to reparse it all the time
		List<FileItem> multipartItems = ServletUtilMethods.getMultipartItems(request);
		logger.debug("Request: " + ServletUtilMethods.getRequestQuery(request));
		ResponseReturnType returnType = setReturnType(request, response, multipartItems);
		response.setCharacterEncoding("ISO-8859-1");
		HttpSession session = request.getSession();
		SessionDataInfo sessionData = (SessionDataInfo) session
				.getAttribute("com.gtwm.pb.servlets.sessionData");
		if (sessionData == null) {
			try {
				// Set up a session for a newly logged in user
				sessionData = new SessionData(this.databaseDefn, this.relationalDataSource, request);
			} catch (SQLException | AgileBaseException ex) {
				ServletUtilMethods.logException(ex, request, "Error creating session data object: " + ex);
				sessionData = new SessionData();
			}
			// Set session cookie expiry date
			// String id = request.getSession().getId();
			// long expireTimestamp = System.currentTimeMillis() + (12 * 60 * 60
			// * 1000); // 12 hours ahead
			// String expireDate = new
			// SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss z").format(new
			// Date(expireTimestamp));
			// response.setHeader("Set-Cookie",
			// String.format("JSESSIONID=%s;Expires=%s;Path=/agileBase", id,
			// expireDate));
		}
		String templateName = ServletUtilMethods.getParameter(request, "return", multipartItems);
		try {
			carryOutSessionActions(request, sessionData, this.databaseDefn, context, session,
					multipartItems);
		} catch (AgileBaseException | RuntimeException | SQLException ex) {
			ServletUtilMethods.logException(ex, request, "Error setting session data");
			if (returnType.equals(ResponseReturnType.XML) || returnType.equals(ResponseReturnType.JSON)) {
				return getUserInterfaceTemplate(request, response, templateName, context, session,
						sessionData, ex, multipartItems);
			} else {
				return getUserInterfaceTemplate(request, response, "report_error", context, session,
						sessionData, ex, multipartItems);
			}
			/*
			 * } catch (Exception ex) { ServletUtilMethods.logException(ex, request,
			 * "General error setting session data"); if
			 * (returnType.equals(ResponseReturnType.XML)) { return
			 * getUserInterfaceTemplate(request, response, templateName, context,
			 * session, sessionData, ex, multipartItems); } else { // override to
			 * return the error template templateName = "report_error"; return
			 * getUserInterfaceTemplate(request, response, templateName, context,
			 * session, sessionData, ex, multipartItems); }
			 */
		}
		// Use StringBuffer to get a mutable string that can be altered by
		// carryOutAppActions
		StringBuffer appActionName = new StringBuffer("");
		try {
			carryOutAppActions(request, sessionData, this.databaseDefn, multipartItems, appActionName);
		} catch (AgileBaseException | SQLException | RuntimeException | FileUploadException
				| IOException | MessagingException ex) {
			ServletUtilMethods.logException(ex, request, "Error carrying out action " + appActionName);
			return getUserInterfaceTemplate(request, response, templateName, context, session,
					sessionData, ex, multipartItems);
		}
		// Set any new values for the session variables after carrying out main
		// app actions
		EnumSet<SessionAction> sessionActions = EnumSet.allOf(SessionAction.class);
		try {
			carryOutPostSessionActions(request, sessionData, this.databaseDefn, sessionActions);
		} catch (AgileBaseException | RuntimeException | SQLException ex) {
			ServletUtilMethods.logException(ex, request, "Error setting session data post acction");
			if (returnType.equals(ResponseReturnType.XML) || returnType.equals(ResponseReturnType.JSON)) {
				return getUserInterfaceTemplate(request, response, templateName, context, session,
						sessionData, ex, multipartItems);
			} else {
				// override to return the error template
				templateName = "report_error";
				return getUserInterfaceTemplate(request, response, templateName, context, session,
						sessionData, ex, multipartItems);
			}
		}
		// Log long request times - doesn't include template rendering time: see
		// mergeTemplate for those
		float secondsToHandleRequest = (System.currentTimeMillis() - handleRequestStartTime)
				/ ((float) 1000);
		if (secondsToHandleRequest > AppProperties.longProcessingTime) {
			String warnMessage = "Long server request processing time of "
					+ String.valueOf(secondsToHandleRequest) + " seconds for URL "
					+ ServletUtilMethods.getRequestQuery(request) + "\r\n";
			warnMessage += "Logged in user: " + request.getRemoteUser();
			logger.warn(warnMessage);
		}
		// Don't do HibernateUtil.closeSession() here, leave it open for the
		// view to display
		// in mergeTemplate()
		// Create ViewMethods object and return the requested template
		return getUserInterfaceTemplate(request, response, templateName, context, session, sessionData,
				null, multipartItems);
	}

	/**
	 * Create an instance of ViewMethods to provide the UI with the necessary
	 * functionality, and return the requested template.
	 * 
	 * TODO: This method obviously doesn't throw any exceptions for a reason,
	 * presumably we always want to return a template whatever happens. Check out
	 * whether there's a better way of doing things though
	 * 
	 * @param exceptionCaught
	 *          An exception thrown by handleRequest. Pass null if none. This will
	 *          be saved in ViewMethods to allow the UI to find out what went
	 *          wrong
	 * @return The template requested, ready to parse by the UI
	 * 
	 */
	private Template getUserInterfaceTemplate(HttpServletRequest request,
			HttpServletResponse response, String templateName, Context context, HttpSession session,
			SessionDataInfo sessionData, Exception exceptionCaught, List<FileItem> multipartItems) {
		// template ('return' parameter) *must* be specified
		if (templateName == null) {
			logger
					.error("No template specified. Please add 'return=<i>templatename</i>' to the HTTP request");
		} else {
			// Allow slashes and dashes but no other special characters
			templateName = Helpers.rinseString(templateName, "\\/-");
			if (templateName.startsWith("/")) {
				logger.error("Invalid template name " + templateName);
				templateName = "";
			}
		}
		try {
			boolean sessionValid = request.isRequestedSessionIdValid();
			// Check user's logged in otherwise an exception will be thrown
			if (sessionValid) {
				// Save any changes to the session data
				session.setAttribute("com.gtwm.pb.servlets.sessionData", sessionData);
			}
			ViewMethodsInfo viewMethods = new ViewMethods(request, this.databaseDefn);
			if (exceptionCaught != null) {
				viewMethods.setException(exceptionCaught);
			}
			context.put("view", viewMethods);
			if (sessionValid) {
				context.put("sessionData", sessionData);
			}
			context.put("viewTools", new ViewTools(request, response, this.webAppRoot));
			// If a custom user-uploaded template, add in field variables from
			// session table and report
			if (templateName != null) {
				if (templateName.startsWith("uploads/")) {
					try {
						addCurrentDataToContext(context, sessionData, viewMethods);
					} catch (AgileBaseException abex) {
						logger.error("Error preparing uploaded custom template variables: " + abex);
						viewMethods.setException(abex);
					} catch (SQLException sqlex) {
						logger.error("SQL Error preparing uploaded custom template variables: " + sqlex);
						viewMethods.setException(sqlex);
					}
				}
			}
			AppUserInfo user = this.databaseDefn.getAuthManager().getLoggedInUser(request);
			/*
			 * if (user.getUsesCustomUI()) { String cleanCompanyName = user.getCompany
			 * ().getCompanyName().toLowerCase().replaceAll("\\W", ""); String
			 * companyPath = "gui/customisations/" + cleanCompanyName + "/"; // Only
			 * allow templates in the company path, or the boot template if
			 * ((!templateName.startsWith(companyPath)) &&
			 * (!templateName.equals("boot"))) { logger.error("Path " + templateName +
			 * " is outside of the company path " + companyPath + " for user " +
			 * user); templateName = null; } }
			 */
		} catch (ObjectNotFoundException onfex) {
			ServletUtilMethods.logException(onfex, request, "Error getting template");
		} catch (DisallowedException dex) {
			ServletUtilMethods.logException(dex, request, "Error getting template");
		}
		templateName = "" + templateName + ".vm";
		Template template = null;
		try {
			// See note about template locations at top of file
			template = getTemplate(templateName);
		} catch (ResourceNotFoundException rnfe) {
			logger.error("Template not found: " + rnfe);
		} catch (ParseErrorException pee) {
			logger.error("Syntax error in the template: " + pee);
		}
		return template;
	}

	/**
	 * Add Velocity variables to the Velocity context for each field in the
	 * current report (values from the current report row) and current table
	 */
	private static void addCurrentDataToContext(Context context, SessionDataInfo sessionData,
			ViewMethodsInfo view) throws DisallowedException, ObjectNotFoundException,
			CodingErrorException, CantDoThatException, SQLException {
		BaseReportInfo report = sessionData.getReport();
		int rowId = sessionData.getRowId();
		TableInfo table = sessionData.getTable();
		Map<BaseField, String> filters = new HashMap<BaseField, String>();
		filters.put(table.getPrimaryKey(), String.valueOf(rowId));
		// First report data
		List<DataRowInfo> reportDataRows = view.getReportDataRows(report, 1, filters, true);
		// There will be only one row
		for (DataRowInfo dataRow : reportDataRows) {
			for (BaseField field : report.getReportBaseFields()) {
				String rinsedFieldName = Helpers.rinseString(field.getFieldName().toLowerCase()).replace(
						" ", "_");
				DataRowFieldInfo value = dataRow.getValue(field);
				if (field instanceof TextField) {
					context.put(rinsedFieldName, value.getKeyValue());
					context.put(field.getInternalFieldName(), value.getKeyValue());
				} else {
					context.put(rinsedFieldName, value.getDisplayValue());
					context.put(field.getInternalFieldName(), value.getDisplayValue());
				}
			}
		}
		// Then table data
		Map<BaseField, BaseValue> tableData = view.getTableDataRow();
		for (Map.Entry<BaseField, BaseValue> tableRow : tableData.entrySet()) {
			BaseField field = tableRow.getKey();
			String value = tableRow.getValue().toString();
			// Append comments
			for (CommentInfo comment : view.getComments(field, rowId)) {
				// TODO: I know, hard coding HTML but what else can we do?
				value += "<div class='comment'>";
				value += "<span class='comment_text'>" + comment.getText() + "</span> ";
				value += "<span class='comment_attribution'>- " + comment.getAuthor() + ", "
						+ comment.getTimestampString() + "</span>";
				value += "</div>";
			}
			String rinsedFieldName = Helpers.rinseString(field.getFieldName().toLowerCase()).replace(" ",
					"_");
			context.put(rinsedFieldName, value);
			context.put(field.getInternalFieldName(), value);
		}
		// Then data from related form tabs
		for (FormTabInfo formTab : table.getFormTabs()) {
			BaseReportInfo selectorReport = formTab.getSelectorReport();
			Map<BaseField, String> filter = new HashMap<BaseField, String>();
			filter.put(table.getPrimaryKey(), String.valueOf(rowId));
			List<DataRowInfo> selectorRows = view.getReportDataRows(selectorReport, 50, filter, true);
			String tabName = formTab.toString().toLowerCase().replace(" ", "_");
			context.put(tabName + "_rows", selectorRows);
			StringBuilder selectorRowsHtml = new StringBuilder("<table class='childData'><tr>\n");
			for (BaseField sField : selectorReport.getReportBaseFields()) {
				if (!sField.equals(sField.getTableContainingField().getPrimaryKey())) {
					selectorRowsHtml.append("<th>" + sField + "</th>");
				}
			}
			selectorRowsHtml.append("</tr>\n");
			for (DataRowInfo selectorRow : selectorRows) {
				selectorRowsHtml.append("  <tr>");
				for (BaseField selectorField : selectorReport.getReportBaseFields()) {
					TableInfo parentTable = selectorField.getTableContainingField();
					if (!selectorField.equals(parentTable.getPrimaryKey())) {
						selectorRowsHtml.append("<td class='"
								+ selectorField.getDbType().toString().toLowerCase() + " table_"
								+ parentTable.getInternalTableName() + "'>" + selectorRow.getValue(selectorField)
								+ "</td>");
					}
				}
				selectorRowsHtml.append("</tr>\n");
			}
			selectorRowsHtml.append("</table>");
			context.put(tabName + "_table", selectorRowsHtml);
		}
	}

	/**
	 * Override Velocity's mergeTemplate to a) time the template processing b)
	 * redirect on error
	 */
	@Override
	public void mergeTemplate(Template template, Context context, HttpServletResponse response)
			throws IOException {
		long mergeTemplateStartTime = System.currentTimeMillis();
		try {
			super.mergeTemplate(template, context, response);
		} catch (Exception ex) {
			ServletUtilMethods.logException(ex, "Error interpreting template " + template.getName());
			try {
				// Make the exception that just occurred accessible for
				// reporting
				ViewMethodsInfo viewMethods = (ViewMethodsInfo) context.get("view");
				viewMethods.setException(ex);
				context.put("view", viewMethods);
				Template errorTemplate = getTemplate(AppProperties.errorTemplateLocation);
				super.mergeTemplate(errorTemplate, context, response);
			} catch (ResourceNotFoundException rnfe) {
				ServletUtilMethods.logException(rnfe, "Error template not found");
			} catch (ParseErrorException pee) {
				ServletUtilMethods.logException(pee, "Syntax error in the template");
			} catch (Exception exex) {
				ServletUtilMethods.logException(exex, "General templating error whilst reporting error");
			}
		}
		float secondsToHandleMerge = (System.currentTimeMillis() - mergeTemplateStartTime)
				/ ((float) 1000);
		if (secondsToHandleMerge > AppProperties.longProcessingTime) {
			logger.warn("Long template request processing time of "
					+ String.valueOf(secondsToHandleMerge) + " seconds for template " + template.getName());
			ViewMethodsInfo viewMethods = (ViewMethodsInfo) context.get("view");
			SessionDataInfo sessionData = (SessionDataInfo) context.get("sessionData");
			try {
				BaseReportInfo report = sessionData.getReport();
				logger.warn("Logged in user: " + viewMethods.getLoggedInUser() + ", session report = "
						+ report + " filtered by " + sessionData.getReportFilterValues(report) + ", limit "
						+ sessionData.getReportRowLimit());
			} catch (DisallowedException dex) {
				logger.warn("Not allowed to get logged in user: " + dex);
			} catch (ObjectNotFoundException onfex) {
				logger.warn("Unable to find logged in user: " + onfex);
			}
		}
	}

	/**
	 * Invalidate the user's session
	 */
	private static void logout(HttpServletRequest request) {
		HttpSession session = request.getSession();
		session.invalidate();
	}

	/**
	 * 
	 * Object representation of the relational database - basically a collection
	 * of tables & views
	 */
	private DatabaseInfo databaseDefn = null;

	private DataSource relationalDataSource = null;

	private static final SimpleLogger logger = new SimpleLogger(AppController.class);

	private String webAppRoot;
}