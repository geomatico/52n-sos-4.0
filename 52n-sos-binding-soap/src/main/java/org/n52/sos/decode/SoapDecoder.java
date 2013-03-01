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
package org.n52.sos.decode;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ogc.ows.OwsException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.soap.SoapFault;
import org.n52.sos.soap.SoapHeader;
import org.n52.sos.soap.SoapHelper;
import org.n52.sos.soap.SoapRequest;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.StringHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.W3cHelper;
import org.n52.sos.util.XmlOptionsHelper;
import org.n52.sos.wsa.WsaConstants;
import org.n52.sos.wsa.WsaHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * class encapsulates decoding methods for SOAP elements.
 * 
 * @author Carsten Hollmann
 */
public class SoapDecoder implements IDecoder<SoapRequest, XmlObject> {

    private static Logger LOGGER = LoggerFactory.getLogger(SoapDecoder.class);
    @SuppressWarnings("unchecked")
    private static final Set<DecoderKey> DECODER_KEYS = CollectionHelper.union(
        CodingHelper.decoderKeysForElements(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE, XmlObject.class),
        CodingHelper.decoderKeysForElements(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE, XmlObject.class)
    );

    public SoapDecoder() {
        LOGGER.debug("Decoder for the following keys initialized successfully: {}!", StringHelper.join(", ", DECODER_KEYS));
    }

    @Override
    public Set<DecoderKey> getDecoderKeyTypes() {
        return Collections.unmodifiableSet(DECODER_KEYS);
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
    public SoapRequest decode(XmlObject xmlObject) throws OwsExceptionReport {
        SoapRequest soapRequest = null;

        String reqNamespaceURI = xmlObject.getDomNode().getFirstChild().getNamespaceURI();
        try {
            // SOAP 1.2
            if (reqNamespaceURI.equals(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE)) {
                soapRequest = checkSOAP12Envelope(xmlObject);
            }
            // SOAP 1.1
            else if (reqNamespaceURI.equals(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE)) {
                soapRequest = checkSOAP11Envelope(xmlObject);
            }
            // Not known SOAP version
            else {
                soapRequest = new SoapRequest(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE, SOAPConstants.SOAP_1_1_PROTOCOL);
                SoapFault fault = new SoapFault();
                fault.setFaultCode(SOAPConstants.SOAP_VERSIONMISMATCH_FAULT);
                fault.setFaultReason("The SOAP version is unknown!");
                fault.setLocale(Locale.ENGLISH);
                fault.setDetailText("The requested SOAP versions is unknown!"
                        + " Valid versions are SOAP 1.1 with SOAPNamespace '" + SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE
                        + "' and SOAP 1.2 with SOAPNamespace '" + SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE + "'!");
                soapRequest.setSoapFault(fault);
            }
        } catch (OwsExceptionReport owse) {
            String requestString = xmlObject.xmlText();
            if (requestString.contains(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE)
                    || requestString.contains(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE)) {
                SoapFault fault = new SoapFault();
                if (requestString.contains(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE)) {
                    soapRequest =
                            new SoapRequest(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE, SOAPConstants.SOAP_1_1_PROTOCOL);
                    fault.setFaultCode(new QName(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE, "Client"));
                } else if (requestString.contains(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE)) {
                    soapRequest =
                            new SoapRequest(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE, SOAPConstants.SOAP_1_2_PROTOCOL);
                    fault.setFaultCode(SOAPConstants.SOAP_SENDER_FAULT);
                }
                fault.setLocale(Locale.ENGLISH);
                StringBuilder faultString = new StringBuilder();
                for (OwsException owsException : owse.getExceptions()) {
                    for (String exceptionText : owsException.getMessages()) {
                        faultString.append(exceptionText).append("\n");
                    }
                    faultString.append("\n");
                }
                fault.setFaultReason(faultString.toString());
                soapRequest.setSoapFault(fault);
            } else {
                throw owse;
            }
        }
        return soapRequest;
    }

    /**
     * Parses SOAP 1.2 Envelope to a SOS internal SOAP request.
     * 
     * @param httpRequest
     *            HTTP request
     * @param requestString
     *            Request as text representation
     * @return SOS internal SOAP request
     * @throws OwsExceptionReport
     *             if an error occurs.
     */
    private SoapRequest checkSOAP12Envelope(XmlObject doc) throws OwsExceptionReport {
        String soapVersion = SOAPConstants.SOAP_1_2_PROTOCOL;
        String soapNamespace = SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE;
        SoapRequest soapRequest = new SoapRequest(soapNamespace, soapVersion);
        String soapAction = "";
        try {
            SOAPMessage soapMessageRequest;
            try {
                soapMessageRequest =
                        SoapHelper.getSoapMessageForProtocol(SOAPConstants.SOAP_1_2_PROTOCOL, doc.newInputStream());
            } catch (IOException ioe) {
                String exceptionText = "Error while parsing SOAPMessage from request string!";
                LOGGER.debug(exceptionText, ioe);
                throw Util4Exceptions.createNoApplicableCodeException(ioe, exceptionText);
            } catch (SOAPException soape) {
                String exceptionText = "Error while parsing SOAPMessage from request string!";
                LOGGER.debug(exceptionText, soape);
                throw Util4Exceptions.createNoApplicableCodeException(soape, exceptionText);
            }
            try {
                if (soapMessageRequest.getSOAPHeader() != null) {
                    soapRequest.setSoapHeader(getSoapHeader(soapMessageRequest.getSOAPHeader()));
                }
                soapRequest.setAction(checkSoapAction(soapAction, soapRequest.getSoapHeader()));
                soapRequest.setSoapBodyContent(getSOAPBodyContent(soapMessageRequest));
            } catch (SOAPException soape) {
                String exceptionText = "Error while parsing SOAPMessage!";
                LOGGER.debug(exceptionText, soape);
                throw Util4Exceptions.createNoApplicableCodeException(soape, exceptionText);
            }
        } catch (OwsExceptionReport owse) {
            throw owse;
        }
        return soapRequest;
    }

    /**
     * Parses SOAP 1.1 Envelope to a SOS internal SOAP request.
     * 
     * @param httpRequest
     *            HTTP request
     * @param requestString
     *            Request as text representation
     * @return SOS internal SOAP request
     * @throws OwsExceptionReport
     *             if an error occurs.
     */
    private SoapRequest checkSOAP11Envelope(XmlObject doc) throws OwsExceptionReport {
        String soapVersion = SOAPConstants.SOAP_1_1_PROTOCOL;
        String soapNamespace = SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE;
        SoapRequest soapRequest = new SoapRequest(soapNamespace, soapVersion);
        String soapAction = "";

        try {
            SOAPMessage soapMessageRequest;
            try {
                soapMessageRequest =
                        SoapHelper.getSoapMessageForProtocol(SOAPConstants.SOAP_1_1_PROTOCOL, doc.newInputStream());
            } catch (IOException ioe) {
                String exceptionText = "Error while parsing SOAPMessage from request string!";
                LOGGER.debug(exceptionText, ioe);
                throw Util4Exceptions.createNoApplicableCodeException(ioe, exceptionText);
            } catch (SOAPException soape) {
                String exceptionText = "Error while parsing SOAPMessage from request string!";
                LOGGER.debug(exceptionText, soape);
                throw Util4Exceptions.createNoApplicableCodeException(soape, exceptionText);
            }
            // if SOAPAction is not spec conform, create SOAPFault
            if (soapAction.isEmpty() || !soapAction.startsWith("SOAPAction:")) {
                SoapFault fault = new SoapFault();
                fault.setFaultCode(new QName(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE, "Client"));
                fault.setFaultReason("The SOAPAction parameter in the HTTP-Header is missing or not valid!");
                fault.setLocale(Locale.ENGLISH);
                soapRequest.setSoapFault(fault);
                soapRequest.setSoapFault(fault);
            }
            // trim SOAPAction value
            else {
                soapAction = soapAction.replace("\"", "");
                soapAction = soapAction.replace(" ", "");
                soapAction = soapAction.replace("SOAPAction:", "");
                soapAction = soapAction.trim();
            }
            try {
                if (soapMessageRequest.getSOAPHeader() != null) {
                    soapRequest.setSoapHeader(getSoapHeader(soapMessageRequest.getSOAPHeader()));
                }
                soapRequest.setAction(checkSoapAction(soapAction, soapRequest.getSoapHeader()));
                soapRequest.setSoapBodyContent(getSOAPBodyContent(soapMessageRequest));
            } catch (SOAPException soape) {
                String exceptionText = "Error while parsing SOAPMessage!";
                LOGGER.debug(exceptionText, soape);
                throw Util4Exceptions.createNoApplicableCodeException(soape, exceptionText);
            }
        } catch (OwsExceptionReport owse) {
            throw owse;
        }
        return soapRequest;
    }

    /**
     * Parses the SOAPBody content to a text representation
     * 
     * @param soapMessageRequest
     *            SOAP message
     * @return SOAPBody content as text
     * @throws OwsExceptionReport
     *             if an error occurs.
     */
    private XmlObject getSOAPBodyContent(SOAPMessage soapMessageRequest) throws OwsExceptionReport {
        try {
            Document bodyRequestDoc = soapMessageRequest.getSOAPBody().extractContentAsDocument();
//            Element rootRequestDoc = bodyRequestDoc.getDocumentElement();
//            return W3cHelper.nodeToXmlString(rootRequestDoc);
            return XmlObject.Factory.parse(W3cHelper.nodeToXmlString(bodyRequestDoc.getDocumentElement()), XmlOptionsHelper.getInstance().getXmlOptions());
//            return XmlObject.Factory.parse(bodyRequestDoc.getFirstChild(), XmlOptionsHelper.getInstance().getXmlOptions());
        } catch (SOAPException soape) {
            String exceptionText = "Error while parsing SOAPMessage body content!";
            LOGGER.debug(exceptionText, soape);
            throw Util4Exceptions.createInvalidRequestException(exceptionText, soape);
        } catch (XmlException xmle) {
            String exceptionText = "Error while parsing SOAPMessage body content!";
            LOGGER.debug(exceptionText, xmle);
            throw Util4Exceptions.createInvalidRequestException(exceptionText, xmle);
        }
    }

    private Map<String, SoapHeader> getSoapHeader(SOAPHeader soapHeader) {
        Map<String, List<SOAPHeaderElement>> headerElementsMap = new HashMap<String, List<SOAPHeaderElement>>();
        Iterator<?> headerElements = soapHeader.extractAllHeaderElements();
        while (headerElements.hasNext()) {
            SOAPHeaderElement element = (SOAPHeaderElement) headerElements.next();
            if (headerElementsMap.containsKey(element.getNamespaceURI())) {
                headerElementsMap.get(element.getNamespaceURI()).add(element);
            } else {
                List<SOAPHeaderElement> list = new LinkedList<SOAPHeaderElement>();
                list.add(element);
                headerElementsMap.put(element.getNamespaceURI(), list);
            }
        }
        Map<String, SoapHeader> soapHeaders = new HashMap<String, SoapHeader>();
        for (String headerElementsNamespace : headerElementsMap.keySet()) {
            try {
                IDecoder<?, List<SOAPHeaderElement>> decoder = Configurator.getInstance().getCodingRepository()
                        .getDecoder(new NamespaceDecoderKey(headerElementsNamespace, SOAPHeaderElement.class));
                if (decoder != null) {
                    Object headerElement = decoder.decode(headerElementsMap.get(headerElementsNamespace));
                    if (headerElement != null && headerElement instanceof SoapHeader) {
                        soapHeaders.put(headerElementsNamespace, (SoapHeader) headerElement);
                    }
                } else {
                    LOGGER.info("The SOAP-Header elements for namespace '{}' are not supported by this server!", headerElementsNamespace);
                }
            } catch (OwsExceptionReport owse) {
                LOGGER.debug("Requested SOAPHeader element is not supported", owse);
            }
        }
        return soapHeaders;
    }

    private String checkSoapAction(String soapAction, Map<String, SoapHeader> soapHeader) {
        if ((soapAction != null && !soapAction.isEmpty())) {
            return soapAction;
        } else if (soapHeader != null && soapHeader.containsKey(WsaConstants.NS_WSA)) {
            WsaHeader wsaHeaderRequest = (WsaHeader) soapHeader.get(WsaConstants.NS_WSA);
            return wsaHeaderRequest.getActionValue();
        }
        return null;
    }
}
