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
package com.gtwm.pb.model.manageSchema;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import com.gtwm.pb.model.interfaces.ModuleInfo;
import com.gtwm.pb.util.RandomString;

@Entity
public class Module implements ModuleInfo, Comparable<ModuleInfo> {

	private Module() {
	}

	public Module(String moduleName, String iconPath, int indexNumber) {
		this.setInternalModuleName(RandomString.generate());
		this.setModuleName(moduleName);
		this.setIconPath(iconPath);
		this.setIndexNumber(indexNumber);
		this.setUseDefaultRelatedModules(true);
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

	public String getAppTemplate() {
		return this.appTemplate;
	}

	public void setAppTemplate(String appTemplate) {
		this.appTemplate = appTemplate;
	}

	public void addRelatedModule(ModuleInfo module) {
		this.getRelatedModules().add(module);
		this.setUseDefaultRelatedModules(false);
	}

	public void removeRelatedModule(ModuleInfo module) {
		this.getRelatedModules().remove(module);
		this.setUseDefaultRelatedModules(false);
	}

	// cascadeType isn't ALL because we don't want users to be deleted when a
	// parent role is deleted
	@ManyToMany(targetEntity = Module.class, cascade = {CascadeType.MERGE, CascadeType.PERSIST,
		CascadeType.REFRESH})
	public Set<ModuleInfo> getRelatedModules() {
		return this.relatedModules;
	}

	/**
	 * For Hibernate
	 */
	private void setRelatedModules(Set<ModuleInfo> relatedModules) {
		this.relatedModules = relatedModules;
	}

	public boolean getUseDefaultRelatedModules() {
		return this.useDefaultRelatedModules;
	}

	/**
	 * For Hibernate
	 */
	private void setUseDefaultRelatedModules(boolean useDefaultRelatedModules) {
		this.useDefaultRelatedModules = useDefaultRelatedModules;
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

	private String appTemplate = "";

	private Set<ModuleInfo> relatedModules = new HashSet<ModuleInfo>();

	private boolean useDefaultRelatedModules = true;

}
