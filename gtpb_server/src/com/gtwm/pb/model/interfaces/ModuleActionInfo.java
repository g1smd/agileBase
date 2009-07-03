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

/**
 * An action that can be added to a report group to form part of an application
 * e.g. a link that loads a data entry wizard
 */
public interface ModuleActionInfo {

	public String getActionName();
	
	public String getDescription();
	
	/**
	 * Any attributes such as CSS definitions that should be passed to the action dialog box
	 */
	public String getAttributes();
	
	/**
	 * Return the name of the function that should be called when the OK button is pressed and the action finishes
	 */
	public String getCallbackFunction();
	
	/**
	 * Return the name of the Velocity template that is (the start of) this action
	 */
	public String getActionTemplate();
	
	/**
	 * A list of the buttons this action uses
	 */
	public String getButtons();
}
