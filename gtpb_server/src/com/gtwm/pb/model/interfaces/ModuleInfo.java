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

import java.util.Set;

/**
 * A module consists of a set of reports plus any custom functionality that can
 * be added in such as data entry wizards
 * 
 * Custom functionality is handled by customisation templates rather than built
 * into this object model
 */
public interface ModuleInfo {

	public String getModuleName();

	public void setModuleName(String moduleName);

	/**
	 * Return the path and filename of the icon for this module, relative to the
	 * agileBase root
	 */
	public String getIconPath();

	public void setIconPath(String iconPath);

	/**
	 * Return the background colour
	 */
	public String getColour();

	public void setColour(String colour);

	/**
	 * Return a number that can be used for ordering the modules
	 */
	public int getIndexNumber();

	public void setIndexNumber(int intexNumber);
	
	/**
	 * Return the section that this module's in
	 */
	public String getSection();
	
	public void setSection(String section);
	
	/**
	 * If this module/app is a standalone app with a separate template, return that
	 */
	public String getAppTemplate();
	
	public void setAppTemplate(String appTemplate);

	public String getInternalModuleName();
	
	public void addRelatedModule(ModuleInfo module);
	
	public void removeRelatedModule(ModuleInfo module);
	
	public Set<ModuleInfo> getRelatedModules();
	
	/**
	 * If the user has added (or removed) any modules manually, this will return false, otherwise it will return true
	 */
	public boolean getUseDefaultRelatedModules();

}
