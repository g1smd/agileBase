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
package com.gtwm.pb.model.interfaces.fields;

/**
 * Java doesn't have a duration class to store an interval of time, we'll make our own
 */
public interface DurationValue extends BaseValue {

    public int getYears();

    public int getMonths();

    public int getDays();

    public int getHours();

    public int getMinutes();

    public int getSeconds();

    /**
     * @return The duration formatted to be recognised as a postgresql interval type
     */
    public String getSqlFormatInterval();

}
