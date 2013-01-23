package com.gtwm.pb.model.manageData;

import java.util.Calendar;

import com.gtwm.pb.model.interfaces.CommentInfo;

public class Comment implements CommentInfo {

	public Comment(String internalFieldName, int rowId, String author, String authorInternalName, Calendar timestamp,
			String text) {
		this.internalFieldName = internalFieldName;
		this.rowId = rowId;
		this.author = author;
		this.authorInternalName = authorInternalName;
		this.timestamp = timestamp;
		this.text = text;
	}

	public String getText() {
		return this.text;
	}

	public Calendar getTimestamp() {
		return this.timestamp;
	}

	public String getTimestampString() {
		long age = System.currentTimeMillis() - this.timestamp.getTimeInMillis();
		if (age > (1000 * 60 * 60 * 24 * 365)) {
			return String.format("%1$td %1$tb %1$tY", this.timestamp);
//		} else if (age > (1000 * 60 * 60 * 24 * 7)) {
//			return String.format("%1$td %1$tb", this.timestamp);
		} else {
			return String.format("%1$td %1$tb at %1$tH:%1$tM", this.timestamp);
		}
	}

	public String getAuthor() {
		return this.author;
	}
	
	public String getAuthorInternalName() {
		return this.authorInternalName;
	}

	public String getInternalFieldName() {
		return this.internalFieldName;
	}

	public int getRowId() {
		return this.rowId;
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		CommentInfo otherComment = (CommentInfo) obj;
		return ((this.rowId == otherComment.getRowId())
				&& (this.internalFieldName.equals(otherComment.getInternalFieldName()))
				&& (this.author.equals(otherComment.getAuthor())) && (this.timestamp
				.equals(otherComment.getTimestamp())));
	}

	public int hashCode() {
		if (this.hashCode == 0) {
			int hashCode = 17;
			hashCode = hashCode + 37 * hashCode + this.rowId;
			hashCode = hashCode + 37 * hashCode + this.internalFieldName.hashCode();
			hashCode = hashCode + 37 * hashCode + this.author.hashCode();
			hashCode = hashCode + 37 * hashCode + this.timestamp.hashCode();
		}
		return this.hashCode;
	}

	/**
	 * Sort comments in descending time order, newest first
	 */
	public int compareTo(CommentInfo otherComment) {
		int timestampCompare = otherComment.getTimestamp().compareTo(this.timestamp);
		if (timestampCompare != 0) {
			return timestampCompare;
		}
		int rowIdCompare = Integer.valueOf(rowId).compareTo(otherComment.getRowId());
		if (rowIdCompare != 0) {
			return rowIdCompare;
		}
		int internalFieldNameCompare = this.internalFieldName.compareTo(otherComment
				.getInternalFieldName());
		if (internalFieldNameCompare != 0) {
			return internalFieldNameCompare;
		}
		return this.author.compareTo(otherComment.getAuthor());
	}

	public String toString() {
		return this.text + " - " + this.author + ", " + this.getTimestampString();
	}

	private final String author;
	
	private final String authorInternalName;

	private final String text;

	private final Calendar timestamp;

	private final String internalFieldName;

	private final int rowId;

	private volatile int hashCode = 0;
}
