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

package org.n52.sos.encode;

import java.util.Collections;
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
import org.n52.sos.exception.CodedException;
import org.n52.sos.exception.ows.OwsExceptionCode;
import org.n52.sos.exception.ows.concrete.XmlDecodingException;
import org.n52.sos.exception.sos.SosExceptionCode;
import org.n52.sos.exception.swes.SwesExceptionCode;
import org.n52.sos.ogc.ows.ExceptionCode;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosSoapConstants;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.soap.SoapConstants;
import org.n52.sos.soap.SoapFault;
import org.n52.sos.soap.SoapHelper;
import org.n52.sos.soap.SoapResponse;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.N52XmlHelper;
import org.n52.sos.util.W3CConstants;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public abstract class AbstractSoapEncoder implements Encoder<ServiceResponse, SoapResponse> {
    public static final String DEFAULT_FAULT_REASON = "A server exception was encountered.";
    public static final String MISSING_RESPONSE_DETAIL_TEXT = "Missing SOS response document!";
    public static final String MISSING_EXCEPTION_DETAIL_TEXT =
                               "Error while creating SOAPFault element from OWSException! OWSException is missing!";
    private final Set<EncoderKey> encoderKey;

    public AbstractSoapEncoder(String namespace) {
        this.encoderKey = CollectionHelper.<EncoderKey>set(new XmlEncoderKey(namespace, SoapResponse.class));
    }

    @Override
    public Set<EncoderKey> getEncoderKeyType() {
        return Collections.unmodifiableSet(encoderKey);
    }

    @Override
    public Map<SupportedTypeKey, Set<String>> getSupportedTypes() {
        return Collections.emptyMap();
    }

    @Override
    public Set<String> getConformanceClasses() {
        return Collections.emptySet();
    }

    @Override
    public void addNamespacePrefixToMap(Map<String, String> nameSpacePrefixMap) {
    }

    @Override
    public String getContentType() {
        return SoapConstants.CONTENT_TYPE;
    }

    @Override
    public ServiceResponse encode(SoapResponse response) throws OwsExceptionReport {
        return encode(response, null);
    }

    /**
     * Creates a SOAPBody element from SOS response
     *
     * @param soapResponseMessage SOAPBody element
     * @param sosResponse         SOS response
     *
     * @throws SOAPException        if an error occurs.
     * @throws XmlDecodingException
     */
    protected String createSOAPBody(SOAPMessage soapResponseMessage, ServiceResponse sosResponse, String actionURI)
            throws SOAPException, XmlDecodingException {
        
        if (sosResponse != null) {
            String xmlString = new String(sosResponse.getByteArray());
            try {
                XmlObject xmlObject = XmlObject.Factory.parse(xmlString);
                addAndRemoveSchemaLocationForSOAP(xmlObject, soapResponseMessage);
                // Document doc = parseSosResponseToDocument(xmlObject);
                // soapResponseMessage.getSOAPBody().addDocument(doc);
                soapResponseMessage.getSOAPBody().addDocument((Document) xmlObject.getDomNode());
                return actionURI;
            } catch (XmlException xmle) {
                throw new XmlDecodingException("SOAP Body", xmlString, xmle);
            }
        } else {
            SoapFault fault = new SoapFault();
            fault.setFaultCode(SOAPConstants.SOAP_RECEIVER_FAULT);
            fault.setFaultSubcode(new QName(OWSConstants.NS_OWS, OwsExceptionCode.NoApplicableCode.name(),
                                            OWSConstants.NS_OWS_PREFIX));
            fault.setFaultReason(DEFAULT_FAULT_REASON);
            fault.setLocale(Locale.ENGLISH);
            fault.setDetailText(MISSING_RESPONSE_DETAIL_TEXT);
            createSOAPFault(soapResponseMessage.getSOAPBody().addFault(), fault);
        }
        return null;
    }

    /**
     * Check SOS response for xsi:schemaLocation, remove attribute and add attribute to SOAP message
     *
     * @param soapResponseMessage
     * @param xmlObject
     *
     * @param SOS                 response
     * @param soapResponseMessage SOAP response message
     *
     * @throws SOAPException If an error occurs
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
        string.append(envelope.getNamespaceURI());
        string.append(" ");
        string.append(envelope.getNamespaceURI());
        // if (soapResponseMessage.getSOAPPart().getEnvelope().getNamespaceURI()
        // .equalsIgnoreCase(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE)) {
        // string.append(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE +
        // " http://schemas.xmlsoap.org/soap/envelope");
        // } else if
        // (soapResponseMessage.getSOAPPart().getEnvelope().getNamespaceURI()
        // .equalsIgnoreCase(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE)) {
        // string.append(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE +
        // " http://www.w3.org/2003/05/soap-envelope");
        //
        // }
        if (value != null && !value.isEmpty()) {
            string.append(" ");
            string.append(value);
        }
        envelope.addAttribute(N52XmlHelper.getSchemaLocationQNameWithPrefix(), string.toString());
    }

    /**
     * Creates a SOAPFault element from SOS internal fault
     *
     * @param fault     SOAPFault element
     * @param soapFault SOS internal fault
     *
     * @throws SOAPException if an error occurs.
     */
    protected void createSOAPFault(SOAPFault fault, SoapFault soapFault) throws SOAPException {
        fault.setFaultCode(soapFault.getFaultCode());
        fault.setFaultString(soapFault.getFaultReason(), soapFault.getLocale());
        if (soapFault.getDetailText() != null) {
            fault.addDetail().setTextContent(soapFault.getDetailText());
        }
    }

    /**
     * Creates a SOAPFault element from SOS exception
     *
     * @param soapFault          SOAPFault element
     * @param owsExceptionReport SOS exception
     *
     * @return SOAP action URI.
     *
     * @throws SOAPException if an error occurs.
     */
    protected String createSOAPFaultFromExceptionResponse(SOAPFault soapFault, OwsExceptionReport owsExceptionReport)
            throws SOAPException {
        // FIXME: check and fix support for ExceptionReport with multiple
        // exceptions!
        if (!owsExceptionReport.getExceptions().isEmpty()) {
            CodedException firstException = owsExceptionReport.getExceptions().iterator().next();
            if (soapFault.getNamespaceURI().equalsIgnoreCase(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE)) {
                QName qname = new QName(soapFault.getNamespaceURI(), "Client", soapFault.getPrefix());
                soapFault.setFaultCode(qname);
            } else {
                soapFault.setFaultCode(SOAPConstants.SOAP_SENDER_FAULT);
                if (firstException.getCode() != null) {
                    soapFault.appendFaultSubcode(new QName(OWSConstants.NS_OWS, firstException.getCode().toString(),
                                                           OWSConstants.NS_OWS_PREFIX));
                } else {
                    soapFault.appendFaultSubcode(OWSConstants.QN_NO_APPLICABLE_CODE);
                }
            }
            soapFault.addFaultReasonText(SoapHelper.getSoapFaultReasonText(firstException.getCode()), Locale.ENGLISH);
            Detail detail = soapFault.addDetail();
            for (CodedException exception : owsExceptionReport.getExceptions()) {
                createSOAPFaultDetail(detail, exception);
            }
            return getExceptionActionURI(firstException.getCode());
        } else {
            SoapFault fault = new SoapFault();
            fault.setFaultCode(SOAPConstants.SOAP_RECEIVER_FAULT);
            fault.setFaultSubcode(OWSConstants.QN_NO_APPLICABLE_CODE);
            fault.setFaultReason(DEFAULT_FAULT_REASON);
            fault.setLocale(Locale.ENGLISH);
            fault.setDetailText(MISSING_EXCEPTION_DETAIL_TEXT);
            createSOAPFault(soapFault, fault);
            return SosSoapConstants.RESP_ACTION_SOS;
        }
    }

    /**
     * Get SOAP action URI depending on Exception code
     *
     * @param exceptionCode Exception code
     *
     * @return SOAP action URI
     */
    protected String getExceptionActionURI(ExceptionCode exceptionCode) {
        if (exceptionCode instanceof OwsExceptionCode) {
            return SosSoapConstants.RESP_ACTION_OWS;
        } else if (exceptionCode instanceof SwesExceptionCode) {
            return SosSoapConstants.RESP_ACTION_SWES;
        } else if (exceptionCode instanceof SosExceptionCode) {
            return SosSoapConstants.RESP_ACTION_SOS;
        } else {
            return SosSoapConstants.RESP_ACTION_OWS;
        }
    }

    /**
     * Creates a SOAPDetail element from SOS exception document.
     *
     * @param detail SOAPDetail
     * @param exception SOS Exception document
     *
     * @throws SOAPException if an error occurs.
     */
    private void createSOAPFaultDetail(Detail detail, CodedException exception) throws SOAPException {
        SOAPElement exRep = detail.addChildElement(OWSConstants.QN_EXCEPTION);
        exRep.addNamespaceDeclaration(OWSConstants.NS_OWS_PREFIX, OWSConstants.NS_OWS);
        String code = exception.getCode().toString();
        String locator = exception.getLocator();
        StringBuilder exceptionText = new StringBuilder();
        exceptionText.append(exception.getMessage());
        exceptionText.append("\n");
        if (exception.getCause() != null) {
            exceptionText.append("\n[EXCEPTION]: \n");
            if (exception.getCause().getLocalizedMessage() != null
                && !exception.getCause().getLocalizedMessage().isEmpty()) {
                exceptionText.append(exception.getCause().getLocalizedMessage());
                exceptionText.append("\n");
            }
            if (exception.getCause().getMessage() != null && !exception.getCause().getMessage().isEmpty()) {
                exceptionText.append(exception.getCause().getMessage());
                exceptionText.append("\n");
            }
        }
        // exRep.addAttribute(new QName(OWSConstants.NS_OWS,
        // OWSConstants.EN_EXCEPTION_CODE, OWSConstants.NS_OWS_PREFIX),
        // code);
        exRep.addAttribute(new QName(OWSConstants.EN_EXCEPTION_CODE), code);
        if (locator != null && !locator.isEmpty()) {
            // exRep.addAttribute(new QName(OWSConstants.NS_OWS,
            // OWSConstants.EN_LOCATOR, OWSConstants.NS_OWS_PREFIX),
            // locator);
            exRep.addAttribute(new QName(OWSConstants.EN_LOCATOR), locator);
        }
        if (exceptionText.length() != 0) {
            SOAPElement execText = exRep.addChildElement(OWSConstants.QN_EXCEPTION_TEXT);
            execText.setTextContent(exceptionText.toString());
        }
    }
}
