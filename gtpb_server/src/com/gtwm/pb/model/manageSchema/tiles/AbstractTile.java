package com.gtwm.pb.model.manageSchema.tiles;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Transient;

import com.gtwm.pb.model.interfaces.TileInfo;
import com.gtwm.pb.util.Enumerations.TileType;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AbstractTile {

	@Id
	public String getInternalTileName() {
		return this.internalTileName;
	}

	protected void setInternalTileName(String internalTileName) {
		this.internalTileName = internalTileName;
	}
	
	@Enumerated(EnumType.STRING)
	public TileType getTileType() {
		return this.tileType;
	}
	
	protected void setTileType(TileType tileType) {
		this.tileType = tileType;
	}
	
	@Transient
	public String getTileName() {
		return this.getTileType().getTileName();
	}
	
	public String getColour() {
		return this.colour;
	}
	
	public void setColour(String colour) {
		this.colour = colour;
	}
	
	public String getIcon() {
		return this.icon;
	}
	
	public void setIcon(String icon) {
		this.icon = icon;
	}
	
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		return this.getInternalTileName().equals(((TileInfo) obj).getInternalTileName());
	}
	
	public int hashCode() {
		return this.getInternalTileName().hashCode();
	}
	
	public String toString() {
		return this.getTileName();
	}
	
	/**
	 * Compare first by app type (apps of the same type go together) then by report, if applicable, then consistent with equals
	 */
	public int compareTo(TileInfo otherTile) {
		TileType otherAppType = otherTile.getTileType();
		if (!otherAppType.equals(this.getTileType())) {
			return this.getTileType().compareTo(otherAppType);
		}
		if ((this instanceof DataLinkTile) && (otherTile instanceof DataLinkTile)) {
			DataLinkTile thisTile = (DataLinkTile) this;
			return thisTile.getReport().compareTo(((DataLinkTile) otherTile).getReport());
		}
		return this.getInternalTileName().compareTo(otherTile.getInternalTileName());
	}

	private String internalTileName = null;
	
	private TileType tileType = null;
	
	private String colour = null;
	
	private String icon = null;

}
