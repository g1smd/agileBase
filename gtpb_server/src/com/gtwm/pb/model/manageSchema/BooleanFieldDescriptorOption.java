/*
 *  Copyright 2011 GT webMarque Ltd
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

import com.gtwm.pb.model.interfaces.BooleanFieldDescriptorOptionInfo;
import java.util.Locale;

public class BooleanFieldDescriptorOption implements BooleanFieldDescriptorOptionInfo {

    public BooleanFieldDescriptorOption(PossibleBooleanOptions booleanOption) {
        this.booleanOption = booleanOption;
    }

    public String getOptionDescription() {
        return this.booleanOption.getOptionDescription();
    }

    public String getFormInputName() {
        return this.booleanOption.getFormInputName();
    }
    
    public void setOptionState(boolean optionState) {
        this.optionState = optionState;
    }
    
    public boolean getOptionState() {
        return this.optionState;
    }

    public boolean isAdvancedOption() {
        return this.booleanOption.isAdvancedOption();
    }

    public enum PossibleBooleanOptions {
        UNIQUE("Unique", true),
        DEFAULTTONOW("Use current time for new records", false), 
        DEFAULTTONULL("Always empty for new records", false),
        ALLOWNOTAPPLICABLE("Allow not applicable", true), 
        USELOOKUP("Use dropdown for input", false), 
        HIDDEN("Hidden field",true),
        MANDATORY("Mandatory", true);

        PossibleBooleanOptions(String optionDescription, boolean advancedOption) {
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
        return this.booleanOption.toString();
    }

    private PossibleBooleanOptions booleanOption;
    
    private boolean optionState = false;
}
