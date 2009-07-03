/*
 *  Copyright 2009 GT webMarque Ltd
 * 
 *  This file is part of GT portalBase.
 *
 *  GT portalBase is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  GT portalBase is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with GT portalBase.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gtwm.pb.model.interfaces;

import java.util.List;
import com.gtwm.pb.model.manageSchema.FieldTypeDescriptor.FieldCategory;
import com.gtwm.pb.model.manageSchema.BooleanFieldDescriptorOption.PossibleBooleanOptions;
import com.gtwm.pb.model.manageSchema.ListFieldDescriptorOption.PossibleListOptions;
import com.gtwm.pb.model.manageSchema.TextFieldDescriptorOption.PossibleTextOptions;
import com.gtwm.pb.util.ObjectNotFoundException;

/**
 * Describes a field type so that the user interface can see how to set up the input form to create that a new
 * field of that type.
 * <p>
 * Usage: The UI should use this when it wants to enumerate all field options. When it just wants to check a
 * particular field property such as whether the field is unique, it's more efficient to use other methods on
 * the field object, such as getUnique()
 * <p>
 * Note: implementing classes should also implement equals and hashCode based on the field type parameter
 */
public interface FieldTypeDescriptorInfo {

    /**
     * Set a boolean option to true or false. For example, passing UNIQUE and true will set the field to
     * report itself to the UI as unique. NB This has no effect on the actual value of the field's unique
     * property but should only be used when constructing a field descriptor to return to the UI from the
     * actual field properties
     * 
     * @param booleanOption
     *            A boolean field option, such as UNIQUE or NOT_NULL
     * @param state
     *            true or false to record the correct state of the option
     * @throws ObjectNotFoundException
     *             If the field doesn't contain an option of the specified type
     */
    public void setBooleanOptionState(PossibleBooleanOptions booleanOption, boolean state) throws ObjectNotFoundException;

    /**
     * Set a text option to its String value.NB This has no effect on the actual value of the field's
     * property but should only be used when constructing a field descriptor to return to the UI from the
     * actual field properties
     *  
     * @param textOption
     * @param value
     * @throws ObjectNotFoundException
     */
    public void setTextOptionValue(PossibleTextOptions textOption, String value) throws ObjectNotFoundException;
    
    /**
     * Sets the current state of a list option, i.e. the selected item, identified by key. For example,
     * passing DATERESOLUTION and String.valueOf(Calendar.MINUTE) will set the field to report that the date
     * stored is accurate to the minute. NB This has no effect on the actual state of the field but should nly
     * be used when constructing a field descriptor to return to the UI from the UI
     * 
     * @param listOption
     *            A list option, such as DATERESOLUTION or NUMBERPRECISION
     * @param selectedItemKey
     *            The key identifying the selected item in the list
     * @throws ObjectNotFoundException
     *             If the field doesn't contain an option of the specified type, or the list doesn't contain
     *             an item idendified by the key provided
     */
    public void setListOptionSelectedItem(PossibleListOptions listOption, String selectedItemKey) throws ObjectNotFoundException;

    /**
     * Sets the current state of a list option by adding a key-value pair and using that
     */
    public void setListOptionSelectedItem(PossibleListOptions listOption, String itemKey, String itemValue) throws ObjectNotFoundException;

    /**
     * @return  The general field type of the field, e.g. whether it's a numeric, text etc. field
     */
    public FieldCategory getFieldCategory();
    
    /**
     * @return The options that the UI can set for this field
     */
    public List<BaseFieldDescriptorOptionInfo> getOptions();
}
