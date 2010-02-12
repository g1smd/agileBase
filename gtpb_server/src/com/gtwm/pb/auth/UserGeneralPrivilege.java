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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Enumerated;
import javax.persistence.EnumType;
import javax.persistence.ManyToOne;
import com.gtwm.pb.model.interfaces.AppUserInfo;
import com.gtwm.pb.model.interfaces.UserGeneralPrivilegeInfo;

@Entity
// Specify inheritance because UserObjectPrivilege extends this type
@Inheritance(strategy = InheritanceType.JOINED) 
public class UserGeneralPrivilege implements UserGeneralPrivilegeInfo {

    /**
     * @see com.gtwm.pb.auth.RoleGeneralPrivilege See RoleGeneralPrivilege for why the protected methods are
     *      protected. Also needs to be at least protected level for Hibernate
     */
    protected UserGeneralPrivilege() {
    }

    public UserGeneralPrivilege(AppUserInfo appUser, PrivilegeType privilegeType) {
        this.setUser(appUser);
        this.setPrivilegeType(privilegeType);
    }

    @Id
    @GeneratedValue
    /**
     * Hibernate needs an ID for a persistent class - this isn't actually used by the app otherwise
     */
    private long getId() {
        return this.id;
    }
    
    private void setId(long id) {
        this.id = id;
    }
    
    protected void setUser(AppUserInfo appUser) {
        this.appUser = appUser;
    }

    protected void setPrivilegeType(PrivilegeType privilegeType) {
        this.privilegeType = privilegeType;
    }

    @ManyToOne(targetEntity=AppUser.class)
    public AppUserInfo getUser() {
        return this.appUser;
    }

    @Enumerated(EnumType.STRING)
    public PrivilegeType getPrivilegeType() {
        return this.privilegeType;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (obj.getClass() != this.getClass())) {
            return false;
        }
        UserGeneralPrivilege otherPrivilege = (UserGeneralPrivilege) obj;
        if (! this.getUser().equals(otherPrivilege.getUser())) {
        	return false;
        }
        return this.getPrivilegeType().equals(otherPrivilege.getPrivilegeType());
    }

    public int hashCode() {
    	if (this.hashCode == 0) {
    		int result = 17;
    		result = 37 * result + this.getUser().hashCode();
    		result = 37 * result + this.getPrivilegeType().hashCode();
    		this.hashCode = result;
    	}
    	return this.hashCode;
    }
    
    public String toString() {
        return "" + this.getUser() + ", " + this.getPrivilegeType();
    }

    private volatile int hashCode = 0;
    
    private AppUserInfo appUser;

    private PrivilegeType privilegeType;
    
    private long id;
}
