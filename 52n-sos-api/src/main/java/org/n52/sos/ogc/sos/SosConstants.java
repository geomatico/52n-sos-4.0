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

import org.n52.sos.exception.ExceptionCode;
import org.n52.sos.ogc.OGCConstants;
import org.n52.sos.ogc.ows.OWSConstants;

/**
 * SosConstants holds all important and often used constants of this SOS (e.g.
 * name of the getCapabilities operation) that are global between all supported
 * versions
 * 
 */
public final class SosConstants {

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

    /** Constant for prefixes of FOIs 
	 * @deprecated Use {@link OGCConstants#URN_FOI_PREFIX} instead*/
	public static final String FOI_PREFIX = OGCConstants.URN_FOI_PREFIX;

    /** Constant for prefixes of procedures 
	 * @deprecated Use {@link OGCConstants#URN_PROCEDURE_PREFIX} instead*/
	public static final String PROCEDURE_PREFIX = OGCConstants.URN_PROCEDURE_PREFIX;

    public static final String PROCEDURE_STANDARD_DESC_URL = "standardURL";

    /** Constant for prefixes of procedures 
	 * @deprecated Use {@link OGCConstants#URN_PHENOMENON_PREFIX} instead*/
	public static final String PHENOMENON_PREFIX = OGCConstants.URN_PHENOMENON_PREFIX;

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
    
    public static final String HTTP_PUT = "PUT";
    
    public static final String HTTP_DELETE = "DELETE";

    public static final String MIN_VALUE = "MinValue";

    public static final String MAX_VALUE = "MaxValue";

    public static final String ALL_RELATED_FEATURES = "allFeatures";

    public static final String SEPARATOR_4_REL_FEAT = "_._";

    public static final String SEPARATOR_4_OFFERINGS = "_._";

    /**
	 * @deprecated Use {@link OGCConstants#URN_PROPERTY_NAME_LOCATION} instead
	 */
	public static final String PROPERTY_NAME_LOCATION = OGCConstants.URN_PROPERTY_NAME_LOCATION;

    /**
	 * @deprecated Use {@link OGCConstants#URN_PROPERTY_NAME_SAMPLING_GEOMETRY} instead
	 */
	public static final String PROPERTY_NAME_SAMPLING_GEOMETRY = OGCConstants.URN_PROPERTY_NAME_SAMPLING_GEOMETRY;

    /**
	 * @deprecated Use {@link OGCConstants#URN_PROPERTY_NAME_SPATIAL_VALUE} instead
	 */
	public static final String PROPERTY_NAME_SPATIAL_VALUE = OGCConstants.URN_PROPERTY_NAME_SPATIAL_VALUE;

    private static final String SOAP_REASON_RESPONSE_EXCEEDS_SIZE_LIMIT =
            "The requested result set exceeds the response size limit of this service and thus cannot be delivered.";

    private static final String SOAP_REASON_INVALID_PROPERTY_OFFERING_COMBINATION =
            "Observations for the requested combination of observedProperty and offering do not use SWE Common encoded results.";

    public static final String GENERATED_IDENTIFIER_PREFIX = "generated_";

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
            for (Enum<?> p : values()) {
                if (p.name().equals(s)) {
                    return true;
                }
            }
            return false;
        }
    }
    
    public enum Filter {
        ValueReference;
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
            for (Enum<?> p : values()) {
                if (p.name().equals(s)) {
                    return true;
                }
            }
            return OWSConstants.RequestParams.contains(s);
        }
    }

    /** enum with parameter names for getCapabilities request */
    public enum GetCapabilitiesParams {
        Sections, AcceptVersions, updateSequence, AcceptFormats, service, request, Section;

        /**
         * method checks whether the string parameter is contained in this
         * enumeration
         * 
         * @param s
         *            the name which should be checked
         * @return true if the name is contained in the enumeration
         */
        public static boolean contains(String s) {
            for (Enum<?> p : values()) {
                if (p.name().equals(s)) {
                    return true;
                }
            }
            return OWSConstants.RequestParams.contains(s);
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
            for (Enum<?> p : values()) {
                if (p.name().equals(s)) {
                    return true;
                }
            }
            return OWSConstants.RequestParams.contains(s);
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
            for (Enum<?> p : values()) {
                if (p.name().equals(s)) {
                    return true;
                }
            }
            return OWSConstants.RequestParams.contains(s);
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
            for (Enum<?> p : values()) {
                if (p.name().equals(s)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * @param valueType
     * @return
     */
    public static ValueTypes getValueTypeForString(String valueType) {
        try {
            return ValueTypes.valueOf(valueType);
        } catch (IllegalArgumentException e) {
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
            for (Enum<?> p : values()) {
                if (p.name().equals(s)) {
                    return true;
                }
            }
            return false;
        }
    }

    public enum FirstLatest {
        first, latest;

        public static boolean contains(String timeString) {
            return timeString.equalsIgnoreCase(first.name()) || timeString.equalsIgnoreCase(latest.name());
        }

        public static FirstLatest getEnumForString(String value) {
           if (value.equalsIgnoreCase(first.name())){
               return first;
           } else if (value.equalsIgnoreCase(latest.name())){
               return latest;
           }
            return null;
        }
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
        GMLID, EXIST_FOI_IN_DOC, VERSION, TYPE, DOCUMENT, FOR_OBSERVATION, ENCODE, ENCODE_NAMESPACE
    }

    public enum SosExceptionCode implements ExceptionCode {
        ResponseExceedsSizeLimit(SOAP_REASON_RESPONSE_EXCEEDS_SIZE_LIMIT), 
        InvalidPropertyOfferingCombination(SOAP_REASON_INVALID_PROPERTY_OFFERING_COMBINATION);
        
        private final String soapFaultReason;
        
        private SosExceptionCode(String soapFaultReason) {
            this.soapFaultReason = soapFaultReason;
        }

        @Override
        public String getSoapFaultReason() {
            return this.soapFaultReason;
        }
    }
}