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

	/**
	 * Get comment author's forename and surname
	 */
	public String getAuthor();

	/**
	 * Get the author's internal user name
	 */
	public String getAuthorInternalName();

	/**
	 * Return the identifier for the field this comment is about
	 */
	public String getInternalFieldName();

	/**
	 * Return the table that this comment is in, if one has been set.
	 *
	 * Note this is only set in some cases, it can't be guaranteed that the table will be known. If unknown, null is returned
	 */
	public TableInfo getTable();

	/**
	 * Return the id of the record we're commenting on
	 */
	public int getRowId();

	public int getCommentId();

}
