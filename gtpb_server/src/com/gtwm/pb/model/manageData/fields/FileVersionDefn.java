package com.gtwm.pb.model.manageData.fields;

import java.util.Calendar;

import com.gtwm.pb.model.interfaces.fields.FileVersion;

public class FileVersionDefn implements FileVersion, Comparable<FileVersion> {

	private FileVersionDefn() {
	}
	
	public FileVersionDefn(String fileName, Calendar lastModified) {
		this.fileName = fileName;
		this.lastModified = lastModified;
	}
	
	public String getFileName() {
		return this.fileName;
	}

	public Calendar getLastModified() {
		return this.lastModified;
	}
	
	/**
	 * equals is based on fileUrl
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		FileVersionDefn otherFileVersion = (FileVersionDefn) obj;
		if (!otherFileVersion.getFileName().equals(this.getFileName())) {
			return false;
		}
		return (this.getFileName()).equals(otherFileVersion.getFileName());
	}
	
	public int hashCode() {
		return this.getFileName().hashCode();
	}
	
	/**
	 * Compare firstly based on file modification time
	 */
	public int compareTo(FileVersion otherFileVersion) {
		if (this == otherFileVersion) {
			return 0;
		}
		int timestampCompare = otherFileVersion.getLastModified().compareTo(this.getLastModified());
		if (timestampCompare != 0) {
			return timestampCompare;
		}
		return otherFileVersion.getFileName().compareTo(otherFileVersion.getFileName());
	}

	public String toString() {
		return this.fileName;
	}
	
	private String fileName;
	
	private Calendar lastModified;

}
