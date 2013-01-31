package com.gtwm.pb.model.manageSchema.apps;

import javax.persistence.Entity;
import com.gtwm.pb.model.interfaces.AppCalendarInfo;
import com.gtwm.pb.util.RandomString;
import com.gtwm.pb.util.Enumerations.AppType;

@Entity
public class CalendarApp extends AbstractApp implements AppCalendarInfo {

	public CalendarApp(String colour) {
		super.setColour(colour);
		super.setInternalAppName(RandomString.generate());
		super.setAppType(AppType.CALENDAR);
	}

}
