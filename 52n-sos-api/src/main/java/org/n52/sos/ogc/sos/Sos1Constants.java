/***************************************************************
 Copyright (C) 2012
 by 52 North Initiative for Geospatial Open Source Software GmbH

 Contact: Andreas Wytzisk
 52 North Initiative for Geospatial Open Source Software GmbH
 Martin-Luther-King-Weg 24
 48155 Muenster, Germany
 info@52north.org

 This program is free software; you can redistribute and/or modify it under 
 the terms of the GNU General Public License version 2 as published by the 
 Free Software Foundation.

 This program is distributed WITHOUT ANY WARRANTY; even without the implied
 WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License along with
 this program (see gnu-gpl v2.txt). If not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 visit the Free Software Foundation web page, http://www.fsf.org.

 Author: <LIST OF AUTHORS/EDITORS>
 Created: <CREATION DATE>
 Modified: <DATE OF LAST MODIFICATION (optional line)>
 ***************************************************************/

package org.n52.sos.ogc.sos;

import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.ows.OWSConstants;

/**
 * SosConstants holds all important and often used constants
 * (e.g. name of the getCapabilities operation) that are 
 * specific to SOS 1.0

 * 
 */
public final class Sos1Constants {
	
	public static final String NS_SOS = "http://www.opengis.net/sos/1.0";
	
	/** Constant for the schema repository of the SOS */
	public static final String SCHEMA_LOCATION_SOS = "http://schemas.opengis.net/sos/1.0.0/sosAll.xsd";
	
	
    /** Constant for the content types of the response formats */
    private static String[] RESPONSE_FORMATS = { OMConstants.CONTENT_TYPE_OM,
        SosConstants.CONTENT_TYPE_ZIP };    

    /** Constant for actual implementing version */
    public static final String SERVICEVERSION = "1.0.0";

    /** private constructor, to enforce use of instance instead of instantiation */
    private Sos1Constants() {
    }

    /** the names of the SOS 1.0 operations that are not supported by all versions */
    public enum Operations {
        GetFeatureOfInterestTime, DescribeFeatureType, DescribeObservtionType, DescribeResultModel,
        RegisterSensor;
        
        public static boolean contains(String s) {
            boolean contained = false;
            contained =
                    (SosConstants.Operations.contains(s))                    
                            || (s.equals(Operations.GetFeatureOfInterestTime.name()))
                            || (s.equals(Operations.DescribeFeatureType.name()))
                            || (s.equals(Operations.DescribeObservtionType.name()))
                            || (s.equals(Operations.DescribeResultModel.name()))
                            || (s.equals(Operations.RegisterSensor.name()));
            return contained;
        }
    }

    /** enum with names of SOS 1.0 Capabilities sections not supported by all versions */
    public enum CapabilitiesSections {
        Filter_Capabilities;

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
                    (SosConstants.CapabilitiesSections.contains(s))
                            || (s.equals(CapabilitiesSections.Filter_Capabilities.name()));
            return contained;
        }
    }

    /** enum with parameter names for SOS 1.0 getObservation request not supported by all versions */
    public enum GetObservationParams {
        eventTime;

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
                    (SosConstants.GetObservationParams.contains(s))
                            || (s.equals(GetObservationParams.eventTime.name()));
            return contained;
        }
    }

    /** enum with parameter names for SOS 1.0 insertObservation request not supported by all versions */
    public enum InsertObservationParams {
        AssignedSensorId, Observation;

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
                            || (s.equals(InsertObservationParams.AssignedSensorId.name()))
                            || (s.equals(InsertObservationParams.Observation.name()));
            return contained;
        }
    }

    /** enum with parameter names for SOS 1.0 getObservation request not supported by all versions */
    public enum DescribeSensorParams {
        outputFormat, time;

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
                    (SosConstants.DescribeSensorParams.contains(s))                    
                            || (s.equals(DescribeSensorParams.outputFormat.name()))
                            || (s.equals(DescribeSensorParams.time.name()));
            return contained;
        }
    }

    /** enum with parameter names for SOS 1.0 getFeatureOfInterest request not supported by all versions */
    public enum GetFeatureOfInterestParams {
        featureOfInterestID, location;

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
                            || (s.equals(GetFeatureOfInterestParams.featureOfInterestID.name()))
                            || (s.equals(GetFeatureOfInterestParams.location.name()));
            return contained;
        }
    }
    
    /** enum with parameter names for getFeatureOfInterestTime request */
    public enum GetFeatureOfInterestTimeParams {
        featureOfInterestID, location, observedProperty, procedure;

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
                            || (s.equals(GetFeatureOfInterestTimeParams.featureOfInterestID.name()))
                            || (s.equals(GetFeatureOfInterestTimeParams.location.name()))
                            || (s.equals(GetFeatureOfInterestTimeParams.observedProperty.name()))
                            || (s.equals(GetFeatureOfInterestTimeParams.procedure.name()));
            return contained;
        }
    }

    /** enum with parameter names for registerSensor request */
    public enum RegisterSensorParams {
        SensorDescription, ObservationTemplate;

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
                            || (s.equals(RegisterSensorParams.SensorDescription.name()))
                            || (s.equals(RegisterSensorParams.ObservationTemplate.name()));
            return contained;
        }
    }
    
    /** enum with parameter names for SOS 1.0 getObservationById request not supported by all versions */
    public enum GetObservationByIdParams {
        srsName, ObservationId, responseFormat, resultModel, responseMode, SortBy;
        
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
                            || (s.equals(GetObservationByIdParams.srsName.name()))
                            || (s.equals(GetObservationByIdParams.ObservationId.name()))
                            || (s.equals(GetObservationByIdParams.responseFormat.name()))
                            || (s.equals(GetObservationByIdParams.resultModel.name()))
                            || (s.equals(GetObservationByIdParams.responseMode.name()))
                            || (s.equals(GetObservationByIdParams.SortBy.name()));
            return contained;
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
