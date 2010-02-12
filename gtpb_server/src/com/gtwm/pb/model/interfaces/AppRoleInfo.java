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
package com.gtwm.pb.model.interfaces;

import java.util.SortedSet;

/**
 * Represents an application role.
 */
public interface AppRoleInfo {
	public static final String ROLENAME = "roleName";
    
    public CompanyInfo getCompany();
	
    public void setRoleName( String roleName );
    
    public String getRoleName();
    
    public String getInternalRoleName();
    
    public SortedSet<AppUserInfo> getUsers();
    
    public void assignUser(AppUserInfo user);
    
    public void removeUser(AppUserInfo user);
}
