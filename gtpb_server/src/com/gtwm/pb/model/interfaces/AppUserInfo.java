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
package com.gtwm.pb.model.interfaces;

import java.util.Set;

import javax.mail.MessagingException;

import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.MissingParametersException;
import com.gtwm.pb.util.Enumerations.InitialView;

/**
 * Represents an application user - someone who can log in to agileBase. Note
 * that no privilege checking is done in these methods, privilege checks must be
 * carried out before returning an AppUserInfo object
 */
public interface AppUserInfo {

	// TODO: get rid of these contants, I think they're only used in one place
	public static final String USERNAME = "username";

	public static final String PASSWORD = "password";

	public static final String SURNAME = "surname";

	public static final String FORENAME = "forename";

	public static final String INITIALVIEW = "initialview";

	public static final String EMAIL = "email";

	public static final String USES_CUSTOM_UI = "uses_custom_ui";

	public CompanyInfo getCompany();

	public String getInternalUserName();

	public void setUserName(String userName) throws MissingParametersException, CantDoThatException;

	public String getUserName();

	public void setSurname(String surname) throws CantDoThatException;

	public String getSurname();

	public void setForename(String forename) throws CantDoThatException;

	public String getForename();

	public void hashAndSetPassword(String plainPassword) throws MissingParametersException,
			CantDoThatException, CodingErrorException;

	/*
	 * public void setPassword(String password) throws
	 * MissingParametersException, CantDoThatException;
	 */

	public String getEmail();

	public void setEmail(String email) throws CantDoThatException;

	public void setUserType(InitialView userType) throws CantDoThatException;

	public InitialView getUserType();

	/**
	 * A user can have some reports hidden from them, not for security reasons
	 * but to reduce clutter
	 */
	public Set<BaseReportInfo> getHiddenReports();

	/**
	 * Reports that should be visible by default on the user's operational
	 * dashboard and calendar
	 */
	public Set<BaseReportInfo> getOperationalDashboardReports();

	public void addOperationalDashboardReport(BaseReportInfo report) throws CantDoThatException;

	public void removeOperationalDashboardReport(BaseReportInfo report) throws CantDoThatException;

	/**
	 * Tables that the user can use the 'form' data input method with
	 */
	public Set<TableInfo> getFormTables() throws CantDoThatException;

	public void addFormTable(TableInfo table) throws CantDoThatException;

	public void removeFormTable(TableInfo table) throws CantDoThatException;

	public void hideReport(BaseReportInfo report) throws CantDoThatException;

	public void unhideReport(BaseReportInfo report) throws CantDoThatException;

	/**
	 * Record the fact that a section in the edit tab is contracted for this
	 * user
	 * 
	 * @param internalFieldName
	 *            The identifier of the section heading field
	 */
	public void contractSection(String internalFieldName) throws CantDoThatException;

	public void expandSection(String internalFieldName) throws CantDoThatException;

	public Set<String> getContractedSections();

	/**
	 * Get the initial report this user should see when logging in
	 */
	public BaseReportInfo getDefaultReport() throws CantDoThatException;

	public void setDefaultReport(BaseReportInfo report) throws CantDoThatException;

	/**
	 * Wether this user is set to use the company custom UI (if one exists).
	 * 
	 * If this is selected the user will be unable to use the agileBase standard
	 * interface but will be taken to the company custom interface on login
	 */
	public boolean getUsesCustomUI();

	public void setUsesCustomUI(boolean usesCustomUI) throws CantDoThatException;

	/**
	 * See whether the user can be sent a link to reset their password.
	 * 
	 * Note the current password is reset to a value that can be sent out by
	 * email, so between the time the email is sent and the user actually resets
	 * the password, the login is at risk. To help protect the account,
	 * passwords have to be reset within a certain time period. If this time
	 * period has passed,
	 */
	public boolean getAllowPasswordReset();

	/**
	 * 1) Sets the counter going for the time period in which the password can
	 * be reset.
	 * 
	 * 2) Resets the password to a random value
	 * 
	 * 3) Sends the user a password reset email
	 * 
	 * @param appUrl
	 *            is used in the email notification to the user
	 * 
	 * @throws CantDoThatException
	 *             if the user doesn't have an email address
	 */
	public void sendPasswordReset(String appUrl) throws CantDoThatException, CodingErrorException,
			MessagingException;
}
