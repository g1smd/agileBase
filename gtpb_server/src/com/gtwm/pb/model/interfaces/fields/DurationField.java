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
package com.gtwm.pb.model.interfaces.fields;

import com.gtwm.pb.util.CantDoThatException;

/**
 * A time duration, e.g. 10 minutes or 3hrs 5 minutes
 */
public interface DurationField extends BaseField {
    /**
     * How accurately should we record this field?<br>
     * Don't want to display unnecessary input boxes on the screen
     * 
     * @param durationResolution
     *            Use values from the Calendar class constants
     * @see java.util.Calendar The Calendar class for the constant values to use
     */
    public void setResolution(int durationResolution);

    /**
     * What's the largest measure to display when inputing a new value? e.g. if we are recording hours &
     * minutes, showing years, months and days in an input form is unecessary
     * 
     * @param scale
     *            Use values from the Calendar class constants
     * @see java.util.Calendar The Calendar class for the constant values to use
     */
    public void setScale(int scale);

    public int getResolution();
    
    public int getScale();
    
    public void setDefault(DurationValue defaultValue) throws CantDoThatException;
    
    public DurationValue getDefault();
    
    public void clearDefault();

}
