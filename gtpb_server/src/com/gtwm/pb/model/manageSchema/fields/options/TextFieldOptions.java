package com.gtwm.pb.model.manageSchema.fields.options;

import com.gtwm.pb.util.Enumerations.TextCase;

public class TextFieldOptions extends BasicFieldOptions {

	public String getDefaultValue() {
		return this.defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public boolean isNotApplicable() {
		return this.notApplicable;
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
		return this.notApplicableValue;
	}

	public void setNotApplicableValue(String notApplicableValue) {
		this.notApplicableValue = notApplicableValue;
	}

	public boolean isUsesLookup() {
		return this.usesLookup;
	}

	public void setUsesLookup(boolean usesLookup) {
		this.usesLookup = usesLookup;
	}

	public boolean isUsesTags() {
		return this.usesTags;
	}

	public void setUsesTags(boolean usesTags) {
		this.usesTags = usesTags;
	}

	public boolean isTieDownLookup() {
		return tieDownLookup;
	}

	public void setTieDownLookup(boolean tieDownLookup) {
		this.tieDownLookup = tieDownLookup;
	}

	public Integer getTextContentSize() {
		return this.textContentSize;
	}

	/**
	 * @see com.gtwm.pb.model.manageSchema.ListFieldDescriptorOption.TextContentSizes
	 */
	public void setTextContentSize(Integer textContentSize) {
		this.textContentSize = textContentSize;
	}

	public TextCase getTextCase() {
		return this.textCase;
	}

	public void setTextCase(TextCase textCase) {
		this.textCase = textCase;
	}

	private String defaultValue = null;

	private boolean notApplicable = false;

	private String notApplicableDescription = null;

	private String notApplicableValue = null;

	private boolean usesLookup = false;

	private boolean usesTags = false;

	private boolean tieDownLookup = false;

	private TextCase textCase = null;

	private Integer textContentSize = null;

}
