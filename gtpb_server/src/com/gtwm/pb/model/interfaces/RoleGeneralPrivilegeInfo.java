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
package com.gtwm.pb.model.interfaces;

import com.gtwm.pb.auth.PrivilegeType;
import com.gtwm.pb.model.interfaces.AppRoleInfo;

/**
 * Represents an application level privilege assigned to a particular role, something like 'edit users' or
 * 'assign privileges'
 */
public interface RoleGeneralPrivilegeInfo {

    /**
     * @return The role that this privilege applies to
     */
    public AppRoleInfo getRole();

    public PrivilegeType getPrivilegeType();
}
