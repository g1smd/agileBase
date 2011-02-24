package com.gtwm.pb.model.manageData;

import com.gtwm.pb.model.interfaces.CachedJSONInfo;

public class CachedJSON implements CachedJSONInfo {

	private CachedJSON() {
		this.cacheTime = 0;
		this.cachedJSON = null;
	}
	
	public CachedJSON(String json) {
		this.cacheTime = System.currentTimeMillis();
		this.cachedJSON = json;
	}
	
	public String getJSON() {
		return this.cachedJSON;
	}

	public long getCacheTime() {
		return this.cacheTime;
	}
	
	private final long cacheTime;
	
	private final String cachedJSON;

}
