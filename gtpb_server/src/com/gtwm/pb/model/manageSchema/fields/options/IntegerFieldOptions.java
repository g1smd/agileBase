package com.gtwm.pb.model.manageSchema.fields.options;

public class IntegerFieldOptions extends BasicFieldOptions {

	public Integer getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(Integer defaultValue) {
		this.defaultValue = defaultValue;
	}

	public boolean isNotApplicable() {
		return notApplicable;
	}

	public void setNotApplicable(boolean notApplicable) {
		this.notApplicable = notApplicable;
	}

	public String getNotApplicableDescription() {
		return notApplicableDescription;
	}

	public void setNotApplicableDescription(String notApplicableDescription) {
		this.notApplicableDescription = notApplicableDescription;
	}

	public Integer getNotApplicableValue() {
		return notApplicableValue;
	}

	public void setNotApplicableValue(Integer notApplicableValue) {
		this.notApplicableValue = notApplicableValue;
	}

	public boolean isUsesLookup() {
		return usesLookup;
	}

	public void setUsesLookup(boolean usesLookup) {
		this.usesLookup = usesLookup;
	}

	public boolean isStoresCurrency() {
		return storesCurrency;
	}

	public void setStoresCurrency(boolean storesCurrency) {
		this.storesCurrency = storesCurrency;
	}

	private Integer defaultValue = null;

	private boolean notApplicable = false;

	private String notApplicableDescription = null;

	private Integer notApplicableValue = null;

	private boolean usesLookup  = false;

	private boolean storesCurrency = false;

}
