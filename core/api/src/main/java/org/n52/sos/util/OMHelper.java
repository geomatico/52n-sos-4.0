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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.features.SFConstants;
import org.n52.sos.ogc.om.values.BooleanValue;
import org.n52.sos.ogc.om.values.CategoryValue;
import org.n52.sos.ogc.om.values.CountValue;
import org.n52.sos.ogc.om.values.Value;
import org.n52.sos.ogc.om.values.QuantityValue;
import org.n52.sos.ogc.om.values.SweDataArrayValue;
import org.n52.sos.ogc.om.values.TextValue;

/**
 * Utility class for Observation and Measurement
 * 
 */
public final class OMHelper {

    public static String getNamespaceForFeatureType(String featureType) {
        if (featureType.equals(SFConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_POINT)
                || featureType.equals(SFConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_CURVE)
                || featureType.equals(SFConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_SURFACE)) {
            return SFConstants.NS_SAMS;
        } else if (featureType.equals(SFConstants.FT_SAMPLINGPOINT)
                || featureType.equals(SFConstants.FT_SAMPLINGCURVE)
                || featureType.equals(SFConstants.FT_SAMPLINGSURFACE)) {
            return SFConstants.NS_SA;
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

    public static String getObservationTypeFor(Value<?> value) {
        if (value instanceof BooleanValue) {
            return OMConstants.OBS_TYPE_TRUTH_OBSERVATION;
        } else if (value instanceof CategoryValue) {
            return OMConstants.OBS_TYPE_CATEGORY_OBSERVATION;
        } else if (value instanceof CountValue) {
            return OMConstants.OBS_TYPE_COUNT_OBSERVATION;
        } else if (value instanceof QuantityValue) {
            return OMConstants.OBS_TYPE_MEASUREMENT;
        } else if (value instanceof TextValue) {
            return OMConstants.OBS_TYPE_TEXT_OBSERVATION;
        } else if (value instanceof SweDataArrayValue) {
            return OMConstants.OBS_TYPE_SWE_ARRAY_OBSERVATION;
        }
        return OMConstants.OBS_TYPE_OBSERVATION;
    }

    private OMHelper() {
    }

    public static String getObservationTypeFor(QName resultModel) {
        if (resultModel.equals(OMConstants.RESULT_MODEL_MEASUREMENT)) {
            return OMConstants.OBS_TYPE_MEASUREMENT;
        } else if (resultModel.equals(OMConstants.RESULT_MODEL_CATEGORY_OBSERVATION)) {
            return OMConstants.OBS_TYPE_CATEGORY_OBSERVATION;
        } else if (resultModel.equals(OMConstants.RESULT_MODEL_GEOMETRY_OBSERVATION)) {
            return OMConstants.OBS_TYPE_GEOMETRY_OBSERVATION;
        } else if (resultModel.equals(OMConstants.RESULT_MODEL_COUNT_OBSERVATION)) {
            return OMConstants.OBS_TYPE_COUNT_OBSERVATION;
        } else if (resultModel.equals(OMConstants.RESULT_MODEL_TRUTH_OBSERVATION)) {
            return OMConstants.OBS_TYPE_TRUTH_OBSERVATION;
        } else if (resultModel.equals(OMConstants.RESULT_MODEL_TEXT_OBSERVATION)) {
            return OMConstants.OBS_TYPE_TEXT_OBSERVATION;
       }
       return OMConstants.OBS_TYPE_OBSERVATION;
    }

    /**
     * Get the QName for resultModels from observationType constant
     * 
     * @param resultModels4Offering
     *            Observation types
     * @return QNames for resultModel parameter
     */
    public static Collection<QName> getQNamesForResultModel(final Collection<String> resultModels4Offering) {
        final List<QName> resultModels = new ArrayList<QName>(9);
        for (final String string : resultModels4Offering) {
            resultModels.add(getQNameFor(string));
        }
        return resultModels;
    }
    
    public static QName getQNameFor(String observationType) {
        if (observationType.equals(OMConstants.OBS_TYPE_MEASUREMENT)) {
            return OMConstants.RESULT_MODEL_MEASUREMENT;
        } else if (observationType.equals(OMConstants.OBS_TYPE_CATEGORY_OBSERVATION)) {
            return OMConstants.RESULT_MODEL_CATEGORY_OBSERVATION;
        } else if (observationType.equals(OMConstants.OBS_TYPE_GEOMETRY_OBSERVATION)) {
            return OMConstants.RESULT_MODEL_GEOMETRY_OBSERVATION;
        } else if (observationType.equals(OMConstants.OBS_TYPE_COUNT_OBSERVATION)) {
            return OMConstants.RESULT_MODEL_COUNT_OBSERVATION;
        } else if (observationType.equals(OMConstants.OBS_TYPE_TRUTH_OBSERVATION)) {
            return OMConstants.RESULT_MODEL_TRUTH_OBSERVATION;
        } else if (observationType.equals(OMConstants.OBS_TYPE_TEXT_OBSERVATION)) {
            return OMConstants.RESULT_MODEL_TEXT_OBSERVATION;
        } else {
            return OMConstants.RESULT_MODEL_OBSERVATION;
        }
    }

    public static Object getEncodedResultModelFor(String resultModel) {
        QName qNameFor = getQNameFor(resultModel);
        StringBuilder builder = new StringBuilder();
        builder.append(qNameFor.getPrefix());
        builder.append(":");
        builder.append(qNameFor.getLocalPart());
        return builder.toString();
    }
}
