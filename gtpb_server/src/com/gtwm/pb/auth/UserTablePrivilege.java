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

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.UserTablePrivilegeInfo;
import com.gtwm.pb.model.interfaces.AppUserInfo;
import com.gtwm.pb.model.manageSchema.TableDefn;

@Entity
public class UserTablePrivilege extends UserGeneralPrivilege implements UserTablePrivilegeInfo {

	private UserTablePrivilege() {
	}

	/**
	 * Construct a table privilege
	 */
	public UserTablePrivilege(AppUserInfo appUser, PrivilegeType privilegeType, TableInfo table)
			throws IllegalArgumentException {
		// Only allow the privilege to be constructed if the privilege type is
		// compatible with a table
		// For example you couldn't have an 'access report' privilege acting on
		// a table, only a report
		if (privilegeType.getObjectClass().equals(TableInfo.class)) {
			super.setUser(appUser);
			super.setPrivilegeType(privilegeType);
			this.setTable(table);
		} else {
			throw new IllegalArgumentException("Can't make an " + privilegeType.toString()
					+ " privilege for a table");
		}
	}

	@ManyToOne(targetEntity = TableDefn.class)
	public TableInfo getTable() {
		return this.table;
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		UserTablePrivilege otherPrivilege = (UserTablePrivilege) obj;
		if (!this.getUser().equals(otherPrivilege.getUser())) {
			return false;
		}
		if (!this.getPrivilegeType().equals(otherPrivilege.getPrivilegeType())) {
			return false;
		}
		return this.getTable().equals(otherPrivilege.getTable());
	}

	public int hashCode() {
		if (this.hashCode == 0) {
			int result = 17;
			result = 37 * result + this.getUser().hashCode();
			result = 37 * result + this.getPrivilegeType().hashCode();
			result = 37 * result + this.getTable().hashCode();
			this.hashCode = result;
		}
		return this.hashCode;
	}

	public String toString() {
		return "" + this.getUser() + ", " + this.getPrivilegeType() + " " + this.getTable();
	}

	/**
	 * Hibernate use only
	 */
	private void setTable(TableInfo table) {
		this.table = table;
	}

	private volatile int hashCode = 0;

	private TableInfo table;

	private long id;
}
