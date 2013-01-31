package com.gtwm.pb.model.manageSchema.apps;

import javax.persistence.Entity;
import com.gtwm.pb.model.interfaces.AppCommentStreamInfo;
import com.gtwm.pb.util.Enumerations.AppType;
import com.gtwm.pb.util.RandomString;

@Entity
public class CommentStreamApp extends AbstractApp implements AppCommentStreamInfo {

	public CommentStreamApp(String colour) {
		super.setColour(colour);
		super.setInternalAppName(RandomString.generate());
		super.setAppType(AppType.COMMENT_STREAM);
	}
	
}
