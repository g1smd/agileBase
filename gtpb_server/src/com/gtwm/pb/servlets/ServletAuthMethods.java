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
package com.gtwm.pb.servlets;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.SortedSet;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.grlea.log.SimpleLogger;
import org.hibernate.HibernateException;
import com.gtwm.pb.auth.AppRole;
import com.gtwm.pb.auth.AppUser;
import com.gtwm.pb.auth.DisallowedException;
import com.gtwm.pb.auth.PrivilegeType;
import com.gtwm.pb.model.interfaces.AppRoleInfo;
import com.gtwm.pb.model.interfaces.AppUserInfo;
import com.gtwm.pb.model.interfaces.CompanyInfo;
import com.gtwm.pb.model.interfaces.SessionDataInfo;
import com.gtwm.pb.model.interfaces.AuthManagerInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.DatabaseInfo;
import com.gtwm.pb.model.interfaces.UserTablePrivilegeInfo;
import com.gtwm.pb.model.interfaces.RoleTablePrivilegeInfo;
import com.gtwm.pb.model.manageData.DataManagement;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.Helpers;
import com.gtwm.pb.util.HibernateUtil;
import com.gtwm.pb.util.MissingParametersException;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.AgileBaseException;
import com.gtwm.pb.util.RandomString;
import com.gtwm.pb.util.Enumerations.InitialView;

/**
 * Methods to do with setting up and using authentication (adding users,
 * checking privileges etc.) to be used by the main agileBase servlet
 * AppController, or any other custom servlet written for a particular
 * application based on agileBase. The JavaDoc here describes the HTTP requests
 * that must be sent to use the methods.
 * 
 * Part of a set of three interfaces, ServletSchemaMethods to manage setting up
 * the database schema, ServletDataMethods to manage data editing and
 * ServletAuthMethods to do with users, roles and privileges
 * 
 * @see ServletSchemaMethods
 * @see ServletDataMethods
 */
public final class ServletAuthMethods {

	/**
	 * Sends a password reset email to a user. Anyone can do this given a username
	 */
	public synchronized static void sendPasswordReset(HttpServletRequest request,
			AuthManagerInfo authManager) throws DisallowedException, ObjectNotFoundException,
			CantDoThatException, CodingErrorException, MessagingException, MissingParametersException {
		String antiRobot = request.getParameter("iamnotanumber");
		if (!antiRobot.trim().equals("9")) {
			throw new CantDoThatException("Wrong answer to the question");
		}
		HibernateUtil.startHibernateTransaction();
		try {
			authManager.sendPasswordReset(request);
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (HibernateException hex) {
			HibernateUtil.rollbackHibernateTransaction();
			throw new CantDoThatException("Sending password reset failed", hex);
		} catch (AgileBaseException pex) {
			HibernateUtil.rollbackHibernateTransaction();
			throw new CantDoThatException(pex.getMessage(), pex);
		} finally {
			HibernateUtil.closeSession();
		}
	}

	/**
	 * TODO: Http example usage
	 * 
	 * @throws MissingParametersException
	 *           If there's an internal error passed up from
	 *           authManager.addUser(). This should never happen
	 * @throws ObjectNotFoundException
	 *           If the userName string of the actual created user differs in any
	 *           way to the userName passed, this exception will be thrown when
	 *           trying to set the session user to the newly created one because
	 *           the user object won't be able to be retrieved
	 */
	public synchronized static void addUser(SessionDataInfo sessionData, HttpServletRequest request,
			AuthManagerInfo authManager) throws DisallowedException, MissingParametersException,
			ObjectNotFoundException, CodingErrorException, CantDoThatException {
		String internalUserName = request.getParameter("internalusername");
		String baseUsername = request.getParameter(AppUserInfo.USERNAME.toLowerCase(Locale.UK));
		String username = baseUsername;
		CompanyInfo company = authManager.getCompanyForLoggedInUser(request);
		if (baseUsername == null) {
			String companyName = company.getCompanyName().toLowerCase();
			companyName = companyName.replaceAll("\\W", "");
			baseUsername = "new" + companyName + "user";
			// Ensure that username is unique
			username = baseUsername;
			int usernameCount = 0;
			Set<String> existingUserNames = new HashSet<String>();
			for (AppUserInfo appUser : authManager.getUsers(request)) {
				existingUserNames.add(appUser.getUserName());
			}
			while (existingUserNames.contains(username)) {
				usernameCount++;
				username = baseUsername + String.valueOf(usernameCount);
			}
		} else {
			Set<String> existingUserNames = new HashSet<String>();
			for (AppUserInfo appUser : authManager.getUsers(request)) {
				existingUserNames.add(appUser.getUserName());
			}
			if (existingUserNames.contains(username)) {
				throw new CantDoThatException("Username " + username
						+ " already exists, please choose a different one");
			}
		}
		String surname = request.getParameter(AppUserInfo.SURNAME.toLowerCase(Locale.UK));
		String forename = request.getParameter(AppUserInfo.FORENAME.toLowerCase(Locale.UK));
		if (surname == null && forename == null) {
			surname = "User";
			forename = "New";
		}
		String password = request.getParameter(AppUserInfo.PASSWORD.toLowerCase(Locale.UK));
		if (password == null) {
			password = RandomString.generate();
		}
		String email = request.getParameter(AppUserInfo.EMAIL.toLowerCase());
		String custom1 = request.getParameter(AppUserInfo.CUSTOM1.toLowerCase());
		String useCustomUIString = request.getParameter(AppUserInfo.USES_CUSTOM_UI.toLowerCase());
		boolean useCustomUI = false;
		if (useCustomUIString != null) {
			useCustomUI = Helpers.valueRepresentsBooleanTrue(useCustomUIString);
		}
		// begin updating model and persisting changes
		AppUserInfo newUser = new AppUser(company, internalUserName, username, surname, forename,
				password, email, custom1, useCustomUI);
		HibernateUtil.startHibernateTransaction();
		try {
			// add the user:
			HibernateUtil.currentSession().save(newUser);
			authManager.addUser(request, newUser);
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (HibernateException hex) {
			HibernateUtil.rollbackHibernateTransaction();
			// remove user
			authManager.removeUser(request, newUser);
			throw new CantDoThatException("User addition failed", hex);
		} catch (AgileBaseException pex) {
			HibernateUtil.rollbackHibernateTransaction();
			// remove user
			authManager.removeUser(request, newUser);
			throw new CantDoThatException("User addition failed", pex);
		} finally {
			HibernateUtil.closeSession();
		}
		sessionData.setUser(newUser);
	}

	/**
	 * @param request
	 * @throws DisallowedException
	 *           If the logged in user doesn't have ADMINISTRATE privileges
	 * @throws ObjectNotFoundException
	 *           If the user isn't found in the application's list of users
	 */
	public synchronized static void removeUser(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn) throws DisallowedException,
			ObjectNotFoundException, CodingErrorException, CantDoThatException {
		String internalUserName = request.getParameter("internalusername");
		AuthManagerInfo authManager = databaseDefn.getAuthManager();
		AppUserInfo appUser;
		if (internalUserName == null) {
			appUser = sessionData.getUser();
		} else {
			appUser = authManager.getUserByInternalName(request, internalUserName);
		}
		if (appUser == null) {
			throw new ObjectNotFoundException(
					"'internalusername' was not provided and there is no user in the session");
		}
		// begin updating model and persisting changes
		HibernateUtil.startHibernateTransaction();
		try {
			authManager.removeUser(request, appUser);
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (HibernateException hex) {
			HibernateUtil.rollbackHibernateTransaction();
			// return user to memory
			try {
				authManager.addUser(request, appUser);
			} catch (MissingParametersException mpex) {
				logger.error("Unexpected error rolling back removeUser: " + appUser);
			}
			throw new CantDoThatException("User removal failed", hex);
		} catch (AgileBaseException pex) {
			HibernateUtil.rollbackHibernateTransaction();
			// return user to memory
			try {
				authManager.addUser(request, appUser);
			} catch (MissingParametersException mpex) {
				logger.error("Unexpected error rolling back removeUser: " + appUser);
			}
			throw new CantDoThatException(pex.getMessage(), pex);
		} finally {
			HibernateUtil.closeSession();
		}
		sessionData.setUser(null);
	}

	public static void uploadProfilePicture(SessionDataInfo sessionData, DatabaseInfo databaseDefn, HttpServletRequest request, List<FileItem> multipartItems) throws ObjectNotFoundException, DisallowedException, CantDoThatException, FileUploadException {
		if (!FileUpload.isMultipartContent(new ServletRequestContext(request))) {
			throw new CantDoThatException(
					"To upload a profile picture, the form must be posted as multi-part form data");
		}
		String internalUserName = ServletUtilMethods.getParameter(request, "internalusername",
				multipartItems);
		AppUserInfo user;
		AuthManagerInfo authManager = databaseDefn.getAuthManager();
		if (internalUserName != null) {
			user = authManager.getUserByInternalName(request, internalUserName);
		} else {
			user = authManager.getLoggedInUser(request);
		}
		String uploadFolderName = databaseDefn.getDataManagement().getWebAppRoot() + "profiles";
		File uploadFolder = new File(uploadFolderName);
		if (!uploadFolder.exists()) {
			if (!uploadFolder.mkdirs()) {
				throw new CantDoThatException("Error creating upload folder " + uploadFolderName);
			}
		}
		for (FileItem item : multipartItems) {
			// if item is a file
			if (!item.isFormField()) {
				long fileSize = item.getSize();
				if (fileSize == 0) {
					throw new CantDoThatException("An empty file was submitted, no upload done");
				}
				String filePath = uploadFolder + "/" + user.getInternalUserName() + ".jpg";
				File profilePicture = new File(filePath);
				try {
					item.write(profilePicture);
					user.setHasProfilePhoto(true);
				} catch (Exception ex) {
					// Catching a general exception?! This is because the
					// library throws a raw exception. Not very good
					throw new FileUploadException("Error writing file: " + ex.getMessage());
				}
				DataManagement.createThumbnail(40, 60, filePath);
			}
		}
	}
	
	public synchronized static void updateUser(SessionDataInfo sessionData,
			HttpServletRequest request, AuthManagerInfo authManager) throws DisallowedException,
			MissingParametersException, ObjectNotFoundException, CantDoThatException,
			CodingErrorException {
		AppUserInfo appUser;
		String internalUserName = request.getParameter("internalusername");
		if (internalUserName == null) {
			appUser = sessionData.getUser();
		} else {
			appUser = authManager.getUserByInternalName(request, internalUserName);
		}
		if (appUser == null) {
			throw new ObjectNotFoundException(
					"'internalusername' was not provided and there is no user in the session");
		}
		String userName = request.getParameter(AppUserInfo.USERNAME.toLowerCase());
		String surname = request.getParameter(AppUserInfo.SURNAME.toLowerCase());
		String forename = request.getParameter(AppUserInfo.FORENAME.toLowerCase());
		String password = request.getParameter(AppUserInfo.PASSWORD.toLowerCase());
		String email = request.getParameter(AppUserInfo.EMAIL.toLowerCase());
		boolean usesCustomUI = appUser.getUsesCustomUI();
		String usesCustomUIString = request.getParameter(AppUserInfo.USES_CUSTOM_UI.toLowerCase());
		if (usesCustomUIString != null) {
			usesCustomUI = Helpers.valueRepresentsBooleanTrue(usesCustomUIString);
		}
		boolean usesAppLauncher = appUser.getUsesAppLauncher();
		String usesAppLauncherString = request
				.getParameter(AppUserInfo.USES_APP_LAUNCHER.toLowerCase());
		if (usesAppLauncherString != null) {
			usesAppLauncher = Helpers.valueRepresentsBooleanTrue(usesAppLauncherString);
		}
		if (password != null) {
			if (password.equals("false")) {
				throw new CantDoThatException("User update failed: error setting password");
			}
		}
		String userTypeString = request.getParameter(AppUserInfo.INITIALVIEW.toLowerCase());
		InitialView userType = null;
		if (userTypeString != null) {
			userType = InitialView.valueOf(userTypeString.toUpperCase());
		}
		// cache the old values for rollback
		String oldUserName = appUser.getUserName();
		String oldSurname = appUser.getSurname();
		String oldForename = appUser.getForename();
		String oldEmail = appUser.getEmail();
		InitialView oldUserType = appUser.getUserType();
		boolean oldUsesCustomUI = appUser.getUsesCustomUI();
		boolean oldUsesAppLauncher = appUser.getUsesAppLauncher();
		// begin updating model and persisting changes
		HibernateUtil.startHibernateTransaction();
		try {
			authManager.updateUser(request, appUser, userName, surname, forename, password, email,
					userType, usesCustomUI, usesAppLauncher);
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (HibernateException hex) {
			HibernateUtil.rollbackHibernateTransaction();
			// rollback memory
			authManager.updateUser(request, appUser, oldUserName, oldSurname, oldForename, null,
					oldEmail, oldUserType, oldUsesCustomUI, oldUsesAppLauncher);
			throw new CantDoThatException("User update failed", hex);
		} catch (AgileBaseException pex) {
			HibernateUtil.rollbackHibernateTransaction();
			// rollback memory
			authManager.updateUser(request, appUser, oldUserName, oldSurname, oldForename, null,
					oldEmail, oldUserType, oldUsesCustomUI, oldUsesAppLauncher);
			throw new CantDoThatException(pex.getMessage(), pex);
		} finally {
			HibernateUtil.closeSession();
		}
	}

	public synchronized static void addRole(SessionDataInfo sessionData, HttpServletRequest request,
			AuthManagerInfo authManager) throws DisallowedException, MissingParametersException,
			ObjectNotFoundException, CodingErrorException, CantDoThatException {
		CompanyInfo company = authManager.getCompanyForLoggedInUser(request);
		String internalRoleName = request.getParameter("internalrolename");
		String baseRoleName = request.getParameter("rolename");
		String roleName = baseRoleName;
		if (baseRoleName == null) {
			String companyName = company.getCompanyName().toLowerCase().replaceAll("\\W", "");
			baseRoleName = "new" + companyName + "role";
			// Ensure that role name is unique
			SortedSet<AppRoleInfo> roles = authManager.getRoles(request);
			Set<String> roleNames = new HashSet<String>(roles.size());
			for (AppRoleInfo role : roles) {
				roleNames.add(role.getRoleName());
			}
			roleName = baseRoleName;
			int roleCount = 0;
			while (roleNames.contains(roleName)) {
				roleCount++;
				roleName = baseRoleName + String.valueOf(roleCount);
			}
		}
		// begin updating model and persisting changes
		AppRole role = new AppRole(company, internalRoleName, roleName);
		HibernateUtil.startHibernateTransaction();
		try {
			HibernateUtil.currentSession().save(role);
			authManager.addRole(request, role);
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (HibernateException hex) {
			HibernateUtil.rollbackHibernateTransaction();
			// rollback memory
			authManager.removeRole(request, role);
			throw new CantDoThatException("Role addition failed", hex);
		} catch (AgileBaseException pex) {
			HibernateUtil.rollbackHibernateTransaction();
			// rollback memory
			authManager.removeRole(request, role);
			throw new CantDoThatException("Role addition failed", pex);
		} finally {
			HibernateUtil.closeSession();
		}
		sessionData.setRole(role);
	}

	public synchronized static void updateRole(SessionDataInfo sessionData,
			HttpServletRequest request, AuthManagerInfo authManager) throws DisallowedException,
			MissingParametersException, ObjectNotFoundException, CodingErrorException,
			CantDoThatException {
		AppRoleInfo role = sessionData.getRole();
		if (role == null) {
			throw new ObjectNotFoundException("No role has been selected for update");
		}
		String newRoleName = request.getParameter("rolename");
		// begin updating model and persisting changes
		String oldRoleName = role.getRoleName();
		HibernateUtil.startHibernateTransaction();
		try {
			authManager.updateRole(request, role, newRoleName);
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (HibernateException hex) {
			HibernateUtil.rollbackHibernateTransaction();
			// rollback memory
			authManager.updateRole(request, role, oldRoleName);
			throw new CantDoThatException("Role update failed", hex);
		} catch (AgileBaseException pex) {
			HibernateUtil.rollbackHibernateTransaction();
			// rollback memory
			authManager.updateRole(request, role, oldRoleName);
			throw new CantDoThatException("Role update failed", pex);
		} finally {
			HibernateUtil.closeSession();
		}
	}

	public synchronized static void removeRole(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn) throws DisallowedException,
			MissingParametersException, ObjectNotFoundException, CodingErrorException,
			CantDoThatException {
		String internalRoleName = request.getParameter("rolename");
		AuthManagerInfo authManager = databaseDefn.getAuthManager();
		AppRoleInfo role = authManager.getRoleByInternalName(internalRoleName);
		if (role == null) {
			role = sessionData.getRole();
		}
		if (role == null) {
			throw new ObjectNotFoundException(
					"'rolename' was not provided or could not be found and there is no role in the session");
		}
		// begin updating model and persisting changes
		HibernateUtil.startHibernateTransaction();
		try {
			authManager.removeRole(request, role);
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (HibernateException hex) {
			HibernateUtil.rollbackHibernateTransaction();
			// rollback memory
			authManager.addRole(request, role);
			throw new CantDoThatException("Role removal failed", hex);
		} catch (AgileBaseException pex) {
			HibernateUtil.rollbackHibernateTransaction();
			// rollback memory
			authManager.addRole(request, role);
			throw new CantDoThatException("Role removal failed", pex);
		} finally {
			HibernateUtil.closeSession();
		}
		sessionData.setRole(null);
		CompanyInfo company = authManager.getCompanyForLoggedInUser(request);
	}

	/**
	 * @throws MissingParametersException
	 *           If role or privilegeType not specified in the request
	 * @throws ObjectNotFoundException
	 *           If tableInternalName specified in the request doesn't map to an
	 *           actual table
	 * @throws IllegalArgumentException
	 *           If privilegeType doesn't map to an actual PrivilegeType
	 */
	public synchronized static void addPrivilege(HttpServletRequest request, DatabaseInfo databaseDefn)
			throws DisallowedException, MissingParametersException, ObjectNotFoundException,
			IllegalArgumentException, CantDoThatException {
		AuthManagerInfo authManager = databaseDefn.getAuthManager();
		String privilegeTypeParameter = request.getParameter("privilegetype");
		String internalRoleName = request.getParameter("internalrolename");
		String internalUserName = request.getParameter("internalusername");
		if ((internalRoleName == null && internalUserName == null) || privilegeTypeParameter == null) {
			throw new MissingParametersException(
					"The 'privilegetype' parameter and either 'internalusername' or 'internalrolename' are required to add a privilege");
		}
		PrivilegeType privilegeType = PrivilegeType.valueOf(privilegeTypeParameter.toUpperCase());
		String internalTableName = request.getParameter("internaltablename");
		try {
			HibernateUtil.startHibernateTransaction();
			if (internalTableName != null) {
				// Add a table specific privilege
				TableInfo table = databaseDefn.getTable(request, internalTableName);
				if (internalRoleName != null) {
					AppRoleInfo role = authManager.getRoleByInternalName(internalRoleName);
					authManager.addRolePrivilege(request, role, privilegeType, table);
				}
				if (internalUserName != null) {
					AppUserInfo appUser = authManager.getUserByInternalName(request, internalUserName);
					authManager.addUserPrivilege(request, appUser, privilegeType, table);
				}
			} else {
				// Add a general privilege
				if (internalRoleName != null) {
					AppRoleInfo role = authManager.getRoleByInternalName(internalRoleName);
					authManager.addRolePrivilege(request, role, privilegeType);
				}
				if (internalUserName != null) {
					AppUserInfo appUser = authManager.getUserByInternalName(request, internalUserName);
					authManager.addUserPrivilege(request, appUser, privilegeType);
				}
			}
		} catch (Exception ex) {
			HibernateUtil.rollbackHibernateTransaction();
			throw new CantDoThatException("Privilege addition failed", ex);
		} finally {
			HibernateUtil.finishHibernateTransaction();
		}
	}

	/**
	 * 
	 * @param request
	 * @throws DisallowedException
	 * @throws MissingParametersException
	 *           If role or privilegetype not specified in the request
	 * @throws ObjectNotFoundException
	 *           If tableInternalName specified in the request doesn't map to an
	 *           actual table
	 * @throws IllegalArgumentException
	 *           If privilegeType doesn't map to an actual PrivilegeType
	 */
	public synchronized static void removePrivilege(HttpServletRequest request,
			DatabaseInfo databaseDefn) throws DisallowedException, MissingParametersException,
			ObjectNotFoundException, IllegalArgumentException, CantDoThatException {
		AuthManagerInfo authManager = databaseDefn.getAuthManager();
		CompanyInfo company = authManager.getCompanyForLoggedInUser(request);
		String privilegeTypeParameter = request.getParameter("privilegetype");
		String internalRoleName = request.getParameter("internalrolename");
		String internalUserName = request.getParameter("internalusername");
		if ((internalRoleName == null && internalUserName == null) || privilegeTypeParameter == null) {
			throw new MissingParametersException(
					"The 'privilegetype' parameter and either 'internalusername' or 'internalrolename' are required to remove a privilege");
		}
		PrivilegeType privilegeType = PrivilegeType.valueOf(privilegeTypeParameter.toUpperCase());
		String internalTableName = request.getParameter("internaltablename");
		try {
			HibernateUtil.startHibernateTransaction();
			if (internalTableName != null) {
				// Remove a table specific privilege
				TableInfo table = databaseDefn.getTable(request, internalTableName);
				if (internalRoleName != null) {
					AppRoleInfo role = authManager.getRoleByInternalName(internalRoleName);
					authManager.removeRolePrivilege(request, role, privilegeType, table);
				}
				if (internalUserName != null) {
					AppUserInfo appUser = authManager.getUserByInternalName(request, internalUserName);
					authManager.removeUserPrivilege(request, appUser, privilegeType, table);
				}
			} else {
				// Remove a general privilege
				if (internalRoleName != null) {
					AppRoleInfo role = authManager.getRoleByInternalName(internalRoleName);
					authManager.removeRolePrivilege(request, role, privilegeType);
				}
				if (internalUserName != null) {
					AppUserInfo appUser = authManager.getUserByInternalName(request, internalUserName);
					authManager.removeUserPrivilege(request, appUser, privilegeType);
				}
			}
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (Exception ex) {
			HibernateUtil.currentSession().getTransaction().rollback();
			throw new CantDoThatException("Privilege removal failed", ex);
		} finally {
			HibernateUtil.closeSession();
		}
	}

	/**
	 * Gives a user/role all privileges up to the level specified by the
	 * privilegetype HTTP parameter. If they have any higher level privileges,
	 * removes them
	 * 
	 * @throws MissingParametersException
	 *           If privilegetype, internaltablename and one of username or
	 *           rolename are not included in the HTTP request
	 * @throws CantDoThatException
	 *           If the privilegetype supplied isn't a table-specific privilege
	 */
	public synchronized static void setMaxTablePrivilege(SessionDataInfo sessionData,
			HttpServletRequest request, DatabaseInfo databaseDefn) throws MissingParametersException,
			CantDoThatException, ObjectNotFoundException, DisallowedException {
		AuthManagerInfo authManager = databaseDefn.getAuthManager();
		String privilegeTypeParameter = request.getParameter("privilegetype");
		String internalRoleName = request.getParameter("internalrolename");
		String internalUserName = request.getParameter("internalusername");
		String internalTableName = request.getParameter("internaltablename");
		String assignTo = request.getParameter("assignto");
		if (privilegeTypeParameter == null || internalTableName == null) {
			throw new MissingParametersException(
					"The 'privilegetype', 'internaltablename' and 'assignto' parameters are required to set the max privilege level");
		}
		PrivilegeType maxPrivilegeType = PrivilegeType.valueOf(privilegeTypeParameter.toUpperCase());
		TableInfo table = databaseDefn.getTable(request, internalTableName);
		// Check it's a table privilege
		if (maxPrivilegeType.isObjectSpecificPrivilege()) {
			if (!(maxPrivilegeType.getObjectClass().equals(TableInfo.class))) {
				throw new CantDoThatException(
						"When setting max privileges for a table, you must supply a table-specific privilege");
			}
		} else {
			throw new CantDoThatException(
					"When setting max privileges for a table, you must supply a table-specific privilege not a general one");
		}
		long startTime = System.currentTimeMillis();
		HibernateUtil.startHibernateTransaction();
		try {
			if (assignTo.equals("user")) {
				// user-specific privileges
				AppUserInfo user = null;
				if (internalUserName != null) {
					user = authManager.getUserByInternalName(request, internalUserName);
				} else {
					// lookup user from session
					user = sessionData.getUser();
				}
				// ensure that user is now not null
				if (user == null) {
					throw new MissingParametersException(
							"User not found. Please check 'internalusername' parameter or ensure that the session user has been set");
				}
				// loop through all the table-specific privileges
				for (PrivilegeType testPrivilegeType : PrivilegeType.values()) {
					// ignore non-table specific privileges
					if (testPrivilegeType.isObjectSpecificPrivilege()) {
						boolean specifiedUserHasPrivilege = authManager.specifiedUserHasPrivilege(request,
								testPrivilegeType, user, table);
						if ((maxPrivilegeType.ordinal() >= testPrivilegeType.ordinal())
								&& (!specifiedUserHasPrivilege)) {
							// if privilege requested higher (or the same as)
							// than one they haven't got, give user
							// the one they haven't got
							authManager.addUserPrivilege(request, user, testPrivilegeType, table);
						} else if ((maxPrivilegeType.ordinal() < testPrivilegeType.ordinal())
								&& specifiedUserHasPrivilege) {
							// if privilege requested lower than one they've
							// got, remove it
							authManager.removeUserPrivilege(request, user, testPrivilegeType, table);
						}
					}
				}
			} else if (assignTo.equals("role")) {
				AppRoleInfo role = null;
				if (internalRoleName == null) {
					// lookup role from session
					role = sessionData.getRole();
				} else {
					role = authManager.getRoleByInternalName(internalRoleName);
				}
				// ensure that role is now not null
				if (role == null) {
					throw new MissingParametersException(
							"Role not found. Please check 'internalrolename' parameter or ensure that the session role has been set");
				}
				CompanyInfo company = authManager.getCompanyForLoggedInUser(request);
				// role specific privileges handled similarly to user specific
				// privileges above
				for (PrivilegeType testPrivilegeType : PrivilegeType.values()) {
					if (testPrivilegeType.isObjectSpecificPrivilege()) {
						if ((maxPrivilegeType.ordinal() >= testPrivilegeType.ordinal())
								&& (!authManager.specifiedRoleHasPrivilege(request, testPrivilegeType, role, table))) {
							authManager.addRolePrivilege(request, role, testPrivilegeType, table);
						} else if ((maxPrivilegeType.ordinal() < testPrivilegeType.ordinal())
								&& (authManager.specifiedRoleHasPrivilege(request, testPrivilegeType, role, table))) {
							authManager.removeRolePrivilege(request, role, testPrivilegeType, table);
						}
					}
				}
			} else {
				throw new CantDoThatException(
						"Unrecognised 'assignto' parameter value. Please use either 'user' or 'role'");
			}
			HibernateUtil.currentSession().getTransaction().commit();
			long duration = System.currentTimeMillis() - startTime;
		} catch (HibernateException hex) {
			HibernateUtil.rollbackHibernateTransaction();
			throw new CantDoThatException("Privilege setting failed", hex);
		} catch (AgileBaseException pbex) {
			HibernateUtil.rollbackHibernateTransaction();
			throw new CantDoThatException("Privilege setting failed", pbex);
		} finally {
			HibernateUtil.closeSession();
		}
	}

	public synchronized static void clearAllTablePrivileges(HttpServletRequest request,
			DatabaseInfo databaseDefn) throws ObjectNotFoundException, MissingParametersException,
			DisallowedException, CantDoThatException {
		AuthManagerInfo authManager = databaseDefn.getAuthManager();
		String internalRoleName = request.getParameter("internalrolename");
		String internalUserName = request.getParameter("internalusername");
		String internalTableName = request.getParameter("internaltablename");
		if ((internalUserName == null && internalRoleName == null) || (internalTableName == null)) {
			throw new MissingParametersException(
					"The 'internaltablename' parameter and either 'internalusername' or 'internalrolename' are required to remove all table privileges from a user/role");
		}
		TableInfo table = databaseDefn.getTable(request, internalTableName);
		Set<UserTablePrivilegeInfo> userPrivilegesOnTable = authManager.getUserPrivilegesOnTable(
				request, table);
		Set<RoleTablePrivilegeInfo> rolePrivilegesOnTable = authManager.getRolePrivilegesOnTable(
				request, table);
		int userPrivilegesSize = userPrivilegesOnTable.size();
		int rolePrivilegesSize = rolePrivilegesOnTable.size();
		try {
			HibernateUtil.startHibernateTransaction();
			// if removing from individual user
			if (internalUserName != null) {
				AppUserInfo user = authManager.getUserByInternalName(request, internalUserName);
				for (PrivilegeType testPrivilegeType : PrivilegeType.values()) {
					if (testPrivilegeType.isObjectSpecificPrivilege()) {
						if (authManager.specifiedUserHasPrivilege(request, testPrivilegeType, user, table)) {
							// Check that at least one other user or role has
							// privileges on the table
							if (rolePrivilegesSize == 0) {
								boolean otherUserPrivsFound = false;
								PRIVLOOP: for (UserTablePrivilegeInfo userPrivilegeOnTable : userPrivilegesOnTable) {
									if (!userPrivilegeOnTable.getUser().equals(user)) {
										otherUserPrivsFound = true;
										break PRIVLOOP;
									}
								}
								if (!otherUserPrivsFound) {
									throw new CantDoThatException(
											"At least one user/role must have privileges on a table");
								}
							}
							authManager.removeUserPrivilege(request, user, testPrivilegeType, table);
						}
					}
				}
			} else {
				// removing privilege from role
				AppRoleInfo role = authManager.getRoleByInternalName(internalRoleName);
				for (PrivilegeType testPrivilegeType : PrivilegeType.values()) {
					if (testPrivilegeType.isObjectSpecificPrivilege()) {
						if (authManager.specifiedRoleHasPrivilege(request, testPrivilegeType, role, table)) {
							// Check that at least one other user or role has
							// privileges on the table
							if (userPrivilegesSize == 0) {
								boolean otherRolePrivsFound = false;
								PRIVLOOP: for (RoleTablePrivilegeInfo rolePrivilegeOnTable : rolePrivilegesOnTable) {
									if (!rolePrivilegeOnTable.getRole().equals(role)) {
										otherRolePrivsFound = true;
										break PRIVLOOP;
									}
								}
								if (!otherRolePrivsFound) {
									throw new CantDoThatException(
											"At least one user/role must have privileges on a table");
								}
							}
							authManager.removeRolePrivilege(request, role, testPrivilegeType, table);
						}
					}
				}
			}
			CompanyInfo company = authManager.getCompanyForLoggedInUser(request);
		} catch (AgileBaseException pbex) {
			HibernateUtil.rollbackHibernateTransaction();
			throw new CantDoThatException("No privileges cleared: " + pbex.getMessage(), pbex);
		} catch (HibernateException hex) {
			HibernateUtil.rollbackHibernateTransaction();
			throw new CantDoThatException("Privilege clearing failed", hex);
		} finally {
			HibernateUtil.finishHibernateTransaction();
		}
	}

	public synchronized static void assignUserToRole(SessionDataInfo sessionData,
			HttpServletRequest request, AuthManagerInfo authManager) throws ObjectNotFoundException,
			DisallowedException, MissingParametersException, CantDoThatException {
		String internalRoleName = request.getParameter("internalrolename");
		String internalUserName = request.getParameter("internalusername");
		AppUserInfo user = null;
		if (internalUserName == null) {
			user = sessionData.getUser();
		}
		if ((internalRoleName == null) || ((user == null) && (internalUserName == null))) {
			throw new MissingParametersException("'internalrolename' and 'internalusername' are required");
		}
		if (user == null) {
			user = authManager.getUserByInternalName(request, internalUserName);
		}
		AppRoleInfo role = authManager.getRoleByInternalName(internalRoleName);
		// begin updating model and persisting changes
		HibernateUtil.startHibernateTransaction();
		try {
			// add the user to role:
			authManager.assignUserToRole(request, user, role);
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (HibernateException hex) {
			HibernateUtil.rollbackHibernateTransaction();
			// rollback memory:
			role.removeUser(user);
			throw new CantDoThatException("Assigning user to role failed", hex);
		} catch (AgileBaseException pex) {
			HibernateUtil.rollbackHibernateTransaction();
			// rollback memory:
			role.removeUser(user);
			throw new CantDoThatException("Assigning user to role failed", pex);
		} finally {
			HibernateUtil.closeSession();
		}
	}

	public synchronized static void removeUserFromRole(HttpServletRequest request,
			DatabaseInfo databaseDefn) throws ObjectNotFoundException, DisallowedException,
			MissingParametersException, CodingErrorException, CantDoThatException {
		String internalRoleName = request.getParameter("internalrolename");
		String internalUserName = request.getParameter("internalusername");
		if (internalRoleName == null || internalUserName == null) {
			throw new MissingParametersException(
					"'internalrolename' and 'internalusername' parameters are required");
		}
		AuthManagerInfo authManager = databaseDefn.getAuthManager();
		AppUserInfo user = authManager.getUserByInternalName(request, internalUserName);
		AppRoleInfo role = authManager.getRoleByInternalName(internalRoleName);
		// begin updating model and persisting changes
		HibernateUtil.startHibernateTransaction();
		try {
			authManager.removeUserFromRole(request, user, role);
			HibernateUtil.currentSession().getTransaction().commit();
		} catch (HibernateException hex) {
			HibernateUtil.rollbackHibernateTransaction();
			// rollback memory:
			role.assignUser(user);
			throw new CantDoThatException("Removing user from role failed", hex);
		} catch (AgileBaseException pex) {
			HibernateUtil.rollbackHibernateTransaction();
			// rollback memory:
			role.assignUser(user);
			throw new CantDoThatException("Removing user from role failed", pex);
		} finally {
			HibernateUtil.closeSession();
		}
	}

	private static final SimpleLogger logger = new SimpleLogger(ServletAuthMethods.class);
}
