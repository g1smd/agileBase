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
package com.gtwm.pb.model.manageData;

import com.gtwm.pb.model.interfaces.DataRowFieldInfo;

public class DataRowField implements DataRowFieldInfo {

    public static final String NULL_COLOR = "";

    private DataRowField() {
    }

    public DataRowField(String keyValue, String displayValue, String standardDevHexColor) {
        this.keyValue = keyValue;
        this.displayValue = displayValue;        
        this.standardDevHexColor = standardDevHexColor;
    }

    public DataRowField(String keyValue, String displayValue) {
        this.keyValue = keyValue;
        this.displayValue = displayValue;        
        this.standardDevHexColor = DataRowField.NULL_COLOR;
    }

    public String getKeyValue() {
        if (this.keyValue == null) {
            return "";
        } else {
            return this.keyValue;
        }
    }
    
    public String getDisplayValue() {
        if (this.displayValue == null) {
            return "";
        } else {
            return this.displayValue;
        }
    }

    public String getStandardDevHexColour() {
        return standardDevHexColor;
    }
    
    public String toString() {
        return this.getDisplayValue();
    }

    private String keyValue = null;
    
    private String displayValue = null;

    private String standardDevHexColor = NULL_COLOR;
}
