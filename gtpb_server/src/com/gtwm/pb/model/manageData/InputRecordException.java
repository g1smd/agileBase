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

import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.util.PortalBaseException;

/**
 * An exception caused by invalid user input when trying to input a field value, e.g. if a letter is input for
 * a number field
 */
public class InputRecordException extends PortalBaseException {

    public InputRecordException(String message, BaseField fieldCausingException) {
        super(message);
        this.fieldCausingException = fieldCausingException;
    }

    public BaseField getFieldCausingException() {
        return this.fieldCausingException;
    }

    private BaseField fieldCausingException = null;
}
