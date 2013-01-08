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

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for Key-Value-Pair (KVP) requests
 * 
 */
public class KvpHelper {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(KvpHelper.class);

    public static Map<String, String> getKvpParameterValueMap(HttpServletRequest req) {
        Map<String, String> kvp = new HashMap<String, String>();
        Enumeration<?> parameterNames = req.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            // all key names to lower case
            String key = (String) parameterNames.nextElement();
            kvp.put(checkKeyForPrefix(key), req.getParameter(key));
        }
        return kvp;
    }

    private static String checkKeyForPrefix(String key) {
        return key.replace("amp;", "").toLowerCase();
    }

    public static String checkParameterSingleValue(String parameterValue, String parameterName)
            throws OwsExceptionReport {
        if (checkParameterMultipleValues(parameterValue, parameterName).size() == 1) {
            return parameterValue;
        } else {
            String exceptionText = String.format("Value(s) of parameter '%s' (%s) is invalid!", parameterName, parameterValue);
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createInvalidParameterValueException(parameterName, exceptionText);
        }
    }

    public static List<String> checkParameterMultipleValues(String parameterValues, String parameterName)
            throws OwsExceptionReport {
        if (parameterValues.isEmpty()) {
            LOGGER.debug("Value(s) of parameter '{}' ({}) is/are missing!", parameterName, parameterValues);
            throw Util4Exceptions.createMissingParameterValueException(parameterName);
        }
        return Arrays.asList(parameterValues.split(","));
    }
    
    private KvpHelper() {
    }
}
