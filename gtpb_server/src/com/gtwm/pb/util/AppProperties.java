/*
 *  Copyright 2009 GT webMarque Ltd
 * 
 *  This file is part of GT portalBase.
 *
 *  GT portalBase is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  GT portalBase is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with GT portalBase.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gtwm.pb.util;

/**
 * Application constants such as title and version
 */
public class AppProperties {
	public static final String applicationName = "GT portalBase";

	public static final String applicationVersion = "2.3.2";

	/**
	 * Location of the Velocity template used to report template processing
	 * errors
	 */
	public static final String errorTemplateLocation = "report_error.vm";

	/**
	 * The number of seconds that is considered a long time to process a
	 * request. portalBase will log any server-side or template processing that
	 * exceeds this time.
	 */
	public static final float longProcessingTime = 3f;

	/**
	 * The number of seconds that is considered a long time for a SQL view
	 * SELECT statement to run (i.e. before it starts having a noticeable effect
	 * to users). Statements taking longer will be logged;
	 */
	public static final float longSqlTime = 0.2f;

	/**
	 * Enable startTimer and stopTimer in ViewTools
	 * 
	 * @see com.gtwm.pb.model.interfaces.ViewToolsInfo#startTimer(String)
	 */
	public static final boolean enableTemplateTimers = false;

	/**
	 * If false, the app will be optimised for correctness in some cases and
	 * will do extra work, e.g. methods returning collections will return copy
	 * collections in an unmodifiable wrapper.
	 * 
	 * If true, this work won't be done and in the case above, the original
	 * 'raw' collections will be returned, saving a small amount of memory and
	 * CPU
	 * 
	 * The app should be run with this setting false at least periodically to
	 * check that calling methods aren't doing anything improper
	 */
	public static final boolean optimiseForPerformance = true;

	/**
	 * Used when getting items for a lookup field - how long to cache items for, in milliseconds
	 */
	public static final int lookupCacheTime = 1000 * 10;
	
	/**
	 * Monthly price per table
	 */
	public static final int tableCost = 10;
	
	/**
	 * Price per table for every table above the discount level
	 */
	public static final int discountTableCost = 5;
	
	/**
	 * Number of tables a company must have before discounting kicks in
	 */
	public static final int numFullPriceTables = 10;
}
