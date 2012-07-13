package org.glowacki;

/**
 * Thrown when an invalid date is encountered in <tt>CalendarParser</tt>.
 */
public class CalendarParserException
    extends Exception
{
    /**
     * Default date format exception.
     */
    public CalendarParserException() { super(); }
    /**
     * Date format exception.
     *
     * @param str error message
     */
    public CalendarParserException(String str) { super(str); }
}
