/*
 *  Copyright 2009 GT webMarque Ltd
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
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Calendar;
import java.util.Collections;
import com.gtwm.pb.model.interfaces.ListFieldDescriptorOptionInfo;
import com.gtwm.pb.util.ObjectNotFoundException;

public class ListFieldDescriptorOption implements ListFieldDescriptorOptionInfo {

	public ListFieldDescriptorOption(PossibleListOptions listOption) {
		this.listOption = listOption;
		switch (listOption) {
		case DATERESOLUTION:
		case DURATIONRESOLUTION:
		case DURATIONSCALE:
			this.optionsList.put(String.valueOf(Calendar.SECOND), "second");
			this.optionsList.put(String.valueOf(Calendar.MINUTE), "minute");
			this.optionsList.put(String.valueOf(Calendar.HOUR_OF_DAY), "hour");
			this.optionsList.put(String.valueOf(Calendar.DAY_OF_MONTH), "day");
			this.optionsList.put(String.valueOf(Calendar.MONTH), "month");
			this.optionsList.put(String.valueOf(Calendar.YEAR), "year");
			this.selectedItemKey = String.valueOf(Calendar.MINUTE);
			break;
		case NUMBERPRECISION:
			for (int i = 0; i < 10; i++) {
				this.optionsList.put(String.valueOf(i), String.valueOf(i));
			}
			this.selectedItemKey = "0";
			break;
		case CHECKBOXDEFAULT:
			this.optionsList.put(Boolean.valueOf(false).toString(), "false");
			this.optionsList.put(Boolean.valueOf(true).toString(), "true");
			break;
		case TEXTCONTENTSIZE:
			this.optionsList.put(String.valueOf(TextContentSizes.FEW_WORDS.getNumChars()), TextContentSizes.FEW_WORDS.getSizeDescription());
			this.optionsList.put(String.valueOf(TextContentSizes.FEW_SENTENCES.getNumChars()), TextContentSizes.FEW_SENTENCES.getSizeDescription());
			this.optionsList.put(String.valueOf(TextContentSizes.FEW_PARAS.getNumChars()), TextContentSizes.FEW_PARAS.getSizeDescription());
			break;
		}
	}

	public String getOptionDescription() {
		return this.listOption.getOptionDescription();
	}

	public String getFormInputName() {
		return this.listOption.getFormInputName();
	}

	/**
	 * Get a list of values from which one can be chosen to submit when creating
	 * a field
	 * 
	 * @return A map of internal value (the value to submit) to value
	 *         description (for display to the user)
	 */
	public Map<String, String> getOptionsList() {
		return Collections.unmodifiableMap(new LinkedHashMap<String, String>(this.optionsList));
	}

	public void setSelectedItem(String selectedItemKey) throws ObjectNotFoundException {
		if (!this.optionsList.containsKey(selectedItemKey)) {
			throw new ObjectNotFoundException("The " + this.listOption.toString()
					+ " field option doesn't contain the item " + selectedItemKey);
		}
		this.selectedItemKey = selectedItemKey;
	}

	public void setSelectedItem(String itemKey, String itemValue) {
		this.optionsList.put(itemKey, itemValue);
		this.selectedItemKey = itemKey;
	}

	public String getSelectedItemKey() {
		return this.selectedItemKey;
	}

	public String getSelectedItemDisplayValue() {
		return this.optionsList.get(this.selectedItemKey);
	}

	public boolean isAdvancedOption() {
		return this.listOption.isAdvancedOption();
	}

	public enum PossibleListOptions {
		DATERESOLUTION("Accuracy", false), NUMBERPRECISION("Precision (decimal places)", false), DURATIONRESOLUTION(
				"Accuracy", false), DURATIONSCALE("Max. duration", false), LISTTABLE(
				"Table to use", false), LISTREPORT("Report to use", false), LISTKEYFIELD(
				"Value to store", false), LISTVALUEFIELD("Value to display", false), CHECKBOXDEFAULT(
				"Default value", true), TEXTCONTENTSIZE("Size", false);

		PossibleListOptions(String optionDescription, boolean advancedOption) {
			this.optionDescription = optionDescription;
			this.advancedOption = advancedOption;
		}

		public String getOptionDescription() {
			return this.optionDescription;
		}

		public String getFormInputName() {
			return "fieldproperty" + this.toString().toLowerCase(Locale.UK);
		}

		public boolean isAdvancedOption() {
			return this.advancedOption;
		}

		private String optionDescription;

		private boolean advancedOption = false;
	}
	
	/**
	 * For the TEXTCONTENTSIZE enum value
	 */
	public enum TextContentSizes {
		FEW_WORDS(35, "short - a word or code"), FEW_SENTENCES(50, "med - a brief sentence"), FEW_PARAS(1000, "large - a few sentences");
		
		TextContentSizes(int numChars, String sizeDescription) {
			this.numChars = numChars;
			this.sizeDescription = sizeDescription;
		}
		
		public int getNumChars() {
			return this.numChars;
		}
		
		public String getSizeDescription() {
			return this.sizeDescription;
		}
		
		private int numChars = 0;
		
		private String sizeDescription = "";
	}

	public String toString() {
		return this.listOption.toString() + " - " + optionsList.toString();
	}

	private PossibleListOptions listOption;

	private Map<String, String> optionsList = new LinkedHashMap<String, String>();

	private String selectedItemKey = null;
}
