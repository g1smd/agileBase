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

/**
 * A on/off field setting, such as whether the field is unique or not null. To turn the setting on, submit
 * "1", to turn it off, submit "0" as the form input value
 */
public interface BooleanFieldDescriptorOptionInfo extends BaseFieldDescriptorOptionInfo {

    /**
     * Set the field option to be on or off (true or false)
     */
    public void setOptionState(boolean optionState);
    
    /**
     * @return  The field option state as set by setOptionState()
     */
    public boolean getOptionState();
}
