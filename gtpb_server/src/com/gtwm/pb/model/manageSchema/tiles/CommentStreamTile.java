package com.gtwm.pb.model.manageSchema.tiles;

import javax.persistence.Entity;
import com.gtwm.pb.model.interfaces.TileCommentStreamInfo;
import com.gtwm.pb.util.Enumerations.TileType;
import com.gtwm.pb.util.RandomString;

@Entity
public class CommentStreamTile extends AbstractTile implements TileCommentStreamInfo {

	private CommentStreamTile() {
	}

	public CommentStreamTile(String colour) {
		super.setColour(colour);
		super.setInternalTileName(RandomString.generate());
		super.setTileType(TileType.COMMENT_STREAM);
	}

}
