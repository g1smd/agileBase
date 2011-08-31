/*
 *  Copyright 2011 GT webMarque Ltd
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

import java.util.Random;

import org.apache.commons.lang.RandomStringUtils;

/**
 * Generates random strings for use as IDs
 */
public class RandomString {
	
	private RandomString() {}

	/**
	 * Creates a new random lowercase alphanumeric string. This can be used to
	 * generate random passwords, internal table names etc. It will be a valid
	 * Postgres object name so can be used for any database object.
	 */
	public static String generate() {
		if (!AppProperties.testMode) {
			return (RandomStringUtils.randomAlphabetic(1) + RandomStringUtils
					.randomAlphanumeric(16)).toLowerCase();
		}
		rand++;
		return String.valueOf(rand);
		// For testing, use deterministic results - seed the random generator
		// with a constant
/*		if (random == null) {
			random = new Random(42);
		}
		return RandomStringUtils.random(1, 0, 0, true, false, null, random)
				+ RandomStringUtils.random(16, 0, 0, true, true, null, random);
*/	}

	private static int rand = 1;

}
