package com.gtwm.pb.model.manageSchema.fields.options;

import com.gtwm.pb.model.manageSchema.ListFieldDescriptorOption.FieldPrintoutSetting;

/**
 * Store field options so they can be passed easily between methods, reducing
 * the number of method parameters necessary
 * 
 * Options for different type of fields will extend this class. This is a really
 * simple set of classes, we don't need fancy stuff like interfaces or abstract
 * classes
 */
public class BasicFieldOptions {

	public void setNotNull(boolean notNull) {
		this.notNull = notNull;
	}

	public boolean getNotNull() {
		return this.notNull;
	}

	public boolean getUnique() {
		return this.unique;
	}

	public void setUnique(boolean unique) {
		this.unique = unique;
	}

	public FieldPrintoutSetting getPrintoutSetting() {
		return this.printoutSetting;
	}

	public void setPrintoutSetting(FieldPrintoutSetting printoutSetting) {
		this.printoutSetting = printoutSetting;
	}

	private boolean notNull = false;

	private boolean unique = false;

	private FieldPrintoutSetting printoutSetting = FieldPrintoutSetting.NAME_AND_VALUE;

}
