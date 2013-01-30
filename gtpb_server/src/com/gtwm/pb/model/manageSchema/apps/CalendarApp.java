package com.gtwm.pb.model.manageSchema.apps;

import javax.persistence.Entity;
import javax.persistence.Transient;

import com.gtwm.pb.model.interfaces.AppCalendarInfo;
import com.gtwm.pb.util.RandomString;

@Entity
public class CalendarApp extends AbstractApp implements AppCalendarInfo {

	public CalendarApp(String colour) {
		super.setColour(colour);
		super.setInternalAppName(RandomString.generate());
	}

	@Transient
	public String getAppName() {
		return "Today";
	}
	
	public String toString() {
		return this.getAppName();
	}
}
