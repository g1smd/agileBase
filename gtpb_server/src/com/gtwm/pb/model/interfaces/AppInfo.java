package com.gtwm.pb.model.interfaces;

import com.gtwm.pb.util.Enumerations.AppType;

/**
 * An app for the Agilebase social interface
 */
public interface AppInfo {
	
	public String getInternalAppName();

	public AppType getAppType();
	
	/**
	 * Get the name of the CSS colour class
	 */
	public String getColour();
	
	public void setColour(String colour);
	
}
