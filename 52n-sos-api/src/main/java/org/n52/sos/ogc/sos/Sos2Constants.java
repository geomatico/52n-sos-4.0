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

import javax.xml.namespace.QName;

import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.ows.OWSConstants;

/**
 * SosConstants holds all important and often used constants (e.g. name of the
 * getCapabilities operation) that are specific to SOS 2.0
 * 
 * 
 */
public final class Sos2Constants {

    public static final String NS_SOS_20 = "http://www.opengis.net/sos/2.0";

    public static final String SCHEMA_LOCATION_SOS = "http://schemas.opengis.net/sos/2.0/sos.xsd";

    /** Constant for the content types of the response formats */
    private static String[] RESPONSE_FORMATS = { OMConstants.RESPONSE_FORMAT_OM_2, SosConstants.CONTENT_TYPE_ZIP };

    /**
     * The names of the extensions that we know off and might support
     */
    public enum Extensions{
        Subsetting;
    }

    /** Constant for actual implementing version */
    public static final String SERVICEVERSION = "2.0.0";

    /**
     * output format for DescribeSensor operation; Currently, SOS only supports
     * SensorML 1.0.1
     */
    public static final String EN_OBSERVATION_OFFERING = "ObservationOffering";

    public static final String EN_OBSERVED_AREA = "observedArea";

    public static final String EN_PHENOMENON_TIME = "phenomenonTime";

    public static final String EN_RESPONSE_FORMAT = "responseFormat";

    public static final String EN_OBSERVATION_TYPE = "observationType";

    public static final String EN_FEATURE_OF_INTEREST_TYPE = "featureOfInterestType";
	
	public static final String EN_SOS_INSERTION_METADATA = "SosInsertionMetadata";
	
	public static final QName QN_OBSERVATION_OFFERING = new QName(NS_SOS_20, EN_OBSERVATION_OFFERING, SosConstants.NS_SOS_PREFIX);
	
	public static final QName QN_SOS_INSERTION_METADATA = new QName(NS_SOS_20, EN_SOS_INSERTION_METADATA, SosConstants.NS_SOS_PREFIX);

    /** private constructor, to enforce use of instance instead of instantiation */
    private Sos2Constants() {
    }

    /**
     * the names of the SOS 2.0 operations that are not supported by all
     * versions
     */
    public enum Operations {
        InsertSensor, DeleteSensor, InsertResult, InsertResultTemplate, GetResultTemplate, UpdateSensorDescription;

        public static boolean contains(String s) {
            for (Enum<?> p : values()) {
                if (p.name().equals(s)) {
                    return true;
                }
            }
            return SosConstants.Operations.contains(s);
        }
    }

    /**
     * enum with names of SOS 2.0 Capabilities sections not supported by all
     * versions
     */
    public enum CapabilitiesSections {
        FilterCapabilities, InsertionCapabilities;

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
            return SosConstants.CapabilitiesSections.contains(s);
        }
    }

    /** enum with parameter names for getObservation request */
    public enum GetObservationParams {
        temporalFilter, spatialFilter, namespaces;

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
            return SosConstants.GetObservationParams.contains(s);
        }
    }

    /**
     * enum with parameter names for SOS 2.0 insertObservation request not
     * supported by all versions
     */
    public enum InsertObservationParams {
        offering, observation, observationType, observedProperty, procedure, featureOfInterest;

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
     * enum with parameter names for SOS 2.0 getObservation request not
     * supported by all versions
     */
    public enum DescribeSensorParams {
        procedureDescriptionFormat, validTime;

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
            return SosConstants.DescribeSensorParams.contains(s);
        }
    }

    /**
     * enum with parameter names for SOS 2.0 getFeatureOfInterest request not
     * supported by all versions
     */
    public enum GetFeatureOfInterestParams {
        featureOfInterest, observedProperty, procedure, spatialFilter;

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

    public enum GetObservationByIdParams {
        observation;

        public static boolean contains(String s) {
            for (Enum<?> p : values()) {
                if (p.name().equals(s)) {
                    return true;
                }
            }
            return OWSConstants.RequestParams.contains(s);
        }
    }

    public enum GetResultTemplateParams {
        offering, observedProperty;
    }
    
    public enum InsertResultTemplateParams {
        offering, observedProperty, procedure, observationType, resultStructure, resultEncoding, proposedTemplate, identifier;
    }

    public enum GetResultParams {
        offering, observedProperty, featureOfInterest, temporalFilter, spatialFilter;
    }
    
    public enum InsertResult {
        template, resultValues
    }

    /** enum with parameter names for registerSensor request */
    public enum InsertSensorParams {
        service, version, procedureDescriptionFormat, procedureDescription, observableProperty, metadata, featureOfInterestType, observationType;

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

    public enum DeleteSensorParams {
        service, version, procedure, procedureDescriptionFormat;

        public static boolean contains(String s) {
            for (Enum<?> p : values()) {
                if (p.name().equals(s)) {
                    return true;
                }
            }
            return false;
        }
    }

    /** enum with parameter names for registerSensor request */
    public enum UpdateSensorDescriptionParams {
        service, version, procedure, procedureDescriptionFormat, description;

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
     * Returns the supported response formats.
     * 
     * @return response formats
     */
    public static String[] getResponseFormats() {
        return RESPONSE_FORMATS;
    }
}
