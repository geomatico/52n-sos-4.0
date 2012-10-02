/**
 * Copyright (C) 2012
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
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for Time formatting and parsing. Uses Joda Time.
 * 
 */
public class DateTimeHelper {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DateTimeHelper.class);

    /**
     * response format for time
     */
    private static String responseFormat;

    /**
     * lease value
     */
    private static int lease;

    /**
     * Hide utility constructor
     */
    private DateTimeHelper() {
        super();
    }

    /**
     * Parses a time String to a Joda Time DateTime object
     * 
     * @param timeString
     *            Time String
     * @return DateTime object
     * @throws OwsExceptionReport
     *             IF an error occurs.
     */
    public static DateTime parseIsoString2DateTime(String timeString) throws DateTimeException {
        if (timeString == null || timeString.equals("")) {
            return null;
        }
        try {
            if (timeString.contains("+") || Pattern.matches("-\\d", timeString) || timeString.contains("Z")
                    || timeString.contains("z")) {
                return ISODateTimeFormat.dateOptionalTimeParser().withOffsetParsed().parseDateTime(timeString);
            } else {
                return ISODateTimeFormat.dateOptionalTimeParser().withZone(DateTimeZone.UTC).parseDateTime(timeString);
            }
        } catch (IllegalArgumentException iae) {
            String exceptionText = "Error while parse time String to DateTime!";
            LOGGER.error(exceptionText, iae);
            throw new DateTimeException(exceptionText, iae);
        } catch (UnsupportedOperationException uoe) {
            String exceptionText = "Error while parse time String to DateTime!";
            LOGGER.error(exceptionText, uoe);
            throw new DateTimeException(exceptionText, uoe);
        }
    }

    /**
     * Formats a DateTime to a ISO-8601 String
     * 
     * @param dateTime
     *            Time object
     * @return ISO-8601 formatted time String
     */
    public static String formatDateTime2IsoString(DateTime dateTime) {
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
     * @throws OwsExceptionReport
     *             If an error occurs.
     */
    public static String formatDateTime2ResponseString(DateTime dateTime) throws DateTimeException {
        return formatDateTime2FormattedString(dateTime, responseFormat);
    }

    /**
     * Formats a DateTime to a String using specified format
     * 
     * @param dateTime
     *            Time object
     * @return Specified formatted time String
     * @throws OwsExceptionReport
     *             If an error occurs.
     */
    public static String formatDateTime2FormattedString(DateTime dateTime, String dateFormat)
            throws DateTimeException {
        try {
            if (dateFormat == null || dateFormat.equals("")) {
                return formatDateTime2IsoString(dateTime);
            } else {
                if (dateTime == null) {
                    return new DateTime(0000, 01, 01, 00, 00, 00, 000, DateTimeZone.UTC).toString(DateTimeFormat
                            .forPattern(dateFormat));
                }
                return dateTime.toString(DateTimeFormat.forPattern(dateFormat)).replace("Z", "+00:00");
            }
        } catch (IllegalArgumentException iae) {
            String exceptionText = "Error while parse time String to DateTime!";
            LOGGER.error(exceptionText, iae);
            throw new DateTimeException(exceptionText, iae);
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
    public static DateTime setDateTime2EndOfDay4RequestedEndPosition(DateTime dateTime, int isoTimeLength) {
        switch (isoTimeLength) {
        // year
        case 4:
            dateTime = dateTime.plusYears(1).minusMillis(1);
            break;
        // year, month
        case 7:
            dateTime = dateTime.plusMonths(1).minusMillis(1);
            break;
        // year, month, day
        case 10:
            dateTime = dateTime.plusDays(1).minusMillis(1);
            break;
        // year, month, day, hour
        case 13:
            dateTime = dateTime.plusHours(1).minusMillis(1);
            break;
        // year, month, day, hour, minute
        case 16:
            dateTime = dateTime.plusMinutes(1).minusMillis(1);
            break;
        // year, month, day, hour, minute, second
        case 19:
            dateTime = dateTime.plusSeconds(1).minusMillis(1);
            break;

        default:
            break;
        }
        return dateTime;
    }

    /**
     * Parse a duration from a String representation
     * 
     * @param stringDuration
     *            Duration as String
     * @return Period object of duration
     */
    public static Period parseDuration(String stringDuration) {
        return ISOPeriodFormat.standard().parsePeriod(stringDuration);
    }

    /**
     * Calculates the expire time for a time object
     * 
     * @param start
     *            Time object
     * @return Expire time
     */
    public static DateTime calculateExpiresDateTime(DateTime start) {
        DateTime end = null;
        end = start.plusMinutes(lease);
        return end;
    }

    /**
     * Set the response format
     * 
     * @param responseFormat
     *            Defined response format
     */
    public static void setResponseFormat(String responseFormat) {
        DateTimeHelper.responseFormat = responseFormat;
    }

    /**
     * Set the lease value
     * 
     * @param lease
     *            Defined lease value
     */
    public static void setLease(int lease) {
        DateTimeHelper.lease = lease;
    }
}
