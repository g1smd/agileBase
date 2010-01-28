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
 * When you're running a method that throws an exception E, but in the situation you're running it in, E
 * should never actually logically be thrown, catch and re-throw it as a this exception as it must be due to
 * programmer error
 */
public class CodingErrorException extends AgileBaseException {

	public CodingErrorException(String message) {
        super(message + ". This exception should never logically occur and must be due to programmer error");
    }

    public CodingErrorException(String message, Throwable cause) {
        super(message + ". This exception should never logically occur and must be due to programmer error", cause);
    }
}
