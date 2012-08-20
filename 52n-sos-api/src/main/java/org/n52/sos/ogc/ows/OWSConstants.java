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

package org.n52.sos.ogc.ows;

import org.n52.sos.exception.IExceptionCode;

/**
 * Constants for OWS.
 * 
 */
public class OWSConstants {

    // namespace and schema locations
    public static final String NS_OWS = "http://www.opengis.net/ows/1.1";

    public static final String NS_OWS_PREFIX = "ows";

    public static final String SCHEMA_LOCATION_OWS = "http://schemas.opengis.net/ows/1.1.0/owsAll.xsd";

    public static final String SCHEMA_LOCATION_OWS_EXCEPTIONREPORT =
            "http://schemas.opengis.net/ows/1.1.0/owsExceptionReport.xsd";

    // exception messages
    public static final String SOAP_REASON_INVALID_PARAMETER_VALUE =
            "The request contained an invalid parameter value.";

    public static final String SOAP_REASON_INVALID_UPDATE_SEQUENCES =
            "The value of the updateSequence parameter in the GetCapabilities operation request was greater than the current value of the service metadata updateSequence number.";

    public static final String SOAP_REASON_MISSING_PARAMETER_VALUE =
            "The request did not include a value for a required parameter and this server does not declare a default value for it.";

    public static final String SOAP_REASON_NO_APPLICABLE_CODE = "A server exception was encountered.";

    public static final String SOAP_REASON_NO_DATA_AVAILABLE = "There are no data available.";

    public static final String SOAP_REASON_OPERATION_NOT_SUPPORTED =
            "The requested operation is not supported by this server.";

    public static final String SOAP_REASON_OPTION_NOT_SUPPORTED =
            "The request included/targeted an option that is not supported by this server.";

    public static final String SOAP_REASON_REQUEST_EXTENSION_NOT_SUPPORTED =
            "The request included an extension that is not supported by this server.";

    public static final String SOAP_REASON_VERSION_NEGOTIATION_FAILED =
            "The list of versions in the ‘AcceptVersions’ parameter value of the GetCapabilities operation request did not include any version supported by this server.";

    public static final String SOAP_REASON_RESPONSE_EXCEEDS_SIZE_LIMIT =
            "The requested result set exceeds the response size limit of this service and thus cannot be delivered.";

    public static final String SOAP_REASON_INVALID_PROPERTY_OFFERING_COMBINATION =
            "Observations for the requested combination of observedProperty and offering do not use SWE Common encoded results.";

    public static final String SOAP_REASON_UNKNOWN = "A server exception was encountered.";

    public static final String EN_EXCEPTION = "Exception";

    public static final String EN_EXCEPTION_CODE = "exceptionCode";

    public static final String EN_LOCATOR = "locator";

    public static final String EN_EXCEPTION_TEXT = "ExceptionText";

    /**
     * Enumeration for related feature role
     * 
     */
    public enum RelatedFeatureRole {
        featureOfInterestID, relatedFeatureID
    }

    /** enum with names of get request parameters for all requests */
    public enum RequestParams {
        request, service, version;

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
                    (s.equals(RequestParams.request.name())) || (s.equals(RequestParams.service.name()))
                            || (s.equals(RequestParams.version.name()));
            return contained;
        }
    }

    public enum MinMax {
        MIN, MAX
    }

    /**
     * ExceptionCodes as defined in the OWS Common Implementation Specification
     * 1.1.0
     */
    public enum OwsExceptionCode implements IExceptionCode {
        OperationNotSupported, MissingParameterValue, InvalidParameterValue, VersionNegotiationFailed, InvalidUpdateSequence, OptionNotSupported, NoApplicableCode, NoDataAvailable
    }

    /** Exception levels */
    public enum ExceptionLevel {
        PlainExceptions, DetailedExceptions
    }
}
