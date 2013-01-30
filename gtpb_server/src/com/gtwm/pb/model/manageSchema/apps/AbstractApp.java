package com.gtwm.pb.model.manageSchema.apps;

import javax.persistence.Id;
import com.gtwm.pb.model.interfaces.AppInfo;
import com.gtwm.pb.util.Enumerations.AppType;

public abstract class AbstractApp {

	@Id
	public String getInternalAppName() {
		return this.internalAppName;
	}

	protected void setInternalAppName(String internalAppName) {
		this.internalAppName = internalAppName;
	}
	
	public AppType getAppType() {
		return this.appType;
	}
	
	public String getColour() {
		return this.colour;
	}
	
	public void setColour(String colour) {
		this.colour = colour;
	}
	
	public String getIcon() {
		return this.icon;
	}
	
	public void setIcon(String icon) {
		this.icon = icon;
	}
	
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		return this.getInternalAppName().equals(((AppInfo) obj).getInternalAppName());
	}
	
	public int hashCode() {
		return this.getInternalAppName().hashCode();
	}

	private String internalAppName = null;
	
	private AppType appType = null;
	
	private String colour = null;
	
	private String icon = null;

}
