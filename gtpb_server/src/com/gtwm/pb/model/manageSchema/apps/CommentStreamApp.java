package com.gtwm.pb.model.manageSchema.apps;

import javax.persistence.Transient;

import com.gtwm.pb.model.interfaces.AppCommentStreamInfo;
import com.gtwm.pb.util.RandomString;

public class CommentStreamApp extends AbstractApp implements AppCommentStreamInfo {

	public CommentStreamApp(String colour) {
		super.setColour(colour);
		super.setInternalAppName(RandomString.generate());
	}
	
	@Transient
	public String getAppName() {
		return "Recent Comments";
	}
	
	public String toString() {
		return this.getAppName();
	}

}