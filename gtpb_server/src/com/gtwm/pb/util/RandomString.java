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
package com.gtwm.pb.util;

import org.apache.commons.lang.RandomStringUtils;

/**
 * Instantiating an object of this class generates a random string.
 * 
 * TODO: make this static
 */
public class RandomString {

	/**
	 * Creates a new random lowercase alphanumeric string. This can be used to generate random
	 * passwords, internal table names etc. It will be a valid postgres object
	 * name so can be used for any database object.
	 */
	public RandomString() {
		// to be a valid postgres object name, start with a letter
		this.generatedHexString = (RandomStringUtils.randomAlphabetic(1)
				+ RandomStringUtils.randomAlphanumeric(16)).toLowerCase();
	}

	/**
	 * Use this to access the generated string
	 */
	public String toString() {
		return this.generatedHexString;
	}

	private final String generatedHexString;

	//private static final SimpleLogger logger = new SimpleLogger(RandomString.class);
}
