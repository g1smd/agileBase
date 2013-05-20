package com.gtwm.pb.model.manageSchema.tiles;

import javax.persistence.Entity;
import com.gtwm.pb.model.interfaces.TileCalendarInfo;
import com.gtwm.pb.util.RandomString;
import com.gtwm.pb.util.Enumerations.TileType;

@Entity
public class CalendarTile extends AbstractTile implements TileCalendarInfo {

	private CalendarTile() {
	}

	public CalendarTile(String colour) {
		super.setColour(colour);
		super.setInternalTileName(RandomString.generate());
		super.setTileType(TileType.CALENDAR);
	}

}
