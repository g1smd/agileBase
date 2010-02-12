/*
 *  Copyright 2010 GT webMarque Ltd
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

import java.util.List;
import java.util.ArrayList;
import org.grlea.log.SimpleLogger;
import com.gtwm.pb.util.Enumerations.FilterType;
import com.gtwm.pb.model.interfaces.BaseFieldDescriptorOptionInfo;
import com.gtwm.pb.model.interfaces.BooleanFieldDescriptorOptionInfo;
import com.gtwm.pb.model.interfaces.ListFieldDescriptorOptionInfo;
import com.gtwm.pb.model.interfaces.TextFieldDescriptorOptionInfo;
import com.gtwm.pb.model.interfaces.FilterTypeDescriptorInfo;
import com.gtwm.pb.model.manageSchema.BooleanFieldDescriptorOption.PossibleBooleanOptions;
import com.gtwm.pb.model.manageSchema.ListFieldDescriptorOption.PossibleListOptions;
import com.gtwm.pb.model.manageSchema.TextFieldDescriptorOption.PossibleTextOptions;
import com.gtwm.pb.util.ObjectNotFoundException;

public class FilterTypeDescriptor implements FilterTypeDescriptorInfo {

    /**
     * When an instance of a FieldTypeDescriptor is created, the class is filled with the appropriate options
     * so that all getter methods work without further work
     */
    public FilterTypeDescriptor(FilterType filterType) {
        this.filterType = filterType;
        switch (this.filterType) {
        case EQUAL:
        case NOT_EQUAL_TO:
        case GREATER_THAN_OR_EQUAL_TO:
        case IS_ONE_OF:
        case LESS_THAN:
        case NEWER_THAN_IN_DAYS:
        case NEWER_THAN_IN_WEEKS:
        case NEWER_THAN_IN_MONTHS:
        case NEWER_THAN_IN_YEARS:
        case OLDER_THAN_IN_DAYS:
        case OLDER_THAN_IN_WEEKS:
        case OLDER_THAN_IN_MONTHS:
        case OLDER_THAN_IN_YEARS:
        case STARTS_WITH: case DOES_NOT_START_WITH:
            this.options.add(new TextFieldDescriptorOption(PossibleTextOptions.DEFAULTVALUE));
            break;
        }
    }

    public FilterType getFilterType() {
        return this.filterType;
    }

    public List<BaseFieldDescriptorOptionInfo> getOptions() {
        return this.options;
    }

    public void setBooleanOptionState(PossibleBooleanOptions booleanOption, boolean state) throws ObjectNotFoundException {
        for (BaseFieldDescriptorOptionInfo option : this.options) {
            if (option instanceof BooleanFieldDescriptorOptionInfo) {
                BooleanFieldDescriptorOptionInfo foundOption = (BooleanFieldDescriptorOptionInfo) option;
                if (foundOption.getOptionDescription().equals(booleanOption.getOptionDescription())) {
                    foundOption.setOptionState(state);
                    return;
                }
            }
        }
        throw new ObjectNotFoundException("The field type descriptor '" + this.filterType.toString() + "' doesn't contain the boolean option "
                + booleanOption.toString());
    }

    public void setTextOptionValue(PossibleTextOptions textOption, String value) throws ObjectNotFoundException {
        for (BaseFieldDescriptorOptionInfo option : this.options) {
            if (option instanceof TextFieldDescriptorOptionInfo) {
                TextFieldDescriptorOptionInfo foundOption = (TextFieldDescriptorOptionInfo) option;
                if (foundOption.getOptionDescription().equals(textOption.getOptionDescription())) {
                    foundOption.setValue(value);
                    return;
                }
            }
        }
        throw new ObjectNotFoundException("The field type descriptor '" + this.filterType.toString() + "' doesn't contain the boolean option "
                + textOption.toString());
    }

    public void setListOptionSelectedItem(PossibleListOptions listOption, String selectedItemKey) throws ObjectNotFoundException {
        for (BaseFieldDescriptorOptionInfo option : this.options) {
            if (option instanceof ListFieldDescriptorOptionInfo) {
                ListFieldDescriptorOptionInfo foundOption = (ListFieldDescriptorOptionInfo) option;
                if (foundOption.getOptionDescription().equals(listOption.getOptionDescription())) {
                    foundOption.setSelectedItem(selectedItemKey);
                    return;
                }
            }
        }
        throw new ObjectNotFoundException("The field type descriptor '" + this.filterType.toString() + "' doesn't contain the list option "
                + listOption.toString());
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (obj.getClass() != this.getClass())) {
            return false;
        }
        return this.filterType.equals(((FilterTypeDescriptorInfo) obj).getFilterType());
    }

    public int hashCode() {
        return this.filterType.hashCode();
    }

    public String toString() {
        return this.filterType.toString() + " - " + this.options.toString();
    }

    private FilterType filterType;

    private List<BaseFieldDescriptorOptionInfo> options = new ArrayList<BaseFieldDescriptorOptionInfo>();

	private static final SimpleLogger logger = new SimpleLogger(FilterTypeDescriptor.class);
}
