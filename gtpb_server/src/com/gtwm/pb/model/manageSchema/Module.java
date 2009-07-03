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
package com.gtwm.pb.model.manageSchema;

import javax.persistence.Entity;
import javax.persistence.Id;
import com.gtwm.pb.model.interfaces.ModuleInfo;
import com.gtwm.pb.util.RandomString;

@Entity
public class Module implements ModuleInfo, Comparable<ModuleInfo> {

	private Module() {
	}
	
	public Module(String moduleName, String iconPath, int indexNumber) {
		this.setInternalModuleName((new RandomString()).toString());
		this.setModuleName(moduleName);
		this.setIconPath(iconPath);
		this.setIndexNumber(indexNumber);
	}
	
	public String getIconPath() {
		return this.iconPath;
	}

	public String getModuleName() {
		return this.moduleName;
	}

	public void setIconPath(String iconPath) {
		this.iconPath = iconPath;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public void setColour(String colour) {
		this.colour = colour;
	}
	
	public String getColour() {
		return this.colour;
	}
	
	public int getIndexNumber() {
		return this.indexNumber;
	}

	public void setIndexNumber(int indexNumber) {
		this.indexNumber = indexNumber;
	}
	
	public String getSection() {
		return this.section;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		ModuleInfo otherModule = (ModuleInfo) obj;
		return (this.getInternalModuleName().equals(otherModule.getInternalModuleName()));
	}

	public int hashCode() {
		return this.getInternalModuleName().hashCode();
	}
	
	/**
	 * Compare on index number, then name, then factors in object equality
	 */
	public int compareTo(ModuleInfo otherModule) {
		if (this == otherModule) {
			return 0;
		}
		int otherIndex = otherModule.getIndexNumber();
		int thisIndex = this.getIndexNumber();
		if (otherIndex != thisIndex) {
			return Integer.valueOf(thisIndex).compareTo(otherIndex);
		}
		String otherComparator = otherModule.getModuleName() + otherModule.getInternalModuleName();
		otherComparator = otherComparator.toLowerCase();
		String thisComparator = this.getModuleName() + this.getInternalModuleName();
		thisComparator = thisComparator.toLowerCase();
		return thisComparator.compareTo(otherComparator);
	}
	
	public String toString() {
		return this.getModuleName();
	}
	
	@Id
	public String getInternalModuleName() {
		return this.internalModuleName;
	}
	
	private void setInternalModuleName(String internalModuleName) {
		this.internalModuleName = internalModuleName;
	}
	
	private String internalModuleName = "";
	
	private String moduleName = "";
	
	private String iconPath = "";
	
	private String colour = "";
	
	private String section = "";

	private int indexNumber = 1;

}
