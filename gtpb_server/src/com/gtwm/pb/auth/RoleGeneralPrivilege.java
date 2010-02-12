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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import com.gtwm.pb.model.interfaces.RoleGeneralPrivilegeInfo;
import com.gtwm.pb.model.interfaces.AppRoleInfo;

@Entity
//Specify inheritance because RoleObjectPrivilege extends this type
@Inheritance(strategy = InheritanceType.JOINED) 
public class RoleGeneralPrivilege implements RoleGeneralPrivilegeInfo {

    /**
     * Protected for the same reason that setRole() is
     */
    protected RoleGeneralPrivilege() {
    }
    
    public RoleGeneralPrivilege(AppRoleInfo role, PrivilegeType privilegeType) {
        this.setRole(role);
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
    
    /**
     * This method is protected because we don't want it publicly usable, in general the role should be passed
     * to the constructor. However, we want classes that extend this one to be able to set the role by calling
     * super.setRole()
     */
    protected void setRole(AppRoleInfo role) {
        this.role = role;
    }

    /**
     * Protected for the same reason that setRole() is
     */
    protected void setPrivilegeType(PrivilegeType privilegeType) {
        this.privilegeType = privilegeType;
    }

    @ManyToOne(targetEntity=AppRole.class)
    public AppRoleInfo getRole() {
        return this.role;
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
        if (this.getRole().equals(((RoleGeneralPrivilege) obj).getRole()) && this.getPrivilegeType().equals(((RoleGeneralPrivilege) obj).getPrivilegeType())) {
            return true;
        }
        return false;
    }

    public int hashCode() {
    	int hashCode = 17;
    	hashCode = 37 * hashCode + this.getRole().hashCode();
    	hashCode = 37 * hashCode + this.getPrivilegeType().hashCode();
        return hashCode;
    }
    
    public String toString() {
        return ("" + this.getRole() + ", " + this.getPrivilegeType());
    }

    private AppRoleInfo role;

    private PrivilegeType privilegeType;
    
    private long id;
}
