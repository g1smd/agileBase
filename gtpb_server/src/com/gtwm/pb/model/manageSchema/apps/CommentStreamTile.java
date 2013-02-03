package com.gtwm.pb.model.manageSchema.apps;

import javax.persistence.Entity;
import com.gtwm.pb.model.interfaces.TileCommentStreamInfo;
import com.gtwm.pb.util.Enumerations.TileType;
import com.gtwm.pb.util.RandomString;

@Entity
public class CommentStreamTile extends AbstractTile implements TileCommentStreamInfo {

	public CommentStreamTile(String colour) {
		super.setColour(colour);
		super.setInternalTileName(RandomString.generate());
		super.setAppType(TileType.COMMENT_STREAM);
	}
	
}
