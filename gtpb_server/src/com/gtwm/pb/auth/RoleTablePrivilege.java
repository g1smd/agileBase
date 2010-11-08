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
import com.gtwm.pb.model.interfaces.RoleTablePrivilegeInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.AppRoleInfo;
import com.gtwm.pb.model.manageSchema.TableDefn;

@Entity
public class RoleTablePrivilege extends RoleGeneralPrivilege implements RoleTablePrivilegeInfo {

    protected RoleTablePrivilege() {    
    }

    /**
     * Construct a table privilege
     * 
     * @param role
     *            The role to assign the privilege type to
     * @param privilegeType
     *            The privilege type to assign, which must be appropriate for a table
     * @param table
     */
    public RoleTablePrivilege(AppRoleInfo role, PrivilegeType privilegeType, TableInfo table) throws IllegalArgumentException {
        // Only allow the privilege to be constructed if the privilege type is compatible with a table
        // For example you couldn't have an 'access report' privilege acting on a table, only a report
        if (privilegeType.getObjectClass().equals(TableInfo.class)) {
            super.setRole(role);
            super.setPrivilegeType(privilegeType);
            this.setTable(table);
        } else {
            throw new IllegalArgumentException("Can't make an " + privilegeType.toString() + " privilege for a table");
        }
    }
    
    @ManyToOne(targetEntity=TableDefn.class)
    public TableInfo getTable() {
        return this.table;
    }
    
    /**
     * Hibernate use only
     */
    private void setTable(TableInfo table) {
        this.table = table;
    }
    
    public boolean equals(Object obj) {
        if (super.equals(obj) == false) {
            return false;
        }
        return this.getTable().equals(((RoleTablePrivilege) obj).getTable());
    }
    
    public int hashCode() {
    	int hashCode = super.hashCode();
    	hashCode = 37 * hashCode + this.getTable().hashCode();
    	return hashCode;
    }
    
    public String toString() {
        return "" + this.getRole() + ", " + this.getPrivilegeType() + " " + this.getTable();
    }
    
    private TableInfo table;
    
    private long id;
}
