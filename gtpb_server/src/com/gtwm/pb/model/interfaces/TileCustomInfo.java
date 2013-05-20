package com.gtwm.pb.model.interfaces;

public interface TileCustomInfo extends TileInfo {

	/**
	 * Get the primary location of the tile's resources, e.g. a folder or a template name
	 */
	public String getLocation();

	public void setLocation(String location);
}
