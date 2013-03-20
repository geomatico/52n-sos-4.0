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
package org.n52.sos.soap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;

import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.ogc.ows.ExceptionCode;
import org.n52.sos.ogc.sos.SosConstants.Operations;
import org.n52.sos.ogc.sos.SosSoapConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;

/**
 * Utility class for SOAP requests
 * 
 */
public class SoapHelper {

    /** the logger, used to log exceptions and additonaly information */
    private static final Logger LOGGER = LoggerFactory.getLogger(SoapHelper.class);

    /**
     * Checks the HTTP-Header for action or SOAPAction elements.
     * 
     * @param request
     *            HTTP request
     * @return SOAP action element
     */
    public static String checkSoapHeader(HttpServletRequest request) {
        Enumeration<?> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerNameKey = (String) headerNames.nextElement();
            if (headerNameKey.equalsIgnoreCase("type")) {
                String type = request.getHeader(headerNameKey);
                String[] typeArray = type.split(";");
                for (String string : typeArray) {
                    if (string.startsWith("action")) {
                        String soapAction = string.replace("action=", "");
                        soapAction = soapAction.replace("\"", "");
                        soapAction = soapAction.trim();
                        return soapAction;
                    }
                }
            } else if (headerNameKey.equalsIgnoreCase("SOAPAction")) {
                return request.getHeader(headerNameKey);
            }
        }
        return null;
    }

    /**
     * Get text content from element by namespace.
     * 
     * @param soapHeader
     *            SOAPHeader element
     * @param namespaceURI
     *            Namespace URI
     * @param localName
     *            local name
     * @return Text content.
     */
    public static String getContentFromElement(SOAPHeader soapHeader, String namespaceURI, String localName) {
        String elementContent = null;
        NodeList nodes = soapHeader.getElementsByTagNameNS(namespaceURI, localName);
        for (int i = 0; i < nodes.getLength(); i++) {
            elementContent = nodes.item(i).getTextContent();
        }
        return elementContent;
    }

    /**
     * Creates a SOAP message for SOAP 1.2 or 1.1
     * 
     * @param version
     *            SOAP version
     * @return Version depending SOAP message
     * @throws SOAPException
     *             if an error occurs.
     */
    public static SOAPMessage getSoapMessageForProtocol(String soapVersion) throws SOAPException {
        return MessageFactory.newInstance(soapVersion).createMessage();
    }

    public static SOAPMessage getSoapMessageForProtocol(String soapVersion, InputStream inputStream)
            throws SOAPException, IOException {
        return MessageFactory.newInstance(soapVersion).createMessage(new MimeHeaders(), inputStream);
    }

    /**
     * Get the reason for a SOAP fault from Exception code
     * 
     * @param exceptionCode
     *            OWS exception code to get reason for.
     * @return Text for SOAP fault reason
     */
    public static String getSoapFaultReasonText(ExceptionCode exceptionCode) {
        if (exceptionCode != null && exceptionCode.getSoapFaultReason() != null) {
            return exceptionCode.getSoapFaultReason();
        } else {
            return OWSConstants.SOAP_REASON_UNKNOWN;
        }
    }

    public static String checkActionURIWithBodyContent(String soapAction, String operationName)
            throws OwsExceptionReport {
        if (soapAction != null && !soapAction.isEmpty()) {
            if (operationName.equals(Operations.GetCapabilities.name())
                    && soapAction.equals(SosSoapConstants.REQ_ACTION_GET_CAPABILITIES)) {
                LOGGER.debug("ActionURI and SOAPBody content are valid!");
                return SosSoapConstants.RESP_ACTION_GET_CAPABILITIES;
            } else if (operationName.equals(Operations.DescribeSensor.name())
                    && soapAction.equals(SosSoapConstants.REQ_ACTION_DESCRIBE_SENSOR)) {
                LOGGER.debug("ActionURI and SOAPBody content are valid!");
                return SosSoapConstants.RESP_ACTION_DESCRIBE_SENSOR;
            } else if (operationName.equals(Operations.GetObservation.name())
                    && soapAction.equals(SosSoapConstants.REQ_ACTION_GET_OBSERVATION)) {
                LOGGER.debug("ActionURI and SOAPBody content are valid!");
                return SosSoapConstants.RESP_ACTION_GET_OBSERVATION;
            } else {
                throw new NoApplicableCodeException()
                        .withMessage("Error while actionURI (%s) is not compatible with the SOAPBody content (%s request)!",
                                     soapAction, operationName);
            }
        }
        return null;
    }

}
