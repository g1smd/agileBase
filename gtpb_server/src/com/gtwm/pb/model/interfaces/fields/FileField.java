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

import java.util.SortedSet;

/**
 * For storing uploaded files
 */
public interface FileField extends BaseField {

	/**
	 * Return a set of all the files that have ever been uploaded into this
	 * field in a particular record except the current version
	 * 
	 * @param rowId
	 *            Identify the record
	 * 
	 * @param webAppRoot
	 *            The serverside root of the application, which the method needs
	 *            to know to examine the files. Can be found with
	 *            ViewTools.getWebAppRoot()
	 *            
	 *            @see com.gtwm.pb.model.interfaces.ViewTools#getWebAppRoot()
	 */
	public SortedSet<FileVersion> getPreviousFileVersions(String webAppRoot, int rowId);
}
