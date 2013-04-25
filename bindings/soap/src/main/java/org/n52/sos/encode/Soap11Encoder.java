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


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.exception.ows.concrete.UnsupportedEncoderInputException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.SoapHeader;
import org.n52.sos.soap.SoapHelper;
import org.n52.sos.soap.SoapResponse;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.N52XmlHelper;
import org.n52.sos.util.StringHelper;
import org.n52.sos.util.W3CConstants;
import org.n52.sos.wsa.WsaConstants;
import org.n52.sos.wsa.WsaHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class Soap11Encoder extends AbstractSoapEncoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(Soap11Encoder.class);

    public Soap11Encoder() {
        super(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE);
        LOGGER.debug("Encoder for the following keys initialized successfully: {}!",
                     StringHelper.join(", ", getEncoderKeyType()));
    }

    @Override
    public ServiceResponse encode(SoapResponse soapResponse,
                                  Map<HelperValues, String> additionalValues) throws OwsExceptionReport {
        if (soapResponse == null) {
            throw new UnsupportedEncoderInputException(this, soapResponse);
        }
        String soapVersion = soapResponse.getSoapVersion();
        SOAPMessage soapResponseMessage;
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
                    addSchemaLocationForExceptionToSOAPMessage(soapResponseMessage);
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
                        Encoder<Map<QName, String>, SoapHeader> encoder =
                                                                Configurator.getInstance().getCodingRepository()
                                .getEncoder(CodingHelper.getEncoderKey(namespace, header));
                        if (encoder != null) {
                            Map<QName, String> headerElements = encoder.encode(header);
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
            soapResponseMessage.setProperty(SOAPMessage.WRITE_XML_DECLARATION, String.valueOf(true));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            soapResponseMessage.writeTo(baos);
            boolean applicationZip = false;
            if (soapResponse.getSoapBodyContent() != null) {
                applicationZip = soapResponse.getSoapBodyContent().getApplyGzipCompression();
            }
            return new ServiceResponse(baos, SosConstants.CONTENT_TYPE_XML, applicationZip, true);
        } catch (SOAPException soape) {
            throw new NoApplicableCodeException().causedBy(soape)
                    .withMessage("Error while encoding SOAPMessage!");
        } catch (IOException ioe) {
            throw new NoApplicableCodeException().causedBy(ioe)
                    .withMessage("Error while encoding SOAPMessage!");
        }
    }

    private void addSchemaLocationForExceptionToSOAPMessage(SOAPMessage soapResponseMessage) throws SOAPException {
        SOAPEnvelope envelope = soapResponseMessage.getSOAPPart().getEnvelope();
        envelope.addNamespaceDeclaration(W3CConstants.NS_XSI_PREFIX, W3CConstants.NS_XSI);
        StringBuilder schemaLocation = new StringBuilder();
        schemaLocation.append(envelope.getNamespaceURI());
        schemaLocation.append(" ");
        schemaLocation.append(envelope.getNamespaceURI());
        schemaLocation.append(" ");
        schemaLocation.append(N52XmlHelper.getSchemaLocationForOWS110Exception());
        envelope.addAttribute(N52XmlHelper.getSchemaLocationQNameWithPrefix(), schemaLocation.toString());
    }
}
