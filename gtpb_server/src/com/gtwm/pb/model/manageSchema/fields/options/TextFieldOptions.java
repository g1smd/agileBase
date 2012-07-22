package com.gtwm.pb.model.manageSchema.fields.options;

public class TextFieldOptions extends BasicFieldOptions {
	
	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
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

	public String getNotApplicableValue() {
		return notApplicableValue;
	}

	public void setNotApplicableValue(String notApplicableValue) {
		this.notApplicableValue = notApplicableValue;
	}

	public boolean isUsesLookup() {
		return usesLookup;
	}

	public void setUsesLookup(boolean usesLookup) {
		this.usesLookup = usesLookup;
	}

	public boolean isTieDownLookup() {
		return tieDownLookup;
	}

	public void setTieDownLookup(boolean tieDownLookup) {
		this.tieDownLookup = tieDownLookup;
	}

	private String defaultValue = null;
	
	private boolean notApplicable = false;
	
	private String notApplicableDescription = null;
	
	private String notApplicableValue = null;
	
	private boolean usesLookup = false;
	
	private boolean tieDownLookup = false;

}
