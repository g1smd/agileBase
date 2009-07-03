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

import java.util.Set;
import java.util.SortedSet;
import java.util.EnumSet;
import javax.servlet.http.HttpServletRequest;
import com.gtwm.pb.auth.PrivilegeType;
import com.gtwm.pb.auth.DisallowedException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.MissingParametersException;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.CantDoThatException;

/**
 * Manage the application's authenticator object - add users, roles etc. and
 * handle persistence. The authenticator itself can be used to check privileges
 * 
 * Note - a HttpServletRequest parameter is passed to most of these methods -
 * this is to allow privilege checking of the current user to take place before
 * carrying out requested actions
 */
public interface AuthManagerInfo {
	/**
	 * @return The application's authenticator object, used to check privileges
	 *         of the logged in user
	 */
	public AuthenticatorInfo getAuthenticator();

	/**
	 * Return all user privileges on a particular table
	 */
	public Set<UserObjectPrivilegeInfo> getUserPrivilegesOnTable(HttpServletRequest request, TableInfo table) throws DisallowedException;
	
	/**
	 * Return all user privileges on a particular table
	 */
	public Set<RoleObjectPrivilegeInfo> getRolePrivilegesOnTable(HttpServletRequest request, TableInfo table) throws DisallowedException;
		
	/**
	 * Add a new company
	 * 
	 * @throws DisallowedException
	 *             If the logged in user doesn't have MASTER privileges
	 */
	public void addCompany(HttpServletRequest request, CompanyInfo company)
			throws DisallowedException, CantDoThatException, CodingErrorException,
			MissingParametersException;

	public void removeCompany(HttpServletRequest request, CompanyInfo company)
			throws DisallowedException, CodingErrorException, CantDoThatException,
			ObjectNotFoundException;

	public CompanyInfo getCompanyByInternalName(HttpServletRequest request,
			String internalCompanyName) throws DisallowedException, ObjectNotFoundException;

	/**
	 * @throws ObjectNotFoundException
	 *             If no user object can be found for the username known by the
	 *             request - could possibly happen if the session gets out of
	 *             sync with objects, e.g. the logged in user is deleted by
	 *             someone else
	 */
	public CompanyInfo getCompanyForLoggedInUser(HttpServletRequest request)
			throws ObjectNotFoundException;

	public SortedSet<String> getCompanyTableNames(HttpServletRequest request)
			throws ObjectNotFoundException;

	/**
	 * Get the set of tables in the company of the logged in user
	 */
	public Set<TableInfo> getCompanyTables(HttpServletRequest request)
			throws ObjectNotFoundException, DisallowedException;

	/**
	 * Get the set of tables in the given company
	 */
	public Set<TableInfo> getCompanyTables(HttpServletRequest request, CompanyInfo company)
			throws ObjectNotFoundException, DisallowedException;

	/**
	 * Return true if the table is in the company. Note that no privilege checks
	 * need to be done as the table object itself is passed in. If the user
	 * didn't have VIEW_TABLE privileges on the table, they wouldn't have been
	 * able to get hold of the object
	 */
	public boolean tableBelongsToCompany(CompanyInfo company, TableInfo table) throws ObjectNotFoundException;

	/**
	 * @throws DisallowedException
	 *             If the logged in user doesn't have MASTER privileges
	 */
	public SortedSet<CompanyInfo> getCompanies(HttpServletRequest request)
			throws DisallowedException;

	/**
	 * @throws SQLException
	 * @throws MissingParametersException
	 *             If userName or password is null or blank
	 * @throws DisallowedException
	 *             If the current user doesn't have ADMINISTRATE privileges, or
	 *             if trying to add a user to another company and the MASTER
	 *             privilege isn't present
	 */
	public void addUser(HttpServletRequest request, AppUserInfo newUser)
			throws DisallowedException, MissingParametersException, ObjectNotFoundException,
			CodingErrorException;

	/**
	 * @throws DisallowedException
	 *             If the current user doesn't have ADMINISTRATE privileges
	 * @throws SQLException
	 * @throws MissingParametersException
	 *             If userName or password is whitespace or zero-length string
	 */
	public void updateUser(HttpServletRequest request, AppUserInfo appUser, String userName,
			String surname, String forename, String password) throws DisallowedException,
			MissingParametersException, CantDoThatException;

	/**
	 * @param appUser
	 *            The user to remove
	 * @throws DisallowedException
	 *             If logged in user doesn't have ADMINISTRATE or MASTER
	 *             privileges
	 * @throws SQLException
	 * @throws ObjectNotFoundException
	 *             If the user isn't found in the application's collection of
	 *             users (stored in com.gtwm.pb.auth.Authenticator)
	 */
	public void removeUser(HttpServletRequest request, AppUserInfo appUser)
			throws DisallowedException, ObjectNotFoundException, CodingErrorException,
			CantDoThatException;

	public AppUserInfo getUserByInternalName(HttpServletRequest request, String internalUserName)
			throws ObjectNotFoundException, DisallowedException;

	/**
	 * @throws DisallowedException
	 *             If user doesn't have ADMINISTRATE privileges, or if a role's
	 *             being added to a different company and the user doesn't have
	 *             MASTER privileges
	 * @throws SQLException
	 * @throws MissingParametersException
	 *             If roleName is null or an empty string
	 */
	public void addRole(HttpServletRequest request, AppRoleInfo role) throws DisallowedException,
			MissingParametersException, ObjectNotFoundException, CodingErrorException;

	public void updateRole(HttpServletRequest request, AppRoleInfo role, String newRoleName)
			throws DisallowedException, MissingParametersException, ObjectNotFoundException;

	/**
	 * @throws DisallowedException
	 *             If logged in user doesn't have ADMINISTRATE or MASTER
	 *             privileges. MASYER is allowed because when deleting a
	 *             company, users and roles of that company must also be deleted
	 */
	public void removeRole(HttpServletRequest request, AppRoleInfo role)
			throws DisallowedException, ObjectNotFoundException, CodingErrorException;

	/**
	 * 
	 * @param internalRoleName
	 * @return role object
	 */
	public AppRoleInfo getRoleByInternalName(String internalRoleName);

	/**
	 * Creates and stores a privilege which is an object that matches a role to
	 * a privilege type, e.g the role 'administrator' may have the privilege
	 * ADMINISTRATE
	 * 
	 * @throws DisallowedException
	 *             If user doesn't have ADMINISTRATE privileges
	 * 
	 * @throws CantDoThatException
	 *             If the privilege type can't be assigned to a role because it
	 *             is invalid for roles
	 */
	public void addRolePrivilege(HttpServletRequest request, AppRoleInfo role,
			PrivilegeType privilegeType) throws DisallowedException, CantDoThatException;

	/**
	 * Creates and stores a table-specific privilege which is an object that
	 * matches a role, privilege type and table together, e.g the role
	 * 'sales_rep', privilege type VIEW_TABLE_DATA and table 'Sales' may be
	 * linked
	 * 
	 * @throws IllegalArgumentException
	 *             If the privilegeType isn't a table-specific privilege
	 * @throws DisallowedException
	 *             If user doesn't have ADMINISTRATE privileges
	 */
	public void addRolePrivilege(HttpServletRequest request, AppRoleInfo role,
			PrivilegeType privilegeType, TableInfo table) throws IllegalArgumentException,
			DisallowedException;

	public void removeRolePrivilege(HttpServletRequest request, AppRoleInfo role,
			PrivilegeType privilegeType) throws DisallowedException;

	public void removeRolePrivilege(HttpServletRequest request, AppRoleInfo role,
			PrivilegeType privilegeType, TableInfo table) throws IllegalArgumentException,
			DisallowedException;

	/**
	 * Add a general user privilege
	 * 
	 * @throws CantDoThatException
	 *             If the privilege type can't be assigned to that particular
	 *             user because it conflicts with other privileges
	 */
	public void addUserPrivilege(HttpServletRequest request, AppUserInfo appUser,
			PrivilegeType privilegeType) throws DisallowedException, CantDoThatException;

	/**
	 * Add a table-specific user privilege
	 * 
	 * @throws CantDoThatException
	 *             If the privilege type can't be assigned to that particular
	 *             user because it conflicts with other privileges
	 */
	public void addUserPrivilege(HttpServletRequest request, AppUserInfo appUser,
			PrivilegeType privilegeType, TableInfo table) throws IllegalArgumentException,
			DisallowedException, CantDoThatException;

	public void removeUserPrivilege(HttpServletRequest request, AppUserInfo appUser,
			PrivilegeType privilegeType) throws DisallowedException;

	public void removeUserPrivilege(HttpServletRequest request, AppUserInfo appUser,
			PrivilegeType privilegeType, TableInfo table) throws IllegalArgumentException,
			DisallowedException;

	/**
	 * For use when a table is deleted, to clear all privileges there were on
	 * that table
	 * 
	 * @throws DisallowedException
	 *             If the logged in user doesn't have MANAGE_TABLE privileges on
	 *             the table
	 */
	public void removePrivilegesOnTable(HttpServletRequest request, TableInfo table)
			throws DisallowedException;

	/**
	 * @throws MissingParametersException
	 *             If role null or empty string
	 * @throws ObjectNotFoundException
	 *             If role specified doesn't exist
	 * @throws DisallowedException
	 *             If the current user doesn't have ADMINISTRATE privileges
	 * @throws CantDoThatException
	 *             If user has MASTER privileges, they can't be added to a role
	 */
	public void assignUserToRole(HttpServletRequest request, AppUserInfo user, AppRoleInfo role)
			throws DisallowedException, ObjectNotFoundException, MissingParametersException,
			CantDoThatException;

	public void removeUserFromRole(HttpServletRequest request, AppUserInfo user, AppRoleInfo role)
			throws DisallowedException, MissingParametersException, ObjectNotFoundException,
			CodingErrorException, CantDoThatException;

	public AppUserInfo getUserByUserName(HttpServletRequest request, String userName)
			throws ObjectNotFoundException, DisallowedException;

	/**
	 * @return A list of users who can log in from the current company
	 * @throws DisallowedException
	 *             If the current user doesn't have ADMINISTRATE privileges
	 * @throws ObjectNotFoundException
	 *             If there's an error retrieving the current company object
	 */
	public SortedSet<AppUserInfo> getUsers(HttpServletRequest request) throws DisallowedException,
			ObjectNotFoundException;

	/**
	 * @return A list of roles from the current company that users can be
	 *         assigned to. Each role can allow a list of privileges
	 * @throws DisallowedException
	 *             If the current user doesn't have ADMINISTRATE privileges
	 * @throws ObjectNotFoundException
	 *             If there's an error retrieving the current company object
	 */
	public SortedSet<AppRoleInfo> getRoles(HttpServletRequest request) throws DisallowedException,
			ObjectNotFoundException;

	/**
	 * Returns a list of roles that a particular user is assigned to. To return
	 * the list of users in a role, just call role.getUsers() directly
	 * 
	 * @throws DisallowedException
	 *             If the current user doesn't have ADMINISTRATE privileges
	 */
	public SortedSet<AppRoleInfo> getRolesForUser(HttpServletRequest request, AppUserInfo user)
			throws DisallowedException;

	/**
	 * @return The complete set of privileges the user specified has (not
	 *         including privileges they may have as a result of being a member
	 *         of a role)
	 * @throws DisallowedException
	 *             If the current (logged in) user doesn't have ADMINISTRATE
	 *             privileges
	 */
	public Set<UserGeneralPrivilegeInfo> getPrivilegesForUser(HttpServletRequest request,
			AppUserInfo user) throws DisallowedException;

	/**
	 * @return The complete set of privileges the specified role has
	 * @throws DisallowedException
	 *             If the current (logged in) user doesn't have ADMINISTRATE
	 *             privileges
	 */
	public Set<RoleGeneralPrivilegeInfo> getPrivilegesForRole(HttpServletRequest request,
			AppRoleInfo role) throws DisallowedException;

	/**
	 * @return A collection of all the privilege types recognised by the
	 *         application. This is exposed so that an administrator can get a
	 *         list of privileges from which to choose some to assign to a role
	 * @throws DisallowedException
	 *             If the current user doesn't have ADMINISTRATE privileges
	 */
	public EnumSet<PrivilegeType> getPrivilegeTypes(HttpServletRequest request)
			throws DisallowedException;

	/**
	 * IMPORTANT NOTE: Don't use these specifiedUserHasPrivilege methods to do
	 * authentication on the logged in user, use
	 * getAuthenticator().userAllowedTo instead which checks both user and any
	 * roles the user is in and which validates the username
	 */
	public boolean specifiedUserHasPrivilege(HttpServletRequest request,
			PrivilegeType privilegeType, AppUserInfo user) throws DisallowedException;

	/**
	 * Checks whether the specified user has a particular table privilege
	 */
	public boolean specifiedUserHasPrivilege(HttpServletRequest request,
			PrivilegeType privilegeType, AppUserInfo user, TableInfo table)
			throws DisallowedException;

	public boolean specifiedRoleHasPrivilege(HttpServletRequest request,
			PrivilegeType privilegeType, AppRoleInfo role) throws DisallowedException;

	public boolean specifiedRoleHasPrivilege(HttpServletRequest request,
			PrivilegeType privilegeType, AppRoleInfo role, TableInfo table)
			throws DisallowedException;
}
