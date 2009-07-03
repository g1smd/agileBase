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
package com.gtwm.pb.model.manageData;

import com.gtwm.pb.model.interfaces.ModuleActionInfo;

public class ModuleAction implements ModuleActionInfo {
	
	private ModuleAction() {
	}
	
	public ModuleAction(String actionName, String description, String attributes, String actionTemplate, String buttons, String callbackFunction) {
		this.actionName = actionName;
		this.description = description;
		this.attributes = attributes;
		this.actionTemplate = actionTemplate;
		this.buttons = buttons;
		this.callbackFunction = callbackFunction;
	}

	public String getActionName() {
		return this.actionName;
	}
	
	public String getDescription() {
		return this.description;
	}

	public String getActionTemplate() {
		return this.actionTemplate;
	}

	public String getAttributes() {
		return this.attributes;
	}

	public String getButtons() {
		return this.buttons;
	}

	public String getCallbackFunction() {
		return this.callbackFunction;
	}
	
	public String toString() {
		return this.getActionName();
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		return ((ModuleAction) obj).getActionName().equals(this.getActionName());
	}
	
	public int hashCode() {
		return this.getActionName().hashCode();
	}

	private String actionName = "";
	
	private String description = "";
	
	private String attributes = "";
	
	private String callbackFunction = "";
	
	private String buttons = "";
	
	private String actionTemplate = "";
}
