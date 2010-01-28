/*
 *  Copyright 2009 GT webMarque Ltd
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

import java.sql.SQLException;
import java.lang.StackTraceElement;
import java.util.Locale;
import java.util.Map;
import java.util.EnumSet;
import java.util.List;
import java.util.LinkedList;
import java.io.IOException;
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
import org.apache.velocity.tools.view.servlet.VelocityViewServlet;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;
import com.gtwm.pb.model.interfaces.AppRoleInfo;
import com.gtwm.pb.model.interfaces.DatabaseInfo;
import com.gtwm.pb.model.interfaces.SessionDataInfo;
import com.gtwm.pb.model.interfaces.ViewMethodsInfo;
import com.gtwm.pb.model.interfaces.CompanyInfo;
import com.gtwm.pb.model.interfaces.WikiManagementInfo;
import com.gtwm.pb.model.manageData.SessionData;
import com.gtwm.pb.model.manageData.ViewMethods;
import com.gtwm.pb.model.manageData.ViewTools;
import com.gtwm.pb.model.manageData.InputRecordException;
import com.gtwm.pb.model.manageSchema.DatabaseDefn;
import com.gtwm.pb.util.AgileBaseException;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.auth.DisallowedException;
import com.gtwm.pb.util.Enumerations.SessionAction;
import com.gtwm.pb.util.Enumerations.AppAction;
import com.gtwm.pb.util.Enumerations.ResponseReturnType;
import com.gtwm.pb.util.Enumerations.SessionContext;
import com.gtwm.pb.util.MissingParametersException;
import com.gtwm.pb.util.AppProperties;
import com.gtwm.pb.integration.MediaWikiManagement;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.grlea.log.SimpleLogger;

/*
 * A note about templates:
 * Apache Velocity is the templating language, similar to Smarty for PHP
 * Velocity templates are located in the folder specified by the
 * file.resource.loader.path
 * key in /velocity.properties
 */

/**
 * The controller in the MCV application. This is the 'main' class.
 */
public class AppController extends VelocityViewServlet {

	/**
	 * init() is called once automatically by the servlet container (e.g.
	 * Tomcat) at servlet startup. We use it to initialise various things,
	 * namely:
	 * 
	 * a) create the DatabaseDefn object which is the top level application
	 * object. The DatabaseDefn object will load the list of tables & reports
	 * into memory when it is constructed. It will also configure and load the
	 * object database
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
		DataSource wikiDataSource = null;
		InitialContext initialContext = null;
		try {
			// Get a data source for the relational database to pass to the
			// DatabaseDefn object
			initialContext = new InitialContext();
			relationalDataSource = (DataSource) initialContext
					.lookup("java:comp/env/jdbc/agileBaseData");
			if (relationalDataSource == null) {
				logger.error("Can't get relational data source");
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
			logException(npex, "Error initialising controller servlet");
			throw new ServletException("Error initialising controller servlet", npex);
		} catch (SQLException sqlex) {
			logException(sqlex, "Database error loading schema");
			throw new ServletException("Database error loading schema", sqlex);
		} catch (NamingException neex) {
			logException(neex, "Can't get initial context");
			throw new ServletException("Can't get initial context");
		} catch (RuntimeException rtex) {
			logException(rtex, "Runtime initialisation error");
			throw new ServletException("Runtime initialisation error", rtex);
		} catch (Exception ex) {
			logException(ex, "General initialisation error");
			throw new ServletException("General initialisation error", ex);
		}
		logger.info("Application fully loaded");
	}

	public void destroy() {
		super.destroy();
		this.databaseDefn.cancelScheduledEvents();
		// Release memory - still not sure if this is actually necessary
		this.databaseDefn = null;
		this.relationalDataSource = null;
		logger.info("agileBase shut down");
	}

	public static List<FileItem> getMultipartItems(HttpServletRequest request)
			throws ServletException {
		// Cache multipart form data so don't have to reparse it all the time
		List<FileItem> multipartItems = new LinkedList<FileItem>();
		if (FileUpload.isMultipartContent(new ServletRequestContext(request))) {
			// See http://jakarta.apache.org/commons/fileupload/using.html
			FileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);
			try {
				multipartItems = upload.parseRequest(request);
			} catch (FileUploadException fuex) {
				throw new ServletException("Error parsing multi-part form data", fuex);
			}
		}
		return multipartItems;
	}

	public static ResponseReturnType setReturnType(HttpServletRequest request,
			HttpServletResponse response, List<FileItem> multipartItems) throws ServletException {
		// Optionally return text/xml content rather than the default text/html
		// This can be useful when using AJAX interfaces with XMLHttpRequest in
		// the browser
		String returnType = getParameter(request, "returntype", multipartItems);
		ResponseReturnType responseReturnType = null;
		if (returnType != null) {
			try {
				responseReturnType = ResponseReturnType.valueOf(returnType.toUpperCase());
				response.setContentType(responseReturnType.getResponseType());
				if (responseReturnType.equals(ResponseReturnType.DOWNLOAD)) {
					String filename = getParameter(request, "returnfilename", multipartItems);
					response.setHeader("Content-Disposition", "attachment;filename=\"" + filename
							+ "\"");
				}
			} catch (IllegalArgumentException iaex) {
				EnumSet<ResponseReturnType> allReturnTypes = EnumSet
						.allOf(ResponseReturnType.class);
				throw new ServletException("Unknown returntype specified: " + returnType
						+ " - must be one of " + allReturnTypes);
			}
		}
		return responseReturnType;
	}

	public static void carryOutSessionActions(HttpServletRequest request,
			SessionDataInfo sessionData, DatabaseInfo databaseDefn, Context context,
			HttpSession session, List<FileItem> multipartItems) throws SQLException,
			AgileBaseException {
		// Set any new values for the session variables before carrying out main
		// app actions
		EnumSet<SessionAction> sessionActions = EnumSet.allOf(SessionAction.class);
		for (SessionAction sessionAction : sessionActions) {
			String sessionActionParam = request.getParameter(sessionAction.toString().toLowerCase(
					Locale.UK));
			if (sessionActionParam != null) {
				switch (sessionAction) {
				case PRESET_ROW_ID:
					ServletDataMethods.setSessionRowId(sessionData, request, sessionActionParam,
							databaseDefn);
					break;
				case SET_REPORT:
					ServletDataMethods.setSessionReport(request, sessionData, sessionActionParam,
							databaseDefn);
					break;
				case SET_TABLE:
					ServletDataMethods.setSessionTable(sessionData, request, sessionActionParam,
							databaseDefn);
					break;
				case SET_ROW_ID:
					ServletDataMethods.setSessionRowId(sessionData, request, sessionActionParam,
							databaseDefn);
					break;
				case SET_REPORT_ROW_LIMIT:
					sessionData.setReportRowLimit(Integer.parseInt(sessionActionParam));
					break;
				case SET_REPORT_FILTER_VALUE:
					ServletDataMethods.setReportFilterValue(sessionData, request, databaseDefn);
					break;
				case CLEAR_ALL_REPORT_FILTER_VALUES:
					ServletDataMethods.clearAllReportFilterValues(sessionData);
					break;
				case SET_REPORT_SORT:
					ServletDataMethods.setSessionReportSort(sessionData, request, databaseDefn);
					break;
				case CLEAR_REPORT_SORT:
					ServletDataMethods.clearSessionReportSort(sessionData, request, databaseDefn);
					break;
				case CLEAR_ALL_REPORT_SORTS:
					ServletDataMethods.clearAllSessionReportSorts(sessionData);
					break;
				case SET_USER:
					ServletDataMethods.setSessionUser(sessionData, request, sessionActionParam,
							databaseDefn);
					break;
				case SET_ROLE:
					AppRoleInfo role = databaseDefn.getAuthManager().getRoleByInternalName(
							sessionActionParam);
					sessionData.setRole(role);
					break;
				case SET_CONTEXT:
					SessionContext sessionContext = SessionContext.valueOf(sessionActionParam
							.toUpperCase());
					sessionData.setContext(sessionContext);
					break;
				case SET_CUSTOM_VARIABLE:
				case SET_CUSTOM_STRING:
					// SET_CUSTOM_VARIABLE is deprecated in favour of
					// SET_CUSTOM_STRING
					ServletDataMethods.setSessionCustomString(sessionData, request);
					break;
				case SET_CUSTOM_INTEGER:
					ServletDataMethods.setSessionCustomInteger(sessionData, request);
					break;
				case SET_CUSTOM_BOOLEAN:
					ServletDataMethods.setSessionCustomBoolean(sessionData, request);
					break;
				case SET_CUSTOM_TABLE:
					ServletDataMethods.setSessionCustomTable(sessionData, request, true,
							databaseDefn);
					break;
				case SET_CUSTOM_REPORT:
					ServletDataMethods.setSessionCustomReport(sessionData, request, true,
							databaseDefn);
					break;
				case SET_CUSTOM_FIELD:
					ServletDataMethods.setSessionCustomField(sessionData, request, databaseDefn);
					break;
				case CLEAR_CUSTOM_VARIABLE:
					sessionData.clearCustomVariable(sessionActionParam);
					break;
				case SET_MODULE:
					ServletDataMethods.setSessionModule(sessionData, request, sessionActionParam,
							databaseDefn);
					break;
				case SET_LOCK_OVERRIDE:
					ServletDataMethods.setSessionLockOverride(sessionData, request, databaseDefn);
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
			throws AgileBaseException, SQLException, FileUploadException, IOException {
		// perform any actions
		EnumSet<AppAction> appActions = EnumSet.allOf(AppAction.class);
		for (AppAction appAction : appActions) {
			// Store so exception handling has access to the action carried out
			appActionName.setLength(0);
			appActionName.append(appAction.toString());
			if (getParameter(request, appAction.toString().toLowerCase(Locale.UK), multipartItems) != null) {
				sessionData.setLastAppAction(appAction);
				switch (appAction) {
				case ADD_USER:
					ServletAuthMethods.addUser(sessionData, request, databaseDefn.getAuthManager());
					break;
				case REMOVE_USER:
					ServletAuthMethods.removeUser(sessionData, request, databaseDefn
							.getAuthManager());
					break;
				case UPDATE_USER:
					ServletAuthMethods.updateUser(sessionData, request, databaseDefn
							.getAuthManager());
					break;
				case ADD_ROLE:
					ServletAuthMethods.addRole(sessionData, request, databaseDefn.getAuthManager());
					break;
				case UPDATE_ROLE:
					ServletAuthMethods.updateRole(sessionData, request, databaseDefn
							.getAuthManager());
					break;
				case REMOVE_ROLE:
					ServletAuthMethods.removeRole(sessionData, request, databaseDefn
							.getAuthManager());
					break;
				case ASSIGN_USER_TO_ROLE:
					ServletAuthMethods.assignUserToRole(request, databaseDefn.getAuthManager());
					break;
				case REMOVE_USER_FROM_ROLE:
					ServletAuthMethods.removeUserFromRole(request, databaseDefn.getAuthManager());
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
					ServletSchemaMethods.updateCalculationInReport(sessionData, request,
							databaseDefn);
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
				case ADD_GROUPING_TO_SUMMARY_REPORT:
					ServletSchemaMethods.addGroupingToSummaryReport(sessionData, request,
							databaseDefn);
					break;
				case REMOVE_GROUPING_FROM_SUMMARY_REPORT:
					ServletSchemaMethods.removeGroupingFromSummaryReport(sessionData, request,
							databaseDefn);
					break;
				case ADD_FUNCTION_TO_SUMMARY_REPORT:
					ServletSchemaMethods.addFunctionToSummaryReport(sessionData, request,
							databaseDefn);
					break;
				case REMOVE_FUNCTION_FROM_SUMMARY_REPORT:
					ServletSchemaMethods.removeFunctionFromSummaryReport(sessionData, request,
							databaseDefn);
					break;
				case SAVE_REPORT_SUMMARY:
					ServletSchemaMethods.saveSummaryReport(sessionData, request, databaseDefn);
					break;
				case REMOVE_REPORT_SUMMARY:
					ServletSchemaMethods.removeSummaryReport(sessionData, request, databaseDefn);
					break;
				case SAVE_NEW_RECORD:
					ServletDataMethods.saveRecord(sessionData, request, true, databaseDefn,
							multipartItems);
					break;
				case CLONE_RECORD:
					ServletDataMethods.cloneRecord(sessionData, request, databaseDefn,
							multipartItems);
					break;
				case UPDATE_RECORD:
					ServletDataMethods.saveRecord(sessionData, request, false, databaseDefn,
							multipartItems);
					break;
				case REMOVE_RECORD:
					ServletDataMethods.removeRecord(sessionData, request, databaseDefn);
					break;
				case GLOBAL_EDIT:
					ServletDataMethods.globalEdit(sessionData, request, databaseDefn,
							multipartItems);
					break;
				case CSV_IMPORT:
					ServletDataMethods.importRecords(sessionData, request, databaseDefn,
							multipartItems);
					break;
				case LOCK_RECORDS:
					ServletDataMethods.lockRecords(sessionData, request, databaseDefn);
					break;
				case ADD_COMPANY:
					ServletSchemaMethods.addCompany(request, databaseDefn.getAuthManager());
					break;
				case REMOVE_COMPANY:
					ServletSchemaMethods.removeCompany(request, databaseDefn.getAuthManager());
					break;
				case ANONYMISE:
					ServletDataMethods.anonymiseTableData(sessionData, request, databaseDefn,
							multipartItems);
					break;
				case ADD_MODULE:
					ServletSchemaMethods.addModule(request, sessionData, databaseDefn
							.getAuthManager());
					break;
				case REMOVE_MODULE:
					ServletSchemaMethods.removeModule(request, sessionData, databaseDefn);
					break;
				case UPDATE_MODULE:
					ServletSchemaMethods.updateModule(request, sessionData, databaseDefn
							.getAuthManager());
					break;
				case ADD_TAB_ADDRESS:
					ServletSchemaMethods.addTabAddress(request, databaseDefn.getAuthManager(),
							databaseDefn);
					break;
				case REMOVE_TAB_ADDRESS:
					ServletSchemaMethods.removeTabAddress(request, databaseDefn.getAuthManager(),
							databaseDefn);
					break;
				}
			}
		}
	}

	public static void carryOutPostSessionActions(HttpServletRequest request,
			SessionDataInfo sessionData, DatabaseInfo databaseDefn,
			EnumSet<SessionAction> sessionActions) throws SQLException, ObjectNotFoundException,
			MissingParametersException, DisallowedException {
		for (SessionAction sessionAction : sessionActions) {
			String sessionActionParam = request.getParameter(sessionAction.toString().toLowerCase(
					Locale.UK));
			if (sessionActionParam != null) {
				switch (sessionAction) {
				case POSTSET_REPORT:
					ServletDataMethods.setSessionReport(request, sessionData, sessionActionParam,
							databaseDefn);
					break;
				case POSTSET_TABLE:
					ServletDataMethods.setSessionTable(sessionData, request, sessionActionParam,
							databaseDefn);
					break;
				case POSTSET_CUSTOM_TABLE:
					ServletDataMethods.setSessionCustomTable(sessionData, request, false,
							databaseDefn);
					break;
				case POSTSET_CUSTOM_REPORT:
					ServletDataMethods.setSessionCustomReport(sessionData, request, false,
							databaseDefn);
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
			Context context) throws ServletException, ObjectNotFoundException {
		// Start timing request
		long handleRequestStartTime = System.currentTimeMillis();
		// Cache multipart form data so don't have to reparse it all the time
		List<FileItem> multipartItems = getMultipartItems(request);
		logger.debug("Request: " + getRequestQuery(request));
		ResponseReturnType returnType = setReturnType(request, response, multipartItems);
		HttpSession session = request.getSession();
		SessionDataInfo sessionData = (SessionDataInfo) session
				.getAttribute("com.gtwm.pb.servlets.sessionData");
		if (sessionData == null) {
			try {
				// Set up a session for a newly logged in user
				sessionData = new SessionData(this.databaseDefn, this.relationalDataSource, request);
			} catch (SQLException sqlex) {
				logException(sqlex, request, "SQL error creating session data object: " + sqlex);
				sessionData = new SessionData();
				// throw new
				// ServletException("SQL error creating session data object",
				// sqlex);
			} catch (AgileBaseException pbex) {
				logException(pbex, request, "Error creating session data object: " + pbex);
				sessionData = new SessionData();
				// throw new
				// ServletException("Error creating session data object", pbex);
			}
			// set up the wiki if the user is the first user logging in from a
			// particular company
			CompanyInfo company = this.databaseDefn.getAuthManager().getCompanyForLoggedInUser(
					request);
			WikiManagementInfo wikiManagement = this.databaseDefn.getWikiManagement(company);
			if (wikiManagement == null) {
				InitialContext initialContext = null;
				DataSource wikiDataSource = null;
				try {
					initialContext = new InitialContext();
					String dataSourceUrl = "java:comp/env/jdbc/wiki"
							+ company.getCompanyName().toLowerCase().replaceAll("\\W", "");
					wikiDataSource = (DataSource) initialContext.lookup(dataSourceUrl);
				} catch (NamingException nex) {
					wikiDataSource = null;
				}
				wikiManagement = new MediaWikiManagement(company, wikiDataSource);
				this.databaseDefn.addWikiManagement(company, wikiManagement);
			}
		}

		String templateName = getParameter(request, "return", multipartItems);
		try {
			carryOutSessionActions(request, sessionData, this.databaseDefn, context, session,
					multipartItems);
		} catch (AgileBaseException pbex) {
			logException(pbex, request, "Error setting session data");
			if (returnType.equals(ResponseReturnType.XML)) {
				return getUserInterfaceTemplate(request, response, templateName, context, session,
						sessionData, pbex, multipartItems);
			} else {
				throw new ServletException("Error setting session data", pbex);
			}
		} catch (NumberFormatException nfex) {
			logException(nfex, request, "Non-numeric value specified for numeric parameter");
			if (returnType.equals(ResponseReturnType.XML)) {
				return getUserInterfaceTemplate(request, response, templateName, context, session,
						sessionData, nfex, multipartItems);
			} else {
				throw new ServletException("Non-numeric value specified for numeric parameter in "
						+ getRequestQuery(request), nfex);
			}
		} catch (RuntimeException rtex) {
			logException(rtex, request, "Runtime error setting session data");
			if (returnType.equals(ResponseReturnType.XML)) {
				return getUserInterfaceTemplate(request, response, templateName, context, session,
						sessionData, rtex, multipartItems);
			} else {
				throw new ServletException("Runtime error setting session data", rtex);
			}
		} catch (SQLException sqlex) {
			logException(sqlex, request, "SQL error setting session data");
			// override to return the error template
			templateName = "report_error";
			return getUserInterfaceTemplate(request, response, templateName, context, session,
					sessionData, sqlex, multipartItems);
		} catch (Exception ex) {
			logException(ex, request, "General error setting session data");
			if (returnType.equals(ResponseReturnType.XML)) {
				return getUserInterfaceTemplate(request, response, templateName, context, session,
						sessionData, ex, multipartItems);
			} else {
				// override to return the error template
				templateName = "report_error";
				return getUserInterfaceTemplate(request, response, templateName, context, session,
						sessionData, ex, multipartItems);
			}
		}
		// Use StringBuffer to get a mutable string that can be altered by
		// carryOutAppActions
		StringBuffer appActionName = new StringBuffer("");
		try {
			carryOutAppActions(request, sessionData, this.databaseDefn, multipartItems,
					appActionName);
		} catch (MissingParametersException mpex) {
			logException(mpex, request, "Required parameters missing for action " + appActionName);
			return getUserInterfaceTemplate(request, response, templateName, context, session,
					sessionData, mpex, multipartItems);
		} catch (DisallowedException dex) {
			logException(dex, request, "No privileges to perform action " + appActionName);
			return getUserInterfaceTemplate(request, response, templateName, context, session,
					sessionData, dex, multipartItems);
		} catch (SQLException sqlex) {
			logException(sqlex, request,
					"Error accessing relational database while performing action" + appActionName);
			return getUserInterfaceTemplate(request, response, templateName, context, session,
					sessionData, sqlex, multipartItems);
		} catch (ObjectNotFoundException onfex) {
			logException(onfex, request, "An object mis-referenced while performing action "
					+ appActionName);
			return getUserInterfaceTemplate(request, response, templateName, context, session,
					sessionData, onfex, multipartItems);
		} catch (InputRecordException irex) {
			logException(irex, request, "Error saving data during " + appActionName);
			return getUserInterfaceTemplate(request, response, templateName, context, session,
					sessionData, irex, multipartItems);
		} catch (Exception ex) {
			logException(ex, request, "General error performing action " + appActionName);
			return getUserInterfaceTemplate(request, response, templateName, context, session,
					sessionData, ex, multipartItems);
		}

		// Set any new values for the session variables after carrying out main
		// app actions
		EnumSet<SessionAction> sessionActions = EnumSet.allOf(SessionAction.class);
		try {
			carryOutPostSessionActions(request, sessionData, this.databaseDefn, sessionActions);
		} catch (ObjectNotFoundException onfex) {
			logException(onfex, request, "Error setting session data");
			if (returnType.equals(ResponseReturnType.XML)) {
				return getUserInterfaceTemplate(request, response, templateName, context, session,
						sessionData, onfex, multipartItems);
			} else {
				throw new ServletException("Error setting session data", onfex);
			}
		} catch (NumberFormatException nfex) {
			logException(nfex, request, "Non-numeric value specified for numeric parameter");
			if (returnType.equals(ResponseReturnType.XML)) {
				return getUserInterfaceTemplate(request, response, templateName, context, session,
						sessionData, nfex, multipartItems);
			} else {
				throw new ServletException("Non-numeric value specified for numeric parameter in "
						+ getRequestQuery(request), nfex);
			}
		} catch (MissingParametersException mpex) {
			logException(mpex, request, "Error setting session data");
			if (returnType.equals(ResponseReturnType.XML)) {
				return getUserInterfaceTemplate(request, response, templateName, context, session,
						sessionData, mpex, multipartItems);
			} else {
				throw new ServletException("Error setting session data", mpex);
			}
		} catch (RuntimeException rtex) {
			logException(rtex, request, "Runtime error setting session data");
			if (returnType.equals(ResponseReturnType.XML)) {
				return getUserInterfaceTemplate(request, response, templateName, context, session,
						sessionData, rtex, multipartItems);
			} else {
				throw new ServletException("Runtime error setting session data", rtex);
			}
		} catch (Exception ex) {
			logException(ex, request, "General error setting session data");
			if (returnType.equals(ResponseReturnType.XML)) {
				return getUserInterfaceTemplate(request, response, templateName, context, session,
						sessionData, ex, multipartItems);
			} else {
				throw new ServletException("General error setting session data", ex);
			}
		}

		// Log long request times - doesn't include template rendering time: see
		// mergeTemplate for those
		float secondsToHandleRequest = (System.currentTimeMillis() - handleRequestStartTime)
				/ ((float) 1000);
		if (secondsToHandleRequest > AppProperties.longProcessingTime) {
			String warnMessage = 
			"Long server request processing time of "
					+ String.valueOf(secondsToHandleRequest) + " seconds for URL "
					+ getRequestQuery(request) + "\r\n";
			warnMessage += "Logged in user: " + request.getRemoteUser();
			logger.warn(warnMessage);
		}
		// Don't to HibernateUtil.closeSession() here, leave it open for the
		// view to display
		// in mergeTemplate()
		// Create ViewMethods object and return the requested template
		return getUserInterfaceTemplate(request, response, templateName, context, session,
				sessionData, null, multipartItems);
	}

	/**
	 * Replacement for and wrapper around
	 * HttpServletRequest.getParameter(String) which works for multi-part form
	 * data as well as normal requests
	 */
	public static String getParameter(HttpServletRequest request, String parameterName,
			List<FileItem> multipartItems) {
		if (FileUpload.isMultipartContent(new ServletRequestContext(request))) {
			for (FileItem item : multipartItems) {
				if (item.getFieldName().equals(parameterName)) {
					if (item.isFormField()) {
						return item.getString();
					} else {
						return item.getName();
					}
				}
			}
			return null;
		} else {
			return request.getParameter(parameterName);
		}
	}

	/**
	 * Like HttpServletgetRequestQuery(request) but works for POST as well as
	 * GET: In the case of POST requests, constructs a query string from
	 * parameter names & values
	 * 
	 * @see HttpServletRequest#getQueryString()
	 */
	public static String getRequestQuery(HttpServletRequest request) {
		String requestQuery = request.getQueryString();
		if (requestQuery != null) {
			return "GET: " + requestQuery;
		}
		if (FileUpload.isMultipartContent(new ServletRequestContext(request))) {
			return "POST: file upload";
		}
		requestQuery = "POST: ";
		Map<String, String[]> parameterMap = request.getParameterMap();
		for (Map.Entry<String, String[]> parameterEntry : parameterMap.entrySet()) {
			requestQuery += "&" + parameterEntry.getKey() + "=" + parameterEntry.getValue()[0];
		}
		return requestQuery;
	}

	/**
	 * Create an instance of ViewMethods to provide the UI with the necessary
	 * functionality, and return the requested template.
	 * 
	 * Note: There's no Hibernate exception handling here because the method
	 * doesn't access schema information. If code is added which will use
	 * Hibernate, exception handling will have to be added to make sure the Hib.
	 * transaction & session are closed
	 * 
	 * @param exceptionCaught
	 *            An exception thrown by handleRequest. Pass null if none. This
	 *            will be saved in ViewMethods to allow the UI to find out what
	 *            went wrong
	 * @return The template requested, ready to parse by the UI
	 */
	private Template getUserInterfaceTemplate(HttpServletRequest request,
			HttpServletResponse response, String templateName, Context context,
			HttpSession session, SessionDataInfo sessionData, Exception exceptionCaught,
			List<FileItem> multipartItems) throws ServletException {
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
		} catch (ObjectNotFoundException onfex) {
			logException(onfex, request, "Error creating view methods object");
			throw new ServletException("Error creating view methods object", onfex);
		}
		// template ('return' parameter) *must* be specified
		if (templateName == null) {
			logger
					.error("No template specified. Please add 'return=<i>templatename</i>' to the HTTP request");
			throw new ServletException(
					"No template specified. Please add 'return=<i>templatename</i>' to the HTTP request");
		}
		templateName = templateName + ".vm";
		Template template = null;
		try {
			// See note about template locations at top of file
			template = getTemplate(templateName);
		} catch (ResourceNotFoundException rnfe) {
			logger.error("Template not found: " + rnfe);
			throw new ServletException("Template not found", rnfe);
		} catch (ParseErrorException pee) {
			logger.error("Syntax error in the template: " + pee);
			throw new ServletException("Syntax error in the template", pee);
		} catch (AgileBaseException pbex) {
			logger.error("Exception instantiating view: " + pbex);
		} catch (SQLException sqlex) {
			logger.error("Database error instantiating view: " + sqlex);
		} catch (Exception ex) {
			logger.error("General templating error in UI template " + templateName + ": " + ex);
			throw new ServletException("General templating error", ex);
		}
		return template;
	}

	/**
	 * Override Velocity's mergeTemplate to a) time the template processing b)
	 * redirect on error
	 */
	public void mergeTemplate(Template template, Context context, HttpServletResponse response)
			throws ServletException, IOException {
		long mergeTemplateStartTime = System.currentTimeMillis();
		try {
			super.mergeTemplate(template, context, response);
		} catch (Exception ex) {
			logException(ex, "Error interpreting template " + template.getName());
			try {
				// Make the exception that just occurred accessible for
				// reporting
				ViewMethodsInfo viewMethods = (ViewMethodsInfo) context.get("view");
				viewMethods.setException(ex);
				context.put("view", viewMethods);
				Template errorTemplate = getTemplate(AppProperties.errorTemplateLocation);
				super.mergeTemplate(errorTemplate, context, response);
			} catch (ResourceNotFoundException rnfe) {
				logger.error("Error template not found: " + rnfe);
				throw new ServletException("Template not found", rnfe);
			} catch (ParseErrorException pee) {
				logger.error("Syntax error in the errortemplate: " + pee);
				throw new ServletException("Syntax error in the template", pee);
			} catch (Exception exex) {
				logger.error("General templating error: " + exex);
				throw new ServletException("General templating error whilst reporting error", exex);
			}
			// Old error handling method
			// throw new ServletException("Error interpreting template " +
			// template.getName(), ex);
		}
		float secondsToHandleMerge = (System.currentTimeMillis() - mergeTemplateStartTime)
				/ ((float) 1000);
		if (secondsToHandleMerge > AppProperties.longProcessingTime) {
			logger.warn("Long template request processing time of "
					+ String.valueOf(secondsToHandleMerge) + " seconds for template "
					+ template.getName());
			ViewMethodsInfo viewMethods = (ViewMethodsInfo) context.get("view");
			try {
				logger.warn("Logged in user: " + viewMethods.getLoggedInUser());
			} catch (DisallowedException dex) {
				logger.warn("Not allowed to get logged in user: " + dex);
			} catch (ObjectNotFoundException onfex) {
				logger.warn("Unable to find logged in user: " + onfex);
			}
		}
	}

	/**
	 * Log errors with as much information as possible: include user, URL,
	 * recursive causes and a stack trace to the original occurence in the
	 * application
	 * 
	 * NB Doesn't throw a servletException, that has to be done as well as
	 * calling this
	 */
	public static void logException(Exception ex, HttpServletRequest request, String topLevelMessage) {
		String errorMessage = "";
		if (topLevelMessage != null) {
			errorMessage += topLevelMessage + "\r\n" + " - ";
		}
		errorMessage += ex.toString() + "\r\n";
		errorMessage += " - URL = " + getRequestQuery(request) + "\r\n";
		errorMessage += " - Logged in user: " + request.getRemoteUser() + "\r\n";
		errorMessage += getExceptionCauses(ex);
		logger.error(errorMessage);
	}

	/**
	 * Log errors in a helpful format. Use this version when no
	 * HttpServletRequest object is available
	 * 
	 * @see #logException(Exception, HttpServletRequest, String)
	 */
	public static void logException(Exception ex, String topLevelMessage) {
		String errorMessage = "";
		if (topLevelMessage != null) {
			errorMessage += topLevelMessage + "\r\n" + " - ";
		}
		errorMessage += ex.toString() + "\r\n";
		errorMessage += getExceptionCauses(ex);
		logger.warn(errorMessage);
	}

	/**
	 * Return a string for logging purposes, of an exception's 'cause stack',
	 * i.e. the original exception(s) thrown and stack trace, i.e. the methods
	 * that the exception was thrown through.
	 * 
	 * Called by logException
	 */
	private static String getExceptionCauses(Exception ex) {
		String errorMessage = ex.toString() + "\r\n";
		if (ex.getCause() != null) {
			// Recursively find causes of exception
			errorMessage += " - Error was due to...";
			Exception exceptionCause = ex;
			String causeIndent = " - ";
			errorMessage += causeIndent + ex.toString() + "\r\n";
			while (exceptionCause.getCause() != null) {
				if (exceptionCause.getCause() instanceof Exception) {
					exceptionCause = (Exception) exceptionCause.getCause();
					causeIndent += " - ";
					errorMessage += causeIndent + getExceptionCauses(exceptionCause);
				}
			}
		}
		// Include out relevant parts of the stack trace
		StackTraceElement[] stackTrace = ex.getStackTrace();
		if (stackTrace.length > 0) {
			errorMessage += " - Stack trace:\r\n";
			int nonGtwmClassesLogged = 0;
			for (StackTraceElement stackTraceElement : stackTrace) {
				if (!stackTraceElement.getClassName().startsWith("com.gtwm.")) {
					nonGtwmClassesLogged++;
				}
				// Only trace our own classes + a few more, stop shortly after
				// we get to java language or 3rd party classes
				if (nonGtwmClassesLogged < 15) {
					errorMessage += "   " + stackTraceElement.toString() + "\r\n";
				}
			}
		}
		return errorMessage;
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