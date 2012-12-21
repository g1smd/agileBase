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

import com.gtwm.pb.model.interfaces.AuthCacheObjectInfo;
import com.gtwm.pb.model.interfaces.TableInfo;

public class AuthCacheObject implements AuthCacheObjectInfo {

	private AuthCacheObject() {
		this.table = null;
		this.privilegeType = null;
	}

	public AuthCacheObject(TableInfo table, PrivilegeType privilegeType, boolean allowedTo) {
		this.table = table;
		this.privilegeType = privilegeType;
		this.allowedTo = allowedTo;
	}

	public AuthCacheObject(PrivilegeType privilegeType, boolean allowedTo) {
		this.table = null;
		this.privilegeType = privilegeType;
		this.allowedTo = allowedTo;
	}

	public PrivilegeType getPrivilegeType() {
		return this.privilegeType;
	}

	public TableInfo getTable() {
		return this.table;
	}

	public boolean userAllowedTo() {
		return this.allowedTo;
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		AuthCacheObject authCacheObject = (AuthCacheObject) obj;
		if (this.getTable() == null) {
			if ((authCacheObject.getTable() == null)
					&& (this.getPrivilegeType().equals(authCacheObject.getPrivilegeType()))
					&& (this.userAllowedTo() == authCacheObject.userAllowedTo())) {
				return true;
			}
		} else {
			if (this.getTable().equals(authCacheObject.getTable())
					&& this.getPrivilegeType().equals(authCacheObject.getPrivilegeType())
					&& (this.userAllowedTo() == authCacheObject.userAllowedTo())) {
				return true;
			}
		}
		return false;
	}

	public int hashCode() {
		if (this.hashCode == 0) {
			int hashCode = 17;
			if (this.getTable() != null) {
				hashCode = 37 * hashCode + this.getTable().hashCode();
			}
			hashCode = 37 * hashCode + this.getPrivilegeType().hashCode();
			hashCode = 37 * hashCode + Boolean.valueOf(this.allowedTo).hashCode();
			this.hashCode = hashCode;
		}
		return this.hashCode;
	}

	private volatile int hashCode = 0;

	private final TableInfo table;

	private final PrivilegeType privilegeType;

	private boolean allowedTo = false;
}