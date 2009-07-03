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
package com.gtwm.pb.model.interfaces;

import edu.umd.cs.treemap.Mappable;

/**
 * Represents an individual box in a treemap corresponding with one record
 * from a report. An extension of edu.umd.cs.treemap.Mappable, adding some extra
 * properties to deal with caption and colour
 */
public interface MappableReportDataInfo extends Mappable {
	
	/**
	 * Return the HTML colour code that this rectangle should be coloured
	 */
	public String getHexColour();

	/**
	 * Return the text explaining this rectangle
	 */
	public String getCaption();
	
	/**
	 * Make things easier for templating languages by returning each bound separately as an integer
	 */
	public int getWidth();
	
	/**
	 * Make things easier for templating languages by returning each bound separately as an integer
	 */
	public int getHeight();
	
	/**
	 * Make things easier for templating languages by returning each bound separately as an integer
	 */
	public int getLeft();
	
	/**
	 * Make things easier for templating languages by returning each bound separately as an integer
	 */
	public int getTop();
}
