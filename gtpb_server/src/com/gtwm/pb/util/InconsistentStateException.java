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
package com.gtwm.pb.util;

/**
 * This exception should be thrown if the application is left in an inconsistent state needing a restart to
 * sort out. This can happen if the relational database and in-memory objects get out of sync for any reason.
 * For example, when adding a field to a record, the code adds the field object to the record object, then
 * performs the necessary SQL. If the SQL fails, it tries to remove the field object from the record object
 * but if this fails as well, then the app is in an inconsistent state.
 */
public class InconsistentStateException extends AgileBaseException {

	public InconsistentStateException(String message) {
        super(message);
    }

    public InconsistentStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
