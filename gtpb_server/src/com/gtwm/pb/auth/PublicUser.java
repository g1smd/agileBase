/*  Copyright 2011 GT webMarque Ltd
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
package com.gtwm.pb.auth;

import java.util.HashSet;
import java.util.Set;
import com.gtwm.pb.model.interfaces.AppUserInfo;
import com.gtwm.pb.model.interfaces.AuthenticatorInfo;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.CompanyInfo;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.Enumerations.UserType;
import com.gtwm.pb.util.MissingParametersException;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.RandomString;

/**
 * Represents a *not logged in* public user who will have VIEW privileges on all tables
 */
public class PublicUser implements AppUserInfo {

	private PublicUser() {
		this.username = null;
		this.surname = null;
		this.forename = null;
		this.company = null;
	}

	public PublicUser(AuthenticatorInfo authenticator, String internalCompanyName, String userName, String surname,
			String forename) throws MissingParametersException, ObjectNotFoundException {
		if (userName == null) {
			throw new MissingParametersException("Username not specified");
		}
		if (userName.equals("")) {
			throw new MissingParametersException("Username blank");
		}
		Set<CompanyInfo> companies = ((Authenticator) authenticator).getCompanies();
		CompanyInfo foundCompany = null;
		COMPANY_LOOP: for (CompanyInfo company : companies) {
			if(internalCompanyName.equals(company.getInternalCompanyName())) {
				foundCompany = company;
				break COMPANY_LOOP;
			}
		}
		if (foundCompany == null) {
			throw new ObjectNotFoundException("Company with ID " + internalCompanyName + " not found");
		}
		this.company = foundCompany;
		this.username = userName;
		this.surname = surname;
		this.forename = forename;
	}

	private void setCompany(CompanyInfo company) throws CantDoThatException {
		throw new CantDoThatException("This public user cannot be reassigned to another company");
	}

	public CompanyInfo getCompany() {
		return this.company;
	}

	public String getInternalUserName() {
		return this.internalUserName;
	}

	public void setUserName(String userName) throws MissingParametersException, CantDoThatException {
		throw new CantDoThatException("The username for the public user " + this + " cannot be changed");
	}

	public String getUserName() {
		return this.username;
	}

	public void setSurname(String surname) throws CantDoThatException {
		throw new CantDoThatException("The name for the public user " + this + " cannot be changed");
	}

	public String getSurname() {
		return this.surname;
	}

	public void setForename(String forename) throws CantDoThatException {
		throw new CantDoThatException("The name for the public user " + this + " cannot be changed");
	}

	public String getForename() {
		return this.forename;
	}

	public void setPassword(String password) throws MissingParametersException, CantDoThatException {
		throw new CantDoThatException("The password for the public user " + this + " cannot be set");
	}

	public String getPassword() throws CantDoThatException {
		throw new CantDoThatException("This public user has no password");
	}

	public void setUserType(UserType userType) throws CantDoThatException {
		throw new CantDoThatException("The user type for the public user " + this + " cannot be set");
	}

	public UserType getUserType() {
		return UserType.EXTERNAL;
	}

	public Set<BaseReportInfo> getHiddenReports()  {
		return new HashSet<BaseReportInfo>();
	}
	
	public Set<BaseReportInfo> getOperationalDashboardReports() {
		return new HashSet<BaseReportInfo>();
	}

	public void hideReport(BaseReportInfo report) throws CantDoThatException {
		throw new CantDoThatException("This public user can have no hidden reports");
	}

	public void unhideReport(BaseReportInfo report) throws CantDoThatException {
		throw new CantDoThatException("This public user can have no hidden reports");
	}

	public void removeOperationalDashboardReport(BaseReportInfo report) throws CantDoThatException {
		throw new CantDoThatException("This public user can have no dashboard reports");
	}

	public void addOperationalDashboardReport(BaseReportInfo report) throws CantDoThatException {
		throw new CantDoThatException("This public user can have no dashboard reports");
	}

	public BaseReportInfo getDefaultReport() throws CantDoThatException {
		throw new CantDoThatException("This public user can have no default report");
	}

	public void setDefaultReport(BaseReportInfo report) throws CantDoThatException {
		throw new CantDoThatException("This public user can have no default report");
	}
	
	public void contractSection(String internalFieldName) throws CantDoThatException {
		throw new CantDoThatException("This public user can have no contracted sections");
	}
	
	public void expandSection(String internalFieldName) throws CantDoThatException {
		throw new CantDoThatException("This public user can have no contracted sections");
	}

	public Set<String> getContractedSections() {
		return new HashSet<String>();
	}

	public String toString() {
		return this.getUserName();
	}

	public int compareTo(AppUserInfo anotherAppUser) {
		if (this == anotherAppUser) {
			return 0;
		}
		int comparison = this.getUserName().compareTo(anotherAppUser.getUserName());
		if (comparison != 0) {
			return comparison;
		}
		return this.getInternalUserName().compareTo(anotherAppUser.getInternalUserName());
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		return this.getInternalUserName().equals(((AppUserInfo) obj).getInternalUserName());
	}

	public int hashCode() {
		if (this.hashCode == 0) {
			this.hashCode = this.getInternalUserName().hashCode();
		}
		return this.hashCode;
	}

	private volatile int hashCode = 0;

	private final CompanyInfo company;
	
	private final String username;
	
	private final String forename;
	
	private final String surname;

	private final String internalUserName = (new RandomString()).toString();

}
