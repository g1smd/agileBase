package com.gtwm.pb.model.manageSchema.tiles;

import javax.persistence.Entity;
import com.gtwm.pb.model.interfaces.TileChatInfo;
import com.gtwm.pb.util.RandomString;
import com.gtwm.pb.util.Enumerations.TileType;

@Entity
public class ChatTile extends AbstractTile implements TileChatInfo {

	private ChatTile() {
	}

	public ChatTile(String colour) {
		super.setColour(colour);
		super.setInternalTileName(RandomString.generate());
		super.setTileType(TileType.CHAT);
	}

}
