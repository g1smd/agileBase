/*
 *  Copyright 2010 GT webMarque Ltd
 * 
 *  This file is part of agileBase.
 *
 *  agileBase is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  agileBase is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with agileBase.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gtwm.pb.model.manageData.fields;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.commons.fileupload.FileUploadException;
import org.grlea.log.SimpleLogger;
import com.gtwm.pb.model.interfaces.fields.FileValue;
import com.gtwm.pb.model.interfaces.fields.FileField;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.ObjectNotFoundException;

public class FileValueDefn implements FileValue {

	private FileValueDefn() {
	}

	/**
	 * Construct a file value object from a known filename
	 */
	public FileValueDefn(String filename) {
		this.filename = filename;
	}

	/**
	 * Construct a file value reading the uploaded file name for a particlular
	 * file field from the HTTP request
	 */
	public FileValueDefn(HttpServletRequest request, FileField fileField,
			List<FileItem> multipartItems) throws CantDoThatException, FileUploadException,
			ObjectNotFoundException {
		if (!FileUpload.isMultipartContent(new ServletRequestContext(request))) {
			throw new CantDoThatException(
					"To upload a file, the form must be posted as multi-part form data");
		}
		String internalFieldName = fileField.getInternalFieldName();
		ITEMLOOP: for (FileItem item : multipartItems) {
			// if item is a file
			if (!item.isFormField()) {
				if (item.getFieldName().equals(internalFieldName)) {
					this.filename = item.getName().replaceAll("^.*\\\\", "");
					break ITEMLOOP;
				}
			}
		}
		if (this.filename == null) {
			throw new ObjectNotFoundException("The file field " + fileField
					+ "wasn't found in the user input");
		}
	}

	public String getFilename() {
		if (this.isNull()) {
			return "";
		} else {
			return this.filename;
		}
	}

	public boolean isNull() {
		return (this.filename == null);
	}

	public String getIconName() {
		Set<String> knownFilenames = new HashSet<String>();
		knownFilenames.add("csv");
		knownFilenames.add("doc");
		knownFilenames.add("pdf");
		knownFilenames.add("xls");
		String filename = this.getFilename();
		if (filename.contains(".")) {
			String extension = filename.replaceAll(".*\\.", "").toLowerCase();
			if (knownFilenames.contains(extension)) {
				return extension;
			} else {
				logger.warn("No icon for filetype " + extension);
				return "unknown";
			}
		}
		return "";
	}

	public boolean isImage() {
		String extension = this.filename.replaceAll("^.*\\.", "").toLowerCase();
		if (extension.equals("jpg") || extension.equals("png") || extension.equals("gif")
				|| extension.equals("jpeg")) {
			return true;
		}
		return false;
	}

	public String toString() {
		if (this.isNull()) {
			return "";
		} else {
			return this.filename;
		}
	}

	private String filename = null;

	private static final SimpleLogger logger = new SimpleLogger(FileValueDefn.class);
}
