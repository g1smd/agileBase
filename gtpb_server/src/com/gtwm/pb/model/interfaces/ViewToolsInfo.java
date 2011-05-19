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
package com.gtwm.pb.model.interfaces;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.SortedMap;
import java.io.File;
import com.gtwm.pb.model.interfaces.fields.BaseField;
import com.gtwm.pb.model.interfaces.fields.BaseValue;
import com.gtwm.pb.model.interfaces.fields.TextValue;
import com.gtwm.pb.model.interfaces.fields.FileValue;
import com.gtwm.pb.util.Enumerations.Browsers;
import com.gtwm.pb.util.CantDoThatException;
import com.gtwm.pb.util.ObjectNotFoundException;

import org.apache.velocity.tools.generic.MathTool;

/**
 * Contains utility methods that are useful to Velocity template designers. To
 * go with ViewMethods which contains the main data and schema reporting methods
 */
public interface ViewToolsInfo {

	/**
	 * Turn a number such as 1023.54 into text such as one thousand and twenty
	 * three point five four, using the ICU tools
	 * 
	 * @see http://icu.sourceforge.net/
	 */
	public String spelloutDecimal(double number);

	/**
	 * Given the input '1023.54', returns 'one thousand and twenty three pounds
	 * 54p'
	 */
	public String spelloutCurrencyFromString(String number);

	/**
	 * The calendar returned represents the current time. Use
	 * calendar.get(calendarConstant) to return particular parts, using this
	 * method and getCalendarConstant()
	 */
	public Calendar getCalendar();

	/**
	 * Returns the current date in the form dd/mm/yyyy
	 */
	public String getDatestampString();

	/**
	 * Because velocity doesn't allow you to access constant values, you can't
	 * use Calendar.DAY_OF_MONTH etc. directly so use this method to access them
	 * instead.
	 * 
	 * In the template, you would use something like this to get the current
	 * year for example:
	 * 
	 * #set($calendar = $viewTools.getCalendar())
	 * 
	 * #set($year = $calendar.get($viewTools.getCalendarConstant("YEAR")))
	 * 
	 * @param constantName
	 *            String representation of one of the calendar constants, e.g.
	 *            DAY_OF_MONTH, HOUR, MINUTE
	 * @throws CantDoThatException
	 *             If you try to access a constant that this method doesn't
	 *             support - not all of the calendar constants are supported
	 * @see java.util.Calendar See the java.util.Calendar class for a list of
	 *      constant values
	 */
	public int getCalendarConstant(String constantName) throws CantDoThatException;

	/**
	 * e.g. given 01792 367514, return Swansea. Return an empty string if the
	 * area can't be found. Currently works for numbers in the UK.
	 */
	public String getAreaForPhoneNumber(String phoneNumber);

	/**
	 * Given a table data row, as returned by ViewMethodsInfo.getTableDataRow(),
	 * return a subset of that data which is a detected address. For example,
	 * the tableDataRow may contain fields 'company name, address 1, address 2,
	 * city, postcode, telephone number'. The returned map would be of fields
	 * 'address 1, address 2, city, postcode' and their corresponding values. If
	 * no address is detected, an empty map is returned
	 * 
	 * Note: obviously, detecting an address from just a set of fields and
	 * values isn't going to be 100% accurate
	 * 
	 * @see ViewMethodsInfo#getTableDataRow()
	 */
	public SortedMap<BaseField, BaseValue> getAddress(Map<BaseField, BaseValue> tableDataRow);

	/**
	 * Provides information helpful to the UI when displaying the section to
	 * create a field
	 * 
	 * @return A set of all field types that can be created, including
	 *         information necessary to display the options relevant to each
	 *         type on screen
	 */
	public Set<FieldTypeDescriptorInfo> getFieldTypeDescriptors() throws ObjectNotFoundException;

	public Set<FilterTypeDescriptorInfo> getFilterTypeDescriptors();

	/**
	 * Log a message to file - can be used for debugging
	 * purposes. If a string is passed, then that string will be logged, if
	 * another object is passed then that object's toString() will be logged
	 * 
	 * The log file is located at [tomcat root]/logs/catalina.out
	 */
	public void log(Object itemToLog);

	/**
	 * Used for performance measuring. Starts a timer with a specified name
	 */
	public void startTimer(String timerName);

	/**
	 * Stops the timer specified and logs the time spent since startTimer was
	 * called
	 * 
	 * @see log(Object)
	 */
	public void stopTimer(String timerName);

	/**
	 * Escape a String by replacing all single quotes with backslash single
	 * quote
	 */
	public String escape(String string);

	/**
	 * Escape a String by replacing all double quotes with two double quotes.
	 * Also surround the entire string with double quotes if it contains any
	 * commas or linebreaks. This aids in generating CSV output which can be
	 * parsed by Excel
	 */
	public String escapeForCSV(String string);

	/**
	 * Escape for use in a mailo tag or URL
	 * 
	 * @param string
	 *            e.g.
	 *            http://appserver.agilebase.co.uk/agileBase/AppController
	 *            .servlet
	 *            ?return=gui/display_application&set_table=a3b09a609b4c70624
	 *            &set_report=ac64d0f1598cf78eb
	 * @return e.g.
	 *         http%3A//appserver.agilebase.co.uk/agileBase/AppController.
	 *         servlet%3Freturn%3Dgui/display_application%26set_table%3D
	 *         a3b09a609b4c70624%26set_report%3Dac64d0f1598cf78eb
	 */
	public String escapeForURL(String string);

	/**
	 * Return a tool which can be used to do floating point maths. Velocity
	 * can't do this by default.
	 * 
	 * An example use would be $viewTools.getMathTool().add(1.4,2.3)
	 * 
	 * @see http://jakarta.apache.org/velocity/tools/generic/MathTool.html See
	 *      the Velocity documentation for full details
	 */
	public MathTool getMathTool();

	/**
	 * Return a TextValue object which contains methods for detecting whether
	 * the text is a URL, an email address, a postcode etc.
	 */
	public TextValue getTextValueTool(String text);

	/**
	 * @see #getTextValueTool(String)
	 */
	public FileValue getFileValueTool(String filename);

	/**
	 * Rounds more accurately than MathTool.roundTo() but parameters have to be
	 * the right types, MathTool accepting many different parameter types
	 */
	public Double roundTo(int decimalPlaces, double number);

	/**
	 * Useful for browser detect template code. NB JavaScript methods are
	 * generally preferred for detection of specific abilities but this method
	 * can be used if the detection needs to be done during template rendering
	 */
	public Browsers getBrowser();

	/**
	 * Useful for browser detect template code. To be used in combination with
	 * getBrowserName(). Versions tested are specific to the browser. So for
	 * example to test for Firefox >= 1.5, you'd pass up "1.5" but to test for
	 * IE 7, you'd pass up "7"
	 * 
	 * NB The string you pass has to represent a decimal number. So to test for
	 * version 1.5.0.1, you'd pass up "1.501". A web page such as
	 * http://www.ericgiguere.com/tools/http-header-viewer.html can show you
	 * what the server detects
	 */
	public boolean browserVersionIsAtLeast(String testVersion);

	/**
	 * Gets version of user's browser. Note you can't do floating point maths
	 * directly in velocity, so use browserVersionIsAtLeast() to check if the
	 * version is greater than a certain number
	 */
	public float getBrowserVersion();

	/**
	 * Returns true if the version of agileBase being accessed is running on
	 * the local machine, false if on a remote server
	 */
	public boolean isRunningLocally();

	/**
	 * Return a random string, typically for use as an ID
	 */
	public String getRandomString();

	/**
	 * Return an empty map of BaseField to String objects - useful for creating
	 * report filters in templates
	 * 
	 * @deprecated
	 * @see #getNewFilterMap() Replaced by getNewFilterMap()
	 */
	public Map<BaseField, String> getNewBaseFieldStringMap();

	/**
	 * The map returned can be used for creating report filters in templates.
	 * use something like:
	 * 
	 * $filters = $viewTools.getNewFilterMap()
	 * 
	 * $filters.put($filterField, "filterValue")
	 * 
	 * then pass $filters in to viewMethods.getReportDataRows(...)
	 * 
	 * @return An empty map of BaseField to String objects
	 */
	public Map<BaseField, String> getNewFilterMap();

	/**
	 * Return an empty map of String to Object
	 */
	public Map<String, Object> getNewStringObjectMap();

	public SortedMap<String, Object> getNewSortedStringObjectMap();

	public SortedMap<ModuleInfo, Object> getNewSortedModuleObjectMap();

	/**
	 * Return an empty set of strings
	 */
	public Set<String> getNewStringSet();

	/**
	 * Return the list having been reordered into reverse order
	 */
	public void reverseList(List list);

	/**
	 * 
	 * Return the list having been reordered into ascending order
	 */
	public void sortList(List list);

	/**
	 * Return a map of parameters to values made in the current HTTP request, as
	 * returned by HttpServletRequest.getParameterMap()
	 * 
	 * @see http 
	 *      ://java.sun.com/j2ee/sdk_1.3/techdocs/api/javax/servlet/ServletRequest
	 *      .html#getParameterMap() HttpServletRequest.getParameterMap()
	 */
	public Map getRequestParameters();

	/**
	 * Return the serverside path of the root of the application
	 * 
	 * NB for security you should be aware this is the serverside path, e.g.
	 * /usr/local/tomcat/webapps/agileBase/
	 */
	public String getWebAppRoot();

	/**
	 * Return the content type of the page being served, e.g. text/html, xml
	 * i.e. as set in the HTTP headers
	 */
	public String getContentType();

	/**
	 * Return the url of the currently running server including the host and
	 * directory examples: http://appserver.agilebase.co.uk/agileBase/
	 * http://localhost:8080/agileBase/
	 */
	public String getAppUrl();

	/**
	 * Similar to postgresql's lpad function
	 * 
	 * @see http://www.postgresql.org/docs/8.1/static/functions-string.html See
	 *      lpad in postgresql doc
	 */
	public String lpad(String stringToPad, int lengthToPadTo, String padCharacter);

	/**
	 * Make any string lower case and remove non-word characters (spaces, quote
	 * marks etc.)
	 */
	public String cleanString(String stringToClean);

	/**
	 * Removes all non-word characters apart from spaces. Doesn't change case
	 */
	public String rinseString(String stringToRinse);

	/**
	 * Return a the String representation of every object in the collection,
	 * with the joiner string inbetween them, e.g.
	 * 
	 * a collection of [Object1 Object2 Object3]
	 * 
	 * and joiner ", " would give
	 * 
	 * "Object1, Object2, Object3"
	 */
	public String joinWith(Collection<Object> collection, String joiner);

	/**
	 * Replace any linebreak characters in text with html paragraphs allowing
	 * proper display in a web page
	 */
	public String lineBreaksToParas(String stringToConvert);

	/**
	 * Replace &lt; and &gt; with < and > to allow HTML tags to work.
	 * 
	 * AgileBase replaces < and > with the two equivalents when saving data
	 */
	public String unencodeHtml(String string);
	
	/**
	 * Tells you whether the given template exists
	 * 
	 * @param templateFilename
	 *            e.g. "gui/customisations/companya/applications.vm"
	 */
	public boolean templateExists(String templateFilename);

	/**
	 * Return a list of files in the specified folder.
	 * 
	 * @param folder
	 *            Folder name relative to agileBase root, e.g.
	 *            "resources/icons/applications/tango"
	 */
	public List<File> listFiles(String folder);

	/**
	 * Causes an exception to be thrown for test purposes - intended for testing
	 * of error handling
	 */
	public void throwException() throws CantDoThatException;
}
