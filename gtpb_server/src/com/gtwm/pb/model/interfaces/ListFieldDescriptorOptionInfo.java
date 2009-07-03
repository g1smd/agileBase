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

import java.util.Map;
import com.gtwm.pb.util.ObjectNotFoundException;

public interface ListFieldDescriptorOptionInfo extends BaseFieldDescriptorOptionInfo {

    /**
     * Get a list of values from which one can be chosen to submit when creating a field
     * 
     * @return A map of internal value (the value to submit) to value description (for display to the user)
     */
    public Map<String, String> getOptionsList();

    /**
     * Sets the current value selected from one of the list options
     * 
     * @param selectedItemKey
     *            Key identifying the value
     * @throws ObjectNotFoundException
     *             If the list doesn't contain the key provided
     */
    public void setSelectedItem(String selectedItemKey) throws ObjectNotFoundException;
    
    /**
     * Sets the current value by adding a key value pair to the list options and using that
     */
    public void setSelectedItem(String itemKey, String itemValue);

    /**
     * @return  The currently selected item
     */
    public String getSelectedItemKey();

    /**
     * A shortcut equivalent to getOptionsList.get(getSelectedItemKey())
     * @return  The currently selected item display value
     */
    public String getSelectedItemDisplayValue();
}
