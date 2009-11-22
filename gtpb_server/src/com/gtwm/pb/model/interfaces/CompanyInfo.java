/*
 *  Copyright 2009 GT webMarque Ltd
 * 
 *  This file is part of GT portalBase.
 *
 *  GT portalBase is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  GT portalBase is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with GT portalBase.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gtwm.pb.model.interfaces;

import java.util.Set;
import java.util.SortedSet;
import com.gtwm.pb.model.interfaces.AppUserInfo;
import com.gtwm.pb.model.interfaces.AppRoleInfo;
import com.gtwm.pb.util.ObjectNotFoundException;

/**
 * portalBase uses companies to separate data, i.e. each company can only see their own data, users etc.
 * 
 * Users and roles will belong to companies. By extension, tables will also belong to companies since in order
 * to use a table at all, a privilege must exist on it and privileges apply to users and roles
 */
public interface CompanyInfo {
    
    public String getInternalCompanyName();

    public void setCompanyName(String companyName);
    
    public String getCompanyName();

    public SortedSet<AppUserInfo> getUsers();

    public SortedSet<AppRoleInfo> getRoles();
    
    public SortedSet<ModuleInfo> getModules();
    
    /**
     * URLs for tabs that are loaded when portalBase starts
     */
    public Set<String> getTabAddresses();
    
    public ModuleInfo getModuleByInternalName(String internalModuleName) throws ObjectNotFoundException;

    public void addUser(AppUserInfo user);

    public void addRole(AppRoleInfo role);
    
    public void addModule(ModuleInfo module);
    
    public void addTabAddress(String tabAddress);

    public void removeUser(AppUserInfo user);

    public void removeRole(AppRoleInfo role);
    
    public void removeModule(ModuleInfo module);
    
    public void removeTabAddress(String tabAddress);
}
