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

import javax.servlet.http.HttpServletRequest;

import com.gtwm.pb.auth.DisallowedException;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.DatabaseInfo;
import com.gtwm.pb.model.interfaces.SessionDataInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.util.MissingParametersException;
import com.gtwm.pb.util.ObjectNotFoundException;

/**
 * Methods shared between ServletAuthMethods, ServletSchemaMethods, ServletDataMethods
 */
public final class ServletUtilMethods {

	private ServletUtilMethods() {
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
			TableInfo table = databaseDefn.findTableContainingReport(request, internalReportName);
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
	
	public static int getRowIdForRequest(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn, boolean defaultToSessionRowId) throws ObjectNotFoundException, MissingParametersException {
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

}
