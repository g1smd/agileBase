package com.gtwm.pb.dashboard.interfaces;

import com.gtwm.pb.util.CantDoThatException;

/**
 * Represents a grid to contain chart blocks. Can optimally position chart
 * blocks when placed into it
 */
public interface DashboardGridInfo {

	/**
	 * When given the width and height of a block to place, calculates and
	 * returns the optimal position for that block
	 * 
	 * @throws CantDoThatException
	 *             If the grid becomes too massive
	 */
	public DashboardGridPositionInfo placeBlock(int widthUnits, int HeightUnits)
			throws CantDoThatException;

	/**
	 * Get the width of a single block in this grid in pixels
	 */
	public int getWidthUnit();

	/**
	 * Get the height of a single block in this grid in pixels
	 */
	public int getHeightUnit();

}
