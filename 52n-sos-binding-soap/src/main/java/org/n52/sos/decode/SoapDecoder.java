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

package org.n52.sos.decode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;

import net.opengis.ows.x11.ExceptionType;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.service.Configurator;
import org.n52.sos.soap.SoapFault;
import org.n52.sos.soap.SoapHeader;
import org.n52.sos.soap.SoapHelper;
import org.n52.sos.soap.SoapRequest;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.W3cHelper;
import org.n52.sos.wsa.WsaConstants;
import org.n52.sos.wsa.WsaHeader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * class encapsulates decoding methods for SOAP elements.
 * 
 * @author Carsten Hollmann
 */
public class SoapDecoder implements IDecoder<SoapRequest, XmlObject> {

    /** the logger, used to log exceptions and additonaly information */
    private static Logger LOGGER = Logger.getLogger(SoapDecoder.class);

    private List<DecoderKeyType> decoderKeyTypes;

    /**
     * constructor
     */
    public SoapDecoder() {
        super();
        decoderKeyTypes = new ArrayList<DecoderKeyType>();
        decoderKeyTypes.add(new DecoderKeyType(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE));
        decoderKeyTypes.add(new DecoderKeyType(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE));
        StringBuilder builder = new StringBuilder();
        for (DecoderKeyType decoderKeyType : decoderKeyTypes) {
            builder.append(decoderKeyType.toString());
            builder.append(", ");
        }
        builder.delete(builder.lastIndexOf(", "), builder.length());
        LOGGER.debug("Decoder for the following keys initialized successfully: " + builder.toString() + "!");
    }

    @Override
    public List<DecoderKeyType> getDecoderKeyTypes() {
        return decoderKeyTypes;
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
                for (ExceptionType exceptionType : owse.getDocument().getExceptionReport().getExceptionArray()) {
                    for (String exceptionText : exceptionType.getExceptionTextArray()) {
                        faultString.append(exceptionText + "\n");
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
                soapRequest.setSoapBodyContent(XmlObject.Factory.parse(getSOAPBodyContent(soapMessageRequest)));
            } catch (SOAPException soape) {
                String exceptionText = "Error while parsing SOAPMessage header!";
                LOGGER.debug(exceptionText, soape);
                throw Util4Exceptions.createNoApplicableCodeException(soape, exceptionText);
            } catch (XmlException xmle) {
                String exceptionText = "Error while parsing SOAPMessage body content!";
                LOGGER.debug(exceptionText, xmle);
                throw Util4Exceptions.createNoApplicableCodeException(xmle, exceptionText);
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
            if (soapAction.equals("") || !soapAction.startsWith("SOAPAction:")) {
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
                soapRequest.setSoapBodyContent(XmlObject.Factory.parse(getSOAPBodyContent(soapMessageRequest)));
            } catch (SOAPException soape) {
                String exceptionText = "Error while parsing SOAPMessage header!";
                LOGGER.debug(exceptionText, soape);
                throw Util4Exceptions.createNoApplicableCodeException(soape, exceptionText);
            } catch (XmlException xmle) {
                String exceptionText = "Error while parsing SOAPMessage body content!";
                LOGGER.debug(exceptionText, xmle);
                throw Util4Exceptions.createNoApplicableCodeException(xmle, exceptionText);
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
    private String getSOAPBodyContent(SOAPMessage soapMessageRequest) throws OwsExceptionReport {
        try {
            Document bodyRequestDoc;
            bodyRequestDoc = soapMessageRequest.getSOAPBody().extractContentAsDocument();
            Element rootRequestDoc = bodyRequestDoc.getDocumentElement();
            return W3cHelper.nodeToXmlString(rootRequestDoc);
        } catch (SOAPException soape) {
            String exceptionText = "Error while parsing SOAPMessage body content!";
            LOGGER.debug(exceptionText, soape);
            throw Util4Exceptions.createNoApplicableCodeException(soape, exceptionText);
        } catch (OwsExceptionReport owse) {
            throw owse;
        }
    }

    private Map<String, SoapHeader> getSoapHeader(SOAPHeader soapHeader) {
        Map<String, SoapHeader> soapHeaders = new HashMap<String, SoapHeader>();
        Map<String, List<SOAPHeaderElement>> headerElementsMap = new HashMap<String, List<SOAPHeaderElement>>();
        Iterator<SOAPHeaderElement> headerElements = soapHeader.extractAllHeaderElements();
        while (headerElements.hasNext()) {
            SOAPHeaderElement element = (SOAPHeaderElement) headerElements.next();
            if (headerElementsMap.containsKey(element.getNamespaceURI())) {
                headerElementsMap.get(element.getNamespaceURI()).add(element);
            } else {
                List<SOAPHeaderElement> list = new ArrayList<SOAPHeaderElement>();
                list.add(element);
                headerElementsMap.put(element.getNamespaceURI(), list);
            }
        }
        for (String headerElementsNamespace : headerElementsMap.keySet()) {
            try {
                IDecoder decoder = Configurator.getInstance().getDecoder(headerElementsNamespace);
                SoapHeader headerElement = (SoapHeader) decoder.decode(headerElementsMap.get(headerElementsNamespace));
                soapHeaders.put(headerElementsNamespace, (SoapHeader) headerElement);
            } catch (OwsExceptionReport owse) {
                LOGGER.debug("Requested SOAPHeader element is not supported", owse);
            }
        }
        return soapHeaders;
    }

    private String checkSoapAction(String soapAction, Map<String, SoapHeader> soapHeader) {
        if ((soapAction != null && !soapAction.isEmpty())) {
            return soapAction;
        } else if (soapHeader != null && soapHeader.containsKey(WsaConstants.NS_WSA)){
                WsaHeader wsaHeaderRequest = (WsaHeader)soapHeader.get(WsaConstants.NS_WSA);
                return wsaHeaderRequest.getActionValue();
        }
        return null;
    }

}
