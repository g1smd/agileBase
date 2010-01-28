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
package com.gtwm.pb.model.interfaces;

import javax.servlet.http.*;
import com.gtwm.pb.auth.PrivilegeType;
import com.gtwm.pb.util.CodingErrorException;

/**
 * Acts as a store of authentication and access control information. Allows the
 * app to check whether the logged in user has particular privileges.
 */
public interface AuthenticatorInfo {

	/**
	 * Checks whether the logged in user has a particular general privilege,
	 * either through directly having the privilege or being a member of a role
	 * that has the privilege
	 * 
	 * @param request
	 *            Allows the method to find the user making the request. Should
	 *            be the request object passed to the servlet.
	 *            HttpServletRequest is used rather than just a String
	 *            representation of the username because it is harder to fake
	 *            the user that way - the user found can't be anyone other than
	 *            the logged in user
	 * @param privilegeType
	 *            What they can do
	 * @return Whether or not the user can do what's requested
	 */
	public boolean loggedInUserAllowedTo(HttpServletRequest request, PrivilegeType privilegeType);

	/**
	 * Checks whether the logged in user has a particular table-specific
	 * privilege
	 */
	public boolean loggedInUserAllowedTo(HttpServletRequest request, PrivilegeType privilegeType,
			TableInfo table);

	/**
	 * Checks whether a report is visible to the logged in user by checking
	 * whether that user has VIEW privileges on the parent table, all joined
	 * tables and recursively all joined reports
	 */
	public boolean loggedInUserAllowedToViewReport(HttpServletRequest request, BaseReportInfo report) throws CodingErrorException;
}
