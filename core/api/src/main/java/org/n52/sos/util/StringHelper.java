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

import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.ogc.ows.OwsExceptionReport;

public final class StringHelper {

    public static StringBuffer join(final CharSequence sep, final StringBuffer buff, final Iterable<?> src) {
        final Iterator<?> it = src.iterator();
        if (it.hasNext()) {
            buff.append(it.next());
        }
        while (it.hasNext()) {
            buff.append(sep).append(it.next());
        }
        return buff;
    }

    public static String join(final CharSequence sep, final Iterable<?> src) {
        return join(sep, new StringBuffer(), src).toString();
    }

    public static StringBuffer join(final CharSequence sep, final StringBuffer buff, final Object... src) {
        return join(sep, buff, Arrays.asList(src));
    }

    public static String join(final CharSequence sep, final Object... src) {
        return join(sep, Arrays.asList(src));
    }

    /**
     * @param toNormalize
     *            the string to normalize
     * @return a normalized String for use in a file path, i.e. all
     *         [\,/,:,*,?,",<,>,;] characters are replaced by '_'.
     */
    public static String normalize(final String toNormalize) {
        // toNormalize = toNormalize.replaceAll("ä", "ae");
        // toNormalize = toNormalize.replaceAll("ö", "oe");
        // toNormalize = toNormalize.replaceAll("ü", "ue");
        // toNormalize = toNormalize.replaceAll("Ä", "AE");
        // toNormalize = toNormalize.replaceAll("Ö", "OE");
        // toNormalize = toNormalize.replaceAll("Ü", "UE");
        // toNormalize = toNormalize.replaceAll("ß", "ss");
        return toNormalize.replaceAll("[\\\\/:\\*?\"<>;,#%=@]", "_");
    }

    /**
     * Sanitize a string for use in an NCName
     * @param string
     * @return
     */
    public static String sanitizeNcName(final String string) {
        String sanitizedString = string.replaceAll("[ !\"#$%&'()*+,/:;<=>?@\\[\\]^`{|}~]","_");
        return sanitizedString.replaceFirst("^[\\-\\.0-9]","_");
    }

    /**
     * Check if string is not null and not empty
     * 
     * @param string
     *            string to check
     * 
     * @return empty or not
     */
    public static boolean isNotEmpty(final String string) {
        return string != null && !string.isEmpty();
    }

    private StringHelper() {
    }

    /**
     * Check if string is null or empty
     * 
     * @param string
     *            string to check
     * @return <tt>true</tt>, if the string is null or empty
     */
    public static boolean isNullOrEmpty(final String string) {
        return string == null || string.isEmpty();
    }

    public static String convertStreamToString(InputStream is, String charsetName) throws OwsExceptionReport {
        try {
            Scanner scanner;
            if (isNotEmpty(charsetName)) {
                scanner = new Scanner(is, charsetName);
            } else {
                scanner = new Scanner(is);
            }
            scanner.useDelimiter("\\A");
            if (scanner.hasNext()) {
                return scanner.next();
            }
        } catch (NoSuchElementException nsee) {
            throw new NoApplicableCodeException().causedBy(nsee).withMessage(
                    "Error while reading content of HTTP request: %s", nsee.getMessage());
        }
        return "";
    }

    public static String convertStreamToString(InputStream descriptionXmlAsStream) throws OwsExceptionReport {
        return convertStreamToString(descriptionXmlAsStream, null);
    }
}
