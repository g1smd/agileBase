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
package com.gtwm.pb.model.interfaces;

/**
 * Represents a field property or option, such as 'Unique' or 'Not Null'. Used only by the user interface when
 * creating a new field
 */
public interface BaseFieldDescriptorOptionInfo {

    /**
     * Return the description of the option as the user interface may present it
     */
    public String getOptionDescription();

    /**
     * Return the name that this option must be submitted as in a HTTP request to the server. Must be lower
     *         case
     */
    public String getFormInputName();
    
    /**
     * Return whether this option is considered advanced (true) or basic (false)
     */
    public boolean isAdvancedOption();
}
