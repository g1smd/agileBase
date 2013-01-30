package com.gtwm.pb.model.manageSchema.apps;

import javax.persistence.Entity;
import javax.persistence.Transient;

import com.gtwm.pb.model.interfaces.AppChatInfo;
import com.gtwm.pb.util.RandomString;

@Entity
public class ChatApp extends AbstractApp implements AppChatInfo {

	public ChatApp(String colour) {
		super.setColour(colour);
		super.setInternalAppName(RandomString.generate());
	}

	@Transient
	public String getAppName() {
		return "Staff";
	}
	
	public String toString() {
		return this.getAppName();
	}

}
