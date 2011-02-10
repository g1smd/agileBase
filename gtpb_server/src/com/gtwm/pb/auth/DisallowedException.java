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

import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.util.AgileBaseException;

/**
 * Should be raised when user tries to do something they haven't got privileges for. 
 * This should never happen in normal circumstances because the UI shouldn't allow it
 */
public class DisallowedException extends AgileBaseException {

    public DisallowedException(PrivilegeType privilegeType) {
        super("User is not allowed to " + privilegeType.getPrivilegeDescription());
        this.privilegeType = privilegeType;
    }
    
    public DisallowedException(PrivilegeType privilegeType, TableInfo table) {
        super("User is not allowed to " + privilegeType.getPrivilegeDescription() + " in table '" + table.getTableName() + "'. An administrator can set up privileges so this can be allowed");
        this.privilegeType = privilegeType;
        this.table = table;
    }
    
    public PrivilegeType getPrivilegeType() {
        return this.privilegeType;
    }
    
    public TableInfo getTable() {
        return this.table;
    }
    
    private PrivilegeType privilegeType = null;
    
    private TableInfo table = null;

}
