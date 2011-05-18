/*
 *  Copyright 2011 GT webMarque Ltd
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
package com.gtwm.pb.model.manageSchema.fields;

import java.io.File;
import java.util.Calendar;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.persistence.Entity;
import javax.persistence.Transient;
import org.grlea.log.SimpleLogger;
import com.gtwm.pb.model.interfaces.FieldTypeDescriptorInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.fields.FileField;
import com.gtwm.pb.model.interfaces.fields.FileVersion;
import com.gtwm.pb.model.manageData.fields.FileValueDefn;
import com.gtwm.pb.model.manageData.fields.FileVersionDefn;
import com.gtwm.pb.model.manageSchema.FieldTypeDescriptor;
import com.gtwm.pb.model.manageSchema.FieldTypeDescriptor.FieldCategory;
import com.gtwm.pb.model.manageSchema.ListFieldDescriptorOption.FieldPrintoutSetting;
import com.gtwm.pb.model.manageSchema.ListFieldDescriptorOption.PossibleListOptions;
import com.gtwm.pb.util.Enumerations.DatabaseFieldType;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.CodingErrorException;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.util.RandomString;

@Entity
public class FileFieldDefn extends AbstractField implements FileField {

	protected FileFieldDefn() {
	}

	public FileFieldDefn(TableInfo tableContainingField, String internalFieldName,
			String fieldName, String fieldDesc) throws CodingErrorException {
		super.setTableContainingField(tableContainingField);
		if (internalFieldName == null) {
			super.setInternalFieldName((new RandomString()).toString());
		} else {
			super.setInternalFieldName(internalFieldName);
		}
		super.setFieldName(fieldName);
		super.setFieldDescription(fieldDesc);
		try {
			super.setUnique(false);
			super.setNotNull(false);
		} catch (CantDoThatException cdtex) {
			throw new CodingErrorException("Error setting file field not unique or nullable", cdtex);
		}
	}

	public SortedSet<FileVersion> getPreviousFileVersions(String webAppRoot, int rowId,
			String currentFileName) {
		SortedSet<FileVersion> fileVersions = new TreeSet<FileVersion>();
		String uploadFolderName = webAppRoot + "uploads/"
				+ this.getTableContainingField().getInternalTableName() + "/"
				+ this.getInternalFieldName() + "/" + rowId;
		File uploadFolder = new File(uploadFolderName);
		if (!uploadFolder.exists()) {
			// If folder isn't there, there have probably never been any files
			// uploaded for this record
			return fileVersions;
		}
		boolean isImage = (new FileValueDefn(currentFileName)).isImage();
		File[] fileArray = uploadFolder.listFiles();
		FILE_LIST: for (File file : fileArray) {
			String fileName = file.getName();
			if (!fileName.equals(currentFileName)) {
				// Ignore thumbnails
				if (isImage && (fileName.contains(".40.")) || (fileName.contains(".500."))) {
					continue FILE_LIST;
				}
				long lastModified = file.lastModified();
				Calendar lastModifiedCal = Calendar.getInstance();
				lastModifiedCal.setTimeInMillis(lastModified);
				FileVersion fileVersion = new FileVersionDefn(fileName, lastModifiedCal);
				fileVersions.add(fileVersion);
			}
		}
		return fileVersions;
	}

	@Transient
	public FieldCategory getFieldCategory() {
		return FieldCategory.FILE;
	}

	/**
	 * File fields will return VARCHAR, as files aren't currently stored
	 * directly in the database, rather a filename is stored as a link to each
	 * actual file
	 */
	@Transient
	public DatabaseFieldType getDbType() {
		return DatabaseFieldType.VARCHAR;
	}

	@Transient
	public FieldTypeDescriptorInfo getFieldDescriptor() throws CantDoThatException {
		FieldTypeDescriptor fieldDescriptor = new FieldTypeDescriptor(FieldCategory.FILE);
		FieldPrintoutSetting printoutSetting = this.getPrintoutSetting();
		try {
			fieldDescriptor.setListOptionSelectedItem(PossibleListOptions.PRINTFORMAT,
					printoutSetting.name());
		} catch (ObjectNotFoundException onfex) {
			throw new CantDoThatException("Internal error setting up " + this.getClass()
					+ " field descriptor", onfex);
		}
		return fieldDescriptor;
	}

	private static final SimpleLogger logger = new SimpleLogger(FileFieldDefn.class);

}
