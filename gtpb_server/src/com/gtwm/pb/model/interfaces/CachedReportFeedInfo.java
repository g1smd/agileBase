package com.gtwm.pb.model.interfaces;

/**
 * A cache of a JSON or RSS string, to save regeneration
 */
public interface CachedReportFeedInfo {

	public String getFeed();

	/** Return the number of milliseconds since this object was created */
	public long getCacheAge();
}
