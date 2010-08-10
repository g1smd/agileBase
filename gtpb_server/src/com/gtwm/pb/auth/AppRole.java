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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;
import com.gtwm.pb.model.interfaces.AppRoleInfo;
import com.gtwm.pb.model.interfaces.AppUserInfo;
import com.gtwm.pb.model.interfaces.CompanyInfo;
import com.gtwm.pb.util.RandomString;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.HashSet;
import java.util.TreeSet;

@Entity
public class AppRole implements AppRoleInfo, Comparable<AppRoleInfo> {

	protected AppRole() {
	}

	public AppRole(CompanyInfo company, String internalRoleName, String roleName) {
		this.setCompany(company);
		if (internalRoleName == null) {
			this.setInternalRoleName((new RandomString()).toString());
		} else {
			this.setInternalRoleName(internalRoleName);
		}
		this.setRoleName(roleName);
	}

	@ManyToOne(targetEntity = Company.class)
	public CompanyInfo getCompany() {
		return this.company;
	}

	private void setCompany(CompanyInfo company) {
		this.company = company;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getRoleName() {
		return this.roleName;
	}

	public void assignUser(AppUserInfo user) {
		this.getUsersDirect().add(user);
	}

	public void removeUser(AppUserInfo user) {
		this.getUsersDirect().remove(user);
	}

	@Transient
	public SortedSet<AppUserInfo> getUsers() {
		return Collections.unmodifiableSortedSet(new TreeSet<AppUserInfo>(this.getUsersDirect()));
	}

	// cascadeType isn't ALL because we don't want users to be deleted when a
	// parent role is deleted
	@ManyToMany(targetEntity = AppUser.class, cascade = { CascadeType.MERGE, CascadeType.PERSIST,
			CascadeType.REFRESH })
	// Uni-directional OneToMany
	private Set<AppUserInfo> getUsersDirect() {
		return this.users;
	}

	private void setUsersDirect(Set<AppUserInfo> users) {
		this.users = users;
	}

	@Id
	public String getInternalRoleName() {
		return this.internalRoleName;
	}

	private void setInternalRoleName(String internalRoleName) {
		this.internalRoleName = internalRoleName;
	}

	public String toString() {
		return this.getRoleName();
	}

	public int compareTo(AppRoleInfo anotherAppRole) {
		if (this == anotherAppRole) {
			return 0;
		}
		// For performance, compare first on item most likely to differ
		int comparison = this.getRoleName().compareTo(anotherAppRole.getRoleName());
		if (comparison != 0) {
			return comparison;
		}
		return this.getInternalRoleName().compareTo(anotherAppRole.getInternalRoleName());
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		return getInternalRoleName().equals(((AppRoleInfo) obj).getInternalRoleName());
	}

	public int hashCode() {
		if (this.hashCode == 0) {
			this.hashCode = this.getInternalRoleName().hashCode();
		}
		return this.hashCode;
	}

	private volatile int hashCode = 0;

	private CompanyInfo company = null;

	private String internalRoleName = "";

	private String roleName = "";

	private Set<AppUserInfo> users = new HashSet<AppUserInfo>();
}
