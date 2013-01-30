package com.gtwm.pb.model.manageSchema.apps;

import com.gtwm.pb.model.interfaces.AppFocusInfo;
import com.gtwm.pb.util.RandomString;

public class FocusApp extends AbstractApp implements AppFocusInfo {

	public FocusApp(String colour) {
		super.setColour(colour);
		super.setInternalAppName(RandomString.generate());
	}
	
	public String getAppName() {
		return "Focus";
	}
	
	public String toString() {
		return this.getAppName();
	}

}
