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

import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.CompanyInfo;
import com.gtwm.pb.model.interfaces.AuthenticatorInfo;
import com.gtwm.pb.model.interfaces.AuthManagerInfo;
import com.gtwm.pb.model.interfaces.AppUserInfo;
import com.gtwm.pb.model.interfaces.AppRoleInfo;
import com.gtwm.pb.model.interfaces.ChartAggregateInfo;
import com.gtwm.pb.model.interfaces.ChartGroupingInfo;
import com.gtwm.pb.model.interfaces.ChartInfo;
import com.gtwm.pb.model.interfaces.ModuleInfo;
import com.gtwm.pb.model.interfaces.ReportFieldInfo;
import com.gtwm.pb.model.interfaces.RoleTablePrivilegeInfo;
import com.gtwm.pb.model.interfaces.SimpleReportInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.UserGeneralPrivilegeInfo;
import com.gtwm.pb.model.interfaces.RoleGeneralPrivilegeInfo;
import com.gtwm.pb.model.interfaces.UserTablePrivilegeInfo;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.manageSchema.ReportCalcFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.DecimalFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.IntegerFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.RelationFieldDefn;
import com.gtwm.pb.model.manageSchema.fields.TextFieldDefn;
import com.gtwm.pb.servlets.ServletSchemaMethods;
import com.gtwm.pb.util.AppProperties;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.Helpers;
import com.gtwm.pb.util.HibernateUtil;
import com.gtwm.pb.util.MissingParametersException;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.RandomString;
import com.gtwm.pb.util.Enumerations.InitialView;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import org.grlea.log.SimpleLogger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.HibernateException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import java.util.SortedSet;
import java.util.HashSet;
import java.util.TreeSet;

/**
 * Manages the application's Authenticator object - use the static methods in
 * this class to add user, roles and privileges. We are basically separating out
 * persistence code so that the Authenticator doesn't have to know about it
 */
public final class AuthManager implements AuthManagerInfo {

	/**
	 * Make the application ready for using authentication by loading the
	 * authentication object from the object database or creating a new one if it
	 * doesn't exist, populated with a default user
	 * 
	 * @throws ObjectNotFoundException
	 *           If there was an internal error populating a new authentication
	 *           object
	 * @throws SQLException
	 *           If there was an error bootstrapping the relational database
	 *           authentication tables
	 * 
	 *           Note: No exception handling (finally block) is done here, that's
	 *           in the DatabaseDefn constructor that calls this
	 */
	public AuthManager(DataSource relationalDataSource, String webAppRoot)
			throws ObjectNotFoundException, CantDoThatException, MissingParametersException,
			CodingErrorException, SQLException {
		logger.info("Loading schema and authentication objects into memory...");
		// Don't pollute javamelody log with startup SQL
		System.setProperty("javamelody.disabled", "true");
		try {
			Session hibernateSession = HibernateUtil.currentSession();
			Transaction hibernateTransaction = hibernateSession.beginTransaction();
			this.authenticator = (AuthenticatorInfo) hibernateSession.createQuery("from Authenticator")
					.uniqueResult();
			if (this.authenticator != null) {
				Authenticator auth = (Authenticator) this.authenticator;
				CompanyInfo singleCompany = null;
				for (CompanyInfo company : auth.getCompanies()) {
					// All the logger.info lines below are basically to force
					// Hibernate to read the objects into memory by printing out
					// their details
					logger.info("" + company + " roles: " + company.getRoles());
					logger.info("" + company + " users: " + company.getUsers());
					for (ModuleInfo module : company.getModules()) {
						logger.info("" + company + " module " + module + " related modules: "
								+ module.getRelatedModules());
					}
					// Company identifiers:
					// GTwM = aeaa2e59ef1798629
					// CH = aa1d402725710a94d
					// TD = dg1mzrcv443adjnpg
					if (company.getInternalCompanyName().equals("dg1mzrcv443adjnpg")) {
						singleCompany = company;
					}
				}
				for (AppUserInfo user : auth.getUsers()) {
					logger.info("User " + user + " hidden reports: " + user.getHiddenReports());
					logger.info("User " + user + " operational dashboard reports: "
							+ user.getOperationalDashboardReports());
					logger.info("User " + user + " form tables: " + user.getFormTables());
					((AppUser) user).setWebAppRoot(webAppRoot);
				}
				for (AppRoleInfo role : auth.getRoles()) {
					logger.info("Role " + role + " users: " + role.getUsers());
				}
				// Cache tables into companies.
				PRIVILEGE_LOOP: for (UserGeneralPrivilegeInfo privilege : auth.getUserPrivileges()) {
					if (privilege instanceof UserTablePrivilege) {
						UserTablePrivilege priv = (UserTablePrivilege) privilege;
						CompanyInfo company = priv.getUser().getCompany();
						if (AppProperties.testMode && !company.equals(singleCompany)) {
							continue PRIVILEGE_LOOP;
						}
						TableInfo table = priv.getTable();
						if (!company.getTables().contains(table)) {
							populateTableFromHibernate(table, relationalDataSource);
							company.addTable(table);
						}
					}
				}
				PRIVILEGE_LOOP: for (RoleGeneralPrivilegeInfo privilege : auth.getRolePrivileges()) {
					if (privilege instanceof RoleTablePrivilege) {
						RoleTablePrivilege priv = (RoleTablePrivilege) privilege;
						CompanyInfo company = priv.getRole().getCompany();
						if (AppProperties.testMode && !company.equals(singleCompany)) {
							continue PRIVILEGE_LOOP;
						}
						TableInfo table = priv.getTable();
						if (!company.getTables().contains(table)) {
							populateTableFromHibernate(table, relationalDataSource);
							company.addTable(table);
						}
					}
				}
				logger.info("User privileges: " + auth.getUserPrivileges());
				logger.info("Role privileges: " + auth.getRolePrivileges());
			}
			if (this.authenticator == null) {
				// There must be one and only one Authenticator object
				// persisted. If there isn't one,
				// we're doing the first boot of agileBase, create one and set
				// up the master user
				try {
					this.authenticator = new Authenticator();
					hibernateSession.save(this.authenticator);
					String masterPassword = RandomString.generate();
					String masterUsername = "master";
					CompanyInfo masterCompany = new Company("Master Company");
					((Authenticator) this.authenticator).addCompany(masterCompany);
					AppUserInfo masterUser = new AppUser(masterCompany, null, masterUsername, "Master",
							"User", masterPassword, "", "", false);
					((Authenticator) this.authenticator).addUser(masterUser);
					((Authenticator) this.authenticator).addUserPrivilege(masterUser, PrivilegeType.MASTER);
					hibernateTransaction.commit();
					logger.info("Created master company + user");
				} catch (HibernateException hex) {
					hibernateTransaction.rollback();
					throw hex;
				}
			}
		} catch (RuntimeException rtex) {
			throw rtex;
		}
		createCommentsTables(relationalDataSource);
		// Re-enable JavaMelody
		System.setProperty("javamelody.disabled", "false");
	}

	private void createCommentsTables(DataSource relationalDataSource) throws SQLException {
		Authenticator auth = (Authenticator) this.authenticator;
		Connection conn = null;
		try {
			conn = relationalDataSource.getConnection();
			conn.setAutoCommit(false);
			// Copy from original comments table to per-company tables
			// First record the company for each comment
			String sqlCode = "ALTER TABLE dbint_comments add column internalcompanyname varchar(1000)";
			Statement statement = conn.createStatement();
			statement.execute(sqlCode);
			statement.close();
			sqlCode = "UPDATE dbint_comments SET internalcompanyname=? WHERE internalfieldname=?";
			PreparedStatement updateStatement = conn.prepareStatement(sqlCode);
			for (CompanyInfo company : auth.getCompanies()) {
				ServletSchemaMethods.addCommentsTableForCompany(conn, company);
				for (TableInfo table : company.getTables()) {
					for (BaseField field : table.getFields()) {
						updateStatement.setString(1, company.getInternalCompanyName());
						updateStatement.setString(2, field.getInternalFieldName());
						updateStatement.executeUpdate();
					}
				}
				for (AppUserInfo appUser : company.getUsers()) {
					String name = appUser.getForename() + " " + appUser.getSurname();
					sqlCode = "INSERT INTO dbint_comments_" + company.getInternalCompanyName();
					sqlCode += "(created, author_internalusername, author, internalfieldname, rowid, text)";
					sqlCode += " SELECT created, '" + name + "'::text, author, internalfieldname, rowid, text";
					sqlCode += " FROM dbint_comments";
					sqlCode += " WHERE author=? AND internalcompanyname=?";
					preparedStatement = conn.prepareStatement(sqlCode);
					preparedStatement.setString(1, name);
					preparedStatement.setString(2, company.getInternalCompanyName());
					int rows = preparedStatement.executeUpdate();
					preparedStatement.close();
				}
			}
			updateStatement.close();
			conn.commit();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	/**
	 * This method makes sure all properties of a table are loaded, assuming we're
	 * inside a hibernate transaction
	 */
	private static void populateTableFromHibernate(TableInfo table, DataSource relationalDataSource)
			throws CantDoThatException, CodingErrorException {
		// Make sure all child objects are activated by calling toString()
		// (indirectly)
		logger.info("Loading table " + table);
		logger.info("...Table Fields: " + table.getFields());
		logger.info("...Table form tabs: " + table.getFormTabs());
		for (BaseReportInfo report : table.getReports()) {
			logger.info("...Report " + table + "." + report);
			if (report instanceof SimpleReportInfo) {
				SimpleReportInfo simpleReport = (SimpleReportInfo) report;
				Set<ReportFieldInfo> reportFields = simpleReport.getReportFields();
				logger.info("......Report fields: " + reportFields);
				for (ReportFieldInfo reportField : reportFields) {
					if (reportField instanceof ReportCalcFieldDefn) {
						((ReportCalcFieldDefn) reportField).setNonPersistentProperties();
					}
				}
				logger.info("......Report filters: " + simpleReport.getFilters());
				logger.info("......Report joins: " + simpleReport.getJoins());
				logger.info("......Report sorts: " + simpleReport.getSorts());
				logger.info("......Report distinct fields: " + simpleReport.getDistinctFields());
			}
			ChartInfo reportSummary = report.getChart();
			logger.info("......Report summary: " + reportSummary);
			for (ChartGroupingInfo grouping : reportSummary.getGroupings()) {
				logger.info(".........Grouping details: " + grouping + " - " + grouping.getCreationTime()
						+ " " + grouping.getGroupingModifier() + " " + grouping.getGroupingReportField());
			}
			for (ChartAggregateInfo aggregate : reportSummary.getAggregateFunctions()) {
				logger.info(".........Aggregate details: " + aggregate);
			}
			for (ChartInfo savedChart : report.getSavedCharts()) {
				logger.info("......Report summary: " + savedChart);
				for (ChartGroupingInfo grouping : savedChart.getGroupings()) {
					logger.info(".........Grouping details: " + grouping + " - " + grouping.getCreationTime()
							+ " " + grouping.getGroupingModifier() + " " + grouping.getGroupingReportField());
				}
				for (ChartAggregateInfo aggregate : savedChart.getAggregateFunctions()) {
					logger.info(".........Aggregate details: " + aggregate);
				}
			}
		}
		// evict the table
		HibernateUtil.currentSession().evict(table);
		// Some fields need a relational datasource property. That isn't
		// persisted so must be set here.
		for (BaseField field : table.getFields()) {
			if (field instanceof TextFieldDefn) {
				((TextFieldDefn) field).setDataSource(relationalDataSource);
			} else if (field instanceof IntegerFieldDefn) {
				((IntegerFieldDefn) field).setDataSource(relationalDataSource);
			} else if (field instanceof DecimalFieldDefn) {
				((DecimalFieldDefn) field).setDataSource(relationalDataSource);
			} else if (field instanceof RelationFieldDefn) {
				((RelationFieldDefn) field).setDataSource(relationalDataSource);
			}
		}
		// re-activate after call to evict above
		HibernateUtil.activateObject(table);
	}

	public AuthenticatorInfo getAuthenticator() {
		return this.authenticator;
	}

	public CompanyInfo getCompanyForLoggedInUser(HttpServletRequest request)
			throws ObjectNotFoundException {
		AppUserInfo loggedInUser = ((Authenticator) this.authenticator).getUserByUserName(request
				.getRemoteUser());
		return loggedInUser.getCompany();
	}

	public SortedSet<CompanyInfo> getCompanies(HttpServletRequest request)
			throws DisallowedException, ObjectNotFoundException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.MASTER))) {
			throw new DisallowedException(this.getLoggedInUser(request), PrivilegeType.MASTER);
		}
		return ((Authenticator) this.authenticator).getCompanies();
	}

	public synchronized void addCompany(HttpServletRequest request, CompanyInfo company)
			throws DisallowedException, CantDoThatException, CodingErrorException,
			MissingParametersException, ObjectNotFoundException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.MASTER))) {
			throw new DisallowedException(this.getLoggedInUser(request), PrivilegeType.MASTER);
		}
		HibernateUtil.activateObject(this.authenticator);
		((Authenticator) this.authenticator).addCompany(company);
		// now add an initial user to the company so it can be logged in to
		String adminUsername = "admin" + company.getCompanyName().toLowerCase();
		adminUsername = adminUsername.replaceAll("\\W", "");
		String adminPassword = RandomString.generate();
		AppUserInfo adminUser = new AppUser(company, null, adminUsername, "User", "Admin",
				adminPassword, "", "", false);
		String adminRolename = adminUsername;
		AppRoleInfo adminRole = new AppRole(company, null, adminRolename);
		// no mapping from role to authenticator so we have to explicitly save it
		HibernateUtil.currentSession().save(adminRole);
		try {
			this.addUser(request, adminUser);
			this.addRole(request, adminRole);
			this.assignUserToRole(request, adminUser, adminRole);
			((Authenticator) this.authenticator).addRolePrivilege(adminRole, PrivilegeType.ADMINISTRATE);
		} catch (MissingParametersException mpex) {
			throw new CodingErrorException("Error constructing/accessing user object", mpex);
		} catch (ObjectNotFoundException onfex) {
			throw new CodingErrorException("Error assigning user to role: user or role not found", onfex);
		}
	}

	public synchronized void removeCompany(HttpServletRequest request, CompanyInfo company)
			throws DisallowedException, CodingErrorException, CantDoThatException,
			ObjectNotFoundException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.MASTER))) {
			throw new DisallowedException(this.getLoggedInUser(request), PrivilegeType.MASTER);
		}
		Set<TableInfo> companyTables = company.getTables();
		if (companyTables.size() > 0) {
			throw new CantDoThatException(
					"All tables must be removed before removing the company. Remaining tables are "
							+ companyTables);
		}
		logger.info("removing users & roles");
		try {
			for (AppUserInfo user : company.getUsers()) {
				logger.info("removing " + user);
				this.removeUser(request, user);
			}
			for (AppRoleInfo role : company.getRoles()) {
				logger.info("removing " + role);
				this.removeRoleWithoutChecks(request, role);
			}
		} catch (ObjectNotFoundException onfex) {
			throw new CodingErrorException("User or role in company " + company
					+ " not found in authenticator");
		}
		logger.info("removing company " + company);
		HibernateUtil.activateObject(this.authenticator);
		((Authenticator) this.authenticator).removeCompany(company);
		HibernateUtil.currentSession().delete(company);
	}

	public CompanyInfo getCompanyByInternalName(HttpServletRequest request, String internalCompanyName)
			throws DisallowedException, ObjectNotFoundException {
		for (CompanyInfo company : this.getCompanies(request)) {
			if (company.getInternalCompanyName().equals(internalCompanyName)) {
				return company;
			}
		}
		throw new ObjectNotFoundException("Company with ID '" + internalCompanyName + "' not found");
	}

	public synchronized void addUser(HttpServletRequest request, AppUserInfo newUser)
			throws DisallowedException, MissingParametersException, ObjectNotFoundException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE) || this.authenticator
				.loggedInUserAllowedTo(request, PrivilegeType.MASTER))) {
			throw new DisallowedException(this.getLoggedInUser(request), PrivilegeType.ADMINISTRATE);
		}
		// only MASTER user is allowed to add a user to a different company
		if (!this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.MASTER)) {
			CompanyInfo companyLoggedInTo = this.getCompanyForLoggedInUser(request);
			CompanyInfo companyUserBelongsTo = newUser.getCompany();
			if (!companyUserBelongsTo.equals(companyLoggedInTo)) {
				throw new DisallowedException(this.getLoggedInUser(request), PrivilegeType.MASTER);
			}
		}
		// Add user in memory by cascading addUser down to the actual
		// authenticator
		HibernateUtil.activateObject(this.authenticator);
		((Authenticator) this.authenticator).addUser(newUser);
	}

	public synchronized void updateUser(HttpServletRequest request, AppUserInfo appUser,
			String userName, String surname, String forename, String password, String email,
			InitialView userType, boolean usesCustomUI, boolean usesAppLauncher)
			throws DisallowedException, MissingParametersException, CantDoThatException,
			ObjectNotFoundException, CodingErrorException {
		// Allow updating of any user if administrator or yourself if not
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE) || appUser
				.getUserName().equals(request.getRemoteUser()))) {
			throw new DisallowedException(this.getLoggedInUser(request), PrivilegeType.ADMINISTRATE);
		}
		// check which fields are to be updated:
		// 1) treat zero-length strings as a deletion request,
		// 2) nulls indicate that the field value is to remain unchanged
		if (userName == null) {
			userName = appUser.getUserName();
		} else {
			if (userName.trim().equals("")) {
				throw new MissingParametersException("Username cannot be empty");
			}
			if (appUser.getUserName().equals(request.getRemoteUser())
					&& (!userName.equals(request.getRemoteUser()))) {
				throw new CantDoThatException("Unable to edit own account username");
			}
			Set<AppUserInfo> allUsers = ((Authenticator) this.authenticator).getUsers();
			// check that the new username isn't already in use by someone else
			for (AppUserInfo testUser : allUsers) {
				if (testUser.getUserName().equals(userName) && (!testUser.equals(appUser))) {
					throw new CantDoThatException("Username " + userName + " already exists");
				}
			}
		}
		if (password != null) {
			if (password.trim().equals("")) {
				throw new MissingParametersException("Password cannot be empty");
			}
		}
		if (surname == null) {
			surname = appUser.getSurname();
		}
		if (forename == null) {
			forename = appUser.getForename();
		}
		if (email == null) {
			email = appUser.getEmail();
		}
		if (userType == null) {
			userType = appUser.getUserType();
		}
		// Update user in memory:
		HibernateUtil.activateObject(appUser);
		((Authenticator) this.authenticator).updateUser(appUser, userName, surname, forename, password,
				email, userType, usesCustomUI, usesAppLauncher);
	}

	public synchronized void removeUser(HttpServletRequest request, AppUserInfo appUser)
			throws DisallowedException, ObjectNotFoundException, CodingErrorException,
			CantDoThatException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE) || this.authenticator
				.loggedInUserAllowedTo(request, PrivilegeType.MASTER))) {
			throw new DisallowedException(this.getLoggedInUser(request), PrivilegeType.ADMINISTRATE);
		}
		if (appUser.getUserName().equals(request.getRemoteUser())) {
			throw new CantDoThatException("Unable to edit own account");
		}
		logger.info("removing user " + appUser);
		HibernateUtil.activateObject(this.authenticator);
		((Authenticator) this.authenticator).removeUser(appUser);
		HibernateUtil.currentSession().delete(appUser);
	}

	public synchronized AppUserInfo getUserByInternalName(HttpServletRequest request,
			String internalUserName) throws ObjectNotFoundException, DisallowedException {
		AppUserInfo foundUser = null;
		for (AppUserInfo user : ((Authenticator) this.authenticator).getUsers()) {
			if (user.getInternalUserName().equals(internalUserName)) {
				foundUser = user;
				break; // user found so no need to continue iterating
			}
		}
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE))) {
			if (foundUser == null) {
				throw new DisallowedException(this.getLoggedInUser(request), PrivilegeType.ADMINISTRATE);
			}
			if (!foundUser.getUserName().equals(request.getRemoteUser())) {
				throw new DisallowedException(this.getLoggedInUser(request), PrivilegeType.ADMINISTRATE);
			}
		}
		if (foundUser == null) {
			throw new ObjectNotFoundException("User with internal name " + internalUserName
					+ " not found");
		} else {
			return foundUser;
		}
	}

	public synchronized void addRole(HttpServletRequest request, AppRoleInfo role)
			throws DisallowedException, MissingParametersException, ObjectNotFoundException,
			CodingErrorException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE) || this.authenticator
				.loggedInUserAllowedTo(request, PrivilegeType.MASTER))) {
			throw new DisallowedException(this.getLoggedInUser(request), PrivilegeType.ADMINISTRATE);
		}
		// only MASTER user is allowed to add a user to a different company
		if (!this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.MASTER)) {
			CompanyInfo companyLoggedInTo = this.getCompanyForLoggedInUser(request);
			CompanyInfo companyRoleBelongsTo = role.getCompany();
			if (!companyRoleBelongsTo.equals(companyLoggedInTo)) {
				throw new DisallowedException(this.getLoggedInUser(request), PrivilegeType.MASTER);
			}
		}
		HibernateUtil.activateObject(this.authenticator);
		((Authenticator) this.authenticator).addRole(role);
	}

	public synchronized void updateRole(HttpServletRequest request, AppRoleInfo role,
			String newRoleName) throws DisallowedException, MissingParametersException,
			ObjectNotFoundException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE))) {
			throw new DisallowedException(this.getLoggedInUser(request), PrivilegeType.ADMINISTRATE);
		}
		if (role == null) {
			throw new MissingParametersException("oldRole field not specified");
		}
		if (role.getRoleName().equals("")) {
			throw new MissingParametersException("oldRole field blank");
		}
		if (newRoleName == null) {
			throw new MissingParametersException("newRole field not specified");
		}
		if (newRoleName.equals("")) {
			throw new MissingParametersException("newRole field blank");
		}
		HibernateUtil.activateObject(role);
		((Authenticator) this.authenticator).updateRole(role, newRoleName);
	}

	public void removeRole(HttpServletRequest request, AppRoleInfo role) throws DisallowedException,
			ObjectNotFoundException, CodingErrorException, CantDoThatException {
		// agileBase has one role with admin privileges, check we're not removing
		// that one
		Set<RoleGeneralPrivilegeInfo> rolePrivileges = this.getPrivilegesForRole(request, role);
		for (RoleGeneralPrivilegeInfo rolePrivilege : rolePrivileges) {
			if (rolePrivilege.getPrivilegeType().equals(PrivilegeType.ADMINISTRATE)) {
				throw new CantDoThatException("Can't remove role '" + role
						+ "' because it has administrator privileges");
			}
		}
		removeRoleWithoutChecks(request, role);
	}

	private synchronized void removeRoleWithoutChecks(HttpServletRequest request, AppRoleInfo role)
			throws ObjectNotFoundException, DisallowedException, CodingErrorException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE) || this.authenticator
				.loggedInUserAllowedTo(request, PrivilegeType.MASTER))) {
			throw new DisallowedException(this.getLoggedInUser(request), PrivilegeType.ADMINISTRATE);
		}
		logger.info("removing role " + role);
		HibernateUtil.activateObject(this.authenticator);
		((Authenticator) this.authenticator).removeRole(role);
		HibernateUtil.currentSession().delete(role);
	}

	public synchronized AppRoleInfo getRoleByInternalName(String internalRoleName) {
		for (AppRoleInfo role : ((Authenticator) this.authenticator).getRoles()) {
			if (role.getInternalRoleName().equals(internalRoleName)) {
				return role;
			}
		}
		return null;
	}

	public synchronized void addRolePrivilege(HttpServletRequest request, AppRoleInfo role,
			PrivilegeType privilegeType) throws DisallowedException, CantDoThatException,
			ObjectNotFoundException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE))) {
			throw new DisallowedException(this.getLoggedInUser(request), PrivilegeType.ADMINISTRATE);
		}
		HibernateUtil.activateObject(this.authenticator);
		((Authenticator) this.authenticator).addRolePrivilege(role, privilegeType);
	}

	public synchronized void addRolePrivilege(HttpServletRequest request, AppRoleInfo role,
			PrivilegeType privilegeType, TableInfo table) throws IllegalArgumentException,
			DisallowedException, ObjectNotFoundException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE))) {
			throw new DisallowedException(this.getLoggedInUser(request), PrivilegeType.ADMINISTRATE);
		}
		HibernateUtil.activateObject(this.authenticator);
		((Authenticator) this.authenticator).addRolePrivilege(role, privilegeType, table);
	}

	public synchronized void removeRolePrivilege(HttpServletRequest request, AppRoleInfo role,
			PrivilegeType privilegeType) throws DisallowedException, ObjectNotFoundException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE))) {
			throw new DisallowedException(this.getLoggedInUser(request), PrivilegeType.ADMINISTRATE);
		}
		HibernateUtil.activateObject(this.authenticator);
		RoleGeneralPrivilegeInfo removedPrivilege = ((Authenticator) this.authenticator)
				.removeRolePrivilege(role, privilegeType);
		HibernateUtil.currentSession().delete(removedPrivilege);
	}

	public synchronized void removeRolePrivilege(HttpServletRequest request, AppRoleInfo role,
			PrivilegeType privilegeType, TableInfo table) throws IllegalArgumentException,
			DisallowedException, ObjectNotFoundException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE))) {
			throw new DisallowedException(this.getLoggedInUser(request), PrivilegeType.ADMINISTRATE);
		}
		HibernateUtil.activateObject(this.authenticator);
		RoleTablePrivilegeInfo removedPrivilege = ((Authenticator) this.authenticator)
				.removeRolePrivilege(role, privilegeType, table);
		HibernateUtil.currentSession().delete(removedPrivilege);
	}

	public synchronized void addUserPrivilege(HttpServletRequest request, AppUserInfo appUser,
			PrivilegeType privilegeType) throws DisallowedException, CantDoThatException,
			ObjectNotFoundException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE))) {
			throw new DisallowedException(this.getLoggedInUser(request), PrivilegeType.ADMINISTRATE);
		}
		if (this.specifiedUserHasPrivilege(request, PrivilegeType.MASTER, appUser)) {
			throw new CantDoThatException("User '" + appUser
					+ "' has MASTER privileges and as such can't have any other privileges");
		}
		HibernateUtil.activateObject(this.authenticator);
		((Authenticator) this.authenticator).addUserPrivilege(appUser, privilegeType);
	}

	public synchronized void addUserPrivilege(HttpServletRequest request, AppUserInfo appUser,
			PrivilegeType privilegeType, TableInfo table) throws IllegalArgumentException,
			DisallowedException, CantDoThatException, ObjectNotFoundException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE))) {
			throw new DisallowedException(this.getLoggedInUser(request), PrivilegeType.ADMINISTRATE);
		}
		if (this.specifiedUserHasPrivilege(request, PrivilegeType.MASTER, appUser)) {
			throw new CantDoThatException("User '" + appUser
					+ "' has MASTER privileges and as such can't have any other privileges");
		}
		HibernateUtil.activateObject(this.authenticator);
		((Authenticator) this.authenticator).addUserPrivilege(appUser, privilegeType, table);
	}

	public synchronized void removeUserPrivilege(HttpServletRequest request, AppUserInfo appUser,
			PrivilegeType privilegeType) throws DisallowedException, ObjectNotFoundException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE))) {
			throw new DisallowedException(this.getLoggedInUser(request), PrivilegeType.ADMINISTRATE);
		}
		HibernateUtil.activateObject(this.authenticator);
		UserGeneralPrivilegeInfo removedPrivilege = ((Authenticator) this.authenticator)
				.removeUserPrivilege(appUser, privilegeType);
		HibernateUtil.currentSession().delete(removedPrivilege);
	}

	public synchronized void removeUserPrivilege(HttpServletRequest request, AppUserInfo appUser,
			PrivilegeType privilegeType, TableInfo table) throws IllegalArgumentException,
			DisallowedException, ObjectNotFoundException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE))) {
			throw new DisallowedException(this.getLoggedInUser(request), PrivilegeType.ADMINISTRATE);
		}
		HibernateUtil.activateObject(this.authenticator);
		UserTablePrivilegeInfo removedPrivilege = ((Authenticator) this.authenticator)
				.removeUserPrivilege(appUser, privilegeType, table);
		HibernateUtil.currentSession().delete(removedPrivilege);
	}

	public Set<RoleTablePrivilegeInfo> getRolePrivilegesOnTable(HttpServletRequest request,
			TableInfo table) throws DisallowedException, ObjectNotFoundException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE))) {
			throw new DisallowedException(this.getLoggedInUser(request), PrivilegeType.ADMINISTRATE,
					table);
		}
		Set<RoleTablePrivilegeInfo> rolePrivilegesOnTable = new HashSet<RoleTablePrivilegeInfo>();
		Set<RoleGeneralPrivilegeInfo> rolePrivileges = ((Authenticator) this.authenticator)
				.getRolePrivileges();
		for (RoleGeneralPrivilegeInfo rolePrivilege : rolePrivileges) {
			if (rolePrivilege instanceof RoleTablePrivilege) {
				RoleTablePrivilege roleObjectPrivilege = (RoleTablePrivilege) rolePrivilege;
				if (roleObjectPrivilege.getTable().equals(table)) {
					rolePrivilegesOnTable.add(roleObjectPrivilege);
				}
			}
		}
		return rolePrivilegesOnTable;
	}

	public Set<UserTablePrivilegeInfo> getUserPrivilegesOnTable(HttpServletRequest request,
			TableInfo table) throws DisallowedException, ObjectNotFoundException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE))) {
			throw new DisallowedException(this.getLoggedInUser(request), PrivilegeType.ADMINISTRATE,
					table);
		}
		Set<UserTablePrivilegeInfo> userPrivilegesOnTable = new HashSet<UserTablePrivilegeInfo>();
		Set<UserGeneralPrivilegeInfo> userPrivileges = ((Authenticator) this.authenticator)
				.getUserPrivileges();
		for (UserGeneralPrivilegeInfo userPrivilege : userPrivileges) {
			if (userPrivilege instanceof UserTablePrivilege) {
				UserTablePrivilege userObjectPrivilege = (UserTablePrivilege) userPrivilege;
				if (userObjectPrivilege.getTable().equals(table)) {
					userPrivilegesOnTable.add(userObjectPrivilege);
				}
			}
		}
		return userPrivilegesOnTable;
	}

	public void removePrivilegesOnTable(HttpServletRequest request, TableInfo table)
			throws DisallowedException, ObjectNotFoundException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.MANAGE_TABLE, table))) {
			throw new DisallowedException(this.getLoggedInUser(request), PrivilegeType.MANAGE_TABLE,
					table);
		}
		HibernateUtil.activateObject(this.authenticator);
		Set<UserGeneralPrivilegeInfo> allUserPrivileges = ((Authenticator) this.authenticator)
				.getUserPrivileges();
		for (UserGeneralPrivilegeInfo userPrivilege : allUserPrivileges) {
			if (userPrivilege instanceof UserTablePrivilegeInfo) {
				if (((UserTablePrivilegeInfo) userPrivilege).getTable().equals(table)) {
					AppUserInfo user = userPrivilege.getUser();
					PrivilegeType privilegeType = userPrivilege.getPrivilegeType();
					((Authenticator) this.authenticator).removeUserPrivilege(user, privilegeType, table);
					HibernateUtil.currentSession().delete(userPrivilege);
				}
			}
		}
		// The same thing with role privileges
		Set<RoleGeneralPrivilegeInfo> allRolePrivileges = ((Authenticator) this.authenticator)
				.getRolePrivileges();
		for (RoleGeneralPrivilegeInfo rolePrivilege : allRolePrivileges) {
			if (rolePrivilege instanceof RoleTablePrivilegeInfo) {
				if (((RoleTablePrivilegeInfo) rolePrivilege).getTable().equals(table)) {
					AppRoleInfo role = rolePrivilege.getRole();
					PrivilegeType privilegeType = rolePrivilege.getPrivilegeType();
					((Authenticator) this.authenticator).removeRolePrivilege(role, privilegeType, table);
					HibernateUtil.currentSession().delete(rolePrivilege);
				}
			}
		}
	}

	public synchronized void assignUserToRole(HttpServletRequest request, AppUserInfo user,
			AppRoleInfo role) throws DisallowedException, MissingParametersException,
			ObjectNotFoundException, CantDoThatException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE) || this.authenticator
				.loggedInUserAllowedTo(request, PrivilegeType.MASTER))) {
			throw new DisallowedException(this.getLoggedInUser(request), PrivilegeType.ADMINISTRATE);
		}
		if (this.specifiedUserHasPrivilege(request, PrivilegeType.MASTER, user)) {
			throw new CantDoThatException("User '" + user
					+ "' has MASTER privileges and therefore can't be a member of any roles");
		}
		HibernateUtil.activateObject(role);
		((Authenticator) this.authenticator).destroyCache();
		role.assignUser(user);
	}

	public synchronized void removeUserFromRole(HttpServletRequest request, AppUserInfo user,
			AppRoleInfo role) throws DisallowedException, MissingParametersException,
			ObjectNotFoundException, CodingErrorException, CantDoThatException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE))) {
			throw new DisallowedException(this.getLoggedInUser(request), PrivilegeType.ADMINISTRATE);
		}
		boolean roleIsCompanyAdmin = false;
		for (RoleGeneralPrivilegeInfo roleGeneralPrivilege : this.getPrivilegesForRole(request, role)) {
			if (roleGeneralPrivilege.getPrivilegeType().equals(PrivilegeType.ADMINISTRATE)) {
				roleIsCompanyAdmin = true;
			}
		}
		if (request.getRemoteUser().equals(user.getUserName()) && roleIsCompanyAdmin) {
			throw new CantDoThatException("Unable to remove admin role from own account");
		}
		HibernateUtil.activateObject(role);
		((Authenticator) this.authenticator).destroyCache();
		role.removeUser(user);
	}

	public AppUserInfo getUserByUserName(HttpServletRequest request, String userName)
			throws ObjectNotFoundException, DisallowedException {
		if (this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE)
				|| userName.equals(request.getRemoteUser())) {
			return ((Authenticator) this.authenticator).getUserByUserName(userName);
		} else {
			throw new DisallowedException(this.getLoggedInUser(request), PrivilegeType.ADMINISTRATE);
		}
	}

	public SortedSet<AppUserInfo> getUsers(HttpServletRequest request) throws DisallowedException,
			ObjectNotFoundException {
		if (this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE)) {
			CompanyInfo company = this.getCompanyForLoggedInUser(request);
			return company.getUsers();
		} else {
			throw new DisallowedException(this.getLoggedInUser(request), PrivilegeType.ADMINISTRATE);
		}
	}

	public SortedSet<AppUserInfo> getAdministrators(HttpServletRequest request)
			throws ObjectNotFoundException, CodingErrorException {
		CompanyInfo company = this.getCompanyForLoggedInUser(request);
		SortedSet<AppUserInfo> admins = new TreeSet<AppUserInfo>();
		for (AppUserInfo user : company.getUsers()) {
			if (((Authenticator) this.getAuthenticator()).userAllowedTo(PrivilegeType.ADMINISTRATE, user)) {
				admins.add(user);
			}
		}
		return admins;
	}

	public SortedSet<AppRoleInfo> getRoles(HttpServletRequest request) throws DisallowedException,
			ObjectNotFoundException {
		if (this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE)) {
			CompanyInfo company = this.getCompanyForLoggedInUser(request);
			return company.getRoles();
		} else {
			throw new DisallowedException(this.getLoggedInUser(request), PrivilegeType.ADMINISTRATE);
		}
	}

	public SortedSet<AppRoleInfo> getRolesForUser(HttpServletRequest request, AppUserInfo user)
			throws DisallowedException, ObjectNotFoundException {
		if (this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE)) {
			return ((Authenticator) this.authenticator).getRolesForUser(user);
		} else {
			throw new DisallowedException(this.getLoggedInUser(request), PrivilegeType.ADMINISTRATE);
		}
	}

	public Set<UserGeneralPrivilegeInfo> getPrivilegesForUser(HttpServletRequest request,
			AppUserInfo user) throws DisallowedException, ObjectNotFoundException {
		if (this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE)
				|| this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.MASTER)) {
			return ((Authenticator) this.authenticator).getPrivilegesForUser(user);
		} else {
			throw new DisallowedException(this.getLoggedInUser(request), PrivilegeType.ADMINISTRATE);
		}
	}

	public Set<RoleGeneralPrivilegeInfo> getPrivilegesForRole(HttpServletRequest request,
			AppRoleInfo role) throws DisallowedException, ObjectNotFoundException {
		if (this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE)) {
			return ((Authenticator) this.authenticator).getPrivilegesForRole(role);
		} else {
			throw new DisallowedException(this.getLoggedInUser(request), PrivilegeType.ADMINISTRATE);
		}
	}

	public boolean specifiedUserHasPrivilege(HttpServletRequest request, PrivilegeType privilegeType,
			AppUserInfo user) throws DisallowedException, ObjectNotFoundException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE) || this.authenticator
				.loggedInUserAllowedTo(request, PrivilegeType.MASTER))) {
			throw new DisallowedException(this.getLoggedInUser(request), PrivilegeType.ADMINISTRATE);
		}
		Set<UserGeneralPrivilegeInfo> userPrivileges = this.getPrivilegesForUser(request, user);
		for (UserGeneralPrivilegeInfo privilege : userPrivileges) {
			// We aren't interested in object specific privileges here, that's
			// the other
			// specifiedUserHasPrivilege method
			if (!(privilege instanceof UserTablePrivilegeInfo)) {
				if (privilege.getPrivilegeType().equals(privilegeType)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean specifiedUserHasPrivilege(HttpServletRequest request, PrivilegeType privilegeType,
			AppUserInfo user, TableInfo table) throws DisallowedException, ObjectNotFoundException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE))) {
			throw new DisallowedException(this.getLoggedInUser(request), PrivilegeType.ADMINISTRATE);
		}
		Set<UserGeneralPrivilegeInfo> userPrivileges = this.getPrivilegesForUser(request, user);
		for (UserGeneralPrivilegeInfo privilege : userPrivileges) {
			// We're only interested in table-specific privileges here, general
			// privileges are handled by the
			// other sessionUserHasPrivileges method
			if (privilege instanceof UserTablePrivilegeInfo) {
				if (privilege.getPrivilegeType().equals(privilegeType)
						&& ((UserTablePrivilegeInfo) privilege).getTable().getInternalTableName()
								.equals(table.getInternalTableName())) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean specifiedRoleHasPrivilege(HttpServletRequest request, PrivilegeType privilegeType,
			AppRoleInfo role, TableInfo table) throws DisallowedException, ObjectNotFoundException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE))) {
			throw new DisallowedException(this.getLoggedInUser(request), PrivilegeType.ADMINISTRATE);
		}
		Set<RoleGeneralPrivilegeInfo> rolePrivileges = this.getPrivilegesForRole(request, role);
		for (RoleGeneralPrivilegeInfo privilege : rolePrivileges) {
			if (privilege instanceof RoleTablePrivilegeInfo) {
				if (privilege.getPrivilegeType().equals(privilegeType)
						&& ((RoleTablePrivilegeInfo) privilege).getTable().getInternalTableName()
								.equals(table.getInternalTableName())) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean specifiedRoleHasPrivilege(HttpServletRequest request, PrivilegeType privilegeType,
			AppRoleInfo role) throws DisallowedException, ObjectNotFoundException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE))) {
			throw new DisallowedException(this.getLoggedInUser(request), PrivilegeType.ADMINISTRATE);
		}
		Set<RoleGeneralPrivilegeInfo> rolePrivileges = this.getPrivilegesForRole(request, role);
		for (RoleGeneralPrivilegeInfo privilege : rolePrivileges) {
			if (!(privilege instanceof RoleTablePrivilegeInfo)) {
				if (privilege.getPrivilegeType().equals(privilegeType)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean specifiedUserAllowedToViewReport(HttpServletRequest request, AppUserInfo user,
			BaseReportInfo report) throws DisallowedException, CodingErrorException,
			ObjectNotFoundException {
		if (!(this.authenticator.loggedInUserAllowedTo(request, PrivilegeType.ADMINISTRATE))) {
			throw new DisallowedException(this.getLoggedInUser(request), PrivilegeType.ADMINISTRATE);
		}
		return ((Authenticator) this.authenticator).specifiedUserAllowedToViewReport(user, report,
				new HashSet<BaseReportInfo>());
	}

	public AppUserInfo getLoggedInUser(HttpServletRequest request) throws DisallowedException,
			ObjectNotFoundException {
		return this.getUserByUserName(request, request.getRemoteUser());
	}

	public void sendPasswordReset(HttpServletRequest request) throws CantDoThatException,
			ObjectNotFoundException, CodingErrorException, MessagingException, DisallowedException,
			MissingParametersException {
		AppUserInfo user = null;
		String internalUserName = request.getParameter("internalusername");
		if (internalUserName != null) {
			user = this.getUserByInternalName(request, internalUserName);
		} else {
			String userName = request.getParameter("username");
			if (userName != null) {
				try {
					user = ((Authenticator) this.getAuthenticator()).getUserByUserName(userName);
				} catch (ObjectNotFoundException onfex) {
					if (userName.contains("@")) {
						user = ((Authenticator) this.getAuthenticator()).getUserByEmail(userName);
					} else {
						throw onfex;
					}
				}
			}
		}
		if (user == null) {
			throw new MissingParametersException(
					"internalusername or username must be supplied to identify user");
		}
		HibernateUtil.activateObject(user);
		// If this was called by the 'forgotten password' link, the URL may be
		// Public ab, replace this
		String appUrl = Helpers.getAppUrl(request).replace("Public.ab", "AppController.servlet");
		user.sendPasswordReset(appUrl);
	}

	private AuthenticatorInfo authenticator = null;

	private static final SimpleLogger logger = new SimpleLogger(AuthManager.class);

}
