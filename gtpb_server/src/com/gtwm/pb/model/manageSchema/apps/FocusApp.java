package com.gtwm.pb.model.manageSchema.apps;

import javax.persistence.Entity;
import com.gtwm.pb.model.interfaces.AppFocusInfo;
import com.gtwm.pb.util.RandomString;
import com.gtwm.pb.util.Enumerations.AppType;

@Entity
public class FocusApp extends AbstractApp implements AppFocusInfo {

	public FocusApp(String colour) {
		super.setColour(colour);
		super.setInternalAppName(RandomString.generate());
		super.setAppType(AppType.FOCUS);
	}
	
}
