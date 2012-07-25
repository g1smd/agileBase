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
package com.gtwm.pb.auth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import com.gtwm.pb.model.interfaces.AppUserInfo;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.CompanyInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.manageData.fields.TextValueDefn;
import com.gtwm.pb.model.manageSchema.BaseReportDefn;
import com.gtwm.pb.model.manageSchema.TableDefn;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.Helpers;
import com.gtwm.pb.util.MissingParametersException;
import com.gtwm.pb.util.RandomString;
import com.gtwm.pb.util.Enumerations.InitialView;
import javax.mail.MessagingException;
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

import org.apache.commons.codec.digest.DigestUtils;
import org.grlea.log.SimpleLogger;

@Entity
public class AppUser implements AppUserInfo, Comparable<AppUserInfo> {

	private AppUser() {
	}

	public AppUser(CompanyInfo company, String internalUserName, String userName, String surname,
			String forename, String password) throws MissingParametersException, CantDoThatException, CodingErrorException {
		if (userName == null || password == null) {
			throw new MissingParametersException("User name or password not specified");
		}
		if (userName.equals("") || password.equals("")) {
			throw new MissingParametersException("User name or password blank");
		}
		this.setCompany(company);
		if (internalUserName == null) {
			this.setInternalUserName(RandomString.generate());
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
		this.hashAndSetPassword(password);
		// Give them a default UI layout
		this.setUserType(InitialView.REPORT);
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

	private String getPassword() {
		return this.password;
	}

	public void hashAndSetPassword(String plainPassword) throws MissingParametersException, CantDoThatException, CodingErrorException {
		if (plainPassword == null) {
			throw new MissingParametersException("Password not specified");
		}
		if (plainPassword.equals("")) {
			throw new MissingParametersException("Password blank");
		}
		//try {
			//MessageDigest md = MessageDigest.getInstance("MD5");
			//logger.debug("Plain password is " + plainPassword);
			//logger.debug("To and from bytes is " + String.valueOf(plainPassword.getBytes()));
			//byte[] plainBytes = plainPassword.getBytes();
			//String hashedPassword = String.valueOf(md.digest(plainPassword.getBytes()));
			String hashedPassword = DigestUtils.md5Hex(plainPassword);
			logger.debug("Hashed password = " + hashedPassword);
			this.setPassword(hashedPassword);
			// Reset the password timer so a password can only be reset once from a single email notification
			this.passwordResetSent = 0;
		//} catch (NoSuchAlgorithmException nsaex) {
		//	throw new CodingErrorException("Algorithm MD5 not found: " + nsaex, nsaex);
		//}
	}
	
	/*
	 * Used by Hibernate
	 */
	private void setPassword(String password) throws MissingParametersException {
		this.password = password;
	}
	
	public String getEmail() {
		return this.email;
	}
	
	public void setEmail(String email) throws CantDoThatException {
		this.email = email;
	}

	@Enumerated(EnumType.STRING)
	public InitialView getUserType() {
		return this.initialView;
	}

	public void setUserType(InitialView userType) {
		this.initialView = userType;
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
	public Set<TableInfo> getFormTables() {
		return Collections.unmodifiableSortedSet(new TreeSet<TableInfo>(this
				.getFormTablesDirect()));
	}

	public synchronized void removeFormTable(TableInfo table) {
		this.getFormTablesDirect().remove(table);
	}

	public synchronized void addFormTable(TableInfo table) {
		this.getFormTablesDirect().add(table);
	}

	@ManyToMany(targetEntity = TableDefn.class, cascade={})
	private Set<TableInfo> getFormTablesDirect() {
		return this.formTables;
	}

	/**
	 * For Hibernate use only
	 */
	private synchronized void setFormTablesDirect(Set<TableInfo> formTables) {
		this.formTables = formTables;
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
	public Set<String> getContractedSections() {
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
	
	public boolean getUsesCustomUI() {
		return this.usesCustomUI;
	}
	
	public void setUsesCustomUI(boolean usesCustomUI) {
		this.usesCustomUI = usesCustomUI;
	}
	
	@Transient
	public boolean getAllowPasswordReset() {
		// Request times out after a day
		if ((System.currentTimeMillis() - this.passwordResetSent) < (24*60*60*1000)) {
			return true;
		} 
		return false;
	}
	
	public void sendPasswordReset(String appUrl) throws CantDoThatException, CodingErrorException, MessagingException {
		if (this.getEmail() == null) {
			throw new CantDoThatException("The user has no email address");
		}
		if (!this.getEmail().contains("@")) {
			throw new CantDoThatException("The user's email isn't valid");
		}
		try {
			String password = RandomString.generate();
			this.hashAndSetPassword(password);
			String passwordResetLink = appUrl + "?return=gui/set_password/email_reset&u=" + this.getUserName() + "&x=" + password;
			if (this.getAllowPasswordReset()) {
				throw new CantDoThatException("The previous password reset request hasn't timed out yet, please use that: " + passwordResetLink);
			}
			Set<String> recipients = new HashSet<String>();
			recipients.add(this.getEmail());
			String subject = "Set your password";
			String body = "Please choose a password for your account by following this link:\n\n";
			body += passwordResetLink + "\n";
			Helpers.sendEmail(recipients, body, subject);
		} catch (MissingParametersException mpex) {
			throw new CodingErrorException("Error generating a password: " + mpex);
		}
		this.passwordResetSent = System.currentTimeMillis();
	}
	
	public String toString() {
		return this.getUserName();
	}

	public int compareTo(AppUserInfo anotherAppUser) {
		if (this == anotherAppUser) {
			return 0;
		}
		int comparison = this.getUserName().toLowerCase().compareTo(anotherAppUser.getUserName().toLowerCase());
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

	private String internalUserName;

	private String userName;

	private String surname = "";

	private String forename = "";

	private InitialView initialView = null;

	private String password;
	
	private String email;

	private CompanyInfo company = null;

	private Set<BaseReportInfo> hiddenReports = new HashSet<BaseReportInfo>();
	
	private Set<BaseReportInfo> operationalDashboardReports = new HashSet<BaseReportInfo>();
	
	private Set<TableInfo> formTables = new HashSet<TableInfo>();
	
	private Set<String> contractedSections = new HashSet<String>();
	
	private BaseReportInfo defaultReport = null;
	
	private boolean usesCustomUI = false;
	
	/**
	 * Epoch time at which a password reset email was sent
	 */
	private long passwordResetSent = 0;

	private static final SimpleLogger logger = new SimpleLogger(AppUser.class);
}
