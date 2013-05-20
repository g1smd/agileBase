package com.gtwm.pb.model.manageSchema.fields.options;

public class RelationFieldOptions extends BasicFieldOptions {

	public boolean isDefaultToNull() {
		return defaultToNull;
	}

	public void setDefaultToNull(boolean defaultToNull) {
		this.defaultToNull = defaultToNull;
	}

	public boolean isOneToOne() {
		return oneToOne;
	}

	public void setOneToOne(boolean oneToOne) {
		this.oneToOne = oneToOne;
	}

	private boolean defaultToNull = true;

	private boolean oneToOne = false;

}
