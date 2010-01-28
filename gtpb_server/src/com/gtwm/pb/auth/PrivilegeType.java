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

import com.gtwm.pb.model.interfaces.TableInfo;

/**
 * A list of privileges that can be used by the application and assigned to user roles. Each privilege is
 * either a general application-wide privilege such as 'edit users' or specific to a particular object, e.g.
 * 'access table'. If the privilege is object specific, then the class of object it applies to is stored
 * 
 * Note: The privilege ADMINISTRATE doesn't give automatic access to all data, but it does allow the user to
 * give themselves view, edit, manage etc. privileges on any item. Full access isn't automatically given so as
 * not to overwhelm the administrator with tables/reports they don't want to see
 */
public enum PrivilegeType {
    // The list of privileges this application recognises, defined in order of increasing privilege - this is
    // important as it allows the app to compare privilege levels
    VIEW_TABLE_DATA("Lets a user view (not edit) data", TableInfo.class), EDIT_TABLE_DATA("Lets a user edit / delete data", TableInfo.class), MANAGE_TABLE(
            "Lets a user edit a table - add & remove fields etc.", TableInfo.class), ADMINISTRATE(
            "Allows users to give themselves any other privilege they like"), MASTER("Allows user to create & manage companies");

    private Boolean objectSpecificPrivilege;

    private Class objectClass;

    private String privilegeDescription = "";

    private PrivilegeType() {
        // Don't allow no-arg constructor
    }
    
    /**
     * If no class of object for the privilege is specified, construct a general application privilege
     */
    PrivilegeType(String privilegeDescription) {
        this.privilegeDescription = privilegeDescription;
        this.objectSpecificPrivilege = false;
    }

    /**
     * Construct an object-specific privilege
     */
    PrivilegeType(String privilegeDescription, Class objectClass) {
        this.privilegeDescription = privilegeDescription;
        this.objectSpecificPrivilege = true;
        this.objectClass = objectClass;
    }

    public Boolean isObjectSpecificPrivilege() {
        return this.objectSpecificPrivilege;
    }

    public Class getObjectClass() {
        return this.objectClass;
    }

    public String getPrivilegeDescription() {
        return this.privilegeDescription;
    }
}
