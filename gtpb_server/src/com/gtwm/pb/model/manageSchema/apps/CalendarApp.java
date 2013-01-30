package com.gtwm.pb.model.manageSchema.apps;

import com.gtwm.pb.model.interfaces.AppCalendarInfo;
import com.gtwm.pb.util.RandomString;

public class CalendarApp extends AbstractApp implements AppCalendarInfo {

	public CalendarApp(String colour) {
		super.setColour(colour);
		super.setInternalAppName(RandomString.generate());
	}

	public String getAppName() {
		return "Today";
	}
	
	public String toString() {
		return this.getAppName();
	}
}
