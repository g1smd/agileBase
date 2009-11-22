/*
 *  Copyright 2009 GT webMarque Ltd
 * 
 *  This file is part of GT portalBase.
 *
 *  GT portalBase is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  GT portalBase is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with GT portalBase.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gtwm.pb.model.interfaces;

import com.gtwm.pb.util.MissingParametersException;

/**
 * Represents an application user - someone who can log in to portalBase. Note
 * that no privilege checking is done in these methods, privilege checks must be
 * carried out before returning an AppUserInfo object
 */
public interface AppUserInfo {
	
	public static final String USERNAME = "userName";

	public static final String PASSWORD = "password";

	public static final String SURNAME = "surname";

	public static final String FORENAME = "forename";

	public CompanyInfo getCompany();

	public String getInternalUserName();

	public void setUserName(String userName) throws MissingParametersException;

	public String getUserName();

	public void setSurname(String surname);

	public String getSurname();

	public void setForename(String forename);

	public String getForename();

	public void setPassword(String password) throws MissingParametersException;

	/**
	 * Note: It is up to the UI to decide whether it wants to retrieve
	 * passwords, security conscious organisations may want 'write-only'
	 * passwords in which case this method should never be called
	 * 
	 * @return Plain text password
	 */
	public String getPassword();
}
