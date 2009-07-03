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

/**
 * Stores the filename of an uploaded file
 */
public interface FileValue extends BaseValue {

	/**
	 * Return the filename of the uploaded file
	 */
	public String getFilename();

	/**
	 * Returns the name of an icon, based on the file extension, for known file
	 * types. For unknown types, returns 'unknown'. Note only a very small
	 * number of types are currently known. For known types, a file iconname.png
	 * must exist in the resources/icons/filetypes folder
	 */
	public String getIconName();
	
	/**
	 * Returns whether the file is an image that can be displayed in the browser, e.g. a JPEG or PNG
	 */
	public boolean isImage();
}
