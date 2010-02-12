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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Calendar;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.Writer;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import org.grlea.log.SimpleLogger;

import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.SimpleReportInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.fields.BaseField;

/**
 * Methods that are sometimes useful but aren't provided by the Java language
 * itself
 */
public class Helpers {

	private Helpers() {
	}

	public static String readFile(String fileName) throws IOException {
		StringBuilder fileContents = new StringBuilder("");
		FileReader fileReader = new FileReader(fileName);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		try {
			String line = bufferedReader.readLine();
			while (line != null) {
				fileContents.append(line + "\n");
				line = bufferedReader.readLine();
			}
		} finally {
			bufferedReader.close();
		}
		return fileContents.toString();
	}

	public static void writeFile(String fileName, String fileContents) throws IOException {
		Writer writer = new BufferedWriter(new FileWriter(fileName));
		try {
			writer.write(fileContents);
		} finally {
			writer.close();
		}
	}

	/**
	 * Perform a liberal check of boolean representation. Any string that starts
	 * with 't', 'y' or '1', case-insensitively, will return true - anything
	 * else, false, including null. Better than using Boolean.valueOf() because
	 * it is less stringent and will never throw an Exception
	 */
	public static boolean valueRepresentsBooleanTrue(String value) {
		if (value == null) {
			return false;
		}
		if (value.toLowerCase(Locale.UK).startsWith("t")
				|| value.toLowerCase(Locale.UK).startsWith("y") || value.startsWith("1")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Given a date resolution that is a Calendar value, e.g. Calendar.MONTH,
	 * return a string that can be used by Java's String.format() method to
	 * format a date value
	 * 
	 * @throws CantDoThatException
	 *             if the dateResolution isn't a valid Calendar value
	 * @see String#format(java.lang.String, java.lang.Object[])
	 * @see java.util.Calendar
	 */
	public static String generateJavaDateFormat(int dateResolution) throws CantDoThatException {
		switch (dateResolution) {
		case Calendar.YEAR:
			return "%1$tY";
		case Calendar.MONTH:
			return "%1$tb %1$tY";
		case Calendar.DAY_OF_MONTH:
			return "%1$td %1$tb %1$tY";
		case Calendar.HOUR_OF_DAY:
			return "%1$td %1$tb %1$tY %1$tH:00";
		case Calendar.MINUTE:
			return "%1$td %1$tb %1$tY %1$tH:%1$tM";
		case Calendar.SECOND:
			return "%1$td %1$tb %1$tY %1$tH:%1$tM:%1$tS";
		default:
			throw new CantDoThatException("Unable to use unrecognised date resolution: "
					+ dateResolution);
		}
	}

	/**
	 * Given a decimal precision, return a string that can be used by Java's
	 * String.format() method to format a float or integer value
	 * 
	 * @see String#format(java.lang.String, java.lang.Object[])
	 */
	public static String generateJavaDecimalFormat(int decimalPrecision) {
		if (decimalPrecision == 0) {
			return "%,1d";
		} else {
			return "%,1." + decimalPrecision + "f";
		}
	}

	/**
	 * Given a date resolution that is a Calendar value, e.g. Calendar.MONTH,
	 * return a string that can be used by postggresql's to_char() function to
	 * format a date value.
	 * 
	 * NB this method is database specific
	 * 
	 * @throws CantDoThatException
	 *             if the dateResolution isn't a valid Calendar value
	 * @see java.util.Calendar
	 */
	public static String generateDbDateFormat(int dateResolution) throws CantDoThatException {
		switch (dateResolution) {
		case Calendar.YEAR:
			return "YYYY";
		case Calendar.MONTH:
			return "Mon YYYY";
		case Calendar.DAY_OF_MONTH:
			return "DD Mon YYYY";
		case Calendar.HOUR_OF_DAY:
			return "DD Mon YYYY HH24:00";
		case Calendar.MINUTE:
			return "DD Mon YYYY HH24:MI";
		case Calendar.SECOND:
			return "DD Mon YYYY HH24:MI:SS";
		default:
			throw new CantDoThatException("Unable to use unrecognised date resolution: "
					+ dateResolution);
		}
	}

	/**
	 * Given a Integer/String map, return the integer key for a given value,
	 * i.e. invert the key/value mapping
	 * 
	 * @param requireExactMatch
	 *            Require that the exact value is found. If false and there's no
	 *            exact match, try to find a close match, e.g. a substring match
	 * @throws CantDoThatException
	 *             If the value occurs more than once in the map, or not at all
	 */
	public static Integer getKeyForValue(Map<Integer, String> map, String value,
			boolean requireExactMatch) throws CantDoThatException {
		// a couple of tests to see that there's one and only one key for the
		// value
		Collection<String> mapValues = map.values();
		int numOccurences = Collections.frequency(mapValues, value);
		if (numOccurences >1) {
			throw new CantDoThatException("To look up the relation there should be only one '" + value
					+ "' but there are actually " + numOccurences);
		} else if ((numOccurences == 0) && requireExactMatch) {
			throw new CantDoThatException("No '" + value + "' items were found");
		}
		Set<Map.Entry<Integer, String>> mapEntrySet = map.entrySet();
		for (Map.Entry<Integer, String> mapEntry : mapEntrySet) {
			String mapValue = mapEntry.getValue();
			if (mapValue != null) {
				if (mapEntry.getValue().equals(value)) {
					return mapEntry.getKey();
				}
			} else {
				logger.warn("Null value found in map when searching for value " + value);
			}
		}
		if (!requireExactMatch) {
			for (Map.Entry<Integer, String> mapEntry : mapEntrySet) {
				String mapValue = mapEntry.getValue();
				if (mapValue != null) {
					//TODO: consider something more sophisticated, e.g. from
					// http://www.dcs.shef.ac.uk/%7Esam/stringmetrics.html
					if (mapEntry.getValue().toLowerCase().contains(value.toLowerCase())) {
						return mapEntry.getKey();
					}
				} else {
					logger.warn("Null value found in map when searching for value " + value);
				}
			}			
		}
		throw new CantDoThatException("No '" + value + "' items were found");
	}

	/**
	 * Convert an array of Strings into a Set of Strings
	 */
	public static Set<String> stringArrayToSet(String[] stringArray) {
		return new HashSet<String>(Arrays.asList(stringArray));
	}

	/**
	 * Replace all non-word characters in a string apart from spaces
	 */
	public static String rinseString(String stringToRinse) {
		String[] wordArray = stringToRinse.split("\\s");
		List<String> words = Arrays.asList(wordArray);
		StringBuilder rinsedStringBuilder = new StringBuilder();
		for (String word : words) {
			rinsedStringBuilder.append((word.replaceAll("\\W", "") + " "));
		}
		// remove the last space(s)
		return rinsedStringBuilder.toString().replaceAll("\\s+$", "");
	}

	/**
	 * Parameterization should be and is used for protection against SQL
	 * injection attacks where possible. This method is a fallback which can be
	 * used when it isn't possible to use prepared statements with parameters.
	 * 
	 * Downsides are a) protection may not be 100% b) legitimate input may be
	 * replaced
	 * 
	 * PostgreSQL specific.
	 * 
	 * @throws CantDoThatException
	 *             if the input string contains stuff which shouldn't be there
	 */
	public static void checkForSQLInjection(String stringToProtect) throws CantDoThatException {
		String cleanedString = stringToProtect;
		Set<String> invalidSubstrings = new HashSet<String>();
		// Postgresql internal views and tables for sys. admin. begin with pg_
		// Disallow access to these
		invalidSubstrings.add("pg_");
		// Disallow semicolons to separate a statement
		invalidSubstrings.add(";");
		// Disallow comments
		invalidSubstrings.add("--");
		invalidSubstrings.add("/*");
		for (String invalidSubstring : invalidSubstrings) {
			if (stringToProtect.contains(invalidSubstring)) {
				throw new CantDoThatException("'" + invalidSubstring + "' is not allowed");
			}
		}
		// Try to disallow references to internal table, report and field names
		// used by agileBase.
		// This is to stop people being able to access tables etc. they don't
		// have privileges on, if they somehow find the internal names for them.
		// Ideally to remove these, we'd pass them all in and check against each
		// one but the calling method may not always have a list of all database
		// objects available.
		// Also at time of coding there are approx. 20000 of them. Given the
		// number possible in future, performance may be an issue.
		// We do know the format of internal names, they're generated by the
		// util.RandomString class
		if (stringToProtect.matches("\\W[a-z][a-z0-9]{16}\\W")
				|| stringToProtect.matches("^[a-z][a-z0-9]{16}\\W")
				|| stringToProtect.matches("\\W[a-z][a-z0-9]{16}$")
				|| stringToProtect.matches("^[a-z][a-z0-9]{16}$")) {
			throw new CantDoThatException(
					"Direct use of internal table, field or report names is not allowed");
		}
	}

	/**
	 * Replace any internal table, report or field names with user friendly
	 * versions.
	 * 
	 * Useful when generating error messages
	 * 
	 * @param report
	 *            Internal names of all objects in this report or related to it
	 *            will be found and replaced
	 */
	public static String replaceInternalNames(String inputString, SimpleReportInfo report)
			throws CodingErrorException {
		String resultString = inputString;
		Set<TableInfo> tables = report.getJoinReferencedTables();
		for (TableInfo table : tables) {
			String internalTableName = table.getInternalTableName();
			resultString = resultString.replaceAll(internalTableName, table.toString());
			for (BaseField field : table.getFields()) {
				resultString = resultString.replaceAll(field.getInternalFieldName(), field
						.toString());
			}
			for (BaseReportInfo tableReport : table.getReports()) {
				resultString = resultString.replaceAll(tableReport.getInternalReportName(),
						tableReport.toString());
			}
		}
		return resultString;
	}

	private static final SimpleLogger logger = new SimpleLogger(Helpers.class);
}
