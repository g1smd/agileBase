package com.gtwm.pb.model.manageData;

import com.gtwm.pb.model.interfaces.CachedReportFeedInfo;

public class CachedFeed implements CachedReportFeedInfo {

	private CachedFeed() {
		this.cacheTime = 0;
		this.cachedFeed = null;
	}
	
	public CachedFeed(String feed) {
		this.cacheTime = System.currentTimeMillis();
		this.cachedFeed = feed;
	}
	
	public String getFeed() {
		return this.cachedFeed;
	}

	public long getCacheAge() {
		return System.currentTimeMillis() - this.cacheTime;
	}
	
	private final long cacheTime;
	
	private final String cachedFeed;

}
