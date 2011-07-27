package com.gtwm.pb.model.manageData;

import org.json.JSONString;

/**
 * Allow outputting of raw Javascript Date objects using JSON.
 * 
 * JSON doesn't technically have a date type but JSON interpreted by JS, e.g.
 * Simile Timeplot will recognise JS dates and parse the JSON quicker
 * 
 * @see http
 *      ://www.simile-widgets.org/wiki/Timeline_EventSources#Date_Time_Formats
 */
public class JSONDate implements JSONString {

	/**
	 * @param milliseconds
	 *            Milliseconds since the epoch
	 */
	public JSONDate(long milliseconds) {
		this.milliseconds = milliseconds;
	}

	public String toJSONString() {
		return "new Date(" + this.milliseconds + ")";
	}

	private final long milliseconds;
}
