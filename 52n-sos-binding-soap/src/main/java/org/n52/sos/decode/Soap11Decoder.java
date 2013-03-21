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
import java.util.Locale;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.soap.SoapFault;
import org.n52.sos.soap.SoapHelper;
import org.n52.sos.soap.SoapRequest;
import org.n52.sos.util.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class Soap11Decoder extends AbstractSoapDecoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(Soap11Decoder.class);

    public Soap11Decoder() {
        super(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE);
        LOGGER.debug("Decoder for the following keys initialized successfully: {}!",
                     StringHelper.join(", ", getDecoderKeyTypes()));
    }

    /**
     * Parses SOAP 1.1 Envelope to a SOS internal SOAP request.
     *
     * @param doc Request as xml representation
     *
     * @return SOS internal SOAP request
     *
     * @throws OwsExceptionReport * if an error occurs.
     */
    @Override
    protected SoapRequest createEnvelope(XmlObject doc) throws OwsExceptionReport {
        SoapRequest soapRequest = new SoapRequest(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE,
                                                  SOAPConstants.SOAP_1_1_PROTOCOL);
        String soapAction = "";

        try {
            SOAPMessage soapMessageRequest;
            try {
                soapMessageRequest =
                SoapHelper.getSoapMessageForProtocol(SOAPConstants.SOAP_1_1_PROTOCOL, doc.newInputStream());
            } catch (IOException ioe) {
                throw new NoApplicableCodeException().causedBy(ioe)
                        .withMessage("Error while parsing SOAPMessage from request string!");
            } catch (SOAPException soape) {
                throw new NoApplicableCodeException().causedBy(soape)
                        .withMessage("Error while parsing SOAPMessage from request string!");
            }
            // if SOAPAction is not spec conform, create SOAPFault
            if (soapAction.isEmpty() || !soapAction.startsWith("SOAPAction:")) {
                SoapFault fault = new SoapFault();
                fault.setFaultCode(new QName(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE, "Client"));
                fault.setFaultReason("The SOAPAction parameter in the HTTP-Header is missing or not valid!");
                fault.setLocale(Locale.ENGLISH);
                soapRequest.setSoapFault(fault);
                soapRequest.setSoapFault(fault);
            } // trim SOAPAction value
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
                throw new NoApplicableCodeException().causedBy(soape)
                        .withMessage("Error while parsing SOAPMessage!");
            }
        } catch (OwsExceptionReport owse) {
            throw owse;
        }
        return soapRequest;
    }

    @Override
    protected SoapRequest createFault(OwsExceptionReport owse) {
        SoapFault fault = new SoapFault();
        fault.setFaultCode(new QName(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE, "Client"));
        fault.setLocale(Locale.ENGLISH);
        fault.setFaultReason(owse.getMessage());
        SoapRequest r = new SoapRequest(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE, SOAPConstants.SOAP_1_1_PROTOCOL);
        r.setSoapFault(fault);
        return r;
    }
}
