/*
 *  Copyright 2012 GT webMarque Ltd
 *
 *  This file is part of agileBase.
 *
 *  agileBase is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  agileBase is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with agileBase.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gtwm.pb.model.manageSchema;

import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import org.grlea.log.SimpleLogger;
import com.gtwm.pb.model.interfaces.BaseFieldDescriptorOptionInfo;
import com.gtwm.pb.model.interfaces.BooleanFieldDescriptorOptionInfo;
import com.gtwm.pb.model.interfaces.ListFieldDescriptorOptionInfo;
import com.gtwm.pb.model.interfaces.TextFieldDescriptorOptionInfo;
import com.gtwm.pb.model.interfaces.FieldTypeDescriptorInfo;
import com.gtwm.pb.model.manageSchema.BooleanFieldDescriptorOption.PossibleBooleanOptions;
import com.gtwm.pb.model.manageSchema.ListFieldDescriptorOption.FieldPrintoutSetting;
import com.gtwm.pb.model.manageSchema.ListFieldDescriptorOption.PossibleListOptions;
import com.gtwm.pb.model.manageSchema.TextFieldDescriptorOption.PossibleTextOptions;
import com.gtwm.pb.util.ObjectNotFoundException;

public class FieldTypeDescriptor implements FieldTypeDescriptorInfo {

	/**
	 * When an instance of a FieldTypeDescriptor is created, the class is filled
	 * with the appropriate options so that all getter methods work without
	 * further work
	 */
	public FieldTypeDescriptor(FieldCategory fieldType) throws ObjectNotFoundException {
		this.fieldCategory = fieldType;
		switch (fieldType) {
		case TEXT:
			this.options.add(new BooleanFieldDescriptorOption(PossibleBooleanOptions.UNIQUE));
			this.options.add(new BooleanFieldDescriptorOption(PossibleBooleanOptions.MANDATORY));
			this.options.add(new BooleanFieldDescriptorOption(PossibleBooleanOptions.USELOOKUP));
			this.options.add(new BooleanFieldDescriptorOption(PossibleBooleanOptions.USETAGS));
			this.options.add(new TextFieldDescriptorOption(PossibleTextOptions.DEFAULTVALUE));
			this.options.add(new BooleanFieldDescriptorOption(PossibleBooleanOptions.TIEDOWNLOOKUP));
			this.options.add(new ListFieldDescriptorOption(PossibleListOptions.TEXTCONTENTSIZE));
			this.options.add(new ListFieldDescriptorOption(PossibleListOptions.TEXTCASE));
			this.options.add(new ListFieldDescriptorOption(PossibleListOptions.PRINTFORMAT));
			// Defaults for new fields
			this.setListOptionSelectedItem(PossibleListOptions.PRINTFORMAT,
					FieldPrintoutSetting.VALUE_ONLY.name());
			break;
		case NUMBER:
			this.options.add(new BooleanFieldDescriptorOption(PossibleBooleanOptions.UNIQUE));
			this.options.add(new BooleanFieldDescriptorOption(PossibleBooleanOptions.MANDATORY));
			this.options.add(new BooleanFieldDescriptorOption(PossibleBooleanOptions.USELOOKUP));
			this.options
					.add(new BooleanFieldDescriptorOption(PossibleBooleanOptions.STORECURRENCY));
			this.options.add(new ListFieldDescriptorOption(PossibleListOptions.NUMBERPRECISION));
			this.options.add(new TextFieldDescriptorOption(PossibleTextOptions.DEFAULTVALUE));
			this.options.add(new ListFieldDescriptorOption(PossibleListOptions.PRINTFORMAT));
			break;
		case DATE:
			this.options.add(new ListFieldDescriptorOption(PossibleListOptions.DATERESOLUTION));
			this.options.add(new BooleanFieldDescriptorOption(PossibleBooleanOptions.DEFAULTTONOW));
			this.options.add(new BooleanFieldDescriptorOption(PossibleBooleanOptions.MANDATORY));
			this.options.add(new ListFieldDescriptorOption(PossibleListOptions.PRINTFORMAT));
			this.options.add(new TextFieldDescriptorOption(PossibleTextOptions.MINYEARS));
			this.options.add(new TextFieldDescriptorOption(PossibleTextOptions.MAXYEARS));
			break;
		case DURATION:
			this.options.add(new ListFieldDescriptorOption(PossibleListOptions.DURATIONRESOLUTION));
			this.options.add(new ListFieldDescriptorOption(PossibleListOptions.DURATIONSCALE));
			break;
		case RELATION:
			this.options.add(new ListFieldDescriptorOption(PossibleListOptions.LISTTABLE));
			this.options.add(new ListFieldDescriptorOption(PossibleListOptions.LISTVALUEFIELD));
			this.options.add(new ListFieldDescriptorOption(PossibleListOptions.LISTSECONDARYFIELD));
			this.options.add(new BooleanFieldDescriptorOption(PossibleBooleanOptions.MANDATORY));
			this.options.add(new BooleanFieldDescriptorOption(PossibleBooleanOptions.ONETOONE));
			this.options
					.add(new BooleanFieldDescriptorOption(PossibleBooleanOptions.DEFAULTTONULL));
			this.options.add(new ListFieldDescriptorOption(PossibleListOptions.PRINTFORMAT));
			this.setListOptionSelectedItem(PossibleListOptions.PRINTFORMAT,
					FieldPrintoutSetting.VALUE_ONLY.name());
			break;
		case CHECKBOX:
			this.options.add(new ListFieldDescriptorOption(PossibleListOptions.CHECKBOXDEFAULT));
			this.options.add(new ListFieldDescriptorOption(PossibleListOptions.PRINTFORMAT));
			break;
		case SEQUENCE:
			this.options.add(new ListFieldDescriptorOption(PossibleListOptions.PRINTFORMAT));
			break;
		case FILE:
			this.options.add(new ListFieldDescriptorOption(PossibleListOptions.ATTACHMENTTYPE));
			this.options.add(new ListFieldDescriptorOption(PossibleListOptions.PRINTFORMAT));
			this.setListOptionSelectedItem(PossibleListOptions.PRINTFORMAT,
					FieldPrintoutSetting.VALUE_ONLY.name());
			break;
		case REFERENCED_REPORT_DATA:
			this.options.add(new ListFieldDescriptorOption(PossibleListOptions.LISTTABLE));
			this.options.add(new ListFieldDescriptorOption(PossibleListOptions.LISTREPORT));
			this.options.add(new ListFieldDescriptorOption(PossibleListOptions.PRINTFORMAT));
			break;
		case SEPARATOR:
		case COMMENT_FEED:
			// No options for these types
			break;
		}
	}

	public FieldCategory getFieldCategory() {
		return this.fieldCategory;
	}

	public List<BaseFieldDescriptorOptionInfo> getOptions() {
		return this.options;
	}

	public void setBooleanOptionState(PossibleBooleanOptions booleanOption, boolean state)
			throws ObjectNotFoundException {
		for (BaseFieldDescriptorOptionInfo option : this.options) {
			if (option instanceof BooleanFieldDescriptorOptionInfo) {
				BooleanFieldDescriptorOptionInfo foundOption = (BooleanFieldDescriptorOptionInfo) option;
				if (foundOption.getOptionDescription().equals(booleanOption.getOptionDescription())) {
					foundOption.setOptionState(state);
					return;
				}
			}
		}
		throw new ObjectNotFoundException("The field type descriptor '"
				+ this.fieldCategory.toString() + "' doesn't contain the boolean option "
				+ booleanOption.toString());
	}

	public void setTextOptionValue(PossibleTextOptions textOption, String value)
			throws ObjectNotFoundException {
		for (BaseFieldDescriptorOptionInfo option : this.options) {
			if (option instanceof TextFieldDescriptorOptionInfo) {
				TextFieldDescriptorOptionInfo foundOption = (TextFieldDescriptorOptionInfo) option;
				if (foundOption.getOptionDescription().equals(textOption.getOptionDescription())) {
					foundOption.setValue(value);
					return;
				}
			}
		}
		throw new ObjectNotFoundException("The field type descriptor '"
				+ this.fieldCategory.toString() + "' doesn't contain the boolean option "
				+ textOption.toString());
	}

	public void setListOptionSelectedItem(PossibleListOptions listOption, String selectedItemKey)
			throws ObjectNotFoundException {
		for (BaseFieldDescriptorOptionInfo option : this.options) {
			if (option instanceof ListFieldDescriptorOptionInfo) {
				ListFieldDescriptorOptionInfo foundOption = (ListFieldDescriptorOptionInfo) option;
				if (foundOption.getOptionDescription().equals(listOption.getOptionDescription())) {
					foundOption.setSelectedItem(selectedItemKey);
					return;
				}
			}
		}
		throw new ObjectNotFoundException("The field type descriptor '"
				+ this.fieldCategory.toString() + "' doesn't contain the list option "
				+ listOption.toString());
	}

	public void setListOptionSelectedItem(PossibleListOptions listOption, String itemKey,
			String itemValue) throws ObjectNotFoundException {
		for (BaseFieldDescriptorOptionInfo option : this.options) {
			if (option instanceof ListFieldDescriptorOptionInfo) {
				ListFieldDescriptorOptionInfo foundOption = (ListFieldDescriptorOptionInfo) option;
				if (foundOption.getOptionDescription().equals(listOption.getOptionDescription())) {
					foundOption.setSelectedItem(itemKey, itemValue);
					return;
				}
			}
		}
		throw new ObjectNotFoundException("The field type descriptor '"
				+ this.fieldCategory.toString() + "' doesn't contain the list option "
				+ listOption.toString());
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		return this.fieldCategory.equals(((FieldTypeDescriptorInfo) obj).getFieldCategory());
	}

	public int hashCode() {
		return this.fieldCategory.hashCode();
	}

	// TODO: Should be in util.Enumerations with the other enumerations
	public enum FieldCategory {
		TEXT("Text", true, true), NUMBER("Number", true, true), DATE("Date", true, true), DURATION(
				"Time duration", false, true), SEQUENCE("Auto-generated number sequence", true,
				true), RELATION("Relation", true, true), CHECKBOX("Checkbox", true, true), FILE(
				"File", true, true), SEPARATOR("Separator", true, false), REFERENCED_REPORT_DATA(
				"Cross referenced data", true, false), COMMENT_FEED("Comment feed", true, false);

		FieldCategory(String typeDescription, boolean enabled, boolean savesData) {
			this.description = typeDescription;
			this.enabled = enabled;
			this.savesData = savesData;
		}

		/**
		 * Returns a plain English description of the field type for display to
		 * the user
		 */
		public String getDescription() {
			return this.description;
		}

		/**
		 * Returns true if the field type should be used in the user interface,
		 * false if it's currently disabled
		 */
		public boolean isEnabled() {
			return this.enabled;
		}

		public boolean savesData() {
			return this.savesData;
		}

		/**
		 * Returns the value of the 'fieldtype' parameter that must be submitted
		 * to the server to create a field of this type
		 */
		public String getFieldTypeParameter() {
			return this.toString().toLowerCase(Locale.UK);
		}

		private String description;

		private boolean enabled = true;

		/**
		 * True if this field type saves user input data, false if it's for
		 * another purpose
		 */
		private boolean savesData;
	}

	public String toString() {
		return this.fieldCategory.toString();
	}

	private final FieldCategory fieldCategory;

	private List<BaseFieldDescriptorOptionInfo> options = new ArrayList<BaseFieldDescriptorOptionInfo>();

	private static final SimpleLogger logger = new SimpleLogger(FieldTypeDescriptor.class);
}
