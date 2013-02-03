package com.gtwm.pb.model.manageSchema.tiles;

import com.gtwm.pb.model.interfaces.TileCustomInfo;
import com.gtwm.pb.util.Enumerations.TileType;
import com.gtwm.pb.util.RandomString;

public class CustomTile extends AbstractTile implements TileCustomInfo {

	public CustomTile(String colour, String location) {
		super.setColour(colour);
		super.setInternalTileName(RandomString.generate());
		super.setTileType(TileType.CUSTOM);
		this.setLocation(location);
	}
	
	public String getLocation() {
		return this.location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
	
	private String location = null;

}
