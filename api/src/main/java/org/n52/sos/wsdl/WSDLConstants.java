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

import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosSoapConstants;
import org.n52.sos.ogc.swe.SWEConstants;

/**
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public interface WSDLConstants {
        
    String NS_HTTP = "http://schemas.xmlsoap.org/wsdl/http/";
    String NS_HTTP_PREFIX = "http";
    String NS_MIME = "http://schemas.xmlsoap.org/wsdl/mime/";
    String NS_MIME_PREFIX = "mime";
    String NS_SOAP = "http://schemas.xmlsoap.org/wsdl/soap/";
    String NS_SOAP_PREFIX = "soap";
    String NS_SOSW = "http://www.opengis.net/sos/2.0/wsdl";
    String NS_SOSW_PREFIX = "sosw";
    String NS_WSAM = "http://www.w3.org/2007/05/addressing/metadata";
    String NS_WSAM_PREFIX = "wsam";
    String NS_WSDL = "http://schemas.xmlsoap.org/wsdl/";
    String NS_WSDL_PREFIX = "wsdl";
    String NS_XSD = "http://www.w3.org/2001/XMLSchema";
    String NS_XSD_PREFIX = "xsd";

    String AN_WSAM_ACTION = "Action";
    String AN_XSD_ELEMENT_FORM_DEFAULT = "elementFormDefault";
    String AN_XSD_SCHEMA_LOCATION = "schemaLocation";
    String AN_XSD_TARGET_NAMESPACE = "targetNamespace";

    String EN_HTTP_ADDRESS = "address";
    String EN_HTTP_BINDING = "binding";
    String EN_HTTP_OPERATION = "operation";
    String EN_HTTP_URL_ENCODED = "urlEncoded";

    String EN_MIME_CONTENT = "content";
    String EN_MIME_MIME_XML = "mimeXml";

    String EN_SOAP_ADDRESS = "address";
    String EN_SOAP_BINDING = "binding";
    String EN_SOAP_BODY = "body";
    String EN_SOAP_FAULT = "fault";
    String EN_SOAP_OPERATION = "operation";
    
    String EN_SOSW_SOS_KVP_BINDING = "SosKvpBinding";
    String EN_SOSW_SOS_GET_PORT_TYPE = "SosGetPortType";
    String EN_SOSW_SOS_POST_PORT_TYPE = "SosPostPortType";
    String EN_SOSW_SOS_POX_BINDING = "SosPoxBinding";
    String EN_SOSW_SOS_SOAP_BINDING = "SosSoapBinding";
    
    String EN_XSD_INCLUDE = "include";
    String EN_XSD_SCHEMA = "schema";

    QName QN_HTTP_ADDRESS = new QName(NS_HTTP, EN_HTTP_ADDRESS, NS_HTTP_PREFIX);
    QName QN_HTTP_BINDING = new QName(NS_HTTP, EN_HTTP_BINDING ,NS_HTTP_PREFIX);
    QName QN_HTTP_OPERATION = new QName(NS_HTTP, EN_HTTP_OPERATION, NS_HTTP_PREFIX);
    QName QN_HTTP_URL_ENCODED = new QName(NS_HTTP, EN_HTTP_URL_ENCODED ,NS_HTTP_PREFIX);

    QName QN_MIME_CONTENT = new QName(NS_MIME, EN_MIME_CONTENT, NS_MIME_PREFIX);
    QName QN_MIME_MIME_XML = new QName(NS_MIME, EN_MIME_MIME_XML, NS_MIME_PREFIX);

    QName QN_SOAP_ADDRESS = new QName(NS_SOAP, EN_SOAP_ADDRESS, NS_SOAP_PREFIX);
    QName QN_SOAP_BINDING = new QName(NS_SOAP, EN_SOAP_BINDING, NS_SOAP_PREFIX);
    QName QN_SOAP_BODY = new QName(NS_SOAP, EN_SOAP_BODY, NS_SOAP_PREFIX);
    QName QN_SOAP_FAULT = new QName(NS_SOAP, EN_SOAP_FAULT, NS_SOAP_PREFIX);
    QName QN_SOAP_OPERATION = new QName(NS_SOAP, EN_SOAP_OPERATION, NS_SOAP_PREFIX);

    QName QN_SOSW_KVP_BINDING = new QName(NS_SOSW, EN_SOSW_SOS_KVP_BINDING, NS_SOSW_PREFIX);
    QName QN_SOSW_GET_PORT_TYPE = new QName(NS_SOSW, EN_SOSW_SOS_GET_PORT_TYPE, NS_SOSW_PREFIX);
    QName QN_SOSW_POST_PORT_TYPE = new QName(NS_SOSW, EN_SOSW_SOS_POST_PORT_TYPE, NS_SOSW_PREFIX);
    QName QN_SOSW_POX_BINDING = new QName(NS_SOSW, EN_SOSW_SOS_POX_BINDING, NS_SOSW_PREFIX);
    QName QN_SOSW_SERVICE = new QName(NS_SOSW, SosConstants.SOS);
    QName QN_SOSW_SOAP_BINDING = new QName(NS_SOSW, EN_SOSW_SOS_SOAP_BINDING, NS_SOSW_PREFIX);
    
    QName QN_WSAM_ACTION = new QName(NS_WSAM, AN_WSAM_ACTION, NS_WSAM_PREFIX);

    QName QN_XSD_SCHEMA = new QName(NS_XSD, EN_XSD_SCHEMA, NS_XSD_PREFIX);

    String MESSAGE_PART ="body";
    String POX_CONTENT_TYPE = SosConstants.CONTENT_TYPE_XML;
    String KVP_HTTP_VERB = SosConstants.HTTP_GET;
    String POX_HTTP_VERB = SosConstants.HTTP_POST;
    String QUALIFIED_ELEMENT_FORM_DEFAULT = "qualified";
    String SOAP_BINDING_HTTP_TRANSPORT = "http://schemas.xmlsoap.org/soap/http";
    String SOAP_DOCUMENT_STYLE= "document";

    URI OWS_EXCEPTION_ACTION = URI.create(SosSoapConstants.RESP_ACTION_OWS);
    URI SWES_EXCEPTION_ACTION = URI.create(SosSoapConstants.RESP_ACTION_SWES);
    
    interface SoapResponseActionUris {
        URI DELETE_SENSOR = URI.create(SosSoapConstants.RESP_ACTION_DELETE_SENSOR);
        URI DESCRIBE_SENSOR = URI.create(SosSoapConstants.RESP_ACTION_DESCRIBE_SENSOR);
        URI GET_CAPABILITIES = URI.create(SosSoapConstants.RESP_ACTION_GET_CAPABILITIES);
        URI GET_FEATURE_OF_INTEREST = URI.create(SosSoapConstants.RESP_ACTION_GET_FEATURE_OF_INTEREST);
        URI GET_OBSERVATION = URI.create(SosSoapConstants.RESP_ACTION_GET_OBSERVATION);
        URI GET_OBSERVATION_BY_ID = URI.create(SosSoapConstants.RESP_ACTION_GET_OBSERVATION_BY_ID);
        URI GET_RESULT = URI.create(SosSoapConstants.RESP_ACTION_GET_RESULT);
        URI GET_RESULT_TEMPLATE = URI.create(SosSoapConstants.RESP_ACTION_GET_RESULT_TEMPLATE);
        URI INSERT_OBSERVATION = URI.create(SosSoapConstants.REQ_ACTION_INSERT_OBSERVATION);
        URI INSERT_RESULT = URI.create(SosSoapConstants.RESP_ACTION_INSERT_RESULT);
        URI INSERT_RESULT_TEMPLATE = URI.create(SosSoapConstants.RESP_ACTION_INSERT_RESULT_TEMPLATE);
        URI INSERT_SENSOR = URI.create(SosSoapConstants.RESP_ACTION_INSERT_SENSOR);
        URI UPDATE_SENSOR_DESCRIPTION = URI.create(SosSoapConstants.RESP_ACTION_UPDATE_SENSOR_DESCRIPTION);
    }
    
    interface SoapRequestActionUris {
        URI DELETE_SENSOR = URI.create(SosSoapConstants.REQ_ACTION_DELETE_SENSOR);
        URI DESCRIBE_SENSOR = URI.create(SosSoapConstants.REQ_ACTION_DESCRIBE_SENSOR);
        URI GET_CAPABILITIES = URI.create(SosSoapConstants.REQ_ACTION_GET_CAPABILITIES);
        URI GET_FEATURE_OF_INTEREST = URI.create(SosSoapConstants.REQ_ACTION_GET_FEATURE_OF_INTEREST);
        URI GET_OBSERVATION = URI.create(SosSoapConstants.REQ_ACTION_GET_OBSERVATION);
        URI GET_OBSERVATION_BY_ID = URI.create(SosSoapConstants.REQ_ACTION_GET_OBSERVATION_BY_ID);
        URI GET_RESULT = URI.create(SosSoapConstants.REQ_ACTION_GET_RESULT);
        URI GET_RESULT_TEMPLATE = URI.create(SosSoapConstants.REQ_ACTION_GET_RESULT_TEMPLATE);
        URI INSERT_OBSERVATION = URI.create(SosSoapConstants.REQ_ACTION_INSERT_OBSERVATION);
        URI INSERT_RESULT = URI.create(SosSoapConstants.REQ_ACTION_INSERT_RESULT);
        URI INSERT_RESULT_TEMPLATE = URI.create(SosSoapConstants.REQ_ACTION_INSERT_RESULT_TEMPLATE);
        URI INSERT_SENSOR = URI.create(SosSoapConstants.REQ_ACTION_INSERT_SENSOR);
        URI UPDATE_SENSOR_DESCRIPTION = URI.create(SosSoapConstants.REQ_ACTION_UPDATE_SENSOR_DESCRIPTION);
    }
    
    interface Operations {
        WSDLOperation DELETE_SENSOR = WSDLOperation.newWSDLOperation()
                .setName(Sos2Constants.Operations.DeleteSensor.name())
                .setVersion(Sos2Constants.SERVICEVERSION)
                .setRequest(SWEConstants.QN_DELETE_SENSOR)
                .setRequestAction(SoapRequestActionUris.DELETE_SENSOR)
                .setResponse(SWEConstants.QN_DELETE_SENSOR_RESPONSE)
                .setResponseAction(SoapResponseActionUris.DELETE_SENSOR)
                .setFaults(WSDLFault.DEFAULT_FAULTS)
                .build();
        WSDLOperation DESCRIBE_SENSOR = WSDLOperation.newWSDLOperation()
                .setName(SosConstants.Operations.DescribeSensor.name())
                .setVersion(Sos2Constants.SERVICEVERSION)
                .setRequest(SWEConstants.QN_DESCRIBE_SENSOR)
                .setRequestAction(SoapRequestActionUris.DESCRIBE_SENSOR)
                .setResponse(SWEConstants.QN_DESCRIBE_SENSOR_RESPONSE)
                .setResponseAction(SoapResponseActionUris.DESCRIBE_SENSOR)
                .setFaults(WSDLFault.DEFAULT_FAULTS)
                .build();
        WSDLOperation GET_CAPABILITIES = WSDLOperation.newWSDLOperation()
                .setName(SosConstants.Operations.GetCapabilities.name())
                .setVersion(Sos2Constants.SERVICEVERSION)
                .setRequest(Sos2Constants.QN_GET_CAPABILITIES)
                .setRequestAction(SoapRequestActionUris.GET_CAPABILITIES)
                .setResponse(Sos2Constants.QN_CAPABILITIES)
                .setResponseAction(SoapResponseActionUris.GET_CAPABILITIES)
                .setFaults(WSDLFault.DEFAULT_FAULTS)
                .addFault(WSDLFault.VERSION_NEGOTIATION_FAILED_EXCEPTION)
                .addFault(WSDLFault.INVALID_UPDATE_SEQUENCE_EXCEPTION)
                .build();
        WSDLOperation GET_FEATURE_OF_INTEREST = WSDLOperation.newWSDLOperation()
                .setName(SosConstants.Operations.GetFeatureOfInterest.name())
                .setVersion(Sos2Constants.SERVICEVERSION)
                .setRequest(Sos2Constants.QN_GET_FEATURE_OF_INTEREST)
                .setRequestAction(SoapRequestActionUris.GET_FEATURE_OF_INTEREST)
                .setResponse(Sos2Constants.QN_GET_FEATURE_OF_INTEREST_RESPONSE)
                .setResponseAction(SoapResponseActionUris.GET_FEATURE_OF_INTEREST)
                .setFaults(WSDLFault.DEFAULT_FAULTS)
                .build();
        WSDLOperation GET_OBSERVATION = WSDLOperation.newWSDLOperation()
                .setName(SosConstants.Operations.GetObservation.name())
                .setVersion(Sos2Constants.SERVICEVERSION)
                .setRequest(Sos2Constants.QN_GET_OBSERVATION)
                .setRequestAction(SoapRequestActionUris.GET_OBSERVATION)
                .setResponse(Sos2Constants.QN_GET_OBSERVATION_RESPONSE)
                .setResponseAction(SoapResponseActionUris.INSERT_OBSERVATION)
                .setFaults(WSDLFault.DEFAULT_FAULTS)
                .build();
        WSDLOperation GET_OBSERVATION_BY_ID = WSDLOperation.newWSDLOperation()
                .setName(SosConstants.Operations.GetObservationById.name())
                .setVersion(Sos2Constants.SERVICEVERSION)
                .setRequest(Sos2Constants.QN_GET_OBSERVATION_BY_ID)
                .setRequestAction(SoapRequestActionUris.GET_OBSERVATION_BY_ID)
                .setResponse(Sos2Constants.QN_GET_OBSERVATION_BY_ID_RESPONSE)
                .setResponseAction(SoapResponseActionUris.GET_OBSERVATION_BY_ID)
                .setFaults(WSDLFault.DEFAULT_FAULTS)
                .build();
        WSDLOperation GET_RESULT = WSDLOperation.newWSDLOperation()
                .setName(SosConstants.Operations.GetResult.name())
                .setVersion(Sos2Constants.SERVICEVERSION)
                .setRequest(Sos2Constants.QN_GET_RESULT)
                .setRequestAction(SoapRequestActionUris.GET_RESULT)
                .setResponse(Sos2Constants.QN_GET_RESULT_RESPONSE)
                .setResponseAction(SoapResponseActionUris.GET_RESULT)
                .setFaults(WSDLFault.DEFAULT_FAULTS)
                .build();
        WSDLOperation GET_RESULT_TEMPLATE = WSDLOperation.newWSDLOperation()
                .setName(Sos2Constants.Operations.GetResultTemplate.name())
                .setVersion(Sos2Constants.SERVICEVERSION)
                .setRequest(Sos2Constants.QN_GET_RESULT_TEMPLATE)
                .setRequestAction(SoapRequestActionUris.GET_RESULT_TEMPLATE)
                .setResponse(Sos2Constants.QN_GET_RESULT_TEMPLATE_RESPONSE)
                .setResponseAction(SoapResponseActionUris.GET_RESULT_TEMPLATE)
                .setFaults(WSDLFault.DEFAULT_FAULTS)
                .build();
        WSDLOperation INSERT_OBSERVATION = WSDLOperation.newWSDLOperation()
                .setName(SosConstants.Operations.InsertObservation.name())
                .setVersion(Sos2Constants.SERVICEVERSION)
                .setRequest(Sos2Constants.QN_INSERT_OBSERVATION)
                .setRequestAction(SoapRequestActionUris.INSERT_OBSERVATION)
                .setResponse(Sos2Constants.QN_INSERT_OBSERVATION_RESPONSE)
                .setResponseAction(SoapResponseActionUris.INSERT_OBSERVATION)
                .setFaults(WSDLFault.DEFAULT_FAULTS)
                .build();
        WSDLOperation INSERT_RESULT = WSDLOperation.newWSDLOperation()
                .setName(Sos2Constants.Operations.InsertResult.name())
                .setVersion(Sos2Constants.SERVICEVERSION)
                .setRequest(Sos2Constants.QN_INSERT_RESULT)
                .setRequestAction(SoapRequestActionUris.INSERT_RESULT)
                .setResponse(Sos2Constants.QN_INSERT_RESULT_RESPONSE)
                .setResponseAction(SoapResponseActionUris.INSERT_RESULT)
                .setFaults(WSDLFault.DEFAULT_FAULTS)
                .build();
        WSDLOperation INSERT_RESULT_TEMPLATE = WSDLOperation.newWSDLOperation()
                .setName(Sos2Constants.Operations.InsertResultTemplate.name())
                .setVersion(Sos2Constants.SERVICEVERSION)
                .setRequest(Sos2Constants.QN_INSERT_RESULT_TEMPLATE)
                .setRequestAction(SoapRequestActionUris.INSERT_RESULT_TEMPLATE)
                .setResponse(Sos2Constants.QN_INSERT_RESULT_TEMPLATE_RESPONSE)
                .setResponseAction(SoapResponseActionUris.INSERT_RESULT_TEMPLATE)
                .setFaults(WSDLFault.DEFAULT_FAULTS)
                .build();
        WSDLOperation INSERT_SENSOR = WSDLOperation.newWSDLOperation()
                .setName(Sos2Constants.Operations.InsertSensor.name())
                .setVersion(Sos2Constants.SERVICEVERSION)
                .setRequest(SWEConstants.QN_INSERT_SENSOR)
                .setRequestAction(SoapRequestActionUris.INSERT_SENSOR)
                .setResponse(SWEConstants.QN_INSERT_SENSOR_RESPONSE)
                .setResponseAction(SoapResponseActionUris.INSERT_SENSOR)
                .setFaults(WSDLFault.DEFAULT_FAULTS)
                .build();
        WSDLOperation UPDATE_SENSOR_DESCRIPTION = WSDLOperation.newWSDLOperation()
                .setName(Sos2Constants.Operations.UpdateSensorDescription.name())
                .setVersion(Sos2Constants.SERVICEVERSION)
                .setRequest(SWEConstants.QN_UPDATE_SENSOR_DESCRIPTION)
                .setRequestAction(SoapRequestActionUris.UPDATE_SENSOR_DESCRIPTION)
                .setResponse(SWEConstants.QN_UPDATE_SENSOR_DESCRIPTION_RESPONSE)
                .setResponseAction(SoapResponseActionUris.UPDATE_SENSOR_DESCRIPTION)
                .setFaults(WSDLFault.DEFAULT_FAULTS)
                .build();
    }
}
