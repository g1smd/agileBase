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
package com.gtwm.pb.model.manageData;

import com.gtwm.pb.model.interfaces.ModuleActionInfo;

public class ModuleAction implements ModuleActionInfo {
	
	private ModuleAction() {
		this.actionName = null;
		this.description = null;
		this.attributes = null;
		this.actionTemplate = null;
		this.buttons = null;
		this.callbackFunction = null;
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
		return this.actionName.hashCode();
	}

	private final String actionName;
	
	private final String description;
	
	private final String attributes;
	
	private final String callbackFunction;
	
	private final String buttons;
	
	private final String actionTemplate;
}
