package com.gtwm.pb.model.interfaces;

import com.gtwm.pb.util.Enumerations.TileType;

/**
 * An app for the Agilebase social interface
 */
public interface TileInfo extends Comparable<TileInfo> {
	
	public String getInternalTileName();

	public TileType getTileType();
	
	public String getTileName();
	
	/**
	 * Get the icon name
	 */
	public String getIcon();
	
	public void setIcon(String icon);
	
	/**
	 * Get the name of the CSS colour class
	 */
	public String getColour();
	
	public void setColour(String colour);
	
}
