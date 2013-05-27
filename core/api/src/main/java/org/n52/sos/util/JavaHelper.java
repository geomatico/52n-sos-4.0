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

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JavaHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaHelper.class);

    /**
     * hexadecimal values
     */
    private static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
            'E', 'F' };

    /**
     * Message digest for generating single identifier
     */
    private static MessageDigest messageDigest;

    /**
     * Instantiation of the message digest
     */
    static {
        try {
            messageDigest = MessageDigest.getInstance("SHA1");
        } catch (final NoSuchAlgorithmException nsae) {
            LOGGER.error("Error while getting SHA-1 messagedigest!", nsae);
        }
    }

    /**
     * Generates a sensor id from description and current time as long.
     * 
     * @param message
     *            sensor description
     * @return generated sensor id as hex SHA-1.
     */
    public static String generateID(final String message) {
        final long autoGeneratredID = new DateTime().getMillis();
        final String concate = message + Long.toString(autoGeneratredID);
        return bytesToHex(messageDigest.digest(concate.getBytes()));
    }

    /**
     * Transforms byte to hex representation
     * 
     * @param b
     *            bytes
     * @return hex
     */
    private static String bytesToHex(final byte[] b) {
        final StringBuilder buf = new StringBuilder();
        for (final byte element : b) {
            buf.append(HEX_DIGITS[(element >> 4) & 0x0f]);
            buf.append(HEX_DIGITS[element & 0x0f]);
        }
        return buf.toString();
    }

    public static void appendTextToStringBuilderWithLineBreak(final StringBuilder stringBuilder, final String message) {
        if (stringBuilder != null && message != null && !message.isEmpty()) {
            stringBuilder.append(message);
            stringBuilder.append("\n");
        }
    }

    /**
     * return Object value as String
     * 
     * @param object
     *            to get as String
     * @return String value
     */
    public static String asString(final Object object) {
        if (object instanceof String) {
            return (String) object;
        } else if (object instanceof BigDecimal) {
            final BigDecimal bdValue = (BigDecimal) object;
            return Double.toString(bdValue.doubleValue());
        } else if (object instanceof Double) {
            return ((Double) object).toString();
        }
        return ""; // TODO why not object.toString()?
    }

    /**
     * return Object value as Double
     * 
     * @param object
     *            to get as Double
     * @return Double value
     */
    public static Double asDouble(final Object object) {
        if (object instanceof String) {
            return Double.parseDouble((String) object);
        } else if (object instanceof BigDecimal) {
            final BigDecimal bdValue = (BigDecimal) object;
            return bdValue.doubleValue();
        } else if (object instanceof Double) {
            return (Double) object;
        }
        return new Double(Double.NaN);
    }

    private JavaHelper() {
    }
}
