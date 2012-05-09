package com.gtwm.pb.model.manageUsage;

import com.gtwm.pb.model.interfaces.AppUserInfo;
import com.gtwm.pb.model.interfaces.DataLogEntryInfo;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.util.Enumerations.AppAction;

public class DataLogEntry implements DataLogEntryInfo {

	public DataLogEntry(AppUserInfo user, BaseField field, int rowId, String value, AppAction appAction) {
		this.time = System.currentTimeMillis();
		this.user = user;
		this.field = field;
		this.rowId = rowId;
		this.value = value;
		this.appAction = appAction;
	}

	public long getTime() {
		return this.time;
	}

	public AppUserInfo getUser() {
		return this.user;
	}
	
	public BaseField getField() {
		return this.field;
	}

	public int getRowId() {
		return this.rowId;
	}

	public String getValue() {
		return this.value;
	}

	public AppAction getAppAction() {
		return this.appAction;
	}

	public String toString() {
		return this.appAction.toString() + "log for " + this.user + ": " + this.field + " " + this.rowId + " = " + this.value;
	}
	
	/**
	 * Equality on all properties
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		DataLogEntry otherEntry = (DataLogEntry) obj;
		if ((this.time != otherEntry.getTime()) || (!this.user.equals(otherEntry.getUser())) || (!this.field.equals(otherEntry.getField()))
				|| (this.rowId != otherEntry.getRowId())
				|| (!this.value.equals(otherEntry.getValue())) || (!this.appAction.equals(otherEntry.getAppAction()))) {
			return false;
		}
		return true;
	}
	
	public int hashCode() {
		if (this.hashCode == 0) {
			int hashCode = 17;
			hashCode = 37 * hashCode + Long.valueOf(this.time).hashCode();
			hashCode = 37 * hashCode + this.user.hashCode();
			hashCode = 37 * hashCode + this.field.hashCode();
			hashCode = 37 * hashCode + this.rowId;
			hashCode = 37 * hashCode + this.value.hashCode();
			hashCode = 37 * hashCode + this.appAction.hashCode();
			this.hashCode = hashCode;
		}
		return this.hashCode;
	}

	private final long time;
	
	private final AppUserInfo user;

	private final BaseField field;

	private final int rowId;

	private final String value;

	private final AppAction appAction;

	private volatile int hashCode = 0;
}
