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

import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.features.SFConstants;

/**
 * Utility class for Observation and Measurement
 * 
 */
public class OMHelper {

    public static String getNamespaceForFeatureType(String featureType) {
        if (featureType.equals(SFConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_POINT)
                || featureType.equals(SFConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_CURVE)
                || featureType.equals(SFConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_SURFACE)) {
            return SFConstants.NS_SAMS;
        }
        return null;
    }

    public static boolean checkOMResponseFormat(String responseFormat) {
        if (responseFormat != null) {
            return responseFormat.equals(OMConstants.CONTENT_TYPE_OM)
                    || responseFormat.equals(OMConstants.CONTENT_TYPE_OM_2)
                    || responseFormat.equals(OMConstants.RESPONSE_FORMAT_OM)
                    || responseFormat.equals(OMConstants.RESPONSE_FORMAT_OM_2);
        }
        return true;
    }

    private OMHelper() {
    }
}
