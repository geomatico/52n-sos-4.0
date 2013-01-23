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
package org.n52.sos.wsdl;

import java.net.URI;
import javax.xml.namespace.QName;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.swe.SWEConstants;

/**
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class WSDLConstants {
        
    public static final String NS_HTTP = "http://schemas.xmlsoap.org/wsdl/http/";
    public static final String NS_HTTP_PREFIX = "http";
    public static final String NS_MIME = "http://schemas.xmlsoap.org/wsdl/mime/";
    public static final String NS_MIME_PREFIX = "mime";
    public static final String NS_OWS = OWSConstants.NS_OWS;
    public static final String NS_OWS_PREFIX = OWSConstants.NS_OWS_PREFIX;
    public static final String NS_SOAP = "http://schemas.xmlsoap.org/wsdl/soap/";
    public static final String NS_SOAP_PREFIX = "soap";
    public static final String NS_SOS = Sos2Constants.NS_SOS_20;
    public static final String NS_SOS_PREFIX = SosConstants.NS_SOS_PREFIX;
    public static final String NS_SOSW = "http://www.opengis.net/sos/2.0/wsdl";
    public static final String NS_SOSW_PREFIX = "sosw";
    public static final String NS_SWES = SWEConstants.NS_SWES_20;
    public static final String NS_SWES_PREFIX = SWEConstants.NS_SWES_PREFIX;
    public static final String NS_WSAM = "http://www.w3.org/2007/05/addressing/metadata";
    public static final String NS_WSAM_PREFIX = "wsam";
    public static final String NS_WSDL = "http://schemas.xmlsoap.org/wsdl/";
    public static final String NS_WSDL_PREFIX = "wsdl";
    public static final String NS_XSD = "http://www.w3.org/2001/XMLSchema";
    public static final String NS_XSD_PREFIX = "xsd";

    public static final String AN_WSAM_ACTION = "Action";
    public static final String AN_XSD_ELEMENT_FORM_DEFAULT = "elementFormDefault";
    public static final String AN_XSD_SCHEMA_LOCATION = "schemaLocation";
    public static final String AN_XSD_TARGET_NAMESPACE = "targetNamespace";

    public static final String EN_HTTP_ADDRESS = "address";
    public static final String EN_HTTP_BINDING = "binding";
    public static final String EN_HTTP_OPERATION = "operation";
    public static final String EN_HTTP_URL_ENCODED = "urlEncoded";

    public static final String EN_MIME_CONTENT = "content";
    public static final String EN_MIME_MIME_XML = "mimeXml";

    public static final String EN_OWS_EXCEPTION = OWSConstants.EN_EXCEPTION;

    public static final String EN_SOAP_ADDRESS = "address";
    public static final String EN_SOAP_BINDING = "binding";
    public static final String EN_SOAP_BODY = "body";
    public static final String EN_SOAP_FAULT = "fault";
    public static final String EN_SOAP_OPERATION = "operation";

    public static final String EN_SOS_CAPABILITIES = "Capabilities";
    public static final String EN_SOS_DELETE_SENSOR = "DeleteSensor";
    public static final String EN_SOS_DELETE_SENSOR_RESPONSE = "DeleteSensorResponse";
    public static final String EN_SOS_GET_CAPABILITIES = "GetCapabilities";
    public static final String EN_SOS_GET_FEATURE_OF_INTEREST = "GetFeatureOfInterest";
    public static final String EN_SOS_GET_FEATURE_OF_INTEREST_RESPONSE = "GetFeatureOfInterestResponse";
    public static final String EN_SOS_GET_OBSERVATION = "GetObservation";
    public static final String EN_SOS_GET_OBSERVATION_BY_ID = "GetObservationById";
    public static final String EN_SOS_GET_OBSERVATION_BY_ID_RESPONSE = "GetObservationByIdResponse";
    public static final String EN_SOS_GET_OBSERVATION_RESPONSE = "GetObservationResponse";
    public static final String EN_SOS_GET_RESULT = "GetResult";
    public static final String EN_SOS_GET_RESULT_RESPONSE = "GetResultResponse";
    public static final String EN_SOS_GET_RESULT_TEMPLATE = "GetResultTemplate";
    public static final String EN_SOS_GET_RESULT_TEMPLATE_RESPONSE = "GetResultTemplateResponse";
    public static final String EN_SOS_INSERT_OBSERVATION = "InsertObservation";
    public static final String EN_SOS_INSERT_OBSERVATION_RESPONSE = "InsertObservationResponse";
    public static final String EN_SOS_INSERT_RESULT = "InsertResult";
    public static final String EN_SOS_INSERT_RESULT_RESPONSE = "InsertResultResponse";
    public static final String EN_SOS_INSERT_RESULT_TEMPLATE = "InsertResultTemplate";
    public static final String EN_SOS_INSERT_RESULT_TEMPLATE_RESPONSE = "InsertResultTemplateResponse";

    public static final String EN_SOSW_SOS_KVP_BINDING = "SosKvpBinding";
    public static final String EN_SOSW_SOS_GET_PORT_TYPE = "SosGetPortType";
    public static final String EN_SOSW_SOS_POST_PORT_TYPE = "SosPostPortType";
    public static final String EN_SOSW_SOS_POX_BINDING = "SosPoxBinding";
    public static final String EN_SOSW_SOS_SOAP_BINDING = "SosSoapBinding";
    
    public static final String EN_SWES_DESCRIBE_SENSOR = "DescribeSensor";
    public static final String EN_SWES_DESCRIBE_SENSOR_RESPONSE = "DescribeSensorResponse";
    public static final String EN_SWES_INSERT_SENSOR = "InsertSensor";
    public static final String EN_SWES_INSERT_SENSOR_RESPONSE = "InsertSensorResponse";
    public static final String EN_SWES_UPDATE_SENSOR_DESCRIPTION = "UpdateSensorDescription";
    public static final String EN_SWES_UPDATE_SENSOR_DESCRIPTION_RESPONSE = "UpdateSensorDescriptionResponse";

    public static final String EN_XSD_INCLUDE = "include";
    public static final String EN_XSD_SCHEMA = "schema";

    public static final QName QN_HTTP_ADDRESS = new QName(NS_HTTP, EN_HTTP_ADDRESS, NS_HTTP_PREFIX);
    public static final QName QN_HTTP_BINDING = new QName(NS_HTTP, EN_HTTP_BINDING ,NS_HTTP_PREFIX);
    public static final QName QN_HTTP_OPERATION = new QName(NS_HTTP, EN_HTTP_OPERATION, NS_HTTP_PREFIX);
    public static final QName QN_HTTP_URL_ENCODED = new QName(NS_HTTP, EN_HTTP_URL_ENCODED ,NS_HTTP_PREFIX);

    public static final QName QN_MIME_CONTENT = new QName(NS_MIME, EN_MIME_CONTENT, NS_MIME_PREFIX);
    public static final QName QN_MIME_MIME_XML = new QName(NS_MIME, EN_MIME_MIME_XML, NS_MIME_PREFIX);

    public static final QName QN_OWS_EXCEPTION_REPORT = new QName(NS_OWS, EN_OWS_EXCEPTION, NS_OWS_PREFIX);

    public static final QName QN_SOAP_ADDRESS = new QName(NS_SOAP, EN_SOAP_ADDRESS, NS_SOAP_PREFIX);
    public static final QName QN_SOAP_BINDING = new QName(NS_SOAP, EN_SOAP_BINDING, NS_SOAP_PREFIX);
    public static final QName QN_SOAP_BODY = new QName(NS_SOAP, EN_SOAP_BODY, NS_SOAP_PREFIX);
    public static final QName QN_SOAP_FAULT = new QName(NS_SOAP, EN_SOAP_FAULT, NS_SOAP_PREFIX);
    public static final QName QN_SOAP_OPERATION = new QName(NS_SOAP, EN_SOAP_OPERATION, NS_SOAP_PREFIX);

    public static final QName QN_SOS_GET_CAPABILITIES = new QName(NS_SOS, EN_SOS_GET_CAPABILITIES, NS_SOS_PREFIX);
    public static final QName QN_SOS_GET_CAPABILITIES_RESPONSE = new QName(NS_SOS, EN_SOS_CAPABILITIES, NS_SOS_PREFIX);
    public static final QName QN_SOS_GET_FEATURE_OF_INTEREST = new QName(NS_SOS, EN_SOS_GET_FEATURE_OF_INTEREST, NS_SOS_PREFIX);
    public static final QName QN_SOS_GET_FEATURE_OF_INTEREST_RESPONSE = new QName(NS_SOS, EN_SOS_GET_FEATURE_OF_INTEREST_RESPONSE, NS_SOS_PREFIX);
    public static final QName QN_SOS_GET_OBSERVATION = new QName(NS_SOS, EN_SOS_GET_OBSERVATION, NS_SOS_PREFIX);
    public static final QName QN_SOS_GET_OBSERVATION_BY_ID = new QName(NS_SOS, EN_SOS_GET_OBSERVATION_BY_ID, NS_SOS_PREFIX);
    public static final QName QN_SOS_GET_OBSERVATION_BY_ID_RESPONSE = new QName(NS_SOS, EN_SOS_GET_OBSERVATION_BY_ID_RESPONSE, NS_SOS_PREFIX);
    public static final QName QN_SOS_GET_OBSERVATION_RESPONSE = new QName(NS_SOS, EN_SOS_GET_OBSERVATION_RESPONSE, NS_SOS_PREFIX);
    public static final QName QN_SOS_GET_RESULT = new QName(NS_SOS, EN_SOS_GET_RESULT, NS_SOS_PREFIX);
    public static final QName QN_SOS_GET_RESULT_RESPONSE = new QName(NS_SOS, EN_SOS_GET_RESULT_RESPONSE, NS_SOS_PREFIX);
    public static final QName QN_SOS_GET_RESULT_TEMPLATE = new QName(NS_SOS, EN_SOS_GET_RESULT_TEMPLATE, NS_SOS_PREFIX);
    public static final QName QN_SOS_GET_RESULT_TEMPLATE_RESPONSE = new QName(NS_SOS, EN_SOS_GET_RESULT_TEMPLATE_RESPONSE, NS_SOS_PREFIX);
    public static final QName QN_SOS_INSERT_OBSERVATION = new QName(NS_SOS, EN_SOS_INSERT_OBSERVATION, NS_SOS_PREFIX);
    public static final QName QN_SOS_INSERT_OBSERVATION_RESPONSE = new QName(NS_SOS, EN_SOS_INSERT_OBSERVATION_RESPONSE, NS_SOS_PREFIX);
    public static final QName QN_SOS_INSERT_RESULT = new QName(NS_SOS, EN_SOS_INSERT_RESULT, NS_SOS_PREFIX);
    public static final QName QN_SOS_INSERT_RESULT_RESPONSE = new QName(NS_SOS, EN_SOS_INSERT_RESULT_RESPONSE, NS_SOS_PREFIX);
    public static final QName QN_SOS_INSERT_RESULT_TEMPLATE = new QName(NS_SOS, EN_SOS_INSERT_RESULT_TEMPLATE, NS_SOS_PREFIX);
    public static final QName QN_SOS_INSERT_RESULT_TEMPLATE_RESPONSE = new QName(NS_SOS, EN_SOS_INSERT_RESULT_TEMPLATE_RESPONSE, NS_SOS_PREFIX);

    public static final QName QN_SOSW_KVP_BINDING = new QName(NS_SOSW, EN_SOSW_SOS_KVP_BINDING, NS_SOSW_PREFIX);
    public static final QName QN_SOSW_GET_PORT_TYPE = new QName(NS_SOSW, EN_SOSW_SOS_GET_PORT_TYPE, NS_SOSW_PREFIX);
    public static final QName QN_SOSW_POST_PORT_TYPE = new QName(NS_SOSW, EN_SOSW_SOS_POST_PORT_TYPE, NS_SOSW_PREFIX);
    public static final QName QN_SOSW_POX_BINDING = new QName(NS_SOSW, EN_SOSW_SOS_POX_BINDING, NS_SOSW_PREFIX);
    public static final QName QN_SOSW_SERVICE = new QName(NS_SOSW, SosConstants.SOS);
    public static final QName QN_SOSW_SOAP_BINDING = new QName(NS_SOSW, EN_SOSW_SOS_SOAP_BINDING, NS_SOSW_PREFIX);

    public static final QName QN_SWES_DELETE_SENSOR = new QName(NS_SWES, EN_SOS_DELETE_SENSOR, NS_SWES_PREFIX);
    public static final QName QN_SWES_DELETE_SENSOR_RESPONSE = new QName(NS_SWES, EN_SOS_DELETE_SENSOR_RESPONSE, NS_SWES_PREFIX);
    public static final QName QN_SWES_DESCRIBE_SENSOR = new QName(NS_SWES, EN_SWES_DESCRIBE_SENSOR, NS_SWES_PREFIX);
    public static final QName QN_SWES_DESCRIBE_SENSOR_RESPONSE = new QName(NS_SWES, EN_SWES_DESCRIBE_SENSOR_RESPONSE, NS_SWES_PREFIX);
    public static final QName QN_SWES_INSERT_SENSOR = new QName(NS_SWES, EN_SWES_INSERT_SENSOR, NS_SWES_PREFIX);
    public static final QName QN_SWES_INSERT_SENSOR_RESPONSE = new QName(NS_SWES, EN_SWES_INSERT_SENSOR_RESPONSE, NS_SWES_PREFIX);
    public static final QName QN_SWES_UPDATE_SENSOR_DESCRIPTION = new QName(NS_SWES, EN_SWES_UPDATE_SENSOR_DESCRIPTION, NS_SWES_PREFIX);
    public static final QName QN_SWES_UPDATE_SENSOR_DESCRIPTION_RESPONSE = new QName(NS_SWES, EN_SWES_UPDATE_SENSOR_DESCRIPTION_RESPONSE, NS_SWES_PREFIX);
    
    public static final QName QN_WSAM_ACTION = new QName(NS_WSAM, AN_WSAM_ACTION, NS_WSAM_PREFIX);

    public static final QName QN_XSD_SCHEMA = new QName(NS_XSD, EN_XSD_SCHEMA, NS_XSD_PREFIX);

    public static final String MESSAGE_PART ="body";
    public static final String POX_CONTENT_TYPE = SosConstants.CONTENT_TYPE_XML;
    public static final String KVP_HTTP_VERB = "GET";
    public static final String POX_HTTP_VERB = "POST";
    public static final String QUALIFIED_ELEMENT_FORM_DEFAULT = "qualified";
    public static final String SOAP_BINDING_HTTP_TRANSPORT = "http://schemas.xmlsoap.org/soap/http";
    public static final String SOAP_DOCUMENT_STYLE= "document";

    public static final URI OWS_EXCEPTION_ACTION = URI.create("http://www.opengis.net/ows/1.1/Exception");
    public static final URI SWES_EXCEPTION_ACTION = URI.create("http://www.opengis.net/swes/2.0/Exception");
    
    public static final class SoapResponseActions {
        public static final URI DELETE_SENSOR = URI.create("http://www.opengis.net/swes/2.0/DeleteSensorResponse");
        public static final URI DESCRIBE_SENSOR = URI.create("http://www.opengis.net/swes/2.0/DescribeSensorResponse");
        public static final URI GET_CAPABILITIES = URI.create("http://www.opengis.net/sos/2.0/GetCapabilitiesResponse");
        public static final URI GET_FEATURE_OF_INTEREST = URI.create("http://www.opengis.net/sos/2.0/GetFeatureOfInterestResponse");
        public static final URI GET_OBSERVATION = URI.create("http://www.opengis.net/sos/2.0/GetObservationResponse");
        public static final URI GET_OBSERVATION_BY_ID = URI.create("http://www.opengis.net/sos/2.0/GetObservationByIdResponse");
        public static final URI GET_RESULT = URI.create("http://www.opengis.net/sos/2.0/GetResultResponse");
        public static final URI GET_RESULT_TEMPLATE = URI.create("http://www.opengis.net/sos/2.0/GetResultTemplateResponse");
        public static final URI INSERT_OBSERVATION = URI.create("http://www.opengis.net/sos/2.0/InsertObservationResponse");
        public static final URI INSERT_RESULT = URI.create("http://www.opengis.net/sos/2.0/InsertResultResponse");
        public static final URI INSERT_RESULT_TEMPLATE = URI.create("http://www.opengis.net/sos/2.0/InsertResultTemplateResponse");
        public static final URI INSERT_SENSOR = URI.create("http://www.opengis.net/swes/2.0/InsertSensorResponse");
        public static final URI UPDATE_SENSOR_DESCRIPTION = URI.create("http://www.opengis.net/swes/2.0/UpdateSensorDescriptionResponse");
        
        private SoapResponseActions() {}
    }
    
    public static final class SoapRequestActions {
        public static final URI DELETE_SENSOR = URI.create("http://www.opengis.net/swes/2.0/DeleteSensor");
        public static final URI DESCRIBE_SENSOR = URI.create("http://www.opengis.net/swes/2.0/DescribeSensor");
        public static final URI GET_CAPABILITIES = URI.create("http://www.opengis.net/sos/2.0/GetCapabilities");
        public static final URI GET_FEATURE_OF_INTEREST = URI.create("http://www.opengis.net/sos/2.0/GetFeatureOfInterest");
        public static final URI GET_OBSERVATION = URI.create("http://www.opengis.net/sos/2.0/GetObservation");
        public static final URI GET_OBSERVATION_BY_ID = URI.create("http://www.opengis.net/sos/2.0/GetObservationById");
        public static final URI GET_RESULT = URI.create("http://www.opengis.net/sos/2.0/GetResult");
        public static final URI GET_RESULT_TEMPLATE = URI.create("http://www.opengis.net/sos/2.0/GetResultTemplate");
        public static final URI INSERT_OBSERVATION = URI.create("http://www.opengis.net/sos/2.0/InsertObservation");
        public static final URI INSERT_RESULT = URI.create("http://www.opengis.net/sos/2.0/InsertResult");
        public static final URI INSERT_RESULT_TEMPLATE = URI.create("http://www.opengis.net/sos/2.0/InsertResultTemplate");
        public static final URI INSERT_SENSOR = URI.create("http://www.opengis.net/swes/2.0/InsertSensor");
        public static final URI UPDATE_SENSOR_DESCRIPTION = URI.create("http://www.opengis.net/swes/2.0/UpdateSensorDescription");
        
        private SoapRequestActions() {}
    }
    
    public static final class Operations {
        public static final WSDLOperation DELETE_SENSOR = WSDLOperation.newBuilder()
                .setName(Sos2Constants.Operations.DeleteSensor.name())
                .setVersion(Sos2Constants.SERVICEVERSION)
                .setRequest(QN_SWES_DELETE_SENSOR)
                .setRequestAction(SoapRequestActions.DELETE_SENSOR)
                .setResponse(QN_SWES_DELETE_SENSOR_RESPONSE)
                .setResponseAction(SoapResponseActions.DELETE_SENSOR)
                .setFaults(WSDLFault.DEFAULT_FAULTS)
                .build();
        public static final WSDLOperation DESCRIBE_SENSOR = WSDLOperation.newBuilder()
                .setName(SosConstants.Operations.DescribeSensor.name())
                .setVersion(Sos2Constants.SERVICEVERSION)
                .setRequest(QN_SWES_DESCRIBE_SENSOR)
                .setRequestAction(SoapRequestActions.DESCRIBE_SENSOR)
                .setResponse(QN_SWES_DESCRIBE_SENSOR_RESPONSE)
                .setResponseAction(SoapResponseActions.DESCRIBE_SENSOR)
                .setFaults(WSDLFault.DEFAULT_FAULTS)
                .build();
        public static final WSDLOperation GET_CAPABILITIES = WSDLOperation.newBuilder()
                .setName(SosConstants.Operations.GetCapabilities.name())
                .setVersion(Sos2Constants.SERVICEVERSION)
                .setRequest(QN_SOS_GET_CAPABILITIES)
                .setRequestAction(SoapRequestActions.GET_CAPABILITIES)
                .setResponse(QN_SOS_GET_CAPABILITIES_RESPONSE)
                .setResponseAction(SoapResponseActions.GET_CAPABILITIES)
                .setFaults(WSDLFault.DEFAULT_FAULTS)
                .addFault(WSDLFault.VERSION_NEGOTIATION_FAILED_EXCEPTION)
                .addFault(WSDLFault.INVALID_UPDATE_SEQUENCE_EXCEPTION)
                .build();
        public static final WSDLOperation GET_FEATURE_OF_INTEREST = WSDLOperation.newBuilder()
                .setName(SosConstants.Operations.GetFeatureOfInterest.name())
                .setVersion(Sos2Constants.SERVICEVERSION)
                .setRequest(QN_SOS_GET_FEATURE_OF_INTEREST)
                .setRequestAction(SoapRequestActions.GET_FEATURE_OF_INTEREST)
                .setResponse(QN_SOS_GET_FEATURE_OF_INTEREST_RESPONSE)
                .setResponseAction(SoapResponseActions.GET_FEATURE_OF_INTEREST)
                .setFaults(WSDLFault.DEFAULT_FAULTS)
                .build();
        public static final WSDLOperation GET_OBSERVATION = WSDLOperation.newBuilder()
                .setName(SosConstants.Operations.GetObservation.name())
                .setVersion(Sos2Constants.SERVICEVERSION)
                .setRequest(QN_SOS_GET_OBSERVATION)
                .setRequestAction(SoapRequestActions.GET_OBSERVATION)
                .setResponse(QN_SOS_GET_OBSERVATION_RESPONSE)
                .setResponseAction(SoapResponseActions.INSERT_OBSERVATION)
                .setFaults(WSDLFault.DEFAULT_FAULTS)
                .build();
        public static final WSDLOperation GET_OBSERVATION_BY_ID = WSDLOperation.newBuilder()
                .setName(SosConstants.Operations.GetObservationById.name())
                .setVersion(Sos2Constants.SERVICEVERSION)
                .setRequest(QN_SOS_GET_OBSERVATION_BY_ID)
                .setRequestAction(SoapRequestActions.GET_OBSERVATION_BY_ID)
                .setResponse(QN_SOS_GET_OBSERVATION_BY_ID_RESPONSE)
                .setResponseAction(SoapResponseActions.GET_OBSERVATION_BY_ID)
                .setFaults(WSDLFault.DEFAULT_FAULTS)
                .build();
        public static final WSDLOperation GET_RESULT = WSDLOperation.newBuilder()
                .setName(SosConstants.Operations.GetResult.name())
                .setVersion(Sos2Constants.SERVICEVERSION)
                .setRequest(QN_SOS_GET_RESULT)
                .setRequestAction(SoapRequestActions.GET_RESULT)
                .setResponse(QN_SOS_GET_RESULT_RESPONSE)
                .setResponseAction(SoapResponseActions.GET_RESULT)
                .setFaults(WSDLFault.DEFAULT_FAULTS)
                .build();
        public static final WSDLOperation GET_RESULT_TEMPLATE = WSDLOperation.newBuilder()
                .setName(Sos2Constants.Operations.GetResultTemplate.name())
                .setVersion(Sos2Constants.SERVICEVERSION)
                .setRequest(QN_SOS_GET_RESULT_TEMPLATE)
                .setRequestAction(SoapRequestActions.GET_RESULT_TEMPLATE)
                .setResponse(QN_SOS_GET_RESULT_TEMPLATE_RESPONSE)
                .setResponseAction(SoapResponseActions.GET_RESULT_TEMPLATE)
                .setFaults(WSDLFault.DEFAULT_FAULTS)
                .build();
        public static final WSDLOperation INSERT_OBSERVATION = WSDLOperation.newBuilder()
                .setName(SosConstants.Operations.InsertObservation.name())
                .setVersion(Sos2Constants.SERVICEVERSION)
                .setRequest(QN_SOS_INSERT_OBSERVATION)
                .setRequestAction(SoapRequestActions.INSERT_OBSERVATION)
                .setResponse(QN_SOS_INSERT_OBSERVATION_RESPONSE)
                .setResponseAction(SoapResponseActions.INSERT_OBSERVATION)
                .setFaults(WSDLFault.DEFAULT_FAULTS)
                .build();
        public static final WSDLOperation INSERT_RESULT = WSDLOperation.newBuilder()
                .setName(Sos2Constants.Operations.InsertResult.name())
                .setVersion(Sos2Constants.SERVICEVERSION)
                .setRequest(QN_SOS_INSERT_RESULT)
                .setRequestAction(SoapRequestActions.INSERT_RESULT)
                .setResponse(QN_SOS_INSERT_RESULT_RESPONSE)
                .setResponseAction(SoapResponseActions.INSERT_RESULT)
                .setFaults(WSDLFault.DEFAULT_FAULTS)
                .build();
        public static final WSDLOperation INSERT_RESULT_TEMPLATE = WSDLOperation.newBuilder()
                .setName(Sos2Constants.Operations.InsertResultTemplate.name())
                .setVersion(Sos2Constants.SERVICEVERSION)
                .setRequest(QN_SOS_INSERT_RESULT_TEMPLATE)
                .setRequestAction(SoapRequestActions.INSERT_RESULT_TEMPLATE)
                .setResponse(QN_SOS_INSERT_RESULT_TEMPLATE_RESPONSE)
                .setResponseAction(SoapResponseActions.INSERT_RESULT_TEMPLATE)
                .setFaults(WSDLFault.DEFAULT_FAULTS)
                .build();
        public static final WSDLOperation INSERT_SENSOR = WSDLOperation.newBuilder()
                .setName(Sos2Constants.Operations.InsertSensor.name())
                .setVersion(Sos2Constants.SERVICEVERSION)
                .setRequest(QN_SWES_INSERT_SENSOR)
                .setRequestAction(SoapRequestActions.INSERT_SENSOR)
                .setResponse(QN_SWES_INSERT_SENSOR_RESPONSE)
                .setResponseAction(SoapResponseActions.INSERT_SENSOR)
                .setFaults(WSDLFault.DEFAULT_FAULTS)
                .build();
        public static final WSDLOperation UPDATE_SENSOR_DESCRIPTION = WSDLOperation.newBuilder()
                .setName(Sos2Constants.Operations.UpdateSensorDescription.name())
                .setVersion(Sos2Constants.SERVICEVERSION)
                .setRequest(QN_SWES_UPDATE_SENSOR_DESCRIPTION)
                .setRequestAction(SoapRequestActions.UPDATE_SENSOR_DESCRIPTION)
                .setResponse(QN_SWES_UPDATE_SENSOR_DESCRIPTION_RESPONSE)
                .setResponseAction(SoapResponseActions.UPDATE_SENSOR_DESCRIPTION)
                .setFaults(WSDLFault.DEFAULT_FAULTS)
                .build();

        private Operations() {}
    }
    
    private WSDLConstants() {}
    
}
