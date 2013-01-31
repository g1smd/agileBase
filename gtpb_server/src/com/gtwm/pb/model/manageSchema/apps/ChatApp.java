package com.gtwm.pb.model.manageSchema.apps;

import javax.persistence.Entity;
import com.gtwm.pb.model.interfaces.AppChatInfo;
import com.gtwm.pb.util.RandomString;
import com.gtwm.pb.util.Enumerations.AppType;

@Entity
public class ChatApp extends AbstractApp implements AppChatInfo {

	public ChatApp(String colour) {
		super.setColour(colour);
		super.setInternalAppName(RandomString.generate());
		super.setAppType(AppType.CHAT);
	}

}
