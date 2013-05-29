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

import javax.xml.namespace.QName;

import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.util.SchemaLocation;

/**
 * SosConstants holds all important and often used constants (e.g. name of the
 * getCapabilities operation) that are specific to SOS 2.0
 */
public interface Sos2Constants {

    String NS_SOS_20 = "http://www.opengis.net/sos/2.0";

    String SCHEMA_LOCATION_URL_SOS = "http://schemas.opengis.net/sos/2.0/sos.xsd";

    String SCHEMA_LOCATION_URL_SOS_GET_CAPABILITIES = "http://schemas.opengis.net/sos/2.0/sosGetCapabilities.xsd";

    String SCHEMA_LOCATION_URL_SOS_GET_FEATURE_OF_INTEREST =
            "http://schemas.opengis.net/sos/2.0/sosGetFeatureOfInterest.xsd";

    String SCHEMA_LOCATION_URL_SOS_GET_OBSERVATION = "http://schemas.opengis.net/sos/2.0/sosGetObservation.xsd";

    String SCHEMA_LOCATION_URL_SOS_GET_OBSERVATION_BY_ID =
            "http://schemas.opengis.net/sos/2.0/sosGetObservationById.xsd";

    String SCHEMA_LOCATION_URL_SOS_GET_RESULT = "http://schemas.opengis.net/sos/2.0/sosGetResult.xsd";

    String SCHEMA_LOCATION_URL_SOS_GET_RESULT_TEMPLATE = "http://schemas.opengis.net/sos/2.0/sosGetResultTemplate.xsd";

    String SCHEMA_LOCATION_URL_SOS_INSERT_OBSERVATION = "http://schemas.opengis.net/sos/2.0/sosInsertObservation.xsd";

    String SCHEMA_LOCATION_URL_SOS_INSERT_RESULT = "http://schemas.opengis.net/sos/2.0/sosInsertResult.xsd";

    String SCHEMA_LOCATION_URL_SOS_INSERT_RESULT_TEMPLATE =
            "http://schemas.opengis.net/sos/2.0/sosInsertResultTemplate.xsd";

    String SCHEMA_LOCATION_URL_SOS_INSERT_SENSOR = "http://schemas.opengis.net/sos/2.0/sosInsertSensor.xsd";

    String SCHEMA_LOCATION_URL_SOS_INSERTION_CAPABILITIES =
            "http://schemas.opengis.net/sos/2.0/sosInsertionCapabilities.xsd#InsertionCapabilities";

    SchemaLocation SOS_SCHEMA_LOCATION = new SchemaLocation(NS_SOS_20,
            SCHEMA_LOCATION_URL_SOS);
    
    SchemaLocation SOS_GET_CAPABILITIES_SCHEMA_LOCATION = new SchemaLocation(NS_SOS_20,
            SCHEMA_LOCATION_URL_SOS_GET_CAPABILITIES);

    SchemaLocation SOS_GET_FEATURE_OF_INTEREST_SCHEMA_LOCATION = new SchemaLocation(NS_SOS_20,
            SCHEMA_LOCATION_URL_SOS_GET_FEATURE_OF_INTEREST);

    SchemaLocation SOS_GET_OBSERVATION_SCHEMA_LOCATION = new SchemaLocation(NS_SOS_20,
            SCHEMA_LOCATION_URL_SOS_GET_OBSERVATION);

    SchemaLocation SOS_GET_OBSERVATION_BY_ID_SCHEMA_LOCATION = new SchemaLocation(NS_SOS_20,
            SCHEMA_LOCATION_URL_SOS_GET_OBSERVATION_BY_ID);

    SchemaLocation SOS_GET_RESULT_SCHEMA_LOCATION = new SchemaLocation(NS_SOS_20, SCHEMA_LOCATION_URL_SOS_GET_RESULT);

    SchemaLocation SOS_GET_RESULT_TEMPLATE_SCHEMA_LOCATION = new SchemaLocation(NS_SOS_20,
            SCHEMA_LOCATION_URL_SOS_GET_RESULT_TEMPLATE);

    SchemaLocation SOS_INSERT_OBSERVATION_SCHEMA_LOCATION = new SchemaLocation(NS_SOS_20,
            SCHEMA_LOCATION_URL_SOS_INSERT_OBSERVATION);

    SchemaLocation SOS_INSERT_RESULT_SCHEMA_LOCATION = new SchemaLocation(NS_SOS_20,
            SCHEMA_LOCATION_URL_SOS_INSERT_RESULT);

    SchemaLocation SOS_INSERT_RESULT_TEMPLATE_SCHEMA_LOCATION = new SchemaLocation(NS_SOS_20,
            SCHEMA_LOCATION_URL_SOS_INSERT_RESULT_TEMPLATE);

    SchemaLocation SOS_INSERT_SNSOR_SCHEMA_LOCATION = new SchemaLocation(NS_SOS_20,
            SCHEMA_LOCATION_URL_SOS_INSERT_SENSOR);

    SchemaLocation SOS_INSERTION_CAPABILITIES_SCHEMA_LOCATION = new SchemaLocation(NS_SOS_20,
            SCHEMA_LOCATION_URL_SOS_INSERTION_CAPABILITIES);

    /**
     * Constant for the content types of the response formats
     */
    Set<String> RESPONSE_FORMATS =
            unmodifiableSet(set(OMConstants.RESPONSE_FORMAT_OM_2, SosConstants.CONTENT_TYPE_ZIP));

    String EN_CAPABILITIES = "Capabilities";

    String EN_GET_CAPABILITIES = "GetCapabilities";

    String EN_GET_FEATURE_OF_INTEREST = "GetFeatureOfInterest";

    String EN_GET_FEATURE_OF_INTEREST_RESPONSE = "GetFeatureOfInterestResponse";

    String EN_GET_OBSERVATION = "GetObservation";

    String EN_GET_OBSERVATION_BY_ID = "GetObservationById";

    String EN_GET_OBSERVATION_BY_ID_RESPONSE = "GetObservationByIdResponse";

    String EN_GET_OBSERVATION_RESPONSE = "GetObservationResponse";

    String EN_GET_RESULT = "GetResult";

    String EN_GET_RESULT_RESPONSE = "GetResultResponse";

    String EN_GET_RESULT_TEMPLATE = "GetResultTemplate";

    String EN_GET_RESULT_TEMPLATE_RESPONSE = "GetResultTemplateResponse";

    String EN_INSERT_OBSERVATION = "InsertObservation";

    String EN_INSERT_OBSERVATION_RESPONSE = "InsertObservationResponse";

    String EN_INSERT_RESULT = "InsertResult";

    String EN_INSERT_RESULT_RESPONSE = "InsertResultResponse";

    String EN_INSERT_RESULT_TEMPLATE = "InsertResultTemplate";

    String EN_INSERT_RESULT_TEMPLATE_RESPONSE = "InsertResultTemplateResponse";

    String EN_OBSERVATION_OFFERING = "ObservationOffering";

    String EN_OBSERVED_AREA = "observedArea";

    String EN_PHENOMENON_TIME = "phenomenonTime";

    String EN_RESULT_TIME = "resultTime";

    String EN_RESPONSE_FORMAT = "responseFormat";

    String EN_OBSERVATION_TYPE = "observationType";

    String EN_FEATURE_OF_INTEREST_TYPE = "featureOfInterestType";

    String EN_SOS_INSERTION_METADATA = "SosInsertionMetadata";

    QName QN_CAPABILITIES = new QName(NS_SOS_20, EN_CAPABILITIES, SosConstants.NS_SOS_PREFIX);

    QName QN_GET_CAPABILITIES = new QName(NS_SOS_20, EN_GET_CAPABILITIES, SosConstants.NS_SOS_PREFIX);

    QName QN_GET_FEATURE_OF_INTEREST = new QName(NS_SOS_20, EN_GET_FEATURE_OF_INTEREST, SosConstants.NS_SOS_PREFIX);

    QName QN_GET_FEATURE_OF_INTEREST_RESPONSE = new QName(NS_SOS_20, EN_GET_FEATURE_OF_INTEREST_RESPONSE,
            SosConstants.NS_SOS_PREFIX);

    QName QN_GET_OBSERVATION = new QName(NS_SOS_20, EN_GET_OBSERVATION, SosConstants.NS_SOS_PREFIX);

    QName QN_GET_OBSERVATION_BY_ID = new QName(NS_SOS_20, EN_GET_OBSERVATION_BY_ID, SosConstants.NS_SOS_PREFIX);

    QName QN_GET_OBSERVATION_BY_ID_RESPONSE = new QName(NS_SOS_20, EN_GET_OBSERVATION_BY_ID_RESPONSE,
            SosConstants.NS_SOS_PREFIX);

    QName QN_GET_OBSERVATION_RESPONSE = new QName(NS_SOS_20, EN_GET_OBSERVATION_RESPONSE, SosConstants.NS_SOS_PREFIX);

    QName QN_GET_RESULT = new QName(NS_SOS_20, EN_GET_RESULT, SosConstants.NS_SOS_PREFIX);

    QName QN_GET_RESULT_RESPONSE = new QName(NS_SOS_20, EN_GET_RESULT_RESPONSE, SosConstants.NS_SOS_PREFIX);

    QName QN_GET_RESULT_TEMPLATE = new QName(NS_SOS_20, EN_GET_RESULT_TEMPLATE, SosConstants.NS_SOS_PREFIX);

    QName QN_GET_RESULT_TEMPLATE_RESPONSE = new QName(NS_SOS_20, EN_GET_RESULT_TEMPLATE_RESPONSE,
            SosConstants.NS_SOS_PREFIX);

    QName QN_INSERT_OBSERVATION = new QName(NS_SOS_20, EN_INSERT_OBSERVATION, SosConstants.NS_SOS_PREFIX);

    QName QN_INSERT_OBSERVATION_RESPONSE = new QName(NS_SOS_20, EN_INSERT_OBSERVATION_RESPONSE,
            SosConstants.NS_SOS_PREFIX);

    QName QN_INSERT_RESULT = new QName(NS_SOS_20, EN_INSERT_RESULT, SosConstants.NS_SOS_PREFIX);

    QName QN_INSERT_RESULT_RESPONSE = new QName(NS_SOS_20, EN_INSERT_RESULT_RESPONSE, SosConstants.NS_SOS_PREFIX);

    QName QN_INSERT_RESULT_TEMPLATE = new QName(NS_SOS_20, EN_INSERT_RESULT_TEMPLATE, SosConstants.NS_SOS_PREFIX);

    QName QN_INSERT_RESULT_TEMPLATE_RESPONSE = new QName(NS_SOS_20, EN_INSERT_RESULT_TEMPLATE_RESPONSE,
            SosConstants.NS_SOS_PREFIX);

    QName QN_OBSERVATION_OFFERING = new QName(NS_SOS_20, EN_OBSERVATION_OFFERING, SosConstants.NS_SOS_PREFIX);

    QName QN_SOS_INSERTION_METADATA = new QName(NS_SOS_20, EN_SOS_INSERTION_METADATA, SosConstants.NS_SOS_PREFIX);

    /**
     * The names of the extensions that we know off and might support
     */
    enum Extensions {
        Subsetting;
    }

    /** Constant for actual implementing version */
    String SERVICEVERSION = "2.0.0";

    /**
     * the names of the SOS 2.0 operations that are not supported by all
     * versions
     */
    enum Operations {
        InsertSensor, DeleteSensor, InsertResult, InsertResultTemplate, GetResultTemplate, UpdateSensorDescription;
    }

    /**
     * enum with names of SOS 2.0 Capabilities sections not supported by all
     * versions
     */
    enum CapabilitiesSections {
        FilterCapabilities, InsertionCapabilities;
    }

    /**
     * enum with parameter names for getObservation request
     */
    enum GetObservationParams {
        temporalFilter, spatialFilter, namespaces;
    }

    /**
     * enum with parameter names for SOS 2.0 insertObservation request not
     * supported by all versions
     */
    enum InsertObservationParams {
        offering, observation, observationType, observedProperty, procedure, featureOfInterest;
    }

    /**
     * enum with parameter names for SOS 2.0 getObservation request not
     * supported by all versions
     */
    enum DescribeSensorParams {
        procedureDescriptionFormat, validTime;
    }

    /**
     * enum with parameter names for SOS 2.0 getFeatureOfInterest request not
     * supported by all versions
     */
    enum GetFeatureOfInterestParams {
        featureOfInterest, observedProperty, procedure, spatialFilter;
    }

    enum GetObservationByIdParams {
        observation;
    }

    enum GetResultTemplateParams {
        offering, observedProperty;
    }

    enum InsertResultTemplateParams {
        offering, observedProperty, procedure, observationType, resultStructure, resultEncoding, proposedTemplate, identifier;
    }

    enum GetResultParams {
        offering, observedProperty, featureOfInterest, temporalFilter, spatialFilter;
    }

    enum InsertResultParams {
        template, resultValues
    }

    /**
     * enum with parameter names for registerSensor request
     */
    enum InsertSensorParams {
        service, version, procedureDescriptionFormat, procedureDescription, observableProperty, metadata, featureOfInterestType, observationType, procedureIdentifier, offeringIdentifier;
    }

    enum DeleteSensorParams {
        service, version, procedure;
    }

    /**
     * enum with parameter names for registerSensor request
     */
    enum UpdateSensorDescriptionParams {
        service, version, procedure, procedureDescriptionFormat, description;
    }
}
