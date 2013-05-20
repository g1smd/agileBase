package com.gtwm.pb.model.manageSchema.fields.options;

public class DecimalFieldOptions extends BasicFieldOptions {

	public Double getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(Double defaultValue) {
		this.defaultValue = defaultValue;
	}

	public Integer getPrecision() {
		return precision;
	}

	public void setPrecision(Integer precision) {
		this.precision = precision;
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

	public Double getNotApplicableValue() {
		return notApplicableValue;
	}

	public void setNotApplicableValue(Double notApplicableValue) {
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

	private Double defaultValue = null;

	private Integer precision = null;

	private boolean notApplicable = false;

	private String notApplicableDescription = null;

	private Double notApplicableValue = null;

	private boolean usesLookup = false;

	private boolean storesCurrency = false;

}
