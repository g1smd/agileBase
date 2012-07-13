package com.gtwm.pb.model.interfaces;

import java.util.Calendar;

/**
 * A comment attached to a field (usually a text field)
 */
public interface CommentInfo extends Comparable<CommentInfo> {
	
	public String getText();
	
	public Calendar getTimestamp();
	
	/**
	 * Return a printable string representation of the timestamp
	 */
	public String getTimestampString();
	
	public String getAuthor();
	
	/**
	 * Return the identifier for the field this comment is about
	 */
	public String getInternalFieldName();
	
	/**
	 * Return the id of the record we're commenting on
	 */
	public int getRowId();

}
