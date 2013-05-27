/**
 * Copyright (C) 2013
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */
package org.n52.sos.util;

import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.format.ISOPeriodFormat;
import org.n52.sos.exception.ows.concrete.DateTimeException;
import org.n52.sos.exception.ows.concrete.DateTimeFormatException;
import org.n52.sos.exception.ows.concrete.DateTimeParseException;
import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.gml.time.ITime.TimeFormat;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for Time formatting and parsing. Uses Joda Time.
 * 
 */
public final class DateTimeHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(DateTimeHelper.class);

    /**
     * response format for time
     */
    private static String responseFormat;

    private static final String YMD_RESPONSE_FORMAT = "yyyy-MM-dd";

    private static final String YM_RESPONSE_FORMAT = "yyyy-MM";

    private static final String Y_RESPONSE_FORMAT = "yyyy";

    /**
     * lease value
     */
    private static int lease;

    /**
     * Parses a time String to a Joda Time DateTime object
     * 
     * @param timeString
     *            Time String
     * @return DateTime object
     * @throws DateTimeException
     *             IF an error occurs.
     */
    public static DateTime parseIsoString2DateTime(final String timeString) throws DateTimeParseException {
        checkForValidity(timeString);
        if (timeString == null || timeString.isEmpty()) {
            return null;
        }
        try {
            if (timeString.contains("+") || Pattern.matches("-\\d", timeString) || timeString.contains("Z")
                    || timeString.contains("z")) {
                return ISODateTimeFormat.dateOptionalTimeParser().withOffsetParsed().parseDateTime(timeString);
            } else {
                return ISODateTimeFormat.dateOptionalTimeParser().withZone(DateTimeZone.UTC).parseDateTime(timeString);
            }
        } catch (final RuntimeException uoe) {
            throw new DateTimeParseException(timeString, uoe);
        }
    }

    private static void checkForValidity(final String timeString) throws DateTimeParseException {
        if (!(timeString.length() == 4 || timeString.length() == 7 || timeString.length() >= 10)) {
            throw new DateTimeParseException(timeString);
        }
    }

    /**
     * Formats the given ITime to ISO 8601 string.
     * 
     * @param time
     *            an {@link ITime} object to be formatted
     * @return an ISO 8601 conform {@link String}.
     * @throws IllegalArgumentException
     *             in the case of receiving <tt>null</tt> or not supported
     *             types.
     * @see {@link #formatDateTime2IsoString}
     */
    public static String format(final ITime time) {
        if (time != null) {
            if (time instanceof TimeInstant) {
                return formatDateTime2IsoString(((TimeInstant) time).getValue());
            } else if (time instanceof TimePeriod) {
                return String.format("%s/%s", formatDateTime2IsoString(((TimePeriod) time).getStart()),
                                     formatDateTime2IsoString(((TimePeriod) time).getEnd()));
            }
        }
        final String exceptionMsg =
                String.format("Given ITime object is not valid: %s", time);
        LOGGER.debug(exceptionMsg);
        throw new IllegalArgumentException(exceptionMsg);
    }

    /**
     * Formats a DateTime to a ISO-8601 String
     * 
     * @param dateTime
     *            Time object
     * @return ISO-8601 formatted time String
     */
    public static String formatDateTime2IsoString(final DateTime dateTime) {
        if (dateTime == null) {
            return new DateTime(0000, 01, 01, 00, 00, 00, 000, DateTimeZone.UTC).toString().replace("Z", "+00:00");
        }
        return dateTime.toString();
    }

    /**
     * Formats a DateTime to a String using the response format
     * 
     * @param dateTime
     *            Time object
     * @return Response formatted time String
     *
     * @throws DateTimeFormatException If an error occurs.
     */
    public static String formatDateTime2ResponseString(final DateTime dateTime) throws DateTimeFormatException {
        return formatDateTime2FormattedString(dateTime, responseFormat);
    }

    public static String formatDateTime2String(final DateTime dateTime, final TimeFormat timeFormat) throws DateTimeFormatException {
        switch (timeFormat) {
        case Y:
            return formatDateTime2YearDateString(dateTime);
        case YM:
            return formatDateTime2YearMonthDateString(dateTime);
        case YMD:
            return formatDateTime2YearMonthDayDateStringYMD(dateTime);
        default:
            return formatDateTime2ResponseString(dateTime);
        }
    }

    /**
     * Formats a DateTime to a String using specified format
     * 
     * @param dateTime
     *            Time object
     * @param dateFormat the date time format
     *
     * @return Specified formatted time String
     *
     * @throws DateTimeFormatException If an error occurs.
     */
    public static String formatDateTime2FormattedString(final DateTime dateTime, final String dateFormat) throws DateTimeFormatException {
        try {
            if (dateFormat == null || dateFormat.isEmpty()) {
                return formatDateTime2IsoString(dateTime);
            } else {
                if (dateTime == null) {
                    return new DateTime(0000, 01, 01, 00, 00, 00, 000, DateTimeZone.UTC).toString(DateTimeFormat
                            .forPattern(dateFormat));
                }
                return dateTime.toString(DateTimeFormat.forPattern(dateFormat)).replace("Z", "+00:00");
            }
        } catch (final IllegalArgumentException iae) {
            throw new DateTimeFormatException(dateTime, iae);
        }
    }

    /**
     * formats a DateTime to a string with year-month-day.
     * 
     * @param dateTime
     *            The DateTime.
     * @return Returns formatted time String.
     *
     * @throws DateTimeFormatException
     */
    public static String formatDateTime2YearMonthDayDateStringYMD(final DateTime dateTime) throws DateTimeFormatException {
        try {
            DateTime result;
            if (dateTime == null) {
                result = new DateTime(0000, 01, 01, 00, 00, 00, 000, DateTimeZone.UTC);
            } else {
                result = new DateTime(dateTime.getMillis(), DateTimeZone.UTC);
            }
            return result.toString(DateTimeFormat.forPattern(YMD_RESPONSE_FORMAT));
        } catch (final IllegalArgumentException iae) {
            throw new DateTimeFormatException(dateTime, iae);
        }
    }

    /**
     * formats a DateTime to a string with year-month.
     * 
     * @param dateTime
     *            The DateTime.
     * @return Returns formatted time String.
     *
     * @throws DateTimeFormatException
     */
    public static String formatDateTime2YearMonthDateString(final DateTime dateTime) throws DateTimeFormatException {
        try {
            DateTime result;
            if (dateTime == null) {
                result = new DateTime(0000, 01, 01, 00, 00, 00, 000, DateTimeZone.UTC);
            } else {
                result = new DateTime(dateTime.getMillis(), DateTimeZone.UTC);
            }
            return result.toString(DateTimeFormat.forPattern(YM_RESPONSE_FORMAT));
        } catch (final IllegalArgumentException iae) {
            throw new DateTimeFormatException(dateTime, iae);
        }
    }

    /**
     * formats a DateTime to a string with year.
     * 
     * @param dateTime
     *            The DateTime.
     * @return Returns formatted time String.
     *
     * @throws DateTimeFormatException
     */
    public static String formatDateTime2YearDateString(final DateTime dateTime) throws DateTimeFormatException {
        try {
            DateTime result;
            if (dateTime == null) {
                result = new DateTime(0000, 01, 01, 00, 00, 00, 000, DateTimeZone.UTC);
            } else {
                result = new DateTime(dateTime.getMillis(), DateTimeZone.UTC);
            }
            return result.toString(DateTimeFormat.forPattern(Y_RESPONSE_FORMAT));
        } catch (final IllegalArgumentException iae) {
            throw new DateTimeFormatException(dateTime, iae);
        }
    }

    /**
     * Set the time object to the end values (seconds, minutes, hours, days,..)
     * if the time Object has not all values
     * 
     * @param dateTime
     *            Time object
     * @param isoTimeLength
     *            Length of the time object
     * @return Modified time object.
     */
    public static DateTime setDateTime2EndOfDay4RequestedEndPosition(final DateTime dateTime, final int isoTimeLength) {
        switch (isoTimeLength) {
            // year
            case 4:
                return dateTime.plusYears(1).minusMillis(1);
            // year, month
            case 7:
                return dateTime.plusMonths(1).minusMillis(1);
            // year, month, day
            case 10:
                return dateTime.plusDays(1).minusMillis(1);
            // year, month, day, hour
            case 13:
                return dateTime.plusHours(1).minusMillis(1);
            // year, month, day, hour, minute
            case 16:
                return dateTime.plusMinutes(1).minusMillis(1);
            // year, month, day, hour, minute, second
            case 19:
                return dateTime.plusSeconds(1).minusMillis(1);
            default:
                return dateTime;
        }
    }

    /**
     * Parse a duration from a String representation
     * 
     * @param stringDuration
     *            Duration as String
     * @return Period object of duration
     */
    public static Period parseDuration(final String stringDuration) {
        return ISOPeriodFormat.standard().parsePeriod(stringDuration);
    }

    /**
     * Calculates the expire time for a time object
     * 
     * @param start
     *            Time object
     * @return Expire time
     */
    public static DateTime calculateExpiresDateTime(final DateTime start) {
        return start.plusMinutes(lease);
    }

    /**
     * Set the response format
     * 
     * @param responseFormat
     *            Defined response format
     */
    public static void setResponseFormat(final String responseFormat) {
        DateTimeHelper.responseFormat = responseFormat;
    }

    /**
     * Set the lease value
     * 
     * @param lease
     *            Defined lease value
     */
    public static void setLease(final int lease) {
        DateTimeHelper.lease = lease;
    }

    /**
     * Hide utility constructor
     */
    private DateTimeHelper() {
    }
}
