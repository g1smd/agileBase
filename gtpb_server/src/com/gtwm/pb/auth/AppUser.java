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
package com.gtwm.pb.auth;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import com.gtwm.pb.model.interfaces.AppUserInfo;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.CompanyInfo;
import com.gtwm.pb.model.manageSchema.BaseReportDefn;
import com.gtwm.pb.util.MissingParametersException;
import com.gtwm.pb.util.RandomString;
import com.gtwm.pb.util.Enumerations.UserType;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.CollectionOfElements;

@Entity
public class AppUser implements AppUserInfo, Comparable<AppUserInfo> {

	private AppUser() {
	}

	public AppUser(CompanyInfo company, String internalUserName, String userName, String surname,
			String forename, String password) throws MissingParametersException {
		if (userName == null || password == null) {
			throw new MissingParametersException("User name or password not specified");
		}
		if (userName.equals("") || password.equals("")) {
			throw new MissingParametersException("User name or password blank");
		}
		this.setCompany(company);
		if (internalUserName == null) {
			this.setInternalUserName((new RandomString()).toString());
		} else {
			this.setInternalUserName(internalUserName);
		}
		this.setUserName(userName);
		if (surname == null) {
			this.setSurname("");
		} else {
			this.setSurname(surname);
		}
		if (forename == null) {
			this.setForename("");
		} else {
			this.setForename(forename);
		}
		this.setPassword(password);
		// Give them a default user type
		this.setUserType(UserType.OPERATIONAL);
	}

	@ManyToOne(targetEntity = Company.class)
	public CompanyInfo getCompany() {
		return this.company;
	}

	@Id
	public String getInternalUserName() {
		return this.internalUserName;
	}

	private void setInternalUserName(String internalUserName) {
		this.internalUserName = internalUserName;
	}

	private void setCompany(CompanyInfo company) {
		this.company = company;
	}

	public String getUserName() {
		return this.userName;
	}

	public void setUserName(String userName) throws MissingParametersException {
		if (userName == null) {
			throw new MissingParametersException("User name not specified");
		}
		if (userName.equals("")) {
			throw new MissingParametersException("User name blank");
		}
		this.userName = userName;
	}

	public String getSurname() {
		return this.surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getForename() {
		return this.forename;
	}

	public void setForename(String forename) {
		this.forename = forename;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) throws MissingParametersException {
		if (password == null) {
			throw new MissingParametersException("Password not specified");
		}
		if (password.equals("")) {
			throw new MissingParametersException("Password blank");
		}
		this.password = password;
	}

	@Enumerated(EnumType.STRING)
	public UserType getUserType() {
		return this.userType;
	}

	public void setUserType(UserType userType) {
		this.userType = userType;
	}

	@Transient
	public Set<BaseReportInfo> getHiddenReports() {
		return Collections.unmodifiableSortedSet(new TreeSet<BaseReportInfo>(this
				.getHiddenReportsDirect()));
	}

	public synchronized void hideReport(BaseReportInfo report) {
		this.getHiddenReportsDirect().add(report);
	}

	public synchronized void unhideReport(BaseReportInfo report) {
		this.getHiddenReportsDirect().remove(report);
	}

	@ManyToMany(targetEntity = BaseReportDefn.class, cascade={})
	private Set<BaseReportInfo> getHiddenReportsDirect() {
		return this.hiddenReports;
	}

	/**
	 * For Hibernate use only
	 */
	private void setHiddenReportsDirect(Set<BaseReportInfo> hiddenReports) {
		this.hiddenReports = hiddenReports;
	}

	@Transient
	public Set<BaseReportInfo> getOperationalDashboardReports() {
		return Collections.unmodifiableSortedSet(new TreeSet<BaseReportInfo>(this
				.getOperationalDashboardReportsDirect()));
	}

	public synchronized void removeOperationalDashboardReport(BaseReportInfo report) {
		this.getOperationalDashboardReportsDirect().remove(report);
	}

	public synchronized void addOperationalDashboardReport(BaseReportInfo report) {
		this.getOperationalDashboardReportsDirect().add(report);
	}

	@ManyToMany(targetEntity = BaseReportDefn.class, cascade={})
	// We need a custom joinTable so Hibernate doesn't confuse this ManyToMany with that for hidden reports, which has the same object types
	@JoinTable(name="appuser_basereportdefn_opdash")
	private Set<BaseReportInfo> getOperationalDashboardReportsDirect() {
		return this.operationalDashboardReports;
	}

	/**
	 * For Hibernate use only
	 */
	private synchronized void setOperationalDashboardReportsDirect(Set<BaseReportInfo> operationalDashboardReports) {
		this.operationalDashboardReports = operationalDashboardReports;
	}

	public synchronized void contractSection(String internalFieldName) {
		this.getContractedSections().add(internalFieldName);
	}
	
	public synchronized void expandSection(String internalFieldName) {
		this.getContractedSections().remove(internalFieldName);
	}
	
	@ElementCollection(fetch = FetchType.EAGER)
	private Set<String> getContractedSections() {
		return this.contractedSections;
	}
	
	private void setContractedSections(Set<String> contractedSections) {
		this.contractedSections = contractedSections;
	}
	
	@OneToOne(targetEntity = BaseReportDefn.class, cascade={})
	public BaseReportInfo getDefaultReport() {
		return this.defaultReport;
	}
	
	public void setDefaultReport(BaseReportInfo report) {
		this.defaultReport = report;
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
		//TODO: actually, two users can't have the same username, isn't this reduntant?
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

	private String internalUserName;

	private String userName;

	private String surname = "";

	private String forename = "";

	private UserType userType = null;

	private String password;

	private CompanyInfo company = null;

	private Set<BaseReportInfo> hiddenReports = new HashSet<BaseReportInfo>();
	
	private Set<BaseReportInfo> operationalDashboardReports = new HashSet<BaseReportInfo>();
	
	private Set<String> contractedSections = new HashSet<String>();
	
	private BaseReportInfo defaultReport = null;
}
