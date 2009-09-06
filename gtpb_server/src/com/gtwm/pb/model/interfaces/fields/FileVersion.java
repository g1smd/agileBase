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
package com.gtwm.pb.model.interfaces.fields;

import java.util.Calendar;

/**
 * portalBase keeps a version history of files uploaded into a file field for a
 * particular record.
 * 
 * This interface represents one file version, containing the important
 * information for display and to allow access to it.
 * 
 * @see FileField Used by FileField
 */
public interface FileVersion {

	/**
	 * Return the URL for the file that will let it be downloaded
	 */
	public String getFileName();
	
	/**
	 * Return the date that this file was either uploaded or replaced by a file with the same name
	 */
	public Calendar getLastModified(); 

}
