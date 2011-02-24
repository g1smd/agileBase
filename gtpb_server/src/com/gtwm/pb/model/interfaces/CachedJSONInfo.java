package com.gtwm.pb.model.interfaces;

/**
 * A cache of a JSON string, to save regeneration
 */
public interface CachedJSONInfo {

	public String getJSON();
	
	/** Return the epoch time that this object was created */
	public long getCacheTime();
}
