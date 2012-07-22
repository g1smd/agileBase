package com.gtwm.pb.model.manageSchema.fields.options;

public class RelationFieldOptions extends BasicFieldOptions {

	public boolean isDefaultToNull() {
		return defaultToNull;
	}

	public void setDefaultToNull(boolean defaultToNull) {
		this.defaultToNull = defaultToNull;
	}
	
	private boolean defaultToNull = true;

}
