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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaHelper {
    
    /** logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(JavaHelper.class);
    
    /**
     * hexadecimal values
     */
    private static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
            'E', 'F' };

    /**
     * Message digest for generating single identifier
     */
    private static MessageDigest MESSAGE_DIGEST;

    /**
     * Instantiation of the message digest
     */
    static {
        try {
            MESSAGE_DIGEST = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException nsae) {
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
    public static String generateID(String message) {
        long autoGeneratredID = new DateTime().getMillis();
        String concate = message + Long.toString(autoGeneratredID);
        return bytesToHex(MESSAGE_DIGEST.digest(concate.getBytes()));
    }

    /**
     * Transforms byte to hex representation
     * 
     * @param b
     *            bytes
     * @return hex
     */
    private static String bytesToHex(byte[] b) {
        StringBuilder buf = new StringBuilder();
        for (int j = 0; j < b.length; j++) {
            buf.append(HEX_DIGITS[(b[j] >> 4) & 0x0f]);
            buf.append(HEX_DIGITS[b[j] & 0x0f]);
        }
        return buf.toString();
    }
    
    public static List<String> conjunctCollections(
            Collection<String> list1, Collection<String> list2) {
        Set<String> conjunction = null;
        if (list2 != null && list1 != null) {
            if (!list2.isEmpty() && !list1.isEmpty()) {
                conjunction = new HashSet<String>(0);
                for (String object : list1) {
                    if (list2.contains(object)) {
                        conjunction.add(object);
                    }
                }
            } else if (list2.isEmpty() && !list1.isEmpty()) {
                new HashSet<String>(list1);
            } else if (!list2.isEmpty() && list1.isEmpty()) {
                new HashSet<String>(list2);
            }
        }
        return new ArrayList<String>(conjunction);
    }
    
    /**
     * @param toNormalize the string to normalize
     * @return a normalized String for use in a file path, i.e. all
     *         [\,/,:,*,?,",<,>,;] characters are replaced by '_'.
     */
    public static String normalize(String toNormalize) {
        // toNormalize = toNormalize.replaceAll("ä", "ae");
        // toNormalize = toNormalize.replaceAll("ö", "oe");
        // toNormalize = toNormalize.replaceAll("ü", "ue");
        // toNormalize = toNormalize.replaceAll("Ä", "AE");
        // toNormalize = toNormalize.replaceAll("Ö", "OE");
        // toNormalize = toNormalize.replaceAll("Ü", "UE");
        // toNormalize = toNormalize.replaceAll("ß", "ss");
        return toNormalize.replaceAll("[\\\\,/,:,\\*,?,\",<,>,;,#,%,=,@]", "_");
    }
    
}
