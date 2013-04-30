package com.gtwm.pb.model.manageData;

import java.util.Calendar;

import com.gtwm.pb.model.interfaces.CommentInfo;
import com.gtwm.pb.model.interfaces.TableInfo;

public class Comment implements CommentInfo {

	/**
	 * @param table	 Optional parameter, can be set if it's needed otherwise supply null
	 */
	public Comment(int commentId, String internalFieldName, int rowId, String author,
			String authorInternalName, Calendar timestamp, String text, TableInfo table) {
		this.commentId = commentId;
		this.internalFieldName = internalFieldName;
		this.rowId = rowId;
		this.author = author;
		this.authorInternalName = authorInternalName;
		this.timestamp = timestamp;
		this.text = text;
		this.table = table;
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
			// } else if (age > (1000 * 60 * 60 * 24 * 7)) {
			// return String.format("%1$td %1$tb", this.timestamp);
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

	public int getCommentId() {
		return this.commentId;
	}

	public int getRowId() {
		return this.rowId;
	}
	
	public TableInfo getTable() {
		return this.table;
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		CommentInfo otherComment = (CommentInfo) obj;
		return ((this.commentId == otherComment.getCommentId()) && (this.getInternalFieldName()
				.equals(otherComment.getInternalFieldName())));
	}

	public int hashCode() {
		if (this.hashCode == 0) {
			int hashCode = 17;
			hashCode = 37 * hashCode + this.getInternalFieldName().hashCode();
			hashCode = 37 * hashCode + this.commentId;
			this.hashCode = hashCode;
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
		int internalFieldNameCompare = this.internalFieldName.compareTo(otherComment
				.getInternalFieldName());
		if (internalFieldNameCompare != 0) {
			return internalFieldNameCompare;
		}
		return Integer.compare(this.commentId, otherComment.getCommentId());
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

	private final int commentId;
	
	private final TableInfo table;
	
	private volatile int hashCode = 0;
}
