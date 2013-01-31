package com.gtwm.pb.model.manageSchema.apps;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import com.gtwm.pb.model.interfaces.AppInfo;
import com.gtwm.pb.util.Enumerations.AppType;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AbstractApp {

	@Id
	public String getInternalAppName() {
		return this.internalAppName;
	}

	protected void setInternalAppName(String internalAppName) {
		this.internalAppName = internalAppName;
	}
	
	@Enumerated(EnumType.STRING)
	public AppType getAppType() {
		return this.appType;
	}
	
	protected void setAppType(AppType appType) {
		this.appType = appType;
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
	
	/**
	 * Compare first by app type (apps of the same type go together) then consistent with equals
	 */
	public int compareTo(AppInfo otherApp) {
		AppType otherAppType = otherApp.getAppType();
		if (!otherAppType.equals(this.getAppType())) {
			return this.getAppType().compareTo(otherAppType);
		}
		return this.getInternalAppName().compareTo(otherApp.getInternalAppName());
	}

	private String internalAppName = null;
	
	private AppType appType = null;
	
	private String colour = null;
	
	private String icon = null;

}
