package com.gtwm.pb.dashboard.interfaces;

/**
 * Represents the position of one block (chart) in the dashboard chart grid
 */
public interface DashboardGridPositionInfo {
	
	/** 
	 * Get the top co-ordinate of the block in grid units
	 */
	public int getTop();
	
	/** 
	 * Get the left co-ordinate of the block in grid units
	 */
	public int getLeft();
}
