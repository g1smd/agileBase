/*
 *  Copyright 2010 GT webMarque Ltd
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

import org.grlea.log.SimpleLogger;
import com.gtwm.pb.model.interfaces.AuthenticatorInfo;
import com.gtwm.pb.model.interfaces.AppRoleInfo;
import com.gtwm.pb.model.interfaces.AppUserInfo;
import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.RoleGeneralPrivilegeInfo;
import com.gtwm.pb.model.interfaces.RoleObjectPrivilegeInfo;
import com.gtwm.pb.model.interfaces.SimpleReportInfo;
import com.gtwm.pb.model.interfaces.UserGeneralPrivilegeInfo;
import com.gtwm.pb.model.interfaces.UserObjectPrivilegeInfo;
import com.gtwm.pb.model.interfaces.AuthCacheObjectInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.CompanyInfo;
import com.gtwm.pb.auth.AuthCacheObject;
import com.gtwm.pb.util.MissingParametersException;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.Enumerations.UserType;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import javax.servlet.http.*;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.persistence.GeneratedValue;
import javax.persistence.CascadeType;

@Entity
public final class Authenticator implements AuthenticatorInfo {

	public Authenticator() {
	}

	@Id
	@GeneratedValue
	/**
	 * Hibernate needs an ID for a persistent class - this isn't actually used
	 * by the app otherwise
	 */
	protected long getId() {
		return this.id;
	}

	private void setId(long id) {
		this.id = id;
	}

	protected synchronized void addCompany(CompanyInfo company) {
		this.getCompaniesCollection().add(company);
	}

	protected synchronized void removeCompany(CompanyInfo company) {
		this.getCompaniesCollection().remove(company);
	}

	protected synchronized void updateCompany(CompanyInfo company, String companyName) {
		this.getCompaniesCollection().remove(company);
		company.setCompanyName(companyName);
		this.getCompaniesCollection().add(company);
	}

	@Transient
	protected SortedSet<CompanyInfo> getCompanies() {
		return Collections
				.unmodifiableSortedSet(new TreeSet<CompanyInfo>(this.getCompaniesCollection()));
	}

	/**
	 * For use only by Hibernate
	 */
	@OneToMany(targetEntity = Company.class, cascade = CascadeType.ALL)
	private synchronized Set<CompanyInfo> getCompaniesCollection() {
		return this.companies;
	}

	/**
	 * Used only by Hibernate
	 */
	private synchronized void setCompaniesCollection(Set<CompanyInfo> companiesCollection) {
		this.companies = companiesCollection;
	}

	protected synchronized void addUser(AppUserInfo newUser) {
		logger.info("Adding new user " + newUser + " with internal name "
				+ newUser.getInternalUserName() + " to " + this.getUsersDirect());
		this.getUsersDirect().add(newUser);
		newUser.getCompany().addUser(newUser);
	}

	protected synchronized void updateUser(AppUserInfo appUser, String userName, String surname,
			String forename, String password, UserType userType) throws MissingParametersException {
		// need to remove and add user to all sorted collections it's in because
		// we may be changing a property (userName) that compareTo depends on
		this.getUsersDirect().remove(appUser);
		((Company) appUser.getCompany()).getUsersCollection().remove(appUser);
		appUser.setUserName(userName);
		appUser.setPassword(password);
		appUser.setSurname(surname);
		appUser.setForename(forename);
		appUser.setUserType(userType);
		((Company) appUser.getCompany()).getUsersCollection().add(appUser);
		this.getUsersDirect().add(appUser);
	}

	protected synchronized void updateRole(AppRoleInfo appRole, String roleName) {
		// need to remove and add role to all sorted collections it's in because
		// we are
		// changing a property (roleName) that compareTo depends on
		((Company) appRole.getCompany()).getRolesCollection().remove(appRole);
		appRole.setRoleName(roleName);
		((Company) appRole.getCompany()).getRolesCollection().add(appRole);
	}

	protected synchronized void removeUser(AppUserInfo appUser) throws ObjectNotFoundException,
			CodingErrorException {
		if (this.getUsersDirect().contains(appUser)) {
			// remove user privileges
			Set<UserGeneralPrivilegeInfo> userPrivileges = this.getUserPrivileges();
			for (UserGeneralPrivilegeInfo userPrivilege : userPrivileges) {
				if (userPrivilege instanceof UserObjectPrivilege) {
					this.removeUserPrivilege(appUser, userPrivilege.getPrivilegeType(), ((UserObjectPrivilege) userPrivilege).getTable());
				} else {
					this.removeUserPrivilege(appUser, userPrivilege.getPrivilegeType());
				}
			}
			// remove user from any roles they're in
			for (AppRoleInfo role : this.getRolesForUser(appUser)) {
				role.removeUser(appUser);
			}
			appUser.getCompany().removeUser(appUser);
			this.getUsersDirect().remove(appUser);
		} else {
			throw new ObjectNotFoundException("Can't remove user " + appUser.getUserName()
					+ " because they're not in the authenticator");
		}
	}

	protected synchronized void addRole(AppRoleInfo role) throws CodingErrorException {
		// Create a new role with no users
		this.getRolesDirect().add(role);
		role.getCompany().addRole(role);
	}

	protected synchronized void removeRole(AppRoleInfo role) throws ObjectNotFoundException,
			CodingErrorException {
		// remove role privileges
		Set<RoleGeneralPrivilegeInfo> rolePrivileges = this.getRolePrivileges();
		for (RoleGeneralPrivilegeInfo rolePrivilege : rolePrivileges) {
			if (rolePrivilege instanceof RoleObjectPrivilege) {
				this.removeRolePrivilege(role, rolePrivilege.getPrivilegeType(), ((RoleObjectPrivilege) rolePrivilege).getTable());
			} else {
				this.removeRolePrivilege(role, rolePrivilege.getPrivilegeType());
			}
		}
		// remove role itself
		role.getCompany().removeRole(role);
		this.getRolesDirect().remove(role);
		this.destroyCache();
	}

	/**
	 * Add a general application functionality privilege for a role.
	 * 
	 * @throws CantDoThatException
	 *             If privilege type is MASTER: The MASTER privilege can only be
	 *             assigned to users, not roles
	 */
	protected void addRolePrivilege(AppRoleInfo role, PrivilegeType privilegeType)
			throws CantDoThatException {
		if (privilegeType.equals(PrivilegeType.MASTER)) {
			throw new CantDoThatException("Cant add the CONTROL privilege because role '" + role
					+ "' already has privileges " + this.getPrivilegesForRole(role));
		}
		RoleGeneralPrivilegeInfo privilege = new RoleGeneralPrivilege(role, privilegeType);
		this.getRolePrivilegesDirect().add(privilege);
	}

	/**
	 * Remove a general application functionality privilege from a role
	 * 
	 * @return the removed privilege
	 */
	protected RoleGeneralPrivilegeInfo removeRolePrivilege(AppRoleInfo role,
			PrivilegeType privilegeType) {
		RoleGeneralPrivilegeInfo privilege = new RoleGeneralPrivilege(role, privilegeType);
		this.getRolePrivilegesDirect().remove(privilege);
		return privilege;
	}

	/**
	 * Add a table privilege for a role
	 * 
	 * @throws IllegalArgumentException
	 *             if the privilege requested isn't compatible with a table
	 *             object
	 */
	protected synchronized void addRolePrivilege(AppRoleInfo role, PrivilegeType privilegeType,
			TableInfo table) throws IllegalArgumentException {
		RoleObjectPrivilegeInfo rolePrivilege = new RoleObjectPrivilege(role, privilegeType, table);
		this.getRolePrivilegesDirect().add(rolePrivilege);
		this.destroyCache();
	}

	/**
	 * Remove a table privilege for a role
	 * 
	 * @return the removed privilege
	 * 
	 * @throws IllegalArgumentException
	 *             if the privilege requested isn't compatible with a table
	 *             object
	 */
	protected synchronized RoleObjectPrivilegeInfo removeRolePrivilege(AppRoleInfo role,
			PrivilegeType privilegeType, TableInfo table) throws IllegalArgumentException {
		RoleObjectPrivilegeInfo rolePrivilege = new RoleObjectPrivilege(role, privilegeType, table);
		this.getRolePrivilegesDirect().remove(rolePrivilege);
		this.destroyCache();
		return rolePrivilege;
	}

	/**
	 * Adds a general application privilege for a specific user
	 * 
	 * @throws CantDoThatException
	 *             If privilege being assigned is MASTER but the user already
	 *             has other privileges.
	 */
	protected synchronized void addUserPrivilege(AppUserInfo appUser, PrivilegeType privilegeType)
			throws CantDoThatException {
		if (privilegeType.equals(PrivilegeType.MASTER)) {
			SortedSet<AppRoleInfo> rolesForUser = this.getRolesForUser(appUser);
			if (rolesForUser.size() > 0) {
				throw new CantDoThatException(
						"If adding a MASTER privilege, the user can't be a member of any roles. User '"
								+ appUser + "' is a member of roles: " + rolesForUser);
			}
			Set<UserGeneralPrivilegeInfo> userPrivileges = this.getPrivilegesForUser(appUser);
			if (userPrivileges.size() > 0) {
				throw new CantDoThatException(
						"If adding a MASTER privilege, the user musn't have any other privileges. User '"
								+ appUser + "' already has privileges: " + userPrivileges);
			}
		}
		UserGeneralPrivilegeInfo userPrivilege = new UserGeneralPrivilege(appUser, privilegeType);
		this.getUserPrivilegesDirect().add(userPrivilege);
	}

	/**
	 * Adds a table privilege for a specific user
	 * 
	 * @throws IllegalArgumentException
	 *             if the privilege requested isn't compatible with a table
	 *             object
	 */
	protected synchronized void addUserPrivilege(AppUserInfo appUser, PrivilegeType privilegeType,
			TableInfo table) throws IllegalArgumentException {
		UserObjectPrivilegeInfo userPrivilege = new UserObjectPrivilege(appUser, privilegeType,
				table);
		this.getUserPrivilegesDirect().add(userPrivilege);
		this.destroyCache();
	}

	/**
	 * Removes a general application privilege for a specific user
	 * 
	 * @return The removed privilege
	 */
	protected UserGeneralPrivilegeInfo removeUserPrivilege(AppUserInfo appUser,
			PrivilegeType privilegeType) {
		UserGeneralPrivilegeInfo userPrivilege = new UserGeneralPrivilege(appUser, privilegeType);
		this.getUserPrivilegesDirect().remove(userPrivilege);
		return userPrivilege;
	}

	/**
	 * Removes a table privilege for a specific user
	 * 
	 * @return the removed privilege
	 * 
	 * @throws IllegalArgumentException
	 *             if the privilege requested isn't compatible with a table
	 *             object
	 */
	protected synchronized UserObjectPrivilegeInfo removeUserPrivilege(AppUserInfo appUser,
			PrivilegeType privilegeType, TableInfo table) throws IllegalArgumentException {
		UserObjectPrivilegeInfo userPrivilege = new UserObjectPrivilege(appUser, privilegeType,
				table);
		this.getUserPrivilegesDirect().remove(userPrivilege);
		this.destroyCache();
		return userPrivilege;
	}

	public boolean loggedInUserAllowedTo(HttpServletRequest request,
			PrivilegeType privilegeType) {
		AppUserInfo appUser = null;
		try {
			appUser = getUserByUserName(request.getRemoteUser());
		} catch (ObjectNotFoundException onfe) {
			logger
					.error("Authentication check can't complete: AppUserInfo object not found for logged in user '"
							+ request.getRemoteUser() + "'");
			// Don't rethrow, can't see any cases where this error would
			// actually occur
			return false;
		}
		// Check whether a role the user is in has the required privilege
		for (AppRoleInfo role : this.getRolesDirect()) {
			if (role.getUsers().contains(appUser)) {
				for (RoleGeneralPrivilegeInfo privilege : this.getRolePrivilegesDirect()) {
					// The role must have the privilege type in question
					if ((privilege.getPrivilegeType().equals(privilegeType))
							&& (privilege.getRole().equals(role))) {
						// Check that privilege is a general one rather than
						// object specific.
						// User can't be allowed to do an object specific thing
						// because we have no object to
						// check, for that see alternative form of
						// userAllowedTo()
						if (!(privilege.getPrivilegeType().isObjectSpecificPrivilege())) {
							return true;
						}
					}
				}
			}
		}
		// User not in a role with the required privilege, but check if they
		// have individual privileges
		for (UserGeneralPrivilegeInfo privilege : this.getUserPrivilegesDirect()) {
			if ((privilege.getPrivilegeType().equals(privilegeType))
					&& (privilege.getUser().equals(appUser))) {
				// See above in role check loop
				if (!(privilege.getPrivilegeType().isObjectSpecificPrivilege())) {
					return true;
				}
			}
		}
		// Neither user nor any role they're in has the required privilege
		return false;
	}

	public synchronized boolean loggedInUserAllowedTo(HttpServletRequest request,
			PrivilegeType privilegeType, TableInfo table) {
		// Check if the user has individual privileges
		AppUserInfo appUser = null;
		try {
			appUser = this.getUserByUserName(request.getRemoteUser());
		} catch (ObjectNotFoundException onfe) {
			logger
					.error("Authentication check can't complete: AppUserInfo object not found for logged in user '"
							+ request.getRemoteUser() + "'");
			// If there's no user then they can't see anything
			return false;
		}
		// We now know the cache works quite well, don't bother testing it
		//if (this.cacheHits > 100000) {
		//	logger.info("Authentication cache hits = " + this.cacheHits + ", misses = " + this.cacheMisses);
		//	this.cacheHits = 0;
		//	this.cacheMisses = 0;
		//}
		// Check cache
		Set<AuthCacheObjectInfo> cachedResults = this.authCache.get(appUser);
		if (cachedResults == null) {
			cachedResults = new HashSet<AuthCacheObjectInfo>();
			this.authCache.put(appUser, cachedResults);
		} else {
			for (AuthCacheObjectInfo authCacheObject : cachedResults) {
				if (authCacheObject.getTable().equals(table) && authCacheObject.getPrivilegeType().equals(privilegeType)) {
					//this.cacheHits++;
					return authCacheObject.userAllowedTo();
				}
			}
		}
		// not in cache
		//this.cacheMisses++;
		for (UserGeneralPrivilegeInfo privilege : this.getUserPrivilegesDirect()) {
			if (privilege instanceof UserObjectPrivilegeInfo) {
				// Use object privilege form
				UserObjectPrivilegeInfo objectPrivilege = (UserObjectPrivilegeInfo) privilege;
				// User must have the privilege type in question for the table
				// in question
				if ((objectPrivilege.getPrivilegeType().equals(privilegeType))
						&& (objectPrivilege.getUser().equals(appUser))
						&& (objectPrivilege.getTable().equals(table))) {
					AuthCacheObjectInfo authCacheObject = new AuthCacheObject(table, privilegeType, true);
					cachedResults.add(authCacheObject);
					this.authCache.put(appUser, cachedResults);
					return true;
				}
			}
		}
		// Check if user's in a role that has the privilege
		for (AppRoleInfo role : this.getRolesDirect()) {
			if (role.getUsers().contains(appUser)) {
				for (RoleGeneralPrivilegeInfo privilege : this.getRolePrivilegesDirect()) {
					if (privilege instanceof RoleObjectPrivilegeInfo) {
						// Use object privilege form
						RoleObjectPrivilegeInfo objectPrivilege = (RoleObjectPrivilegeInfo) privilege;
						// Role must have the privilege type in question for the
						// table in question
						if ((objectPrivilege.getPrivilegeType().equals(privilegeType))
								&& (objectPrivilege.getRole().equals(role))
								&& (objectPrivilege.getTable().equals(table))) {
							AuthCacheObjectInfo authCacheObject = new AuthCacheObject(table, privilegeType, true);
							cachedResults.add(authCacheObject);
							this.authCache.put(appUser, cachedResults);
							return true;
						}
					}
				}
			}
		}
		// Neither user nor any role they're in has the required privilege
		AuthCacheObjectInfo authCacheObject = new AuthCacheObject(table, privilegeType, false);
		cachedResults.add(authCacheObject);
		this.authCache.put(appUser, cachedResults);
		return false;
	}

	public boolean loggedInUserAllowedToViewReport(HttpServletRequest request, BaseReportInfo report)
			throws CodingErrorException {
		return this.loggedInUserAllowedToViewReport(request, report, new HashSet<BaseReportInfo>());
	}

	/**
	 * Recursive method called by isReportViewable(report)
	 */
	private boolean loggedInUserAllowedToViewReport(HttpServletRequest request,
			BaseReportInfo report, Set<BaseReportInfo> checkedReports) throws CodingErrorException {
		// first check parent table of the report
		if (!this.loggedInUserAllowedTo(request, PrivilegeType.VIEW_TABLE_DATA, report
				.getParentTable())) {
			return false;
		}
		// check any joined tables
		SimpleReportInfo simpleReport = (SimpleReportInfo) report;
		Set<TableInfo> joinedTables = simpleReport.getJoinedTables();
		for (TableInfo joinedTable : joinedTables) {
			if (!this.loggedInUserAllowedTo(request, PrivilegeType.VIEW_TABLE_DATA, joinedTable)) {
				return false;
			}
		}
		// recursively check any joined reports
		checkedReports.add(report);
		Set<BaseReportInfo> joinedReports = simpleReport.getJoinedReports();
		for (BaseReportInfo joinedReport : joinedReports) {
			if (!checkedReports.contains(joinedReport)) {
				if (!this.loggedInUserAllowedToViewReport(request, joinedReport, checkedReports)) {
					return false;
				} else {
					checkedReports.add(joinedReport);
				}
			}
		}
		return true;
	}

	@Transient
	protected synchronized AppUserInfo getUserByUserName(String userName)
			throws ObjectNotFoundException {
		AppUserInfo cachedUser = this.usersCache.get(userName);
		if (cachedUser != null) {
			return cachedUser;
		}
		for (AppUserInfo user : this.getUsersDirect()) {
			if (user.getUserName().equals(userName)) {
				this.usersCache.put(userName, user);
				return user;
			}
		}
		throw new ObjectNotFoundException("Username '" + userName + "' not found");
	}

	@Transient
	protected AppRoleInfo getRoleByRoleName(String roleName)
			throws ObjectNotFoundException {
		for (AppRoleInfo role : this.getRolesDirect()) {
			if (role.getRoleName().equals(roleName)) {
				return role;
			}
		}
		throw new ObjectNotFoundException("Role '" + roleName + "' not found");
	}

	@Transient
	protected SortedSet<AppUserInfo> getUsers() {
		return Collections.unmodifiableSortedSet(new TreeSet<AppUserInfo>(this.getUsersDirect()));
	}

	@OneToMany(targetEntity = AppUser.class, cascade = CascadeType.ALL)
	private synchronized Set<AppUserInfo> getUsersDirect() {
		return this.users;
	}

	/**
	 * For hibernate only
	 */
	private synchronized void setUsersDirect(Set<AppUserInfo> usersCollection) {
		this.users = usersCollection;
	}

	@Transient
	protected SortedSet<AppRoleInfo> getRoles() {
		return Collections.unmodifiableSortedSet(new TreeSet<AppRoleInfo>(this.getRolesDirect()));
	}

	@OneToMany(targetEntity = AppRole.class, cascade = CascadeType.ALL)
	@org.hibernate.annotations.Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	private synchronized Set<AppRoleInfo> getRolesDirect() {
		return this.roles;
	}

	/**
	 * For hibernate only
	 */
	private synchronized void setRolesDirect(Set<AppRoleInfo> rolesCollection) {
		this.roles = rolesCollection;
	}

	@Transient
	protected SortedSet<AppRoleInfo> getRolesForUser(AppUserInfo user) {
		SortedSet<AppRoleInfo> rolesForUser = new TreeSet<AppRoleInfo>();
		for (AppRoleInfo role : this.getRolesDirect()) {
			SortedSet<AppUserInfo> usersForRole = role.getUsers();
			if (usersForRole.contains(user)) {
				rolesForUser.add(role);
			}
		}
		return Collections.unmodifiableSortedSet(rolesForUser);
	}

	/**
	 * @return Only the privileges which the specified user has (not counting
	 *         privileges they have as a result of being a member of roles)
	 */
	@Transient
	protected Set<UserGeneralPrivilegeInfo> getPrivilegesForUser(AppUserInfo user) {
		Set<UserGeneralPrivilegeInfo> privilegesForUser = new LinkedHashSet<UserGeneralPrivilegeInfo>();
		for (UserGeneralPrivilegeInfo privilege : this.getUserPrivilegesDirect()) {
			if (privilege.getUser().equals(user)) {
				privilegesForUser.add(privilege);
			}
		}
		return Collections.unmodifiableSet(privilegesForUser);
	}

	@Transient
	protected Set<RoleGeneralPrivilegeInfo> getPrivilegesForRole(AppRoleInfo role) {
		Set<RoleGeneralPrivilegeInfo> privilegesForRole = new LinkedHashSet<RoleGeneralPrivilegeInfo>();
		for (RoleGeneralPrivilegeInfo privilege : this.getRolePrivilegesDirect()) {
			if (privilege.getRole().equals(role)) {
				privilegesForRole.add(privilege);
			}
		}
		return Collections.unmodifiableSet(privilegesForRole);
	}

	/**
	 * Return all role privileges
	 */
	@Transient
	protected Set<RoleGeneralPrivilegeInfo> getRolePrivileges() {
		return Collections.unmodifiableSet(new LinkedHashSet<RoleGeneralPrivilegeInfo>(
				getRolePrivilegesDirect()));
	}

	@OneToMany(targetEntity = RoleGeneralPrivilege.class, cascade = CascadeType.ALL)
	@org.hibernate.annotations.Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	// Uni-directional OneToMany
	private synchronized Set<RoleGeneralPrivilegeInfo> getRolePrivilegesDirect() {
		return this.rolePrivileges;
	}

	private synchronized void setRolePrivilegesDirect(Set<RoleGeneralPrivilegeInfo> rolePrivileges) {
		this.rolePrivileges = rolePrivileges;
	}

	/**
	 * Return all user privileges
	 */
	@Transient
	protected Set<UserGeneralPrivilegeInfo> getUserPrivileges() {
		return Collections.unmodifiableSet(new LinkedHashSet<UserGeneralPrivilegeInfo>(
				getUserPrivilegesDirect()));
	}

	@OneToMany(targetEntity = UserGeneralPrivilege.class, cascade = CascadeType.ALL)
	@org.hibernate.annotations.Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	// Uni-directional OneToMany
	private synchronized Set<UserGeneralPrivilegeInfo> getUserPrivilegesDirect() {
		return this.userPrivileges;
	}

	private synchronized void setUserPrivilegesDirect(Set<UserGeneralPrivilegeInfo> userPrivileges) {
		this.userPrivileges = userPrivileges;
	}

	/**
	 * Used to removed cached checks when authentication info changes, e.g. a privilege removed
	 * 
	 * Note: not synchronized because all calling methods are
	 */
	protected void destroyCache() {
		this.authCache = new HashMap<AppUserInfo, Set<AuthCacheObjectInfo>>();
		this.usersCache = new HashMap<String, AppUserInfo>();
	}
	
	public String toString() {
		return "Low level authentication handling object";
	}

	private Set<CompanyInfo> companies = new HashSet<CompanyInfo>();

	private Set<AppUserInfo> users = new HashSet<AppUserInfo>();

	private Set<AppRoleInfo> roles = new HashSet<AppRoleInfo>();

	/**
	 * A collection of privileges, each consisting of a role and privilege type
	 * (and possibly an object identifier on which the privilege acts). We use a
	 * list to maintain ordering so that if two privileges conflict, privilege
	 * checking always returns the same result (the first match wins)
	 * 
	 * Note: Don't think hibernate maintains ordering between restarts but at
	 * least will be consistent within one running of the application
	 */
	private Set<RoleGeneralPrivilegeInfo> rolePrivileges = new LinkedHashSet<RoleGeneralPrivilegeInfo>();

	private Set<UserGeneralPrivilegeInfo> userPrivileges = new LinkedHashSet<UserGeneralPrivilegeInfo>();

	/**
	 * Use the UserGeneralPrivilegeInfo objects to cache the results of
	 * loggedInUserAllowedTo() calls. Not their intended use but works well
	 */
	private Map<AppUserInfo, Set<AuthCacheObjectInfo>> authCache = new HashMap<AppUserInfo, Set<AuthCacheObjectInfo>>();

	/**
	 * Lookup of username to user object
	 */
	private Map<String, AppUserInfo> usersCache = new HashMap<String, AppUserInfo>();
	
	private long id;
	
	private static final SimpleLogger logger = new SimpleLogger(Authenticator.class);
}
