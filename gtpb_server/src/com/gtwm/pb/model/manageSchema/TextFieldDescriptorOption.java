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
import com.gtwm.pb.model.interfaces.TextFieldDescriptorOptionInfo;

public class TextFieldDescriptorOption implements TextFieldDescriptorOptionInfo {

    public TextFieldDescriptorOption(PossibleTextOptions textOption) {
        this.textOption = textOption;
    }

    public String getOptionDescription() {
        return this.textOption.getOptionDescription();
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isValueNull() {
        return (value == null);
    }

    public String getFormInputName() {
        return this.textOption.getFormInputName();
    }
    
    public boolean isAdvancedOption() {
        return this.textOption.isAdvancedOption();
    }

    public enum PossibleTextOptions {
        DEFAULTVALUE("Default Value", true),
        NOTAPPLICABLEDESCRIPTION("Not applicable description", true),
        NOTAPPLICABLEVALUE("Not applicable value", true);

        PossibleTextOptions(String optionDescription, boolean advancedOption) {
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

    public String toString() {
        return this.textOption.toString() + " = " + this.value;
    }

    private PossibleTextOptions textOption;

    private String value;
}
