package com.gtwm.pb.model.manageSchema.tiles;

import javax.persistence.Entity;
import com.gtwm.pb.model.interfaces.TileFocusInfo;
import com.gtwm.pb.util.RandomString;
import com.gtwm.pb.util.Enumerations.TileType;

@Entity
public class FocusTile extends AbstractTile implements TileFocusInfo {

	private FocusTile() {
	}

	public FocusTile(String colour) {
		super.setColour(colour);
		super.setInternalTileName(RandomString.generate());
		super.setTileType(TileType.FOCUS);
	}

}
