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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.grlea.log.SimpleLogger;

import com.gtwm.pb.auth.DisallowedException;
import com.gtwm.pb.auth.PublicUser;
import com.gtwm.pb.model.interfaces.AppUserInfo;
import com.gtwm.pb.model.interfaces.AuthenticatorInfo;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.DatabaseInfo;
import com.gtwm.pb.model.interfaces.SessionDataInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.util.MissingParametersException;
import com.gtwm.pb.util.ObjectNotFoundException;

/**
 * Methods shared between ServletAuthMethods, ServletSchemaMethods,
 * ServletDataMethods
 */
public final class ServletUtilMethods {

	private ServletUtilMethods() {
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
	 * Provide the ability to cache multi-part items in a variable to save re-parsing
	 */
	public static List<FileItem> getMultipartItems(HttpServletRequest request) {
		List<FileItem> multipartItems = new LinkedList<FileItem>();
		if (FileUpload.isMultipartContent(new ServletRequestContext(request))) {
			// See http://jakarta.apache.org/commons/fileupload/using.html
			FileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);
			try {
				multipartItems = upload.parseRequest(request);
			} catch (FileUploadException fuex) {
				logException(fuex, request, "Error parsing multi-part form data");
			}
		}
		return multipartItems;
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
	 * Like HttpServlet#getRequestQuery(request) but works for POST as well as
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

	public static BaseReportInfo getReportForRequest(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn, boolean defaultToSessionReport)
			throws MissingParametersException, ObjectNotFoundException, DisallowedException {
		BaseReportInfo report = null;
		String internalReportName = request.getParameter("internalreportname");
		if ((internalReportName == null) && (defaultToSessionReport)) {
			report = sessionData.getReport();
			if (report == null) {
				throw new ObjectNotFoundException(
						"There is no report in the session - perhaps the session has timed out");
			}
		} else if (internalReportName == null) {
			throw new MissingParametersException("'internalreportname' parameter needed in request");
		} else {
			String internalTableName = request.getParameter("internaltablename");
			TableInfo table = null;
			if (internalTableName != null) {
				table = databaseDefn.getTable(request, internalTableName);
			} else {
				table = databaseDefn.findTableContainingReport(request, internalReportName);
			}
			report = table.getReport(internalReportName);
		}
		return report;
	}

	public static TableInfo getTableForRequest(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn, boolean defaultToSessionTable)
			throws MissingParametersException, ObjectNotFoundException, DisallowedException {
		TableInfo table = null;
		String internalTableName = request.getParameter("internaltablename");
		if ((internalTableName == null) && (defaultToSessionTable)) {
			table = sessionData.getTable();
			if (table == null) {
				throw new ObjectNotFoundException(
						"There is no table in the session - perhaps the session has timed out");
			}
		} else if (internalTableName == null) {
			throw new MissingParametersException("'internaltablename' parameter needed in request");
		} else {
			// locate the table by internalTableName
			// throws ObjectNotFoundException if table doesn't exist
			// throws DisallowedException is the user doesn't have permission to
			// view the table
			table = databaseDefn.getTable(request, internalTableName);
		}
		return table;
	}

	public static AppUserInfo getPublicUserForRequest(HttpServletRequest request,
			AuthenticatorInfo authenticator) throws MissingParametersException,
			ObjectNotFoundException {
		String internalCompanyName = request.getParameter("c");
		if (internalCompanyName == null) {
			throw new MissingParametersException("c needed to identify company");
		}
		String userName = "public";
		String forename = "Host " + request.getRemoteHost();
		String surname = "Addr " + request.getRemoteAddr();
		return new PublicUser(authenticator, internalCompanyName, userName, surname, forename);
	}

	public static int getRowIdForRequest(SessionDataInfo sessionData, HttpServletRequest request,
			DatabaseInfo databaseDefn, boolean defaultToSessionRowId)
			throws ObjectNotFoundException, MissingParametersException {
		int rowId = -1;
		String rowIdString = request.getParameter("rowid");
		if ((rowIdString == null) && defaultToSessionRowId) {
			rowId = sessionData.getRowId();
			if (rowId == -1) {
				throw new ObjectNotFoundException("There is no row id in the session");
			}
		} else if (rowIdString == null) {
			throw new MissingParametersException("'rowid' parameter needed in request");
		} else {
			rowId = Integer.valueOf(rowIdString);
		}
		return rowId;
	}

	public static final boolean USE_SESSION = true;

	public static final boolean DO_NOT_USE_SESSION = false;

	private static final SimpleLogger logger = new SimpleLogger(ServletUtilMethods.class);
}
