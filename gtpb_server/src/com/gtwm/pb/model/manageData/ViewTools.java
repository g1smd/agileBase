/*
 *  Copyright 2012 GT webMarque Ltd
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
package com.gtwm.pb.model.manageData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Collections;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.math.MathContext;
import java.nio.charset.Charset;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.gtwm.pb.model.interfaces.ModuleInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.ViewToolsInfo;
import com.gtwm.pb.model.interfaces.FilterTypeDescriptorInfo;
import com.gtwm.pb.model.manageSchema.FilterTypeDescriptor;
import com.gtwm.pb.util.Enumerations.FilterType;
import com.gtwm.pb.util.Enumerations.Browsers;
import com.gtwm.pb.util.Helpers;
import com.gtwm.pb.util.ObjectNotFoundException;
import com.gtwm.pb.model.interfaces.FieldTypeDescriptorInfo;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.fields.BaseValue;
import com.gtwm.pb.model.interfaces.fields.TextField;
import com.gtwm.pb.model.interfaces.fields.TextValue;
import com.gtwm.pb.model.interfaces.fields.FileValue;
import com.gtwm.pb.model.manageSchema.FieldTypeDescriptor;
import com.gtwm.pb.model.manageSchema.FieldTypeDescriptor.FieldCategory;
import com.gtwm.pb.model.manageData.fields.TextValueDefn;
import com.gtwm.pb.model.manageData.fields.FileValueDefn;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.AppProperties;
import com.gtwm.pb.util.RandomString;
import org.apache.velocity.tools.generic.MathTool;
import org.grlea.log.SimpleLogger;
import com.ibm.icu.text.RuleBasedNumberFormat;

public final class ViewTools implements ViewToolsInfo {

	private ViewTools() {
		this.webAppRoot = null;
		this.request = null;
		this.response = null;
	}

	public ViewTools(HttpServletRequest request, HttpServletResponse response, String webAppRoot) {
		this.request = request;
		this.response = response;
		this.webAppRoot = webAppRoot;
	}

	public String getWebAppRoot() {
		return this.webAppRoot;
	}

	public boolean isNull(Object o) {
		return (o == null);
	}

	public boolean isInteger(String string) {
		return (string.matches("\\d+"));
	}

	public String spelloutDecimal(double number) {
		RuleBasedNumberFormat rbnf = new RuleBasedNumberFormat(RuleBasedNumberFormat.SPELLOUT);
		return rbnf.format(number);
	}

	public String spelloutCurrencyFromString(String number) {
		double doubleNumber = Double.valueOf(number);
		double poundsPart = Math.floor(doubleNumber);
		String penceString = "0";
		if (number.contains(".")) {
			penceString = number.replaceAll("^.*\\.", "");
		}
		int pencePart = Integer.valueOf(penceString);
		return this.spelloutDecimal(poundsPart) + " pounds " + pencePart + "p";
	}

	public String getDatestampString() {
		Calendar calendar = Calendar.getInstance();
		return String.format("%1$td/%1$tm/%1$tY", calendar);
	}

	public Calendar getCalendar() {
		return Calendar.getInstance();
	}

	public int getCalendarConstant(String constantName) throws CantDoThatException {
		if (constantName.equalsIgnoreCase("YEAR")) {
			return Calendar.YEAR;
		}
		if (constantName.equalsIgnoreCase("DAY_OF_MONTH")) {
			return Calendar.DAY_OF_MONTH;
		}
		if (constantName.equalsIgnoreCase("MONTH")) {
			return Calendar.MONTH;
		}
		if (constantName.equalsIgnoreCase("HOUR_OF_DAY")) {
			return Calendar.HOUR_OF_DAY;
		}
		if (constantName.equalsIgnoreCase("MINUTE")) {
			return Calendar.MINUTE;
		}
		if (constantName.equalsIgnoreCase("SECOND")) {
			return Calendar.SECOND;
		}
		throw new CantDoThatException("Unimplemented calendar constant: " + constantName);
	}

	public SortedMap<BaseField, BaseValue> getAddress(Map<BaseField, BaseValue> tableDataRow) {
		SortedMap<BaseField, BaseValue> address = new TreeMap<BaseField, BaseValue>();
		SortedMap<BaseField, BaseValue> sortedTableDataRows = new TreeMap<BaseField, BaseValue>(
				tableDataRow);
		for (Map.Entry<BaseField, BaseValue> tableDataRowEntry : sortedTableDataRows.entrySet()) {
			BaseField field = tableDataRowEntry.getKey();
			BaseValue value = tableDataRowEntry.getValue();
			if (value instanceof TextValue) {
				// Postcode is a marker for the end of an address
				if (((TextValue) value).isPostcode()) {
					SortedMap<BaseField, BaseValue> toPostcode = new TreeMap<BaseField, BaseValue>(
							sortedTableDataRows.headMap(field));
					toPostcode.put(field, value);
					// Found the last part of the address (the postcode)
					// Now find the first - a less accurate task.
					// Start by removing all fields before and including the
					// last non-text field
					// as we know all parts of the address will be text fields
					address = toPostcode;
					BaseField addrField = null;
					BaseField nonTextField = null;
					for (Map.Entry<BaseField, BaseValue> addressRow : toPostcode.entrySet()) {
						addrField = addressRow.getKey();
						if (!(addrField instanceof TextField)) {
							nonTextField = addrField;
						}
					}
					if (nonTextField != null) {
						address = address.tailMap(nonTextField);
						address.remove(nonTextField);
					}
					// An address isn't going to be more than six fields long
					int numExtraneousFields = address.size() - 6;
					for (int i = 0; i < numExtraneousFields; i++) {
						address.remove(address.firstKey());
					}
					// The slightly dodgy part - detect first line of address
					// based on field name
					for (BaseField firstField : address.keySet()) {
						String fieldName = firstField.getFieldName().toLowerCase();
						if (fieldName.startsWith("addr") || fieldName.startsWith("add.")
								|| fieldName.startsWith("house") || fieldName.startsWith("flat")
								|| fieldName.startsWith("street")) {
							address = new TreeMap<BaseField, BaseValue>(address.tailMap(firstField));
							return address;
						}
					}
					// No first line can be detected, treat just the postcode on
					// its own as the full address
					address = new TreeMap<BaseField, BaseValue>(address.tailMap(address.lastKey()));
					return address;
				}
			}
		}
		return address;
	}

	public Set<FieldTypeDescriptorInfo> getFieldTypeDescriptors() throws ObjectNotFoundException {
		Set<FieldTypeDescriptorInfo> fieldTypeDescriptors = new LinkedHashSet<FieldTypeDescriptorInfo>();
		for (FieldCategory possibleFieldType : EnumSet
				.allOf(FieldTypeDescriptor.FieldCategory.class)) {
			if (possibleFieldType.isEnabled()) {
				FieldTypeDescriptorInfo fieldTypeDescriptor = new FieldTypeDescriptor(
						possibleFieldType);
				fieldTypeDescriptors.add(fieldTypeDescriptor);
			}
		}
		return fieldTypeDescriptors;
	}

	public Set<FilterTypeDescriptorInfo> getFilterTypeDescriptors() {
		Set<FilterTypeDescriptorInfo> filterTypeDescriptors = new LinkedHashSet<FilterTypeDescriptorInfo>();
		for (FilterType possibleFilterType : EnumSet.allOf(FilterType.class)) {
			FilterTypeDescriptorInfo filterTypeDescriptor = new FilterTypeDescriptor(
					possibleFilterType);
			filterTypeDescriptors.add(filterTypeDescriptor);
		}
		return filterTypeDescriptors;
	}

	public void log(Object itemToLog) {
		logger.info("Template message at " + System.currentTimeMillis() + "("
				+ this.request.getRemoteUser() + ") : " + itemToLog);
	}

	public void startTimer(String timerName) {
		if (AppProperties.enableTemplateTimers) {
			this.timers.put(timerName, BigInteger.valueOf(System.currentTimeMillis()));
		}
	}

	public void stopTimer(String timerName) {
		if (AppProperties.enableTemplateTimers) {
			BigInteger startTime = this.timers.get(timerName);
			if (startTime == null) {
				logger.warn("Timer '" + timerName + "' has not been started");
				return;
			}
			this.timers.remove(timerName);
			long elapsedTime = System.currentTimeMillis() - startTime.longValue();
			this.log(timerName + " elapsed time = " + elapsedTime + "ms");
		}
	}

	public String escape(String string) {
		if (string == null) {
			return "";
		}
		// What a rubbish Java regex!
		String escapedString = string.replaceAll("'", "\\\\'");
		escapedString = escapedString.replaceAll("\\r", "");
		escapedString = escapedString.replaceAll("\\n", "");
		return escapedString;
	}

	public String escapeForCSV(String string) {
		if (string == null) {
			return "";
		}
		String escapedString = string.replaceAll("\"", "\"\"");
		if (escapedString.contains(",") || escapedString.contains("\n")) {
			escapedString = "\"" + escapedString + "\"";
		}
		return escapedString;
	}

	// TODO: rename method to urlEncode
	public String escapeForURL(String string) {
		String encoded = string;
		try {
			// URLEncoder.encode replaces spaces with plus signs which is not
			// what we want. We want to keep newlines too
			encoded = string.replaceAll("\\s", "gtpb_special_variable_space");
			encoded = encoded.replaceAll("\\n", "gtpb_special_variable_newline");
			if (encoded.contains("/")) {
				// Only encode content after the path
				String filename = encoded.replaceAll("^.*\\/", "");
				String path = encoded.substring(0, encoded.length() - filename.length());
				encoded = path + java.net.URLEncoder.encode(filename, "UTF-8");
			} else {
				encoded = java.net.URLEncoder.encode(encoded, "UTF-8");
			}
			encoded = encoded.replace("gtpb_special_variable_space", "%20");
			encoded = encoded.replace("gtpb_special_variable_newline", "\n");
		} catch (UnsupportedEncodingException ueex) {
			logger.error("Error URL encoding string '" + string + "': " + ueex);
		}
		return encoded;
	}

	public String joinWith(Collection<Object> collection, String joiner) {
		String result = "";
		for (Object obj : collection) {
			result = result + obj + joiner;
		}
		if (result.length() > joiner.length()) {
			result = result.substring(0, result.length() - joiner.length());
		}
		return result;
	}

	public MathTool getMathTool() {
		return this.mathTool;
	}

	public TextValue getTextValueTool(String text) {
		return new TextValueDefn(text);
	}

	public FileValue getFileValueTool(String filename) {
		return new FileValueDefn(filename);
	}

	public Double roundTo(int decimalPlaces, double number) {
		BigDecimal bigDecimal = new BigDecimal(number);
		String stringRepresentation = bigDecimal.toPlainString();
		stringRepresentation = stringRepresentation.replaceAll("^\\-", "");
		stringRepresentation = stringRepresentation.replaceAll("\\.\\d+$", "");
		int precision = stringRepresentation.length() + decimalPlaces;
		MathContext mc = new MathContext(precision, RoundingMode.HALF_UP);
		bigDecimal = bigDecimal.round(mc);
		return bigDecimal.doubleValue();
	}

	public Browsers getBrowser() {
		String userAgent = request.getHeader("User-Agent").toLowerCase();
		// The user agent may match multiple browsers, e.g. the iPhone will
		// trigger IPHONE and SAFARI
		EnumSet<Browsers> browsersMatched = EnumSet.noneOf(Browsers.class);
		for (Browsers browser : EnumSet.allOf(Browsers.class)) {
			if (userAgent.contains(browser.getUserAgentString())) {
				browsersMatched.add(browser);
			}
		}
		// Treat the iPhone and iPod as one
		if (browsersMatched.contains(Browsers.IPHONE) || browsersMatched.contains(Browsers.IPOD)) {
			return Browsers.APPLE_MOBILE;
		} else {
			for (Browsers browser : browsersMatched) {
				return browser;
			}
		}
		return Browsers.UNKNOWN;
	}

	public boolean browserVersionIsAtLeast(String testVersionString) {
		float testVersion = Float.valueOf(testVersionString);
		float detectedVersion = this.getBrowserVersion();
		if (detectedVersion >= testVersion) {
			return true;
		} else {
			return false;
		}
	}

	public float getBrowserVersion() {
		String userAgent = this.request.getHeader("User-Agent").toLowerCase();
		Browsers browser = this.getBrowser();
		String versionString = "";
		float detectedVersion = 0.0f;
		// Firefox variants
		if (browser.equals(Browsers.FIREFOX) || browser.equals(Browsers.MINEFIELD)
				|| browser.equals(Browsers.CAMINO)) {
			// example user agents
			// Mozilla/5.0 (Windows; U; Windows NT 5.1; en-GB; rv:1.8.0.1)
			// Gecko/20060111 Firefox/1.5.0.1
			// Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.7.6) Gecko/20050306
			// Firefox/1.0.1 (Debian package 1.0.1-2)
			versionString = userAgent.replaceAll("^.*" + browser.getUserAgentString() + "\\/", "");
			versionString = versionString.replaceAll("\\s.*$", "");
			// version string will now be something like 1.5.0.2
			// Just in case it's an alpha or beta like 3.0b1, remove the
			// designation
			versionString = versionString.replaceAll("[a-z]", "");
			// get the number before the first decimal point
			String majorVersionString = (new StringBuilder(versionString)).reverse().toString();
			// majorVersion is now 2.0.5.1
			majorVersionString = majorVersionString.replaceAll("^.*\\.", "");
			majorVersionString = (new StringBuilder(majorVersionString)).reverse().toString();
			// majorVersion is now 1
			String minorVersionString = versionString.replaceFirst(
					"^" + majorVersionString + "\\.", "");
			minorVersionString = minorVersionString.replaceAll("\\.", "");
			// minorVersion is now 502
			detectedVersion = Float.valueOf(majorVersionString + "." + minorVersionString);
		} else if (browser.equals(Browsers.MSIE)) {
			// example user agent
			// Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; SV1; .NET CLR
			// 1.0.3705; .NET CLR 1.1.4322)
			versionString = userAgent.replaceAll("^.*" + browser.getUserAgentString() + "\\s",
					browser.getUserAgentString());
			versionString = versionString.replaceAll("\\;.*$", "");
			versionString = versionString.replaceAll(browser.getUserAgentString(), "");
			detectedVersion = Float.valueOf(versionString);
		} else if (browser.equals(Browsers.SAFARI)) {
			// e.g. Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en)
			// AppleWebKit/417.9 (KHTML, like Gecko) Safari/417.8
			// iPhone example:
			// Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en-us)
			// AppleWebKit/523.10.3 (KHTML, like Gecko)
			versionString = userAgent.replaceAll("^.*" + browser.getUserAgentString() + "\\/", "");
			versionString = versionString.replaceAll("^\\s.*", "");
			String majorVersionString = (new StringBuilder(versionString)).reverse().toString();
			majorVersionString = majorVersionString.replaceAll("^.*\\.", "");
			majorVersionString = (new StringBuilder(majorVersionString)).reverse().toString();
			String minorVersionString = versionString.replaceFirst(
					"^" + majorVersionString + "\\.", "");
			minorVersionString = minorVersionString.replaceAll("\\.", "");
			detectedVersion = Float.valueOf(majorVersionString + "." + minorVersionString);
		} else if (browser.equals(Browsers.OPERA)) {
			// Opera/8.02 (Macintosh; PPC Mac OS X; U; en)
			versionString = userAgent.replaceAll("^.*" + browser.getUserAgentString() + "\\/", "");
			versionString = versionString.replaceAll("\\s.*$", "");
			detectedVersion = Float.valueOf(versionString);
		} else if (browser.equals(Browsers.SYMBIAN_MOBILE)) {
			// TODO: Symbian Safari version detect
			return 1.0f;
		} else {
			logger.warn("Unable to detect browser version from " + userAgent);
		}
		return detectedVersion;
	}

	public Map<BaseField, String> getNewBaseFieldStringMap() {
		return this.getNewFilterMap();
	}

	public SortedMap<String, Object> getNewSortedStringObjectMap() {
		return new TreeMap<String, Object>();
	}

	public SortedMap<ModuleInfo, Object> getNewSortedModuleObjectMap() {
		return new TreeMap<ModuleInfo, Object>();
	}

	public Map<BaseField, String> getNewFilterMap() {
		return new HashMap<BaseField, String>();
	}

	public Map<String, Object> getNewStringObjectMap() {
		return new HashMap<String, Object>();
	}

	public Set<String> getNewStringSet() {
		return new TreeSet<String>();
	}

	public Set<TableInfo> getNewTableSet() {
		return new TreeSet<TableInfo>();
	}

	public void reverseList(List list) {
		Collections.reverse(list);
	}

	public void sortList(List list) {
		Collections.sort(list);
	}

	public Map getRequestParameters() {
		return this.request.getParameterMap();
	}

	public String getContentType() {
		return this.response.getContentType();
	}

	public String getRandomString() {
		return RandomString.generate();
	}

	public String getAppUrl() {
		return Helpers.getAppUrl(this.request);
	}

	public String lpad(String stringToPad, int lengthToPadTo, String padCharacter) {
		String paddedString = stringToPad;
		int numExtraCharsNecessary = lengthToPadTo - stringToPad.length();
		for (int i = 0; i < numExtraCharsNecessary; i++) {
			paddedString = padCharacter + paddedString;
		}
		return paddedString;
	}

	public String cleanString(String stringToClean) {
		return stringToClean.toLowerCase().replaceAll("\\W", "");
	}

	public String rinseString(String stringToRinse) {
		return Helpers.rinseString(stringToRinse);
	}

	public String lineBreaksToParas(String stringToConvert) {
		if (stringToConvert == null) {
			return "";
		}
		return stringToConvert.replaceAll("\n", "<p>");
	}

	public String unencodeHtml(String string) {
		return Helpers.unencodeHtml(string);
	}

	public synchronized boolean templateExists(String templateFilename) {
		Boolean templateExists = this.templateExistsCache.get(templateFilename);
		if (templateExists != null) {
			return templateExists;
		}
		String absoluteFilename = this.request.getSession().getServletContext()
				.getRealPath("/WEB-INF/templates/" + templateFilename);
		File templateFile = new File(absoluteFilename);
		templateExists = templateFile.exists();
		this.templateExistsCache.put(templateFilename, templateExists);
		return templateExists;
	}

	public Set<File> listFiles(String folderName) {
		Set<File> files = null;
		String absoluteFolderName = this.request.getSession().getServletContext()
				.getRealPath("/" + folderName);
		File folder = new File(absoluteFolderName);
		File[] filesArray = folder.listFiles();
		if (filesArray != null) {
			files = new TreeSet<File>(Arrays.asList(filesArray));
		} else {
			files = new TreeSet<File>();
		}
		return files;
	}

	public String getCommitUrl() throws IOException, CantDoThatException {
		String commitFileName = this.request.getSession().getServletContext()
				.getRealPath("/lastcommit.txt");
		File commitFile = new File(commitFileName);
		try {
			InputStream inputStream = new FileInputStream(commitFileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,
					Charset.forName("UTF-8")));
			String commitLine = reader.readLine();
			String commitId = commitLine.replace("commit ", "");
			inputStream.close();
			return "https://github.com/okohll/agileBase/commit/" + commitId;
		} catch (FileNotFoundException fnfex) {
			logger.error("Commit file " + commitFileName + " not found: " + fnfex);
			// Throw exception but don't show the actual file path
			throw new CantDoThatException("Commit log not found");
		}
	}

	public String getCommitMessage() throws CantDoThatException, IOException {
		String commitFileName = this.request.getSession().getServletContext()
				.getRealPath("/lastcommit.txt");
		File commitFile = new File(commitFileName);
		try {
			InputStream inputStream = new FileInputStream(commitFileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,
					Charset.forName("UTF-8")));
			String line = null;
			String message = "";
			while ((line = reader.readLine()) != null) {
				message += line + "<br />";
			}
			inputStream.close();
			return message;
		} catch (FileNotFoundException fnfex) {
			logger.error("Commit file " + commitFileName + " not found: " + fnfex);
			// Throw exception but don't show the actual file path
			throw new CantDoThatException("Commit log not found");
		}
	}

	public String toString() {
		return "ViewTools contains utility methods useful to Velocity template designers";
	}

	public void throwException() throws CantDoThatException {
		throw new CantDoThatException("Test error message");
	}

	private final HttpServletRequest request;

	private final HttpServletResponse response;

	private final String webAppRoot;

	private MathTool mathTool = new MathTool();

	private Map<String, BigInteger> timers = new HashMap<String, BigInteger>();


	private Map<String, Boolean> templateExistsCache = new HashMap<String, Boolean>();

	private static final SimpleLogger logger = new SimpleLogger(ViewTools.class);

}