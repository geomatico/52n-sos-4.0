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
package org.n52.sos.ogc.sos;

import static java.util.Collections.unmodifiableSet;
import static org.n52.sos.util.CollectionHelper.set;

import java.util.Set;

import org.n52.sos.ogc.om.OMConstants;

/**
 * SosConstants holds all important and often used constants (e.g. name of the
 * getCapabilities operation) that are specific to SOS 1.0
 */
public interface Sos1Constants {

    String NS_SOS = "http://www.opengis.net/sos/1.0";

    /** Constant for the schema repository of the SOS */
    String SCHEMA_LOCATION_SOS = "http://schemas.opengis.net/sos/1.0.0/sosAll.xsd";

    /** Constant for the content types of the response formats */
    Set<String> RESPONSE_FORMATS = unmodifiableSet(set(OMConstants.CONTENT_TYPE_OM, SosConstants.CONTENT_TYPE_ZIP));

    /** Constant for actual implementing version */
    String SERVICEVERSION = "1.0.0";

    /**
     * the names of the SOS 1.0 operations that are not supported by all versions
     */
    enum Operations {
        GetFeatureOfInterestTime,
        DescribeFeatureType,
        DescribeObservationType,
        DescribeResultModel,
        RegisterSensor;
    }

    /**
     * enum with names of SOS 1.0 Capabilities sections not supported by all versions
     */
    enum CapabilitiesSections {
        Filter_Capabilities;
    }

    /**
     * enum with parameter names for SOS 1.0 getObservation request not supported by all versions
     */
    enum GetObservationParams {
        eventTime;
    }

    /**
     * enum with parameter names for SOS 1.0 insertObservation request not supported by all versions
     */
    enum InsertObservationParams {
        AssignedSensorId,
        Observation;
    }

    /**
     * enum with parameter names for SOS 1.0 getObservation request not supported by all versions
     */
    enum DescribeSensorParams {
        outputFormat,
        time;
    }

    /**
     * enum with parameter names for SOS 1.0 getFeatureOfInterest request not supported by all versions
     */
    enum GetFeatureOfInterestParams {
        featureOfInterestID,
        location;
    }

    /**
     * enum with parameter names for getFeatureOfInterestTime request
     */
    enum GetFeatureOfInterestTimeParams {
        featureOfInterestID,
        location,
        observedProperty,
        procedure;
    }

    /**
     * enum with parameter names for registerSensor request
     */
    enum RegisterSensorParams {
        SensorDescription,
        ObservationTemplate;
    }

    /**
     * enum with parameter names for SOS 1.0 getObservationById request not supported by all versions
     */
    enum GetObservationByIdParams {
        srsName,
        ObservationId,
        responseFormat,
        resultModel,
        responseMode,
        SortBy;
    }
}
