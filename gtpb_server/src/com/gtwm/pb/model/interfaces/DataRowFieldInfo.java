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
 * Represents one field value in a database record
 * 
 * @author Craig McDonnell
 * 
 */
public interface DataRowFieldInfo {

    /**
     * @return A string representation of the field value as stored in the db table.
     *         Always return an empty string instead of null as
     *         the templating language Velocity doesn't like nulls
     */
    public String getKeyValue();
    
    /**
     * @return A string representation of the field's display value.
     *         Always return an empty string instead of null as
     *         the templating language Velocity doesn't like nulls
     */
    public String getDisplayValue();

    /**
     * 
     * @return In a report, numeric fields are coloured according to the no. std. devs. away for the column's
     *         mean value. This returns the hex representation of that colour for use in HTML
     */
    public String getStandardDevHexColour();
}
