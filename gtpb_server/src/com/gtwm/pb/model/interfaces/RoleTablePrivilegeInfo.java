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

/**
 * Represents an privilege that ties together a user role with a particular object an privilege, e.g
 * 'Directors may access the accounts table'
 */
public interface RoleTablePrivilegeInfo extends RoleGeneralPrivilegeInfo {

    /**
     *
     * @return TableInfo object to which permission refers
     *
     *         NOTE: we could look at this again in future
     */
    public TableInfo getTable();
}
