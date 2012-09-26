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
package org.n52.sos.ogc.sos;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.n52.sos.exception.IExceptionCode;
import org.n52.sos.ogc.ows.OWSConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SosConstants holds all important and often used constants of this SOS (e.g.
 * name of the getCapabilities operation) that are global between all supported
 * versions
 * 
 */
public final class SosConstants {

    /** logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(SosConstants.class);

    public static final String NS_SOS_PREFIX = "sos";

    /** Constant for the content type of the response */
    public static final String CONTENT_TYPE_XML = "text/xml";

    /** Constant for the content type of the response */
    public static final String CONTENT_TYPE_ZIP = "application/zip";

    /** Constant for the content types of the accept formats */
    private static String[] ACCEPT_FORMATS = { CONTENT_TYPE_XML, CONTENT_TYPE_ZIP };

    /**
     * name of System capabilities containing parent procedures for
     * RegisterSensor and DescribeSensor
     */
    public static final String SYS_CAP_PARENT_PROCEDURES_NAME = "parentProcedures";

    /**
     * hexadecimal values
     */
    public static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
            'E', 'F' };

    /**
     * Message digest for generating single identifier
     */
    public static MessageDigest MESSAGE_DIGEST;

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

    /** Constant for prefixes of FOIs */
    public static final String FOI_PREFIX = "urn:ogc:def:object:feature:";

    /** Constant for prefixes of procedures */
    public static final String PROCEDURE_PREFIX = "urn:ogc:object:feature:Sensor:IFGI:";

    public static final String PROCEDURE_STANDARD_DESC_URL = "standardURL";

    /** Constant for prefixes of procedures */
    public static final String PHENOMENON_PREFIX = "urn:ogc:def:phenomenon:OGC:1.0.30:";

    /** Constant for the service name of the SOS */
    public static final String SOS = "SOS";

    /**
     * String representing parameter value, if parameter is not set in an
     * operation request
     */
    public static final String PARAMETER_NOT_SET = "NOT_SET";

    /**
     * String representing parameter value, if parameter is any in an operation
     * request
     */
    public static final String PARAMETER_ANY = "ANY";

    /**
     * String representing parameter value, if parameter is no values in an
     * operation request
     */
    public static final String PARAMETER_NO_VALUES = "NoValues";

    public static final String NOT_DEFINED = "NOT_DEFINED";

    /**
     * request timeout in ms for split requests to SOS instances
     */
    public static final long UPDATE_TIMEOUT = 10000;

    /**
     * Constant for actual implementing version Measurement
     */
    public static final String OBS_ID_PREFIX = "o_";

    /**
     * Constant for actual implementing version OvservationCollection
     */
    public static final String OBS_GENERIC_ID_PREFIX = "go_";

    /**
     * Constant for actual implementing version OvservationCollection
     */
    public static final String OBS_COL_ID_PREFIX = "oc_";

    /**
     * Constant for actual implementing version ObservationTemplate
     */
    public static final String OBS_TEMP_ID_PREFIX = "ot_";

    /**
     * Constant 'out-of-bands' for response mode, which means that the results
     * in an observation response appear external to the observation element
     */
    public static final String RESPONSE_MODE_OUT_OF_BANDS = "out-of-bands";

    /**
     * Constant 'resultTemplate' for response mode, which means that the result
     * is an ObservationTemplate for subsequent calls to GetResult operation
     */
    public static final String RESPONSE_RESULT_TEMPLATE = "resultTemplate";

    /**
     * Constant 'inline' for response mode, which means that results are
     * contained inline the Observation elements of an observation response
     * document
     */
    public static final String RESPONSE_MODE_INLINE = "inline";

    /**
     * Constant 'attached' for response mode, which means that result values of
     * an observation response are attached as MIME attachments
     */
    public static final String RESPONSE_MODE_ATTACHED = "attached";

    /**
     * Array of constants for response mode.
     */
    private static final String[] RESPONSE_MODES = { RESPONSE_MODE_INLINE, RESPONSE_RESULT_TEMPLATE };

    public static final String HTTP_GET = "GET";

    public static final String HTTP_POST = "POST";

    public static final String MIN_VALUE = "MinValue";

    public static final String MAX_VALUE = "MaxValue";

    public static final String ALL_RELATED_FEATURES = "allFeatures";

    public static final String SEPARATOR_4_REL_FEAT = "_._";

    public static final String SEPARATOR_4_OFFERINGS = "_._";

    public static final String PROPERTY_NAME_LOCATION = "urn:ogc:data:location";

    public static final String PROPERTY_NAME_SAMPLING_GEOMETRY = "urn:ogc:data:samplingGeometry";

    public static final String PROPERTY_NAME_SPATIAL_VALUE = "urn:ogc:data:spatialValue";

    public static final String SOAP_REASON_RESPONSE_EXCEEDS_SIZE_LIMIT =
            "The requested result set exceeds the response size limit of this service and thus cannot be delivered.";

    public static final String SOAP_REASON_INVALID_PROPERTY_OFFERING_COMBINATION =
            "Observations for the requested combination of observedProperty and offering do not use SWE Common encoded results.";

    /** private constructor, to enforce use of instance instead of instantiation */
    private SosConstants() {
    }

    /**
     * the names of the operations supported by all versions of the SOS
     * specification
     */
    public enum Operations {
        GetCapabilities, GetObservation, GetObservationById, DescribeSensor, InsertObservation, GetResult, GetFeatureOfInterest;

        public static boolean contains(String s) {
            boolean contained = false;
            contained =
                    (s.equals(Operations.GetCapabilities.name())) || (s.equals(Operations.GetObservation.name()))
                            || (s.equals(Operations.GetObservationById.name()))
                            || (s.equals(Operations.DescribeSensor.name()))
                            || (s.equals(Operations.InsertObservation.name()))
                            || (s.equals(Operations.GetResult.name()))
                            || (s.equals(Operations.GetFeatureOfInterest.name()));
            return contained;
        }
    }

    /** enum with names of Capabilities sections supported by all versions */
    public enum CapabilitiesSections {
        ServiceIdentification, ServiceProvider, OperationsMetadata, Contents, All;

        /**
         * method checks whether the string parameter is contained in this
         * enumeration
         * 
         * @param s
         *            the name which should be checked
         * @return true if the name is contained in the enumeration
         */
        public static boolean contains(String s) {
            boolean contained = false;
            contained =
                    (OWSConstants.RequestParams.contains(s))
                            || (s.equals(CapabilitiesSections.ServiceIdentification.name()))
                            || (s.equals(CapabilitiesSections.ServiceProvider.name()))
                            || (s.equals(CapabilitiesSections.OperationsMetadata.name()))
                            || (s.equals(CapabilitiesSections.Contents.name()))
                            || (s.equals(CapabilitiesSections.All.name()));
            return contained;
        }
    }

    /** enum with parameter names for getCapabilities request */
    public enum GetCapabilitiesParams {
        Sections, AcceptVersions, updateSequence, AcceptFormats, service, request;

        /**
         * method checks whether the string parameter is contained in this
         * enumeration
         * 
         * @param s
         *            the name which should be checked
         * @return true if the name is contained in the enumeration
         */
        public static boolean contains(String s) {
            boolean contained = false;
            contained =
                    (OWSConstants.RequestParams.contains(s)) || (s.equals(GetCapabilitiesParams.Sections.name()))
                            || (s.equals(GetCapabilitiesParams.AcceptVersions.name()))
                            || (s.equals(GetCapabilitiesParams.updateSequence.name()))
                            || (s.equals(GetCapabilitiesParams.AcceptFormats.name()))
                            || (s.equals(GetCapabilitiesParams.service.name()))
                            || (s.equals(GetCapabilitiesParams.request.name()));
            return contained;
        }
    }

    /**
     * enum with parameter names for getObservation request supported by all
     * versions
     */
    public enum GetObservationParams {
        srsName, resultType, startPosition, maxRecords, offering, procedure, observedProperty, featureOfInterest, result, responseFormat, resultModel, responseMode, SortBy, BBOX;

        /**
         * method checks whether the string parameter is contained in this
         * enumeration
         * 
         * @param s
         *            the name which should be checked
         * @return true if the name is contained in the enumeration
         */
        public static boolean contains(String s) {
            boolean contained = false;
            contained =
                    (OWSConstants.RequestParams.contains(s)) || (s.equals(GetObservationParams.srsName.name()))
                            || (s.equals(GetObservationParams.resultType.name()))
                            || (s.equals(GetObservationParams.startPosition.name()))
                            || (s.equals(GetObservationParams.maxRecords.name()))
                            || (s.equals(GetObservationParams.offering.name()))
                            || (s.equals(GetObservationParams.procedure.name()))
                            || (s.equals(GetObservationParams.observedProperty.name()))
                            || (s.equals(GetObservationParams.featureOfInterest.name()))
                            || (s.equals(GetObservationParams.result.name()))
                            || (s.equals(GetObservationParams.responseFormat.name()))
                            || (s.equals(GetObservationParams.resultModel.name()))
                            || (s.equals(GetObservationParams.responseMode.name()));
            return contained;
        }
    }

    /**
     * enum with parameter names for getObservation request supported by all
     * versions
     */
    public enum DescribeSensorParams {
        procedure;

        /**
         * method checks whether the string parameter is contained in this
         * enumeration
         * 
         * @param s
         *            the name which should be checked
         * @return true if the name is contained in the enumeration
         */
        public static boolean contains(String s) {
            boolean contained = false;
            contained = (OWSConstants.RequestParams.contains(s)) || s.equals(DescribeSensorParams.procedure.name());
            return contained;
        }
    }

    /**
     * 
     * Enumeration with values for value type
     */
    public enum ValueTypes {
        textType, numericType, booleanType, countType, categoryType, isoTimeType, spatialType, commonType, externalReferenceType, referenceValueTextType, referenceValueNumericType, referenceValueExternalReferenceType, uncertaintyType, notDefined;

        /**
         * method checks whether the string parameter is contained in this
         * enumeration
         * 
         * @param s
         *            the name which should be checked
         * @return true if the name is contained in the enumeration
         */
        public static boolean contains(String s) {
            boolean contained = false;
            contained =
                    (s.equals(ValueTypes.textType.name()))
                            || (s.equals(ValueTypes.numericType.name()))
                            || (s.equals(ValueTypes.booleanType.name()))
                            || (s.equals(ValueTypes.countType.name()))
                            || (s.equals(ValueTypes.categoryType.name()))
                            || (s.equals(ValueTypes.isoTimeType.name()))
                            || (s.equals(ValueTypes.spatialType.name()))
                            || (s.equals(ValueTypes.externalReferenceType.name()))
                            || (s.equals(ValueTypes.referenceValueTextType.name()))
                            || (s.equals(ValueTypes.referenceValueNumericType.name()))
                            || (s.equals(ValueTypes.referenceValueExternalReferenceType.name()) || (s
                                    .equals(ValueTypes.uncertaintyType.name())));
            return contained;
        }
    }

    /**
     * @param valueType
     * @return
     */
    public static ValueTypes getValueTypeForString(String valueType) {
        if (valueType.equals(ValueTypes.textType.name())) {
            return ValueTypes.textType;
        } else if (valueType.equals(ValueTypes.numericType.name())) {
            return ValueTypes.numericType;
        } else if (valueType.equals(ValueTypes.booleanType.name())) {
            return ValueTypes.booleanType;
        } else if (valueType.equals(ValueTypes.countType.name())) {
            return ValueTypes.countType;
        } else if (valueType.equals(ValueTypes.categoryType.name())) {
            return ValueTypes.categoryType;
        } else if (valueType.equals(ValueTypes.isoTimeType.name())) {
            return ValueTypes.isoTimeType;
        } else if (valueType.equals(ValueTypes.spatialType.name())) {
            return ValueTypes.spatialType;
        } else if (valueType.equals(ValueTypes.externalReferenceType.name())) {
            return ValueTypes.externalReferenceType;
        } else if (valueType.equals(ValueTypes.referenceValueTextType.name())) {
            return ValueTypes.referenceValueTextType;
        } else if (valueType.equals(ValueTypes.referenceValueNumericType.name())) {
            return ValueTypes.referenceValueNumericType;
        } else if (valueType.equals(ValueTypes.referenceValueExternalReferenceType.name())) {
            return ValueTypes.referenceValueExternalReferenceType;
        } else if (valueType.equals(ValueTypes.uncertaintyType.name())) {
            return ValueTypes.uncertaintyType;
        } else {
            return ValueTypes.notDefined;
        }
    }

    /**
     * possible resultTypes in getObservation request
     * 
     */
    public static enum ResultType {
        results, hits;

        /**
         * method checks whether the string parameter is contained in this
         * enumeration
         * 
         * @param s
         *            the name which should be checked
         * @return true if the name is contained in the enumeration
         */
        public static boolean contains(String s) {
            boolean contained = false;
            contained = (s.equals(ResultType.results.name())) || (s.equals(ResultType.hits.name()));
            return contained;
        }
    }

    public enum FirstLatest {
        getFirst, latest
    }

    /**
     * Returns the accepted formats.
     * 
     * @return accepted formats
     */
    public static String[] getAcceptFormats() {
        return ACCEPT_FORMATS;
    }

    /**
     * Returns the supported response modes.
     * 
     * @return response modes
     */
    public static String[] getResponseModes() {
        return RESPONSE_MODES;
    }

    public enum HelperValues {
        GMLID, EXIST_FOI_IN_DOC, VERSION
    }

    public enum SosExceptionCode implements IExceptionCode {
        ResponseExceedsSizeLimit, InvalidPropertyOfferingCombination
    }
}