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
package org.n52.sos.encode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OWSConstants.OwsExceptionCode;
import org.n52.sos.ogc.ows.OwsException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.ogc.sos.SosSoapConstants;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.soap.SoapFault;
import org.n52.sos.soap.SoapHeader;
import org.n52.sos.soap.SoapHelper;
import org.n52.sos.soap.SoapResponse;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.W3CConstants;
import org.n52.sos.wsa.WsaConstants;
import org.n52.sos.wsa.WsaHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class SoapEncoder implements IEncoder<ServiceResponse, SoapResponse> {

    /** the logger, used to log exceptions and additonaly information */
    private static Logger LOGGER = LoggerFactory.getLogger(SoapEncoder.class);

    private List<EncoderKeyType> encoderKeyTypes;
    
    private Map<SupportedTypeKey, Set<String>> supportedTypes;
    
    private Set<String> conformanceClasses;
    
    /**
     * constructor
     */
    public SoapEncoder() {
        super();
        supportedTypes = new HashMap<SupportedTypeKey, Set<String>>(0);
        conformanceClasses = new HashSet<String>(0);
        encoderKeyTypes = new ArrayList<EncoderKeyType>();
        encoderKeyTypes.add(new EncoderKeyType(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE));
        encoderKeyTypes.add(new EncoderKeyType(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE));
        StringBuilder logMsgBuilder = new StringBuilder();
        logMsgBuilder.append("Encoder for the following keys initialized successfully: ");
        for (EncoderKeyType encoderKeyType : encoderKeyTypes) {
            logMsgBuilder.append(encoderKeyType.toString());
            logMsgBuilder.append(", ");
        }
        logMsgBuilder.delete(logMsgBuilder.lastIndexOf(", "), logMsgBuilder.length());
        logMsgBuilder.append("!");
        LOGGER.info(logMsgBuilder.toString());
    }

    @Override
    public List<EncoderKeyType> getEncoderKeyType() {
        return encoderKeyTypes;
    }
    
    @Override
    public Map<SupportedTypeKey, Set<String>> getSupportedTypes() {
        return supportedTypes;
    }

    @Override
    public Set<String> getConformanceClasses() {
        return conformanceClasses;
    }

    public void addNamespacePrefixToMap(Map<String, String> nameSpacePrefixMap) {
    }
    
    @Override
    public String getContentType() {
        return "application/soap+xml";
    }

    @Override
    public ServiceResponse encode(SoapResponse response) throws OwsExceptionReport {
        return encode(response, null);
    }

    @Override
    public ServiceResponse encode(SoapResponse response, Map<HelperValues, String> additionalValues)
            throws OwsExceptionReport {
        SoapResponse soapResponse = (SoapResponse) response;
        String soapVersion = soapResponse.getSoapVersion();
        SOAPMessage soapResponseMessage = null;
        String action = null;
        try {
            soapResponseMessage = SoapHelper.getSoapMessageForProtocol(soapVersion);
            if (soapResponse.getSoapFault() != null) {
                createSOAPFault(soapResponseMessage.getSOAPBody().addFault(), soapResponse.getSoapFault());
            } else {
                if (soapResponse.getException() != null) {
                    action =
                            createSOAPFaultFromExceptionResponse(soapResponseMessage.getSOAPBody().addFault(),
                                    soapResponse.getException());

                } else {
                    action =
                            createSOAPBody(soapResponseMessage, soapResponse.getSoapBodyContent(),
                                    soapResponse.getSoapAction());
                }
            }
            if (soapResponse.getHeader() != null) {
                Map<String, SoapHeader> headers = soapResponse.getHeader();
                for (String namespace : headers.keySet()) {
                    SoapHeader header = headers.get(namespace);
                    if (namespace.equals(WsaConstants.NS_WSA)) {
                        WsaHeader wsa = (WsaHeader) header;
                        wsa.setActionValue(action);
                    }
                    try {
                        IEncoder encoder = Configurator.getInstance().getEncoder(namespace);
                        if (encoder != null) {
                            Map<QName, String> headerElements = (Map<QName, String>) encoder.encode(header);
                            for (QName qName : headerElements.keySet()) {
                                soapResponseMessage.getSOAPHeader().addChildElement(qName)
                                        .setTextContent(headerElements.get(qName));
                            }
                        }
                    } catch (OwsExceptionReport owse) {
                        throw owse;
                    }
                }

            } else {
                soapResponseMessage.getSOAPHeader().detachNode();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            soapResponseMessage.writeTo(baos);
            boolean applicationZip = false;
            if (soapResponse.getSoapBodyContent() != null){
                applicationZip = soapResponse.getSoapBodyContent().getApplyGzipCompression();
            }
            return new ServiceResponse(baos, SosConstants.CONTENT_TYPE_XML, applicationZip, true);
        } catch (SOAPException soape) {
            String exceptionText = "Error while encoding SOAPMessage!";
            LOGGER.debug(exceptionText, soape);
            throw Util4Exceptions.createNoApplicableCodeException(soape, exceptionText);
        } catch (IOException ioe) {
            String exceptionText = "Error while encoding SOAPMessage!";
            LOGGER.debug(exceptionText, ioe);
            throw Util4Exceptions.createNoApplicableCodeException(ioe, exceptionText);
        }
    }

    /**
     * Creates a SOAPBody element from SOS response
     * 
     * @param soapResponseMessage
     *            SOAPBody element
     * @param sosResponse
     *            SOS response
     * @throws SOAPException
     *             if an error occurs.
     */
    private String createSOAPBody(SOAPMessage soapResponseMessage, ServiceResponse sosResponse, String actionURI)
            throws SOAPException {
        try {
            if (sosResponse != null) {
                XmlObject xmlObject = XmlObject.Factory.parse(new String(sosResponse.getByteArray()));
                addAndRemoveSchemaLocationForSOAP(xmlObject, soapResponseMessage);
                // Document doc = parseSosResponseToDocument(xmlObject);
                // soapResponseMessage.getSOAPBody().addDocument(doc);
                soapResponseMessage.getSOAPBody().addDocument((Document) xmlObject.getDomNode());
                return actionURI;
            } else {
                SoapFault fault = new SoapFault();
                fault.setFaultCode(SOAPConstants.SOAP_RECEIVER_FAULT);
                fault.setFaultSubcode(new QName(OWSConstants.NS_OWS, OwsExceptionCode.NoApplicableCode.name(),
                        OWSConstants.NS_OWS_PREFIX));
                fault.setFaultReason("A server exception was encountered.");
                fault.setLocale(Locale.ENGLISH);
                fault.setDetailText("Missing SOS response document!");
                createSOAPFault(soapResponseMessage.getSOAPBody().addFault(), fault);
            }
        } catch (XmlException xmle) {
            LOGGER.error("Error while creating SOAP body !", xmle);
        }
        return null;
    }

    /**
     * Creates a SOAPFault element from SOS internal fault
     * 
     * @param fault
     *            SOAPFault element
     * @param sosSOAPFault
     *            SOS internal fault
     * @throws SOAPException
     *             if an error occurs.
     */
    private void createSOAPFault(SOAPFault fault, SoapFault soapFault) throws SOAPException {
        fault.setFaultCode(soapFault.getFaultCode());
        fault.setFaultString(soapFault.getFaultReason(), soapFault.getLocale());
        if (soapFault.getDetailText() != null) {
            fault.addDetail().setTextContent(soapFault.getDetailText());
        }
    }

    /**
     * Creates a SOAPFault element from SOS exception
     * 
     * @param soapFault
     *            SOAPFault element
     * @param sosResponse
     *            SOS exception
     * @return SOAP action URI.
     * @throws SOAPException
     *             if an error occurs.
     */
    private String createSOAPFaultFromExceptionResponse(SOAPFault soapFault, OwsExceptionReport owsExceptionReport)
            throws SOAPException {
        // FIXME: check and fix support for ExceptionReport with multiple exceptions!
        if (owsExceptionReport.getExceptions() != null && !owsExceptionReport.getExceptions().isEmpty()) {
            OwsException firstException = owsExceptionReport.getExceptions().get(0);
            if (soapFault.getNamespaceURI().equalsIgnoreCase(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE)) {
                QName qname = new QName(soapFault.getNamespaceURI(), "Client", soapFault.getPrefix());
                soapFault.setFaultCode(qname);
            } else {
                soapFault.setFaultCode(SOAPConstants.SOAP_SENDER_FAULT);
                if (firstException.getCode() != null) {
                    soapFault.appendFaultSubcode(new QName(OWSConstants.NS_OWS, firstException.getCode().toString(),
                            OWSConstants.NS_OWS_PREFIX));
                } else {
                    soapFault.appendFaultSubcode(new QName(OWSConstants.NS_OWS, OwsExceptionCode.NoApplicableCode
                            .name(), OWSConstants.NS_OWS_PREFIX));
                }
            }
            soapFault.addFaultReasonText(SoapHelper.getSoapFaultReasonText(firstException.getCode()), Locale.ENGLISH);
            for (OwsException exception : owsExceptionReport.getExceptions()) {
                // set fault detail
                Detail detail = soapFault.addDetail();
                createSOAPFaultDetail(detail, exception);
            }
            return SoapHelper.getExceptionActionURI(firstException.getCode());
        }
        SoapFault fault = new SoapFault();
        fault.setFaultCode(SOAPConstants.SOAP_RECEIVER_FAULT);
        fault.setFaultSubcode(new QName(OWSConstants.NS_OWS, OwsExceptionCode.NoApplicableCode.name(),
                OWSConstants.NS_OWS_PREFIX));
        fault.setFaultReason("A server exception was encountered.");
        fault.setLocale(Locale.ENGLISH);
        fault.setDetailText("Error while creating SOAPFault element from OWSException! OWSException is missing!");
        createSOAPFault(soapFault, fault);
        return SosSoapConstants.RESP_ACTION_SOS;
    }

    /**
     * Creates a SOAPDetail element from SOS exception document.
     * 
     * @param detail
     *            SOAPDetail
     * @param exception
     *            SOS Exception document
     * @throws SOAPException
     *             if an error occurs.
     */
    private void createSOAPFaultDetail(Detail detail, OwsException exception) throws SOAPException {
        SOAPElement exRep =
                detail.addChildElement(new QName(OWSConstants.NS_OWS, OWSConstants.EN_EXCEPTION,
                        OWSConstants.NS_OWS_PREFIX));
        exRep.addNamespaceDeclaration(OWSConstants.NS_OWS_PREFIX, OWSConstants.NS_OWS);
        String code = exception.getCode().toString();
        String locator = exception.getLocator();
        StringBuilder exceptionText = new StringBuilder();
        for (String text : exception.getMessages()) {
            exceptionText.append(text);
            exceptionText.append("\n");
        }
        if (exception.getException() != null) {
            exceptionText.append("\n[EXEPTION]: \n");
            if (exception.getException().getLocalizedMessage() != null
                    && !exception.getException().getLocalizedMessage().isEmpty()) {
                exceptionText.append(exception.getException().getLocalizedMessage());
                exceptionText.append("\n");
            }
            if (exception.getException().getMessage() != null && !exception.getException().getMessage().isEmpty()) {
                exceptionText.append(exception.getException().getMessage());
                exceptionText.append("\n");
            }
        }
        exRep.addAttribute(new QName(OWSConstants.NS_OWS, OWSConstants.EN_EXCEPTION_CODE, OWSConstants.NS_OWS_PREFIX),
                code);
        if (locator != null && !locator.equals("")) {
            exRep.addAttribute(new QName(OWSConstants.NS_OWS, OWSConstants.EN_LOCATOR, OWSConstants.NS_OWS_PREFIX),
                    locator);
        }
        if (exceptionText.length() != 0) {
            SOAPElement execText =
                    exRep.addChildElement(new QName(OWSConstants.NS_OWS, OWSConstants.EN_EXCEPTION_TEXT,
                            OWSConstants.NS_OWS_PREFIX));
            execText.setTextContent(exceptionText.toString());
        }
    }

    /**
     * Check SOS response for xsi:schemaLocation, remove attribute and add
     * attribute to SOAP message
     * 
     * @param soapResponseMessage
     * @param xmlObject
     * 
     * @param SOS
     *            response
     * @param soapResponseMessage
     *            SOAP response message
     * @throws SOAPException
     *             If an error occurs
     */
    private void addAndRemoveSchemaLocationForSOAP(XmlObject xmlObject, SOAPMessage soapResponseMessage)
            throws SOAPException {
        String value = null;
        Node nodeToRemove = null;
        NamedNodeMap attributeMap = xmlObject.getDomNode().getFirstChild().getAttributes();
        for (int i = 0; i < attributeMap.getLength(); i++) {
            Node node = attributeMap.item(i);
            if (node.getLocalName().equals(W3CConstants.AN_SCHEMA_LOCATION)) {
                value = node.getNodeValue();
                nodeToRemove = node;
            }
        }
        if (nodeToRemove != null) {
            attributeMap.removeNamedItem(nodeToRemove.getNodeName());
        }
        SOAPEnvelope envelope = soapResponseMessage.getSOAPPart().getEnvelope();
        StringBuilder string = new StringBuilder();
        if (soapResponseMessage.getSOAPPart().getEnvelope().getNamespaceURI()
                .equalsIgnoreCase(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE)) {
            string.append(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE + " http://schemas.xmlsoap.org/soap/envelope");
        } else if (soapResponseMessage.getSOAPPart().getEnvelope().getNamespaceURI()
                .equalsIgnoreCase(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE)) {
            string.append(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE + " http://www.w3.org/2003/05/soap-envelope");

        }
        if (value != null && !value.isEmpty()) {
            string.append(" ");
            string.append(value);
        }
        QName qName = new QName(W3CConstants.NS_XSI, W3CConstants.AN_SCHEMA_LOCATION, W3CConstants.NS_XSI_PREFIX);
        envelope.addAttribute(qName, string.toString());
        value = string.toString();
    }
}
