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
package com.gtwm.pb.util;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Calendar;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.Writer;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.imageio.ImageIO;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.lang.StringEscapeUtils;
import org.grlea.log.SimpleLogger;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;

import com.gtwm.pb.model.interfaces.BaseReportInfo;
import com.gtwm.pb.model.interfaces.DataRowFieldInfo;
import com.gtwm.pb.model.interfaces.DataRowInfo;
import com.gtwm.pb.model.interfaces.ReportFieldInfo;
import com.gtwm.pb.model.interfaces.SimpleReportInfo;
import com.gtwm.pb.model.interfaces.TableInfo;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.fields.FileField;
import com.gtwm.pb.model.interfaces.fields.FileValue;
import com.gtwm.pb.model.manageData.fields.FileValueDefn;
import com.gtwm.pb.util.Enumerations.AttachmentType;
import com.gtwm.pb.util.Enumerations.DatabaseFieldType;

/**
 * Methods that are sometimes useful but aren't provided by the Java language
 * itself
 */
public final class Helpers {

	private Helpers() {
	}

	public static String joinWith(Collection collection, String joiner) {
		String result = "";
		for (Object obj : collection) {
			result = result + obj + joiner;
		}
		if (result.length() > joiner.length()) {
			result = result.substring(0, result.length() - joiner.length());
		}
		return result;
	}

	public static void sendEmail(Set<String> recipients, String body, String subject)
			throws MessagingException {
		Properties props = new Properties();
		props.setProperty("mail.smtp.host", "localhost");
		Session mailSession = Session.getDefaultInstance(props, null);
		MimeMessage message = new MimeMessage(mailSession);
		message.setSubject(subject);
		for (String emailRecipient : recipients) {
			Address toAddress = new InternetAddress(emailRecipient);
			message.addRecipient(Message.RecipientType.TO, toAddress);
		}
		Address fromAddress = new InternetAddress("notifications@agilebase.co.uk");
		message.setFrom(fromAddress);
		String bodySansEntities = StringEscapeUtils.unescapeHtml(body);
		message.setText(bodySansEntities);
		Transport.send(message);
		logger.info("Sent message '" + subject + "' to " + recipients);
	}

	public static String getAppUrl(HttpServletRequest request) {
		String appUrl = "";
		if (request.isSecure()) {
			appUrl = "https://";
		} else {
			appUrl = "http://";
		}
		String serverName = request.getServerName();
		appUrl += serverName;
		int port = request.getServerPort();
		if ((port != 80) && (!request.isSecure())) {
			appUrl += ":" + port;
		}
		appUrl += request.getContextPath() + request.getServletPath();
		return appUrl;
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
	 * with 't', 'y' or '1', case-insensitively, will return true - anything else,
	 * false, including null. Compared to Boolean.valueOf(), it is less stringent
	 * and will never throw an Exception
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

	public static boolean isImage(String fileName) {
		if (fileName == null) {
			return false;
		}
		String extension = fileName.replaceAll("^.*\\.", "").toLowerCase().trim();
		if (extension.equals("jpg") || extension.equals("png") || extension.equals("gif")
				|| extension.equals("jpeg") || extension.equals("tif") || extension.equals("tiff")) {
			return true;
		}
		return false;
	}

	/**
	 * Given a date resolution that is a Calendar value, e.g. Calendar.MONTH,
	 * return a string that can be used by Java's String.format() method to format
	 * a date value
	 *
	 * @throws CantDoThatException
	 *           if the dateResolution isn't a valid Calendar value
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
			throw new CantDoThatException("Unable to use unrecognised date resolution: " + dateResolution);
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
	 *           if the dateResolution isn't a valid Calendar value
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
			throw new CantDoThatException("Unable to use unrecognised date resolution: " + dateResolution);
		}
	}

	/**
	 * Given a Integer/String map, return the integer key for a given value, i.e.
	 * invert the key/value mapping
	 *
	 * @param requireExactMatch
	 *          Require that the exact value is found. If false and there's no
	 *          exact match, try to find a close match, e.g. a substring match
	 * @throws CantDoThatException
	 *           If the value occurs more than once in the map, or not at all
	 */
	public static Integer getKeyForValue(Map<Integer, String> map, String value,
			boolean requireExactMatch) throws CantDoThatException {
		// a couple of tests to see that there's one and only one key for the
		// value
		Collection<String> mapValues = map.values();
		int numOccurences = Collections.frequency(mapValues, value);
		if (numOccurences > 1) {
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
					// TODO: consider something more sophisticated, e.g. from
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
		return rinsedStringBuilder.toString().trim();
	}

	/**
	 * Replace all non-word characters apart from the allowed characters
	 *
	 * @param escapedAllowedCharacters
	 *          Special characters must be double escaped. E.g. to allow slashes,
	 *          pass in "\\/"
	 */
	public static String rinseString(String stringToRinse, String escapedAllowedChars) {
		if (stringToRinse == null) {
			return null;
		}
		return stringToRinse.replaceAll("[^\\w" + escapedAllowedChars + "]", "");
	}

	/**
	 * Parameterization should be and is used for protection against SQL injection
	 * attacks where possible. This method is a fallback which can be used when it
	 * isn't possible to use prepared statements with parameters.
	 *
	 * Downsides are a) protection may not be 100% b) legitimate input may be
	 * replaced
	 *
	 * PostgreSQL specific.
	 *
	 * @throws CantDoThatException
	 *           if the input string contains stuff which shouldn't be there
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
		// We do know the format of internal names as they're generated by the
		// util.RandomString class
		if (stringToProtect.matches(".*\\W[a-z][a-z0-9]{16}\\W.*")
				|| stringToProtect.matches("^[a-z][a-z0-9]{16}\\W.*")
				|| stringToProtect.matches(".*\\W[a-z][a-z0-9]{16}$")
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
	 *          Internal names of all objects in this report or related to it will
	 *          be found and replaced
	 */
	public static String replaceInternalNames(String inputString, SimpleReportInfo report)
			throws CodingErrorException {
		String resultString = inputString;
		Set<TableInfo> tables = report.getJoinReferencedTables();
		for (TableInfo table : tables) {
			String internalTableName = table.getInternalTableName();
			resultString = resultString.replaceAll(internalTableName, table.toString());
			for (BaseField field : table.getFields()) {
				resultString = resultString.replaceAll(field.getInternalFieldName(), field.toString());
			}
			for (BaseReportInfo tableReport : table.getReports()) {
				resultString = resultString.replaceAll(tableReport.getInternalReportName(),
						tableReport.toString());
			}
		}
		return resultString;
	}

	/**
	 * Replace HTML entities with plain versions
	 *
	 * @param string
	 * @return
	 */
	public static String unencodeHtml(String string) {
		if (string == null) {
			return "";
		}
		String unencoded = string.replace("&amp;", "&");
		unencoded = unencoded.replace("&lt;", "<");
		unencoded = unencoded.replace("&gt;", ">");
		unencoded = unencoded.replace("&#x27;", "'");
		unencoded = unencoded.replace("&quot;", "'");
		return unencoded;
	}

	public static String smartCharsReplace(String string) {
		if (string == null) {
			return "";
		}
		String replaced = string.replace("\u2018", "'").replace("\u2019", "'").replace("\u201C", "\"")
				.replace("\u201D", "\"").replace("\u2014", "-").replace("\u2013", "-");
		return replaced;
	}

	/**
	 * @param shortTitle
	 *          If true, return only the first part of the title
	 */
	public static String buildEventTitle(BaseReportInfo report, DataRowInfo reportDataRow,
			boolean shortTitle, boolean includeNumbers) {
		// ignore any date fields other than the one used for specifying
		// the event date
		// ignore any blank fields
		// for numeric and boolean fields, include the field title
		StringBuilder eventTitleBuilder = new StringBuilder();
		int fieldCount = 0;
		REPORT_FIELD_LOOP: for (ReportFieldInfo reportField : report.getReportFields()) {
			BaseField baseField = reportField.getBaseField();
			DataRowFieldInfo dataRowField = reportDataRow.getValue(baseField);
			String displayValue = dataRowField.getDisplayValue();
			DatabaseFieldType dbType = baseField.getDbType();
			if (displayValue.equals("") || dbType.equals(DatabaseFieldType.TIMESTAMP)
					|| baseField.equals(baseField.getTableContainingField().getPrimaryKey())) {
				continue REPORT_FIELD_LOOP;
			}
			switch (dbType) {
			case BOOLEAN:
				boolean reportFieldTrue = valueRepresentsBooleanTrue(dataRowField.getKeyValue());
				if (reportFieldTrue) {
					eventTitleBuilder.append(reportField.getFieldName() + ", ");
					fieldCount++;
				}
				break;
			case INTEGER:
			case FLOAT:
				if (includeNumbers) {
					eventTitleBuilder.append(reportField.getFieldName()).append(" = ")
							.append(displayValue + ", ");
					fieldCount++;
				}
				break;
			case SERIAL:
				if (includeNumbers) {
					eventTitleBuilder.append(reportField.getFieldName()).append(" = ")
							.append(dataRowField.getKeyValue() + ", ");
					fieldCount++;
				}
				break;
			default:
				if (baseField instanceof FileField) {
					if(((FileField) baseField).getAttachmentType().equals(AttachmentType.DOCUMENT)) {
						eventTitleBuilder.append(displayValue + ", ");
						fieldCount++;
					}
				} else {
					eventTitleBuilder.append(displayValue + ", ");
					fieldCount++;
				}
			}
			if (shortTitle && (fieldCount > 3)) {
				break REPORT_FIELD_LOOP;
			}
		}
		int titleLength = eventTitleBuilder.length();
		if (titleLength > 1) {
			eventTitleBuilder.delete(eventTitleBuilder.length() - 2, eventTitleBuilder.length());
		}
		String eventTitle = eventTitleBuilder.toString();
		return eventTitle;
	}

	private static final SimpleLogger logger = new SimpleLogger(Helpers.class);

	/**
	 * Checks if the original image is larger than the specified thumbnail size. If so, create a thumbnail, if just copy the original image
	 * @param size max. image width and height
	 */
	public static void createThumbnail(FileField field, FileValue fileValue, String filePath, int size) throws FileUploadException {
		int filenameNumber = size;
		if (field.getAttachmentType().equals(AttachmentType.PROFILE_PHOTO)) {
			// For profile photos, size is 250 but filename is .500 for backwards compatibility reasons
			size = 250;
		}
		boolean needResize = false;
		String extension = "";
		if (filePath.contains(".")) {
			extension = filePath.replaceAll("^.*\\.", "").toLowerCase();
		}
		File selectedFile = new File(filePath);
		if (!selectedFile.exists()) {
			throw new FileUploadException("File "+ filePath + " not found");
		}
		if ((extension.equals("pdf"))
				|| (!fileValue.getExtension().equals(fileValue.getPreviewExtension()))) {
			needResize = true;
		} else {
			try {
				BufferedImage originalImage = ImageIO.read(selectedFile);
				int height = originalImage.getHeight();
				int width = originalImage.getWidth();
				if ((height > size) || (width > size)) {
					needResize = true;
				}
			} catch (IOException ex) {
				// Certain images can sometimes fail to be read
				// e.g. CMYK JPGs fail with IOex
				// NullPointerException
				// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5100094
				// http://code.google.com/p/thumbnailator/issues/detail?id=40
				logger.error("Error reading image dimensions: " + ex);
				if (selectedFile.length() > 1000000) {
					needResize = true;
				}
			}
		}
		// Conditional resize
		if (needResize) {
			Helpers.createThumbnailWork(size, size, filePath);
		} else {
			String thumbPath = filePath + "." + filenameNumber + "." + extension;
			File thumbFile = new File(thumbPath);
			try {
				Files.copy(selectedFile.toPath(), thumbFile.toPath(),
						StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException ioex) {
				throw new FileUploadException(
						"Error copying " + selectedFile + " to " + thumbFile, ioex);
			}
		}
	}

	/**
	 * Does the actual work of creating a thumbnail or smaller image
	 */
	public static void createThumbnailWork(int width, int height, String inputFilePath)
			throws FileUploadException {
		ConvertCmd convert = new ConvertCmd();
		IMOperation op = new IMOperation();
		op.addImage(); // Placeholder for input PDF
		op.resize(width, height);
		op.addImage(); // Placeholder for output PNG
		String newExtension = (new FileValueDefn(inputFilePath)).getPreviewExtension();
		int filenameSize = width;
		if (filenameSize == 250) {
			// Profile photos are saves as 250x250 but for backwards compatibility
			// should still be named 500
			filenameSize = 500;
		}
		try {
			String convertPath = inputFilePath;
			if (inputFilePath.endsWith("pdf")) {
				// [0] means convert only first page if a PDF
				convertPath += "[0]";
				newExtension = "png";
			}
			convert.run(op, new Object[] { convertPath,
					inputFilePath + "." + filenameSize + "." + newExtension });
		} catch (IOException ioex) {
			throw new FileUploadException("IO error while converting " + inputFilePath + " to "
					+ newExtension + ": " + ioex);
		} catch (InterruptedException iex) {
			throw new FileUploadException("Interrupted while converting " + inputFilePath + " to "
					+ newExtension + ": " + iex);
		} catch (IM4JavaException im4jex) {
			throw new FileUploadException("Problem converting " + inputFilePath + " to " + newExtension
					+ ": " + im4jex);
		}
	}

}
