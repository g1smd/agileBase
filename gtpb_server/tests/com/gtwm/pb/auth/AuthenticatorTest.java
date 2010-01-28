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
package com.gtwm.pb.auth;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.Test;
import org.junit.Before;
import com.gtwm.pb.auth.AppUser;
import com.gtwm.pb.model.interfaces.AppRoleInfo;
import com.gtwm.pb.model.interfaces.AppUserInfo;
import com.gtwm.pb.model.interfaces.CompanyInfo;
import com.gtwm.pb.model.interfaces.RoleGeneralPrivilegeInfo;
import com.gtwm.pb.model.interfaces.UserGeneralPrivilegeInfo;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.MissingParametersException;
import com.gtwm.pb.util.RandomString;

public class AuthenticatorTest {

	AppUserInfo appUser = null;
	CompanyInfo company = null;
	AppRoleInfo appRole = null;
	Authenticator authenticator = new Authenticator();
	
	@Before public void setUp() throws MissingParametersException, CodingErrorException { 
		this.company = new Company("Test Company");
		this.appUser = new AppUser(this.company, new RandomString().toString(), "testuser", "User", "Test", "password");
		this.appRole = new AppRole(this.company, new RandomString().toString(), "testrole");
		this.authenticator.addCompany(company);
		this.authenticator.addUser(appUser);
		this.authenticator.addRole(appRole);
	}
	
	@Test
	public void getPrivilegesForUser() throws CantDoThatException {
 		 this.authenticator.addUserPrivilege(this.appUser, PrivilegeType.ADMINISTRATE);
		 Set<UserGeneralPrivilegeInfo> privileges = this.authenticator.getPrivilegesForUser(this.appUser);
		 assertEquals(privileges.size(),1);
		 for(UserGeneralPrivilegeInfo privilege : privileges) {
			 assertEquals(privilege.getPrivilegeType(), PrivilegeType.ADMINISTRATE);
			 assertEquals(privilege.getUser(), this.appUser);
		 }
	}
	
	@Test
	public void getPrivilegesForRole() throws CantDoThatException {
		 this.authenticator.addRolePrivilege(this.appRole, PrivilegeType.ADMINISTRATE);
		 Set<RoleGeneralPrivilegeInfo> privileges = this.authenticator.getPrivilegesForRole(this.appRole);
		 assertEquals(privileges.size(),1);
		 for(RoleGeneralPrivilegeInfo privilege : privileges) {
			 assertEquals(privilege.getPrivilegeType(), PrivilegeType.ADMINISTRATE);
			 assertEquals(privilege.getRole(), this.appRole);
		 }
	}
}
